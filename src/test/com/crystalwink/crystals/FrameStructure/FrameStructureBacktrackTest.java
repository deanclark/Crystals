package test.com.crystalwink.crystals.FrameStructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.crystalwink.crystals.FrameStructure.FrameStructure;
import com.crystalwink.crystals.GraphPanel.BoardRecord;
import com.crystalwink.crystals.GraphPanel.GraphPanel;
import com.crystalwink.crystalscommon.Tile.Tile;

public class FrameStructureBacktrackTest {

    private FrameStructure frame;

    @Before
    public void createFrame() {
        Tile[] tiles = new Tile[16];
        for (int tileIndex = 0; tileIndex < tiles.length; tileIndex++) {
            tiles[tileIndex] = new Tile(tileIndex + 1, new int[] {0, 1, 2, 3}, 1);
        }
        frame = new FrameStructure(4, 4, tiles, tiles.length);
    }

    /**
     * A saved board goes into the frame as hint tiles using the tile's real colours, verifyFrame finds it again with the same
     * cell, turn and colours, and a better-than-on-file frame is written to the best-boards directory (not the console).
     */
    @Test
    public void aSavedBoardImportsIntoTheFrameAndVerifiedFramesAreWrittenToAFile() throws Exception {
        Tile[] tiles = new Tile[16];
        for (int tileIndex = 0; tileIndex < 15; tileIndex++) {
            tiles[tileIndex] = new Tile(tileIndex + 1, new int[] {0, 1, 2, 3}, 1);
        }
        tiles[15] = new Tile(16, new int[] {4, 5, 6, 7}, 1);
        FrameStructure small = new FrameStructure(4, 4, tiles, tiles.length);

        java.util.List<BoardRecord.Entry> wanted = new java.util.ArrayList<>();
        wanted.add(new BoardRecord.Entry(16, 1, 1, 1, new int[] {7, 4, 5, 6}));
        BoardRecord record = new BoardRecord(BoardRecord.FRAME, 16, 4, 4, wanted);
        assertEquals(1, small.importBoard(record));
        assertEquals("a tile cannot be imported twice", 0, small.importBoard(record));

        java.nio.file.Path directory = java.nio.file.Files.createTempDirectory("frame-boards");
        java.nio.file.Path savedDirectory = GraphPanel.boardDirectory;
        int savedMinimum = BoardRecord.minimumFrameTiles;
        GraphPanel.boardDirectory = directory;
        BoardRecord.minimumFrameTiles = 0;
        try {
            small.verifyFrame();
            BoardRecord found = small.verifiedBoard();
            assertEquals(1, found.placed());
            BoardRecord.Entry entry = found.entries.get(0);
            assertEquals(16, entry.tileId);
            assertEquals(1, entry.column);
            assertEquals(1, entry.row);
            assertEquals(1, entry.orientation);
            org.junit.Assert.assertArrayEquals(new int[] {7, 4, 5, 6}, entry.colours);

            assertEquals("the frame was written to a file", 1, BoardRecord.bestSaved(directory, BoardRecord.FRAME));
            assertEquals("floating boards are recorded separately", 0, BoardRecord.bestSaved(directory));
            assertEquals(BoardRecord.FRAME, BoardRecord.read(small.lastRecordFile).mode);
        } finally {
            GraphPanel.boardDirectory = savedDirectory;
            BoardRecord.minimumFrameTiles = savedMinimum;
        }
    }

    /**
     * Hint tiles in a saved board are imported first whatever their place in the file, stay permanent (counted by hintCount,
     * so the backtracking never reaches them) and are written first, flagged as hints, in the frame files.
     */
    @Test
    public void hintTilesImportFirstStayPermanentAndAreSavedFirst() throws Exception {
        Tile[] tiles = new Tile[16];
        for (int tileIndex = 0; tileIndex < 14; tileIndex++) {
            tiles[tileIndex] = new Tile(tileIndex + 1, new int[] {0, 1, 2, 3}, 1);
        }
        tiles[14] = new Tile(16, new int[] {4, 5, 6, 7}, 1);
        tiles[15] = new Tile(17, new int[] {8, 9, 10, 11}, 1);
        FrameStructure small = new FrameStructure(4, 4, tiles, tiles.length);

        java.util.List<BoardRecord.Entry> wanted = new java.util.ArrayList<>();
        wanted.add(new BoardRecord.Entry(17, 2, 2, 0, new int[] {8, 9, 10, 11}));
        wanted.add(new BoardRecord.Entry(16, 1, 1, 1, new int[] {7, 4, 5, 6}, true));
        assertEquals(2, small.importBoard(new BoardRecord(BoardRecord.FRAME, 16, 4, 4, wanted)));
        assertEquals(1, small.hintCount());

        java.nio.file.Path directory = java.nio.file.Files.createTempDirectory("frame-hint-boards");
        java.nio.file.Path savedDirectory = GraphPanel.boardDirectory;
        int savedMinimum = BoardRecord.minimumFrameTiles;
        GraphPanel.boardDirectory = directory;
        BoardRecord.minimumFrameTiles = 0;
        try {
            small.verifyFrame();
            BoardRecord found = small.verifiedBoard();
            assertEquals(16, found.entries.get(0).tileId);
            assertTrue(found.entries.get(0).hint);
            for (int index = 1; index < found.entries.size(); index++) {
                assertTrue(!found.entries.get(index).hint);
            }
            BoardRecord saved = BoardRecord.read(small.lastRecordFile);
            assertEquals(16, saved.entries.get(0).tileId);
            assertTrue(saved.entries.get(0).hint);
        } finally {
            GraphPanel.boardDirectory = savedDirectory;
            BoardRecord.minimumFrameTiles = savedMinimum;
        }
    }

    /** Frame mode builds tiles of its own; that must not change the tile size the floating mode lays its grid out with. */
    @Test
    public void buildingATileFromTheFrameKeepsTheDisplayedTileSize() {
        int before = Tile.getSegmentScale();
        Tile.setSegmentScale(28);
        try {
            frame.defineTileFromFrame(1, 1);
            assertEquals(28, Tile.getSegmentScale());
        } finally {
            Tile.setSegmentScale(before);
        }
    }

    @Test
    public void firstDeadEndUsesTheBaseDepth() {
        assertEquals(3, frame.nextBacktrackDepth(20));
    }

    @Test
    public void repeatedDeadEndsAtTheSameDepthBacktrackFurtherEachTime() {
        int previous = frame.nextBacktrackDepth(20);
        for (int attempt = 0; attempt < 5; attempt++) {
            int depth = frame.nextBacktrackDepth(20);
            assertTrue("depth " + depth + " should exceed " + previous, depth > previous);
            previous = depth;
        }
    }

    @Test
    public void progressPastThePreviousDeadEndResetsTheDepth() {
        frame.nextBacktrackDepth(20);
        frame.nextBacktrackDepth(20);
        assertEquals(3, frame.nextBacktrackDepth(25));
    }

    @Test
    public void neverBacktracksBelowTheHintTile() {
        for (int attempt = 0; attempt < 10; attempt++) {
            assertTrue(frame.nextBacktrackDepth(4) <= 3);
        }
        assertEquals(1, frame.nextBacktrackDepth(1));
    }
}
