package Middle.type;

import java.util.HashSet;

public class Branch extends BlockNode {
    private final Operand cond;
    private final BasicBlock thenBlock;
    private final BasicBlock elseBlock;
    private final boolean thenFirst;

    public Branch(Operand cond, BasicBlock thenBlock, BasicBlock elseBlock, boolean thenFirst) {
        this.cond = cond;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
        this.thenFirst = thenFirst;
    }

    public boolean isThenFirst() {
        return this.thenFirst;
    }

    public Operand getCond() {
        return cond;
    }

    public BasicBlock getThenBlock() {
        return thenBlock;
    }

    public BasicBlock getElseBlock() {
        return elseBlock;
    }

    public HashSet<BlockNode> getNextBlockNode() {
        HashSet<BlockNode> nextBlockNode = new HashSet<>();
        nextBlockNode.addAll(thenBlock.getFirstBlockNode());
        nextBlockNode.addAll(elseBlock.getFirstBlockNode());
        return nextBlockNode;
    }

    @Override
    public String toString() {
        return "Branch " + cond + " ? " + thenBlock + " : " + elseBlock;
    }
}
