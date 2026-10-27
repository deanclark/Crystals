package com.crystalwink.crystals.GraphPanel;

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

import com.crystalwink.crystals.Crystal;
import com.crystalwink.crystals.FrameStructure.FrameStructure;
import com.crystalwink.crystals.TilePrintableAwt.TilePrintableAwt;
import com.crystalwink.crystalscommon.CrystalGlobals;
import com.crystalwink.crystalscommon.LockManager.LockManager;
import com.crystalwink.crystalscommon.Tile.Tile;
import com.crystalwink.crystalscommon.Segment.Segment;
import com.crystalwink.crystalscommon.Trace;
import com.crystalwink.crystalscommon.Wire.Wire;

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
    public static boolean showFloatingTiles = true; // prevent display of floating tiles
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
    public boolean largeTilesCheckbox;
    public static final int TILE_SCALE_NORMAL = 15;   // matches the scale tiles are created with
    public static final int TILE_SCALE_LARGE = 28;

    /**
     * Change the size tiles are displayed at. Tiles keep their centres and are then pushed apart on the next
     * relax() so that they butt up rather than overlap at the new size.
     */
    public synchronized void setTileScale(int scale) {
        // a crystal on the grid keeps its cells when the tiles change size: only the pixel positions are recomputed
        boolean keepCrystal = hasUsableSize() && !Double.isNaN(anchoredLeft);
        boolean anyMember = false;
        for (int tileIndex = 0; tileIndex < numberOfTiles; tileIndex++) {
            anyMember |= crystalMember[tileIndex];
        }
        keepCrystal &= anyMember;
        double oldLeft = boardLeft();
        double oldTop = boardTop();
        double oldScale = Tile.getSegmentScale();
        double[] cellX = new double[numberOfTiles];
        double[] cellY = new double[numberOfTiles];
        for (int tileIndex = 0; tileIndex < numberOfTiles; tileIndex++) {
            cellX[tileIndex] = Math.rint((tiles[tileIndex].xWorldSpace - oldLeft) / oldScale - 0.5) + 0.5;   // snapped to the cell
            cellY[tileIndex] = Math.rint((tiles[tileIndex].yWorldSpace - oldTop) / oldScale - 0.5) + 0.5;
        }
        double[][] bestX = {runBest.x, globalBest.x};
        double[][] bestY = {runBest.y, globalBest.y};
        double[][] bestCellX = new double[2][];
        double[][] bestCellY = new double[2][];
        for (int board = 0; board < 2 && keepCrystal; board++) {
            if (bestX[board] != null) {
                bestCellX[board] = new double[numberOfTiles];
                bestCellY[board] = new double[numberOfTiles];
                for (int tileIndex = 0; tileIndex < numberOfTiles; tileIndex++) {
                    bestCellX[board][tileIndex] = Math.rint((bestX[board][tileIndex] - oldLeft) / oldScale - 0.5) + 0.5;
                    bestCellY[board][tileIndex] = Math.rint((bestY[board][tileIndex] - oldTop) / oldScale - 0.5) + 0.5;
                }
            }
        }

        Tile.setSegmentScale(scale);
        for (int tileIndex = 0; tileIndex < numberOfTiles; tileIndex++) {
            tiles[tileIndex].rescale(scale);
            if (!keepCrystal) {
                tileLockManager.removeLock(tiles[tileIndex].getId());   // the grid cell size changed: grow a new crystal
            }
        }
        anchoredLeft = Double.NaN;
        anchoredTop = Double.NaN;
        if (keepCrystal) {
            anchoredLeft = boardLeft();
            anchoredTop = boardTop();
            for (int tileIndex = 0; tileIndex < numberOfTiles; tileIndex++) {
                if (crystalMember[tileIndex]) {
                    tiles[tileIndex].xWorldSpace = anchoredLeft + cellX[tileIndex] * scale;
                    tiles[tileIndex].yWorldSpace = anchoredTop + cellY[tileIndex] * scale;
                    tiles[tileIndex].dx = 0;
                    tiles[tileIndex].dy = 0;
                }
            }
            for (int board = 0; board < 2; board++) {
                if (bestCellX[board] != null) {
                    for (int tileIndex = 0; tileIndex < numberOfTiles; tileIndex++) {
                        bestX[board][tileIndex] = anchoredLeft + bestCellX[board][tileIndex] * scale;
                        bestY[board][tileIndex] = anchoredTop + bestCellY[board][tileIndex] * scale;
                    }
                }
            }
            memory.clear();
            resolveTileOverlaps(tiles, numberOfTiles, tileLockManager, getSize(), settledTiles());
        } else {
            java.util.Arrays.fill(crystalMember, false);
            seedTileIndex = -1;
            runBestPlaced = 0;
            runBest.valid = false;
            globalBest.valid = false;
            searchIterations = 0;
            runsStarted = 1;
        }
        if (aFrame != null) {
            boolean large = scale >= TILE_SCALE_LARGE;
            FrameStructure.LINE_LENGTH = large ? 40 : 6;
            FrameStructure.CELL_SCALE = large ? 40 : 6;
        }
        repaint();
    }
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

        // scatter over the whole panel (380 x 380 until the panel has a size)
        double spreadX = getSize().width > 40 ? getSize().width - 20 : 380;
        double spreadY = getSize().height > 40 ? getSize().height - 20 : 380;
        newTile.xWorldSpace = 10 + spreadX * Math.random();
        newTile.yWorldSpace = 10 + spreadY * Math.random();

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

            // crystal tiles show (and are treated by Scramble / Shake) as locked even after their lock level has run out
            for (int aTileIndexNotId = 0; aTileIndexNotId < getNumberOfTiles(); aTileIndexNotId++) {
                if (crystalMember[aTileIndexNotId] && tiles[aTileIndexNotId].lock == Tile.UNLOCKED) {
                    tiles[aTileIndexNotId].lock = Tile.LOCKED;
                }
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

            if (!pauseGame && numberOfTiles > 0 && hasUsableSize()) {
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

                // the crystal grows from a locked tile; the first one is a corner tile set in the corner of the playing area
                if (tileLockManager.getLocksInUse() == 0 && pendingImport != null) {
                    try {
                        importBoard(pendingImport);
                        System.out.println("Imported " + pendingImport.toAbsolutePath());
                    } catch (java.io.IOException | RuntimeException problem) {
                        System.err.println("Could not import " + pendingImport + ": " + problem);
                    }
                    pendingImport = null;
                }
                if (tileLockManager.getLocksInUse() == 0) {
                    seedCrystal();
                }
                aTileIndexNotId = getTileIndexFromId(tileLockManager.getRandomLockId());        // gets the tileIndex to be used in tiles[]
                if (aTileIndexNotId < 0) {
                    repaint();
                    return;
                }
                growCrystal(aTileIndexNotId);
                for (int attempt = 0; attempt < HOLE_ATTEMPTS; attempt++) {
                    repairHole();
                }
                advanceSearch();
                double dx = 0;
                double dy = 0;

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
                if (tileLockManager.isLocked(tiles[aTileIndexNotId].getId()) && !crystalMember[aTileIndexNotId]) {
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
                        if (!isSettled(tileIndexNotId)) {
                            biasTowardsBorder(eachTile);
                        }
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
                    else if (eachTile.xWorldSpace > (dimension.width - Tile.segmentScale / 2)) {
                        eachTile.xWorldSpace = dimension.width - Tile.segmentScale / 2;
                    }

                    // move away from upper/top edge
                    if (eachTile.yWorldSpace < (Tile.segmentScale / 2)) {
                        eachTile.yWorldSpace = (Tile.segmentScale / 2);
                    } // move away from lower/bottom edge
                    else if (eachTile.yWorldSpace > dimension.height - Tile.segmentScale / 2) {
                        eachTile.yWorldSpace = dimension.height - Tile.segmentScale / 2;
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

                    // crystal tiles keep their lock until repairHole unlocks them to make room
                    if (tileLockManager.isLocked(eachTile.getId()) && !crystalMember[tileIndexNotId]) {
                        tileLockManager.decreaseLock(eachTile.getId(), 1);
                    }
                }

                resolveTileOverlaps(tiles, numberOfTiles, tileLockManager, dimension, settledTiles());

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

    // ---- the square playing area -------------------------------------------------------------------------------------
    // Tiles are laid on a grid of Tile.getSegmentScale() cells, columns x rows (about square), centred on the panel.
    // Cell (0,0) is the top-left corner. World sides are numbered 0 top, 1 right, 2 bottom, 3 left; segment s of a tile
    // with orientation o faces world side (s + o) % 4.
    private static final int[] SIDE_DC = {0, 1, 0, -1};
    private static final int[] SIDE_DR = {-1, 0, 1, 0};

    int boardColumns() {
        return Math.max(1, (int) Math.ceil(Math.sqrt(Math.max(1, numberOfTiles))));
    }

    int boardRows() {
        return Math.max(1, (numberOfTiles + boardColumns() - 1) / boardColumns());
    }

    // The grid's origin is fixed when the crystal is seeded, so tiles already on it stay on it if the panel is
    // resized afterwards (NaN until then, when it follows the centre of the panel).
    private double anchoredLeft = Double.NaN;
    private double anchoredTop = Double.NaN;

    double boardLeft() {
        if (!Double.isNaN(anchoredLeft)) {
            return anchoredLeft;
        }
        return Math.max(0, (getSize().width - boardColumns() * Tile.getSegmentScale()) / 2.0);
    }

    double boardTop() {
        if (!Double.isNaN(anchoredTop)) {
            return anchoredTop;
        }
        return Math.max(0, (getSize().height - boardRows() * Tile.getSegmentScale()) / 2.0);
    }

    /** False until the panel has been laid out; growing a crystal before then would anchor the grid to a wrong size. */
    private boolean hasUsableSize() {
        Dimension size = getSize();
        return size.width >= 3 * Tile.getSegmentScale() && size.height >= 3 * Tile.getSegmentScale();
    }

    double cellCentreX(int column) {
        return boardLeft() + (column + 0.5) * Tile.getSegmentScale();
    }

    double cellCentreY(int row) {
        return boardTop() + (row + 0.5) * Tile.getSegmentScale();
    }

    int cellColumn(Tile tile) {
        return (int) Math.round((tile.xWorldSpace - boardLeft()) / Tile.getSegmentScale() - 0.5);
    }

    int cellRow(Tile tile) {
        return (int) Math.round((tile.yWorldSpace - boardTop()) / Tile.getSegmentScale() - 0.5);
    }

    boolean isCellInBoard(int column, int row) {
        return column >= 0 && column < boardColumns() && row >= 0 && row < boardRows();
    }

    /** Index of a tile sitting on (or overlapping) the spot, ignoring one tile, or -1. */
    private int tileNear(double x, double y, int ignoreIndex) {
        double limit = Tile.getSegmentScale() * 0.75;
        for (int index = 0; index < numberOfTiles; index++) {
            if (index != ignoreIndex && Math.abs(tiles[index].xWorldSpace - x) < limit && Math.abs(tiles[index].yWorldSpace - y) < limit) {
                return index;
            }
        }
        return -1;
    }

    /** Index of a crystal tile sitting exactly in a cell of the grid (locked or not), ignoring one tile, or -1. */
    private int tileOnCell(int column, int row, int ignoreIndex) {
        double x = cellCentreX(column);
        double y = cellCentreY(row);
        for (int index = 0; index < numberOfTiles; index++) {
            if (index != ignoreIndex && crystalMember[index] && Math.abs(tiles[index].xWorldSpace - x) <= 2 && Math.abs(tiles[index].yWorldSpace - y) <= 2) {
                return index;
            }
        }
        return -1;
    }

    /** Colour a tile shows on a world side when it has the given orientation. */
    private static int colourOnSide(Tile tile, int orientation, int side) {
        return tile.tileSegment[(((side - orientation) % 4) + 4) % 4].colour;
    }

    /**
     * A tile belongs in a cell when every side facing outside the playing area is the outside (grey) colour and no
     * side facing another cell is: so corner tiles only fit corners and edge tiles only the border.
     */
    boolean tileFitsCell(Tile tile, int orientation, int column, int row) {
        for (int side = 0; side < 4; side++) {
            boolean neighbourInside = isCellInBoard(column + SIDE_DC[side], row + SIDE_DR[side]);
            boolean grey = colourOnSide(tile, orientation, side) == 0;
            if (neighbourInside == grey) {
                return false;
            }
        }
        return true;
    }

    /**
     * Orientation that turns the tile's grey segments to face outwards, or -1 when it has no suitable turn: with one
     * grey segment it faces outwardSide, with two (a corner tile) they face outwardSide and the side after it.
     */
    private static int orientationFacingOutwards(Tile tile, int outwardSide) {
        int count = 0;
        for (int segment = 0; segment < 4; segment++) {
            count += tile.tileSegment[segment].colour == 0 ? 1 : 0;
        }
        for (int orientation = 0; orientation < 4; orientation++) {
            boolean greyOnOutward = colourOnSide(tile, orientation, outwardSide) == 0;
            boolean greyOnNext = colourOnSide(tile, orientation, (outwardSide + 1) % 4) == 0;
            if (count == 1 && greyOnOutward) {
                return orientation;
            }
            if (count == 2 && greyOnOutward && greyOnNext) {
                return orientation;
            }
        }
        return -1;
    }

    /** Lock a corner tile into the top-left cell so the crystal has a fixed anchor on the playing area's grid. */
    private void seedCrystal() {
        int seed = 0;
        int orientation = 0;
        for (int index = 0; index < numberOfTiles; index++) {
            int facing = tiles[index].getEdgeCount() == 2 ? orientationFacingOutwards(tiles[index], 3) : -1;   // greys left and top
            if (facing >= 0) {
                seed = index;
                orientation = facing;
                break;
            }
        }
        Tile seedTile = tiles[seed];
        if (tileLockManager.isLocked(seedTile.getId())) {
            return;
        }
        anchoredLeft = Double.NaN;
        anchoredTop = Double.NaN;
        anchoredLeft = boardLeft();
        anchoredTop = boardTop();
        seedTileIndex = seed;
        crystalMember[seed] = true;
        seedTile.setOrientation(orientation);
        seedTile.xWorldSpace = cellCentreX(0);
        seedTile.yWorldSpace = cellCentreY(0);
        seedTile.dx = 0;
        seedTile.dy = 0;
        // Tile.LOCKED is -1, which the lock decay would release at once: use the strongest level any tile gets
        tileLockManager.updateLock(seedTile.getId(), numberOfTiles * numberOfTiles);
        seedTile.lock = tileLockManager.getLockLevel(seedTile.getId());
    }

    /**
     * One growth step: from a random segment of a locked tile, try its matching wires (starting at a random one) until
     * one lets the tile at the wire's other end into the cell beside it. See {@link #placeAcrossWire}.
     */
    private void growCrystal(int primaryIndex) {
        Tile primary = tiles[primaryIndex];
        int primarySegment = (int) (Math.random() * 4);
        if (primary.tileSegment[primarySegment].colour == 0) {
            return;
        }
        Segment segment = primary.tileSegment[primarySegment];
        int wireCount = segment.nConnectedWires;
        int start = wireCount > 0 ? (int) (Math.random() * wireCount) : 0;
        for (int attempt = 0; attempt < wireCount; attempt++) {
            if (placeAcrossWire(primaryIndex, primarySegment, segment.connectedWires[(start + attempt) % wireCount])) {
                return;
            }
        }
    }

    /**
     * Put the tile at the far end of the wire into the cell beside the primary tile, turned so the two matching
     * segments touch. It is only placed when the cell is inside the playing area, holds no locked tile, the tile
     * suits the cell (grey edges face outside only) and every tile already beside the cell matches it edge to edge.
     *
     * @return true when the tile was placed and locked
     */
    private boolean placeAcrossWire(int primaryIndex, int primarySegment, Wire wire) {
        Tile primary = tiles[primaryIndex];
        int secondaryIndex;
        int secondarySegment;
        if (wire.fromTile == primaryIndex) {
            secondaryIndex = wire.toTile;
            secondarySegment = wire.toSegment;
        } else if (wire.toTile == primaryIndex) {
            secondaryIndex = wire.fromTile;
            secondarySegment = wire.fromSegment;
        } else {
            return false;
        }
        Tile secondary = tiles[secondaryIndex];
        // growth only brings in free tiles; moving tiles that are already in the crystal is the job of repairHole
        if (secondaryIndex == primaryIndex || crystalMember[secondaryIndex] || tileLockManager.isLocked(secondary.getId())) {
            return false;
        }

        int side = (primarySegment + primary.getOrientation()) % 4;                   // side of the primary that is touched
        int secondaryOrientation = (((side + 2 - secondarySegment) % 4) + 4) % 4;     // turns the secondary's segment to face back
        int column = cellColumn(primary) + SIDE_DC[side];
        int row = cellRow(primary) + SIDE_DR[side];
        if (!isCellInBoard(column, row) || !tileFitsCell(secondary, secondaryOrientation, column, row)) {
            return false;
        }
        // every tile already sitting beside the cell must match on the touching side, not just the one it grew from
        for (int neighbourSide = 0; neighbourSide < 4; neighbourSide++) {
            int neighbour = tileOnCell(column + SIDE_DC[neighbourSide], row + SIDE_DR[neighbourSide], secondaryIndex);
            if (neighbour >= 0 && colourOnSide(secondary, secondaryOrientation, neighbourSide)
                    != colourOnSide(tiles[neighbour], tiles[neighbour].getOrientation(), (neighbourSide + 2) % 4)) {
                return false;
            }
        }
        double x = cellCentreX(column);
        double y = cellCentreY(row);
        int occupant = tileNear(x, y, secondaryIndex);
        if (occupant >= 0 && (crystalMember[occupant] || tileLockManager.isLocked(tiles[occupant].getId()))) {
            return false;   // a crystal tile is already there; a wandering tile will be pushed out of the way
        }

        secondary.setOrientation(secondaryOrientation);
        secondary.xWorldSpace = x;
        secondary.yWorldSpace = y;
        secondary.dx = 0;
        secondary.dy = 0;
        primary.tileSegment[primarySegment].setActiveWire(wire);
        secondary.tileSegment[secondarySegment].setActiveWire(wire);

        // lock init to a value relative to the number of tiles already locked
        int remaining = getNumberOfTiles() - tileLockManager.getLocksInUse();
        tileLockManager.updateLock(secondary.getId(), remaining * remaining);
        // each locked neighbour makes the tile stay fixed for longer
        for (int neighbourSide = 0; neighbourSide < 4; neighbourSide++) {
            int neighbour = tileOnCell(column + SIDE_DC[neighbourSide], row + SIDE_DR[neighbourSide], secondaryIndex);
            if (neighbour >= 0 && neighbour != primaryIndex && tileLockManager.isLocked(tiles[neighbour].getId())) {
                tileLockManager.increaseLock(secondary.getId(), Tile.TEMPORARY_LOCKED);
            }
        }
        secondary.lock = tileLockManager.getLockLevel(secondary.getId());
        crystalMember[secondaryIndex] = true;
        return true;
    }

    // ---- repairing holes ---------------------------------------------------------------------------------------------
    // A hole is an empty cell of the playing area beside the crystal. Wire growth cannot fill one that is boxed in by
    // tiles whose edges no free tile matches. Then the neighbours that stand in the way are unlocked (taken out of the
    // crystal and set free) so that a tile can go in, and the vacated cells are refilled if possible. The swap is
    // kept only when the number of placed tiles does not go down; otherwise everything is put back.

    private static final int HOLE_ATTEMPTS = 30;           // per relax() iteration
    private static final int MAX_EVICTED_NEIGHBOURS = 2;   // neighbours that may be unlocked to let one tile in
    private static final int SWAPS_TRIED = 6;              // candidate tiles tried per hole
    private static final double CORNER_FOCUS = 0.15;      // share of hole searches that look at a corner cell
    private static final double PLATEAU_ACCEPT = 0.3;      // chance of keeping a swap that gains nothing (explores)
    private int seedTileIndex = -1;
    int holesFilled;      // tiles placed into a hole without disturbing anything
    int holesSwapped;     // swaps that unlocked tiles and ended with at least as many placed tiles

    /**
     * The tiles that have been placed into the crystal (by index), locked or not. A tile that merely happens to be
     * pushed up against the crystal is not one, so it is never counted as a neighbour whose edges must match.
     */
    private final boolean[] crystalMember = new boolean[CrystalGlobals.MAX_TILES];

    boolean isCrystalMember(int index) {
        return crystalMember[index];
    }

    /** True for a crystal tile that is sitting on its cell of the playing area. */
    boolean isSettled(int index) {
        Tile tile = tiles[index];
        int column = cellColumn(tile);
        int row = cellRow(tile);
        return crystalMember[index] && isCellInBoard(column, row)
                && Math.abs(tile.xWorldSpace - cellCentreX(column)) <= 2 && Math.abs(tile.yWorldSpace - cellCentreY(row)) <= 2;
    }

    private boolean[] settledTiles() {
        boolean[] settled = new boolean[numberOfTiles];
        for (int index = 0; index < numberOfTiles; index++) {
            settled[index] = isSettled(index);
        }
        return settled;
    }

    private static final class TileMemory {
        final int index;
        final boolean wasMember;
        final Tile tile;
        final double x;
        final double y;
        final int orientation;
        final int lockLevel;
        final boolean wasLocked;

        TileMemory(int index, Tile tile, boolean wasMember, LockManager locks) {
            this.index = index;
            this.wasMember = wasMember;
            this.tile = tile;
            this.x = tile.xWorldSpace;
            this.y = tile.yWorldSpace;
            this.orientation = tile.getOrientation();
            this.wasLocked = locks.isLocked(tile.getId());
            this.lockLevel = locks.getLockLevel(tile.getId());
        }
    }

    private final java.util.ArrayList<TileMemory> memory = new java.util.ArrayList<>();

    private void remember(int tileIndex) {
        for (TileMemory entry : memory) {
            if (entry.index == tileIndex) {
                return;
            }
        }
        memory.add(new TileMemory(tileIndex, tiles[tileIndex], crystalMember[tileIndex], tileLockManager));
    }

    private void rollback() {
        for (TileMemory entry : memory) {
            crystalMember[entry.index] = entry.wasMember;
            entry.tile.xWorldSpace = entry.x;
            entry.tile.yWorldSpace = entry.y;
            entry.tile.dx = 0;
            entry.tile.dy = 0;
            entry.tile.setOrientation(entry.orientation);
            if (entry.wasLocked) {
                tileLockManager.updateLock(entry.tile.getId(), entry.lockLevel);
                entry.tile.lock = entry.lockLevel;
            } else {
                tileLockManager.removeLock(entry.tile.getId());
                entry.tile.lock = Tile.UNLOCKED;
            }
        }
        memory.clear();
    }

    private void placeTile(int tileIndex, int orientation, int column, int row) {
        Tile tile = tiles[tileIndex];
        remember(tileIndex);
        crystalMember[tileIndex] = true;
        tile.setOrientation(orientation);
        tile.xWorldSpace = cellCentreX(column);
        tile.yWorldSpace = cellCentreY(row);
        tile.dx = 0;
        tile.dy = 0;
        int remaining = Math.max(2, getNumberOfTiles() - tileLockManager.getLocksInUse());
        tileLockManager.updateLock(tile.getId(), remaining * remaining);
        tile.lock = tileLockManager.getLockLevel(tile.getId());
    }

    /** Unlock a tile and put it down somewhere around the playing area, free to be used again. */
    private void evictTile(int tileIndex) {
        Tile tile = tiles[tileIndex];
        remember(tileIndex);
        crystalMember[tileIndex] = false;
        tileLockManager.removeLock(tile.getId());
        tile.lock = Tile.UNLOCKED;
        double scale = Tile.getSegmentScale();
        double left = boardLeft() - scale;
        double top = boardTop() - scale;
        double right = boardLeft() + (boardColumns() + 1) * scale;
        double bottom = boardTop() + (boardRows() + 1) * scale;
        double x = tile.xWorldSpace;
        double y = tile.yWorldSpace;
        for (int attempt = 0; attempt < 30; attempt++) {
            x = scale / 2 + Math.random() * Math.max(1, getSize().width - scale);
            y = scale / 2 + Math.random() * Math.max(1, getSize().height - scale);
            if (x < left || x > right || y < top || y > bottom) {
                break;
            }
        }
        tile.xWorldSpace = x;
        tile.yWorldSpace = y;
        tile.dx = 0;
        tile.dy = 0;
    }

    private static final class Candidate {
        final int tile;
        final int orientation;
        final int[] conflictingNeighbours;   // tile indices whose touching edge does not match

        Candidate(int tile, int orientation, int[] conflictingNeighbours) {
            this.tile = tile;
            this.orientation = orientation;
            this.conflictingNeighbours = conflictingNeighbours;
        }
    }

    /**
     * Every tile and orientation that suits the cell and mismatches at most maxConflicts of the tiles beside it.
     * Settled tiles are only offered when asked for (using one leaves its own cell empty). The seed tile is never
     * moved, and a candidate that would need it unlocked is not offered.
     */
    private java.util.ArrayList<Candidate> findCandidates(int column, int row, int maxConflicts, boolean includeSettled, boolean[] settled) {
        int[] neighbour = new int[4];
        for (int side = 0; side < 4; side++) {
            neighbour[side] = tileOnCell(column + SIDE_DC[side], row + SIDE_DR[side], -1);
        }
        java.util.ArrayList<Candidate> found = new java.util.ArrayList<>();
        for (int index = 0; index < numberOfTiles; index++) {
            if (index == seedTileIndex || (settled[index] && !includeSettled)) {
                continue;
            }
            boolean nextToCell = false;
            for (int side = 0; side < 4; side++) {
                nextToCell |= neighbour[side] == index;
            }
            if (nextToCell) {
                continue;
            }
            Tile tile = tiles[index];
            for (int orientation = 0; orientation < 4; orientation++) {
                if (!tileFitsCell(tile, orientation, column, row)) {
                    continue;
                }
                int[] conflicts = new int[4];
                int conflictCount = 0;
                boolean usable = true;
                for (int side = 0; side < 4 && usable; side++) {
                    if (neighbour[side] >= 0 && colourOnSide(tile, orientation, side)
                            != colourOnSide(tiles[neighbour[side]], tiles[neighbour[side]].getOrientation(), (side + 2) % 4)) {
                        usable = neighbour[side] != seedTileIndex && conflictCount < maxConflicts;
                        conflicts[conflictCount++] = neighbour[side];
                    }
                }
                if (usable) {
                    found.add(new Candidate(index, orientation, java.util.Arrays.copyOf(conflicts, conflictCount)));
                }
            }
        }
        return found;
    }

    // ---- local search --------------------------------------------------------------------------------------------------
    // The repair above never makes the board worse, so it gets stuck once every remaining hole is boxed in. To get out,
    // swaps that lose a tile or two are accepted with a probability that shrinks as a temperature falls (simulated
    // annealing) and the best board seen is remembered. A "run" is a greedy phase followed by a few cooling cycles; every
    // cycle starts from the best board of the run. When a run is over the crystal is cleared and a fresh run begins
    // (different random choices find different boards), keeping the best board of all runs. After the last run that
    // board is put back and annealing carries on from it.

    private static final int GREEDY_ITERATIONS = 300;       // plain repair first: it gets ~215 tiles in a hundred iterations
    private static final int CYCLE_ITERATIONS = 1500;       // one cooling cycle
    static int cyclesPerRun = 4;                            // package-private so a test can shorten the runs
    static int freshRuns = 2;                               // restarts from an empty crystal after the first run
    private static final double START_TEMPERATURE = 0.3;    // a swap losing one tile is then accepted 3.6% of the time, 0 when cold
    private static final double MIN_TEMPERATURE = 0.05;
    private static final int MAX_WORSENING = 2;             // most tiles one accepted swap may lose
    static boolean annealingEnabled = true;

    /** A board: where every tile is, how it is turned, whether it is in the crystal and its lock level (0 = unlocked). */
    private static final class Board {
        double[] x;
        double[] y;
        int[] orientation;
        int[] lock;
        boolean[] member;
        int placed;
        boolean valid;

        void copyFrom(Board other) {
            x = other.x.clone();
            y = other.y.clone();
            orientation = other.orientation.clone();
            lock = other.lock.clone();
            member = other.member.clone();
            placed = other.placed;
            valid = other.valid;
        }
    }

    private int searchIterations;       // since the start of the current run
    private int runsStarted = 1;
    private double temperature;
    int swapsWorsening;                 // accepted swaps that lost tiles
    private int runBestPlaced;          // most tiles placed so far in this run
    private final Board runBest = new Board();       // valid once the run's best board had to be saved (see saveBest)
    private final Board globalBest = new Board();    // best board of the runs that are over

    int runsStarted() {
        return runsStarted;
    }

    int placedCount() {
        int placed = 0;
        for (int index = 0; index < numberOfTiles; index++) {
            placed += isSettled(index) ? 1 : 0;
        }
        return placed;
    }

    /** Most tiles placed at any time in any run. */
    int bestPlaced() {
        return Math.max(runBestPlaced, globalBest.valid ? globalBest.placed : 0);
    }

    /**
     * Copy the board into a snapshot. With applyMemory the tiles the move being decided touched are put back to what they
     * were (the undo memory holds exactly that), which gives the board as it was before the move.
     */
    private void capture(Board board, int placed, boolean applyMemory) {
        if (board.x == null || board.x.length != numberOfTiles) {
            board.x = new double[numberOfTiles];
            board.y = new double[numberOfTiles];
            board.orientation = new int[numberOfTiles];
            board.lock = new int[numberOfTiles];
            board.member = new boolean[numberOfTiles];
        }
        for (int index = 0; index < numberOfTiles; index++) {
            Tile tile = tiles[index];
            board.x[index] = tile.xWorldSpace;
            board.y[index] = tile.yWorldSpace;
            board.orientation[index] = tile.getOrientation();
            board.member[index] = crystalMember[index];
            board.lock[index] = tileLockManager.isLocked(tile.getId()) ? Math.max(1, tileLockManager.getLockLevel(tile.getId())) : 0;
        }
        if (applyMemory) {
            for (TileMemory entry : memory) {
                board.x[entry.index] = entry.x;
                board.y[entry.index] = entry.y;
                board.orientation[entry.index] = entry.orientation;
                board.member[entry.index] = entry.wasMember;
                board.lock[entry.index] = entry.wasLocked ? Math.max(1, entry.lockLevel) : 0;
            }
        }
        board.placed = placed;
        board.valid = true;
    }

    /** The run's best board is about to be left by a worsening move: keep it. */
    private void saveBest(int placedBefore) {
        capture(runBest, placedBefore, true);
        runBestPlaced = placedBefore;
        recordIfBest(runBest);
    }

    private void apply(Board board) {
        for (int index = 0; index < numberOfTiles; index++) {
            Tile tile = tiles[index];
            if (board.member[index]) {
                crystalMember[index] = true;
                tile.xWorldSpace = board.x[index];
                tile.yWorldSpace = board.y[index];
                tile.dx = 0;
                tile.dy = 0;
                tile.setOrientation(board.orientation[index]);
                tileLockManager.updateLock(tile.getId(), Math.max(1, board.lock[index]));
                tile.lock = tileLockManager.getLockLevel(tile.getId());
            } else if (crystalMember[index]) {
                evictTile(index);
            }
        }
        memory.clear();
        // free tiles may be wandering over cells that are crystal again: push them out now rather than on the next step
        resolveTileOverlaps(tiles, numberOfTiles, tileLockManager, getSize(), settledTiles());
    }

    /** Put the best board of this run back, if the current one is worse. */
    private void restoreRunBest() {
        if (runBest.valid && placedCount() < runBest.placed) {
            apply(runBest);
        }
    }

    /** Put the best board of all runs so far back, if the current one is worse. */
    void restoreBest() {
        Board best = globalBest.valid && globalBest.placed >= (runBest.valid ? runBest.placed : 0) ? globalBest : runBest;
        if (best.valid && placedCount() < best.placed) {
            apply(best);
        }
    }

    /** Remember the best board of the run that is ending if it beats the best of the earlier runs. */
    private void foldRunIntoGlobal() {
        int placed = placedCount();
        if (placed >= runBestPlaced && (!globalBest.valid || placed > globalBest.placed)) {
            capture(globalBest, placed, false);   // the run ended on its best board
        } else if (runBest.valid && (!globalBest.valid || runBest.placed > globalBest.placed)) {
            globalBest.copyFrom(runBest);
        }
        recordIfBest(globalBest);
    }

    /** The run is over: keep its best board if it is the best so far, then clear the crystal for a fresh run. */
    private void startFreshRun() {
        foldRunIntoGlobal();
        runBest.valid = false;
        runBestPlaced = 0;
        for (int index = 0; index < numberOfTiles; index++) {
            if (crystalMember[index]) {
                evictTile(index);
            }
        }
        memory.clear();
        seedTileIndex = -1;
        searchIterations = 0;
        runsStarted++;
    }

    /** The last run is over: put the best board of all runs back and let the annealing carry on from it. */
    private void continueFromBest() {
        foldRunIntoGlobal();
        if (globalBest.valid) {
            if (placedCount() < globalBest.placed) {
                apply(globalBest);
            }
            runBest.copyFrom(globalBest);
            runBestPlaced = globalBest.placed;
        }
    }

    /** Called once per iteration after the repairs: note a new best board and move the temperature along its schedule. */
    private void advanceSearch() {
        searchIterations++;
        int placed = placedCount();
        if (placed > runBestPlaced) {
            runBestPlaced = placed;
            runBest.valid = false;   // the current board is the best; it is only copied when a worsening move leaves it
            if (placed > savedPlaced() && placed >= minimumRecorded()
                    && (placed == numberOfTiles || searchIterations - lastRecordIteration >= RECORD_INTERVAL)) {
                capture(scratch, placed, false);
                recordIfBest(scratch);
            }
        }
        if (!annealingEnabled || searchIterations <= GREEDY_ITERATIONS) {
            temperature = 0;
            return;
        }
        if (runsStarted <= freshRuns && searchIterations >= (GREEDY_ITERATIONS + cyclesPerRun * CYCLE_ITERATIONS)) {
            startFreshRun();
            temperature = 0;
            return;
        }
        if (runsStarted == freshRuns + 1 && searchIterations == (GREEDY_ITERATIONS + cyclesPerRun * CYCLE_ITERATIONS) && freshRuns > 0) {
            continueFromBest();
        }
        int step = (searchIterations - GREEDY_ITERATIONS) % CYCLE_ITERATIONS;
        if (step == 0) {
            restoreRunBest();
        }
        temperature = START_TEMPERATURE * (1 - step / (double) CYCLE_ITERATIONS);
    }

    // ---- keeping the best boards ---------------------------------------------------------------------------------------
    // Every board that beats the best one on file is written to a text file (see BoardRecord), so that a good result
    // - and one day a complete board, i.e. a solution - is never lost when the window is closed. Only strictly better
    // boards are written, so a session leaves at most a few dozen small files. A file can be loaded to seed a later run.

    /** Directory the best boards are written to (-Dcrystals.boards=...). */
    static java.nio.file.Path boardDirectory = java.nio.file.Paths.get(System.getProperty("crystals.boards", "best-boards"));
    static boolean recordingEnabled = true;
    private static final double MIN_RECORDED_FRACTION = 0.85;   // boards below this share of the tiles are not worth a file
    private static final int RECORD_INTERVAL = 100;             // iterations between files for a board still being improved
    private int savedPlaced = -1;                                // most tiles on any file in the directory (read once)
    private int lastRecordIteration;
    java.nio.file.Path lastRecordFile;
    private final Board scratch = new Board();
    private java.nio.file.Path pendingImport = System.getProperty("crystals.import") == null
            ? null : java.nio.file.Paths.get(System.getProperty("crystals.import"));

    private int savedPlaced() {
        if (savedPlaced < 0) {
            savedPlaced = BoardRecord.bestSaved(boardDirectory);
        }
        return savedPlaced;
    }

    private int minimumRecorded() {
        return (int) Math.ceil(numberOfTiles * MIN_RECORDED_FRACTION);
    }

    private BoardRecord recordOf(Board board) {
        java.util.ArrayList<BoardRecord.Entry> entries = new java.util.ArrayList<>();
        for (int index = 0; index < numberOfTiles; index++) {
            if (!board.member[index]) {
                continue;
            }
            Tile tile = tiles[index];
            int column = (int) Math.round((board.x[index] - boardLeft()) / Tile.getSegmentScale() - 0.5);
            int row = (int) Math.round((board.y[index] - boardTop()) / Tile.getSegmentScale() - 0.5);
            int[] colours = new int[4];
            for (int side = 0; side < 4; side++) {
                colours[side] = colourOnSide(tile, board.orientation[index], side);
            }
            entries.add(new BoardRecord.Entry(tile.getId(), column, row, board.orientation[index], colours));
        }
        entries.sort((a, b) -> a.row != b.row ? Integer.compare(a.row, b.row) : Integer.compare(a.column, b.column));
        return new BoardRecord(numberOfTiles, boardColumns(), boardRows(), entries);
    }

    /** Write the board to a file when it has more tiles than any board on file. */
    private void recordIfBest(Board board) {
        if (!recordingEnabled || !board.valid || board.placed <= savedPlaced() || board.placed < minimumRecorded()) {
            return;
        }
        try {
            lastRecordFile = recordOf(board).save(boardDirectory);
            savedPlaced = board.placed;
            lastRecordIteration = searchIterations;
            System.out.printf("Saved %d of %d tiles to %s%n", board.placed, numberOfTiles, lastRecordFile.toAbsolutePath());
        } catch (java.io.IOException | RuntimeException problem) {
            System.err.println("Could not save the board: " + problem);
            lastRecordIteration = searchIterations;   // do not retry every iteration
        }
    }

    /**
     * Load a board saved earlier: its tiles are put on their cells and locked, and the search carries on from it (it also
     * counts as the best board of the run). Needs the panel to have its size, as the cells are placed on the grid.
     */
    void importBoard(java.nio.file.Path file) throws java.io.IOException {
        BoardRecord record = BoardRecord.read(file);
        if (!hasUsableSize()) {
            throw new IllegalStateException("the panel has no size yet");
        }
        if (record.tiles != numberOfTiles || record.columns != boardColumns() || record.rows != boardRows()) {
            throw new IllegalArgumentException("the board file is for " + record.tiles + " tiles on " + record.columns + "x"
                    + record.rows + ", this panel has " + numberOfTiles + " on " + boardColumns() + "x" + boardRows());
        }
        anchoredLeft = Double.NaN;
        anchoredTop = Double.NaN;
        anchoredLeft = boardLeft();
        anchoredTop = boardTop();
        capture(scratch, 0, false);
        java.util.Arrays.fill(scratch.member, false);
        java.util.Arrays.fill(scratch.lock, 0);
        seedTileIndex = -1;
        for (BoardRecord.Entry entry : record.entries) {
            int index = getTileIndexFromId(entry.tileId);
            if (index < 0 || scratch.member[index]) {
                throw new IllegalArgumentException("tile " + entry.tileId + " is unknown or listed twice");
            }
            scratch.member[index] = true;
            scratch.x[index] = cellCentreX(entry.column);
            scratch.y[index] = cellCentreY(entry.row);
            scratch.orientation[index] = entry.orientation;
            scratch.lock[index] = numberOfTiles;
            if (entry.column == 0 && entry.row == 0) {
                seedTileIndex = index;
            }
        }
        scratch.placed = record.placed();
        apply(scratch);
        runBest.copyFrom(scratch);
        runBestPlaced = scratch.placed;
        savedPlaced = Math.max(savedPlaced(), scratch.placed);   // already on file
    }

    /** Chance that a hole search follows the fill order (border ring, then rows) rather than picking a cell at random. */
    private static final double ORDERED_PICK = 0.75;

    private boolean isHole(boolean[] occupied, int column, int row) {
        if (!isCellInBoard(column, row) || occupied[row * boardColumns() + column]) {
            return false;
        }
        for (int side = 0; side < 4; side++) {
            int neighbourColumn = column + SIDE_DC[side];
            int neighbourRow = row + SIDE_DR[side];
            if (isCellInBoard(neighbourColumn, neighbourRow) && occupied[neighbourRow * boardColumns() + neighbourColumn]) {
                return true;
            }
        }
        return false;
    }

    /**
     * An empty cell of the playing area with at least one crystal tile beside it, or null. Mostly the first one in fill
     * order (border ring clockwise from the top-left corner, then the interior row by row) so holes are dealt with from the
     * corner outwards and early ones are repaired before more tiles pile up around them; sometimes a corner or a random
     * cell so that one hard hole cannot take every attempt.
     */
    private int[] pickHole() {
        int columns = boardColumns();
        int rows = boardRows();
        boolean[] occupied = new boolean[columns * rows];
        for (int index = 0; index < numberOfTiles; index++) {
            if (isSettled(index)) {
                occupied[cellRow(tiles[index]) * columns + cellColumn(tiles[index])] = true;
            }
        }

        double choice = Math.random();
        if (choice < CORNER_FOCUS) {
            // the four corner cells are the hardest to get right, so look at them more often
            int column = Math.random() < 0.5 ? 0 : columns - 1;
            int row = Math.random() < 0.5 ? 0 : rows - 1;
            if (isHole(occupied, column, row)) {
                return new int[] {column, row};
            }
        }
        if (choice < CORNER_FOCUS + ORDERED_PICK) {
            for (int column = 0; column < columns; column++) {
                if (isHole(occupied, column, 0)) {
                    return new int[] {column, 0};
                }
            }
            for (int row = 1; row < rows; row++) {
                if (isHole(occupied, columns - 1, row)) {
                    return new int[] {columns - 1, row};
                }
            }
            for (int column = columns - 2; column >= 0; column--) {
                if (isHole(occupied, column, rows - 1)) {
                    return new int[] {column, rows - 1};
                }
            }
            for (int row = rows - 2; row >= 1; row--) {
                if (isHole(occupied, 0, row)) {
                    return new int[] {0, row};
                }
            }
            for (int row = 1; row < rows - 1; row++) {
                for (int column = 1; column < columns - 1; column++) {
                    if (isHole(occupied, column, row)) {
                        return new int[] {column, row};
                    }
                }
            }
        }
        for (int attempt = 0; attempt < 40; attempt++) {
            int column = (int) (Math.random() * columns);
            int row = (int) (Math.random() * rows);
            if (isHole(occupied, column, row)) {
                return new int[] {column, row};
            }
        }
        return null;
    }

    private static <T> T random(java.util.List<T> list) {
        return list.get((int) (Math.random() * list.size()));
    }

    /**
     * Try to fill one hole: with a free tile that matches every neighbour, or else by unlocking the neighbour(s) (or
     * moving a tile out of another cell) that stand in the way. Returns true when the board changed.
     */
    private boolean repairHole() {
        int[] hole = pickHole();
        if (hole == null) {
            return false;
        }
        int column = hole[0];
        int row = hole[1];
        boolean[] settled = settledTiles();

        java.util.ArrayList<Candidate> exact = findCandidates(column, row, 0, false, settled);
        if (!exact.isEmpty()) {
            Candidate chosen = random(exact);
            memory.clear();
            placeTile(chosen.tile, chosen.orientation, column, row);
            memory.clear();
            holesFilled++;
            return true;
        }

        java.util.ArrayList<Candidate> swaps = findCandidates(column, row, MAX_EVICTED_NEIGHBOURS, true, settled);
        java.util.Collections.shuffle(swaps);
        // prefer the swaps that disturb the fewest tiles
        swaps.sort((a, b) -> Integer.compare(a.conflictingNeighbours.length, b.conflictingNeighbours.length));
        for (int attempt = 0; attempt < Math.min(SWAPS_TRIED, swaps.size()); attempt++) {
            Candidate chosen = swaps.get(attempt);
            memory.clear();

            java.util.ArrayList<int[]> vacated = new java.util.ArrayList<>();
            if (settled[chosen.tile]) {
                vacated.add(new int[] {cellColumn(tiles[chosen.tile]), cellRow(tiles[chosen.tile])});
            }
            for (int neighbourIndex : chosen.conflictingNeighbours) {
                vacated.add(new int[] {cellColumn(tiles[neighbourIndex]), cellRow(tiles[neighbourIndex])});
                evictTile(neighbourIndex);
            }
            placeTile(chosen.tile, chosen.orientation, column, row);

            int gained = 1 - vacated.size();
            for (int[] cell : vacated) {
                java.util.ArrayList<Candidate> refill = findCandidates(cell[0], cell[1], 0, false, settledTiles());
                if (!refill.isEmpty()) {
                    Candidate again = random(refill);
                    placeTile(again.tile, again.orientation, cell[0], cell[1]);
                    gained++;
                }
            }
            boolean accept = gained > 0 || (gained == 0 && Math.random() < PLATEAU_ACCEPT);
            if (!accept && gained < 0 && gained >= -MAX_WORSENING && temperature > MIN_TEMPERATURE
                    && Math.random() < Math.exp(gained / temperature)) {
                // worse for now, to get out of a dead end: first make sure the best board so far can be put back
                int placedBefore = placedCount() - gained;
                if (placedBefore > runBestPlaced || (placedBefore == runBestPlaced && !runBest.valid)) {
                    saveBest(placedBefore);
                }
                accept = true;
                swapsWorsening++;
            }
            if (accept) {
                memory.clear();
                holesSwapped++;
                return true;
            }
            rollback();
        }
        return false;
    }

    private static final double BORDER_PULL = 3;        // pixels per iteration
    private static final double BORDER_BAND = 2;         // tiles stop being pulled this many tile widths from their target

    /**
     * Unlocked corner tiles drift towards a corner of the playing area and edge tiles towards a side, turned so their
     * grey edges face outwards. Corners and sides are shared out by tile id so they do not all crowd one place, and a
     * tile is only pulled until it is within a band near its target so that it never fights the overlap resolver.
     * Interior tiles are left alone.
     */
    private void biasTowardsBorder(Tile tile) {
        int greyEdges = tile.getEdgeCount();
        if (greyEdges < 1) {
            return;
        }
        double scale = Tile.getSegmentScale();
        double minX = cellCentreX(0);
        double maxX = cellCentreX(boardColumns() - 1);
        double minY = cellCentreY(0);
        double maxY = cellCentreY(boardRows() - 1);
        int share = tile.getId() % 4;
        double targetX;
        double targetY;
        int outwardSide;
        if (greyEdges >= 2) {
            // 0 top left, 1 top right, 2 bottom right, 3 bottom left
            targetX = share == 0 || share == 3 ? minX : maxX;
            targetY = share == 0 || share == 1 ? minY : maxY;
            outwardSide = share == 0 ? 3 : share == 1 ? 0 : share == 2 ? 1 : 2;   // first of the two outward sides, clockwise
        } else {
            // 0 top, 1 right, 2 bottom, 3 left: stay level with where the tile is, on the chosen side
            outwardSide = share;
            targetX = Math.max(minX, Math.min(maxX, tile.xWorldSpace));
            targetY = Math.max(minY, Math.min(maxY, tile.yWorldSpace));
            if (share == 0) {
                targetY = minY;
            } else if (share == 2) {
                targetY = maxY;
            } else if (share == 3) {
                targetX = minX;
            } else {
                targetX = maxX;
            }
        }
        int orientation = orientationFacingOutwards(tile, outwardSide);
        if (orientation >= 0) {
            tile.setOrientation(orientation);
        }
        double vx = targetX - tile.xWorldSpace;
        double vy = targetY - tile.yWorldSpace;
        double distance = Math.hypot(vx, vy);
        if (distance > BORDER_BAND * scale) {
            double step = Math.min(distance, BORDER_PULL);
            tile.dx += vx / distance * step;
            tile.dy += vy / distance * step;
        }
    }

    private static final int MAX_OVERLAP_PASSES = 60;
    private static final double OVERLAP_EPSILON = 0.001;

    static void resolveTileOverlaps(Tile[] tiles, int numberOfTiles, LockManager tileLockManager, Dimension dimension) {
        resolveTileOverlaps(tiles, numberOfTiles, tileLockManager, dimension, null);
    }

    /**
     * @param settled tiles sitting on a cell of the playing area (may be null); they are treated like locked tiles so
     *                a wandering tile is pushed away from them rather than the other way round
     */
    static void resolveTileOverlaps(Tile[] tiles, int numberOfTiles, LockManager tileLockManager, Dimension dimension, boolean[] settled) {
        double minimumSeparation = Tile.getSegmentScale();
        if (minimumSeparation <= 0) {
            return;
        }

        // lock state does not change while overlaps are resolved, and looking it up is a linear scan: read it once
        boolean[] locked = new boolean[numberOfTiles];
        int[] lockLevel = new int[numberOfTiles];
        for (int tileIndex = 0; tileIndex < numberOfTiles; tileIndex++) {
            locked[tileIndex] = tileLockManager.isLocked(tiles[tileIndex].getId(), Tile.TEMPORARY_LOCKED);
            lockLevel[tileIndex] = tileLockManager.getLockLevel(tiles[tileIndex].getId());
            if (settled != null && settled[tileIndex]) {
                locked[tileIndex] = true;
                lockLevel[tileIndex] = Math.max(lockLevel[tileIndex], 1);
            }
        }

        // Pushing one tile off another can push it onto a third, so a dense clump needs many passes to settle;
        // stop as soon as a whole pass finds nothing to separate.
        for (int pass = 0; pass < MAX_OVERLAP_PASSES; pass++) {
            boolean separated = false;
            for (int firstIndex = 0; firstIndex < numberOfTiles; firstIndex++) {
                Tile firstTile = tiles[firstIndex];
                boolean firstLocked = locked[firstIndex];

                for (int secondIndex = firstIndex + 1; secondIndex < numberOfTiles; secondIndex++) {
                    Tile secondTile = tiles[secondIndex];
                    double deltaX = secondTile.xWorldSpace - firstTile.xWorldSpace;
                    double deltaY = secondTile.yWorldSpace - firstTile.yWorldSpace;
                    double overlapX = minimumSeparation - Math.abs(deltaX);
                    double overlapY = minimumSeparation - Math.abs(deltaY);
                    if (overlapX <= OVERLAP_EPSILON || overlapY <= OVERLAP_EPSILON) {
                        continue;
                    }
                    boolean secondLocked = locked[secondIndex];
                    separated = true;

                    // An unlocked tile always yields to a locked one. When both are locked (e.g. a wire has just
                    // pulled one tile onto a neighbour) the more weakly locked tile yields so tiles never overlap.
                    boolean firstCanMove = !firstLocked;
                    boolean secondCanMove = !secondLocked;
                    if (firstLocked && secondLocked) {
                        int firstLevel = lockLevel[firstIndex];
                        int secondLevel = lockLevel[secondIndex];
                        firstCanMove = firstLevel <= secondLevel;
                        secondCanMove = secondLevel <= firstLevel;
                    }

                    double direction = deltaX == 0 && deltaY == 0
                            ? (((firstIndex + secondIndex) & 1) == 0 ? 1 : -1)
                            : 0;
                    if (overlapX <= overlapY) {
                        if (direction == 0) {
                            direction = deltaX == 0 ? (deltaY > 0 ? 1 : -1) : Math.signum(deltaX);
                        }
                        if (firstCanMove && secondCanMove) {
                            firstTile.xWorldSpace -= direction * overlapX / 2;
                            secondTile.xWorldSpace += direction * overlapX / 2;
                        } else if (firstCanMove) {
                            firstTile.xWorldSpace -= direction * overlapX;
                        } else {
                            secondTile.xWorldSpace += direction * overlapX;
                        }
                    } else {
                        if (direction == 0) {
                            direction = deltaY == 0 ? (deltaX > 0 ? 1 : -1) : Math.signum(deltaY);
                        }
                        if (firstCanMove && secondCanMove) {
                            firstTile.yWorldSpace -= direction * overlapY / 2;
                            secondTile.yWorldSpace += direction * overlapY / 2;
                        } else if (firstCanMove) {
                            firstTile.yWorldSpace -= direction * overlapY;
                        } else {
                            secondTile.yWorldSpace += direction * overlapY;
                        }
                    }
                }
            }

            double minimumCenter = minimumSeparation / 2;
            double maximumX = Math.max(minimumCenter, dimension.width - minimumCenter);
            double maximumY = Math.max(minimumCenter, dimension.height - minimumCenter);
            // keep every tile on the panel: a locked tile may have been pushed out when it lost an overlap
            for (int tileIndex = 0; tileIndex < numberOfTiles; tileIndex++) {
                tiles[tileIndex].xWorldSpace = Math.max(minimumCenter, Math.min(maximumX, tiles[tileIndex].xWorldSpace));
                tiles[tileIndex].yWorldSpace = Math.max(minimumCenter, Math.min(maximumY, tiles[tileIndex].yWorldSpace));
            }
            if (!separated) {
                return;
            }
        }
        relocateOverlappingTiles(tiles, numberOfTiles, tileLockManager, dimension, settled);
    }

    private static boolean overlaps(Tile first, Tile second, double separation) {
        return separation - Math.abs(second.xWorldSpace - first.xWorldSpace) > OVERLAP_EPSILON
                && separation - Math.abs(second.yWorldSpace - first.yWorldSpace) > OVERLAP_EPSILON;
    }

    private static boolean isFreeAt(Tile[] tiles, int numberOfTiles, int skipIndex, double x, double y, double separation) {
        for (int index = 0; index < numberOfTiles; index++) {
            if (index != skipIndex
                    && separation - Math.abs(tiles[index].xWorldSpace - x) > OVERLAP_EPSILON
                    && separation - Math.abs(tiles[index].yWorldSpace - y) > OVERLAP_EPSILON) {
                return false;
            }
        }
        return true;
    }

    /**
     * Last resort for tiles that pushing could not separate (a tile trapped between others in a dense clump): the weaker
     * tile of each overlapping pair is moved to the nearest empty spot on the panel, searching outwards in rings.
     */
    private static void relocateOverlappingTiles(Tile[] tiles, int numberOfTiles, LockManager tileLockManager, Dimension dimension, boolean[] settled) {
        double separation = Tile.getSegmentScale();
        double step = separation / 2;
        double minimumCenter = separation / 2;
        double maximumX = Math.max(minimumCenter, dimension.width - minimumCenter);
        double maximumY = Math.max(minimumCenter, dimension.height - minimumCenter);
        int maximumRing = (int) (Math.max(dimension.width, dimension.height) / step) + 1;

        for (int sweep = 0; sweep < numberOfTiles; sweep++) {
            boolean moved = false;
            for (int firstIndex = 0; firstIndex < numberOfTiles; firstIndex++) {
                int otherIndex = -1;
                for (int index = 0; index < numberOfTiles; index++) {
                    if (index != firstIndex && overlaps(tiles[firstIndex], tiles[index], separation)) {
                        otherIndex = index;
                        break;
                    }
                }
                if (otherIndex < 0) {
                    continue;
                }
                int moverIndex = tileLockManager.getLockLevel(tiles[otherIndex].getId())
                        < tileLockManager.getLockLevel(tiles[firstIndex].getId()) ? otherIndex : firstIndex;
                if (settled != null && settled[moverIndex]) {
                    moverIndex = moverIndex == firstIndex ? otherIndex : firstIndex;   // crystal tiles stay on the grid
                    if (settled[moverIndex]) {
                        continue;
                    }
                }
                Tile mover = tiles[moverIndex];

                double bestX = 0;
                double bestY = 0;
                boolean found = false;
                for (int ring = 1; ring <= maximumRing && !found; ring++) {
                    double bestDistance = Double.MAX_VALUE;
                    for (int ox = -ring; ox <= ring; ox++) {
                        for (int oy = -ring; oy <= ring; oy++) {
                            if (Math.max(Math.abs(ox), Math.abs(oy)) != ring) {
                                continue;
                            }
                            double x = mover.xWorldSpace + ox * step;
                            double y = mover.yWorldSpace + oy * step;
                            if (x < minimumCenter || x > maximumX || y < minimumCenter || y > maximumY) {
                                continue;
                            }
                            double distance = ox * ox + oy * oy;
                            if (distance < bestDistance && isFreeAt(tiles, numberOfTiles, moverIndex, x, y, separation)) {
                                bestDistance = distance;
                                bestX = x;
                                bestY = y;
                                found = true;
                            }
                        }
                    }
                }
                if (found) {
                    mover.xWorldSpace = bestX;
                    mover.yWorldSpace = bestY;
                    moved = true;
                }
            }
            if (!moved) {
                return;
            }
        }
    }


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
            Crystal.panel.aFrame.drawFrame(offScreenGraphics, palate, wireframeTileModeCheckbox);          // display frame based implementation
            
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
        if (showFloatingTiles && numberOfTiles > 0) {
            int placedTiles = 0;
            for (int tileIndex = 0; tileIndex < numberOfTiles; tileIndex++) {
                placedTiles += crystalMember[tileIndex] ? 1 : 0;
            }
            offScreenGraphics.drawString("Placed: ", dimension.width - 140, dimension.height - 40);
            offScreenGraphics.drawString(placedTiles + " (best " + Math.max(bestPlaced(), placedTiles) + ")", dimension.width - 90, dimension.height - 40);
        }

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
