package src.gui;

import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Processor {
	private String procesor;
	private int shape = 0; // 0 = rectangle 1 = squere
	private int numOfPins;
	private Rectangle mainBody = new Rectangle();
	private Group root = new Group();
	private Label text;

	// pins
	private Rectangle[] pins;
	private boolean[] pinState;
	private boolean IOPins;

	// deafult constructor for EMZ-1001
	public Processor() {
		this.procesor = "EMZ-1001";
		this.numOfPins = 40;
		this.pins = new Rectangle[this.numOfPins];
		this.pinState = new boolean[this.numOfPins];

		this.mainBody.setWidth(50);
		this.mainBody.setHeight(150);
		this.mainBody.setFill(Color.BLACK);
		this.mainBody.setLayoutX(340);
		this.mainBody.setLayoutY(100);

		this.root.getChildren().add(this.mainBody);

		int n = 0;
		int ofset = 0;
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 20; j++) {
				this.pins[n] = new Rectangle();
				this.pins[n].setHeight(5);
				this.pins[n].setWidth(5);
				this.pins[n].setLayoutX(ofset + this.mainBody.getLayoutX());
				this.pins[n].setLayoutY(j * 7.5 + this.mainBody.getLayoutY() + 0.75);
				this.pins[n].setFill(Color.GOLD);
				this.root.getChildren().add(this.pins[n]);
				n++;
			}
			ofset += (int) this.mainBody.getWidth() - 5;
		}
		this.text = new Label("EMZ-1001");
		this.text.setRotate(-90);
		this.text.setLayoutX(this.mainBody.getLayoutX() + this.mainBody.getWidth() / 8);
		this.text.setLayoutY(this.mainBody.getLayoutY() + this.mainBody.getHeight() / 2);
		this.text.setTextFill(Color.BLUE);
		this.root.getChildren().add(this.text);
	}

	public int getNumOfPins() {
		return this.numOfPins;
	}

	public int getShape() {
		return this.shape;
	}

	public Group getRootGroup() {
		return this.root;
	}
}
