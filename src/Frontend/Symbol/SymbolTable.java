package Frontend.Symbol;

import java.util.HashMap;

public class SymbolTable {
    private final SymbolTable parent;  // top SymbolTable has no parent
    private final HashMap<String, Symbol> symbols = new HashMap<>();
    private final SymbolTable funcSymbolTable;  // 当前符号表所属的funcSymbolTable
    private int stackSize = 0;

    public SymbolTable(SymbolTable parent, SymbolTable funcSymbolTable) {
        this.parent = parent;
        this.funcSymbolTable = funcSymbolTable;
        assert this.parent == null || this.parent.parent == null
                || this.funcSymbolTable != null : "要么是最顶层符号表，要么是函数符号表，要么拥有属于的函数符号表";
    }

    public void addSymbol(Symbol symbol) {
        this.symbols.put(symbol.getIdent().getContent(), symbol);
        this.stackSize += symbol.getSize();
        if (this.funcSymbolTable != null) {
            this.funcSymbolTable.addSize(symbol.getSize());  // 在函数符号表中申请空间
        }
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

    public void addSize(int stackSize) {  // 在While块等符号表中添加符号时要向函数符号表+size
        this.stackSize += stackSize;
    }

    public int getStackSize() {
        if (this.funcSymbolTable != null) {  // 如果当前符号表属于某一个函数符号表，则返回函数符号表的StackSize
            return this.funcSymbolTable.getStackSize();  // 如while，if，block等块生成的新符号表
        } else {
            return this.stackSize;
        }
    }
}
