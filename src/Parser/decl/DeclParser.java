package Parser.decl;

import Lexer.Token;
import Lexer.TokenType;
import Parser.TokenHandler;
import Parser.decl.types.Decl;
import Parser.decl.types.Def;
import Parser.decl.types.InitVal;
import Parser.decl.types.Var;
import Parser.expr.ExprParser;
import Parser.expr.types.ConstExp;

import java.util.ArrayList;

public class DeclParser {
    /*
        // declare
        // semicolon is stored in Decl

        声明 Decl → ConstDecl | VarDecl
        变量声明 VarDecl → 'int' VarDef { ',' VarDef } ';'
        常量声明 ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'

        常数定义 ConstDef → Var '=' ConstInitVal
        变量定义 VarDef → Var | Var '=' InitVal

        常量变量 Var -> Ident { '[' ConstExp ']' }

        常量初值 ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
        变量初值 InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
    */

    public final TokenHandler tokenHandler;

    public DeclParser(TokenHandler tokenHandler) {
        this.tokenHandler = tokenHandler;
    }

    // 常量声明 ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
    // 变量声明 VarDecl → BType VarDef { ',' VarDef } ';'
    public Decl parseDecl() {
        boolean isConst = tokenHandler.getForwardToken().getType() == TokenType.CONSTTK;
        Token constToken = null;
        if (isConst) constToken = tokenHandler.getTokenAndMove();  // skip const
        Token Btype = tokenHandler.getTokenAndMove();  // skip BType
        ArrayList<Token> seps = new ArrayList<>();
        ArrayList<Def> defs = new ArrayList<>();
        Def def = parseDef(isConst);
        Token sep = tokenHandler.getTokenAndMove();
        while (sep.getType() == TokenType.COMMA) {
            seps.add(sep);
            defs.add(parseDef(isConst));
            sep = tokenHandler.getTokenAndMove();
        }
        if (sep.getType() == TokenType.SEMICN) {
            return new Decl(constToken, Btype, def, seps, defs, sep);
        } else {
            tokenHandler.retract(1);
            return new Decl(constToken, Btype, def, seps, defs, null);
        }
    }

    // 常数定义 ConstDef → Var '=' ConstInitVal // 包含普通变 量、一维数组、二维数组共三种情况
    // 变量定义 VarDef → Var | Var '=' InitVal
    public Def parseDef(boolean isConst) {
        Var var = parseVar(isConst);
        Token token = tokenHandler.getForwardToken();
        if (token.getType() == TokenType.ASSIGN) {
            tokenHandler.moveForward(1);
            return new Def(var, token, parseInitVal(isConst), isConst);
        }
        return new Def(var, isConst);
    }

    // 常量变量 Var -> Ident { '[' ConstExp ']' }
    public Var parseVar(boolean isConst) {
        Token ident = tokenHandler.getTokenAndMove();
        ArrayList<ConstExp> constExps = new ArrayList<>();
        ArrayList<Token> brack = new ArrayList<>();
        Token token = tokenHandler.getForwardToken();
        while (token.getType() == TokenType.LBRACK) {
            Token lBrack = tokenHandler.getTokenAndMove();  // skip [
            constExps.add(new ExprParser(tokenHandler).parseConstExp());
            Token rBrack = tokenHandler.getTokenAndMove();  // skip ]
            if (rBrack.getType() != TokenType.RBRACK) {
                rBrack = null;
                tokenHandler.retract(1);
            }
            brack.add(lBrack);
            brack.add(rBrack);
            token = tokenHandler.getForwardToken();
        }
        return new Var(ident, constExps, brack, isConst);
    }

    // 变量初值 InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'   // const or not
    public InitVal parseInitVal(boolean isConst) {
        Token token = tokenHandler.getForwardToken();
        // {{init, init}, {init, init}}, {{init, init}, {init, init}}
        if (token.getType() == TokenType.LBRACE) {
            Token left = tokenHandler.getTokenAndMove();
            ArrayList<InitVal> initVals = new ArrayList<>();
            ArrayList<Token> seps = new ArrayList<>();
            if (tokenHandler.getForwardToken().getType() != TokenType.RBRACE) {
                initVals.add(parseInitVal(isConst));
                Token sep = tokenHandler.getForwardToken();  // check is , or not
                while (sep.getType() != TokenType.RBRACE) {
                    seps.add(tokenHandler.getTokenAndMove());
                    initVals.add(parseInitVal(isConst));
                    sep = tokenHandler.getForwardToken();
                }
                Token right = tokenHandler.getTokenAndMove();
                return new InitVal(left, right, initVals, seps, isConst);
            } else {
                return new InitVal(left, tokenHandler.getTokenAndMove(), initVals, seps, isConst);
            }
        } else {
            if (isConst) {
                return new InitVal(new ExprParser(tokenHandler).parseConstExp());
            } else {
                return new InitVal(new ExprParser(tokenHandler).parseExp());
            }
        }
    }
}
