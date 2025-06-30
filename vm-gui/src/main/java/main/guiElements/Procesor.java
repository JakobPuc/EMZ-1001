package main.guiElements;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Procesor extends BorderPane {

	public Procesor() {
		// CPU body in the center
		Pane cpuBody = new Pane();
		cpuBody.setMinSize(200, 400);
		cpuBody.setStyle("-fx-background-color: gray; -fx-border-color: black; -fx-border-width: 2;");
		this.setCenter(cpuBody);

		// Left side pins (1–20)
		VBox leftPins = new VBox(5);
		leftPins.setAlignment(Pos.CENTER_RIGHT);
		for (int i = 1; i <= 20; i++) {
			leftPins.getChildren().add(createPin(i));
		}

		// Right side pins (21–40)
		VBox rightPins = new VBox(5);
		rightPins.setAlignment(Pos.CENTER_LEFT);
		for (int i = 21; i <= 40; i++) {
			rightPins.getChildren().add(createPin(i));
		}

		this.setLeft(leftPins);
		this.setRight(rightPins);
	}

	private StackPane createPin(int number) {
		Circle pin = new Circle(10, Color.LIGHTBLUE);
		pin.setStroke(Color.BLACK);
		Label label = new Label(String.valueOf(number));
		label.setStyle("-fx-font-size: 10;");
		return new StackPane(pin, label);
	}
}
