package Parser.stmt.types;

import Lexer.Token;
import Parser.expr.types.Exp;

import java.util.ArrayList;

public class PrintfStmt extends Stmt {
    private final Token formatString;
    private final ArrayList<Exp> exps = new ArrayList<>();

    public PrintfStmt(Token token) {
        this.formatString = token;
    }

    public void addExp(Exp exp) {
        this.exps.add(exp);
    }
}
