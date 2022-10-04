package Parser.expr.types;

import Config.IO;
import Lexer.Token;
import Parser.Output;

import java.util.ArrayList;

public class AddExp implements Output {
    // AddExp → MulExp {('+' | '−') MulExp}
    private final MulExp firstExp;
    private final ArrayList<MulExp> exps;
    private final ArrayList<Token> seps;

    public AddExp(MulExp firstExp, ArrayList<MulExp> exps, ArrayList<Token> seps) {
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

    public MulExp getFirstExp() {
        return firstExp;
    }

    public ArrayList<MulExp> getExps() {
        return exps;
    }

    @Override
    public void output() {
        firstExp.output();
        IO.print("<AddExp>");  // correctly print BNF
        for (int i = 0; i < exps.size(); i++) {
            IO.print(seps.get(i).toString());
            exps.get(i).output();
            IO.print("<AddExp>");
        }
    }
}
