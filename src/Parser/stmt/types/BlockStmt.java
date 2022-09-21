package Parser.stmt.types;

import java.util.ArrayList;

public class BlockStmt extends Stmt {
    private final ArrayList<BlockItem> blockItems = new ArrayList<>();

    public void addBlockItem(BlockItem blockItem) {
        this.blockItems.add(blockItem);
    }
}
