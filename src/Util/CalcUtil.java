package Util;

import Lexer.Token;
import Parser.expr.types.AddExp;
import Parser.expr.types.BraceExp;
import Parser.expr.types.ConstExp;
import Parser.expr.types.Exp;
import Parser.expr.types.FuncExp;
import Parser.expr.types.LVal;
import Parser.expr.types.MulExp;
import Parser.expr.types.Number;
import Lexer.TokenType;
import Parser.expr.types.PrimaryExp;
import Parser.expr.types.PrimaryExpInterface;
import Parser.expr.types.UnaryExp;
import Parser.expr.types.UnaryExpInterface;
import Parser.expr.types.UnaryOp;

import java.util.ArrayList;

public class CalcUtil {
    // TODO: 只编写了数字表达式的运算
    public static int calcConstExp(ConstExp constExp) {
        return calcAddExp(constExp.getAddExp());
    }

    public static int calcExp(Exp exp) {
        return calcAddExp(exp.getAddExp());
    }

    public static int calcAddExp(AddExp addExp) {
        int first = calcMulExp(addExp.getFirstExp());
        ArrayList<MulExp> mulExps = addExp.getExps();
        ArrayList<Token> seps = addExp.getSeps();
        assert mulExps.size() == seps.size();
        for (int i = 0; i < mulExps.size(); i++) {
            Token op = seps.get(i);
            MulExp mulExp = mulExps.get(i);
            if (op.getType() == TokenType.PLUS) {
                first += calcMulExp(mulExp);
            } else if (op.getType() == TokenType.MINU) {
                first -= calcMulExp(mulExp);
            } else {
                assert false;
            }
        }
        return first;
    }

    public static int calcMulExp(MulExp mulExp) {
        int first = calcUnaryExp(mulExp.getFirstExp());
        ArrayList<UnaryExp> unaryExps = mulExp.getExps();
        ArrayList<Token> seps = mulExp.getSeps();
        assert unaryExps.size() == seps.size();
        for (int i = 0; i < unaryExps.size(); i++) {
            Token op = seps.get(i);
            UnaryExp unaryExp = unaryExps.get(i);
            if (op.getType() == TokenType.MULT) {
                first *= calcUnaryExp(unaryExp);
            } else if (op.getType() == TokenType.DIV) {
                first /= calcUnaryExp(unaryExp);
            } else if (op.getType() == TokenType.MOD) {
                first %= calcUnaryExp(unaryExp);
            } else {
                assert false;
            }
        }
        return first;
    }

    public static int calcUnaryExp(UnaryExp unaryExp) {
        UnaryExpInterface unaryExpInterface = unaryExp.getUnaryExpInterface();
        UnaryOp unaryOp = unaryExp.getOp();
        if (unaryExpInterface instanceof FuncExp) {
            assert false;  // TODO: 不进行这种表达式的计算
            return calcFuncExp((FuncExp) unaryExpInterface);
        } else if (unaryExpInterface instanceof PrimaryExp) {
            return calcPrimaryExp((PrimaryExp) unaryExpInterface);
        } else {
            assert unaryOp != null;
            int res = calcUnaryExp(unaryExp);
            if (unaryOp.getToken().getType() == TokenType.MINU) {
                return -res;
            } else if (unaryOp.getToken().getType() == TokenType.NOT) {
                if (res == 0) {
                    return 1;
                } else {
                    return 0;
                }
            } else {
                assert unaryOp.getToken().getType() == TokenType.PLUS;
                return res;
            }
        }
    }

    public static int calcPrimaryExp(PrimaryExp primaryExp) {
        PrimaryExpInterface primaryExpInterface = primaryExp.getPrimaryExpInterface();
        if (primaryExpInterface instanceof BraceExp) {
            return calcExp(((BraceExp) primaryExpInterface).getExp());
        } else if (primaryExpInterface instanceof LVal) {
            assert false;  // TODO: 不进行这种表达式的计算
            return calcLVal((LVal) primaryExpInterface);
        } else if (primaryExpInterface instanceof Number) {
            return ((Number) primaryExpInterface).getNumber();
        } else {
            assert false;
        }
        return -20231164;
    }

    public static int calcLVal(LVal lVal) {
        return -20231164;
    }

    public static int calcFuncExp(FuncExp funcExp) {
        return -20231164;
    }
}
