package main.gui;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import main.logic.InterConnect;

public class Input extends Group {
	private Circle[] pins = new Circle[8];
	private Text[] labels = new Text[8];

	private boolean[] pinsI = new boolean[4];
	private boolean[] pinsK = new boolean[4];

	private InterConnect connector;

	public Input(int x, int y, int size) {
		int spacing = size * 3; // vertical spacing

		for (int i = 0; i < 8; i++) {
			String prefix = (i < 4) ? "I" : "K";
			int index = (i < 4) ? i : i - 4;
			String labelText = prefix + index;

			int column = (i < 4) ? 0 : 2;
			int row = index;

			double cx = x + column * (size * 6);
			double cy = y + row * spacing;

			int pinIndex = i;
			Circle pin = new Circle(cx, cy, size);
			pin.setFill(Color.BLACK);
			pin.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					handlePinClick(pinIndex, pin);
					// System.out.println("Klick");
				}
			});
			Text text = new Text(labelText);
			text.setFont(Font.font("Arial", size * 2));
			text.setLayoutX(cx + size + 10);
			text.setLayoutY(cy + (size / 2.5));

			pins[i] = pin;
			labels[i] = text;

			this.getChildren().addAll(pin, text);

		}
	}

	private void handlePinClick(int pinIndex, Circle pin) {
		if (pinIndex < 4) { // pins I
			this.pinsI[pinIndex] = !this.pinsI[pinIndex];
			if (this.connector != null) {
				this.connector.setPinsI(this.pinsI);
			}
			pin.setFill(this.pinsI[pinIndex] ? Color.RED : Color.BLACK);
		} else { // pins K
			int kIndex = pinIndex - 4;
			this.pinsK[kIndex] = !this.pinsK[kIndex];
			if (this.connector != null) {
				this.connector.setPinsK(this.pinsK);
			}
			pin.setFill(this.pinsK[kIndex] ? Color.RED : Color.BLACK);
		}
	}

	public boolean[] getPinsI() {
		return this.pinsI;
	}

	public boolean[] getPinsK() {
		return this.pinsK;
	}

	public void setInterConnect(InterConnect connector) {
		this.connector = connector;
	}
}
