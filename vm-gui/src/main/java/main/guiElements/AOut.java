package main.guiElements;

import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.scene.text.*;

public class AOut extends Group {
	private Circle[] pins = new Circle[12];
	private Text[] lable = new Text[12];

	public AOut(int x, int y, int size) {
		for (int i = 0; i < 12; i++) {
			Circle tmp = new Circle(x, (i * size * 2 + 10) + y, size);
			Text tmpText = new Text("A" + Integer.toString(i));
			tmpText.setLayoutX(x + 10 + size);
			tmpText.setLayoutY(y + (i * size * 2 + 10) + size);
			tmpText.setScaleX(0.75);
			tmpText.setScaleY(0.75);
			lable[i] = tmpText;
			tmp.setFill(javafx.scene.paint.Color.GREY);
			pins[i] = tmp;
			this.getChildren().add(tmp);
			this.getChildren().add(tmpText);
		}
	}

	public Circle[] getPins() {
		return this.pins;
	}

	public void setPins(boolean[] pins) {
		for (int i = 0; (i < 12) && (i < pins.length); i++) {
			if (pins[i] == true) {
				this.pins[i].setFill(javafx.scene.paint.Color.RED);
			} else {
				this.pins[i].setFill(javafx.scene.paint.Color.GREY);
			}
		}
	}
}
