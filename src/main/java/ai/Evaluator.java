package ai;

import game.Board;

public class Evaluator {
    private static final int[] mirrorTable = new int[90];
    // 基础价值
    private static final int KING_VALUE   = 1000;
    private static final int ROOK_VALUE   = 210;
    private static final int CANNON_VALUE = 200;
    private static final int KNIGHT_VALUE = 100;
    private static final int BISHOP_VALUE = 20;
    private static final int ADVISOR_VALUE=20;
    private static final int PAWN_VALUE   = 15;

    // 位值表 我方
    public static final int[] kingTable   = new int[90];
    public static final int[] advisorTable= new int[90];
    public static final int[] bishopTable = new int[90];
    public static final int[] knightTable = new int[90];
    public static final int[] rookTable   = new int[90];
    public static final int[] cannonTable = new int[90];
    public static final int[] pawnTable   = new int[90];

    // 位值表 敌方
    public static final int[] enemyKingTable   = new int[90];
    public static final int[] enemyAdvisorTable= new int[90];
    public static final int[] enemyBishopTable = new int[90];
    public static final int[] enemyKnightTable = new int[90];
    public static final int[] enemyRookTable   = new int[90];
    public static final int[] enemyCannonTable = new int[90];
    public static final int[] enemyPawnTable   = new int[90];

    static {
        // 初始化镜像表
        for (int i = 0; i < 90; i++) {
            int x = i % 9, y = i / 9;
            mirrorTable[i] = (9 - y) * 9 + x;
        }

        // 初始化双方棋子的位值表.都用直接赋值的方式可视化数据

        // 兵
        int [] rawPawn = {
                9, 9, 9, 11, 13, 11, 9, 9, 9,
                19, 24, 34, 42, 44, 42, 34, 24, 19,
                19, 24, 32, 37, 37, 37, 32, 24, 19,
                19, 23, 27, 29, 30, 29, 27, 23, 19,
                14, 18, 20, 27, 29, 27, 20, 18, 14,
                7, 0, 13, 0, 16, 0, 13, 0, 7,
                7, 0, 7, 0, 15, 0, 7, 0, 7,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
        };
        System.arraycopy(rawPawn, 0, enemyPawnTable, 0, 90);
        for (int i = 0; i < 90; i++) {
            pawnTable[i] = rawPawn[mirrorTable[i]];
        }

        // 马
        int[] rawKnight = {
                90, 90, 90, 96, 90, 96, 90, 90, 90,
                90, 96, 103, 97, 94, 97, 103, 96, 90,
                92, 98, 99, 103, 99, 103, 99, 98, 92,
                93, 108, 100, 107, 100, 107, 100, 108, 93,
                90, 100, 99, 103, 104, 103, 99, 100, 90,
                90, 98, 101, 102, 103, 102, 101, 98, 90,
                92, 94, 98, 95, 98, 95, 98, 94, 92,
                93, 92, 94, 95, 92, 95, 94, 92, 93,
                85, 90, 92, 93, 78, 93, 92, 90, 85,
                88, 85, 90, 88, 90, 88, 90, 85, 88,
        };
        System.arraycopy(rawKnight, 0, enemyKnightTable, 0, 90);
        for (int i = 0; i < 90; i++) {
            knightTable[i] = rawKnight[mirrorTable[i]];
        }

        // 车
        int[] rawRook = {
                206, 208, 207, 213, 214, 213, 207, 208, 206,
                206, 212, 209, 216, 233, 216, 209, 212, 206,
                206, 208, 207, 214, 216, 214, 207, 208, 206,
                206, 213, 213, 216, 216, 216, 213, 213, 206,
                208, 211, 211, 214, 215, 214, 211, 211, 208,
                208, 212, 212, 214, 215, 214, 212, 212, 208,
                204, 209, 204, 212, 214, 212, 204, 209, 204,
                198, 208, 204, 212, 212, 212, 204, 208, 198,
                200, 208, 206, 212, 200, 212, 206, 208, 200,
                194, 206, 204, 212, 200, 212, 204, 206, 194,
        };
        System.arraycopy(rawRook, 0, enemyRookTable, 0, 90);
        for (int i = 0; i < 90; i++) {
            rookTable[i] = rawRook[mirrorTable[i]];
        }

        // 炮
        int [] rawCannon = {
                100, 100, 96, 91, 90, 91, 96, 100, 100,
                98, 98, 96, 92, 89, 92, 96, 98, 98,
                97, 97, 96, 91, 92, 91, 96, 97, 97,
                96, 99, 99, 98, 100, 98, 99, 99, 96,
                96, 96, 96, 96, 100, 96, 96, 96, 96,
                95, 96, 99, 96, 100, 96, 99, 96, 95,
                96, 96, 96, 96, 96, 96, 96, 96, 96,
                97, 96, 100, 99, 101, 99, 100, 96, 97,
                96, 97, 98, 98, 98, 98, 98, 97, 96,
                96, 96, 97, 99, 99, 99, 97, 96, 96,
        };
        System.arraycopy(rawCannon, 0, enemyCannonTable, 0, 90);
        for (int i = 0; i < 90; i++) {
            cannonTable[i] = rawCannon[mirrorTable[i]];
        }

        // 象
        int[] rawBishop = {
                0,  0,  0,  0,  0,  0,  0,  0,  0,
                0,  0,  0,  0,  0,  0,  0,  0,  0,
                0,  0,  0,  0,  0,  0,  0,  0,  0,
                0,  0,  0,  0,  0,  0,  0,  0,  0,
                0,  0,  0,  0,  0,  0,  0,  0,  0,
                0,  0, 20,  0,  0,  0, 20,  0,  0,
                0,  0,  0,  0,  0,  0,  0,  0,  0,
                18, 0,  0,  0, 23,  0,  0,  0,  18,
                0,  0,  0,  0,  0,  0,  0,  0,  0,
                0,  0, 20,  0,  0,  0, 20,  0,  0,
        };
        System.arraycopy(rawBishop, 0, enemyBishopTable, 0, 90);
        for (int i = 0; i < 90; i++) {
            bishopTable[i] = rawBishop[mirrorTable[i]];
        }

        // 士
        int[] rawAdvisor = {
                0,  0,  0,  0,  0,  0,  0,  0,  0,
                0,  0,  0,  0,  0,  0,  0,  0,  0,
                0,  0,  0,  0,  0,  0,  0,  0,  0,
                0,  0,  0,  0,  0,  0,  0,  0,  0,
                0,  0,  0,  0,  0,  0,  0,  0,  0,
                0,  0,  0,  0,  0,  0,  0,  0,  0,
                0,  0,  0,  0,  0,  0,  0,  0,  0,
                0,  0,  0,  20, 0,  20, 0,  0,  0,
                0,  0,  0,  0,  23, 0,  0,  0,  0,
                0,  0,  0,  20, 0,  20, 0,  0,  0,
        };
        System.arraycopy(rawAdvisor, 0, enemyAdvisorTable, 0, 90);
        for (int i = 0; i < 90; i++) {
            advisorTable[i] = rawAdvisor[mirrorTable[i]];
        }

        // 将
        int[] rawKing = {
                0,  0,  0,  0,  0,  0,  0,  0,  0,
                0,  0,  0,  0,  0,  0,  0,  0,  0,
                0,  0,  0,  0,  0,  0,  0,  0,  0,
                0,  0,  0,  0,  0,  0,  0,  0,  0,
                0,  0,  0,  0,  0,  0,  0,  0,  0,
                0,  0,  0,  0,  0,  0,  0,  0,  0,
                0,  0,  0,  0,  0,  0,  0,  0,  0,
                0,  0,  0, -10,-20,-10,  0,  0,  0,
                0,  0,  0, -5,  0, -5,  0,  0,  0,
                0,  0,  0, 11, 15, 11,  0,  0,  0,
        };
        System.arraycopy(rawKing, 0, enemyKingTable, 0, 90);
        for (int i = 0; i < 90; i++) {
            kingTable[i] = rawKing[mirrorTable[i]];
        }
    }

    // 主评估函数
    public int evaluate(Board board,boolean isOurSide) {
        int score = 0;

        //杀王：
        if (isOurSide){
            //小写方
            if (board.pieces.get("k")==null){
                return Integer.MIN_VALUE;//输
            }else if (board.pieces.get("K")==null){
                return Integer.MAX_VALUE;//赢
            }
        }else {
            //大写方
            if (board.pieces.get("K")==null){
                return Integer.MIN_VALUE;//输
            }else if (board.pieces.get("k")==null){
                return Integer.MAX_VALUE;//赢
            }
        }

        for (int i = 0; i < 90; i++) {
            byte piece = board.board[i];
            if (piece == 0) continue;


            int base = getPieceValue(piece);
            int pos = getPositionValue(piece, i, isOurSide);

            //ourside为true，那么加上小写棋子的分
            if (isOurSide) {
                if (Character.isLowerCase(piece)){
                    score += (base + pos);
                }else {
                    score -= (base + pos);
                }
            }else {
                if (Character.isUpperCase(piece)){
                    score -= (base + pos);
                }else {
                    score += (base + pos);
                }
            }

            //temp:
//            int x=i%9;
//            int y=i/9;
//            System.out.println((char) piece+" "+i+" -> "+x+"，"+y+" "+"base:"+base+" pos:"+pos);
//            System.out.println("score:"+score);
        }
        return score;
    }

    // 获取基础分值
    private int getPieceValue(byte piece) {
        switch (Character.toLowerCase(piece)) {
            case 'k': return KING_VALUE;
            case 'r': return ROOK_VALUE;
            case 'c': return CANNON_VALUE;
            case 'n': return KNIGHT_VALUE;
            case 'b': return BISHOP_VALUE;
            case 'a': return ADVISOR_VALUE;
            case 'p': return PAWN_VALUE;
            default: return 0;
        }
    }

    // 获取位置价值
    private int getPositionValue(byte piece, int i, boolean isOurSide) {

        switch (piece) {
            case 'k': return kingTable[i];
            case 'r': return rookTable[i];
            case 'c': return cannonTable[i];
            case 'n': return knightTable[i];
            case 'b': return bishopTable[i];
            case 'a': return advisorTable[i];
            case 'p': return pawnTable[i];
            case 'K': return enemyKingTable[i];
            case 'R': return enemyRookTable[i];
            case 'C': return enemyCannonTable[i];
            case 'N': return enemyKnightTable[i];
            case 'B': return enemyBishopTable[i];
            case 'A': return enemyAdvisorTable[i];
            case 'P': return enemyPawnTable[i];
            default: return 0;
        }
    }

    // 用于调试任意表
    public static void printTable(int[] table) {
        for(int y = 9; y >= 0; y--) {
            for(int x = 0; x < 9; x++) {
                int idx = y * 9 + x;
                System.out.printf("%2d ", table[idx]);
            }
            System.out.println();
        }
    }

    public static void printAllTables() {
        //输出我方表
        System.out.println("kingTable:");
        printTable(kingTable);
        System.out.println("rookTable:");
        printTable(rookTable);
        System.out.println("cannonTable:");
        printTable(cannonTable);
        System.out.println("knightTable:");
        printTable(knightTable);
        System.out.println("bishopTable:");
        printTable(bishopTable);
        System.out.println("advisorTable:");
        printTable(advisorTable);
        System.out.println("pawnTable:");
        printTable(pawnTable);
        //输出敌方表
        System.out.println("enemyKingTable:");
        printTable(enemyKingTable);
        System.out.println("enemyRookTable:");
        printTable(enemyRookTable);
        System.out.println("enemyCannonTable:");
        printTable(enemyCannonTable);
        System.out.println("enemyKnightTable:");
        printTable(enemyKnightTable);
        System.out.println("enemyBishopTable:");
        printTable(enemyBishopTable);
        System.out.println("enemyAdvisorTable:");
        printTable(enemyAdvisorTable);
        System.out.println("enemyPawnTable:");
        printTable(enemyPawnTable);
    }
}