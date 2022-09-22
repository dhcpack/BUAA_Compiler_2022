package Parser.expr.types;

public class EqExp extends ExpGroup{
    public EqExp(ExpGroup expGroup) {
        getExpGroups().add(expGroup);
        setTag("<EqExp>");
    }
}
