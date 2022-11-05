package BackEnd.instructions;

public class MoveInstr implements Instruction {
    private final InstructionType type = InstructionType.ALU;
    private final int source;
    private final int target;

    public MoveInstr(int source, int target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public double getCost() {
        return this.type.getCost();
    }

    @Override
    public String toString() {
        return String.format("move $%d, $%d\n", target, source);
    }
}
