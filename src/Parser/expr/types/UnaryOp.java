package Parser.expr.types;

import Config.IO;
import Lexer.Token;
import Parser.Output;

public class UnaryOp implements Output {
    private final Token token;

    public UnaryOp(Token token) {
        this.token = token;
    }

    @Override
    public void output() {
        IO.print(token.toString());
        IO.print("<UnaryOp>");
    }
}
