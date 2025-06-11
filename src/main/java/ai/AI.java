package ai;

import game.*;
import util.Constants;

import java.util.Arrays;
import java.util.Comparator;

public class AI {
    public Move iterativeDeepening(Board board, int maxDepth) {
        /*if (maxDepth <= 0) return null;
        Move bestMove = null;
        int bestValue = -Constants.INF;


        for (int depth = 1; depth <= maxDepth; depth++) {

        return bestMove;*/
        return null;
    }

    public Move getRandomMove(Board board){
        Move[] moves = board.generateMoves(false);
        return moves[(int)(Math.random() * moves.length)];
    }


}