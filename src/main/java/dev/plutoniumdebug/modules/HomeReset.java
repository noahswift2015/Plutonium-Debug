package dev.plutoniumdebug.modules;

import dev.plutoniumdebug.PlutoniumDebug;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;

/** Explicit, one-shot home command helper. It never triggers automatically. */
public final class HomeReset extends Module {
    private final Setting<String> slot = settings.getDefaultGroup().add(new StringSetting.Builder().name("home-slot").description("Home name/slot understood by your server.").defaultValue("1").build());
    public HomeReset() { super(PlutoniumDebug.CATEGORY, "home-reset", "On activation, explicitly sends delete then set home commands."); }
    @Override public void onActivate() {
        if (mc.player == null || mc.getNetworkHandler() == null) { error("Join a server before using Home Reset."); toggle(); return; }
        mc.getNetworkHandler().sendChatCommand("home delete " + slot.get());
        mc.getNetworkHandler().sendChatCommand("home set " + slot.get());
        info("Sent home reset commands for slot %s.", slot.get());
        toggle();
    }
}
