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
import net.minecraft.util.math.ChunkPos;

import java.util.LinkedHashMap;
import java.util.Map;

/** Highlights client-visible amethyst at or below a configurable Y level. */
public final class AmethystEsp extends Module {
    private final Setting<Integer> maximumY = settings.getDefaultGroup().add(new IntSetting.Builder()
        .name("maximum-y")
        .description("Only inspect loaded blocks at or below this height.")
        .defaultValue(0)
        .range(-64, 319)
        .build()
    );

    private final Setting<Integer> suspiciousChunkThreshold = settings.getDefaultGroup().add(new IntSetting.Builder()
        .name("suspicious-chunk-threshold")
        .description("A chunk becomes pink when it contains at least this many detected amethyst blocks.")
        .defaultValue(3)
        .range(1, 64)
        .build()
    );

    private final Map<ChunkPos, Integer> chunkCounts = new LinkedHashMap<>();

    public AmethystEsp() {
        super(PlutoniumDebug.CATEGORY, "amethyst-esp", "Highlights loaded amethyst clusters below your chosen level.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!LoadedWorld.ready() || mc.player.age % 20 != 0) return;

        chunkCounts.clear();

        BlockPos center = mc.player.getBlockPos();

        for (int x = -24; x <= 24; x++) {
            for (int y = -24; y <= 24; y++) {
                for (int z = -24; z <= 24; z++) {
                    BlockPos pos = center.add(x, y, z);

                    if (pos.getY() > maximumY.get() || !LoadedWorld.isLoaded(mc.world, pos)) continue;

                    if (mc.world.getBlockState(pos).isOf(Blocks.AMETHYST_CLUSTER)
                        || mc.world.getBlockState(pos).isOf(Blocks.BUDDING_AMETHYST)) {
                        ChunkPos chunk = new ChunkPos(pos);
                        chunkCounts.merge(chunk, 1, Integer::sum);
                    }
                }
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        Color suspiciousSide = new Color(255, 60, 190, 45);
        Color suspiciousLine = new Color(255, 90, 210);

        for (Map.Entry<ChunkPos, Integer> entry : chunkCounts.entrySet()) {
            if (entry.getValue() < suspiciousChunkThreshold.get()) continue;

            ChunkPos chunk = entry.getKey();
            double surfaceY = LoadedWorld.getChunkSurfaceY(mc.world, chunk);

            event.renderer.box(
                chunk.getStartX(),
                surfaceY + 0.05,
                chunk.getStartZ(),
                chunk.getEndX() + 1,
                surfaceY + 0.25,
                chunk.getEndZ() + 1,
                suspiciousSide,
                suspiciousLine,
                ShapeMode.Both,
                0
            );
        }
    }
}
