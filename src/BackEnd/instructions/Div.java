package BackEnd.instructions;

public class Div implements Instruction{
    private final InstructionType type = InstructionType.DIV;

    private final int rOperand1;
    private final int rOperand2;

    public Div(int rOperand1, int rOperand2) {
        this.rOperand1 = rOperand1;
        this.rOperand2 = rOperand2;
    }

    @Override
    public String toString() {
        return String.format("div $%d $%d\n", rOperand1, rOperand2);
    }
}
