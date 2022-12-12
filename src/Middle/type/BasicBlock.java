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
    private ArrayList<BlockNode> content = new ArrayList<>();
    private ArrayList<Operand> operandUsage = new ArrayList<>();

    public BasicBlock(String label) {
        this.label = label;
    }

    public BasicBlock(String label, int index) {
        this.label = label;
        this.index = index;
        this.hasIndex = true;
    }

    public HashSet<BlockNode> getFirstBlockNode() {
        HashSet<BlockNode> nextNodes = new HashSet<>();
        if (content.size() != 0) {
            nextNodes.add(this.content.get(0));
            return nextNodes;
        }
        if (nextBlock.size() == 0) {
            return nextNodes;
        } else {
            for (BasicBlock block : nextBlock) {
                nextNodes.addAll(block.getFirstBlockNode());
            }
        }
        return nextNodes;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        assert !hasIndex;
        this.index = index;
        this.hasIndex = true;
    }

    // 可以跳转到的基本块
    private final HashSet<BasicBlock> nextBlock = new HashSet<>();

    public void addContent(BlockNode blockNode) {
        this.content.add(blockNode);
        blockNode.setBelongBlock(this);
    }

    public void setContent(ArrayList<BlockNode> content) {
        this.content = content;
    }

    public void getOperandUsage() {
        this.operandUsage = new ArrayList<>();

        for (BlockNode blockNode : getContent()) {
            if (blockNode instanceof Branch) {
                // "Branch " + cond + " ? " + thenBlock + " : " + elseBlock;
                operandUsage.add(((Branch) blockNode).getCond());
                // add to next block
                nextBlock.add(((Branch) blockNode).getElseBlock());
                nextBlock.add(((Branch) blockNode).getThenBlock());
            } else if (blockNode instanceof FourExpr) {
                // this.op.name() + ", " + this.res + ", " + this.left + ", " + this.right;
                // this.op.name() + ", " + this.res + ", " + this.left;
                operandUsage.add(((FourExpr) blockNode).getLeft());
                operandUsage.add(((FourExpr) blockNode).getRight());
            } else if (blockNode instanceof FuncBlock) {
                assert false : "不会出现FuncBlock";
            } else if (blockNode instanceof FuncCall) {
                // "Call %s; Params: %s"
                operandUsage.addAll(((FuncCall) blockNode).getrParams());
            } else if (blockNode instanceof GetInt) {
                // "GETINT " + target;
                GetInt getInt = (GetInt) blockNode;
                if(getInt.isArray()){
                    operandUsage.add(getInt.getBase());
                    operandUsage.add(getInt.getOffset());
                } else {
                    operandUsage.add(getInt.getOffset());
                }
            } else if (blockNode instanceof Jump) {
                // add To nextBlock
                nextBlock.add(((Jump) blockNode).getTarget());
            } else if (blockNode instanceof Memory) {
                // "OFFSET (" + base + "+" + offset + ")->" + res;
                operandUsage.add(((Memory) blockNode).getOffset());
                operandUsage.add(((Memory) blockNode).getBase());
            } else if (blockNode instanceof Pointer) {
                Pointer pointer = (Pointer) blockNode;
                operandUsage.add(pointer.getBase());
                operandUsage.add(pointer.getOffset());
                if (pointer.getOp() == Pointer.Op.LOAD) {
                    // "LOAD " + pointer + ", " + load;
                } else if (pointer.getOp() == Pointer.Op.STORE) {
                    // "STORE " + pointer + ", " + store;
                    operandUsage.add(pointer.getStore());
                }
            } else if (blockNode instanceof PrintInt) {
                // "PRINT_INT " + val;
                operandUsage.add(((PrintInt) blockNode).getVal());
            } else if (blockNode instanceof PrintStr) {
                continue;
            } else if (blockNode instanceof Return) {
                // "RETURN " + returnVal;
                if (((Return) blockNode).hasReturnVal()) {
                    operandUsage.add(((Return) blockNode).getReturnVal());
                }
            } else {
                assert false;
            }
        }
    }

    // TODO: 记录临时变量被 ***使用*** 的次数
    public HashMap<Symbol, Integer> getBlockSymUsageMap() {
        getOperandUsage();
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
        if (!hasIndex) {
            assert false;
        }
        return this.index - o.index;
    }

    // jump 记录多少个跳转语句会跳到该基本块，如果只有一个j，就可以不保存临时变量和全局变量
    private int totalJumps = 0;

    public int getTotalJumps() {
        return totalJumps;
    }

    public void addJump(){
        this.totalJumps++;
    }
}
