package src;

import src.gui.Processor;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

	@Override
	public void start(Stage primaryStage) {
		Group root = new Group();
		Scene scene = new Scene(root, 720, 480); // set width and height

		Processor processor = new Processor();

		root.getChildren().add(processor.getRootGroup());
		primaryStage.setTitle("EMZ-1001 vm");
		primaryStage.setScene(scene); // set the scene on the stage
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
