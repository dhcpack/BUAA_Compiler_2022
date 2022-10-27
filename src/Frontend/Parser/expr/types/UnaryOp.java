package Frontend.Parser.expr.types;

import Config.Reader;
import Config.SyntaxWriter;
import Frontend.Lexer.Token;
import Config.Output;

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
        SyntaxWriter.print(token.toString());
        SyntaxWriter.print("<UnaryOp>");
    }
}
