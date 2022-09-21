package Parser.expr.types;

import Lexer.Token;

public class UnaryOp {
    private final String tag = "<UnaryOp>";
    private final Token token;

    public UnaryOp(Token token) {
        this.token = token;
    }
}
