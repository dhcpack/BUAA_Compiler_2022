package Frontend.Parser.stmt.types;

import Config.IO;
import Frontend.Lexer.Token;
import Frontend.Lexer.TokenType;
import Frontend.Parser.TokenHandler;

public class Stmt implements BlockItem {
    // Stmt --> StmtInterface ';'
    private final StmtInterface stmt;
    private final Token semicolon;
    private final boolean missSemicolon;

    public Stmt(StmtInterface stmt) {
        this.stmt = stmt;
        this.semicolon = null;
        this.missSemicolon = false;
    }

    public Stmt(StmtInterface stmt, TokenHandler tokenHandler) {  // pass tokenHandler and check if miss semicn
        this.stmt = stmt;
        if (tokenHandler.getForwardToken().getType() != TokenType.SEMICN) {
            this.semicolon = null;
            this.missSemicolon = true;
        } else {
            this.semicolon = tokenHandler.getTokenAndMove();
            this.missSemicolon = false;
        }
    }

    public StmtInterface getStmt() {
        return this.stmt;
    }

    public boolean missSemicolon() {
        return this.missSemicolon;
    }

    // using when miss semicolon
    public int getLastSymbolLine() {
        return stmt.getSemicolonLine();
    }

    @Override
    public void output() {
        if (stmt != null) {  // if stmt not null then print stmt
            stmt.output();
        }
        if (semicolon != null) {  // if has semicn then print semicn
            IO.print(semicolon.toString());
        }
        IO.print("<Stmt>");
    }
}
