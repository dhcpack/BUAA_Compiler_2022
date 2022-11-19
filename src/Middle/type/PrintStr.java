package Middle.type;

public class PrintStr implements BlockNode {
    private final PrintInt.PrintType type = PrintInt.PrintType.PRINT_STR;

    private final String strName;

    public PrintStr(String strName) {
        this.strName = strName;
    }

    public String getStrName() {
        return strName;
    }

    @Override
    public String toString() {
        return "PRINT_STR " + strName;
    }
}
