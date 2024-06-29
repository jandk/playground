package be.twofold.playground.gol;

public class MyGameOfLife implements GameOfLife {
    private Board tempBoard;
    private Board board;
    private final int cols;
    private final int rows;

    public MyGameOfLife(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.tempBoard = new Board(rows + 2, cols + 2);
        this.board = new Board(rows + 2, cols + 2);
    }

    @Override
    public void setAlive(int row, int col) {
        board.set(row + 1, col + 1);
    }

    @Override
    public int countAlive() {
        int count = 0;
        for (int r = 1; r < rows + 1; r++) {
            for (int c = 1; c < cols + 1; c++) {
                if (board.get(r, c)) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public boolean[][] getBoard() {
        boolean[][] result = new boolean[rows][cols];
        for (int r = 1; r < rows + 1; r++) {
            for (int c = 1; c < cols + 1; c++) {
                result[r - 1][c - 1] = board.get(r, c);
            }
        }
        return result;
    }

    @Override
    public void evolve(int generations) {
        for (int i = 0; i < generations; i++) {
            for (int r = 1; r < rows + 1; r++) {
                for (int c = 1; c < cols + 1; c++) {
                    int aliveNeighbours = countAliveNeighbours(r, c);
                    if (board.get(r, c) && (aliveNeighbours == 2 || aliveNeighbours == 3) || !board.get(r, c) && aliveNeighbours == 3) {
                        tempBoard.set(r, c);
                    } else {
                        tempBoard.clear(r, c);
                    }
                }
            }

            Board temp = board;
            board = tempBoard;
            tempBoard = temp;
        }
    }

    private int countAliveNeighbours(int row, int col) {
        int count = 0;
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                if (board.get(r, c) && (r != row || c != col)) {
                    count++;
                }
            }
        }
        return count;
    }

    private static final class Board {
        private final boolean[] board;
        private final int stride;

        private Board(int rows, int cols) {
            this.stride = cols;
            this.board = new boolean[cols * rows];
        }

        private boolean get(int row, int col) {
            return board[row * stride + col];
        }

        private void set(int row, int col) {
            board[row * stride + col] = true;
        }

        private void clear(int row, int col) {
            board[row * stride + col] = false;
        }
    }
}
