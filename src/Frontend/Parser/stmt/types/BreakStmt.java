package Frontend.Parser.stmt.types;

import Config.Reader;
import Config.SyntaxWriter;
import Frontend.Lexer.Token;

public class BreakStmt implements StmtInterface {
    private final Token breakToken;

    public BreakStmt(Token breakToken) {
        this.breakToken = breakToken;
    }

    @Override
    public void output() {
        SyntaxWriter.print(breakToken.toString());
    }

    @Override
    public int getSemicolonLine() {
        return breakToken.getLine();
    }


}
