package BackEnd.instructions;

public class Mult implements Instruction {
    private final InstructionType type = InstructionType.MULT;

    private final boolean isUnsigned;
    private final int rOperand1;
    private final int rOperand2;

    public Mult(int rOperand1, int rOperand2, boolean isUnsigned) {
        this.isUnsigned = isUnsigned;
        this.rOperand1 = rOperand1;
        this.rOperand2 = rOperand2;
    }

    @Override
    public double getCost() {
        return this.type.getCost();
    }

    @Override
    public String toString() {
        if(isUnsigned){
            return String.format("multu $%d $%d\n", rOperand1, rOperand2);
        } else {
            return String.format("mult $%d $%d\n", rOperand1, rOperand2);
        }
    }
}
