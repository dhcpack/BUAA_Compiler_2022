package Parser.expr.types;

import Config.IO;
import Lexer.Token;
import Parser.Output;

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

    @Override
    public void output() {
        firstExp.output();
        IO.print("<LOrExp>");
        for (int i=0;i<exps.size();i++){
            IO.print(seps.get(i).toString());
            exps.get(i).output();
            IO.print("<LOrExp>");
        }
    }
}
