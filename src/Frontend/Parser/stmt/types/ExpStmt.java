package Frontend.Parser.stmt.types;

import Frontend.Parser.expr.types.Exp;

public class ExpStmt implements StmtInterface {
    // Exp ';'
    private final Exp exp;

    public ExpStmt(Exp exp) {
        this.exp = exp;
    }

    public Exp getExp() {
        return exp;
    }

    @Override
    public String toString() {
        return this.exp.toString();
    }

    @Override
    public int getSemicolonLine() {
        return exp.getLine();
    }
}
