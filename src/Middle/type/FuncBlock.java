package Middle.type;

import Frontend.Symbol.Symbol;
import Frontend.Symbol.SymbolTable;

import java.util.ArrayList;

public class FuncBlock extends BlockNode{
    private final ReturnType returnType;
    private final String funcName;
    private final ArrayList<Symbol> params;
    private final SymbolTable funcSymbolTable;
    private final BasicBlock body;
    private final Boolean isMainFunc;

    public enum ReturnType {
        INT,
        VOID,
    }

    public FuncBlock(ReturnType returnType, String funcName, ArrayList<Symbol> params, SymbolTable funcSymbolTable,
                     BasicBlock body, Boolean isMainFunc) {
        this.returnType = returnType;
        this.funcName = funcName;
        this.params = params;
        this.funcSymbolTable = funcSymbolTable;
        this.body = body;
        this.isMainFunc = isMainFunc;
    }

    public ReturnType getReturnType() {
        return returnType;
    }

    public String getFuncName() {
        return funcName;
    }

    public ArrayList<Symbol> getParams() {
        return params;
    }

    public BasicBlock getBody() {
        return body;
    }

    public Boolean getMainFunc() {
        return isMainFunc;
    }

    public SymbolTable getFuncSymbolTable() {
        return funcSymbolTable;
    }

    public int getStackSize() {
        return this.funcSymbolTable.getStackSize();
    }
}
