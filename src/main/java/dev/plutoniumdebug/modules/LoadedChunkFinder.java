package dev.plutoniumdebug.modules;

import dev.plutoniumdebug.PlutoniumDebug;
import dev.plutoniumdebug.util.LoadedWorld;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.CaveVines;
import net.minecraft.block.KelpBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;

import java.util.LinkedHashSet;
import java.util.Set;

/** Marks loaded chunks containing enabled suspicious block types. */
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

    private final Setting<Boolean> amethyst = sgGeneral.add(new BoolSetting.Builder()
        .name("amethyst")
        .description("Mark chunks containing amethyst clusters or budding amethyst.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> kelp = sgGeneral.add(new BoolSetting.Builder()
        .name("kelp")
        .description("Mark chunks containing kelp.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> rotatedDeepslate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotated-deepslate")
        .description("Mark chunks containing deepslate rotated sideways.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> cocoa = sgGeneral.add(new BoolSetting.Builder()
        .name("cocoa")
        .description("Mark chunks containing cocoa.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> caveVines = sgGeneral.add(new BoolSetting.Builder()
        .name("cave-vines")
        .description("Mark chunks containing cave vines.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> vines = sgGeneral.add(new BoolSetting.Builder()
        .name("vines")
        .description("Mark chunks containing regular vines.")
        .defaultValue(true)
        .build()
    );

    private final Set<ChunkPos> suspiciousChunks = new LinkedHashSet<>();
    private int cooldown;

    public LoadedChunkFinder() {
        super(
            PlutoniumDebug.CATEGORY,
            "sus-chunk-finder",
            "Marks loaded chunks containing selected suspicious blocks."
        );
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!LoadedWorld.ready() || cooldown-- > 0) return;

        suspiciousChunks.clear();

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

                    if (isSuspicious(mc.world.getBlockState(pos))) {
                        suspiciousChunks.add(new ChunkPos(pos));
                    }
                }
            }
        }

        cooldown = 20;
    }

    private boolean isSuspicious(BlockState state) {
        boolean hasAmethyst = amethyst.get()
            && (state.isOf(Blocks.AMETHYST_CLUSTER) || state.isOf(Blocks.BUDDING_AMETHYST));

        boolean hasKelp = kelp.get() && state.getBlock() instanceof KelpBlock;

        boolean hasRotatedDeepslate = rotatedDeepslate.get()
            && state.isOf(Blocks.DEEPSLATE)
            && state.contains(Properties.AXIS)
            && state.get(Properties.AXIS) != Direction.Axis.Y;

        boolean hasCocoa = cocoa.get() && state.isOf(Blocks.COCOA);

        boolean hasCaveVines = caveVines.get() && state.getBlock() instanceof CaveVines;

        boolean hasVines = vines.get() && state.isOf(Blocks.VINE);

        return hasAmethyst || hasKelp || hasRotatedDeepslate
            || hasCocoa || hasCaveVines || hasVines;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        Color fill = new Color(255, 0, 0, 20);
        Color outline = new Color(255, 40, 40);

        for (ChunkPos chunk : suspiciousChunks) {
            event.renderer.box(
                chunk.getStartX(),
                mc.world.getBottomY(),
                chunk.getStartZ(),
                chunk.getEndX() + 1,
                mc.world.getTopY(),
                chunk.getEndZ() + 1,
                fill,
                outline,
                ShapeMode.Both,
                0
            );
        }
    }
}
