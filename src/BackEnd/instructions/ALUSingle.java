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
    private final int immediate;
    private final String label;

    public ALUSingle(ALUSingleType aluSingleType, int rResult, int immediate) {
        this.aluSingleType = aluSingleType;
        this.rResult = rResult;
        this.immediate = immediate;
        this.label = null;
    }

    public ALUSingle(ALUSingleType aluSingleType, int rResult, String label) {
        this.aluSingleType = aluSingleType;
        this.rResult = rResult;
        this.immediate = -2022;  // not use
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
            return String.format("%s $%d, %d\n", aluSingleType.name(), rResult, immediate);
        } else {
            assert false;
            return null;
        }
    }
}
