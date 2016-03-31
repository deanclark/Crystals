/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crystalwink.crystals.FrameStructure;

import com.crystalwink.crystalscommon.CrystalGlobals;
import com.crystalwink.crystalscommon.Trace;
import com.crystalwink.crystalscommon.Tile.Tile;
//import com.crystalwink.crystalscommon.Trace.*;
import com.crystalwink.crystalscommon.TilePlacementInfo.TilePlacementInfo;
import com.crystalwink.crystals.GraphPanel.GraphPanel;

import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
//import java.io.UnsupportedEncodingException;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * Stores edge information rather than tiles
 *
 * @author Dean Clark deancl
 *
 */
public class FrameStructure extends GridFrameStructure{
    
    final int Orientations = 4;  // default to 4 for a four sided tile
    //int tileIdOrient[][] = new int[CrystalGlobals.MAX_TILES][Orientations];
    Tile tilesLocal[] = new Tile[CrystalGlobals.MAX_TILES];
    int nTilesLocal = 0;
    
    boolean tileIdAlreadyFound[] = new boolean[CrystalGlobals.MAX_TILES];
    int localOrientation = 0; // @todo return orientation of tile
    //private int maxCornerColoursInUse = 4;
    boolean improvedTileCountVerified = false;
    int totalTilesVerifiedEVER = 0;
    //int tilePlacementOrder[][] = new int[CrystalGlobals.MAX_TILES][3];  // [placementOrder][row, col]
    // public static final int CONCURRENT_HISTORY = 1;  // @TODO - would be nice to have multiple branches running concurrently
    int startHistoryIndex = 0; // 0=Hint Tile, 1 will start in the corners

    public static int LINE_LENGTH = 6; // 40
    public static int CELL_SCALE = 6; // 40
    public static boolean asPoly = false;      // @todo
    //static final boolean runFast = true; // = true; //
    public static boolean randomFillMode = false;
    static int TILEID_INDEX = 2;
    static int HIGH_WATER_TRIGGER = 20;
    static int BACK_TRACK_LOW = 1;
    static int BACK_TRACK_HIGH = 3;

    public FrameStructure(int verticalTiles, int horizontalTiles, Tile tiles[], int ntiles) {
        
        nTilesLocal = ntiles;
        System.out.println("FrameStructure() " + nTilesLocal + " tiles");

        
        // import from XML file
        String filename = "/home/dean.clark/workspace/Codility/Crystals/SilverTiles.xml";
        //String filename = "/opt/catd/data/catd/twitter-raw/236/wales/CATD-TwitterStream-1440388948176.xml";
        // see http://comments.gmane.org/gmane.comp.cms.wyona.user/93
        //  <!ENTITY hellip   "&#8230;" ><!-- horizontal ellipsis = three dot leader, U+2026 ISOpub  -->
        /*  XML Header
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE content [
                <!ENTITY hellip   "&#8230;" ><!-- horizontal ellipsis = three dot leader, U+2026 ISOpub  -->
                <!ENTITY pound  "&#163;" ><!-- pound sign,                                U+00A3 ISOnum -->
                <!ENTITY hearts   "&#9829;" ><!-- black heart suit = valentine,           U+2665 ISOpub -->
                <!ENTITY agrave "&#224;" ><!-- latin small letter a with grave = latin small letter a grave,       U+00E0 ISOlat1 -->
                <!ENTITY Agrave "&#192;" ><!-- latin capital letter A with grave = latin capital letter A grave,   U+00C0 ISOlat1 -->
                <!ENTITY % xhtml-symbol  PUBLIC "-//W3C//ENTITIES Symbols for XHTML//EN"  "xhtml-symbol.ent" >     %xhtml-symbol;
                <!ENTITY % xhtml-special PUBLIC "-//W3C//ENTITIES Special for XHTML//EN"  "xhtml-special.ent" >    %xhtml-special;
                <!ENTITY % xhtml-lat1    PUBLIC "-//W3C//ENTITIES Latin 1 for XHTML//EN"  "xhtml-lat1.ent" >       %xhtml-lat1;
            ]>
         */
        
        boolean dtdValidate = false;
        boolean xsdValidate = false;
        //String schemaSource = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setValidating(dtdValidate || xsdValidate);
        DocumentBuilder db;
        try {
            OutputStreamWriter errorWriter;
            errorWriter = new OutputStreamWriter(System.err, outputEncoding);
            PrintWriter outPrintWriter = new PrintWriter(errorWriter, true);
            this.out = outPrintWriter;

            db = dbf.newDocumentBuilder();
            db.setErrorHandler(new MyErrorHandler (new PrintWriter(errorWriter, true)));
            Document doc = db.parse(new File(filename));

//            Node node  = doc.getLastChild(); //getFirstChild(); //list.item(i);
//            echo(node);

            // appears to loop forever but does complete after around 5 minutes
            for (Node child = doc.getFirstChild(); child != null;
                    child = child.getNextSibling()) {
                   //echo(child);

//                   NodeList list = child.getChildNodes();
                   
//                   if(child != null)
//                   {
//                       int type = child.getNodeType();
//                       if(type == Node.TEXT_NODE)
//                       {
                           //System.out.println(child.getTextContent());
      ///                     echo(child);
//                       }
//                   }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // duplicate tile information ????
        for (int aTile = 0; aTile < nTilesLocal; aTile++) {
            tilesLocal[aTile] = tiles[aTile];
//            tilePlacementOrder2[aTile] = new TilePlacementInfo(rowX, colY, tileId);
//            tilePlacementOrder2[aTile] = new TilePlacementInfo(-1, -1, -1);  // [placementOrder][row, col]
        }

        initFrame(verticalTiles, horizontalTiles);
//        initFrame(horizontalTiles, verticalRowTiles);

        if(!testFrameSerialisation(verticalTiles, horizontalTiles))
            System.out.println("**** FAILED testFrameSerialisation ****");

    }

//    public class DOMEcho {
        static final String outputEncoding = "UTF-8";
        private static final int SEGMENTS_PER_TILE = 4;

        private PrintWriter out;
        private int indent = 0;
        private final String basicIndent = " ";

//      DOMEcho(PrintWriter out) {
//      this.out = out;
//  }
//      FrameStructure(PrintWriter out) {
//          this.out = out;
//      }

        private void outputIndentation() {
            for (int i = 0; i < indent; i++) {
                out.print(basicIndent);
            }
        }
        
        private void printlnCommon(Node n) {
            out.print(" nodeName=\"" + n.getNodeName() + "\"");

            String val = n.getNamespaceURI();
            if (val != null) {
                out.print(" uri=\"" + val + "\"");
            }

            val = n.getPrefix();

            if (val != null) {
                out.print(" pre=\"" + val + "\"");
            }

            val = n.getLocalName();
            if (val != null) {
                out.print(" local=\"" + val + "\"");
            }

            val = n.getNodeValue();
            if (val != null) {
                out.print(" nodeValue=");
                if (val.trim().equals("")) {
                    // Whitespace
                    out.print("[WS]");
                }
                else {
                    out.print("\"" + n.getNodeValue() + "\"");
                }
            }
            out.println();
        }
        
        private void echo(Node n) {
            
            outputIndentation();
            int type = n.getNodeType();
    
            if(out!= null)
            {
                switch (type) {
                    case Node.ATTRIBUTE_NODE:
                        out.print("ATTR:");
                        printlnCommon(n);
                        break;
        
                    case Node.CDATA_SECTION_NODE:
                        out.print("CDATA:");
                        printlnCommon(n);
                        break;
        
                    case Node.COMMENT_NODE:
                        out.print("COMM:");
                        printlnCommon(n);
                        break;
        
                    case Node.DOCUMENT_FRAGMENT_NODE:
                        out.print("DOC_FRAG:");
                        printlnCommon(n);
                        break;
        
                    case Node.DOCUMENT_NODE:
                        out.print("DOC:");
                        printlnCommon(n);
                        break;
        
                    case Node.DOCUMENT_TYPE_NODE:
                        out.print("DOC_TYPE:");
                        printlnCommon(n);
                        NamedNodeMap nodeMap = ((DocumentType)n).getEntities();
                        indent += 2;
                        for (int i = 0; i < nodeMap.getLength(); i++) {
                            Entity entity = (Entity)nodeMap.item(i);
                            echo(entity);
                        }
                        indent -= 2;
                        break;
        
                    case Node.ELEMENT_NODE:
                        out.print("ELEM:");
                        printlnCommon(n);
        
                        NamedNodeMap atts = n.getAttributes();
                        indent += 2;
                        for (int i = 0; i < atts.getLength(); i++) {
                            Node att = atts.item(i);
                            echo(att);
                        }
                        indent -= 2;
                        break;
        
                    case Node.ENTITY_NODE:
                        out.print("ENT:");
                        printlnCommon(n);
                        break;
        
                    case Node.ENTITY_REFERENCE_NODE:
                        out.print("ENT_REF:");
                        printlnCommon(n);
                        break;
        
                    case Node.NOTATION_NODE:
                        out.print("NOTATION:");
                        printlnCommon(n);
                        break;
        
                    case Node.PROCESSING_INSTRUCTION_NODE:
                        out.print("PROC_INST:");
                        printlnCommon(n);
                        break;
        
                    case Node.TEXT_NODE:
                        out.print("TEXT:");
                        printlnCommon(n);
                        break;
        
                    default:
                        out.print("UNSUPPORTED NODE: " + type);
                        printlnCommon(n);
                        break;
                }
            }
    
            indent++;
            for (Node child = n.getFirstChild(); child != null;
                 child = child.getNextSibling()) {
                echo(child);
            }
            indent--;
        }
    //}

    private static class MyErrorHandler implements ErrorHandler {
        
        private PrintWriter out;

        MyErrorHandler(PrintWriter out) {
            this.out = out;
        }

        private String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();
            if (systemId == null) {
                systemId = "null";
            }

            String info = "URI=" + systemId + " Line=" + spe.getLineNumber() +
                          ": " + spe.getMessage();
            return info;
        }

        public void warning(SAXParseException spe) throws SAXException {
            out.println("Warning: " + getParseExceptionInfo(spe));
        }
            
        public void error(SAXParseException spe) throws SAXException {
            String message = "Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }

        public void fatalError(SAXParseException spe) throws SAXException {
            String message = "Fatal Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }
    }
    
    /**
     * Find the named subnode in a node's sublist.
     * <ul>
     * <li>Ignores comments and processing instructions.
     * <li>Ignores TEXT nodes (likely to exist and contain
     *         ignorable whitespace, if not validating.
     * <li>Ignores CDATA nodes and EntityRef nodes.
     * <li>Examines element nodes to find one with
     *        the specified name.
     * </ul>
     * @param name  the tag name for the element to find
     * @param node  the element node to start searching from
     * @return the Node found
     */
    public Node findSubNode(String name, Node node) {
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            System.err.println("Error: Search node not of element type");
            System.exit(22);
        }

        if (! node.hasChildNodes()) return null;

        NodeList list = node.getChildNodes();
        for (int i=0; i < list.getLength(); i++) {
            Node subnode = list.item(i);
            if (subnode.getNodeType() == Node.ELEMENT_NODE) {
               if (subnode.getNodeName().equals(name)) 
                   return subnode;
            }
        }
        return null;
    }
    /**
     *FAILED
     * @param verticalRowTiles
     * @param horizontalColumnCells
     */
    public void initFrame(int verticalRowTiles, int horizontalColumnCells) {
//    public void initFrame(int horizontalColumnCells, int verticalRowTiles) {
        System.out.println("initFrame() - Start");
        HorizontalColumnCells = horizontalColumnCells;
        VerticalRowCells = verticalRowTiles;
        nGridColumns = HorizontalColumnCells + 1;
        nGridRows = (VerticalRowCells * 2) + 1;

        super.GridFrameStructure();  // init frameArray
        //frameArray = new int[nGridRows][nGridColumns];
        lockArray = new int[nGridRows][nGridColumns];

        // one time clear all
        clearEntireFrame();

        // reset
        for (int ii = 0; ii < CrystalGlobals.MAX_TILES; ii++) {
            tileIdAlreadyFound[ii] = false;
        }

        // test harness
        if (HorizontalColumnCells == 4 && VerticalRowCells == 4) {
            loadTilesStatic4x4();
        }
//        else
//            loadTilesStatic16x16();
//            loadTiles();    // load from local file


        setMaxColoursIndex();
        availableColoursFromTiles();
        getAvailableGridColours();
        // dump colour info to console
//        if((Trace.debugLevel & Trace.DEBUG_GENERAL) == Trace.DEBUG_GENERAL)
        listAvailableColours();


        // initialise the frame
        resetGridFrame();
        resetGridFrameEdges();

        //dumpFrameToTxt();
        System.out.println("initFrame() - End");
    }

    /**
     *
     * @param row - 0..VerticalRowCells
     * @param col - 0..HorizontalColumnCells
     * @return colour if set or -1 if out of range
     */
    public int setEdge(int row, int col, int colour) {
        int status = -1;
        int rowRange = nGridRows;   // 0..HorizontalColumnCells+1
        int colRange = nGridColumns; // 0..VerticalRowCells+1

        // if row even, limit col to VerticalRowCells
        if (row % 2 == 0) // even
        {
            rowRange -= 1;     // horizontal line "--"
        }
        // if col even, limit row to VerticalRowCells
        if (col % 2 == 0) {
            colRange -= 1;     // vertical line "|"
        }
        // check within range
        if (row >= 0 && (row < rowRange) && col >= 0 && (col < colRange)) {
            setColour(row, col, colour, CrystalGlobals.LOCKED_EDGE);
            //frameArray[row][col] = colour;
            status = frameArray[row][col];
        }

        return status;
    }

    /**
     * Defines a tile from the surounding edges.
     *                                                      row,col
     * Cell  1,1  will contain the edges in clockwise order   0,0    1,1    2,0    1,0
     * Cell  1,16 will contain the edges in clockwise order   0,15   1,16   2,16   1,15
     * Cell 16,16 will contain the edges in clockwise order  30,15  31,16  32,16  31,15
     *
     * @param row (board dimention e.g 1-16)
     * @param col (board dimention e.g 1-16)
     * @return Tile
     */
    public Tile defineTileFromFrame(int row, int col) {
        int id = -1;
        int colours[] = new int[SEGMENTS_PER_TILE];  // 4
        colours[0] = frameArray[(row - 1) * 2][col - 1];
        colours[1] = frameArray[((row - 1) * 2) + 1][col];
        colours[2] = frameArray[((row - 1) * 2) + 2][col - 1];
        colours[3] = frameArray[((row - 1) * 2) + 1][col - 1];

//        colours[0] = frameArray[(col*2)-2][row-1];
//        colours[1] = frameArray[(col*2)-1][row];
//        colours[2] = frameArray[(col*2)][row-1];
//        colours[3] = frameArray[(col*2)-1][row-1];

        int scale = 15;
        //Tile aTile = new Tile(id, colours);
        Tile aTile = new Tile(id, colours, scale);

        return aTile;
    }

    public boolean testFrameSerialisation(int verticalTiles, int horizontalTiles) {
        boolean result = true;

        int testFrameRowSize = verticalTiles;
        int testFrameColSize = horizontalTiles;
        int serializedFrameNumber = 0;

        System.out.println("testFrameSerialisation() - Test Start " + testFrameRowSize + " x " + testFrameColSize);

        for (int rowIndex = 1; rowIndex <= testFrameRowSize; rowIndex++) {
            for (int colIndex = 1; colIndex <= testFrameColSize; colIndex++) {
                serializedFrameNumber = rowColToSerialisedFrameNumber(testFrameRowSize, testFrameColSize, rowIndex, colIndex);

                if (rowIndex != rowFromSerialisedFrameNumber(testFrameRowSize, testFrameColSize, serializedFrameNumber)) {
                    System.out.println("testFrameSerialisation() - rowFromSerialisedFrameNumber() - ERROR decoding serialisedFrameNumber row " + rowIndex);
                    result = false; // failure
                }

                if (colIndex != colFromSerialisedFrameNumber(testFrameRowSize, testFrameColSize, serializedFrameNumber, rowIndex)) {
                    System.out.println("testFrameSerialisation() - colFromSerialisedFrameNumber() - ERROR decoding serialisedFrameNumber col " + colIndex + " at row " + rowIndex);
                    result = false; // failure
                }
                //System.out.println("testFrameSerialisation() - serializedFrameNumber " + serializedFrameNumber + " row: " + rowIndex + " col: " + colIndex);
            }
        }
        System.out.println("testFrameSerialisation() - Test End");

        return result;
    }


    /**
     * 
     * @param offScreenGraphics
     * @param palate
     */
    public void drawFrame(Graphics offScreenGraphics, Color palate[]) {
        //boolean runFast = true;// = false; //

        final int nPolyPoints = 5;
        int x1, y1, x2, y2;

        int xPointArrayPrint[] = new int[nPolyPoints];
        int yPointArrayPrint[] = new int[nPolyPoints];

        int xPointArray[] = new int[nPolyPoints];
        int yPointArray[] = new int[nPolyPoints];

        // top
        xPointArray[0] = 0;
        yPointArray[0] = -1 * (CELL_SCALE / 2);
        xPointArray[1] = (CELL_SCALE / 2);
        yPointArray[1] = 0;    // centre point
        xPointArray[2] = 0;
        yPointArray[2] = CELL_SCALE / 2;
        xPointArray[3] = -1 * (CELL_SCALE / 2);
        yPointArray[3] = 0;
        xPointArray[4] = 0;
        yPointArray[4] = -1 * (CELL_SCALE / 2);

        // @todo verify the frame
        // @todo remove unlocked vectors
        verifyFrame();
        resetGridFrame();  // @todo remove this line
        resetGridFrameEdges();  // @todo remove this line


        // try to select the next appropriate tile location before randomising the remaining segments
        if (randomFillMode) // @todo - Debug serialisedFrame logic
        {
            resetGridFrame();
            resetGridFrameEdges();
            randomizeGridFrame();
        } else {
            int serialisedFrameNumber = cellWithMostLockedNeighbours();

            int preferedRowI = rowFromSerialisedFrameNumber(HorizontalColumnCells, VerticalRowCells, serialisedFrameNumber); //(int) Math.round((serialisedFrameNumber / HorizontalColumnCells)+0.5);  // mod 16
            int preferedColI = colFromSerialisedFrameNumber(HorizontalColumnCells, VerticalRowCells, serialisedFrameNumber, preferedRowI); //serialisedFrameNumber - ((preferedRowI-1) * HorizontalColumnCells);
            int MAX_ITERATIONS = 2000; // 2000

            boolean debugSerialisedFrameNumber = false;
            if (debugSerialisedFrameNumber) {
                if (serialisedFrameNumber > -1) {
                    if (serialisedFrameNumber == 256 &&
                            preferedRowI != 16 &&
                            preferedColI != 16) {
                        System.out.println("cellWithMostLockedNeighbours() - ERROR decoding serialisedFrameNumber " + serialisedFrameNumber + " row " + preferedRowI + " col " + preferedColI);
                    }
                }

                if (serialisedFrameNumber == 16) {
                    System.out.println("cellWithMostLockedNeighbours() - INFO decoding serialisedFrameNumber " + serialisedFrameNumber + " row " + preferedRowI + " col " + preferedColI);
                }
            }

            if ((Trace.debugLevel & Trace.DEBUG_OUT_OF_RANGE) == Trace.DEBUG_OUT_OF_RANGE ||
                    (Trace.activeDebugTrace & Trace.DEBUG_OUT_OF_RANGE) == Trace.DEBUG_OUT_OF_RANGE) //                System.out.println("drawFrame() - Out Of Range row " + rowI + " col " + colI);
            {
                System.out.println("drawFrame() -              deserialized                 " + serialisedFrameNumber + " row:" + preferedRowI + " col:" + preferedColI);
            }

            int tileIdFound = 0;
            resetTile(preferedRowI, preferedColI, CrystalGlobals.UNLOCKED, false);
            //randomizeTileUntilValid(preferedRowI, preferedColI, MAX_ITERATIONS);
            tileIdFound = randomizeTileUntilValid(preferedRowI, preferedColI, MAX_ITERATIONS);
            //if(tileIdFound = randomizeTileUntilValid(preferedRowI, preferedColI, MAX_ITERATIONS) >= 0)

            // tileId 0..255
            if (tileIdFound >= startHistoryIndex) {
                // @todo

                //++tileHistory[tileIdFound];     // track the number of times this tile has been selected (live-lock detection)
                ++tilePlacementOrder2[gridCellFullyPlacedGet() - 1].tileHistory[tileIdFound];

                boolean debug_tileHistory = false;
                if (debug_tileHistory) {
                    System.out.println("tileHistory - ");
                    for (int tileIdTemp = 0; tileIdTemp < CrystalGlobals.MAX_TILES; tileIdTemp++) {
                        if (tilePlacementOrder2[gridCellFullyPlacedGet() - 1].tileHistory[tileIdTemp] > 0) {
                            System.out.print(" " + tileIdTemp);
                        }

//                        if(tileHistory[tileIdTemp] > 0)
//                            System.out.print( " " + tileIdTemp);
                    }
                    System.out.println("");

                    for (int tileIdTemp = 0; tileIdTemp < CrystalGlobals.MAX_TILES; tileIdTemp++) {
                        if (tilePlacementOrder2[gridCellFullyPlacedGet() - 1].tileHistory[tileIdTemp] > 0) {
                            System.out.print(" " + tilePlacementOrder2[gridCellFullyPlacedGet() - 1].tileHistory[tileIdTemp]);
                        }

//                        if(tileHistory[tileIdTemp] > 0)
//                            System.out.print( " " + tileHistory[tileIdTemp]);
                    }
                    System.out.println("");
                }

                //if( tilePlacementOrder[gridCellFullyPlacedGet()][TILEID_INDEX] == tileIdFound)
                if (tilePlacementOrder2[gridCellFullyPlacedGet()] != null && tilePlacementOrder2[gridCellFullyPlacedGet()].TILEID_INDEX == tileIdFound) {
                    // already been here
                }

                /**
                 * If all compatible tiles reach the High Water Mark we apear to be stuck.
                 * As the tile selection is random there is a likelyhood that not all compatible tiles will be tried
                 */
                boolean belowHighWater = false;
                for (int tileIdTemp = 0; tileIdTemp < CrystalGlobals.MAX_TILES; tileIdTemp++) {
                    if (tilePlacementOrder2[gridCellFullyPlacedGet() - 1].tileHistory[tileIdTemp] > 0 &&
                            tilePlacementOrder2[gridCellFullyPlacedGet() - 1].tileHistory[tileIdTemp] < HIGH_WATER_TRIGGER) //                    if( tileHistory[tileIdTemp] > 0 && tileHistory[tileIdTemp] < HIGH_WATER_TRIGGER)
                    {
                        belowHighWater = true;
                        break;
                    }
                }
                // clear tileHistory if apear to be stuck
//                if( tileHistory[tileIdFound] > HIGH_WATER_TRIGGER)
                if (belowHighWater == false) {
                    if (debug_tileHistory) {
                        System.out.println("HighWater Reached -  row:" + preferedRowI + " col:" + preferedColI + " tileFound:" + tileIdFound + " Colours Avail:" + getAvailableGridColours());
                    }

                    for (int tileIdTemp = 0; tileIdTemp < CrystalGlobals.MAX_TILES; tileIdTemp++) {
                        tilePlacementOrder2[gridCellFullyPlacedGet() - 1].tileHistory[tileIdTemp] = 0;
                        //tileHistory[tileIdTemp] = 0;
                    }

                    // reset about 10%
                    resetTile(preferedRowI, preferedColI, CrystalGlobals.LOCKED, false);  // @todo - release associated tileIdAlreadyFound before reseting a locked segment
                    int lasttilesPlaced = gridCellFullyPlacedGet();
                    for (int ii = lasttilesPlaced; ii > 0; ii--) {
//                        if(ii > (lasttilesPlaced - (lasttilesPlaced/10)) )
                        if (ii > (lasttilesPlaced - BACK_TRACK_LOW)) {
                            resetTile(tilePlacementOrder2[ii - 1].ROW_INDEX, tilePlacementOrder2[ii - 1].COL_INDEX, CrystalGlobals.LOCKED, false);  // @todo - release associated tileIdAlreadyFound before reseting a locked segment
                        }                            //resetTile(tilePlacementOrder[ii-1][ROW_INDEX], tilePlacementOrder[ii-1][COL_INDEX], CrystalGlobals.LOCKED);  // @todo - release associated tileIdAlreadyFound before reseting a locked segment

                        // @todo - prevent re-selection of persistently incompatible tiles (how?)

                    }
                } else {
//                System.out.println("belowHighWater -  row:" + preferedRowI + " col:" + preferedColI + " tileFound:" + tileIdFound + " Colours Avail:" + getAvailableGridColours());
                    // only proceed if the High Water has not been hit for this tile.
//                    if( tileHistory[tileIdFound] < HIGH_WATER_TRIGGER)
                    if (tilePlacementOrder2[gridCellFullyPlacedGet() - 1].tileHistory[tileIdFound] < HIGH_WATER_TRIGGER) {
                        // track Tile Placement Order to accommodate a backtrack when dead end is reached.
                        if (tilePlacementOrder2[gridCellFullyPlacedGet()] == null) {
                            tilePlacementOrder2[gridCellFullyPlacedGet()] = new TilePlacementInfo(preferedRowI, preferedColI, tileIdFound);

                            if (debug_tileHistory) {
                                System.out.println("belowHighWater - (NEW) row:" + preferedRowI + " col:" + preferedColI + " tileFound:" + tileIdFound + " Colours Avail:" + getAvailableGridColours() + " tiles placed " + gridCellFullyPlacedGet());
                            }
                        } else {
                            tilePlacementOrder2[gridCellFullyPlacedGet()].ROW_INDEX = preferedRowI;
                            tilePlacementOrder2[gridCellFullyPlacedGet()].COL_INDEX = preferedColI;
                            tilePlacementOrder2[gridCellFullyPlacedGet()].TILEID_INDEX = tileIdFound;

                            if (debug_tileHistory) {
                                System.out.println("belowHighWater -  row:" + preferedRowI + " col:" + preferedColI + " tileFound:" + tileIdFound + " Colours Avail:" + getAvailableGridColours() + " tiles placed " + gridCellFullyPlacedGet());
                            }
                        }
//                        tilePlacementOrder[gridCellFullyPlacedGet()][ROW_INDEX]    = preferedRowI;
//                        tilePlacementOrder[gridCellFullyPlacedGet()][COL_INDEX]    = preferedColI;
//                        tilePlacementOrder[gridCellFullyPlacedGet()][TILEID_INDEX] = tileIdFound;
                        gridCellFullyPlacedUp();
                    } else {
                        if (debug_tileHistory) {
                            System.out.println("HighWater BREACH -  row:" + preferedRowI + " col:" + preferedColI + " tileFound:" + tileIdFound + " Colours Avail:" + getAvailableGridColours() + " tiles placed " + gridCellFullyPlacedGet());
                        }

                        // clear for retry
                        resetTile(preferedRowI, preferedColI, CrystalGlobals.UNLOCKED, false);  // @todo - release associated tileIdAlreadyFound before reseting a locked segment
                        //gridCellFullyPlacedDown();
                    }

                    // insert a sleep if game over
                    if (debug_tileHistory) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }
                    }
                }



                //int tileIdFound = verifyTile(preferedRowI, preferedColI);
//                System.out.println("drawFrame() - verifyTile() row:" + preferedRowI + " col:" + preferedColI + " tileFound:" + tileIdFound + " Colours Avail:" + getAvailableGridColours());

//                if (tileIdFound > 0) {
//                    if (tileIdAlreadyFound[tileIdFound] == true) {
//                        resetTile(preferedRowI, preferedColI, CrystalGlobals.UNLOCKED, false);  // reset unlocked segments of this tile
                //resetTile(preferedRowI, preferedColI, CrystalGlobals.LOCKED, false);  // @todo - release associated tileIdAlreadyFound before reseting a locked segment
//                    }
                verifyFrame();
                //               }
                //               else
                //               {
                //resetTile(preferedRowI, preferedColI, CrystalGlobals.UNLOCKED, false);  // reset unlocked segments of this tile
//                    resetTile(preferedRowI, preferedColI, CrystalGlobals.LOCKED, false);  // @todo - release associated tileIdAlreadyFound before reseting a locked segment
//                    verifyFrame();
//                }

                // TODO - This hack was an attempt to increase the number of tiles placed however the tilePlacementOrder is not maintained so this is not useful
//                    if(gridCellFullyPlacedGet() > 150)
//                        randomizeGridFrame(); // @todo - remove the need for this
            } else {
                // @todo - unlock only unshared segments

                // @todo - need to identify a solution to locked pieces that can not be reset due to neighbour lock

                //clearNeighbourLocks(preferedRowI, preferedColI);
                //resetTile(preferedRowI, preferedColI, CrystalGlobals.UNLOCKED, false);  // reset unlocked segments of this tile

                // back track
                // reset 50% or a few tiles
                resetTile(preferedRowI, preferedColI, CrystalGlobals.LOCKED, false);  // @todo - release associated tileIdAlreadyFound before reseting a locked segment
                int lasttilesPlaced = gridCellFullyPlacedGet();
                for (int ii = lasttilesPlaced; ii > 0; ii--) {
                    if (ii > (lasttilesPlaced - BACK_TRACK_HIGH)) {
                        // reset history
                        // @todo - only if the newly selected tile is not the same as the previously rejected tile
                        for (int tileIdTemp = 0; tileIdTemp < CrystalGlobals.MAX_TILES; tileIdTemp++) {
                            //tileHistory[tileIdTemp] = 0;
                            if (tilePlacementOrder2[ii - 1] != null) {
                                tilePlacementOrder2[ii - 1].tileHistory[tileIdTemp] = 0;
                            }
                        }

//                    if(ii > (lasttilesPlaced - (lasttilesPlaced/2)) )
                        resetTile(tilePlacementOrder2[ii - 1].ROW_INDEX, tilePlacementOrder2[ii - 1].COL_INDEX, CrystalGlobals.LOCKED, false);  // @todo - release associated tileIdAlreadyFound before reseting a locked segment
                        //resetTile(tilePlacementOrder[ii-1][ROW_INDEX], tilePlacementOrder[ii-1][COL_INDEX], CrystalGlobals.LOCKED, false);  // @todo - release associated tileIdAlreadyFound before reseting a locked segment

                    }
                }

                int initialColours = GetLastColourIndex() - 68; // colours minus edge(64) - hint(4)
                if (getAvailableGridColours() < initialColours) {
                    System.out.println("WARNING - Available colours have been lost, was " + initialColours + " now " + getAvailableGridColours());
                }

                // TODO - This hack was an attempt to increase the number of tiles placed however the tilePlacementOrder is not maintained so this is not useful
                //if(gridCellFullyPlacedGet() > 150)
                //    randomizeGridFrame(); // @todo - remove the need for this

                verifyFrame();
//                resetGridFrame();
//                resetGridFrameEdges();
            }
        }


//        System.out.println("Start drawFrame()");
        for (int rowI = 0; rowI < nGridRows; rowI++) {
            if (rowI % 2 == 0) // even
            {
                // horizontal line
                for (int colI = 0; colI < nGridColumns - 1; colI++) {
                    x1 = (colI * CELL_SCALE);
                    y1 = ((rowI / 2) * CELL_SCALE);
                    x2 = (colI * CELL_SCALE) + LINE_LENGTH;
                    y2 = ((rowI / 2) * CELL_SCALE);
                    int colour = frameArray[rowI][colI];

                    if (!asPoly) {
                        offScreenGraphics.setColor(palate[2]);
                    }

                    if (colour >= CrystalGlobals.COLOUR_GREY) // i.e. not -1 (unset)
                    {
                        if (colI < (nGridColumns - 1)) // Do not print the last element of a horizontal
                        {
                            if (asPoly) {
                                offScreenGraphics.setColor(palate[colour]);
                            }

                            if (asPoly) {
                                for (int ii = 0; ii < nPolyPoints; ii++) {
                                    xPointArrayPrint[ii] = xPointArray[ii] + x1 + (LINE_LENGTH / 2);
                                    yPointArrayPrint[ii] = yPointArray[ii] + y1;
                                }
                                offScreenGraphics.fillPolygon(xPointArrayPrint, yPointArrayPrint, nPolyPoints);
                            }

                            offScreenGraphics.setColor(palate[CrystalGlobals.COLOUR_GREY]);
                            offScreenGraphics.drawLine(x1, y1, x2, y2);   // paint an edge line
                        }
                    }
                }
            } else // odd
            {
                // vetical line
                for (int colI = 0; colI < nGridColumns; colI++) {
                    x1 = (colI * CELL_SCALE);
                    y1 = ((rowI / 2) * CELL_SCALE);
                    x2 = (colI * CELL_SCALE);
                    y2 = ((rowI / 2) * CELL_SCALE) + LINE_LENGTH;
                    int colour = frameArray[rowI][colI];

                    if (colour >= CrystalGlobals.COLOUR_GREY) // not -1 (unset)
                    {
                        if (asPoly) {
                            offScreenGraphics.setColor(palate[colour]);
                        }

                        if (asPoly) {
                            for (int ii = 0; ii < nPolyPoints; ii++) {
                                xPointArrayPrint[ii] = xPointArray[ii] + x1;
                                yPointArrayPrint[ii] = yPointArray[ii] + y1 + (LINE_LENGTH / 2);
                            }
                            offScreenGraphics.fillPolygon(xPointArrayPrint, yPointArrayPrint, nPolyPoints);
                        }

                        offScreenGraphics.setColor(palate[CrystalGlobals.COLOUR_GREY]);
                        offScreenGraphics.drawLine(x1, y1, x2, y2);   // paint an edge line
                    }
                }
            }
        }

        // Show frequency spectrum carrier wave
        if (!asPoly) {
            //offScreenGraphics.drawImage(panel.offScreenImage, 0, 0, null);
            offScreenGraphics.setColor(palate[6]);

            // small narrow carrier
//            offScreenGraphics.drawLine(10, 120, 50, 110);   // @todo REMOVE hor
//            offScreenGraphics.drawLine(50, 110, 52, 20);    // @todo REMOVE ver
//            offScreenGraphics.drawLine(52, 20, 56, 20);     // @todo REMOVE hor
//            offScreenGraphics.drawLine(56, 20, 58, 110);    // @todo REMOVE ver
//            offScreenGraphics.drawLine(58, 110, 98, 120);   // @todo REMOVE hor

            // wide carrier
            offScreenGraphics.drawLine(CELL_SCALE, (VerticalRowCells - 1) * CELL_SCALE, ((HorizontalColumnCells / 2) - 3) * CELL_SCALE, (VerticalRowCells - 2) * CELL_SCALE);
            offScreenGraphics.drawLine(((HorizontalColumnCells / 2) - 3) * CELL_SCALE, (VerticalRowCells - 2) * CELL_SCALE, ((HorizontalColumnCells / 2) - 2) * CELL_SCALE, (2) * CELL_SCALE);
            offScreenGraphics.drawLine(((HorizontalColumnCells / 2) - 2) * CELL_SCALE, (2) * CELL_SCALE, ((HorizontalColumnCells / 2) + 2) * CELL_SCALE, (2) * CELL_SCALE);
            offScreenGraphics.drawLine(((HorizontalColumnCells / 2) + 2) * CELL_SCALE, (2) * CELL_SCALE, ((HorizontalColumnCells / 2) + 3) * CELL_SCALE, (VerticalRowCells - 2) * CELL_SCALE);
            offScreenGraphics.drawLine(((HorizontalColumnCells / 2) + 3) * CELL_SCALE, (VerticalRowCells - 2) * CELL_SCALE, (HorizontalColumnCells - 1) * CELL_SCALE, (VerticalRowCells - 1) * CELL_SCALE);
            offScreenGraphics.setColor(palate[2]);
        }
        //System.out.println("End drawFrame()");
        // Display stats
//        Dimension dimension = getSize();
        offScreenGraphics.setColor(palate[CrystalGlobals.COLOUR_GREY]);
        offScreenGraphics.drawString("Tiles Placed: ", 140, 40);       offScreenGraphics.drawString(String.valueOf(gridCellFullyPlacedGet()), 250, 40);
        offScreenGraphics.drawString("Available Colours: ", 140, 60);  offScreenGraphics.drawString(String.valueOf(getAvailableGridColours()), 250, 60);

        // insert a sleep if in slow mode
        try {
            if (GraphPanel.runFast) {
                Thread.sleep(0);
            } else {
                Thread.sleep(3000);
            }
        } catch (InterruptedException e) {
        }

        // clear active debug trace
        Trace.activeDebugTrace = 0;

    }

    public boolean arrayIndexWithinRange(int row, int col) {
        if (row >= 0 && col >= 0 && row < nGridRows && col < nGridColumns) {
            return true;
        }

        return false;
    }

    /**
     * Warning: When unlocking a tile you need to considder the impact to neighbouring tiles.
     * @param row
     * @param col
     * @param LockLevel
     */
    public void resetTile(int row, int col, int LockLevel, boolean overrideNeighbourProtection) {
        //int id = -1;
        //int colours[] = new int[SEGMENTS_PER_TILE];
        int segmentsCleared = 0;

        int rowTemp = 0;
        int colTemp = 0;

        // above
        rowTemp = (row - 1) * 2;
        colTemp = col - 1;
        if (arrayIndexWithinRange(rowTemp, colTemp)) {
            if (lockArray[rowTemp][colTemp] >= LockLevel) // return colour to available colour array before clearing
            {
                if (!isGridCellLocked(row - 1, col)) // check if neighbour tile locked or check overridden
                {
                    if (clearGridColour(rowTemp, colTemp) == CrystalGlobals.STATUS_OK) {
                        ++segmentsCleared;
                    }
                } else if (overrideNeighbourProtection) {
                    resetTile(row - 1, col, CrystalGlobals.LOCKED, false);
                }
            }
        }

        // right
        rowTemp = ((row - 1) * 2) + 1;
        colTemp = col;
        if (arrayIndexWithinRange(rowTemp, colTemp)) {
            if (lockArray[rowTemp][colTemp] >= LockLevel) // return colour to available colour array before clearing
            {
                if (!isGridCellLocked(row, col + 1)) // check if neighbour tile locked or check overridden
                {
                    if (clearGridColour(rowTemp, colTemp) == CrystalGlobals.STATUS_OK) {
                        ++segmentsCleared;
                    }
                } else if (overrideNeighbourProtection) {
                    resetTile(row, col + 1, CrystalGlobals.LOCKED, false);
                }
            }
        }

        // below
        rowTemp = ((row - 1) * 2) + 2;
        colTemp = col - 1;
        if (arrayIndexWithinRange(rowTemp, colTemp)) {
            if (lockArray[rowTemp][colTemp] >= LockLevel) // return colour to available colour array before clearing
            {
                if (!isGridCellLocked(row + 1, col)) // check if neighbour tile CrystalGlobals.LOCKED or check overridden
                {
                    if (clearGridColour(rowTemp, colTemp) == CrystalGlobals.STATUS_OK) {
                        ++segmentsCleared;
                    }
                } else if (overrideNeighbourProtection) {
                    resetTile(row + 1, col, CrystalGlobals.LOCKED, false);
                }
            }
        }


        // left
        rowTemp = ((row - 1) * 2) + 1;
        colTemp = col - 1;
        if (arrayIndexWithinRange(rowTemp, colTemp)) {
            if (lockArray[rowTemp][colTemp] >= LockLevel) // return colour to available colour array before clearing
            {
                if (!isGridCellLocked(row, col - 1)) // check if neighbour tile locked or check overridden
                {
                    if (clearGridColour(rowTemp, colTemp) == CrystalGlobals.STATUS_OK) {
                        ++segmentsCleared;
                    }
                } else if (overrideNeighbourProtection) {
                    resetTile(row, col - 1, CrystalGlobals.LOCKED, false);
                }
            }
        }

        // if a segment has been cleared the tile has been removed from the frame
        if (segmentsCleared > 0) {
            // track Tile Placemnet Order to acomadate a backtrack when dead end is reached.
//            if( tilePlacementOrder[gridCellFullyPlacedGet()-1][ROW_INDEX] == row &&
//                tilePlacementOrder[gridCellFullyPlacedGet()-1][COL_INDEX] == col)
            if (tilePlacementOrder2[gridCellFullyPlacedGet() - 1].ROW_INDEX == row &&
                    tilePlacementOrder2[gridCellFullyPlacedGet() - 1].COL_INDEX == col) {
                gridCellFullyPlacedDown();
                //System.out.println("segmentsCleared()");
            }
        }

    }

    /**
     * randomize frame (only uninitialsed cells)
     * hint: 139 8 9 1
     * tile: 139 6 12 18 6
     *
     *      1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7
     * 16   0 0 0 0 0 0 0 6 0 0 0 0 0 0 0 0 0
     * 17   0 0 0 0 0 0 0 8 12 0 0 0 0 0 0 0 0
     * 18   0 0 0 0 0 0 0 18 0 0 0 0 0 0 0 0 0
     *
     * 6=amber
     * 8=orange
     * 12=dark_blue
     * 18=light_blue
     *
     */
    public void hintToFrame(int tileId, int rowX, int colY, int orientation) {
        if ((rowX <= VerticalRowCells) && (colY <= HorizontalColumnCells)) {
            // allow lock only if neighboring tile is CrystalGlobals.LOCKED or CrystalGlobals.LOCKED_HINT
            // 8 down 9 across
            /*
            setColour( (rowX - 1) * 2     ,colY - 1, 6, CrystalGlobals.LOCKED_HINT);        // above?
            setColour(((rowX - 1) * 2) + 1,colY,     6, CrystalGlobals.LOCKED_HINT);       // right?
            setColour(((rowX - 1) * 2) + 2,colY - 1, 12, CrystalGlobals.LOCKED_HINT);       // below
            setColour(((rowX - 1) * 2) + 1,colY - 1, 18, CrystalGlobals.LOCKED_HINT);        // left?
             */

            // 9 down 8 across - CONFIRMED

        setColour(
                getGridIndex(CrystalGlobals.ROW_SELECTION, rowX, colY, CrystalGlobals.SEGMENT_INDEX_ABOVE),
                getGridIndex(CrystalGlobals.COL_SELECTION, rowX, colY, CrystalGlobals.SEGMENT_INDEX_ABOVE), 6, CrystalGlobals.LOCKED_HINT);

        setColour(
                getGridIndex(CrystalGlobals.ROW_SELECTION, rowX, colY, CrystalGlobals.SEGMENT_INDEX_RIGHT),
                getGridIndex(CrystalGlobals.COL_SELECTION, rowX, colY, CrystalGlobals.SEGMENT_INDEX_RIGHT), 6, CrystalGlobals.LOCKED_HINT);

        setColour(
                getGridIndex(CrystalGlobals.ROW_SELECTION, rowX, colY, CrystalGlobals.SEGMENT_INDEX_BELOW),
                getGridIndex(CrystalGlobals.COL_SELECTION, rowX, colY, CrystalGlobals.SEGMENT_INDEX_BELOW), 12, CrystalGlobals.LOCKED_HINT);

        setColour(
                getGridIndex(CrystalGlobals.ROW_SELECTION, rowX, colY, CrystalGlobals.SEGMENT_INDEX_LEFT),
                getGridIndex(CrystalGlobals.COL_SELECTION, rowX, colY, CrystalGlobals.SEGMENT_INDEX_LEFT), 18, CrystalGlobals.LOCKED_HINT);

        /*
            setColour((rowX * 2), colY - 2, 6, CrystalGlobals.LOCKED_HINT);
            setColour((rowX * 2) + 1, colY - 1, 6, CrystalGlobals.LOCKED_HINT);
            setColour((rowX * 2) + 2, colY - 2, 12, CrystalGlobals.LOCKED_HINT);
            setColour((rowX * 2) + 1, colY - 2, 18, CrystalGlobals.LOCKED_HINT);
         */

            // 9 down 8 across - UNCONFIRMED
//            setColour((rowX * 2), colY - 2, tilesLocal[tileId].tileSegment[ tilesLocal[tileId].getSegmetnWithOrientation(0, orientation) ].colour, CrystalGlobals.LOCKED_HINT);
//            setColour((rowX * 2) + 1, colY - 1, tilesLocal[tileId].tileSegment[ tilesLocal[tileId].getSegmetnWithOrientation(1, orientation) ].colour, CrystalGlobals.LOCKED_HINT);
//            setColour((rowX * 2) + 2, colY - 2, tilesLocal[tileId].tileSegment[ tilesLocal[tileId].getSegmetnWithOrientation(2, orientation) ].colour, CrystalGlobals.LOCKED_HINT);
//            setColour((rowX * 2) + 1, colY - 2, tilesLocal[tileId].tileSegment[ tilesLocal[tileId].getSegmetnWithOrientation(3, orientation) ].colour, CrystalGlobals.LOCKED_HINT);

            // 9 down 8 across - UNCONFIRMED
//            setColour((rowX * 2) -1,     colY - 1, tilesLocal[tileId].tileSegment[ tilesLocal[tileId].getSegmetnWithOrientation(0, orientation) ].colour, CrystalGlobals.LOCKED_HINT);
//            setColour((rowX * 2) , colY,     tilesLocal[tileId].tileSegment[ tilesLocal[tileId].getSegmetnWithOrientation(1, orientation) ].colour, CrystalGlobals.LOCKED_HINT);
//            setColour((rowX * 2) + 1, colY - 1, tilesLocal[tileId].tileSegment[ tilesLocal[tileId].getSegmetnWithOrientation(2, orientation) ].colour, CrystalGlobals.LOCKED_HINT);
//            setColour((rowX * 2) , colY - 1, tilesLocal[tileId].tileSegment[ tilesLocal[tileId].getSegmetnWithOrientation(3, orientation) ].colour, CrystalGlobals.LOCKED_HINT);

            // 8 down 9 across
//            setColour((rowX * 2), colY - 1, tilesLocal[tileId].tileSegment[ tilesLocal[tileId].getSegmetnWithOrientation(0, orientation) ].colour,  CrystalGlobals.LOCKED_HINT);       // above?
//            setColour((rowX * 2), colY,     tilesLocal[tileId].tileSegment[ tilesLocal[tileId].getSegmetnWithOrientation(1, orientation) ].colour,  CrystalGlobals.LOCKED_HINT);       // right?
//            setColour((rowX * 2), colY - 1, tilesLocal[tileId].tileSegment[ tilesLocal[tileId].getSegmetnWithOrientation(2, orientation) ].colour, CrystalGlobals.LOCKED_HINT);       // below
//            setColour((rowX * 2), colY - 1, tilesLocal[tileId].tileSegment[ tilesLocal[tileId].getSegmetnWithOrientation(3, orientation) ].colour, CrystalGlobals.LOCKED_HINT);       // left?

            System.out.println("hintToFrame() - setColour TileId:" + tileId + " row:" + rowX + " colY:" + colY
                    + " Colour0:" + tilesLocal[tileId].tileSegment[0].colour
                    + " Colour1:" + tilesLocal[tileId].tileSegment[1].colour
                    + " Colour2:" + tilesLocal[tileId].tileSegment[2].colour
                    + " Colour3:" + tilesLocal[tileId].tileSegment[3].colour
                    + " orientation:" + orientation + "==" + tilesLocal[tileId].getOrientation());

            // track Tile Placemnet Order to acomadate a backtrack when dead end is reached.
            if (tilePlacementOrder2[gridCellFullyPlacedGet()] == null) {
                tilePlacementOrder2[gridCellFullyPlacedGet()] = new TilePlacementInfo(rowX, colY, tileId);
            } else {
                tilePlacementOrder2[gridCellFullyPlacedGet()].ROW_INDEX = rowX;
                tilePlacementOrder2[gridCellFullyPlacedGet()].COL_INDEX = colY;
                tilePlacementOrder2[gridCellFullyPlacedGet()].TILEID_INDEX = tileId;
            }
//            tilePlacementOrder[gridCellFullyPlacedGet()][ROW_INDEX] = rowX;
//            tilePlacementOrder[gridCellFullyPlacedGet()][COL_INDEX] = colY;
            gridCellFullyPlacedUp();

            if (!isGridCellLocked(rowX, colY)) {
                System.out.println("hintToFrame() - ***ERROR** Known Hint Tile " + tileId + " is NOT locked to row:" + rowX + " colY:" + colY);
            }
//            else
//                System.out.println("hintToFrame() - Known Hint Tile " + tileId + " is locked to row:" + rowX + " colY:" + colY);

        }

        //dumpFrameToTxt();
    }
    /**
     * Only tiles with all four edqes set
     * First pass, Loop through each full tile looking for any invalid tracking invalid ones
     * Detect invlaid tiles with invalid neighbour and clear common edge -1
     * Second pass, look through each full tile looking for any invalid tracking invlaid ones
     * PROTECT edge and hint tile edges from clear.
     * check for close match spare tile match and replace appropriate edge
     * or randomly select an edge to clear
     *
     * From here return to ranomizeFrame()
     */
    public void verifyFrame() {
        // todo reset cells that fail the verify
//        Tile aTile = null;

        int totalTilesVerified = 0;  // @todo disalow duplicates
        int isAttached = 0;

        // reset list of previously found tiles
        for (int ii = 0; ii < CrystalGlobals.MAX_TILES; ii++) {
            tileIdAlreadyFound[ii] = false;
        }

        for (int numberOfLockedNeighbours = 4; numberOfLockedNeighbours > 0; numberOfLockedNeighbours--) {
            for (int rowI = 1; rowI < VerticalRowCells + 1; rowI++) {
                for (int colI = 1; colI < HorizontalColumnCells + 1; colI++) {
                    //                    System.out.println("verifyFrame() - Tile xWorldSpace:" + rowI + " yWorldSpace:" + colI);
                    //                if(frameArray[rowI][colI] != -1)
                    //                {
                    //                        System.out.println("verifyFrame() - Tile Verified xWorldSpace:" + rowI + " yWorldSpace:" + colI);
                    int tileIdFound = verifyTile(rowI, colI);

                    if (tileIdFound > -1) {
//                        System.out.println("verifyTile() - tileIdFound " + tileIdFound);
//                        if (tileIdAlreadyFound[tileIdFound] != false)
//                            System.out.println("verifyTile() - (ERROR) tileIdFound " + tileIdFound + " NOT in tileIdAlreadyFound");
                    }

                    if (tileIdFound != -1) {
                        //System.out.println("verifyFrame() - Tile id " + tileIdFound + " Verified xWorldSpace:" + rowI + " yWorldSpace:" + colI);

                        if (tileIdAlreadyFound[tileIdFound] == false) {

                            //numberOfLockedNeighbours
                            // allow lock only if neighboring tile is CrystalGlobals.LOCKED or LOCLED_HINT
                            isAttached = 0;
                            if (lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)] == CrystalGlobals.LOCKED_HINT || lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)] == CrystalGlobals.LOCKED || lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)] == CrystalGlobals.LOCKED_EDGE) {
                                ++isAttached;
                            }
                            if (lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT)] == CrystalGlobals.LOCKED_HINT || lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT)] == CrystalGlobals.LOCKED || lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT)] == CrystalGlobals.LOCKED_EDGE) {
                                ++isAttached;
                            }
                            if (lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW)] == CrystalGlobals.LOCKED_HINT || lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW)] == CrystalGlobals.LOCKED || lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW)] == CrystalGlobals.LOCKED_EDGE) {
                                ++isAttached;
                            }
                            if (lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT)] == CrystalGlobals.LOCKED_HINT || lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT)] == CrystalGlobals.LOCKED || lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT)] == CrystalGlobals.LOCKED_EDGE) {
                                ++isAttached;
                            }

                            // @todo lock current tile only if atleast one of it's edges are already locked to a hint or locked tile
                            if (isAttached == numberOfLockedNeighbours) {
                                if (lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)] == CrystalGlobals.UNLOCKED) {
                                    lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)] = CrystalGlobals.LOCKED;
                                    // remove used colour from available colour array
                                    //setColour((rowI - 1) * 2, colI - 1, frameArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)], CrystalGlobals.LOCKED);
                                    //--totalColours[frameArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_ABOVE)]];
                                }
                                if (lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT)] == CrystalGlobals.UNLOCKED) {
                                    lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT)] = CrystalGlobals.LOCKED;
                                    // remove used colour from available colour array
                                    //setColour(((rowI - 1) * 2) + 1, colI, frameArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT)], CrystalGlobals.LOCKED);
                                    //--totalColours[frameArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_RIGHT)]];
                                }
                                if (lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW)] == CrystalGlobals.UNLOCKED) {
                                    lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW)] = CrystalGlobals.LOCKED;
                                    // remove used colour from available colour array
                                    //setColour(((rowI - 1) * 2) + 2, colI - 1, frameArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW)], CrystalGlobals.LOCKED);
                                    //--totalColours[frameArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_BELOW)]];
                                }
                                if (lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT)] == CrystalGlobals.UNLOCKED) {
                                    lockArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT)] = CrystalGlobals.LOCKED;
                                    // remove used colour from available colour array
                                    //setColour(((rowI - 1) * 2) + 1, colI - 1, frameArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT)], CrystalGlobals.LOCKED);
                                    //--totalColours[frameArray[getGridIndex(CrystalGlobals.ROW_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT)][getGridIndex(CrystalGlobals.COL_SELECTION, rowI, colI, CrystalGlobals.SEGMENT_INDEX_LEFT)]];
                                }

                                //if tile not already in use
                                tileIdAlreadyFound[tileIdFound] = true;

                                // if a tile was found, reset the outer loop
                                numberOfLockedNeighbours = 4;
                                totalTilesVerified = 0;

//                                System.out.println("verifyFrame() - Locking Tile (NOT ALREADY FOUND) xWorldSpace:" + rowI + " yWorldSpace:" + colI + " tileIdFound " + tileIdFound);

                            }
                        }
                        if (numberOfLockedNeighbours == 1) {
                            ++totalTilesVerified;

                            // Trace in HINT format 139, 8, 9, 1
                            if (improvedTileCountVerified) {
                                System.out.println("verifyFrame() - Tile id, col, row, orientation " + tileIdFound + ", " + colI + ", " + rowI + ", " + localOrientation);
                            }
                        }
                    }

                    //                }
                    //                    System.out.println("verifyFrame() - Tile verified xWorldSpace:" + rowI + " yWorldSpace:" + colI);
                }
            }
        }

        if (improvedTileCountVerified) {
            if(totalTilesVerified != (gridCellFullyPlacedGet()))
                System.out.println("verifyFrame() -                                    Total verified " + totalTilesVerified + " should equal tilesPlacedGet() " + (gridCellFullyPlacedGet()) + " ** WARNING **");
            else
                System.out.println("verifyFrame() -                                    Total verified " + totalTilesVerified);
        }

        if (totalTilesVerified > totalTilesVerifiedEVER) {
            totalTilesVerifiedEVER = totalTilesVerified;
            improvedTileCountVerified = true;
        } else {
            improvedTileCountVerified = false;
        }
    }

    public int randomizeTileUntilValid(int rowI, int colI, int MAX_ITERATIONS) {
        //int MAX_ITERATIONS = 2;
        int tileIdFound = -1;
        boolean abort = false;
        int iterations = 0;

        while (tileIdFound == -1 && !abort) {
            if (rowI > 0 && rowI <= VerticalRowCells && colI > 0 && colI <= HorizontalColumnCells) {
                randomizeUnlockedSegments(rowI, colI);  // @todo optomization required

                tileIdFound = verifyTile(rowI, colI);

                // tile already in use or exceeded number of tries
                if (tileIdFound != -1) {
                    if (tileIdAlreadyFound[tileIdFound] == true ||
                            //                            tileHistory[tileIdFound] > HIGH_WATER_TRIGGER ||
                            tilePlacementOrder2[gridCellFullyPlacedGet() - 1].tileHistory[tileIdFound] > HIGH_WATER_TRIGGER) {
                        tileIdFound = -1;
                    }
                }

                if (tileIdFound == -1) {
                    resetTile(rowI, colI, CrystalGlobals.UNLOCKED, false);  // reset unlocked segments of this tile
                }//                    resetGridFrame();
            } else {
                Trace.activeDebugTrace &= Trace.DEBUG_OUT_OF_RANGE;
                if ((Trace.debugLevel & Trace.DEBUG_OUT_OF_RANGE) == Trace.DEBUG_OUT_OF_RANGE ||
                        (Trace.activeDebugTrace & Trace.DEBUG_OUT_OF_RANGE) == Trace.DEBUG_OUT_OF_RANGE) {
                    System.out.println("randomizeTileUntilValid() - Out Of Range row " + rowI + " col " + colI);
                }
            }


            ++iterations;
            if (iterations > MAX_ITERATIONS) {
                abort = true;
            }

        }

        boolean debug_randomizeTileUntilValid = false;
        if (debug_randomizeTileUntilValid) {
            if (tileIdFound != -1) {
                System.out.println("randomizeTileUntilValid() - row " + rowI + " col " + colI + "    iterations " + iterations + " tileIdFound " + tileIdFound);
            }
        }

        return tileIdFound;
    }

    public int compareTiles(Tile aTileReference, Tile bTile) {
        int tileIdFound = -1;

        // find first matching segment[0]
        for (int segmentB = 0; segmentB < 4; segmentB++) {
            if (aTileReference.tileSegment[0].colour == bTile.tileSegment[segmentB].colour) {
                boolean status = true;
                // found a match now check for 4 in a row
                for (int fourInRow = 0; fourInRow < 4; fourInRow++) {
                    if (aTileReference.tileSegment[fourInRow].colour != bTile.tileSegment[validSegmentRecursion(fourInRow + segmentB)].colour) {
                        status = false;
                        break;
                    }
                }
                if (status) {
                    // match found
                    tileIdFound = aTileReference.getId();
                    localOrientation = segmentB;
                    break;
                }
            }
        }
        /*
        // @todo loop through all tiles and each orentation to verify a match
        for (int segmentA = 0 ; segmentA < 4; segmentA++) {
        for (int segmentB = 0 ; segmentB < 4; segmentB++) {

        // synch tile orientation
        if( aTileReference.tileSegment[segmentA].colour == bTile.tileSegment[segmentB].colour)
        {
        boolean status = true;
        for (int segmentASync = 0 ; segmentASync < 4; segmentASync++) {
        for (int segmentBSync = 0 ; segmentBSync < 4; segmentBSync++) {
        if( aTileReference.tileSegment[validSegmentRecursion(segmentASync+segmentA)].colour != bTile.tileSegment[validSegmentRecursion(segmentBSync+segmentB)].colour)
        {
        status = false;
        break;
        }
        }
        if(status)
        {
        // match found
        tileIdFound = aTileReference.id;
        break;
        }
        }
        }
        }
        }
         */
        return tileIdFound;
    }

    public int validSegmentRecursion(int input) {
//        System.out.println("validSegmentRecursion() - input:" + input);
        if (input >= SEGMENTS_PER_TILE) {
            return validSegmentRecursion(input - SEGMENTS_PER_TILE);
        } else if (input < 0) {
            return validSegmentRecursion(input + SEGMENTS_PER_TILE);
        }

        return input;
    }

    /**
     * number of colours available - can be used to verify a match of uncoloured tiles to colours available
     * reads all the segment colours from the list of tiles and compiles a count of each colour assigned to each segment
     */
    public void availableColoursFromTiles() {
        int totalAvailableColours = 0;

        if(tilesLocal[0] == null)
            System.out.println("availableColoursFromTiles - WARNING tilesLocal[] has not been initialised " + nTilesLocal + " tiles");
            
        System.out.println("availableColoursFromTiles " + nTilesLocal + " tiles");
        for (int aTileIndex = 0; aTileIndex < nTilesLocal; aTileIndex++) {
            for (int segmentIndex = 0; segmentIndex < SEGMENTS_PER_TILE; segmentIndex++) {
                ++totalColours[tilesLocal[aTileIndex].tileSegment[segmentIndex].colour];

                totalAvailableColours++;
            }
        }
        // debug missing colours (total 1000 should be 1024
        System.out.println("availableColoursFromTiles totalAvailableColours:" + totalAvailableColours);
    }

    public int verifyTile(int row, int col) {
        Tile aTile = defineTileFromFrame(row, col);  // returns a new tile defined from frame
        int tileIdFound = -1;

        // @todo loop through all tiles and each orentation to verify a match
        for (int aTileIndexNotId = 0; aTileIndexNotId < nTilesLocal; aTileIndexNotId++) {
            if (compareTiles(tilesLocal[aTileIndexNotId], aTile) != -1) {
//                System.out.println("verifyTile() - Tile id:" + tilesLocal[aTileIndexNotId].id);
                tileIdFound = tilesLocal[aTileIndexNotId].getId();
                break;
            }
        }

        return tileIdFound;
    }

}
