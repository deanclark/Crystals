package com.crystalwink.crystals.TilePrintableAwt;

import com.crystalwink.crystalscommon.Tile.Tile;

import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * Tile defines the basic tile representative of the eternatyII tile with AWT Graphics Print capability.
 *
 * @author Dean Clark deancl
 */
public class TilePrintableAwt extends Tile {

    public TilePrintableAwt(int tileId, int colours[], int segmentScale) {
        super(tileId, colours, segmentScale);  // Tile constructor
    }
    
    public void PrintTile(Graphics graphics, Color palate[], boolean showTileIdCheckbox, boolean drawRotation, boolean asWireframeTile) {
        for (int segment = 0; segment < tileSegment[0].segmentPoints; segment++) {
            // if tile location has moved, update the print

            if (moved()) {
                //System.out.printf("Tile %d moved xWorldSpace=%d yWorldSpace=%d\n", id, (int)xWorldSpace, (int)yWorldSpace);
                for (int pointsIndex = 0; pointsIndex < tileSegment[0].segmentPoints; pointsIndex++) {
                    // update print poly array
                    tileSegment[segment].xPointArrayPrint[pointsIndex] = tileSegment[getOrientation(segment)].xPointArray[pointsIndex] + (int) xWorldSpace;
                    tileSegment[segment].yPointArrayPrint[pointsIndex] = tileSegment[getOrientation(segment)].yPointArray[pointsIndex] + (int) yWorldSpace;
//                    tileSegment[segment].xPointArrayPrint[pointsIndex] = tileSegment[segment].xPointArray[pointsIndex] + (int)xWorldSpace;
//                    tileSegment[segment].yPointArrayPrint[pointsIndex] = tileSegment[segment].yPointArray[pointsIndex] + (int)yWorldSpace;
                }

            // prevent duplication translation
            }

            if (drawRotation) {
//                rotationZ(tileSegment[segment], this);
            }

//            System.out.printf("Tile print segment = %d\n", segment);            //graphics.setColor((n == pick) ? selectColor : (n.locked == Tile.LOCKED ? lockedColor : nodeColor));
            graphics.setColor(palate[tileSegment[segment].colour]);
            // comment the following for wireframe
            if (asWireframeTile) {
                graphics.drawPolygon(tileSegment[segment].xPointArrayPrint, tileSegment[segment].yPointArrayPrint, tileSegment[segment].points);
            } else {
                if (locked()) {
                    graphics.fillPolygon(tileSegment[segment].xPointArrayPrint, tileSegment[segment].yPointArrayPrint, tileSegment[segment].points);
                    graphics.setColor(Color.RED);
                    graphics.drawPolygon(tileSegment[segment].xPointArrayPrint, tileSegment[segment].yPointArrayPrint, tileSegment[segment].points);
                } else // show unlocked tiles
                //                    graphics.drawPolygon(tileSegment[segment].xPointArrayPrint, tileSegment[segment].yPointArrayPrint, tileSegment[segment].points);
                {
                    graphics.fillPolygon(tileSegment[segment].xPointArrayPrint, tileSegment[segment].yPointArrayPrint, tileSegment[segment].points);
                }

//            graphics.setColor(Color.black);
            }
        }

        if (showTileIdCheckbox) {
            String stTileId = String.valueOf(getId());
            //String stTileId = String.valueOf(rotationVector);   //
            if (locked()) {
                graphics.setColor(Color.RED);
            } else {
                graphics.setColor(Color.BLACK);
            }

            graphics.drawString(stTileId, (int) xWorldSpace - 7, (int) yWorldSpace + 4);
        }
        moved(false);
    }
    
}