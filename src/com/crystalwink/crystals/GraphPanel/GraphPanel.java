package com.crystalwink.crystals.GraphPanel;

import com.crystalwink.crystalscommon.Trace;
import com.crystalwink.crystalscommon.CrystalGlobals;
import com.crystalwink.crystalscommon.Tile.Tile;
import com.crystalwink.crystalscommon.Wire.Wire;
import com.crystalwink.crystalscommon.LockManager.LockManager;
//import com.crystalwink.crystalscommon.Wire.Wire;
import com.crystalwink.crystals.TilePrintableAwt.TilePrintableAwt;
import com.crystalwink.crystals.FrameStructure.FrameStructure;
import com.crystalwink.crystals.Crystal;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.event.ContainerEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author Dean Clark deancl
 */
@SuppressWarnings("serial")
public class GraphPanel extends Panel
        implements Runnable, MouseListener, MouseMotionListener {

    Tile pick;
    int picklocked;
    Image offScreenImage;
    Dimension offScreenDimension;
    Graphics offScreenGraphics;
    final Color lockedColor = Color.red;
    final Color selectColor = Color.pink;
    final Color wireColor = Color.black;
    final Color nodeColor = new Color(250, 220, 100);
    final Color stressColor = Color.darkGray;
    final Color arcColor1 = Color.black;
    final Color arcColor2 = Color.pink;
    final Color arcColor3 = Color.red;


    Crystal graph;
    private int numberOfTiles;
    int iterations = 0;
    boolean pauseGame = false;
    boolean suppressGraphics = false;
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //******* THERE ARE TWO(2) IMPLEMENTATION ATTEMPTS controlled by the flag below - try both *******// 
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // public static final boolean showFloatingTiles = true; // prevent display of floating tiles
    public static boolean showFloatingTiles = false; // prevent display of floating tiles
    public static boolean runFast = !showFloatingTiles; // = true; // when fast the UI will appear to stutter as frames are only updated periodically 
    
//    public Tile tiles[] = new Tile[CrystalGlobals.MAX_TILES];
    public TilePrintableAwt tiles[] = new TilePrintableAwt[CrystalGlobals.MAX_TILES];
    
    int nwires;
    Wire wires[] = new Wire[CrystalGlobals.MAX_WIRES];
    Color palate[] = new Color[CrystalGlobals.MAX_COLORS];
    public LockManager tileLockManager = new LockManager(256);
    // 16x16 frame/board
    public FrameStructure aFrame = null;
    //FrameStructure aFrame = new FrameStructure(CrystalGlobals.MAX_ROWS, CrystalGlobals.MAX_COLUMNS, tiles, 256); // ntiles
    //FrameStructure aFrame = new FrameStructure(8, 16);
    //FrameStructure aFrame = new FrameStructure(4, 4);
    Thread relaxer;
    public boolean showFloatingTilesCheckbox;
    public boolean stressModeCheckbox;
    public boolean randomModeCheckbox;
    public boolean activeWiresCheckbox;
    public boolean showTileIdCheckbox;
    public boolean drawRotationModeCheckbox;
    public boolean wireframeTileModeCheckbox = true;
    public boolean showWiresModeCheckbox;
    int numMouseButtonsDown = 0;

    /**
     *
     * @return numberOfTiles
     */
    public int getNumberOfTiles() {
        return numberOfTiles;
    }

    /**
     *
     * @param graph
     */
    public GraphPanel(Crystal graph) {
        this.graph = graph;
        addMouseListener(this);
    }


    /**
     *
     * @param TileId
     * @return tileIndex
     */
    public int getTileIndexFromId(int TileId) {
        int tileIndex = 0;
        for (tileIndex = 0; tileIndex < numberOfTiles; tileIndex++) {
            if (tiles[tileIndex].getId() == TileId) {
                return tileIndex;
            }
        }

        System.out.printf("getTileIndexFromId() - Unable to locate TileId %d\n", TileId);
        return -1;
    }

    int findNode(String lbl) {
        for (int i = 0; i < getNumberOfTiles(); i++) {
            if (tiles[i].getLbl().equals(lbl)) {
                return i;
            }
        }
        return addNode(lbl);
    }

    /**
     *
     */
    public void loadTileColors() {
        //Color tileColor;
        // TODO - read from palate file
        /*
        for(int colorIndex=0; colorIndex<MaxColors; colorIndex++)
        {
            Color tempColor = new Color((int)10+(colorIndex*10), (int)30+(colorIndex*7), (int)30+(colorIndex*7));
            palate[colorIndex] = tempColor;
        }
         */
        palate[0] = new Color(196, 196, 196); // 196 196 196
        palate[1] = new Color(128, 255, 255); // 255 128 64
        palate[2] = new Color(255, 255, 0); // 255 0 128
        palate[3] = new Color(0, 128, 64); // 128 0 0
        palate[4] = new Color(255, 0, 128); // 128 128 255
        palate[5] = new Color(0, 0, 160); // 0 128 64
        palate[6] = new Color(255, 215, 0); // 128 0 255
        palate[7] = new Color(255, 0, 255); // 0 128 192
        palate[8] = new Color(255, 128, 0); // 0 0 128
        palate[9] = new Color(215, 255, 0); // 0 0 160
        palate[10] = new Color(128, 255, 255); // 128 0 128
        palate[11] = new Color(255, 128, 64); // 0 128 64
        palate[12] = new Color(0, 0, 160); //  255 255 0
        palate[13] = new Color(255, 128, 64); // 128 0 64
        palate[14] = new Color(255, 0, 128); // 0 128 0
        palate[15] = new Color(0, 128, 64); // 255 255 0
        palate[16] = new Color(128, 225, 225); // 0 0 128
        palate[17] = new Color(128, 255, 215); // 255 0 255
        palate[18] = new Color(128, 215, 225); // 255 255 0
        palate[19] = new Color(215, 215, 0); // 128 0 128
        palate[20] = new Color(128, 0, 128); // 255 128 64
        palate[21] = new Color(155, 255, 0); // 255 0 255
        palate[22] = new Color(155, 0, 128); // 0 0 128
    }

    /**
     * 
     * @param colorRef
     * @param palate
     * @return Colour
     */
    public Color lookupTileColor(int colorRef, Color palate[]) {
        //Color tileColor;
        // TODO
        return palate[colorRef];
    }

    /**
     *
     * @param wireInput
     * @param tileId
     * @return angleOfRotation
     */
    public double getActiveWireAngle(Wire wireInput, int tileId) {
        double angleOfRotation = 0;
        double dx = 0.0;
        double dy = 0.0;

        if (tileId == wireInput.fromTile) {
            dx = tiles[wireInput.toTile].xWorldSpace - tiles[wireInput.fromTile].xWorldSpace;
            dy = tiles[wireInput.toTile].yWorldSpace - tiles[wireInput.fromTile].yWorldSpace;
            angleOfRotation = Math.sin(-dy / (Math.sqrt((dx * dx) * (dy * dy))));      // "todo this is not correct
            //angleOfRotation = Math.sin( dx / (Math.sqrt((dx*dx) * (dy*dy))));      // "todo this is not correct
            //angleOfRotation = ( dx / (Math.sqrt((dx*dx) * (dy*dy))));      // "todo this is not correct
            //angleOfRotation = 0.7071;      // "todo this is not correct
        } else if (tileId == wireInput.toTile) {
            dx = tiles[wireInput.fromTile].xWorldSpace - tiles[wireInput.toTile].xWorldSpace;
            dy = tiles[wireInput.fromTile].yWorldSpace - tiles[wireInput.toTile].yWorldSpace;
            //angleOfRotation = Math.cos( dy / (Math.sqrt((dx*dx) * (dy*dy))));      // "todo this is not correct
            angleOfRotation = Math.cos(-dx / (Math.sqrt((dx * dx) * (dy * dy))));      // "todo this is not correct//            angleOfRotation = ( dy / (Math.sqrt((dx*dx) * (dy*dy))));      // "todo this is not correct
            //angleOfRotation = 0.7071;      // "todo this is not correct
        }
        return angleOfRotation;
    }

    /**
     *
     * @param id
     * @param colours
     * @return numberOfTiles
     */
    @SuppressWarnings("unused")
    public int addTile(int id, int colours[]) {
        if ((Trace.debugLevel & Trace.DEBUG_GENERAL) == Trace.DEBUG_GENERAL) {
            System.out.printf("start addTile id=%d\n", id);
        }

        int scale = 15;
        //Tile newTile = new Tile(id, colours);
        //Tile newTile = new Tile(id, colours,scale);
        TilePrintableAwt newTile = new TilePrintableAwt(id, colours, scale);

        if (newTile != null) {
            if ((Trace.debugLevel & Trace.DEBUG_GENERAL) == Trace.DEBUG_GENERAL) {
                System.out.printf("Tile created id=%d\n", id);
            }
        }

        newTile.xWorldSpace = 10 + 380 * Math.random();
        newTile.yWorldSpace = 10 + 380 * Math.random();

        // prevent duplication translation
        newTile.moved(true);

        if (getNumberOfTiles() != newTile.getId()) {
            System.out.printf("*** Warning tile.getId() does not match tileIndex within tiles[]s\n");
        }

        tiles[numberOfTiles] = newTile;
        numberOfTiles++;

        if ((Trace.debugLevel & Trace.DEBUG_GENERAL) == Trace.DEBUG_GENERAL) {
            System.out.printf("end addTile\n");
        }

        return numberOfTiles;
    }

    int addNode(String lbl) {
        int colours[] = new int[4];
        colours[0] = 12;
        colours[1] = 12;
        colours[2] = 12;
        colours[3] = 12;

        int scale = 15;
        //Tile newTile = new Tile(1, colours);
        //Tile newTile = new Tile(1, colours, scale);
        TilePrintableAwt newTile = new TilePrintableAwt(1, colours, scale);

        //Node n = new Node();
        newTile.xWorldSpace = 10 + 380 * Math.random();
        newTile.yWorldSpace = 10 + 380 * Math.random();
        newTile.setLbl(lbl);

//        n.nodeColor = new Color(250, 220, 100);

        tiles[numberOfTiles] = newTile;
        return numberOfTiles++;
    }

    /**
     * Wire refers to a connecting line between two nodes
     *
     * @param from
     * @param to
     * @param len
     */
    public void addWire(String from, String to, int len) {
        Wire newWire = new Wire();
        newWire.fromTile = findNode(from);    // get matching node index from tiles[]
        newWire.toTile = findNode(to);        // get matching node index from tiles[]
        newWire.wireLength = len;
        wires[nwires++] = newWire;
    }

    /**
     *
     * @param fromTile
     * @param fromSegment
     * @param toTile
     * @param toSegment
     * @param len
     */
    @SuppressWarnings("unused")
    public void addWire(int fromTile, int fromSegment, int toTile, int toSegment, int len) {
        //System.out.printf("**** TEST addWire() Start ****\n");
        boolean duplicate = false;

        if (fromTile == toTile) {
            System.out.printf("**** SWFM - addWire SAME TILE SUPPLIED FOR BOTH fromTile and toTile ****\n");
            return;
        }

        if (tiles[fromTile].tileSegment[fromSegment].nneighbours < CrystalGlobals.MAX_NEIGHBOURS) {
            if (nwires < CrystalGlobals.MAX_WIRES) {
                for (int aWire = 0; aWire < nwires; aWire++) {
                    // check for duplicate connected in the same direction
                    if (wires[aWire].fromTile == fromTile && wires[aWire].fromSegment == fromSegment) {
                        // check if match
                        if (wires[aWire].toTile == toTile && wires[aWire].toSegment == toSegment) {
                            // BAIL OUT
                            duplicate = true;
                        }
                    } // check for duplicate connected in the other direction
                    else if (wires[aWire].toTile == fromTile && wires[aWire].toSegment == fromSegment) {
                        // check if match
                        if (wires[aWire].fromTile == toTile && wires[aWire].fromSegment == toSegment) {
                            // BAIL OUT
                            duplicate = true;
                        }
                    }
                }

                if (!duplicate) {
                    Wire newWire = new Wire();
                    newWire.fromTile = fromTile;
                    newWire.fromSegment = fromSegment;
                    newWire.toTile = toTile;
                    newWire.toSegment = toSegment;
                    newWire.wireLength = len; // initial length

                    wires[nwires++] = newWire;

                    // notify both Segments of wire linkage
                    tiles[fromTile].tileSegment[fromSegment].nneighbours++;
                    tiles[toTile].tileSegment[toSegment].nneighbours++;
                    tiles[newWire.fromTile].tileSegment[fromSegment].addWireConnect(newWire);     // @todo - connect to segment array
                    tiles[newWire.toTile].tileSegment[toSegment].addWireConnect(newWire);         // @todo - connect to segment array

                    if ((Trace.debugLevel & Trace.DEBUG_WIRES) == Trace.DEBUG_WIRES) {
                        System.out.printf("**** addWire (%d)t%d:s%d - (%d)t%d:s%d, %d from %d,%d to %d,%d  (print from %d,%d to %d,%d)\n",
                                tiles[fromTile].getId(), fromTile, fromSegment, tiles[toTile].getId(), toTile, toSegment, nwires,
                                tiles[fromTile].tileSegment[fromSegment].xPointArray[tiles[toTile].WireLinkIndex],
                                tiles[fromTile].tileSegment[fromSegment].yPointArray[tiles[toTile].WireLinkIndex],
                                tiles[toTile].tileSegment[toSegment].xPointArray[tiles[toTile].WireLinkIndex],
                                tiles[toTile].tileSegment[toSegment].yPointArray[tiles[toTile].WireLinkIndex],
                                tiles[fromTile].tileSegment[fromSegment].xPointArrayPrint[tiles[toTile].WireLinkIndex],
                                tiles[fromTile].tileSegment[fromSegment].yPointArrayPrint[tiles[toTile].WireLinkIndex],
                                tiles[toTile].tileSegment[toSegment].xPointArrayPrint[tiles[toTile].WireLinkIndex],
                                tiles[toTile].tileSegment[toSegment].yPointArrayPrint[tiles[toTile].WireLinkIndex]);
                    }

                }
//                else
//                    System.out.printf("**** addWire BAIL due to duplicate ****\n");
            }
        }
    }

    /**
     * main method allows us to run as a standalone demo.
     */
    public static void main(String[] args) {
        Crystal graph = new Crystal();
        graph.mainInit();
    }

    public void run() {
        Thread me = Thread.currentThread();
        printDateTime();

        // display frame
        //graph.panel.aFrame.drawFrame(offScreenGraphics, palate);

        while (relaxer == me) {
            relax();
            /*
            if (random && (Math.random() < 0.03)) {
            Tile aTile = tiles[(int)(Math.random() * getNumberOfTiles())];
            if (!tileLockManager.isLocked(aTile.getId())) {
            //                if (!aTile.locked()) {
            aTile.xWorldSpace += 100*Math.random() - 50;
            aTile.yWorldSpace += 100*Math.random() - 50;
            }
            //graph.play(graph.getCodeBase(), "audio/drip.au");
            }
             */
            try {
                if (GraphPanel.runFast) {
                    Thread.sleep(0); // was 100, set to 0 for reduced sollution time
                } else {
                    Thread.sleep(200); // was 100, set to 0 for reduced sollution time
                }
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    synchronized void relax() {

        // adjust the wire length relative to the distance of a tile to the origin 0,0 ???? - @todo Why?
        int totalActiveWires = 0;
        int totalLockedTiles = 0;

        // display frame
        //aFrame.drawFrame(offScreenGraphics, palate);


        if (!pauseGame && numberOfTiles > 0) {
            // count number of locked tiles
            for (int aTileIndexNotId = 0; aTileIndexNotId < getNumberOfTiles(); aTileIndexNotId++) {
                tiles[aTileIndexNotId].lock = tileLockManager.getLockLevel(tiles[aTileIndexNotId].getId());
                if (tiles[aTileIndexNotId].locked()) {
                    totalLockedTiles++;
                }

            //               tiles[aTileIndexNotId].moved(false);  // prevent tiles from moving after this point
            }
            int locksInUse = tileLockManager.getLocksInUse();

            if (locksInUse != totalLockedTiles) {
                System.out.printf("relax() - locksInUse %d != totalLockedTiles %d - investigate\n", locksInUse, totalLockedTiles);
            }

            // ToDo - Check if solved
            if (tileLockManager.getLocksInUse() == tileLockManager.getMaxLocksAllowed()) {
                System.out.printf("relax() - ** POSSIBLE SOLUTION ** All Tiles Locked! - %d iterations\n", iterations);

                // Check if solved

                //dumpSolution() //todo
                printDateTime();

                pauseGame = true;
            }
        }

        if (showFloatingTiles)
        {

            if (!pauseGame && numberOfTiles > 0) {
                iterations++;

                // each wire
                for (int aWire = 0; aWire < nwires; aWire++) {
                    Wire eachWire = wires[aWire];

                    if (eachWire.wireActive) {
                        totalActiveWires++;
                    }

                    if (!eachWire.wireActive && activeWiresCheckbox) {
                        // WireLinkIndex is the point on a segment that the Wire links to (additional point in the poly array structure)
                        // xWorldSpace and yWorldSpace are the relative tile location on the panel
                        //                double vx = tiles[e.toTile].tileSegment[e.toSegment].xPointArray[tiles[e.toTile].WireLinkIndex] + tiles[e.toTile].xWorldSpace;
                        //                double vy = tiles[e.toTile].tileSegment[e.toSegment].yPointArray[tiles[e.toTile].WireLinkIndex] + tiles[e.toTile].yWorldSpace;
                        //if ((true == true) && !runFast) // DC-Trace.DEBUG - old or new version
                        {
                            double vx = tiles[eachWire.toTile].tileSegment[eachWire.toSegment].xPointArray[tiles[eachWire.toTile].WireLinkIndex] + tiles[eachWire.fromTile].tileSegment[eachWire.fromSegment].xPointArray[tiles[eachWire.fromTile].WireLinkIndex];
                            double vy = tiles[eachWire.toTile].tileSegment[eachWire.toSegment].yPointArray[tiles[eachWire.toTile].WireLinkIndex] + tiles[eachWire.fromTile].tileSegment[eachWire.fromSegment].yPointArray[tiles[eachWire.fromTile].WireLinkIndex];
                            double len = Math.sqrt(vx * vx + vy * vy);
                            len = (len == 0) ? .0001 : len;
                            double f = (wires[aWire].wireLength - len) / (len * 2);
                            double dx = f * vx;
                            double dy = f * vy;

                            if (!tileLockManager.isLocked(tiles[eachWire.toTile].getId())) {
                                tiles[eachWire.toTile].dx += dx;
                                tiles[eachWire.toTile].dy += dy;
                            } else if (!tileLockManager.isLocked(tiles[eachWire.fromTile].getId())) {
                                tiles[eachWire.fromTile].dx += dx;
                                tiles[eachWire.fromTile].dy += dy;
                            }
                        }

                    }
                }


                /// DC-Trace.DEBUG - new version
                /// @TODO - Select random unlocked tile and move it in the direction of the wire if not at target length
                int aTileIndexNotId = 0;
                /*
                /// requires at least one initial locked tile
                if(tileLockManager.getLocksInUse() == 0)
                //        if(totalLockedTiles == 0)
                {
                // TODO - Add some way of locking hint tiles
                //int randomTileLocked = (int)Math.round((ntiles-1) * Math.random());
                int randomTileLocked = tileLockManager.getRandomLockId();
                //tiles[randomTileLocked].lock = Tile.LOCKED; // Tile.TEMPORARY_LOCKED; // Tile.LOCKED; //
                tileLockManager.updateLock(tiles[randomTileLocked].lock, ntiles);
                aTile = randomTileLocked;
                System.out.printf("relax() - init random tile as locked as none currently exist\n");
                }
                else
                {
                /// select a random active (not locked) Tile from
                /// if locked randomly select one of its segments and one of the connecting wires randomly
                //aTile = (int)Math.round((ntiles-1) * Math.random());
                aTile = tileLockManager.getRandomLockId();  // returns tile.getId() not tileIndex
                }
                 */

                // pick a tile that is locked
                aTileIndexNotId = getTileIndexFromId(tileLockManager.getRandomLockId());        // gets the tileIndex to be used in tiles[]

                // the random locked tile selection is very slow when many tiles.  A short list/lookup of locked tiles has been implemented in the form of LockManager.
                double dx = 0;
                double dy = 0;

                if (tiles[aTileIndexNotId] == null) {
                    System.out.printf("relax() - primaryTile == null, This should never happen\n");
                } else {
                    Tile primaryTile = tiles[aTileIndexNotId];

                    // select a random active Wire from a random Segment
                    int randomSegment = (int) Math.round(3 * Math.random());

                    // check for edge segment
                    if (primaryTile.tileSegment[randomSegment].colour != 0) {
                        // TODO check if an active wire already exists and if so check if locked and valid.

                        // if not clear active wire (if one exists) and select a random wi
                        Wire selectedWire = primaryTile.tileSegment[randomSegment].getRandomWire();
                        //Wire selectedWire = primaryTile.tileSegment[randomSegment].getActiveWire();   // dc-debug may want to select any old wire
                        if (selectedWire != null) {
                            double lengthAdjustment = 1; //0.2;  // 20% of length

                            //                if(selectedWire.wireLength > 1)
                            //                if(selectedWire.wireLength > WIRE_LENGTH_TARGET)
                            //                if(selectedWire.wireLength > Tile.segmentScale)
                            //                {
                            Tile secondaryTile = null;
                            int secondaryTileSegment = 0;

                            if (selectedWire.fromTile == aTileIndexNotId) // fromTile is primaryTile, move toTile
                            {
                                secondaryTile = tiles[selectedWire.toTile];
                                secondaryTileSegment = selectedWire.toSegment;
                            } else if (selectedWire.toTile == aTileIndexNotId) // toTile is primaryTile, move fromTile
                            {
                                secondaryTile = tiles[selectedWire.fromTile];
                                secondaryTileSegment = selectedWire.fromSegment;
                            } else {
                                System.out.printf("relax() - ** BAD WIRE **\n");
                            }

                            if (!tileLockManager.isLocked(secondaryTile.getId())) {

                                // X&Y Relative to tile orientation
                                double relativePriX = primaryTile.xWorldSpace;
                                double relativePriY = primaryTile.yWorldSpace;
                                double relativeSecX = secondaryTile.xWorldSpace;
                                double relativeSecY = secondaryTile.yWorldSpace;

                                // orientate secondary tile to mate with primary
                                //secondaryTile.setOrientation( (randomSegment <= 1) ? (randomSegment + 2) : (randomSegment - 2));
                                int primaryOrientation = randomSegment + primaryTile.getOrientation();  // i.e. 2+1 = 3
                                primaryOrientation = (primaryOrientation < 0) ? (primaryOrientation + 4) : primaryOrientation; // i.e. na    // todo - replace with MAX/MIN
                                primaryOrientation = (primaryOrientation > 3) ? (primaryOrientation - 4) : primaryOrientation;  // i.e. ns
    //                                int secondaryOrientation = secondaryTileSegment + 2;  // 0+2=2

                                int secondaryOrientation = 0;
                                switch (secondaryTileSegment) {
                                    case 0:
                                        secondaryOrientation = secondaryTileSegment + primaryOrientation + 2;
                                        break;
                                    case 1:
                                        secondaryOrientation = secondaryTileSegment + primaryOrientation + 1;
                                        break;
                                    case 2:
                                        secondaryOrientation = secondaryTileSegment + primaryOrientation;
                                        break;
                                    case 3:
                                        switch (primaryOrientation) {
                                            case 0:
                                                secondaryOrientation = secondaryTileSegment + 3;
                                                break;
                                            case 1:
                                                secondaryOrientation = secondaryTileSegment;
                                                break;
                                            case 2:
                                                secondaryOrientation = secondaryTileSegment + 1;
                                                break;
                                            case 3:
                                                secondaryOrientation = secondaryTileSegment + 2;
                                                break;
                                        }
                                        break;
                                }
    //                                int secondaryOrientation = (primaryOrientation + 2) - secondaryTileSegment;  // 2+3+2=7
                                secondaryOrientation = (secondaryOrientation < 0) ? (secondaryOrientation + 4) : secondaryOrientation; // na = 7
                                secondaryOrientation = (secondaryOrientation > 3) ? (secondaryOrientation - 4) : secondaryOrientation; // 7-4=3
                                secondaryTile.setOrientation(secondaryOrientation);
    //                                secondaryTile.getOrientation() = Math.abs(primaryOrientation - secondaryOrientation + 2); // 3-3+2= 2
    //                                if(secondaryTile.getOrientation() < 0)
    //                                    secondaryTile.setOrientation(secondaryTile.getOrientation() + 4; // -1+4 = 3


                                if (((primaryOrientation) != (secondaryTile.getOrientation() + 2)) && (primaryOrientation != (secondaryTile.getOrientation() - 2))) //                                if( ((primaryOrientation) != (secondaryTileSegment + secondaryTile.getOrientation() + 2)) && (primaryOrientation != (secondaryTileSegment + secondaryTile.getOrientation() - 2)) )
                                {
                                    if ((Trace.debugLevel & Trace.DEBUG_RELAX2) == Trace.DEBUG_RELAX2) {
                                        System.out.printf("relax() - segment %d(prim) tile_Orientation %d(prim) relativeOrient %d(Prim), segment %d(sec) tileOrientation %d(sec)\n", randomSegment, primaryTile.getOrientation(), primaryOrientation, secondaryTileSegment, secondaryTile.getOrientation());
                                        System.out.printf("relax() - **** Relative Orient %d(prim), %d(sec)\n", primaryOrientation, secondaryOrientation);
                                    }
                                }

                                relativePriX += rebalanceX(randomSegment, primaryTile.getOrientation());     // relative to orientation
                                relativePriY += rebalanceY(randomSegment, primaryTile.getOrientation());     // relative to orientation
                                relativeSecX += rebalanceX(secondaryTileSegment, secondaryTile.getOrientation());     // relative to orientation
                                relativeSecY += rebalanceY(secondaryTileSegment, secondaryTile.getOrientation());     // relative to orientation

                                dx = relativePriX - relativeSecX;
                                dy = relativePriY - relativeSecY;

                                if (true == false) {
                                    secondaryTile.dx = dx * lengthAdjustment;      // TODO LOOK HERE
                                    secondaryTile.dy = dy * lengthAdjustment;
                                } else {
                                    //tileLockManager.updateLock(secondaryTile.getId(), Tile.TEMPORARY_LOCKED);
                                    //secondaryTile.lock = Tile.TEMPORARY_LOCKED;
                                    if ((Trace.debugLevel & Trace.DEBUG_RELAX2) == Trace.DEBUG_RELAX2) {
                                        System.out.printf("dx = %f, dy %f sec orientation %d\n", dx, dy, secondaryTile.getOrientation());
                                    }

                                    // TODO - adjuct relative to orientation
                                    if (relativePriX > relativeSecX) {
                                        dx = relativePriX - relativeSecX;
                                        secondaryTile.dx += dx * lengthAdjustment;
                                    } else {
                                        dx = relativeSecX - relativePriX;
                                        secondaryTile.dx -= dx * lengthAdjustment;
                                    }

                                    if (relativePriY > relativeSecY) {
                                        dy = relativePriY - secondaryTile.yWorldSpace;
                                        secondaryTile.dy += dy * lengthAdjustment;
                                    } else {
                                        dy = relativeSecY - relativePriY;
                                        secondaryTile.dy -= dy * lengthAdjustment;
                                    }

                                } // true == true
                                secondaryTile.tileSegment[randomSegment].setActiveWire(selectedWire);


                                // lock init to a vaulue relative to the number of tiles already locked, should be (nTiles - locksInUse)factorial (but for now use sqr)
                                tileLockManager.updateLock(secondaryTile.getId(), (getNumberOfTiles() - tileLockManager.getLocksInUse()) * (getNumberOfTiles() - tileLockManager.getLocksInUse()));
    //                                tileLockManager.updateLock(secondaryTile.getId(), getNumberOfTiles() - tileLockManager.getLocksInUse());
                                secondaryTile.lock = tileLockManager.getLockLevel(secondaryTile.getId());


                                if ((Trace.debugLevel & Trace.DEBUG_RELAX1) == Trace.DEBUG_RELAX1) {
                                    System.out.printf("relax() - tile_%d(prim), randomSegment_%d(prim), tile_%d(sec), randomSegment_%d(sec) moved by x:%f y:%f\n", aTileIndexNotId, randomSegment, secondaryTile.getId(), selectedWire.toSegment, secondaryTile.dx, secondaryTile.dy);
                                }

                                // move locked Tile at this point here as it will not be moved later
                                secondaryTile.xWorldSpace += secondaryTile.dx;
                                secondaryTile.yWorldSpace += secondaryTile.dy;
                                secondaryTile.dx = 0;
                                secondaryTile.dy = 0;


                                //System.out.printf("TODO - Check if the secondaryTile neighbours are a match if not release the secondaryTile\n");
                                // TODO - Check if the secondaryTile neighbours are a match if not release the secondaryTile
                                // first update all wire lengths
                                for (int eachSegment = 0; eachSegment < 4; eachSegment++) {
                                    // check each wire for a link between these two segments
                                    Wire secondaryWire = secondaryTile.tileSegment[eachSegment].getActiveWire();
                                    //for(int ii=0; ii<secondaryTile.tileSegment[eachSegment].nConnectedWires; ii++) {
                                    //Wire secondaryWire = secondaryTile.tileSegment[eachSegment].connectedWires[ii];

                                    if (secondaryWire != null) {
                                        Tile neighbouringTile = null;
                                        if (secondaryTile == tiles[secondaryWire.fromTile]) {
                                            if (tileLockManager.isLocked(tiles[selectedWire.toTile].getId())) {
                                                neighbouringTile = tiles[secondaryWire.toTile];

                                                // TODO - check touching segments for match
                                                if (secondaryTile.tileSegment[randomSegment].colour == neighbouringTile.tileSegment[secondaryWire.toSegment].colour) // match
                                                //secondaryTile.lock += Tile.TEMPORARY_LOCKED; // increase tile fix duration if a neighbour edge match
                                                {
                                                    tileLockManager.increaseLock(secondaryTile.getId(), Tile.TEMPORARY_LOCKED);
                                                }
                                            }
                                        } else if (secondaryTile == tiles[secondaryWire.toTile]) {
                                            if (tileLockManager.isLocked(tiles[selectedWire.fromTile].getId())) {
                                                neighbouringTile = tiles[secondaryWire.fromTile];

                                                // TODO - check touching segments for match
                                                if (secondaryTile.tileSegment[randomSegment].colour == neighbouringTile.tileSegment[secondaryWire.fromSegment].colour) {
                                                    tileLockManager.increaseLock(secondaryTile.getId(), Tile.TEMPORARY_LOCKED);
                                                }
                                            }
                                        } else {
                                            // no match
                                            tileLockManager.removeLock(secondaryTile.getId());
                                            secondaryTile.tileSegment[eachSegment].setActiveWire(null);

                                        }
                                    }
                                }
                            } // not locked
                            else {
                                // should wire be inactive if we are here?
                                }
                        } // selectedWire != null
                    } // edge colour
                } // primaryTile == null

                int lockedNeighbours = 0;
                for (int segmentIndex = 0; segmentIndex < 4; segmentIndex++) {
    //                   System.out.printf("segmentIndex = %d, aTileIndexNotId = %d\n", segmentIndex, aTileIndexNotId);
    //                   System.out.printf("nConnectedWires = %d\n", tiles[aTileIndexNotId].tileSegment[segmentIndex].nConnectedWires);
                    for (int wireIndex = 0; wireIndex < tiles[aTileIndexNotId].tileSegment[segmentIndex].nConnectedWires; wireIndex++) {
                        if (tiles[aTileIndexNotId].tileSegment[segmentIndex].connectedWires[wireIndex].wireActive == true) {
                            lockedNeighbours++;
                        }
                    }
                }
                // TODO decrease lock relative to number of locked neighbours if less than 2 locked neighbours
                if (tileLockManager.isLocked(tiles[aTileIndexNotId].getId())) {
                    if (lockedNeighbours == 3) {
                        tileLockManager.decreaseLock(tiles[aTileIndexNotId].getId(), 1);
                    } else if (lockedNeighbours < 3) {
                        tileLockManager.decreaseLock(tiles[aTileIndexNotId].getId(), 2);
                    }
                }

                if ((Trace.debugLevel & Trace.DEBUG_WIRES) == Trace.DEBUG_WIRES) {
                    System.out.printf("totalActiveWires = %d, totalLockedTiles = %d\n", totalActiveWires, totalLockedTiles);
                }


                if (activeWiresCheckbox) {
                    //            for (aTile = 0 ; aTile < tileLockManager.getLocksInUse(); aTile++) {
                    // adjust each tile location relative to all other tiles to maintain an even distance from neighbouring tiles ???? - @todo Why?
                    //                Tile eachTile = tiles[tileLockManager.getLockId(aTile)];
                    for (aTileIndexNotId = 0; aTileIndexNotId < getNumberOfTiles(); aTileIndexNotId++) {
                        // pick a tile that is locked
                        if (!tileLockManager.isLocked(tiles[aTileIndexNotId].getId())) {
                            //                 if(!tiles[aTile].locked() && (tiles[aTile].lock != Tile.UNLOCKED)) {        // pick a tile that is locked
                            Tile eachTile = tiles[aTileIndexNotId];
                            //                    double dx = 0;
                            //                    double dy = 0;
                            dx = 0;
                            dy = 0;

                            for (int seg = 0; seg < 4; seg++) {
                                double shortestWire = 0;
                                int shortestWireIndex = 0;

                                // adjust neighbour tile proximity based on orientation of primary tile
                                //                    double vx = eachTile.xWorldSpace;
                                //                    double vy = eachTile.yWorldSpace;
                                //                    vx += rebalanceX(seg, eachTile.getOrientation());     // relative to orientation
                                //                    vy += rebalanceY(seg, eachTile.getOrientation());     // relative to orientation


                                // determine the shortest wire (i.e. closest neighbour)
                                for (int wireIndex = 0; wireIndex < eachTile.tileSegment[seg].nConnectedWires; wireIndex++) {
                                    if (eachTile.tileSegment[seg].connectedWires[wireIndex].wireLength < shortestWire) {
                                        if (tileLockManager.isLocked(eachTile.tileSegment[seg].connectedWires[wireIndex].neighbourId(eachTile.getId()))) {
                                            shortestWire = eachTile.tileSegment[seg].connectedWires[wireIndex].wireLength;
                                            shortestWireIndex = wireIndex;
                                        }
                                    }
                                }
                                Wire aWire = eachTile.tileSegment[seg].connectedWires[shortestWireIndex];
                                if (aWire != null && !aWire.wireActive) {
                                    Tile aNeighbourTile = null;
                                    // select the closet neighbour
                                    aNeighbourTile = tiles[eachTile.tileSegment[seg].connectedWires[shortestWireIndex].neighbourId(aTileIndexNotId)];
                                    /*
                                    if(aTile != eachTile.tileSegment[seg].connectedWires[shortestWireIndex].fromTile)
                                    aNeighbourTile = tiles[eachTile.tileSegment[seg].connectedWires[shortestWireIndex].fromTile];
                                    else
                                    aNeighbourTile = tiles[eachTile.tileSegment[seg].connectedWires[shortestWireIndex].toTile];
                                     */
                                    // move neighbour tile nearer if not locked or if only just locked
                                    //if(!aNeighbourTile.locked() || (aNeighbourTile.locked == Tile.TEMPORARY_LOCKED)
                                    if (!tileLockManager.isLocked(aNeighbourTile.getId())) //                            if(!aNeighbourTile.locked())
                                    {
                                        double vx = eachTile.xWorldSpace - aNeighbourTile.xWorldSpace;
                                        double vy = eachTile.yWorldSpace - aNeighbourTile.yWorldSpace;
                                        double len = vx * vx + vy * vy;

                                        // check for target length
                                        if ((int) Math.abs(len) != CrystalGlobals.WIRE_LENGTH_TARGET) {
                                            // segmentScale
                                            // hysterissis
                                            if (len < (CrystalGlobals.WIRE_LENGTH_TARGET + 5) || len > (CrystalGlobals.WIRE_LENGTH_TARGET - 5)) {
                                                //                                        dx -= 50 * Math.random();
                                                //                                        dy -= 50 * Math.random();
                                                //                                    dx += 0.1;
                                                //                                    dy += 0.1;
                                                //                                        if((Trace.debugLevel & Trace.DEBUG_GENERAL) == Trace.DEBUG_GENERAL)
                                                //                                            System.out.printf("Histerissis\n");
                                            } else {
                                                if (vx < 0) // Pri left of Neighbour
                                                {
                                                    dx -= vx / len;
                                                } else {
                                                    dx += vx / len;
                                                }

                                                if (vy < 0) // Pri above Neighbour
                                                {
                                                    dy -= vy / len;
                                                } else {
                                                    dy += vy / len;
                                                }

                                                if ((Trace.debugLevel & Trace.DEBUG_GENERAL) == Trace.DEBUG_GENERAL) {
                                                    System.out.printf("dx = %f, dy %f\n", dx, dy);
                                                }
                                            }

                                            double dlen = dx * dx + dy * dy;
                                            if (dlen > 0) {
                                                dlen = Math.sqrt(dlen) / 2;
                                                aNeighbourTile.dx += dx / dlen;
                                                aNeighbourTile.dy += dy / dlen;
                                            //System.out.printf("totalActiveWires = %d, totalLockedTiles = %d\n", totalActiveWires, totalLockedTiles);
                                            }
                                        }
                                    }
                                }
                            }

                        /*
                        for (int jj = 0 ; jj < ntiles ; jj++)
                        {
                        // skip self
                        if (aTile == jj) {
                        continue;
                        }
                        Tile aNeighbourTile = tiles[jj];
                        double vx = eachTile.xWorldSpace - aNeighbourTile.xWorldSpace;
                        double vy = eachTile.yWorldSpace - aNeighbourTile.yWorldSpace;
                        double len = vx * vx + vy * vy;
                        if ((int)Math.abs(len) != WIRE_LENGTH_TARGET)
                        {
                        if (len == 0)
                        {
                        dx += Math.random();
                        dy += Math.random();
                        }
                        else if (len < 100*100)
                        {
                        dx += vx / len;
                        dy += vy / len;
                        }
                        }
                        }
                        double dlen = dx * dx + dy * dy;
                        if (dlen > 0)
                        {
                        dlen = Math.sqrt(dlen) / 2;
                        eachTile.dx += dx / dlen;
                        eachTile.dy += dy / dlen;
                        }
                         */
                        } // locked
                    }
                }

                // move each tile location by its delta calculated above
                Dimension dimension = getSize();
                //System.out.printf("**** d.width %d d.height %d ****\n", d.width, d.height);
                for (int tileIndexNotId = 0; tileIndexNotId < getNumberOfTiles(); tileIndexNotId++) {
                    Tile eachTile = tiles[tileIndexNotId];
                    //System.out.printf("tileIndex=%d, dx=%f, dy=%f\n", tileIndex, eachTile.dx, eachTile.dy);
                    // move neighbour tile nearer if not locked or if only just locked
                    if (!tileLockManager.isLocked(eachTile.getId(), Tile.TEMPORARY_LOCKED)) {     // Allow TEMPORARY_LOCKED this move
                        //if (!eachTile.locked() || (eachTile.lock == Tile.TEMPORARY_LOCKED)) {     // Allow TEMPORARY_LOCKED this move
                        // TODO - Kills animation
                        //eachTile.xWorldSpace += Math.max(-5, Math.min(5, eachTile.dx));
                        //eachTile.yWorldSpace += Math.max(-5, Math.min(5, eachTile.dy));
                        eachTile.xWorldSpace += eachTile.dx;
                        eachTile.yWorldSpace += eachTile.dy;
                    }

                    // move away from left edge
                    if (eachTile.xWorldSpace < (Tile.segmentScale / 2)) {
                        eachTile.xWorldSpace = (Tile.segmentScale / 2);
                    } // move away from right edge
                    else if (eachTile.xWorldSpace > (dimension.width - Tile.segmentScale)) {   // segmentScale
                        eachTile.xWorldSpace = dimension.width - Tile.segmentScale;
                    }

                    // move away from upper/top edge
                    if (eachTile.yWorldSpace < (Tile.segmentScale / 2)) {
                        eachTile.yWorldSpace = (Tile.segmentScale / 2);
                    } // move away from lower/bottom edge
                    else if (eachTile.yWorldSpace > dimension.height - Tile.segmentScale) {  // segmentScale
                        eachTile.yWorldSpace = dimension.height - Tile.segmentScale;
                    }
                    // decelerate
                    eachTile.dx /= 2;
                    eachTile.dy /= 2;


                    // set Tile rotation angle
                    int shortestActiveWireOnSegment = 0;
                    double tempWireLength = 2000;
                    for (int eachSegment = 0; eachSegment < 4; eachSegment++) {
                        Wire aWire = eachTile.tileSegment[eachSegment].getActiveWire();
                        if (aWire != null) {
                            if (tempWireLength > aWire.wireLength) {
                                tempWireLength = aWire.wireLength;
                                shortestActiveWireOnSegment = eachSegment;
                            }
                        }
                    }

                    if (tempWireLength < CrystalGlobals.WIRE_LENGTH_TARGET_PROXIMITY) // DC-Trace.DEBUG - added to reduce the cpu load
                    {
                        Wire aWire = eachTile.tileSegment[shortestActiveWireOnSegment].getActiveWire();
                        if (aWire != null) {
                            eachTile.rotationVector = getActiveWireAngle(aWire, tileIndexNotId);
                        } else {
                            eachTile.rotationVector = 0;
                        }
                    } else {
                        eachTile.rotationVector = 0;
                    }

                    if (tileLockManager.isLocked(eachTile.getId())) {
                        tileLockManager.decreaseLock(eachTile.getId(), 1);
                    }
                }

                for (aTileIndexNotId = 0; aTileIndexNotId < getNumberOfTiles(); aTileIndexNotId++) {
                    tiles[aTileIndexNotId].moveTile((int) tiles[aTileIndexNotId].xWorldSpace, (int) tiles[aTileIndexNotId].yWorldSpace);
                }
            } // pauseGame
        }
        repaint();

        if (pauseGame) {
            // insert a sleep if game over
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }

        }
    }  // relax


    @Override
    public synchronized void update(Graphics onScreenGraphics) {

        Dimension dimension = getSize();
        if ((offScreenImage == null) || (dimension.width != offScreenDimension.width) || (dimension.height != offScreenDimension.height)) {
            offScreenImage = createImage(dimension.width, dimension.height);
            offScreenDimension = dimension;
            if (offScreenGraphics != null) {
                offScreenGraphics.dispose();
            }
            offScreenGraphics = offScreenImage.getGraphics();
            offScreenGraphics.setFont(getFont());
        }

        offScreenGraphics.setColor(getBackground());
        offScreenGraphics.fillRect(0, 0, dimension.width, dimension.height);

        // change mode of operation from frame based to floating wire based
//        if(showFloatingTilesCheckbox)
//            showFloatingTiles = true;
//        else
//            showFloatingTiles = false;

        // display floating tile based implementation
        if (showFloatingTiles)
        {

            // Paint Inactive Wires
            for (int i = 0; i < nwires; i++) {
                Wire e = wires[i];
                if (!e.wireActive) {
                    int x1 = (int) tiles[e.fromTile].tileSegment[e.fromSegment].xPointArrayPrint[3];  // segment.wireConnectPoint      TODO
                    int y1 = (int) tiles[e.fromTile].tileSegment[e.fromSegment].yPointArrayPrint[3];  // segment.wireConnectPoint      TODO
                    int x2 = (int) tiles[e.toTile].tileSegment[e.toSegment].xPointArrayPrint[3];    // segment.wireConnectPoint      TODO
                    int y2 = (int) tiles[e.toTile].tileSegment[e.toSegment].yPointArrayPrint[3];    // segment.wireConnectPoint      TODO
                    //int len = (int)Math.abs(Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2)) - e.wireLength);
                    int len = (int) Math.abs(Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)));
    
                    if(!suppressGraphics)
                    {
        //                offScreenGraphics.setColor(nodeColor) ;
                        offScreenGraphics.setColor(palate[0]);       // grey
        //                offScreenGraphics.setColor((len < 10) ? arcColor1 : (len < 20 ? arcColor2 : arcColor3)) ;

                        if (showWiresModeCheckbox) {
                            offScreenGraphics.drawLine(x1, y1, x2, y2);       // draw / paint connecting wire
                        }
                        if (stressModeCheckbox) {
                            String lbl = String.valueOf(len);
                            offScreenGraphics.setColor(stressColor);
                            offScreenGraphics.drawString(lbl, x1 + (x2 - x1) / 2, y1 + (y2 - y1) / 2);
                            offScreenGraphics.setColor(wireColor);
                        }
                        
                    }
                }
            }

    
            // Paint Active Wires
            for (int i = 0; i < nwires; i++) {
                Wire eachWire = wires[i];
                if (eachWire.wireActive) {
                    int x1 = (int) tiles[eachWire.fromTile].tileSegment[eachWire.fromSegment].xPointArrayPrint[3];  // segment.wireConnectPoint      TODO
                    int y1 = (int) tiles[eachWire.fromTile].tileSegment[eachWire.fromSegment].yPointArrayPrint[3];  // segment.wireConnectPoint      TODO
                    int x2 = (int) tiles[eachWire.toTile].tileSegment[eachWire.toSegment].xPointArrayPrint[3];    // segment.wireConnectPoint      TODO
                    int y2 = (int) tiles[eachWire.toTile].tileSegment[eachWire.toSegment].yPointArrayPrint[3];    // segment.wireConnectPoint      TODO
                    //int len = (int)Math.abs(Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2)) - e.wireLength);
                    int len = (int) Math.abs(Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)));
    
                    if(!suppressGraphics)
                    {
                        //offScreenGraphics.setColor((len < 10) ? arcColor1 : (len < 20 ? arcColor2 : arcColor3)) ;
                        offScreenGraphics.setColor(palate[tiles[eachWire.fromTile].tileSegment[eachWire.fromSegment].colour]);
                        if (showWiresModeCheckbox) {
                            offScreenGraphics.drawLine(x1, y1, x2, y2);       // draw / paint connecting wire
                        }
                        if (stressModeCheckbox) {
                            String lbl = String.valueOf(len);
                            offScreenGraphics.setColor(stressColor);
                            offScreenGraphics.drawString(lbl, x1 + (x2 - x1) / 2, y1 + (y2 - y1) / 2);
                            offScreenGraphics.setColor(wireColor);
                        }
                    }
                }
            }
    
            /*      for (int aTileIndexNotId = 0  ; aTileIndexNotId < getNumberOfTiles() ; aTileIndexNotId++) {
            if(aTileIndexNotId < getNumberOfTiles())  // todo - what is this all about ????
            tiles[aTileIndexNotId].moveTile((int)tiles[aTileIndexNotId].xWorldSpace, (int)tiles[aTileIndexNotId].yWorldSpace);
            }
             */
            
        } // showFloatingTiles
        else
        {
            // display frameStructure based implementation.  also runs the update for the frameStructure base implementation
            Crystal.panel.aFrame.drawFrame(offScreenGraphics, palate);          // display frame based implementation
            
        }
        
        loadTileColors();

        if (showFloatingTiles) {
            for (int aTileIndexNotId = 0; aTileIndexNotId < getNumberOfTiles(); aTileIndexNotId++) {
                /*
                if(!tileLockManager.isLocked(tiles[aTileIndexNotId].getId()))
                tiles[aTileIndexNotId].lock = Tile.TEMPORARY_LOCKED;
                else
                tiles[aTileIndexNotId].lock = Tile.UNLOCKED;
                 */
                tiles[aTileIndexNotId].PrintTile(offScreenGraphics, palate, showTileIdCheckbox, drawRotationModeCheckbox, wireframeTileModeCheckbox);
            }
        }

        // Display stats
        String stStats = String.valueOf(tileLockManager.getLocksInUse());
        offScreenGraphics.drawString("Locks in Use: ", dimension.width - 140, dimension.height);
        offScreenGraphics.drawString(stStats, dimension.width - 50, dimension.height);
        stStats = String.valueOf(iterations);
        offScreenGraphics.drawString("Iterations: ", dimension.width - 140, dimension.height - 20);
        offScreenGraphics.drawString(stStats, dimension.width - 50, dimension.height - 20);

        // display frame
//        aFrame.drawFrame(offScreenGraphics, palate);

//        if(!suppressGraphics)
        onScreenGraphics.drawImage(offScreenImage, 0, 0, null);
    }

    /**
     *
     * @param seg
     * @param orientation
     * @return offset X
     */
    @SuppressWarnings("unused")
    public int rebalanceX(int seg, int orientation) {
        int offsetX = 0;
        int relativeSegment = seg + orientation;
        int scalingFactor = Tile.segmentScale / 2;
//        scalingFactor = Tile.segmentScale*2;      // debug

        if (relativeSegment > 3) {
            relativeSegment -= 4;
        }

        // top segment
        switch (relativeSegment) {
            case 0:
                break;  // top segment, no change to xWorldSpace
            case 1:
                offsetX = scalingFactor;
                break;  // right segment, increase xWorldSpace
            case 2:
                break;  // lower segment, no change to xWorldSpace
            case 3:
                offsetX = scalingFactor * -1;
                break;  // left segment, decrease xWorldSpace
        }
        if ((Trace.debugLevel & Trace.DEBUG_REBALANCE) == Trace.DEBUG_REBALANCE) {
            System.out.printf("rebalanceX() segment %d orientation %d relative segment %d scaling factor %d\n", seg, orientation, seg + orientation, offsetX);
        }
        return offsetX;
    }

    /**
     *
     * @param seg
     * @param orientation
     * @return offset Y
     */
    @SuppressWarnings("unused")
    public int rebalanceY(int seg, int orientation) {
        int offsetY = 0;
        int relativeSegment = seg + orientation;
        int scalingFactor = Tile.segmentScale / 2;
//        scalingFactor = Tile.segmentScale*2;      // debug

        if (relativeSegment > 3) {
            relativeSegment -= 4;
        }

        // top segment
        switch (relativeSegment) {
            case 0:
                offsetY = scalingFactor * -1;   // top segment, decrease yWorldSpace
                break;
            case 1:                             // right segment, no change to yWorldSpace
                break;
            case 2:
                offsetY = scalingFactor;  // lower segment, increase yWorldSpace
                break;
            case 3:                             // left segment, no change to yWorldSpace
                break;
        }
        if ((Trace.debugLevel & Trace.DEBUG_REBALANCE) == Trace.DEBUG_REBALANCE) {
            System.out.printf("rebalanceY() segment %d orientation %d relative segment %d scaling factor %d\n", seg, orientation, seg + orientation, offsetY);
        }
        return offsetY;
    }

    //1.1 event handling
    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        numMouseButtonsDown++;
        addMouseMotionListener(this);
        double bestdist = Double.MAX_VALUE;

        int x = e.getX();
        int y = e.getY();
        for (int aTileIndexNotId = 0; aTileIndexNotId < getNumberOfTiles(); aTileIndexNotId++) {
            Tile n = tiles[aTileIndexNotId];
            double dist = (n.xWorldSpace - x) * (n.xWorldSpace - x) + (n.yWorldSpace - y) * (n.yWorldSpace - y);
            if (dist < bestdist) {
                pick = n;
                bestdist = dist;
            }
        }
        picklocked = pick.lock;   // store current locked state for later restore
        pick.lock = Tile.LOCKED;
        pick.xWorldSpace = x;
        pick.yWorldSpace = y;

        //if((Trace.debugLevel & Trace.DEBUG_MOUSE) == Trace.DEBUG_MOUSE)
        System.out.printf("mousePressed() pick.x %f pick.y %f\n", pick.xWorldSpace, pick.yWorldSpace);

        repaint();
        e.consume();
    }

    public void mouseReleased(MouseEvent e) {
        numMouseButtonsDown--;
        removeMouseMotionListener(this);

        pick.lock = picklocked;     // restore previous locked state
        pick.xWorldSpace = e.getX();
        pick.yWorldSpace = e.getY();

        //if((Trace.debugLevel & Trace.DEBUG_MOUSE) == Trace.DEBUG_MOUSE)
        System.out.printf("mouseReleased() pick.x %f pick.y %f\n", pick.xWorldSpace, pick.yWorldSpace);

        if (numMouseButtonsDown == 0) {
            pick = null;
        }

        repaint();
        e.consume();
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        pick.xWorldSpace = e.getX();
        pick.yWorldSpace = e.getY();
        repaint();
        e.consume();
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void start() {
        relaxer = new Thread(this);
        relaxer.start();
    }

    public void stop() {
        relaxer = null;
    }

    @Override
    public void processEvent(AWTEvent e) {
        if (e.getID() == Event.WINDOW_DESTROY) {
            System.exit(0);
        }
        else if (e instanceof ContainerEvent) {
            processContainerEvent((ContainerEvent)e);
            return;
        }

        super.processEvent(e);
    }

    public static String now(String dateFormat) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(cal.getTime());
    }

    public void printDateTime() {
//     System.out.println(now("dd MMMMM yyyy"));
//     System.out.println(now("yyyyMMdd"));
//     System.out.println(now("dd.MM.yy"));
//     System.out.println(now("MM/dd/yy"));
        System.out.println(now("yyyy.MM.dd 'at' hh:mm:ss z"));
//     System.out.println(now("yyyy.MM.dd G 'at' hh:mm:ss z"));
//     System.out.println(now("EEE, MMM d, ''yy"));
//     System.out.println(now("h:mm a"));
//     System.out.println(now("H:mm:ss:SSS"));
//     System.out.println(now("K:mm a,z"));
//     System.out.println(now("yyyy.MMMMM.dd GGG hh:mm aaa"));
    }
}
