package Parser.expr.types;

import Lexer.Token;

import java.util.ArrayList;

public class AddExp extends ExpGroup{
    // private final ArrayList<ExpGroup> expGroups = new ArrayList<>();
    // private final ArrayList<Token> ops = new ArrayList<>();
    //
    public AddExp(ExpGroup expGroup) {
        getExpGroups().add(expGroup);
    }
    //
    // public void add(Token token, ExpGroup expGroup) {
    //     this.expGroups.add(expGroup);
    //     this.ops.add(token);
    // }
}
