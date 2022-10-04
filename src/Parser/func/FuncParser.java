package Parser.func;

import Lexer.Token;
import Lexer.TokenType;
import Parser.TokenHandler;
import Parser.expr.ExprParser;
import Parser.expr.types.ConstExp;
import Parser.func.types.FuncDef;
import Parser.func.types.FuncFParam;
import Parser.func.types.MainFuncDef;
import Parser.stmt.StmtParser;

import java.util.ArrayList;

public class FuncParser {
    /*
        // functions
        函数定义 FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        主函数定义 MainFuncDef → 'int' 'main' '(' ')' Block

        函数类型 FuncType → 'void' | 'int'

        函数形参表 FuncFParams → FuncFParam { ',' FuncFParam }
        函数形参 FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
    * */
    public final TokenHandler tokenHandler;

    public FuncParser(TokenHandler tokenHandler) {
        this.tokenHandler = tokenHandler;
    }

    // FuncDef → FuncType Ident '(' [FuncFParam { ',' FuncFParam }] ')' Block
    public FuncDef parseFuncDef() {
        Token funcType = tokenHandler.getTokenAndMove();
        Token ident = tokenHandler.getTokenAndMove();
        ArrayList<FuncFParam> funcFParams = new ArrayList<>();
        Token left = tokenHandler.getTokenAndMove();  // skip (
        Token right = tokenHandler.getForwardToken();  // get FuncFParam or ). point to same place
        ArrayList<Token> seps = new ArrayList<>();
        if (right.getType() != TokenType.RPARENT && right.getType() != TokenType.LBRACE) {
            while (right.getType() != TokenType.RPARENT && right.getType() != TokenType.LBRACE) {
                seps.add(right);
                funcFParams.add(parseFuncFParam());
                right = tokenHandler.getTokenAndMove();  // get , or ) and already skip
            }
            seps.remove(0);  // first element is not a sep
        } else {
            right = tokenHandler.getTokenAndMove();  // skip )
        }
        if (right.getType() != TokenType.RPARENT) {
            right = null;
            tokenHandler.retract(1);
        }
        return new FuncDef(funcType, ident, left, funcFParams, right,
                new StmtParser(tokenHandler).parseBlockStatement(), seps);
    }

    public MainFuncDef parseMainFuncDef() {
        return new MainFuncDef(parseFuncDef());
    }

    // FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
    public FuncFParam parseFuncFParam() {
        Token BType = tokenHandler.getTokenAndMove();  // skip BType
        Token ident = tokenHandler.getTokenAndMove();
        Token token = tokenHandler.getForwardToken();
        ArrayList<Token> bracs = new ArrayList<>();
        if (token.getType() == TokenType.LBRACK) {
            bracs.add(tokenHandler.getTokenAndMove());  // skip [
            if (tokenHandler.getForwardToken().getType() == TokenType.RBRACK) {
                bracs.add(tokenHandler.getTokenAndMove());  // skip ]
            } else {
                bracs.add(null);
            }
            token = tokenHandler.getForwardToken();
            ArrayList<ConstExp> constExps = new ArrayList<>();
            while (token.getType() == TokenType.LBRACK) {
                bracs.add(tokenHandler.getTokenAndMove());  // skip [ and point to ConstExp
                constExps.add(new ExprParser(tokenHandler).parseConstExp());
                if (tokenHandler.getForwardToken().getType() == TokenType.RBRACK) {
                    bracs.add(tokenHandler.getTokenAndMove());  // skip ]
                } else {
                    bracs.add(null);
                }
                token = tokenHandler.getForwardToken();
            }
            return new FuncFParam(BType, ident, true, constExps, bracs);
        }
        return new FuncFParam(BType, ident, false, new ArrayList<>(), bracs);
    }
}
