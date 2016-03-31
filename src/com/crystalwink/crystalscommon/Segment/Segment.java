/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.crystalwink.crystalscommon.Segment;

import com.crystalwink.crystalscommon.Wire.Wire;

/**
 *
 * @author Dean Clark deancl
 */
public class Segment {

    public final int segmentPoints = 4; // 3 + 1 for the wire connect point
    public int xPointArray[] = new int[segmentPoints];
    public int yPointArray[] = new int[segmentPoints];
    public int points = segmentPoints;
    public int colour;
    public int xPointArrayPrint[] = new int[segmentPoints];
    public int yPointArrayPrint[] = new int[segmentPoints];
    public int zPointArrayPrint[] = new int[segmentPoints];

    // wireConnectPoint uses additional point stored in segmentPoints+1
    // int wireConnectPointX; //[] = new int[2];   // x, y Edge Link Point    TODO
    // int wireConnectPointY;   // x, y Edge Link Point    TODO

    // int neighbourList[] = new int[64];  // TileId/SegmentId         TODO
    public int nneighbours;
    final int MAX_NEIGHBOURS = 64;  // shound be 64 I guess as this is the maximum number of segments of any one colour (also defined in GraphPanel class)
    public Wire connectedWires[] = new Wire[MAX_NEIGHBOURS];
    public int nConnectedWires = 0;

    public void addWireConnect(Wire aWire) {
        if (nConnectedWires < MAX_NEIGHBOURS) {
            connectedWires[nConnectedWires] = aWire;
            nConnectedWires++;
        } else {
            System.out.printf("addWireConnect - Wire not connected as MAX_NEIGHBOURS reached %d\n", nConnectedWires);
        }

    }

    public void setActiveWire(Wire activeWire) {
        // clear all existing active wires before setting a wire active
        for (int aConnectedWire = 0; aConnectedWire < nConnectedWires; aConnectedWire++) {
            if (connectedWires[aConnectedWire].wireActive) {
                connectedWires[aConnectedWire].wireActive = false;
            }

            if (connectedWires[aConnectedWire].fromTile == activeWire.fromTile &&
                    connectedWires[aConnectedWire].fromSegment == activeWire.fromSegment &&
                    connectedWires[aConnectedWire].toTile == activeWire.toTile &&
                    connectedWires[aConnectedWire].fromSegment == activeWire.fromSegment) {
                connectedWires[aConnectedWire].wireActive = true;
            }
        }
    }

    public void selectActiveWire() {

        boolean hasActiveWire = false;

        if (nConnectedWires > 0) {
            // check for existing active wires before setting a wire active
            for (int aConnectedWire = 0; aConnectedWire < nConnectedWires; aConnectedWire++) {
                if (connectedWires[aConnectedWire].wireActive) {
                    hasActiveWire = true;
                }
            }

            if (hasActiveWire) // was !
            {
                for (int aConnectedWire = 0; aConnectedWire < nConnectedWires; aConnectedWire++) {
                    // select one   - @todo - change this to a random selection
                    if (aConnectedWire == 0) // @todo -  randomize the selection of an active wire.
                    {
                        connectedWires[aConnectedWire].wireActive = false; // true;
                    } else {
                        connectedWires[aConnectedWire].wireActive = false;      // DC-DEBUG set all other wire to inactive
                    }
                }
            }
            // check for existing active wires before setting a wire active
            // select a random active Wire from a random Segment
            int randomWire = (int) Math.round((nConnectedWires - 1) * Math.random());
            connectedWires[randomWire].wireActive = true;

        }
//        System.out.printf("nConnectedWires = %d\n", nConnectedWires);
    } // end selectActiveWire()

    public boolean hasActiveWire() {
        if (nConnectedWires > 0) {
            for (int aConnectedWire = 0; aConnectedWire < nConnectedWires; aConnectedWire++) {
                if (connectedWires[aConnectedWire].wireActive) {
                    return false;
                }
            }
        }
        return false;
    } // end hasActiveWire()

    public double getActiveWireLength() {
        if (nConnectedWires > 0) {
            for (int aConnectedWire = 0; aConnectedWire < nConnectedWires; aConnectedWire++) {
                if (connectedWires[aConnectedWire].wireActive) {
                    return connectedWires[aConnectedWire].wireLength;
                }
            }
        }
        return 200.0;
    }

    public Wire getActiveWire() {
        if (nConnectedWires > 0) {
            for (int aConnectedWire = 0; aConnectedWire < nConnectedWires; aConnectedWire++) {
                if (connectedWires[aConnectedWire].wireActive) {
                    return connectedWires[aConnectedWire];
                }
            }
        }

//        setActiveWire(getRandomWire());
        return getRandomWire();

    // if all else fails set the first Wire as active
//        connectedWires[0].wireActive = true;
//        return connectedWires[0];
//        return null;
    }

    public Wire getRandomWire() {
        if (nConnectedWires > 0) {
            int randomWire = (int) Math.round(nConnectedWires * Math.random());

            return connectedWires[randomWire];
        }

        // if all else fails set the first Wire as active
        return null;
    }
} // end Segment class
