/**
 * @author Jakob Puc
 * 
 *
 */
package main.logic;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import java.io.EOFException;
import main.logic.Emz1001Instructions.*;

public class Emz1001 {

	private InterConnect connect;

	// RAM and RAM registers
	private byte[][] RAM = new byte[4][16]; // each 4 bits
	private byte BU; // max 3
	private byte BL; // max 15

	// flags
	private SignalSimulator next;
	private SignalSimulator secondsTimer = new SignalSimulator();
	private boolean secondsFlag;
	private boolean carry;
	private boolean flag1;
	private boolean flag2;

	// ROM
	private int sizeOfRom; // default 1024
	private int[] ROM;
	private int programCounter; // is 13 bits long
	// private Instruction[] instructionArray;

	// stack
	private int sizeOfStack = 4;
	private int stackPointer; // max 3
	private int[] stack = new int[sizeOfStack]; // 10 bits wide

	// mode flags and laches
	private boolean floatingModeOnDLines;
	private boolean invertedPolarityOnDLines;
	private boolean PPFlag = false;
	private boolean previousPPFlag = false;
	private int procesorFreq = 900000;

	private int lachOnDLines;
	private int stateOfDLines;
	private int lachInALines; // TODO may need to implement master slave lach
	private int stateOfALines;

	// registers
	private int PPR; // prepere page register
	private int PBR; // prepere bank register
	private int ACC; // 4 bit
	private int E; // 4 bit

	private int selectedK; // which input k is selected
	private int selectedI; // which input i is selected

	// pins
	private final int numberOfPins = 40; // number of all pins including power
	private byte[] stateOfPins; // includes non io pins, io pins in difrent arreys but mirrored in here
	private boolean[] inputK = new boolean[4]; // 1 or 0
	private boolean[] inputI = new boolean[4]; // 1 or 0
	private boolean[] pinsD = new boolean[8];
	private boolean[] pinsA = new boolean[13];
	private boolean EXIT;

	// instructions
	private Instruction[] instructions;

	// default constructor sets all the features to default ones/minimal
	public Emz1001() {
		this.programCounter = 0;
		this.sizeOfRom = 1024;
		this.ROM = new int[this.sizeOfRom];
		this.secondsTimer = new SignalSimulator();

	}

	public Emz1001(File f) {

		this.programCounter = 0;
		this.sizeOfRom = 1024;
		this.ROM = new int[this.sizeOfRom];
		this.secondsTimer = new SignalSimulator();
		this.next = new SignalSimulator();
		this.stateOfPins = new byte[40];
		List<Instruction> tmpInst = getInstructionArray();
		this.instructions = new Instruction[tmpInst.size()];
		for (int i = 0; i < tmpInst.size(); i++) {
			this.instructions[i] = tmpInst.get(i);
		}
		try {
			this.next.setSignalFreqency(procesorFreq);
			this.ROM = readFile(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NegativeFrequencyException e) {
			System.out.println("Chek file configuration");
			e.printStackTrace();
		}
	}

	// function reads up to the sizeOfRom limit if file is longer than it cuts it of
	private int[] readFile(File f) throws FileNotFoundException {
		if (f.exists() == false) {
			throw new FileNotFoundException();
		}
		int[] tmpROM = new int[this.sizeOfRom];
		try (DataInputStream input = new DataInputStream(new FileInputStream(f))) {
			for (int i = 0; i < tmpROM.length; i++) {
				try {
					tmpROM[i] = input.readUnsignedByte();
				} catch (EOFException e) {
					break;
				}
			}
		} catch (IOException e) {
		}
		return tmpROM;
	}

	private List<Instruction> getInstructionArray() {
		InstructionSet set = new InstructionSet();
		int[] opCodes = set.getOpCodes();
		int[] masks = set.getMasks();
		List<Instruction> list = new LinkedList<Instruction>();
		for (int i = 0; i < opCodes.length; i++) {
			list.add(new Instruction(masks[i], opCodes[i]));
		}
		return list;
	}

	public void dumpROM() {
		System.out.println("Dump of ROM");
		for (int i = 0; i < this.ROM.length; i++) {
			if (this.ROM[i] != 0)
				System.out.println(
						String.format("0x%04X", i) + " : "
								+ String.format("%8s",
										Integer.toBinaryString(this.ROM[i]))
										.replace(' ', '0'));
		}
	}

	public byte[][] getRAM() {
		return this.RAM;
	}

	public void dumpRAM() {
		System.out.println("Dump of RAM");
		for (int i = 0; i < this.RAM.length; i++) {
			for (int j = 0; j < this.RAM[i].length; j++) {
				System.out.print(
						String.format("%4s", Integer.toBinaryString(this.RAM[i][j]))
								.replace(' ', '0') + " ");
			}
			System.out.println();
		}
	}

	// subrutine for skiping PP instruction
	private void skip() {
		while ((this.ROM[this.programCounter] & 0b11110000) == 0x60) {
			this.programCounter++;
		}
		this.programCounter++;
	}

	private static int returnIndexOfInstruction(int instruction, Instruction[] listOFInstructions) {
		int i = 0;
		for (; i < listOFInstructions.length; i++) {
			Instruction ins = listOFInstructions[i];
			if (ins.getOpCode() == (instruction & (~ins.getMask()))) {
				return i;
			}
		}
		return -1;
	}

	public void run(boolean debug) {
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
		}
		if (debug == false) {
			simulation();
		} else {
			simulationDebug();
		}
	}

	private void simulation() {
		System.out.println("Entered simulation loop");
		secondsTimer.start();
		next.start();
		System.out.println("Started timer.");
		int instruction = 0;

		if (this.connect == null) {
			System.out.println("Connector is null");
			return;
		}

		while (true) {
			// System.out.println("Entered a loop.");
			this.connect.updateFromProcesor(pinsA, floatingModeOnDLines, pinsD, EXIT);
			if (next.getFlag()) {
				// System.out.println("zz");
				next.setFlag(false);
				instruction = this.ROM[this.programCounter];
				this.programCounter++;
				if (this.programCounter >= this.sizeOfRom) {
					break;
				}
				int indexOfInstruction = returnIndexOfInstruction(instruction, this.instructions);
				if (indexOfInstruction == -1) {
					System.out.println("ERROR: Instruction does not exist");
					break;
				}
				if (this.instructions[indexOfInstruction].getMask() == 0x00) {
					executeInstruction(this.instructions[indexOfInstruction].getOpCode(), 0);
				} else {
					executeInstruction(this.instructions[indexOfInstruction].getOpCode(),
							instruction & this.instructions[indexOfInstruction].getMask());
				}
			}

			// System.out.println(Arrays.toString(this.inputI));
			// System.out.println(this.ACC);
			// dumpRAM();
		}
	}

	private long executeTo = 0;

	public void setExecuteTo(long value) {
		this.executeTo = value;
	}

	private void simulationDebug() {
		System.out.println("Entered debug simulation loop");
		// dumpROM();
		secondsTimer.start();

		if (this.connect == null) {
			System.out.println("Connector is null");
			return;
		}

		int instruction = 0;
		while (true) {
			this.connect.updateFromProcesor(pinsA, floatingModeOnDLines, pinsD, EXIT);
			if (executeTo > 0) {
				executeTo--;
				// System.out.println("aa");
				// if (next.getFlag()) {
				// next.setFlag(false);
				instruction = this.ROM[this.programCounter];
				this.programCounter++;
				if (this.programCounter >= this.sizeOfRom) {
					break;
				}
				int indexOfInstruction = returnIndexOfInstruction(instruction,
						this.instructions);
				if (indexOfInstruction == -1) {
					System.out.println("ERROR: Instruction does not exist");
					break;
				}
				if (this.instructions[indexOfInstruction].getMask() == 0x00) {
					executeInstruction(this.instructions[indexOfInstruction].getOpCode(),
							0);
				} else {
					executeInstruction(this.instructions[indexOfInstruction].getOpCode(),
							instruction & this.instructions[indexOfInstruction]
									.getMask());
				}
			}
			// }

			if (this.programCounter >= this.sizeOfRom) {
				break;
			}
		}
		dumpROM();

	}

	private void decodeDlines() {
		int tmp = this.stateOfDLines;
		// Map bits correctly: bit 0 = G, bit 1 = F, bit 2 = E, etc.
		this.pinsD[6] = (tmp & 0b00000001) != 0; // G - bit 0
		this.pinsD[5] = (tmp & 0b00000010) != 0; // F - bit 1
		this.pinsD[4] = (tmp & 0b00000100) != 0; // E - bit 2
		this.pinsD[3] = (tmp & 0b00001000) != 0; // D - bit 3
		this.pinsD[2] = (tmp & 0b00010000) != 0; // C - bit 4
		this.pinsD[1] = (tmp & 0b00100000) != 0; // B - bit 5
		this.pinsD[0] = (tmp & 0b01000000) != 0; // A - bit 6
		this.pinsD[7] = (tmp & 0b10000000) != 0; // DP - bit 7
	}

	private void decodeAlines() {
		int tmp = this.stateOfALines;
		for (int i = 0; i < 13; i++) {
			if ((tmp & 1) == 1) {
				this.pinsA[i] = true;
			} else {
				this.pinsA[i] = false;
			}
			tmp = tmp >> 1;
		}
	}

	private void encodeDLines() {
		int tmp = 0;
		if (this.pinsD[6])
			tmp |= (1 << 0); // G - bit 0
		if (this.pinsD[5])
			tmp |= (1 << 1); // F - bit 1
		if (this.pinsD[4])
			tmp |= (1 << 2); // E - bit 2
		if (this.pinsD[3])
			tmp |= (1 << 3); // D - bit 3
		if (this.pinsD[2])
			tmp |= (1 << 4); // C - bit 4
		if (this.pinsD[1])
			tmp |= (1 << 5); // B - bit 5
		if (this.pinsD[0])
			tmp |= (1 << 6); // A - bit 6
		if (this.pinsD[7])
			tmp |= (1 << 7); // DP - bit 7
		this.stateOfDLines = tmp;
	}

	private boolean previosBL = false;
	private boolean currentBL = false;
	private boolean previosLAI = false;
	private boolean currentLAI = false;

	// may be changed to public
	// this method that executes instructions
	private void executeInstruction(int opcode, int param) {
		this.secondsFlag = this.secondsTimer.getFlag();
		this.EXIT = false;
		int tmp = 0;
		this.previousPPFlag = this.PPFlag;
		switch (opcode) {
			case 0x00: // NOP
				break;
			case 0x01: // BRK not implemented treated as NOP
				break;
			case 0x02: // RT
				this.stackPointer--;
				if (this.stackPointer < 0) {
					this.stackPointer = this.sizeOfStack - 1;
				}
				tmp = this.programCounter & 0b1110000000000;
				tmp = tmp | (this.stack[this.stackPointer] & 0b1111111111);
				this.programCounter = tmp;
				break;
			case 0x03: // RTS
				this.stackPointer--;
				if (this.stackPointer < 0) {
					this.stackPointer = this.sizeOfStack - 1;
				}
				tmp = this.programCounter & 0b1110000000000;
				tmp = tmp | (this.stack[this.stackPointer] & 0b1111111111);
				this.programCounter = tmp;
				skip();
				break;
			case 0x04: // PSH
				if ((this.BL <= 12) && (this.BL >= 0)) {
					tmp = 1;
					tmp = tmp << this.BL;
					this.lachInALines = this.lachInALines | tmp;
					break;
				}
				if (this.BL == 13) {
					// SET MULTIPLEX OPERATION
					break;
				}
				if (this.BL == 14) {
					this.floatingModeOnDLines = false;
					break;
				}
				if (this.BL == 15) {
					this.lachInALines = 0b1111111111111;
					break;
				}

				break;
			case 0x05: // PSL
				if ((this.BL >= 0) && (this.BL <= 12)) {
					tmp = 1;
					tmp = tmp << this.BL;
					tmp = ~tmp;
					this.lachInALines = this.lachInALines | tmp;
					break;
				}
				if (this.BL == 13) {
					// set static operation
					break;
				}
				if (this.BL == 14) {
					this.floatingModeOnDLines = true;
					break;
				}
				if (this.BL == 15) {
					this.lachInALines = 0b0;
					break;
				}
			case 0x06: // AND
				this.ACC = this.RAM[this.BU][this.BL] & this.ACC;
				break;
			case 0x07: // SOS
				if (this.secondsFlag == true) {
					this.secondsTimer.setFlag(false);
					this.secondsFlag = false;
					skip();
				}
				break;
			case 0x08: // SBE
				if (this.BL == this.E) {
					skip();
				}
				break;
			case 0x09: // SZC
				if (this.carry == false) {
					skip();
				}

				break;
			case 0x0A: // STC
				this.carry = true;
				break;
			case 0x0B: // RSC
				this.carry = false;
				break;
			case 0x0C: // LAE
				this.ACC = this.E;
				break;
			case 0x0D: // XAE
				tmp = this.ACC;
				this.ACC = this.E;
				this.E = tmp;
				break;
			case 0x0E: // INP does somthing with IO
				if (this.floatingModeOnDLines == true) {
					this.ACC = this.stateOfDLines & 0b1111;
					this.RAM[this.BU][this.BL] = (byte) ((this.stateOfDLines >> 4) & 0b1111);
					System.out.println(Integer.toBinaryString(this.stateOfDLines));
				} else {
					// inputs data from lach to ACC and RAM
					tmp = this.lachOnDLines;
					this.ACC = tmp & 0b1111;
					tmp = tmp >> 4;
					this.RAM[this.BU][this.BL] = (byte) (tmp & 0b1111);
				}
				break;
			case 0x0F: // EUR
				if ((this.ACC & 1) == 1) {
					this.invertedPolarityOnDLines = false;
				} else {
					this.invertedPolarityOnDLines = true;
				}
				// need to add a timer for seconds flag
				if (((this.ACC >> 2) & 1) == 1) {
					this.secondsTimer.setSignalFreqency(true);
				} else {
					this.secondsTimer.setSignalFreqency(false);
				}
				break;
			case 0x10: // CMA
				this.ACC = 15 - this.ACC;
				break;
			case 0x11: // XABU
				tmp = this.BU;
				this.BU = (byte) (this.ACC & 0b11);
				this.ACC = this.ACC & 0b1100;
				this.ACC = this.ACC | tmp;
				break;
			case 0x12: // LAB
				this.ACC = this.BL;
				break;
			case 0x13: // XAB
				tmp = this.ACC;
				this.ACC = this.BL;
				this.BL = (byte) tmp;
				break;
			case 0x14: // ADCS
				if (carry == true) {
					this.ACC++;
				}
				this.ACC = this.ACC + this.RAM[this.BU][this.BL];
				if (this.ACC <= 15) {
					this.carry = false;
					skip();
				} else {
					this.ACC = this.ACC & 0b1111;
					this.carry = true;
				}
				break;
			case 0x15: // XOR
				this.ACC = this.ACC ^ this.RAM[this.BU][this.BL];
				break;

			case 0x16: // ADD
				this.ACC = (this.ACC + this.RAM[this.BU][this.BL]) & 0b1111;
				break;
			case 0x17: // SAM
				if (this.ACC == this.RAM[this.BU][this.BL]) {
					skip();
				}
				break;
			case 0x18: // DISB
				this.floatingModeOnDLines = false;
				this.lachOnDLines = this.RAM[this.BU][this.BL];
				this.lachOnDLines = this.lachOnDLines << 4;
				this.lachOnDLines = this.lachOnDLines | this.ACC;
				if (this.invertedPolarityOnDLines == true) {
					this.lachOnDLines = (~this.lachOnDLines) & 0b11111111;
				}
				this.stateOfDLines = this.lachOnDLines;
				decodeDlines();
				break;
			case 0x19: // MVS
				this.stateOfALines = this.lachInALines;
				this.floatingModeOnDLines = true;
				decodeAlines();
				break;
			case 0x1A: // OUT
				this.stateOfDLines = 0;
				this.stateOfDLines = this.RAM[this.BU][this.BL];
				this.stateOfDLines = this.stateOfDLines << 4;
				this.stateOfDLines = this.stateOfDLines | this.ACC;
				this.EXIT = true;
				decodeDlines();
				// System.out.println(java.util.Arrays.toString(getPinsD()));
				break;
			case 0x1B: // DISN
				this.floatingModeOnDLines = false;
				if (this.carry == true) {
					this.lachOnDLines = 0b10000000;
				} else {
					this.lachOnDLines = 0b0;
				}

				switch (this.ACC) {
					case 0:
						this.lachOnDLines = this.lachOnDLines | 0b01111110; // A,B,C,D,E,F
						break;
					case 1:
						this.lachOnDLines = this.lachOnDLines | 0b00110000; // B,C on
						break;
					case 2:
						this.lachOnDLines = this.lachOnDLines | 0b01101101; // A,B,D,E,G on
						break;
					case 3:
						this.lachOnDLines = this.lachOnDLines | 0b01111001; // A,B,C,D,G on
						break;
					case 4:
						this.lachOnDLines = this.lachOnDLines | 0b00110011; // B,C,F,G on
						break;
					case 5:
						this.lachOnDLines = this.lachOnDLines | 0b01011011; // A,C,D,F,G on
						break;
					case 6:
						this.lachOnDLines = this.lachOnDLines | 0b01011111; // A,C,D,E,F,G on
						break;
					case 7:
						this.lachOnDLines = this.lachOnDLines | 0b01110000; // A,B,C on
						break;
					case 8:
						this.lachOnDLines = this.lachOnDLines | 0b01111111; // All segments on
						break;
					case 9:
						this.lachOnDLines = this.lachOnDLines | 0b01111011; // A,B,C,D,F,G on
						break;
					case 10: // A
						this.lachOnDLines = this.lachOnDLines | 0b01110111; // A,B,C,E,F,G on
						break;
					case 11: // b
						this.lachOnDLines = this.lachOnDLines | 0b00011111; // C,D,E,F,G on
						break;
					case 12: // C
						this.lachOnDLines = this.lachOnDLines | 0b01001110; // A,D,E,F on
						break;
					case 13: // d
						this.lachOnDLines = this.lachOnDLines | 0b00111101; // B,C,D,E,G on
						break;
					case 14: // E
						this.lachOnDLines = this.lachOnDLines | 0b01001111; // A,D,E,F,G on
						break;
					case 15: // F
						this.lachOnDLines = this.lachOnDLines | 0b01000111; // A,E,F,G on
						break;
					default:
						break;
				}
				if (this.invertedPolarityOnDLines == true) {
					this.lachOnDLines = (~this.lachOnDLines) & 0b11111111;
				}
				this.stateOfDLines = this.lachOnDLines;
				decodeDlines();
				break;
			case 0x1C: // SZM B
				tmp = this.RAM[this.BU][this.BL];
				tmp = tmp >> param;
				if ((tmp & 1) == 0) {
					skip();
				}
				break;
			case 0x20: // STM B
				tmp = 0b1;
				tmp = tmp << param;
				this.RAM[BU][BL] = (byte) (this.RAM[BU][BL] | tmp);
				break;
			case 0x24: // RSM B
				tmp = 0b1;
				tmp = (byte) (tmp << param);
				tmp = (byte) (~tmp & 0b01111111);
				this.RAM[BU][BL] = (byte) (this.RAM[this.BU][this.BL] & tmp);
				break;
			case 0x28: // SZK
				tmp = this.selectedK;
				boolean tmpFlagK = true;
				if ((tmp & 0b1000) == 8) {
					if (this.inputK[3] == true)
						tmpFlagK = false;
				}
				if ((tmp & 0b100) == 4) {
					if (this.inputK[2] == true)
						tmpFlagK = false;
				}
				if ((tmp & 0b10) == 2) {
					if (this.inputK[1] == true)
						tmpFlagK = false;
				}
				if ((tmp & 0b1) == 1) {
					if (this.inputK[0] == true)
						tmpFlagK = false;
				}
				if (tmpFlagK) {
					skip();
				}
				break;
			case 0x29: // SZI
				tmp = this.selectedI;
				boolean tmpFlagI = true;
				if ((tmp & 0b1000) == 8) {
					// System.out.println("aa");
					if (this.inputI[3] == true)
						tmpFlagI = false;
				}
				if ((tmp & 0b100) == 4) {
					// System.out.println("aa");
					if (this.inputI[2] == true)
						tmpFlagI = false;
				}
				if ((tmp & 0b10) == 2) {
					// System.out.println("aa");
					if (this.inputI[1] == true)
						tmpFlagI = false;
				}
				if ((tmp & 0b1) == 1) {
					// System.out.println("aa");
					if (this.inputI[0] == true) {
						tmpFlagI = false;
					}
				}
				if (tmpFlagI) {
					skip();
				}
				// System.out.println(Arrays.toString(inputI));
				break;
			case 0x2A: // RF1
				this.flag1 = false;
				break;
			case 0x2B: // ST1
				this.flag1 = true;
				break;
			case 0x2C: // RF2
				this.flag2 = false;
				break;
			case 0x2D: // ST2
				this.flag2 = true;
				break;
			case 0x2E: // TF1
				if (this.flag1 == true) {
					skip();
				}
				break;
			case 0x2F: // TF2
				if (this.flag2 == true) {
					skip();
				}
				break;
			case 0x30: // XCI Y*
				tmp = this.RAM[this.BU][this.BL];
				this.RAM[this.BU][this.BL] = (byte) this.ACC;
				this.ACC = tmp;
				this.BU = (byte) (this.BU ^ ~param);
				this.BU = (byte) (this.BU & 0x03);
				this.BL++;
				this.BL = (byte) (this.BL & 0x0F);
				if (this.BL == 0) {
					skip();
				}
				break;
			case 0x34: // XCD Y*
				tmp = this.RAM[this.BU][this.BL];
				this.RAM[this.BU][this.BL] = (byte) this.ACC;
				this.ACC = tmp;
				break;
			case 0x38: // XC
				tmp = this.RAM[this.BU][this.BL];
				this.RAM[this.BU][this.BL] = (byte) this.ACC;
				this.ACC = tmp;
				this.BU = (byte) ((this.BU ^ ~param) & 0x3);
				break;
			case 0x40: // LBZ
				if (this.previosBL) {
					this.currentBL = false;
				} else {
					this.BL = 0;
					this.BU = (byte) param;
					this.currentBL = true;
				}

				break;
			case 0x44: // LBF
				if (this.previosBL) {
					this.currentBL = false;
				} else {
					this.BL = 0xF;
					this.BU = (byte) param;
					this.currentBL = true;
				}
				break;
			case 0x48: // LBE
				if (this.previosBL) {
					this.currentBL = false;
				} else {
					this.BL = (byte) this.E;
					this.BU = (byte) param;
					this.currentBL = true;
				}
				break;
			case 0x4C: // LBEP Y
				if (this.previosBL) {
					this.currentBL = false;
				} else {
					this.BL = (byte) ((this.E + 1) & 0x0F);
					this.BU = (byte) param;
					this.currentBL = true;
				}
				break;
			case 0x50: // ADIS
				this.ACC = (byte) (this.ACC + param);
				if (this.ACC <= 15) {
					skip();
				}
				this.ACC = (byte) (this.ACC & 0b1111);

				break;
			case 0x60: // PP X*
				if (this.previousPPFlag == false) {
					this.PPR = (byte) (~param & 0b1111);
				} else {
					this.PBR = (byte) (~param & 0b111);
				}
				this.PPFlag = true;
				break;
			case 0x70: // LAI X
				if (this.previosLAI) {
					this.currentLAI = false;
				} else {
					this.ACC = (byte) param;
					this.selectedK = (byte) param;
					this.selectedI = (byte) param;
					this.currentLAI = true;
				}

				break;
			case 0x80: // JMS X
				if (previousPPFlag == true) {
					tmp = this.programCounter & 0b1111111111;
					this.stack[this.stackPointer] = tmp;
					this.stackPointer++;
					if (this.stackPointer > 2) {
						this.stackPointer = 0;
					}
					this.programCounter = (this.PBR << 10) | (this.PPR << 6) | param;
				} else {
					tmp = this.programCounter & 0b1111111111;
					this.stack[this.stackPointer] = tmp;
					this.stackPointer++;
					if (this.stackPointer > 2) {
						this.stackPointer = 0;
					}
					this.programCounter = (this.programCounter & 0b1110000000000) | (15 << 6)
							| param;
				}
				break;
			case 0xC0: // JMP X
				if (previousPPFlag == true) {
					this.programCounter = (this.PBR << 10) | (this.PPR << 6) | param;
				} else {
					tmp = this.programCounter & 0b1111111000000;
					tmp = tmp | param;
					this.programCounter = tmp;
				}

				break;
			default:
				break;
		}
		this.previosLAI = this.currentLAI;
		this.previosBL = this.currentBL;

		this.currentBL = false;
		this.currentLAI = false;

	}

	public boolean[] getPinsA() {
		return this.pinsA;
	}

	public boolean[] getPinsD() {
		return this.pinsD;
	}

	public void setPinsD(boolean[] D) {
		this.pinsD = D;
		encodeDLines();
		// System.out.println("Did it work?" + Arrays.toString(D));
	}

	public boolean[] getPinsI() {
		return this.inputI;
	}

	public boolean[] getPinsK() {
		return this.inputK;
	}

	public void setPinsI(boolean[] pins) {
		this.inputI = pins;
		// System.out.println(Arrays.toString(inputI));
	}

	public void setPinsK(boolean[] pins) {
		this.inputK = pins;
	}

	public boolean getDDir() {
		return this.floatingModeOnDLines;
	}

	public int getACC() {
		return this.ACC;
	}

	public byte getBL() {
		return this.BL;
	}

	public byte getBU() {
		return this.BU;
	}

	public int getE() {
		return this.E;
	}

	public int[] getROM() {
		return this.ROM;
	}

	public void setInterConnect(InterConnect connect) {
		this.connect = connect;
	}

	public int getProgramCounter() {
		return this.programCounter;
	}

	public int getStackPointer() {
		return this.stackPointer;
	}

	public int[] getStack() {
		return this.stack;
	}

	public boolean getCarry() {
		return this.carry;
	}

	public boolean getFlag1() {
		return this.flag1;
	}

	public boolean getFlag2() {
		return this.flag2;
	}

	public boolean getPPFlag() {
		return this.PPFlag;
	}

	public boolean getSecondsFlag() {
		return this.secondsFlag;
	}

	public boolean getFloatingModeOnDLines() {
		return this.floatingModeOnDLines;
	}

	public boolean getInvertedPolarityOnDLines() {
		return this.invertedPolarityOnDLines;
	}

	public int getSelectedK() {
		return this.selectedK;
	}

	public int getSelectedI() {
		return this.selectedI;
	}

	public int getStateOfDLines() {
		return this.stateOfDLines;
	}

	public int getStateOfALines() {
		return this.stateOfALines;
	}

	public int getLachOnDLines() {
		return this.lachOnDLines;
	}

	public int getLachInALines() {
		return this.lachInALines;
	}

	public int getPPR() {
		return this.PPR;
	}

	public int getPBR() {
		return this.PBR;
	}

}
