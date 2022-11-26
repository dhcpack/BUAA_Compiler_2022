package Middle.optimizer.DAG;

import Middle.MiddleCode;
import Middle.optimizer.DAG.DAGraph;
import Middle.type.BasicBlock;
import Middle.type.FuncBlock;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class DeleteCommonExpr {
    public static void optimize(MiddleCode middleCode) {
        LinkedHashMap<FuncBlock, ArrayList<BasicBlock>> funcToSortedBlock = middleCode.getFuncToSortedBlock();
        for (ArrayList<BasicBlock> basicBlocks : funcToSortedBlock.values()) {
            for (BasicBlock basicBlock : basicBlocks) {
                // 每个基本块经过DAG图删除公共子表达式优化
                DAGraph daGraph = new DAGraph(basicBlock);
                daGraph.dump();
            }
        }
    }
}
