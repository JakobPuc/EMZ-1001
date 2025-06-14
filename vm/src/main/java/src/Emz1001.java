/**
 * @author Jakob Puc
 * 
 *A polished and corrected version of Main.java not finished.
 *
 * */

package src;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.EOFException;

public class Emz1001 {

	// RAM and RAM registers
	private byte[][] RAM = new byte[4][16]; // each 4 bits
	private byte BU; // max 3
	private byte BL; // max 15

	// flags
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
	// Main method that executes instructions
	private void executeInstruction(int opcode, int param) {
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
					skip();
					this.secondsFlag = false;
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

				} else {

				}
				break;
			case 0x10: // CMA
				this.ACC = 15 - this.ACC;
				break;
			default:
				break;
		}

	}
}
