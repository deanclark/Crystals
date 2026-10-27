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

    @Before
    public final void beginFrameStructureTest() {
        
        System.out.println("beginFrameStructureTest() - Start");

        // 4x4 frame
        int totalTestTiles  = 16;
        Tile tilesTestSet[] = new Tile[totalTestTiles];
        
        int verticalRowTiles      = 4;
        int horizontalColumnCells = 4;
        
        for( int tileIndex = 0; tileIndex<totalTestTiles; tileIndex++)
        {
            int colours[] = {-1,-1,-1,-1};  // TODO not enough colours
            int scale = 1;
            int tileId = tileIndex+1;
            tilesTestSet[tileIndex] = new Tile(tileId, colours, scale);
        }
        
        
        System.out.println("       0, 1, 2, 3, 4");
        System.out.println("   0    -  -  -  -  ");
        System.out.println("   1   |  |  |  |  |");
        System.out.println("   2    -  -  -  -  ");
        System.out.println("   3   |  |  |  |  |");
        System.out.println("   4    -  -  -  -  ");
        System.out.println("   5   |  |  |  |  |");
        System.out.println("   6    -  -  -  -  ");
        System.out.println("   7   |  |  |  |  |");
        System.out.println("   8    -  -  -  -  ");

        aFrameStructure = new FrameStructure(verticalRowTiles, horizontalColumnCells, tilesTestSet, verticalRowTiles*horizontalColumnCells);
        aFrameStructure.loadTilesStatic4x4(); // 
        aFrameStructure.initFrame(verticalRowTiles, horizontalColumnCells);
        
        //aFrameStructure.getGridIndex(ROW_COL_SELECTION, row, col, SEGMENT_INDEX);
        // TODO verify structure
        aFrameStructure.dumpFrameToTxt();
        
        System.out.println("beginFrameStructureTest() - End");
        
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
    public final void testLoadTilesStatic16x16() {
        System.out.println("testLoadTilesStatic16x16() - Start");
        int verticalRowTiles      = 16;
        int horizontalColumnCells = 16;
        
        int totalTestTiles  = 256;
        Tile tilesTestSet[] = new Tile[totalTestTiles];
        for( int tileId = 0; tileId<totalTestTiles; tileId++)
        {
            //int colours[] = {-1,-1,-1,-1};
            int colours[] = {0,1,2,3};
            int scale = 1;
            tilesTestSet[tileId] = new Tile(tileId+1, colours, scale);
        }
        

//        int ntiles = totalTestTiles;

        FrameStructure anotherFrameStructure = new FrameStructure(verticalRowTiles, horizontalColumnCells, tilesTestSet, totalTestTiles);
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
        //fail("Not yet implemented"); // TODO
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
            //assert(maxTiles != 256); // should be 256
            assertEquals( maxTiles, 256);

            combinedtileId -= maxTiles/2;  // triangular sumation of the sequence from 1 through 256 is 32768
            long expectedSum   = Math.round(Math.pow(CrystalGlobals.MAX_ROWS*CrystalGlobals.MAX_COLUMNS,2) /2); // 32768

            assertEquals( expectedSum, combinedtileId);
            
            System.out.println("testSimpleFrame() - End");
    }


}
