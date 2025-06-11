package ai;

import game.Move;

import java.util.HashMap;
import java.util.Map;

public class TranspositionTable {
    static class TTEntry {
        long zobristHash;
        int value;
        int depth;
        int flag; // EXACT, LOWER, UPPER
        int bestMoveIndex;
        Move bestMove;

        static final int EXACT = 0;
        static final int LOWER = 1;
        static final int UPPER = 2;

        TTEntry(long zobristHash, int value, int depth, int flag, int bestMoveIndex, Move bestMove) {
            this.zobristHash = zobristHash;
            this.value = value;
            this.depth = depth;
            this.flag = flag;
            this.bestMoveIndex = bestMoveIndex;
            this.bestMove = bestMove;
        }
    }

    private Map<Long, TTEntry> table = new HashMap<>();
    private static final int TABLE_SIZE = 1 << 20; // 约100万条目

    public TTEntry get(long zobristHash) {
        TTEntry entry = table.get(zobristHash);
        if (entry != null && entry.zobristHash == zobristHash) {
            return entry;
        }
        return null;
    }

    public void store(long zobristHash, int value, int depth, int flag, int bestMoveIndex, Move bestMove) {
        // 覆盖策略：总是存储更高或相等深度的条目
        TTEntry existing = table.get(zobristHash);
        if (existing == null || depth >= existing.depth) {
            table.put(zobristHash, new TTEntry(zobristHash, value, depth, flag, bestMoveIndex, bestMove));
        }
    }

    public void clear() {
        table.clear();
    }
}