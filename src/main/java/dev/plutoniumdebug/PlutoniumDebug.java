package dev.plutoniumdebug;

import dev.plutoniumdebug.hud.DiagnosticsHud;
import dev.plutoniumdebug.hud.RegionMapHud;
import dev.plutoniumdebug.modules.AdvancedBlockEsp;
import dev.plutoniumdebug.modules.AmethystEsp;
import dev.plutoniumdebug.modules.BlockNotifier;
import dev.plutoniumdebug.modules.HomeReset;
import dev.plutoniumdebug.modules.LoadedChunkFinder;
import dev.plutoniumdebug.modules.RegionMap;
import dev.plutoniumdebug.modules.SpawnerNametags;
import dev.plutoniumdebug.modules.SpawnerBeacons;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.item.Items;
import dev.plutoniumdebug.modules.AutoRelog;
import dev.plutoniumdebug.modules.PlayerBypass;

public final class PlutoniumDebug extends MeteorAddon {
    public static final Category CATEGORY = new Category(
        "Plutonium Debug",
        Items.SPYGLASS.getDefaultStack()
    );

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public void onInitialize() {
        Modules.get().add(new PlayerBypass());
        Modules.get().add(new AutoRelog());
        Modules.get().add(new LoadedChunkFinder());
        Modules.get().add(new RegionMap());
        Modules.get().add(new AdvancedBlockEsp());
        Modules.get().add(new AmethystEsp());
        Modules.get().add(new SpawnerNametags());
        Modules.get().add(new SpawnerBeacons());
        Modules.get().add(new BlockNotifier());
        Modules.get().add(new HomeReset());
        Hud.get().register(DiagnosticsHud.INFO);
        Hud.get().register(RegionMapHud.INFO);
    }

    @Override
    public String getPackage() {
        return "dev.plutoniumdebug";
    }
}
