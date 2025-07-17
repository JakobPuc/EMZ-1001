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
import main.logic.Emz1001;
import main.logic.RamUpdater;

public class App extends Application {

	private File selectedFile;
	private Emz1001 procesor;
	private RamUpdater ramUpdater;
	private SevenSegmentDisplay display;
	private Procesor guiProcesor;
	private Task<Void> procesorTask;
	// private Scene newRamPanScene;
	// tmp
	private byte[][] RAM = new byte[4][16];

	@Override
	public void start(Stage stage) {
		Pane root = new Pane();
		guiProcesor = new Procesor();
		guiProcesor.setLayoutY(50);
		guiProcesor.hide();
		Button openButton = new Button("Open file");
		openButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				FileChooser fileChooser = new FileChooser();

				while (selectedFile == null) {
					selectedFile = fileChooser.showOpenDialog(stage);
				}

				// guiProcesor = new Procesor();
				guiProcesor.setScaleX(0.5);
				guiProcesor.setScaleY(0.5);
				guiProcesor.setLayoutX(200);
				guiProcesor.setLayoutY(200);
				guiProcesor.show();
				// System.out.println(selectedFile.getPath());
				// System.out.println("program started");
				procesorTask = new Task<>() {
					@Override
					protected Void call() throws Exception {
						procesor = new Emz1001(selectedFile);
						// System.out.println("LOL");
						procesor.run(false);
						// System.out.println("program finished");
						return null;
					}
				};

			}
		});

		Button openRamDebug = new Button("RAM de bug");
		openRamDebug.setLayoutX(80);
		openRamDebug.setOnAction(new EventHandler<ActionEvent>() {
			Stage ramStage = new Stage();

			@Override
			public void handle(ActionEvent e) {
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
		});

		Button startSimulation = new Button("Start");
		startSimulation.setLayoutX(180);
		startSimulation.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				Thread backgroundThread = new Thread(procesorTask);
				backgroundThread.setDaemon(true); // allows app to exit when main window closes
				backgroundThread.start();
			}

		});

		root.getChildren().addAll(openButton, openRamDebug, startSimulation, guiProcesor);

		SevenSegmentDisplay display = new SevenSegmentDisplay(100, 100, 0.7);
		root.getChildren().addAll(display);
		/*
		 * Procesor cpu = new Procesor();
		 * cpu.setScaleX(0.5);
		 * cpu.setScaleY(0.5);
		 * cpu.setLayoutX(200);
		 * cpu.setLayoutY(200);
		 * 
		 * display.setPins(new boolean[] { true, false, true, false, true, false, true,
		 * true });
		 * display.setDotVisible(true);
		 * 
		 * root.getChildren().add(display);
		 * root.getChildren().add(cpu);
		 */

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

}
