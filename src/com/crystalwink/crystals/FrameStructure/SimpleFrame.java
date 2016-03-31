package com.crystalwink.crystals.FrameStructure;

import com.crystalwink.crystalscommon.CrystalGlobals;

/**
 * Defines a simple 16x16 structure containing tileId and orientation
 *
 * @author Dean Clark deancl
 */
public class SimpleFrame {
    public SimpleTileInfo simpleFrameArray[][]; // = new SimpleTileInfo[CrystalGlobals.MAX_ROWS][CrystalGlobals.MAX_COLUMNS];

    public SimpleFrame() {
        
        simpleFrameArray = new SimpleTileInfo[CrystalGlobals.MAX_ROWS][CrystalGlobals.MAX_COLUMNS];
        
        for(int eachRow=0; eachRow < CrystalGlobals.MAX_ROWS; eachRow++)
            for(int eachColumn=0; eachColumn < CrystalGlobals.MAX_COLUMNS; eachColumn++)
        {
            simpleFrameArray[eachRow][eachColumn] = new SimpleTileInfo(); // [eachRow][CrystalGlobals.MAX_COLUMNS];
        }
        
    }
}
