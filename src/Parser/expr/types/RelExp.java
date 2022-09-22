package Parser.expr.types;

import Lexer.Token;

import java.util.ArrayList;

public class RelExp extends ExpGroup {
    // private final ArrayList<ExpGroup> expGroups = new ArrayList<>();
    // private final ArrayList<Token> ops = new ArrayList<>();

    public RelExp(ExpGroup expGroup) {
        getExpGroups().add(expGroup);
        setTag("<RelExp>");
    }

    // public void add(Token token, ExpGroup expGroup) {
    //     this.expGroups.add(expGroup);
    //     this.ops.add(token);
    // }
}
