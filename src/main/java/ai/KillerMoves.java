package ai;

import game.Move;

public class KillerMoves {
    private static final int MAX_DEPTH = 100; // 假设最大深度
    private Move[][] killerMoves = new Move[MAX_DEPTH][2]; // 每层存储两个杀手移动

    public void addKillerMove(int depth, Move move) {
        if (depth >= MAX_DEPTH) return;
        if (killerMoves[depth][0] == null || !killerMoves[depth][0].equals(move)) {
            killerMoves[depth][1] = killerMoves[depth][0];
            killerMoves[depth][0] = move;
        }
    }

    public Move[] getKillerMoves(int depth) {
        if (depth >= MAX_DEPTH) return new Move[0];
        int count = 0;
        for (Move move : killerMoves[depth]) {
            if (move != null) count++;
        }
        Move[] result = new Move[count];
        System.arraycopy(killerMoves[depth], 0, result, 0, count);
        return result;
    }

    public void clear() {
        killerMoves = new Move[MAX_DEPTH][2];
    }
}