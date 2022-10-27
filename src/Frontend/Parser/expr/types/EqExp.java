package Frontend.Parser.expr.types;

import Config.Reader;
import Config.SyntaxWriter;
import Frontend.Lexer.Token;
import Config.Output;

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

    public RelExp getFirstExp() {
        return firstExp;
    }

    public ArrayList<RelExp> getExps() {
        return exps;
    }

    public ArrayList<Token> getSeps() {
        return seps;
    }

    public int getLine() {
        if (exps.size() != 0) {
            return this.exps.get(exps.size() - 1).getLine();
        } else {
            return this.firstExp.getLine();
        }
    }

    @Override
    public void output() {
        firstExp.output();
        SyntaxWriter.print("<EqExp>");
        for (int i=0;i<exps.size();i++){
            SyntaxWriter.print(seps.get(i).toString());
            exps.get(i).output();
            SyntaxWriter.print("<EqExp>");
        }
    }
}
