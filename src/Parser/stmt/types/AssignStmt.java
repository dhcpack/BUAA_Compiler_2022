package Parser.stmt.types;

import Config.IO;
import Lexer.Token;
import Parser.expr.types.Exp;
import Parser.expr.types.LVal;

public class AssignStmt implements StmtInterface {
    // LVal '=' Exp
    private final LVal lVal;
    private final Token assign;
    private final Exp exp;

    public AssignStmt(LVal lVal, Token assign, Exp exp) {
        this.lVal = lVal;
        this.assign = assign;
        this.exp = exp;
    }

    @Override
    public void output() {
        lVal.output();
        IO.print(assign.toString());
        exp.output();
    }
}
