package Middle.type;

import java.util.HashSet;

public class Branch extends BlockNode {
    public enum BranchType{
        BEQZ,
        BNEZ,
    }

    private Operand cond;
    // private final BranchType branchType;
    private final BasicBlock thenBlock;
    private final BasicBlock elseBlock;
    private final boolean thenFirst;

    public Branch(Operand cond,  BasicBlock thenBlock, BasicBlock elseBlock, boolean thenFirst) {
        this.cond = cond;
        // this.branchType = branchType;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
        this.thenFirst = thenFirst;
        // 记录被跳转到的次数
        this.thenBlock.addJump();
        this.elseBlock.addJump();
    }

    // public BranchType getBranchType() {
    //     return branchType;
    // }

    public boolean isThenFirst() {
        return this.thenFirst;
    }

    public Operand getCond() {
        return cond;
    }

    public void setCond(Operand cond) {
        this.cond = cond;
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
