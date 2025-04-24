package src;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

public class Main {
    public static final int sizeOfStack = 4; // depth of stack
    public static final String srcFile = "src/main.bin";
    //public static final int sizeOfRom = 1024; // size of ROM

    public static int programCounter; // program counter
    public static byte accummulator; // accumulator
    public static byte BL;
    public static byte BU;
    public static byte E;

    //flags
    public static boolean secondsFlag;
    public static boolean carryFlag;


    public static void main(String[] args) {
        // Read a file from method call or use default file
        //System.out.println(System.currentTimeMillis());
        File file = null;
        if (args == null) {
            file = new File(args[0].strip());
        } else {
            file = new File(srcFile);
        }

    }

    public static byte [] readFile(File file) {
        byte[] bytes = new byte[(int) file.length()];
        try (DataInputStream input = new DataInputStream(new FileInputStream(file))) {
            

        } catch (Exception e) {
            // TODO: handle exception
        }

        return null;
    }
}