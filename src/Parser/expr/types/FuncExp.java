package Parser.expr.types;

import Config.IO;
import Lexer.Token;
import Symbol.SymbolType;

public class FuncExp implements UnaryExpInterface, LeafNode {
    // 函数调用 FuncExp --> Ident '(' [FuncRParams] ')'
    // 函数实参表 FuncRParams → Exp { ',' Exp }
    private final Token ident;
    private final Token left;
    private final Token right;    // error check: right could be null
    private final FuncRParams params;

    public FuncExp(Token token, Token left, Token right, FuncRParams params) {
        this.ident = token;
        this.left = left;
        this.right = right;
        this.params = params;
    }

    public boolean missRightParenthesis() {
        return this.right == null;
    }

    public int getLine() {
        if (params.getExps().size() != 0) {
            return this.params.getLine();
        } else {
            return this.left.getLine();
        }
    }

    public Token getIdent() {
        return ident;
    }

    public FuncRParams getParams() {
        return params;
    }

    @Override
    public void output() {
        IO.print(ident.toString());
        IO.print(left.toString());
        if (params != null) {  // params could be null
            params.output();
        }
        IO.print(right.toString());
    }

    @Override
    public SymbolType getSymbolType() {
        // function 一定不是 void， 返回类型一定要是int
        return SymbolType.INT;
    }

    @Override
    public int getDims() {
        return 0;
    }
}
