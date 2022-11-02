package Frontend.Parser.expr.types;

import Frontend.Lexer.Token;

import java.util.ArrayList;

public class EqExp {
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
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(firstExp);
        stringBuilder.append("<EqExp>\n");
        for (int i=0;i<exps.size();i++){
            stringBuilder.append(seps.get(i)).append(exps.get(i)).append("<EqExp>\n");
        }
        return stringBuilder.toString();
    }
}
