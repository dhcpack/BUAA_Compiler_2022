package Parser.expr.types;

import Lexer.Token;

public class Number implements PrimaryExpInterface{
    private final Token number;

    public Number(Token token) {
        this.number = token;
    }
}
