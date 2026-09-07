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

/** Identifies mob spawners delivered to the client; it does not inspect hidden terrain. */
public final class SpawnerNametags extends Module {
    private final Setting<Integer> radius = settings.getDefaultGroup().add(
        new IntSetting.Builder()
            .name("radius")
            .description("Radius of the loaded-block scan.")
            .defaultValue(32)
            .range(4, 64)
            .build()
    );

    private final Set<BlockPos> spawners = new LinkedHashSet<>();

    public SpawnerNametags() {
        super(
            PlutoniumDebug.CATEGORY,
            "spawner-nametags",
            "Marks loaded mob spawners without reading hidden terrain."
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

                    if (
                        LoadedWorld.isLoaded(mc.world, pos)
                        && mc.world.getBlockState(pos).isOf(Blocks.SPAWNER)
                    ) {
                        spawners.add(pos.toImmutable());
                    }
                }
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        for (BlockPos pos : spawners) {
            event.renderer.box(
                pos,
                new Color(255, 210, 60, 35),
                new Color(255, 210, 60),
                ShapeMode.Both,
                0
            );
        }
    }
}
