package Frontend.Parser.expr.types;

public class ConstExp  {
    // ConstExp -> Exp
    private final AddExp addExp;

    public AddExp getAddExp() {
        return addExp;
    }

    public ConstExp(AddExp addExp){
        this.addExp = addExp;
    }

    @Override
    public String toString() {
        return addExp + "<ConstExp>\n";
    }
}
