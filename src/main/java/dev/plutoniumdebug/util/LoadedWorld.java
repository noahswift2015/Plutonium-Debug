package dev.plutoniumdebug.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Centralizes the addon safety boundary: never force-load, probe, or infer unloaded chunks. */
public final class LoadedWorld {
    private LoadedWorld() {}

    public static boolean isLoaded(World world, BlockPos pos) {
        return world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public static boolean ready() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.player != null && client.world != null;
    }
}
