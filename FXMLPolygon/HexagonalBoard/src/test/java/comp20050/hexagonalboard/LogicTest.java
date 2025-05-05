package comp20050.hexagonalboard;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Polygon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogicTest {
    private HexGame game;
    private Pane board;

    @BeforeEach
    void setUp() {
        game = new HexGame();
        board = game.createHexagonalBoard();
        game.resetState(board);
    }

    @Test
    void adjacentHexesAreDetected() {
        List<Polygon> hexes = board.getChildren().stream()
                .filter(n -> n instanceof Polygon)
                .map(Polygon.class::cast)
                .toList();
        Polygon a = hexes.get(0);
        Polygon b = hexes.get(1);
        assertTrue(game.isAdjacent(a, b), "Neighboring hexes should be adjacent");
    }

    @Test
    void nonAdjacentHexesAreNotDetected() {
        List<Polygon> hexes = board.getChildren().stream()
                .filter(n -> n instanceof Polygon)
                .map(Polygon.class::cast)
                .toList();
        Polygon a = hexes.get(0);
        Polygon c = hexes.get(10);
        assertFalse(game.isAdjacent(a, c), "Distant hexes should not be adjacent");
    }

    @Test
    void computeGroupSingle() {
        Polygon h = board.getChildren().stream()
                .filter(n -> n instanceof Polygon)
                .map(Polygon.class::cast)
                .findFirst().orElseThrow();
        h.getProperties().put("occupied", true);
        h.getProperties().put("player", "Player1");

        List<Polygon> group = game.computeGroup(h, "Player1", board);
        assertEquals(1, group.size(), "Isolated placement → group size 1");
    }

    @Test
    void computeGroupExcludesOpponentStones() {
        List<Polygon> hexes = board.getChildren().stream()
                .filter(n -> n instanceof Polygon)
                .map(Polygon.class::cast)
                .toList();
        Polygon h1 = hexes.get(0);
        Polygon h2 = hexes.get(1);

        // Occupy h1 with Player1, h2 with Player2
        h1.getProperties().put("occupied", true);
        h1.getProperties().put("player", "Player1");
        h2.getProperties().put("occupied", true);
        h2.getProperties().put("player", "Player2");

        List<Polygon> group = game.computeGroup(h1, "Player1", board);
        assertEquals(1, group.size(), "Group should exclude opponent stones");
    }

    @Test
    void computeGroupMulti() {
        List<Polygon> hexes = board.getChildren().stream()
                .filter(n -> n instanceof Polygon)
                .map(Polygon.class::cast)
                .toList();
        Polygon h1 = hexes.get(0);
        Polygon h2 = hexes.get(1);

        // Occupy two adjacent hexes
        h1.getProperties().put("occupied", true);
        h1.getProperties().put("player", "Player1");
        h2.getProperties().put("occupied", true);
        h2.getProperties().put("player", "Player1");

        List<Polygon> group = game.computeGroup(h1, "Player1", board);
        assertEquals(2, group.size(), "Adjacent same‐color stones → group size 2");
    }

    @Test
    void computeGroupChain() {
        // Occupy three in a chain so computeGroup picks all three
        List<Polygon> hexes = board.getChildren().stream()
                .filter(n -> n instanceof Polygon)
                .map(Polygon.class::cast)
                .toList();
        Polygon h1 = hexes.get(0);
        Polygon h2 = hexes.get(1);
        Polygon h3 = hexes.get(2);

        h1.getProperties().put("occupied", true);
        h1.getProperties().put("player", "Player1");
        h2.getProperties().put("occupied", true);
        h2.getProperties().put("player", "Player1");
        h3.getProperties().put("occupied", true);
        h3.getProperties().put("player", "Player1");

        List<Polygon> group = game.computeGroup(h1, "Player1", board);
        assertEquals(3, group.size(), "Connected chain of 3 stones → group size 3");
    }

    @Test
    void checkWinRequiresBothPlayersPlayed() {
        game.player1TokensPlaced = 1;
        game.player2TokensPlaced = 0;
        // Even though there are no Player2 stones, Player2 hasn't played → no win
        assertFalse(game.checkWin(board),
                "Win should not be declared until both players have placed at least one token");
    }

    @Test
    void checkWinFalseWithOpponentsRemaining() {
        game.player1TokensPlaced = 1;
        game.player2TokensPlaced = 1;
        // Place a single Player2 stone
        Polygon opp = board.getChildren().stream()
                .filter(n -> n instanceof Polygon)
                .map(Polygon.class::cast)
                .findFirst().orElseThrow();
        opp.getProperties().put("occupied", true);
        opp.getProperties().put("player", "Player2");

        assertFalse(game.checkWin(board),
                "Win should be false if the opponent still has stones on the board");
    }
}
