package Frontend.Parser.func.types;

import Frontend.Lexer.Token;
import Frontend.Parser.expr.types.ConstExp;

import java.util.ArrayList;

public class FuncFParam {
    // 函数形参 FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }] // 1.普通变量 2.一维数组变量 3.二维数组变量
    // BType -> int
    private final Token BType;
    private final Token ident;
    private final boolean isArray;
    private final ArrayList<ConstExp> constExps;
    private ArrayList<Integer> dimSize;
    private final ArrayList<Token> bracks; // error check: right could be null

    public FuncFParam(Token BType, Token ident, boolean isArray, ArrayList<ConstExp> constExps,
                      ArrayList<Token> bracks) {
        this.BType = BType;
        this.ident = ident;
        this.isArray = isArray;
        this.constExps = constExps;
        this.bracks = bracks;
        // if (bracks.size() != 0) {  // bracks is 0, represents not an array
        //     dimSize.add(-2023);  // represent empty dim;  第一维省略
        // }
        // for (ConstExp constExp : constExps) {
        //     dimNum.add(ConstExpCalculator.calcConstExp(constExp));
        // }
    }

    // public ArrayList<Integer> getDimSize() {
    //     return dimSize;
    // }
    //
    // public void setDimSize(ArrayList<Integer> dimSize) {
    //     this.dimSize = dimSize;
    // }

    // public Frontend.Symbol toSymbol() {  // 第一维省略exp，因此检查bracks才不会出错
    //     return new Frontend.Symbol(bracks.size() == 0 ? SymbolType.INT : SymbolType.ARRAY, bracks, dimNum, ident, false);
    // }

    public ArrayList<ConstExp> getDimExp() {
        return this.constExps;
    }

    // public ArrayList<Integer> getDimNum() {
    //     return dimNum;
    // }

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

    public ArrayList<ConstExp> getConstExps() {
        return constExps;
    }

    public ArrayList<Integer> getDimSize() {
        return dimSize;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(BType).append(ident);
        if (isArray) {
            int index = 0;
            stringBuilder.append(bracks.get(index++)).append(bracks.get(index++));
            for (ConstExp constExp : constExps) {
                stringBuilder.append(bracks.get(index++)).append(constExp).append(bracks.get(index++));
            }
        }
        stringBuilder.append("<FuncFParam>\n");
        return stringBuilder.toString();
    }
}
