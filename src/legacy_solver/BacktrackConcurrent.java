package legacy_solver;

import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

public class BacktrackConcurrent implements Strategy {
    private static final int SIZE = 9;
    private final ForkJoinPool pool;

    public BacktrackConcurrent() {
        this.pool = new ForkJoinPool();
    }

    @Override
    public int[][] solve(String puzzle) {
        int[][] board = SudokuParser.parseBoard(puzzle);
        pool.invoke(new SudokuTask(board));
        return board;
    }

    private class SudokuTask extends RecursiveTask<Boolean> {
        private final int[][] board;

        public SudokuTask(int[][] board) {
            this.board = board;
        }

        @Override
        protected Boolean compute() {
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    if (board[row][col] == 0) { // Find first empty cell
                        return solveEmptyCell(row, col);
                    }
                }
            }
            return true; // Puzzle solved
        }

        private Boolean solveEmptyCell(int row, int col) {
            for (int num = 1; num <= 9; num++) {
                if (isValid(board, row, col, num)) {
                    board[row][col] = num; // Try the number

                    // Create new task for the next empty cell
                    SudokuTask nextTask = new SudokuTask(board);
                    if (nextTask.fork().join()) return true; // If solved, return true

                    board[row][col] = 0; // Backtrack
                }
            }
            return false; // No solution found
        }
    }

    private boolean isValid(int[][] board, int row, int col, int num) {
        for (int i = 0; i < SIZE; i++) {
            if (board[row][i] == num || board[i][col] == num ||
                    board[row - row % 3 + i / 3][col - col % 3 + i % 3] == num)
                return false;
        }
        return true;
    }
}
