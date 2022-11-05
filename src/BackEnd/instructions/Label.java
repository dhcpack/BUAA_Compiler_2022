package BackEnd.instructions;

public class Label implements Instruction {
    private final InstructionType type = InstructionType.Label;

    private final String label;

    public Label(String label) {
        this.label = label;
    }

    @Override
    public double getCost() {
        return this.type.getCost();
    }

    @Override
    public String toString() {
        return label + ":";
    }
}
