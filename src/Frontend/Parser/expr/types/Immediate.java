package Frontend.Parser.expr.types;

import Frontend.Symbol.SymbolType;

import java.util.ArrayList;

public class Immediate implements LeafNode{
    private int num;

    public Immediate(int num) {
        this.num = num;
    }

    public int getNumber() {
        return num;
    }

    @Override
    public SymbolType getSymbolType() {
        return SymbolType.INT;
    }

    @Override
    public int getDimCount() {
        return 0;
    }

    @Override
    public ArrayList<Integer> getDimSize() {
        return null;
    }
}
