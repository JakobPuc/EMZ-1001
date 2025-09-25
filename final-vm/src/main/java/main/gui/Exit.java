package main.gui;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class Exit extends Group {

	private Circle exit;
	private Text text;

	public Exit(double x, double y, int size) {
		this.exit = new Circle(x, y, size);
		this.exit.setFill(Color.BLACK);

		this.text = new Text(x + size + 5, y + 5, "Exit");

		this.getChildren().addAll(this.exit, this.text);
	}

	public void setStatus(boolean exit) {
		if (exit) {
			this.exit.setFill(Color.RED);
		} else {
			this.exit.setFill(Color.BLACK);
		}
	}
}
