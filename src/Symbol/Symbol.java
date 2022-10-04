package Symbol;

import Lexer.Token;
import Parser.expr.types.ConstExp;

import java.util.ArrayList;

public class Symbol {
    private final SymbolType symbolType;
    private final ArrayList<ConstExp> dims;  // 数组维数（定义时的Const Exp，未经过化简计算）
    private final Token ident;
    private final boolean isConst;

    private final boolean isFunc;
    private final ArrayList<Symbol> params;

    // FuncFParam只用到前两个
    public Symbol(SymbolType symbolType, ArrayList<ConstExp> dims, Token ident, Boolean isConst) {  // int or array
        this.symbolType = symbolType;
        this.dims = dims;
        this.ident = ident;
        this.isConst = isConst;
        this.params = null;
        this.isFunc = false;
    }

    public Symbol(SymbolType symbolType, ArrayList<Symbol> params, Token ident) {  // function
        this.symbolType = symbolType;
        this.dims = null;
        this.ident = ident;
        this.isConst = false;
        this.isFunc = true;
        this.params = params;
    }

    public int getDimsCount() {
        return this.dims.size();
    }

    public ArrayList<Symbol> getParams() {
        return params;
    }

    public ArrayList<ConstExp> getDims() {
        return dims;
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
}
