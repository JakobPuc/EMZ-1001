package src.logic;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import src.logic.*;

// multy threading needs to be fixed
public class Emz1001 {

	public final String srcFile = "emzasm/bin/zaporedje.bin";
	public final boolean debugmode = false;

	// ! not final may change
	private final int sizeOfRom = 1024; // size of ROM
	private int[] ROM = new int[sizeOfRom]; // size of ROM

	public int programCounter; // program counter
	// need to include for RT,JMP JMS
	private int PPR; // prepare page reg
	private int PBR; // prepare bank reg
	// program counterocation reg

	private final int procesorFreq = 900000;

	// pins that means all of them inc. VCC and GND
	// 8D lines
	//
	//
	// private int[] StateOfPins = new int[40];

	// registers
	private byte accummulator; // accumulator
	private byte BL;
	private byte BU;
	private byte E;

	private byte[] KInputs = new byte[4];
	private byte[] IInputs = new byte[4];
	private byte KSelected = 0;
	private byte ISelected = 0;

	// flags
	private boolean secondsFlag;
	private boolean carryFlag;
	private boolean flagOne;
	private boolean flagTwo;
	private boolean invertedPowerOnDLines = false;
	private boolean floatingModeOnDLines = false;

	// lach
	private boolean selectorForProgramCounterOrLach = false;

	private boolean EXTPulse = false;

	private int displayLach = 0;
	private int DLines = 0;
	private int addresControlLines = 0;
	private int masterStrobeLach = 0;

	private boolean flagPP = false;
	private boolean previousFlagPP = false;

	// ram
	private byte[][] RAM = new byte[4][16]; // uses bytes but is 4 bit

	private Instruction[] instructions;

	// stack //! to change
	private final int sizeOfStack = 3; // depth of stack
	private int[] stack = new int[sizeOfStack]; // stack
	private int stackPointer = 0; // if stack pointer == 0 that means that you arent in a subroutine

	public Emz1001(File f) {
		List<Instruction> InstructionArray = getInstructionArray();
		Instruction[] instructionArray = new Instruction[InstructionArray.size()];
		for (int i = 0; i < InstructionArray.size(); i++) {
			instructionArray[i] = InstructionArray.get(i);
		}
		this.instructions = instructionArray;
		try {
			this.ROM = readFile(f);

		} catch (FileToLongExeption e) {
			e.printStackTrace();
		}
	}

	private List<Instruction> getInstructionArray() {
		InstructionSetEmz1001 set = new InstructionSetEmz1001();
		int[] opCodes = set.getOpCodes();
		int[] masks = set.getMasks();
		List<Instruction> list = new LinkedList<Instruction>();
		for (int i = 0; i < opCodes.length; i++) {
			list.add(new Instruction(masks[i], opCodes[i]));
		}
		return list;
	}

	public int[] readFile(File file) throws FileToLongExeption {
		long fileLength = file.length();
		if (fileLength > this.sizeOfRom) {
			throw new FileToLongExeption("File is to long for a given ROM");
		}
		int[] bytes = new int[this.sizeOfRom];
		try (DataInputStream input = new DataInputStream(new FileInputStream(file))) {
			for (int i = 0; i < bytes.length; i++) {
				try {
					bytes[i] = input.readUnsignedByte();
				} catch (EOFException e) {
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return bytes;
	}

	public void dumpROM() {
		System.out.println("Dump of ROM");
		for (int i = 0; i < this.ROM.length; i++) {
			if (this.ROM[i] != 0)
				System.out.println(String.format("0x%04X", i) + " : " + String
						.format("%8s", Integer.toBinaryString(this.ROM[i])).replace(' ', '0'));
		}
	}

	public void dumpRAM() {
		System.out.println("Dump of RAM");
		for (int i = 0; i < this.RAM.length; i++) {
			for (int j = 0; j < this.RAM[i].length; j++) {
				System.out.print(String.format("%4s", Integer.toBinaryString(this.RAM[i][j]))
						.replace(' ', '0') + " ");
			}
			System.out.println();
		}
	}

	public void StartPrcesor(boolean debug) {
		if (debug == false) {
			// simulation(this.instructions);
		} else {
			// simulationDebug(this.instructions);
		}
	}

	/*
	 * private void simulation(Instruction[] ArraayOfInstructions) {
	 * SignalSimulator secondsFlagSetter = new SignalSimulator();
	 * SignalSimulator clockGenerator = new SignalSimulator();
	 * try {
	 * clockGenerator.setSignalFreqency(procesorFreq);
	 * } catch (NegativeFrequencyException e) {
	 * e.printStackTrace();
	 * }
	 * secondsFlagSetter.start();
	 * clockGenerator.start();
	 * int instruction = 0;
	 * while (true) {
	 * // dumpRAM();
	 * if (clockGenerator.getFlag()) {
	 * clockGenerator.setFlag(false);
	 * instruction = this.ROM[this.programCounter];
	 * this.programCounter++;
	 * if (this.programCounter >= this.sizeOfRom) {
	 * break;
	 * }
	 * int indexOfOnstruction = returnIndexOfInstruction(instruction,
	 * ArraayOfInstructions);
	 * if (indexOfOnstruction == -1) {
	 * System.out.println("Instruction does not exist");
	 * break;
	 * }
	 * if (ArraayOfInstructions[indexOfOnstruction].getMask() == 0x00) {
	 * executeInstruction(ArraayOfInstructions[indexOfOnstruction].getOpCode(), 0,
	 * secondsFlagSetter);
	 * } else {
	 * executeInstruction(ArraayOfInstructions[indexOfOnstruction].getOpCode(),
	 * instruction & ArraayOfInstructions[indexOfOnstruction]
	 * .getMask(),
	 * secondsFlagSetter);
	 * }
	 * }
	 * }
	 * // dumpROM();
	 * // dumpRAM();
	 * // clockGenerator.kill();
	 * // secondsFlagSetter.kill();
	 * }
	 * 
	 * private void simulationDebug(Instruction[] ArraayOfInstructions) {
	 * SignalSimulator secondsFlagSetter = new SignalSimulator();
	 * secondsFlagSetter.start();
	 * int instruction = 0;
	 * int indexOfOnstruction = 0;
	 * int executeNumberOf = 0;
	 * while (true) {
	 * executeNumberOf = readCLI();
	 * if (this.programCounter >= this.sizeOfRom) {
	 * break;
	 * }
	 * while (true) {
	 * if (executeNumberOf >= 0) {
	 * instruction = this.ROM[this.programCounter];
	 * indexOfOnstruction = returnIndexOfInstruction(instruction,
	 * ArraayOfInstructions);
	 * executeNumberOf--;
	 * this.programCounter++;
	 * if (ArraayOfInstructions[indexOfOnstruction].getMask() == 0x00) {
	 * executeInstruction(ArraayOfInstructions[indexOfOnstruction].getOpCode(),
	 * 0,
	 * secondsFlagSetter);
	 * } else {
	 * executeInstruction(ArraayOfInstructions[indexOfOnstruction].getOpCode(),
	 * instruction & ArraayOfInstructions[indexOfOnstruction]
	 * .getMask(),
	 * secondsFlagSetter);
	 * }
	 * if (indexOfOnstruction == -1) {
	 * System.out.println("Instruction does not exist");
	 * break;
	 * }
	 * }
	 * if (executeNumberOf == 0) {
	 * dumpRAM();
	 * System.out.println("Executing inst: " +
	 * String.format("0x%04X ",
	 * ArraayOfInstructions[indexOfOnstruction]
	 * .getOpCode())
	 * +
	 * " program counter " + this.programCounter);
	 * break;
	 * }
	 * }
	 * }
	 * secondsFlagSetter.kill();
	 * }
	 */
	private void executeInstruction(int opCode, int param,
			/* SignalSimulator clock, */ SignalSimulator secondfFlag) {
		this.EXTPulse = false;
		this.previousFlagPP = this.flagPP;
		this.flagPP = false;
		int tmpInt = 0;
		byte tmpByte = 0;
		switch (opCode) {
			case 0x00: // NOP
				break;
			case 0x01: // BRK trated as NOP
				break;
			case 0x02: // RT
				this.stackPointer--;
				if (this.stackPointer < 0) {
					this.stackPointer = this.sizeOfStack - 1;
				}
				tmpInt = programCounter & 0b1110000000000;
				tmpInt = tmpInt | (this.stack[this.stackPointer] & 0b1111111111);
				programCounter = tmpInt;
				break;
			case 0x03: // RTS
				this.stackPointer--;
				if (this.stackPointer < 0) {
					this.stackPointer = this.sizeOfStack - 1;
				}
				tmpInt = programCounter & 0b1110000000000;
				tmpInt = tmpInt | (this.stack[this.stackPointer] & 0b1111111111);
				programCounter = tmpInt;
				skip();
				break;
			case 0x04: // PSH
				if ((this.BL >= 0) && (this.BL <= 12)) {
					tmpInt = 1;
					tmpInt = tmpInt << this.BL;
					this.masterStrobeLach = this.masterStrobeLach | tmpInt;
					break;
				}
				if (this.BL == 13) {
					// set multiplex operation
					break;
				}
				if (this.BL == 14) {
					this.floatingModeOnDLines = false;
					break;
				}
				if (this.BL == 15) {
					this.masterStrobeLach = 0b1111111111111;
					break;
				}
				break;
			case 0x05: // PSL
				if ((this.BL >= 0) && (this.BL <= 12)) {
					tmpInt = 1;
					tmpInt = tmpInt << this.BL;
					tmpInt = ~tmpInt;
					this.masterStrobeLach = this.masterStrobeLach | tmpInt;
					break;
				}
				if (this.BL == 13) {
					// set operation
					break;
				}
				if (this.BL == 14) {
					this.floatingModeOnDLines = true;
					break;
				}
				if (this.BL == 15) {
					this.masterStrobeLach = 0b0;
					break;
				}
				break;
			case 0x06: // AND
				accummulator = (byte) (accummulator & (RAM[BU][BL]) & 0x0F);
				break;
			case 0x07: // SOS
				if (this.secondsFlag == true) {
					this.secondsFlag = false;
					skip();
				}
				break;
			case 0x08: // SBE
				if (this.E == this.BL) {
					skip();
				}
				break;
			case 0x09: // SZC
				if (this.carryFlag == false) {
					skip();
				}
				break;
			case 0x0A: // STC
				this.carryFlag = true;
				break;
			case 0x0B: // RSC
				this.carryFlag = false;
				break;
			case 0x0C: // LAE
				this.accummulator = this.E;
				break;
			case 0x0D: // XAE
				tmpByte = this.accummulator;
				this.accummulator = this.E;
				this.E = tmpByte;
				break;
			case 0x0E: // INP //TODO chc
				if (this.floatingModeOnDLines == true) {
					tmpInt = this.DLines;
					this.accummulator = (byte) (this.DLines & 0b1111);
					this.RAM[BU][BL] = (byte) ((tmpInt >> 4) & 0b1111);
				} else {
					tmpInt = this.displayLach;
					this.accummulator = (byte) (this.displayLach & 0b1111);
					this.RAM[BU][BL] = (byte) ((tmpInt >> 4) & 0b1111);
				}
				break;
			case 0x0F: // EUR
				if ((accummulator & 0x1) == 1) {
					this.invertedPowerOnDLines = false;
				} else {
					this.invertedPowerOnDLines = true;
				}
				if (((accummulator >> 2) & 0x1) == 1) {
					secondfFlag.setSignalFreqency(true);
				} else {
					secondfFlag.setSignalFreqency(false);
				}
				break;
			case 0x10: // CMA
				this.accummulator = (byte) (15 - this.accummulator);
				break;
			case 0x11: // XABU
				tmpByte = this.BU;
				this.BU = (byte) (this.accummulator & 0b11);
				this.accummulator = (byte) (this.accummulator & 0b1100);
				this.accummulator = (byte) (this.accummulator | tmpByte);
				break;
			case 0x12: // LAB
				this.accummulator = this.BL;
				break;
			case 0x13: // XAB
				tmpByte = this.accummulator;
				this.accummulator = this.BL;
				this.BL = tmpByte;
				break;
			case 0x14: // ADCS
				this.accummulator = (byte) (this.accummulator + this.RAM[BU][BL]);
				if (this.accummulator > 15) {
					this.carryFlag = true;
				} else {
					this.carryFlag = false;
					skip();
				}
				this.accummulator = (byte) (this.accummulator & 0b1111);
				break;
			case 0x15: // XOR
				this.accummulator = (byte) (this.accummulator ^ this.RAM[this.BU][this.BL]);
				break;
			case 0x16: // ADD
				this.accummulator = (byte) (this.RAM[this.BU][this.BL] + this.accummulator);
				break;
			case 0x17: // SAM
				if (this.accummulator == this.RAM[this.BU][this.BL]) {
					skip();
				}
				break;
			case 0x18: // DISB //! Not final may change
				this.floatingModeOnDLines = false;
				this.displayLach = this.DLines = this.RAM[this.BU][this.BL];
				this.displayLach = this.DLines = this.displayLach << 4;
				this.displayLach = this.DLines = this.accummulator;
				if (this.invertedPowerOnDLines = true) {
					this.DLines = (~this.DLines & 0b11111111);
				}
				break;
			case 0x19: // MVS
				this.floatingModeOnDLines = true;
				this.addresControlLines = this.masterStrobeLach;
				break;
			case 0x1A: // OUT //! needs to output a pulse ;not implemented
				this.DLines = this.RAM[this.BU][this.BL];
				this.DLines = this.displayLach << 4;
				this.displayLach = this.DLines = this.accummulator;
				if (this.invertedPowerOnDLines = true) {
					this.DLines = (~this.DLines & 0b11111111);
				}
				break;
			case 0x1B: // DISN //! Not final may change
				this.floatingModeOnDLines = false;
				if (this.carryFlag == true) {
					this.displayLach = 0b10000000;
				}
				switch (this.accummulator) {
					case 0:
						this.displayLach = this.displayLach | 0b01111110;
						break;
					case 1:
						this.displayLach = this.displayLach | 0b00110000;
						break;
					case 2:
						this.displayLach = this.displayLach | 0b01101101;
						break;
					case 3:
						this.displayLach = this.displayLach | 0b01111001;
						break;
					case 4:
						this.displayLach = this.displayLach | 0b00110011;
						break;
					case 5:
						this.displayLach = this.displayLach | 0b01011011;
						break;
					case 6:
						this.displayLach = this.displayLach | 0b01011111;
						break;
					case 7:
						this.displayLach = this.displayLach | 0b01110000;
						break;
					case 8:
						this.displayLach = this.displayLach | 0b01111111;
						break;
					case 9:
						this.displayLach = this.displayLach | 0b01111011;
						break;
					case 10:
						this.displayLach = this.displayLach | 0b01110111;
						break;
					case 11:
						this.displayLach = this.displayLach | 0b00011111;
						break;
					case 12:
						this.displayLach = this.displayLach | 0b01001110;
						break;
					case 13:
						this.displayLach = this.displayLach | 0b00111101;
						break;
					case 14:
						this.displayLach = this.displayLach | 0b01001111;
						break;
					case 15:
						this.displayLach = this.displayLach | 0b01000111;
						break;
					default:
						break;
				}
				if (this.invertedPowerOnDLines == true) {
					this.displayLach = (~this.displayLach) & 0b11111111;
				}
				this.DLines = this.displayLach;
				break;
			case 0x1C: // SZM B
				tmpByte = this.RAM[BU][BL];
				tmpByte = (byte) (tmpByte >> param);
				if ((tmpByte & 0b1) == 0) {
					skip();
				}
				break;
			case 0x20: // STM B
				tmpByte = 0b1;
				tmpByte = (byte) (tmpByte << param);
				this.RAM[BU][BL] = (byte) (this.RAM[BU][BL] | tmpByte);
				break;
			case 0x24: // RSM B
				tmpByte = 0b1;
				tmpByte = (byte) (tmpByte << param);
				tmpByte = (byte) (~tmpByte & 0b01111111);
				this.RAM[BU][BL] = (byte) (this.RAM[BU][BL] & tmpByte);
				break;
			case 0x28: // SZK
				tmpByte = KSelected; // 8, 4, 2, 1 are the walues for individual bits
				boolean tmpFlagK = true;
				if ((tmpByte & 0b1000) == 1) {
					if (this.KInputs[4] == 1)
						tmpFlagK = false;
				}
				if ((tmpByte & 0b100) == 1) {
					if (this.KInputs[3] == 1)
						tmpFlagK = false;
				}
				if ((tmpByte & 0b10) == 1) {
					if (this.KInputs[2] == 1)
						tmpFlagK = false;
				}
				if ((tmpByte & 0b1) == 1) {
					if (this.KInputs[1] == 1)
						tmpFlagK = false;
				}
				if (tmpFlagK) {
					skip();
				}
				break;
			case 0x29: // SZI
				tmpByte = ISelected; // 8, 4, 2, 1 are the walues for individual bits
				boolean tmpFlagI = true;
				if ((tmpByte & 0b1000) == 1) {
					if (this.IInputs[4] == 1)
						tmpFlagI = false;
				}
				if ((tmpByte & 0b100) == 1) {
					if (this.IInputs[3] == 1)
						tmpFlagI = false;
				}
				if ((tmpByte & 0b10) == 1) {
					if (this.IInputs[2] == 1)
						tmpFlagI = false;
				}
				if ((tmpByte & 0b1) == 1) {
					if (this.IInputs[1] == 1)
						tmpFlagI = false;
				}
				if (tmpFlagI) {
					skip();
				}
				break;
			case 0x2A: // RF1
				this.flagOne = false;
				break;
			case 0x2B: // ST1
				this.flagOne = true;
				break;
			case 0x2C: // RF2
				this.flagTwo = false;
				break;
			case 0x2D: // ST2
				this.flagTwo = true;
				break;
			case 0x2E: // TF1
				if (this.flagOne == true) {
					skip();
				}
				break;
			case 0x2F: // TF2
				if (this.flagTwo == true) {
					skip();
				}
				break;
			case 0x30: // XCI Y*
				tmpByte = this.RAM[this.BU][this.BL];
				this.RAM[this.BU][this.BL] = this.accummulator;
				this.accummulator = tmpByte;
				this.BU = (byte) (this.BU ^ ~param);
				this.BU = (byte) (this.BU & 0x03);
				this.BL++;
				this.BL = (byte) (this.BL & 0x0F);
				if (this.BL == 0) {
					skip();
				}
				break;
			case 0x34: // XCD Y*
				tmpByte = this.RAM[this.BU][this.BL];
				this.RAM[this.BU][this.BL] = this.accummulator;
				this.accummulator = tmpByte;
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
				tmpByte = this.RAM[this.BU][this.BL];
				this.RAM[this.BU][this.BL] = this.accummulator;
				this.BU = (byte) (this.BU ^ ~param);
				this.BU = (byte) (this.BU & 0x03);
				break;
			case 0x3C: // LAM Y*
				this.accummulator = this.RAM[this.BU][this.BL];
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
				this.BL = this.E;
				this.BU = (byte) param;
				break;
			case 0x4C: // LBEP Y
				this.BL = (byte) ((this.E + 1) & 0x0F);
				this.BU = (byte) param;
				break;
			case 0x50: // ADIS
				this.accummulator = (byte) (this.accummulator + param);
				if (this.accummulator <= 15) {
					skip();
				}
				this.accummulator = (byte) (this.accummulator & 0b1111);
				break;
			case 0x60: // PP X*
				if (this.previousFlagPP == false) {
					this.PPR = (byte) (~param & 0b1111);
				} else {
					this.PBR = (byte) (~param & 0b111);
				}
				this.flagPP = true;
				break;
			case 0x70: // LAI X
				this.accummulator = (byte) param;
				this.KSelected = (byte) param;
				this.ISelected = (byte) param;
				break;
			case 0x80: // JMS X
				if (previousFlagPP == true) {
					tmpInt = this.programCounter & 0b1111111111;
					this.stack[this.stackPointer] = tmpInt;
					this.stackPointer++;
					if (this.stackPointer > 2) {
						this.stackPointer = 0;
					}
					this.programCounter = (this.PBR << 10) | (this.PPR << 6) | param;
				} else {
					tmpInt = this.programCounter & 0b1111111111;
					this.stack[this.stackPointer] = tmpInt;
					this.stackPointer++;
					if (this.stackPointer > 2) {
						this.stackPointer = 0;
					}
					this.programCounter = (this.programCounter & 0b1110000000000) | (15 << 6)
							| param;
				}
				break;
			case 0xC0: // JMP X
				if (previousFlagPP == true) {
					this.programCounter = (this.PBR << 10) | (this.PPR << 6) | param;
				} else {
					tmpInt = this.programCounter & 0b1111111000000;
					tmpInt = tmpInt | param;
					this.programCounter = tmpInt;
				}
				break;
			default:
				System.out.println("Illegal instruction");
				return;
		}
		this.previousFlagPP = false;
	}

	// 0x60 is PP and needs to be skiped
	private void skip() {
		while ((this.ROM[programCounter] & 0b11110000) == 0x60) {
			this.programCounter++;
		}
		this.programCounter++;
	}

	private int returnIndexOfInstruction(int instruction, Instruction[] listOFInstructions) {
		int i = 0;
		for (; i < listOFInstructions.length; i++) {
			Instruction ins = listOFInstructions[i];
			if (ins.getOpCode() == (instruction & (~ins.getMask()))) {
				return i;
			}
		}
		return -1;
	}

	public void setKInputs(byte[] input) {
		for (int i = 0; i < KInputs.length; i++) {
			if (input[i] == 0 || input[i] == 1)
				this.KInputs[i] = input[i];
		}
	}

	public void setIInputs(byte[] input) {
		for (int i = 0; i < IInputs.length; i++) {
			if (input[i] == 0 || input[i] == 1)
				this.IInputs[i] = input[i];
		}
	}

	public int readCLI() {
		int n = 0;
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
			String s = r.readLine();
			if (s.isEmpty()) {
				n = 1;
				return n;
			} else {
				n = Integer.valueOf(s);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return n;
	}
}

class FileToLongExeption extends Exception {
	public FileToLongExeption(String message) {
		super(message);
	}
}
