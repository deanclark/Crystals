/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.crystalwink.crystalscommon.Tile;

import com.crystalwink.crystalscommon.TileAbstract.TileAbstract;
import com.crystalwink.crystalscommon.Segment.Segment;
import com.crystalwink.crystalscommon.Trace;

/**
 *
 * Tile defines the basic 4 segment tile representative of the eternatyII tile.
 * Consists of Segment[segmentCount]
 * 
 * @author Dean Clark deancl
 */
public class Tile extends TileAbstract {

    public static int segmentCount = 4; // 4 sided shape (square)
    public static int segmentScale = 14; // 26;  UI specific
//    public static int segmentScale = 50;  // UI specific (android)
    public int WireLinkIndex = 3;

    public Segment tileSegment[] = new Segment[segmentCount];

    // the Wire connect with the closest optimum length will be used to determine the angle of rotation.
    // i.e. the closest segments will be orientated towards each other
    public double rotationVector = 0.0; // 0 default, increments in 45 degree steps (max=3)
    // scan all active wires of all segments and note the wire with the shortest length.
    public final static int UNLOCKED = 0;
    public final static int LOCKED = -1;
    public final static int TEMPORARY_LOCKED = 20;


    // Related to WorldSpace
    public double xWorldSpace = 50.0;
    public double yWorldSpace = 50.0;
    public double dx = 50.0;
    public double dy = 50.0;
    private boolean moved = true;
    public int lock = UNLOCKED;
    int primarySegment = 0;                 // @todo - used to lock the graphical rotation of the tile
    int segmentPriority[] = new int[segmentCount];     // @todo - used to resolve contention
    int numberOfEdgeSegments = 0;           // indicates the number of edge segments
    int indexOfEdgeSegment[] = new int[2];  // maximum of 2 edges populated in checkForEdgeSegments()
    public int indexOppositeEdge = -1;              // only set if numberOfEdgeSegments=1
    public int nextToEdgeSegments[] = new int[2];
    int edgeColorIndex = 0;    // grey

    public boolean locked() {
        return lock == UNLOCKED ? false : true;
    }

    public boolean moved() {
        return moved;
    }

    public void moved(boolean hasMoved) {
        moved = hasMoved;
    }

    public Tile(int tileId, int colours[], int scale) {
        super(tileId, "", segmentCount);  // TileAbstract constructor
        segmentScale = scale;
        initTile(tileId);
        initTileColours(colours);
    }

    public void TileScaled(int tileId, int colours[], int scale) {
        segmentScale = scale;
        initTile(tileId);
        initTileColours(colours);
    }

    public void getTileColours(int colours[]) {
        for (int ii = 0; ii < segmentCount; ii++) {
            colours[ii] = tileSegment[ii].colour;
        }
    }

    public void initTileColours(int colours[]) {
        for (int ii = 0; ii < segmentCount; ii++) {
            tileSegment[ii].colour = colours[ii];
        }

        // init edge segment information
        checkForEdgeSegments();
    }

    public void initTile(int tileId) {
        lock = UNLOCKED;

        if ((Trace.debugLevel & Trace.DEBUG_GENERAL) == Trace.DEBUG_GENERAL) {
            System.out.printf("start Tile Constructor tileId=%d\n", tileId);
        }

        tileSegment[0] = new Segment();
        tileSegment[1] = new Segment();
        tileSegment[2] = new Segment();
        tileSegment[3] = new Segment();

        // all share the same centre point
        for (int ii = 0; ii < segmentCount; ii++) {
//            System.out.printf("Tile init ii-%d\n", ii);
            tileSegment[ii].xPointArray[1] = 0;    // centre point
            tileSegment[ii].yPointArray[1] = 0;    // centre point
//            System.out.printf("Tile colour=%d\n", tileSegment[ii].colour);
        }

        // face 0
        tileSegment[0].xPointArray[2] = -segmentScale / 2;
        tileSegment[0].yPointArray[2] = -segmentScale / 2;
        tileSegment[0].xPointArray[0] = segmentScale / 2;
        tileSegment[0].yPointArray[0] = -segmentScale / 2;
        // Wire Point
        tileSegment[0].xPointArray[WireLinkIndex] = 0;
        tileSegment[0].yPointArray[WireLinkIndex] = -segmentScale / 2;

        // face 1
        tileSegment[1].xPointArray[2] = segmentScale / 2;
        tileSegment[1].yPointArray[2] = -segmentScale / 2;
        tileSegment[1].xPointArray[0] = segmentScale / 2;
        tileSegment[1].yPointArray[0] = segmentScale / 2;
        // Wire Point
        tileSegment[1].xPointArray[WireLinkIndex] = segmentScale / 2;
        tileSegment[1].yPointArray[WireLinkIndex] = 0;

        // face 2
        tileSegment[2].xPointArray[2] = segmentScale / 2;
        tileSegment[2].yPointArray[2] = segmentScale / 2;
        tileSegment[2].xPointArray[0] = -segmentScale / 2;
        tileSegment[2].yPointArray[0] = segmentScale / 2;
        // Wire Point
        tileSegment[2].xPointArray[WireLinkIndex] = 0;
        tileSegment[2].yPointArray[WireLinkIndex] = segmentScale / 2;

        // face 3
        tileSegment[3].xPointArray[2] = -segmentScale / 2;
        tileSegment[3].yPointArray[2] = segmentScale / 2;
        tileSegment[3].xPointArray[0] = -segmentScale / 2;
        tileSegment[3].yPointArray[0] = -segmentScale / 2;
        // Wire Point
        tileSegment[3].xPointArray[WireLinkIndex] = -segmentScale / 2;
        tileSegment[3].yPointArray[WireLinkIndex] = 0;

        if ((Trace.debugLevel & Trace.DEBUG_GENERAL) == Trace.DEBUG_GENERAL) {
            System.out.printf("end Tile Constructor tileId=%d\n", tileId);
        }
    }

    public static int getSegmentScale()
    {
        return segmentScale;
    }

    public static void setSegmentScale(int segmentScale)
    {
        Tile.segmentScale = segmentScale;
    }

    public int getOrientation(int segmentId) {
        // @todo - need to test the following logic
        // return orientation adjusted segmentId
        return (((segmentId + super.getOrientation()) <= (segmentCount-1)) ? (segmentId + super.getOrientation()) : ((segmentId + super.getOrientation()) - segmentCount));
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //Using the 3D-xyz values and rot-xyz , new-xyz values are calulated.
    //void rotationZ(Segment anObject, Craft viewer) {
    void rotationZ(Segment anObject, Tile viewer) {
        double mat1[] = new double[]{0, 0, 0, 1};         // matrix (x,y,z)
        double mat2[] = new double[]{1, 0, 0, 0, // rotation matrix
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1};

        double mat3[] = new double[]{0, 0, 0, 1};         // result matrix

        //	double angle = viewer.Pitch;		// rotate about x.
        //	double angle = viewer.Yaw;		// rotate about y.
        ////	double angle = -viewer.Roll;		// rotate about z.
        double angle = viewer.rotationVector;		// rotate about z.


        // Rotation matrix for rotation about z axis.

        mat2[0] = Math.cos(angle);		// rotate x points (x,-,-,0)  x
        mat2[1] = -Math.sin(angle);		// rotate y points (-,x,-,0)  y
        mat2[4] = Math.sin(angle);		// rotate x points (x,-,-,0)  x
        mat2[5] = Math.cos(angle);		// rotate y points (-,x,-,0)  y


        // for each point in the object, obtain the homogeneous coordinates.
        //    for (int i=0; i<anObject.vertices; i++) {
        for (int i = 0; i < anObject.segmentPoints; i++) {

            //        mat1[0] = ( (float)anObject.newX[i] - (float)viewer.eyeX );	// copy xyz for each point to homogeneous array (x,y,x,1)
            //        mat1[1] = ( (float)anObject.newY[i] - (float)viewer.eyeY );
            //        mat1[2] = ( (float)anObject.newZ[i] - (float)viewer.eyeZ );

            //        mat1[0] = ( (double)anObject.newX[i]);		// copy xyz for each point to homogeneous array (x,y,x,1)
            //        mat1[1] = ( (double)anObject.newY[i]);
            //        mat1[2] = ( (double)anObject.newZ[i]);

            //        mat1[0] = ( (double)anObject.xPointArrayPrint[i]);		// copy xyz for each point to homogeneous array (x,y,x,1)
            //        mat1[1] = ( (double)anObject.yPointArrayPrint[i]);
            //        mat1[2] = ( (double)anObject.zPointArrayPrint[i]);  // Not used in Eternity 2D view

            mat1[0] = ( anObject.xPointArray[i]);		// copy xyz for each point to homogeneous array (x,y,x,1)
            mat1[1] = ( anObject.yPointArray[i]);
            mat1[2] = ( anObject.zPointArrayPrint[i]);  // Not used in Eternity 2D view

            //        System.out.printf("rotationZ before Tile=%d  x=%d  y=%d, angle=%f\n", viewer.id, anObject.xPointArrayPrint[i], anObject.yPointArrayPrint[i], viewer.rotationVector);

            threeDcross14(mat1, mat2, mat3);			// multiply mat1 with mat2 = mat3.

            //        anObject.newX[i] = (int) mat3[0];			// copy homogeneous array to new xyz
            //        anObject.newY[i] = (int) mat3[1];
            //        anObject.newZ[i] = (int) mat3[2];


            anObject.xPointArrayPrint[i] = (int) mat3[0];			// copy homogeneous array to new xyz
            anObject.yPointArrayPrint[i] = (int) mat3[1];
            anObject.zPointArrayPrint[i] = (int) mat3[2];

            anObject.xPointArrayPrint[i] += (int) viewer.xWorldSpace;			// copy homogeneous array to new xyz
            anObject.yPointArrayPrint[i] += (int) viewer.yWorldSpace;

        //        System.out.printf("rotationZ after  Tile=%d  x=%d  y=%d\n", viewer.id, anObject.xPointArrayPrint[i],anObject.yPointArrayPrint[i]);
        }
    }   // end rotationZ

    ////////////////////////////////////////////////////////////////////////////////////
    // mat1 = 1*4, mat2 = 4*4, mat3 = 1*4
    //   (0,1,2,3) X ( 0, 1, 2, 3)        = (0,1,2,3)
    //               ( 4, 5, 6, 7)
    //               ( 8, 9,10,11)
    //               (12,13,14,15)
    static void threeDcross14(double mat1[], double mat2[], double mat3[]) {
        float temp;

        for (int i = 0; i < segmentCount; i++) {   // clear output mat3
            mat3[i] = 0;
        }

        for (int j = 0; j < segmentCount; j++) {
            temp = 0;
            for (int k = 0; k < segmentCount; k++) {
                temp += mat1[k] * mat2[k + (k * 3) + j];
            }
            mat3[j] = temp;
        }

    } /* end threeDcross14 */

    ////////////////////////////////////////////////////////////////////////////////////

    public void moveTile(int newX, int newY) {
        xWorldSpace = newX;
        yWorldSpace = newY;
        moved(true);
    }

    public int getEdgeAdjacent(int segmentIndex) {
        /*
        int numberOfEdgeSegments = 0;                   // @todo REMOVE THIS LINE
        int nextToEdgeSegments[] = new int[2];     // @todo REMOVE THIS LINE
         */
        if (segmentIndex < 2 && segmentIndex >= 0) {
            if (numberOfEdgeSegments == 0) {
                return -1; // error, no edges
            }

            return nextToEdgeSegments[segmentIndex];
        }
        return -1; // error, segmentIndex out of range
    } // end getEdgeAdjacent()

    /* called by checkForEdgeSegments()
     *
     */
    public int setEdgeOposite() {
        // only interested in edge pieces not corners
        if (getEdgeCount() == 1) {
            if (indexOfEdgeSegment[0] <= 1) {
                indexOppositeEdge = indexOfEdgeSegment[0] + 2;
            } else {
                indexOppositeEdge = indexOfEdgeSegment[0] - 2;
            }
        } else {
            indexOppositeEdge = -1; // error
        }
        return indexOppositeEdge;
    } // end setEdgeOposite()

    public int getEdgeOposite() {
        return indexOppositeEdge;
    }

    public int getEdgeCount() {
        return numberOfEdgeSegments;
    }
    /*
    public int setTileRotationVector()
    {
    double len = 2000.0;
    double lenTemp = 2000.0;
    int selectedSegmentForRotation = 0;

    for(int indexSegment=0; indexSegment<segmentCount; indexSegment++)
    {
    lenTemp = tileSegment[indexSegment].getActiveWireLength();
    if(lenTemp < len)
    {
    len = lenTemp;
    selectedSegmentForRotation = indexSegment;
    }
    }

    // getshortestWire();
    rotationVector = tileSegment[selectedSegmentForRotation].getActiveWireAngle(id);
    return rotationVector;

    }
     */

    // process corners first, edges second and remaining last.
    // corners(numberOfEdgeSegments==2) only connect to numberOfEdgeSegments=1 and nextToEdgeSegments[2]
    // edgesTiles only connect nextToEdgeSegments[2] to other nextToEdgeSegments[2] (corners done in step above)
    // edgeTiles only connect edgeOppositeSegment to numberOfEdgeSegments=0 Tiles
    public int checkForEdgeSegments() {
        // count each edge segment of each Tile
        // for eachTile on init
        // for tiles[eachTile].eachsegment
        // if edge, Tile.numberOfEdgeSegments++

        // two pass check for edge segments and update nextToEdgeSegments[]
        int indexSegment = 0;
        int indexSegmentTemp = 0;
        for (indexSegment = 0; indexSegment < segmentCount; indexSegment++) {
            if (tileSegment[indexSegment].colour == edgeColorIndex) {
                indexOfEdgeSegment[numberOfEdgeSegments] = indexSegment;
                numberOfEdgeSegments++;

                if ((Trace.debugLevel & Trace.DEBUG_SEGMENT) == Trace.DEBUG_SEGMENT) {
                    System.out.printf("checkForEdgeSegments numberOfEdgeSegments = %d\n", numberOfEdgeSegments);
                }

                if (indexSegment < 3) {
                    if (tileSegment[indexSegment + 1].colour != edgeColorIndex) {
                        nextToEdgeSegments[indexSegmentTemp] = indexSegment + 1;
                        indexSegmentTemp++;
                    }
                } else {
                    if (tileSegment[0].colour != edgeColorIndex) {
                        nextToEdgeSegments[indexSegmentTemp] = 0;
                        indexSegmentTemp++;
                    }
                }
            }
        }
        // second pass in revers order
        for (indexSegment = 3; indexSegment >= 0; indexSegment--) {
            if (tileSegment[indexSegment].colour == edgeColorIndex) {
                if (indexSegment > 0) {
                    if (tileSegment[indexSegment - 1].colour != edgeColorIndex) {
                        nextToEdgeSegments[indexSegmentTemp] = indexSegment - 1;
                        indexSegmentTemp++;
                    }
                } else {
                    if (tileSegment[3].colour != edgeColorIndex) {
                        nextToEdgeSegments[indexSegmentTemp] = 3;
                        indexSegmentTemp++;
                    }
                }
            }
        }

        // if the tile contains a single edge segment, set the index or the oposite segment for later
        if (numberOfEdgeSegments == 1) {
            if (setEdgeOposite() < 0) {
                System.out.printf("**** SWFM - setEdgeOposite() Failed with -1\n");
            }
        }

        return numberOfEdgeSegments;
    } // end checkForEdgeSegments()
} // end Tile class

