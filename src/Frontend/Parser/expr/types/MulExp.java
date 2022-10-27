package Frontend.Parser.expr.types;

import Config.Reader;
import Config.SyntaxWriter;
import Frontend.Lexer.Token;
import Config.Output;

import java.util.ArrayList;

public class MulExp implements Output {
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
    public void output() {
        firstExp.output();
        SyntaxWriter.print("<MulExp>");
        for (int i = 0; i < exps.size(); i++) {
            SyntaxWriter.print(seps.get(i).toString());
            exps.get(i).output();
            SyntaxWriter.print("<MulExp>");
        }
    }
}
