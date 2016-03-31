/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.crystalwink.crystals.FrameStructure;

import org.junit.Test;

import com.crystalwink.crystalscommon.CrystalGlobals;
import com.crystalwink.crystalscommon.Trace;
import com.crystalwink.crystalscommon.TilePlacementInfo.TilePlacementInfo;
/**
 *
 * @author Dean Clark deancl
 */
public class GridFrameStructure extends SimpleFrame {
    public static final int ORIGIN_FROM_HINT = 0;
    public static final int ORIGIN_FROM_EDGE = 1;
    public static final int ORIGIN_FROM_CORNER = 2;
    public static final int ORIGIN_FROM_CORNER_EDGE_HINT_EQUAL = 3;
    public static final int ORIGIN_FROM_HINT_SEED = 4;

    /////////////////////
    //  CONFIGURATION  //
    /////////////////////
    // When "false" a single path is used, this appears to reach a dead end faster than multiple selection and simplifies backtracking.
    // When "true" the placement looks interesting but is very slow and causes the computer to be slow to respond to mouse and kwyboard input 
    boolean selectFromAllQualifyingCells = false;  // Random selection from a list of all qualifying cells with equal neighbour score.
    
    
    //public static int scoreLockMode = ORIGIN_FROM_HINT;
    //public static int scoreLockMode = ORIGIN_FROM_EDGE;
    //public static int scoreLockMode = ORIGIN_FROM_CORNER;
    public static int scoreLockMode = ORIGIN_FROM_CORNER_EDGE_HINT_EQUAL;
    //public static int scoreLockMode = ORIGIN_FROM_HINT_SEED;

    static int ROW_INDEX = 0;
    static int COL_INDEX = 1;

    private int lastColourIndex = 0;
    private int gridCellFullyPlaced = 0;

    int VerticalRowCells = CrystalGlobals.MAX_ROWS;
    int HorizontalColumnCells = CrystalGlobals.MAX_COLUMNS;

    /*
     * 3x3 frame requires 7 rows and 4 columns to represent each of the edges
     * 
     *       0, 1, 2, 3
     *   0    -  -  -
     *   1   |  |  |  |
     *   2    -  -  -
     *   3   |  |  |  |
     *   4    -  -  -
     *   5   |  |  |  |
     *   6    -  -  - 
     *          
     *      therefore a 16x16 frame requires 33 rows, 17 columns
     */
    protected int nGridRows = (CrystalGlobals.MAX_ROWS * 2) + 1;  // (modified in initFrame)
    protected int nGridColumns = CrystalGlobals.MAX_COLUMNS + 1;  // one extra for vertical cells only (modified in initFrame)
    int totalColours[] = new int[CrystalGlobals.MAX_COLORS];    // max 23

    int frameArray[][]; // = new int[nGridRows][nGridColumns];
    int  lockArray[][]; // = new int[nGridRows][nGridColumns];

    TilePlacementInfo tilePlacementOrder2[] = new TilePlacementInfo[CrystalGlobals.MAX_TILES];  // [placementOrder][row, col]

    boolean singlePathMode = true; // @todo - debug
    boolean debugSinglePath = false;


    public void GridFrameStructure() {
        frameArray = new int[nGridRows][nGridColumns];
        
        if(!testGridFrameStructure())  // 
            System.out.println("GridFrameStructure() failed to verify frameArray[][] using testGridFrameStructure()");
    }

    /**
     * verify that the grid indexing does not go outside of the array bounds 
     * 
     * @return testPassed
     */
    public boolean testGridFrameStructure() {
        
        boolean testPassed = true;
        
        // Test getGridIndex
        System.out.println("TEST getGridIndex - Start");
        int tRow = 0;
        int tCol = 0;
        for(int rowIndex=1; rowIndex <= CrystalGlobals.MAX_ROWS; rowIndex++){
            for(int colIndex=1; colIndex <= CrystalGlobals.MAX_COLUMNS; colIndex++){
                for(int segmentIndex = CrystalGlobals.SEGMENT_INDEX_ABOVE; segmentIndex <= CrystalGlobals.SEGMENT_INDEX_LEFT; segmentIndex++){
                    for(int rowColIndex = CrystalGlobals.ROW_SELECTION; rowColIndex <= CrystalGlobals.COL_SELECTION; rowColIndex++){
                        if(rowColIndex == CrystalGlobals.ROW_SELECTION)
                            tRow = getGridIndex(CrystalGlobals.ROW_SELECTION, rowIndex, colIndex, segmentIndex);
                        else
                            tCol = getGridIndex(CrystalGlobals.COL_SELECTION, rowIndex, colIndex, segmentIndex);

                    }
                    if( tRow < 0)
                    {
                        System.out.println("TEST getGridIndex ROW(" + segmentIndex + ") row=" + rowIndex + " col= " + colIndex + " ** OUT OF RANGE **");
                        testPassed = false;
                    }

                    if( tCol < 0)
                    {
                        System.out.println("TEST getGridIndex COL(" + segmentIndex + ") row=" + rowIndex + " col= " + colIndex + " ** OUT OF RANGE **");
                        testPassed = false;
                    }

                    System.out.print(" (" + segmentIndex + ")" + tRow + "," + tCol + "  ");
                }
            }
            System.out.println("");

        }
        System.out.println("TEST getGridIndex - End");
        return testPassed;
    }

    public int GetLastColourIndex() {
        return lastColourIndex;
    }

    public void SetLastColourIndex(int lastColour) {
        lastColourIndex = lastColour;

        if(lastColourIndex == 0)
            System.out.println("SetLastColourIndex = " + lastColourIndex + " ** OUT OF COLOURS **");
    }

    public boolean isColourAvailable(int tempColour) {
        if (totalColours[tempColour] > 0) {
            return true;
        }
        return false;
    }

    /**
     * on init set limit the number of colours in use
     */
    public void setMaxColoursIndex() {
        for (int ii = 0; ii < CrystalGlobals.MAX_COLORS; ii++) {
            if (isColourAvailable(ii)) {
                SetLastColourIndex(ii);
            }
        }
        // debug missing colours (total 1000 should be 1024
        //System.out.println("setMaxColoursIndex GetLastColourIndex() " + GetLastColourIndex());
    }

    /**
     * number of colours available - can be used to verify a match of uncoloured tiles to colours available
     */
    public void listAvailableColours() {
        int totalAvailableColours = 0;
        System.out.println("listAvailableColours lastColourIndex " + lastColourIndex);
        for (int ii = 0; ii <= lastColourIndex; ii++) {
            System.out.println("listAvailableColours colour[" + ii + "] " + totalColours[ii]);
            totalAvailableColours += totalColours[ii];
        }

        // debug missing colours (total 1000 should be 1024
        System.out.println("listAvailableColours totalAvailableColours " + totalAvailableColours);

    }



    public void dumpFrameToTxt() {
        // text dump of initial frame
        for (int rowI = 0; rowI < nGridRows; rowI++) {
            System.out.print(rowI + "  ");

            // if mod
            if (rowI % 2 == 0) // even
            {
                for (int colI = 0; colI < nGridColumns - 1; colI++) {
                    System.out.print(" " + frameArray[rowI][colI]);
                }
            } else {
                for (int colI = 0; colI < nGridColumns; colI++) {
                    System.out.print(" " + frameArray[rowI][colI]);
                }
            }

            System.out.println("");
        }
        System.out.println("");
    }

    /**
     * clear frame
     */
    public void clearEntireFrame() {
        // reset all cells invalid (-1)
        for (int rowI = 0; rowI < nGridRows; rowI++) {
            for (int colI = 0; colI < nGridColumns; colI++) {
                // remove used colour from available colour array
                //--totalColours[frameArray[rowI][colI]];
                //if(lockArray[rowI][colI] < UNLOCKED)
                clearGridColour(rowI, colI);

                // set colour
                frameArray[rowI][colI] = -1;

                // @todo - lock hint tiles, i.e. non-removable
                lockArray[rowI][colI] = CrystalGlobals.UNLOCKED;

            }
        }
//      System.out.println("clearEntireFrame() - available grey after clear " + totalColours[GraphPanel.COLOUR_GREY] + " total available:" + getAvailableGridColours());
        System.out.println("clearEntireFrame() - available grey after clear " + totalColours[CrystalGlobals.COLOUR_GREY] + " total available:" + getAvailableGridColours());
    }

    // take a item from the pot to avoid duplicate use 
    public int setColour(int rowX, int colY, int colourIndex, int lockLevel) {
        int status = CrystalGlobals.STATUS_FAIL_UNAVAILABLE; // failure

        if (!isColourAvailable(colourIndex)) {
            System.out.println("setColour() - Colour " + colourIndex + " not available for rowX:" + rowX + " colY:" + colY);
        } else {

            // check bounds as edge tiles may only use edge colours (i.e. grey)
//            if((rowX>=0 && rowX<VerticalRowCells) && (colY>=0 && colY<HorizontalColumnCells))
            {
                status = CrystalGlobals.STATUS_FAIL;
                if (lockArray[rowX][colY] == CrystalGlobals.UNLOCKED) {
                    // set colour
                    frameArray[rowX][colY] = colourIndex;

                    // @todo - lock hint tiles, i.e. non-removable
                    lockArray[rowX][colY] = lockLevel;

                    // remove used colour from available colour array
                    //if(lockLevel < CrystalGlobals.UNLOCKED)
                    --totalColours[colourIndex];

                    status = CrystalGlobals.STATUS_OK;
                }
            }
        }
        return status;
    }

    // remove the current assignment, placing it back in the pot for later use 
    public int clearGridColour(int rowX, int colY) {
        int cleared = -1;

        // not already clear
        if (frameArray[rowX][colY] > 0) {  // WAS =0 which would prevent the grey from being removed.  Was this intentional?
            // return colour to available colour array
            
            //if(lockArray[rowX][colY] < CrystalGlobals.UNLOCKED)
            ++totalColours[frameArray[rowX][colY]];

            // set colour cleared
            frameArray[rowX][colY] = -1;

            // @todo - lock hint tiles, i.e. non-removable
            lockArray[rowX][colY] = CrystalGlobals.UNLOCKED;

            cleared = 0;
        }

        return cleared;
    }


    /**
     * number of colours available - can be used to verify a match of uncoloured tiles to colours available
     */
    public int getAvailableGridColours() {
        int coloursAvailable = 0;

//        for (int ii = 0; ii <= lastColourIndex; ii++) {
        for (int ii = 0; ii < CrystalGlobals.MAX_COLORS; ii++) {
            coloursAvailable += totalColours[ii];
        }

        // debug missing colours (total 1000 should be 1024
        //System.out.println("getAvailableGridColours coloursAvailable " + coloursAvailable);

        return coloursAvailable;
    }


    public boolean isLockedGridLocation(int rowI, int colI) {
        //GridLocation thisSegment = new GridLocation();

        // range check

        if (lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)] == CrystalGlobals.UNLOCKED) {
            return false;
        }
        return true;
    }

    public int selectRandomColour(int rowI, int colI) {
        int tempColour = 0;

        // Limit colour selection if corner or edge to reduce backtrack
        if (isCorner(rowI, colI)) {
            // limit available tiles to those used in corner tiles
            tempColour = (int) Math.round((CrystalGlobals.maxCornerColoursInUse - 1) * Math.random()); // +1 to exclude gray
            // map corner colours
            switch (tempColour) {
                case 0:
                    tempColour = 1;   // @todo - extract corner colours from loaded tile info
//                    System.out.println("randomizeUnlockedSegments() - INFO mapping corner tile colours 0 to 1");
                    break;
                case 1:
                    tempColour = 5;
//                    System.out.println("randomizeUnlockedSegments() - INFO mapping corner tile colours 1 to 5");
                    break;
                case 2:
                    tempColour = 9;
//                    System.out.println("randomizeUnlockedSegments() - INFO mapping corner tile colours 2 to 9");
                    break;
                case 3:
                    tempColour = 17;
//                    System.out.println("randomizeUnlockedSegments() - INFO mapping corner tile colours 3 to 17");
                    break;
                default:
                    System.out.println("randomizeUnlockedSegments() - ERROR mapping corner tile colours");
                    break;

            }
        } else {
            tempColour = (int) Math.round((GetLastColourIndex() - 1) * Math.random()) + 1; // +1 to exclude gray
        }
        // if the colour is available
        if (!isColourAvailable(tempColour)) {
            tempColour = -1;
        }

        return tempColour;
    }

    /*
     * Only usable when alternate colours are available i.e. following a resetFrame
     */
    public void randomizeUnlockedSegments(int rowI, int colI) {
        int tempColour = 0;

        //isLockedGridLocation(rowI, colI);
        if (lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)] == CrystalGlobals.UNLOCKED &&
                frameArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)] == -1) {

            tempColour = selectRandomColour(rowI, colI);
            // if the colour is available
            if (isColourAvailable(tempColour)) {
                if (setColour(getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE),getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE), tempColour, CrystalGlobals.UNLOCKED) != 0) {
                //if (setColour((rowI - 1) * 2, colI - 1, tempColour, CrystalGlobals.UNLOCKED) != 0) {
                    System.out.println("randomizeUnlockedSegments() - checked if out before trying this. out of available colours - This Should never be possible. (TOP) row:" + rowI + " col:" + colI);
                }
            } else {
                System.out.println("randomizeUnlockedSegments() - colour unavailable " + tempColour);
            }
        }

        if (lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT)] == CrystalGlobals.UNLOCKED &&
                frameArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT)] == -1) {
//            tempColour = (int) Math.round((lastColourIndex - 1) * Math.random()) + 1; // +1 to exclude gray
            tempColour = selectRandomColour(rowI, colI);

            // if the colour is available
            if (isColourAvailable(tempColour)) {
                if (setColour(getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT),getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT), tempColour, CrystalGlobals.UNLOCKED) != 0) {
//                if (setColour(((rowI - 1) * 2) + 1, colI, tempColour, CrystalGlobals.UNLOCKED) != 0) {
                    System.out.println("randomizeUnlockedSegments() - checked if out before trying this. out of available colours - This Should never be possible. (RIGHT) row:" + rowI + " col:" + colI);
                }
            } else {
                System.out.println("randomizeUnlockedSegments() - colour unavailable " + tempColour);
            }
        }
        // above
        if (lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW)] == CrystalGlobals.UNLOCKED &&
                frameArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW)] == -1) {
//            tempColour = (int) Math.round((lastColourIndex - 1) * Math.random()) + 1; // +1 to exclude gray
            tempColour = selectRandomColour(rowI, colI);

            // if the colour is available
            if (isColourAvailable(tempColour)) {
                if (setColour(getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW),getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW), tempColour, CrystalGlobals.UNLOCKED) != 0) {
//                if (setColour(((rowI - 1) * 2) + 2, colI - 1, tempColour, CrystalGlobals.UNLOCKED) != 0) {
                    System.out.println("randomizeUnlockedSegments() - checked if out before trying this. out of available colours - This Should never be possible. (BOTTOM) row:" + rowI + " col:" + colI);
                }
            } else {
                System.out.println("randomizeUnlockedSegments() - colour unavailable " + tempColour);
            }
        }

        if (lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT)] == CrystalGlobals.UNLOCKED &&
                frameArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT)] == -1) {
//            tempColour = (int) Math.round((lastColourIndex - 1) * Math.random()) + 1; // +1 to exclude gray
            tempColour = selectRandomColour(rowI, colI);

            // if the colour is available
            if (isColourAvailable(tempColour)) {
                if (setColour(getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT),getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT), tempColour, CrystalGlobals.UNLOCKED) != 0) {
//                if (setColour(((rowI - 1) * 2) + 1, colI - 1, tempColour, CrystalGlobals.UNLOCKED) != 0) {
                    System.out.println("randomizeUnlockedSegments() - checked if out before trying this. out of available colours - This Should never be possible. (LEFT) row:" + rowI + " col:" + colI);
                }
            } else {
                System.out.println("randomizeUnlockedSegments() - colour unavailable " + tempColour);
            }

        }

    }

    public boolean isCorner(int rowI, int colI) {
        boolean isCorner = false;
//        System.out.println("isCorner() - INFO checking row:" + rowI + " col:" + colI);

        // if tile is an edge it could be a corner
        if (rowI == 1 ||
                rowI == CrystalGlobals.MAX_ROWS ||
                colI == 1 ||
                colI == CrystalGlobals.MAX_COLUMNS) {

            int edgeCount = 0;

            // allow lock only if neighboring tile is CrystalGlobals.LOCKED or LOCLED_HINT
            if (frameArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)] == CrystalGlobals.COLOUR_GREY) // above?
            {
                ++edgeCount;
            }
            if (frameArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT)] == CrystalGlobals.COLOUR_GREY) // right?
            {
                ++edgeCount;
            }
            if (frameArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW)] == CrystalGlobals.COLOUR_GREY) // below
            {
                ++edgeCount;
            }
            if (frameArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT)] == CrystalGlobals.COLOUR_GREY) // left?
            {
                ++edgeCount;
            }

            if (edgeCount == 2) {
                isCorner = true;
            }

//            if(isCorner == true)
//                System.out.println("isCorner() - INFO row:" + rowI + " col:" + colI + " is corner");

            if (rowI == 1 && colI == 1 && isCorner != true) {
                System.out.println("isCorner() - Known Corner Tile has edgeCount count of " + edgeCount);
            }
        }

        return isCorner;
    }


    /**
     * randomize frame (only uninitialsed cells)
     * Can be called on a partially populated frame
     */
    public void randomizeGridFrame() {
        // @todo exclude grey
        // @todo select only colours from the pot of available colours
        // @todo randomly assign a colour to each dimond
//        int cellCount = 0;
//        System.out.println("randomizeGridFrame() - remaining " + getAvailableGridColours());

        for (int rowI = 0; rowI < nGridRows; rowI++) {
            if (rowI % 2 == 0) // even
            {
                for (int colI = 0; colI < nGridColumns - 1; colI++) {
                    while (frameArray[rowI][colI] == -1 && getAvailableGridColours() != 0) {
                        // select a random available colour
                        int tempColour = (int) Math.round((GetLastColourIndex() - 1) * Math.random()) + 1; // +1 to exclude gray

                        if (tempColour < 1 || tempColour > GetLastColourIndex()) {
                            System.out.println("randomizeFrame() - x:" + rowI + " y:" + colI + " colour:" + tempColour + ", colours available " + getAvailableGridColours());
                        }

                        // if the colour is available
                        if (isColourAvailable(tempColour)) {
                            if (setColour(rowI, colI, tempColour, CrystalGlobals.UNLOCKED) != 0) {
                                System.out.println("randomizeFrame() - checked if out before trying this. out of available colours - This Should never be possible. (ODD) row:" + rowI + " col:" + colI);
                            }
                        }

                        if (getAvailableGridColours() == 0 && frameArray[rowI][colI] == -1) {
                            System.out.println("randomizeFrame() - out of available colours - This Should never be possible");
                        }
                    }

                    if (frameArray[rowI][colI] == -1) {
                        System.out.println("randomizeFrame() - This Should never be possible (even) x:" + rowI + " y:" + colI + " col:" + frameArray[rowI][colI] + " remaining " + getAvailableGridColours());
                    }
                }
            } else // odd lines
            {
                for (int colI = 0; colI < nGridColumns; colI++) {
                    while (frameArray[rowI][colI] == -1 && getAvailableGridColours() != 0) {
                        // select a random available colour
                        int tempColour = (int) Math.round((GetLastColourIndex() - 1) * Math.random()) + 1; // +1 to exclude gray

                        if (tempColour < 1 || tempColour > GetLastColourIndex()) {
                            System.out.println("randomizeFrame() - x:" + rowI + " y:" + colI + " colour:" + tempColour + ", colours available " + getAvailableGridColours());
                        }

                        // if the colour is available
                        if (isColourAvailable(tempColour)) {
                            if (setColour(rowI, colI, tempColour, CrystalGlobals.UNLOCKED) != 0) {
                                System.out.println("randomizeFrame() - checked if out before trying this. out of available colours - This Should never be possible. (EVEN) row:" + rowI + " col:" + colI);
                            }
                        }

                        if (getAvailableGridColours() == 0 && frameArray[rowI][colI] == -1) {
                            System.out.println("randomizeFrame() - out of available colours - This Should never be possible");
                        }
                    }

                    if (frameArray[rowI][colI] == -1) {
                        System.out.println("randomizeFrame() - This Should never be possible (odd) x:" + rowI + " y:" + colI + " col:" + frameArray[rowI][colI] + " remaining " + getAvailableGridColours());
                    }
                }
            }
        }
        //dumpFrameToTxt();
    }


    /**
     * Set all edges within the frame to -1 and unlock
     */
    public int resetGridFrame() {
        int status = CrystalGlobals.STATUS_OK;

        // reset all cells invalid (-1)
        for (int rowI = 0; rowI < nGridRows; rowI++) {
            if (rowI % 2 == 0) // even
            {
                for (int colI = 0; colI < nGridColumns; colI++) {
                    // @todo - only unlocked faces
                    if (lockArray[rowI][colI] == CrystalGlobals.UNLOCKED) {
                        // return colour to available colour array before clearing
                        status = clearGridColour(rowI, colI);

//                        if(status != STATUS_OK)
//                            System.out.println("resetGridFrame() Failed to clearGridColour rowI:" + rowI + " colI:" + colI);

                    }
                }
            } else {
                for (int colI = 0; colI < nGridColumns - 1; colI++) {
                    // @todo - only unlocked faces
                    if (lockArray[rowI][colI] == CrystalGlobals.UNLOCKED) {
                        // return colour to available colour array before clearing
                        status = clearGridColour(rowI, colI);

//                        if(status != STATUS_OK)
//                            System.out.println("resetGridFrame() Failed to clearGridColour rowI:" + rowI + " colI:" + colI);
                    }
                }
            }
        }
        return status;
    }

    /**
     * Set all edges around the frame to COLOUR_GREY and lock
     * 3x3 frame requires 4x3 and a 3x3 to represent each of the edges
     * 
     *       -  -  -
     *      |  |  |  |
     *       -  -  -
     *      |  |  |  |
     *       -  -  -
     *      |  |  |  |
     *       -  -  -
     *       
     *       nGridColumns and nGridRows are initialised as final within the class definition 
     *      
     */
    public int resetGridFrameEdges() {
        int status = CrystalGlobals.STATUS_OK;

        // set outer edges grey
        // set top and bottom rows to edge
        for (int colI = 0; colI < nGridColumns - 1; colI++) {
            // @todo - only unlocked faces
            if (lockArray[0][colI] != CrystalGlobals.LOCKED_EDGE) {
                status = setColour(0, colI, CrystalGlobals.COLOUR_GREY, CrystalGlobals.LOCKED_EDGE);

                if(status != CrystalGlobals.STATUS_OK)
                    System.out.println("resetFrame() Failed to setColour rowI:" + 0 + " colI:" + colI);
            }

            if (lockArray[nGridRows - 1][colI] != CrystalGlobals.LOCKED_EDGE) {
                status = setColour(nGridRows - 1, colI, CrystalGlobals.COLOUR_GREY, CrystalGlobals.LOCKED_EDGE);

                if(status != CrystalGlobals.STATUS_OK)
                    System.out.println("resetFrame() Failed to setColour rowI:" + (nGridRows - 1) + " colI:" + colI);
            }
        }

        //dumpFrameToTxt();

        // set left and right Column to edge
        for (int rowI = 0; rowI < nGridRows; rowI++) {
            if (rowI % 2 != 0) // odd
            {
                if (lockArray[rowI][0] != CrystalGlobals.LOCKED_EDGE) {
                    status = setColour(rowI, 0, CrystalGlobals.COLOUR_GREY, CrystalGlobals.LOCKED_EDGE);

                    if(status != CrystalGlobals.STATUS_OK)
                        System.out.println("resetFrame() Failed to setColour rowI:" + rowI + " colI:" + 0);
                }

                if (lockArray[rowI][nGridColumns - 1] != CrystalGlobals.LOCKED_EDGE) {
                    status = setColour(rowI, nGridColumns - 1, CrystalGlobals.COLOUR_GREY, CrystalGlobals.LOCKED_EDGE);

                    if(status != CrystalGlobals.STATUS_OK)
                        System.out.println("resetFrame() Failed to setColour rowI:" + rowI + " colI:" + (nGridColumns - 1));
                }
            }
        }

        //dumpFrameToTxt();

        // check if the availability doesn't match our expectations
        if( (getAvailableGridColours() > 256*4) || (totalColours[CrystalGlobals.COLOUR_GREY] > 0) )
            System.out.println("resetGridFrameEdges() - expecting 0 grey of 64 available, after init " + totalColours[CrystalGlobals.COLOUR_GREY] + " total available:" + getAvailableGridColours() + " of Max " + (256*4));

        return status;

    } // resetGridFrameEdges

    public boolean isGridCellLocked(int rowI, int colI) {
        boolean isLocked = false;

        // if tile outside range it can not be locked
        if (rowI < 1 ||
                rowI > VerticalRowCells ||
                colI < 1 ||
                colI > HorizontalColumnCells) {
            return isLocked;
        }

        int isAttached = 0;

        // allow lock only if neighbouring tile is CrystalGlobals.LOCKED or CrystalGlobals.LOCKED_HINT
        if (lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)] < CrystalGlobals.UNLOCKED) // above?
        {
            ++isAttached;
        }
        if (lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT)] < CrystalGlobals.UNLOCKED) // right?
        {
            ++isAttached;
        }
        if (lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW)] < CrystalGlobals.UNLOCKED) // below
        {
            ++isAttached;
        }
        if (lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT)] < CrystalGlobals.UNLOCKED) // left?
        {
            ++isAttached;
        }

        if (isAttached == 4) {
            isLocked = true;
        }

        if (rowI == 8 && colI == 9) {
//            System.out.println("isGridCellLocked() - Known Hint Tile has isAttached count of " + isAttached);
        }

        return isLocked;
    }

    public void gridCellFullyPlacedUp() {
        gridCellFullyPlaced++;
    }

    public void gridCellFullyPlacedDown() {
        gridCellFullyPlaced--;
    }

    public int gridCellFullyPlacedGet() {
        return gridCellFullyPlaced;
    }


    /**
     * Locks are scored based on considered importance as follows.
     *
     * Any exposed CrystalGlobals.LOCKED_HINT take pressidance over all other locks unless additional 
     * locks are present.
     *
     * CrystalGlobals.LOCKED_HINT & 2 CrystalGlobals.LOCKED
     * CrystalGlobals.LOCKED_HINT & 1 CrystalGlobals.LOCKED
     * CrystalGlobals.LOCKED_HINT
     *
     * 2 CrystalGlobals.LOCKED & 2 CrystalGlobals.LOCKED_EDGE
     * 1 CrystalGlobals.LOCKED & 2 CrystalGlobals.LOCKED_EDGE
     * 3 CrystalGlobals.LOCKED
     * 2 CrystalGlobals.LOCKED & 1 CrystalGlobals.LOCKED_EDGE
     * 1 CrystalGlobals.LOCKED & 2 CrystalGlobals.LOCKED_EDGE
     * 1 CrystalGlobals.LOCKED & 1 CrystalGlobals.LOCKED_EDGE
     * 1 CrystalGlobals.LOCKED
     *
     * CrystalGlobals.LOCKED_EDGE Alone carries no influance.
     *
     * CrystalGlobals.LOCKED_HINT scores 10
     * CrystalGlobals.LOCKED      Scores 3
     * EDGE        scores 1
     *
     * @return serialisedFrameNumber
     */
    public int cellWithMostLockedNeighbours() {
        final int ALL_SEGMENTS_ATTACHED = 4;
        final int TARGET_SEGMENTS_ATTACHED = 3;
        int serialisedFrameNumber = 0;
        int isAttached = 0;
        int rowTemp = 0;
        int colTemp = 0;

        // track highest scoring cell
        int currentScore = 0;
        int bestScore = 0;
        int bestRow = 0;
        int bestCol = 0;
        int numberOfBestFits = 0; // reset
        int bestList[][] = new int[256][2];

//        for (int numberOfLockedNeighbours = TARGET_SEGMENTS_ATTACHED; numberOfLockedNeighbours > 0; numberOfLockedNeighbours--)
//        {

        bestScore = 0;
        int numberOfNonMovibleHintTiles = 1; // must be one to exclude hint tile as start of historical path 
        int searchRadius = 1;
        int rowLowerLimit = 1;
        int colLowerLimit = 1;
        int rowUpperLimit = VerticalRowCells;
        int colUpperLimit = HorizontalColumnCells;
        /**
         * @TODO - If a cell has already been placed (gridCellFullyPlacedGet()>0) limit the next cell selection to neighbour of the last cell placed.
         * This will ensure multiple branches aren't formed as the back track feature will potentially undo a good path.
         * tilePlacementOrder2[gridCellFullyPlacedGet()-1].tileHistory[tileIdFound];
         * if( tilePlacementOrder2[gridCellFullyPlacedGet()-1].ROW_INDEX == row &&
         *     tilePlacementOrder2[gridCellFullyPlacedGet()-1].COL_INDEX == col)
         */
        // expand search radius if a dead end is reached
        for (searchRadius = 1; searchRadius < Math.max(VerticalRowCells, HorizontalColumnCells); searchRadius++) {
            if (singlePathMode && (gridCellFullyPlacedGet() > numberOfNonMovibleHintTiles)) { // must be one to exclude hint tile as start of historical path
                rowLowerLimit = Math.max(1, tilePlacementOrder2[gridCellFullyPlacedGet() - 1].ROW_INDEX - searchRadius);
                colLowerLimit = Math.max(1, tilePlacementOrder2[gridCellFullyPlacedGet() - 1].COL_INDEX - searchRadius);
                rowUpperLimit = Math.min(VerticalRowCells, tilePlacementOrder2[gridCellFullyPlacedGet() - 1].ROW_INDEX + searchRadius);
                colUpperLimit = Math.min(HorizontalColumnCells, tilePlacementOrder2[gridCellFullyPlacedGet() - 1].COL_INDEX + searchRadius);
                if(debugSinglePath)
                    System.out.println("cellWithMostLockedNeighbours() - single path route, row " + tilePlacementOrder2[gridCellFullyPlacedGet() - 1].ROW_INDEX + " col " + tilePlacementOrder2[gridCellFullyPlacedGet() - 1].COL_INDEX + " rowLower " + rowLowerLimit + " rowUpper " + rowUpperLimit + " colLower " + colLowerLimit + " colUpper " + colUpperLimit);
            }

            int lockedHintCount = 0;
            int lockedLockCount = 0;
            int lockedEdgeCount = 0;

            for (int rowI = rowLowerLimit; rowI <= rowUpperLimit; rowI++) {
                for (int colI = colLowerLimit; colI <= colUpperLimit; colI++) {
                    isAttached = 0;
                    currentScore = 0;

                    lockedHintCount = 0;
                    lockedLockCount = 0;
                    lockedEdgeCount = 0;

                    // exclude fully locked cells
                    if (lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)] < CrystalGlobals.UNLOCKED && // above?
                            lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT)] < CrystalGlobals.UNLOCKED && // right?
                            lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW)] < CrystalGlobals.UNLOCKED && // below
                            lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT)] < CrystalGlobals.UNLOCKED) // left?
                    {
                        // ((rowI - 1) * 2) + 2 below
                        } else {
                        // proceed as cell not fully locked

                        // attached above?
                        rowTemp = getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE);
                        colTemp = getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE);
//                        rowTemp = ((rowI - 1) * 2); // getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)
//                        colTemp = colI - 1;         // getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)
                        if (rowTemp >= 0 &&
                                rowTemp <= VerticalRowCells * 2 &&
                                colTemp >= 0 &&
                                colTemp <= HorizontalColumnCells) {
                            if (lockArray[rowTemp][colTemp] == CrystalGlobals.LOCKED_HINT || lockArray[rowTemp][colTemp] == CrystalGlobals.LOCKED || lockArray[rowTemp][colTemp] == CrystalGlobals.LOCKED_EDGE) {
                                ++isAttached;

                                if (lockArray[rowTemp][colTemp] == CrystalGlobals.LOCKED_HINT)
                                    ++lockedHintCount;
                                if (lockArray[rowTemp][colTemp] == CrystalGlobals.LOCKED)
                                    ++lockedLockCount;
                                if (lockArray[rowTemp][colTemp] == CrystalGlobals.LOCKED_EDGE)
                                    ++lockedEdgeCount;

                            }

                            currentScore += scoreLock(rowTemp, colTemp);
                        }

                        // attached right?
                        rowTemp = getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT);
                        colTemp = getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT);
//                        rowTemp = ((rowI - 1) * 2) + 1;  // getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT)
//                        colTemp = colI;                  // getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT)
                        if (rowTemp >= 0 &&
                                rowTemp <= VerticalRowCells * 2 &&
                                colTemp >= 0 &&
                                colTemp <= HorizontalColumnCells) {
                            if (lockArray[rowTemp][colTemp] == CrystalGlobals.LOCKED_HINT || lockArray[rowTemp][colTemp] == CrystalGlobals.LOCKED || lockArray[rowTemp][colTemp] == CrystalGlobals.LOCKED_EDGE) {
                                ++isAttached;
                            }

                                if (lockArray[rowTemp][colTemp] == CrystalGlobals.LOCKED_HINT)
                                    ++lockedHintCount;
                                if (lockArray[rowTemp][colTemp] == CrystalGlobals.LOCKED)
                                    ++lockedLockCount;
                                if (lockArray[rowTemp][colTemp] == CrystalGlobals.LOCKED_EDGE)
                                    ++lockedEdgeCount;

                            currentScore += scoreLock(rowTemp, colTemp);
                        }

                        // attached below?
                        rowTemp = getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW);
                        colTemp = getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW);
//                        rowTemp = ((rowI - 1) * 2) + 2;  // getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW)
//                        colTemp = colI - 1;              // getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW)
                        if (rowTemp >= 0 &&
                                rowTemp <= VerticalRowCells * 2 &&
                                colTemp >= 0 &&
                                colTemp <= HorizontalColumnCells) {
                            if (lockArray[rowTemp][colTemp] == CrystalGlobals.LOCKED_HINT || lockArray[rowTemp][colTemp] == CrystalGlobals.LOCKED || lockArray[rowTemp][colTemp] == CrystalGlobals.LOCKED_EDGE) {
                                ++isAttached;
                            }

                                if (lockArray[rowTemp][colTemp] == CrystalGlobals.LOCKED_HINT)
                                    ++lockedHintCount;
                                if (lockArray[rowTemp][colTemp] == CrystalGlobals.LOCKED)
                                    ++lockedLockCount;
                                if (lockArray[rowTemp][colTemp] == CrystalGlobals.LOCKED_EDGE)
                                    ++lockedEdgeCount;

                            currentScore += scoreLock(rowTemp, colTemp);
                        }

                        // attached left?
                        rowTemp = getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT);
                        colTemp = getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT);
//                        rowTemp = ((rowI - 1) * 2) + 1;   // getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT)
//                        colTemp = colI - 1;               // getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT)
                        if (rowTemp >= 0 &&
                                rowTemp <= VerticalRowCells * 2 &&
                                colTemp >= 0 &&
                                colTemp <= HorizontalColumnCells) {

                            if (lockArray[rowTemp][colTemp] == CrystalGlobals.LOCKED_HINT || lockArray[rowTemp][colTemp] == CrystalGlobals.LOCKED || lockArray[rowTemp][colTemp] == CrystalGlobals.LOCKED_EDGE) {
                                ++isAttached;
                            }

                                if (lockArray[rowTemp][colTemp] == CrystalGlobals.LOCKED_HINT)
                                    ++lockedHintCount;
                                if (lockArray[rowTemp][colTemp] == CrystalGlobals.LOCKED)
                                    ++lockedLockCount;
                                if (lockArray[rowTemp][colTemp] == CrystalGlobals.LOCKED_EDGE)
                                    ++lockedEdgeCount;

                            currentScore += scoreLock(rowTemp, colTemp);
                        }

                        // determine score using 3bits from each locak counter
                        int neighbourWeighting = CrystalGlobals.LOCKED_EDGE;
                        //int neighbourWeighting = CrystalGlobals.LOCKED_HINT;
                        switch(neighbourWeighting)
                        {
                            case CrystalGlobals.LOCKED_EDGE:
                                currentScore  = lockedEdgeCount;
                                currentScore  = currentScore << 4;
                                currentScore += lockedLockCount;
                                currentScore  = currentScore << 4;
                                currentScore += lockedHintCount;
                                break;
                            case CrystalGlobals.LOCKED_HINT:
                                currentScore  = lockedHintCount;
                                currentScore  = currentScore << 4;
                                currentScore += lockedLockCount;
                                currentScore  = currentScore << 4;
                                currentScore += lockedEdgeCount;
                                break;
                            default: // same as CrystalGlobals.LOCKED_EDGE
                                currentScore  = lockedEdgeCount;
                                currentScore  = currentScore << 4;
                                currentScore += lockedLockCount;
                                currentScore  = currentScore << 4;
                                currentScore += lockedHintCount;
                                break;

                        }

                        //System.out.println("cellWithMostLockedNeighbours() - currentScore " + currentScore + " row:" + rowI + " col:" + colI + " lockedEdgeCount:" + lockedEdgeCount + " lockedLockCount:" + lockedLockCount + " lockedHintCount:" + lockedHintCount);
                        //                        if(currentScore > 6)
                        //                            currentScore = 4;

                        boolean debug_cellWithMostLockedNeighbours = false;
                        if (debug_cellWithMostLockedNeighbours) {
                            if (currentScore > 512) {
                                System.out.println("cellWithMostLockedNeighbours() - currentScore " + currentScore + " row " + rowI + " col " + colI);
                            }
                        }

                        // Find cells with the most attached neighbours
                        if (currentScore > bestScore && (isAttached < ALL_SEGMENTS_ATTACHED)) {
                            // @todo - clear list of best as we have found an improved best
                            numberOfBestFits = 0; // reset

                            bestScore = currentScore;
                            bestRow = rowI;
                            bestCol = colI;

                            if ((Trace.debugLevel & Trace.DEBUG_OUT_OF_RANGE) == Trace.DEBUG_OUT_OF_RANGE ||
                                    (Trace.activeDebugTrace & Trace.DEBUG_OUT_OF_RANGE) == Trace.DEBUG_OUT_OF_RANGE) {
                                System.out.println("cellWithMostLockedNeighbours() - row: " + bestRow + " col: " + bestCol + " BestScore " + bestScore);
                            }

                            // add to bestList
                            bestList[numberOfBestFits][ROW_INDEX] = bestRow;
                            bestList[numberOfBestFits][COL_INDEX] = bestCol;

                            ++numberOfBestFits; // increment index used to store next best fit

                        } else if (currentScore == bestScore && isAttached < ALL_SEGMENTS_ATTACHED) {
                            // @todo - add current to list of best
                            bestRow = rowI;
                            bestCol = colI;

                            if ((Trace.debugLevel & Trace.DEBUG_OUT_OF_RANGE) == Trace.DEBUG_OUT_OF_RANGE ||
                                    (Trace.activeDebugTrace & Trace.DEBUG_OUT_OF_RANGE) == Trace.DEBUG_OUT_OF_RANGE) {
                                System.out.println("cellWithMostLockedNeighbours() - row: " + bestRow + " col: " + bestCol + " BestScore " + bestScore);
                            }

                            // add to bestList
                            bestList[numberOfBestFits][ROW_INDEX] = bestRow;
                            bestList[numberOfBestFits][COL_INDEX] = bestCol;

                            ++numberOfBestFits; // increment index used to store next best fit
                            }

                        /*
                        if(selectFromAllQualifyingCells)
                        {
                        }
                        else
                        {
                        // stop when the first good match is found
                        if (isAttached == numberOfLockedNeighbours) {
                        serialisedFrameNumber = ((rowI-1) * HorizontalColumnCells) + colI;

                        if((Trace.debugLevel & Trace.DEBUG_OUT_OF_RANGE) == Trace.DEBUG_OUT_OF_RANGE ||
                        (Trace.activeDebugTrace & Trace.DEBUG_OUT_OF_RANGE) == Trace.DEBUG_OUT_OF_RANGE)
                        System.out.println("cellWithMostLockedNeighbours() - serialisedFrameNumber: " + serialisedFrameNumber + " row:" + rowI + " col:" + colI);

                        return serialisedFrameNumber;
                        }
                        }
                         */
                    }
                }
            }

            if (numberOfBestFits > 0) {
                break;
            } else {
                System.out.println("cellWithMostLockedNeighbours() - Extending search radius " + (searchRadius + 1));
            }

        } // searchRadius
        int indexToUse = 0;
        if (selectFromAllQualifyingCells) {
            /**
             * Random selection from a list of all qualifying cells.
             */
            indexToUse = (int) Math.round((numberOfBestFits) * Math.random());
        }
        // add to bestList
        bestRow = bestList[indexToUse][ROW_INDEX];
        bestCol = bestList[indexToUse][COL_INDEX];

//            serialisedFrameNumber = ((bestRow-1) * HorizontalColumnCells) + bestCol;
        serialisedFrameNumber = rowColToSerialisedFrameNumber(HorizontalColumnCells, VerticalRowCells, bestRow, bestCol);

        if ((Trace.debugLevel & Trace.DEBUG_OUT_OF_RANGE) == Trace.DEBUG_OUT_OF_RANGE ||
                (Trace.activeDebugTrace & Trace.DEBUG_OUT_OF_RANGE) == Trace.DEBUG_OUT_OF_RANGE) {
            System.out.println("cellWithMostLockedNeighbours() - row: " + bestRow + " col: " + bestCol + " BestScore " + bestScore);
        }

        //return serialisedFrameNumber;
//        }

        return serialisedFrameNumber;
    }


    /**
     * Each placed tile can have a lock associated with it.  This enables permanent locking of hint tiles,
     * edge, corner tiles, and a seed list of tiles imported from a starting configuration file.
     * 
     * Depending on the scoreLockMode a tile can have a score associated with it, which is used to select 
     * ordering of tile placement
     *    
     * Default Hint     First = H=10, E=1,  L=3
     *         Edge     First = H=1,  E=3,  L=10
     *         Corners  First = H=1,  E=10, L=3
     *         HintSeed First = H=3,  E=1,  L=10
     * @param rowTemp
     * @param colTemp
     * @return lockScore
     */
    public int scoreLock(int rowTemp, int colTemp) {

        int lockScore = 0;

        switch (lockArray[rowTemp][colTemp]) {
            case CrystalGlobals.LOCKED_HINT:
                switch (scoreLockMode) {
                    case ORIGIN_FROM_HINT:
                        lockScore = 10;
                        break;
                    case ORIGIN_FROM_EDGE:
                        lockScore = 1; //10;
                        break;
                    case ORIGIN_FROM_CORNER:
                        lockScore = 1; //10;
                        break;
                    case ORIGIN_FROM_CORNER_EDGE_HINT_EQUAL:
                        lockScore = 1;
                        break;
                    case ORIGIN_FROM_HINT_SEED:
                        lockScore = 3;
                        break;
                }
                break;
            case CrystalGlobals.LOCKED_EDGE:
                switch (scoreLockMode) {
                    case ORIGIN_FROM_HINT:
                        lockScore = 1;
                        break;
                    case ORIGIN_FROM_EDGE:
                        lockScore = 3;
                        break;
                    case ORIGIN_FROM_CORNER:
                        lockScore = 10;
                        break;
                    case ORIGIN_FROM_CORNER_EDGE_HINT_EQUAL:
                        lockScore = 10;
                        break;
                    case ORIGIN_FROM_HINT_SEED:
                        lockScore = 1;
                        break;
                }
                break;
            case CrystalGlobals.LOCKED:
                switch (scoreLockMode) {
                    case ORIGIN_FROM_HINT:
                        lockScore = 3;
                        break;
                    case ORIGIN_FROM_EDGE:
                        lockScore = 10;
                        break;
                    case ORIGIN_FROM_CORNER:
                        lockScore = 3;
                        break;
                    case ORIGIN_FROM_CORNER_EDGE_HINT_EQUAL:
                        lockScore = 1;
                        break;
                    case ORIGIN_FROM_HINT_SEED:
                        lockScore = 4;
                        break;
                }
                break;
            default:
                lockScore = 0;
        }

        return lockScore;
    }

    public int rowColToSerialisedFrameNumber(int HorizontalCellsLimit, int VerticalCellsLimit, int rowNumber, int colNumber) {
        return ((rowNumber - 1) * HorizontalCellsLimit) + colNumber;
    }

    public int rowFromSerialisedFrameNumber(int HorizontalCellsLimit, int VerticalCellsLimit, int serialisedFrameNumber) {
        int preferedRowI;

        // last col within a row
        if (serialisedFrameNumber % HorizontalCellsLimit == 0) {
            return serialisedFrameNumber / HorizontalCellsLimit;
        }

        preferedRowI = (int) Math.round((serialisedFrameNumber / HorizontalCellsLimit) + 0.5);  // mod 16

        if (preferedRowI > HorizontalColumnCells) {
            preferedRowI = (int) Math.round((serialisedFrameNumber / HorizontalCellsLimit));  // mod 16
        }
        if (preferedRowI > HorizontalColumnCells) {
            System.out.println("rowFromSerialisedFrameNumber() - ERROR decoding serialisedFrameNumber row " + preferedRowI + " exceeds rowLimit " + HorizontalColumnCells);
        }

        return preferedRowI;
    }

    public int colFromSerialisedFrameNumber(int HorizontalCellsLimit, int VerticalCellsLimit, int serialisedFrameNumber, int preferedRowI) {
        int preferedColI;

        // last col within a row
        //if(serialisedFrameNumber % HorizontalCellsLimit == 0)


        if (serialisedFrameNumber == HorizontalCellsLimit) {
            return serialisedFrameNumber;
        }

        preferedColI = serialisedFrameNumber - ((preferedRowI - 1) * HorizontalCellsLimit);

        if (preferedColI > VerticalRowCells) {
            System.out.println("colFromSerialisedFrameNumber() - ERROR decoding serialisedFrameNumber col " + preferedColI + " exceeds colLimit " + VerticalCellsLimit);
        }

        return preferedColI;
    }

    /**
     * Static 4x4 tiles for test harness
     */
    public void loadTilesStatic4x4() {
        System.out.println("loadTilesStatic4x4()");

        // prevent the colours from being initialised more than once
        if(totalColours[0] <= 0)
        {
            totalColours[0] = 4*4;      // 16 grey edges
            totalColours[1] = 12 / 2;   //  6
            totalColours[2] = 12 / 2;   //  6
            totalColours[3] = 12 / 2;   //  6
            totalColours[4] = 12 / 2;   //  6
            //  0 0 2 1 0 , 1 0 0 1 2 , 2 2 0 0 2 , 3 1 0 0 1 ,
            //  4 4 3 4 4 , 5 3 3 3 4 , 6 3 3 4 4 , 7 3 3 4 4 ,
            //  8 1 3 1 0 , 9 2 3 2 0 , 10 2 3 1 0 , 11 1 3 2 0 ,
            // 12 1 4 1 0 , 13 2 4 2 0 , 14 1 4 2 0 , 15 2 4 1 0
    
            //  @note - Not sure about the reliability of the following tile definitions
            //  0 0 1 2 1 ,   1 3 3 2 2 ,   2 0 4 1 0 ,  3 0 1 1 0
            //  4 4 0 0 4 ,   5 2 3 2 2 ,   6 2 1 1 1 ,  7 1 0 1 3
            //  8 3 4 0 4 ,   9 1 2 4 0 ,  10 1 3 4 0 , 11 2 4 0 4
            // 12 3 3 2 2 ,  13 0 4 2 1 ,  14 4 3 1 0 , 15 0 0 1 4
        }
    }

    /**
     * Static 16x16 tiles for test harness
     */
    public void loadTilesStatic16x16() {
        System.out.println("loadTilesStatic16x16()");
        /**
         * 0 1 17 0 0 ,  1 1 5 0 0 ,   2 9 17 0 0 ,  3 17 9 0 0 ,
         * 4 2 1 0 1 ,   5 10 9 0 1 ,  6 6 1 0 1 ,   7 6 13 0 1 ,
         * 8 11 17 0 1 , 9 7 5 0 1 ,  10 15 9 0 1 , 11 8 5 0 1 ,
         * 12 8 13 0 1 , 13 21 5 0 1 , 14 10 1 0 9 , 15 18 17 0 9 ,
         * 16 14 13 0 9 ,17 19 13 0 9 ,18 7 9 0 9 ,  19 15 9 0 9 ,
         * 20 4 5 0 9 ,  21 12 1 0 9 , 22 12 13 0 9 ,23 20 1 0 9 ,
         * 24 21 1 0 9 ,25 2 9 0 17 ,26 2 17 0 17 ,27 10 17 0 17 ,
         * 28 18 17 0 17 ,29 7 13 0 17 ,30 15 9 0 17 ,31 20 17 0 17 ,
         * 32 8 9 0 17 ,33 8 5 0 17 ,34 16 13 0 17 ,35 22 5 0 17 ,
         * 36 18 1 0 5 ,37 3 13 0 5 ,38 11 13 0 5 ,39 19 9 0 5 ,
         * 40 19 17 0 5 ,41 15 1 0 5 ,42 15 9 0 5 ,43 15 17 0 5 ,
         * 44 4 1 0 5 ,45 20 5 0 5 ,46 8 5 0 5 ,47 16 5 0 5 ,
         * 48 2 13 0 13 ,49 10 1 0 13 ,50 10 9 0 13 ,51 6 1 0 13 ,
         * 52 7 5 0 13 ,53 4 5 0 13 ,54 4 13 0 13 ,55 8 17 0 13 ,
         * 56 16 1 0 13 ,57 16 13 0 13 ,58 21 9 0 13 ,59 22 17 0 13 ,
         * 60 6 18 2 2 ,61 14 7 2 2 ,62 10 3 2 10 ,63 2 8 2 18 ,
         * 64 18 22 2 18 ,65 14 14 2 18 ,66 11 10 2 18 ,67 20 6 2 18 ,
         * 68 22 8 2 18 ,69 3 7 2 3 ,70 7 12 2 3 ,71 14 18 2 11 ,
         * 72 15 4 2 11 ,73 20 15 2 11 ,74 8 3 2 11 ,75 14 15 2 19 ,
         * 76 19 15 2 19 ,77 3 16 2 7 ,78 20 3 2 7 ,79 16 21 2 7 ,
         * 80 19 18 2 15 ,81 18 18 2 4 ,82 11 4 2 4 ,83 18 19 2 12 ,
         * 84 6 14 2 12 ,85 8 12 2 12 ,86 16 20 2 12 ,87 2 21 2 20 ,
         * 88 6 22 2 20 ,89 4 16 2 20 ,90 11 12 2 8 ,91 19 15 2 8 ,
         * 92 19 4 2 8 ,93 4 21 2 8 ,94 12 14 2 8 ,95 21 3 2 21 ,
         * 96 4 19 2 22 ,97 20 8 2 22 ,98 21 6 2 22 ,99 22 21 2 22 ,
         * 100 12 15 10 10 ,101 12 16 10 10 ,102 16 19 10 10 ,103 22 6 10 10 ,
         * 104 4 15 10 18 ,105 3 8 10 6 ,106 19 8 10 6 ,107 4 15 10 6 ,
         * 108 16 11 10 6 ,109 15 12 10 14 ,110 12 15 10 14 ,111 20 19 10 3 ,
         * 112 20 16 10 3 ,113 14 4 10 11 ,114 7 12 10 11 ,115 12 11 10 11 ,
         * 116 22 16 10 11 ,117 3 21 10 19 ,118 16 12 10 7 ,119 8 22 10 15 ,
         * 120 14 22 10 4 ,121 6 16 10 20 ,122 14 19 10 20 ,123 20 15 10 20 ,
         * 124 12 22 10 8 ,125 21 15 10 8 ,126 14 6 10 16 ,127 19 21 10 16 ,
         * ,128 4 3 10 16 ,129 20 8 10 16 ,130 6 20 10 21 ,131 12 14 10 21 ,
         * 132 14 16 10 22 ,133 11 4 10 22 ,134 4 3 10 22 ,135 16 20 10 22 ,
         * 136 20 7 18 18 ,137 6 3 18 6 ,138 6 11 18 6 ,139 6 12 18 6 ,
         * 140 19 21 18 6 ,141 15 6 18 6 ,142 16 12 18 6 ,143 21 21 18 6 ,
         * 144 3 4 18 14 ,145 18 12 18 3 ,146 18 22 18 3 ,147 3 14 18 3 ,
         * 148 15 12 18 3 ,149 6 11 18 19 ,150 4 22 18 19 ,151 11 11 18 7
         * ,152 11 19 18 7 ,153 22 16 18 7 ,154 7 7 18 4 ,155 7 12 18 4 ,4
         * 156 22 7 18 4 ,157 7 16 18 20 ,158 8 6 18 20 ,159 21 21 18 8 ,
         * ,160 6 20 18 16 ,161 14 20 18 16 ,162 15 11 18 22 ,163 4 16 18 22 ,
         * 164 3 4 6 14 ,165 4 8 6 14 ,166 3 3 6 11 ,167 11 15 6 19 ,
         * 168 19 21 6 19 ,169 4 8 6 7 ,170 20 16 6 7 ,171 21 11 6 7 ,
         * 172 15 15 6 15 ,173 12 20 6 15 ,174 7 21 6 4 ,175 7 19 6 12
         * ,176 14 4 6 20 ,177 12 16 6 8 ,178 8 15 6 8 ,179 7 16 6 16 ,
         * 180 11 16 6 21 ,181 7 11 6 21 ,182 19 8 14 14 ,183 22 7 14 3 ,
         * 184 19 12 14 11 ,185 8 8 14 11 ,186 21 7 14 19 ,187 14 21 14 7 ,
         * 188 3 19 14 7 ,189 16 19 14 7 ,190 3 3 14 15 ,191 15 20 14 15 ,
         * 192 11 7 14 4 ,193 21 11 14 12 ,194 21 22 14 12 ,195 22 15 14 12 ,
         * 196 11 22 14 20 ,197 19 8 14 20 ,198 20 20 14 20 ,199 19 3 14 8 ,
         * 200 21 8 14 16 ,201 22 7 14 16 ,202 12 19 14 21 ,203 12 8 14 21 ,
         * 204 16 3 14 21 ,205 22 21 14 21 ,206 22 7 3 3 ,207 19 22 3 11 ,
         * 208 8 15 3 11 ,209 11 19 3 7 ,210 16 15 3 7 ,211 3 16 3 15 ,
         * 212 8 8 3 4 ,213 3 20 3 12 ,214 4 22 3 12 ,215 22 21 3 12 ,
         * 216 19 15 3 20 ,217 4 12 3 16 ,218 11 4 3 21 ,219 11 16 3 22 ,
         * 220 21 21 3 22 ,221 21 22 3 22 ,222 12 22 11 11 ,223 20 7 11 11 ,
         * 224 16 15 11 11 ,225 19 15 11 7 ,226 12 12 11 7 ,227 19 8 11 4 ,
         * 228 7 22 11 20 ,229 16 8 11 20 ,230 12 20 11 8 ,231 12 21 11 8 ,
         * 232 19 20 19 19 ,233 16 4 19 7 ,234 7 4 19 4 ,235 7 20 19 4 ,
         * 236 12 15 19 4 ,237 4 16 19 12 ,238 15 22 19 20 ,239 21 15 19 20 ,
         * 240 7 21 19 8 ,241 4 21 19 8 ,242 15 12 7 15 ,243 20 8 7 15 ,
         * 244 22 20 7 4 ,245 16 22 7 21 ,246 21 22 15 15 ,247 12 4 15 4 ,
         * 248 4 21 15 12 ,249 16 21 15 20 ,250 22 8 4 4 ,251 8 12 4 12 ,
         * 252 16 20 12 8 ,253 21 16 20 16 ,254 16 22 20 22 ,255 21 22 8 22 ,
         */
        // prevent the colours from being initialised more than once
        if(totalColours[0] <= 0)
        {
            totalColours[0] = 16*4;   // 64 grey edges
            totalColours[1] = 24 / 2;
            totalColours[2] = 48 / 2;
            totalColours[3] = 50 / 2;
            totalColours[4] = 50 / 2;
            totalColours[5] = 24 / 2;
            totalColours[6] = 48 / 2;
            totalColours[7] = 50 / 2;
            totalColours[8] = 50 / 2;
            totalColours[9] = 24 / 2;
            totalColours[10] = 48 / 2;
            totalColours[11] = 50 / 2;
            totalColours[12] = 50 / 2;
            totalColours[13] = 24 / 2;
            totalColours[14] = 48 / 2;
            totalColours[15] = 50 / 2;
            totalColours[16] = 50 / 2;
            totalColours[17] = 24 / 2;
            totalColours[18] = 48 / 2;
            totalColours[19] = 50 / 2;
            totalColours[20] = 50 / 2;
            totalColours[21] = 50 / 2;
            totalColours[22] = 50 / 2;
        }

    }

    /**
     * A tile consists of 4 segments, represented as four elements within an array.
     * Due to the different number of cells used to represent rows and columns this method returns the
     * index for the give tile segment.
     * NOTE: This method must be called twice for each segment to obtain the row and column individually.
     * 
     * Provide with the segment index starting at the top (index 0) with clockwise rotation through
     * each of the 4 segments
     *  
     * From 16x16 get grid coordinates
     * 
     * ROW_COL_SELECTION (0=row, 1=Column)
     * row 0 through 15
     * column 0 through 15
     * segment 0 through 3
     * 
     */
    public int getGridIndex(int ROW_COL_SELECTION, int row, int col, int SEGMENT_INDEX) {

        int result = -1;

        switch (ROW_COL_SELECTION) {
            case CrystalGlobals.ROW_SELECTION:

                switch (SEGMENT_INDEX) {
                    case CrystalGlobals.SEGMENT_INDEX_ABOVE:
                        result = (row - 1) * 2;
                        break;
                    case CrystalGlobals.SEGMENT_INDEX_RIGHT:
                        result = ((row - 1) * 2) + 1;
                        break;
                    case CrystalGlobals.SEGMENT_INDEX_BELOW:
                        result = ((row - 1) * 2) + 2;
                        break;
                    case CrystalGlobals.SEGMENT_INDEX_LEFT:
                        result = ((row - 1) * 2) + 1;
                        break;
                }
                break;

            case CrystalGlobals.COL_SELECTION:

                switch (SEGMENT_INDEX) {
                    case CrystalGlobals.SEGMENT_INDEX_ABOVE:
                        result = col - 1;
                        break;
                    case CrystalGlobals.SEGMENT_INDEX_RIGHT:
                        result = col;
                        break;
                    case CrystalGlobals.SEGMENT_INDEX_BELOW:
                        result = col - 1;
                        break;
                    case CrystalGlobals.SEGMENT_INDEX_LEFT:
                        result = col - 1;
                        break;
                }
                break;

        }

        if (result < 0)
            System.out.println("getGridIndex() - ERROR decoding grid coordinates from row:" + row + " col:" + col + " ROW_COL_SELECTION:" + ROW_COL_SELECTION + " SEGMENT_INDEX:" + SEGMENT_INDEX);

//        if (row == 15 && col == 15)
//            System.out.println("getGridIndex() - decoding grid coordinates from row:" + row + " col:" + col + " ROW_COL_SELECTION:" + ROW_COL_SELECTION + " SEGMENT_INDEX:" + SEGMENT_INDEX + " result=" + result);

        return result;
    }


}
