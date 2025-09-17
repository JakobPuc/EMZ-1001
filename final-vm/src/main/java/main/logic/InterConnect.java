/**
 * @author Jakob Puc
 *
 * */
package main.logic;

import java.io.File;
import java.util.Arrays;

import javafx.application.Platform;
import javafx.concurrent.Task;
import main.gui.Input;
import main.gui.Procesor;
import main.gui.AOut;
import main.gui.SevenSegmentDisplay;

public class InterConnect {

	// Emz1001 and its realated objects
	private Emz1001 procesor;
	private Procesor procesorGUI;
	private Task procesorTask;
	private Thread procesorThread;
	private SevenSegmentDisplay display;

	private boolean[] pinsI = new boolean[4];
	private boolean[] pinsK = new boolean[4];

	private Input input;
	private AOut aOut;

	public InterConnect() {
		System.out.println("Class InterConnect created");
	}

	// default init for procesor
	public void initProcesor(File selectedFile) {
		this.procesorGUI = new Procesor();
		this.procesorGUI.setScaleX(0.5);
		this.procesorGUI.setScaleY(0.5);
		this.procesorGUI.setLayoutX(200);
		this.procesorGUI.setLayoutY(200);
		initProcesorTask(selectedFile);
	}

	// init for proseor
	public void initProcesor(File selectedFile, float setScaleX, float setScaleY, int setLayoutX, int setLayoutY) {
		this.procesorGUI = new Procesor();
		this.procesorGUI.setScaleX(setScaleX);
		this.procesorGUI.setScaleY(setScaleY);
		this.procesorGUI.setLayoutX(setLayoutX);
		this.procesorGUI.setLayoutY(setLayoutY);
		initProcesorTask(selectedFile);
	}

	private void initProcesorTask(File selectedFile) {
		System.out.println("Procesor task init.");
		this.procesor = new Emz1001(selectedFile);
		this.procesor.setInterConnect(this);
		this.procesorTask = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				System.out.println("procesor created");
				procesor.run(false);
				return null;
			}
		};
	}

	public void initInput(int x, int y, int size) {
		this.input = new Input(x, y, size);
		this.input.setInterConnect(this);
	}

	public void initAOut(int x, int y, int size) {
		this.aOut = new AOut(x, y, size);
	}

	public void initSevenSegmentDisplay(double x, double y, double scale) {
		this.display = new SevenSegmentDisplay(x, y, scale);
	}

	public void startProcesorTask() {
		this.procesorThread = new Thread(this.procesorTask);
		this.procesorThread.setDaemon(true);
		this.procesorThread.start();
		System.out.println("Procesor task started.");
	}

	public void updateFromProcesor(boolean[] aOut, boolean dDir, boolean[] dOut) {
		Platform.runLater(() -> {
			this.aOut.setPins(aOut);
			this.display.setDdir(dDir);
			if (dDir) {
				this.procesor.setPinsD(this.display.getPins());
			} else {
				this.display.setPins(dOut);
			}
		});

		this.procesor.setPinsI(this.input.getPinsI());
		this.procesor.setPinsK(this.input.getPinsK());

		System.out.println("A:" + Arrays.toString(aOut) + " D dir " + dDir + " dOut: " + Arrays.toString(dOut));
	}
	// set methods

	// get methods
	public Procesor getProcesorGUI() {
		return this.procesorGUI;
	}

	public Input getInput() {
		return this.input;
	}

	public Emz1001 getEmz1001() {
		return this.procesor;
	}

	public AOut getAOut() {
		return this.aOut;
	}

	public SevenSegmentDisplay getSevenSegmentDisplay() {
		return this.display;
	}

}
