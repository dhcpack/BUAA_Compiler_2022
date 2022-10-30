package Middle.type;

import Frontend.Symbol.Symbol;

public class Branch extends BlockNode {
    private Operand cond;
    private BasicBlock thenBlock;
    private BasicBlock elseBlock;
    private boolean thenFirst;

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

    @Override
    public String toString() {
        return "Branch " + cond + " ? " + thenBlock + " : " + elseBlock;
    }
}
