package Frontend.Parser.expr;

import Exceptions.MyAssert;
import Frontend.Lexer.Token;
import Frontend.Lexer.TokenType;
import Frontend.Parser.TokenHandler;
import Frontend.Parser.expr.types.AddExp;
import Frontend.Parser.expr.types.BraceExp;
import Frontend.Parser.expr.types.Cond;
import Frontend.Parser.expr.types.ConstExp;
import Frontend.Parser.expr.types.EqExp;
import Frontend.Parser.expr.types.Exp;
import Frontend.Parser.expr.types.FuncExp;
import Frontend.Parser.expr.types.FuncRParams;
import Frontend.Parser.expr.types.LAndExp;
import Frontend.Parser.expr.types.LOrExp;
import Frontend.Parser.expr.types.LVal;
import Frontend.Parser.expr.types.MulExp;
import Frontend.Parser.expr.types.Number;
import Frontend.Parser.expr.types.PrimaryExp;
import Frontend.Parser.expr.types.RelExp;
import Frontend.Parser.expr.types.UnaryExp;
import Frontend.Parser.expr.types.UnaryOp;

import java.util.ArrayList;

public class ExprParser {
    /*
        // expressions
        左值表达式 LVal → Ident {'[' Exp ']'}
        数值 Number → IntConst

        一元表达式 UnaryExp → PrimaryExp | FuncExp | UnaryOp UnaryExp
        单目运算符 UnaryOp → '+' | '−' | '!'
        带括号的表达式 BraceExp -> '(' Exp ')'
        基本表达式 PrimaryExp → BraceExp | LVal | Number

        函数调用 FuncExp --> Ident '(' [FuncRParams] ')'
        函数实参表 FuncRParams → Exp { ',' Exp }

        表达式 Exp --> AddExp
        常量表达式 const Exp --> AddExp
        条件表达式 Cond → LOrExp
        乘除模表达式 MulExp → UnaryExp {('*' | '/' | '%') UnaryExp}
        加减表达式 AddExp → MulExp {('+' | '−') MulExp}
        关系表达式 RelExp → AddExp {('<' | '>' | '<=' | '>=') AddExp}
        相等性表达式 EqExp → RelExp {('==' | '!=') RelExp}
        逻辑与表达式 LAndExp → EqExp {'&&' EqExp}
        逻辑或表达式 LOrExp → LAndExp {'||' LAndExp}
    */

    public final TokenHandler tokenHandler;

    public ExprParser(TokenHandler tokenHandler) {
        this.tokenHandler = tokenHandler;
    }

    public boolean inFirstExp(Token token) {  // Exp的FIRST集
        return token.getType() == TokenType.LPARENT || token.getType() == TokenType.IDENFR || token.getType() == TokenType.INTCON
                || token.getType() == TokenType.PLUS || token.getType() == TokenType.MINU || token.getType() == TokenType.NOT;
    }

    // LVal → Ident {'[' Exp ']'}
    public LVal parseLVal() {
        Token ident = tokenHandler.getTokenAndMove();
        Token token = tokenHandler.getForwardToken();
        ArrayList<Token> bracs = new ArrayList<>();
        ArrayList<Exp> exps = new ArrayList<>();
        if (token.getType() != TokenType.LBRACK) {
            return new LVal(ident, exps, bracs);
        }
        while (token.getType() == TokenType.LBRACK) {
            bracs.add(tokenHandler.getTokenAndMove());  // pass [
            exps.add(parseExp());
            if (tokenHandler.getForwardToken().getType() == TokenType.RBRACK) {
                bracs.add(tokenHandler.getTokenAndMove());  // pass ]
            } else {
                bracs.add(null);
            }
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
        if (token.getType() == TokenType.PLUS || token.getType() == TokenType.MINU || token.getType() == TokenType.NOT) {
            UnaryOp unaryOp = new UnaryOp(tokenHandler.getTokenAndMove());
            return new UnaryExp(unaryOp, parseUnaryExp());  // has UnaryOp
        }
        if (tokenHandler.getForwardToken().getType() == TokenType.IDENFR) {
            tokenHandler.moveForward(1);
            token = tokenHandler.getForwardToken();
            tokenHandler.retract(1);
            if (token.getType() == TokenType.LPARENT) {
                return new UnaryExp(null, parseFuncExp());  // only funcExp
            }
        }
        return new UnaryExp(null, parsePrimaryExp());  // only primaryExp
    }

    // PrimaryExp → '(' Exp ')' | LVal | Number
    public PrimaryExp parsePrimaryExp() {
        Token token = tokenHandler.getForwardToken();
        if (token.getType() == TokenType.LPARENT) {
            Token left = tokenHandler.getTokenAndMove();
            Exp exp = parseExp();
            Token right = tokenHandler.getTokenAndMove();
            if (right.getType() != TokenType.RPARENT) {
                right = null;
                tokenHandler.retract(1);
            }
            return new PrimaryExp(new BraceExp(left, exp, right));
        } else if (token.getType() == TokenType.IDENFR) {
            return new PrimaryExp(parseLVal());
        } else if (token.getType() == TokenType.INTCON) {
            return new PrimaryExp(parseNum());
        } else {
            MyAssert.ass(false, "Warning: bad parse of an expr");
            assert false;
            System.out.println("Warning: bad parse of an expr");
            return null;  // not an expr  Warning!!!
        }
    }

    // 函数调用 FuncExp --> Ident '(' [FuncRParams] ')'
    public FuncExp parseFuncExp() {
        Token ident = tokenHandler.getTokenAndMove();
        Token left = tokenHandler.getTokenAndMove();
        Token token = tokenHandler.getForwardToken();
        FuncRParams funcRParams = null;
        if (token.getType() != TokenType.RPARENT) {  // 不是右括号可能是有实参，也可能是有括号缺失
            if (inFirstExp(token)) {  // 检查是否在Exp的FIRST集中
                funcRParams = parseFuncRParams();
            }
        }
        Token right = tokenHandler.getTokenAndMove();
        if (right.getType() != TokenType.RPARENT) {
            right = null;
            tokenHandler.retract(1);
        }
        return new FuncExp(ident, left, right, funcRParams);
    }

    public FuncRParams parseFuncRParams() {
        // 函数实参表 FuncRParams → Exp { ',' Exp }
        Exp exp = parseExp();
        Token sep = tokenHandler.getForwardToken();
        ArrayList<Exp> exps = new ArrayList<>();
        ArrayList<Token> seps = new ArrayList<>();
        exps.add(exp);
        while (sep.getType() == TokenType.COMMA) {
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
        while (token.getType() == TokenType.PLUS || token.getType() == TokenType.MINU) {
            seps.add(tokenHandler.getTokenAndMove());
            exps.add(parseMulExp());
            token = tokenHandler.getForwardToken();  // refresh token
        }
        return new AddExp(mulExp, exps, seps);
    }

    public Exp parseExp() {
        return new Exp(parseAddExp());
    }

    public ConstExp parseConstExp() {
        return new ConstExp(parseAddExp());
    }

    public MulExp parseMulExp() {
        UnaryExp firstExp = parseUnaryExp();
        Token token = tokenHandler.getForwardToken();
        ArrayList<UnaryExp> exps = new ArrayList<>();
        ArrayList<Token> seps = new ArrayList<>();
        while (token.getType() == TokenType.MULT || token.getType() == TokenType.DIV || token.getType() == TokenType.MOD) {
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
        while (token.getType() == TokenType.GEQ || token.getType() == TokenType.GRE || token.getType() == TokenType.LEQ || token.getType() == TokenType.LSS) {
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
        while (token.getType() == TokenType.EQL || token.getType() == TokenType.NEQ) {
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
        while (token.getType() == TokenType.AND) {
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
        while (token.getType() == TokenType.OR) {
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
