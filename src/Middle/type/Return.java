package Middle.type;

import Frontend.Parser.expr.types.LeafNode;

public class Return {
    public final LeafNode returnVal;

    // return int
    public Return(LeafNode returnVal) {
        this.returnVal = returnVal;
    }

    // is void
    public Return() {
        this.returnVal = null;
    }

    public LeafNode getReturnVal() {
        return returnVal;
    }

    public boolean hasReturnVal() {
        return this.returnVal != null;
    }
}
