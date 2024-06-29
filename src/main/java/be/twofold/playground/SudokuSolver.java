package be.twofold.playground;

public class SudokuSolver {

    public static final int GridSize = 9;

    public static void main(String[] args) {
        Sudoku sudoku = new Sudoku(new int[][]{
            {7, 0, 2, 0, 5, 0, 6, 0, 0},
            {0, 0, 0, 0, 0, 3, 0, 0, 0},
            {1, 0, 0, 0, 0, 9, 5, 0, 0},
            {8, 0, 0, 0, 0, 0, 0, 9, 0},
            {0, 4, 3, 0, 0, 0, 7, 5, 0},
            {0, 9, 0, 0, 0, 0, 0, 0, 8},
            {0, 0, 9, 7, 0, 0, 0, 0, 5},
            {0, 0, 0, 2, 0, 0, 0, 0, 0},
            {0, 0, 7, 0, 4, 0, 2, 0, 3}
        });

        System.out.println(sudoku);

        if (sudoku.solve()) {
            System.out.println("Solved successfully!");
        } else {
            System.out.println("Unsolvable board :(");
        }

        System.out.println(sudoku);
    }

}
