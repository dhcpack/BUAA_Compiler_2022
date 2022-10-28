package BackEnd.instructions;

// 单寄存器
public class ALUSingle implements Instruction{
    public enum ALUSingleType {
        li,


    }

    private final InstructionType type = InstructionType.ALU;

    private final ALUSingle.ALUSingleType aluSingleType;
    private final int rResult;
    private final int immediate;

    public ALUSingle(ALUSingleType aluSingleType, int rResult, int immediate) {
        this.aluSingleType = aluSingleType;
        this.rResult = rResult;
        this.immediate = immediate;
    }

    @Override
    public String toString() {
        return String.format("%s $%d, %d\n", aluSingleType.name(), rResult, immediate);
    }
}
