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

        // 位运算
        sll,
        srl,
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

    public ALUDoubleType getAluDoubleType() {
        return aluDoubleType;
    }

    public int getrResult() {
        return rResult;
    }

    public int getrOperand() {
        return rOperand;
    }

    public int getImmediate() {
        return immediate;
    }

    @Override
    public double getCost() {
        return this.type.getCost();
    }

    @Override
    public String toString() {
        return String.format("%s $%d, $%d, %d\n", aluDoubleType.name(), rResult, rOperand, immediate);
    }
}
