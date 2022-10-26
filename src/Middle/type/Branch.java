package Middle.type;

import Frontend.Symbol.Symbol;

public class Branch extends BlockNode{
    private Symbol cond;
    private BasicBlock thenBlock;
    private BasicBlock elseBlock;


    public Branch(Symbol cond, BasicBlock thenBlock, BasicBlock elseBlock) {
        this.cond = cond;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
    }

    public Symbol getCond() {
        return cond;
    }

    public BasicBlock getThenBlock() {
        return thenBlock;
    }

    public BasicBlock getElseBlock() {
        return elseBlock;
    }
}
