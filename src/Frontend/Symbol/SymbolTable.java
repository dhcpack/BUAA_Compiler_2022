package Frontend.Symbol;

import Frontend.SymbolTableBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Collectors;

public class SymbolTable {
    private final SymbolTable parent;  // top SymbolTable has no parent
    private final ArrayList<SymbolTable> childSymbolTables = new ArrayList<>();
    private final HashMap<String, Symbol> symbols = new HashMap<>();
    private final SymbolTable funcSymbolTable;  // 当前符号表所属的funcSymbolTable
    private int stackSize = 0;

    public SymbolTable(SymbolTable parent, SymbolTable funcSymbolTable) {
        this.parent = parent;
        if (this.parent != null) {
            this.parent.addChildSymbolTable(this);
        }
        this.funcSymbolTable = funcSymbolTable;
        assert this.parent == null || this.parent.parent == null
                || this.funcSymbolTable != null : "要么是最顶层符号表，要么是函数符号表，要么拥有属于的函数符号表";
    }

    public void addChildSymbolTable(SymbolTable symbolTable) {
        this.childSymbolTables.add(symbolTable);
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

    public Symbol getSymbol(String string, boolean findParent, SymbolTable funcSymbolTable, boolean inlining) {
        if (inlining) {
            return mapper.get(funcSymbolTable.getNormalSymbol(string, findParent));
        } else {
            return this.getNormalSymbol(string, findParent);
        }
    }

    public Symbol getNormalSymbol(String string, boolean findParent) {
        if (symbols.containsKey(string)) {
            return symbols.get(string);
        } else if (parent != null && findParent) {
            return parent.getNormalSymbol(string, findParent);
        }
        return null;
    }

    public HashSet<Symbol> getAllSymbols() {
        return new HashSet<>(this.symbols.values());
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

    public ArrayList<SymbolTable> getChildSymbolTables() {
        return this.childSymbolTables;
    }

    private final HashMap<Symbol, Symbol> mapper = new HashMap<>();
    // private final HashSet<SymbolTable> mapped = new HashSet<>();

    public HashMap<Symbol, Symbol> copySymbolTable(SymbolTable symbolTable) {
        Queue<SymbolTable> queue = new LinkedList<>();
        queue.add(symbolTable);
        while (!queue.isEmpty()) {
            SymbolTable curr = queue.remove();
            // if (mapped.contains(curr)) continue;
            // mapped.add(curr);
            HashSet<Symbol> symbols = curr.getAllSymbols();
            for (Symbol symbol : symbols) {
                Symbol newSymbol = symbol.clone();
                mapper.put(symbol, newSymbol);
                this.addSymbol(newSymbol);
                if (this.funcSymbolTable != null) {
                    newSymbol.setAddress(funcSymbolTable.getStackSize());
                } else if (this.parent.parent == null) {
                    newSymbol.setAddress(this.getStackSize());
                } else {
                    assert false : "这里必须是函数符号表";
                }
            }
            queue.addAll(curr.getChildSymbolTables());
        }
        return mapper;
    }
}
