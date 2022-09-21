package Parser.expr.types;

import java.util.ArrayList;

public class UnaryExp extends ExpGroup implements UnaryExpInterface {
    private final String tag = "<UnaryExp>";
    private final ArrayList<UnaryOp> ops = new ArrayList<>();
    // PrimaryExp | FuncExp | UnaryExp
    private UnaryExpInterface unaryExpInterface;

    public void addOp(UnaryOp op) {
        this.ops.add(op);
    }

    public void addContent(UnaryExpInterface unaryExpInterface) {
        this.unaryExpInterface = unaryExpInterface;
    }
}
