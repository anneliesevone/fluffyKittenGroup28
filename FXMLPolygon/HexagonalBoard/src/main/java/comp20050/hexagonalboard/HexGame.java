package comp20050.hexagonalboard;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import java.util.Arrays;

public class HexGame {
    private static final double HEX_SIZE = 26;
    private static final double HEX_HEIGHT = HEX_SIZE * Math.sqrt(3);
    private static final int BASE_SIZE = 7;

    private boolean isPlayerOneTurn = true;  // Track turns
    private Label turnLabel;

    public void startGame(Stage stage) {
        BorderPane root = new BorderPane();
        Pane hexBoard = createHexagonalBoard();

        // Turn Label
        turnLabel = new Label("Your turn: Player 1");
        turnLabel.setStyle("-fx-font-size: 16px; -fx-padding: 10px;");

        // Exit Button
        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> Platform.exit());

        // Layout
        root.setTop(turnLabel);
        root.setCenter(hexBoard);
        root.setBottom(exitButton);

        Scene scene = new Scene(root, 650, 650);
        stage.setTitle("HexOust Game");
        stage.setScene(scene);
        stage.show();
    }

    private Pane createHexagonalBoard() {
        Pane pane = new Pane();
        double centerX = 500;
        double centerY = 300;

        for (int q = -BASE_SIZE + 1; q < BASE_SIZE; q++) {
            int r1 = Math.max(-BASE_SIZE + 1, -q - BASE_SIZE + 1);
            int r2 = Math.min(BASE_SIZE - 1, -q + BASE_SIZE - 1);
            for (int r = r1; r <= r2; r++) {
                double x = centerX + HEX_SIZE * 1.5 * q;
                double y = centerY + HEX_HEIGHT * (r + q / 2.0);

                Polygon hex = createHexagon(x, y);
                pane.getChildren().add(hex);
            }
        }
        return pane;
    }

    private Polygon createHexagon(double x, double y) {
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
        hex.setOnMouseClicked(event -> handleHexClick(hex));

        return hex;
    }

    private void handleHexClick(Polygon hex) {
        if (!hex.getFill().equals(Color.WHITE)) return; // Ignore already clicked hexes

        // Mark hex for the current player
        hex.setFill(isPlayerOneTurn ? Color.BLUE : Color.RED);

        // Switch turns
        isPlayerOneTurn = !isPlayerOneTurn;
        turnLabel.setText("Your turn: " + (isPlayerOneTurn ? "Player 1" : "Player 2"));
    }
}

