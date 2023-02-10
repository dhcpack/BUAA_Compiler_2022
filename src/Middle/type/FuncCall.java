package Middle.type;

import Frontend.Symbol.Symbol;

import java.util.ArrayList;
import java.util.StringJoiner;

public class FuncCall extends BlockNode {
    private final FuncBlock funcBlock;
    private ArrayList<Operand> rParams;
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

    public ArrayList<Operand> getrParams() {
        return rParams;
    }

    public void setrParams(ArrayList<Operand> rParams) {
        this.rParams = rParams;
    }

    public String getTargetLabel() {
        return this.funcBlock.getLabel();
    }

    public boolean saveRet() {
        return this.saveRet;
    }

    public Symbol getRet() {
        return this.ret;
    }

    @Override
    public String toString() {
        String call = String.format("CALL %s", funcBlock.getLabel());
        StringJoiner params = new StringJoiner(", ");
        for (Operand para : rParams) {
            params.add(para.toString());
        }
        if (ret == null) {
            return String.format("Call %s; Params: %s", funcBlock.getLabel(), params);
        } else {
            return String.format("Call %s; Params: %s; RET %s", funcBlock.getLabel(), params, ret);
        }
    }
}
