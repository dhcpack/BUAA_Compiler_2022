package BackEnd.instructions;

public class Mflo implements Instruction{
    private final InstructionType type = InstructionType.OTHER;

    private final int rRes;

    public Mflo(int rRes) {
        this.rRes = rRes;
    }

    @Override
    public String toString() {
        return "mflo " + rRes + "\n";
    }
}
