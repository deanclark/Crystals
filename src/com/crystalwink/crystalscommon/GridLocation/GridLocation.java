/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.crystalwink.crystalscommon.GridLocation;

/**
 *
 * @author Dean Clark deancl
 */
public class GridLocation {
    public int row = -1;
    public int col = -1;

    public GridLocation segmentAbove(int rowInput, int colInput)
    {
        // above
        GridLocation aGridLocation = new GridLocation();
        aGridLocation.row = (rowInput - 1) * 2;
        aGridLocation.col = colInput - 1;
        return aGridLocation;
    }
    public GridLocation segmentRight(int rowInput, int colInput)
    {
        // right
        GridLocation aGridLocation = new GridLocation();
        aGridLocation.row = ((rowInput - 1) * 2) + 1;
        aGridLocation.col = colInput;
        return aGridLocation;
    }
    public GridLocation segmentBelow(int rowInput, int colInput)
    {
        // below
        GridLocation aGridLocation = new GridLocation();
        aGridLocation.row = ((rowInput - 1) * 2) + 2;
        aGridLocation.col = colInput - 1;
        return aGridLocation;
    }
    public GridLocation segmentLeft(int rowInput, int colInput)
    {
        // left
        GridLocation aGridLocation = new GridLocation();
        aGridLocation.row = ((rowInput - 1) * 2) + 1;
        aGridLocation.col = colInput - 1;
        return aGridLocation;
    }


}

