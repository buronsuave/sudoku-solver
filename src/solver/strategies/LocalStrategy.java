package solver.strategies;

import solver.com.Board;

public interface LocalStrategy {

    boolean perform(Board board, int[] cellIndexes);
}
