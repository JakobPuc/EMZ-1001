/**
 * @author Jakob Puc
 * 
 *A polished and corrected version of this.java not finished.
 *
 * */
package main;

import java.io.File;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import main.gui.Procesor;
import main.logic.InterConnect;
import main.gui.Input;

public class App extends Application {

	// GUI elements
	Procesor procesor;
	Button openFileButton;
	Button startSimulationButton;
	Button openDebugButton;
	Input input;

	// logic
	InterConnect conector;

	// de-bug related stuff
	Scene deBugScene;
	Pane deBugRoot;
	Stage deBugWindow;

	@Override
	public void start(Stage primaryStage) {
		Pane root = new Pane();
		this.conector = new InterConnect();

		initButtons(primaryStage, root);

		// show contents
		// root.getChildren().add(this.procesor);
		root.getChildren().addAll(this.openFileButton, this.startSimulationButton, this.openDebugButton);
		Scene scene = new Scene(root, 1200, 600);

		primaryStage.setScene(scene);
		primaryStage.setTitle("EMZ-1001 vm");
		primaryStage.show();
		// primaryStage.centerOnScreen();

	}

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void stop() throws Exception {
		System.exit(0);
	}

	// Init buttons
	private void initButtons(Stage stage, Pane pane) {
		this.openFileButton = new Button("Open File");
		this.openFileButton.setOnAction(e -> openFileButtonFunction(stage, pane));

		this.startSimulationButton = new Button("Start");
		this.startSimulationButton.setLayoutX(100);
		this.startSimulationButton.setOnAction(e -> startSimulationButtonFunction());

		this.openDebugButton = new Button("De-Bug");
		this.openDebugButton.setLayoutX(200);
		this.openDebugButton.setOnAction(e -> openDebugButtonFunction());
	}

	// TODO check file before opening it
	private void openFileButtonFunction(Stage stage, Pane pane) {
		FileChooser fileChooser = new FileChooser();
		File selectedFile = fileChooser.showOpenDialog(stage);

		if (selectedFile != null) {
			this.conector.initProcesor(selectedFile);

			this.procesor = conector.getProcesorGUI();

			// Init Input GUI
			this.conector.setInput(100, 50, 5);
			this.input = conector.getInput();

			// Add input GUI to the pane if not already added
			if (!pane.getChildren().contains(this.input)) {
				pane.getChildren().add(this.input);
			}
			if (!pane.getChildren().contains(this.procesor)) {
				pane.getChildren().add(this.procesor);
			}
		}

	}

	private void startSimulationButtonFunction() {
		if (this.conector.getEmz1001() != null) {
			this.conector.startProcesorTask();
		}
	}

	// TODO implement logic so it can open only when stat is presed
	private void openDebugButtonFunction() {
		this.deBugWindow = new Stage();

		this.deBugRoot = new Pane();
		this.deBugScene = new Scene(this.deBugRoot, 400, 300);

		this.deBugWindow.setScene(this.deBugScene);
		this.deBugWindow.show();
	}
}
