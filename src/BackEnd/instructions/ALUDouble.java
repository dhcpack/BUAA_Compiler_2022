package BackEnd.instructions;

// 两寄存器
public class ALUDouble implements Instruction {
    public enum ALUDoubleType {
        addiu,
        andi,
        ori,

    }

    private final InstructionType type = InstructionType.ALU;

    private final ALUDoubleType aluDoubleType;
    private final int rOperand;
    private final int rResult;
    private final int immediate;

    public ALUDouble(ALUDoubleType aluDoubleType, int rOperand, int rResult, int immediate) {
        this.aluDoubleType = aluDoubleType;
        this.rOperand = rOperand;
        this.rResult = rResult;
        this.immediate = immediate;
    }

    @Override
    public String toString() {
        return String.format("%s $%d, $%d, %d\n", aluDoubleType.name(), rResult, rOperand, immediate);
    }
}
