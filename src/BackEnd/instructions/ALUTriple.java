package BackEnd.instructions;

// 三寄存器
public class ALUTriple implements Instruction{
    public enum ALUTripleType {
        addu,
        and,
        or,
        subu,

        seq,
        sne,  // 可以用来写NOT(sne $t1, $0, $t0  # t0不是0时t1置一 )

        slt,  // 一条
        sgt,  // 一条
        sge,  // 三条
        sle,  // 三条
    }

    private final InstructionType type = InstructionType.ALU;

    private final ALUTripleType aluTripleType;
    private final int rResult;
    private final int rOperand1;
    private final int rOperand2;

    public ALUTriple(ALUTripleType aluTripleType, int rResult, int rOperand1, int rOperand2) {
        this.aluTripleType = aluTripleType;
        this.rResult = rResult;
        this.rOperand1 = rOperand1;
        this.rOperand2 = rOperand2;
    }

    @Override
    public double getCost() {
        return this.type.getCost();
    }

    @Override
    public String toString() {
        return String.format("%s $%d, $%d, $%d\n", aluTripleType.name(), rResult, rOperand1, rOperand2);
    }
}
