package Parser.stmt;

import Lexer.Token;
import Lexer.Type;
import Parser.TokenHandler;
import Parser.decl.DeclParser;
import Parser.expr.ExprParser;
import Parser.expr.types.Cond;
import Parser.expr.types.Exp;
import Parser.expr.types.LVal;
import Parser.stmt.types.AssignStmt;
import Parser.stmt.types.BlockItem;
import Parser.stmt.types.BlockStmt;
import Parser.stmt.types.BreakStmt;
import Parser.stmt.types.ContinueStatement;
import Parser.stmt.types.ExpStmt;
import Parser.stmt.types.GetIntStmt;
import Parser.stmt.types.IfStmt;
import Parser.stmt.types.PrintfStmt;
import Parser.stmt.types.ReturnStmt;
import Parser.stmt.types.Stmt;
import Parser.stmt.types.WhileStmt;

import java.util.ArrayList;

public class StmtParser {
    /*
        // statement
        // semicn is stored in Stmt

        语句块 Block → '{' { BlockItem } '}'
        语句块项 BlockItem → Decl | Stmt
        语句 Stmt --> StmtInterface ';'

        StmtInterface --> 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
                        | 'while' '(' Cond ')' Stmt
                        | 'break' ';' | 'continue' ';'
                        | 'return' [Exp] ';'
                        | 'printf''('FormatString{','Exp}')'';'
                        | Block
                        | LVal '=' 'getint''('')'';'
                        | LVal '=' Exp ';'
                        | [Exp] ';'
    * */
    public final TokenHandler tokenHandler;

    public StmtParser(TokenHandler tokenHandler) {
        this.tokenHandler = tokenHandler;
    }

    public Stmt parseStmt() {
        // all ; are saved in stmt
        Token token = tokenHandler.getForwardToken();
        if (token.getType() == Type.IFTK) {
            return new Stmt(parseIfStmt(), null);
        } else if (token.getType() == Type.WHILETK) {
            return new Stmt(parseWhileStmt(), null);
        } else if (token.getType() == Type.BREAKTK) {
            return new Stmt(parseBreakStmt(), tokenHandler.getTokenAndMove());
        } else if (token.getType() == Type.CONTINUETK) {
            return new Stmt(parseContinueStmt(), tokenHandler.getTokenAndMove());
        } else if (token.getType() == Type.RETURNTK) {
            return new Stmt(parseReturnStmt(), tokenHandler.getTokenAndMove());
        } else if (token.getType() == Type.PRINTFTK) {
            return new Stmt(parsePrintfStmt(), tokenHandler.getTokenAndMove());
        } else if (token.getType() == Type.LBRACE) {
            return new Stmt(parseBlockStatement(), null);
        } else if (token.getType() == Type.SEMICN) {
            return new Stmt(null, tokenHandler.getTokenAndMove());  // a single ';'
        } else {
            int step = 0;
            while (true) {
                token = tokenHandler.getTokenAndMove();
                step++;
                if (token.getType() == Type.ASSIGN) {  // encounter '='
                    tokenHandler.retract(step);  // retract to begin place
                    LVal lVal = new ExprParser(tokenHandler).parseLVal();  // point to =
                    Token assign = tokenHandler.getTokenAndMove();
                    token = tokenHandler.getForwardToken();  // check  it
                    if (token.getType() == Type.GETINTTK) {
                        // LVal '=' 'getint''('')'';'
                        GetIntStmt getIntStmt = new GetIntStmt(lVal, assign, tokenHandler.getTokenAndMove(),
                                tokenHandler.getTokenAndMove(), tokenHandler.getTokenAndMove());
                        return new Stmt(getIntStmt, tokenHandler.getTokenAndMove());
                    } else {
                        // LVal '=' Exp ';'
                        Exp exp = new ExprParser(tokenHandler).parseExp();
                        return new Stmt(new AssignStmt(lVal, assign, exp), tokenHandler.getTokenAndMove());
                    }
                } else if (token.getType() == Type.SEMICN) {
                    tokenHandler.retract(step);
                    // Exp ';'
                    ExpStmt expStmt = new ExpStmt(new ExprParser(tokenHandler).parseExp());
                    return new Stmt(expStmt, tokenHandler.getTokenAndMove());
                }
            }
        }
    }

    // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
    public IfStmt parseIfStmt() {
        Token ifToken = tokenHandler.getTokenAndMove();
        Token left = tokenHandler.getTokenAndMove();
        Cond cond = new ExprParser(tokenHandler).parseCond();
        Token right = tokenHandler.getTokenAndMove();
        Stmt stmt = parseStmt();
        Token elseToken = tokenHandler.getForwardToken();
        ArrayList<Token> elses = new ArrayList<>();
        ArrayList<Stmt> stmts = new ArrayList<>();
        stmts.add(stmt);
        if (elseToken.getType() == Type.ELSETK) {
            elses.add(tokenHandler.getTokenAndMove());
            stmts.add(parseStmt());
        }
        return new IfStmt(ifToken, left, right, cond, stmts, elses);
    }

    // 'while' '(' Cond ')' Stmt
    public WhileStmt parseWhileStmt() {
        Token whileToken = tokenHandler.getTokenAndMove();
        Token left = tokenHandler.getTokenAndMove();
        Cond cond = new ExprParser(tokenHandler).parseCond();
        Token right = tokenHandler.getTokenAndMove();
        return new WhileStmt(whileToken, left, cond, right, parseStmt());
    }

    // 'break' ';'
    public BreakStmt parseBreakStmt() {
        return new BreakStmt(tokenHandler.getTokenAndMove());
    }

    // 'continue' ';'
    public ContinueStatement parseContinueStmt() {
        return new ContinueStatement(tokenHandler.getTokenAndMove());
    }

    // 'return' [Exp] ';'
    public ReturnStmt parseReturnStmt() {
        Token returnToken = tokenHandler.getTokenAndMove();
        if (tokenHandler.getForwardToken().getType() == Type.SEMICN) {
            return new ReturnStmt(returnToken);
        } else {
            return new ReturnStmt(returnToken, new ExprParser(tokenHandler).parseExp());
        }
    }

    // 'printf''('FormatString{','Exp}')'';'
    public PrintfStmt parsePrintfStmt() {
        Token printf = tokenHandler.getTokenAndMove();
        Token left = tokenHandler.getTokenAndMove();
        Token formatString = tokenHandler.getTokenAndMove();
        Token token = tokenHandler.getTokenAndMove();  // get , or ). point to exp or ;
        ArrayList<Token> seps = new ArrayList<>();
        ArrayList<Exp> exps = new ArrayList<>();
        while (token.getType() == Type.COMMA) {
            seps.add(token);
            exps.add(new ExprParser(tokenHandler).parseExp());
            token = tokenHandler.getTokenAndMove();  // get , or ). point to exp or ;
        }
        return new PrintfStmt(printf, left, formatString, seps, exps, token);
    }

    // Block → '{' { BlockItem } '}'
    public BlockStmt parseBlockStatement() {
        Token left = tokenHandler.getTokenAndMove();
        Token right = tokenHandler.getForwardToken();
        ArrayList<BlockItem> blockItems = new ArrayList<>();
        while (right.getType() != Type.RBRACE) {
            if (right.getType() == Type.CONSTTK || right.getType() == Type.INTTK) {
                blockItems.add(new DeclParser(tokenHandler).parseDecl());
            } else {
                blockItems.add(parseStmt());
            }
            right = tokenHandler.getForwardToken();
        }
        right = tokenHandler.getTokenAndMove();  // skip }
        return new BlockStmt(left, blockItems, right);
    }
}
