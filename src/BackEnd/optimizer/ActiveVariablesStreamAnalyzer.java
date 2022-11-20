package BackEnd.optimizer;

import Frontend.Symbol.Symbol;
import Middle.optimizer.DefUseCalcUtil;
import Middle.type.BasicBlock;
import Middle.type.BlockNode;
import Middle.type.Branch;
import Middle.type.FuncParamBlock;
import Middle.type.Jump;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

/*
 * 活跃变量数据流分析器
 * */
public class ActiveVariablesStreamAnalyzer {
    private final ArrayList<BlockNode> blockNodes = new ArrayList<>();
    private final LinkedHashMap<BlockNode, HashSet<Symbol>> inSymbols = new LinkedHashMap<>();
    private final LinkedHashMap<BlockNode, HashSet<Symbol>> outSymbols = new LinkedHashMap<>();

    public ActiveVariablesStreamAnalyzer(ArrayList<BasicBlock> basicBlocks, ArrayList<Symbol> params) {
        for (int i = basicBlocks.size() - 1; i >= 0; i--) {
            BasicBlock block = basicBlocks.get(i);
            ArrayList<BlockNode> blockNodes = block.getContent();
            for (int j = blockNodes.size() - 1; j >= 0; j--) {
                DefUseCalcUtil.calcDefUse(blockNodes.get(j));
                this.blockNodes.add(blockNodes.get(j));
                inSymbols.put(blockNodes.get(j), new HashSet<>());
                outSymbols.put(blockNodes.get(j), new HashSet<>());
            }
        }
        HashSet<Symbol> paramSet = new HashSet<>(params);
        FuncParamBlock funcParamBlock = new FuncParamBlock(paramSet);
        this.blockNodes.add(funcParamBlock);
        inSymbols.put(funcParamBlock, new HashSet<>());
        outSymbols.put(funcParamBlock, new HashSet<>());
        getActiveVariableStream();
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
            for (BlockNode nextBlockNode : getNextBlockNode(blockNode, i)) {
                outSymbols.get(blockNode).addAll(inSymbols.get(nextBlockNode));
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

    public HashSet<Symbol> getInSymbols(BlockNode blockNode) {
        return this.inSymbols.get(blockNode);
    }

    public HashSet<Symbol> getOutSymbols(BlockNode blockNode) {
        return this.outSymbols.get(blockNode);
    }

    // 检查变量是否活跃，用于删除死代码
    public boolean checkActive(Symbol symbol, BlockNode blockNode) {
        return symbol.getScope() != Symbol.Scope.LOCAL || this.outSymbols.get(blockNode).contains(symbol);
    }

    public HashSet<BlockNode> getNextBlockNode(BlockNode blockNode) {
        return getNextBlockNode(blockNode, blockNodes.indexOf(blockNode));
    }

    public HashSet<BlockNode> getNextBlockNode(BlockNode blockNode, int index) {
        if (blockNode instanceof Jump) {
            return ((Jump) blockNode).getNextBlockNode();
        } else if (blockNode instanceof Branch) {
            return ((Branch) blockNode).getNextBlockNode();
        }
        HashSet<BlockNode> nextBlockNode = new HashSet<>();
        if (index != 0) {
            nextBlockNode.add(blockNodes.get(index - 1));
        }
        return nextBlockNode;
    }
}