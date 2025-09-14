/**
 * @author Jakob Puc
 *
 * Atempt at implementation generic GUI procesor
 */
package main.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Procesor extends BorderPane {
	private Pane cpuBody;
	private VBox leftPins;
	private VBox rightPins;

	public Procesor() {
		// CPU body
		cpuBody = new Pane();
		cpuBody.setMinSize(200, 400);
		cpuBody.setStyle("-fx-background-color: black; -fx-border-color: black; -fx-border-width: 2;");
		this.setCenter(cpuBody);

		// left pins (1-20)
		leftPins = new VBox(5);
		leftPins.setAlignment(Pos.CENTER_LEFT);
		for (int i = 1; i <= 20; i++) {
			leftPins.getChildren().add(createPin(i));
		}

		// right pins (21-40)
		rightPins = new VBox(5);
		rightPins.setAlignment(Pos.CENTER_LEFT);
		for (int i = 21; i <= 40; i++) {
			rightPins.getChildren().add(createPin(i));
		}
		this.setLeft(leftPins);
		this.setRight(rightPins);
	}

	private StackPane createPin(int number) {
		Circle pin = new Circle(10, Color.GOLD);
		pin.setStroke(Color.BLACK);
		Label label = new Label(String.valueOf(number));
		label.setStyle("-fx-font-size: 15;");
		return new StackPane(pin, label);
	}

	public void hide() {
		cpuBody.setVisible(false);
		leftPins.setVisible(false);
		rightPins.setVisible(false);
	}

	public void show() {
		cpuBody.setVisible(true);
		leftPins.setVisible(true);
		rightPins.setVisible(true);
	}
}
