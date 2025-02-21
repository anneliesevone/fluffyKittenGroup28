package comp20050.hexagonalboard;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Polygon;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HexBoardTest {
    @Test
    public void testCreateHexagonalBoardCount() {
        Pane board = HexBoard.createHexagonalBoard();
        // For a side=7 hex board, we expect 127 cells
        Assertions.assertEquals(127, board.getChildren().size(),
                "Hex board should have 127 hex cells");
    }

    @Test
    public void testEachHexProperties() {
        Pane board = HexBoard.createHexagonalBoard();

        board.getChildren().forEach(node -> {// Iterate over every child node  
            Assertions.assertTrue(node instanceof Polygon, "Each cell should be a Polygon");
            Polygon hex = (Polygon) node;
            // 6 corners meaning 12 points
            Assertions.assertEquals(12, hex.getPoints().size(), "Polygon should have 6 corners");
        });
    }
}