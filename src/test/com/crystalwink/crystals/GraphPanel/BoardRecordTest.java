package com.crystalwink.crystals.GraphPanel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class BoardRecordTest {
    private static BoardRecord sample() {
        List<BoardRecord.Entry> entries = new ArrayList<>();
        entries.add(new BoardRecord.Entry(12, 0, 0, 3, new int[] {0, 5, 7, 0}));
        entries.add(new BoardRecord.Entry(200, 1, 0, 0, new int[] {0, 2, 9, 5}));
        entries.add(new BoardRecord.Entry(7, 0, 1, 2, new int[] {7, 1, 0, 0}));
        return new BoardRecord(4, 2, 2, entries);
    }

    @Test
    public void formattedBoardParsesBackToTheSameTilesCellsAndTurns() {
        BoardRecord original = sample();
        BoardRecord copy = BoardRecord.parse(original.format(LocalDateTime.of(2026, 10, 27, 15, 30, 12)));
        assertEquals(4, copy.tiles);
        assertEquals(2, copy.columns);
        assertEquals(2, copy.rows);
        assertEquals(3, copy.placed());
        for (int i = 0; i < 3; i++) {
            BoardRecord.Entry a = original.entries.get(i);
            BoardRecord.Entry b = copy.entries.get(i);
            assertEquals(a.tileId, b.tileId);
            assertEquals(a.column, b.column);
            assertEquals(a.row, b.row);
            assertEquals(a.orientation, b.orientation);
            assertArrayEquals(a.colours, b.colours);
        }
    }

    @Test
    public void textShowsTheCellGridForReading() {
        String text = sample().format(LocalDateTime.of(2026, 10, 27, 15, 30, 12));
        assertTrue(text, text.contains("placed=3"));
        assertTrue(text, text.contains("saved=2026-10-27T15:30:12"));
        assertTrue(text, text.contains("#   12  200"));
        assertTrue(text, text.contains("#    7    ."));
    }

    @Test
    public void saveWritesANewFilePerCallAndBestSavedFindsTheLargest() throws Exception {
        Path directory = Files.createTempDirectory("boards").resolve("nested");
        assertEquals(0, BoardRecord.bestSaved(directory));
        Path first = sample().save(directory);
        Path second = sample().save(directory);   // same second: must not overwrite
        assertTrue(!first.equals(second));
        assertTrue(Files.exists(first) && Files.exists(second));
        assertEquals(3, BoardRecord.bestSaved(directory));
        assertEquals(3, BoardRecord.read(first).placed());
        Files.write(directory.resolve("best-250-20260101-000000.txt"), new byte[0]);
        Files.write(directory.resolve("best-notanumber.txt"), new byte[0]);
        assertEquals(250, BoardRecord.bestSaved(directory));
    }

    @Test
    public void damagedFilesAreRejected() {
        String[] bad = {
            "columns=2\nrows=2\n1 0 0 0\n",                               // no tile count
            "tiles=4\ncolumns=2\nrows=2\n1 5 0 0\n",                      // column outside the board
            "tiles=4\ncolumns=2\nrows=2\n1 0 0 4\n",                      // no such orientation
            "tiles=4\ncolumns=2\nrows=2\n1 0\n",                          // short line
        };
        for (String text : bad) {
            try {
                BoardRecord.parse(text);
                fail("accepted: " + text);
            } catch (IllegalArgumentException expected) {
                // fine
            }
        }
    }
}
