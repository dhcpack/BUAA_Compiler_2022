package Parser.expr.types;

import Config.IO;
import Lexer.Token;

public class BraceExp implements PrimaryExpInterface{
    private final Token left;
    private final Token right;
    private final Exp exp;

    public BraceExp(Token left, Exp exp, Token right) {
        this.left = left;
        this.exp = exp;
        this.right = right;
    }


    @Override
    public void output() {
        IO.print(left.toString());
        exp.output();
        IO.print(right.toString());
    }
}
