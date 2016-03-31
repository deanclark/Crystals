/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.crystalwink.crystalscommon.Wire;

/**
 *
 * @author Dean Clark deancl
 */
public class Wire {

    public int fromTile;   // tileIndex
    public int fromSegment; // segmentIndex
    public int toTile;     // tileIndex
    public int toSegment; // segmentIndex
    public double wireLength;
    public boolean wireActive = false; // default wire to inactive

    /**
     * Return the tileId of the neighbouring tile
     * @param knownId
     * @return toTileOrFromTile
     */
    public int neighbourId(int knownId) {
        return (knownId == fromTile) ? toTile : fromTile;
    }
}
