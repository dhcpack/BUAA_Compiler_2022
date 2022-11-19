package Middle.type;

import Frontend.Symbol.Symbol;

public class Pointer implements BlockNode {
    public enum Op {
        LOAD,
        STORE,
    }

    private Symbol load;
    private Operand store;
    private final Op op;
    private final Symbol pointer;

    public Pointer(Op op, Symbol pointer, Symbol load) {
        this.op = op;
        this.pointer = pointer;
        this.load = load;
    }

    public Pointer(Op op, Symbol pointer, Operand store) {
        this.op = op;
        this.pointer = pointer;
        this.store = store;
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

    public Symbol getPointer() {
        return pointer;
    }

    @Override
    public String toString() {
        if (op == Op.LOAD) {
            return "LOAD " + pointer + ", " + load;
        } else {
            return "STORE " + pointer + ", " + store;
        }
    }
}
