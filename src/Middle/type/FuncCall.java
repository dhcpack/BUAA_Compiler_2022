package Middle.type;

import Frontend.Parser.expr.types.LeafNode;
import Frontend.Symbol.Symbol;

import java.util.ArrayList;
import java.util.StringJoiner;

public class FuncCall extends BlockNode {
    private final FuncBlock funcBlock;
    private final ArrayList<Operand> rParams;
    private final Symbol ret;
    private final boolean saveRet;

    // save int ret
    public FuncCall(FuncBlock funcBlock, ArrayList<Operand> rParams, Symbol ret) {
        this.funcBlock = funcBlock;
        this.rParams = rParams;
        this.ret = ret;
        this.saveRet = true;
    }

    // don't save ret
    public FuncCall(FuncBlock funcBlock, ArrayList<Operand> rParams) {
        this.funcBlock = funcBlock;
        this.rParams = rParams;
        this.ret = null;
        this.saveRet = false;
    }

    @Override
    public String toString() {
        String call = String.format("CALL %s\n", funcBlock.getLabel());
        StringJoiner params = new StringJoiner(", ");
        for (Operand para : rParams) {
            params.add(para.toString());
        }
        if (ret == null) {
            return call + params + "\n";
        } else {
            return call + params + "\n" + "RET " + ret + "\n";
        }
    }
}
