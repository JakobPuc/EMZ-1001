package main.logic;

import javafx.scene.control.TextArea;

public class RamUpdater {
	private int height;
	private int width;

	private Emz1001 src;
	private TextArea dest;

	public RamUpdater() {
	}

	public RamUpdater(int height, int width, Emz1001 src, TextArea dest) {
		if (width < 0 || height < 0) {
			throw new NumberFormatException("Negative size");
		} else {
			this.height = height;
			this.width = width;
		}
		if (src != null) {
			this.src = src;
		} else {
			throw new NullPointerException("Source can't be null");
		}
		if (dest != null) {
			this.dest = dest;
		} else {
			throw new NullPointerException("Destination can't be null");
		}
		this.dest.setPrefColumnCount(width);
		this.dest.setPrefRowCount(height);
		this.dest.setEditable(false);
		this.dest.setWrapText(false);

	}

	public void updateTextAreaConst() {
		while (true) {
			try {
				Thread.sleep(10);
			} catch (Exception e) {
				//
			}
			updateTextArea();
		}
	}

	public void updateTextArea() {
		byte[][] RAM = src.getRAM();
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
		dest.setText(s);
	}
}
