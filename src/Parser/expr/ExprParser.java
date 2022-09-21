package Parser.expr;

import Lexer.Token;
import Lexer.Type;
import Parser.TokenHandler;
import Parser.expr.types.AddExp;
import Parser.expr.types.Cond;
import Parser.expr.types.EqExp;
import Parser.expr.types.Exp;
import Parser.expr.types.FuncExp;
import Parser.expr.types.LAndExp;
import Parser.expr.types.LOrExp;
import Parser.expr.types.LVal;
import Parser.expr.types.MulExp;
import Parser.expr.types.Number;
import Parser.expr.types.PrimaryExp;
import Parser.expr.types.RelExp;
import Parser.expr.types.UnaryExp;
import Parser.expr.types.UnaryOp;

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
        LVal lVal = new LVal(tokenHandler.getTokenAndMove());
        Token token = tokenHandler.getForwardToken();
        if (token.getType() != Type.LBRACK) {
            return lVal;
        }
        while (token.getType() == Type.LBRACK) {
            tokenHandler.moveForward(1);  // pass [
            lVal.addDim(parseExp());
            tokenHandler.moveForward(1);  // pass ]
            tokenHandler.getForwardToken();
        }
        return lVal;
    }

    // Number → IntConst
    public Number parseNum() {
        return new Number(tokenHandler.getTokenAndMove());
    }

    // UnaryExp → {UnaryOp} PrimaryExp | FuncExp
    // PrimaryExp -> LVal -> Indent {'[' Exp ']'}
    // FuncExp -> Ident '(' [FuncRParams] ')'
    public UnaryExp parseUnaryExp() {
        UnaryExp unaryExp = new UnaryExp();
        Token token = tokenHandler.getForwardToken();
        while (token.getType() == Type.PLUS || token.getType() == Type.MINU || token.getType() == Type.NOT) {
            unaryExp.addOp(new UnaryOp(tokenHandler.getTokenAndMove()));
            token = tokenHandler.getForwardToken();
        }
        if (tokenHandler.getForwardToken().getType() == Type.IDENFR) {
            tokenHandler.moveForward(1);
            token = tokenHandler.getForwardToken();
            tokenHandler.retract(1);
            if (token.getType() == Type.LPARENT) {
                unaryExp.addContent(parseFuncExp());
                return unaryExp;
            }
        }
        unaryExp.addContent(parsePrimaryExp());
        return unaryExp;
    }

    // PrimaryExp → '(' Exp ')' | LVal | Number
    public PrimaryExp parsePrimaryExp() {
        Token token = tokenHandler.getForwardToken();
        if (token.getType() == Type.LPARENT) {
            tokenHandler.moveForward(1);
            PrimaryExp primaryExp = new PrimaryExp(parseExp());
            tokenHandler.moveForward(1);
            return primaryExp;
        } else if (token.getType() == Type.IDENFR) {
            return new PrimaryExp(parseLVal());
        } else {
            return new PrimaryExp(parseNum());
        }
    }

    // FuncExp -> Ident '(' {Exp} ')'
    public FuncExp parseFuncExp() {
        FuncExp funcExp = new FuncExp(tokenHandler.getTokenAndMove());
        tokenHandler.moveForward(1);
        Token token = tokenHandler.getForwardToken();
        if (token.getType() != Type.RPARENT) {
            while (token.getType() != Type.RPARENT) {  // not ) means there is an Exp
                funcExp.addParam(parseExp());
                token = tokenHandler.getTokenAndMove();  // , or ).  if , continue the loop.(already move forward)
            }
        } else {
            tokenHandler.moveForward(1);  // detect ), move forward
        }
        return funcExp;
    }

    public AddExp parseAddExp() {
        // AddExp addExp = new AddExp(parseMulExp());
        // Token token = tokenHandler.getForwardToken();
        // while (token.getType() == Type.PLUS || token.getType() == Type.MINU) {
        //     tokenHandler.moveForward(1);  // skip + or -
        //     addExp.add(token, parseMulExp());
        //     token = tokenHandler.getForwardToken();  // refresh token
        // }
        // return addExp;
        return (AddExp) parseExp();
    }

    public Exp parseExp() {
        // return (Exp) parseAddExp();
        Exp exp = new Exp(parseMulExp());
        Token token = tokenHandler.getForwardToken();
        while (token.getType() == Type.PLUS || token.getType() == Type.MINU) {
            tokenHandler.moveForward(1);  // skip + or -
            exp.add(token, parseMulExp());
            token = tokenHandler.getForwardToken();  // refresh token
        }
        return exp;
    }

    public MulExp parseMulExp() {
        MulExp mulExp = new MulExp(parseUnaryExp());
        Token token = tokenHandler.getForwardToken();
        while (token.getType() == Type.MULT || token.getType() == Type.DIV || token.getType() == Type.MOD) {
            tokenHandler.moveForward(1);  // skip * or / or %
            mulExp.add(token, parseUnaryExp());
            token = tokenHandler.getForwardToken();  // refresh token
        }
        return mulExp;
    }

    public RelExp parseRelExp() {
        RelExp relExp = new RelExp(parseAddExp());
        Token token = tokenHandler.getForwardToken();
        while (token.getType() == Type.GEQ || token.getType() == Type.GRE || token.getType() == Type.LEQ || token.getType() == Type.LSS) {
            tokenHandler.moveForward(1);  // skip >= or > or <= or <
            relExp.add(token, parseAddExp());
            token = tokenHandler.getForwardToken();  // refresh token
        }
        return relExp;
    }

    public EqExp parseEqExp() {
        EqExp eqExp = new EqExp(parseRelExp());
        Token token = tokenHandler.getForwardToken();
        while (token.getType() == Type.EQL || token.getType() == Type.NEQ) {
            tokenHandler.moveForward(1);  // skip == or !=
            eqExp.add(token, parseRelExp());
            token = tokenHandler.getForwardToken();  // refresh token
        }
        return eqExp;
    }

    public LAndExp parseLAndExp() {
        LAndExp lAndExp = new LAndExp(parseEqExp());
        Token token = tokenHandler.getForwardToken();
        while (token.getType() == Type.AND) {
            tokenHandler.moveForward(1);  // skip &&
            lAndExp.add(token, parseEqExp());
            token = tokenHandler.getForwardToken();  // refresh token
        }
        return lAndExp;
    }

    public LOrExp parseLOrExp() {
        return (LOrExp) parseCond();
    }

    public Cond parseCond() {
        Cond cond = new Cond(parseLAndExp());
        Token token = tokenHandler.getForwardToken();
        while (token.getType() == Type.OR) {
            tokenHandler.moveForward(1);  // skip ||
            cond.add(token, parseLAndExp());
            token = tokenHandler.getForwardToken();  // refresh token
        }
        return cond;
    }
}
