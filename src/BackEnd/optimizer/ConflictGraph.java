package BackEnd.optimizer;

import BackEnd.MipsCode;
import BackEnd.Registers;
import BackEnd.instructions.ALUSingle;
import BackEnd.instructions.MemoryInstr;
import BackEnd.instructions.Syscall;
import Frontend.Symbol.Symbol;
import Middle.optimizer.DefUseCalcUtil;
import Middle.type.BasicBlock;
import Middle.type.BlockNode;
import Middle.type.Branch;
import Middle.type.FourExpr;
import Middle.type.FuncBlock;
import Middle.type.FuncParamBlock;
import Middle.type.GetInt;
import Middle.type.Immediate;
import Middle.type.Jump;
import Middle.type.Memory;
import Middle.type.Pointer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ConflictGraph {
    // private final ArrayList<BasicBlock> basicBlocks = new ArrayList<>();
    private final FuncBlock funcBlock;
    private final ArrayList<Symbol> funcParams;
    private final ArrayList<BlockNode> blockNodes = new ArrayList<>();
    private LinkedHashMap<BlockNode, HashSet<Symbol>> inSymbols = new LinkedHashMap<>();
    private LinkedHashMap<BlockNode, HashSet<Symbol>> outSymbols = new LinkedHashMap<>();
    private LinkedHashMap<BlockNode, HashSet<Symbol>> inSymbolsTemplate = new LinkedHashMap<>();
    private LinkedHashMap<BlockNode, HashSet<Symbol>> outSymbolsTemplate = new LinkedHashMap<>();
    private final HashMap<Symbol, ConflictGraphNode> conflictNodes = new HashMap<>();
    private static final int TOP = 5;

    public ConflictGraph(FuncBlock funcBlock, ArrayList<BasicBlock> basicBlocks, ArrayList<Symbol> params) {
        this.funcBlock = funcBlock;
        this.funcParams = params;
        HashSet<Symbol> allSymbols = new HashSet<>();
        for (int i = basicBlocks.size() - 1; i >= 0; i--) {
            // this.basicBlocks.add(basicBlocks.get(i));
            BasicBlock block = basicBlocks.get(i);
            ArrayList<BlockNode> blockNodes = block.getContent();
            for (int j = blockNodes.size() - 1; j >= 0; j--) {
                DefUseCalcUtil.calcDefUse(blockNodes.get(j));
                this.blockNodes.add(blockNodes.get(j));
                inSymbols.put(blockNodes.get(j), new HashSet<>());
                inSymbolsTemplate.put(blockNodes.get(j), new HashSet<>());
                outSymbols.put(blockNodes.get(j), new HashSet<>());
                outSymbolsTemplate.put(blockNodes.get(j), new HashSet<>());
                allSymbols.addAll(blockNodes.get(j).getDefSet());
                allSymbols.addAll(blockNodes.get(j).getUseSet());
            }
            // basicBlocks.get(i).getSymbolUsageMap().forEach((key, value) -> funcSymbolUsageMap.merge(key, value, Integer::sum));
        }
        HashSet<Symbol> paramSet = new HashSet<>(params);
        FuncParamBlock funcParamBlock = new FuncParamBlock(paramSet);
        this.blockNodes.add(funcParamBlock);
        inSymbols.put(funcParamBlock, new HashSet<>());
        inSymbolsTemplate.put(funcParamBlock, new HashSet<>());
        outSymbols.put(funcParamBlock, new HashSet<>());
        outSymbolsTemplate.put(funcParamBlock, new HashSet<>());
        allSymbols.addAll(paramSet);
        recuTimes = 0;
        getActiveVariableStream();
        if (recuTimes != TOP) {
            getConflictMap();
            manageRegisters();
        } else {
            overflowSymbol.addAll(allSymbols);
            Registers.localRegisters = new ArrayList<>(Registers.registersGroup1);
            Registers.globalRegisters = new ArrayList<>(Registers.registersGroup2);
            for (int i = 0; i < blockNodes.size(); i++) {
                BlockNode blockNode = blockNodes.get(i);
                outSymbols.get(blockNode).addAll(allSymbols);
                inSymbols.get(blockNode).addAll(allSymbols);
            }
        }
    }

    // 活跃变量数据流分析
    // OUT[B] = U(B的后继p)(IN[p])
    // IN[B] = USE[B] U (OUT[B] - DEF[B])
    private int recuTimes = 0;

    private void getActiveVariableStream() {
        boolean flag = true;
        while (flag) {
            flag = false;
            if (recuTimes == TOP) {
                return;
            }
            for (int i = 0; i < blockNodes.size(); i++) {
                BlockNode blockNode = blockNodes.get(i);
                int outSize = outSymbols.get(blockNode).size();
                outSymbols.get(blockNode).clear();
                if (blockNode instanceof Jump) {
                    for (BlockNode nextBlockNode : ((Jump) blockNode).getNextBlockNode()) {
                        outSymbols.get(blockNode).addAll(inSymbols.get(nextBlockNode));
                    }
                } else if (blockNode instanceof Branch) {
                    for (BlockNode nextBlockNode : ((Branch) blockNode).getNextBlockNode()) {
                        outSymbols.get(blockNode).addAll(inSymbols.get(nextBlockNode));
                    }
                } else {
                    if (i != 0) {
                        BlockNode nextBlockNode = blockNodes.get(i - 1);
                        outSymbols.get(blockNode).addAll(inSymbols.get(nextBlockNode));
                    }
                }
                int inSize = inSymbols.get(blockNode).size();
                inSymbols.get(blockNode).clear();
                inSymbols.get(blockNode).addAll(outSymbols.get(blockNode));
                inSymbols.get(blockNode).removeAll(blockNode.getDefSet());
                inSymbols.get(blockNode).addAll(blockNode.getUseSet());
                if (outSize != outSymbols.get(blockNode).size() || inSize != inSymbols.get(blockNode).size()) {
                    flag = true;
                }
            }
            // System.out.printf("%b", flag);
            recuTimes++;
        }

        final boolean[] deleted = {false};
        blockNodes.removeIf(new Predicate<BlockNode>() {
            @Override
            public boolean test(BlockNode blockNode) {
                if (blockNode instanceof FourExpr && ((FourExpr) blockNode).getOp() == FourExpr.ExprOp.ASS && ((FourExpr) blockNode).getLeft() instanceof Immediate && ((Immediate) ((FourExpr) blockNode).getLeft()).getNumber() == 13) {
                    System.out.println(1);
                }
                if (blockNode instanceof FourExpr) {  // 四元式的计算结果不活跃
                    if (!checkActive(((FourExpr) blockNode).getRes(), blockNode)) {
                        inSymbolsTemplate.remove(blockNode);
                        outSymbolsTemplate.remove(blockNode);
                        funcBlock.refreshSymUsageMap(blockNode);
                        deleted[0] = true;
                        System.out.printf("REMOVE %s\n", blockNode);
                        return true;
                    }
                } else if (blockNode instanceof Pointer && ((Pointer) blockNode).getOp() == Pointer.Op.LOAD) {  // load的结果不活跃
                    if (!checkActive(((Pointer) blockNode).getLoad(), blockNode)) {
                        inSymbolsTemplate.remove(blockNode);
                        outSymbolsTemplate.remove(blockNode);
                        funcBlock.refreshSymUsageMap(blockNode);
                        deleted[0] = true;
                        System.out.printf("REMOVE %s\n", blockNode);
                        return true;
                    }
                } else if (blockNode instanceof Memory) {
                    if (!checkActive(((Memory) blockNode).getRes(), blockNode)) {
                        inSymbolsTemplate.remove(blockNode);
                        outSymbolsTemplate.remove(blockNode);
                        funcBlock.refreshSymUsageMap(blockNode);
                        deleted[0] = true;
                        System.out.printf("REMOVE %s\n", blockNode);
                        return true;
                    }
                }
                return false;
            }
        });
        if (deleted[0]) {
            recuTimes = 0;
            inSymbols = new LinkedHashMap<>(inSymbolsTemplate);
            outSymbols = new LinkedHashMap<>(outSymbolsTemplate);

            getActiveVariableStream();
        }
    }
    /*
    func getActiveVariableStream(){
        // 计算一次
        if(change)
            getActiveVariableStream
    }
    */

    private ConflictGraphNode getConflictNode(Symbol symbol) {
        if (conflictNodes.containsKey(symbol)) {
            return conflictNodes.get(symbol);
        } else {
            ConflictGraphNode conflictGraphNode = new ConflictGraphNode(symbol);
            conflictNodes.put(symbol, conflictGraphNode);
            return conflictGraphNode;
        }
    }

    // 构建冲突图
    private void getConflictMap() {
        // 参数之间互相冲突
        for (int i = 0; i < funcParams.size(); i++) {
            ConflictGraphNode nodeI = getConflictNode(funcParams.get(i));
            for (int j = i + 1; j < funcParams.size(); j++) {
                ConflictGraphNode nodeJ = getConflictNode(funcParams.get(j));
                nodeI.addConflictEdge(nodeJ);
                nodeJ.addConflictEdge(nodeI);
            }
        }

        // 基本块入口互相冲突
        for (HashSet<Symbol> conflictGroup : inSymbols.values()) {
            ArrayList<Symbol> conflictArrayList = new ArrayList<>(conflictGroup);
            for (int i = 0; i < conflictArrayList.size() - 1; i++) {
                ConflictGraphNode nodeI = getConflictNode(conflictArrayList.get(i));
                for (int j = i + 1; j < conflictArrayList.size(); j++) {
                    ConflictGraphNode nodeJ = getConflictNode(conflictArrayList.get(j));
                    nodeI.addConflictEdge(nodeJ);
                    nodeJ.addConflictEdge(nodeI);
                }
            }
        }
    }

    // 图着色法分配寄存器
    // TODO: 全局寄存器溢出
    private void manageRegisters() {
        // 初始化冲突边
        // 有限队列
        PriorityQueue<ConflictGraphNode> nodesHeap = new PriorityQueue<>(
                (o1, o2) -> o2.getCurrEdgeCount() - o1.getCurrEdgeCount());  // 大根堆
        nodesHeap.addAll(conflictNodes.values());

        // 得到节点着色顺序
        while (nodesHeap.size() != 0) {
            if (nodesHeap.peek().getCurrEdgeCount() >= registers.size()) {
                ConflictGraphNode overflow = nodesHeap.remove();
                overflowSymbol.add(overflow.getSymbol());
                for (ConflictGraphNode conflictGraphNode : nodesHeap) {
                    conflictGraphNode.removeConnection(overflow);
                }
                continue;
            }
            while (!nodesHeap.isEmpty()) {
                ConflictGraphNode node = nodesHeap.remove();
                HashSet<Integer> availRegister = new HashSet<>(registers);
                availRegister.removeAll(node.getConflictRegister());
                for (Integer r : availRegister) {
                    node.setRegister(r);
                    symbolRegisterMap.put(node.getSymbol(), r);
                    break;
                }
            }
            break;
        }
    }

    // 保存图着色法的寄存器分配结果
    // TODO: CHECK!!!三种结果：1.冲突图中分配寄存器symbolRegisterMap；2.冲突图中溢出overflowSymbol；3.不在冲突图中，可以任意分配寄存器
    private final HashMap<Symbol, Integer> symbolRegisterMap = new HashMap<>();
    private final HashSet<Symbol> overflowSymbol = new HashSet<>();
    // private final HashSet<Integer> freeRegisters = new HashSet<>();  // 未参与着色的寄存器

    // 可分配的全局寄存器
    // 固定顺序，着色时候会优先使用较小的寄存器
    private final ArrayList<Integer> registers = new ArrayList<>(Registers.globalRegisters);

    private final HashMap<Symbol, Integer> symbolToTempRegister = new HashMap<>();

    private final HashMap<Symbol, Integer> symbolToGlobalRegister = new HashMap<>();

    // TODO: 相当于符号和寄存器绑定
    // 管理LOCAL or Param Symbol的寄存器分配(全局寄存器)
    public int allocGlobalRegister(Symbol symbol, Registers tempRegisters) {
        assert hasGlobalRegister(symbol);
        assert symbol.getScope() == Symbol.Scope.LOCAL || symbol.getScope() == Symbol.Scope.PARAM;
        if (symbolRegisterMap.containsKey(symbol)) {
            int register = symbolRegisterMap.get(symbol);
            symbolToGlobalRegister.put(symbol, register);
            // System.out.printf("%s 1=> %d\n", symbol, register);
            return register;
        } else {
            int register = registers.get(0);
            symbolToGlobalRegister.put(symbol, register);
            // System.out.printf("%s 3=> %d\n", symbol, register);
            return register;
        }
    }

    public void settleOverflowSymbol(Symbol symbol, int register) {
        symbolToTempRegister.put(symbol, register);
        // System.out.printf("%s 2=> %d\n", symbol, register);
    }

    public void freeOverflowSymbol(Symbol symbol) {
        assert this.overflowSymbol.contains(symbol);
        int register = this.symbolToTempRegister.get(symbol);
        this.symbolToTempRegister.remove(symbol);
    }

    public int getSymbolRegister(Symbol symbol) {
        return this.symbolToGlobalRegister.get(symbol);
    }

    // TODO: only used when translate func call
    public void freeAllGlobalRegisters(Registers tempRegisters, MipsCode mipsCode, BlockNode currBlockNode) {
        HashSet<Symbol> activeSet = new HashSet<>(outSymbols.get(currBlockNode));
        for (Map.Entry<Symbol, Integer> symbolRegister : symbolToGlobalRegister.entrySet()) {
            Symbol symbol = symbolRegister.getKey();
            int register = symbolRegister.getValue();
            if (!activeSet.contains(symbol)) continue;
            assert symbol.getScope() == Symbol.Scope.LOCAL || symbol.getScope() == Symbol.Scope.PARAM;
            mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.sw, Registers.sp, -symbol.getAddress(), register));
        }
        for (Map.Entry<Symbol, Integer> symbolRegister : symbolToTempRegister.entrySet()) {
            Symbol symbol = symbolRegister.getKey();
            int register = symbolRegister.getValue();
            tempRegisters.freeRegister(register);
            assert (symbol.getScope() == Symbol.Scope.LOCAL || symbol.getScope() == Symbol.Scope.PARAM) && overflowSymbol.contains(
                    symbol);
            mipsCode.addInstr(new MemoryInstr(MemoryInstr.MemoryType.sw, Registers.sp, -symbol.getAddress(), register));
        }
        symbolToTempRegister.clear();
        symbolToGlobalRegister.clear();
    }

    public boolean hasGlobalRegister(Symbol symbol) {
        return (symbol.getScope() == Symbol.Scope.LOCAL || symbol.getScope() == Symbol.Scope.PARAM) && !overflowSymbol.contains(
                symbol);
    }

    public boolean occupyingGlobalRegister(Symbol symbol) {
        return this.symbolToGlobalRegister.containsKey(symbol);
    }

    public boolean inOutSymbols(Symbol symbol, BlockNode blockNode) {
        return this.outSymbols.get(blockNode).contains(symbol);
    }

    public HashSet<Symbol> getActiveGlobalSymbols(BlockNode currBlockNode) {
        return symbolToGlobalRegister.keySet().stream().filter(t -> outSymbols.get(currBlockNode).contains(t))
                .collect(Collectors.toCollection(HashSet<Symbol>::new));
    }

    // 检查变量是否活跃，用于删除死代码
    public boolean checkActive(Symbol symbol, BlockNode blockNode) {
        if (symbol.getScope() == Symbol.Scope.TEMP) {
            return funcBlock.getSymbolUsageMap().containsKey(symbol);
        } else if (symbol.getScope() == Symbol.Scope.LOCAL || symbol.getScope() == Symbol.Scope.PARAM) {
            return this.outSymbols.get(blockNode).contains(symbol);
        } else {
            return true;
        }
    }

    public HashSet<Symbol> memorizeGlobalRegisters() {
        return new HashSet<>(this.symbolToGlobalRegister.keySet());
    }
}
