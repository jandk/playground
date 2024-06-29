package be.twofold.playground.legsolver;

import java.util.*;

public final class Board {
    private static final char Block = '·';

    private final int width;
    private final int height;
    private final char[] cells;

    public Board(int width, int height) {
        if (width < 1) {
            throw new IllegalArgumentException("width must be positive");
        }
        if (height < 1) {
            throw new IllegalArgumentException("height must be positive");
        }
        this.width = width;
        this.height = height;
        this.cells = new char[width * height];
        Arrays.fill(this.cells, ' ');
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public void block(int x, int y, int width, int height) {
        for (int yy = y; yy < height + y; yy++) {
            for (int xx = x; xx < width + x; xx++) {
                cells[yy * this.width + xx] = Block;
            }
        }
    }

    public void fillFrom(List<String> board) {
        for (int y = 0; y < board.size(); y++) {
            String row = board.get(y);
            for (int x = 0; x < row.length(); x++) {
                char c = row.charAt(x);
                if (c != ' ' && c != Block) {
                    throw new IllegalArgumentException("board must only contain spaces and " + Block);
                }
                cells[y * width + x] = c;
            }
        }
    }

    public void fill(String s, Position position) {
        int dx = position.direction().dx();
        int dy = position.direction().dy();
        int index = position.y() * this.width + position.x();
        for (int i = 0; i < s.length(); i++) {
            int ci = index + i * dy * width + i * dx;
            if (cells[ci] == Block) {
                throw new IllegalArgumentException();
            }
            cells[ci] = s.charAt(i);
        }
    }

    public List<Position> analyzePossiblePositions() {
        List<Position> positions = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            int start = -1;
            for (int x = 0; x < width; x++) {
                char c = cells[y * width + x];
                if (c == Block) {
                    if (start != -1) {
                        int length = x - start;
                        if (length > 1) {
                            positions.add(new Position(start, y, length, Direction.Horizontal));
                        }
                        start = -1;
                    }
                } else if (start == -1) {
                    start = x;
                }
            }
            if (start != -1) {
                int length = width - start;
                if (length > 1) {
                    positions.add(new Position(start, y, length, Direction.Horizontal));
                }
            }
        }
        for (int x = 0; x < width; x++) {
            int start = -1;
            for (int y = 0; y < height; y++) {
                char c = cells[y * width + x];
                if (c == Block) {
                    if (start != -1) {
                        int length = y - start;
                        if (length > 1) {
                            positions.add(new Position(x, start, length, Direction.Vertical));
                        }
                        start = -1;
                    }
                } else if (start == -1) {
                    start = y;
                }
            }
            if (start != -1) {
                int length = height - start;
                if (length > 1) {
                    positions.add(new Position(x, start, length, Direction.Vertical));
                }
            }
        }
        return positions;
    }

    public boolean testIfWordFits(String word, Position position) {
        int dx = position.direction().dx();
        int dy = position.direction().dy();
        int index = position.y() * this.width + position.x();
        for (int i = 0; i < word.length(); i++) {
            int ci = index + i * dy * width + i * dx;
            if (cells[ci] != ' ' && cells[ci] != word.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public boolean testIfPositionIncomplete(Position position) {
        int dx = position.direction().dx();
        int dy = position.direction().dy();
        int index = position.y() * this.width + position.x();
        int count = 0;
        for (int i = 0; i < position.length(); i++) {
            int ci = index + i * dy * width + i * dx;
            if (cells[ci] == ' ') {
                count++;
            }
        }
        return count > 0 && count < position.length();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < height; y++) {
            sb.append(cells, y * width, width);
            sb.append('\n');
        }
        return sb.toString();
    }
}
