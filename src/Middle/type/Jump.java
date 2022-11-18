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

    private HashSet<BlockNode> nextBlockNode = null;

    public HashSet<BlockNode> getNextBlockNode() {
        if (nextBlockNode != null) {
            return nextBlockNode;
        }
        nextBlockNode = new HashSet<>();
        nextBlockNode.addAll(target.getFirstBlockNode());
        return nextBlockNode;
    }

    @Override
    public String toString() {
        return "JUMP " + target.getLabel();
    }
}
