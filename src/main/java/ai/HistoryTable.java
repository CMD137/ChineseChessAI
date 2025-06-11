package ai;

import game.Move;

import java.util.HashMap;
import java.util.Map;

public class HistoryTable {
    private Map<Move, Long> history = new HashMap<>();

    public void updateHistory(Move move, int depth) {
        long score = history.getOrDefault(move, 0L);
        score += 1L << (2 * depth); // 深度平方加权
        history.put(move, score);
    }

    public int[] getHistoryScores(Move[] moves) {
        int[] scores = new int[moves.length];
        for (int i = 0; i < moves.length; i++) {
            scores[i] = (int) Math.min(history.getOrDefault(moves[i], 0L), Integer.MAX_VALUE);
        }
        return scores;
    }

    public void clear() {
        history.clear();
    }
}