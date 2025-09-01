package main.logic;

import main.guiElements.Inputs;
import main.guiElements.AOut;
import main.guiElements.SevenSegmentDisplay;

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
	private boolean DDir;
	private Inputs input;
	private AOut out;
	private SevenSegmentDisplay dis;

	public Conector(Emz1001 src, boolean[] dest) {
		if (src != null) {
			this.src = src;
		}
		if (dest != null) {
			this.destD = dest;
		}
	}

	public Conector(Emz1001 src, Inputs in, AOut out, SevenSegmentDisplay dis, String pins) {
		if (src != null) {
			this.src = src;
		}
		if (in != null)
			this.input = in;
		if (out != null)
			this.out = out;
		if (dis != null)
			this.dis = dis;

		if (pins.contains("A"))
			this.pinsA = true;
		if (pins.contains("D"))
			this.pinsD = true;
		if (pins.contains("I"))
			this.pinsI = true;
		if (pins.contains("K"))
			this.pinsK = true;

	}

	// TODO i need to implement bidirectonal data travel on d pins
	public void run() {
		while (true) {
			this.DDir = this.src.getDDir();
			this.dis.setDdir(this.DDir);
			if (this.pinsA == true) {
				this.out.setPins(this.src.getPinsA());
			}

			if (this.pinsD == true) {
				if (this.inputD == true) {
					this.dis.setPins(this.src.getPinsD());
				} else {
					this.src.setPinsD(destD);
				}
			}

			if (this.pinsI == true) {
				this.src.setPinsI(this.input.getPinsI());
			}
			if (this.pinsK == true) {
				this.src.setPinsK(this.input.getPinsK());
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
