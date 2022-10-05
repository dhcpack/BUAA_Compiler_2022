package Parser.stmt.types;

import Config.IO;
import Lexer.Token;
import Parser.Output;
import Parser.expr.types.Cond;

import java.util.ArrayList;

public class IfStmt implements StmtInterface, Output {
    // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
    private final Token ifToken;
    private final Token left;
    private final Token right;  // error check: right could be null
    private final Cond cond;
    private final ArrayList<Stmt> stmts;  // stmts比elses多一项
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
        return this.elses.size() != 0;
    }

    public boolean missRightParenthesis() {
        return this.right == null;
    }

    public Cond getCond() {
        return cond;
    }

    public ArrayList<Stmt> getStmts() {
        return stmts;
    }

    public ArrayList<Token> getElses() {
        return elses;
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

    @Override
    public int getSemicolonLine() {
        return -20231164;
    }
}
