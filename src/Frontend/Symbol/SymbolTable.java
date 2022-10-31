package Frontend.Symbol;

import java.util.HashMap;

public class SymbolTable {
    private final SymbolTable parent;  // top SymbolTable has no parent
    private final HashMap<String, Symbol> symbols = new HashMap<>();
    private int stackSize = 0;

    public SymbolTable(SymbolTable parent) {
        this.parent = parent;
    }

    public void addSymbol(Symbol symbol) {
        this.symbols.put(symbol.getIdent().getContent(), symbol);
        this.stackSize += symbol.getSize();
    }

    public SymbolTable getParent() {
        return parent;
    }

    public boolean contains(String string, boolean findParent) {
        if (symbols.containsKey(string)) {
            return true;
        } else if (parent != null && findParent) {
            return parent.contains(string, findParent);
        }
        return false;
    }

    public Symbol getSymbol(String string, boolean findParent) {
        if (symbols.containsKey(string)) {
            return symbols.get(string);
        } else if (parent != null && findParent) {
            return parent.getSymbol(string, findParent);
        }
        return null;
    }

    public int getStackSize() {
        SymbolTable curr = this;
        int size = 0;
        while (curr.parent != null){
            size += curr.stackSize;
            curr = curr.parent;
        }
        return size;
    }
}
