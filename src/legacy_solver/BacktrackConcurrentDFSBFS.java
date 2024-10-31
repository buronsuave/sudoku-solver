package legacy_solver;

import java.util.*;
import java.util.concurrent.*;

public class BacktrackConcurrentDFSBFS implements Strategy {

    private static final int MAX_THREADS = 1_000_000;  // Max number of threads
    private static final int SEARCH_DEPTH = 2;  // Search depth to perform BFS
    private ForkJoinPool pool;

    public BacktrackConcurrentDFSBFS() {
        this.pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    public int[][] solve(String puzzle) {
        int[][] board = SudokuParser.parseBoard(puzzle);
        List<Cell> emptyCells = collectAndSortEmptyCells(board);  // Sort cells by # of candidates

        // Start with BFS on the first empty cell (root level of the search tree)
        Queue<PartialSolution> queue = performInitialBFS(board, emptyCells);

        try {
            // Use ForkJoinPool to solve subtrees concurrently
            pool.invoke(new SolveTask(queue, emptyCells));
        } catch (Exception e) {
            throw new IllegalStateException("No solution found for the given puzzle.", e);
        }

        return board;
    }

    // Perform BFS up to the given SEARCH_DEPTH and return a queue of partial solutions
    private Queue<PartialSolution> performInitialBFS(int[][] board, List<Cell> emptyCells) {
        Queue<PartialSolution> queue = new ConcurrentLinkedQueue<>();
        Cell firstCell = emptyCells.get(0);  // Start with the first cell

        for (int num = 1; num <= 9; num++) {
            if (isValid(board, firstCell.row, firstCell.col, num)) {
                int[][] newBoard = deepCopyBoard(board);
                newBoard[firstCell.row][firstCell.col] = num;
                queue.add(new PartialSolution(newBoard, 1));  // Push to queue with next index
            }
        }
        return queue;
    }

    // ForkJoinTask to handle concurrent solving of subtrees
    private class SolveTask extends RecursiveTask<Boolean> {
        private final Queue<PartialSolution> queue;
        private final List<Cell> emptyCells;

        public SolveTask(Queue<PartialSolution> queue, List<Cell> emptyCells) {
            this.queue = queue;
            this.emptyCells = emptyCells;
        }

        @Override
        protected Boolean compute() {
            List<SolveTask> tasks = new ArrayList<>();

            while (!queue.isEmpty()) {
                PartialSolution solution = queue.poll();
                if (solution == null) continue;

                // Perform DFS on this partial solution up to the search depth
                if (dfs(solution.board, solution.index)) {
                    return true;  // Puzzle solved
                }

                // If not solved, fork new tasks for further subtrees
                tasks.add(new SolveTask(queue, emptyCells));
            }

            // Fork all tasks and wait for completion
            return invokeAll(tasks).stream().anyMatch(ForkJoinTask::join);
        }

        // DFS from the current index onwards
        private boolean dfs(int[][] board, int index) {
            if (index >= emptyCells.size()) return true;  // Puzzle solved

            Cell cell = emptyCells.get(index);
            int row = cell.row, col = cell.col;

            for (int num = 1; num <= 9; num++) {
                if (isValid(board, row, col, num)) {
                    board[row][col] = num;
                    if (dfs(board, index + 1)) return true;  // Recurse to the next cell
                    board[row][col] = 0;  // Backtrack
                }
            }
            return false;  // No solution found
        }
    }

    // Collect all empty cells and sort them by the number of valid candidates (MRV heuristic)
    private List<Cell> collectAndSortEmptyCells(int[][] board) {
        List<Cell> emptyCells = new ArrayList<>();

        for (int row = 0; row < SudokuParser.SIZE; row++) {
            for (int col = 0; col < SudokuParser.SIZE; col++) {
                if (board[row][col] == 0) {
                    int candidates = countCandidates(board, row, col);
                    emptyCells.add(new Cell(row, col, candidates));
                }
            }
        }

        // Sort cells by the number of candidates (ascending)
        emptyCells.sort(Comparator.comparingInt(cell -> cell.candidates));
        return emptyCells;
    }

    // Count the number of valid candidates for a given cell
    private int countCandidates(int[][] board, int row, int col) {
        int count = 0;
        for (int num = 1; num <= 9; num++) {
            if (isValid(board, row, col, num)) count++;
        }
        return count;
    }

    // Check if placing a number in a cell is valid
    private boolean isValid(int[][] board, int row, int col, int num) {
        for (int i = 0; i < SudokuParser.SIZE; i++) {
            if (board[row][i] == num || board[i][col] == num ||
                    board[row - row % 3 + i / 3][col - col % 3 + i % 3] == num) {
                return false;
            }
        }
        return true;
    }

    // Helper to deep copy a board
    private int[][] deepCopyBoard(int[][] board) {
        return Arrays.stream(board).map(int[]::clone).toArray(int[][]::new);
    }

    // Helper class to represent a partial solution
    private static class PartialSolution {
        int[][] board;
        int index;

        PartialSolution(int[][] board, int index) {
            this.board = board;
            this.index = index;
        }
    }

    // Helper class to represent an empty cell with candidate count
    private static class Cell {
        int row, col, candidates;

        Cell(int row, int col, int candidates) {
            this.row = row;
            this.col = col;
            this.candidates = candidates;
        }
    }
}
