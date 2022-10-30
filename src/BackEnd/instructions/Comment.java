package BackEnd.instructions;

public class Comment implements Instruction {
    private final InstructionType type = InstructionType.COMMENT;

    private final String comment;

    public Comment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        if (comment.equals("")) {
            return "\n";
        }
        return "# " + comment + "\n";
    }
}
