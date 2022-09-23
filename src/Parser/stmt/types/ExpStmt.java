package Parser.stmt.types;

import Parser.expr.types.Exp;

public class ExpStmt implements StmtInterface {
    // Exp ';'
    private final Exp exp;

    public ExpStmt(Exp exp) {
        this.exp = exp;
    }

    @Override
    public void output() {
        exp.output();
    }
}
