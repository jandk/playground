package be.twofold.playground;

record Sudoku(int[][] board) {
    private boolean isNumberInRow(int number, int row) {
        for (int i = 0; i < SudokuSolver.GridSize; i++) {
            if (board[row][i] == number) {
                return true;
            }
        }
        return false;
    }

    private boolean isNumberInCol(int number, int col) {
        for (int i = 0; i < SudokuSolver.GridSize; i++) {
            if (board[i][col] == number) {
                return true;
            }
        }
        return false;
    }

    private boolean isNumberInBox(int number, int row, int col) {
        int localBoxRow = row - row % 3;
        int localBoxCol = col - col % 3;

        for (int i = localBoxRow; i < localBoxRow + 3; i++) {
            for (int j = localBoxCol; j < localBoxCol + 3; j++) {
                if (board[i][j] == number) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValidPlacement(int number, int row, int col) {
        return !isNumberInRow(number, row)
            && !isNumberInCol(number, col)
            && !isNumberInBox(number, row, col);
    }

    public boolean solve() {
        for (int row = 0; row < SudokuSolver.GridSize; row++) {
            for (int col = 0; col < SudokuSolver.GridSize; col++) {
                if (board[row][col] == 0) {
                    for (int numberToTry = 1; numberToTry <= SudokuSolver.GridSize; numberToTry++) {
                        if (isValidPlacement(numberToTry, row, col)) {
                            board[row][col] = numberToTry;

                            if (solve()) {
                                return true;
                            } else {
                                board[row][col] = 0;
                            }
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int row = 0; row < SudokuSolver.GridSize; row++) {
            if (row % 3 == 0 && row != 0) {
                builder.append("-----------\n");
            }
            for (int col = 0; col < SudokuSolver.GridSize; col++) {
                if (col % 3 == 0 && col != 0) {
                    builder.append('|');
                }
                builder.append(board[row][col]);
            }
            builder.append('\n');
        }
        return builder.toString();
    }
}
