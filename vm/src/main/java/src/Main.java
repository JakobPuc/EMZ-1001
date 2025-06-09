package src;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class Main {

    public static final String srcFile = "emzasm/bin/zaporedje.bin";
    public static final boolean debugmode = false;

    // ! not final may change
    private static final int sizeOfRom = 1024; // size of ROM
    private static int[] ROM = new int[sizeOfRom]; // size of ROM

    public static int programCounter; // program counter
    // need to include for RT,JMP JMS
    private static int PPR; // prepare page reg
    private static int PBR; // prepare bank reg
    // program counterocation reg

    private static final int procesorFreq = 900000;

    // pins that means all of them inc. VCC and GND
    // 8D lines
    //
    //
    // private static int[] StateOfPins = new int[40];

    // registers
    private static byte accummulator; // accumulator
    private static byte BL;
    private static byte BU;
    private static byte E;

    private static byte[] KInputs = new byte[4];
    private static byte[] IInputs = new byte[4];
    private static byte KSelected = 0;
    private static byte ISelected = 0;

    // flags
    private static boolean secondsFlag;
    private static boolean carryFlag;
    private static boolean flagOne;
    private static boolean flagTwo;
    private static boolean invertedPowerOnDLines = false;
    private static boolean floatingModeOnDLines = false;

    // lach
    private static boolean selectorForProgramCounterOrLach = false;

    private static boolean EXTPulse = false;

    private static int displayLach = 0;
    private static int DLines = 0;
    private static int addresControlLines = 0;
    private static int masterStrobeLach = 0;

    private static boolean flagPP = false;
    private static boolean previousFlagPP = false;

    // ram
    private static byte[][] RAM = new byte[4][16]; // uses bytes but is 4 bit

    // stack //! to change
    private static final int sizeOfStack = 3; // depth of stack
    private static int[] stack = new int[sizeOfStack]; // stack
    private static byte stackPointer = 0; // if stack pointer == 0 that means that you arent in a subroutine

    public static void main(String[] args) {
        File file = null;
        List<Instruction> InstructionArray = getInstructionArray();
        Instruction[] instructionArray = new Instruction[InstructionArray.size()];
        for (int i = 0; i < InstructionArray.size(); i++) {
            instructionArray[i] = InstructionArray.get(i);
        }

        // ! IDK if it works
        if (args != null && args.length > 0) {
            file = new File(args[0].strip());
        } else {
            file = new File(srcFile);
        }
        // ROM init
        try {
            Main.ROM = readFile(file);
        } catch (FileToLongExeption e) {
            e.printStackTrace();
        }
        if (Main.debugmode == false) {
            simulation(instructionArray);
        } else {
            simulationDebug(instructionArray);
        }
        // readCLI();
        // Start a simulation
        // simulation(instructionArray);
        //
        // dumpROM();
        // dumpRAM();
    }

    private static List<Instruction> getInstructionArray() {
        InstructionSet set = new InstructionSet();
        int[] opCodes = set.getOpCodes();
        int[] masks = set.getMasks();
        List<Instruction> list = new LinkedList<Instruction>();
        for (int i = 0; i < opCodes.length; i++) {
            list.add(new Instruction(masks[i], opCodes[i]));
        }
        return list;
    }

    public static int[] readFile(File file) throws FileToLongExeption {
        long fileLength = file.length();
        if (fileLength > Main.sizeOfRom) {
            throw new FileToLongExeption("File is to long for a given ROM");
        }
        int[] bytes = new int[Main.sizeOfRom];
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

    public static void dumpROM() {
        System.out.println("Dump of ROM");
        for (int i = 0; i < Main.ROM.length; i++) {
            if (Main.ROM[i] != 0)
                System.out.println(
                        String.format("0x%04X", i) + " : "
                                + String.format("%8s", Integer.toBinaryString(Main.ROM[i])).replace(' ', '0'));
        }
    }

    public static void dumpRAM() {
        System.out.println("Dump of RAM");
        for (int i = 0; i < Main.RAM.length; i++) {
            for (int j = 0; j < Main.RAM[i].length; j++) {
                System.out.print(
                        String.format("%4s", Integer.toBinaryString(Main.RAM[i][j])).replace(' ', '0') + " ");
            }
            System.out.println();
        }
    }

    private static void simulation(Instruction[] ArraayOfInstructions) {
        SignalSimulator secondsFlagSetter = new SignalSimulator();
        SignalSimulator clockGenerator = new SignalSimulator();
        try {
            clockGenerator.setSignalFreqency(procesorFreq);
        } catch (NegativeFrequencyException e) {
            e.printStackTrace();
        }
        secondsFlagSetter.start();
        clockGenerator.start();
        int instruction = 0;
        while (true) {
            // dumpRAM();
            if (clockGenerator.getFlag()) {
                clockGenerator.setFlag(false);
                instruction = Main.ROM[Main.programCounter];
                Main.programCounter++;
                if (Main.programCounter >= Main.sizeOfRom) {
                    break;
                }
                int indexOfOnstruction = returnIndexOfInstruction(instruction, ArraayOfInstructions);
                if (indexOfOnstruction == -1) {
                    System.out.println("Instruction does not exist");
                    break;
                }
                if (ArraayOfInstructions[indexOfOnstruction].getMask() == 0x00) {
                    executeInstruction(ArraayOfInstructions[indexOfOnstruction].getOpCode(), 0,
                            secondsFlagSetter);
                } else {
                    executeInstruction(ArraayOfInstructions[indexOfOnstruction].getOpCode(),
                            instruction & ArraayOfInstructions[indexOfOnstruction].getMask(),
                            secondsFlagSetter);
                }
            }
        }
        dumpROM();
        dumpRAM();
        clockGenerator.kill();
        secondsFlagSetter.kill();
    }

    private static void simulationDebug(Instruction[] ArraayOfInstructions) {
        SignalSimulator secondsFlagSetter = new SignalSimulator();
        secondsFlagSetter.start();
        int instruction = 0;
        int indexOfOnstruction = 0;
        int executeNumberOf = 0;
        while (true) {
            executeNumberOf = readCLI();
            if (Main.programCounter >= Main.sizeOfRom) {
                break;
            }
            while (true) {
                if (executeNumberOf >= 0) {
                    instruction = Main.ROM[Main.programCounter];
                    indexOfOnstruction = returnIndexOfInstruction(instruction, ArraayOfInstructions);
                    executeNumberOf--;
                    Main.programCounter++;
                    if (ArraayOfInstructions[indexOfOnstruction].getMask() == 0x00) {
                        executeInstruction(ArraayOfInstructions[indexOfOnstruction].getOpCode(), 0,
                                secondsFlagSetter);
                    } else {
                        executeInstruction(ArraayOfInstructions[indexOfOnstruction].getOpCode(),
                                instruction & ArraayOfInstructions[indexOfOnstruction].getMask(),
                                secondsFlagSetter);
                    }
                    if (indexOfOnstruction == -1) {
                        System.out.println("Instruction does not exist");
                        break;
                    }
                }
                if (executeNumberOf == 0) {
                    dumpRAM();
                    System.out.println("Executing inst: " +
                            String.format("0x%04X ", ArraayOfInstructions[indexOfOnstruction].getOpCode()) +
                            " program counter " + Main.programCounter);
                    break;
                }
            }
        }
        secondsFlagSetter.kill();
    }

    private static void executeInstruction(int opCode, int param,
            /* SignalSimulator clock, */ SignalSimulator secondfFlag) {
        Main.EXTPulse = false;
        Main.previousFlagPP = Main.flagPP;
        Main.flagPP = false;
        int tmpInt = 0;
        byte tmpByte = 0;
        switch (opCode) {
            case 0x00: // NOP
                break;
            case 0x01: // BRK trated as NOP
                break;
            case 0x02: // RT
                Main.stackPointer--;
                if (Main.stackPointer < 0) {
                    Main.stackPointer = Main.sizeOfStack - 1;
                }
                tmpInt = programCounter & 0b1110000000000;
                tmpInt = tmpInt | (Main.stack[Main.stackPointer] & 0b1111111111);
                programCounter = tmpInt;
                break;
            case 0x03: // RTS
                Main.stackPointer--;
                if (Main.stackPointer < 0) {
                    Main.stackPointer = Main.sizeOfStack - 1;
                }
                tmpInt = programCounter & 0b1110000000000;
                tmpInt = tmpInt | (Main.stack[Main.stackPointer] & 0b1111111111);
                programCounter = tmpInt;
                skip();
                break;
            case 0x04: // PSH
                if ((Main.BL >= 0) && (Main.BL <= 12)) {
                    tmpInt = 1;
                    tmpInt = tmpInt << Main.BL;
                    Main.masterStrobeLach = Main.masterStrobeLach | tmpInt;
                    break;
                }
                if (Main.BL == 13) {
                    // set multiplex operation
                    break;
                }
                if (Main.BL == 14) {
                    Main.floatingModeOnDLines = false;
                    break;
                }
                if (Main.BL == 15) {
                    Main.masterStrobeLach = 0b1111111111111;
                    break;
                }
                break;
            case 0x05: // PSL
                if ((Main.BL >= 0) && (Main.BL <= 12)) {
                    tmpInt = 1;
                    tmpInt = tmpInt << Main.BL;
                    tmpInt = ~tmpInt;
                    Main.masterStrobeLach = Main.masterStrobeLach | tmpInt;
                    break;
                }
                if (Main.BL == 13) {
                    // set static operation
                    break;
                }
                if (Main.BL == 14) {
                    Main.floatingModeOnDLines = true;
                    break;
                }
                if (Main.BL == 15) {
                    Main.masterStrobeLach = 0b0;
                    break;
                }
                break;
            case 0x06: // AND
                accummulator = (byte) (accummulator & (RAM[BU][BL]) & 0x0F);
                break;
            case 0x07: // SOS
                if (Main.secondsFlag == true) {
                    Main.secondsFlag = false;
                    skip();
                }
                break;
            case 0x08: // SBE
                if (Main.E == Main.BL) {
                    skip();
                }
                break;
            case 0x09: // SZC
                if (Main.carryFlag == false) {
                    skip();
                }
                break;
            case 0x0A: // STC
                Main.carryFlag = true;
                break;
            case 0x0B: // RSC
                Main.carryFlag = false;
                break;
            case 0x0C: // LAE
                Main.accummulator = Main.E;
                break;
            case 0x0D: // XAE
                tmpByte = Main.accummulator;
                Main.accummulator = Main.E;
                Main.E = tmpByte;
                break;
            case 0x0E: // INP //TODO chc
                if (Main.floatingModeOnDLines == true) {
                    tmpInt = Main.DLines;
                    Main.accummulator = (byte) (Main.DLines & 0b1111);
                    Main.RAM[BU][BL] = (byte) ((tmpInt >> 4) & 0b1111);
                } else {
                    tmpInt = Main.displayLach;
                    Main.accummulator = (byte) (Main.displayLach & 0b1111);
                    Main.RAM[BU][BL] = (byte) ((tmpInt >> 4) & 0b1111);
                }
                break;
            case 0x0F: // EUR
                if ((accummulator & 0x1) == 1) {
                    Main.invertedPowerOnDLines = false;
                } else {
                    Main.invertedPowerOnDLines = true;
                }
                if (((accummulator >> 2) & 0x1) == 1) {
                    secondfFlag.setSignalFreqency(true);
                } else {
                    secondfFlag.setSignalFreqency(false);
                }
                break;
            case 0x10: // CMA
                Main.accummulator = (byte) (15 - Main.accummulator);
                break;
            case 0x11: // XABU
                tmpByte = Main.BU;
                Main.BU = (byte) (Main.accummulator & 0b11);
                Main.accummulator = (byte) (Main.accummulator & 0b1100);
                Main.accummulator = (byte) (Main.accummulator | tmpByte);
                break;
            case 0x12: // LAB
                Main.accummulator = Main.BL;
                break;
            case 0x13: // XAB
                tmpByte = Main.accummulator;
                Main.accummulator = Main.BL;
                Main.BL = tmpByte;
                break;
            case 0x14: // ADCS
                Main.accummulator = (byte) (Main.accummulator + Main.RAM[BU][BL]);
                if (Main.accummulator > 15) {
                    Main.carryFlag = true;
                } else {
                    Main.carryFlag = false;
                    skip();
                }
                Main.accummulator = (byte) (Main.accummulator & 0b1111);
                break;
            case 0x15: // XOR
                Main.accummulator = (byte) (Main.accummulator ^ Main.RAM[Main.BU][Main.BL]);
                break;
            case 0x16: // ADD
                Main.accummulator = (byte) (Main.RAM[Main.BU][Main.BL] + Main.accummulator);
                break;
            case 0x17: // SAM
                if (Main.accummulator == Main.RAM[Main.BU][Main.BL]) {
                    skip();
                }
                break;
            case 0x18: // DISB //! Not final may change
                Main.floatingModeOnDLines = false;
                Main.displayLach = Main.DLines = Main.RAM[Main.BU][Main.BL];
                Main.displayLach = Main.DLines = Main.displayLach << 4;
                Main.displayLach = Main.DLines = Main.accummulator;
                if (Main.invertedPowerOnDLines = true) {
                    Main.DLines = (~Main.DLines & 0b11111111);
                }
                break;
            case 0x19: // MVS
                Main.floatingModeOnDLines = true;
                Main.addresControlLines = Main.masterStrobeLach;
                break;
            case 0x1A: // OUT //! needs to output a pulse ;not implemented
                Main.DLines = Main.RAM[Main.BU][Main.BL];
                Main.DLines = Main.displayLach << 4;
                Main.displayLach = Main.DLines = Main.accummulator;
                if (Main.invertedPowerOnDLines = true) {
                    Main.DLines = (~Main.DLines & 0b11111111);
                }
                break;
            case 0x1B: // DISN //! Not final may change
                Main.floatingModeOnDLines = false;
                if (Main.carryFlag == true) {
                    Main.displayLach = 0b10000000;
                }
                switch (Main.accummulator) {
                    case 0:
                        Main.displayLach = Main.displayLach | 0b01111110;
                        break;
                    case 1:
                        Main.displayLach = Main.displayLach | 0b00110000;
                        break;
                    case 2:
                        Main.displayLach = Main.displayLach | 0b01101101;
                        break;
                    case 3:
                        Main.displayLach = Main.displayLach | 0b01111001;
                        break;
                    case 4:
                        Main.displayLach = Main.displayLach | 0b00110011;
                        break;
                    case 5:
                        Main.displayLach = Main.displayLach | 0b01011011;
                        break;
                    case 6:
                        Main.displayLach = Main.displayLach | 0b01011111;
                        break;
                    case 7:
                        Main.displayLach = Main.displayLach | 0b01110000;
                        break;
                    case 8:
                        Main.displayLach = Main.displayLach | 0b01111111;
                        break;
                    case 9:
                        Main.displayLach = Main.displayLach | 0b01111011;
                        break;
                    case 10:
                        Main.displayLach = Main.displayLach | 0b01110111;
                        break;
                    case 11:
                        Main.displayLach = Main.displayLach | 0b00011111;
                        break;
                    case 12:
                        Main.displayLach = Main.displayLach | 0b01001110;
                        break;
                    case 13:
                        Main.displayLach = Main.displayLach | 0b00111101;
                        break;
                    case 14:
                        Main.displayLach = Main.displayLach | 0b01001111;
                        break;
                    case 15:
                        Main.displayLach = Main.displayLach | 0b01000111;
                        break;
                    default:
                        break;
                }
                if (Main.invertedPowerOnDLines == true) {
                    Main.displayLach = (~Main.displayLach) & 0b11111111;
                }
                Main.DLines = Main.displayLach;
                break;
            case 0x1C: // SZM B
                tmpByte = Main.RAM[BU][BL];
                tmpByte = (byte) (tmpByte >> param);
                if ((tmpByte & 0b1) == 0) {
                    skip();
                }
                break;
            case 0x20: // STM B
                tmpByte = 0b1;
                tmpByte = (byte) (tmpByte << param);
                Main.RAM[BU][BL] = (byte) (Main.RAM[BU][BL] | tmpByte);
                break;
            case 0x24: // RSM B
                tmpByte = 0b1;
                tmpByte = (byte) (tmpByte << param);
                tmpByte = (byte) (~tmpByte & 0b01111111);
                Main.RAM[BU][BL] = (byte) (Main.RAM[BU][BL] & tmpByte);
                break;
            case 0x28: // SZK
                tmpByte = KSelected; // 8, 4, 2, 1 are the walues for individual bits
                boolean tmpFlagK = true;
                if ((tmpByte & 0b1000) == 1) {
                    if (Main.KInputs[4] == 1)
                        tmpFlagK = false;
                }
                if ((tmpByte & 0b100) == 1) {
                    if (Main.KInputs[3] == 1)
                        tmpFlagK = false;
                }
                if ((tmpByte & 0b10) == 1) {
                    if (Main.KInputs[2] == 1)
                        tmpFlagK = false;
                }
                if ((tmpByte & 0b1) == 1) {
                    if (Main.KInputs[1] == 1)
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
                    if (Main.IInputs[4] == 1)
                        tmpFlagI = false;
                }
                if ((tmpByte & 0b100) == 1) {
                    if (Main.IInputs[3] == 1)
                        tmpFlagI = false;
                }
                if ((tmpByte & 0b10) == 1) {
                    if (Main.IInputs[2] == 1)
                        tmpFlagI = false;
                }
                if ((tmpByte & 0b1) == 1) {
                    if (Main.IInputs[1] == 1)
                        tmpFlagI = false;
                }
                if (tmpFlagI) {
                    skip();
                }
                break;
            case 0x2A: // RF1
                Main.flagOne = false;
                break;
            case 0x2B: // ST1
                Main.flagOne = true;
                break;
            case 0x2C: // RF2
                Main.flagTwo = false;
                break;
            case 0x2D: // ST2
                Main.flagTwo = true;
                break;
            case 0x2E: // TF1
                if (Main.flagOne == true) {
                    skip();
                }
                break;
            case 0x2F: // TF2
                if (Main.flagTwo == true) {
                    skip();
                }
                break;
            case 0x30: // XCI Y*
                tmpByte = Main.RAM[Main.BU][Main.BL];
                Main.RAM[Main.BU][Main.BL] = Main.accummulator;
                Main.accummulator = tmpByte;
                Main.BU = (byte) (Main.BU ^ ~param);
                Main.BU = (byte) (Main.BU & 0x03);
                Main.BL++;
                Main.BL = (byte) (Main.BL & 0x0F);
                if (Main.BL == 0) {
                    skip();
                }
                break;
            case 0x34: // XCD Y*
                tmpByte = Main.RAM[Main.BU][Main.BL];
                Main.RAM[Main.BU][Main.BL] = Main.accummulator;
                Main.accummulator = tmpByte;
                Main.BU = (byte) (Main.BU ^ ~param);
                Main.BU = (byte) (Main.BU & 0x03);
                if (Main.BL >= 1) {
                    Main.BL--;
                } else {
                    Main.BL = 15;
                }
                if (Main.BL == 15) {
                    skip();
                }
                break;
            case 0x38: // XC Y*
                tmpByte = Main.RAM[Main.BU][Main.BL];
                Main.RAM[Main.BU][Main.BL] = Main.accummulator;
                Main.BU = (byte) (Main.BU ^ ~param);
                Main.BU = (byte) (Main.BU & 0x03);
                break;
            case 0x3C: // LAM Y*
                Main.accummulator = Main.RAM[Main.BU][Main.BL];
                Main.BU = (byte) (Main.BU ^ ~param);
                break;
            case 0x40: // LBZ Y
                Main.BL = 0x00;
                Main.BU = (byte) param;
                break;
            case 0x44: // LBF Y
                Main.BL = 0x0F;
                Main.BU = (byte) param;
                break;
            case 0x48: // LBE Y
                Main.BL = Main.E;
                Main.BU = (byte) param;
                break;
            case 0x4C: // LBEP Y
                Main.BL = (byte) ((Main.E + 1) & 0x0F);
                Main.BU = (byte) param;
                break;
            case 0x50: // ADIS
                Main.accummulator = (byte) (Main.accummulator + param);
                if (Main.accummulator <= 15) {
                    skip();
                }
                Main.accummulator = (byte) (Main.accummulator & 0b1111);
                break;
            case 0x60: // PP X*
                if (Main.previousFlagPP == false) {
                    Main.PPR = (byte) (~param & 0b1111);
                } else {
                    Main.PBR = (byte) (~param & 0b111);
                }
                Main.flagPP = true;
                break;
            case 0x70: // LAI X
                Main.accummulator = (byte) param;
                Main.KSelected = (byte) param;
                Main.ISelected = (byte) param;
                break;
            case 0x80: // JMS X
                if (previousFlagPP == true) {
                    tmpInt = Main.programCounter & 0b1111111111;
                    Main.stack[Main.stackPointer] = tmpInt;
                    Main.stackPointer++;
                    if (Main.stackPointer > 2) {
                        Main.stackPointer = 0;
                    }
                    Main.programCounter = (Main.PBR << 10) | (Main.PPR << 6) | param;
                } else {
                    tmpInt = Main.programCounter & 0b1111111111;
                    Main.stack[Main.stackPointer] = tmpInt;
                    Main.stackPointer++;
                    if (Main.stackPointer > 2) {
                        Main.stackPointer = 0;
                    }
                    Main.programCounter = (Main.programCounter & 0b1110000000000) | (15 << 6) | param;
                }
                break;
            case 0xC0: // JMP X
                if (previousFlagPP == true) {
                    Main.programCounter = (Main.PBR << 10) | (Main.PPR << 6) | param;
                } else {
                    tmpInt = Main.programCounter & 0b1111111000000;
                    tmpInt = tmpInt | param;
                    Main.programCounter = tmpInt;
                }
                break;
            default:
                System.out.println("Illegal instruction");
                return;
        }
        Main.previousFlagPP = false;
    }

    // 0x60 is PP and needs to be skiped
    private static void skip() {
        while ((Main.ROM[programCounter] & 0b11110000) == 0x60) {
            Main.programCounter++;
        }
        Main.programCounter++;
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

    public static void setKInputs(byte[] input) {
        for (int i = 0; i < KInputs.length; i++) {
            if (input[i] == 0 || input[i] == 1)
                Main.KInputs[i] = input[i];
        }
    }

    public static void setIInputs(byte[] input) {
        for (int i = 0; i < IInputs.length; i++) {
            if (input[i] == 0 || input[i] == 1)
                Main.IInputs[i] = input[i];
        }
    }

    public static int readCLI() {
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