package Frontend.Parser.stmt.types;

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
    public String toString() {
        if(returnExp != null){
            return returnToken.toString() + returnExp;  // if has exp then print exp
        } else {
            return returnToken.toString();
        }
    }

    @Override
    public int getSemicolonLine() {
        return returnToken.getLine();
    }
}
