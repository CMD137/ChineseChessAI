package game;

import java.util.*;

public class Board {
    public byte[] board = new byte[90];
    public Map<String, PieceInfo> pieces = new HashMap<>();
    public boolean sideToMove = true;

    public Board() {
        setupInitialPosition();
    }

    private void setupInitialPosition() {
        pieces.clear();
        add("R2", 0, 9); add("N2", 1, 9); add("B2", 2, 9); add("A2", 3, 9); add("K", 4, 9);
        add("A1", 5, 9); add("B1", 6, 9); add("R1", 8, 9);
        add("C2", 1, 7); add("C1", 7, 7);
        add("P5", 0, 6); add("P4", 2, 6); add("P3", 4, 6); add("P2", 6, 6); add("P1", 8, 6);

        // 我方 (小写)
        add("r1", 0, 0); add("b1", 2, 0); add("a1", 3, 0); add("k", 4, 0);
        add("a2", 5, 0); add("b2", 6, 0); add("n2", 7, 0); add("r2", 8, 0);
        add("c1", 1, 2); add("c2", 7, 2);
        add("p1", 0, 3); add("p2", 2, 3); add("p3", 4, 3); add("p4", 6, 3); add("p5", 8, 3);
    }

    private void add(String id, int x, int y) {
        pieces.put(id, new PieceInfo(id, x, y));
        board[y * 9 + x] = (byte) id.charAt(0);
    }

    public void makeMove(Move move) {
        PieceInfo p = pieces.get(move.pieceId);
        if (p == null) return;

        int from = p.y * 9 + p.x;
        int to = move.y * 9 + move.x;


        //吃子
        move.captured = null;
        if (board[to]!=0){
            for (Map.Entry<String, PieceInfo> entry : pieces.entrySet()) {
                if (entry.getValue().x == move.x && entry.getValue().y == move.y) {
                    move.captured = new PieceInfo(entry.getValue());
                    pieces.remove(entry.getKey());//吃子
                    break;
                }
            }
        }


        board[from] = 0;
        board[to] = p.getPiece();
        p.x = move.x;
        p.y = move.y;
        sideToMove = !sideToMove;
    }

    public void undoMove(Move move) {
        PieceInfo p = pieces.get(move.pieceId);
        int to = p.y * 9 + p.x;           // 当前棋子所在格（目的地）
        int from = move.fromY * 9 + move.fromX; // 棋子原来位置

        // 把目标格清空，棋子撤回原格
        board[to] = 0;
        board[from] = p.getPiece();

        // 恢复棋子坐标
        p.x = move.fromX;
        p.y = move.fromY;

        // 如果吃子，恢复被吃的棋子
        if (move.captured != null) {
            pieces.put(move.captured.id, move.captured);
            int capPos = move.captured.y * 9 + move.captured.x;
            board[capPos] = move.captured.getPiece();
        }

        sideToMove = !sideToMove;
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
        //都转为小写字母方便写，但是地方放数据还在p中
        char type = p.getPieceType();


        switch (type) {
            //车：直线移动原位置与目标位置间不能有棋子
            case 'r':
                for (int i = x + 1; i < 9; i++) {
                    if(countPiecesBetween(x, y, i, y)==0)
                        addMove(p, i, y, moves, capturesOnly);
                    if (board[y * 9 + i] != 0) break; // 遇到任何棋子剪枝
                }
                for (int i = x - 1; i >= 0; i--) {
                    if(countPiecesBetween(x, y, i, y)==0)
                        addMove(p, i, y, moves, capturesOnly);
                    if (board[y * 9 + i] != 0) break; // 遇到任何棋子剪枝
                }
                for (int i = y + 1; i < 10; i++) {
                    if(countPiecesBetween(x, y, x, i)==0)
                        addMove(p, x, i, moves, capturesOnly);
                    if (board[i * 9 + x] != 0) break; // 遇到任何棋子剪枝
                }
                for (int i = y - 1; i >= 0; i--) {
                    if(countPiecesBetween(x, y, x, i)==0)
                        addMove(p, x, i, moves, capturesOnly);
                    if (board[i * 9 + x] != 0) break; // 遇到任何棋子剪枝
                }
                break;
            case 'n'://马：注意卡马脚
                int[][] knightMoves = {{1, 2}, {2, 1}, {-1, 2}, {-2, 1}, {1, -2}, {2, -1}, {-1, -2}, {-2, -1}};
                for (int[] d : knightMoves) {
                    int nx = x + d[0], ny = y + d[1];
                    if (!isBlockedKnight(x, y, nx, ny)) {
                        addMove(p, nx, ny, moves, capturesOnly);
                    }
                }
                break;
            case 'c':
                // 方向：右、左、上、下
                int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                for (int[] dir : directions) {
                    int dx = dir[0], dy = dir[1];

                    for (int i = 1; i < (dx != 0 ? 9 : 10); i++) {
                        int nx = x + i * dx;
                        int ny = y + i * dy;

                        // 1. 检查位置合法性
                        if (!isValidPosition(nx, ny)) break;

                        // 2. 检查 wouldKingsFace（仅当 x 变化时）
                        if (dx != 0 && wouldKingsFace(p.x, nx)) continue;

                        int idx = ny * 9 + nx;
                        int piecesBetween = countPiecesBetween(x, y, nx, ny);

                        // 3. 吃子移动（capturesOnly=true 或 false）
                        if (piecesBetween == 1 && isEnemy(nx, ny, p.isOurSide())) {
                            moves.add(new Move(p.id, p.x, p.y, nx, ny));
                        }

                        // 4. 非吃子移动（capturesOnly=false）
                        if (!capturesOnly && piecesBetween == 0 && board[idx] == 0) {
                            moves.add(new Move(p.id, p.x, p.y, nx, ny));
                        }

                        // 5. 如果路径上有棋子，终止该方向的搜索（除非吃子已处理）
                        if (piecesBetween > 0) break;
                    }
                }
                break;

            case 'k'://王，只能在宫殿里走
                int[][] kingMoves = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
                for (int[] d : kingMoves) {
                    int nx = x + d[0], ny = y + d[1];
                    if (isValidKingPosition(nx, ny,p.isOurSide())) {
                        addMove(p, nx, ny, moves, capturesOnly);
                    }
                }
                break;
            case 'a'://士：只能在宫殿里斜着走
                int[][] advisorMoves = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
                for (int[] d : advisorMoves) {
                    int nx = x + d[0], ny = y + d[1];
                    if (isValidKingPosition(nx, ny,  p.isOurSide())) {
                        addMove(p, nx, ny, moves, capturesOnly);
                    }
                }
                break;
            case 'b'://象：直线移动原位置与目标位置间不能有棋子，不能过河
                int[][] bishopMoves = {{2, 2}, {2, -2}, {-2, 2}, {-2, -2}};
                for (int[] d : bishopMoves) {
                    int nx = x + d[0], ny = y + d[1];
                    if (isOwnLand(nx, ny,p.isOurSide()) && !isBlockedBishop(x, y, nx, ny)) {
                        addMove(p, nx, ny, moves, capturesOnly);
                    }
                }
                break;
            case 'p'://兵：过河前只能y+-1；过河后可以左右移动。
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

    //统计直线上两点之间存在的棋子数//象不能用
    private int countPiecesBetween(int sx, int sy, int x, int y) {
        int cnt=0;
        if(sx==x){
            int step = (y>sy ? 1:-1);
            for(int yy=sy+step; yy!=y; yy+=step){
                if(board[yy*9+x]!=0){
                    cnt++;
                }
            }
        } else if(sy==y){
            int step = (x>sx ? 1:-1);
            for(int xx=sx+step; xx!=x; xx+=step){
                if(board[y*9+xx]!=0){
                    cnt++;
                }
            }
        }
        return cnt;
    }

    //检测是否在棋盘内
    private boolean isValidPosition(int x,int y){
        return x>=0 && x<9 && y>=0 && y<10;
    }

    //检查是否卡马脚
    private boolean isBlockedKnight(int sx, int sy, int x, int y) {
        if (!isValidPosition(x, y)) return true;
        int dx = x - sx, dy = y - sy;
        if (Math.abs(dx) == 2 && Math.abs(dy) == 1) {
            int mx = sx + dx / 2; // 马腿位置
            return board[sy * 9 + mx] != 0;
        } else if (Math.abs(dx) == 1 && Math.abs(dy) == 2) {
            int my = sy + dy / 2; // 马腿位置
            return board[my * 9 + sx] != 0;
        }
        return true; // 无效的马移动
    }

    //检查是否在宫殿内
    private boolean isValidKingPosition(int x, int y,boolean ourSide) {
        if (ourSide){
            return x >= 3 && x <= 5 && y >= 0 && y <= 2;
        }else {
            return x >= 3 && x <= 5 && y >= 7 && y <= 9;
        }
    }


    //检查是否没有过河，象，兵
    private boolean isOwnLand(int x, int y,boolean ourSide){
        return ourSide ? y<5 : y>4;
    }

    //检查是否卡象腿
    private boolean isBlockedBishop(int sx, int sy, int x, int y) {
        if (!isValidPosition(x, y)) return false;
        int mx = (sx+x)/2, my = (sy+y)/2;
        return board[mx+my*9]!=0;
    }


    //检查是否是敌人
    private boolean isEnemy(int x, int y,boolean ourSide){
        if(ourSide){
            return board[x+y*9]<='Z'&&board[x+y*9]!=0;
        }else{
            return board[x+y*9]>='a';
        }
    }

    //检查飞将：1.两个王是不是在同一x上。2.移动的棋子是不是从这个x移开了？3.如果是，数判断两王之间棋子数量。
    private boolean wouldKingsFace(int sx,int tx){
        PieceInfo k=pieces.get("k");
        PieceInfo K=pieces.get("K");

        if (k == null || K == null) return false;

        if (k.x != K.x) return false;

        if (sx != k.x) return false;

        if (sx==tx) return false;

        return countPiecesBetween(k.x, k.y,K.x, K.y) == 0;
    }

    private void addMove(PieceInfo p, int x, int y, List<Move> moves, boolean capturesOnly) {
        if(!isValidPosition(x, y)) return;
        if(wouldKingsFace(p.x,x)) return;
        int idx = y * 9 + x;
        if (board[idx] == 0) {
            if (!capturesOnly) moves.add(new Move(p.id, p.x, p.y, x, y));
        } else if (isEnemy(x, y,p.isOurSide())) {//是敌方棋子也可以走,吃子
            moves.add(new Move(p.id, p.x, p.y, x, y));
        }
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

}