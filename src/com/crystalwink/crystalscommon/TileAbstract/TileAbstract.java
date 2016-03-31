package com.crystalwink.crystalscommon.TileAbstract;


/**
*
* Tile defines the basic 4 segment tile representative of the eternatyII tile.
*
* @author Dean Clark deancl
*/
public class TileAbstract {

   private int id;
   private String lbl;
   private int orientation = 0; // 0-(numberOfEdges-1)
   private int numberOfEdges = 4;
   //private int tileEdge[] = new int[4];  //

   public TileAbstract(int tileId, String tileLable, int edges) {
       setId(tileId);
       setLbl(tileLable);
       setOrientation(0);
       setNumberOfEdges(edges);

   }

public int getId()
{
    return id;
}

public void setId(int id)
{
    this.id = id;
}

public String getLbl()
{
    return lbl;
}

public void setLbl(String lbl)
{
    this.lbl = lbl;
}

/**
 * Orientation in 4 steps of 90 degrees, where:
 * 0 = no orientation
 * 1 = 90  degrees Clockwise
 * 2 = 180 degrees Clockwise
 * 3 = 270 degrees Clockwise
 */
public int getOrientation()
{
    return orientation;
}

/**
 * Orientation in 4 steps of 90 degrees, where:
 * 0 = no orientation
 * 1 = 90  degrees Clockwise
 * 2 = 180 degrees Clockwise
 * 3 = 270 degrees Clockwise
 * 
 * @param orientation
 */
public void setOrientation(int orientation)
{
    this.orientation = orientation;
}


/**
 * Number of segments configured as an edge colour
 * @return numberOfEdges
 */
public int getNumberOfEdges()
{
    return numberOfEdges;
}

/**
 * 
 * @param numberOfEdges
 */
public void setNumberOfEdges(int numberOfEdges)
{
    this.numberOfEdges = numberOfEdges;
}


/*
public int[] getTileEdge()
{
    return tileEdge;
}

public void setTileEdge(int[] tileEdge)
{
    this.tileEdge = tileEdge;
}
*/   
}