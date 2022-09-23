package Parser.decl;

import Lexer.Token;
import Lexer.Type;
import Parser.TokenHandler;
import Parser.decl.types.Decl;
import Parser.decl.types.Def;
import Parser.decl.types.InitVal;
import Parser.decl.types.Var;
import Parser.expr.ExprParser;
import Parser.expr.types.ConstExp;

import java.util.ArrayList;

public class DeclParser {
    public final TokenHandler tokenHandler;

    public DeclParser(TokenHandler tokenHandler) {
        this.tokenHandler = tokenHandler;
    }

    // 常量声明 ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
    // 变量声明 VarDecl → BType VarDef { ',' VarDef } ';'
    public Decl parseDecl() {
        boolean isConst = tokenHandler.getForwardToken().getType() == Type.CONSTTK;
        Token constToken = null;
        if (isConst) constToken = tokenHandler.getTokenAndMove();  // skip const
        Token Btype = tokenHandler.getTokenAndMove();  // skip BType
        ArrayList<Token> seps = new ArrayList<>();
        ArrayList<Def> defs = new ArrayList<>();
        Def def = parseDef(isConst);
        Token sep = tokenHandler.getTokenAndMove();
        while (sep.getType() == Type.COMMA) {
            seps.add(sep);
            defs.add(parseDef(isConst));
            sep = tokenHandler.getTokenAndMove();
        }
        return new Decl(constToken, Btype, def, seps, defs, sep);
    }

    // 常数定义 ConstDef → Var '=' ConstInitVal // 包含普通变 量、一维数组、二维数组共三种情况
    // 变量定义 VarDef → Var | Var '=' InitVal
    public Def parseDef(boolean isConst) {
        Var var = parseVar();
        Token token = tokenHandler.getForwardToken();
        if (token.getType() == Type.ASSIGN) {
            tokenHandler.moveForward(1);
            return new Def(var, token, parseInitVal(isConst), isConst);
        }
        return new Def(var, isConst);
    }

    // 常量变量 Var -> Ident { '[' ConstExp ']' }
    public Var parseVar() {
        Token ident = tokenHandler.getTokenAndMove();
        ArrayList<ConstExp> constExps = new ArrayList<>();
        ArrayList<Token> brack = new ArrayList<>();
        Token token = tokenHandler.getForwardToken();
        while (token.getType() == Type.LBRACK) {
            Token lBrack = tokenHandler.getTokenAndMove();  // skip [
            constExps.add(new ExprParser(tokenHandler).parseConstExp());
            Token rBrack = tokenHandler.getTokenAndMove();  // skip ]
            brack.add(lBrack);
            brack.add(rBrack);
            token = tokenHandler.getForwardToken();
        }
        return new Var(ident, constExps, brack);
    }

    // 变量初值 InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'   // const or not
    public InitVal parseInitVal(boolean isConst) {
        Token token = tokenHandler.getForwardToken();
        // {{init, init}, {init, init}}, {{init, init}, {init, init}}
        if (token.getType() == Type.LBRACE) {
            Token left = tokenHandler.getTokenAndMove();
            ArrayList<InitVal> initVals = new ArrayList<>();
            ArrayList<Token> seps = new ArrayList<>();
            initVals.add(parseInitVal(isConst));
            Token sep = tokenHandler.getForwardToken();  // check is , or not
            while (sep.getType() != Type.RBRACE) {
                seps.add(tokenHandler.getTokenAndMove());
                initVals.add(parseInitVal(isConst));
                sep = tokenHandler.getForwardToken();
            }
            Token right = tokenHandler.getTokenAndMove();
            return new InitVal(left, right, initVals, seps, isConst);
        } else {
            if (isConst) {
                return new InitVal(new ExprParser(tokenHandler).parseConstExp());
            } else {
                return new InitVal(new ExprParser(tokenHandler).parseExp());
            }
        }
    }
}
