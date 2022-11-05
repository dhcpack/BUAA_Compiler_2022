package BackEnd;

import Frontend.Symbol.Symbol;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class Registers {
    public static final int zero = 0;
    public static final int at = 1;
    public static final int v0 = 2;
    public static final int v1 = 3;
    public static final int a0 = 4;
    public static final int gp = 28;
    public static final int sp = 29;
    public static final int fp = 30;
    public static final int ra = 31;

    private Integer[] availRegisters = {
            5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27
    };

    // TODO: 全局寄存器和临时寄存器
    private Integer[] globalRegisters = {
            5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16
    };

    private Integer[] localRegisters = {
            17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27
    };

    // free registers
    // private final HashSet<Integer> freeRegisters = new HashSet<>(Arrays.asList(availRegisters));
    private Queue<Integer> freeRegisters = new LinkedList<>(Arrays.asList(availRegisters));

    // allocated registers
    private final HashMap<Integer, Symbol> registerToSymbol = new HashMap<>();
    private final HashMap<Symbol, Integer> symbolToRegister = new HashMap<>();

    // LRU
    private final Queue<Integer> registerCache = new LinkedList<>();

    public boolean hasFreeRegister() {
        return !this.freeRegisters.isEmpty();
    }

    public int leastRecentlyUsed() {
        return registerCache.remove();
    }

    public int getFirstFreeRegister() {
        assert !this.freeRegisters.isEmpty();
        return this.freeRegisters.peek();
    }

    public int allocRegister(Symbol symbol) {
        if (symbolToRegister.containsKey(symbol)) {
            return symbolToRegister.get(symbol);
        }
        // LRU is in MIPS translator, so there must be free register
        int register = freeRegisters.remove();
        registerToSymbol.put(register, symbol);
        symbolToRegister.put(symbol, register);
        registerCache.add(register);
        return register;
    }

    public void freeRegister(Symbol symbol) {
        assert symbolToRegister.containsKey(symbol);
        int register = symbolToRegister.get(symbol);
        registerToSymbol.remove(register);
        symbolToRegister.remove(symbol);
        registerCache.remove(register);
        freeRegisters.add(register);
    }

    public void freeRegister(int register) {
        assert registerToSymbol.containsKey(register);
        Symbol symbol = registerToSymbol.get(register);
        registerToSymbol.remove(register);
        symbolToRegister.remove(symbol);
        registerCache.remove(register);
        freeRegisters.add(register);
    }

    public void clearRegister() {
        registerToSymbol.clear();
        symbolToRegister.clear();
        registerCache.clear();
        freeRegisters = new LinkedList<>(Arrays.asList(availRegisters));
    }

    public void refreshCache(int register) {
        if ((0 <= register && register <= 4) || (28 <= register && register <= 31)) {
            return;
        }
        assert registerCache.contains(register) : "更新的寄存器不在LRU序列中";
        registerCache.remove(register);
        registerCache.add(register);
    }

    public boolean isOccupied(int register) {
        return this.registerToSymbol.containsKey(register);
    }

    public boolean occupyingRegister(Symbol symbol) {
        return symbolToRegister.containsKey(symbol);
    }

    public int getSymbolRegister(Symbol symbol) {
        return this.symbolToRegister.get(symbol);
    }

    public Symbol getRegisterSymbol(int register) {
        return this.registerToSymbol.get(register);
    }

    public HashSet<Integer> getAllOccupiedRegister() {
        return new HashSet<>(registerToSymbol.keySet());
    }
}
