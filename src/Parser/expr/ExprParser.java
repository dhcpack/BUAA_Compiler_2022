package Parser.expr;

import Lexer.Token;
import Lexer.Type;
import Parser.TokenHandler;
import Parser.expr.types.AddExp;
import Parser.expr.types.BraceExp;
import Parser.expr.types.Cond;
import Parser.expr.types.ConstExp;
import Parser.expr.types.EqExp;
import Parser.expr.types.Exp;
import Parser.expr.types.FuncExp;
import Parser.expr.types.FuncRParams;
import Parser.expr.types.LAndExp;
import Parser.expr.types.LOrExp;
import Parser.expr.types.LVal;
import Parser.expr.types.MulExp;
import Parser.expr.types.Number;
import Parser.expr.types.PrimaryExp;
import Parser.expr.types.RelExp;
import Parser.expr.types.UnaryExp;
import Parser.expr.types.UnaryOp;

import java.util.ArrayList;

public class ExprParser {
    /*
    // expressions
    左值表达式 LVal → Ident {'[' Exp ']'} //1.普通变量 2.一维数组 3.二维数组
    数值 Number → IntConst // 存在即可

    一元表达式 UnaryExp → {UnaryOp} PrimaryExp | FuncExp // 存在即可
    单目运算符 UnaryOp → '+' | '−' | '!' 注：'!'仅出现在条件表达式中
    基本表达式 PrimaryExp → '(' Exp ')' | LVal | Number // 三种情况均需覆盖
    函数调用   FuncExp -> Ident '(' {Exp} ')'

    // Exp、const Exp --> AddExp
    条件表达式 Cond → LOrExp // 存在即可
    乘除模表达式 MulExp → UnaryExp {('*' | '/' | '%') UnaryExp} // 1.UnaryExp 2.* 3./ 4.% 均需覆盖
    加减表达式 AddExp → MulExp {('+' | '−') MulExp} // 1.MulExp 2.+ 需覆盖 3.- 需覆盖
    关系表达式 RelExp → AddExp {('<' | '>' | '<=' | '>=') AddExp} // 1.AddExp 2.< 3.> 4.<= 5.>= 均需覆盖
    相等性表达式 EqExp → RelExp {('==' | '!=') RelExp} // 1.RelExp 2.== 3.!= 均 需覆盖
    逻辑与表达式 LAndExp → EqExp {'&&' EqExp} // 1.EqExp 2.&& 均需覆盖
    逻辑或表达式 LOrExp → LAndExp {'||' LAndExp} // 1.LAndExp 2.|| 均需覆盖
    */

    public final TokenHandler tokenHandler;

    public ExprParser(TokenHandler tokenHandler) {
        this.tokenHandler = tokenHandler;
    }

    // LVal → Ident {'[' Exp ']'}
    public LVal parseLVal() {
        // LVal lVal = new LVal(tokenHandler.getTokenAndMove());
        Token ident = tokenHandler.getTokenAndMove();
        Token token = tokenHandler.getForwardToken();
        ArrayList<Token> bracs = new ArrayList<>();
        ArrayList<Exp> exps = new ArrayList<>();
        if (token.getType() != Type.LBRACK) {
            return new LVal(ident, exps, bracs);
        }
        while (token.getType() == Type.LBRACK) {
            bracs.add(tokenHandler.getTokenAndMove());  // pass [
            exps.add(parseExp());
            bracs.add(tokenHandler.getTokenAndMove());  // pass ]
            // tokenHandler.moveForward(1);
            token = tokenHandler.getForwardToken();
        }
        return new LVal(ident, exps, bracs);
    }

    // Number → IntConst
    public Number parseNum() {
        return new Number(tokenHandler.getTokenAndMove());
    }

    // UnaryExp → {UnaryOp} PrimaryExp | FuncExp
    // PrimaryExp -> LVal -> Indent {'[' Exp ']'}
    // FuncExp -> Ident '(' [FuncRParams] ')'
    public UnaryExp parseUnaryExp() {
        Token token = tokenHandler.getForwardToken();
        if (token.getType() == Type.PLUS || token.getType() == Type.MINU || token.getType() == Type.NOT) {
            UnaryOp unaryOp = new UnaryOp(tokenHandler.getTokenAndMove());
            return new UnaryExp(unaryOp, parseUnaryExp());
        }
        if (tokenHandler.getForwardToken().getType() == Type.IDENFR) {
            tokenHandler.moveForward(1);
            token = tokenHandler.getForwardToken();
            tokenHandler.retract(1);
            if (token.getType() == Type.LPARENT) {
                return new UnaryExp(null, parseFuncExp());
                // UnaryExpInterface unaryExpInterface = parseFuncExp();
                // unaryExp.addContent(parseFuncExp());
                // return unaryExp;
            }
        }
        return new UnaryExp(null, parsePrimaryExp());
        // unaryExp.addContent(parsePrimaryExp());
        // return unaryExp;
    }

    // PrimaryExp → '(' Exp ')' | LVal | Number
    public PrimaryExp parsePrimaryExp() {
        Token token = tokenHandler.getForwardToken();
        if (token.getType() == Type.LPARENT) {
            Token left = tokenHandler.getTokenAndMove();
            Exp exp = parseExp();
            Token right = tokenHandler.getTokenAndMove();
            return new PrimaryExp(new BraceExp(left, exp, right));
        } else if (token.getType() == Type.IDENFR) {
            return new PrimaryExp(parseLVal());
        } else {
            return new PrimaryExp(parseNum());
        }
    }

    // Ident ‘(’ FuncRParams ‘)’
    public FuncExp parseFuncExp() {
        Token ident = tokenHandler.getTokenAndMove();
        Token left = tokenHandler.getTokenAndMove();
        Token token = tokenHandler.getForwardToken();
        FuncRParams funcRParams = null;
        if(token.getType() != Type.RPARENT){
            funcRParams = parseFuncRParams();
        }
        Token right = tokenHandler.getTokenAndMove();
        return new FuncExp(ident, left, right, funcRParams);
    }

    public FuncRParams parseFuncRParams() {
        // 函数实参表 FuncRParams → Exp { ',' Exp } // 1.花括号内重复0次 2.花括号内重复多次 3. Exp需要覆盖数组传参和部分数组传参
        Exp exp = parseExp();
        Token sep = tokenHandler.getForwardToken();
        ArrayList<Exp> exps = new ArrayList<>();
        ArrayList<Token> seps = new ArrayList<>();
        exps.add(exp);
        while (sep.getType() == Type.COMMA) {
            seps.add(tokenHandler.getTokenAndMove());
            exps.add(parseExp());
            sep = tokenHandler.getForwardToken();
        }
        return new FuncRParams(exps, seps);
    }

    public AddExp parseAddExp() {
        MulExp mulExp = parseMulExp();
        ArrayList<MulExp> exps = new ArrayList<>();
        ArrayList<Token> seps = new ArrayList<>();
        Token token = tokenHandler.getForwardToken();
        while (token.getType() == Type.PLUS || token.getType() == Type.MINU) {
            seps.add(tokenHandler.getTokenAndMove());
            exps.add(parseMulExp());
            token = tokenHandler.getForwardToken();  // refresh token
        }
        return new AddExp(mulExp, exps, seps);
    }

    public Exp parseExp() {
        return new Exp(parseAddExp());
        // Exp exp = new Exp(parseMulExp());
        // Token token = tokenHandler.getForwardToken();
        // while (token.getType() == Type.PLUS || token.getType() == Type.MINU) {
        //     tokenHandler.moveForward(1);  // skip + or -
        //     exp.add(token, parseMulExp());
        //     token = tokenHandler.getForwardToken();  // refresh token
        // }
        // return exp;
    }

    public ConstExp parseConstExp() {
        return new ConstExp(parseAddExp());
    }

    public MulExp parseMulExp() {
        UnaryExp firstExp = parseUnaryExp();
        Token token = tokenHandler.getForwardToken();
        ArrayList<UnaryExp> exps = new ArrayList<>();
        ArrayList<Token> seps = new ArrayList<>();
        while (token.getType() == Type.MULT || token.getType() == Type.DIV || token.getType() == Type.MOD) {
            seps.add(tokenHandler.getTokenAndMove());
            exps.add(parseUnaryExp());
            token = tokenHandler.getForwardToken();
        }
        return new MulExp(firstExp, exps, seps);
    }

    public RelExp parseRelExp() {
        AddExp firstExp = parseAddExp();
        Token token = tokenHandler.getForwardToken();
        ArrayList<AddExp> exps = new ArrayList<>();
        ArrayList<Token> seps = new ArrayList<>();
        while (token.getType() == Type.GEQ || token.getType() == Type.GRE || token.getType() == Type.LEQ || token.getType() == Type.LSS) {
            seps.add(tokenHandler.getTokenAndMove());
            exps.add(parseAddExp());
            token = tokenHandler.getForwardToken();
        }
        return new RelExp(firstExp, exps, seps);
    }

    public EqExp parseEqExp() {
        RelExp firstExp = parseRelExp();
        Token token = tokenHandler.getForwardToken();
        ArrayList<RelExp> exps = new ArrayList<>();
        ArrayList<Token> seps = new ArrayList<>();
        while (token.getType() == Type.EQL || token.getType() == Type.NEQ) {
            seps.add(tokenHandler.getTokenAndMove());
            exps.add(parseRelExp());
            token = tokenHandler.getForwardToken();
        }
        return new EqExp(firstExp, exps, seps);
    }

    public LAndExp parseLAndExp() {
        EqExp firstExp = parseEqExp();
        Token token = tokenHandler.getForwardToken();
        ArrayList<EqExp> exps = new ArrayList<>();
        ArrayList<Token> seps = new ArrayList<>();
        while (token.getType() == Type.AND) {
            seps.add(tokenHandler.getTokenAndMove());
            exps.add(parseEqExp());
            token = tokenHandler.getForwardToken();
        }
        return new LAndExp(firstExp, exps, seps);
    }

    public LOrExp parseLOrExp() {
        LAndExp firstExp = parseLAndExp();
        Token token = tokenHandler.getForwardToken();
        ArrayList<LAndExp> exps = new ArrayList<>();
        ArrayList<Token> seps = new ArrayList<>();
        while (token.getType() == Type.OR) {
            seps.add(tokenHandler.getTokenAndMove());
            exps.add(parseLAndExp());
            token = tokenHandler.getForwardToken();
        }
        return new LOrExp(firstExp, exps, seps);
    }

    public Cond parseCond() {
        return new Cond(parseLOrExp());
    }
}
