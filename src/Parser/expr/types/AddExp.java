package Parser.expr.types;

import Config.IO;
import Lexer.Token;
import Parser.Output;

import java.util.ArrayList;

public class AddExp implements Output {
    // AddExp → MulExp {('+' | '−') MulExp} // 1.MulExp 2.+ 需覆盖 3.- 需覆盖
    private final MulExp firstExp;
    private final ArrayList<MulExp> exps;
    private final ArrayList<Token> seps;

    public AddExp(MulExp firstExp, ArrayList<MulExp> exps, ArrayList<Token> seps) {
        this.firstExp = firstExp;
        this.exps = exps;
        this.seps = seps;
    }

    @Override
    public void output() {
        firstExp.output();
        IO.print("<AddExp>");
        for (int i = 0; i < exps.size(); i++) {
            IO.print(seps.get(i).toString());
            exps.get(i).output();
            IO.print("<AddExp>");
        }
    }
}
