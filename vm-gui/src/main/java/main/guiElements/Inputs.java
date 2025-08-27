package main.guiElements;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Inputs extends Group {
	private Circle[] pins = new Circle[8];
	private Text[] labels = new Text[8];

	private boolean[] pinsI = new boolean[4];
	private boolean[] pinsK = new boolean[4];

	public Inputs(int x, int y, int size) {
		int spacing = size * 3; // vertical spacing

		for (int i = 0; i < 8; i++) {
			String prefix = (i < 4) ? "I" : "K";
			int index = (i < 4) ? i : i - 4;
			String labelText = prefix + index;

			// column and row
			int col = (i < 4) ? 0 : 1;
			int row = index;

			double cx = x + col * (size * 6);
			double cy = y + row * spacing;

			// Circle button
			Circle circle = new Circle(cx, cy, size);
			circle.setFill(Color.GREY);

			int pinIndex = i; // capture for lambda

			circle.setOnMouseClicked(e -> {
				if (pinIndex < 4) { // I pins
					pinsI[pinIndex] = !pinsI[pinIndex];
					circle.setFill(pinsI[pinIndex] ? Color.RED : Color.GREY);
					System.out.println(labelText + " clicked! I[" + pinIndex + "] = "
							+ pinsI[pinIndex]);
				} else { // K pins
					int kIndex = pinIndex - 4;
					pinsK[kIndex] = !pinsK[kIndex];
					circle.setFill(pinsK[kIndex] ? Color.RED : Color.GREY);
					System.out.println(
							labelText + " clicked! K[" + kIndex + "] = " + pinsK[kIndex]);
				}
			});

			// Label
			Text text = new Text(labelText);
			text.setFont(Font.font("Arial", size * 2));
			text.setLayoutX(cx + size + 10);
			text.setLayoutY(cy + (size / 2.5));

			pins[i] = circle;
			labels[i] = text;

			this.getChildren().addAll(circle, text);
		}
	}

	public Circle[] getPins() {
		return pins;
	}

	public boolean[] getPinsI() {
		return pinsI;
	}

	public boolean[] getPinsK() {
		return pinsK;
	}
}
