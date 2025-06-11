package game;

import java.util.*;

public class Board {
    private byte[] board = new byte[90];
    private Map<String, PieceInfo> pieces = new HashMap<>();
    public boolean sideToMove = true;
    private long zobristHash;
    private static long[][] zobristTable = new long[90][256];

    static {
        Random rand = new Random(42);
        for (int i = 0; i < 90; i++) {
            for (int j = 0; j < 256; j++) {
                zobristTable[i][j] = rand.nextLong();
            }
        }
    }

    public Board() {
        setupInitialPosition();
        initZobristHash();
    }

    private void setupInitialPosition() {
        pieces.clear();
        add("R2", 0, 9); add("N2", 1, 9); add("B2", 2, 9); add("A2", 3, 9); add("K", 4, 9);
        add("A1", 5, 9); add("B1", 6, 9); add("R1", 8, 9);
        add("C2", 1, 7); add("C1", 7, 7);
        add("P5", 0, 6); add("P4", 2, 6); add("P3", 4, 6); add("P2", 6, 6); add("P1", 8, 6);
<<<<<<< Updated upstream
        add("r1", 0, 0); add("n1", 1, 0); add("b1", 2, 0); add("a1", 3, 0); add("k", 4, 0);
=======
        // 我方 (小写)
        add("r1", 0, 0); add("b1", 2, 0); add("a1", 3, 0); add("k", 4, 0);
>>>>>>> Stashed changes
        add("a2", 5, 0); add("b2", 6, 0); add("n2", 7, 0); add("r2", 8, 0);
        add("c1", 1, 2); add("c2", 7, 2);
        add("p1", 0, 3); add("p2", 2, 3); add("p3", 4, 3); add("p4", 6, 3); add("p5", 8, 3);
    }

    private void add(String id, int x, int y) {
        pieces.put(id, new PieceInfo(id, x, y));
        board[y * 9 + x] = (byte) id.charAt(0);
    }

    private void initZobristHash() {
        zobristHash = 0;
        for (Map.Entry<String, PieceInfo> e : pieces.entrySet()) {
            PieceInfo p = e.getValue();
            zobristHash ^= zobristTable[p.y * 9 + p.x][p.getPiece()];
        }
    }

    public void makeMove(Move move) {
        PieceInfo p = pieces.get(move.pieceId);
        if (p == null) return;
        if (move.fromX == 0 && move.fromY == 0) { // 修复：设置 fromX, fromY
            move.fromX = p.x;
            move.fromY = p.y;
        }
        int from = p.y * 9 + p.x;
        int to = move.y * 9 + move.x;

        move.captured = null;
        for (Map.Entry<String, PieceInfo> entry : pieces.entrySet()) {
            if (entry.getValue().x == move.x && entry.getValue().y == move.y) {
                move.captured = new PieceInfo(entry.getValue());
                pieces.remove(entry.getKey());
                break;
            }
        }

        board[from] = 0;
        board[to] = p.getPiece();
        p.x = move.x;
        p.y = move.y;
        updateZobristHash(move);
        sideToMove = !sideToMove;
    }

    public void undoMove(Move move) {
        PieceInfo p = pieces.get(move.pieceId);
        int to = p.y * 9 + p.x;
        int from = move.fromY * 9 + move.fromX;
        board[to] = move.captured != null ? move.captured.getPiece() : 0;
        board[from] = p.getPiece();
        p.x = move.fromX;
        p.y = move.fromY;
        if (move.captured != null) {
            pieces.put(move.captured.id, move.captured);
        }
        updateZobristHash(move);
        sideToMove = !sideToMove;
    }

    private void updateZobristHash(Move move) {
        PieceInfo p = pieces.get(move.pieceId);
        int from = move.fromY * 9 + move.fromX;
        int to = move.y * 9 + move.x;
        zobristHash ^= zobristTable[from][p.getPiece()];
        zobristHash ^= zobristTable[to][p.getPiece()];
        if (move.captured != null) {
            zobristHash ^= zobristTable[to][move.captured.getPiece()];
        }
    }

    public Move[] generateMoves(boolean capturesOnly) {
        List<Move> moves = new ArrayList<>();
        for (PieceInfo p : pieces.values()) {
            if (p.isOurSide() == sideToMove) {
                moves.addAll(generateMovesForPiece(p, capturesOnly));
            }
        }
        return moves.toArray(new Move[0]);
    }

    private List<Move> generateMovesForPiece(PieceInfo p, boolean capturesOnly) {
        List<Move> moves = new ArrayList<>();
        int x = p.x, y = p.y;
        char type = p.getPieceType();

        switch (type) {
            case 'r':
                for (int i = x + 1; i < 9; i++) addMove(p, i, y, moves, capturesOnly);
                for (int i = x - 1; i >= 0; i--) addMove(p, i, y, moves, capturesOnly);
                for (int i = y + 1; i < 10; i++) addMove(p, x, i, moves, capturesOnly);
                for (int i = y - 1; i >= 0; i--) addMove(p, x, i, moves, capturesOnly);
                break;
            case 'n':
                int[][] knightMoves = {{1, 2}, {2, 1}, {-1, 2}, {-2, 1}, {1, -2}, {2, -1}, {-1, -2}, {-2, -1}};
                for (int[] d : knightMoves) {
                    int nx = x + d[0], ny = y + d[1];
                    if (isValidPosition(nx, ny) && !isBlockedKnight(x, y, nx, ny)) {
                        addMove(p, nx, ny, moves, capturesOnly);
                    }
                }
                break;
            case 'c':
                if (!capturesOnly) {
                    for (int i = x + 1; i < 9 && board[i + y * 9] == 0; i++) moves.add(new Move(p.id, x, y, i, y));
                    for (int i = x - 1; i >= 0 && board[i + y * 9] == 0; i--) moves.add(new Move(p.id, x, y, i, y));
                    for (int i = y + 1; i < 10 && board[x + i * 9] == 0; i++) moves.add(new Move(p.id, x, y, x, i));
                    for (int i = y - 1; i >= 0 && board[x + i * 9] == 0; i--) moves.add(new Move(p.id, x, y, x, i));
                }
                for (int i = x + 1; i < 9; i++) {
                    if (board[i + y * 9] != 0) {
                        for (i++; i < 9; i++) if (board[i + y * 9] != 0) { addMove(p, i, y, moves, true); break; }
                        break;
                    }
                }
<<<<<<< Updated upstream
                // 其他方向类似，省略
=======
                for (int i = x - 1; i >= 0; i--) {
                    if (board[i + y * 9] != 0) {
                        for (i--; i >= 0; i--) if (board[i + y * 9] != 0) { addMove(p, i, y, moves, true); break; }
                        break;
                    }
                }
                for (int i = y + 1; i < 10; i++) {
                    if (board[x + i * 9] != 0) {
                        for (i++; i < 10; i++) if (board[x + i * 9] != 0) { addMove(p, x, i, moves, true); break; }
                        break;
                    }
                }
                for (int i = y - 1; i >= 0; i--) {
                    if (board[x + i * 9] != 0) {
                        for (i--; i >= 0; i--) if (board[x + i * 9] != 0) { addMove(p, x, i, moves, true); break; }
                        break;
                    }
                }
>>>>>>> Stashed changes
                break;
            case 'k':
                int[][] kingMoves = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
                for (int[] d : kingMoves) {
                    int nx = x + d[0], ny = y + d[1];
                    if (isValidKingPosition(nx, ny, p.isOurSide())) addMove(p, nx, ny, moves, capturesOnly);
                }
                break;
            case 'a':
                int[][] advisorMoves = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
                for (int[] d : advisorMoves) {
                    int nx = x + d[0], ny = y + d[1];
                    if (isValidAdvisorPosition(nx, ny, p.isOurSide())) addMove(p, nx, ny, moves, capturesOnly);
                }
                break;
            case 'b':
                int[][] bishopMoves = {{2, 2}, {2, -2}, {-2, 2}, {-2, -2}};
                for (int[] d : bishopMoves) {
                    int nx = x + d[0], ny = y + d[1];
                    if (isValidBishopPosition(nx, ny, p.isOurSide()) && !isBlockedBishop(x, y, nx, ny)) {
                        addMove(p, nx, ny, moves, capturesOnly);
                    }
                }
                break;
            case 'p':
                int dir = p.isOurSide() ? 1 : -1;
                if (p.isOurSide() ? y >= 5 : y <= 4) {
                    addMove(p, x + 1, y, moves, capturesOnly);
                    addMove(p, x - 1, y, moves, capturesOnly);
                }
                addMove(p, x, y + dir, moves, capturesOnly);
                break;
        }
        return moves;
    }


    private void addMove(PieceInfo p, int x, int y, List<Move> moves, boolean capturesOnly) {
        if (!isValidPosition(x, y)) return;
        int idx = y * 9 + x;
        if (board[idx] == 0) {
            if (!capturesOnly) moves.add(new Move(p.id, p.x, p.y, x, y));
        } else if (isEnemyPiece(board[idx], p.isOurSide())) {
            moves.add(new Move(p.id, p.x, p.y, x, y));
        }
    }

    private boolean isValidPosition(int x, int y) {
        return x >= 0 && x < 9 && y >= 0 && y < 10;
    }

    private boolean isValidKingPosition(int x, int y, boolean ourSide) {
        return isValidPosition(x, y) && (ourSide ? (x >= 3 && x <= 5 && y <= 2) : (x >= 3 && x <= 5 && y >= 7));
    }

    private boolean isValidAdvisorPosition(int x, int y, boolean ourSide) {
        return isValidKingPosition(x, y, ourSide);
    }

    private boolean isValidBishopPosition(int x, int y, boolean ourSide) {
        return isValidPosition(x, y) && (ourSide ? y <= 4 : y >= 5);
    }

    private boolean isBlockedKnight(int x, int y, int nx, int ny) {
        int dx = nx - x, dy = ny - y;
        int mx = x + (Math.abs(dx) == 2 ? dx / 2 : 0);
        int my = y + (Math.abs(dy) == 2 ? dy / 2 : 0);
        return board[my * 9 + mx] != 0;
    }

    private boolean isBlockedBishop(int x, int y, int nx, int ny) {
        int mx = (x + nx) / 2, my = (y + ny) / 2;
        return board[my * 9 + mx] != 0;
    }

    private boolean isEnemyPiece(byte piece, boolean ourSide) {
        return (ourSide && Character.isUpperCase(piece)) || (!ourSide && Character.isLowerCase(piece));
    }

    public int evaluate() {
        int score = 0;
        for (PieceInfo p : pieces.values()) {
            int value = getPieceValue(p.getPiece());
            score += p.isOurSide() ? value : -value;
        }
        return sideToMove ? score : -score;
    }

    private int getPieceValue(byte piece) {
        switch (Character.toLowerCase(piece)) {
            case 'k': return 10000;
            case 'r': return 900;
            case 'c': return 450;
            case 'n': return 400;
            case 'b': return 200;
            case 'a': return 100;
            case 'p': return 100;
            default: return 0;
        }
    }

    public boolean isGameOver() {
        Move[] moves = generateMoves(false);
        return moves.length == 0 && isInCheck(sideToMove);
    }

    public boolean isInCheck(boolean ourSide) {
        String kingId = ourSide ? "k" : "K";
        PieceInfo king = pieces.get(kingId);
        if (king == null) return true;

        boolean originalSide = sideToMove;
        sideToMove = !ourSide;
        Move[] enemyMoves = generateMoves(true);
        sideToMove = originalSide;

        for (Move m : enemyMoves) {
            if (m.x == king.x && m.y == king.y) return true;
        }
        return false;
    }

    public boolean isLegalMove(Move move) {
        makeMove(move);
        boolean legal = !isInCheck(sideToMove);
        undoMove(move);
        return legal;
    }

    public long getZobristHash() {
        return zobristHash;
    }

    public boolean getSideToMove() {
        return sideToMove;
    }

    public void print() {
        for (int y = 9; y >= 0; y--) {
            for (int x = 0; x < 9; x++) {
                int idx = y * 9 + x;
                System.out.printf("%2c ", board[idx] == 0 ? '.' : (char) board[idx]);
            }
            System.out.println();
        }
    }

    public Board clone() {
        Board b = new Board();
        System.arraycopy(this.board, 0, b.board, 0, 90);
        b.pieces = new HashMap<>();
        for (Map.Entry<String, PieceInfo> e : this.pieces.entrySet()) {
            b.pieces.put(e.getKey(), new PieceInfo(e.getValue()));
        }
        b.zobristHash = this.zobristHash;
        b.sideToMove = this.sideToMove;
        return b;
    }
}