package src;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Main {

    public static final String srcFile = "vm/src/main/resources/out.bin";

    // ! not final may change
    private static final int sizeOfRom = 1024; // size of ROM
    private static int[] ROM = new int[sizeOfRom]; // size of ROM

    public static int programCounter; // program counter

    private static final int procesorFreq = 900000;

    // pins that means all of them inc. VCC and GND
    // 8D lines
    //
    //
    private static int[] StateOfPins = new int[40];

    // registers
    private static byte accummulator; // accumulator
    private static byte BL;
    private static byte BU;
    private static byte E;

    // flags
    private static boolean secondsFlag;
    private static boolean carryFlag;

    // ram
    private static byte[][] RAM = new byte[4][16]; // uses bytes but is 4 bit

    // stack
    private static final int sizeOfStack = 4; // depth of stack
    private static int[] stack = new int[sizeOfStack]; // stack
    private static byte stackPointer = 0; // if stack pointer == 0 that means that you arent in a subroutine

    public static void main(String[] args) {
        // Read a file from method call or use default file
        // System.out.println(System.currentTimeMillis());
        File file = null;
        List<Instruction> InstructionArray = getInstructionArray();
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
        // Start a simulation
        simulation(InstructionArray);
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
            System.out.println(
                    String.format("0x%04X", i) + " : "
                            + String.format("%8s", Integer.toBinaryString(Main.ROM[i])).replace(' ', '0'));
        }
    }

    public static void dumpRAM() {
        System.out.println("Dump of RAM");
        for (int i = 0; i < Main.RAM.length; i++) {
            for (int j = 0; j < Main.RAM[i].length; j++) {
                System.out.print(String.format("%4s", Integer.toBinaryString(Main.RAM[i][j])).replace(' ', '0') + " ");
            }
            System.out.println();
        }
    }

    private static void simulation(List<Instruction> listOfinstructions) {
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
                instruction = Main.ROM[Main.programCounter];
                Main.programCounter++;
                if (Main.programCounter >= Main.sizeOfRom) {
                    break;
                }
                int indexOfOnstruction = returnIndexOfInstruction(instruction, listOfinstructions);
                if(indexOfOnstruction == -1){
                    System.out.println("Instruction does not exist");
                    break;
                }
                System.out.println(indexOfOnstruction);
                // instruction = instruction & 0xFF;
                // System.out.println(instruction);

            }
        }
        clockGenerator.kill();
        secondsFlagSetter.kill();
    }

    private static int returnIndexOfInstruction(int instruction, List<Instruction> listOFInstructions) {
        int i = 0;
        for (; i < listOFInstructions.size(); i++) {
            Instruction ins = listOFInstructions.get(i);
            if(ins.getOpCode() == (instruction & (~ins.getMask()))){
                return i;
            }
        }
        return -1;
    }
}

class FileToLongExeption extends Exception {
    public FileToLongExeption(String message) {
        super(message);
    }
}