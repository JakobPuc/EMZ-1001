package main.gui;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import main.logic.InterConnect;

public class SevenSegmentDisplay extends Group {
	private final Rectangle[] segments = new Rectangle[7];
	private final Circle dot;
	private final Rectangle background;

	private boolean[] pinStates = new boolean[8];
	private boolean inputD;
	private final Circle[] pinDots = new Circle[8];
	private final Text[] pinLabels = new Text[8];
	private static String[] PIN_NAMES = { "A", "B", "C", "D", "E", "F", "G", "DP" };

	private InterConnect connect;

	public SevenSegmentDisplay(double x, double y, double scale) {
		double hLen = 20 * scale;
		double vLen = 40 * scale;
		double thickness = 5 * scale;

		this.background = new Rectangle(x - thickness, y - thickness, (hLen + 25 * scale),
				((vLen + 10 * scale) * 2) + thickness);
		this.background.setFill(Color.BLACK);

		this.segments[0] = createSegment(x + thickness, y, hLen, thickness); // A
		this.segments[1] = createSegment(x + hLen + thickness, y + thickness, thickness, vLen); // B
		this.segments[2] = createSegment(x + hLen + thickness, y + vLen + 2 * thickness, thickness, vLen); // C
		this.segments[3] = createSegment(x + thickness, y + 2 * vLen + thickness / 2, hLen, thickness); // D
		this.segments[4] = createSegment(x, y + vLen + 2 * thickness, thickness, vLen); // E
		this.segments[5] = createSegment(x, y + thickness, thickness, vLen); // F
		this.segments[6] = createSegment(x + thickness, y + vLen + thickness, hLen, thickness); // G

		this.getChildren().add(background);
		this.getChildren().addAll(segments);

		dot = new Circle(x + hLen + 3 * thickness, y + 2 * vLen + 2 * thickness, thickness / 2);
		dot.setFill(Color.DARKRED);
		dot.setVisible(false);
		this.getChildren().add(dot);

		for (int i = 0; i < 8; i++) {
			double px = x + hLen + 40 * scale;
			double py = y + i * 20 * scale;

			Circle pin = new Circle(px, py, 5 * scale);
			pin.setFill(Color.BLACK);
			pin.setStroke(Color.BLACK);

			Text label = new Text(px + 10 * scale + 10, py + 4 * scale, PIN_NAMES[i]);
			label.setFont(Font.font("Courier New", 12 * scale));
			label.setFill(Color.BLACK);

			pinDots[i] = pin;
			pinLabels[i] = label;
			pinDots[i].centerXProperty().bind(segments[0].xProperty().add((hLen + 40) * scale));
			pinDots[i].centerYProperty().bind(segments[0].yProperty().add((i * 20) * scale));

			int index = i; // required for lambda

			pin.setOnMouseClicked(e -> {
				if (this.inputD) {
					pinStates[index] = !pinStates[index];
					pin.setFill(pinStates[index] ? Color.RED : Color.BLACK);
				}
			});

			/*
			 * pin.setOnMouseClicked(e -> {
			 * pinStates[index] = !pinStates[index];
			 * pin.setFill(pinStates[index] ? Color.LIMEGREEN : Color.DARKGRAY);
			 * });
			 */

			this.getChildren().addAll(pin, label);
		}
		setPins(new boolean[] { false, false, false, false, false, false, false, false });

	}

	private Rectangle createSegment(double x, double y, double width, double height) {
		Rectangle r = new Rectangle(x, y, width, height);
		r.setArcWidth(3);
		r.setArcHeight(3);
		r.setFill(Color.DARKRED);
		return r;
	}

	public void setPins(boolean[] pins) {
		if (this.inputD == false) {
			for (int i = 0; i < 7 && i < pins.length; i++) {
				segments[i].setVisible(pins[i]);
				pinDots[i].setFill(pins[i] ? Color.LIMEGREEN : Color.BLACK);
			}
			if (pins.length >= 8) {
				dot.setVisible(pins[7]);
				pinDots[7].setFill(pins[7] ? Color.LIMEGREEN : Color.BLACK);
			}
			// this.pinStates = pins;
		}

	}

	public boolean[] getPins() {
		return this.pinStates;
	}

	public void setDdir(boolean b) {
		this.inputD = b;
	}

	public void setInterConnect(InterConnect connect) {
		this.connect = connect;
	}

}
