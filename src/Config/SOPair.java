package Config;

import Frontend.Symbol.Symbol;
import Middle.type.Operand;

// Symbol--Operand Pair
public class SOPair {
    private final Symbol base;
    private final Operand offset;

    public SOPair(Symbol base, Operand offset) {
        this.base = base;
        this.offset = offset;
    }

    public SOPair(Operand operand){
        this.base = null;
        this.offset = operand;
    }

    public Symbol getBase() {
        return base;
    }

    public Operand getOffset() {
        return offset;
    }

    public boolean isArray(){
        return this.base != null;
    }

    public Operand getOperand(){
        return this.offset;
    }
}
