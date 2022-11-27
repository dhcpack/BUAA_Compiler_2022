package Middle.type;

import Frontend.Symbol.Symbol;
import Frontend.Symbol.SymbolTable;

import java.util.ArrayList;

public class FuncBlock extends BlockNode {
    private final ReturnType returnType;
    private final String funcName;
    private final ArrayList<Symbol> params;
    private final SymbolTable funcSymbolTable;
    private BasicBlock body;
    private final Boolean isMainFunc;

    // 递归函数，不能inline
    private boolean isRecursive = false;

    public void setRecursive() {
        this.isRecursive = true;
    }

    public boolean isRecursive() {
        return this.isRecursive;
    }

    public enum ReturnType {
        INT,
        VOID,
    }

    public FuncBlock(ReturnType returnType, String funcName, ArrayList<Symbol> params, SymbolTable funcSymbolTable,
                     Boolean isMainFunc) {
        this.returnType = returnType;
        this.funcName = funcName;
        this.params = params;
        this.funcSymbolTable = funcSymbolTable;
        this.body = null;  // not pass body
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

    public void setBody(BasicBlock body) {
        this.body = body;
    }

    public Boolean isMainFunc() {
        return isMainFunc;
    }

    public SymbolTable getFuncSymbolTable() {
        return funcSymbolTable;
    }

    public int getStackSize() {
        return this.funcSymbolTable.getStackSize();
    }

    public String getLabel() {
        return "FUNC_" + getFuncName();
    }
}
