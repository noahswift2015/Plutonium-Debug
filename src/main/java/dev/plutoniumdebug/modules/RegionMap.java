package dev.plutoniumdebug.modules;

import dev.plutoniumdebug.PlutoniumDebug;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.List;

/** Renders a local-only overview of chunks the client has already loaded. */
public final class RegionMap extends Module {
    private static final Color BACKGROUND = new Color(15, 20, 48, 190);
    private static final Color UNLOADED = new Color(30, 37, 76, 180);
    private static final Color LOADED = new Color(69, 113, 201, 210);
    private static final Color PLAYER = new Color(151, 89, 233, 235);
    private static final Color GRID = new Color(118, 135, 205, 120);
    private static final Color TITLE = new Color(255, 92, 92);
    private static final Color NOTE = new Color(215, 215, 235);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgNotes = settings.createGroup("Notes");

    private final Setting<Integer> x = sgGeneral.add(new IntSetting.Builder()
        .name("x")
        .description("Horizontal screen position of the map.")
        .defaultValue(8)
        .min(0)
        .build()
    );

    private final Setting<Integer> y = sgGeneral.add(new IntSetting.Builder()
        .name("y")
        .description("Vertical screen position of the map.")
        .defaultValue(8)
        .min(0)
        .build()
    );

    private final Setting<Integer> radius = sgGeneral.add(new IntSetting.Builder()
        .name("radius")
        .description("How many chunks to display in each direction from the player.")
        .defaultValue(5)
        .range(1, 10)
        .build()
    );

    private final Setting<Integer> cellSize = sgGeneral.add(new IntSetting.Builder()
        .name("cell-size")
        .description("Size of each chunk cell in pixels.")
        .defaultValue(18)
        .range(8, 32)
        .build()
    );

    private final Setting<Boolean> showHotkeys = sgGeneral.add(new BoolSetting.Builder()
        .name("show-hotkeys")
        .description("Shows configured Plutonium Debug module keybinds underneath the map.")
        .defaultValue(true)
        .build()
    );

    private final Setting<String> noteOne = sgNotes.add(note("note-one", "Glitched - no RTP spots").build());
    private final Setting<String> noteTwo = sgNotes.add(note("note-two", "Good for baltop and media").build());
    private final Setting<String> noteThree = sgNotes.add(note("note-three", "Good for media and other bases").build());

    private final List<Module> boundModules = new ArrayList<>();

    public RegionMap() {
        super(PlutoniumDebug.CATEGORY, "region-map", "Shows client-loaded chunks, notes, and configured addon hotkeys.");
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (mc.player == null || mc.world == null) return;

        collectBoundModules();

        HudRenderer renderer = HudRenderer.INSTANCE;
        renderer.begin(event.drawContext);
        render(renderer);
        renderer.end();
    }

    private void collectBoundModules() {
        boundModules.clear();
        if (!showHotkeys.get()) return;

        for (Module module : Modules.get().getAll()) {
            if (module.category == PlutoniumDebug.CATEGORY && module.keybind.isSet()) boundModules.add(module);
        }
    }

    private void render(HudRenderer renderer) {
        int diameter = radius.get() * 2 + 1;
        double textHeight = renderer.textHeight();
        double mapSize = diameter * cellSize.get();
        double mapY = y.get() + textHeight + 2;
        ChunkPos center = new ChunkPos(mc.player.getBlockPos());

        renderer.text("LOADED REGION MAP", x.get(), y.get(), TITLE, true);
        renderer.quad(x.get(), mapY, mapSize, mapSize, BACKGROUND);

        for (int dz = -radius.get(); dz <= radius.get(); dz++) {
            for (int dx = -radius.get(); dx <= radius.get(); dx++) {
                double cellX = x.get() + (dx + radius.get()) * cellSize.get();
                double cellY = mapY + (dz + radius.get()) * cellSize.get();
                Color color = dx == 0 && dz == 0 ? PLAYER
                    : mc.world.isChunkLoaded(center.x + dx, center.z + dz) ? LOADED : UNLOADED;
                renderer.quad(cellX + 1, cellY + 1, cellSize.get() - 2, cellSize.get() - 2, color);
                renderer.line(cellX, cellY, cellX + cellSize.get(), cellY, GRID);
                renderer.line(cellX, cellY, cellX, cellY + cellSize.get(), GRID);
            }
        }

        double lineY = mapY + mapSize + 4;
        lineY = renderNote(renderer, noteOne.get(), lineY);
        lineY = renderNote(renderer, noteTwo.get(), lineY);
        lineY = renderNote(renderer, noteThree.get(), lineY);

        if (showHotkeys.get()) {
            renderer.text("HOTKEYS", x.get(), lineY, TITLE, true);
            lineY += textHeight;
            if (boundModules.isEmpty()) {
                renderer.text("No Plutonium Debug keys bound", x.get(), lineY, NOTE, true);
            } else {
                for (Module module : boundModules) {
                    renderer.text(module.title + "  " + module.keybind, x.get(), lineY, Color.WHITE, true);
                    lineY += textHeight;
                }
            }
        }
    }

    private static StringSetting.Builder note(String name, String value) {
        return new StringSetting.Builder().name(name).description("A manual label shown below the region map.").defaultValue(value);
    }

    private double renderNote(HudRenderer renderer, String note, double lineY) {
        if (note.isBlank()) return lineY;
        renderer.text(note, x.get(), lineY, NOTE, true);
        return lineY + renderer.textHeight();
    }
}
