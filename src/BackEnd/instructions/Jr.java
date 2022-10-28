package BackEnd.instructions;

public class Jr implements Instruction{
    private final InstructionType type = InstructionType.JUMP;

    @Override
    public String toString() {
        return "jr $ra\n";
    }
}
