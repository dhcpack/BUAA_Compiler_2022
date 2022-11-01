package BackEnd.instructions;

// 两寄存器
public class ALUDouble implements Instruction {
    public enum ALUDoubleType {
        addiu,
        andi,
        ori,

        sne,
        seq,

        // slti, 可能出现立即数越界的问题，加载到寄存器后用slt代替
        sle,
        sgt,
        sge,
    }

    private final InstructionType type = InstructionType.ALU;

    private final ALUDoubleType aluDoubleType;
    private final int rResult;
    private final int rOperand;
    private final int immediate;

    public ALUDouble(ALUDoubleType aluDoubleType, int rResult, int rOperand, int immediate) {
        this.aluDoubleType = aluDoubleType;
        this.rResult = rResult;
        this.rOperand = rOperand;
        this.immediate = immediate;
    }

    @Override
    public String toString() {
        return String.format("%s $%d, $%d, %d\n", aluDoubleType.name(), rResult, rOperand, immediate);
    }
}
