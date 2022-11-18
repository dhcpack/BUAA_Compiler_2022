package Middle.type;

import Frontend.Symbol.Symbol;

import java.util.HashSet;

public abstract class BlockNode {
    private final HashSet<Symbol> defSet = new HashSet<>();
    private final HashSet<Symbol> useSet = new HashSet<>();

    private final HashSet<BlockNode> nextBlockNode = new HashSet<>();

    public HashSet<Symbol> getDefSet() {
        return defSet;
    }

    public HashSet<Symbol> getUseSet() {
        return useSet;
    }
}
