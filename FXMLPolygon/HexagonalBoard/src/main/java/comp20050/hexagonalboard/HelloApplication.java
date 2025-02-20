package comp20050.hexagonalboard;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {
        HexGame game = new HexGame();  // Create a new HexGame instance
        game.startGame(stage);
    }

    public static void main(String[] args) {
        launch();
    }
}