package Parser.expr.types;

import Config.IO;
import Lexer.Token;

public class FuncExp implements UnaryExpInterface {
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
        return this.params.getLine();
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
}
