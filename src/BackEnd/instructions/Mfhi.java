package BackEnd.instructions;

public class Mfhi implements Instruction{
    private final InstructionType type = InstructionType.OTHER;

    private final int rRes;

    public Mfhi(int rRes) {
        this.rRes = rRes;
    }

    @Override
    public String toString() {
        return "mfhi " + rRes + "\n";
    }
}
