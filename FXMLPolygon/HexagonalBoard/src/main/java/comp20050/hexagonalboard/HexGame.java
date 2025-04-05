package comp20050.hexagonalboard;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class HexGame {
    private static final double HEX_SIZE = 26;
    private static final double HEX_HEIGHT = HEX_SIZE * Math.sqrt(3);
    private static final int BASE_SIZE = 7;

    private boolean isPlayerOneTurn = true;  // Track turns
    private ImageView turnIcon; // The image that represents the turn indicator
    private Label turnLabel;
    private Label messageLabel; // New label for displaying messages (instead of error popups)
    private int player1TokensPlaced = 0;
    private int player2TokensPlaced = 0;

    public void startGame(Stage stage) {
        BorderPane root = new BorderPane();
        Pane hexBoard = createHexagonalBoard();

        turnLabel = new Label("Your turn: Player 1");
        turnLabel.setStyle("-fx-font-size: 16px; -fx-padding: 10px;");

        // New message label for in-game messages
        messageLabel = new Label();
        messageLabel.setStyle("-fx-font-size: 16px; -fx-padding: 10px;");
        messageLabel.setText(""); // Start with an empty message

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

        // Layout for the top
        AnchorPane topPane = new AnchorPane();
        turnIcon.setLayoutX(30); // Set X position
        turnIcon.setLayoutY(29.5); // Set Y position

        turnLabel.setLayoutX(80); // Position label next to icon
        turnLabel.setLayoutY(30);

        topPane.getChildren().addAll(turnIcon, turnLabel);
        root.setTop(topPane);

        // Layout for the bottom: message text and exit button in an HBox
        HBox bottomBox = new HBox(10);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.getChildren().addAll(messageLabel, exitButton);
        root.setBottom(bottomBox);

        root.setCenter(hexBoard);

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
                hex.setFill(Color.LIGHTYELLOW);  // Highlight color
            }
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
        Object occupied = hex.getProperties().get("occupied");

        if (Boolean.TRUE.equals(occupied)) {
            System.out.println("Triggering invalid move alert!");
            showInvalidMoveMessage("That hexagon is already occupied!");
            return; // Don’t place the token
        }

        placeToken(hex);
    }

    private void handleDragDrop(javafx.scene.input.DragEvent event, Polygon hex) {
        Object occupied = hex.getProperties().get("occupied");

        if (Boolean.TRUE.equals(occupied)) {
            showInvalidMoveMessage("You can't drop a token there — it's already occupied!");
            event.setDropCompleted(false);
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
        hex.getProperties().put("player", isPlayerOneTurn ? "Player1" : "Player2");
        hex.setFill(Color.WHITE);

        if (isPlayerOneTurn) {
            player1TokensPlaced++;
        } else {
            player2TokensPlaced++;
        }

        boolean enlargesGroup = false;
        for (Object obj : parent.getChildren()) {
            if (obj instanceof Polygon otherHex && otherHex != hex) {
                if (Boolean.TRUE.equals(otherHex.getProperties().get("occupied"))
                        && isAdjacent(hex, otherHex)
                        && (isPlayerOneTurn ? "Player1".equals(otherHex.getProperties().get("player"))
                        : "Player2".equals(otherHex.getProperties().get("player")))) {
                    enlargesGroup = true;
                    break;
                }
            }
        }

        if (enlargesGroup) {
            int newGroupSize = computeGroupSize(hex, isPlayerOneTurn ? "Player1" : "Player2", parent);
            boolean touchesOpponent = false;

            for (Object obj : parent.getChildren()) {
                if (obj instanceof Polygon otherHex && otherHex != hex) {
                    if (Boolean.TRUE.equals(otherHex.getProperties().get("occupied"))
                            && isAdjacent(hex, otherHex)) {
                        String otherPlayer = (String) otherHex.getProperties().get("player");
                        if (!otherPlayer.equals(isPlayerOneTurn ? "Player1" : "Player2")) {
                            touchesOpponent = true;
                            int oppGroupSize = computeGroupSize(otherHex, otherPlayer, parent);
                            if (oppGroupSize >= newGroupSize) {
                                parent.getChildren().remove(token);
                                hex.getProperties().put("occupied", false);
                                hex.getProperties().remove("player");
                                System.out.println("Illegal capturing move: adjacent opponent group is not smaller.");
                                return;
                            }
                        }
                    }
                }
            }

            if (!touchesOpponent) {
                parent.getChildren().remove(token);
                hex.getProperties().put("occupied", false);
                hex.getProperties().remove("player");
                // Instead of showing an error box, update the text message in the game.
                showInvalidMoveMessage("Illegal capturing move: must touch an opponent stone.");
                return;
            }

            for (Object obj : new ArrayList<>(parent.getChildren())) {
                if (obj instanceof Polygon otherHex && otherHex != hex) {
                    if (Boolean.TRUE.equals(otherHex.getProperties().get("occupied"))
                            && isAdjacent(hex, otherHex)) {
                        String otherPlayer = (String) otherHex.getProperties().get("player");
                        if (!otherPlayer.equals(isPlayerOneTurn ? "Player1" : "Player2")) {
                            removeToken(otherHex, parent);
                        }
                    }
                }
            }

            // WIN CHECK after capturing
            String opponent = isPlayerOneTurn ? "Player2" : "Player1";
            boolean opponentHasTokens = false;

            for (Object obj : parent.getChildren()) {
                if (obj instanceof Polygon otherHex) {
                    if (Boolean.TRUE.equals(otherHex.getProperties().get("occupied"))) {
                        String owner = (String) otherHex.getProperties().get("player");
                        if (opponent.equals(owner)) {
                            opponentHasTokens = true;
                            break;
                        }
                    }
                }
            }

            if ((player1TokensPlaced > 0 && player2TokensPlaced > 0) && !opponentHasTokens) {
                String winner = isPlayerOneTurn ? "Player 1 (BLUE)" : "Player 2 (RED)";
                turnLabel.setText("🎉 " + winner + " wins!");
                turnIcon.setImage(null);
                return;
            }

            System.out.println("Capturing move performed. Make another move!");
            return;
        }

        // WIN CHECK after regular (non-capturing) move
        String opponent = isPlayerOneTurn ? "Player2" : "Player1";
        boolean opponentHasTokens = false;

        for (Object obj : parent.getChildren()) {
            if (obj instanceof Polygon otherHex) {
                if (Boolean.TRUE.equals(otherHex.getProperties().get("occupied"))) {
                    String owner = (String) otherHex.getProperties().get("player");
                    if (opponent.equals(owner)) {
                        opponentHasTokens = true;
                        break;
                    }
                }
            }
        }

        if ((player1TokensPlaced > 0 && player2TokensPlaced > 0) && !opponentHasTokens) {
            String winner = isPlayerOneTurn ? "Player 1 (BLUE)" : "Player 2 (RED)";
            turnLabel.setText("🎉 " + winner + " wins!");
            turnIcon.setImage(null);
            return;
        }

        // Clear any previous error message when a valid move is made.
        messageLabel.setText("");

        isPlayerOneTurn = !isPlayerOneTurn;
        turnLabel.setText("Your turn: " + (isPlayerOneTurn ? "Player 1" : "Player 2"));

        Image newIcon = isPlayerOneTurn
                ? new Image(Objects.requireNonNull(getClass().getResourceAsStream("/comp20050/hexagonalboard/player1.png")))
                : new Image(Objects.requireNonNull(getClass().getResourceAsStream("/comp20050/hexagonalboard/player2.png")));
        turnIcon.setImage(newIcon);
    }

    // Helper method to check if two hexes are adjacent based on center distance
    private boolean isAdjacent(Polygon hex1, Polygon hex2) {
        double x1 = 0, y1 = 0;
        for (int i = 0; i < hex1.getPoints().size(); i += 2) {
            x1 += hex1.getPoints().get(i);
            y1 += hex1.getPoints().get(i + 1);
        }
        x1 /= (hex1.getPoints().size() / 2.0);
        y1 /= (hex1.getPoints().size() / 2.0);

        double x2 = 0, y2 = 0;
        for (int i = 0; i < hex2.getPoints().size(); i += 2) {
            x2 += hex2.getPoints().get(i);
            y2 += hex2.getPoints().get(i + 1);
        }
        x2 /= (hex2.getPoints().size() / 2.0);
        y2 /= (hex2.getPoints().size() / 2.0);

        double distance = Math.hypot(x1 - x2, y1 - y2);
        return distance < HEX_SIZE * 2.0; // Rough threshold for adjacency
    }

    // Helper method to compute the group size for a given player starting at a hex
    private int computeGroupSize(Polygon hex, String player, Pane parent) {
        java.util.List<Polygon> queue = new ArrayList<>();
        java.util.List<Polygon> visited = new ArrayList<>();
        queue.add(hex);
        while (!queue.isEmpty()) {
            Polygon current = queue.remove(0);
            if (!visited.contains(current)) {
                visited.add(current);
                for (Object obj : parent.getChildren()) {
                    if (obj instanceof Polygon) {
                        Polygon otherHex = (Polygon) obj;
                        if (!visited.contains(otherHex)
                                && isAdjacent(current, otherHex)
                                && Boolean.TRUE.equals(otherHex.getProperties().get("occupied"))
                                && player.equals(otherHex.getProperties().get("player"))) {
                            queue.add(otherHex);
                        }
                    }
                }
            }
        }
        return visited.size();
    }

    // Helper method to remove a token from a hex and mark it as unoccupied
    private void removeToken(Polygon hex, Pane parent) {
        double centerX = 0, centerY = 0;
        var points = hex.getPoints();
        for (int i = 0; i < points.size(); i += 2) {
            centerX += points.get(i);
            centerY += points.get(i + 1);
        }
        centerX /= ((double) points.size() / 2);
        centerY /= ((double) points.size() / 2);
        for (Object obj : new ArrayList<>(parent.getChildren())) {
            if (obj instanceof Circle) {
                Circle token = (Circle) obj;
                double dx = token.getCenterX() - centerX;
                double dy = token.getCenterY() - centerY;
                if (Math.hypot(dx, dy) < HEX_SIZE) {
                    parent.getChildren().remove(token);
                    break;
                }
            }
        }
        hex.getProperties().put("occupied", false);
        hex.getProperties().remove("player");
        hex.setFill(Color.WHITE);
    }

    // Instead of showing an alert, update the messageLabel text to display the message.
    private void showInvalidMoveMessage(String message) {
        Platform.runLater(() -> messageLabel.setText(message));
    }
}
