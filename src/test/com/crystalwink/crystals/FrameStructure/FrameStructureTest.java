package test.com.crystalwink.crystals.FrameStructure;

import static org.junit.Assert.*;

import java.awt.Panel;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.crystalwink.crystals.Crystal;
import com.crystalwink.crystals.FrameStructure.FrameStructure;
import com.crystalwink.crystals.FrameStructure.GridFrameStructure;
import com.crystalwink.crystals.FrameStructure.SimpleFrame;
import com.crystalwink.crystals.GraphPanel.GraphPanel;
import com.crystalwink.crystals.TilePrintableAwt.TilePrintableAwt;
import com.crystalwink.crystalscommon.CrystalGlobals;
import com.crystalwink.crystalscommon.Tile.Tile;

public class FrameStructureTest {

    FrameStructure aFrameStructure;

    // 4x4 frame
    //int totalTestTiles  = 16;
    // 16x16 frame
//    int totalTestTiles  = 256;
//    Tile tilesTestSet[] = new Tile[totalTestTiles];

    @Before
    public final void beginFrameStructureTest() {
        
        System.out.println("beginFrameStructureTest() - Start");

//        int verticalRowTiles      = 4;
//        int horizontalColumnCells = 4;
//        
//        for( int tileIndex = 0; tileIndex<totalTestTiles; tileIndex++)
//        {
//            int colours[] = {-1,-1,-1,-1};  // TODO not enough colours
//            int scale = 1;
//            int tileId = tileIndex+1;
//            tilesTestSet[tileIndex] = new Tile(tileId, colours, scale);
//        }
//        
//        aFrameStructure = new FrameStructure(verticalRowTiles, horizontalColumnCells, tilesTestSet, verticalRowTiles*horizontalColumnCells);
//        aFrameStructure.loadTilesStatic4x4(); // 
//        aFrameStructure.initFrame(verticalRowTiles, horizontalColumnCells);
//        
//        //aFrameStructure.getGridIndex(ROW_COL_SELECTION, row, col, SEGMENT_INDEX);
//        // TODO verify structure
//        aFrameStructure.dumpFrameToTxt();
        
        System.out.println("beginFrameStructureTest() - End");
        
    }

    @Test
    public void testFrameStructure() {
        
        System.out.println("testFrameStructure() - Start");
        int totalTestTiles = 16;  // 4x4
        Tile tilesTestSet[] = new Tile[totalTestTiles];
        
        for( int tileId = 0; tileId<totalTestTiles; tileId++)
        {
            int colours[] = {-1,-1,-1,-1};
            int scale = 1;
            tilesTestSet[tileId] = new Tile(tileId+1, colours, scale);
        }
        
        // TODO populate tilesTestSet from tile definition file
        // TODO loadTilesFromFile populates a panel.tiles this need to be moved as panel is not relavent to this test 
        Crystal.loadTilesFromFile("/home/dean.clark/workspace/Codility/Crystals/src/e2pieces.txt");    // use local tile definition

        int verticalRowTiles      = 4;
        int horizontalColumnCells = 4;
        int ntiles = totalTestTiles;
        FrameStructure anotherFrameStructure = new FrameStructure(verticalRowTiles, horizontalColumnCells, tilesTestSet, ntiles);
        // for some reason availableColoursFromTiles is called within initFrame which is called in the constructor FrameStructure() 

        anotherFrameStructure.loadTilesStatic4x4(); // 
        anotherFrameStructure.initFrame(verticalRowTiles, horizontalColumnCells);
        
        
        // TODO verify structure
        anotherFrameStructure.dumpFrameToTxt();
        
        System.out.println("testFrameStructure() - End");
        fail("test implemention not complete"); // TODO
    }


    @Test
    public final void testFindSubNode() {
        System.out.println("testFindSubNode() - Start");
        System.out.println("testFindSubNode() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testInitFrame() {
        System.out.println("testInitFrame() - Start");
        System.out.println("testInitFrame() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testSetEdge() {
        System.out.println("testSetEdge() - Start");
        System.out.println("testSetEdge() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testDefineTileFromFrame() {
        System.out.println("testDefineTileFromFrame() - Start");
        System.out.println("testDefineTileFromFrame() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testTestFrameSerialisation() {
        System.out.println("testTestFrameSerialisation() - Start");
        System.out.println("testTestFrameSerialisation() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testDrawFrame() {
        System.out.println("testDrawFrame() - Start");
        System.out.println("testDrawFrame() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testArrayIndexWithinRange() {
        System.out.println("testArrayIndexWithinRange() - Start");
        System.out.println("testArrayIndexWithinRange() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testResetTile() {
        System.out.println("testResetTile() - Start");
        System.out.println("testResetTile() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testHintToFrame() {
        System.out.println("testHintToFrame() - Start");
        System.out.println("testHintToFrame() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testVerifyFrame() {
        System.out.println("testVerifyFrame() - Start");
        System.out.println("testVerifyFrame() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testRandomizeTileUntilValid() {
        System.out.println("testRandomizeTileUntilValid() - Start");
        System.out.println("testRandomizeTileUntilValid() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testCompareTiles() {
        System.out.println("testCompareTiles() - Start");
        System.out.println("testCompareTiles() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testValidSegmentRecursion() {
        System.out.println("testValidSegmentRecursion() - Start");
        System.out.println("testValidSegmentRecursion() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testAvailableColoursFromTiles() {
        System.out.println("testAvailableColoursFromTiles() - Start");
        System.out.println("testAvailableColoursFromTiles() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testVerifyTile() {
        System.out.println("testVerifyTile() - Start");
        System.out.println("testVerifyTile() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testGridFrameStructure() {
        System.out.println("testGridFrameStructure() - Start");
        GridFrameStructure aTestGridFrameStructure = new GridFrameStructure();
        
        boolean result = aTestGridFrameStructure.testGridFrameStructure();
        assert(result);
        
        System.out.println("testGridFrameStructure() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testGetLastColourIndex() {
        System.out.println("testGetLastColourIndex() - Start");
        System.out.println("testGetLastColourIndex() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testSetLastColourIndex() {
        System.out.println("testSetLastColourIndex() - Start");
        System.out.println("testSetLastColourIndex() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testIsColourAvailable() {
        System.out.println("testIsColourAvailable() - Start");
        System.out.println("testIsColourAvailable() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testSetMaxColoursIndex() {
        System.out.println("testSetMaxColoursIndex() - Start");
        System.out.println("testSetMaxColoursIndex() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testListAvailableColours() {
        System.out.println("testListAvailableColours() - Start");
        System.out.println("testListAvailableColours() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testDumpFrameToTxt() {
        System.out.println("testDumpFrameToTxt() - Start");
        System.out.println("testDumpFrameToTxt() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testClearEntireFrame() {
        System.out.println("testClearEntireFrame() - Start");
        System.out.println("testClearEntireFrame() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testSetColour() {
        System.out.println("testSetColour() - Start");
        System.out.println("testSetColour() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testClearGridColour() {
        System.out.println("testClearGridColour() - Start");
        System.out.println("testClearGridColour() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testGetAvailableGridColours() {
        System.out.println("testGetAvailableGridColours() - Start");
        System.out.println("testGetAvailableGridColours() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testIsLockedGridLocation() {
        System.out.println("testIsLockedGridLocation() - Start");
        System.out.println("testIsLockedGridLocation() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testSelectRandomColour() {
        System.out.println("testSelectRandomColour() - Start");
        // check for each frame location
        int rowI = 0;
        int colI = 0;
        
        if(aFrameStructure != null)
            aFrameStructure.selectRandomColour(rowI, colI);
        else
            fail("aFrameStructure does not exist for test testSelectRandomColour to run"); // TODO

        System.out.println("testSelectRandomColour() - End");
        //fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testRandomizeUnlockedSegments() {
        System.out.println("testRandomizeUnlockedSegments() - Start");
        System.out.println("testRandomizeUnlockedSegments() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testIsCorner() {
        System.out.println("testIsCorner() - Start");
        System.out.println("testIsCorner() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testRandomizeGridFrame() {
        System.out.println("testRandomizeGridFrame() - Start");
        System.out.println("testRandomizeGridFrame() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testResetGridFrame() {
        System.out.println("testResetGridFrame() - Start");
        System.out.println("testResetGridFrame() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testResetGridFrameEdges() {
        System.out.println("testResetGridFrameEdges() - Start");
        System.out.println("testResetGridFrameEdges() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testIsGridCellLocked() {
        System.out.println("testIsGridCellLocked() - Start");
        System.out.println("testIsGridCellLocked() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testGridCellFullyPlacedUp() {
        System.out.println("testGridCellFullyPlacedUp() - Start");
        System.out.println("testGridCellFullyPlacedUp() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testGridCellFullyPlacedDown() {
        System.out.println("testGridCellFullyPlacedDown() - Start");
        System.out.println("testGridCellFullyPlacedDown() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testGridCellFullyPlacedGet() {
        System.out.println("testGridCellFullyPlacedGet() - Start");
        System.out.println("testGridCellFullyPlacedGet() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testCellWithMostLockedNeighbours() {
        System.out.println("testCellWithMostLockedNeighbours() - Start");
        System.out.println("testCellWithMostLockedNeighbours() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testScoreLock() {
        System.out.println("testScoreLock() - Start");
        System.out.println("testScoreLock() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testRowColToSerialisedFrameNumber() {
        System.out.println("testRowColToSerialisedFrameNumber() - Start");
        System.out.println("testRowColToSerialisedFrameNumber() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testRowFromSerialisedFrameNumber() {
        System.out.println("testRowFromSerialisedFrameNumber() - Start");
        System.out.println("testRowFromSerialisedFrameNumber() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testColFromSerialisedFrameNumber() {
        System.out.println("testColFromSerialisedFrameNumber() - Start");
        System.out.println("testColFromSerialisedFrameNumber() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testLoadTilesStatic4x4() {
        System.out.println("testLoadTilesStatic4x4() - Start");
        
        int verticalRowTiles      = 4;
        int horizontalColumnCells = 4;
        int totalTestTiles  = 16;

        // 4x4 frame
        Tile tilesTestSet[] = new Tile[totalTestTiles];

        FrameStructure anotherFrameStructure = new FrameStructure(verticalRowTiles, horizontalColumnCells, tilesTestSet, totalTestTiles);
        // for some reason availableColoursFromTiles is called within initFrame which is called in the constructor FrameStructure() 

        anotherFrameStructure.loadTilesStatic4x4(); // 
        anotherFrameStructure.initFrame(verticalRowTiles, horizontalColumnCells);

        // TODO verify structure
        anotherFrameStructure.dumpFrameToTxt();
        
        System.out.println("testLoadTilesStatic4x4() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testLoadTilesStatic16x16() {
        System.out.println("testLoadTilesStatic16x16() - Start");
        int verticalRowTiles      = 8;
        int horizontalColumnCells = 8;
        
        int totalTestTiles  = 256;
        Tile tilesTestSet[] = new Tile[totalTestTiles];

        int ntiles = totalTestTiles;

        FrameStructure anotherFrameStructure = new FrameStructure(verticalRowTiles, horizontalColumnCells, tilesTestSet, ntiles);
        // for some reason availableColoursFromTiles is called within initFrame which is called in the constructor FrameStructure() 

        if(anotherFrameStructure != null)
        {
            anotherFrameStructure.loadTilesStatic16x16(); //
            anotherFrameStructure.initFrame(verticalRowTiles, horizontalColumnCells);
    
            // TODO verify structure
            anotherFrameStructure.dumpFrameToTxt();
        }
        else
            fail("anotherFrameStructure does not exist for testLoadTilesStatic16x16 test to run!"); // TODO

        
        System.out.println("testLoadTilesStatic16x16() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testGetGridIndex() {
        
        System.out.println("testGetGridIndex() - Start");
        /**
         * From 16x16 get grid coordinates
         */
        //public int getGridIndex() {

        int ROW_COL_SELECTION = 0;
        int row = 0;
        int col = 0;
        int SEGMENT_INDEX = 0;
        
        if(aFrameStructure != null)
            aFrameStructure.getGridIndex(ROW_COL_SELECTION, row, col, SEGMENT_INDEX);
        else
            fail("aFrameStructure does not exist for test testGetGridIndex to run"); // TODO
        
        System.out.println("testGetGridIndex() - End");
    }

    @Test
    public final void testGridFrameStructureTest() {
        System.out.println("testGetGridIndex() - Start");
        System.out.println("testGetGridIndex() - End");
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testSimpleFrame() {
        
        System.out.println("testSimpleFrame() - Start");
            SimpleFrame aTestFrame = new SimpleFrame();

            if(aTestFrame == null)
                fail("Failed to new SimpleFrame");
                
            int maxTiles = 0;
            int combinedtileId = 0;
            
            
            for(int ii=0; ii<CrystalGlobals.MAX_ROWS; ii++)
            {
                for(int jj=0; jj<CrystalGlobals.MAX_COLUMNS; jj++)
                {
                    aTestFrame.simpleFrameArray[ii][jj].tileId      = (jj+1) + (CrystalGlobals.MAX_COLUMNS*ii);
                    aTestFrame.simpleFrameArray[ii][jj].orientation = 1;  // max number of sides
                    
                    maxTiles++;
                    
                    combinedtileId += aTestFrame.simpleFrameArray[ii][jj].tileId; // 32896 ( half of the maxTiles
                    assertEquals( maxTiles, aTestFrame.simpleFrameArray[ii][jj].tileId); // should follow the tile init
                }
            }
            
            // sum all cells and verify result
            assert(maxTiles != 256); // should be 256

            combinedtileId -= maxTiles/2;  // triangular sumation of the sequence from 1 through 256 is 32768
            long expectedSum   = Math.round(Math.pow(CrystalGlobals.MAX_ROWS*CrystalGlobals.MAX_COLUMNS,2) /2); // 32768

            assertEquals( expectedSum, combinedtileId);
            
            System.out.println("testSimpleFrame() - End");
    }


}
