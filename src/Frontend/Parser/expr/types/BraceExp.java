package Frontend.Parser.expr.types;

import Config.Reader;
import Config.SyntaxWriter;
import Frontend.Lexer.Token;

public class BraceExp implements PrimaryExpInterface {
    // BraceExp -> '(' Exp ')'
    private final Token left;
    private final Token right; // error check: right could be null
    private final Exp exp;

    public BraceExp(Token left, Exp exp, Token right) {
        this.left = left;
        this.exp = exp;
        this.right = right;
    }

    public int getLine() {
        return this.exp.getLine();
        // return this.left.getLine();
    }

    public Exp getExp() {
        return exp;
    }

    public boolean missRightParenthesis() {
        return this.right == null;
    }

    @Override
    public void output() {
        SyntaxWriter.print(left.toString());
        exp.output();
        SyntaxWriter.print(right.toString());
    }
}
