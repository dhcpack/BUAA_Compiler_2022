package Middle.type;

import Frontend.Symbol.Symbol;

public class Memory extends BlockNode {
    private final Symbol base;
    private final Operand offset;
    private final Symbol res;

    public Memory(Symbol base, Operand offset, Symbol res) {
        this.base = base;
        this.offset = offset;
        this.res = res;
    }

    public Symbol getBase() {
        return base;
    }

    public Operand getOffset() {
        return offset;
    }

    public Symbol getRes() {
        return res;
    }

    @Override
    public String toString() {
        return "OFFSET (" + base + "+" + offset + ")->" + res;
    }
}
