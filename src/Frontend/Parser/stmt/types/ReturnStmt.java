package Frontend.Parser.stmt.types;

import Config.IO;
import Frontend.Lexer.Token;
import Frontend.Parser.expr.types.Exp;

public class ReturnStmt implements StmtInterface {
    // 'return' [Exp]
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

    public Exp getReturnExp() {
        return returnExp;
    }

    public boolean returnInt() {
        return returnExp != null;
    }

    public Token getReturnToken() {
        return returnToken;
    }

    @Override
    public void output() {
        IO.print(returnToken.toString());
        if (returnExp != null) {  // if has exp then print exp
            returnExp.output();
        }
    }

    @Override
    public int getSemicolonLine() {
        return returnToken.getLine();
    }
}
