package com.crystalwink.crystals.GraphPanel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * A board worth keeping, as a plain text file: which tile sits on which cell and how it is turned. The files are meant to
 * be kept for the record (a complete board would be a solution), reloaded to seed a later run, or just read.
 *
 * <pre>
 * # Crystals board
 * tiles=256
 * columns=16
 * rows=16
 * placed=234
 * saved=2026-10-27T15:30:12
 * # tile-id column row orientation top right bottom left hint     (edge colours as the board shows them)
 * 139 8 7 1 6 6 12 18 1       (hint = 1: permanent hint tile, always listed first)
 * 12 0 0 3 0 5 7 0 0
 * ...
 * # tile ids by cell (. = empty)
 * #  12  45 ...
 * </pre>
 */
public final class BoardRecord {
    /** One placed tile. */
    public static final class Entry {
        public final int tileId;
        public final int column;
        public final int row;
        public final int orientation;
        public final int[] colours;   // top, right, bottom, left
        /** A permanent hint tile: locked in place for good when the board is loaded, and listed first in the file. */
        public final boolean hint;

        public Entry(int tileId, int column, int row, int orientation, int[] colours) {
            this(tileId, column, row, orientation, colours, false);
        }

        public Entry(int tileId, int column, int row, int orientation, int[] colours, boolean hint) {
            this.hint = hint;
            this.tileId = tileId;
            this.column = column;
            this.row = row;
            this.orientation = orientation;
            this.colours = colours;
        }
    }

    /** File name prefixes: floating-mode boards and frame-mode boards keep separate records of the best. */
    public static final String FLOATING = "best";
    public static final String FRAME = "frame";

    /** Boards with fewer than this share of the tiles are not worth a file. */
    public static double minimumFraction = 0.85;

    /** Frame mode fills without gaps but struggles to get past its hint tile, so far fewer tiles are worth a file. */
    public static int minimumFrameTiles = 110;

    public final String mode;
    public final int tiles;
    public final int columns;
    public final int rows;
    public final List<Entry> entries;

    public BoardRecord(String mode, int tiles, int columns, int rows, List<Entry> entries) {
        this.mode = mode;
        this.tiles = tiles;
        this.columns = columns;
        this.rows = rows;
        this.entries = entries;
    }

    public int placed() {
        return entries.size();
    }

    public String format(LocalDateTime when) {
        StringBuilder text = new StringBuilder();
        text.append("# Crystals board\n");
        text.append("mode=").append(mode).append('\n');
        text.append("tiles=").append(tiles).append('\n');
        text.append("columns=").append(columns).append('\n');
        text.append("rows=").append(rows).append('\n');
        text.append("placed=").append(placed()).append('\n');
        text.append("saved=").append(when.truncatedTo(java.time.temporal.ChronoUnit.SECONDS)).append('\n');
        text.append("# tile-id column row orientation top right bottom left hint\n");
        int[][] idAt = new int[rows][columns];
        for (int[] line : idAt) {
            java.util.Arrays.fill(line, -1);
        }
        List<Entry> hintsFirst = new ArrayList<>();
        for (Entry entry : entries) {
            if (entry.hint) {
                hintsFirst.add(entry);
            }
        }
        for (Entry entry : entries) {
            if (!entry.hint) {
                hintsFirst.add(entry);
            }
        }
        for (Entry entry : hintsFirst) {
            text.append(entry.tileId).append(' ').append(entry.column).append(' ').append(entry.row).append(' ')
                    .append(entry.orientation);
            for (int colour : entry.colours) {
                text.append(' ').append(colour);
            }
            text.append(' ').append(entry.hint ? 1 : 0).append('\n');
            idAt[entry.row][entry.column] = entry.tileId;
        }
        text.append("# tile ids by cell (. = empty)\n");
        for (int[] line : idAt) {
            text.append('#');
            for (int id : line) {
                text.append(String.format("%5s", id < 0 ? "." : Integer.toString(id)));
            }
            text.append('\n');
        }
        return text.toString();
    }

    public static BoardRecord parse(String text) {
        String mode = FLOATING;
        int tiles = -1;
        int columns = -1;
        int rows = -1;
        List<Entry> entries = new ArrayList<>();
        for (String raw : text.split("\\R")) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            if (line.contains("=")) {
                String[] pair = line.split("=", 2);
                if (pair[0].equals("mode")) {
                    mode = pair[1].trim();
                } else if (pair[0].equals("tiles")) {
                    tiles = Integer.parseInt(pair[1].trim());
                } else if (pair[0].equals("columns")) {
                    columns = Integer.parseInt(pair[1].trim());
                } else if (pair[0].equals("rows")) {
                    rows = Integer.parseInt(pair[1].trim());
                }
                continue;
            }
            String[] field = line.split("\\s+");
            if (field.length < 4) {
                throw new IllegalArgumentException("bad board line: " + line);
            }
            int[] colours = new int[4];
            for (int side = 0; side < 4 && 4 + side < field.length; side++) {
                colours[side] = Integer.parseInt(field[4 + side]);
            }
            boolean hint = field.length > 8 && field[8].equals("1");
            entries.add(new Entry(Integer.parseInt(field[0]), Integer.parseInt(field[1]), Integer.parseInt(field[2]),
                    Integer.parseInt(field[3]), colours, hint));
        }
        if (tiles < 0 || columns <= 0 || rows <= 0) {
            throw new IllegalArgumentException("board file lacks tiles/columns/rows");
        }
        for (Entry entry : entries) {
            if (entry.column < 0 || entry.column >= columns || entry.row < 0 || entry.row >= rows
                    || entry.orientation < 0 || entry.orientation > 3) {
                throw new IllegalArgumentException("tile " + entry.tileId + " is outside the board");
            }
        }
        return new BoardRecord(mode, tiles, columns, rows, entries);
    }

    public static BoardRecord read(Path file) throws IOException {
        return parse(new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
    }

    /** Writes &lt;prefix&gt;-&lt;placed&gt;-&lt;date and time&gt;.txt in the directory (created if need be) and returns the file. */
    public Path save(Path directory) throws IOException {
        String prefix = FRAME.equals(mode) ? FRAME : FLOATING;
        Files.createDirectories(directory);
        LocalDateTime now = LocalDateTime.now();
        String stamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path file = directory.resolve(prefix + "-" + placed() + "-" + stamp + ".txt");
        for (int suffix = 2; Files.exists(file); suffix++) {
            file = directory.resolve(prefix + "-" + placed() + "-" + stamp + "-" + suffix + ".txt");
        }
        Files.write(file, format(now).getBytes(StandardCharsets.UTF_8));
        return file;
    }

    /** The most tiles placed in any best-*.txt already in the directory (0 if none): only better boards are worth saving. */
    public static int bestSaved(Path directory) {
        return bestSaved(directory, FLOATING);
    }

    /** As above for the files with the given prefix (FLOATING or FRAME). */
    public static int bestSaved(Path directory, String prefix) {
        int best = 0;
        if (!Files.isDirectory(directory)) {
            return 0;
        }
        try (DirectoryStream<Path> files = Files.newDirectoryStream(directory, prefix + "-*.txt")) {
            for (Path file : files) {
                String[] part = file.getFileName().toString().split("-");
                try {
                    best = Math.max(best, Integer.parseInt(part[1]));
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
                    // not one of ours
                }
            }
        } catch (IOException ignored) {
            // an unreadable directory counts as empty
        }
        return best;
    }
}
