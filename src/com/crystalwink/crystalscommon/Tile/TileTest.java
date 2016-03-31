package com.crystalwink.crystalscommon.Tile;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.crystalwink.crystalscommon.Segment.Segment;

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
    public void testLocked() {
        aTile.locked();
        //fail("Not yet implemented");
    }

    @Test
    public void testMoved() {
        aTile.moved();
        //fail("Not yet implemented");
    }

    @Test
    public void testMovedBoolean() {
        
        boolean hasMoved = true;
        aTile.moved(hasMoved);
        //fail("Not yet implemented");
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
    public void testTileScaled() {
        int id = 9;
        int colours[] = {0,1,2,3};
        int scale = 1;

        aTile.TileScaled(id, colours, scale);
        
        //fail("Not yet implemented");
    }

    @Test
    public void testGetTileColours() {
        
        aTile.initTile(0);
        
        // init tile segment where segmentCount = 4
        int[] coloursSet = {1,2,3,4};
        aTile.initTileColours(coloursSet);
        
        int[] coloursGet = new int[4];
        aTile.getTileColours(coloursGet);
        
        //fail("Not yet implemented");
    }

    @Test
    public void testInitTileColours() {
        int[] coloursSet = {1,2,3,4};
        aTile.initTileColours(coloursSet);
        
        //fail("Not yet implemented");
    }

    @Test
    public void testInitTile() {
        aTile.initTile(0);
        //fail("Not yet implemented");
    }

    @Test
    public void testGetSegmentScale() {
        aTile.getSegmentScale();
        //fail("Not yet implemented");
    }

    @Test
    public void testSetSegmentScale() {
        aTile.setSegmentScale(1);
        //fail("Not yet implemented");
    }

    @Test
    public void testGetOrientationInt() {
        aTile.getOrientation(0);
        aTile.getOrientation();

        //fail("Not yet implemented");
    }

    @Test
    public void testRotationZ() {
//        Segment anObject = null;
//        Tile viewer = null;
//        aTile.rotationZ(anObject, viewer);
        fail("Not yet implemented");
    }

    @Test
    public void testThreeDcross14() {
//        double[] mat1 = null;
//        double[] mat2 = null;
//        double[] mat3 = null;
//        
//        aTile.threeDcross14(mat1, mat2, mat3);
        fail("Not yet implemented");
    }

    @Test
    public void testMoveTile() {
        aTile.moveTile(0, 0);
        //fail("Not yet implemented");
    }

    @Test
    public void testGetEdgeAdjacent() {
        aTile.getEdgeAdjacent(0);
        //fail("Not yet implemented");
    }

    @Test
    public void testSetEdgeOposite() {
        aTile.setEdgeOposite();
        //fail("Not yet implemented");
    }

    @Test
    public void testGetEdgeOposite() {
        aTile.getEdgeOposite();
        //fail("Not yet implemented");
    }

    @Test
    public void testGetEdgeCount() {
        
        aTile.getEdgeCount();
        
        //fail("Not yet implemented");
    }

    @Test
    public void testCheckForEdgeSegments() {
        aTile.checkForEdgeSegments();
        //fail("Not yet implemented");
    }

}
