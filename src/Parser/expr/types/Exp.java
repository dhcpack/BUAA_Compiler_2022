package Parser.expr.types;

public class Exp extends AddExp implements PrimaryExpInterface {
    public Exp(ExpGroup expGroup) {
        super(expGroup);
    }
    // AddExp = (Exp) AddExp
}
