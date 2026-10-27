package com.crystalwink.crystalscommon.Tile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

public class TileTest {

    Tile aTile;
    
    @Before
    public void beforeTestInit()
    {
    
        int tileId = 9;
        int tileColours[] = {0,1,2,3};
        int tileScale = 1;

        aTile = new Tile(tileId, tileColours, tileScale);
    }
    
    @Test
    public void testTile() {
        int id = 9;
        int colours[] = {0,1,2,3};
        int scale = 1;


        Tile anotherTile = new Tile(id, colours, scale);
        
        if(anotherTile == null)
            fail("failed to create anotherTile");
    }

    @Test
    public void testCornerEdgeCountCanBeReinitialized() {
        int[] cornerColours = {0, 0, 2, 3};
        int[] rotatedCornerColours = {1, 0, 0, 3};

        aTile.initTileColours(cornerColours);
        assertEquals(2, aTile.checkForEdgeSegments());

        aTile.initTileColours(rotatedCornerColours);
        assertEquals(2, aTile.checkForEdgeSegments());
    }

}
