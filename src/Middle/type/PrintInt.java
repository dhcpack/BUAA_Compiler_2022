package Middle.type;

import Frontend.Parser.expr.types.LeafNode;

public class PrintInt extends BlockNode {
    public enum PrintType {
        PRINT_INT,
        PRINT_STR,
    }
    private final PrintType type = PrintType.PRINT_INT;

    private final Operand val;

    public PrintInt(Operand val) {
        this.val = val;
    }

    public Operand getVal() {
        return val;
    }

    @Override
    public String toString() {
        return "PRINT_INT " + val;
    }
}
