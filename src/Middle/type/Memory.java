package Middle.type;

import Frontend.Parser.expr.types.LeafNode;
import Frontend.Symbol.Symbol;

public class Memory extends BlockNode {
    private Symbol array;
    private Operand offset;
    private Symbol res;

    public Memory(Symbol array, Operand offset, Symbol res) {
        this.array = array;
        this.offset = offset;
        this.res = res;
    }

    public Symbol getArray() {
        return array;
    }

    public Operand getOffset() {
        return offset;
    }

    public Symbol getRes() {
        return res;
    }

    @Override
    public String toString() {
        return "OFFSET " + array.getAddress() + "+" + offset + "->" + res;
    }
}
