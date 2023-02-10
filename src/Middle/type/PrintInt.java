package Middle.type;

public class PrintInt extends BlockNode {
    public enum PrintType {
        PRINT_INT,
        PRINT_STR,
    }

    private final PrintType type = PrintType.PRINT_INT;

    private Operand val;

    public PrintInt() {
    }

    public PrintInt(Operand val) {
        this.val = val;
    }

    public void setVal(Operand val) {
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
