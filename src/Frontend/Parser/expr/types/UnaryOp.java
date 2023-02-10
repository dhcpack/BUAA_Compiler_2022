package Frontend.Parser.expr.types;

import Frontend.Lexer.Token;

public class UnaryOp {
    // UnaryOp → '+' | '−' | '!'
    private final Token token;

    public UnaryOp(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    @Override
    public String toString() {
        return token + "<UnaryOp>\n";
    }
}
