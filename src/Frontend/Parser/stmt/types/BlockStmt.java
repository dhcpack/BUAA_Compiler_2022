package Frontend.Parser.stmt.types;

import Config.Reader;
import Config.SyntaxWriter;
import Frontend.Lexer.Token;
import Config.Output;

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
    public void output() {
        SyntaxWriter.print(left.toString());
        for (BlockItem blockItem : blockItems) {
            blockItem.output();
        }
        SyntaxWriter.print(right.toString());
        SyntaxWriter.print("<Block>");
    }

    @Override
    public int getSemicolonLine() {
        return right.getLine();
    }
}
