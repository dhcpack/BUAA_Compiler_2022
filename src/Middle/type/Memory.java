package Middle.type;

import Frontend.Parser.expr.types.LeafNode;
import Frontend.Symbol.Symbol;

public class Memory extends BlockNode{
    private Symbol array;
    private LeafNode offset;
    private Symbol res;

    public Memory(Symbol array, LeafNode offset, Symbol res) {
        this.array = array;
        this.offset = offset;
        this.res = res;
    }

    public Symbol getArray() {
        return array;
    }

    public LeafNode getOffset() {
        return offset;
    }

    public Symbol getRes() {
        return res;
    }
}
