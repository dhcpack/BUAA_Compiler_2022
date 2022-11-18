package BackEnd.optimizer;

import BackEnd.Registers;
import Frontend.Symbol.Symbol;
import Middle.optimizer.DefUseCalcUtil;
import Middle.type.BasicBlock;
import Middle.type.BlockNode;
import Middle.type.Branch;
import Middle.type.Jump;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Stack;

public class ConflictGraph {
    // private final ArrayList<BasicBlock> basicBlocks = new ArrayList<>();
    private final ArrayList<BlockNode> blockNodes = new ArrayList<>();
    private final LinkedHashMap<BlockNode, HashSet<Symbol>> inSymbols = new LinkedHashMap<>();
    private final LinkedHashMap<BlockNode, HashSet<Symbol>> outSymbols = new LinkedHashMap<>();
    private final HashMap<Symbol, ConflictGraphNode> conflictNodes = new HashMap<>();

    // 保存图着色法的寄存器分配结果
    // TODO: CHECK!!!三种结果：1.冲突图中分配寄存器symbolRegisterMap；2.冲突图中溢出overflowSymbol；3.不在冲突图中，可以任意分配寄存器
    private final HashMap<Symbol, Integer> symbolRegisterMap = new HashMap<>();
    private final HashSet<Symbol> overflowSymbol = new HashSet<>();
    // private final HashSet<Integer> freeRegisters = new HashSet<>();  // 未参与着色的寄存器

    // 可分配的全局寄存器
    // 固定顺序，着色时候会优先使用较小的寄存器
    private final ArrayList<Integer> registers = new ArrayList<>(Registers.globalRegisters);


    public ConflictGraph(ArrayList<BasicBlock> basicBlocks, ArrayList<Symbol> params) {
        for (int i = basicBlocks.size() - 1; i >= 0; i--) {
            // this.basicBlocks.add(basicBlocks.get(i));
            BasicBlock block = basicBlocks.get(i);
            ArrayList<BlockNode> blockNodes = block.getContent();
            for (int j = blockNodes.size() - 1; j >= 0; j--) {
                DefUseCalcUtil.calcDefUse(blockNodes.get(j));
                this.blockNodes.add(blockNodes.get(j));
                inSymbols.put(blockNodes.get(j), new HashSet<>());
                outSymbols.put(blockNodes.get(j), new HashSet<>());
            }
        }
        getActiveVariableStream();
        getConflictMap();
        manageRegisters();
    }

    // 活跃变量数据流分析
    // OUT[B] = U(B的后继p)(IN[p])
    // IN[B] = USE[B] U (OUT[B] - DEF[B])
    private void getActiveVariableStream() {
        boolean flag = false;
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
                // System.out.printf("%d, %d\n", inSize, inSymbols.get(block).size());
                // System.out.printf("%d, %d\n", outSize, outSymbols.get(block).size());
            }
        }
        // System.out.printf("%b", flag);
        if (flag) {
            getActiveVariableStream();
        }
    }

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
        ArrayList<ConflictGraphNode> nodesList = new ArrayList<>();
        for (ConflictGraphNode conflictGraphNode : conflictNodes.values()) {
            conflictGraphNode.initialEdgeCount();
            nodesList.add(conflictGraphNode);
        }
        // 得到节点着色顺序
        Stack<ConflictGraphNode> stack = new Stack<>();
        for (ConflictGraphNode node : nodesList) {
            System.out.println(node);
        }
        System.out.println();
        while (nodesList.size() != 0) {
            // 根据边数从高到低排序
            nodesList.sort(ConflictGraphNode::compareTo);
            if (nodesList.get(nodesList.size() - 1).getEdgeCount() >= registers.size()) {  // !!!OVERFLOW
                ConflictGraphNode overflow = nodesList.remove(0);
                overflowSymbol.add(overflow.getSymbol());
                for (ConflictGraphNode conflictGraphNode : nodesList) {
                    conflictGraphNode.removeConnection(overflow);
                }
            } else {  // 分配给刚好小于globalRegisters.length的节点
                ConflictGraphNode coloredNode = null;
                for (ConflictGraphNode conflictGraphNode : nodesList) {
                    if (conflictGraphNode.getEdgeCount() < registers.size()) {
                        coloredNode = conflictGraphNode;
                        break;
                    }
                }
                assert coloredNode != null;
                nodesList.remove(coloredNode);
                stack.add(coloredNode);
                for (ConflictGraphNode conflictGraphNode : nodesList) {
                    conflictGraphNode.removeConnection(coloredNode);
                }
            }
        }
        // 节点着色
        while (stack.size() != 0) {
            ConflictGraphNode node = stack.pop();
            HashSet<Integer> usedRegister = node.getConflictRegister();
            for (Integer r : registers) {
                if (!usedRegister.contains(r)) {
                    node.setRegister(r);
                    symbolRegisterMap.put(node.getSymbol(), r);
                }
            }
        }

    }

    public static final int NO_GLOBAL = -2;
    private final HashMap<Symbol, Integer> symbolToRegister = new HashMap<>();
    private final HashMap<Integer, Symbol> registerToSymbol = new HashMap<>();

    public boolean occupyingRegister(Symbol symbol) {
        return this.symbolToRegister.containsKey(symbol);
    }

    public Symbol getRegisterSymbol(int register) {
        assert registerToSymbol.containsKey(register);
        return registerToSymbol.get(register);
    }

    public int getSymbolRegister(Symbol symbol) {
        assert occupyingRegister(symbol);
        return symbolToRegister.get(symbol);
    }

    // TODO: 得到目标寄存器但不进行分配
    public int getTargetGlobalRegister(Symbol symbol) {
        if (!(symbol.getScope() == Symbol.Scope.LOCAL)) {
            return NO_GLOBAL;  // TODO: 不分配全局寄存器时返回-2
        }
        if (overflowSymbol.contains(symbol)) {
            return NO_GLOBAL;  // TODO: 不分配全局寄存器时返回-2
        }
        // TODO: 如果没在symbolRegisterMap可以随便分配一个寄存器，这里分配的是$5
        return this.symbolRegisterMap.getOrDefault(symbol, Registers.$5);
    }

    // TODO: 得到目标寄存器并进行分配
    public Integer allocGlobalRegister(Symbol symbol) {
        if (!(symbol.getScope() == Symbol.Scope.LOCAL)) {
            return NO_GLOBAL;  // TODO: 不分配全局寄存器时返回-2
        }
        if (overflowSymbol.contains(symbol)) {
            return NO_GLOBAL;  // TODO: 不分配全局寄存器时返回-2
        }
        // TODO: 如果没在symbolRegisterMap可以随便分配一个寄存器，这里分配的是$5
        int register = this.symbolRegisterMap.getOrDefault(symbol, Registers.$5);
        if (registerToSymbol.containsKey(register)) {
            Symbol occ = registerToSymbol.get(register);
            symbolToRegister.remove(occ);
            registerToSymbol.remove(register);
        }
        symbolToRegister.put(symbol, register);
        registerToSymbol.put(register, symbol);
        return register;
    }

    public boolean isOccupied(int register) {
        return this.registerToSymbol.containsKey(register);
    }

    public void freeRegister(int register) {
        // 检查是否属于全局寄存器
        if (!Registers.globalRegisters.contains(register) && register != Registers.$5) {
            return;
        }
        Symbol symbol = registerToSymbol.get(register);
        symbolToRegister.remove(symbol);
        registerToSymbol.remove(register);
        if (register != Registers.$5) {
            registers.add(register);
        }
    }

    public HashMap<Symbol, Integer> getSymbolToRegister() {
        return symbolToRegister;
    }

    public HashMap<Integer, Symbol> getRegisterToSymbol() {
        return registerToSymbol;
    }

    // 不与任何Symbol冲突
    public boolean hasNoConflict(Symbol symbol) {
        return symbol.getScope() == Symbol.Scope.LOCAL && !overflowSymbol.contains(symbol) && !symbolRegisterMap.containsKey(
                symbol);
    }

    // 检查变量是否活跃，用于删除死代码
    public boolean checkActive(Symbol symbol, BlockNode blockNode) {
        return symbol.getScope() != Symbol.Scope.LOCAL || this.outSymbols.get(blockNode).contains(symbol);
    }
}
