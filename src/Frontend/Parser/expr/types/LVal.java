package Frontend.Parser.expr.types;

import Frontend.Lexer.Token;
import Frontend.Symbol.Symbol;
import Frontend.Symbol.SymbolType;

import java.util.ArrayList;

public class LVal implements PrimaryExpInterface, LeafNode {
    // LVal → Ident {'[' Exp ']'}
    private final Token ident;
    private final ArrayList<Exp> exps;
    private final ArrayList<Token> bracks;
    private Symbol symbol;

    public LVal(Token token, ArrayList<Exp> exps, ArrayList<Token> bracks) {
        this.ident = token;
        this.exps = exps;
        this.bracks = bracks;
    }

    public boolean missRBrack() {
        for (Token token : bracks) {
            if (token == null) return true;
        }
        return false;
    }

    public Token getIdent() {
        return ident;
    }

    @Override
    public int getLine() {
        return this.ident.getLine();
    }

    public Symbol getSymbol() {
        assert symbol != null;
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    public ArrayList<Exp> getExps() {
        return exps;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ident);
        int index = 0;
        for (Exp exp : exps) {
            stringBuilder.append(bracks.get(index++)).append(exp).append(bracks.get(index++));
        }
        stringBuilder.append("<LVal>\n");
        return stringBuilder.toString();
    }

    // WARNING: 对类型根据使用方法进行了修改
    // int a[10];
    // a[1] ---> int
    // WARNING: 只能在funcExp被使用
    @Override
    public SymbolType getSymbolType() {
        assert symbol != null;  // Symbol已经赋好值了
        if (symbol.getSymbolType() == SymbolType.FUNCTION) {
            return symbol.getReturnType();
        } else if (symbol.getSymbolType() == SymbolType.ARRAY && getDimCount() == 0) {
            return SymbolType.INT;
        } else {
            return symbol.getSymbolType();
        }
    }

    // Warning: for LVal, it is using Dim!!!
    // int a[1][2];
    // when using a[0], getDimCount() -> 1;
    // generally used in funcRPara
    @Override
    public int getDimCount() {
        assert symbol.getSymbolType() == SymbolType.ARRAY;
        return symbol.getDimCount() - bracks.size() / 2;
    }

    // 这个函数返回的是LVal每一维的大小，而不是LVal每一维的index
    @Override
    public ArrayList<Integer> getDimSize() {
        assert symbol != null;
        return symbol.getDimSize();
    }

    public ArrayList<Token> getBracks() {
        return bracks;
    }
}
