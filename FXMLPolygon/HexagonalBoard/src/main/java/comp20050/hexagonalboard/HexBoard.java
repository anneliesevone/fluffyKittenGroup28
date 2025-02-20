package comp20050.hexagonalboard;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import java.util.Arrays;

public class HexBoard {
    private static final double HEX_SIZE = 26;  // Side length of hexagon
    private static final double HEX_HEIGHT = HEX_SIZE * Math.sqrt(3);  // Height of hexagon
    private static final int BASE_SIZE = 7;  // Number of rings

    public static Pane createHexagonalBoard() {
        Pane pane = new Pane();
        double centerX = 600;  // Center X of the board
        double centerY = 350;  // Center Y of the board

        int hexCount = 1;  // ID tracking

        // Loop through axial coordinates to build the hexagonal board
        for (int q = -BASE_SIZE + 1; q < BASE_SIZE; q++) {
            int r1 = Math.max(-BASE_SIZE + 1, -q - BASE_SIZE + 1);
            int r2 = Math.min(BASE_SIZE - 1, -q + BASE_SIZE - 1);
            for (int r = r1; r <= r2; r++) {
                double x = centerX + HEX_SIZE * 1.5 * q;
                double y = centerY + HEX_HEIGHT * (r + q / 2.0);

                Polygon hex = createHexagon(x, y, "hex" + hexCount++);
                pane.getChildren().add(hex);
            }
        }

        return pane;
    }

    private static Polygon createHexagon(double x, double y, String id) {
        Polygon hex = new Polygon();
        double[] points = new double[12];

        for (int i = 0; i < 6; i++) {
            double angle = Math.PI / 3 * i;
            points[i * 2] = x + HEX_SIZE * Math.cos(angle);
            points[i * 2 + 1] = y + HEX_SIZE * Math.sin(angle);
        }

        hex.getPoints().addAll(Arrays.stream(points).boxed().toArray(Double[]::new));
        hex.setFill(Color.WHITE);
        hex.setStroke(Color.BLACK);
        hex.setId(id);
        hex.setOnMouseClicked(event -> hex.setFill(Color.BLACK));  // Click to change color

        return hex;
    }
}
