package Parser.expr.types;

public class LAndExp extends ExpGroup{
    public LAndExp(ExpGroup expGroup) {
        getExpGroups().add(expGroup);
    }
}
