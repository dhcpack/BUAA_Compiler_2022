package Frontend.Parser.expr.types;

import Config.IO;
import Frontend.Lexer.Token;
import Frontend.Parser.Output;

import java.util.ArrayList;

public class LOrExp implements Output {
    // LOrExp → LAndExp {'||' LAndExp}
    private final LAndExp firstExp;
    private final ArrayList<LAndExp> exps;
    private final ArrayList<Token> seps;

    public LOrExp(LAndExp firstExp, ArrayList<LAndExp> exps, ArrayList<Token> seps) {
        this.firstExp = firstExp;
        this.exps = exps;
        this.seps = seps;
    }

    public int getLine() {
        if (exps.size() != 0) {
            return this.exps.get(exps.size() - 1).getLine();
        } else {
            return this.firstExp.getLine();
        }
    }

    public LAndExp getFirstExp() {
        return firstExp;
    }

    public ArrayList<LAndExp> getExps() {
        return exps;
    }

    public ArrayList<Token> getSeps() {
        return seps;
    }

    @Override
    public void output() {
        firstExp.output();
        IO.print("<LOrExp>");
        for (int i = 0; i < exps.size(); i++) {
            IO.print(seps.get(i).toString());
            exps.get(i).output();
            IO.print("<LOrExp>");
        }
    }
}
