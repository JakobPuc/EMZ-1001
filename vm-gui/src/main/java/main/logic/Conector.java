package main.logic;

public class Conector {

	private Emz1001 src;
	private boolean[] dest;
	private String pinsToLookAt;
	private boolean biDirectonal;

	public Conector(Emz1001 src, boolean[] dest) {
		if (src != null) {
			this.src = src;
		}
		if (dest != null) {
			this.dest = dest;
		}
	}
}
