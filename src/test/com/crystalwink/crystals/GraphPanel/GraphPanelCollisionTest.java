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

    @Test
    public void separatesTwoLockedTilesByMovingTheWeakerLock() {
        Tile[] tiles = {
            new Tile(0, new int[] {1, 2, 3, 4}, 10),
            new Tile(1, new int[] {1, 2, 3, 4}, 10)
        };
        tiles[0].xWorldSpace = 50;
        tiles[0].yWorldSpace = 50;
        tiles[1].xWorldSpace = 54;
        tiles[1].yWorldSpace = 53;
        LockManager lockManager = new LockManager(2);
        lockManager.updateLock(0, 500);
        lockManager.updateLock(1, 100);

        GraphPanel.resolveTileOverlaps(tiles, tiles.length, lockManager, new Dimension(200, 200));

        assertEquals(50, tiles[0].xWorldSpace, 0);
        assertEquals(50, tiles[0].yWorldSpace, 0);
        double distanceX = Math.abs(tiles[1].xWorldSpace - tiles[0].xWorldSpace);
        double distanceY = Math.abs(tiles[1].yWorldSpace - tiles[0].yWorldSpace);
        assertTrue(distanceX >= 10 || distanceY >= 10);
    }

    @Test
    public void tilesButtUpExactlyAtTheRescaledTileSize() {
        Tile[] tiles = {
            new Tile(0, new int[] {1, 2, 3, 4}, 10),
            new Tile(1, new int[] {1, 2, 3, 4}, 10)
        };
        tiles[0].xWorldSpace = 100;
        tiles[0].yWorldSpace = 100;
        tiles[1].xWorldSpace = 110;   // just butting at scale 10
        tiles[1].yWorldSpace = 100;

        for (Tile tile : tiles) {
            tile.rescale(28);
        }
        GraphPanel.resolveTileOverlaps(tiles, tiles.length, new LockManager(2), new Dimension(400, 400));

        assertEquals(28, Math.abs(tiles[1].xWorldSpace - tiles[0].xWorldSpace), 0.001);
        assertEquals(0, Math.abs(tiles[1].yWorldSpace - tiles[0].yWorldSpace), 0.001);
    }

    private static int countOverlaps(Tile[] tiles, double separation) {
        int overlaps = 0;
        for (int first = 0; first < tiles.length; first++) {
            for (int second = first + 1; second < tiles.length; second++) {
                double overlapX = separation - Math.abs(tiles[second].xWorldSpace - tiles[first].xWorldSpace);
                double overlapY = separation - Math.abs(tiles[second].yWorldSpace - tiles[first].yWorldSpace);
                if (overlapX > 0.01 && overlapY > 0.01) {
                    overlaps++;
                }
            }
        }
        return overlaps;
    }

    /** The shape seen in the running applet: hundreds of tiles, most of them locked, packed into one clump. */
    @Test
    public void denseClumpOfMostlyLockedTilesEndsUpWithoutOverlaps() {
        int count = 240;
        java.util.Random random = new java.util.Random(7);
        Tile[] tiles = new Tile[count];
        LockManager lockManager = new LockManager(count);
        for (int index = 0; index < count; index++) {
            tiles[index] = new Tile(index, new int[] {1, 2, 3, 4}, 28);
            tiles[index].xWorldSpace = 200 + random.nextInt(260);
            tiles[index].yWorldSpace = 150 + random.nextInt(220);
            if (index % 4 != 0) {
                lockManager.updateLock(index, 1 + random.nextInt(400));
            }
        }

        GraphPanel.resolveTileOverlaps(tiles, count, lockManager, new Dimension(643, 640));

        assertEquals("overlapping pairs left", 0, countOverlaps(tiles, 28));
    }
}
