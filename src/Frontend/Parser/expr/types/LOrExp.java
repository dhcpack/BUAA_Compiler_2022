package Frontend.Parser.expr.types;

import Frontend.Lexer.Token;

import java.util.ArrayList;

public class LOrExp{
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
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(firstExp).append("<LOrExp>\n");

        for (int i = 0; i < exps.size(); i++) {
            stringBuilder.append(seps.get(i)).append(exps.get(i)).append("<LOrExp>\n");
        }
        return stringBuilder.toString();
    }
}
