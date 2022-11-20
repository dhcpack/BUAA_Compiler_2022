package Middle.type;

public class Return extends BlockNode {
    private final Operand returnVal;

    // return int
    public Return(Operand returnVal) {
        this.returnVal = returnVal;
    }

    // return void
    public Return() {
        this.returnVal = null;
    }

    public Operand getReturnVal() {
        return returnVal;
    }

    public boolean hasReturnVal() {
        return this.returnVal != null;
    }

    @Override
    public String toString() {
        if (returnVal == null) {
            return "RETURN VOID";
        } else {
            return "RETURN " + returnVal;
        }
    }
}
