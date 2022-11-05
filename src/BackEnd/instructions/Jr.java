package BackEnd.instructions;

public class Jr implements Instruction{
    private final InstructionType type = InstructionType.JUMP;

    @Override
    public double getCost() {
        return this.type.getCost();
    }

    @Override
    public String toString() {
        return "jr $ra\n";
    }
}
