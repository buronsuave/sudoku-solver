package legacy_solver;

import java.util.Stack;

public class BacktrackSequentialDFS implements Strategy {

    @Override
    public int[][] solve(String puzzle) {
        int[][] board = SudokuParser.parseBoard(puzzle);
        Stack<State> stack = new Stack<>();
        if (solveWithDFS(board, stack)) {
            return board;
        } else {
            throw new IllegalStateException("No solution found for the given puzzle.");
        }
    }

    private boolean solveWithDFS(int[][] board, Stack<State> stack) {
        stack.push(findNextEmpty(board, 0, 0));  // Start with the first empty cell

        while (!stack.isEmpty()) {
            State state = stack.peek();
            int row = state.row;
            int col = state.col;

            if (state.num > 9) {  // All numbers tried, backtrack
                board[row][col] = 0;
                stack.pop();
                continue;
            }

            if (isValid(board, row, col, state.num)) {
                board[row][col] = state.num;  // Set the number
                State nextState = findNextEmpty(board, row, col);
                if (nextState == null) return true;  // Puzzle solved
                stack.push(nextState);
            } else {
                state.num++;  // Try the next number
            }
        }
        return false;  // No solution found
    }

    private State findNextEmpty(int[][] board, int startRow, int startCol) {
        for (int row = startRow; row < SudokuParser.SIZE; row++) {
            for (int col = (row == startRow ? startCol : 0); col < SudokuParser.SIZE; col++) {
                if (board[row][col] == 0) {
                    return new State(row, col, 1);  // Start with num = 1
                }
            }
        }
        return null;  // No empty cells left
    }

    private boolean isValid(int[][] board, int row, int col, int num) {
        for (int i = 0; i < SudokuParser.SIZE; i++) {
            if (board[row][i] == num || board[i][col] == num ||
                    board[row - row % 3 + i / 3][col - col % 3 + i % 3] == num)
                return false;
        }
        return true;
    }

    // Helper class to represent the state of a cell
    private static class State {
        int row, col, num;

        State(int row, int col, int num) {
            this.row = row;
            this.col = col;
            this.num = num;
        }
    }
}

