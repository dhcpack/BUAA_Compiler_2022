package Parser.stmt.types;

import Config.IO;
import Lexer.Token;
import Parser.expr.types.Exp;

public class ReturnStmt implements StmtInterface {
    private final Token returnToken;
    private final Exp returnExp;

    public ReturnStmt(Token returnToken) {
        this.returnToken = returnToken;
        this.returnExp = null;
    }

    public ReturnStmt(Token returnToken, Exp returnExp) {
        this.returnToken = returnToken;
        this.returnExp = returnExp;
    }

    public boolean isVoid() {
        return returnExp == null;
    }

    @Override
    public void output() {
        IO.print(returnToken.toString());
        if (returnExp != null) {
            returnExp.output();
        }
    }
}
