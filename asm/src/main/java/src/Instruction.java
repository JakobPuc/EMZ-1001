package src;

public class Instruction {
    private int mask;
    private int opCode;

    public Instruction (int mask, int opCode) {
        mask = mask & 0xFF;
        opCode = opCode & 0xFF;
        this.mask = mask;
        this.opCode = opCode;
    }

    public int getMask(){
        return this.mask;
    }

    public int getOpCode(){
        return this.opCode;
    }
}
