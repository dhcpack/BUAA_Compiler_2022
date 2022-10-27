package Middle.type;

import Frontend.Parser.expr.types.LeafNode;
import Frontend.Symbol.SymbolType;

import java.util.ArrayList;

public class Immediate implements Operand {
    private int num;

    public Immediate(int num) {
        this.num = num;
    }

    public int getNumber() {
        return num;
    }

    @Override
    public String toString() {
        return String.valueOf(this.num);
    }
}
