package Parser.stmt.types;

import Parser.expr.types.Exp;

public class ExpStmt extends Stmt {
    private Exp exp = null;

    public ExpStmt() {
    }

    public ExpStmt(Exp exp) {
        this.exp = exp;
    }

    public boolean isEmpty() {
        return exp == null;
    }
}
