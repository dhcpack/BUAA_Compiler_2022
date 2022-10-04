package Parser.expr.types;

import Config.IO;
import Lexer.Token;
import Parser.Output;

import java.util.ArrayList;

public class EqExp implements Output {
    // EqExp → RelExp {('==' | '!=') RelExp}
    private final RelExp firstExp;
    private final ArrayList<RelExp> exps;
    private final ArrayList<Token> seps;

    public EqExp(RelExp firstExp, ArrayList<RelExp> exps, ArrayList<Token> seps) {
        this.firstExp = firstExp;
        this.exps = exps;
        this.seps = seps;
    }

    public int getLine() {
        return exps.get(exps.size() - 1).getLine();
    }

    @Override
    public void output() {
        firstExp.output();
        IO.print("<EqExp>");
        for (int i=0;i<exps.size();i++){
            IO.print(seps.get(i).toString());
            exps.get(i).output();
            IO.print("<EqExp>");
        }
    }
}
