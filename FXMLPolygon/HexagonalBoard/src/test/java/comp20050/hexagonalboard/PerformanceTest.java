package comp20050.hexagonalboard;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PerformanceTest {
    @Test
    void boardCreationUnder5Seconds() {
        long start = System.currentTimeMillis();
        HexBoard.createHexagonalBoard();
        long duration = System.currentTimeMillis() - start;
        assertTrue(duration < 5_000, "Board creation should be under 5 seconds");
    }
}

