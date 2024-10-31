package legacy_solver;

public class SudokuParser {
    public static final int SIZE = 9;
    public static int[][] parseBoard(String puzzle) {
        int[][] board = new int[SIZE][SIZE];
        for (int i = 0; i < puzzle.length(); i++) {
            char ch = puzzle.charAt(i);
            board[i / SIZE][i % SIZE] = (ch == '.') ? 0 : ch - '0';
        }
        return board;
    }
}
