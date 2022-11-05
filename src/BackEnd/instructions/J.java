package BackEnd.instructions;

public class J implements Instruction{
    private final InstructionType type = InstructionType.JUMP;

    private final String label;

    public J(String label) {
        this.label = label;
    }

    @Override
    public double getCost() {
        return this.type.getCost();
    }

    @Override
    public String toString() {
        return "j " + label + "\n";
    }
}
