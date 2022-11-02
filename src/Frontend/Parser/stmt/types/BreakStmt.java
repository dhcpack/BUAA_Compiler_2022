package Frontend.Parser.stmt.types;

import Frontend.Lexer.Token;

public class BreakStmt implements StmtInterface {
    private final Token breakToken;

    public Token getBreakToken() {
        return breakToken;
    }

    public BreakStmt(Token breakToken) {
        this.breakToken = breakToken;
    }

    @Override
    public String toString() {
        return breakToken.toString();
    }

    @Override
    public int getSemicolonLine() {
        return breakToken.getLine();
    }
}
