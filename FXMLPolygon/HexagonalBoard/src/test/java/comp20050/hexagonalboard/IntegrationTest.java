package comp20050.hexagonalboard;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class IntegrationTest {
    // Boot JavaFX once
    @BeforeAll
    static void initJavaFX() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        latch.await();
    }

    private HexGame game;
    private Pane board;

    @BeforeEach
    void setUp() {
        game = new HexGame();
        board = game.createHexagonalBoard();
        game.resetState(board);

        // Inject the UI controls so none of the logic hits NPEs
        game.turnLabel     = new Label("Your turn: Player 1");
        game.messageLabel  = new Label();
        game.scoreLabel    = new Label(
                "Wins - P1 (YELLOW): " + game.player1Wins +
                        " | P2 (RED): "     + game.player2Wins
        );
        game.undoButton    = new Button("Undo");
        game.restartButton = new Button("Restart");
        game.turnIcon      = new ImageView();
    }

    // Helpers to pick hexes on the board
    private List<Polygon> allHexes() {
        return board.getChildren().stream()
                .filter(n -> n instanceof Polygon)
                .map(Polygon.class::cast)
                .collect(Collectors.toList());
    }
    private Polygon firstHex() { return allHexes().get(0); }
    private Polygon adjacentTo(Polygon hex) {
        return allHexes().stream()
                .filter(h -> !h.equals(hex) && game.isAdjacent(hex, h))
                .findFirst().orElseThrow();
    }
    private Polygon commonNeighbor(Polygon a, Polygon b) {
        return allHexes().stream()
                .filter(h -> !h.equals(a) && !h.equals(b)
                        && game.isAdjacent(a,h)
                        && game.isAdjacent(b,h))
                .findFirst().orElseThrow();
    }
    private Polygon farAwayFrom(Polygon... exclude) {
        return allHexes().stream()
                .filter(h -> Arrays.stream(exclude)
                        .noneMatch(e -> e.equals(h) || game.isAdjacent(e,h)))
                .findFirst().orElseThrow();
    }

    @Test
    void nonCapturingMoveSwitchesTurnAndPlacesOneToken() {
        Polygon h = firstHex();
        game.handleHexClick(h);

        assertTrue(h.getProperties().containsKey("occupied"));
        assertEquals("Player1", h.getProperties().get("player"));
        assertFalse(game.isPlayerOneTurn);

        long tokens = board.getChildren().stream()
                .filter(n -> n instanceof Circle)
                .count();
        assertEquals(1, tokens);
    }

    @Test
    void illegalPlacementOnOccupiedCellDoesNothing() {
        Polygon h = firstHex();
        game.handleHexClick(h); // P1
        game.handleHexClick(h); // P2 tries same cell

        // now P2’s turn remains (false)
        assertFalse(game.isPlayerOneTurn);
    }

    @Test
    void validCaptureRemovesOpponentHexAndKeepsTurn() {
        Polygon h1 = firstHex();
        Polygon h2 = adjacentTo(h1);
        Polygon h3 = commonNeighbor(h1, h2);

        game.handleHexClick(h1); // P1
        game.handleHexClick(h2); // P2
        game.handleHexClick(h3); // P1 captures

        // your code keeps P1’s turn
        assertTrue(game.isPlayerOneTurn);
    }

    @Test
    void undoRemovesLastPlayer2MoveAndUpdatesCounters() {
        // Arrange: place one token for P1, then one for P2
        Polygon h1 = firstHex();
        Polygon h2 = adjacentTo(h1);
        game.handleHexClick(h1);  // P1
        game.handleHexClick(h2);  // P2

        // Sanity: two tokens on board, counters both 1
        long beforeTokens = board.getChildren().stream()
                .filter(n -> n instanceof Circle)
                .count();
        assertEquals(2, beforeTokens, "Should have 2 tokens before undo");
        assertEquals(1, game.player1TokensPlaced, "P1 counter should be 1");
        assertEquals(1, game.player2TokensPlaced, "P2 counter should be 1");

        // Act: undo P2's move
        game.undoLastMove();

        // Assert: only P1's token remains and P2 counter reset
        long afterTokens = board.getChildren().stream()
                .filter(n -> n instanceof Circle)
                .count();
        assertEquals(1, afterTokens, "Undo should remove the last placed token");
        assertEquals(1, game.player1TokensPlaced, "P1 counter should remain 1");
        assertEquals(0, game.player2TokensPlaced, "P2 counter should be 0 after undo");
    }



    @Test
    void winningCaptureSetsGameOverAndIncrementsWins() {
        Polygon h1 = firstHex();
        Polygon h2 = adjacentTo(h1);
        Polygon h3 = commonNeighbor(h1, h2);

        game.handleHexClick(h1);  // P1
        game.handleHexClick(h2);  // P2
        game.handleHexClick(h3);  // P1 captures last P2 → win

        assertTrue(game.gameOver);
        assertEquals(1, game.player1Wins);
    }
}
