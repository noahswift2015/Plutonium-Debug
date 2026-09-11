package dev.plutoniumdebug.modules;

import dev.plutoniumdebug.PlutoniumDebug;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.ChunkPos;

import java.util.LinkedHashSet;
import java.util.Set;

/** Cinematically highlights chunks containing loaded players. */
public final class PlayerBypass extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> minimumY = sgGeneral.add(new IntSetting.Builder()
        .name("minimum-y")
        .description("Only mark chunks containing players at or above this height.")
        .defaultValue(-64)
        .range(-64, 128)
        .build()
    );

    private final Setting<Integer> maximumY = sgGeneral.add(new IntSetting.Builder()
        .name("maximum-y")
        .description("Only mark chunks containing players at or below this height.")
        .defaultValue(128)
        .range(-64, 128)
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

    private final Set<ChunkPos> playerChunks = new LinkedHashSet<>();

    public PlayerBypass() {
        super(
            PlutoniumDebug.CATEGORY,
            "player-bypass",
            "Cinematically highlights loaded chunks containing players."
        );
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        int minY = Math.min(minimumY.get(), maximumY.get());
        int maxY = Math.max(minimumY.get(), maximumY.get());

        playerChunks.clear();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            if (player.getY() < minY || player.getY() > maxY) continue;

            playerChunks.add(new ChunkPos(player.getBlockPos()));
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
