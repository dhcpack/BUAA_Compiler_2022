package Middle.type;

public class Jump extends BlockNode{
    public BasicBlock target;

    public Jump(BasicBlock target) {
        this.target = target;
    }

    public BasicBlock getTarget() {
        return target;
    }
}
