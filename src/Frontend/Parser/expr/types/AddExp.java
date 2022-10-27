package Frontend.Parser.expr.types;

import Config.Reader;
import Config.SyntaxWriter;
import Frontend.Lexer.Token;
import Config.Output;

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

    public ArrayList<Token> getSeps() {
        return seps;
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
        SyntaxWriter.print("<AddExp>");  // correctly print BNF
        for (int i = 0; i < exps.size(); i++) {
            SyntaxWriter.print(seps.get(i).toString());
            exps.get(i).output();
            SyntaxWriter.print("<AddExp>");
        }
    }
}
