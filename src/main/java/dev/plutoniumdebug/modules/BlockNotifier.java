package dev.plutoniumdebug.modules;

import dev.plutoniumdebug.PlutoniumDebug;
import dev.plutoniumdebug.util.LoadedWorld;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import java.util.List;

/** A rate-limited notifier for user-selected, client-visible blocks. */
public final class BlockNotifier extends Module {
    private final Setting<List<Block>> blocks = settings.getDefaultGroup().add(new BlockListSetting.Builder().name("blocks").description("Blocks to report when first seen nearby.").defaultValue(Blocks.CHEST, Blocks.ENDER_CHEST, Blocks.SPAWNER).build());
    private final Setting<Integer> radius = settings.getDefaultGroup().add(new IntSetting.Builder().name("radius").description("Search radius in loaded terrain.").defaultValue(16).range(2, 48).build());
    private BlockPos last; private int cooldown;
    public BlockNotifier() { super(PlutoniumDebug.CATEGORY, "block-notifier", "Notifies you about selected blocks in already-loaded chunks."); }
    @EventHandler private void onTick(TickEvent.Post event) {
        if (!LoadedWorld.ready() || cooldown-- > 0) return;
        BlockPos c=mc.player.getBlockPos(); int r=radius.get();
        for(int x=-r;x<=r;x++) for(int y=-r;y<=r;y++) for(int z=-r;z<=r;z++) { BlockPos p=c.add(x,y,z); if(LoadedWorld.isLoaded(mc.world,p) && blocks.get().contains(mc.world.getBlockState(p).getBlock()) && !p.equals(last)) { info("%s at %d, %d, %d", mc.world.getBlockState(p).getBlock().getName().getString(),p.getX(),p.getY(),p.getZ()); last=p.toImmutable(); cooldown=80; return; } }
        cooldown=10;
    }
}
