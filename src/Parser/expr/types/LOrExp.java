package Parser.expr.types;

public class LOrExp extends ExpGroup {
    public LOrExp(ExpGroup expGroup) {
        getExpGroups().add(expGroup);
        setTag("<LOrExp>");
    }
}
