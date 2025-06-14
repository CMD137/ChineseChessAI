package ai;

import game.*;
import util.Constants;

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

            //temp
            System.out.println("MAX"+maxDepth);
            System.out.println("IDS"+depth);


            int bestValue = -Constants.INF;
            Move[] moves = board.generateAllMoves(true);  // AI走法，小写方走法

            for (Move move : moves) {
                board.makeMove(move);
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
            System.out.println("alpha-beta搜索超时："+depth);
            int fallback = evaluator.evaluate(board);
            if (maximizingPlayer) {
                return alpha != -Constants.INF ? alpha : fallback;
            } else {
                return beta != Constants.INF ? beta : fallback;
            }
        }

        //递归边界条件：
        if (depth == 0) {
            return evaluator.evaluate(board);
        }


        //生成当前局面下，当前行动方的所有合法走法
        Move[] moves = board.generateAllMoves(maximizingPlayer);

        //若无合法走法，则视为该方被将死（输）或无路可走（和棋）
        //返回极端分数，表明输赢局面
        if (moves.length == 0) {
            //如果当前是极大方（AI），无路可走即输，返回极小值
            //否则对手无路可走，视为AI赢，返回极大值
            return maximizingPlayer ? -Constants.INF : Constants.INF;
        }

        // 极大化分支：当前节点为AI轮次，目标是最大化估值
        if (maximizingPlayer) {
            int maxEval = -Constants.INF; // 初始化最大值为负无穷
            for (Move move : moves) {
                board.makeMove(move); // 执行当前走法
                // 递归调用alphaBeta搜索下一层，对手轮次为极小化分支
                int eval = alphaBeta(board, depth - 1, alpha, beta, false);
                board.undoMove(move); // 撤销走法，恢复局面

                // 更新最大估值
                if (eval > maxEval) maxEval = eval;
                // alpha记录当前极大方已发现的最大估值
                if (eval > alpha) alpha = eval;
                // 剪枝条件：alpha >= beta时，停止继续搜索该节点的剩余分支
                if (beta <= alpha) break;
            }
            return maxEval; // 返回该节点的最大估值
        }
        // 极小化分支：当前节点为对手轮次，目标是最小化估值（对AI而言）
        else {
            int minEval = Constants.INF; // 初始化最小值为正无穷
            for (Move move : moves) {
                board.makeMove(move); // 执行当前走法
                // 递归调用alphaBeta搜索下一层，AI轮次为极大化分支
                int eval = alphaBeta(board, depth - 1, alpha, beta, true);
                board.undoMove(move); // 撤销走法，恢复局面

                // 更新最小估值
                if (eval < minEval) minEval = eval;
                // beta记录当前极小方已发现的最小估值
                if (eval < beta) beta = eval;
                // 剪枝条件：alpha >= beta时，停止继续搜索该节点的剩余分支
                if (beta <= alpha) break;
            }
            return minEval; // 返回该节点的最小估值
        }
    }


    public Move getRandomMove(Board board) {
        Move[] moves = board.generateAllMoves(true);
        return moves[(int)(Math.random() * moves.length)];
    }
}
