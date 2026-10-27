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
 * # tile-id column row orientation top right bottom left      (edge colours as the board shows them)
 * 12 0 0 3 0 5 7 0
 * ...
 * # tile ids by cell (. = empty)
 * #  12  45 ...
 * </pre>
 */
final class BoardRecord {
    /** One placed tile. */
    static final class Entry {
        final int tileId;
        final int column;
        final int row;
        final int orientation;
        final int[] colours;   // top, right, bottom, left

        Entry(int tileId, int column, int row, int orientation, int[] colours) {
            this.tileId = tileId;
            this.column = column;
            this.row = row;
            this.orientation = orientation;
            this.colours = colours;
        }
    }

    final int tiles;
    final int columns;
    final int rows;
    final List<Entry> entries;

    BoardRecord(int tiles, int columns, int rows, List<Entry> entries) {
        this.tiles = tiles;
        this.columns = columns;
        this.rows = rows;
        this.entries = entries;
    }

    int placed() {
        return entries.size();
    }

    String format(LocalDateTime when) {
        StringBuilder text = new StringBuilder();
        text.append("# Crystals board\n");
        text.append("tiles=").append(tiles).append('\n');
        text.append("columns=").append(columns).append('\n');
        text.append("rows=").append(rows).append('\n');
        text.append("placed=").append(placed()).append('\n');
        text.append("saved=").append(when.truncatedTo(java.time.temporal.ChronoUnit.SECONDS)).append('\n');
        text.append("# tile-id column row orientation top right bottom left\n");
        int[][] idAt = new int[rows][columns];
        for (int[] line : idAt) {
            java.util.Arrays.fill(line, -1);
        }
        for (Entry entry : entries) {
            text.append(entry.tileId).append(' ').append(entry.column).append(' ').append(entry.row).append(' ')
                    .append(entry.orientation);
            for (int colour : entry.colours) {
                text.append(' ').append(colour);
            }
            text.append('\n');
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

    static BoardRecord parse(String text) {
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
                if (pair[0].equals("tiles")) {
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
            entries.add(new Entry(Integer.parseInt(field[0]), Integer.parseInt(field[1]), Integer.parseInt(field[2]),
                    Integer.parseInt(field[3]), colours));
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
        return new BoardRecord(tiles, columns, rows, entries);
    }

    static BoardRecord read(Path file) throws IOException {
        return parse(new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
    }

    /** Writes best-&lt;placed&gt;-&lt;date and time&gt;.txt in the directory (created if need be) and returns the file. */
    Path save(Path directory) throws IOException {
        Files.createDirectories(directory);
        LocalDateTime now = LocalDateTime.now();
        Path file = directory.resolve("best-" + placed() + "-" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".txt");
        for (int suffix = 2; Files.exists(file); suffix++) {
            file = directory.resolve("best-" + placed() + "-" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + "-" + suffix + ".txt");
        }
        Files.write(file, format(now).getBytes(StandardCharsets.UTF_8));
        return file;
    }

    /** The most tiles placed in any best-*.txt already in the directory (0 if none): only better boards are worth saving. */
    static int bestSaved(Path directory) {
        int best = 0;
        if (!Files.isDirectory(directory)) {
            return 0;
        }
        try (DirectoryStream<Path> files = Files.newDirectoryStream(directory, "best-*.txt")) {
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
