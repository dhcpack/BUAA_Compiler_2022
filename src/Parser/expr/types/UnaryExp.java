package Parser.expr.types;

import Config.IO;

public class UnaryExp implements UnaryExpInterface {
    private final UnaryOp op;
    // PrimaryExp | FuncExp | UnaryExp
    private final UnaryExpInterface unaryExpInterface;

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
