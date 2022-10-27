package Frontend.Parser.expr.types;

import Config.Reader;
import Config.SyntaxWriter;

public class UnaryExp implements UnaryExpInterface {
    private final UnaryOp op;
    // UnaryExp --> PrimaryExp | FuncExp | UnaryOp UnaryExp
    private final UnaryExpInterface unaryExpInterface;

    // UnaryOp could be null
    /*
     *  if UnaryOp is NULL then UnaryExp is PrimaryExp | FuncExp
     *  else UnaryOp is UnaryOp UnaryExp
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
    public void output() {
        if (op != null) {
            op.output();
        }
        unaryExpInterface.output();
        SyntaxWriter.print("<UnaryExp>");
    }
}
