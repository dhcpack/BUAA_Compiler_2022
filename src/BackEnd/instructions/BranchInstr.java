package BackEnd.instructions;

public class BranchInstr implements Instruction {
    public enum BranchType {
        beq,
        bne,

        bge,
        bgt,

        ble,
        blt,


        blez,
        bltz,
        beqz,
        bnez,
        bgtz,
        bgez,
    }

    private final InstructionType type = InstructionType.BRANCH;

    private final BranchType branchType;
    private final int rOperand1;
    private int rOperand2;
    private int number;
    private boolean isNumber;
    private final String label;
    private final boolean isCalcBranch;

    public BranchInstr(BranchType branchType, int rOperand1, int numberOrROperand, String label, boolean isNumber) {
        this.branchType = branchType;
        this.rOperand1 = rOperand1;
        this.label = label;
        this.isNumber = isNumber;
        if (isNumber) {
            this.number = numberOrROperand;
        } else {
            this.rOperand2 = numberOrROperand;
        }
        this.isCalcBranch = true;
    }

    public BranchInstr(BranchType branchType, int rOperand1, String label) {
        this.branchType = branchType;
        this.rOperand1 = rOperand1;
        this.label = label;
        this.isCalcBranch = false;
    }

    public boolean isCalcBranch() {
        return this.isCalcBranch;
    }

    public boolean isNumber() {
        return this.isNumber;
    }

    public int getNumber() {
        assert isNumber;
        return number;
    }

    public BranchType getBranchType() {
        return this.branchType;
    }

    public int getrOperand1() {
        return rOperand1;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public double getCost() {
        return this.type.getCost();
    }

    @Override
    public String toString() {
        if (this.isCalcBranch) {
            if (isNumber) {
                return String.format("%s $%d, %d, %s\n", branchType.name(), rOperand1, number, label);
            } else {
                return String.format("%s $%d, $%d, %s\n", branchType.name(), rOperand1, rOperand2, label);
            }
        } else {
            if (branchType == BranchType.beqz || branchType == BranchType.bnez || branchType == BranchType.bltz || branchType == BranchType.bgtz || branchType == BranchType.bgez || branchType == BranchType.blez) {
                return String.format("%s $%d, %s\n", branchType.name(), rOperand1, label);
            } else {
                assert false;
                return null;
            }
        }
    }
}
