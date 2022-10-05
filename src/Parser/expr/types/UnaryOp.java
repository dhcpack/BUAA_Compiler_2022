package Parser.expr.types;

import Config.IO;
import Lexer.Token;
import Parser.Output;

public class UnaryOp implements Output {
    // UnaryOp → '+' | '−' | '!'
    private final Token token;

    public UnaryOp(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    @Override
    public void output() {
        IO.print(token.toString());
        IO.print("<UnaryOp>");
    }
}
