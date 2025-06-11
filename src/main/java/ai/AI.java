package ai;

import game.*;
import util.Constants;

import java.util.Arrays;
import java.util.Comparator;

public class AI {
    private TranspositionTable transpositionTable = new TranspositionTable();
    private KillerMoves killerMoves = new KillerMoves();
    private HistoryTable historyTable = new HistoryTable();

    public Move iterativeDeepening(Board board, int maxDepth) {
        if (maxDepth <= 0) return null;
        Move bestMove = null;
        int bestValue = -Constants.INF;


        for (int depth = 1; depth <= maxDepth; depth++) {
            int[] result = alphaBeta(board, depth, -Constants.INF, Constants.INF, true);
            bestValue = result[0];
            bestMove = result[1] >= 0 ? board.generateMoves(false)[result[1]] : null;
            System.out.println("Depth: " + depth + ", bestValue: " + bestValue + ", bestMoveIndex: " + result[1]); // 调试
            if (bestMove != null) { // 修复：一旦找到有效移动，提前返回
                break;
            }
            if (bestValue > 9000) {
                break;
            }
        }
        return bestMove;
    }


    private int[] alphaBeta(Board board, int depth, int alpha, int beta, boolean maximizingPlayer) {
        long zobrist = board.getZobristHash();
        TranspositionTable.TTEntry ttEntry = transpositionTable.get(zobrist);
        if (ttEntry != null && ttEntry.depth >= depth) {
            if (ttEntry.flag == TranspositionTable.TTEntry.EXACT ||
                    (ttEntry.flag == TranspositionTable.TTEntry.LOWER && ttEntry.value >= beta) ||
                    (ttEntry.flag == TranspositionTable.TTEntry.UPPER && ttEntry.value <= alpha)) {
                return new int[]{ttEntry.value, ttEntry.bestMoveIndex};
            }
        }

        Move[] moves = orderMoves(board, depth);
        if (moves.length == 0) { // 修复：处理无合法移动
            return new int[]{board.evaluate(), -1};
        }

        if (depth == 0) {
            int score = quiescenceSearch(board, alpha, beta);
            return new int[]{score, -1};
        }

        int bestMoveIndex = -1;
        int bestValue = maximizingPlayer ? -Constants.INF : Constants.INF;

        for (int i = 0; i < moves.length; i++) {
            Move move = moves[i];
            board.makeMove(move);
            int[] childResult = alphaBeta(board, depth - 1, alpha, beta, !maximizingPlayer);
            int value = childResult[0];
            board.undoMove(move);

            if (maximizingPlayer) {
                if (value > bestValue) {
                    bestValue = value;
                    bestMoveIndex = i;
                    alpha = Math.max(alpha, bestValue);
                }
                if (beta <= alpha) {
                    killerMoves.addKillerMove(depth, move);
                    historyTable.updateHistory(move, depth);
                    break; // Beta截断
                }
            } else {
                if (value < bestValue) {
                    bestValue = value;
                    bestMoveIndex = i;
                    beta = Math.min(beta, bestValue);
                }
                if (beta <= alpha) {
                    killerMoves.addKillerMove(depth, move);
                    historyTable.updateHistory(move, depth);
                    break; // Alpha截断
                }
            }
        }

        int flag = (bestValue <= alpha) ? TranspositionTable.TTEntry.UPPER :
                (bestValue >= beta) ? TranspositionTable.TTEntry.LOWER : TranspositionTable.TTEntry.EXACT;
        transpositionTable.store(zobrist, bestValue, depth, flag, bestMoveIndex, bestMoveIndex >= 0 ? moves[bestMoveIndex] : null);

        return new int[]{bestValue, bestMoveIndex};
    }

    private int quiescenceSearch(Board board, int alpha, int beta) {
        int standPat = board.evaluate();
        if (standPat >= beta) {
            return beta;
        }
        if (standPat > alpha) {
            alpha = standPat;
        }

        Move[] captureMoves = board.generateMoves(true);
        if (captureMoves.length == 0) return standPat; // 修复：无吃子移动时返回评估值

        for (Move move : captureMoves) {
            board.makeMove(move);
            int score = -quiescenceSearch(board, -beta, -alpha);
            board.undoMove(move);

            if (score >= beta) {
                return beta;
            }
            if (score > alpha) {
                alpha = score;
            }
        }
        return alpha;
    }

    private Move[] orderMoves(Board board, int depth) {
        Move[] moves = board.generateMoves(false);
        if (moves.length == 0) return moves;

        TranspositionTable.TTEntry ttEntry = transpositionTable.get(board.getZobristHash());
        Move ttMove = ttEntry != null ? ttEntry.bestMove : null;
        Move[] killers = killerMoves.getKillerMoves(depth);
        int[] historyScores = historyTable.getHistoryScores(moves);

        Integer[] indices = new Integer[moves.length];
        for (int i = 0; i < moves.length; i++) indices[i] = i;

        Arrays.sort(indices, (i, j) -> {
            Move moveI = moves[i];
            Move moveJ = moves[j];

            if (ttMove != null) {
                if (moveI.equals(ttMove)) return -1;
                if (moveJ.equals(ttMove)) return 1;
            }

            boolean isKillerI = false, isKillerJ = false;
            for (Move killer : killers) {
                if (killer != null) {
                    if (moveI.equals(killer)) isKillerI = true;
                    if (moveJ.equals(killer)) isKillerJ = true;
                }
            }
            if (isKillerI && !isKillerJ) return -1;
            if (isKillerJ && !isKillerI) return 1;

            boolean isCaptureI = moveI.captured != null;
            boolean isCaptureJ = moveJ.captured != null;
            if (isCaptureI && !isCaptureJ) return -1;
            if (isCaptureJ && !isCaptureI) return 1;

            return Integer.compare(historyScores[j], historyScores[i]);
        });

        Move[] sortedMoves = new Move[moves.length];
        for (int i = 0; i < moves.length; i++) {
            sortedMoves[i] = moves[indices[i]];
        }

        return sortedMoves;
    }
}