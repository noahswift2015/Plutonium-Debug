package dev.plutoniumdebug.modules;

import dev.plutoniumdebug.PlutoniumDebug;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;

/** Cinematic highlighting for loaded players within a selected height range. */
public final class PlayerBypass extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> minimumY = sgGeneral.add(new IntSetting.Builder()
        .name("minimum-y")
        .description("Only render players at or above this height.")
        .defaultValue(-64)
        .range(-64, 128)
        .build()
    );

    private final Setting<Integer> maximumY = sgGeneral.add(new IntSetting.Builder()
        .name("maximum-y")
        .description("Only render players at or below this height.")
        .defaultValue(128)
        .range(-64, 128)
        .build()
    );

    public PlayerBypass() {
        super(
            PlutoniumDebug.CATEGORY,
            "player-bypass",
            "Cinematically highlights loaded players within a chosen height range."
        );
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        int minY = Math.min(minimumY.get(), maximumY.get());
        int maxY = Math.max(minimumY.get(), maximumY.get());

        Color fill = new Color(80, 190, 255, 25);
        Color outline = new Color(100, 220, 255);

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            if (player.getY() < minY || player.getY() > maxY) continue;

            event.renderer.box(
                player.getBoundingBox(),
                fill,
                outline,
                ShapeMode.Both,
                0
            );
        }
    }
}
