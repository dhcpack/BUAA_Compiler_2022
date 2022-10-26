package Middle.type;

import Frontend.Parser.expr.types.LeafNode;
import Frontend.Symbol.Symbol;

import java.util.ArrayList;

public class FuncCall extends BlockNode {
    private final FuncBlock funcBlock;
    private final ArrayList<LeafNode> rParams;
    private final Symbol ret;
    private final boolean saveRet;

    // save int ret
    public FuncCall(FuncBlock funcBlock, ArrayList<LeafNode> rParams, Symbol ret) {
        this.funcBlock = funcBlock;
        this.rParams = rParams;
        this.ret = ret;
        this.saveRet = true;
    }

    // don't save ret
    public FuncCall(FuncBlock funcBlock, ArrayList<LeafNode> rParams) {
        this.funcBlock = funcBlock;
        this.rParams = rParams;
        this.ret = null;
        this.saveRet = false;
    }
}
