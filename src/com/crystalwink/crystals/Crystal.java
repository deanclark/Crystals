package com.crystalwink.crystals;
/**
 *
 * @(#)Crystal.java	2.18 06/06/22
 * 
 * Eternity 
 * @author Dean Clark
 * @date   June 2015
 * 
 * @(#)Graph.java
 * 
 * TODO
 * Wire length auto adjustment - done using relax()
 * Option to hide lines - done
 * Some tiles indicate more than 4 active wires - to be investigated.
 * orientation
 * move origin of Wire to the centre of the tile to simplify rotation of tile and avoid need to copy of segment structure
 * Export to file feature, also create a file compatible with tool used to view tiles (tile id’s will not match though!).
 * Constrain board dimensions
 * Shift all locked cells to within board area (consider locked hint tiles)
 * Process edge pieces first
 * Determine every possible quadTile and export to file.
 * Upload best solutions to db
 * Show iterations per minute
 * Disable graphics option (except iterations per minute)
 * Hint tile can be in one of 4 possible locations depending on orientation of the board.  Adding the hint restriction initially may not be efficient   
 *
 *
 *********************************
 * Controlling mode of operation *
 *********************************
 * CrystalGlobals.showFloatingTiles = false
 * CrystalGlobals.suppressGraphics = true
 * CrystalGlobals.ModeOfOperation = MODE_FRAME_SINGLE_PATH;
 * FrameStructure.asPoly = true
 * FrameStructure.LINE_LENGTH = 6; // 40
 * FrameStructure.CELL_SCALE = 6; // 40
 * FrameStructure.selectFromAllQualifyingCells = true;  // first match or Random selection from a list of all qualifying tiles.
 * FrameStructure.singlePathMode = false;
 * FrameStructure. = ORIGIN_FROM_CORNER;s
 * FrameStructure.startHistoryIndex = 0; // 0=Hint Tile, 1 will start in the corner
 */
//import java.util.*;
import com.crystalwink.crystalscommon.CrystalGlobals;
import com.crystalwink.crystalscommon.Trace;
import com.crystalwink.crystalscommon.Tile.Tile;
import com.crystalwink.crystals.FrameStructure.FrameStructure;
import com.crystalwink.crystals.GraphPanel.GraphPanel;

import java.awt.*;
import java.applet.Applet;
import java.awt.event.*;
import java.net.URL;
import java.util.StringTokenizer;
import java.io.*; //RandomAccessFile;


/**
 *
 * @author deancl
 */
public class Crystal extends Applet implements ActionListener, ItemListener
{

    /**
     *
     */
    public static GraphPanel panel;
    Panel controlPanel;

    Button button1;
    Button button2;

    Checkbox checkBox1;            // random
    Checkbox checkBox2;            // activeWiresCheckbox
    Checkbox showTileIdCheckbox;   // showTileIdCheckbox
    Checkbox drawRotation;         // drawRotation
    Checkbox wireframeTile;        // wireframeTile
    Checkbox showWires;            // showWires
    Checkbox showFloatingTilesCheckbox; // showFloatingTiles
    

    int wireLength = 10;
    final int MAX_SEGMENTS = 4;

    /**
     * commonInit
     */
    public void commonInit() {

        panel = new GraphPanel(this);
        add("Center", panel);
        controlPanel = new Panel();
        add("South", controlPanel);

        //Checkbox stress = new Checkbox("Stress");
        showTileIdCheckbox = new Checkbox("Id's");
        drawRotation = new Checkbox("Rotate");
        wireframeTile = new Checkbox("Wireframe");
        showWires = new Checkbox("Wires");
        showFloatingTilesCheckbox = new Checkbox("Floating");

        if(CrystalGlobals.ModeOfOperation == CrystalGlobals.MODE_ELASTIC)
        {
            button1 = new Button("Scramble");
            button2 = new Button("Shake");
            checkBox1 = new Checkbox("Random");
            checkBox2 = new Checkbox("Active");
        }
        else if(CrystalGlobals.ModeOfOperation == CrystalGlobals.MODE_FRAME_SINGLE_PATH)
        {
            button1 = new Button("  Show  ");
            button2 = new Button("Infill "); //
            checkBox1 = new Checkbox("Random"); // randomFillMode
            checkBox2 = new Checkbox("Large"); // Large or activeWiresCheckbox
        }

        controlPanel.add(button1);
        button1.addActionListener(this);
        controlPanel.add(button2);
        button2.addActionListener(this);

        //controlPanel.add(stress); stress.addItemListener(this);
        controlPanel.add(checkBox1);  // Random
            checkBox1.addItemListener(this);
            checkBox1.setState(panel.randomModeCheckbox);
        
        controlPanel.add(checkBox2);  // Active/Large
            checkBox2.addItemListener(this);
            checkBox2.setState(panel.activeWiresCheckbox);
        
        controlPanel.add(showTileIdCheckbox);
            showTileIdCheckbox.addItemListener(this);
            showTileIdCheckbox.setState(panel.showTileIdCheckbox);
        
        controlPanel.add(drawRotation);
            drawRotation.addItemListener(this);
            drawRotation.setState(panel.drawRotationModeCheckbox);

        controlPanel.add(wireframeTile);
            wireframeTile.addItemListener(this);
            wireframeTile.setState(panel.wireframeTileModeCheckbox);
        
        controlPanel.add(showWires);
            showWires.addItemListener(this);
            showWires.setState(panel.showWiresModeCheckbox);
        
        controlPanel.add(showFloatingTilesCheckbox);
            showFloatingTilesCheckbox.addItemListener(this);
            showFloatingTilesCheckbox.setState(GraphPanel.showFloatingTiles);
            GraphPanel.runFast = !GraphPanel.showFloatingTiles;
        
    }

    /**
     *
     */
    public void mainInit() {
        System.out.println("mainInit() - Start");
        final Frame mainFrame = new Frame("Crystals");
        setLayout(new BorderLayout());
        mainFrame.addMouseListener(panel);

//        panel = new GraphPanel(this);
//        add("Center", panel);
//        controlPanel = new Panel();
//        add("South", controlPanel);

        commonInit();

        mainFrame.add("Center", panel);
        mainFrame.add("South", controlPanel);
        mainFrame.setSize(660, 720);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mainFrame.dispose();
                int status = 0;
                System.exit(status);
            }
        });
        mainFrame.setVisible(true); //f.show();

        boolean loaded = false;
        System.out.printf("**** TEST loadTilesURL() Start ****\n");
        //loadTilesURL();
        if (panel.getNumberOfTiles() < 1) {
            //loaded = loadTilesFromFile("e2pieces.txt");    // use local tile definition
            loaded = loadTilesFromFile("/home/dean.clark/workspace/Codility/Crystals/src/e2pieces.txt");    // use local tile definition
            
        }
        if(!loaded)
            return;

        System.out.printf("**** TEST loadTilesURL() End ****\n");

        wireTilesWithCornerSegments();
        wireTilesWithEdgeSegments();
        wireTileNonEdgeSegments();
        activateWires();

        panel.aFrame = new FrameStructure(CrystalGlobals.MAX_ROWS, CrystalGlobals.MAX_COLUMNS, panel.tiles, panel.getNumberOfTiles()); // ntiles
        panel.aFrame.setMaxColoursIndex();
        //panel.aFrame.availableColoursFromTiles();
        panel.aFrame.getAvailableGridColours();
        panel.aFrame.listAvailableColours();
        panel.aFrame.resetGridFrame();

        // populate hint tiles
        // TODO why does commenting out the following HINT line cause an Exception in thread "AWT-EventQueue-0" java.lang.ArrayIndexOutOfBoundsException: -1 in resetTile(FrameStructure.java:981)  
        panel.aFrame.hintToFrame(139, 8, 9, 1);  // colours 6 12 18 6
//        loadHintTilesFromFile("e2hints.txt");  // TODO - loadHintTilesFromFile
//        loadHintTilesFromFile("160tilesAsHints.txt");  // TODO - loadHintTilesFromFile
//        loadHintTilesFromFile("165tilesAsHints.txt");  // TODO - loadHintTilesFromFile

        // populate unassigned cells with a colour
        //panel.aFrame.randomizeGridFrame();


        panel.start();
        //panel.run();  // TODO why is this here?

        System.out.println("mainInit() - End");
    }  // mainInit

    /**
     *
     */
    @Override
    public void init() {
        setLayout(new BorderLayout());

//        panel = new GraphPanel(this);
//        add("Center", panel);
//        controlPanel = new Panel();
//        add("South", controlPanel);

        commonInit();

        String tileString = getParameter("tiles");
        for (StringTokenizer t = new StringTokenizer(tileString, ","); t.hasMoreTokens();) {
            String str = t.nextToken();
            //System.out.printf("str %s\n", str);
            int jj = 0;
            int j = str.indexOf(" ");  // e.g. "15 10 1 0 9"
            if (j > 0) {
                //System.out.printf("jj=%d, j=%d\n", jj, j);
                int tileId = Integer.valueOf(str.substring(jj, j)).intValue();
                int colour[] = new int[4];

                for (int wireIndex = 0; wireIndex < 4; wireIndex++) {
                    jj = j + 1;
                    j = str.indexOf(" ", jj);  // e.g. "15 10 1 0 9"
                    //System.out.printf("jj=%d, j=%d\n", jj, j);

                    if (j > 0) {
                        colour[wireIndex] = Integer.valueOf(str.substring(jj, j)).intValue();
                    } else {
                        colour[wireIndex] = Integer.valueOf(str.substring(jj)).intValue();
                    }

                    System.out.printf("jj=%d, j=%d    tileId=%d  colour=%d \n", jj, j, tileId, colour[wireIndex]);
                }
                panel.addTile(tileId, colour);
//                allTileArray[tileId] = newTile; //.wires[wireIndex] = Integer.parseInt(st.nextToken());
            }
        }
        panel.tileLockManager.setMaxLocksAllowed(panel.getNumberOfTiles());


        wireTilesWithCornerSegments();
        wireTilesWithEdgeSegments();
        wireTileNonEdgeSegments();
        activateWires();

        /************************
         * DISABLED CODE - START?
         ************************/
        if (true == false) {
            /**
             * link edge tiles using only faces ajacent to a grey
             * Add Wires by searching each face of every tile for a colour match and an adjacent grey
             */
//            for(int aTile=0; aTile<2; aTile++)
            for (int aTileIndexNotId = 0; aTileIndexNotId < panel.getNumberOfTiles(); aTileIndexNotId++) {
                for (int aSegment = 0; aSegment < MAX_SEGMENTS; aSegment++) {
                    // is grey
                    if (panel.tiles[aTileIndexNotId].tileSegment[aSegment].colour == 0) {
                        if ((aSegment > 0) && (panel.tiles[aTileIndexNotId].tileSegment[aSegment - 1].colour != 0)) {
                            // previous face is adjacent to a grey
                            linkToAllGreyTiles(aTileIndexNotId, aSegment - 1);
                        }
                        if ((aSegment < MAX_SEGMENTS - 1) && (panel.tiles[aTileIndexNotId].tileSegment[aSegment + 1].colour != 0)) {
                            // next face is adjacent to a grey
                            linkToAllGreyTiles(aTileIndexNotId, aSegment + 1);
                        }
                        if ((aSegment == MAX_SEGMENTS - 1) && (panel.tiles[aTileIndexNotId].tileSegment[0].colour != 0)) {
                            // first face is adjacent to last face which is a grey
                            linkToAllGreyTiles(aTileIndexNotId, 0);
                        }
                    }
                }
            }
        }

        // EXCLUDE WIRES (SEE MAX_NEIGHBOURS
        if (false == true) {
            /**
             * Add Wires by searching each face of every tile for a colour match
             */
//            for(int aTile=0; aTile<2; aTile++)
            for (int aTileIndexNotId = 0; aTileIndexNotId < panel.getNumberOfTiles(); aTileIndexNotId++) {
                for (int aSegment = 0; aSegment < MAX_SEGMENTS; aSegment++) {
                    for (int neighbourTileIndexNotId = 0; neighbourTileIndexNotId < panel.getNumberOfTiles(); neighbourTileIndexNotId++) {
                        // exclude self
                        if (aTileIndexNotId != neighbourTileIndexNotId) {
                            for (int neighbourSegment = 0; neighbourSegment < MAX_SEGMENTS; neighbourSegment++) {
                                // exclude grey
                                if (panel.tiles[aTileIndexNotId].tileSegment[aSegment].colour != 0) {
                                    if (panel.tiles[aTileIndexNotId].tileSegment[aSegment].colour == panel.tiles[neighbourTileIndexNotId].tileSegment[neighbourSegment].colour) {
                                        panel.addWire(aTileIndexNotId, aSegment, neighbourTileIndexNotId, neighbourSegment, wireLength);
                                        //                                    System.out.printf("**** TEST addWire() End ****\n");
                                        continue;
                                    }
                                }
                            }
                        }
                    }
                }
            }  // Add Wires
        }
        /************************
         * DISABLED CODE - END?
         ************************/

    } // end init()

    /**
     *
     */
    public void activateWires() {
        for (int currentTileIndexNotId = 0; currentTileIndexNotId < panel.getNumberOfTiles(); currentTileIndexNotId++) {
            // check for matching non-edge segments
            for (int segmentIndexCurrent = 0; segmentIndexCurrent < 4; segmentIndexCurrent++) {
                if (!panel.tiles[currentTileIndexNotId].tileSegment[segmentIndexCurrent].hasActiveWire()) {
                    panel.tiles[currentTileIndexNotId].tileSegment[segmentIndexCurrent].selectActiveWire(); // @todo - connect to segment array
                }
            }
        }
    } // end activateWires

    /**
     * wireTilesWithCornerSegments
     * once all ties have been added, add connecting wires
     * process corners first, edges second and remaining last.
     * corners(numberOfEdgeSegments==2) only connect to numberOfEdgeSegments=1 and nextToEdgeSegments[2]
     * edgesTiles only connect nextToEdgeSegments[2] to other nextToEdgeSegments[2] (corners done in step above)
     * edgeTiles only connect edgeOppositeSegment to numberOfEdgeSegments=0 Tiles
     */
    public void wireTilesWithCornerSegments() {
        for (int currentTileIndexNotId = 0; currentTileIndexNotId < panel.getNumberOfTiles(); currentTileIndexNotId++) {
            // corner tile
            if (panel.tiles[currentTileIndexNotId].getEdgeCount() > 1) {
                // all other tiles
                for (int otherTileIndexNotId = 0; otherTileIndexNotId < panel.getNumberOfTiles(); otherTileIndexNotId++) {
                    // exclude self
                    if (otherTileIndexNotId != currentTileIndexNotId) {
                        // edge tile
                        if (panel.tiles[otherTileIndexNotId].getEdgeCount() == 1) {
                            // check for matching non-edge segments
                            for (int nextToEdgeIndexCurrent = 0; nextToEdgeIndexCurrent < 2; nextToEdgeIndexCurrent++) {
                                int searchColorIndex = panel.tiles[currentTileIndexNotId].tileSegment[panel.tiles[currentTileIndexNotId].nextToEdgeSegments[nextToEdgeIndexCurrent]].colour;

                                // check for matching non-edge segments
                                for (int nextToEdgeIndexOther = 0; nextToEdgeIndexOther < 2; nextToEdgeIndexOther++) {
                                    if (searchColorIndex == panel.tiles[otherTileIndexNotId].tileSegment[panel.tiles[otherTileIndexNotId].nextToEdgeSegments[nextToEdgeIndexOther]].colour) {
                                        panel.addWire(currentTileIndexNotId, panel.tiles[currentTileIndexNotId].nextToEdgeSegments[nextToEdgeIndexCurrent], otherTileIndexNotId, panel.tiles[otherTileIndexNotId].nextToEdgeSegments[nextToEdgeIndexOther], CrystalGlobals.WIRE_LENGTH_INITIAL);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } // end wireTilesWithCornerSegments

    /**
     *
     */
    public void wireTilesWithEdgeSegments() {
        int searchColorIndex = 0;

        for (int currentTileIndexNotId = 0; currentTileIndexNotId < panel.getNumberOfTiles(); currentTileIndexNotId++) {
            // edge tiles
            if (panel.tiles[currentTileIndexNotId].getEdgeCount() == 1) {
                // all other tiles
                for (int otherTileIndexNotId = 0; otherTileIndexNotId < panel.getNumberOfTiles(); otherTileIndexNotId++) {
                    // exclude self
                    if (otherTileIndexNotId != currentTileIndexNotId) {
                        // edge tile (ignore corner tiles as these are handled in wireTilesWithCornerSegments())
                        if (panel.tiles[otherTileIndexNotId].getEdgeCount() == 1) {
                            // check for matching non-edge segments
                            for (int nextToEdgeIndexCurrent = 0; nextToEdgeIndexCurrent < 2; nextToEdgeIndexCurrent++) {
                                searchColorIndex = panel.tiles[currentTileIndexNotId].tileSegment[panel.tiles[currentTileIndexNotId].nextToEdgeSegments[nextToEdgeIndexCurrent]].colour;

                                // check for matching non-edge segments
                                for (int nextToEdgeIndexOther = 0; nextToEdgeIndexOther < 2; nextToEdgeIndexOther++) {
                                    if (searchColorIndex == panel.tiles[otherTileIndexNotId].tileSegment[panel.tiles[otherTileIndexNotId].nextToEdgeSegments[nextToEdgeIndexOther]].colour) {
                                        panel.addWire(currentTileIndexNotId, panel.tiles[currentTileIndexNotId].nextToEdgeSegments[nextToEdgeIndexCurrent], otherTileIndexNotId, panel.tiles[otherTileIndexNotId].nextToEdgeSegments[nextToEdgeIndexOther], CrystalGlobals.WIRE_LENGTH_INITIAL);
                                    }
                                }
                            }


                            // TODO Take care of opositeSegment
                            searchColorIndex = panel.tiles[currentTileIndexNotId].tileSegment[panel.tiles[currentTileIndexNotId].indexOppositeEdge].colour;

                            // check for matching non-edge segments
                            if (searchColorIndex == panel.tiles[otherTileIndexNotId].tileSegment[panel.tiles[otherTileIndexNotId].indexOppositeEdge].colour) {
                                panel.addWire(currentTileIndexNotId, panel.tiles[currentTileIndexNotId].indexOppositeEdge, otherTileIndexNotId, panel.tiles[otherTileIndexNotId].indexOppositeEdge, CrystalGlobals.WIRE_LENGTH_INITIAL);
                            }

                        } else if (panel.tiles[otherTileIndexNotId].getEdgeCount() == 0) {
                            // TODO Take care of opositeSegment
                            searchColorIndex = panel.tiles[currentTileIndexNotId].indexOppositeEdge;

                            // check for matching non-edge segments
                            for (int segmentIndex = 0; segmentIndex < 4; segmentIndex++) {
                                if (searchColorIndex == panel.tiles[otherTileIndexNotId].tileSegment[segmentIndex].colour) {
                                    panel.addWire(currentTileIndexNotId, panel.tiles[currentTileIndexNotId].indexOppositeEdge, otherTileIndexNotId, segmentIndex, CrystalGlobals.WIRE_LENGTH_INITIAL);
                                }
                            }
                        }

                    }
                }
            }
        }
    } // end wireTilesWithEdgeSegments()

    /**
     *
     */
    public void wireTileNonEdgeSegments() {
        for (int currentTileIndexNotId = 0; currentTileIndexNotId < panel.getNumberOfTiles(); currentTileIndexNotId++) {
            // non-edge tiles
            if (panel.tiles[currentTileIndexNotId].getEdgeCount() == 0) {
                // all other tiles
                for (int otherTileIndexNotId = 0; otherTileIndexNotId < panel.getNumberOfTiles(); otherTileIndexNotId++) {
                    // exclude self
                    if (otherTileIndexNotId != currentTileIndexNotId) {
                        // non-edge tile (ignore corner tiles as these are handled in wireTilesWithCornerSegments())
                        if (panel.tiles[currentTileIndexNotId].getEdgeCount() == 0) {
                            // check for matching non-edge segments
                            for (int segmentIndexCurrent = 0; segmentIndexCurrent < 4; segmentIndexCurrent++) {
                                int searchColorIndex = panel.tiles[currentTileIndexNotId].tileSegment[segmentIndexCurrent].colour;

                                // check for matching non-edge segments
                                for (int segmentIndexOther = 0; segmentIndexOther < 4; segmentIndexOther++) {
                                    if (searchColorIndex == panel.tiles[otherTileIndexNotId].tileSegment[segmentIndexOther].colour) {
                                        panel.addWire(currentTileIndexNotId, segmentIndexCurrent, otherTileIndexNotId, segmentIndexOther, CrystalGlobals.WIRE_LENGTH_INITIAL);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } // end wireTileNonEdgeSegments()

    /**
     * Called from init, this links a tile to all tile continaing a grey edge
     * @param aTileIndex
     * @param aSegmentIndex
     * @return numberOfWiresFound
     */
    public int linkToAllGreyTiles(int aTileIndex, int aSegmentIndex) {
        int numberOfWiresFound = 0;

        for (int aTileIndexNotId = 0; aTileIndexNotId < panel.getNumberOfTiles(); aTileIndexNotId++) {
            for (int aSegment = 0; aSegment < MAX_SEGMENTS; aSegment++) {
                // is grey
                if (panel.tiles[aTileIndexNotId].tileSegment[aSegment].colour == 0) {
                    if ((aSegment > 0) && (panel.tiles[aTileIndexNotId].tileSegment[aSegment - 1].colour != 0)) {
                        // previous face is adjacent to a grey
                        panel.addWire(aTileIndex, aSegmentIndex, aTileIndexNotId, aSegment - 1, wireLength);
                        numberOfWiresFound++;
                    }
                    if ((aSegment < MAX_SEGMENTS - 1) && (panel.tiles[aTileIndexNotId].tileSegment[aSegment + 1].colour != 0)) {
                        // next face is adjacent to a grey
                        panel.addWire(aTileIndex, aSegmentIndex, aTileIndexNotId, aSegment + 1, wireLength);
                        numberOfWiresFound++;
                    }
                    if ((aSegment == MAX_SEGMENTS - 1) && (panel.tiles[aTileIndexNotId].tileSegment[0].colour != 0)) {
                        // first face is adjacent to last face which is a grey
                        panel.addWire(aTileIndex, aSegmentIndex, aTileIndexNotId, 0, wireLength);
                        numberOfWiresFound++;
                    }
                }
            }
        }

        return numberOfWiresFound;
    }

    @Override
    public void destroy() {
        remove(panel);
        remove(controlPanel);
    }

    @Override
    public void start() {
        panel.start();
    }

    @Override
    public void stop() {
        panel.stop();
    }

    /**
     *
     * @param e
     */
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == button1) // scramble
        {
            if(CrystalGlobals.ModeOfOperation == CrystalGlobals.MODE_FRAME_SINGLE_PATH)
            {
                 if(FrameStructure.asPoly == true)
                         FrameStructure.asPoly = false;
                 else
                     FrameStructure.asPoly = true;
                 

            }
            else
            {
    //            play(getCodeBase(), "audio/computer.au");
                Dimension dimension = getSize();
                for (int aTileIndexNotId = 0; aTileIndexNotId < panel.getNumberOfTiles(); aTileIndexNotId++) {
                    Tile aTile = panel.tiles[aTileIndexNotId];
    //                if(!tileLockManager.isLocked(aTile.id))
                    if (aTile.lock == Tile.UNLOCKED) {
                        aTile.xWorldSpace = 10 + (dimension.width - 20) * Math.random();
                        aTile.yWorldSpace = 10 + (dimension.height - 20) * Math.random();
                    }
                }
            }
            return;
        }
        else if (src == button2) // scramble
        {
            if(CrystalGlobals.ModeOfOperation == CrystalGlobals.MODE_FRAME_SINGLE_PATH)
            {
                ++FrameStructure.scoreLockMode;
                if(FrameStructure.scoreLockMode > FrameStructure.ORIGIN_FROM_CORNER_EDGE_HINT_EQUAL)
                    FrameStructure.scoreLockMode=0;
            }

            return;
        }

        if (src == controlPanel.getComponent(2)) // shake)
        {
            //play(getCodeBase(), "audio/gong.au");
            Dimension d = getSize();
            for (int aTileIndexNotId = 0; aTileIndexNotId < panel.getNumberOfTiles(); aTileIndexNotId++) {
                Tile aTile = panel.tiles[aTileIndexNotId];
//                Node n = panel.tiles[i];
//                if(!tileLockManager.isLocked(aTile.id))
                if (aTile.lock == Tile.UNLOCKED) {
                    aTile.xWorldSpace += 80 * Math.random() - 40;
                    aTile.yWorldSpace += 80 * Math.random() - 40;
                }
            }
        }
    }

    /**
     *
     * @param e
     */
    public void itemStateChanged(ItemEvent e) {
        
        Object src = e.getSource();
        boolean on  = e.getStateChange() == ItemEvent.SELECTED;
        boolean off = e.getStateChange() == ItemEvent.DESELECTED;

        if (src == checkBox1) {    // Random
            if(CrystalGlobals.ModeOfOperation == CrystalGlobals.MODE_FRAME_SINGLE_PATH)
                FrameStructure.randomFillMode = on;
            else
                panel.randomModeCheckbox = on;
        }
        
        else if (src == showTileIdCheckbox) {
            panel.showTileIdCheckbox = on;
            
        } else if (src == checkBox2) {    // Active/Large
            if(CrystalGlobals.ModeOfOperation == CrystalGlobals.MODE_FRAME_SINGLE_PATH)
            {
                panel.randomModeCheckbox = on;
                
                if(panel.randomModeCheckbox) {
                    FrameStructure.LINE_LENGTH = 40;
                    FrameStructure.CELL_SCALE = 40;
                }
                else
                {
                    FrameStructure.LINE_LENGTH = 6; // 40
                    FrameStructure.CELL_SCALE = 6; // 40
                }
            }
            else
                panel.activeWiresCheckbox = on;
            
        } else if (src == drawRotation) {
            panel.drawRotationModeCheckbox = on;
            
        } else if (src == wireframeTile) {
            panel.wireframeTileModeCheckbox = on;
            
        } else if (src == showWires) {
            panel.showWiresModeCheckbox = on;
        }
        else if (src == showFloatingTilesCheckbox) {
            GraphPanel.showFloatingTiles = on;
            GraphPanel.runFast = !GraphPanel.showFloatingTiles;

        }

        // Show/Hide wire check button when not in floating mode
        if (GraphPanel.showFloatingTiles)
        {
            showWires.setVisible(true);
            CrystalGlobals.ModeOfOperation = CrystalGlobals.MODE_ELASTIC;
            
            button1.setLabel("Scramble");
            button2.setLabel("Shake");
            checkBox1.setLabel("Random");
            checkBox2.setLabel("Active");

        }
        else // if (panel.showFloatingTilesCheckbox == off)
        {
            showWires.setVisible(false);
            CrystalGlobals.ModeOfOperation = CrystalGlobals.MODE_FRAME_SINGLE_PATH;

            button1.setLabel("  Show  ");
            button2.setLabel("Infill ");
            checkBox1.setLabel("Random");
            checkBox2.setLabel("Large");

        }


    }

    @Override
    public String getAppletInfo() {
        return "Title: GraphLayout \nAuthor: D.Clark";
    }

    @Override
    public String[][] getParameterInfo() {
        String[][] info = {
            {"wires", "delimited string", "A comma-delimited list of all the wires.  It takes the form of 'C-N1,C-N2,C-N3,C-NX,N1-N2/M12,N2-N3/M23,N3-NX/M3X,...' where C is the name of center node (see 'center' parameter) and NX is a node attached to the center node.  For the wires connecting nodes to each other (and not to the center node) you may (optionally) specify a length MXY separated from the wire name by a forward slash."},
            {"center", "string", "The name of the center node."}
        };
        return info;
    }

    /**
     * Import tile details from file
     */
    public void loadTilesURL() {
        int tileIndex = 0;
        int segmentIndex = 0;
//        String objectName = "http://192.168.0.9/e2Samplepieces.txt";
//        String objectName = "http://192.168.0.9/e2pieces.txt";                    // 16x16  256 tile puzzle
        //    String objectName = "http://deanohome.dyndns.org/e2pieces.txt";
        String objectName = "http://deanohome.dyndns.org/e2Samplepieces.txt";       // 4x4    16 tile puzzle
        int colour[] = new int[4];

        System.out.println("loadTilesURL attempting to open file " + objectName);

        try {
            URL url = new URL(objectName);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            String str;

            StringTokenizer st; // = new StringTokenizer(str);
            String tokenStr; // = st.nextToken();
//                while (tileId < 256)
            while (tileIndex < 256) {

                // get poly list
                str = in.readLine();

                // quit if EOF
                if (str == null) {
                    break;
                }

                st = new StringTokenizer(str);
//        System.out.printf("loadTilesURL %s\n   read as :",  str);

                try {
                    for (segmentIndex = 0; segmentIndex < 4; segmentIndex++) {
                        colour[segmentIndex] = Integer.parseInt(st.nextToken());
//        System.out.printf(" %d ",  colour[segmentIndex]);
                    }
                    panel.addTile(tileIndex, colour);
                    System.out.printf(" tileIndex %d\n", tileIndex);
                } catch (java.lang.NumberFormatException e) {
                    System.out.println("NumberFormatException in loadLandItems!");
                }
                tileIndex++;
//        System.out.printf("\n");
            } // end while

            /* Close file */
            in.close();
            System.out.println("loadTilesURL close file, tiles loaded:" + tileIndex);

        } catch (java.io.FileNotFoundException e) {
            System.out.println("cannot open file " + objectName);
        } catch (java.io.EOFException e) {
            System.out.println("EOF error " + e);
        } catch (java.io.IOException e) {
            System.out.println("Input/Output error " + e);
        }

    } // end loadTilesURL

    /**
     * Import tile details from local file
     * Must configure "Project Properties -> Run -> Working Directory"
     *   currently: D:\Documents and Settings\deancl\My Documents\NetBeansProjects\Crystals\build\classes
     * @return status
     * 
     * File is of the following form whereby the first line contains the number of hints to follow
     * e.g.
     *       1
     *       139 8 9 1
     *    where: 139=tileId,  8=row, 9=col, 1=90CW rotation
     *
     */
    public boolean loadHintTilesFromFile(String objectName) {
        boolean status = false;
        int tileId = 0;
        int row = 0;
        int col = 0;
        int orientation = 0;
        //int segmentIndex = 0;
        //String objectName = "e2pieces.txt";  // rlative to working directory
        //String objectName = "e2pieces4x4sample.txt";  // rlative to working directory

        //int colour[] = new int[4];

        System.out.println("loadHintTilesFromFile attempting to open file " + objectName);

        try {
            File myFile = new File(objectName);

            String openMode = "r";
            int[] line = new int[4];
            int[] file = new int[4];

            int ii = 0;
            int jj = 0;

            System.out.println("loadHintTilesFromFile file open " + myFile.getParent() + "/" + objectName);

            try {
                RandomAccessFile fileAccess;
                fileAccess = new RandomAccessFile(myFile, openMode);
                System.out.println("loadHintTilesFromFile file open " + objectName);

                String str = "";

                StringTokenizer st; // = new StringTokenizer(str);
                String tokenStr; // = st.nextToken();

                // get number of hints within file
                str = fileAccess.readLine();
                // quit if EOF
                if (str == null) {
                    return status;
                }

                st = new StringTokenizer(str);
                int numHintsWithinFile = Integer.parseInt(st.nextToken());
                System.out.println("loadHintTilesFromFile reading numHintsWithinFile " + numHintsWithinFile);

                int hintsImported = 0;
                while (hintsImported < numHintsWithinFile) {  // TODO check for EOF

                    // get poly list
                    str = fileAccess.readLine();

                    // quit if EOF
                    if (str == null) {
                        break;
                    }

                    st = new StringTokenizer(str);

                    try {
                        tileId   = Integer.parseInt(st.nextToken());
                        row         = Integer.parseInt(st.nextToken());
                        col         = Integer.parseInt(st.nextToken());
                        orientation = Integer.parseInt(st.nextToken());
                        System.out.println("loadHintTilesFromFile reading tileId " + tileId + " row:" + row + " col:" + col + " orientation:" + orientation);

/*
                        for (segmentIndex = 0; segmentIndex < 4; segmentIndex++) {
                            colour[segmentIndex] = Integer.parseInt(st.nextToken());

                        // add colour to totalColours array
//                                ++totalColours[colour[segmentIndex]];
                        }
                        panel.addTile(tileId, colour);
*/
                        panel.aFrame.hintToFrame(tileId, row, col, orientation);  // colours 6 12 18 6

                        if ((Trace.debugLevel & Trace.DEBUG_GENERAL) == Trace.DEBUG_GENERAL) {
                            System.out.println("loadHintTilesFromFile reading tileIndex " + tileId + " data:" + str);
                        }

                    } catch (java.lang.NumberFormatException e) {
                        System.out.println("NumberFormatException in loadHintTilesFromFile!");
                    }
                    hintsImported++;
                } // end while

                /* Close file */
                fileAccess.close();
                System.out.println("loadHintTilesFromFile close file, hints loaded:" + hintsImported);
                status = true;

            } catch (java.lang.NumberFormatException e) {
                System.out.println("NumberFormatException in loadHintTilesFromFile!");
            }

        // https://ems.southglos.gov.uk/admit_transfer/at_welcome.asp
        } catch (java.io.FileNotFoundException e) {
            System.out.println("cannot open file " + objectName);
        } catch (java.io.IOException e) {
            System.out.println("Input/Output error " + e);
        }

        return status;
    }

    /**
     * Import tile details from local file
     * Must configure "Project Properties -> Run -> Working Directory"
     *   currently: C:\NetBeansProjects\Crystals\build\classes
     * @return status
     */
    public static boolean loadTilesFromFile(String objectName) {
        boolean status = false;
        int tileIndex = 0;
        int segmentIndex = 0;
        int checkTotalColours[] = new int[CrystalGlobals.MAX_COLORS];    // max 23

        //String objectName = "e2pieces.txt";  // rlative to working directory
        //String objectName = "e2pieces4x4sample.txt";  // rlative to working directory

        int colour[] = new int[4];

        System.out.println("loadTilesFromFile attempting to open file " + objectName);

        try {
            File myFile = new File(objectName);

            String openMode = "r";
//            int[] line = new int[4];
//            int[] file = new int[4];
//
//            int ii = 0;
//            int jj = 0;

            System.out.println("loadTilesFromFile file open " + myFile.getParent() + "/" + myFile.getName());

            try {
                RandomAccessFile fileAccess;
                fileAccess = new RandomAccessFile(myFile, openMode);
                //System.out.println("loadTilesFromFile file open " + objectName);

                String str = "";

                StringTokenizer st; // = new StringTokenizer(str);
                String tokenStr; // = st.nextToken();
                while (tileIndex < 256) {  // TODO check for EOF

                    // get poly list
                    str = fileAccess.readLine();

                    // quit if EOF
                    if (str == null) {
                        break;
                    }

                    st = new StringTokenizer(str);

                    try {
                        for (segmentIndex = 0; segmentIndex < 4; segmentIndex++) {
                            colour[segmentIndex] = Integer.parseInt(st.nextToken());

                        // add colour to totalColours array
//                                ++totalColours[colour[segmentIndex]];
                            
                            // for test purpose cound the number of colours loaded to be checked later
                            ++checkTotalColours[colour[segmentIndex]];
                        }
                        
                        
                        if(panel != null)
                            panel.addTile(tileIndex, colour);
                        else
                            System.out.println("loadTilesFromFile WARNING panel is NULL when running unit tests");


                        if ((Trace.debugLevel & Trace.DEBUG_GENERAL) == Trace.DEBUG_GENERAL) {
                            System.out.println("loadTilesFromFile reading tileIndex " + tileIndex + " data:" + str);
                        }

                    } catch (java.lang.NumberFormatException e) {
                        System.out.println("NumberFormatException in loadTilesFromFile!");
                    }
                    tileIndex++;
                } // end while

                /* Close file */
                fileAccess.close();
                System.out.println("loadTilesFromFile close file, tiles loaded:" + tileIndex);
                status = true;

            } catch (java.lang.NumberFormatException e) {
                System.out.println("NumberFormatException in loadTilesFromFile!");
            }

        // https://ems.southglos.gov.uk/admit_transfer/at_welcome.asp
        } catch (java.io.FileNotFoundException e) {
            System.out.println("cannot open file " + objectName);
        } catch (java.io.IOException e) {
            System.out.println("Input/Output error " + e);
        }

        int colourTotal = 0;
        for (int eachColourToBeCounted = 0; eachColourToBeCounted < CrystalGlobals.MAX_COLORS; eachColourToBeCounted++) {
            colourTotal += checkTotalColours[eachColourToBeCounted];
        }
        System.out.println("loadTilesFromFile total edges " + colourTotal);

        return status;
    } // end loadTilesFromFile()
}

/*
 * @todo release a used tile in favour of filling a hole
 * @todo a hole where no matching valid tile exists should release neighbour tiles and remove from tileIdAlreadyFound[]
 */


