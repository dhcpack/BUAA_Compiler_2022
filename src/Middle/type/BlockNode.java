package Middle.type;

import Frontend.Symbol.Symbol;

import java.util.HashSet;

public abstract class BlockNode {
    private HashSet<Symbol> defSet = new HashSet<>();
    private HashSet<Symbol> useSet = new HashSet<>();

    private final HashSet<BlockNode> nextBlockNode = new HashSet<>();

    public HashSet<Symbol> getDefSet() {
        return defSet;
    }

    public HashSet<Symbol> getUseSet() {
        return useSet;
    }

    public void setDefSet(HashSet<Symbol> defSet) {
        this.defSet = defSet;
    }

    public void setUseSet(HashSet<Symbol> useSet) {
        this.useSet = useSet;
    }

    private BasicBlock belongBlock = null;

    public BasicBlock getBelongBlock(){
        return belongBlock;
    }

    public void setBelongBlock(BasicBlock block){
        this.belongBlock = block;
    }
}
