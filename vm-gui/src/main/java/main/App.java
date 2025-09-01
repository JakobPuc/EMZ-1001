package main;

import java.io.File;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.TextArea;

import main.guiElements.SevenSegmentDisplay;
import main.guiElements.Procesor;
import main.guiElements.AOut;
import main.logic.Emz1001;
import main.logic.RamUpdater;
import main.guiElements.Inputs;
import main.logic.Conector;

public class App extends Application {

	// private File selectedFile;
	private Emz1001 procesor;
	private RamUpdater ramUpdater;
	private SevenSegmentDisplay display;
	private Procesor guiProcesor;
	private Task<Void> procesorTask;
	private Conector con;
	private Task<Void> conectorTask;
	private AOut aOut;
	private Inputs inp;

	@Override
	public void start(Stage stage) {
		Pane root = new Pane();
		guiProcesor = new Procesor();
		guiProcesor.setLayoutY(50);
		guiProcesor.hide();
		Button openButton = new Button("Open file");

		Button openRamDebug = new Button("RAM de bug");
		Button startSimulation = new Button("Start");

		openButton.setOnAction(e -> openButtonFunction(stage));
		openRamDebug.setLayoutX(80);
		openRamDebug.setOnAction(e -> openRamDebugFunction());
		startSimulation.setLayoutX(180);
		startSimulation.setOnAction(e -> startSimulationButtonFunction());

		aOut = new AOut(500, 30, 5);
		inp = new Inputs(200, 100, 10);

		root.getChildren().add(inp);
		root.getChildren().addAll(openButton, openRamDebug, startSimulation, guiProcesor);

		this.display = new SevenSegmentDisplay(100, 100, 0.7);
		root.getChildren().addAll(display, aOut);

		Scene scene = new Scene(root, 720, 720);

		stage.setTitle("Demo");
		stage.setScene(scene);
		stage.show();
	}

	@Override
	public void stop() throws Exception {
		System.exit(0);
	}

	public static void main(String[] args) {
		launch(args);
	}

	// rewriten button functions
	// Open button
	private void openButtonFunction(Stage stage) {
		FileChooser fileChooser = new FileChooser();
		File selectedFile = null;

		while (selectedFile == null) {
			selectedFile = fileChooser.showOpenDialog(stage);
		}
		initProcesor(selectedFile);
	}

	private void openRamDebugFunction() {
		Stage ramStage = new Stage();

		if (ramStage == null || !ramStage.isShowing()) {
			Pane newRamPane = new Pane();
			TextArea textArea = new TextArea();
			textArea.setEditable(false);
			newRamPane.getChildren().add(textArea);

			Scene newRamScene = new Scene(newRamPane);

			Task<Void> updateRam = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					while (procesor == null) {
						Thread.sleep(1);
					}
					ramUpdater = new RamUpdater(6, 43, procesor, textArea);
					Platform.runLater(() -> {
						ramStage.setScene(newRamScene);
						ramStage.setResizable(false);
						ramStage.setTitle("RAM Debug");
						ramStage.sizeToScene();
						ramStage.show();
					});
					ramUpdater.updateTextAreaConst();
					return null;
				}
			};

			Thread backgroundThread = new Thread(updateRam);
			backgroundThread.setDaemon(true);
			backgroundThread.start();

		} else {
			ramStage.hide();
		}
	}

	private void startSimulationButtonFunction() {
		Thread backgroundThread = new Thread(procesorTask);

		// init conector
		Thread backgroundThreadConnector = new Thread(() -> {
			while (this.procesor == null) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					return;
				}
			}
			con = new Conector(this.procesor, this.inp, this.aOut, this.display, "ADKI");
			con.run();
		});

		backgroundThread.setDaemon(true); // allows app to exit when main window closes
		backgroundThreadConnector.setDaemon(true);
		backgroundThread.start();
		backgroundThreadConnector.start();
	}

	// this function inits the gui procesor
	private void initProcesor(File selectedFile) {
		this.guiProcesor.setScaleX(0.5);
		this.guiProcesor.setScaleY(0.5);
		this.guiProcesor.setLayoutX(200);
		this.guiProcesor.setLayoutY(200);

		this.procesorTask = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				procesor = new Emz1001(selectedFile);
				procesor.run(false);
				return null;
			}
		};
	}

	private void initConector() {

		this.conectorTask = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				con.run();
				return null;
			}
		};

	}

}
