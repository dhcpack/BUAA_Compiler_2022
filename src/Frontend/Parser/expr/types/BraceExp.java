package Frontend.Parser.expr.types;

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

    public Token getLeft() {
        return left;
    }

    public Token getRight() {
        return right;
    }

    public Exp getExp() {
        return exp;
    }

    public boolean missRightParenthesis() {
        return this.right == null;
    }

    @Override
    public String toString() {
        return left.toString() + exp.toString() + right.toString();
    }
}
