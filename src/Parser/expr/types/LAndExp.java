package Parser.expr.types;

import Config.IO;
import Lexer.Token;
import Parser.Output;

import java.util.ArrayList;

public class LAndExp implements Output {
    // LAndExp → EqExp {'&&' EqExp}
    private final EqExp firstExp;
    private final ArrayList<EqExp> exps;
    private final ArrayList<Token> seps;

    public LAndExp(EqExp firstExp, ArrayList<EqExp> exps, ArrayList<Token> seps) {
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
        IO.print("<LAndExp>");
        for (int i = 0; i < exps.size(); i++) {
            IO.print(seps.get(i).toString());
            exps.get(i).output();
            IO.print("<LAndExp>");
        }
    }
}
