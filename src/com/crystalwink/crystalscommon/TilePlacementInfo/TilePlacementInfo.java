/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.crystalwink.crystalscommon.TilePlacementInfo;

import com.crystalwink.crystalscommon.CrystalGlobals;

/**
 *
 * @author Dean Clark deancl
 */
public class TilePlacementInfo {
    public int ROW_INDEX    = 0;
    public int COL_INDEX    = 0;
    public int TILEID_INDEX = 0;

    public int tileHistory[] = new int[CrystalGlobals.MAX_TILES];

    public TilePlacementInfo(int preferedRowI, int preferedColI, int tileIdFound)
    {
        ROW_INDEX    = preferedRowI;
        COL_INDEX    = preferedColI;
        TILEID_INDEX = tileIdFound;
    }

}
