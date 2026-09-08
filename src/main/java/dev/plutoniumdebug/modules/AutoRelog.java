package dev.plutoniumdebug.modules;

import dev.plutoniumdebug.PlutoniumDebug;
import dev.plutoniumdebug.util.LoadedWorld;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AutoReconnect;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;

/** Disconnects and reconnects once when the player reaches a chosen height. */
public final class AutoRelog extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> triggerY = sgGeneral.add(new IntSetting.Builder()
        .name("trigger-y")
        .description("Disconnect and reconnect when you reach this Y level.")
        .defaultValue(200)
        .range(-64, 319)
        .build()
    );

    private final Setting<Double> reconnectDelay = sgGeneral.add(new DoubleSetting.Builder()
        .name("reconnect-delay")
        .description("Seconds to wait before Meteor reconnects.")
        .defaultValue(3.5)
        .min(0)
        .sliderMax(30)
        .decimalPlaces(1)
        .build()
    );

    private boolean armed;

    public AutoRelog() {
        super(
            PlutoniumDebug.CATEGORY,
            "auto-relog",
            "Disconnects and reconnects when you reach a selected height."
        );
    }

    @Override
    public void onActivate() {
        armed = true;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!LoadedWorld.ready() || mc.isInSingleplayer()) return;

        int currentY = mc.player.getBlockY();

        // Re-arm only after falling below the configured height.
        if (currentY < triggerY.get()) armed = true;

        if (!armed || currentY < triggerY.get()) return;

        AutoReconnect autoReconnect = Modules.get().get(AutoReconnect.class);
        autoReconnect.time.set(reconnectDelay.get());

        if (!autoReconnect.isActive()) {
            autoReconnect.toggle();
        }

        armed = false;
        info("Auto relogging at Y=%d.", currentY);

        mc.getNetworkHandler().getConnection().disconnect(
            Text.literal("Auto relog at Y=" + currentY)
        );
    }
}
