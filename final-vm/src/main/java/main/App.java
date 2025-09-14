/**
 * @author Jakob Puc
 * 
 *A polished and corrected version of this.java not finished.
 *
 * */
package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import main.logic.InterConnect;

public class App extends Application {

	// scene and all the GUI elements

	InterConnect conector;

	@Override
	public void start(Stage primaryStage) {
		Pane root = new Pane();
		Scene scene = new Scene(root, 300, 200);
		conector = new InterConnect();

		primaryStage.setScene(scene);
		primaryStage.setTitle("EMZ-1001 vm");
		primaryStage.centerOnScreen();
		primaryStage.show();

	}

	public static void main(String[] args) {
		launch(args);
	}
}
