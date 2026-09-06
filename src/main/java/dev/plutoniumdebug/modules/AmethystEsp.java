package dev.plutoniumdebug.modules;

import dev.plutoniumdebug.PlutoniumDebug;
import dev.plutoniumdebug.util.LoadedWorld;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import java.util.LinkedHashSet;
import java.util.Set;

/** Highlights client-visible amethyst at or below a configurable Y level. */
public final class AmethystEsp extends Module {
    private final Setting<Integer> maximumY = settings.getDefaultGroup().add(new IntSetting.Builder().name("maximum-y").description("Only inspect loaded blocks at or below this height.").defaultValue(0).range(-64, 319).build());
    private final Set<BlockPos> clusters = new LinkedHashSet<>();
    public AmethystEsp() { super(PlutoniumDebug.CATEGORY, "amethyst-esp", "Highlights loaded amethyst clusters below your chosen level."); }
    @EventHandler private void onTick(TickEvent.Post event) {
        if (!LoadedWorld.ready() || mc.player.age % 20 != 0) return;
        clusters.clear(); BlockPos c = mc.player.getBlockPos();
        for (int x=-24; x<=24; x++) for (int y=-24; y<=24; y++) for (int z=-24; z<=24; z++) {
            BlockPos p = c.add(x, y, z);
            if (p.getY() <= maximumY.get() && LoadedWorld.isLoaded(mc.world, p) && (mc.world.getBlockState(p).isOf(Blocks.AMETHYST_CLUSTER) || mc.world.getBlockState(p).isOf(Blocks.BUDDING_AMETHYST))) clusters.add(p.toImmutable());
        }
    }
    @EventHandler private void onRender(Render3DEvent event) { for (BlockPos p : clusters) event.renderer.box(p, new Color(170, 90, 255, 35), new Color(205, 130, 255), ShapeMode.Both, 0); }
}
