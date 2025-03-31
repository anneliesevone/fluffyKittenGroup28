package comp20050.hexagonalboard;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.Objects;

public class HexGame {
    private static final double HEX_SIZE = 26;
    private static final double HEX_HEIGHT = HEX_SIZE * Math.sqrt(3);
    private static final int BASE_SIZE = 7;

    private boolean isPlayerOneTurn = true;  // Track turns
    private ImageView turnIcon; // The image that represents the turn indicator
    private Label turnLabel;

    public void startGame(Stage stage) {
        BorderPane root = new BorderPane();
        Pane hexBoard = createHexagonalBoard();

        turnLabel = new Label("Your turn: Player 1");
        turnLabel.setStyle("-fx-font-size: 16px; -fx-padding: 10px;");

        // Load player icons from resources
        Image player1Icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/comp20050/hexagonalboard/player1.png")));
        Image player2Icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/comp20050/hexagonalboard/player2.png")));
        turnIcon = new ImageView(player1Icon);

        // Style the turn indicator icon
        turnIcon.setFitWidth(50);
        turnIcon.setFitHeight(50);


        // Enable drag-and-drop on the turn indicator
        turnIcon.setOnDragDetected(event -> {
            Dragboard db = turnIcon.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putImage(turnIcon.getImage());
            db.setContent(content);
            event.consume();
        });

        // Exit Button
        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> Platform.exit());

        // Layout
        AnchorPane topPane = new AnchorPane();
        turnIcon.setLayoutX(30); // Set X position
        turnIcon.setLayoutY(29.5); // Set Y position

        turnLabel.setLayoutX(80); // Position label next to icon
        turnLabel.setLayoutY(30);

        topPane.getChildren().addAll(turnIcon, turnLabel);
        root.setTop(topPane);

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


        // Only temporarily highlight on hover if unoccupied
        hex.setOnMouseEntered(event -> {
            if (!Boolean.TRUE.equals(hex.getProperties().get("occupied"))) {
                hex.setFill(Color.LIGHTYELLOW);  // Highlight colour
            }//check if hex is not already occupied using .get("occupied") property
            //if empty fill with highlight colour
        });

        hex.setOnMouseExited(event -> {
            if (!Boolean.TRUE.equals(hex.getProperties().get("occupied"))) {
                hex.setFill(Color.WHITE);  // Reset to default
            }
        });

        hex.setOnMouseClicked(event -> handleHexClick(hex));

        return hex;
    }

    private void handleHexClick(Polygon hex) {
        if (Boolean.TRUE.equals(hex.getProperties().get("occupied"))) {
            return;
        }

        placeToken(hex);
    }

    private void handleDragDrop(DragEvent event, Polygon hex) {
        if (Boolean.TRUE.equals(hex.getProperties().get("occupied"))) {
            return;
        }

        Dragboard db = event.getDragboard();
        if (db.hasImage()) {
            placeToken(hex);
            event.setDropCompleted(true);
        } else {
            event.setDropCompleted(false);
        }
        event.consume();
    }

    private void placeToken(Polygon hex) {
        double centerX = 0, centerY = 0;
        var points = hex.getPoints();
        for (int i = 0; i < points.size(); i += 2) {
            centerX += points.get(i);
            centerY += points.get(i + 1);
        }
        centerX /= ((double) points.size() / 2);
        centerY /= ((double) points.size() / 2);

        double tokenRadius = HEX_SIZE * 0.6;
        Circle token = new Circle(centerX, centerY, tokenRadius);
        token.setFill(isPlayerOneTurn ? Color.BLUE : Color.RED);
        token.setStroke(Color.BLACK);

        Pane parent = (Pane) hex.getParent();
        parent.getChildren().add(token);

        hex.getProperties().put("occupied", true);
        hex.setFill(Color.WHITE); //background set white after token place

        // Switch turns and update the turn icon
        isPlayerOneTurn = !isPlayerOneTurn;
        turnLabel.setText("Your turn: " + (isPlayerOneTurn ? "Player 1" : "Player 2"));
        Image newIcon = isPlayerOneTurn
                ? new Image(Objects.requireNonNull(getClass().getResourceAsStream("/comp20050/hexagonalboard/player1.png")))
                : new Image(Objects.requireNonNull(getClass().getResourceAsStream("/comp20050/hexagonalboard/player2.png")));
        turnIcon.setImage(newIcon);
    }
}