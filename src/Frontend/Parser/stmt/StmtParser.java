package Frontend.Parser.stmt;

import Frontend.Lexer.Token;
import Frontend.Lexer.TokenType;
import Frontend.Parser.TokenHandler;
import Frontend.Parser.decl.DeclParser;
import Frontend.Parser.expr.ExprParser;
import Frontend.Parser.expr.types.Cond;
import Frontend.Parser.expr.types.Exp;
import Frontend.Parser.expr.types.LVal;
import Frontend.Parser.stmt.types.AssignStmt;
import Frontend.Parser.stmt.types.BlockItem;
import Frontend.Parser.stmt.types.BlockStmt;
import Frontend.Parser.stmt.types.BreakStmt;
import Frontend.Parser.stmt.types.ContinueStatement;
import Frontend.Parser.stmt.types.ExpStmt;
import Frontend.Parser.stmt.types.GetIntStmt;
import Frontend.Parser.stmt.types.IfStmt;
import Frontend.Parser.stmt.types.PrintfStmt;
import Frontend.Parser.stmt.types.ReturnStmt;
import Frontend.Parser.stmt.types.Stmt;
import Frontend.Parser.stmt.types.WhileStmt;

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
        if (token.getType() == TokenType.IFTK) {
            return new Stmt(parseIfStmt());
        } else if (token.getType() == TokenType.WHILETK) {
            return new Stmt(parseWhileStmt());
        } else if (token.getType() == TokenType.BREAKTK) {
            return new Stmt(parseBreakStmt(), tokenHandler);
        } else if (token.getType() == TokenType.CONTINUETK) {
            return new Stmt(parseContinueStmt(), tokenHandler);
        } else if (token.getType() == TokenType.RETURNTK) {
            return new Stmt(parseReturnStmt(), tokenHandler);
        } else if (token.getType() == TokenType.PRINTFTK) {
            return new Stmt(parsePrintfStmt(), tokenHandler);
        } else if (token.getType() == TokenType.LBRACE) {
            return new Stmt(parseBlockStatement());
        } else if (token.getType() == TokenType.SEMICN) {
            return new Stmt(null, tokenHandler);  // a single ';'
        } else {
            int step = 0;
            Token ident = tokenHandler.getTokenAndMove();  // skip ident
            step++;
            while (true) {
                token = tokenHandler.getTokenAndMove();
                step++;
                if (token.getType() == TokenType.ASSIGN) {  // encounter '='
                    tokenHandler.retract(step);  // retract to begin place
                    LVal lVal = new ExprParser(tokenHandler).parseLVal();  // point to =
                    Token assign = tokenHandler.getTokenAndMove();
                    token = tokenHandler.getForwardToken();  // check  it
                    if (token.getType() == TokenType.GETINTTK) {
                        // LVal '=' 'getint''('')'';'
                        GetIntStmt getIntStmt = new GetIntStmt(lVal, assign, tokenHandler.getTokenAndMove(),
                                tokenHandler.getTokenAndMove(), tokenHandler.getTokenAndMove(), tokenHandler);
                        return new Stmt(getIntStmt, tokenHandler);
                    } else {
                        // LVal '=' Exp ';'
                        Exp exp = new ExprParser(tokenHandler).parseExp();
                        return new Stmt(new AssignStmt(lVal, assign, exp), tokenHandler);
                    }
                } else if (token.getType() == TokenType.SEMICN) {
                    tokenHandler.retract(step);
                    // Exp ';'
                    ExpStmt expStmt = new ExpStmt(new ExprParser(tokenHandler).parseExp());
                    return new Stmt(expStmt, tokenHandler);
                } else if (token.getLine() != ident.getLine()) {
                    tokenHandler.retract(step);
                    // Exp and miss a ';'
                    ExpStmt expStmt = new ExpStmt(new ExprParser(tokenHandler).parseExp());
                    return new Stmt(expStmt, tokenHandler);
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
        if (right.getType() != TokenType.RPARENT) {
            right = null;
            tokenHandler.retract(1);
        }
        Stmt stmt = parseStmt();
        Token elseToken = tokenHandler.getForwardToken();
        ArrayList<Token> elses = new ArrayList<>();
        ArrayList<Stmt> stmts = new ArrayList<>();
        stmts.add(stmt);
        if (elseToken.getType() == TokenType.ELSETK) {
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
        if (right.getType() != TokenType.RPARENT) {
            right = null;
            tokenHandler.retract(1);
        }
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
        if (tokenHandler.getForwardToken().getType() == TokenType.SEMICN ||
                tokenHandler.getForwardToken().getType() == TokenType.RBRACE ||  // 接到任何关键字都表示return缺失分号而终止
                tokenHandler.getForwardToken().getType().isKeyWord()) {  // 可能缺失分号
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
        while (token.getType() == TokenType.COMMA) {
            seps.add(token);
            exps.add(new ExprParser(tokenHandler).parseExp());
            token = tokenHandler.getTokenAndMove();  // get , or ). point to exp or ;
        }
        if (token.getType() != TokenType.RPARENT) {
            token = null;
            tokenHandler.retract(1);
        }
        return new PrintfStmt(printf, left, formatString, seps, exps, token);
    }

    // Block → '{' { BlockItem } '}'
    public BlockStmt parseBlockStatement() {
        Token left = tokenHandler.getTokenAndMove();
        Token right = tokenHandler.getForwardToken();
        ArrayList<BlockItem> blockItems = new ArrayList<>();
        while (right.getType() != TokenType.RBRACE) {
            if (right.getType() == TokenType.CONSTTK || right.getType() == TokenType.INTTK) {
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
