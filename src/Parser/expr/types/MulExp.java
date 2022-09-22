package Parser.expr.types;

import Lexer.Token;

import java.util.ArrayList;

public class MulExp extends ExpGroup {
    // private final ArrayList<ExpGroup> expGroups = new ArrayList<>();
    // private final ArrayList<Token> ops = new ArrayList<>();

    public MulExp(ExpGroup expGroup) {
        getExpGroups().add(expGroup);
        setTag("<MulExp>");
    }

    // public void add(Token token, ExpGroup expGroup) {
    //     this.expGroups.add(expGroup);
    //     this.ops.add(token);
    // }
}
