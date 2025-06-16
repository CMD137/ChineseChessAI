/**
 * 主入口
 */
import game.*;
import ai.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static util.Constants.MAX_DEPTH;

public class Main {
    public static void main(String[] args) {
        Board board = new Board(); // 初始化棋盘
        AI ai = new AI(); // 初始化AI

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line;

        //temp
//        int round=0;
//        System.out.println("第"+round+"回合:");
//        board.print(); // 显示棋盘

        try {
            while((line = reader.readLine()) != null){
                //temp
                //long start=System.currentTimeMillis();

                //处理输入
                if(line.isEmpty()) continue;
                if(line.equals("START")){
                } else {
                    //System.out.println("sideToMove: " + board.getSideToMove());

                    String[] sp = line.trim().split("\\s+");
                    String pid = sp[0];
                    int x = Integer.parseInt(sp[1]);
                    int y = Integer.parseInt(sp[2]);

                    Move move=new Move(pid, x, y);
                    board.makeMove(move);

                    //temp
                    //System.out.println("sideToMove: " + board.getSideToMove());
                    //board.print(); // 显示棋盘
                }

                //生成移动
                Move bestMove = ai.iterativeDeepening(board,MAX_DEPTH); // 迭代加深搜索

                //temp
                //System.out.println("MAX:"+MAX_DEPTH);

                if (bestMove != null) {
                    board.makeMove(bestMove); // 执行AI最佳移动

                    //输出我方移动
                    System.out.println(bestMove.pieceId + " " + bestMove.x + " " + bestMove.y);
                    System.out.flush(); //注意要写这个
                } else {
                    //temp
                    //System.out.println("bestMove空");
                    //没有bestMove应该是被将死了，随便走一步
                    bestMove=ai.getRandomMove(board);
                    board.makeMove(bestMove);
                    //输出我方移动
                    System.out.println(bestMove.pieceId + " " + bestMove.x + " " + bestMove.y);
                    System.out.flush(); //注意要写这个
                }

                //temp
//                round++;
//                System.out.println("第"+round+"回合:");
//                board.print(); // 显示棋盘
//                System.out.println("用时:"+(System.currentTimeMillis()-start)+"ms");

            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}