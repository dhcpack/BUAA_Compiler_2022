package BackEnd;

import BackEnd.instructions.ALUDouble;
import BackEnd.instructions.ALUSingle;
import BackEnd.instructions.ALUTriple;
import BackEnd.instructions.BranchInstr;
import BackEnd.instructions.Comment;
import BackEnd.instructions.Div;
import BackEnd.instructions.Instruction;
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
import BackEnd.optimizer.ConflictGraph;
import Config.Config;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

public class Translator {
    private final MiddleCode middleCode;
    private final MipsCode mipsCode = new MipsCode();
    private Registers tempRegisters = new Registers();
    private HashMap<Symbol, Integer> symbolUsageMap;

    // 函数内联
    private LinkedHashMap<FuncBlock, ArrayList<BasicBlock>> funcToSortedBlock = null;
    private boolean translatingInLineFunction = false;
    private int inlineFuncIndex = 1;
    private String inlineFuncEnd = null;

    // 当前函数和当前函数栈空间
    private FuncBlock currentFunc = null;
    private int currentStackSize = 0;

    // 当前冲突图
    private ConflictGraph currentConflictGraph;

    // 当前基本块和当前指令位置
    private BasicBlock currentBasicBlock;
    private int currentBlockNodeIndex;

    public Translator(MiddleCode middleCode) {
        this.middleCode = middleCode;
    }

    private void translateGlobals() {
        PriorityQueue<SIPair> nameAddr = new PriorityQueue<>();
        for (Map.Entry<String, Integer> na : middleCode.getNameToAddr().entrySet()) {
            nameAddr.add(new SIPair(na.getKey(), na.getValue()));
        }
        LinkedHashMap<String, Integer> nameToVal = middleCode.getNameToVal();
        LinkedHashMap<String, ArrayList<Integer>> nameToArray = middleCode.getNameToArray();
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

    public MipsCode translate() {
        translateGlobals();
        translateFuncs();
        return mipsCode;
    }

    private void translateFuncs() {
        this.funcToSortedBlock = middleCode.getFuncToSortedBlock();
        for (Map.Entry<FuncBlock, ArrayList<BasicBlock>> funcAndBlock : funcToSortedBlock.entrySet()) {
            currentFunc = funcAndBlock.getKey();
            if (!(currentFunc.isRecursive() || currentFunc.isMainFunc())) {
                continue;
            }
            currentStackSize = currentFunc.getStackSize();
            ArrayList<Symbol> params = currentFunc.getParams();
            currentConflictGraph = new ConflictGraph(currentFunc.getFuncName(), funcAndBlock.getValue(), params);
            tempRegisters = new Registers();
            ArrayList<BasicBlock> funcBlocks = funcAndBlock.getValue();
            for (int i = 0; i < funcBlocks.size(); i++) {
                if (i == 0) {
                    translateBasicBlock(funcBlocks.get(0), params);
                } else {
                    translateBasicBlock(funcBlocks.get(i), null);
                }
            }
            mipsCode.addInstr(new Comment(""));
        }
    }

    // TODO: +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // TODO: ABOUT REGISTERS!!!!
    // TODO: 临时变量的使用次数减一， 如果减到零则释放它占用的寄存器
    public void consumeUsage(Operand operand) {
        if (operand instanceof Symbol && ((Symbol) operand).getScope() == Symbol.Scope.TEMP) {
            Symbol symbol = (Symbol) operand;
            assert symbolUsageMap.containsKey(symbol);
            if (symbolUsageMap.get(symbol) == 1) {
                symbolUsageMap.remove(symbol);
                if (tempRegisters.occupyingRegister(symbol)) {
                    int register = tempRegisters.getSymbolRegister(symbol);
                    freeSymbolRegister(symbol, false);  // 释放占用的寄存器，不必保存Symbol
                    System.err.printf("FREE TEMP SYMBOL(%s), REGISTER(%d)\n", symbol.getName(), register);
                }
            } else {
                symbolUsageMap.put(symbol, symbolUsageMap.get(symbol) - 1);
            }
        }
    }

    // TODO: ABOUT REGISTERS!!!!
    // TODO: CHANGE TO OPT  11.18
    // 分配一个空闲寄存器，如果没有空闲则根据LRU释放一个，如果正在占有寄存器则返回该寄存器，同时更新LRU
    // TODO: 此函数的功能是为Symbol分配一个寄存器，操作数Symbol置loadVal = True会从内存中加载数据
    // Symbol已经在寄存器中会直接返回Symbol占用的寄存器
    // TODO: 首先尝试分配全局寄存器，分配失败(ConflictGraph.NO_GLOBAL)后分配局部寄存器
    private final static int loadGlobalRegister = 0b01;
    private final static int loadTempRegister = 0b10;
    private final static int noLoad = 0;

    public int allocRegister(Symbol symbol, int mode) {
        if (currentConflictGraph.occupyingGlobalRegister(symbol)) {
            // 检查Symbol是否占用global register
            return currentConflictGraph.getSymbolRegister(symbol);
        }
        if (currentConflictGraph.hasGlobalRegister(symbol)) {
            int register = currentConflictGraph.allocGlobalRegister(symbol, tempRegisters);
            if ((mode & loadGlobalRegister) != 0) {  // 如果是函数参数
                mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.lw, Registers.sp, -symbol.getAddress(), register));
            }
            return register;
        }
        if (tempRegisters.occupyingRegister(symbol)) {
            // 检查Symbol是否占用local register
            return tempRegisters.getSymbolRegister(symbol);
        }
        // 为该变量分配临时寄存器
        if (!tempRegisters.hasFreeRegister()) {
            // TODO: OPT
            System.out.println("Call OPT");
            Symbol optSymbol = tempRegisters.OPTStrategy(currentBasicBlock, currentBlockNodeIndex);
            freeSymbolRegister(optSymbol, true);
        }
        int register = tempRegisters.getFirstFreeRegister();
        if ((mode & loadTempRegister) != 0) {
            loadSymbol(symbol, register);  // loadRegister要求先不占用该寄存器
        }
        if (symbol.getScope() == Symbol.Scope.LOCAL || symbol.getScope() == Symbol.Scope.PARAM) {
            currentConflictGraph.settleOverflowSymbol(symbol, register);
        }
        return tempRegisters.allocRegister(symbol);
    }

    // TODO: ABOUT REGISTERS!!!!
    // TODO: WARNING!!! loadSymbol函数可能会造成某个符号占用两个寄存器的情况，导致Registers出错
    // TODO: 此函数的功能是直接将某个Symbol加载到特定的寄存器（如果某个Symbol正在占用寄存器，则使用move将其移动到target寄存器）
    // TODO: 目前loadSymbol主要用于特殊寄存器，allocRegister调用时也会保证symbol没有占用寄存器
    public void loadSymbol(Symbol symbol, int target) {
        // 检查全局寄存器
        if (currentConflictGraph.occupyingGlobalRegister(symbol)) {
            int register = currentConflictGraph.getSymbolRegister(symbol);
            if (register != target) {
                mipsCode.addInstr(new MoveInstr(register, target));
            }
            return;
        }
        // 检查临时寄存器
        if (tempRegisters.occupyingRegister(symbol)) {
            int register = tempRegisters.getSymbolRegister(symbol);
            if (register != target) {
                mipsCode.addInstr(new MoveInstr(register, target));
            }
            return;
        }
        // load Val
        if (symbol.getScope() == Symbol.Scope.GLOBAL) {
            mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.lw, Registers.gp, symbol.getAddress(), target));
        } else {
            assert symbol.hasAddress();  // temp Symbol要么占有寄存器，要么具有地址
            mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.lw, Registers.sp, -symbol.getAddress(), target));
        }
    }

    // TODO: ABOUT REGISTERS!!!!
    // 给临时变量分配地址
    public void allocateSpAddress(Symbol symbol) {
        currentStackSize += 4;
        symbol.setAddress(currentStackSize);
    }

    // TODO: ABOUT REGISTERS!!!!
    // TODO: 保存Symbol并释放寄存器
    public void freeSymbolRegister(Symbol symbol, boolean save) {
        if (symbol.getScope() == Symbol.Scope.LOCAL || symbol.getScope() == Symbol.Scope.PARAM) {
            if (currentConflictGraph.hasGlobalRegister(symbol)) {
                return;
            } else {
                currentConflictGraph.freeOverflowSymbol(symbol);  // 在冲突图中删除，并在下面做可能的保存
            }
        }
        int register = 0;
        if (tempRegisters.occupyingRegister(symbol)) {
            register = tempRegisters.getSymbolRegister(symbol);
            tempRegisters.freeRegister(register);
        } else {
            assert false : "Symbol没有占用寄存器";
        }
        if (save) {
            if (symbol.getScope() == Symbol.Scope.GLOBAL) {
                mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.sw, Registers.gp, symbol.getAddress(), register));
            } else if (symbol.getScope() == Symbol.Scope.LOCAL || symbol.getScope() == Symbol.Scope.PARAM) {
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
    }

    // TODO: ABOUT REGISTERS!!!!
    // TODO: 保存所有Symbol并释放所有寄存器
    private static final int FREE_GLOBAL = 0b0001;
    private static final int FREE_LOCAL = 0b0010;
    private static final int FREE_PARAM = 0b0100;
    private static final int FREE_TEMP = 0b1000;

    public void freeAllRegisters(int type, boolean save) {
        HashSet<Symbol> symbols = new HashSet<>(tempRegisters.getSymbolToRegister().keySet());
        for (Symbol symbol : symbols) {
            if ((type & FREE_GLOBAL) != 0 && symbol.getScope() == Symbol.Scope.GLOBAL) {
                freeSymbolRegister(symbol, save);
            } else if ((type & FREE_LOCAL) != 0 && symbol.getScope() == Symbol.Scope.LOCAL && currentConflictGraph.hasGlobalRegister(
                    symbol)) {
                freeSymbolRegister(symbol, save);
            } else if ((type & FREE_PARAM) != 0 && symbol.getScope() == Symbol.Scope.PARAM && currentConflictGraph.hasGlobalRegister(
                    symbol)) {
                freeSymbolRegister(symbol, save);
            } else if ((type & FREE_TEMP) != 0 && (symbol.getScope() == Symbol.Scope.TEMP || ((symbol.getScope() == Symbol.Scope.LOCAL || symbol.getScope() == Symbol.Scope.PARAM) && !currentConflictGraph.hasGlobalRegister(
                    symbol)))) {
                freeSymbolRegister(symbol, save);
            }
        }
        if ((type & FREE_LOCAL) != 0 && (type & FREE_PARAM) != 0) {  // FREE_LOCAL和FREE_PARAM会同时出现
            currentConflictGraph.freeAllGlobalRegisters(tempRegisters, mipsCode,
                    currentBasicBlock.getContent().get(currentBlockNodeIndex));
        }
    }
    // TODO: +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    private void translateBasicBlock(BasicBlock basicBlock, ArrayList<Symbol> params) {
        // 更新当前基本块的SymbolUsageMap
        currentBasicBlock = basicBlock;
        symbolUsageMap = basicBlock.getSymbolUsageMap();
        mipsCode.addInstr(new Label(basicBlock.getLabel()));
        // pay attention to label
        if (params != null) {
            for (Symbol param : params) {
                allocRegister(param, loadGlobalRegister | loadTempRegister);
            }
        }
        ArrayList<BlockNode> blockNodes = basicBlock.getContent();
        for (int i = 0; i < blockNodes.size(); i++) {
            BlockNode blockNode = blockNodes.get(i);
            currentBlockNodeIndex = i;
            // TODO: 死代码删除
            // TODO: 1121
            if (blockNode instanceof FourExpr) {  // 四元式的计算结果不活跃
                if (!currentConflictGraph.checkActive(((FourExpr) blockNode).getRes(), blockNode)) {
                    continue;
                }
            } else if (blockNode instanceof Pointer && ((Pointer) blockNode).getOp() == Pointer.Op.LOAD) {  // load的结果不活跃
                if (!currentConflictGraph.checkActive(((Pointer) blockNode).getLoad(), blockNode)) {
                    continue;
                }
            } else if (blockNode instanceof GetInt) {
                if (!currentConflictGraph.checkActive(((GetInt) blockNode).getTarget(), blockNode)) {  // getint的结果不活跃
                    mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v0, Syscall.get_int));
                    mipsCode.addInstr(new Syscall());
                    continue;
                }
            } else if (blockNode instanceof Memory) {
                if (!currentConflictGraph.checkActive(((Memory) blockNode).getRes(), blockNode)) {
                    continue;
                }
            }
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
        freeAllRegisters(FREE_TEMP | FREE_GLOBAL, true);  // TODO: is it necessary? check
        Operand cond = branch.getCond();
        if (cond instanceof Immediate) {
            mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v1, ((Immediate) cond).getNumber()));
            mipsCode.addInstr(
                    new BranchInstr(BranchInstr.BranchType.bne, Registers.v1, Registers.zero, branch.getThenBlock().getLabel()));
        } else if (cond instanceof Symbol) {
            Symbol symbol = (Symbol) cond;
            int register = allocRegister(symbol, loadTempRegister);
            mipsCode.addInstr(
                    new BranchInstr(BranchInstr.BranchType.bne, register, Registers.zero, branch.getThenBlock().getLabel()));
        }

        mipsCode.addInstr(new J(branch.getElseBlock().getLabel()));
        consumeUsage(cond);
        // queue.add(branch.getThenBlock());
        // queue.add(branch.getElseBlock());
        // consumeUsage(cond);
    }

    // 注意要保证先寻找左右操作数寄存器，再寻找res寄存器
    private void translateFourExpr(FourExpr fourExpr) {
        FourExpr.ExprOp op = fourExpr.getOp();
        Operand left = fourExpr.getLeft();
        Symbol res = fourExpr.getRes();
        // int resRegister = allocRegister(res, false);
        if (fourExpr.isSingle()) {
            if (op == FourExpr.ExprOp.DEF || op == FourExpr.ExprOp.ASS) {
                if (left instanceof Immediate) {
                    int resRegister = allocRegister(res, noLoad);
                    mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, resRegister, ((Immediate) left).getNumber()));
                } else if (left instanceof Symbol) {
                    int leftRegister = allocRegister((Symbol) left, loadTempRegister);
                    int resRegister = allocRegister(res, noLoad);  // 要注意先找leftRegister，再找resRegister
                    mipsCode.addInstr(new MoveInstr(leftRegister, resRegister));
                } else {
                    assert false;
                }
            } else if (op == FourExpr.ExprOp.NOT) {
                if (left instanceof Immediate) {
                    int resRegister = allocRegister(res, noLoad);
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.seq, resRegister, Registers.zero,  // 等于0时候not值为1
                            ((Immediate) left).getNumber()));
                } else if (left instanceof Symbol) {
                    int leftRegister = allocRegister((Symbol) left, loadTempRegister);
                    int resRegister = allocRegister(res, noLoad);  // 要注意先找leftRegister，再找resRegister
                    mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.seq, resRegister, Registers.zero, leftRegister));
                } else {
                    assert false;
                }
            } else if (op == FourExpr.ExprOp.NEG) {  // 取反
                if (left instanceof Immediate) {
                    int resRegister = allocRegister(res, noLoad);
                    mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, resRegister, -((Immediate) left).getNumber()));
                } else if (left instanceof Symbol) {
                    int leftRegister = allocRegister((Symbol) left, loadTempRegister);
                    int resRegister = allocRegister(res, noLoad);  // 要注意先找leftRegister，再找resRegister
                    mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.subu, resRegister, Registers.zero, leftRegister));
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
                int resRegister = allocRegister(res, noLoad);
                if (op == FourExpr.ExprOp.ADD) {  //  零个寄存器，两个立即数
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
                } else if (op == FourExpr.ExprOp.OR) {   // only in cond Exp  or直接算and转换成逻辑值再算
                    mipsCode.addInstr(
                            new ALUSingle(ALUSingle.ALUSingleType.li, resRegister, leftVal | rightVal));
                } else if (op == FourExpr.ExprOp.AND) {
                    mipsCode.addInstr(
                            new ALUSingle(ALUSingle.ALUSingleType.li, resRegister, leftVal == 0 || rightVal == 0 ? 0 : 1));
                } else {
                    assert false;
                }
            } else if (left instanceof Symbol && right instanceof Immediate) {
                int leftRegister = allocRegister((Symbol) left, loadTempRegister);  // 先找leftRegister
                int rightVal = ((Immediate) right).getNumber();
                int resRegister = allocRegister(res, noLoad);  // 再找resRegister
                if (op == FourExpr.ExprOp.ADD) {  // 左值寄存器，右值立即数
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.addiu, resRegister, leftRegister, rightVal));
                } else if (op == FourExpr.ExprOp.SUB) {
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.addiu, resRegister, leftRegister, -rightVal));
                } else if (op == FourExpr.ExprOp.MUL) {
                    translateMult(rightVal, leftRegister, resRegister, null);
                    // mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v1, rightVal));
                    // mipsCode.addInstr(new Mult(leftRegister, Registers.v1));
                    // mipsCode.addInstr(new Mflo(resRegister));
                } else if (op == FourExpr.ExprOp.DIV) {
                    translateDivMod(rightVal, leftRegister, resRegister, false);
                    // mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v1, rightVal));
                    // mipsCode.addInstr(new Div(leftRegister, Registers.v1));
                    // mipsCode.addInstr(new Mflo(resRegister));
                } else if (op == FourExpr.ExprOp.MOD) {
                    translateDivMod(rightVal, leftRegister, resRegister, true);
                    // mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v1, rightVal));
                    // mipsCode.addInstr(new Div(leftRegister, Registers.v1));
                    // mipsCode.addInstr(new Mfhi(resRegister));
                } else if (op == FourExpr.ExprOp.GT) {
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.sgt, resRegister, leftRegister, rightVal));
                } else if (op == FourExpr.ExprOp.GE) {
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.sge, resRegister, leftRegister, rightVal));
                } else if (op == FourExpr.ExprOp.LT) {
                    mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v1, rightVal));
                    mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.slt, resRegister, leftRegister, Registers.v1));
                    // mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.slti, resRegister, leftRegister, rightVal));
                } else if (op == FourExpr.ExprOp.LE) {
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.sle, resRegister, leftRegister, rightVal));
                } else if (op == FourExpr.ExprOp.EQ) {
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.seq, resRegister, leftRegister, rightVal));
                } else if (op == FourExpr.ExprOp.NEQ) {
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.sne, resRegister, leftRegister, rightVal));
                } else if (op == FourExpr.ExprOp.OR) {   // only in cond Exp
                    if (rightVal == 0) {
                        mipsCode.addInstr(new MoveInstr(leftRegister, resRegister));
                    } else {
                        mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, resRegister, 1));
                    }
                    // mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.ori, resRegister, leftRegister, rightVal));
                } else if (op == FourExpr.ExprOp.AND) {
                    if (rightVal == 0) {
                        mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, resRegister, 0));
                    } else {
                        mipsCode.addInstr(new MoveInstr(leftRegister, resRegister));
                    }
                    // mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.andi, resRegister, leftRegister, rightVal));
                } else {
                    assert false;
                }
            } else if (left instanceof Immediate && right instanceof Symbol) {
                int rightRegister = allocRegister((Symbol) right, loadTempRegister);
                int leftVal = ((Immediate) left).getNumber();
                int resRegister = allocRegister(res, noLoad);  // 先找rightRegister，再找resRegister
                if (op == FourExpr.ExprOp.ADD) {
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.addiu, resRegister, rightRegister, leftVal));
                } else if (op == FourExpr.ExprOp.SUB) {
                    mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.subu, Registers.v1, Registers.zero, rightRegister));
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.addiu, resRegister, Registers.v1, leftVal));
                } else if (op == FourExpr.ExprOp.MUL) {
                    translateMult(leftVal, rightRegister, resRegister, null);
                    // mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v1, leftVal));
                    // mipsCode.addInstr(new Mult(Registers.v1, rightRegister));
                    // mipsCode.addInstr(new Mflo(resRegister));
                } else if (op == FourExpr.ExprOp.DIV) {
                    // translateDivMod(leftVal, rightRegister, resRegister, false);
                    mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v1, leftVal));
                    mipsCode.addInstr(new Div(Registers.v1, rightRegister));
                    mipsCode.addInstr(new Mflo(resRegister));
                } else if (op == FourExpr.ExprOp.MOD) {
                    // translateDivMod(leftVal, rightRegister, resRegister, true);
                    mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v1, leftVal));
                    mipsCode.addInstr(new Div(Registers.v1, rightRegister));
                    mipsCode.addInstr(new Mfhi(resRegister));
                } else if (op == FourExpr.ExprOp.GT) {
                    mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v1, leftVal));
                    mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.slt, resRegister, rightRegister, Registers.v1));
                    // mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.slti, resRegister, rightRegister, leftVal));
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
                } else if (op == FourExpr.ExprOp.OR) {   // only in cond Exp
                    if (leftVal == 0) {
                        mipsCode.addInstr(new MoveInstr(rightRegister, resRegister));
                    } else {
                        mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, resRegister, 1));
                    }
                    // mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.ori, resRegister, rightRegister, leftVal));
                } else if (op == FourExpr.ExprOp.AND) {
                    if (leftVal == 0) {
                        mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, resRegister, 0));
                    } else {
                        mipsCode.addInstr(new MoveInstr(rightRegister, resRegister));
                    }
                    // mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.andi, resRegister, rightRegister, leftVal));
                } else {
                    assert false;
                }
            } else if (left instanceof Symbol && right instanceof Symbol) {
                int leftRegister = allocRegister((Symbol) left, loadTempRegister);
                int rightRegister = allocRegister((Symbol) right, loadTempRegister);
                int resRegister = allocRegister(res, noLoad);  // 要注意寻找左、右、res寄存器的顺序
                if (op == FourExpr.ExprOp.ADD) {
                    mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.addu, resRegister, leftRegister, rightRegister));
                } else if (op == FourExpr.ExprOp.SUB) {
                    mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.subu, resRegister, leftRegister, rightRegister));
                } else if (op == FourExpr.ExprOp.MUL) {
                    mipsCode.addInstr(new Mult(leftRegister, rightRegister, false));
                    mipsCode.addInstr(new Mflo(resRegister));
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
                } else if (op == FourExpr.ExprOp.OR) {   // only in cond Exp
                    mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.or, resRegister, leftRegister, rightRegister));
                } else if (op == FourExpr.ExprOp.AND) {
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.sne, Registers.v0, leftRegister, 0));
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.sne, Registers.v1, rightRegister, 0));
                    mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.and, resRegister, Registers.v0, Registers.v1));
                    // mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.and, resRegister, leftRegister, rightRegister));
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

    // 优化div, mod
    // resRegister = operandRegister / c;
    private static int divLabel = 1;

    private void translateDivMod(int immediate, int operandRegister, int resRegister, boolean isMod) {
        // 正数 / or % 正数
        ArrayList<Instruction> instructions = new ArrayList<>();
        int sign = 1;
        if (immediate < 0) {
            immediate = -immediate;
            sign = -1;
        }
        int k = (int) (Math.log(immediate) / Math.log(2));
        if ((int) Math.pow(2, k) == immediate) {
            if (!isMod) {  // div
                instructions.add(new ALUDouble(ALUDouble.ALUDoubleType.srl, resRegister, operandRegister, k));
            } else {  // mod
                instructions.add(new ALUDouble(ALUDouble.ALUDoubleType.andi, resRegister, operandRegister, immediate - 1));
            }
        } else {
            int n = k + 32;  // k+32
            double f = Math.pow(2, n) / immediate;  // 2^n / b
            long upper = (long) f + 1;
            long lower = (long) f;
            double e_upper = (double) upper - f;
            double e_lower = f - (double) lower;
            double split = Math.pow(2, k) / immediate;
            if (e_upper < split) {
                instructions.add(new ALUSingle(ALUSingle.ALUSingleType.li, resRegister, upper));
            } else if (e_lower < split) {
                instructions.add(new ALUSingle(ALUSingle.ALUSingleType.li, resRegister, lower));
            } else {
                assert false : "e_upper和e_lower中一定有一个小于split";
            }
            instructions.add(new Mult(resRegister, operandRegister, true));
            instructions.add(new Mfhi(resRegister));
            instructions.add(new ALUDouble(ALUDouble.ALUDoubleType.srl, resRegister, resRegister, k));
            if (isMod) {
                translateMult(immediate, resRegister, Registers.v0, instructions);  // translateMult里面会用到v0寄存器
                instructions.add(new ALUTriple(ALUTriple.ALUTripleType.subu, resRegister, operandRegister, Registers.v0));
            }
        }
        String start = "DIV_" + divLabel++, end = "DIV_END_" + divLabel++;
        mipsCode.addInstr(new BranchInstr(BranchInstr.BranchType.bltz, operandRegister, Registers.zero, start));
        mipsCode.addInstrs(instructions);
        if (sign == -1) {  // 正负
            mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.subu, resRegister, Registers.zero, resRegister));
        }
        mipsCode.addInstr(new J(end));
        mipsCode.addInstr(new Label(start));
        mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.subu, operandRegister, Registers.zero, operandRegister));
        mipsCode.addInstrs(instructions);
        if (sign == 1) {  // 负正
            mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.subu, resRegister, Registers.zero, resRegister));
        }
        mipsCode.addInstr(new Label(end));
    }

    // 优化mult
    private void translateMult(int immediate, int operandRegister, int resRegister, ArrayList<Instruction> instructions) {
        if (immediate == 0) {
            if (instructions != null) {
                instructions.add(new ALUSingle(ALUSingle.ALUSingleType.li, resRegister, 0));
            } else {
                mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, resRegister, 0));
            }
            return;
        }
        long num = 1;
        int shiftTime = 0, newImm = immediate;
        ArrayList<Integer> shiftTimes = new ArrayList<>();
        while ((num << 1) <= immediate) {
            num <<= 1;
            shiftTime++;
        }
        while (immediate != 0) {
            while (immediate < num) {
                num >>= 1;
                shiftTime--;
            }
            immediate -= num;
            shiftTimes.add(shiftTime);
        }

        if (instructions == null) {
            if (shiftTimes.size() <= 3) {
                mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.sll, resRegister, operandRegister, shiftTimes.get(0)));
                if (shiftTimes.size() == 1) return;
                for (int i = 1; i < shiftTimes.size() - 1; i++) {
                    mipsCode.addInstr(
                            new ALUDouble(ALUDouble.ALUDoubleType.sll, Registers.v1, operandRegister, shiftTimes.get(i)));
                    mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.addu, resRegister, resRegister, Registers.v1));
                }
                int last = shiftTimes.get(shiftTimes.size() - 1);
                if (last == 0) {
                    mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.addu, resRegister, resRegister, operandRegister));
                } else {
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.sll, Registers.v1, operandRegister, last));
                    mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.addu, resRegister, resRegister, Registers.v1));
                }
            } else {
                mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v1, newImm));
                mipsCode.addInstr(new Mult(operandRegister, Registers.v1, false));
                mipsCode.addInstr(new Mflo(resRegister));
            }
        } else {
            if (shiftTimes.size() <= 3) {
                instructions.add(new ALUDouble(ALUDouble.ALUDoubleType.sll, resRegister, operandRegister, shiftTimes.get(0)));
                if (shiftTimes.size() == 1) return;
                for (int i = 1; i < shiftTimes.size() - 1; i++) {
                    instructions.add(
                            new ALUDouble(ALUDouble.ALUDoubleType.sll, Registers.v1, operandRegister, shiftTimes.get(i)));
                    instructions.add(new ALUTriple(ALUTriple.ALUTripleType.addu, resRegister, resRegister, Registers.v1));
                }
                int last = shiftTimes.get(shiftTimes.size() - 1);
                if (last == 0) {
                    instructions.add(new ALUTriple(ALUTriple.ALUTripleType.addu, resRegister, resRegister, operandRegister));
                } else {
                    instructions.add(new ALUDouble(ALUDouble.ALUDoubleType.sll, Registers.v1, operandRegister, last));
                    instructions.add(new ALUTriple(ALUTriple.ALUTripleType.addu, resRegister, resRegister, Registers.v1));
                }
            } else {
                instructions.add(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v1, newImm));
                instructions.add(new Mult(operandRegister, Registers.v1, false));
                instructions.add(new Mflo(resRegister));
            }
        }

    }

    private double calcCost(ArrayList<Instruction> instructions) {
        return instructions.stream().mapToDouble(Instruction::getCost).sum();
    }

    // 函数内联
    // private LinkedHashMap<FuncBlock, ArrayList<BasicBlock>> funcToSortedBlock = null;
    // private boolean translatingInLineFunction = false;
    // private int inlineFuncIndex = 1;
    // private String inlineFuncEnd = null;

    private void translateFuncCall(FuncCall funcCall) {
        // 保存所有正在使用的寄存器
        mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.sw, Registers.sp, 0, Registers.ra));
        // 找到子函数栈基地址
        ArrayList<Symbol> tempNoAddress = this.tempRegisters.getSymbolToRegister().keySet().stream().filter(t -> !t.hasAddress())
                .collect(Collectors.toCollection(ArrayList::new));
        int size = -currentStackSize - 4 - tempNoAddress.size() * 4;
        mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.addiu, Registers.a0, Registers.sp, size));
        // 设置参数
        // 被调用的函数的参数保存在函数栈指针的前几个位置
        int offset = 0;
        for (Operand param : funcCall.getrParams()) {
            offset -= 4;  // 函数从addr = 4开始放参数
            if (param instanceof Immediate) {
                mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v1, ((Immediate) param).getNumber()));
                mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.sw, Registers.a0, offset, Registers.v1));
            } else if (param instanceof Symbol) {
                // TODO: 当函数向子函数传递接收到的数组参数时候会出错 FIXED20221103
                // Symbol paramSymbol = (Symbol) param;
                // System.err.println(paramSymbol.getSymbolType());
                // System.err.println(paramSymbol.getScope());
                // System.err.println(paramSymbol.isPointerParam());
                // System.err.println();
                int register = allocRegister((Symbol) param, loadTempRegister);
                // loadSymbol((Symbol) param, Registers.v1);
                mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.sw, Registers.a0, offset, register));
            } else {
                assert false;
            }
            consumeUsage(param);
        }
        HashSet<Symbol> formerGlobalSymbols = currentConflictGraph.getActiveGlobalSymbols(
                currentBasicBlock.getContent().get(currentBlockNodeIndex));
        freeAllRegisters(FREE_TEMP | FREE_LOCAL | FREE_PARAM | FREE_GLOBAL, true);
        mipsCode.addInstr(new MoveInstr(Registers.a0, Registers.sp));
        // freeAllRegisters(false);
        // 跳转
        mipsCode.addInstr(new Jal(funcCall.getTargetLabel()));
        // 恢复sp
        mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.addiu, Registers.sp, Registers.sp, -size));
        mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.lw, Registers.sp, 0, Registers.ra));
        // 得到返回值
        if (funcCall.saveRet()) {
            Symbol ret = funcCall.getRet();
            int retRegister = allocRegister(ret, noLoad);
            mipsCode.addInstr(new MoveInstr(Registers.v0, retRegister));
        }
        // 恢复局部变量
        for (Symbol symbol : formerGlobalSymbols) {
            allocRegister(symbol, loadTempRegister | loadGlobalRegister);
        }
    }

    private void translateGetInt(GetInt getInt) {
        mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v0, Syscall.get_int));
        mipsCode.addInstr(new Syscall());
        Symbol symbol = getInt.getTarget();
        if (symbol.getSymbolType() == SymbolType.INT) {
            int register = allocRegister(symbol, noLoad);
            mipsCode.addInstr(new MoveInstr(Registers.v0, register));
        } else if (symbol.getSymbolType() == SymbolType.POINTER) {  // 如果是pointer，直接存在内存里
            int pointer = tempRegisters.getSymbolRegister(symbol);
            mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.sw, pointer, 0, Registers.v0));
            consumeUsage(symbol);
        } else {
            assert false;
        }
    }

    private void translateJump(Jump jump) {
        freeAllRegisters(FREE_TEMP | FREE_GLOBAL, true);  // TODO: check
        mipsCode.addInstr(new J(jump.getTarget().getLabel()));
        // queue.add(jump.getTarget());
    }

    private void translateMemory(Memory memory) {
        Symbol base = memory.getBase();
        assert base.getSymbolType() == SymbolType.ARRAY;  // 一定是数组
        Symbol res = memory.getRes();
        Operand offset = memory.getOffset();
        if (offset instanceof Immediate) {
            int resRegister = allocRegister(res, noLoad);
            if (base.getScope() == Symbol.Scope.GLOBAL) {
                mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.addiu, resRegister, Registers.gp,
                        base.getAddress() + ((Immediate) offset).getNumber()));
            } else if (base.getScope() == Symbol.Scope.PARAM) {  // base是param，先把base在内存中的地址取出来，再和offset相加
                if (((Immediate) offset).getNumber() == 0) {
                    mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.lw, Registers.sp, -base.getAddress(), resRegister));
                } else {
                    mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.lw, Registers.sp, -base.getAddress(), Registers.v1));
                    mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.addiu, resRegister, Registers.v1,
                            ((Immediate) offset).getNumber()));
                }
            } else {
                mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.addiu, resRegister, Registers.sp,
                        -base.getAddress() + ((Immediate) offset).getNumber()));
            }
        } else if (offset instanceof Symbol) {
            // TODO: WARNING!!! base是global或local时，可以直接计算出它在内存中的地址(sp, gp)，但是base是param时，需要先把base在内存中的地址取出来，再和offset相加
            if (base.getScope() == Symbol.Scope.PARAM) {
                int baseRegister = allocRegister(base, loadTempRegister);
                int offsetRegister = allocRegister((Symbol) offset, loadTempRegister);
                int resRegister = allocRegister(res, noLoad);
                mipsCode.addInstr(new ALUTriple(ALUTriple.ALUTripleType.addu, resRegister, baseRegister, offsetRegister));
                return;
            }
            if (base.getScope() == Symbol.Scope.GLOBAL) {
                mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.addiu, Registers.v1, Registers.gp, base.getAddress()));
            } else {
                mipsCode.addInstr(new ALUDouble(ALUDouble.ALUDoubleType.addiu, Registers.v1, Registers.sp, -base.getAddress()));
            }
            int offsetRegister = allocRegister((Symbol) offset, loadTempRegister);
            int resRegister = allocRegister(res, noLoad);
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
            int baseRegister = allocRegister(point, loadTempRegister);
            int targetRegister = allocRegister(res, noLoad);
            mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.lw, baseRegister, 0, targetRegister));
            consumeUsage(point);
        } else if (pointer.getOp() == Pointer.Op.STORE) {
            Symbol point = pointer.getPointer();
            Operand store = pointer.getStore();
            int baseRegister = allocRegister(point, loadTempRegister);
            if (store instanceof Immediate) {
                mipsCode.addInstr(new ALUSingle(ALUSingle.ALUSingleType.li, Registers.v1, ((Immediate) store).getNumber()));
                mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.sw, baseRegister, 0, Registers.v1));
            } else if (store instanceof Symbol) {
                int storeRegister = allocRegister((Symbol) store, loadTempRegister);
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
            int register = allocRegister(symbol, loadTempRegister);
            mipsCode.addInstr(new MoveInstr(register, Registers.a0));  // 把Symbol加载到a0寄存器
            // if (registers.occupyingRegister(symbol)) {
            //     mipsCode.addInstr(new MoveInstr(registers.getSymbolRegister(symbol), Registers.a0));
            // } else {
            //     loadSymbol(symbol, Registers.a0);
            // }
        }
        mipsCode.addInstr(new Syscall());
        consumeUsage(printInt.getVal());
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
                int register = allocRegister((Symbol) returnVal, loadTempRegister);
                mipsCode.addInstr(new MoveInstr(register, Registers.v0));
            }
        }
        freeAllRegisters(FREE_GLOBAL, true);
        mipsCode.addInstr(new Jr());
        consumeUsage(r.getReturnVal());
    }
}
