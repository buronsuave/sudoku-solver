package legacy_solver;

public class BacktrackSequential implements Strategy {

    @Override
    public int[][] solve(String puzzle) {
        int[][] board = SudokuParser.parseBoard(puzzle);
        solveSudoku(board, 0, 0);
        return board;
    }

    private boolean solveSudoku(int[][] board, int row, int col) {
        if (row == SudokuParser.SIZE) return true; // Solution found
        if (col == SudokuParser.SIZE) return solveSudoku(board, row + 1, 0); // Move to next row

        if (board[row][col] != 0) // Skip filled cells
            return solveSudoku(board, row, col + 1);

        for (int num = 1; num <= 9; num++) {
            if (isValid(board, row, col, num)) {
                board[row][col] = num;
                if (solveSudoku(board, row, col + 1)) return true;
                board[row][col] = 0; // Backtrack
            }
        }
        return false;
    }

    private boolean isValid(int[][] board, int row, int col, int num) {
        for (int i = 0; i < SudokuParser.SIZE; i++) {
            if (board[row][i] == num || board[i][col] == num ||
                    board[row - row % 3 + i / 3][col - col % 3 + i % 3] == num)
                return false;
        }
        return true;
    }
}
