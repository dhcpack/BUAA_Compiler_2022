package Frontend.Parser.expr.types;

import Frontend.Lexer.Token;

import java.util.ArrayList;

public class MulExp {
    // MulExp → UnaryExp {('*' | '/' | '%') UnaryExp}
    private final UnaryExp firstExp;
    private final ArrayList<UnaryExp> exps;
    private final ArrayList<Token> seps;

    public MulExp(UnaryExp firstExp, ArrayList<UnaryExp> exps, ArrayList<Token> seps) {
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

    public UnaryExp getFirstExp() {
        return firstExp;
    }

    public ArrayList<UnaryExp> getExps() {
        return exps;
    }

    public ArrayList<Token> getSeps() {
        return seps;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(firstExp).append("<MulExp>\n");  // correctly print BNF
        for (int i = 0; i < exps.size(); i++) {
            stringBuilder.append(seps.get(i)).append(exps.get(i)).append("<MulExp>\n");  // correctly print BNF
        }
        return stringBuilder.toString();
    }
}
