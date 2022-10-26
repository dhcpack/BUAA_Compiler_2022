package Middle.type;

import java.util.ArrayList;

public class BasicBlock {
    private final String label;
    private final ArrayList<BlockNode> content = new ArrayList<>();

    public BasicBlock(String label) {
        this.label = label;
    }

    public void addContent(BlockNode blockNode) {
        this.content.add(blockNode);
    }

}
