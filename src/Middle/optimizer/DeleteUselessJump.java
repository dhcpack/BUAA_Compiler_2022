package Middle.optimizer;

import Middle.type.BasicBlock;
import Middle.type.BlockNode;
import Middle.type.FuncBlock;
import Middle.type.Jump;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class DeleteUselessJump {
    public static void optimize(LinkedHashMap<FuncBlock, ArrayList<BasicBlock>> funcToSortedBlock) {
        for (Map.Entry<FuncBlock, ArrayList<BasicBlock>> funcAndBlock : funcToSortedBlock.entrySet()) {
            ArrayList<BasicBlock> blocks = funcAndBlock.getValue();
            for (int i = 1; i < blocks.size(); i++) {
                BasicBlock formerBlock = blocks.get(i - 1);
                BasicBlock currentBlock = blocks.get(i);
                if (formerBlock.getLastContent() != null) {
                    // 如果前一个基本块的最后一句是跳转语句并跳转到当前块，则跳转语句多余，删除该跳转语句
                    BlockNode lastNode = formerBlock.getLastContent();
                    if (lastNode instanceof Jump && ((Jump) lastNode).getTarget() == currentBlock) {
                        formerBlock.getContent().remove(lastNode);
                    }
                }
            }
        }
    }
}
