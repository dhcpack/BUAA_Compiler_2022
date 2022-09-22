package Parser.expr.types;

public class Cond extends LOrExp{
    public Cond(ExpGroup expGroup) {
        super(expGroup);
        setTag("<Cond>");
    }
    // LOrExp = (LOrExp) Cond
}
