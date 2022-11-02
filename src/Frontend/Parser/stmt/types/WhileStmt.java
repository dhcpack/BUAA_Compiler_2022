package Frontend.Parser.stmt.types;

import Frontend.Lexer.Token;
import Frontend.Parser.expr.types.Cond;

public class WhileStmt implements StmtInterface {
    // 'while' '(' Cond ')' Stmt
    private final Token whileToken;
    private final Token left;
    private final Token right;  // error check: right could be null
    private final Cond cond;
    private final Stmt stmt;

    public WhileStmt(Token whileToken, Token left, Cond cond, Token right, Stmt stmt) {
        this.whileToken = whileToken;
        this.left = left;
        this.cond = cond;
        this.right = right;
        this.stmt = stmt;
    }

    public int getLine() {
        return cond.getLine();
    }

    public Stmt getStmt() {
        return stmt;
    }

    public Cond getCond() {
        return cond;
    }

    public boolean missRightParenthesis() {
        return this.right == null;
    }

    @Override
    public String toString() {
        return whileToken.toString() + left + cond + right + stmt;
    }

    @Override
    public int getSemicolonLine() {
        return -20231164;
    }
}
