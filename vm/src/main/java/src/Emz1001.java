/**
 * @author Jakob Puc
 * 
 *A polished and corrected version of this.java not finished.
 *
 * */

package src;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.EOFException;

public class Emz1001 {

	// RAM and RAM registers
	private byte[][] RAM = new byte[4][16]; // each 4 bits
	private byte BU; // max 3
	private byte BL; // max 15

	// flags
	private SignalSimulator secondsTimer = new SignalSimulator();
	private boolean secondsFlag;
	private boolean carry;
	private boolean flag1;
	private boolean flag2;

	// ROM
	private int sizeOfRom; // default 1024
	private int[] ROM;
	private int programCounter; // is 13 bits long

	// stack
	private int sizeOfStack = 4;
	private int stackPointer; // max 3
	private int[] stack = new int[sizeOfStack]; // 10 bits wide

	// mode flags and laches
	private boolean floatingModeOnDLines;
	private boolean invertedPolarityOnDLines;
	private boolean PPFlag = false;
	private boolean previousPPFlag = false;

	private int lachOnDLines;
	private int lachInALines; // TODO may need to implement master slave lach

	// registers
	private int PPR; // prepere page register
	private int PBR; // prepere bank register
	private int ACC; // 4 bit
	private int E; // 4 bit

	private int selectedK; // which input k is selected
	private int selectedI; // which input i is selected

	// pins
	private int numberOfPins = 40; // number of all pins including power
	private byte[] stateOfPins; // includes non io pins, io pins in difrent arreys but mirrored in here
	private byte[] inputK = new byte[4]; // 1 or 0
	private byte[] inputI = new byte[4]; // 1 or 0

	// instructions
	private Instruction[] instructions;

	// default constructor sets all the features to default ones/minimal
	public Emz1001() {
		this.programCounter = 0;
		this.sizeOfRom = 1024;
		this.ROM = new int[this.sizeOfRom];

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
			System.out.println("O no chek faild please report the problem :)");
		}
		return tmpROM;
	}

	// subrutine for skiping PP instruction
	private void skip() {
		while ((this.ROM[this.programCounter] & 0b11110000) == 0x60) {
			this.programCounter++;
		}
		this.programCounter++;
	}

	// may be changed to public
	// this method that executes instructions
	private void executeInstruction(int opcode, int param) {
		this.secondsFlag = this.secondsTimer.getFlag();
		int tmp = 0;
		this.previousPPFlag = this.PPFlag;
		switch (opcode) {
			case 0x01: // NOP
				break;
			case 0x02: // BRK not implemented treated as NOP
				break;
			case 0x03: // RT
				this.stackPointer--;
				if (this.stackPointer < 0) {
					this.stackPointer = this.sizeOfStack - 1;
				}
				tmp = this.programCounter & 0b1110000000000;
				tmp = tmp | (this.stack[this.stackPointer] & 0b1111111111);
				this.programCounter = tmp;
				break;
			case 0x04: // RTS
				this.stackPointer--;
				if (this.stackPointer < 0) {
					this.stackPointer = this.sizeOfStack - 1;
				}
				tmp = this.programCounter & 0b1110000000000;
				tmp = tmp | (this.stack[this.stackPointer] & 0b1111111111);
				this.programCounter = tmp;
				skip();
				break;
			case 0x05: // PSH does somthing with IO
				break;
			case 0x06: // PSL does somthing with IO
				break;
			case 0x07: // AND
				this.ACC = this.RAM[this.BU][this.BL] & this.ACC;
				break;
			case 0x08: // SOS
				if (this.secondsFlag == true) {
					this.secondsFlag = false;
					skip();
				}
				break;
			case 0x09: // SBE
				if (this.BL == this.E) {
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
				break;
			case 0x0F: // EUR
				if ((this.ACC & 1) == 1) {
					this.invertedPolarityOnDLines = true;
				} else {
					this.invertedPolarityOnDLines = false;
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
				break;
			case 0x19: // MVS
				break;
			case 0x1A: // OUT
				break;
			case 0x1B: // DISN
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
				break;
			case 0x29: // SZI
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
				this.BU = (byte) (this.BU ^ ~param);
				this.BU = (byte) (this.BU & 0x03);
				if (this.BL >= 1) {
					this.BL--;
				} else {
					this.BL = 15;
				}
				if (this.BL == 15) {
					skip();
				}
				break;
			case 0x38: // XC Y*
				tmp = this.RAM[this.BU][this.BL];
				this.RAM[this.BU][this.BL] = (byte) this.ACC;
				this.BU = (byte) (this.BU ^ ~param);
				this.BU = (byte) (this.BU & 0x03);
				break;
			case 0x3C: // LAM Y*
				this.ACC = this.RAM[this.BU][this.BL];
				this.BU = (byte) (this.BU ^ ~param);
				break;
			case 0x40: // LBZ Y
				this.BL = 0x00;
				this.BU = (byte) param;
				break;
			case 0x44: // LBF Y
				this.BL = 0x0F;
				this.BU = (byte) param;
				break;
			case 0x48: // LBE Y
				this.BL = (byte) this.E;
				this.BU = (byte) param;
				break;
			case 0x4C: // LBEP Y
				this.BL = (byte) ((this.E + 1) & 0x0F);
				this.BU = (byte) param;
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
				this.ACC = (byte) param;
				this.selectedK = (byte) param;
				this.selectedI = (byte) param;
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

	}
}
