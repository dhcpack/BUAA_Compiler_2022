package BackEnd.instructions;

public class BranchInstr implements Instruction {
    public enum BranchType {
        beq,
        bne,

        bgez,
        bgtz,

        blez,
        bltz,
    }

    private final InstructionType type = InstructionType.BRANCH;

    private final BranchType branchType;
    private final int rOperand1;
    private int rOperand2;
    private final String label;

    public BranchInstr(BranchType branchType, int rOperand1, int rOperand2, String label) {
        this.branchType = branchType;
        this.rOperand1 = rOperand1;
        this.rOperand2 = rOperand2;
        this.label = label;
    }

    public BranchInstr(BranchType branchType, int rOperand1, String label) {
        this.branchType = branchType;
        this.rOperand1 = rOperand1;
        this.label = label;
    }

    @Override
    public double getCost() {
        return this.type.getCost();
    }

    @Override
    public String toString() {
        if (branchType == BranchType.bne || branchType == BranchType.beq) {
            return String.format("%s $%d, $%d, %s\n", branchType.name(), rOperand1, rOperand2, label);
        } else {
            return String.format("%s $%d, %s\n", branchType.name(), rOperand1, label);
        }
    }
}
