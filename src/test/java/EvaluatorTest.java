import game.*;
import ai.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ai.Evaluator.kingTable;
import static org.junit.jupiter.api.Assertions.*;

public class EvaluatorTest {
    private Evaluator evaluator;
    private Board board;

    @BeforeEach
    public void setUp() {
        evaluator = new Evaluator();
        board = new Board();
    }
   @Test
    public void testEvaluateTable() {
        Evaluator.printAllTables();
        System.out.println("MY king:");
        System.out.println(kingTable[0*9+4]);

        System.out.println("evaluate:"+evaluator.evaluate(board));
    }
}
