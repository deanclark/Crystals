package com.crystalwink.crystals.GraphPanel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.junit.BeforeClass;
import org.junit.Test;

import com.crystalwink.crystals.Crystal;
import com.crystalwink.crystalscommon.Tile.Tile;

/**
 * Headless run of the floating-mode relaxation on the 256 real tiles (src/e2pieces.txt) at the Large tile size.
 * Crystal is an Applet and cannot be constructed headless, so an instance is allocated without running its constructor.
 */
public class FloatingLayoutDiagnosticsTest {
    @BeforeClass
    public static void keepBestBoardsOutOfTheWorkingDirectory() throws Exception {
        GraphPanel.boardDirectory = java.nio.file.Files.createTempDirectory("crystals-boards");
    }

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
        for (int iteration = 0; iteration < 1500; iteration++) {
            panel.relax();
        }
        checkCrystal(panel);
    }

    /**
     * The local search accepts swaps that lose tiles to get out of dead ends, remembers the best board and puts it back.
     * After a few cooling cycles the best board must beat what plain repair reaches (about 222-225), and restoring it must
     * give a board that is just as valid: all neighbours match, no overlaps, locks equal to the placed tiles.
     */
    @Test
    public void localSearchFindsABetterBoardAndRestoringItIsValid() throws Exception {
        GraphPanel panel = createPanel();
        for (int iteration = 0; iteration < 5000; iteration++) {
            panel.relax();
        }
        assertTrue("some swaps that lose tiles should have been accepted", panel.swapsWorsening > 0);
        assertTrue("the best board should beat plain repair: " + panel.bestPlaced(), panel.bestPlaced() >= 226);

        panel.restoreBest();
        assertEquals("the best board is back", panel.bestPlaced(), panel.placedCount());
        checkCrystal(panel, panel.bestPlaced());
    }

    /**
     * A run is a greedy phase plus cooling cycles; when it is over the crystal is cleared and a fresh run starts, keeping
     * the best board of all runs. When the last run ends the best board of all runs must be back on the table.
     */
    @Test
    public void freshRunsKeepTheBestBoardAndTheLastRunEndsOnIt() throws Exception {
        int savedRuns = GraphPanel.freshRuns;
        int savedCycles = GraphPanel.cyclesPerRun;
        try {
            GraphPanel.freshRuns = 1;
            GraphPanel.cyclesPerRun = 1;   // runs of 300 + 1500 iterations
            GraphPanel panel = createPanel();
            int placedAtEndOfFirstRun = 0;
            for (int iteration = 1; iteration <= 3600; iteration++) {
                panel.relax();
                if (iteration == 1799) {
                    placedAtEndOfFirstRun = panel.bestPlaced();
                }
                if (iteration == 1801) {
                    assertEquals("the crystal is cleared for the second run", 2, panel.runsStarted());
                    assertTrue("a fresh run starts from few tiles, not the old board: " + panel.placedCount(), panel.placedCount() < 100);
                    assertTrue("the first run's best is kept", panel.bestPlaced() >= placedAtEndOfFirstRun);
                }
            }
            assertEquals("the last run ends on the best board of all runs", panel.bestPlaced(), panel.placedCount());
            assertTrue("and it is a good one: " + panel.placedCount(), panel.placedCount() >= 222);
            checkCrystal(panel, panel.bestPlaced());
        } finally {
            GraphPanel.freshRuns = savedRuns;
            GraphPanel.cyclesPerRun = savedCycles;
        }
    }

    /**
     * Boards that beat the best on file are written as they are found, and a file loads back onto a fresh panel as the
     * same valid board (same tiles on the same cells, turned the same way), ready for the search to carry on from.
     */
    @Test
    public void bestBoardsAreSavedAndCanBeImportedOntoAFreshPanel() throws Exception {
        java.nio.file.Path directory = java.nio.file.Files.createTempDirectory("crystals-boards");
        java.nio.file.Path savedDirectory = GraphPanel.boardDirectory;
        GraphPanel.boardDirectory = directory;
        try {
            GraphPanel panel = createPanel();
            for (int iteration = 0; iteration < 2500; iteration++) {
                panel.relax();
            }
            int onFile = BoardRecord.bestSaved(directory);
            assertTrue("a good board should be on file: " + onFile, onFile >= 220);
            assertTrue("the file is reported", panel.lastRecordFile != null && java.nio.file.Files.exists(panel.lastRecordFile));

            BoardRecord record = BoardRecord.read(panel.lastRecordFile);
            assertEquals(onFile, record.placed());

            GraphPanel fresh = createPanel();
            fresh.importBoard(panel.lastRecordFile);
            assertEquals(record.placed(), fresh.placedCount());
            for (BoardRecord.Entry entry : record.entries) {
                Tile tile = fresh.tiles[fresh.getTileIndexFromId(entry.tileId)];
                assertEquals(entry.column, fresh.cellColumn(tile));
                assertEquals(entry.row, fresh.cellRow(tile));
                assertEquals(entry.orientation, tile.getOrientation());
            }
            checkCrystal(fresh, record.placed(), false);
            for (int iteration = 0; iteration < 100; iteration++) {
                fresh.relax();
            }
            assertTrue("the search carries on from the imported board", fresh.placedCount() >= record.placed());
            checkCrystal(fresh, record.placed(), false);
        } finally {
            GraphPanel.boardDirectory = savedDirectory;
        }
    }

    /** Ticking or clearing Large changes the tile size: the crystal must keep its cells, not start again from nothing. */
    @Test
    public void changingTheTileSizeKeepsTheCrystal() throws Exception {
        GraphPanel panel = createPanel();
        for (int iteration = 0; iteration < 1500; iteration++) {
            panel.relax();
        }
        int placed = panel.placedCount();
        int locks = panel.tileLockManager.getLocksInUse();
        int[] column = new int[panel.getNumberOfTiles()];
        int[] row = new int[panel.getNumberOfTiles()];
        for (int i = 0; i < column.length; i++) {
            column[i] = panel.cellColumn(panel.tiles[i]);
            row[i] = panel.cellRow(panel.tiles[i]);
        }
        int bestBefore = panel.bestPlaced();

        panel.setTileScale(15);
        assertEquals("the same tiles are placed", placed, panel.placedCount());
        assertEquals("and still locked", locks, panel.tileLockManager.getLocksInUse());
        assertEquals(bestBefore, panel.bestPlaced());
        for (int i = 0; i < column.length; i++) {
            if (panel.isSettled(i)) {
                assertEquals(column[i], panel.cellColumn(panel.tiles[i]));
                assertEquals(row[i], panel.cellRow(panel.tiles[i]));
            }
        }
        for (int iteration = 0; iteration < 100; iteration++) {
            panel.relax();
        }
        checkCrystal(panel, placed, false);

        panel.setTileScale(SCALE);
        assertTrue(panel.placedCount() >= placed);
        for (int iteration = 0; iteration < 100; iteration++) {
            panel.relax();
        }
        checkCrystal(panel, placed, false);
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
        for (int iteration = 0; iteration < 1500; iteration++) {
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
        checkCrystal(panel, 200);
    }

    private void checkCrystal(GraphPanel panel, int minimumPlaced) {
        checkCrystal(panel, minimumPlaced, true);
    }

    private void checkCrystal(GraphPanel panel, int minimumPlaced, boolean expectSwaps) {
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
                if (Tile.getSegmentScale() - Math.abs(dx) > 1 && Tile.getSegmentScale() - Math.abs(dy) > 1) {
                    overlaps++;
                }
                if (!panel.isSettled(a) || !panel.isSettled(b)) {
                    continue;
                }
                boolean horizontal = Math.abs(Math.abs(dx) - Tile.getSegmentScale()) <= 1.5 && Math.abs(dy) <= 1.5;
                boolean vertical = Math.abs(Math.abs(dy) - Tile.getSegmentScale()) <= 1.5 && Math.abs(dx) <= 1.5;
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
        System.out.printf("after the run: %d of %d tiles placed (%d connected), %d touching pairs, %d mismatched, overlaps %d, "
                + "corners placed %d, edge tiles placed %d, tiles unlocked to fill holes: %d swaps, %d plain fills%n",
                placed, n, largest, touching, mismatches, overlaps, cornersPlaced, edgesPlaced, panel.holesSwapped, panel.holesFilled);

        assertEquals("every placed tile keeps its lock, so locks in use equal placed tiles", placed, panel.tileLockManager.getLocksInUse());
        assertEquals("tiles must not overlap", 0, overlaps);
        assertEquals("neighbouring crystal tiles must match edge to edge (" + touching + " pairs)", 0, mismatches);
        assertEquals("every crystal tile suits its cell (grey edges outside only)", 0, misfits);
        assertTrue("most of the board should be placed: " + placed, placed >= minimumPlaced);
        assertTrue("the crystal should be one connected piece: " + largest + " of " + placed, largest >= placed * 0.9);
        assertEquals("all four corner tiles should be in the corners", 4, cornersPlaced);
        assertTrue("edge tiles should be on the border: " + edgesPlaced, edgesPlaced >= 40);
        assertTrue("holes should have been filled by unlocking neighbours", !expectSwaps || panel.holesSwapped > 0);
    }
}
