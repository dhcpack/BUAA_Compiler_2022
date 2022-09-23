package Parser.func.types;

import Config.IO;
import Lexer.Token;
import Parser.Output;
import Parser.expr.types.ConstExp;

import java.util.ArrayList;

public class FuncFParam implements Output {
    // 函数形参 FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }] // 1.普通变量 2.一维数组变量 3.二维数组变量
    // BType -> int
    private final Token BType;
    private final Token ident;
    private final boolean isArray;
    private final ArrayList<ConstExp> constExps;
    private final ArrayList<Token> bracs;

    public FuncFParam(Token BType, Token ident, boolean isArray, ArrayList<ConstExp> constExps,
                      ArrayList<Token> bracs) {
        this.BType = BType;
        this.ident = ident;
        this.isArray = isArray;
        this.constExps = constExps;
        this.bracs = bracs;
    }

    public int getDims() {
        return this.bracs.size() / 2;
    }

    @Override
    public void output() {
        IO.print(BType.toString());
        IO.print(ident.toString());
        if (isArray) {
            int index = 0;
            IO.print(bracs.get(index++).toString());
            IO.print(bracs.get(index++).toString());
            for (ConstExp constExp : constExps) {
                IO.print(bracs.get(index++).toString());
                constExp.output();
                IO.print(bracs.get(index++).toString());
            }
        }
        IO.print("<FuncFParam>");
    }
}
