package BackEnd.instructions;

// 三寄存器
public class ALUTriple implements Instruction{
    public enum ALUTripleType {
        addu,
        and,
        or,
        subu,

    }

    private final InstructionType type = InstructionType.ALU;

    private final ALUTripleType aluTripleType;
    private final int rOperand1;
    private final int rOperand2;
    private final int rResult;

    public ALUTriple(ALUTripleType aluTripleType, int rOperand1, int rOperand2, int rResult) {
        this.aluTripleType = aluTripleType;
        this.rOperand1 = rOperand1;
        this.rOperand2 = rOperand2;
        this.rResult = rResult;
    }

    @Override
    public String toString() {
        return String.format("%s $%d, $%d, %d\n", aluTripleType.name(), rResult, rOperand1, rOperand2);
    }
}
