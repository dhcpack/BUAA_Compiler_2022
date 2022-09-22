package Parser.expr.types;

import Config.IO;
import Lexer.Token;
import Parser.Output;

import java.util.ArrayList;

public class MulExp implements Output {
    // MulExp → UnaryExp {('*' | '/' | '%') UnaryExp} // 1.UnaryExp 2.* 3./ 4.% 均需覆盖
    private final UnaryExp firstExp;
    private final ArrayList<UnaryExp> exps;
    private final ArrayList<Token> seps;

    public MulExp(UnaryExp firstExp, ArrayList<UnaryExp> exps, ArrayList<Token> seps) {
        this.firstExp = firstExp;
        this.exps = exps;
        this.seps = seps;
    }

    @Override
    public void output() {
        firstExp.output();
        IO.print("<MulExp>");
        for (int i=0;i<exps.size();i++){
            IO.print(seps.get(i).toString());
            exps.get(i).output();
            IO.print("<MulExp>");
        }
    }
}
