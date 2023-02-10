package Frontend.Parser.expr.types;

public class Exp {
    // Exp -> AddExp
    private final AddExp addExp;

    public Exp(AddExp addExp){
        this.addExp = addExp;
    }

    public AddExp getAddExp() {
        return addExp;
    }

    public int getLine(){
        return this.addExp.getLine();
    }


    @Override
    public String toString() {
        return addExp + "<Exp>\n";
    }
}
