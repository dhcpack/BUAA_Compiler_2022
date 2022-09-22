package Parser.expr.types;

import Lexer.Token;
import Parser.Output;

import java.util.ArrayList;

public class AddExp extends ExpGroup implements Output {
    // private final ArrayList<ExpGroup> expGroups = new ArrayList<>();
    // private final ArrayList<Token> ops = new ArrayList<>();
    //
    public AddExp(ExpGroup expGroup) {
        getExpGroups().add(expGroup);
        setTag("<AddExp>");
    }

    @Override
    public void output() {
    }

    //
    // public void add(Token token, ExpGroup expGroup) {
    //     this.expGroups.add(expGroup);
    //     this.ops.add(token);
    // }
}
