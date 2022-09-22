package Parser.expr.types;

import Config.IO;
import Lexer.Token;
import Parser.Output;

import java.util.ArrayList;

public class ExpGroup implements Output {
    private final ArrayList<ExpGroup> expGroups = new ArrayList<>();
    private final ArrayList<Token> ops = new ArrayList<>();
    private String tag;

    // public void add(Token token, ExpGroup expGroup) {
    //     this.expGroups.add(expGroup);
    //     this.ops.add(token);
    // }

    public ArrayList<ExpGroup> getExpGroups() {
        return expGroups;
    }

    public ArrayList<Token> getOps() {
        return ops;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    // public void
    //
    // public void preOutput() {
    //
    //
    // }

    @Override
    public void output() {
        assert expGroups.size() != 0;
        expGroups.get(0).output();
        for (int i = 1; i < expGroups.size(); i++) {
            IO.print(ops.get(0).toString());
            expGroups.get(i).output();
        }
        IO.print(tag);
    }
}
