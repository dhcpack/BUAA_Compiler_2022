package Middle.type;

import Frontend.Symbol.Symbol;
import Frontend.Symbol.SymbolType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class BasicBlock implements Comparable<BasicBlock> {
    private int index;
    private boolean hasIndex = false;
    private final String label;
    private final ArrayList<BlockNode> content = new ArrayList<>();
    private final ArrayList<Operand> operandUsage = new ArrayList<>();

    public BasicBlock(String label) {
        this.label = label;
    }

    public BasicBlock(String label, int index) {
        this.label = label;
        this.index = index;
        this.hasIndex = true;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        assert !hasIndex;
        this.index = index;
        this.hasIndex = true;
    }

    // 定义或赋值先于使用
    private final HashSet<Symbol> defSet = new HashSet<>();
    // 使用先于定义
    private final HashSet<Symbol> useSet = new HashSet<>();
    // 可以跳转到的基本块
    private final HashSet<BasicBlock> nextBlock = new HashSet<>();

    // 记录全局变量（LOCAL变量）
    // DEF
    private void addToDefSet(Operand operand) {
        if (!(operand instanceof Symbol && ((Symbol) operand).getScope() == Symbol.Scope.LOCAL)) {
            return;
        }
        Symbol localSymbol = (Symbol) operand;
        if (useSet.contains(localSymbol)) {
            return;
        }
        defSet.add(localSymbol);
    }

    // USE
    private void addToUseSet(Operand operand) {
        if (!(operand instanceof Symbol && ((Symbol) operand).getScope() == Symbol.Scope.LOCAL)) {
            return;
        }
        Symbol localSymbol = (Symbol) operand;
        if (defSet.contains(localSymbol)) {
            return;
        }
        useSet.add(localSymbol);
    }

    public void addContent(BlockNode blockNode) {
        this.content.add(blockNode);
        if (blockNode instanceof Branch) {
            // "Branch " + cond + " ? " + thenBlock + " : " + elseBlock;
            operandUsage.add(((Branch) blockNode).getCond());
            addToUseSet(((Branch) blockNode).getCond());
            // add to next block
            nextBlock.add(((Branch) blockNode).getElseBlock());
            nextBlock.add(((Branch) blockNode).getThenBlock());
        } else if (blockNode instanceof FourExpr) {
            // this.op.name() + ", " + this.res + ", " + this.left + ", " + this.right;
            // this.op.name() + ", " + this.res + ", " + this.left;
            operandUsage.add(((FourExpr) blockNode).getLeft());
            operandUsage.add(((FourExpr) blockNode).getRight());
            addToUseSet(((FourExpr) blockNode).getLeft());
            addToUseSet(((FourExpr) blockNode).getRight());
            addToDefSet(((FourExpr) blockNode).getRes());
        } else if (blockNode instanceof FuncBlock) {
            assert false : "不会出现FuncBlock";
            return;
        } else if (blockNode instanceof FuncCall) {
            // "Call %s; Params: %s"
            operandUsage.addAll(((FuncCall) blockNode).getrParams());
            for (Operand operand : ((FuncCall) blockNode).getrParams()) {
                addToUseSet(operand);
            }
        } else if (blockNode instanceof GetInt) {
            // "GETINT " + target;
            GetInt getInt = (GetInt) blockNode;
            if (getInt.getTarget().getSymbolType() == SymbolType.POINTER) {  // TODO: CHECK!!!
                operandUsage.add(getInt.getTarget());
            }
            addToDefSet(getInt.getTarget());
        } else if (blockNode instanceof Jump) {
            // add To nextBlock
            nextBlock.add(((Jump) blockNode).getTarget());
            return;
        } else if (blockNode instanceof Memory) {
            // "OFFSET (" + base + "+" + offset + ")->" + res;
            operandUsage.add(((Memory) blockNode).getOffset());
            operandUsage.add(((Memory) blockNode).getBase());
            addToUseSet(((Memory) blockNode).getOffset());
            addToUseSet(((Memory) blockNode).getBase());
            addToDefSet(((Memory) blockNode).getRes());
        } else if (blockNode instanceof Pointer) {
            Pointer pointer = (Pointer) blockNode;
            operandUsage.add(pointer.getPointer());
            addToUseSet(pointer.getPointer());
            if (pointer.getOp() == Pointer.Op.LOAD) {
                // "LOAD " + pointer + ", " + load;
                addToDefSet(pointer.getLoad());
            } else if (pointer.getOp() == Pointer.Op.STORE) {
                // "STORE " + pointer + ", " + store;
                operandUsage.add(pointer.getStore());
                addToUseSet(pointer.getStore());
            }
        } else if (blockNode instanceof PrintInt) {
            // "PRINT_INT " + val;
            operandUsage.add(((PrintInt) blockNode).getVal());
            addToUseSet(((PrintInt) blockNode).getVal());
        } else if (blockNode instanceof PrintStr) {
            return;
        } else if (blockNode instanceof Return) {
            // "RETURN " + returnVal;
            if (((Return) blockNode).hasReturnVal()) {
                operandUsage.add(((Return) blockNode).getReturnVal());
                addToUseSet(((Return) blockNode).getReturnVal());
            }
        } else {
            assert false;
        }
    }

    // TODO: 记录临时变量被 ***使用*** 的次数
    public HashMap<Symbol, Integer> getSymbolUsageMap() {
        HashMap<Symbol, Integer> symbolUsageMap = new HashMap<>();
        for (Operand operand : operandUsage) {
            if (operand instanceof Symbol && ((Symbol) operand).getScope() == Symbol.Scope.TEMP) {
                Symbol symbol = (Symbol) operand;
                if (symbolUsageMap.containsKey(symbol)) {
                    symbolUsageMap.put(symbol, symbolUsageMap.get(symbol) + 1);
                } else {
                    symbolUsageMap.put(symbol, 1);
                }
            }
        }
        return symbolUsageMap;
    }

    // 定义或赋值先于使用
    public HashSet<Symbol> getDef() {
        return this.defSet;
    }

    // 使用先于定义
    public HashSet<Symbol> getUse() {
        return this.useSet;
    }

    // 可以跳转到的基本块
    public HashSet<BasicBlock> getNextBlock() {
        return this.nextBlock;
    }

    public ArrayList<Operand> getOperandUsage() {
        return operandUsage;
    }

    public BlockNode getFirst() {
        return this.content.get(0);
    }

    public ArrayList<BlockNode> getContent() {
        return this.content;
    }

    public String getLabel() {
        return label;
    }

    public BlockNode getLastContent() {
        if (content.size() == 0) {
            return null;
        } else {
            return this.content.get(this.content.size() - 1);
        }
    }

    @Override
    public String toString() {
        return this.label;
    }

    @Override
    public int compareTo(BasicBlock o) {
        assert hasIndex;
        return this.index - o.index;
    }
}
