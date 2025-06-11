import game.*;
import ai.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
//AI测试类
public class AITest {
    private AI ai;
    private Board board;

    @BeforeEach
    public void setUp() {
        ai = new AI();
        board = new Board();
    }

    @Test
    public void testGetRandomMove_NotNull() {
        int round=0;
        board.print(); // 显示棋盘

        int times=20;
        while (times!=0){
            // 测试随机移动是否不为空
            Move bestMove = ai.getRandomMove(board);
            assertNotNull(bestMove, "随机移动不应为空");

            if (bestMove != null) {
                board.makeMove(bestMove);

                //输出我方移动
                System.out.println("\n\n"+bestMove.pieceId + " " + bestMove.x + " " + bestMove.y);
                System.out.flush(); //注意要写这个
            } else {
                //temp
                System.out.println("bestMove空");
                break;
            }

            //temp
            round++;
            System.out.println("--------------------------第"+round+"回合:--------------------------");
            board.print(); // 显示棋盘
            times--;
        }


    }
}
