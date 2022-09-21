package Parser.stmt.types;

import Parser.expr.types.Cond;


public class WhileStmt extends Stmt {
    private final Cond cond;
    private final Stmt stmt;

    public WhileStmt(Cond cond, Stmt stmt) {
        this.cond = cond;
        this.stmt = stmt;
    }
}
