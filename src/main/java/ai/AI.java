package ai;

import game.*;
import util.Constants;

public class AI {
    private Evaluator evaluator = new Evaluator();

    /**
     * 迭代加深搜索（Iterative Deepening Search）
     * 从浅层开始逐步加深搜索，每一层都寻找当前最佳走法。
     * 好处是可以提前中止搜索，返回当前最优结果，适合实战使用。
     *
     * @param board 当前棋盘状态
     * @param maxDepth 最大搜索深度
     * @return 当前计算出的最佳走法
     */
    public Move iterativeDeepening(Board board, int maxDepth) {
        Move bestMove = null; // 当前最佳走法
        for (int depth = 1; depth <= maxDepth; depth++) {
            int bestValue = -Constants.INF; // 当前深度下最大值初始化为极小值
            Move[] moves = board.generateMoves(false); // 获取我方所有合法走法

            for (Move move : moves) {
                board.makeMove(move); // 执行走法
                // 对方是极小化方，传 false
                int value = alphaBeta(board, depth - 1, -Constants.INF, Constants.INF, false);
                board.undoMove(move); // 回溯走法

                // 若当前分数更优，则更新最佳走法
                if (value > bestValue) {
                    bestValue = value;
                    bestMove = move;
                }
            }
        }
        return bestMove; // 返回最优走法
    }

    /**
     * Alpha-Beta 剪枝极大极小搜索（Negamax 形式）
     * 通过剪枝减少不必要的搜索节点。
     *
     * @param board 当前棋盘状态
     * @param depth 当前递归深度
     * @param alpha 极大值下界（我方最差能接受的分数）
     * @param beta 极小值上界（对方最差能接受的分数）
     * @param maximizingPlayer 当前是否为极大方（我方）
     * @return 当前局面的评分值
     */
    private int alphaBeta(Board board, int depth, int alpha, int beta, boolean maximizingPlayer) {
        // 搜索到底：调用评估函数
        if (depth == 0) {
            return evaluator.evaluate(board, maximizingPlayer);
        }

        // 当前方的所有走法（注意始终传 false，即我方视角）
        Move[] moves = board.generateMoves(false);

        // 没有合法走法：说明被将死或无棋可走
        if (moves.length == 0) {
            return maximizingPlayer ? -Constants.INF : Constants.INF;
        }

        // 极大化分支（我方）
        if (maximizingPlayer) {
            int maxEval = -Constants.INF;
            for (Move move : moves) {
                board.makeMove(move);
                int eval = alphaBeta(board, depth - 1, alpha, beta, false); // 下一层是对方
                board.undoMove(move);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break; // β剪枝
            }
            return maxEval;
        }
        // 极小化分支（对方）
        else {
            int minEval = Constants.INF;
            for (Move move : moves) {
                board.makeMove(move);
                int eval = alphaBeta(board, depth - 1, alpha, beta, true); // 下一层是我方
                board.undoMove(move);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break; // α剪枝
            }
            return minEval;
        }
    }

    public Move getRandomMove(Board board) {
        Move[] moves = board.generateMoves(false);
        return moves[(int)(Math.random() * moves.length)];
    }
}
