package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import main.guiElements.SevenSegmentDisplay;
import main.guiElements.Procesor;

public class App extends Application {

	@Override
	public void start(Stage stage) {
		Pane root = new Pane();

		SevenSegmentDisplay display = new SevenSegmentDisplay(100, 100, 0.7);
		Procesor cpu = new Procesor();
		cpu.setScaleX(0.5);
		cpu.setScaleY(0.5);
		cpu.setLayoutX(200);
		cpu.setLayoutY(200);

		display.setPins(new boolean[] { true, false, true, false, true, false, true, true });
		display.setDotVisible(true);

		root.getChildren().add(display);
		root.getChildren().add(cpu);

		Scene scene = new Scene(root, 250, 200);

		stage.setTitle("Demo");
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
