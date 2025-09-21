/**
 * @author Jakob Puc
 *
 * */
package main.logic;

import java.io.File;
import java.util.Arrays;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import main.gui.Input;
import main.gui.Procesor;
import main.gui.AOut;
import main.gui.SevenSegmentDisplay;

public class InterConnect {

	// Emz1001 and its realated objects
	private Emz1001 procesor;
	private Procesor procesorGUI;
	private Task<Void> procesorTask;
	private Thread procesorThread;
	private SevenSegmentDisplay display;

	private boolean[] pinsI = new boolean[4];
	private boolean[] pinsK = new boolean[4];

	private Input input;
	private AOut aOut;

	private boolean procesorRunning = false;
	private boolean deBugStarted = false;

	// second scene
	private TextArea textAreaRAM;
	private Button nextStep;
	private TextField executionCountField;
	private Label lblACC, lblE, lblBU, lblBL, lblPPR, lblPBR, lblPC, lblSP;
	private Label lblCarry, lblFlag1, lblFlag2, lblPPFlag, lblSecondsFlag;
	private Label lblFloatingMode, lblInvertedPolarity;
	private Label lblSelectedK, lblSelectedI, lblInputK, lblInputI;
	private Label lblPinsD, lblPinsA, lblStateD, lblStateA, lblLatchD, lblLatchA;
	private Label lblStack0, lblStack1, lblStack2, lblStack3;

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

		this.procesor = new Emz1001(selectedFile);
		this.procesor.setInterConnect(this);
		System.out.println("Procesor created");
	}

	// init for proseor
	public void initProcesor(File selectedFile, float setScaleX, float setScaleY, int setLayoutX, int setLayoutY) {
		this.procesorGUI = new Procesor();
		this.procesorGUI.setScaleX(setScaleX);
		this.procesorGUI.setScaleY(setScaleY);
		this.procesorGUI.setLayoutX(setLayoutX);
		this.procesorGUI.setLayoutY(setLayoutY);

		this.procesor = new Emz1001(selectedFile);
		this.procesor.setInterConnect(this);
		System.out.println("Procesor created");
		// initProcesorTask(selectedFile, run);
	}

	private void initProcesorTask(boolean run) {
		System.out.println("Procesor task init. And status of run " + run);

		this.procesorTask = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				System.out.println("procesor created");
				procesor.run(run);
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

	public void initDebugWindow(Stage deBugStage, Pane deBugRoot, Scene deBugScene) {
		deBugStage.setWidth(900);
		deBugStage.setHeight(700);

		// start of AI
		// REGISTERS - Column 1
		Label lblRegisters = new Label("REGISTERS:");
		lblRegisters.setLayoutX(10);
		lblRegisters.setLayoutY(200);
		lblRegisters.setFont(Font.font("Arial", FontWeight.BOLD, 12));

		this.lblACC = new Label("ACC: 0");
		this.lblACC.setLayoutX(10);
		this.lblACC.setLayoutY(220);

		this.lblE = new Label("E: 0");
		this.lblE.setLayoutX(10);
		this.lblE.setLayoutY(240);

		this.lblBU = new Label("BU: 0");
		this.lblBU.setLayoutX(10);
		this.lblBU.setLayoutY(260);

		this.lblBL = new Label("BL: 0");
		this.lblBL.setLayoutX(10);
		this.lblBL.setLayoutY(280);

		this.lblPPR = new Label("PPR: 0");
		this.lblPPR.setLayoutX(10);
		this.lblPPR.setLayoutY(300);

		this.lblPBR = new Label("PBR: 0");
		this.lblPBR.setLayoutX(10);
		this.lblPBR.setLayoutY(320);

		this.lblPC = new Label("PC: 0000");
		this.lblPC.setLayoutX(10);
		this.lblPC.setLayoutY(340);

		this.lblSP = new Label("SP: 0");
		this.lblSP.setLayoutX(10);
		this.lblSP.setLayoutY(360);

		// FLAGS - Column 2
		Label lblFlags = new Label("FLAGS:");
		lblFlags.setLayoutX(150);
		lblFlags.setLayoutY(200);
		lblFlags.setFont(Font.font("Arial", FontWeight.BOLD, 12));

		this.lblCarry = new Label("Carry: false");
		this.lblCarry.setLayoutX(150);
		this.lblCarry.setLayoutY(220);

		this.lblFlag1 = new Label("Flag1: false");
		this.lblFlag1.setLayoutX(150);
		this.lblFlag1.setLayoutY(240);

		this.lblFlag2 = new Label("Flag2: false");
		this.lblFlag2.setLayoutX(150);
		this.lblFlag2.setLayoutY(260);

		this.lblPPFlag = new Label("PPFlag: false");
		this.lblPPFlag.setLayoutX(150);
		this.lblPPFlag.setLayoutY(280);

		this.lblSecondsFlag = new Label("SecondsFlag: false");
		this.lblSecondsFlag.setLayoutX(150);
		this.lblSecondsFlag.setLayoutY(300);

		this.lblFloatingMode = new Label("FloatingMode: false");
		this.lblFloatingMode.setLayoutX(150);
		this.lblFloatingMode.setLayoutY(320);

		this.lblInvertedPolarity = new Label("InvertedPolarity: false");
		this.lblInvertedPolarity.setLayoutX(150);
		this.lblInvertedPolarity.setLayoutY(340);

		// STACK - Column 3
		Label lblStack = new Label("STACK:");
		lblStack.setLayoutX(350);
		lblStack.setLayoutY(200);
		lblStack.setFont(Font.font("Arial", FontWeight.BOLD, 12));

		this.lblStack0 = new Label("Stack[0]: 0");
		this.lblStack0.setLayoutX(350);
		this.lblStack0.setLayoutY(220);

		this.lblStack1 = new Label("Stack[1]: 0");
		this.lblStack1.setLayoutX(350);
		this.lblStack1.setLayoutY(240);

		this.lblStack2 = new Label("Stack[2]: 0");
		this.lblStack2.setLayoutX(350);
		this.lblStack2.setLayoutY(260);

		this.lblStack3 = new Label("Stack[3]: 0");
		this.lblStack3.setLayoutX(350);
		this.lblStack3.setLayoutY(280);

		// I/O PINS - Column 4
		Label lblIO = new Label("I/O PINS:");
		lblIO.setLayoutX(450);
		lblIO.setLayoutY(200);
		lblIO.setFont(Font.font("Arial", FontWeight.BOLD, 12));

		this.lblSelectedK = new Label("Selected K: 0");
		this.lblSelectedK.setLayoutX(450);
		this.lblSelectedK.setLayoutY(220);

		this.lblSelectedI = new Label("Selected I: 0");
		this.lblSelectedI.setLayoutX(450);
		this.lblSelectedI.setLayoutY(240);

		this.lblInputK = new Label("Input K: [0,0,0,0]");
		this.lblInputK.setLayoutX(450);
		this.lblInputK.setLayoutY(260);

		this.lblInputI = new Label("Input I: [0,0,0,0]");
		this.lblInputI.setLayoutX(450);
		this.lblInputI.setLayoutY(280);

		// PIN STATES - Second row (Y=400)
		this.lblPinsD = new Label("Pins D: [0,0,0,0,0,0,0,0]");
		this.lblPinsD.setLayoutX(10);
		this.lblPinsD.setLayoutY(400);

		this.lblPinsA = new Label("Pins A: [0,0,0,0,0,0,0,0,0,0,0,0,0]");
		this.lblPinsA.setLayoutX(10);
		this.lblPinsA.setLayoutY(420);

		// LINE STATES
		this.lblStateD = new Label("State D Lines: 0");
		this.lblStateD.setLayoutX(10);
		this.lblStateD.setLayoutY(440);

		this.lblStateA = new Label("State A Lines: 0");
		this.lblStateA.setLayoutX(10);
		this.lblStateA.setLayoutY(460);

		this.lblLatchD = new Label("Latch D Lines: 0");
		this.lblLatchD.setLayoutX(10);
		this.lblLatchD.setLayoutY(480);

		this.lblLatchA = new Label("Latch A Lines: 0");
		this.lblLatchA.setLayoutX(10);
		this.lblLatchA.setLayoutY(500);

		// Add all labels to the pane
		deBugRoot.getChildren().addAll(
				lblRegisters, this.lblACC, this.lblE, this.lblBU, this.lblBL, this.lblPPR, this.lblPBR,
				this.lblPC, this.lblSP,
				lblFlags, this.lblCarry, this.lblFlag1, this.lblFlag2, this.lblPPFlag,
				this.lblSecondsFlag,
				this.lblFloatingMode, this.lblInvertedPolarity,
				lblIO, this.lblSelectedK, this.lblSelectedI, this.lblInputK, this.lblInputI,
				this.lblPinsD, this.lblPinsA,
				this.lblStateD, this.lblStateA, this.lblLatchD, this.lblLatchA,
				lblStack, this.lblStack0, this.lblStack1, this.lblStack2, this.lblStack3);

		// end of AI
		this.executionCountField = new TextField("1");
		this.executionCountField.setLayoutX(600);
		this.executionCountField.setLayoutY(70);
		this.executionCountField.setPrefWidth(100);
		deBugRoot.getChildren().add(this.executionCountField);

		this.nextStep = new Button("Next step");
		this.nextStep.setLayoutY(10);
		this.nextStep.setLayoutX(600);
		this.nextStep.setOnAction((ActionEvent e) -> {
			try {
				String text = this.executionCountField.getText();
				long steps = Long.parseLong(text);
				if (steps > 0) {
					this.procesor.setExecuteTo(steps);
				} else {
					System.out.println("Please enter a positive number");
				}
			} catch (NumberFormatException ex) {
				System.out.println("Invalid number format: " + this.executionCountField.getText());
			}
		});

		deBugRoot.getChildren().add(this.nextStep);

		this.deBugStarted = true;
		this.textAreaRAM = new TextArea();
		this.textAreaRAM.setPrefRowCount(8);
		this.textAreaRAM.setPrefColumnCount(50);
		this.textAreaRAM.setFont(Font.font("Courier New", 12));
		this.textAreaRAM.setWrapText(false);
		this.textAreaRAM.setEditable(false);
		this.textAreaRAM.setLayoutX(10);
		this.textAreaRAM.setLayoutY(10);
		this.textAreaRAM.setPrefSize(550, 120);
		deBugRoot.getChildren().add(textAreaRAM);
		deBugStage.show();

	}

	public void startProcesorTask(boolean run) {
		initProcesorTask(run);
		this.procesorThread = new Thread(this.procesorTask);
		this.procesorThread.setDaemon(true);
		this.procesorThread.start();
		System.out.println("Procesor task started.");
		this.procesorRunning = true;
	}

	// Add these fields
	private long lastUpdateTime = 0;
	private final long UPDATE_INTERVAL = 16; // ~60 FPS (16ms between updates)

	// ... rest of your existing code ...

	public void updateFromProcesor(boolean[] aOut, boolean dDir, boolean[] dOut) {
		long currentTime = System.currentTimeMillis();

		// Only update UI at most 60 times per second
		if (currentTime - lastUpdateTime >= UPDATE_INTERVAL) {
			lastUpdateTime = currentTime;
			Platform.runLater(() -> {
				this.aOut.setPins(aOut);
				this.display.setDdir(dDir);
				if (dDir) {
					this.procesor.setPinsD(this.display.getPins());
				} else {
					this.display.setPins(dOut);
				}

				// update debug pannel if exists
				if (this.deBugStarted == true) {
					updateTextAreaRam();
					updateDebugLabels();
				}
			});

		}
	}

	public void updateTextAreaRam() {
		byte[][] RAM = procesor.getRAM();
		StringBuilder sb = new StringBuilder();
		sb.append("Dump of RAM\n");
		for (int i = 0; i < RAM.length; i++) {
			for (int j = 0; j < RAM[i].length; j++) {
				sb.append(String.format("%4s", Integer.toBinaryString(RAM[i][j]))
						.replace(' ', '0')).append(" ");
			}
			sb.append("\n");
		}
		String s = sb.toString();
		this.textAreaRAM.setText(s);
	}

	// Method to update all debug labels (call this periodically)
	public void updateDebugLabels() {
		// Update all the labels with current values from this.procesor object
		this.lblACC.setText("ACC: " + this.procesor.getACC());
		this.lblE.setText("E: " + this.procesor.getE());
		this.lblBU.setText("BU: " + this.procesor.getBU());
		this.lblBL.setText("BL: " + this.procesor.getBL());
		this.lblPPR.setText("PPR: " + this.procesor.getPPR());
		this.lblPBR.setText("PBR: " + this.procesor.getPBR());
		this.lblPC.setText("PC: " + String.format("0x%04X", this.procesor.getProgramCounter()));
		this.lblSP.setText("SP: " + this.procesor.getStackPointer());

		this.lblCarry.setText("Carry: " + this.procesor.getCarry());
		this.lblFlag1.setText("Flag1: " + this.procesor.getFlag1());
		this.lblFlag2.setText("Flag2: " + this.procesor.getFlag2());
		this.lblPPFlag.setText("PPFlag: " + this.procesor.getPPFlag());
		this.lblSecondsFlag.setText("SecondsFlag: " + this.procesor.getSecondsFlag());
		this.lblFloatingMode.setText("FloatingMode: " + this.procesor.getFloatingModeOnDLines());
		this.lblInvertedPolarity.setText("InvertedPolarity: " + this.procesor.getInvertedPolarityOnDLines());

		this.lblSelectedK.setText("Selected K: " + this.procesor.getSelectedK());
		this.lblSelectedI.setText("Selected I: " + this.procesor.getSelectedI());
		this.lblInputK.setText("Input K: " + Arrays.toString(this.procesor.getPinsK()));
		this.lblInputI.setText("Input I: " + Arrays.toString(this.procesor.getPinsI()));
		this.lblPinsD.setText("Pins D: " + Arrays.toString(this.procesor.getPinsD()));
		this.lblPinsA.setText("Pins A: " + Arrays.toString(this.procesor.getPinsA()));
		this.lblStateD.setText("State D Lines: " + this.procesor.getStateOfDLines());
		this.lblStateA.setText("State A Lines: " + this.procesor.getStateOfALines());
		this.lblLatchD.setText("Latch D Lines: " + this.procesor.getLachOnDLines());
		this.lblLatchA.setText("Latch A Lines: " + this.procesor.getLachInALines());

		int[] stack = this.procesor.getStack();
		lblStack0.setText("Stack[0]: " + stack[0]);
		lblStack1.setText("Stack[1]: " + stack[1]);
		lblStack2.setText("Stack[2]: " + stack[2]);
		lblStack3.setText("Stack[3]: " + stack[3]);
	}

	// set methods
	public void setPinsI(boolean[] pins) {
		this.procesor.setPinsI(pins);
	}

	public void setPinsK(boolean[] pins) {
		this.procesor.setPinsK(pins);
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

	public AOut getAOut() {
		return this.aOut;
	}

	public SevenSegmentDisplay getSevenSegmentDisplay() {
		return this.display;
	}

	public boolean getProcesorRunning() {
		return this.procesorRunning;
	}

}
