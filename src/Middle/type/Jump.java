package Middle.type;

import java.util.HashSet;

public class Jump extends BlockNode {
    private final BasicBlock target;

    public Jump(BasicBlock target) {
        this.target = target;
    }

    public BasicBlock getTarget() {
        return target;
    }

    public HashSet<BlockNode> getNextBlockNode() {
        return new HashSet<>(target.getFirstBlockNode());
    }

    @Override
    public String toString() {
        return "JUMP " + target.getLabel();
    }
}
