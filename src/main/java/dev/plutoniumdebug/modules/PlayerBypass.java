package dev.plutoniumdebug.modules;

import dev.plutoniumdebug.PlutoniumDebug;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Cinematically highlights chunks containing loaded players via raw server packet tracking. */
public final class PlayerBypass extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> minimumY = sgGeneral.add(new IntSetting.Builder()
        .name("minimum-y")
        .description("Only mark chunks containing players at or above this height.")
        .defaultValue(-64)
        .range(-64, 320)
        .build()
    );

    private final Setting<Integer> maximumY = sgGeneral.add(new IntSetting.Builder()
        .name("maximum-y")
        .description("Only mark chunks containing players at or below this height.")
        .defaultValue(128)
        .range(-64, 320)
        .build()
    );

    private final Setting<SettingColor> fillColor = sgGeneral.add(new ColorSetting.Builder()
        .name("fill-color")
        .description("The translucent color inside marked player chunks.")
        .defaultValue(new SettingColor(60, 170, 255, 20))
        .build()
    );

    private final Setting<SettingColor> outlineColor = sgGeneral.add(new ColorSetting.Builder()
        .name("outline-color")
        .description("The outline color for marked player chunks.")
        .defaultValue(new SettingColor(80, 210, 255, 255))
        .build()
    );

    // Track tracked entity ID -> Current Position (Vec3d)
    private final Map<Integer, Vec3d> trackedPlayers = new HashMap<>();
    private final Set<ChunkPos> playerChunks = new LinkedHashSet<>();

    public PlayerBypass() {
        super(
            PlutoniumDebug.CATEGORY,
            "player-bypass",
            "Cinematically highlights loaded chunks containing players."
        );
    }

    @Override
    public void onActivate() {
        trackedPlayers.clear();
        playerChunks.clear();
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (mc.world == null || mc.player == null) return;

        // 1. Detect player entity spawn packet
        if (event.packet instanceof EntitySpawnS2CPacket packet) {
            if (packet.getEntityType() == EntityType.PLAYER) {
                // Exclude self spawn packet
                if (packet.getEntityId() != mc.player.getId()) {
                    trackedPlayers.put(
                        packet.getEntityId(),
                        new Vec3d(packet.getX(), packet.getY(), packet.getZ())
                    );
                }
            }
        }

        // 2. Detect absolute position updates (EntityPositionS2CPacket / Teleport)
        else if (event.packet instanceof EntityPositionS2CPacket packet) {
            if (trackedPlayers.containsKey(packet.getId())) {
                trackedPlayers.put(
                    packet.getId(),
                    new Vec3d(packet.getX(), packet.getY(), packet.getZ())
                );
            }
        }

        // 3. Detect relative position updates (EntityS2CPacket - Move / Move & Rotate)
        else if (event.packet instanceof EntityS2CPacket packet) {
            int entityId = packet.getEntityId(mc.world);
            if (trackedPlayers.containsKey(entityId)) {
                Vec3d currentPos = trackedPlayers.get(entityId);
                
                // Packets specify offset shifts in 1/4096th fixed-point units or delta steps
                double deltaX = packet.getDeltaX() / 4096.0;
                double deltaY = packet.getDeltaY() / 4096.0;
                double deltaZ = packet.getDeltaZ() / 4096.0;

                trackedPlayers.put(
                    entityId,
                    currentPos.add(deltaX, deltaY, deltaZ)
                );
            }
        }

        // 4. Detect entity despawn / destruction packets
        else if (event.packet instanceof EntitiesDestroyS2CPacket packet) {
            for (int id : packet.getEntityIds()) {
                trackedPlayers.remove(id);
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        int minY = Math.min(minimumY.get(), maximumY.get());
        int maxY = Math.max(minimumY.get(), maximumY.get());

        playerChunks.clear();

        // Calculate chunk positions directly from stored raw packet coordinates
        for (Vec3d pos : trackedPlayers.values()) {
            if (pos.y < minY || pos.y > maxY) continue;

            int chunkX = (int) Math.floor(pos.x) >> 4;
            int chunkZ = (int) Math.floor(pos.z) >> 4;
            playerChunks.add(new ChunkPos(chunkX, chunkZ));
        }

        for (ChunkPos chunk : playerChunks) {
            event.renderer.box(
                chunk.getStartX(),
                mc.world.getBottomY(),
                chunk.getStartZ(),
                chunk.getEndX() + 1,
                mc.world.getBottomY() + mc.world.getHeight(),
                chunk.getEndZ() + 1,
                fillColor.get(),
                outlineColor.get(),
                ShapeMode.Both,
                0
            );
        }
    }
}
