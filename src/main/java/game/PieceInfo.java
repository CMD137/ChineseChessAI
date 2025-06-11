package game;

/**
 * 棋子
 */
public class PieceInfo {
    public String id;
    public int x, y;

    public PieceInfo(String id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public PieceInfo(PieceInfo other) {
        this.id = other.id;
        this.x = other.x;
        this.y = other.y;
    }

    public byte getPiece() {
        return (byte) id.charAt(0);
    }

    public boolean isOurSide() {
        return Character.isLowerCase(id.charAt(0));
    }

    public char getPieceType() {
        return Character.toLowerCase(id.charAt(0));
    }
}