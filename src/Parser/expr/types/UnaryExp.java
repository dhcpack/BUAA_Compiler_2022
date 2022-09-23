package Parser.expr.types;

import Config.IO;

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

    @Override
    public void output() {
        if (op != null) {
            op.output();
        }
        unaryExpInterface.output();
        IO.print("<UnaryExp>");
    }
}
