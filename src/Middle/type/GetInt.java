package Middle.type;

import Frontend.Symbol.Symbol;

public class GetInt extends BlockNode {
    private final Symbol target;

    public GetInt(Symbol target) {
        this.target = target;
    }

    public Symbol getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "GETINT " + target.toString();
    }
}
