package BackEnd.optimizer;

import Frontend.Symbol.Symbol;
import Middle.type.BasicBlock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

/*
 * 活跃变量数据流分析器
 * */
public class ActiveVariablesStreamAnalyzer {
    private final ArrayList<BasicBlock> basicBlocks = new ArrayList<>();
    private final LinkedHashMap<BasicBlock, HashSet<Symbol>> inSymbols = new LinkedHashMap<>();
    private final LinkedHashMap<BasicBlock, HashSet<Symbol>> outSymbols = new LinkedHashMap<>();

    public ActiveVariablesStreamAnalyzer(ArrayList<BasicBlock> basicBlocks, ArrayList<Symbol> params) {
        for (int i = basicBlocks.size() - 1; i >= 0; i--) {
            this.basicBlocks.add(basicBlocks.get(i));
            BasicBlock block = basicBlocks.get(i);
            inSymbols.put(block, new HashSet<>());
            outSymbols.put(block, new HashSet<>());
        }
        BasicBlock firstBlock = basicBlocks.get(0);
        for (Symbol param : params) {  // 函数参数加到第一个基本块的defSet中
            firstBlock.addToDefSet(param);
        }
        getActiveVariableStream();
    }

    // 活跃变量数据流分析
    // OUT[B] = U(B的后继p)(IN[p])
    // IN[B] = USE[B] U (OUT[B] - DEF[B])
    private void getActiveVariableStream() {
        boolean flag = false;
        for (BasicBlock block : basicBlocks) {
            int outSize = outSymbols.get(block).size();
            outSymbols.get(block).clear();
            for (BasicBlock nextBlock : block.getNextBlock()) {
                outSymbols.get(block).addAll(inSymbols.get(nextBlock));
            }
            int inSize = inSymbols.get(block).size();
            inSymbols.get(block).clear();
            inSymbols.get(block).addAll(outSymbols.get(block));
            inSymbols.get(block).removeAll(block.getDef());
            inSymbols.get(block).addAll(block.getUse());
            if (outSize != outSymbols.get(block).size() || inSize != inSymbols.get(block).size()) {
                flag = true;
            }
        }
        if (flag) {
            getActiveVariableStream();
        }
    }

    public HashSet<Symbol> getInSymbols(BasicBlock block) {
        return this.inSymbols.get(block);
    }

    public HashSet<Symbol> getOutSymbols(BasicBlock block) {
        return this.outSymbols.get(block);
    }
}