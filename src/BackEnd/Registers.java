package BackEnd;

import Frontend.Symbol.Symbol;
import Middle.type.BasicBlock;
import Middle.type.BlockNode;
import Middle.type.Branch;
import Middle.type.FourExpr;
import Middle.type.FuncBlock;
import Middle.type.FuncCall;
import Middle.type.GetInt;
import Middle.type.Jump;
import Middle.type.Memory;
import Middle.type.Operand;
import Middle.type.Pointer;
import Middle.type.PrintInt;
import Middle.type.PrintStr;
import Middle.type.Return;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

    // 计算密集型
    public static final List<Integer> registersGroup1 = Arrays.asList(6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
            21, 22, 23, 24, 25, 26, 27);
    // 控制密集型
    public static final List<Integer> registersGroup2 = Arrays.asList(5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
    public static final List<Integer> registersGroup3 = Arrays.asList(17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27);


    // TODO: 全局寄存器(跨基本块)
    public static List<Integer> globalRegisters = Arrays.asList(5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
    // TODO: 临时寄存器(基本块内部)
    public static List<Integer> localRegisters = Arrays.asList(17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27);

    // free registers
    private final Queue<Integer> freeRegisters = new LinkedList<>(localRegisters);

    // allocated registers
    private final HashMap<Integer, Symbol> registerToSymbol = new HashMap<>();
    private final HashMap<Symbol, Integer> symbolToRegister = new HashMap<>();

    // TODO: CHANGE OPT: 将最长时间不会访问的Symbol移出

    public boolean hasFreeRegister() {
        return !this.freeRegisters.isEmpty();
    }

    public int getFirstFreeRegister() {
        assert !this.freeRegisters.isEmpty();
        return this.freeRegisters.peek();
    }

    public int allocRegister(Symbol symbol) {
        if (symbolToRegister.containsKey(symbol)) {
            return symbolToRegister.get(symbol);
        }
        // OPT is in MIPS translator, so there must be free register
        int register = freeRegisters.remove();
        registerToSymbol.put(register, symbol);
        symbolToRegister.put(symbol, register);
        System.out.printf("%s 4=> %d\n", symbol, register);
        return register;
    }

    public void freeRegister(int register) {
        if (!localRegisters.contains(register)) {
            return;
        }
        assert registerToSymbol.containsKey(register);
        Symbol symbol = registerToSymbol.get(register);
        registerToSymbol.remove(register);
        symbolToRegister.remove(symbol);
        freeRegisters.add(register);
    }

    public Symbol OPTStrategy(BasicBlock currentBasicBlock, int currentBlockNodeIndex) {
        assert registerToSymbol.size() == localRegisters.size() : "局部寄存器未满不需要使用OPT策略";
        int latestRegister = -1, latestPlace = -1;
        Symbol latestSymbol = null;
        ArrayList<BlockNode> blockNodes = currentBasicBlock.getContent();
        for (Map.Entry<Integer, Symbol> entry : registerToSymbol.entrySet()) {
            int currRegister = entry.getKey(), currPlace = blockNodes.size() + 1;
            Symbol currSymbol = entry.getValue();
            return currSymbol;
            // for (int i = currentBlockNodeIndex; i < blockNodes.size(); i++) {
            //     BlockNode blockNode = blockNodes.get(i);
            //     if (blockNode instanceof Branch) {
            //         // "Branch " + cond + " ? " + thenBlock + " : " + elseBlock;
            //         if (((Branch) blockNode).getCond() == currSymbol) {
            //             currPlace = i;
            //             break;
            //         }
            //     } else if (blockNode instanceof FourExpr) {
            //         // this.op.name() + ", " + this.res + ", " + this.left + ", " + this.right;
            //         // this.op.name() + ", " + this.res + ", " + this.left;
            //         if (((FourExpr) blockNode).getLeft() == currSymbol || ((FourExpr) blockNode).getRes() == currSymbol) {
            //             currPlace = i;
            //             break;
            //         }
            //         if (!((FourExpr) blockNode).isSingle() && ((FourExpr) blockNode).getRight() == currSymbol) {
            //             currPlace = i;
            //             break;
            //         }
            //     } else if (blockNode instanceof FuncBlock) {
            //         assert false : "不会出现FuncBlock";
            //         continue;
            //     } else if (blockNode instanceof FuncCall) {
            //         // "Call %s; Params: %s"
            //         for (Operand params : ((FuncCall) blockNode).getrParams()) {
            //             if (params == currSymbol) {
            //                 currPlace = i;
            //                 break;
            //             }
            //         }
            //     } else if (blockNode instanceof GetInt) {
            //         // "GETINT " + target;
            //         if (((GetInt) blockNode).getTarget() == currSymbol) {
            //             currPlace = i;
            //             break;
            //         }
            //     } else if (blockNode instanceof Jump) {
            //         // add To nextBlock
            //         continue;
            //     } else if (blockNode instanceof Memory) {
            //         // "OFFSET (" + base + "+" + offset + ")->" + res;
            //         if (((Memory) blockNode).getRes() == currSymbol || ((Memory) blockNode).getBase() == currSymbol || ((Memory) blockNode).getOffset() == currSymbol) {
            //             currPlace = i;
            //             break;
            //         }
            //     } else if (blockNode instanceof Pointer) {
            //         Pointer pointer = (Pointer) blockNode;
            //         if (pointer.getPointer() == currSymbol) {
            //             currPlace = i;
            //             break;
            //         }
            //         if (pointer.getOp() == Pointer.Op.LOAD && pointer.getLoad() == currSymbol) {
            //             currPlace = i;
            //             break;
            //         }
            //         if (pointer.getOp() == Pointer.Op.STORE && pointer.getStore() == currSymbol) {
            //             currPlace = i;
            //             break;
            //         }
            //     } else if (blockNode instanceof PrintInt) {
            //         // "PRINT_INT " + val;
            //         if (((PrintInt) blockNode).getVal() == currSymbol) {
            //             currPlace = i;
            //             break;
            //         }
            //     } else if (blockNode instanceof PrintStr) {
            //         continue;
            //     } else if (blockNode instanceof Return) {
            //         // "RETURN " + returnVal;
            //         if (((Return) blockNode).getReturnVal() == currSymbol) {
            //             currPlace = i;
            //             break;
            //         }
            //     } else {
            //         assert false;
            //     }
            // }
            // if (currPlace > latestPlace) {
            //     latestPlace = currPlace;
            //     latestRegister = currRegister;
            //     latestSymbol = currSymbol;
            // }
            // if (latestPlace == blockNodes.size() + 1) {
            //     break;
            // }
        }
        System.out.printf("OPT: Register%2d, Place%4d, Symbol(%s)\n", latestRegister, latestPlace, latestSymbol.getName());
        return latestSymbol;
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

    public HashMap<Integer, Symbol> getRegisterToSymbol() {
        return registerToSymbol;
    }

    public HashMap<Symbol, Integer> getSymbolToRegister() {
        return symbolToRegister;
    }

    // public void freeAllTempRegisters() {
    //     this.symbolToRegister.clear();
    //     this.registerToSymbol.clear();
    //     this.freeRegisters = new LinkedList<>(localRegisters);
    // }
}
