package BackEnd.instructions;

public class Jal implements Instruction{
    private final InstructionType type = InstructionType.JUMP;

    private final String label;

    public Jal(String label) {
        this.label = label;
    }

    @Override
    public double getCost() {
        return this.type.getCost();
    }

    @Override
    public String toString() {
        return "jal " + label + "\n";
    }
}
