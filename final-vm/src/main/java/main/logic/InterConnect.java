/**
 * @author Jakob Puc
 *
 * */
package main.logic;

import java.io.File;

import javafx.concurrent.Task;
import main.gui.Input;
import main.gui.Procesor;

public class InterConnect {

	// Emz1001 and its realated objects
	private Emz1001 procesor;
	private Procesor procesorGUI;
	private Task procesorTask;
	private Thread procesorThread;

	private boolean[] pinsI = new boolean[4];
	private boolean[] pinsK = new boolean[4];

	private Input input;

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
		this.procesorTask = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				procesor = new Emz1001(selectedFile);
				procesor.run(false);
				return null;
			}
		};
	}

	public void startProcesorTask() {
		this.procesorThread = new Thread(this.procesorTask);
		this.procesorThread.start();
	}

	// set methods
	public void setInput(int x, int y, int size) {
		this.input = new Input(x, y, size);
		this.input.setInterConnect(this);
	}

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

}
