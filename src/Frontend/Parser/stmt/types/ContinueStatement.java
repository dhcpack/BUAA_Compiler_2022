package Frontend.Parser.stmt.types;

import Frontend.Lexer.Token;

public class ContinueStatement implements StmtInterface {
    private final Token continueToken;

    public Token getContinueToken() {
        return continueToken;
    }

    public ContinueStatement(Token continueToken) {
        this.continueToken = continueToken;
    }

    @Override
    public String toString() {
        return this.continueToken.toString();
    }

    @Override
    public int getSemicolonLine() {
        return continueToken.getLine();
    }
}
