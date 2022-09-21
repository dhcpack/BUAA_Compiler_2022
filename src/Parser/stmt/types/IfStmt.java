package Parser.stmt.types;

import Parser.expr.types.Cond;

import java.util.ArrayList;

public class IfStmt extends Stmt {
    private final Cond cond;
    private ArrayList<Stmt> stmts = new ArrayList<>();

    public IfStmt(Cond cond, Stmt stmt) {
        this.cond = cond;
        stmts.add(stmt);
    }

    public void addBranch(Stmt stmt) {
        stmts.add(stmt);
    }

    public boolean hasElse() {
        return this.stmts.size() == 2;
    }
}
