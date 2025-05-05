package comp20050.hexagonalboard;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Polygon;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HexBoardTest {
    @Test
    void boardHas127Cells() {
        Pane board = HexBoard.createHexagonalBoard();
        assertEquals(127, board.getChildren().size(), "Hex board should have 127 hex cells");
    }

    @Test
    void eachCellIsHexagon() {
        Pane board = HexBoard.createHexagonalBoard();
        board.getChildren().forEach(node -> {
            assertTrue(node instanceof Polygon, "Each cell should be a Polygon");
            Polygon hex = (Polygon) node;
            assertEquals(12, hex.getPoints().size(), "Polygon should have 6 corners");
        });
    }
}

