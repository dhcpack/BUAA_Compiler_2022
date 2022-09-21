package Parser.expr.types;

import Lexer.Token;

import java.util.ArrayList;

public class ExpGroup {
    private final ArrayList<ExpGroup> expGroups = new ArrayList<>();
    private final ArrayList<Token> ops = new ArrayList<>();

    public void add(Token token, ExpGroup expGroup) {
        this.expGroups.add(expGroup);
        this.ops.add(token);
    }

    public ArrayList<ExpGroup> getExpGroups() {
        return expGroups;
    }

    public ArrayList<Token> getOps() {
        return ops;
    }
}
