package Frontend.Symbol;

import Frontend.Lexer.Token;
import Frontend.Parser.expr.types.LeafNode;

import java.util.ArrayList;

public class Symbol implements LeafNode {
    private String name;
    private SymbolType symbolType;
    private Token ident;
    private final boolean isConst;
    private int size;
    private int address;
    // int
    private int initInt;

    // 数组
    // 指针(省略第一维的数组) ???
    private final ArrayList<Integer> dimSize;
    private ArrayList<Integer> initArray;
    private final int dimCount;
    // private final ArrayList<Token> bracks;  // 数组维数（定义时的Const Exp，未经过化简计算）

    // 函数
    private final ArrayList<Symbol> params;
    private final SymbolType returnType;

    // boolean  // TODO:

    // int
    public Symbol(SymbolType symbolType, Token ident, Boolean isConst) {
        this.symbolType = symbolType;
        this.name = getIdent().getContent();
        this.ident = ident;
        this.isConst = isConst;
        this.dimSize = null;
        this.initArray = null;
        this.dimCount = 0;
        this.params = null;
        this.returnType = symbolType;
    }

    public Symbol(SymbolType symbolType, String name, Boolean isConst) {
        this.symbolType = symbolType;
        this.name = name;
        this.isConst = isConst;
        this.dimSize = null;
        this.initArray = null;
        this.dimCount = 0;
        this.params = null;
        this.returnType = symbolType;
    }

    // array or pointer
    public Symbol(SymbolType symbolType, Token ident, ArrayList<Integer> dimSize, int dimCount, Boolean isConst) {
        this.symbolType = symbolType;
        this.ident = ident;
        this.name = getIdent().getContent();
        this.isConst = isConst;
        this.initInt = -20231164;
        this.dimSize = dimSize;
        this.dimCount = dimCount;
        this.params = null;
        this.returnType = symbolType;
    }

    public Symbol(SymbolType symbolType, String name, ArrayList<Integer> dimSize, int dimCount, Boolean isConst) {
        this.symbolType = symbolType;
        this.name = name;
        this.isConst = isConst;
        this.initInt = -20231164;
        this.dimSize = dimSize;
        this.dimCount = dimCount;
        this.params = null;
        this.returnType = symbolType;
    }


    // function
    public Symbol(SymbolType symbolType, SymbolType returnType, ArrayList<Symbol> params, Token ident) {
        this.symbolType = symbolType;
        this.ident = ident;
        this.name = getIdent().getContent();
        this.isConst = true;
        this.initInt = -20231164;
        this.dimSize = null;
        this.initArray = null;
        this.dimCount = 0;
        this.params = params;
        this.returnType = returnType;
    }

    public Symbol(SymbolType symbolType, SymbolType returnType, ArrayList<Symbol> params, String name) {
        this.symbolType = symbolType;
        this.name = name;
        this.isConst = true;
        this.initInt = -20231164;
        this.dimSize = null;
        this.initArray = null;
        this.dimCount = 0;
        this.params = params;
        this.returnType = returnType;
    }

    public Token getIdent() {
        return ident;
    }

    public String getName() {
        return this.name;
    }

    public boolean isConst() {
        return isConst;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    // int
    public void setInitInt(int initInt) {
        this.initInt = initInt;
    }

    public int getInitInt() {
        return initInt;
    }


    // array and pointer
    // 维数
    public int getDimCount() {
        return this.dimCount;
    }

    // 每一维的大小
    public ArrayList<Integer> getDimSize() {
        return dimSize;
    }

    // 数组初始值
    public void setInitArray(ArrayList<Integer> initArray) {
        this.initArray = initArray;
    }

    // 数组初始值
    public ArrayList<Integer> getInitArray() {
        return initArray;
    }

    // 得到数组指定位置的初始值
    public int queryVal(ArrayList<Integer> place) {
        assert dimSize != null;
        assert place.size() == dimCount;
        int cw = 1, res = 0;
        for (int i = dimCount - 1; i >= 0; i--) {
            res += (cw * place.get(i));
            cw *= dimSize.get(i);
        }
        return initArray.get(res);
    }

    // function
    public boolean isFunc() {
        return symbolType == SymbolType.FUNCTION;
    }

    public ArrayList<Symbol> getParams() {
        return params;
    }

    public SymbolType getReturnType() {
        return returnType;
    }

    public int getSize() {
        if (symbolType == SymbolType.INT) {
            return 4;
        } else if (symbolType == SymbolType.POINTER) {
            return 4;
        } else if (symbolType == SymbolType.ARRAY) {
            assert dimSize != null;
            int res = 1;
            for (int d : dimSize) res *= d;
            return res;
        }
        assert false;
        return -20231164;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    private static int tempIntCount = 0;
    private static int tempPointerCount = 0;
    private static int tempBoolCount = 0;

    public static Symbol tempSymbol(SymbolType symbolType) {
        if (symbolType == SymbolType.INT) {
            return new Symbol(symbolType, "tmp_int_" + tempIntCount++, false);
        } else if (symbolType == SymbolType.POINTER) {
            return new Symbol(symbolType, "tmp_pointer_" + tempPointerCount++, false);
        } else if (symbolType == SymbolType.BOOL) {
            return new Symbol(symbolType, "tmp_bool_" + tempBoolCount++, false);
        }
        assert false;
        return null;
    }
}
