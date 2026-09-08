package dev.plutoniumdebug;

import dev.plutoniumdebug.hud.DiagnosticsHud;
import dev.plutoniumdebug.modules.AdvancedBlockEsp;
import dev.plutoniumdebug.modules.AmethystEsp;
import dev.plutoniumdebug.modules.BlockNotifier;
import dev.plutoniumdebug.modules.HomeReset;
import dev.plutoniumdebug.modules.LoadedChunkFinder;
import dev.plutoniumdebug.modules.SpawnerNametags;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.item.Items;
import dev.plutoniumdebug.modules.AutoRelog;
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
        Modules.get().add(new LoadedChunkFinder());
        Modules.get().add(new AdvancedBlockEsp());
        Modules.get().add(new AmethystEsp());
        Modules.get().add(new SpawnerNametags());
        Modules.get().add(new BlockNotifier());
        Modules.get().add(new HomeReset());
        Hud.get().register(DiagnosticsHud.INFO);
    }

    @Override
    public String getPackage() {
        return "dev.plutoniumdebug";
    }
}
