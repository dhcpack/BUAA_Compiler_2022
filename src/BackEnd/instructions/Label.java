package BackEnd.instructions;

public class Label {
    private final InstructionType type = InstructionType.Label;

    private final String label;

    public Label(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label + ":";
    }
}
