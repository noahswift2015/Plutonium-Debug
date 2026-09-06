package dev.plutoniumdebug.modules;

import dev.plutoniumdebug.PlutoniumDebug;
import dev.plutoniumdebug.util.LoadedWorld;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.shape.ShapeMode;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import java.util.LinkedHashSet;
import java.util.Set;

/** ESP for selected, placed-looking blocks in visible loaded chunks. */
public final class AdvancedBlockEsp extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Integer> radius = sgGeneral.add(new IntSetting.Builder().name("radius").description("Scan radius; smaller is gentler on the client.").defaultValue(24).range(4, 64).build());
    private final Set<BlockPos> findings = new LinkedHashSet<>();
    private static final Set<Block> INTERESTING = Set.of(Blocks.BOOKSHELF, Blocks.CHISELED_BOOKSHELF, Blocks.NOTE_BLOCK, Blocks.CRAFTER, Blocks.LOOM, Blocks.CARTOGRAPHY_TABLE, Blocks.ENCHANTING_TABLE, Blocks.BREWING_STAND, Blocks.AMETHYST_CLUSTER, Blocks.BUDDING_AMETHYST);

    public AdvancedBlockEsp() { super(PlutoniumDebug.CATEGORY, "advanced-block-esp", "Marks configured base-like blocks that are already loaded."); }

    @EventHandler private void onTick(TickEvent.Post event) {
        if (!LoadedWorld.ready() || mc.player.age % 20 != 0) return;
        findings.clear();
        BlockPos center = mc.player.getBlockPos(); int r = radius.get();
        for (int x = -r; x <= r; x++) for (int y = -r; y <= r; y++) for (int z = -r; z <= r; z++) {
            BlockPos pos = center.add(x, y, z);
            if (LoadedWorld.isLoaded(mc.world, pos) && INTERESTING.contains(mc.world.getBlockState(pos).getBlock())) findings.add(pos.toImmutable());
        }
    }
    @EventHandler private void onRender(Render3DEvent event) {
        for (BlockPos pos : findings) event.renderer.box(pos, new Color(210, 66, 66, 35), new Color(255, 80, 80), ShapeMode.Both, 0);
    }
}
