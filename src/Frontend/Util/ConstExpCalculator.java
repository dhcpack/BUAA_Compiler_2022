package Frontend.Util;

import Config.SIPair;
import Exceptions.MissRbrackException;
import Exceptions.MyAssert;
import Exceptions.UndefinedTokenException;
import Frontend.Lexer.Token;
import Frontend.Lexer.TokenType;
import Frontend.Parser.expr.types.AddExp;
import Frontend.Parser.expr.types.BraceExp;
import Frontend.Parser.expr.types.ConstExp;
import Frontend.Parser.expr.types.Exp;
import Frontend.Parser.expr.types.FuncExp;
import Frontend.Parser.expr.types.LVal;
import Frontend.Parser.expr.types.MulExp;
import Frontend.Parser.expr.types.Number;
import Frontend.Parser.expr.types.PrimaryExp;
import Frontend.Parser.expr.types.PrimaryExpInterface;
import Frontend.Parser.expr.types.UnaryExp;
import Frontend.Parser.expr.types.UnaryExpInterface;
import Frontend.Parser.expr.types.UnaryOp;
import Frontend.Symbol.Errors;
import Frontend.Symbol.Symbol;
import Frontend.Symbol.SymbolTable;
import Frontend.Symbol.SymbolType;

import java.util.ArrayList;

/*
 * 此工具类只能进行常量表达式的计算，最终计算结果是一个int
 * */
public class ConstExpCalculator {
    private final SymbolTable symbolTable;
    private final Errors errors;
    private final boolean inlining;
    private final int currentIndex;
    private final SymbolTable inlineSymbolTable;
    private final SymbolTable topSymbolTable;

    private static final int origin = 0;
    private static final int inlineLocal = 1;
    private static final int global = 2;

    public SIPair transferIdent(String former) {
        if (!inlining) {
            return new SIPair(former, origin);
        } else {
            if (inlineSymbolTable.defined(former)) {
                return new SIPair("INLINE_" + former + "_" + currentIndex, inlineLocal);
            }
            return new SIPair(former, global);  // global Var
        }
    }

    public ConstExpCalculator(SymbolTable symbolTable, Errors errors, boolean inlining, int currentIndex,
                              SymbolTable inlineSymbolTable, SymbolTable topSymbolTable) {
        this.symbolTable = symbolTable;
        this.errors = errors;
        this.currentIndex = currentIndex;
        this.inlining = inlining;
        this.inlineSymbolTable = inlineSymbolTable;
        this.topSymbolTable = topSymbolTable;
    }

    public int calcConstExp(ConstExp constExp) {
        return calcAddExp(constExp.getAddExp());
    }

    public int calcExp(Exp exp) {
        return calcAddExp(exp.getAddExp());
    }

    public int calcAddExp(AddExp addExp) {
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
                assert false : "op in addExp is wrong";
            }
        }
        return first;
    }

    public int calcMulExp(MulExp mulExp) {
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
                assert false : "op in mulExp is wrong";
            }
        }
        return first;
    }

    public int calcUnaryExp(UnaryExp unaryExp) {
        UnaryExpInterface unaryExpInterface = unaryExp.getUnaryExpInterface();
        UnaryOp unaryOp = unaryExp.getOp();
        if (unaryExpInterface instanceof FuncExp) {
            MyAssert.ass(false, "函数表达式不能在编译阶段计算");  // TODO: 不进行这种表达式的计算, assert false
            return -20231164;
        } else if (unaryExpInterface instanceof PrimaryExp) {
            return calcPrimaryExp((PrimaryExp) unaryExpInterface);
        } else {
            MyAssert.ass(unaryOp != null);
            int res = calcUnaryExp((UnaryExp) unaryExpInterface);
            if (unaryOp.getToken().getType() == TokenType.MINU) {
                return -res;
            } else if (unaryOp.getToken().getType() == TokenType.NOT) {
                if (res == 0) {
                    return 1;
                } else {
                    return 0;
                }
            } else if (unaryOp.getToken().getType() == TokenType.PLUS) {
                return res;
            } else {
                MyAssert.ass(false);
                return -2022;
            }
        }
    }

    public int calcPrimaryExp(PrimaryExp primaryExp) {
        PrimaryExpInterface primaryExpInterface = primaryExp.getPrimaryExpInterface();
        if (primaryExpInterface instanceof BraceExp) {
            return calcExp(((BraceExp) primaryExpInterface).getExp());
        } else if (primaryExpInterface instanceof LVal) {
            return calcLVal((LVal) primaryExpInterface);
        } else if (primaryExpInterface instanceof Number) {
            return ((Number) primaryExpInterface).getNumber();
        } else {
            MyAssert.ass(false);
            return -20231164;
        }
    }

    public int calcLVal(LVal lVal) {
        Token ident = lVal.getIdent();
        SIPair siPair = transferIdent(ident.getContent());
        Symbol symbol;
        if (siPair.getInteger() == origin || siPair.getInteger() == inlineLocal) {
            symbol = symbolTable.getSymbol(siPair.getString(), true);
        } else {
            symbol = topSymbolTable.getSymbol(siPair.getString(), false);
        }
        if (symbol == null) {
            errors.add(new UndefinedTokenException(ident.getLine()));
            MyAssert.ass(false);
            return -20231164;  // error  TODO: WARNING!!!: NOT EXIST IN SYMBOL TABLE
        }
        // TODO: check(if the symbol should be const or not)
        // TODO: 文法：全局变量声明中指定的初值表达式必须是常量表达式。
        MyAssert.ass(symbol.isConst(), "ConstExpCalculator只进行常量表达式的计算");
        if (lVal.missRBrack()) {
            errors.add(new MissRbrackException(ident.getLine()));
        }

        if (symbol.getSymbolType() == SymbolType.INT) {
            return symbol.getInitInt();
        } else if (symbol.getSymbolType() == SymbolType.ARRAY) {
            ArrayList<Exp> indexExp = lVal.getExps();
            ArrayList<Integer> place = new ArrayList<>();
            for (Exp exp : indexExp) {
                place.add(calcExp(exp));
            }
            return symbol.queryVal(place);
        } else {
            MyAssert.ass(false, "ConstExpCalculator只能计算LVal中的INT或ARRAY类型");
            return -20231164;
        }
    }
}
