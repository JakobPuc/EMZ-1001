package src;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class OldMain {
        public static final String srcFile = "emzasm/bin/zaporedje.bin";

    // ! not final may change
    private static final int sizeOfRom = 1024; // size of ROM
    private static int[] ROM = new int[sizeOfRom]; // size of ROM

    public static int programCounter; // program counter

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
    // need to include for RT,JMP JMS
    private static int PPR;
    private static int PBR;

    // flags
    private static boolean secondsFlag;
    private static boolean carryFlag;
    private static boolean flagOne;
    private static boolean flagTwo;

    private static boolean previouPP = false;

    // ram
    private static byte[][] RAM = new byte[4][16]; // uses bytes but is 4 bit

    // stack //! to change
    private static final int sizeOfStack = 3; // depth of stack
    private static int[] stack = new int[sizeOfStack]; // stack
    private static byte stackPointer = 0; // if stack pointer == 0 that means that you arent in a subroutine

    public static void main(String[] args) {
        // Read a file from method call or use default file
        // System.out.println(System.currentTimeMillis());
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
            OldMain.ROM = readFile(file);
        } catch (FileToLongExeption e) {
            e.printStackTrace();
        }
        // Start a simulation
        simulation(instructionArray);
        //
        dumpROM();
        dumpRAM();
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
        if (fileLength > OldMain.sizeOfRom) {
            throw new FileToLongExeption("File is to long for a given ROM");
        }
        int[] bytes = new int[OldMain.sizeOfRom];
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
        for (int i = 0; i < OldMain.ROM.length; i++) {
            System.out.println(
                    String.format("0x%04X", i) + " : "
                            + String.format("%8s", Integer.toBinaryString(OldMain.ROM[i])).replace(' ', '0'));
        }
    }

    public static void dumpRAM() {
        System.out.println("Dump of RAM");
        for (int i = 0; i < OldMain.RAM.length; i++) {
            for (int j = 0; j < OldMain.RAM[i].length; j++) {
                System.out.print(String.format("%4s", Integer.toBinaryString(OldMain.RAM[i][j])).replace(' ', '0') + " ");
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
            if (clockGenerator.getFlag()) {
                clockGenerator.setFlag(false);
                instruction = OldMain.ROM[OldMain.programCounter];
                OldMain.programCounter++;
                if (OldMain.programCounter >= OldMain.sizeOfRom) {
                    break;
                }
                int indexOfOnstruction = returnIndexOfInstruction(instruction, ArraayOfInstructions);
                if (indexOfOnstruction == -1) {
                    System.out.println("Instruction does not exist");
                    break;
                }
                if (ArraayOfInstructions[indexOfOnstruction].getMask() == 0x00) {
                    executeInstruction(ArraayOfInstructions[indexOfOnstruction].getOpCode(), 0, clockGenerator,
                            secondsFlagSetter);
                } else {
                    executeInstruction(ArraayOfInstructions[indexOfOnstruction].getOpCode(),
                            instruction & ArraayOfInstructions[indexOfOnstruction].getMask(), clockGenerator,
                            secondsFlagSetter);
                }

            }
        }
        clockGenerator.kill();
        secondsFlagSetter.kill();
    }

    private static void executeInstruction(int opCode, int param, SignalSimulator clock, SignalSimulator secondfFlag) {
        byte tmp = 0;
        switch (opCode) {
            case 0x00: // NOP
            case 0x01: // BRK trated as NOP
                break;
            case 0x02: // RT
                OldMain.stackPointer--;
                if (OldMain.stackPointer < 0) {
                    OldMain.stackPointer = OldMain.sizeOfStack - 1;
                }
                OldMain.programCounter = OldMain.stack[stackPointer];
                break;
            case 0x03: // RTS
                OldMain.stackPointer--;
                if (OldMain.stackPointer < 0) {
                    OldMain.stackPointer = OldMain.sizeOfStack - 1;
                }
                OldMain.programCounter = OldMain.stack[stackPointer];
                skip();
                break;
            case 0x04: // PSH
                break;
            case 0x05: // PSL
                break;
            case 0x06: // AND
                accummulator = (byte) (accummulator & (RAM[BU][BL]) & 0x0F);
                break;
            case 0x07:
                if (secondfFlag.getFlag() == true) {
                    secondfFlag.setFlag(false);
                    skip();
                }
                break;
            case 0x08: // SBE
                if (BL == accummulator) {
                    skip();
                }
                break;
            case 0x09: // SZC
                if (carryFlag == false) {
                    skip();
                }
                break;
            case 0x0A: // STC
                carryFlag = true;
                break;
            case 0x0B: // RSC
                carryFlag = false;
                break;
            case 0x0C: // LAE
                accummulator = E;
                break;
            case 0x0D:// XAE
                tmp = E;
                E = accummulator;
                accummulator = tmp;
                break;
            case 0x0E: // INP
                break;
            case 0x0F:
                if ((accummulator & 0x1) == 1) {
                    // Do somthing IO
                } else {
                    // Do somthing else IO
                }
                if (((accummulator >> 2) & 0x1) == 1) {
                    secondfFlag.setSignalFreqency(true);
                } else {
                    secondfFlag.setSignalFreqency(false);
                }
                break;
            case 0x10: // CMA
                OldMain.accummulator = (byte) ~OldMain.accummulator;
                break;
            case 0x11: // XABU
                tmp = (byte) (OldMain.BU & 0x3);
                OldMain.BU = (byte) (accummulator & 0x03);
                OldMain.accummulator = (byte) ((OldMain.accummulator & 0b00000011) | tmp);
                break;
            case 0x12: // LAB
                OldMain.accummulator = OldMain.BL;
                break;
            case 0x13: // XAB
                tmp = OldMain.BL;
                OldMain.BL = OldMain.accummulator;
                OldMain.accummulator = tmp;
                break;
            case 0x14: // ADCS
                if (OldMain.carryFlag == true) {
                    tmp = OldMain.RAM[OldMain.BU][OldMain.BL];
                }
                tmp++;
                OldMain.accummulator = (byte) (OldMain.accummulator + tmp);
                if (OldMain.accummulator >= 15) {
                    skip();
                    carryFlag = true;
                } else {
                    carryFlag = false;
                }
                break;
            case 0x15: // XOR
                OldMain.accummulator = (byte) (OldMain.accummulator ^ OldMain.RAM[OldMain.BU][OldMain.BL]);
                break;
            case 0x16: // ADD
                OldMain.accummulator = (byte) (OldMain.RAM[OldMain.BU][OldMain.BL] + OldMain.accummulator);
                break;
            case 0x17: // SAM
                if (OldMain.accummulator == OldMain.RAM[OldMain.BU][OldMain.BL]) {
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
                tmp = OldMain.RAM[OldMain.BU][OldMain.BL];
                if ((0 <= param) && (param <= 3)) {
                    tmp = (byte) (tmp >> param);
                    tmp = (byte) (tmp & 0x01);
                    if (tmp == 0x01) {
                        skip();
                    }
                } else {
                    System.out.println("Index out of bounts");
                }
                break;
            case 0x20: // STM B
                tmp = 0x01;// Main.RAM[Main.BU][Main.BL];
                if ((0 <= param) && (param <= 3)) {
                    param = param - 1;
                    tmp = (byte) (tmp << param);
                    OldMain.RAM[OldMain.BU][OldMain.BL] = (byte) (OldMain.RAM[OldMain.BU][OldMain.BL] | tmp);
                } else {
                    System.out.println("Index out of bounts");
                }
                break;
            case 0x24: // RSM B
                tmp = 0x01;// Main.RAM[Main.BU][Main.BL];
                if ((0 <= param) && (param <= 3)) {
                    param = param - 1;
                    tmp = (byte) (tmp << param);
                    tmp = (byte) (~tmp);
                    OldMain.RAM[OldMain.BU][OldMain.BL] = (byte) (OldMain.RAM[OldMain.BU][OldMain.BL] & tmp);
                } else {
                    System.out.println("Index out of bounts");
                }
                break;
            case 0x28: // SZK
                break;
            case 0x29: // SZI
                break;
            case 0x2A: // RF1
                OldMain.flagOne = false;
                break;
            case 0x2B: // ST1
                OldMain.flagOne = true;
                break;
            case 0x2C: // RF2
                OldMain.flagTwo = false;
                break;
            case 0x2D: // ST2
                OldMain.flagTwo = true;
                break;
            case 0x2E: // TF1
                if (OldMain.flagOne == true) {
                    skip();
                }
                break;
            case 0x2F: // TF2
                if (OldMain.flagTwo == true) {
                    skip();
                }
                break;
                case 0x30: // XCI Y*
                System.out.println(~param);
                tmp = OldMain.RAM[OldMain.BU][OldMain.BL];
                OldMain.RAM[OldMain.BU][OldMain.BL] = OldMain.accummulator;
                OldMain.accummulator = tmp;
                if ((0 <= (~param & 3)) && ((~param & 3) <= 3)) {
                    OldMain.BU = (byte) (OldMain.BU ^ ~param);
                    OldMain.BU = (byte) (OldMain.BU & 0x03);
                    OldMain.BL++;
                    OldMain.BL = (byte) (OldMain.BL & 0x0F);
                    if (OldMain.BL == 0) {
                        skip();
                    }
                } else {
                    System.out.println("Index out of bounts");
                }
                break;
            case 0x34: // XCD Y*
                tmp = OldMain.RAM[OldMain.BU][OldMain.BL];
                OldMain.RAM[OldMain.BU][OldMain.BL] = OldMain.accummulator;
                OldMain.accummulator = tmp;
                if ((0 <= param) && (param <= 3)) {
                    OldMain.BU = (byte) (OldMain.BU + param);
                    OldMain.BU = (byte) (OldMain.BU & 0x03);
                    if (OldMain.BL >= 1) {
                        OldMain.BL--;
                    } else {
                        OldMain.BL = 15;
                    }
                    if (OldMain.BL == 15) {
                        skip();
                    }
                } else {
                    System.out.println("Index out of bounts");
                }
                break;
            case 0x38:
                tmp = OldMain.RAM[OldMain.BU][OldMain.BL];
                OldMain.RAM[OldMain.BU][OldMain.BL] = OldMain.accummulator;
                OldMain.BU++;
                OldMain.BU = (byte) (OldMain.BU & 0x03);
                break;
            case 0x3C: // LAM Y*
                OldMain.accummulator = OldMain.RAM[OldMain.BU][OldMain.BL];
                OldMain.BU = (byte) (OldMain.BU ^ param);
                OldMain.BU = (byte) (OldMain.BU & 0x03);
                break;
            case 0x40: // LBZ Y
                if ((0 <= param) && (param <= 3)) {
                    OldMain.BL = 0x00;
                    OldMain.BU = (byte) param;
                } else {
                    System.out.println("Index out of bounts");
                }
                break;
            case 0x44: // LBF Y
                if ((0 <= param) && (param <= 3)) {
                    OldMain.BL = 0x0F;
                    OldMain.BU = (byte) param;
                } else {
                    System.out.println("Index out of bounts");
                }
                break;
            case 0x48: // LBE Y
                if ((0 <= param) && (param <= 3)) {
                    OldMain.BL = OldMain.E;
                    OldMain.BU = (byte) param;
                } else {
                    System.out.println("Index out of bounts");
                }
                break;
            case 0x4C: // LBEP Y
                if ((0 <= param) && (param <= 3)) {
                    OldMain.BL = (byte) ((OldMain.E + 1) & 0x0F);
                    OldMain.BU = (byte) param;
                } else {
                    System.out.println("Index out of bounts");
                }
                break;
            case 0x50: // ADIS X
                if ((0 <= param) && (param <= 15)) {
                    OldMain.accummulator = (byte) (OldMain.accummulator + param);
                    if (OldMain.accummulator <= 15) {
                        skip();
                    }
                } else {
                    System.out.println("Index out of bounts");
                }
                break;
            case 0x60: // PP X*
                if (OldMain.previouPP == false) {
                    OldMain.previouPP = true;
                    if ((0 <= param) && (param <= 15)) {
                        OldMain.PPR = (byte) param;
                    } else {
                        System.out.println("Index out of bounts");
                    }
                } else {
                    OldMain.previouPP = false;
                    if ((0 <= param) && (param <= 7)) {
                        OldMain.PBR = (byte) param;
                    } else {
                        System.out.println("Index out of bounts");
                    }
                }
                break;
            case 0x70: // LAI X //! no IO
                if ((0 <= param) && (param <= 15)) {
                    OldMain.accummulator = (byte) param;
                } else {
                    System.out.println("Index out of bounts");
                }
                break;

            case 0x80: // JMS X
                break;
            case 0xC0: // JMP X
                break;
            default:
                System.out.println("Illigal instruction");
                return;
        }

    }

    private static void skip() {
        while ((OldMain.ROM[programCounter] & 0b11110000) == 0x60) {
            programCounter++;
        }
        programCounter++;
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
}
