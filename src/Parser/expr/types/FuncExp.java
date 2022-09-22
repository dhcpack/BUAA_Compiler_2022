package Parser.expr.types;

import Config.IO;
import Lexer.Token;

public class FuncExp implements UnaryExpInterface {
    // 函数调用形式是 Ident ‘(’ FuncRParams ‘)’，
    // 函数实参表 FuncRParams → Exp { ',' Exp }
    private final Token ident;
    private final Token left;
    private final Token right;
    private final FuncRParams params;

    public FuncExp(Token token, Token left, Token right, FuncRParams params) {
        this.ident = token;
        this.left = left;
        this.right = right;
        this.params = params;
    }


    @Override
    public void output() {
        IO.print(ident.toString());
        IO.print(left.toString());
        if (params != null) {
            params.output();
        }
        IO.print(right.toString());
    }
}
