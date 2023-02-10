package Frontend.Parser.stmt.types;

import Frontend.Lexer.Token;
import Frontend.Parser.expr.types.Exp;
import Frontend.Parser.expr.types.LVal;

public class AssignStmt implements StmtInterface {
    // LVal '=' Exp ;
    private final LVal lVal;
    private final Token assign;
    private final Exp exp;

    public AssignStmt(LVal lVal, Token assign, Exp exp) {
        this.lVal = lVal;
        this.assign = assign;
        this.exp = exp;
    }

    public LVal getLVal() {
        return lVal;
    }

    public Exp getExp() {
        return exp;
    }

    @Override
    public String toString() {
        return lVal + assign.toString() + exp;
    }

    @Override
    public int getSemicolonLine() {
        return this.exp.getLine();
    }
}
