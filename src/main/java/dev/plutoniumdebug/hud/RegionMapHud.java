package dev.plutoniumdebug.hud;

import dev.plutoniumdebug.PlutoniumDebug;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.List;

/** A local-only overview of chunks the client has already loaded, with addon keybinds below it. */
public final class RegionMapHud extends HudElement {
    public static final HudElementInfo<RegionMapHud> INFO = new HudElementInfo<>(
        DiagnosticsHud.GROUP,
        "region-map",
        "Displays nearby client-loaded chunks, optional notes, and Plutonium Debug hotkeys.",
        RegionMapHud::new
    );

    private static final Color BACKGROUND = new Color(15, 20, 48, 190);
    private static final Color UNLOADED = new Color(30, 37, 76, 180);
    private static final Color LOADED = new Color(69, 113, 201, 210);
    private static final Color PLAYER = new Color(151, 89, 233, 235);
    private static final Color GRID = new Color(118, 135, 205, 120);
    private static final Color TITLE = new Color(255, 92, 92);
    private static final Color NOTE = new Color(215, 215, 235);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgNotes = settings.createGroup("Notes");

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

    public RegionMapHud() {
        super(INFO);
    }

    @Override
    public void tick(HudRenderer renderer) {
        boundModules.clear();
        if (showHotkeys.get()) {
            for (Module module : Modules.get().getAll()) {
                if (module.category == PlutoniumDebug.CATEGORY && module.keybind.isSet()) boundModules.add(module);
            }
        }

        int diameter = radius.get() * 2 + 1;
        double mapSize = diameter * cellSize.get();
        double textHeight = renderer.textHeight();
        double notesHeight = noteHeight(textHeight);
        double hotkeyHeight = showHotkeys.get() ? textHeight * (boundModules.size() + 1) : 0;
        setSize(Math.max(mapSize, widestText(renderer)), textHeight + mapSize + notesHeight + hotkeyHeight + 8);
    }

    @Override
    public void render(HudRenderer renderer) {
        int diameter = radius.get() * 2 + 1;
        double textHeight = renderer.textHeight();
        double mapSize = diameter * cellSize.get();
        double mapY = y + textHeight + 2;

        renderer.text("LOADED REGION MAP", x, y, TITLE, true);
        renderer.quad(x, mapY, mapSize, mapSize, BACKGROUND);

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.world != null) {
            ChunkPos center = new ChunkPos(client.player.getBlockPos());
            for (int dz = -radius.get(); dz <= radius.get(); dz++) {
                for (int dx = -radius.get(); dx <= radius.get(); dx++) {
                    double cellX = x + (dx + radius.get()) * cellSize.get();
                    double cellY = mapY + (dz + radius.get()) * cellSize.get();
                    Color color = dx == 0 && dz == 0 ? PLAYER
                        : client.world.isChunkLoaded(center.x + dx, center.z + dz) ? LOADED : UNLOADED;
                    renderer.quad(cellX + 1, cellY + 1, cellSize.get() - 2, cellSize.get() - 2, color);
                    renderer.line(cellX, cellY, cellX + cellSize.get(), cellY, GRID);
                    renderer.line(cellX, cellY, cellX, cellY + cellSize.get(), GRID);
                }
            }
        } else if (isInEditor()) {
            renderer.quad(x + mapSize / 2 - cellSize.get() / 2.0 + 1, mapY + mapSize / 2 - cellSize.get() / 2.0 + 1,
                cellSize.get() - 2, cellSize.get() - 2, PLAYER);
        }

        double lineY = mapY + mapSize + 4;
        lineY = renderNote(renderer, noteOne.get(), lineY);
        lineY = renderNote(renderer, noteTwo.get(), lineY);
        lineY = renderNote(renderer, noteThree.get(), lineY);

        if (showHotkeys.get()) {
            renderer.text("HOTKEYS", x, lineY, TITLE, true);
            lineY += textHeight;
            if (boundModules.isEmpty()) {
                renderer.text("No Plutonium Debug keys bound", x, lineY, NOTE, true);
            } else {
                for (Module module : boundModules) {
                    renderer.text(module.title + "  " + module.keybind, x, lineY, Color.WHITE, true);
                    lineY += textHeight;
                }
            }
        }
    }

    private static StringSetting.Builder note(String name, String value) {
        return new StringSetting.Builder().name(name).description("A manual label shown below the region map.").defaultValue(value);
    }

    private double noteHeight(double textHeight) {
        int count = 0;
        if (!noteOne.get().isBlank()) count++;
        if (!noteTwo.get().isBlank()) count++;
        if (!noteThree.get().isBlank()) count++;
        return count * textHeight;
    }

    private double renderNote(HudRenderer renderer, String note, double lineY) {
        if (note.isBlank()) return lineY;
        renderer.text(note, x, lineY, NOTE, true);
        return lineY + renderer.textHeight();
    }

    private double widestText(HudRenderer renderer) {
        double widest = renderer.textWidth("LOADED REGION MAP");
        widest = Math.max(widest, renderer.textWidth(noteOne.get()));
        widest = Math.max(widest, renderer.textWidth(noteTwo.get()));
        widest = Math.max(widest, renderer.textWidth(noteThree.get()));
        widest = Math.max(widest, renderer.textWidth("No Plutonium Debug keys bound"));
        for (Module module : boundModules) widest = Math.max(widest, renderer.textWidth(module.title + "  " + module.keybind));
        return widest;
    }
}
