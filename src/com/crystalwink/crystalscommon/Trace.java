/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.crystalwink.crystalscommon;

/**
 *
 * @author Dean Clark deancl
 */
public class Trace {
    public final static int DEBUG_GENERAL      = 0x00000001;
    public final static int DEBUG_SETUP        = 0x00000002;
    public final static int DEBUG_RELAX1       = 0x00000004;
    public final static int DEBUG_RELAX2       = 0x00000008;
    public final static int DEBUG_RELAX3       = 0x00000010;
    public final static int DEBUG_TILE         = 0x00000020;
    public final static int DEBUG_SEGMENT      = 0x00000040;
    public final static int DEBUG_WIRES        = 0x00000080;
    public final static int DEBUG_MOUSE        = 0x00000100;
    public final static int DEBUG_REBALANCE    = 0x00000200;
    public final static int DEBUG_OUT_OF_RANGE = 0x00000400;
    public final static int debugLevel = DEBUG_MOUSE; // | DEBUG_REBALANCE; // | DEBUG_RELAX2;
    public static int activeDebugTrace = 0;  // Configured at runtime to dump trace following a failure.  cleared each itteration
//    int debugLevel = DEBUG_RELAX1 | DEBUG_RELAX2;
//    int debugLevel = DEBUG_GENERAL | DEBUG_SETUP | DEBUG_RELAX2;

}
