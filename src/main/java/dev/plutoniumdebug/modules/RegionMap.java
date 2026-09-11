package dev.plutoniumdebug.modules;

import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;

import java.util.Arrays;
import java.util.List;

public class RegionMapModule extends Module {
    // Custom color map matching your key
    private final Color COLOR_GLITCHED = new Color(128, 128, 128, 180); // Grey: Glitched / No RTP spots
    private final Color COLOR_MEDIA = new Color(50, 130, 240, 180);    // Blue: Good for media & other bases
    private final Color COLOR_BALTOP = new Color(160, 60, 220, 180);   // Purple: Good for baltop & media bases
    private final Color COLOR_INDICATOR = new Color(255, 50, 50, 255);  // Red border: Active player region

    // DonutSMP region categorizations
    private final List<Integer> GLITCHED_REGIONS = Arrays.asList(
        1, 2, 3, 7, 9, 10, 11, 12, 13, 14, 23, 24, 25, 34, 35, 36, 37, 38, 39, 40, 
        41, 42, 44, 45, 46, 47, 51, 54, 59, 61, 62, 71, 72, 74, 75
    );

    private final List<Integer> MEDIA_REGIONS = Arrays.asList(
        4, 5, 8, 30
    );

    private final List<Integer> BALTOP_REGIONS = Arrays.asList(
        82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 
        100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113
    );

    // 9x9 Layout matching Plutonium Debug image
    private final int[][] GRID_LAYOUT = {
        {82, 100, 101, 102, 103, 104, 105, 106, 91},
        {83,  44,  75,  42,  41,  40,  39,  38, 92},
        {84,  45,  14,  13,  12,  11,  10,  37, 93},
        {85,  46,  74,   3,   2,   1,  25,  36, 94},
        {86,  47,  72,  71,   5,   4,  24,  35, 95},
        {87,  51,  17,   9,   8,   7,  23,  34, 96},
        {88,  54,  18,  61,  62,  21,  22,  33, 97},
        {89,  26,  27,  28,  29,  30,  59,  32, 98},
        {90, 107, 108, 109, 110, 111, 112, 113, 99}
    };

    public RegionMapModule() {
        super(Categories.Render, "region-map", "Displays a DonutSMP region map overlay.");
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        GuiRenderer renderer = event.renderer;
        int cellSize = 18;
        int padding = 2;

        int startX = 10;
        int startY = 10;

        int currentRegion = getCurrentPlayerRegion(mc);

        for (int row = 0; row < GRID_LAYOUT.length; row++) {
            for (int col = 0; col < GRID_LAYOUT[row].length; col++) {
                int regionId = GRID_LAYOUT[row][col];
                double x = startX + (col * (cellSize + padding));
                double y = startY + (row * (cellSize + padding));

                // Draw background cell color
                Color boxColor = getRegionColor(regionId);
                renderer.quad(x, y, cellSize, cellSize, boxColor);

                // Highlight player's active region position
                if (regionId == currentRegion) {
                    renderer.quad(x, y, cellSize, 2, COLOR_INDICATOR);
                    renderer.quad(x, y + cellSize - 2, cellSize, 2, COLOR_INDICATOR);
                    renderer.quad(x, y, 2, cellSize, COLOR_INDICATOR);
                    renderer.quad(x + cellSize - 2, y, 2, cellSize, COLOR_INDICATOR);
                }

                // Render cell region ID number
                String text = String.valueOf(regionId);
                double textX = x + (cellSize / 2.0) - (renderer.textWidth(text) / 2.0);
                double textY = y + (cellSize / 2.0) - (renderer.textHeight() / 2.0);
                renderer.text(text, textX, textY, Color.WHITE, false);
            }
        }
    }

    private Color getRegionColor(int regionId) {
        if (GLITCHED_REGIONS.contains(regionId)) return COLOR_GLITCHED;
        if (MEDIA_REGIONS.contains(regionId)) return COLOR_MEDIA;
        if (BALTOP_REGIONS.contains(regionId)) return COLOR_BALTOP;
        return new Color(60, 60, 60, 180);
    }

    private int getCurrentPlayerRegion(MinecraftClient mc) {
        int blockX = (int) mc.player.getX();
        int blockZ = (int) mc.player.getZ();

        int regionX = Math.floorDiv(blockX, 2048);
        int regionZ = Math.floorDiv(blockZ, 2048);

        return getRegionFromCoords(regionX, regionZ);
    }

    private int getRegionFromCoords(int rx, int rz) {
        return 5; // Replace with coordinate-to-region lookup algorithm if needed
    }
}
