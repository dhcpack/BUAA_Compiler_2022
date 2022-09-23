package Parser.stmt.types;

import Config.IO;
import Lexer.Token;
import Parser.expr.types.LVal;

public class GetIntStmt implements StmtInterface {
    // LVal '=' 'getint''('')'
    private final LVal lVal;
    private final Token assign;
    private final Token getint;
    private final Token left;
    private final Token right;

    public GetIntStmt(LVal lVal, Token assign, Token getint, Token left, Token right) {
        this.lVal = lVal;
        this.assign = assign;
        this.getint = getint;
        this.left = left;
        this.right = right;
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
