package Frontend.Parser.expr.types;

public class Cond {
    // Cond -> LOrExp
    private final LOrExp lOrExp;

    public int getLine(){
        return lOrExp.getLine();
    }

    public Cond(LOrExp lOrExp){
        this.lOrExp = lOrExp;
    }

    public LOrExp getLOrExp() {
        return lOrExp;
    }

    @Override
    public String toString() {
        return lOrExp + "<Cond>\n";
    }
}
