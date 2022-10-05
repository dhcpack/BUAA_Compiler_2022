package Parser.stmt.types;

import Config.IO;
import Lexer.Token;
import Lexer.TokenType;
import Parser.TokenHandler;
import Parser.expr.types.LVal;

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
    public void output() {
        lVal.output();
        IO.print(assign.toString());
        IO.print(getint.toString());
        IO.print(left.toString());
        IO.print(right.toString());
    }
}
