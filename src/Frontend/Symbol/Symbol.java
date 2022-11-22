package Frontend.Symbol;

import Frontend.Lexer.Token;
import Frontend.Parser.expr.types.LeafNode;
import Middle.type.Operand;

import java.util.ArrayList;

public class Symbol implements LeafNode, Operand, Cloneable {
    public enum Scope {
        GLOBAL,
        LOCAL,
        TEMP,
        PARAM,  // 只记录函数形参中的array类型变量
    }

    // for all
    private final String name;
    private final SymbolType symbolType;
    private Token ident;
    private final boolean isConst;
    private Scope scope;
    private int size;
    private int address;
    private boolean hasAddress = false;

    // int
    private int constInitInt;
    private boolean hasConstInitInt = false;

    // 数组
    // 指针(省略第一维的数组) ???
    private final ArrayList<Integer> dimSize;
    private ArrayList<Integer> constInitArray;
    private boolean hasConstInitArray = false;
    private final int dimCount;

    // 函数
    private final ArrayList<Symbol> params;
    private final SymbolType returnType;

    // int
    public Symbol(SymbolType symbolType, Token ident, Boolean isConst, Scope scope) {
        this.symbolType = symbolType;
        this.name = ident.getContent();
        this.ident = ident;
        this.isConst = isConst;
        this.dimSize = null;
        this.constInitArray = null;
        this.dimCount = 0;
        this.params = null;
        this.returnType = symbolType;
        this.scope = scope;
    }

    public Symbol(SymbolType symbolType, String name, Boolean isConst, Scope scope) {
        this.symbolType = symbolType;
        this.name = name;
        this.isConst = isConst;
        this.dimSize = null;
        this.constInitArray = null;
        this.dimCount = 0;
        this.params = null;
        this.returnType = symbolType;
        this.scope = scope;
    }

    // array or pointer
    public Symbol(SymbolType symbolType, Token ident, ArrayList<Integer> dimSize, int dimCount, Boolean isConst,
                  Scope scope) {
        this.symbolType = symbolType;
        this.ident = ident;
        this.name = ident.getContent();
        this.isConst = isConst;
        this.constInitInt = -20231164;
        this.dimSize = dimSize;
        this.dimCount = dimCount;
        this.params = null;
        this.returnType = symbolType;
        this.scope = scope;
    }

    public Symbol(SymbolType symbolType, String name, ArrayList<Integer> dimSize, int dimCount, Boolean isConst,
                  Scope scope) {
        this.symbolType = symbolType;
        this.name = name;
        this.isConst = isConst;
        this.constInitInt = -20231164;
        this.dimSize = dimSize;
        this.dimCount = dimCount;
        this.params = null;
        this.returnType = symbolType;
        this.scope = scope;
    }

    // function
    public Symbol(SymbolType symbolType, SymbolType returnType, ArrayList<Symbol> params, Token ident) {
        this.symbolType = symbolType;
        this.ident = ident;
        this.name = ident.getContent();
        this.isConst = true;
        this.constInitInt = -20231164;
        this.dimSize = null;
        this.constInitArray = null;
        this.dimCount = 0;
        this.params = params;
        this.returnType = returnType;
        this.scope = Scope.GLOBAL;
    }

    public Symbol(SymbolType symbolType, SymbolType returnType, ArrayList<Symbol> params, String name) {
        this.symbolType = symbolType;
        this.name = name;
        this.isConst = true;
        this.constInitInt = -20231164;
        this.dimSize = null;
        this.constInitArray = null;
        this.dimCount = 0;
        this.params = params;
        this.returnType = returnType;
        this.scope = Scope.GLOBAL;
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
    public void setConstInitInt(int constInitInt) {
        this.constInitInt = constInitInt;
        this.hasConstInitInt = true;
    }

    public int getInitInt() {
        assert this.hasConstInitInt;
        return constInitInt;
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
    public void setConstInitArray(ArrayList<Integer> constInitArray) {
        this.constInitArray = constInitArray;
        this.hasConstInitArray = true;
    }

    // 数组初始值
    public ArrayList<Integer> getConstInitArray() {
        assert this.hasConstInitArray;
        return constInitArray;
    }

    // 得到数组指定位置的初始值
    public int queryVal(ArrayList<Integer> place) {
        assert dimSize != null;
        assert place.size() == dimCount;
        assert this.hasConstInitArray;
        int cw = 1, res = 0;
        for (int i = dimCount - 1; i >= 0; i--) {
            res += (cw * place.get(i));
            cw *= dimSize.get(i);
        }
        return constInitArray.get(res);
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
        if (scope == Scope.PARAM) {
            return 4;
        }
        if (symbolType == SymbolType.INT) {
            return 4;
        } else if (symbolType == SymbolType.POINTER) {
            return 4;
        } else if (symbolType == SymbolType.ARRAY) {
            assert dimSize != null;
            int res = 4;
            for (int d : dimSize) res *= d;
            return res;
        } else if (symbolType == SymbolType.FUNCTION) {  // TODO: CHECK 函数不占用栈空间
            return 0;
        }
        assert false;
        return -20231164;
    }

    public int getAddress() {
        assert hasAddress;
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
        this.hasAddress = true;
    }

    public boolean hasAddress() {
        return this.hasAddress;
    }

    private static int tempIntCount = 0;
    private static int tempPointerCount = 0;

    public static Symbol tempSymbol(SymbolType symbolType) {
        if (symbolType == SymbolType.INT) {
            return new Symbol(symbolType, "tmp_int_" + tempIntCount++, false, Scope.TEMP);
        } else if (symbolType == SymbolType.POINTER) {
            return new Symbol(symbolType, "tmp_pointer_" + tempPointerCount++, false, Scope.TEMP);
        }
        assert false;
        return null;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    @Override
    public String toString() {
        String addr;
        if (scope == Scope.GLOBAL) {
            addr = String.format("[data+0x%x]", this.address);
        } else if (scope == Scope.LOCAL || scope == Scope.PARAM) {
            addr = String.format("[sp-0x%x]", this.address);
        } else {
            if (hasAddress) {
                addr = String.format("[sp-0x%x]", this.address);
            } else {
                addr = "[temp]";
            }
        }
        return String.format("%s(%s%s)", this.symbolType, this.name, addr);
    }

    @Override
    public Symbol clone() {
        try {
            // Symbol clone = (Symbol) super.clone();
            // // TODO: copy mutable state here, so the clone can't change the internals of the original
            // return clone;
            return (Symbol) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
