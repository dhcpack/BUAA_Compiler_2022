package Parser.stmt.types;

import Parser.expr.types.Exp;

public class ReturnStmt extends Stmt {
    private final Exp returnExp;

    public ReturnStmt() {
        this.returnExp = null;
    }

    public ReturnStmt(Exp returnExp) {
        this.returnExp = returnExp;
    }

    public boolean isVoid() {
        return returnExp == null;
    }
}
