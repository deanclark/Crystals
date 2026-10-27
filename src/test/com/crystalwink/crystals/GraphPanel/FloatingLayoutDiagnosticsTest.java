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
        System.setProperty("java.awt.headless", "true");
        Field unsafeField = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
        Crystal crystal = (Crystal) unsafe.allocateInstance(Crystal.class);

        GraphPanel panel = new GraphPanel(crystal);
        panel.setSize(WIDTH, HEIGHT);
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
    public void lockedTilesFormOneCrystalAndBorderTilesHeadForTheBorder() throws Exception {
        GraphPanel panel = createPanel();
        for (int iteration = 0; iteration < 3000; iteration++) {
            panel.relax();
        }

        int n = panel.getNumberOfTiles();
        int locks = panel.tileLockManager.getLocksInUse();
        int overlaps = 0;
        int touching = 0;
        int touchingMatching = 0;
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
                boolean bothLocked = panel.tileLockManager.isLocked(first.getId()) && panel.tileLockManager.isLocked(second.getId());
                boolean horizontal = Math.abs(Math.abs(dx) - SCALE) <= 1.5 && Math.abs(dy) <= 1.5;
                boolean vertical = Math.abs(Math.abs(dy) - SCALE) <= 1.5 && Math.abs(dx) <= 1.5;
                if (bothLocked && (horizontal || vertical)) {
                    touching++;
                    int sideOfFirst = horizontal ? (dx > 0 ? 1 : 3) : (dy > 0 ? 2 : 0);
                    if (facing(first, sideOfFirst) == facing(second, (sideOfFirst + 2) % 4)) {
                        touchingMatching++;
                    }
                    group[find(group, a)] = find(group, b);
                }
            }
        }
        int[] size = new int[n];
        int largest = 0;
        for (int i = 0; i < n; i++) {
            if (panel.tileLockManager.isLocked(panel.tiles[i].getId())) {
                largest = Math.max(largest, ++size[find(group, i)]);
            }
        }

        double left = panel.cellCentreX(0);
        double right = panel.cellCentreX(panel.boardColumns() - 1);
        double top = panel.cellCentreY(0);
        double bottom = panel.cellCentreY(panel.boardRows() - 1);
        double near = SCALE * 2.5;
        int corners = 0;
        int cornersNear = 0;
        int edges = 0;
        int edgesNear = 0;
        for (int i = 0; i < n; i++) {
            Tile tile = panel.tiles[i];
            boolean nearVerticalSide = Math.abs(tile.xWorldSpace - left) < near || Math.abs(tile.xWorldSpace - right) < near;
            boolean nearHorizontalSide = Math.abs(tile.yWorldSpace - top) < near || Math.abs(tile.yWorldSpace - bottom) < near;
            if (tile.getEdgeCount() == 2) {
                corners++;
                cornersNear += nearVerticalSide && nearHorizontalSide ? 1 : 0;
            } else if (tile.getEdgeCount() == 1) {
                edges++;
                edgesNear += nearVerticalSide || nearHorizontalSide ? 1 : 0;
            }
        }
        System.out.printf("after 3000 iterations: locks %d, one crystal of %d, %d/%d touching pairs match, overlaps %d, "
                + "corner tiles at corners %d/%d, edge tiles at sides %d/%d%n",
                locks, largest, touchingMatching, touching, overlaps, cornersNear, corners, edgesNear, edges);

        assertEquals("tiles must not overlap", 0, overlaps);
        assertTrue("a crystal should have grown, locks " + locks, locks >= 60);
        assertTrue("locked tiles should be one connected crystal: " + largest + " of " + locks, largest >= locks * 0.9);
        assertTrue("every locked tile sits on the playing-area grid", onGrid(panel));
        assertTrue("corner tiles should head for corners: " + cornersNear + "/" + corners, cornersNear >= corners - 1);
        assertTrue("edge tiles should head for the sides: " + edgesNear + "/" + edges, edgesNear >= edges * 0.7);
    }

    private static boolean onGrid(GraphPanel panel) {
        for (int i = 0; i < panel.getNumberOfTiles(); i++) {
            Tile tile = panel.tiles[i];
            if (panel.tileLockManager.isLocked(tile.getId())) {
                double offsetX = Math.abs(tile.xWorldSpace - panel.cellCentreX(panel.cellColumn(tile)));
                double offsetY = Math.abs(tile.yWorldSpace - panel.cellCentreY(panel.cellRow(tile)));
                if (offsetX > 1.5 || offsetY > 1.5) {
                    return false;
                }
            }
        }
        return true;
    }
}
