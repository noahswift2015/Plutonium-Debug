package dev.plutoniumdebug.hud;

import dev.plutoniumdebug.PlutoniumDebug;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;

/** Compact status element, registered under a dedicated Plutonium Debug HUD section. */
public final class DiagnosticsHud extends HudElement {
    public static final HudGroup GROUP = new HudGroup("Plutonium Debug");
    public static final HudElementInfo<DiagnosticsHud> INFO = new HudElementInfo<>(GROUP, "diagnostics", "Shows safe loaded-world diagnostic status.", DiagnosticsHud::new);
    private static final Color TITLE = new Color(255, 80, 80);

    public DiagnosticsHud() { super(INFO); }
    @Override public void render(HudRenderer renderer) {
        String title = "Plutonium Debug";
        String status = "Loaded-world diagnostics";
        setSize(Math.max(renderer.textWidth(title), renderer.textWidth(status)), renderer.textHeight() * 2);
        renderer.text(title, x, y, TITLE, true);
        renderer.text(status, x, y + renderer.textHeight(), Color.WHITE, true);
    }
}
