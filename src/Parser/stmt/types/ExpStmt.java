package Parser.stmt.types;

import Parser.expr.types.Exp;

public class ExpStmt implements StmtInterface {
    private final Exp exp;

    public ExpStmt(Exp exp) {
        this.exp = exp;
    }

    public boolean isEmpty() {
        return exp == null;
    }

    @Override
    public void output() {

    }
}
