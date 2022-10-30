package BackEnd;

import BackEnd.instructions.ALUDouble;
import BackEnd.instructions.ALUSingle;
import BackEnd.instructions.ALUTriple;
import BackEnd.instructions.BranchInstr;
import BackEnd.instructions.Comment;
import BackEnd.instructions.Div;
import BackEnd.instructions.J;
import BackEnd.instructions.Jal;
import BackEnd.instructions.Jr;
import BackEnd.instructions.Label;
import BackEnd.instructions.MemoryInstr;
import BackEnd.instructions.Mfhi;
import BackEnd.instructions.Mflo;
import BackEnd.instructions.MoveInstr;
import BackEnd.instructions.Mult;
import BackEnd.instructions.Syscall;
import Config.SIPair;
import Frontend.Symbol.Symbol;
import Frontend.Symbol.SymbolType;
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
        // mipsCode.addInstr(new Label(funcBlock.getLabel()));
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

    // 记录所有（包括临时和局部和全局）变量的使用次数（表达式右端）
    private void addUsage(ArrayList<Operand> operands) {
        for (Operand operand : operands) {
            if (operand instanceof Symbol) {
                Symbol symbol = (Symbol) operand;
                if (defUseMap.containsKey(symbol)) {
                    defUseMap.put(symbol, defUseMap.get(symbol) + 1);
                } else {
                    defUseMap.put(symbol, 1);
                }
            }
        }
    }

    // 所有变量使用次数减一， 如果减到零则释放它占用的寄存器
    public void consumeUsage(Operand operand) {
        if (operand instanceof Symbol) {
            Symbol symbol = (Symbol) operand;
            assert defUseMap.containsKey(symbol);
            if (defUseMap.get(symbol) == 1) {
                defUseMap.remove(symbol);
                if (registers.occupyingRegister(symbol)) {
                    freeRegister(registers.getSymbolRegister(symbol), false);
                }
            } else {
                defUseMap.put(symbol, defUseMap.get(symbol) - 1);
            }
        }
    }

    // 分配一个空闲寄存器，如果没有空闲则根据LRU释放一个，如果正在占有寄存器则返回该寄存器，同时更新LRU
    public int allocRegister(Symbol symbol, boolean loadVal) {
        if (registers.occupyingRegister(symbol)) {
            int register = registers.getSymbolRegister(symbol);
            registers.refreshCache(register);
            return register;
        }
        if (!registers.hasFreeRegister()) {
            int lru = registers.leastRecentlyUsed();
            freeRegister(lru, true);  // 如果临时变量被LRU了需要保存
        }
        if (loadVal) {
            int target = registers.getFirstFreeRegister();
            loadSymbol(symbol, target);
            return target;
        } else {
            return registers.allocRegister(symbol);
        }
    }

    // 将symbol加载到target寄存器(从内存中取值)，这个函数可以指定寄存器
    public void loadSymbol(Symbol symbol, int target) {
        if (registers.occupyingRegister(symbol)) {
            int register = registers.getSymbolRegister(symbol);
            if (register == target) {
                return;
            }
            assert !registers.isOccupied(target);  // 寄存器没有被占用
            mipsCode.addInstr(new MoveInstr(registers.getSymbolRegister(symbol), Registers.a0));
        } else {
            assert !registers.isOccupied(target);  // 寄存器没有被占用
            if (symbol.getScope() == Symbol.Scope.GLOBAL) {
                mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.lw, Registers.gp, symbol.getAddress(), target));
            } else {
                assert symbol.hasAddress();  // could it be temp symbol? 即使是tempSymbol在saveSymbol时也会分配address
                mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.lw, Registers.sp, -symbol.getAddress(), target));
            }
        }
    }

    // 给临时变量分配地址
    public void allocateSpAddress(Symbol symbol) {
        currentStackSize += 4;
        symbol.setAddress(currentStackSize);
    }

    // 保存Symbol（只更新Symbol在内存中的数值，但是不释放寄存器）
    // boolean tempAllocate表示是否给temp变量分配内存保存数值
    public void saveSymbol(Symbol symbol, boolean saveTemp) {
        assert registers.occupyingRegister(symbol);
        int register = registers.getSymbolRegister(symbol);
        if (symbol.getScope() == Symbol.Scope.GLOBAL) {
            mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.sw, Registers.gp, symbol.getAddress(), register));
        } else if (symbol.getScope() == Symbol.Scope.LOCAL) {
            mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.sw, Registers.sp, -symbol.getAddress(), register));
        } else if (symbol.getScope() == Symbol.Scope.TEMP) {
            if (saveTemp) {
                if (!symbol.hasAddress()) {
                    allocateSpAddress(symbol);
                }
                mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.sw, Registers.sp, -symbol.getAddress(), register));
            }
        } else {
            assert false;
        }
    }

    // 保存Symbol+释放寄存器
    public void freeRegister(int register, boolean saveTemp) {
        // 释放寄存器，保存寄存器中的变量
        // save var
        if (registers.isOccupied(register)) {
            Symbol symbol = registers.getRegisterSymbol(register);
            // save symbol to memory
            saveSymbol(symbol, saveTemp);
            // free register
            registers.freeRegister(register);
        } else {
            System.err.printf("WARNING: register %d is not occupied\n", register);
        }
    }

    // 保存所有Symbol并释放所有寄存器
    public void freeAllRegisters(boolean save) {
        if (!save) {  // 不保存，直接暴力全部释放
            registers.clearRegister();
        } else {  // 保存+释放
            HashSet<Integer> occupied = registers.getAllOccupiedRegister();
            for (Integer register : occupied) {
                freeRegister(register, true);  // 临时变量也保存
            }
        }
    }

    private void translateBasicBlock(BasicBlock basicBlock) {
        defUseMap.clear();
        addUsage(basicBlock.getOperandUsage());
        mipsCode.addInstr(new Label(basicBlock.getLabel()));
        // pay attention to label
        for (BlockNode blockNode : basicBlock.getContent()) {
            mipsCode.addInstr(new Comment(blockNode.toString()));
            if (blockNode instanceof Branch) {
                translateBranch((Branch) blockNode);
            } else if (blockNode instanceof FourExpr) {
                translateFourExpr((FourExpr) blockNode);
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
        freeAllRegisters(true);  // TODO: is it necessary? check
        Operand cond = branch.getCond();
        if (cond instanceof Immediate) {
            mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v1, ((Immediate) cond).getNumber()));
        } else if (cond instanceof Symbol) {
            Symbol symbol = (Symbol) cond;
            loadSymbol(symbol, Registers.v1);
        }
        mipsCode.addInstr(
                new BranchInstr(BranchInstr.BranchType.bne, Registers.v1, Registers.zero, branch.getThenBlock().getLabel()));
        mipsCode.addInstr(new J(branch.getElseBlock().getLabel()));
        queue.add(branch.getThenBlock());
        queue.add(branch.getElseBlock());
        consumeUsage(cond);
    }

    private void translateFourExpr(FourExpr fourExpr) {
        FourExpr.ExprOp op = fourExpr.getOp();
        Operand left = fourExpr.getLeft();
        Symbol res = fourExpr.getRes();
        int resRegister = allocRegister(res, false);
        if (fourExpr.isSingle()) {
            if (op == FourExpr.ExprOp.DEF || op == FourExpr.ExprOp.ASS) {
                if (left instanceof Immediate) {
                    mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, resRegister, ((Immediate) left).getNumber()));
                } else if (left instanceof Symbol) {
                    int leftRegister = allocRegister((Symbol) left, true);
                    mipsCode.addInstr(new MoveInstr(leftRegister, resRegister));
                } else {
                    assert false;
                }
            } else if (op == FourExpr.ExprOp.NOT) {
                if (left instanceof Immediate) {
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.sne, resRegister, Registers.zero,
                            ((Immediate) left).getNumber()));
                } else if (left instanceof Symbol) {
                    int leftRegister = allocRegister((Symbol) left, true);
                    mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.sne, resRegister, Registers.zero, leftRegister));
                } else {
                    assert false;
                }
            } else if (op == FourExpr.ExprOp.NEG) {
                if (left instanceof Immediate) {
                    mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, resRegister, -((Immediate) left).getNumber()));
                } else if (left instanceof Symbol) {
                    int leftRegister = allocRegister((Symbol) left, true);
                    mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.addu, resRegister, Registers.zero, leftRegister));
                } else {
                    assert false;
                }
            } else {
                assert false;
            }
            consumeUsage(left);
        } else {
            Operand right = fourExpr.getRight();
            if (left instanceof Immediate && right instanceof Immediate) {
                int leftVal = ((Immediate) left).getNumber();
                int rightVal = ((Immediate) right).getNumber();
                if (op == FourExpr.ExprOp.ADD) {
                    mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, resRegister, leftVal + rightVal));
                } else if (op == FourExpr.ExprOp.SUB) {
                    mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, resRegister, leftVal - rightVal));
                } else if (op == FourExpr.ExprOp.MUL) {
                    mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, resRegister, leftVal * rightVal));
                } else if (op == FourExpr.ExprOp.DIV) {
                    mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, resRegister, leftVal / rightVal));
                } else if (op == FourExpr.ExprOp.MOD) {
                    mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, resRegister, leftVal % rightVal));
                } else if (op == FourExpr.ExprOp.GT) {
                    mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, resRegister, leftVal > rightVal ? 1 : 0));
                } else if (op == FourExpr.ExprOp.GE) {
                    mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, resRegister, leftVal >= rightVal ? 1 : 0));
                } else if (op == FourExpr.ExprOp.LT) {
                    mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, resRegister, leftVal < rightVal ? 1 : 0));
                } else if (op == FourExpr.ExprOp.LE) {
                    mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, resRegister, leftVal <= rightVal ? 1 : 0));
                } else if (op == FourExpr.ExprOp.EQ) {
                    mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, resRegister, leftVal == rightVal ? 1 : 0));
                } else if (op == FourExpr.ExprOp.NEQ) {
                    mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, resRegister, leftVal != rightVal ? 1 : 0));
                } else {
                    assert false;
                }
            } else if (left instanceof Symbol && right instanceof Immediate) {
                int leftRegister = allocRegister((Symbol) left, true);
                int rightVal = ((Immediate) right).getNumber();
                if (op == FourExpr.ExprOp.ADD) {
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.addiu, resRegister, leftRegister, rightVal));
                } else if (op == FourExpr.ExprOp.SUB) {
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.addiu, resRegister, leftRegister, -rightVal));
                } else if (op == FourExpr.ExprOp.MUL) {
                    mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v1, rightVal));
                    mipsCode.addInstr(new Mult(leftRegister, Registers.v1));
                    mipsCode.addInstr(new Mflo(resRegister));
                } else if (op == FourExpr.ExprOp.DIV) {
                    mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v1, rightVal));
                    mipsCode.addInstr(new Div(leftRegister, Registers.v1));
                    mipsCode.addInstr(new Mflo(resRegister));
                } else if (op == FourExpr.ExprOp.MOD) {
                    mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v1, rightVal));
                    mipsCode.addInstr(new Div(leftRegister, Registers.v1));
                    mipsCode.addInstr(new Mfhi(resRegister));
                } else if (op == FourExpr.ExprOp.GT) {
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.sgt, resRegister, leftRegister, rightVal));
                } else if (op == FourExpr.ExprOp.GE) {
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.sge, resRegister, leftRegister, rightVal));
                } else if (op == FourExpr.ExprOp.LT) {
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.slti, resRegister, leftRegister, rightVal));
                } else if (op == FourExpr.ExprOp.LE) {
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.sle, resRegister, leftRegister, rightVal));
                } else if (op == FourExpr.ExprOp.EQ) {
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.seq, resRegister, leftRegister, rightVal));
                } else if (op == FourExpr.ExprOp.NEQ) {
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.sne, resRegister, leftRegister, rightVal));
                } else {
                    assert false;
                }
            } else if (left instanceof Immediate && right instanceof Symbol) {
                int rightRegister = allocRegister((Symbol) right, true);
                int leftVal = ((Immediate) left).getNumber();
                if (op == FourExpr.ExprOp.ADD) {
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.addiu, resRegister, rightRegister, leftVal));
                } else if (op == FourExpr.ExprOp.SUB) {
                    mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.subu, Registers.v1, Registers.zero, rightRegister));
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.addiu, resRegister, Registers.v1, leftVal));
                } else if (op == FourExpr.ExprOp.MUL) {
                    mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v1, leftVal));
                    mipsCode.addInstr(new Mult(Registers.v1, rightRegister));
                    mipsCode.addInstr(new Mflo(resRegister));
                } else if (op == FourExpr.ExprOp.DIV) {
                    mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v1, leftVal));
                    mipsCode.addInstr(new Div(Registers.v1, rightRegister));
                    mipsCode.addInstr(new Mflo(resRegister));
                } else if (op == FourExpr.ExprOp.MOD) {
                    mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v1, leftVal));
                    mipsCode.addInstr(new Div(Registers.v1, rightRegister));
                    mipsCode.addInstr(new Mfhi(resRegister));
                } else if (op == FourExpr.ExprOp.GT) {
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.slti, resRegister, rightRegister, leftVal));
                } else if (op == FourExpr.ExprOp.GE) {
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.sle, resRegister, rightRegister, leftVal));
                } else if (op == FourExpr.ExprOp.LT) {
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.sgt, resRegister, rightRegister, leftVal));
                } else if (op == FourExpr.ExprOp.LE) {
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.sge, resRegister, rightRegister, leftVal));
                } else if (op == FourExpr.ExprOp.EQ) {
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.seq, resRegister, rightRegister, leftVal));
                } else if (op == FourExpr.ExprOp.NEQ) {
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.sne, resRegister, rightRegister, leftVal));
                } else {
                    assert false;
                }
            } else if (left instanceof Symbol && right instanceof Symbol) {
                int leftRegister = allocRegister((Symbol) left, true);
                int rightRegister = allocRegister((Symbol) right, true);
                if (op == FourExpr.ExprOp.ADD) {
                    mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.addu, resRegister, leftRegister, rightRegister));
                } else if (op == FourExpr.ExprOp.SUB) {
                    mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.subu, resRegister, leftRegister, rightRegister));
                } else if (op == FourExpr.ExprOp.MUL) {
                    mipsCode.addInstr(new Mult(leftRegister, rightRegister));
                    mipsCode.addInstr(new Mfhi(resRegister));
                } else if (op == FourExpr.ExprOp.DIV) {
                    mipsCode.addInstr(new Div(leftRegister, rightRegister));
                    mipsCode.addInstr(new Mflo(resRegister));
                } else if (op == FourExpr.ExprOp.MOD) {
                    mipsCode.addInstr(new Div(leftRegister, rightRegister));
                    mipsCode.addInstr(new Mfhi(resRegister));
                } else if (op == FourExpr.ExprOp.GT) {
                    mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.sgt, resRegister, leftRegister, rightRegister));
                } else if (op == FourExpr.ExprOp.GE) {
                    mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.sge, resRegister, leftRegister, rightRegister));
                } else if (op == FourExpr.ExprOp.LT) {
                    mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.slt, resRegister, leftRegister, rightRegister));
                } else if (op == FourExpr.ExprOp.LE) {
                    mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.sle, resRegister, leftRegister, rightRegister));
                } else if (op == FourExpr.ExprOp.EQ) {
                    mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.seq, resRegister, leftRegister, rightRegister));
                } else if (op == FourExpr.ExprOp.NEQ) {
                    mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.sne, resRegister, leftRegister, rightRegister));
                } else {
                    assert false;
                }
            } else {
                assert false;
            }
            consumeUsage(left);
            consumeUsage(right);
        }
    }

    private void translateFuncCall(FuncCall funcCall) {
        // 保存所有正在使用的寄存器
        freeAllRegisters(true);
        mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.sw, Registers.sp, 0, Registers.ra));
        // 找到子函数栈基地址
        mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.addiu, Registers.a0, Registers.sp, -currentStackSize - 4));
        // 设置参数
        // 被调用的函数的参数保存在函数栈指针的前几个位置
        int offset = 0;
        for (Operand param : funcCall.getrParams()) {
            offset -= 4;  // 函数从4开始放参数
            if (param instanceof Immediate) {
                mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v1, ((Immediate) param).getNumber()));
                mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.sw, Registers.a0, offset, Registers.v1));
            } else if (param instanceof Symbol) {
                loadSymbol((Symbol) param, Registers.v1);
                mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.sw, Registers.a0, offset, Registers.v1));
            } else {
                assert false;
            }
            consumeUsage(param);
        }
        mipsCode.addInstr(new MoveInstr(Registers.a0, Registers.sp));
        // freeAllRegisters(false);
        // 跳转
        mipsCode.addInstr(new Jal(funcCall.getTargetLabel()));
        // 恢复sp
        mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.addiu, Registers.sp, Registers.sp, currentStackSize + 4));
        mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.lw, Registers.sp, 0, Registers.ra));
        // 得到返回值
        if (funcCall.saveRet()) {
            Symbol ret = funcCall.getRet();
            int retRegister = allocRegister(ret, false);
            mipsCode.addInstr(new MoveInstr(Registers.v0, retRegister));
        }
    }

    private void translateGetInt(GetInt getInt) {
        mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v0, Syscall.get_int));
        mipsCode.addInstr(new Syscall());
        Symbol symbol = getInt.getTarget();
        if (symbol.getSymbolType() == SymbolType.INT) {
            int register = allocRegister(symbol, false);
            mipsCode.addInstr(new MoveInstr(Registers.v0, register));
        } else if (symbol.getSymbolType() == SymbolType.POINTER) {
            int pointer = registers.getSymbolRegister(symbol);
            mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.sw, pointer, 0, Registers.v0));
            consumeUsage(symbol);
        } else {
            assert false;
        }
    }

    private void translateJump(Jump jump) {
        freeAllRegisters(true);  // TODO: check
        mipsCode.addInstr(new J(jump.getTarget().getLabel()));
        queue.add(jump.getTarget());
    }

    private void translateMemory(Memory memory) {
        Symbol base = memory.getBase();
        assert base.getSymbolType() == SymbolType.ARRAY;  // 一定是数组
        Symbol res = memory.getRes();
        Operand offset = memory.getOffset();
        int resRegister = allocRegister(res, false);
        if (offset instanceof Immediate) {
            if (base.getScope() == Symbol.Scope.GLOBAL) {
                mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.addiu, resRegister, Registers.gp,
                        base.getAddress() + ((Immediate) offset).getNumber()));
            } else {
                mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.addiu, resRegister, Registers.sp,
                        -base.getAddress() + ((Immediate) offset).getNumber()));
            }
        } else if (offset instanceof Symbol) {
            if (base.getScope() == Symbol.Scope.GLOBAL) {
                mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.addiu, Registers.v1, Registers.gp, base.getAddress()));
            } else {
                mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.addiu, Registers.v1, Registers.sp, -base.getAddress()));
            }
            int offsetRegister = allocRegister((Symbol) offset, true);
            mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.addu, resRegister, Registers.v1, offsetRegister));
        } else {
            assert false;
        }
        consumeUsage(base);
        consumeUsage(offset);
    }

    private void translatePointer(Pointer pointer) {
        if (pointer.getOp() == Pointer.Op.LOAD) {
            Symbol point = pointer.getPointer();
            Symbol res = pointer.getLoad();
            int baseRegister = allocRegister(point, true);
            int targetRegister = allocRegister(res, false);
            mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.lw, baseRegister, 0, targetRegister));
            consumeUsage(point);
        } else if (pointer.getOp() == Pointer.Op.STORE) {
            Symbol point = pointer.getPointer();
            Operand store = pointer.getStore();
            int baseRegister = allocRegister(point, true);
            if (store instanceof Immediate) {
                mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v1, ((Immediate) store).getNumber()));
                mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.sw, baseRegister, 0, Registers.v1));
            } else if (store instanceof Symbol) {
                int storeRegister = allocRegister((Symbol) store, true);
                mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.sw, baseRegister, 0, storeRegister));
            } else {
                assert false;
            }
            consumeUsage(point);
            consumeUsage(store);
        } else {
            assert false;
        }
    }

    private void translatePrintInt(PrintInt printInt) {
        mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v0, Syscall.print_int));
        if (printInt.getVal() instanceof Immediate) {
            mipsCode.addInstr(
                    new ALUSingle(ALUSingle.ALUSingleType.li, Registers.a0, ((Immediate) printInt.getVal()).getNumber()));
        } else if (printInt.getVal() instanceof Symbol) {
            Symbol symbol = (Symbol) printInt.getVal();
            loadSymbol(symbol, Registers.a0);  // 把symbol加载到a0寄存器，如果symbol本身就在a0寄存器则直接返回
            // if (registers.occupyingRegister(symbol)) {
            //     mipsCode.addInstr(new MoveInstr(registers.getSymbolRegister(symbol), Registers.a0));
            // } else {
            //     loadSymbol(symbol, Registers.a0);
            // }
        }
        mipsCode.addInstr(new Syscall());
    }

    private void translatePrintStr(PrintStr printStr) {
        mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v0, Syscall.print_str));
        String label = printStr.getStrName();
        mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.la, Registers.a0, label));
        mipsCode.addInstr(new Syscall());
    }

    private void translateReturn(Return r) {
        if (currentFunc.isMainFunc()) {
            mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v0, Syscall.exit));
            mipsCode.addInstr(new Syscall());
            return;
        }
        if (r.hasReturnVal()) {
            Operand returnVal = r.getReturnVal();
            if (returnVal instanceof Immediate) {
                mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v0, ((Immediate) returnVal).getNumber()));
            } else {
                loadSymbol((Symbol) returnVal, Registers.v0);
            }
        }
        freeAllRegisters(true);
        mipsCode.addInstr(new Jr());
    }
}
