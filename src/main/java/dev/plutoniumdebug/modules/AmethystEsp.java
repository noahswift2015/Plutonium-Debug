package dev.plutoniumdebug.modules;

import dev.plutoniumdebug.PlutoniumDebug;
import dev.plutoniumdebug.util.LoadedWorld;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Highlights client-visible amethyst at or below a configurable Y level. */
public final class AmethystEsp extends Module {
    private final Setting<Integer> maximumY = settings.getDefaultGroup().add(new IntSetting.Builder()
        .name("maximum-y")
        .description("Only inspect loaded blocks at or below this height.")
        .defaultValue(0)
        .range(-64, 319)
        .build()
    );

    private final Setting<Boolean> tracers = settings.getDefaultGroup().add(new BoolSetting.Builder()
        .name("tracers")
        .description("Draw red lines from you to detected amethyst.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> suspiciousChunkThreshold = settings.getDefaultGroup().add(new IntSetting.Builder()
        .name("suspicious-chunk-threshold")
        .description("A chunk becomes pink when it contains at least this many detected amethyst blocks.")
        .defaultValue(3)
        .range(1, 64)
        .build()
    );

    private final Set<BlockPos> clusters = new LinkedHashSet<>();
    private final Map<ChunkPos, Integer> chunkCounts = new LinkedHashMap<>();

    public AmethystEsp() {
        super(PlutoniumDebug.CATEGORY, "amethyst-esp", "Highlights loaded amethyst clusters below your chosen level.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!LoadedWorld.ready() || mc.player.age % 20 != 0) return;

        clusters.clear();
        chunkCounts.clear();

        BlockPos center = mc.player.getBlockPos();

        for (int x = -24; x <= 24; x++) {
            for (int y = -24; y <= 24; y++) {
                for (int z = -24; z <= 24; z++) {
                    BlockPos pos = center.add(x, y, z);

                    if (pos.getY() > maximumY.get() || !LoadedWorld.isLoaded(mc.world, pos)) continue;

                    if (mc.world.getBlockState(pos).isOf(Blocks.AMETHYST_CLUSTER)
                        || mc.world.getBlockState(pos).isOf(Blocks.BUDDING_AMETHYST)) {
                        clusters.add(pos.toImmutable());
                        ChunkPos chunk = new ChunkPos(pos);
                        chunkCounts.merge(chunk, 1, Integer::sum);
                    }
                }
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        Color amethystSide = new Color(170, 90, 255, 35);
        Color amethystLine = new Color(205, 130, 255);
        Color tracerColor = new Color(255, 40, 40);
        Color suspiciousSide = new Color(255, 60, 190, 25);
        Color suspiciousLine = new Color(255, 90, 210);

        for (BlockPos pos : clusters) {
            event.renderer.box(pos, amethystSide, amethystLine, ShapeMode.Both, 0);

            if (tracers.get()) {
                event.renderer.line(
                    mc.player.getX(),
                    mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()),
                    mc.player.getZ(),
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    tracerColor
                );
            }
        }

        for (Map.Entry<ChunkPos, Integer> entry : chunkCounts.entrySet()) {
            if (entry.getValue() < suspiciousChunkThreshold.get()) continue;

            ChunkPos chunk = entry.getKey();

            event.renderer.box(
                chunk.getStartX(),
                mc.world.getBottomY(),
                chunk.getStartZ(),
                chunk.getEndX() + 1,
                maximumY.get() + 1,
                chunk.getEndZ() + 1,
                suspiciousSide,
                suspiciousLine,
                ShapeMode.Both,
                0
            );
        }
    }
}
