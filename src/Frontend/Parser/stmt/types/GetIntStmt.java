package Frontend.Parser.stmt.types;

import Frontend.Lexer.Token;
import Frontend.Lexer.TokenType;
import Frontend.Parser.TokenHandler;
import Frontend.Parser.expr.types.LVal;

public class GetIntStmt implements StmtInterface {
    // LVal '=' 'getint''('')'
    private final LVal lVal;
    private final Token assign;
    private final Token getint;
    private final Token left;
    private final Token right;  // error check: right could be null

    public GetIntStmt(LVal lVal, Token assign, Token getint, Token left, Token right, TokenHandler tokenHandler) {
        this.lVal = lVal;
        this.assign = assign;
        this.getint = getint;
        this.left = left;
        if (right.getType() != TokenType.RPARENT) {
            this.right = null;
            tokenHandler.retract(1);
        } else {
            this.right = right;
        }
    }

    public LVal getLVal() {
        return lVal;
    }

    public boolean missRightParenthesis() {
        return this.right == null;
    }

    public int getLine() {
        return this.left.getLine();
    }

    @Override
    public int getSemicolonLine() {
        return this.left.getLine();
    }

    @Override
    public String toString() {
        return lVal.toString() + assign + getint + left + right;
    }
}
