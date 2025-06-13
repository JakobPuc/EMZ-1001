package src.logic;

public class Instruction {
	private int mask;
	private int opCode;

	// default constructor for 8-bit instruction;
	public Instruction(int mask, int opCode) {
		mask = mask & 0xFF;
		opCode = opCode & 0xFF;
		this.mask = mask;
		this.opCode = opCode;
	}

	// for custom length instruction for other procesors
	public Instruction(int mask, int opCode, int length) {
		int tmp = 1;
		for (int i = 0; i < length; i++) {
			tmp = tmp << 1;
			tmp = tmp | 1;
		}
		this.mask = mask & tmp;
		this.opCode = opCode & tmp;
	}

	public int getMask() {
		return this.mask;
	}

	public int getOpCode() {
		return this.opCode;
	}
}
