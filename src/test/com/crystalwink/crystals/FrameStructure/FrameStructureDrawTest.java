package test.com.crystalwink.crystals.FrameStructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.Test;

import com.crystalwink.crystals.FrameStructure.FrameStructure;

public class FrameStructureDrawTest {

    private static final int SIZE = 40;

    private static Color[] palate() {
        Color[] palate = new Color[23];
        for (int index = 0; index < palate.length; index++) {
            palate[index] = new Color(200 - index * 5, 20 + index * 9, 60 + index * 3);
        }
        return palate;
    }

    private static BufferedImage draw(boolean wireframe) {
        BufferedImage image = new BufferedImage(SIZE + 10, SIZE + 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        FrameStructure.drawCell(graphics, palate(), new int[] {1, 2, 3, 4}, 5, 5, SIZE, wireframe);
        graphics.dispose();
        return image;
    }

    @Test
    public void filledTileColoursEachSegment() {
        BufferedImage image = draw(false);
        Color[] palate = palate();

        // a point well inside each triangle: top, right, bottom, left
        assertEquals(palate[1].getRGB(), image.getRGB(5 + SIZE / 2, 5 + SIZE / 5));
        assertEquals(palate[2].getRGB(), image.getRGB(5 + SIZE - SIZE / 5, 5 + SIZE / 2));
        assertEquals(palate[3].getRGB(), image.getRGB(5 + SIZE / 2, 5 + SIZE - SIZE / 5));
        assertEquals(palate[4].getRGB(), image.getRGB(5 + SIZE / 5, 5 + SIZE / 2));
    }

    @Test
    public void wireframeTileLeavesTheSegmentInteriorsEmpty() {
        BufferedImage image = draw(true);

        assertEquals(Color.WHITE.getRGB(), image.getRGB(5 + SIZE / 2, 5 + SIZE / 5));
        assertEquals(Color.WHITE.getRGB(), image.getRGB(5 + SIZE - SIZE / 5, 5 + SIZE / 2));

        // ...but the segment edges running from the corners to the centre are drawn in the segment colour
        assertNotEquals(Color.WHITE.getRGB(), image.getRGB(5 + SIZE / 4, 5 + SIZE / 4));
    }

    @Test
    public void cellWithNoAssignedEdgesIsNotDrawn() {
        BufferedImage image = new BufferedImage(SIZE + 10, SIZE + 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        FrameStructure.drawCell(graphics, palate(), new int[] {-1, -1, -1, -1}, 5, 5, SIZE, false);
        graphics.dispose();

        assertEquals(Color.WHITE.getRGB(), image.getRGB(5 + SIZE / 2, 5 + SIZE / 5));
    }
}
