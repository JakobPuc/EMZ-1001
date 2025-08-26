package main.guiElements;

import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;

public class Inputs extends Group {
	private Circle[] pins = new Circle[8];
	private Text[] labels = new Text[8];

	private boolean[] pinsI = new boolean[4];
	private boolean[] pinsK = new boolean[4];

	public Inputs(int x, int y, int size) {
		// 8 pins total: I1–I4 (row 0), K1–K4 (row 1)
		for (int i = 0; i < 8; i++) {
			String prefix = (i < 4) ? "I" : "K";
			int number = (i % 4);
			String labelText = prefix + number;

			// Row & column
			int row = (i < 4) ? 0 : 1;
			int col = i % 4;

			double cx = x + col * (size * 3);
			double cy = y + row * (size * 3);

			// Circle as clickable "button"
			Circle circle = new Circle(cx, cy, size);
			circle.setFill(Color.GREY);

			// store the index
			int pinIndex = i;

			circle.setOnMouseClicked(e -> {
				System.out.println("Button " + labelText + " clicked!");
				if (pinIndex < 4) { // I pins
					pinsI[pinIndex] = !pinsI[pinIndex];
					circle.setFill(pinsI[pinIndex] ? Color.RED : Color.GREY);
				} else { // K pins
					int kIndex = pinIndex - 4;
					pinsK[kIndex] = !pinsK[kIndex];
					circle.setFill(pinsK[kIndex] ? Color.RED : Color.GREY);
				}
			});

			// Label text beside circle
			Text text = new Text(labelText);
			text.setLayoutX(cx + size + 5);
			text.setLayoutY(cy + 4); // small vertical align tweak
			text.setScaleX(0.9);
			text.setScaleY(0.9);

			pins[i] = circle;
			labels[i] = text;

			this.getChildren().addAll(circle, text);
		}
	}

	public Circle[] getPins() {
		return this.pins;
	}

	public void setPins(boolean[] states) {
		for (int i = 0; i < this.pins.length && i < states.length; i++) {
			if (states[i]) {
				this.pins[i].setFill(Color.RED);
			} else {
				this.pins[i].setFill(Color.GREY);
			}
		}
	}
}
