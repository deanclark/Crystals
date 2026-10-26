package com.crystalwink.crystals.GraphPanel;

import java.awt.Dimension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import com.crystalwink.crystalscommon.LockManager.LockManager;
import com.crystalwink.crystalscommon.Tile.Tile;

public class GraphPanelCollisionTest {
    @Test
    public void separatesOverlappingUnlockedTiles() {
        Tile[] tiles = {
            new Tile(0, new int[] {1, 2, 3, 4}, 10),
            new Tile(1, new int[] {1, 2, 3, 4}, 10)
        };
        tiles[0].xWorldSpace = 50;
        tiles[0].yWorldSpace = 50;
        tiles[1].xWorldSpace = 55;
        tiles[1].yWorldSpace = 50;

        GraphPanel.resolveTileOverlaps(tiles, tiles.length, new LockManager(2), new Dimension(200, 200));

        double distanceX = Math.abs(tiles[1].xWorldSpace - tiles[0].xWorldSpace);
        double distanceY = Math.abs(tiles[1].yWorldSpace - tiles[0].yWorldSpace);
        assertTrue(distanceX >= 10 || distanceY >= 10);
    }

    @Test
    public void movesUnlockedTileAwayFromLockedTile() {
        Tile[] tiles = {
            new Tile(0, new int[] {1, 2, 3, 4}, 10),
            new Tile(1, new int[] {1, 2, 3, 4}, 10)
        };
        tiles[0].xWorldSpace = 50;
        tiles[0].yWorldSpace = 50;
        tiles[1].xWorldSpace = 55;
        tiles[1].yWorldSpace = 50;
        LockManager lockManager = new LockManager(2);
        lockManager.updateLock(0, Tile.LOCKED);
        assertTrue(lockManager.isLocked(0));

        GraphPanel.resolveTileOverlaps(tiles, tiles.length, lockManager, new Dimension(200, 200));

        assertEquals(50, tiles[0].xWorldSpace, 0);
        double distanceX = Math.abs(tiles[1].xWorldSpace - tiles[0].xWorldSpace);
        double distanceY = Math.abs(tiles[1].yWorldSpace - tiles[0].yWorldSpace);
        assertTrue(distanceX >= 10 || distanceY >= 10);
    }
}