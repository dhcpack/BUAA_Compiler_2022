package Middle.type;

import Frontend.Symbol.Symbol;

import java.util.HashSet;
import java.util.Objects;

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

    // @Override
    // public boolean equals(Object o) {
    //     return false;
    // }
    //
    // @Override
    // public int hashCode() {
    //     return Objects.hash(nextBlockNode);
    // }
}
