package org.ujorm.ujoservlet.tools;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pavel Ponec
 */
public class BoardModelTest {

    @Test
    public void testBoard_0() {
        System.out.println("board");

        int width = 80;
        int height = 40;

        String base64 = null;
        BoardModel board1 = new BoardModel(width,height, base64);

        base64 = board1.exportBoard();
        BoardModel board2 = new BoardModel(width,height, base64);

        Assert.assertFalse(base64.endsWith("="));
        Assert.assertEquals(board1.isStone(0, 0), board2.isStone(0, 0));
    }

    @Test
    public void testBoard_1() {
        System.out.println("board");

        int width = 1;
        int height = 1;

        String base64 = null;
        BoardModel board1 = new BoardModel(width,height, base64);
        board1.setStone(0);

        base64 = board1.exportBoard();
        BoardModel board2 = new BoardModel(width,height, base64);

        Assert.assertFalse(base64.endsWith("="));
        Assert.assertEquals(board1.isStone(0, 0), board2.isStone(0, 0));
    }
}