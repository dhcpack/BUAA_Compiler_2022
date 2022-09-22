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
        语句块 Block → '{' { BlockItem } '}' // 1.花括号内重复0次 2.花括号内重复多次
        语句块项 BlockItem → Decl | Stmt // 覆盖两种语句块项
        语句 Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆
        | [Exp] ';' //有无Exp两种情况
        | Block
        | 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
        | 'while' '(' Cond ')' Stmt
        | 'break' ';' | 'continue' ';'
        | 'return' [Exp] ';' // 1.有Exp 2.无Exp
        | LVal '=' 'getint''('')'';'
        | 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
    * */
    public final TokenHandler tokenHandler;

    public StmtParser(TokenHandler tokenHandler) {
        this.tokenHandler = tokenHandler;
    }

    public Stmt parseStmt() {
        // all ; are saved in stmt
        Token token = tokenHandler.getForwardToken();
        Stmt stmt = null;
        if (token.getType() == Type.IFTK) {
            return new Stmt(parseIfStmt(), null);
        } else if (token.getType() == Type.WHILETK) {
            return new Stmt(parseWhileStmt(), null);
        } else if (token.getType() == Type.BREAKTK) {
            BreakStmt breakStmt = parseBreakStmt();
            Token semicn = tokenHandler.getTokenAndMove();
            return new Stmt(breakStmt, semicn);
        } else if (token.getType() == Type.CONTINUETK) {
            ContinueStatement continueStatement = parseContinueStmt();
            Token semicn = tokenHandler.getTokenAndMove();
            return new Stmt(continueStatement, semicn);
        } else if (token.getType() == Type.RETURNTK) {
            ReturnStmt returnStmt = parseReturnStmt();
            Token semicn = tokenHandler.getTokenAndMove();
            return new Stmt(returnStmt, semicn);
        } else if (token.getType() == Type.PRINTFTK) {
            PrintfStmt printfStmt = parsePrintfStmt();
            Token semicn = tokenHandler.getTokenAndMove();
            return new Stmt(printfStmt, semicn);
        } else if (token.getType() == Type.LBRACE) {
            BlockStmt blockStmt = parseBlockStatement();
            return new Stmt(blockStmt, null);
        } else if (token.getType() == Type.SEMICN) {
            // ;
            Token semicn = tokenHandler.getTokenAndMove();
            return new Stmt(null, semicn);
        } else {
            int step = 0;
            while (true) {
                token = tokenHandler.getTokenAndMove();
                step++;
                if (token.getType() == Type.ASSIGN) {
                    tokenHandler.retract(step);
                    LVal lVal = new ExprParser(tokenHandler).parseLVal();  // point to =
                    Token assign = tokenHandler.getTokenAndMove();
                    token = tokenHandler.getForwardToken();  // check  it
                    if (token.getType() == Type.GETINTTK) {
                        // LVal '=' 'getint''('')'';'
                        GetIntStmt getIntStmt = new GetIntStmt(lVal, assign, tokenHandler.getTokenAndMove(),
                                tokenHandler.getTokenAndMove(), tokenHandler.getTokenAndMove());
                        Token semicn = tokenHandler.getTokenAndMove();
                        return new Stmt(getIntStmt, semicn);
                    } else {
                        // LVal '=' Exp ';'
                        Exp exp = new ExprParser(tokenHandler).parseExp();
                        Token semicn = tokenHandler.getTokenAndMove();
                        return new Stmt(new AssignStmt(lVal, assign, exp), semicn);
                    }
                }
                else if (token.getType() == Type.SEMICN) {
                    tokenHandler.retract(step);
                    // Exp ';'
                    ExpStmt expStmt = new ExpStmt(new ExprParser(tokenHandler).parseExp());
                    Token semicn = tokenHandler.getTokenAndMove();
                    return new Stmt(expStmt, semicn);
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
        // PrintfStmt printfStmt = new PrintfStmt(tokenHandler.getTokenAndMove());  // , or )
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
