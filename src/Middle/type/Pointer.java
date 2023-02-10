package Middle.type;

import Frontend.Symbol.Symbol;

public class Pointer extends BlockNode {
    public enum Op {
        LOAD,
        STORE,
    }

    private Symbol load;
    private Operand store;
    private final Op op;
    private Symbol base;
    private Operand offset;

    public Pointer(Op op, Symbol base, Operand offset, Symbol load) {
        this.op = op;
        this.base = base;
        this.load = load;
        this.offset = offset;
    }

    public Pointer(Op op, Symbol base, Operand offset, Operand store) {
        this.op = op;
        this.base = base;
        this.store = store;
        this.offset = offset;
    }

    public Symbol getLoad() {
        return load;
    }

    public Operand getStore() {
        return store;
    }

    public Op getOp() {
        return op;
    }

    public Symbol getBase() {
        return base;
    }

    public void setStore(Operand store) {
        this.store = store;
    }

    public void setBase(Symbol base) {
        this.base = base;
    }

    public Operand getOffset() {
        return offset;
    }

    public void setOffset(Operand offset) {
        this.offset = offset;
    }

    public void setLoad(Symbol load) {
        this.load = load;
    }

    public boolean isLoad(){
        return this.load != null;
    }

    @Override
    public String toString() {
        if (op == Op.LOAD) {
            return "LOAD " + load +", "+ offset +"[" + base+"]";
        } else {
            return "STORE " + store +", "+ offset +"[" + base+"]";
        }
    }
}
