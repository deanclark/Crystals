package test.com.crystalwink.crystals.FrameStructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.crystalwink.crystals.FrameStructure.FrameStructure;
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
