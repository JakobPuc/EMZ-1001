package main.logic;

public class RamUpdater {
	private int length;
	private int width;

	public RamUpdater() {
	}

	public RamUpdater(int length, int width) {
		if (width < 0 || length < 0) {
			throw new NumberFormatException("Negative size");
		} else {
			this.length = length;
			this.width = width;
		}
	}
}
