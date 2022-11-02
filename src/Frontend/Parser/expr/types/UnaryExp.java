package Frontend.Parser.expr.types;

public class UnaryExp implements UnaryExpInterface {
    private final UnaryOp op;
    // UnaryExp --> PrimaryExp | FuncExp | UnaryOp UnaryExp
    private final UnaryExpInterface unaryExpInterface;

    // UnaryOp could be null
    /*
     *  if UnaryOp is NULL then UnaryExp is PrimaryExp | FuncExp
     *  else UnaryExp is UnaryOp UnaryExp(this time PrimaryExp and FuncExp is NULL)
     */
    public UnaryExp(UnaryOp op, UnaryExpInterface unaryExpInterface) {
        this.op = op;
        this.unaryExpInterface = unaryExpInterface;
    }

    public int getLine() {
        return this.unaryExpInterface.getLine();
    }

    public UnaryExpInterface getUnaryExpInterface() {
        return unaryExpInterface;
    }

    public UnaryOp getOp() {
        return op;
    }

    @Override
    public String toString() {
        if(op != null) {
            return op.toString() + unaryExpInterface + "<UnaryExp>\n";
        } else {
            return unaryExpInterface + "<UnaryExp>\n";
        }
    }
}
