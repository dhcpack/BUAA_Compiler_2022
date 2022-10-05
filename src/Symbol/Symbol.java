package Symbol;

import Lexer.Token;

import java.util.ArrayList;

public class Symbol {
    private final SymbolType symbolType;
    private final ArrayList<Integer> dimNum;
    private final ArrayList<Token> bracks;  // 数组维数（定义时的Const Exp，未经过化简计算）
    private final Token ident;
    private final boolean isConst;

    private final boolean isFunc;
    private final ArrayList<Symbol> params;
    private final SymbolType returnType;

    public Symbol(SymbolType symbolType, ArrayList<Token> bracks, ArrayList<Integer> dimNum, Token ident,
                  Boolean isConst) {  // int or array
        this.symbolType = symbolType;
        this.bracks = bracks;
        this.dimNum = dimNum;
        this.ident = ident;
        this.isConst = isConst;
        this.params = null;
        this.isFunc = false;
        this.returnType = null;
    }

    public Symbol(SymbolType symbolType, SymbolType returnType, ArrayList<Symbol> params, Token ident) {  // function
        this.symbolType = symbolType;
        this.returnType = returnType;
        this.bracks = null;
        this.dimNum = null;
        this.ident = ident;
        this.isConst = false;
        this.isFunc = true;
        this.params = params;
    }

    public int getDimsCount() {
        assert this.bracks != null;
        return this.bracks.size() / 2;  // 用brack计算更好
    }

    public ArrayList<Symbol> getParams() {
        return params;
    }

    public ArrayList<Integer> getDimNum() {
        return dimNum;
    }

    public Token getIdent() {
        return ident;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public boolean isConst() {
        return isConst;
    }

    public boolean isFunc() {
        return isFunc;
    }

    public SymbolType getReturnType() {
        return returnType;
    }
}
