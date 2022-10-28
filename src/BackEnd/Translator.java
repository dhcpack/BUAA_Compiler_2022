package BackEnd;

import BackEnd.instructions.ALUDouble;
import BackEnd.instructions.ALUSingle;
import BackEnd.instructions.BranchInstr;
import BackEnd.instructions.Comment;
import BackEnd.instructions.J;
import BackEnd.instructions.MemoryInstr;
import BackEnd.instructions.Syscall;
import Config.SIPair;
import Frontend.Symbol.Symbol;
import Middle.MiddleCode;
import Middle.type.BasicBlock;
import Middle.type.BlockNode;
import Middle.type.Branch;
import Middle.type.FourExpr;
import Middle.type.FuncBlock;
import Middle.type.FuncCall;
import Middle.type.GetInt;
import Middle.type.Immediate;
import Middle.type.Jump;
import Middle.type.Memory;
import Middle.type.Operand;
import Middle.type.Pointer;
import Middle.type.PrintInt;
import Middle.type.PrintStr;
import Middle.type.Return;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class Translator {
    private final MiddleCode middleCode;
    private final MipsCode mipsCode = new MipsCode();
    private final Registers registers = new Registers();

    public Translator(MiddleCode middleCode) {
        this.middleCode = middleCode;
    }

    public MipsCode translate() {
        translateGlobals();
        for (FuncBlock funcBlock : middleCode.getNameToFunc().values()) {
            translateFunc(funcBlock);
        }
        return mipsCode;
    }

    private void translateGlobals() {
        PriorityQueue<SIPair> nameAddr = new PriorityQueue<>();
        for (Map.Entry<String, Integer> na : middleCode.getNameToAddr().entrySet()) {
            nameAddr.add(new SIPair(na.getKey(), na.getValue()));
        }
        HashMap<String, Integer> nameToVal = middleCode.getNameToVal();
        HashMap<String, ArrayList<Integer>> nameToArray = middleCode.getNameToArray();
        ArrayList<Integer> globalWords = new ArrayList<>();
        while (!nameAddr.isEmpty()) {
            SIPair pair = nameAddr.poll();
            if (nameToVal.containsKey(pair.getName())) {
                globalWords.add(nameToVal.get(pair.getName()));
            } else if (nameToArray.containsKey(pair.getName())) {
                globalWords.addAll(nameToArray.get(pair.getName()));
            } else {
                assert false;
            }
        }
        mipsCode.setGlobalWords(globalWords);
        mipsCode.setGlobalStrings(middleCode.getNameToAsciiz());
    }

    // BFS 基本块
    private final HashSet<BasicBlock> visited = new HashSet<>();
    private final Queue<BasicBlock> queue = new LinkedList<>();
    // 记录当前正在翻译的函数
    private FuncBlock currentFunc = null;
    private int currentStackSize = 0; // 当前正在翻译的函数已经用掉的栈的大小（局部变量+临时变量）
    private final HashMap<Symbol, Integer> defUseMap = new HashMap<>();

    private void translateFunc(FuncBlock funcBlock) {
        currentFunc = funcBlock;
        currentStackSize = funcBlock.getStackSize();
        BasicBlock body = funcBlock.getBody();
        queue.add(body);
        while (!queue.isEmpty()) {
            BasicBlock basicBlock = queue.poll();
            if (visited.contains(basicBlock)) {
                continue;
            }
            visited.add(basicBlock);
            translateBasicBlock(basicBlock);
        }
    }

    private void addUsage(ArrayList<Operand> operands) {
        for (Operand operand : operands) {
            if (operand instanceof Symbol && ((Symbol) operand).getScope() == Symbol.Scope.TEMP) {
                Symbol tempSymbol = (Symbol) operand;
                if (defUseMap.containsKey(tempSymbol)) {
                    defUseMap.put(tempSymbol, defUseMap.get(tempSymbol) + 1);
                } else {
                    defUseMap.put(tempSymbol, 1);
                }
            }
        }
    }

    public void consumeUsage(Operand operand) {
        if (operand instanceof Symbol && ((Symbol) operand).getScope() == Symbol.Scope.TEMP) {
            Symbol tempSymbol = (Symbol) operand;
            assert defUseMap.containsKey(tempSymbol);
            if (defUseMap.get(tempSymbol) == 1) {
                defUseMap.remove(tempSymbol);
                if (registers.occupyingRegister(tempSymbol)) {
                    registers.freeRegister(tempSymbol);
                }
            } else {
                defUseMap.put(tempSymbol, defUseMap.get(tempSymbol) - 1);
            }
        }
    }

    // 分配一个空闲寄存器，如果没有空闲则根据LRU释放一个
    public int allocRegister(Symbol symbol) {
        if (registers.occupyingRegister(symbol)) {
            int register = registers.getSymbolRegister(symbol);
            registers.refreshCache(register);
        }
        if (!registers.hasFreeRegister()) {
            int lru = registers.leastRecentlyUsed();
            freeRegister(lru);
        }
        return registers.allocRegister(symbol);
    }

    public void allocateSpAddress(Symbol symbol) {
        currentStackSize += 4;
        symbol.setAddress(currentStackSize);
    }

    // 保存Symbol
    public void saveSymbol(Symbol symbol) {
        assert registers.occupyingRegister(symbol);
        int register = registers.getSymbolRegister(symbol);
        if (symbol.getScope() == Symbol.Scope.GLOBAL) {
            mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.sw, Registers.gp, symbol.getAddress(), register));
        } else if (symbol.getScope() == Symbol.Scope.LOCAL) {
            mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.sw, Registers.sp, -symbol.getAddress(), register));
        } else if (symbol.getScope() == Symbol.Scope.TEMP) {
            if (!symbol.hasAddress()) {
                allocateSpAddress(symbol);
            }
            mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.sw, Registers.sp, -symbol.getAddress(), register));
        } else {
            assert false;
        }
    }

    // 保存+释放
    public void freeRegister(int register) {
        // 释放寄存器，保存寄存器中的变量
        // save var
        if (registers.isOccupied(register)) {
            Symbol symbol = registers.getRegisterSymbol(register);
            // save symbol to memory
            saveSymbol(symbol);
            // free register
            registers.freeRegister(register);
        } else {
            System.err.printf("WARNING: register %d is not occupied\n", register);
        }
    }

    public void freeAllRegisters(boolean save) {
        if (!save) {  // 不保存，直接暴力全部释放
            registers.clearRegister();
        } else {  // 保存+释放
            HashSet<Integer> occupied = registers.getAllOccupiedRegister();
            for (Integer register : occupied) {
                freeRegister(register);
            }
        }
    }

    private void translateBasicBlock(BasicBlock basicBlock) {
        defUseMap.clear();
        addUsage(basicBlock.getOperandUsage());
        // pay attention to label
        for (BlockNode blockNode : basicBlock.getContent()) {
            mipsCode.addInstr(new Comment(blockNode.toString()));
            if (blockNode instanceof Branch) {
                translateBranch((Branch) blockNode);
            } else if (blockNode instanceof FourExpr) {
                translateFourExpr((FourExpr) blockNode);
            } else if (blockNode instanceof FuncBlock) {
                translateFuncBlock((FuncBlock) blockNode);
            } else if (blockNode instanceof FuncCall) {
                translateFuncCall((FuncCall) blockNode);
            } else if (blockNode instanceof GetInt) {
                translateGetInt((GetInt) blockNode);
            } else if (blockNode instanceof Jump) {
                translateJump((Jump) blockNode);
            } else if (blockNode instanceof Memory) {
                translateMemory((Memory) blockNode);
            } else if (blockNode instanceof Pointer) {
                translatePointer((Pointer) blockNode);
            } else if (blockNode instanceof PrintInt) {
                translatePrintInt((PrintInt) blockNode);
            } else if (blockNode instanceof PrintStr) {
                translatePrintStr((PrintStr) blockNode);
            } else if (blockNode instanceof Return) {
                translateReturn((Return) blockNode);
            } else {
                assert false;
            }
        }
    }

    private void translateBranch(Branch branch) {
        freeAllRegisters(true);  // is it necessary?
        Operand cond = branch.getCond();
        int midRes = Registers.v1;
        if (cond instanceof Immediate) {
            mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, midRes, ((Immediate) cond).getNumber()));
        } else if (cond instanceof Symbol) {
            Symbol symbol = (Symbol) cond;
            if (registers.occupyingRegister(symbol)) {
                saveSymbol(symbol);  // 更新内存中的数值，但是不释放
            }
            if (symbol.getScope() == Symbol.Scope.GLOBAL) {
                mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.lw, Registers.gp, symbol.getAddress(), midRes));
            } else {
                assert symbol.hasAddress();  // could it be temp symbol?
                mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.lw, Registers.sp, -symbol.getAddress(), midRes));
            }
        }
        mipsCode.addInstr(new BranchInstr(BranchInstr.BranchType.bne, midRes, Registers.zero, branch.getThenBlock().getLabel()));
        mipsCode.addInstr(new J(branch.getElseBlock().getLabel()));
        queue.add(branch.getThenBlock());
        queue.add(branch.getElseBlock());
    }

    private void translateFourExpr(FourExpr fourExpr) {

    }

    private void translateFuncBlock(FuncBlock funcBlock) {

    }

    private void translateFuncCall(FuncCall funcCall) {

    }

    private void translateGetInt(GetInt getInt) {

    }

    private void translateJump(Jump jump) {
        mipsCode.addInstr(new J(jump.getTarget().getLabel()));
        queue.add(jump.getTarget());
    }

    private void translateMemory(Memory memory) {

    }

    private void translatePointer(Pointer pointer) {

    }

    private void translatePrintInt(PrintInt printInt) {
        mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.addiu, Registers.v0, Registers.zero, Syscall.print_int));
        if (printInt.getVal() instanceof Immediate) {
            mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.addiu, Registers.a0, Registers.zero,
                    ((Immediate) printInt.getVal()).getNumber()));
        } else if (printInt.getVal() instanceof Symbol) {

        }
    }

    private void translatePrintStr(PrintStr printStr) {

    }

    private void translateReturn(Return r) {

    }
}
