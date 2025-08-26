package main.logic;

public class Conector {

	private Emz1001 src;
	private boolean[] destD;
	private boolean[] destA;
	private boolean[] inpI;
	private boolean[] inpK;
	private boolean inputD; // if true = input, false = output
	private boolean pinsA = false;
	private boolean pinsD = false;
	private boolean pinsI = false;
	private boolean pinsK = false;

	public Conector(Emz1001 src, boolean[] dest) {
		if (src != null) {
			this.src = src;
		}
		if (dest != null) {
			this.destD = dest;
		}
	}

	public Conector(Emz1001 src, boolean[] dest, String pins) {
		if (src != null) {
			this.src = src;
		}
		if (dest != null) {
		}

		if (pins.contains("A"))
			this.pinsA = true;
		if (pins.contains("D"))
			this.pinsD = true;
		if (pins.contains("I"))
			this.pinsI = true;
		if (pins.contains("K"))
			this.pinsK = true;

	}

	public void run() {
		while (true) {

			if (this.pinsA == true) {
				this.destA = this.src.getPinsA();
			}
			if (this.pinsD == true) {
				if (this.inputD == true) {
					this.destD = this.src.getPinsD();
				} else {
					this.src.setPinsD(destD);
				}
			}

			if (this.pinsI == true) {
				this.src.setPinsI(inpI);
			}
			if (this.pinsK == true) {
				this.src.setPinsK(inpK);
			}

		}
	}

	public boolean[] getDestD() {
		return this.destD;
	}

	public void setPinsD(boolean[] D) {
		if (this.inputD == true) {
			this.destD = D;
		}
	}
}
