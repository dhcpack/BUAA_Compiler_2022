package Parser.stmt.types;

import Config.IO;
import Lexer.Token;
import Parser.Output;

import java.util.ArrayList;

public class BlockStmt implements StmtInterface, Output {
    // Block → '{' { BlockItem } '}'
    private final Token left;
    private final Token right;
    private final ArrayList<BlockItem> blockItems;

    public BlockStmt(Token left, ArrayList<BlockItem> blockItems, Token right) {
        this.left = left;
        this.right = right;
        this.blockItems = blockItems;
    }

    public boolean returnInt() {
        if (blockItems.size() == 0) {
            return false;
        }
        BlockItem blockItem = blockItems.get(blockItems.size() - 1);
        if (blockItem instanceof ReturnStmt) {
            return ((ReturnStmt) blockItem).returnInt();
        }
        return false;
    }

    public Token getRightBrace() {
        return this.right;
    }

    public Token getReturn() {
        return ((ReturnStmt) blockItems.get(blockItems.size() - 1)).getReturnToken();
    }

    public ArrayList<BlockItem> getBlockItems() {
        return blockItems;
    }

    @Override
    public void output() {
        IO.print(left.toString());
        for (BlockItem blockItem : blockItems) {
            blockItem.output();
        }
        IO.print(right.toString());
        IO.print("<Block>");
    }

    @Override
    public int getSemicolonLine() {
        return right.getLine();
    }
}
