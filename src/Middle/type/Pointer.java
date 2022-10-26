package Middle.type;

import Frontend.Parser.expr.types.LeafNode;
import Frontend.Symbol.Symbol;

public class Pointer extends BlockNode {
    public enum Op {
        LOAD,
        STORE,
    }

    private Symbol load;
    private LeafNode store;
    private Op op;
    private Symbol pointer;

    public Pointer(Op op, Symbol pointer, Symbol load) {
        this.op = op;
        this.pointer = pointer;
        this.load = load;
    }

    public Pointer(Op op, Symbol pointer, LeafNode store) {
        this.op = op;
        this.pointer = pointer;
        this.store = store;
    }
}
