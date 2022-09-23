package Parser.expr.types;

import Config.IO;
import Lexer.Token;
import Parser.Output;

import java.util.ArrayList;

public class RelExp implements Output {
    // RelExp → AddExp {('<' | '>' | '<=' | '>=') AddExp}
    private final AddExp firstExp;
    private final ArrayList<AddExp> exps;
    private final ArrayList<Token> seps;

    public RelExp(AddExp firstExp, ArrayList<AddExp> exps, ArrayList<Token> seps) {
        this.firstExp = firstExp;
        this.exps = exps;
        this.seps = seps;
    }

    @Override
    public void output() {
        firstExp.output();
        IO.print("<RelExp>");
        for (int i=0;i<exps.size();i++){
            IO.print(seps.get(i).toString());
            exps.get(i).output();
            IO.print("<RelExp>");
        }
    }
}
