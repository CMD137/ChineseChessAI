package ai;

import game.*;
import util.Constants;

import java.util.Arrays;

import static util.Constants.TIME_LIMIT;

public class AI {
    private Evaluator evaluator = new Evaluator();
    private long startTime;

    /**
     * 迭代加深搜索，固定为AI（小写方）走棋
     * @param board 当前棋盘状态
     * @param maxDepth 最大搜索深度
     * @return 最佳走法
     */
    public Move iterativeDeepening(Board board, int maxDepth) {
        Move bestMove = null;
        startTime = System.currentTimeMillis();

        for (int depth = 1; depth <= maxDepth; depth++) {
            if (System.currentTimeMillis() - startTime > TIME_LIMIT) {
                System.out.println("IDS搜索超时，停止于深度：" + depth);
                break;
            }

            int bestValue = -Constants.INF;
            Move[] moves = board.generateAllMoves(true);  // AI走法，小写方走法

            // 启发式排序（使用相同的 getMoveScore 方法）
            Arrays.sort(moves, (a, b) -> Integer.compare(
                    getMoveScore(board, b, true), getMoveScore(board, a, true)
            ));

            for (Move move : moves) {
                board.makeMove(move);

                //temp
                //System.out.println("模拟走子: " + move);
                //System.out.println("此时我方是否被将军: " + board.isInCheck(true));

                // 找到胜招，直接返回
                if (board.isKingDead(false)) {
                    board.undoMove(move);
                    return move;
                }


                // 检查是否走完后自己被将军（如给对方当炮架）
                if (board.isInCheck(true)) {
                    board.undoMove(move);
                    continue;
                }
                // 下一层轮到对方（大写方）走
                int value = alphaBeta(board, depth - 1, -Constants.INF, Constants.INF, false);
                board.undoMove(move);

                if (value > bestValue) {
                    bestValue = value;
                    bestMove = move;
                }
            }
        }
        return bestMove;
    }

    /**
     * Alpha-Beta 剪枝搜索（极大极小）
     * @param board
     * @param depth
     * @param alpha
     * @param beta
     * @param maximizingPlayer 当前是否为极大方（AI为极大方）
     * @return
     */
    private int alphaBeta(Board board, int depth, int alpha, int beta, boolean maximizingPlayer) {
        if (System.currentTimeMillis() - startTime > TIME_LIMIT) {
            //temp
            //System.out.println("alpha-beta搜索超时："+depth);
            int fallback = evaluator.evaluate(board,maximizingPlayer);
            if (maximizingPlayer) {
                return alpha != -Constants.INF ? alpha : fallback;
            } else {
                return beta != Constants.INF ? beta : fallback;
            }
        }

        //递归边界条件：
        if (depth == 0) {
            return evaluator.evaluate(board,maximizingPlayer);
        }


        //生成当前局面下，当前行动方的所有合法走法
        Move[] moves = board.generateAllMoves(maximizingPlayer);

        //启发排序
        Arrays.sort(moves,(a,b)->Integer.compare(
                getMoveScore(board,b,maximizingPlayer),getMoveScore(board,a,maximizingPlayer)
        ));


        //若无合法走法，则视为该方被将死（输）或无路可走（和棋）
        if (moves.length == 0) {
            return maximizingPlayer ? -Constants.INF : Constants.INF;
        }

        // 极大化分支：当前节点为AI轮次，目标是最大化估值
        if (maximizingPlayer) {
            int maxEval = -Constants.INF; // 初始化最大值为负无穷
            for (Move move : moves) {
                board.makeMove(move);
                // 检查是否导致自己暴露在将军状态
                if (board.isInCheck(true)) {
                    board.undoMove(move);
                    continue;
                }
                int eval = alphaBeta(board, depth - 1, alpha, beta, false);
                board.undoMove(move);

                // 更新最大估值
                if (eval > maxEval) maxEval = eval;
                // alpha记录当前极大方已发现的最大估值
                if (eval > alpha) alpha = eval;
                // 剪枝条件：alpha >= beta时，停止继续搜索该节点的剩余分支
                if (beta <= alpha) break;
            }
            return maxEval;
        }
        // 极小化分支：当前节点为对手轮次，目标是最小化估值
        else {
            int minEval = Constants.INF; // 初始化最小值为正无穷
            for (Move move : moves) {
                board.makeMove(move);
                // 检查是否导致自己暴露在将军状态
                if (board.isInCheck(false)) {
                    board.undoMove(move);
                    continue;
                }
                int eval = alphaBeta(board, depth - 1, alpha, beta, true);
                board.undoMove(move);

                // 更新最小估值
                if (eval < minEval) minEval = eval;
                // beta记录当前极小方已发现的最小估值
                if (eval < beta) beta = eval;
                // 剪枝条件：alpha >= beta时，停止继续搜索该节点的剩余分支
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

    //用于排序，给move打分
    private int getMoveScore(Board board, Move move,boolean isAISide) {
        int from = move.fromY * 9 + move.fromX;
        int to = move.y * 9 + move.x;

        int score = 0;

        // 1. MVV-LVA 吃子排序:低价值吃高价值优先
        byte captured = board.board[to];
        if (captured != 0) {
            int victimValue = evaluator.getPieceValue(captured);
            int attackerValue = evaluator.getPieceValue(board.board[from]);
            score += (victimValue * 10 - attackerValue);
        }

        // 2. 杀手着法（Killer Move）：某层之前反复剪枝的走法
        //if (isKillerMove(move)) score += 200;

        // 3. 历史启发（History Heuristic）：历史上这个起点-终点的表现好
        //score += historyTable[move.from][move.to]; // 二维数组记录效果

        // 4. 将军检测：走完是否能将军
        if (doesGiveCheck(board, move,isAISide)) score += 80;

        return score;
    }

    private boolean doesGiveCheck(Board board, Move move, boolean isAISide) {
        board.makeMove(move);
        boolean result = board.isInCheck(!isAISide); // 判断敌方是否被将军
        board.undoMove(move);
        return result;
    }


    public Move getRandomMove(Board board) {
        Move[] moves = board.generateAllMoves(true);
        return moves[(int)(Math.random() * moves.length)];
    }
}
