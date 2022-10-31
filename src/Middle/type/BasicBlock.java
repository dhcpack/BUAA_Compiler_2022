package Middle.type;

import Frontend.Symbol.SymbolType;

import java.util.ArrayList;

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

    public void addContent(BlockNode blockNode) {
        this.content.add(blockNode);
        if (blockNode instanceof Branch) {
            operandUsage.add(((Branch) blockNode).getCond());
        } else if (blockNode instanceof FourExpr) {
            operandUsage.add(((FourExpr) blockNode).getLeft());
            operandUsage.add(((FourExpr) blockNode).getRight());
        } else if (blockNode instanceof FuncBlock) {
            return;
        } else if (blockNode instanceof FuncCall) {
            operandUsage.addAll(((FuncCall) blockNode).getrParams());
        } else if (blockNode instanceof GetInt) {
            GetInt getInt = (GetInt) blockNode;
            if (getInt.getTarget().getSymbolType() == SymbolType.POINTER) {
                operandUsage.add(getInt.getTarget());
            }
        } else if (blockNode instanceof Jump) {
            return;
        } else if (blockNode instanceof Memory) {
            operandUsage.add(((Memory) blockNode).getOffset());
            operandUsage.add(((Memory) blockNode).getBase());
        } else if (blockNode instanceof Pointer) {
            Pointer pointer = (Pointer) blockNode;
            operandUsage.add(pointer.getPointer());
            if (pointer.getStore() != null) {
                operandUsage.add(pointer.getStore());
            }
        } else if (blockNode instanceof PrintInt) {
            operandUsage.add(((PrintInt) blockNode).getVal());
        } else if (blockNode instanceof PrintStr) {
            return;
        } else if (blockNode instanceof Return) {
            if (((Return) blockNode).hasReturnVal()) {
                operandUsage.add(((Return) blockNode).getReturnVal());
            }
        } else {
            assert false;
        }
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
        return this.label.toString();
    }

    @Override
    public int compareTo(BasicBlock o) {
        assert hasIndex;
        return this.index - o.index;
    }
}
