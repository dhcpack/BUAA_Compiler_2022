package Parser.func;

import Lexer.Token;
import Lexer.Type;
import Parser.TokenHandler;
import Parser.expr.ExprParser;
import Parser.expr.types.ConstExp;
import Parser.expr.types.Exp;
import Parser.func.types.FuncDef;
import Parser.func.types.FuncFParam;
import Parser.func.types.MainFuncDef;
import Parser.stmt.StmtParser;

import java.util.ArrayList;

public class FuncParser {
    /*
        // functions
        函数定义 FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // 1.无形参 2.有形参
        主函数定义 MainFuncDef → 'int' 'main' '(' ')' Block // 存在main函数
        函数类型 FuncType → 'void' | 'int' // 覆盖两种类型的函数
        函数形参表 FuncFParams → FuncFParam { ',' FuncFParam } // 1.花括号内重复0次 2.花括号 内重复多次
        函数形参 FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }] // 1.普通变量 2.一维数组变量 3.二维数组变量
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
        if (right.getType() != Type.RPARENT) {
            while (right.getType() != Type.RPARENT) {
                seps.add(right);
                funcFParams.add(parseFuncFParam());
                right = tokenHandler.getTokenAndMove();  // get , or ) and already skip
            }
            seps.remove(0);
        } else {
            right = tokenHandler.getTokenAndMove();  // skip )
        }
        return new FuncDef(funcType, ident, left, funcFParams, right,
                new StmtParser(tokenHandler).parseBlockStatement(), seps);
    }

    public MainFuncDef parseMainFuncDef() {
        return new MainFuncDef(parseFuncDef());
    }

    // FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }] // 1.普通变量 2.一维数组变量 3.二维数组变量
    public FuncFParam parseFuncFParam() {
        Token BType = tokenHandler.getTokenAndMove();  // skip BType
        Token ident = tokenHandler.getTokenAndMove();
        Token token = tokenHandler.getForwardToken();
        ArrayList<Token> bracs = new ArrayList<>();
        if (token.getType() == Type.LBRACK) {
            bracs.add(tokenHandler.getTokenAndMove());  // skip [
            bracs.add(tokenHandler.getTokenAndMove());  // skip ]
            // tokenHandler.moveForward(2);  // point to next [ or end
            token = tokenHandler.getForwardToken();
            ArrayList<ConstExp> constExps = new ArrayList<>();
            while (token.getType() == Type.LBRACK) {
                bracs.add(tokenHandler.getTokenAndMove());  // skip [ and point to ConstExp
                // tokenHandler.moveForward(1);  // skip [ and point to ConstExp
                constExps.add(new ExprParser(tokenHandler).parseConstExp());
                // tokenHandler.moveForward(1);  // skip ]
                bracs.add(tokenHandler.getTokenAndMove());  // skip ]
                token = tokenHandler.getForwardToken();
            }
            return new FuncFParam(BType, ident, true, constExps, bracs);
        }
        return new FuncFParam(BType, ident, false, new ArrayList<>(), bracs);
    }
}
