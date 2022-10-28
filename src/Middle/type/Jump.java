package Middle.type;

public class Jump extends BlockNode {
    private final BasicBlock target;

    public Jump(BasicBlock target) {
        this.target = target;
    }

    public BasicBlock getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "JUMP " + target.getLabel();
    }
}
