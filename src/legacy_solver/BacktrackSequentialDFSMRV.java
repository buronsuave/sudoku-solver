package legacy_solver;

import java.util.Comparator;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class BacktrackSequentialDFSMRV implements Strategy {

    @Override
    public int[][] solve(String puzzle) {
        int[][] board = SudokuParser.parseBoard(puzzle);
        List<Cell> emptyCells = collectAndSortEmptyCells(board);
        Stack<State> stack = new Stack<>();

        if (solveWithDFS(board, emptyCells, stack)) {
            return board;
        } else {
            throw new IllegalStateException("No solution found for the given puzzle.");
        }
    }

    // DFS using a list of sorted cells
    private boolean solveWithDFS(int[][] board, List<Cell> emptyCells, Stack<State> stack) {
        stack.push(new State(0, 1)); // Start at the first empty cell with num = 1

        while (!stack.isEmpty()) {
            State state = stack.peek();
            int index = state.index;

            if (index >= emptyCells.size()) return true; // All cells solved

            Cell cell = emptyCells.get(index);
            int row = cell.row, col = cell.col;

            if (state.num > 9) {  // All numbers tried, backtrack
                board[row][col] = 0;
                stack.pop();
                continue;
            }

            if (isValid(board, row, col, state.num)) {
                board[row][col] = state.num;  // Set the number
                stack.push(new State(index + 1, 1)); // Push the next cell with num = 1
            } else {
                state.num++;  // Try the next number
            }
        }
        return false;  // No solution found
    }

    // Collect all empty cells and sort them by the number of valid candidates
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

    private boolean isValid(int[][] board, int row, int col, int num) {
        for (int i = 0; i < SudokuParser.SIZE; i++) {
            if (board[row][i] == num || board[i][col] == num ||
                    board[row - row % 3 + i / 3][col - col % 3 + i % 3] == num)
                return false;
        }
        return true;
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

    // Helper class to represent the state of a cell in DFS
    private static class State {
        int index, num;

        State(int index, int num) {
            this.index = index;
            this.num = num;
        }
    }
}
