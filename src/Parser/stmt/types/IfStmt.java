package Parser.stmt.types;

import Config.IO;
import Lexer.Token;
import Parser.Output;
import Parser.expr.types.Cond;

import java.util.ArrayList;

public class IfStmt implements StmtInterface, Output {
    private final Token ifToken;
    private final Token left;
    private final Token right;
    private final Cond cond;
    private final ArrayList<Stmt> stmts;
    private final ArrayList<Token> elses;

    public IfStmt(Token ifToken, Token left, Token right, Cond cond, ArrayList<Stmt> stmts, ArrayList<Token> elses) {
        this.ifToken = ifToken;
        this.left = left;
        this.right = right;
        this.cond = cond;
        this.stmts = stmts;
        this.elses = elses;
    }

    public boolean hasElse() {
        return this.stmts.size() == 2;
    }

    @Override
    public void output() {
        IO.print(ifToken.toString());
        IO.print(left.toString());
        cond.output();
        IO.print(right.toString());
        stmts.get(0).output();
        int index = 1;
        for (Token els : elses) {
            IO.print(els.toString());
            stmts.get(index++).output();
        }
    }
}
