package Middle.type;

import Frontend.Parser.expr.types.LeafNode;

public class Print {
    public enum PrintType {
        PRINT_INT,
        PRINT_STR,
    }

    private final LeafNode val;
    private final String label;
    private final PrintType type;

    public Print(LeafNode val) {
        this.val = val;
        this.label = null;
        this.type = PrintType.PRINT_INT;
    }

    public Print(String label) {
        this.val = null;
        this.label = label;
        this.type = PrintType.PRINT_STR;
    }
}
