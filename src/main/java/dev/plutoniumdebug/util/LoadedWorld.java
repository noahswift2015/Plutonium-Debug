package dev.plutoniumdebug.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.Heightmap;

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

    /** Returns a Y level immediately above the highest terrain point in a loaded chunk. */
    public static int getChunkSurfaceY(World world, ChunkPos chunk) {
        int highestY = world.getBottomY();

        for (int x = chunk.getStartX(); x <= chunk.getEndX(); x++) {
            for (int z = chunk.getStartZ(); z <= chunk.getEndZ(); z++) {
                highestY = Math.max(
                    highestY,
                    world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z)
                );
            }
        }

        return highestY;
    }
}
