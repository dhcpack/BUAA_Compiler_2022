package Parser.stmt;

import Lexer.Token;
import Lexer.Type;
import Parser.TokenHandler;
import Parser.decl.DeclParser;
import Parser.expr.ExprParser;
import Parser.expr.types.Cond;
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
        Token token = tokenHandler.getForwardToken();
        Stmt stmt = null;
        if (token.getType() == Type.IFTK) {
            return parseIfStmt();
        } else if (token.getType() == Type.WHILETK) {
            return parseWhileStmt();
        } else if (token.getType() == Type.BREAKTK) {
            stmt = parseBreakStmt();
        } else if (token.getType() == Type.CONTINUETK) {
            stmt = parseContinueStmt();
        } else if (token.getType() == Type.RETURNTK) {
            stmt = parseReturnStmt();
        } else if (token.getType() == Type.PRINTFTK) {
            stmt = parsePrintfStmt();
        } else if (token.getType() == Type.LBRACE) {
            stmt = parseBlockStatement();
        } else if (token.getType() == Type.SEMICN) {
            // ;
            stmt = new ExpStmt();
        } else {
            int step = 0;
            while (true) {
                token = tokenHandler.getTokenAndMove();
                step++;
                if (token.getType() == Type.EQL) {
                    tokenHandler.retract(step);
                    token = tokenHandler.getForwardToken();
                    LVal lVal = new ExprParser(tokenHandler).parseLVal();  // point to =
                    tokenHandler.moveForward(1);
                    if (token.getType() == Type.GETINTTK) {
                        // LVal '=' 'getint''('')'';'
                        stmt = new GetIntStmt(lVal);
                        tokenHandler.moveForward(3);  // skip getint ( )
                    } else {
                        // LVal '=' Exp ';'
                        stmt = new AssignStmt(lVal, new ExprParser(tokenHandler).parseExp());
                    }
                    break;
                }
                if (token.getType() == Type.SEMICN) {
                    tokenHandler.retract(step);
                    // Exp ';'
                    stmt = new ExpStmt(new ExprParser(tokenHandler).parseExp());
                    break;
                }
            }
        }
        tokenHandler.moveForward(1);
        return stmt;
    }

    // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
    public IfStmt parseIfStmt() {
        tokenHandler.moveForward(2);
        Cond cond = new ExprParser(tokenHandler).parseCond();
        tokenHandler.moveForward(1);
        IfStmt ifStmt = new IfStmt(cond, parseStmt());
        Token token = tokenHandler.getForwardToken();
        if (token.getType() == Type.ELSETK) {
            tokenHandler.moveForward(1);
            ifStmt.addBranch(parseStmt());
        }
        return ifStmt;
    }

    // 'while' '(' Cond ')' Stmt
    public WhileStmt parseWhileStmt() {
        tokenHandler.moveForward(2);
        Cond cond = new ExprParser(tokenHandler).parseCond();
        tokenHandler.moveForward(1);
        return new WhileStmt(cond, parseStmt());
    }

    // 'break' ';'
    public BreakStmt parseBreakStmt() {
        return new BreakStmt();
    }

    // 'continue' ';'
    public ContinueStatement parseContinueStmt() {
        return new ContinueStatement();
    }

    // 'return' [Exp] ';'
    public ReturnStmt parseReturnStmt() {
        tokenHandler.moveForward(1);
        if (tokenHandler.getForwardToken().getType() == Type.SEMICN) {
            return new ReturnStmt();
        } else {
            return new ReturnStmt(new ExprParser(tokenHandler).parseExp());
        }
    }

    // 'printf''('FormatString{','Exp}')'';'
    public PrintfStmt parsePrintfStmt() {
        tokenHandler.moveForward(2);  // point to FormatString
        PrintfStmt printfStmt = new PrintfStmt(tokenHandler.getTokenAndMove());  // , or )
        Token token = tokenHandler.getTokenAndMove();  // get , or ). point to exp or ;
        while (token.getType() == Type.COMMA) {
            printfStmt.addExp(new ExprParser(tokenHandler).parseExp());
            token = tokenHandler.getTokenAndMove();  // get , or ). point to exp or ;
        }
        return printfStmt;
    }

    // Block → '{' { BlockItem } '}'
    public BlockStmt parseBlockStatement() {
        BlockStmt blockStmt = new BlockStmt();
        tokenHandler.moveForward(1);
        Token token = tokenHandler.getForwardToken();
        while (token.getType() != Type.RBRACE) {
            if (token.getType() == Type.CONSTTK || token.getType() == Type.INTTK) {
                blockStmt.addBlockItem(new DeclParser(tokenHandler).parseDecl());
            } else {
                blockStmt.addBlockItem(parseStmt());
            }
            token = tokenHandler.getForwardToken();
        }
        tokenHandler.moveForward(1);  // skip }
        return blockStmt;
    }

    // public BlockItem parseBlockItem() {
    //
    // }

}
