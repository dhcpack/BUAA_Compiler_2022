package BackEnd.instructions;

public enum InstructionType {
    DIV(50),
    MULT(4),
    ALU(1),
    JUMP(1.2),
    BRANCH(1.2),
    MEM(2),
    OTHER(1),
    COMMENT(0),
    Label(0);

    private final double cost;

    InstructionType(double cost) {
        this.cost = cost;
    }

    public double getCost() {
        return cost;
    }
}
