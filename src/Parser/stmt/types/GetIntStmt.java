package Parser.stmt.types;

import Parser.expr.types.LVal;

public class GetIntStmt extends Stmt {
    private final LVal lVal;

    public GetIntStmt(LVal lVal) {
        this.lVal = lVal;
    }

}
