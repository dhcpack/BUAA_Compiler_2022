package Middle.type;

import BackEnd.instructions.BranchInstr;
import Frontend.Symbol.Symbol;

import java.util.HashSet;

public class Branch extends BlockNode {
    private Operand cond;
    private BranchInstr.BranchType branchType;
    private final BasicBlock thenBlock;
    private final BasicBlock elseBlock;
    private final boolean thenFirst;

    private final boolean isCalcBranch;
    private Symbol leftSymbol;
    private Operand rightOperand;

    public Branch(BranchInstr.BranchType branchType, Symbol leftSymbol, Operand rightOperand, BasicBlock thenBlock,
                  BasicBlock elseBlock, boolean thenFirst) {
        this.branchType = branchType;
        this.leftSymbol = leftSymbol;
        this.rightOperand = rightOperand;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
        this.thenFirst = thenFirst;
        // 记录被跳转到的次数
        this.thenBlock.addJump();
        this.elseBlock.addJump();
        this.isCalcBranch = true;
    }

    public Branch(Operand cond, BasicBlock thenBlock, BasicBlock elseBlock, boolean thenFirst) {
        this.cond = cond;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
        this.thenFirst = thenFirst;
        // 记录被跳转到的次数
        this.thenBlock.addJump();
        this.elseBlock.addJump();
        this.isCalcBranch = false;
    }

    // for calc branch
    public BranchInstr.BranchType getBranchType() {
        return branchType;
    }

    public boolean isCalcBranch() {
        return isCalcBranch;
    }

    public Symbol getLeftSymbol() {
        assert isCalcBranch;
        return leftSymbol;
    }

    public Operand getRightOperand() {
        assert isCalcBranch;
        return rightOperand;
    }

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
        if (isCalcBranch) {
            return String.format("BRANCH(%s) %s %s ? %s : %s", branchType, leftSymbol, rightOperand, thenBlock, elseBlock);
        } else {
            return "Branch " + cond + " ? " + thenBlock + " : " + elseBlock;
        }
    }
}
