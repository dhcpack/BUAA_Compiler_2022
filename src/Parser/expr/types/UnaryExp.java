package Parser.expr.types;

import Config.IO;

import java.util.ArrayList;

public class UnaryExp extends ExpGroup implements UnaryExpInterface {
    private final ArrayList<UnaryOp> ops = new ArrayList<>();
    // PrimaryExp | FuncExp | UnaryExp
    private UnaryExpInterface unaryExpInterface;

    public UnaryExp() {
        setTag("<UnaryExp>");
    }

    public void addOp(UnaryOp op) {
        this.ops.add(op);
    }

    public void addContent(UnaryExpInterface unaryExpInterface) {
        this.unaryExpInterface = unaryExpInterface;
    }

    @Override
    public void output() {
        // super.output();
        for (UnaryOp op:ops){
            op.output();
        }
        unaryExpInterface.output();
        IO.print("<UnaryExp>");
    }
}
