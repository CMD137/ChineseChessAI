package game;

/**
 * 移动结构
 */

import java.util.Objects;

public class Move {
    public String pieceId;
    public int fromX, fromY, x, y;
        public PieceInfo captured;


    public Move(String pieceId, int fromX, int fromY, int x, int y) {
        this.pieceId = pieceId;
        this.fromX = fromX;
        this.fromY = fromY;
        this.x = x;
        this.y = y;
    }

    public Move(String pieceId, int x, int y) {
        this.pieceId = pieceId;
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return pieceId + ": (" + fromX + "," + fromY + ") -> (" + x + "," + y + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Move)) return false;
        Move move = (Move) o;
        return x == move.x && y == move.y &&
                fromX == move.fromX && fromY == move.fromY &&
                pieceId.equals(move.pieceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceId, fromX, fromY, y, x);
    }
}