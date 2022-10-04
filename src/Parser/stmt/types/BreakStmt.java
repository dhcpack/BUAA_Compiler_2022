package Parser.stmt.types;

import Config.IO;
import Lexer.Token;

public class BreakStmt implements StmtInterface {
    private final Token breakToken;

    public BreakStmt(Token breakToken) {
        this.breakToken = breakToken;
    }

    @Override
    public void output() {
        IO.print(breakToken.toString());
    }

    @Override
    public int getSemicolonLine() {
        return breakToken.getLine();
    }


}
