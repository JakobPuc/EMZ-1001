package src;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

public class Main {

    public static final String srcFile = "src/main.bin";



    // ! not final may change
    public static final int sizeOfRom = 1024; // size of ROM
    public static final byte[] ROM = new byte[sizeOfRom]; // size of ROM

    public static int programCounter; // program counter

    // registers
    public static byte accummulator; // accumulator
    public static byte BL;
    public static byte BU;
    public static byte E;

    // flags
    public static boolean secondsFlag;
    public static boolean carryFlag;

    // ram
    public static byte[][] RAM = new byte[4][16]; // uses bytes but is 4 bit

    // stack
    public static final int sizeOfStack = 4; // depth of stack
    public static int[] stack = new int[sizeOfStack]; // stack
    public static byte stackPointer = 0; // if stack pointer == 0 that means that you arent in a subroutine

    public static void main(String[] args) {
        // Read a file from method call or use default file
        // System.out.println(System.currentTimeMillis());
        File file = null;
        if (args != null && args.length > 0) {
            file = new File(args[0].strip());
        } else {
            file = new File(srcFile);
        }
        byte[] ROM = readFile(file);
    }

    public static byte[] readFile(File file) {
        byte[] bytes = new byte[(int) file.length()];
        try (DataInputStream input = new DataInputStream(new FileInputStream(file))) {

        } catch (Exception e) {
            // TODO: handle exception
        }

        return null;
    }
}