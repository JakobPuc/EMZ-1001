package src;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

public class Main {

    public static final String srcFile = "src/main.bin";

    // instruction set
    public static final int[] instructionSet = {
            0x00, // NOP
            0x01, // BREAKE CODE NOT TO USE FOR DEVELOPMENT
            0x02, // LRT
            0x03, // RTS
            0x04, // PSH
            0x05, // PSL
            0x06, // AND
            0x07, // SOS
            0x08, // SBE
            0x09, // SZC
            0x0A, // STC
            0x0B, // RSC
            0x0C, // LAE
            0x0D, // XAE
            0x0E, // INP
            0x0F, // EUR
            0x10, // CMA
            0x11, // XABU
            0x12, // LAB
            0x13, // XAB
            0x14, // ADCS
            0x15, // XOR
            0x16, // ADD
            0x17, // SAM
            0x18, // DISB
            0x19, // MVS
            0x1A, // OUT
            0x1B, // DISN
            0x1C, // * SZM B
            0x1D, // * SZM B
            0x1E, // * SZM B
            0x1F, // * SZM B
            0x20, // ** STM B
            0x21, // ** STM B
            0x22, // ** STM B
            0x23, // ** STM B
            0x24, // * RSM B
            0x25, // * RSM B
            0x26, // * RSM B
            0x27, // * RSM B
            0x28, // SZK
            0x29, // SZI
            0x2A, // RF1
            0x2B, // SF1
            0x2C, // RF2
            0x2D, // SF2
            0x2E, // TF1
            0x2F, // TF2
            0x30, // * XCI Y*
            0x31, // * XCI Y*
            0x32, // * XCI Y*
            0x33, // * XCI Y*
            0x34, // ** XCD Y*
            0x35, // ** XCD Y*
            0x36, // ** XCD Y*
            0x37, // ** XCD Y*
            0x38, // * XC Y*
            0x39, // * XC Y*
            0x3A, // * XC Y*
            0x3B, // * XC Y*
            0x3C, // ** LAM Y*
            0x3D, // ** LAM Y*
            0x3E, // ** LAM Y*
            0x3F, // ** LAM Y*
            0x40, // * LBZ Y
            0x41, // * LBZ Y
            0x42, // * LBZ Y
            0x43, // * LBZ Y
            0x44, // ** LBF Y
            0x45, // ** LBF Y
            0x46, // ** LBF Y
            0x47, // ** LBF Y
            0x48, // * LBE Y
            0x49, // * LBE Y
            0x4A, // * LBE Y
            0x4B, // * LBE Y
            0x4C, // ** LBMP Y
            0x4D, // ** LBMP Y
            0x4E, // ** LBMP Y
            0x4F, // ** LBMP Y
            0x50, // * ADIS X
            0x51, // * ADIS X
            0x52, // * ADIS X
            0x53, // * ADIS X
            0x54, // * ADIS X
            0x55, // * ADIS X
            0x56, // * ADIS X
            0x57, // * ADIS X
            0x58, // * ADIS X
            0x59, // * ADIS X
            0x5A, // * ADIS X
            0x5B, // * ADIS X
            0x5C, // * ADIS X
            0x5D, // * ADIS X
            0x5E, // * ADIS X
            0x5F, // * ADIS X
            0x60, // ** PP X*
            0x61, // ** PP X*
            0x62, // ** PP X*
            0x63, // ** PP X*
            0x64, // ** PP X*
            0x65, // ** PP X*
            0x66, // ** PP X*
            0x67, // ** PP X*
            0x68, // ** PP X*
            0x69, // ** PP X*
            0x6A, // ** PP X*
            0x6B, // ** PP X*
            0x6C, // ** PP X*
            0x6D, // ** PP X*
            0x6E, // ** PP X*
            0x6F, // ** PP X*
            0x70, // * LAI X
            0x71, // * LAI X
            0x72, // * LAI X
            0x73, // * LAI X
            0x74, // * LAI X
            0x75, // * LAI X
            0x76, // * LAI X
            0x77, // * LAI X
            0x78, // * LAI X
            0x79, // * LAI X
            0x7A, // * LAI X
            0x7B, // * LAI X
            0x7C, // * LAI X
            0x7D, // * LAI X
            0x7E, // * LAI X
            0x7F, // * LAI X
            0x80, // ** JMS X
            0x81, // ** JMS X
            0x82, // ** JMS X
            0x83, // ** JMS X
            0x84, // ** JMS X
            0x85, // ** JMS X
            0x86, // ** JMS X
            0x87, // ** JMS X
            0x88, // ** JMS X
            0x89, // ** JMS X
            0x8A, // ** JMS X
            0x8B, // ** JMS X
            0x8C, // ** JMS X
            0x8D, // ** JMS X
            0x8E, // ** JMS X
            0x8F, // ** JMS X
            0xC0, // ** JMP X
            0xC1, // ** JMP X
            0xC2, // ** JMP X
            0xC3, // ** JMP X
            0xC4, // ** JMP X
            0xC5, // ** JMP X
            0xC6, // ** JMP X
            0xC7, // ** JMP X
            0xC8, // ** JMP X
            0xC9, // ** JMP X
            0xCA, // ** JMP X
            0xCB, // ** JMP X
            0xCC, // ** JMP X
            0xCD, // ** JMP X
            0xCE, // ** JMP X
            0xCF, // ** JMP X
            0xD0, // ** JMP X
            0xD1, // ** JMP X
            0xD2, // ** JMP X
            0xD3, // ** JMP X
            0xD4, // ** JMP X
            0xD5, // ** JMP X
            0xD6, // ** JMP X
            0xD7, // ** JMP X
            0xD8, // ** JMP X
            0xD9, // ** JMP X
            0xDA, // ** JMP X
            0xDB, // ** JMP X
            0xDC, // ** JMP X
            0xDD, // ** JMP X
            0xDE, // ** JMP X
            0xDF, // ** JMP X
            0xE0, // ** JMP X
            0xE1, // ** JMP X
            0xE2, // ** JMP X
            0xE3, // ** JMP X
            0xE4, // ** JMP X
            0xE5, // ** JMP X
            0xE6, // ** JMP X
            0xE7, // ** JMP X
            0xE8, // ** JMP X
            0xE9, // ** JMP X
            0xEA, // ** JMP X
            0xEB, // ** JMP X
            0xEC, // ** JMP X
            0xED, // ** JMP X
            0xEE, // ** JMP X
            0xEF, // ** JMP X
            0xF0, // ** JMP X
            0xF1, // ** JMP X
            0xF2, // ** JMP X
            0xF3, // ** JMP X
            0xF4, // ** JMP X
            0xF5, // ** JMP X
            0xF6, // ** JMP X
            0xF7, // ** JMP X
            0xF8, // ** JMP X
            0xF9, // ** JMP X
            0xFA, // ** JMP X
            0xFB, // ** JMP X
            0xFC, // ** JMP X
            0xFD, // ** JMP X
            0xFE, // ** JMP X
            0xFF  // ** JMP X
    };

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