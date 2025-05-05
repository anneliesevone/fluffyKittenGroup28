package comp20050.hexagonalboard;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import java.util.*;

public class HexGame {
    private static final double HEX_SIZE = 23;
    private static final double HEX_HEIGHT = HEX_SIZE * Math.sqrt(3);
    private static final int BASE_SIZE = 7;

    boolean isPlayerOneTurn = true;  // Track turns
    boolean gameOver = false;        // Flag to indicate game over

    protected ImageView turnIcon;              // Turn indicator image
    Label turnLabel;
    Label messageLabel;              // Label for messages (instead of error dialogs)
    Button restartButton;            // Restart button (hidden until game over)
    Label scoreLabel; // Shows the win count
    Button undoButton;

    int player1TokensPlaced = 0;
    int player2TokensPlaced = 0;
    int player1Wins = 0;
    int player2Wins = 0;


    private Deque<Move> moveHistory = new ArrayDeque<>();


    void resetState(Pane board) {  // Helper for test
        // Remove all token circles
        board.getChildren().removeIf(node -> node instanceof Circle);

        // Clear occupancy properties on each hex
        for (Node node : board.getChildren()) {
            if (node instanceof Polygon) {
                Map<Object,Object> props = node.getProperties();
                props.remove("occupied");
                props.remove("player");
            }
        }

        // Reset game‐logic state
        isPlayerOneTurn    = true;
        gameOver           = false;
        player1TokensPlaced = 0;
        player2TokensPlaced = 0;
        moveHistory.clear();
    }


    // Records one move: where it went and what it captured.
    private static class Move {
        Polygon hex;
        Circle token;
        List<Polygon> capturedHexes;
        List<Circle> capturedTokens;

        Move(Polygon hex, Circle token,
             List<Polygon> capturedHexes,
             List<Circle> capturedTokens) {
            this.hex = hex;
            this.token = token;
            this.capturedHexes = capturedHexes;
            this.capturedTokens = capturedTokens;
        }
    }

    public void startGame(Stage stage) {
        // Capture current full-screen state
        boolean wasFullScreen = stage.isFullScreen();

        // Reset game state
        isPlayerOneTurn = true;
        gameOver = false;
        player1TokensPlaced = 0;
        player2TokensPlaced = 0;
        moveHistory.clear(); //clear undo history when reset

        BorderPane root = new BorderPane();

        // hex board
        Pane hexBoard = createHexagonalBoard();
        hexBoard.setMaxHeight(1000); // Limit board height so it doesn’t cover buttons

        // Create label to indicate which player's turn it is
        turnLabel = new Label("Your turn: Player 1");
        turnLabel.setStyle("-fx-font-size: 16px; -fx-padding: 10px;");

        // Label for in-game messages
        messageLabel = new Label();
        messageLabel.setStyle("-fx-font-size: 16px; -fx-padding: 10px;");
        messageLabel.setText("");
        // Label to track wins
        scoreLabel = new Label("Wins - Player 1 (Yellow): " + player1Wins + " | Player 2 (RED): " + player2Wins);
        scoreLabel.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");


        // Load player icons from resources
        Image player1Icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/comp20050/hexagonalboard/player1.png")));
        Image player2Icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/comp20050/hexagonalboard/player2.png")));
        turnIcon = new ImageView(player1Icon);
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
        exitButton.setOnAction(e -> {
            System.out.println("Exit button clicked");
            Platform.exit();
            System.exit(0); // Ensure the app exits even if Platform.exit fails
        });
        //undo button
        undoButton = new Button("Undo");
        undoButton.setDisable(true);
        undoButton.setOnAction(e -> undoLastMove());


        // Restart Button (hidden until game over)
        restartButton = new Button("Restart");
        restartButton.setVisible(false);
        restartButton.setOnAction(e -> {
            System.out.println("Restart button clicked");
            Stage currentStage = (Stage) restartButton.getScene().getWindow();
            startGame(currentStage);
        });

        // Layout for the top: turn indicator and turn label
        HBox topBox = new HBox(10, turnIcon, turnLabel);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setStyle("-fx-padding: 10px; -fx-background-color: black;");
        turnLabel.setStyle("-fx-font-size: 16px; -fx-padding: 10px; -fx-text-fill: white;");


        // Layout for the bottom: message label, restart and exit buttons
        HBox bottomBox = new HBox(20, messageLabel, scoreLabel, undoButton, restartButton, exitButton);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setStyle("-fx-padding: 20px; -fx-background-color: black;");
        messageLabel.setStyle("-fx-font-size: 16px; -fx-padding: 10px; -fx-text-fill: white;");
        scoreLabel.setStyle("-fx-font-size: 14px; -fx-padding: 10px; -fx-text-fill: white;");

        bottomBox.setPrefHeight(30);

        root.setTop(topBox);
        root.setCenter(hexBoard);
        root.setBottom(bottomBox);

        // Create scene using fixed dimensions (or use stage.getWidth/Height if preferred)
        Scene scene = new Scene(root, 650, 650);
        stage.setTitle("HexOust Game");
        stage.setScene(scene);
        stage.show();

        // Reapply full-screen state if it was enabled
        if (wasFullScreen) {
            stage.setFullScreen(true);
        }
    }


    Pane createHexagonalBoard() {
        Pane pane = new Pane();
        pane.setStyle("-fx-background-color: black;");

        double centerX = 500;
        double centerY = 230;

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
        hex.setFill(Color.BLACK);
        hex.setStroke(Color.YELLOW);
        hex.setStrokeWidth(2);


        // Highlight on hover if unoccupied
        hex.setOnMouseEntered(event -> {
            if (!Boolean.TRUE.equals(hex.getProperties().get("occupied"))) {
                hex.setFill(Color.DARKGREY);
            }
        });
        hex.setOnMouseExited(event -> {
            if (!Boolean.TRUE.equals(hex.getProperties().get("occupied"))) {
                hex.setFill(Color.BLACK);
            }
        });
        hex.setOnMouseClicked(event -> handleHexClick(hex));

        return hex;
    }

    void handleHexClick(Polygon hex) {
        // Ignore clicks if game is over.
        if (gameOver) return;

        Object occupied = hex.getProperties().get("occupied");
        if (Boolean.TRUE.equals(occupied)) {
            System.out.println("Triggering invalid move message!");
            showInvalidMoveMessage("That hexagon is already occupied!");
            return;
        }
        placeToken(hex);
    }

    /*
    Places a token or captures opponent stones according to game rules.
    Draws the token at the center of the clicked hex.
    Updates internal occupied/player flags and placement counters.
    Performs multi-hex capture when your group touches smaller opponent groups.
   */
    private void placeToken(Polygon hex) {

        // Calculate crenter of hex and draw token
        double centerX = 0, centerY = 0;
        var points = hex.getPoints();
        for (int i = 0; i < points.size(); i += 2) {
            centerX += points.get(i);
            centerY += points.get(i + 1);
        }
        centerX /= (points.size() / 2.0);
        centerY /= (points.size() / 2.0);

        //Create and style the token (circle)
        double tokenRadius = HEX_SIZE * 0.6;
        Circle token = new Circle(centerX, centerY, tokenRadius);
        token.setFill(isPlayerOneTurn ? Color.YELLOW : Color.RED);
        token.setStroke(Color.BLACK);

        //Add token to the board pane
        Pane parent = (Pane) hex.getParent();
        parent.getChildren().add(token);

        // mark the hex as occupied by current player
        hex.getProperties().put("occupied", true);
        String me = isPlayerOneTurn ? "Player1" : "Player2";
        hex.getProperties().put("player", me);
        hex.setFill(Color.BLACK);//Repaint the hex black to show it’s no longer empty

        // Increment the player's placed-tokens counter
        if (isPlayerOneTurn) {
            player1TokensPlaced++;
        } else {
            player2TokensPlaced++;
        }

        // prepare undo tracking
        List<Polygon> capturedHexes = new ArrayList<>();
        List<Circle> capturedTokens = new ArrayList<>();

        // Compute your connected group of stones after placement
        List<Polygon> myGroup = computeGroup(hex, me, parent);

        // If your group grew larger (size > 1), attempt to capture adjacent smaller opponent groups
        if (myGroup.size() > 1) {
            int myGroupSize = myGroup.size();
            String them = isPlayerOneTurn ? "Player2" : "Player1";

            // Will hold each distinct opponent connected component touching your group
            List<List<Polygon>> oppGroups = new ArrayList<>();
            // avoid processing the same opponent hex or group more than once
            Set<Polygon> seenOpp = new HashSet<>();

            // For each hex in your newly formed group
            for (Polygon p : myGroup) {
                //look at every node in the board plane
                for (Object obj : parent.getChildren()) {
                    if (!(obj instanceof Polygon)) continue;// skip non-hex nodes
                    Polygon other = (Polygon) obj;
                    boolean isOpp = Boolean.TRUE.equals(other.getProperties().get("occupied"))
                            && them.equals(other.getProperties().get("player"));
                    if (isOpp && isAdjacent(p, other) && !seenOpp.contains(other)) {
                        // get the full connected component for this opponent stone
                        List<Polygon> group = computeGroup(other, them, parent);
                        oppGroups.add(group);
                        seenOpp.addAll(group);
                    }
                }
            }

            //  Illegal capture if no opponent group is touched
            if (oppGroups.isEmpty()) {
                undoPlacement(token, hex);
                showInvalidMoveMessage("Illegal capturing move: must touch at least one opponent group.");
                return;
            }
            //  Illegal capture if any touched opponent group is not strictly smaller
            for (List<Polygon> opp : oppGroups) {
                if (opp.size() >= myGroupSize) {
                    undoPlacement(token, hex);
                    showInvalidMoveMessage("Illegal capturing move: adjacent opponent group is not smaller.");
                    return;
                }
            }
            // capture
            // Remove all stones in each smaller opponent group
            for (List<Polygon> opp : oppGroups) {
                for (Polygon h : opp) {
                    Circle rem = removeToken(h, parent);
                    capturedHexes.add(h);
                    capturedTokens.add(rem);
                }
            }

            // win-check after capture
            if (checkWin(parent)) {
                recordMove(hex, token, capturedHexes, capturedTokens);
                endGame();
                return;
            }

            messageLabel.setText("Capturing move performed. Make another move!");
            recordMove(hex, token, capturedHexes, capturedTokens);
            return;
        }

        // reset message on normal capture move
        messageLabel.setText("");
        isPlayerOneTurn = !isPlayerOneTurn;
        updateTurnIcon();
        recordMove(hex, token, capturedHexes, capturedTokens);
    }



    // Push onto history stack and enable Undo.
    private void recordMove(Polygon hex, Circle token, List<Polygon> caps, List<Circle> capsTok) {
        moveHistory.push(new Move(hex, token, caps, capsTok));
        undoButton.setDisable(false);
    }

    // Returns true and updates win counts if the other side has no tokens.
    boolean checkWin(Pane parent) {
        String opp = isPlayerOneTurn ? "Player2" : "Player1";
        boolean hasOpp = false;
        for (Object obj : parent.getChildren()) {
            if (obj instanceof Polygon h) {
                if (Boolean.TRUE.equals(h.getProperties().get("occupied"))) {
                    String owner = (String) h.getProperties().get("player");
                    if (opp.equals(owner)) {
                        hasOpp = true;
                        break;
                    }
                }
            }
        }
        if (!hasOpp && player1TokensPlaced > 0 && player2TokensPlaced > 0) {
            if (isPlayerOneTurn) player1Wins++;
            else player2Wins++;
            scoreLabel.setText(
                    "Wins - P1 (YELLOW): " + player1Wins +
                            " | P2 (RED): " + player2Wins
            );
            return true;
        }
        return false;
    }

    // Undo the very last move.
    void undoLastMove() {
        if (moveHistory.isEmpty()) return;
        Move last = moveHistory.pop();
        Pane parent = (Pane) last.hex.getParent();

        // remove placed token
        parent.getChildren().remove(last.token);
        last.hex.getProperties().put("occupied", false);
        last.hex.getProperties().remove("player");
        last.hex.setFill(Color.BLACK);

        // restore captured tokens
        for (int i = 0; i < last.capturedHexes.size(); i++) {
            Polygon h = last.capturedHexes.get(i);
            Circle c = last.capturedTokens.get(i);
            parent.getChildren().add(c);
            h.getProperties().put("occupied", true);
            String owner = isPlayerOneTurn ? "Player2" : "Player1";
            h.getProperties().put("player", owner);
            h.setFill(Color.BLACK);
        }

        // fix counts and turn
        if (isPlayerOneTurn) player2TokensPlaced--;
        else player1TokensPlaced--;
        isPlayerOneTurn = !isPlayerOneTurn;
        updateTurnIcon();
        //Update the on‐screen turn icon and clear any messages.
        messageLabel.setText("");
        undoButton.setDisable(moveHistory.isEmpty());
    }

    private void updateTurnIcon() {
        turnLabel.setText("Your turn: " + (isPlayerOneTurn ? "Player 1" : "Player 2"));
        String path = isPlayerOneTurn
                ? "/comp20050/hexagonalboard/player1.png"
                : "/comp20050/hexagonalboard/player2.png";
        turnIcon.setImage(new Image(Objects.requireNonNull(
                getClass().getResourceAsStream(path)
        )));
    }


    // Finalizes the win and shows it on the turn label.
    private void endGame() {
        String winner = isPlayerOneTurn ? "Player 1 (YELLOW)" : "Player 2 (RED)";
        turnLabel.setText("🐝 " + winner + " wins! 🐝");
        turnIcon.setImage(null);
        gameOver = true;
        restartButton.setVisible(true);
    }


    //Helper returns the connected component (list of hexes) for the given player starting at startHex.
    List<Polygon> computeGroup(Polygon startHex, String player, Pane parent) {
        List<Polygon> queue = new ArrayList<>();
        List<Polygon> visited = new ArrayList<>();
        queue.add(startHex);

        while (!queue.isEmpty()) {
            Polygon current = queue.remove(0);
            if (visited.contains(current)) continue;
            visited.add(current);

            for (Object obj : parent.getChildren()) {
                if (!(obj instanceof Polygon)) continue;
                Polygon other = (Polygon) obj;
                if (visited.contains(other)) continue;
                // Only follow same-player occupied adjacent hexes
                if (!Boolean.TRUE.equals(other.getProperties().get("occupied"))) continue;
                if (!player.equals(other.getProperties().get("player"))) continue;
                if (!isAdjacent(current, other)) continue;
                queue.add(other);
            }
        }
        return visited;
    }

    // helper reverts a placement when a capturing move is illegal.
    private void undoPlacement(Circle token, Polygon hex) {
        Pane parent = (Pane) hex.getParent();
        parent.getChildren().remove(token);
        hex.getProperties().put("occupied", false);
        hex.getProperties().remove("player");
        hex.setFill(Color.BLACK);
    }

    // Helper method to check if two hexes are adjacent based on center distance.
    boolean isAdjacent(Polygon hex1, Polygon hex2) {
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
        return distance < HEX_SIZE * 2.0;
    }

    // Helper method to remove a token from a hex and mark it as unoccupied.
    private Circle removeToken(Polygon hex, Pane parent) {
        double centerX = 0, centerY = 0;
        List<Double> points = hex.getPoints();
        for (int i = 0; i < points.size(); i += 2) {
            centerX += points.get(i);
            centerY += points.get(i + 1);
        }
        centerX /= (points.size() / 2.0);
        centerY /= (points.size() / 2.0);

        Circle found = null;
        List<javafx.scene.Node> copy = new ArrayList<>(parent.getChildren());
        for (javafx.scene.Node node : copy) {
            if (node instanceof Circle c) {
                double dx = c.getCenterX()  - centerX;
                double dy = c.getCenterY()- centerY;
                if (Math.hypot(dx, dy) < HEX_SIZE) {
                    found = c;
                    parent.getChildren().remove(c);
                    break;
                }
            }
        }
        hex.getProperties().put("occupied", false);
        hex.getProperties().remove("player");
        hex.setFill(Color.BLACK);
        return found;
    }

    // Update the message label with the provided message.
    private void showInvalidMoveMessage(String message) {
        Platform.runLater(() -> messageLabel.setText(message));
    }


}
