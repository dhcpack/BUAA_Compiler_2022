package Parser.stmt.types;

import Config.IO;
import Lexer.Token;
import Parser.Output;

import java.util.ArrayList;

public class BlockStmt implements StmtInterface,Output {
    // Block → '{' { BlockItem } '}'
    private final Token left;
    private final Token right;
    private final ArrayList<BlockItem> blockItems;

    public BlockStmt(Token left, ArrayList<BlockItem> blockItems, Token right){
        this.left = left;
        this.right = right;
        this.blockItems = blockItems;
    }

    @Override
    public void output() {
        IO.print(left.toString());
        for(BlockItem blockItem:blockItems){
            blockItem.output();
        }
        IO.print(right.toString());
        IO.print("<Block>");
    }

    // public void addBlockItem(BlockItem blockItem) {
    //     this.blockItems.add(blockItem);
    // }

    // public void addAllBlockItem(ArrayList<BlockItem> blockItems){
    //     this.blockItems.addAll(blockItems);
    // }
}
