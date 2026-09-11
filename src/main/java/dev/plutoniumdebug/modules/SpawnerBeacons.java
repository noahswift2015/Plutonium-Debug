package dev.plutoniumdebug.modules;

import dev.plutoniumdebug.PlutoniumDebug;
import dev.plutoniumdebug.util.LoadedWorld;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedHashSet;
import java.util.Set;

/** Draws a red beacon column through each nearby spawner already loaded by the client. */
public final class SpawnerBeacons extends Module {
    private static final Color FILL = new Color(255, 0, 0, 35);
    private static final Color OUTLINE = new Color(255, 40, 40, 220);

    private final Setting<Integer> radius = settings.getDefaultGroup().add(new IntSetting.Builder()
        .name("radius")
        .description("Horizontal and vertical radius used to find already-loaded spawners.")
        .defaultValue(32)
        .range(4, 64)
        .build()
    );

    private final Set<BlockPos> spawners = new LinkedHashSet<>();

    public SpawnerBeacons() {
        super(
            PlutoniumDebug.CATEGORY,
            "spawner-beacons",
            "Draws red world-height beacon columns through nearby loaded spawners."
        );
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!LoadedWorld.ready() || mc.player.age % 20 != 0) return;

        spawners.clear();
        BlockPos center = mc.player.getBlockPos();
        int r = radius.get();

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = center.add(x, y, z);
                    if (LoadedWorld.isLoaded(mc.world, pos) && mc.world.getBlockState(pos).isOf(Blocks.SPAWNER)) {
                        spawners.add(pos.toImmutable());
                    }
                }
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        int worldBottom = mc.world.getBottomY();
        int worldTop = worldBottom + mc.world.getHeight();

        for (BlockPos pos : spawners) {
            event.renderer.box(
                pos.getX() + 0.35,
                worldBottom,
                pos.getZ() + 0.35,
                pos.getX() + 0.65,
                worldTop,
                pos.getZ() + 0.65,
                FILL,
                OUTLINE,
                ShapeMode.Both,
                0
            );
        }
    }
}
