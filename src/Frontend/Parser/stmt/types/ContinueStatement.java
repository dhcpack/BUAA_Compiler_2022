package Frontend.Parser.stmt.types;

import Config.Reader;
import Config.SyntaxWriter;
import Frontend.Lexer.Token;

public class ContinueStatement implements StmtInterface {
    private final Token continueToken;

    public ContinueStatement(Token continueToken) {
        this.continueToken = continueToken;
    }

    @Override
    public void output() {
        SyntaxWriter.print(continueToken.toString());
    }

    @Override
    public int getSemicolonLine() {
        return continueToken.getLine();
    }
}
