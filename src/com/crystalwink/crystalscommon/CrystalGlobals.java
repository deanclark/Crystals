/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.crystalwink.crystalscommon;

/**
 *
 * @author Dean Clark deancl
 */
public class CrystalGlobals {

    public final static int MAX_ROWS = 16;
    public final static int MAX_COLUMNS = 16;
    public final static int MAX_TILES = 1024;
    public final static int MAX_WIRES = 17408; //32768; // 16384; //8192//4096; - Recorded 16552 Connected Wires
    public final static int MAX_NEIGHBOURS = 64;  // shound be 64 I guess as this is the maximum number of segments of any one colour (also defined in Segment class)
    public final static int WIRE_LENGTH_TARGET = 8; // Tile.segmentScale; // 4;  // 2
    public final static int WIRE_LENGTH_TARGET_PROXIMITY = 100;
    public final static int WIRE_LENGTH_INITIAL = 13;  // 8

    public final static int MODE_ELASTIC           = 0;
    public final static int MODE_FRAME_SCATTER     = 1;
    public final static int MODE_FRAME_SINGLE_PATH = 2;
    //public       static int ModeOfOperation = MODE_FRAME_SINGLE_PATH;
    public       static int ModeOfOperation = MODE_ELASTIC;  // odd behaviour when "Active" button selected
    

    public final static int MAX_COLORS = 23;
    public final static int maxCornerColoursInUse = 4;


    public final static int STATUS_OK = 0;
    public final static int STATUS_FAIL = -1;
    public final static int STATUS_FAIL_UNAVAILABLE = -2;

    public final static int UNLOCKED = 0;
    public final static int LOCKED = -1;
    public final static int LOCKED_EDGE = -2;
    public final static int LOCKED_HINT = -3;

    public final static int COLOUR_GREY = 0;

    public final static int ROW_SELECTION = 0; 
    public final static int COL_SELECTION = 1;

    public final static int SEGMENT_INDEX_ABOVE = 0;
    public final static int SEGMENT_INDEX_RIGHT = 1;
    public final static int SEGMENT_INDEX_BELOW = 2;
    public final static int SEGMENT_INDEX_LEFT  = 3;




}
