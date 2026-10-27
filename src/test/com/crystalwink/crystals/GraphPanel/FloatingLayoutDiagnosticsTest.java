package com.crystalwink.crystals.GraphPanel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.junit.Test;

import com.crystalwink.crystals.Crystal;
import com.crystalwink.crystalscommon.Tile.Tile;

/**
 * Headless run of the floating-mode relaxation on the 256 real tiles (src/e2pieces.txt) at the Large tile size.
 * Crystal is an Applet and cannot be constructed headless, so an instance is allocated without running its constructor.
 */
public class FloatingLayoutDiagnosticsTest {
    static final int SCALE = 28;
    static final int WIDTH = 643;
    static final int HEIGHT = 640;

    /** A panel loaded and wired exactly as Crystal.mainInit does, at the Large tile size. */
    static GraphPanel createPanel() throws Exception {
        return createPanel(WIDTH, HEIGHT);
    }

    /** As above, with the panel at the given size (0 x 0 is what an applet has before its window is laid out). */
    static GraphPanel createPanel(int width, int height) throws Exception {
        System.setProperty("java.awt.headless", "true");
        Field unsafeField = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
        Crystal crystal = (Crystal) unsafe.allocateInstance(Crystal.class);

        GraphPanel panel = new GraphPanel(crystal);
        panel.setSize(width, height);
        Crystal.panel = panel;
        GraphPanel.showFloatingTiles = true;
        GraphPanel.runFast = false;
        panel.activeWiresCheckbox = false;
        if (!Crystal.loadTilesFromFile("src/e2pieces.txt")) {
            throw new IllegalStateException("tiles not loaded");
        }
        panel.setTileScale(SCALE);
        crystal.wireTilesWithCornerSegments();
        crystal.wireTilesWithEdgeSegments();
        crystal.wireTileNonEdgeSegments();
        crystal.activateWires();
        return panel;
    }

    /** Colour a tile shows on a world side (0 top, 1 right, 2 bottom, 3 left). */
    static int facing(Tile tile, int side) {
        return tile.tileSegment[(side - tile.getOrientation() + 8) % 4].colour;
    }

    private static int find(int[] group, int index) {
        while (group[index] != index) {
            group[index] = group[group[index]];
            index = group[index];
        }
        return index;
    }

    @Test
    public void crystalFillsMostOfTheBoardWithEveryNeighbourMatching() throws Exception {
        GraphPanel panel = createPanel();
        for (int iteration = 0; iteration < 6000; iteration++) {
            panel.relax();
        }
        checkCrystal(panel);
    }

    /** The applet's panel has no size until its window is laid out: the grid must not be anchored to that. */
    @Test
    public void crystalStaysOnTheBoardGridWhenThePanelIsSizedLate() throws Exception {
        GraphPanel panel = createPanel(0, 0);
        for (int iteration = 0; iteration < 200; iteration++) {
            panel.relax();
        }
        assertEquals("nothing grows before the panel has a size", 0, panel.tileLockManager.getLocksInUse());

        panel.setSize(WIDTH, HEIGHT);
        for (int iteration = 0; iteration < 6000; iteration++) {
            panel.relax();
        }
        checkCrystal(panel);

        // the seed is a corner tile in the top-left cell of the board centred on the panel
        double boardLeft = (WIDTH - panel.boardColumns() * SCALE) / 2.0;
        double boardTop = (HEIGHT - panel.boardRows() * SCALE) / 2.0;
        boolean cornerInTopLeft = false;
        for (int i = 0; i < panel.getNumberOfTiles(); i++) {
            Tile tile = panel.tiles[i];
            if (tile.getEdgeCount() == 2 && Math.abs(tile.xWorldSpace - (boardLeft + SCALE / 2.0)) <= 1.5
                    && Math.abs(tile.yWorldSpace - (boardTop + SCALE / 2.0)) <= 1.5) {
                cornerInTopLeft = true;
            }
        }
        assertTrue("a corner tile sits in the top-left corner of the board", cornerInTopLeft);
    }

    private void checkCrystal(GraphPanel panel) {
        int n = panel.getNumberOfTiles();
        int placed = 0;
        int cornersPlaced = 0;
        int edgesPlaced = 0;
        int misfits = 0;
        for (int i = 0; i < n; i++) {
            if (!panel.isSettled(i)) {
                continue;
            }
            Tile tile = panel.tiles[i];
            placed++;
            int column = panel.cellColumn(tile);
            int row = panel.cellRow(tile);
            boolean onVerticalSide = column == 0 || column == panel.boardColumns() - 1;
            boolean onHorizontalSide = row == 0 || row == panel.boardRows() - 1;
            if (tile.getEdgeCount() == 2 && onVerticalSide && onHorizontalSide) {
                cornersPlaced++;
            } else if (tile.getEdgeCount() == 1 && (onVerticalSide ^ onHorizontalSide)) {
                edgesPlaced++;
            }
            if (!panel.tileFitsCell(tile, tile.getOrientation(), column, row)) {
                misfits++;
            }
        }

        int overlaps = 0;
        int touching = 0;
        int mismatches = 0;
        int[] group = new int[n];
        for (int i = 0; i < n; i++) {
            group[i] = i;
        }
        for (int a = 0; a < n; a++) {
            Tile first = panel.tiles[a];
            for (int b = a + 1; b < n; b++) {
                Tile second = panel.tiles[b];
                double dx = second.xWorldSpace - first.xWorldSpace;
                double dy = second.yWorldSpace - first.yWorldSpace;
                if (SCALE - Math.abs(dx) > 1 && SCALE - Math.abs(dy) > 1) {
                    overlaps++;
                }
                if (!panel.isSettled(a) || !panel.isSettled(b)) {
                    continue;
                }
                boolean horizontal = Math.abs(Math.abs(dx) - SCALE) <= 1.5 && Math.abs(dy) <= 1.5;
                boolean vertical = Math.abs(Math.abs(dy) - SCALE) <= 1.5 && Math.abs(dx) <= 1.5;
                if (horizontal || vertical) {
                    touching++;
                    int sideOfFirst = horizontal ? (dx > 0 ? 1 : 3) : (dy > 0 ? 2 : 0);
                    if (facing(first, sideOfFirst) != facing(second, (sideOfFirst + 2) % 4)) {
                        mismatches++;
                    }
                    group[find(group, a)] = find(group, b);
                }
            }
        }
        int[] size = new int[n];
        int largest = 0;
        for (int i = 0; i < n; i++) {
            if (panel.isSettled(i)) {
                largest = Math.max(largest, ++size[find(group, i)]);
            }
        }
        System.out.printf("after 6000 iterations: %d of %d tiles placed (%d connected), %d touching pairs, %d mismatched, overlaps %d, "
                + "corners placed %d, edge tiles placed %d, tiles unlocked to fill holes: %d swaps, %d plain fills%n",
                placed, n, largest, touching, mismatches, overlaps, cornersPlaced, edgesPlaced, panel.holesSwapped, panel.holesFilled);

        assertEquals("tiles must not overlap", 0, overlaps);
        assertEquals("neighbouring crystal tiles must match edge to edge (" + touching + " pairs)", 0, mismatches);
        assertEquals("every crystal tile suits its cell (grey edges outside only)", 0, misfits);
        assertTrue("most of the board should be placed: " + placed, placed >= 190);
        assertTrue("the crystal should be one connected piece: " + largest + " of " + placed, largest >= placed * 0.9);
        assertEquals("all four corner tiles should be in the corners", 4, cornersPlaced);
        assertTrue("edge tiles should be on the border: " + edgesPlaced, edgesPlaced >= 40);
        assertTrue("holes should have been filled by unlocking neighbours", panel.holesSwapped > 0);
    }
}
