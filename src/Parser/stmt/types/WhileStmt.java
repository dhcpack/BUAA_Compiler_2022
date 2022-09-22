package Parser.stmt.types;

import Config.IO;
import Lexer.Token;
import Parser.expr.types.Cond;


public class WhileStmt implements StmtInterface {
    private final Token whileToken;
    private final Token left;
    private final Token right;
    private final Cond cond;
    private final Stmt stmt;

    public WhileStmt(Token whileToken, Token left, Cond cond, Token right, Stmt stmt) {
        this.whileToken = whileToken;
        this.left = left;
        this.cond = cond;
        this.right = right;
        this.stmt = stmt;
    }

    @Override
    public void output() {
        IO.print(whileToken.toString());
        IO.print(left.toString());
        cond.output();
        IO.print(right.toString());
        stmt.output();
    }
}
