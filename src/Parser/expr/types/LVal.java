package Parser.expr.types;

import Config.IO;
import Lexer.Token;
import Parser.Output;
import Symbol.Symbol;
import Symbol.SymbolType;

import java.util.ArrayList;

public class LVal implements PrimaryExpInterface, Output, LeafNode {
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

    @Override
    public void output() {
        IO.print(ident.toString());
        int index = 0;
        for (Exp exp : exps) {
            IO.print(bracks.get(index++).toString());
            exp.output();
            IO.print(bracks.get(index++).toString());
        }
        IO.print("<LVal>");
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
        return symbol.getDimsCount() - bracks.size() / 2;
    }

    @Override
    public ArrayList<Integer> getDimNum() {
        assert symbol != null;
        return symbol.getDimNum();
    }
}
