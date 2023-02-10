package BackEnd.instructions;

// 单寄存器
public class ALUSingle implements Instruction {
    public enum ALUSingleType {
        li,
        la,
    }

    private final InstructionType type = InstructionType.ALU;

    private final ALUSingle.ALUSingleType aluSingleType;
    private final int rResult;

    // for li
    private boolean isInt;
    private int intImmediate;
    private long longImmediate;

    // for la
    private final String label;

    public ALUSingle(ALUSingleType aluSingleType, int rResult, int immediate) {
        this.aluSingleType = aluSingleType;
        this.rResult = rResult;
        this.isInt = true;
        this.intImmediate = immediate;
        this.label = null;
    }

    public ALUSingle(ALUSingleType aluSingleType, int rResult, long immediate){
        this.aluSingleType = aluSingleType;
        this.rResult = rResult;
        this.isInt = false;
        this.longImmediate = immediate;
        this.label = null;
    }

    public ALUSingle(ALUSingleType aluSingleType, int rResult, String label) {
        this.aluSingleType = aluSingleType;
        this.rResult = rResult;
        this.intImmediate = -2022;  // not use
        this.label = label;
    }

    @Override
    public double getCost() {
        return this.type.getCost();
    }

    @Override
    public String toString() {
        if (this.aluSingleType == ALUSingleType.la) {
            assert this.label != null;
            return String.format("%s $%d, %s\n", aluSingleType.name(), rResult, label);
        } else if (this.aluSingleType == ALUSingleType.li) {
            assert this.label == null;
            if(this.isInt){
                return String.format("%s $%d, %d\n", aluSingleType.name(), rResult, intImmediate);
            } else {
                return String.format("%s $%d, %d\n", aluSingleType.name(), rResult, longImmediate);
            }
        } else {
            assert false;
            return null;
        }
    }
}
