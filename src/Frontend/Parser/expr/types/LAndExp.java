package Frontend.Parser.expr.types;

import Frontend.Lexer.Token;

import java.util.ArrayList;

public class LAndExp {
    // LAndExp → EqExp {'&&' EqExp}
    private final EqExp firstExp;
    private final ArrayList<EqExp> exps;
    private final ArrayList<Token> seps;

    public LAndExp(EqExp firstExp, ArrayList<EqExp> exps, ArrayList<Token> seps) {
        this.firstExp = firstExp;
        this.exps = exps;
        this.seps = seps;
    }

    public EqExp getFirstExp() {
        return firstExp;
    }

    public ArrayList<EqExp> getExps() {
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
        stringBuilder.append(firstExp).append("<LAndExp>\n");

        for (int i = 0; i < exps.size(); i++) {
            stringBuilder.append(seps.get(i)).append(exps.get(i)).append("<LAndExp>\n");
        }
        return stringBuilder.toString();
    }
}
