package Parser.stmt.types;

import Parser.expr.types.Exp;
import Parser.expr.types.LVal;

public class AssignStmt extends Stmt {
    private final LVal lVal;
    private final Exp exp;

    public AssignStmt(LVal lVal, Exp exp) {
        this.lVal = lVal;
        this.exp = exp;
    }
}
