package Parser.func.types;

import Config.IO;
import Lexer.Token;
import Parser.Output;
import Parser.expr.types.ConstExp;
import Symbol.Symbol;
import Symbol.SymbolType;
import Util.CalcUtil;

import java.util.ArrayList;

public class FuncFParam implements Output {
    // 函数形参 FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }] // 1.普通变量 2.一维数组变量 3.二维数组变量
    // BType -> int
    private final Token BType;
    private final Token ident;
    private final boolean isArray;
    private final ArrayList<ConstExp> constExps;
    private final ArrayList<Integer> dimNum = new ArrayList<>();
    private final ArrayList<Token> bracks; // error check: right could be null

    public FuncFParam(Token BType, Token ident, boolean isArray, ArrayList<ConstExp> constExps,
                      ArrayList<Token> bracks) {
        this.BType = BType;
        this.ident = ident;
        this.isArray = isArray;
        this.constExps = constExps;
        this.bracks = bracks;
        if (bracks.size() != 0) {  // bracks is 0, represents not an array
            dimNum.add(-2023);  // represent empty dim;
        }
        for (ConstExp constExp : constExps) {
            dimNum.add(CalcUtil.calcConstExp(constExp));
        }
    }

    // public Symbol toSymbol() {  // 第一维省略exp，因此检查bracks才不会出错
    //     return new Symbol(bracks.size() == 0 ? SymbolType.INT : SymbolType.ARRAY, bracks, dimNum, ident, false);
    // }

    public ArrayList<ConstExp> getDimExp() {
        return this.constExps;
    }

    public ArrayList<Integer> getDimNum() {
        return dimNum;
    }

    public ArrayList<Token> getBracks() {
        return bracks;
    }

    public boolean missRBrack() {
        for (Token token : bracks) {
            if (token == null) return true;
        }
        return false;
    }

    public Token getBType() {
        return BType;
    }

    public Token getIdent() {
        return ident;
    }

    public boolean isArray() {
        return isArray;
    }

    @Override
    public void output() {
        IO.print(BType.toString());
        IO.print(ident.toString());
        if (isArray) {
            int index = 0;
            IO.print(bracks.get(index++).toString());
            IO.print(bracks.get(index++).toString());
            for (ConstExp constExp : constExps) {
                IO.print(bracks.get(index++).toString());
                constExp.output();
                IO.print(bracks.get(index++).toString());
            }
        }
        IO.print("<FuncFParam>");
    }
}
