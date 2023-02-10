package Middle.type;

import Frontend.Symbol.Symbol;
import Frontend.Symbol.SymbolTable;
import Frontend.Symbol.SymbolType;

import java.util.ArrayList;
import java.util.HashMap;

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

    private ArrayList<BasicBlock> funcBlocks;

    public void setBlocks(ArrayList<BasicBlock> funcBlocks) {
        this.funcBlocks = funcBlocks;
    }

    private HashMap<Symbol, Integer> symbolUsageMap = null;

    public HashMap<Symbol, Integer> getSymbolUsageMap() {
        // System.out.println(this.funcName);
        if (symbolUsageMap == null) {
            symbolUsageMap = new HashMap<>();
            for (BasicBlock block : funcBlocks) {
                block.getBlockSymUsageMap().forEach((key, value) -> symbolUsageMap.merge(key, value, Integer::sum));
            }
            // System.out.printf("symbolUsage Map size = %d\n", symbolUsageMap.size());
        }
        return symbolUsageMap;
    }

    public void refreshBasicBlock(){
        symbolUsageMap = new HashMap<>();
        for (BasicBlock block : funcBlocks) {
            block.getBlockSymUsageMap().forEach((key, value) -> symbolUsageMap.merge(key, value, Integer::sum));
        }
    }

    public void refreshSymUsageMap(BlockNode blockNode) {
        if (symbolUsageMap == null) {
            getSymbolUsageMap();
        }
        blockNode.getBelongBlock().getContent().remove(blockNode);
        if (blockNode instanceof Branch) {
            // "Branch " + cond + " ? " + thenBlock + " : " + elseBlock;
            refresh(((Branch) blockNode).getCond());
        } else if (blockNode instanceof FourExpr) {
            // this.op.name() + ", " + this.res + ", " + this.left + ", " + this.right;
            // this.op.name() + ", " + this.res + ", " + this.left;
            refresh(((FourExpr) blockNode).getLeft());
            refresh(((FourExpr) blockNode).getRight());
        } else if (blockNode instanceof FuncBlock) {
            assert false : "不会出现FuncBlock";
            return;
        } else if (blockNode instanceof FuncCall) {
            // "Call %s; Params: %s"
            for (Operand operand : ((FuncCall) blockNode).getrParams()) {
                refresh(operand);
            }
        } else if (blockNode instanceof GetInt) {
            // "GETINT " + target;
            GetInt getInt = (GetInt) blockNode;
            if (getInt.getTarget().getSymbolType() == SymbolType.POINTER) {  // TODO: CHECK!!!
                refresh(getInt.getTarget());
            }
        } else if (blockNode instanceof Jump) {
            // add To nextBlock
            return;
        } else if (blockNode instanceof Memory) {
            // "OFFSET (" + base + "+" + offset + ")->" + res;
            refresh(((Memory) blockNode).getOffset());
            refresh(((Memory) blockNode).getBase());
        } else if (blockNode instanceof Pointer) {
            Pointer pointer = (Pointer) blockNode;
            refresh(pointer.getBase());
            if (pointer.getOp() == Pointer.Op.STORE) {
                // "STORE " + pointer + ", " + store;
                refresh(pointer.getStore());
            }
        } else if (blockNode instanceof PrintInt) {
            // "PRINT_INT " + val;
            refresh(((PrintInt) blockNode).getVal());
        } else if (blockNode instanceof PrintStr) {
            return;
        } else if (blockNode instanceof Return) {
            // "RETURN " + returnVal;
            if (((Return) blockNode).hasReturnVal()) {
                refresh(((Return) blockNode).getReturnVal());
            }
        } else {
            assert false;
        }
    }

    public void refresh(Operand operand) {
        if (operand instanceof Symbol && ((Symbol) operand).getScope() == Symbol.Scope.TEMP) {
            Symbol symbol = (Symbol) operand;
            // if(symbol.getName().equals("tmp_pointer_14")){
            //     System.out.println(1);
            // }
            if (symbolUsageMap.containsKey(symbol)) {
                // System.out.printf("\nSYMBOL %s: %d\n\n", symbol, symbolUsageMap.get(symbol));
                if (symbolUsageMap.get(symbol) == 1) {
                    symbolUsageMap.remove(symbol);
                    // System.err.printf("REMOVE TEMP SYMBOL %s\n", symbol);
                } else {
                    symbolUsageMap.put(symbol, symbolUsageMap.get(symbol) - 1);
                }
            } else {
                System.out.println("WARNING: TEMP SYMBOL NOT EXIST");
            }
        }
    }
}
