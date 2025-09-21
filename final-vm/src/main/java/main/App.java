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
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import main.gui.Procesor;
import main.logic.InterConnect;
import main.gui.Input;
import main.gui.AOut;
import main.gui.SevenSegmentDisplay;

public class App extends Application {

	// GUI elements
	Procesor procesor;
	Button openFileButton;
	Button startSimulationButton;
	Button openDebugButton;
	Input input;
	AOut aOut;
	SevenSegmentDisplay display;
	ToggleButton modeOfDebug;
	boolean isToggleLocked;
	boolean runDebug;
	Label debugModeLabel;

	// logic
	InterConnect conector;

	// de-bug related stuff
	Scene deBugScene;
	Pane deBugRoot;
	Stage deBugWindow;

	boolean openDeBugWindow;

	@Override
	public void start(Stage primaryStage) {
		this.openDeBugWindow = false;
		this.isToggleLocked = false;
		this.runDebug = false;

		Pane root = new Pane();
		this.conector = new InterConnect();

		initButtons(primaryStage, root);

		// Add the label
		this.debugModeLabel = new Label("Debug Mode: OFF");
		this.debugModeLabel.setLayoutX(420); // Position it next to the toggle button
		this.debugModeLabel.setLayoutY(4); // Align with the button
		this.debugModeLabel.setStyle("-fx-font-weight: bold;");
		updateDebugModeLabel();

		// show contents
		// root.getChildren().add(this.procesor);
		root.getChildren().addAll(this.openFileButton, this.startSimulationButton, this.openDebugButton,
				this.modeOfDebug, this.debugModeLabel);
		Scene scene = new Scene(root, 1200, 600);

		primaryStage.setScene(scene);
		primaryStage.setTitle("EMZ-1001 vm");
		primaryStage.show();
		// primaryStage.centerOnScreen();

	}

	public static void main(String[] args) {
		launch(args);
	}

	// TODO implement a better stoping method
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

		this.modeOfDebug = new ToggleButton("Mode of debug");
		this.modeOfDebug.setLayoutX(300);
		this.modeOfDebug.setOnAction(e -> setDebugMode());

	}

	// TODO check file before opening it
	private void openFileButtonFunction(Stage stage, Pane pane) {
		FileChooser fileChooser = new FileChooser();
		File selectedFile = fileChooser.showOpenDialog(stage);

		if (selectedFile != null) {
			this.conector.initProcesor(selectedFile);

			this.procesor = conector.getProcesorGUI();

			// Init Input GUI
			this.conector.initInput(100, 50, 5);
			this.input = conector.getInput();

			// Init aOut
			this.conector.initAOut(300, 50, 5);
			this.aOut = this.conector.getAOut();

			// init Display
			this.conector.initSevenSegmentDisplay(400, 50, 1);
			this.display = this.conector.getSevenSegmentDisplay();

			// Add input GUI to the pane if not already added
			if (!pane.getChildren().contains(this.input)) {
				pane.getChildren().add(this.input);
			}
			if (!pane.getChildren().contains(this.procesor)) {
				pane.getChildren().add(this.procesor);
			}
			if (!pane.getChildren().contains(this.aOut)) {
				pane.getChildren().add(this.aOut);
			}
			if (!pane.getChildren().contains(this.display)) {
				pane.getChildren().add(this.display);
			}
		}

	}

	private void startSimulationButtonFunction() {
		this.isToggleLocked = true;
		updateDebugModeLabel();
		// System.out.println(this.runDebug);
		System.out.println(this.conector.getEmz1001());
		if (this.conector.getEmz1001() != null) {
			this.conector.startProcesorTask(this.runDebug);
			// System.out.println("Does this work");
			if (this.openDeBugWindow == true) {
				openDebugFunction();
			}
		}
	}

	// TODO implement logic so it can open only when stat is presed
	private void openDebugButtonFunction() {
		this.openDeBugWindow = true;
		if ((this.conector.getEmz1001() != null) && (this.deBugScene == null)
				&& (this.conector.getProcesorRunning())) {
			openDebugFunction();
		}
	}

	private void openDebugFunction() {
		this.deBugWindow = new Stage();
		this.deBugRoot = new Pane();
		this.deBugScene = new Scene(this.deBugRoot, 400, 300);

		this.conector.initDebugWindow(deBugWindow, deBugRoot, deBugScene);

		this.deBugWindow.setScene(this.deBugScene);
		this.deBugWindow.setAlwaysOnTop(true);
		// this.deBugWindow.show();

	}

	private void setDebugMode() {
		if (this.isToggleLocked) {
			this.modeOfDebug.setSelected(this.runDebug);
		} else {
			// this.isToggleLocked = true;
			this.runDebug = this.modeOfDebug.isSelected();
			updateDebugModeLabel();
		}
		System.out.println(runDebug);
	}

	private void updateDebugModeLabel() {
		String status = this.modeOfDebug.isSelected() ? "ON" : "OFF";
		String lockStatus = this.isToggleLocked ? " (LOCKED)" : "";
		this.debugModeLabel.setText("Debug Mode: " + status + lockStatus);

		// Optional: Change color based on state
		if (this.modeOfDebug.isSelected()) {
			this.debugModeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: green;");
		} else {
			this.debugModeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: red;");
		}
	}
}
