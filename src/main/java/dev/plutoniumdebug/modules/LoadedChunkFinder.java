package dev.plutoniumdebug.modules;

import dev.plutoniumdebug.PlutoniumDebug;
import dev.plutoniumdebug.util.LoadedWorld;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.CaveVines;
import net.minecraft.block.CropBlock;
import net.minecraft.block.KelpBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

/** Reports observable mature crops in chunks the client already has. */
public final class LoadedChunkFinder extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Integer> radius = sgGeneral.add(new IntSetting.Builder()
        .name("radius")
        .description("Horizontal scan radius in loaded blocks.")
        .defaultValue(32)
        .min(8)
        .sliderMax(96)
        .build()
    );
    private final Setting<Boolean> crops = sgGeneral.add(new BoolSetting.Builder()
        .name("mature-crops")
        .description("Report mature crop and wart blocks.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> kelp = sgGeneral.add(new BoolSetting.Builder()
        .name("kelp")
        .description("Report visible kelp and cave vines.")
        .defaultValue(true)
        .build()
    );

    private BlockPos lastReport;
    private int cooldown;

    public LoadedChunkFinder() {
        super(
            PlutoniumDebug.CATEGORY,
            "sus-chunk-finder",
            "Finds observable plant activity in already-loaded chunks."
        );
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!LoadedWorld.ready() || cooldown-- > 0) return;

        BlockPos origin = mc.player.getBlockPos();
        int r = radius.get();

        for (int x = -r; x <= r; x += 2) {
            for (int z = -r; z <= r; z += 2) {
                int worldX = origin.getX() + x;
                int worldZ = origin.getZ() + z;
                int topY = mc.world.getTopY(
                    Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                    worldX,
                    worldZ
                );

                for (int y = mc.world.getBottomY(); y < topY; y += 2) {
                    BlockPos pos = new BlockPos(worldX, y, worldZ);

                    if (!LoadedWorld.isLoaded(mc.world, pos)) continue;

                    BlockState state = mc.world.getBlockState(pos);
                    boolean mature = crops.get()
                        && (
                            (state.getBlock() instanceof CropBlock crop && crop.isMature(state))
                            || (
                                state.getBlock() instanceof NetherWartBlock
                                && state.get(NetherWartBlock.AGE) == 3
                            )
                        );
                    boolean aquatic = kelp.get()
                        && (
                            state.getBlock() instanceof KelpBlock
                            || state.getBlock() instanceof CaveVines
                        );

                    if ((mature || aquatic) && !pos.equals(lastReport)) {
                        info(
                            "Observed %s at %d, %d, %d (loaded chunk only).",
                            state.getBlock().getName().getString(),
                            pos.getX(),
                            pos.getY(),
                            pos.getZ()
                        );
                        lastReport = pos;
                        cooldown = 100;
                        return;
                    }
                }
            }
        }

        cooldown = 20;
    }
}
