package Frontend.Parser.stmt.types;

import Frontend.Lexer.Token;

import java.util.ArrayList;

public class BlockStmt implements StmtInterface{
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
        if (!(blockItem instanceof Stmt)) {
            return false;
        }
        Stmt stmt = (Stmt) blockItem;
        if (stmt.getStmt() instanceof ReturnStmt) {
            return ((ReturnStmt) stmt.getStmt()).returnInt();
        }
        return false;
    }

    public Token getRightBrace() {
        return this.right;
    }

    // BlockItem -> Stmt(StmtInterface) -> ReturnStmt
    // 最后一条语句是否是return
    public Token getReturn() {
        if (blockItems.size() == 0) {
            return null;
        }
        BlockItem blockItem = blockItems.get(blockItems.size() - 1);
        if (!(blockItem instanceof Stmt)) {
            return null;
        }
        Stmt stmt = (Stmt) blockItem;
        if (stmt.getStmt() instanceof ReturnStmt) {
            return ((ReturnStmt) stmt.getStmt()).getReturnToken();
        }
        return null;
    }

    public ArrayList<BlockItem> getBlockItems() {
        return blockItems;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(left);
        for (BlockItem blockItem : blockItems) {
            stringBuilder.append(blockItem);
        }
        stringBuilder.append(right).append("<Block>\n");
        return stringBuilder.toString();
    }

    @Override
    public int getSemicolonLine() {
        return right.getLine();
    }

    public Token getLeft() {
        return left;
    }

    public Token getRight() {
        return right;
    }
}
