package main;

import java.io.File;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.TextArea;

import main.guiElements.SevenSegmentDisplay;
import main.guiElements.Procesor;

public class App extends Application {

	private File selectedFile;

	// tmp
	private byte[][] RAM = new byte[4][16];

	@Override
	public void start(Stage stage) {
		Pane root = new Pane();

		Button openButton = new Button("Open file");
		openButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				FileChooser fileChooser = new FileChooser();

				selectedFile = fileChooser.showOpenDialog(stage);
				// System.out.println(selectedFile.getPath());
			}
		});

		Button openRamDebug = new Button("RAM de bug");
		openRamDebug.setLayoutX(100);
		openRamDebug.setOnAction(new EventHandler<ActionEvent>() {
			boolean open = false;
			Stage stage = new Stage();

			@Override
			public void handle(ActionEvent e) {
				if (open == false) {
					Pane root = new Pane();
					TextArea textArea = new TextArea();
					textArea.setEditable(false);
					textArea.setWrapText(false);
					textArea.setText(dumpRAM());

					root.getChildren().add(textArea);
					Scene scene = new Scene(root, 600, 400);
					stage.setScene(scene);
					stage.show();
					open = !open;
				} else {
					stage.hide();
					open = !open;
				}
			}
		});

		root.getChildren().addAll(openButton, openRamDebug);
		/*
		 * SevenSegmentDisplay display = new SevenSegmentDisplay(100, 100, 0.7);
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

	public static void main(String[] args) {
		launch(args);
	}

	public String dumpRAM() {
		StringBuilder sb = new StringBuilder();
		sb.append("Dump of RAM\n");
		for (int i = 0; i < RAM.length; i++) {
			for (int j = 0; j < RAM[i].length; j++) {
				sb.append(String.format("%4s", Integer.toBinaryString(RAM[i][j]))
						.replace(' ', '0')).append(" ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}
