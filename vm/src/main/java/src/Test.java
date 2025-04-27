package src;

public class Test {
    public static void main(String[] args) {
        InstructionSet set = new InstructionSet();
        int []  opCodes = set.getOpCodes();
        int [] mask = set.getMasks();
        for(int i = 0 ; i < opCodes.length; i++){
            System.out.println("Opcode: "+opCodes[i]+" Mask: "+mask[i]);
        }
    }
}