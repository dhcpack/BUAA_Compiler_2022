package Frontend.Parser.expr.types;

import Config.Reader;
import Config.SyntaxWriter;
import Frontend.Lexer.Token;
import Config.Output;

import java.util.ArrayList;

public class RelExp implements Output {
    // RelExp → AddExp {('<' | '>' | '<=' | '>=') AddExp}
    private final AddExp firstExp;
    private final ArrayList<AddExp> exps;
    private final ArrayList<Token> seps;

    public RelExp(AddExp firstExp, ArrayList<AddExp> exps, ArrayList<Token> seps) {
        this.firstExp = firstExp;
        this.exps = exps;
        this.seps = seps;
    }

    public AddExp getFirstExp() {
        return firstExp;
    }

    public ArrayList<AddExp> getExps() {
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
        SyntaxWriter.print("<RelExp>");
        for (int i=0;i<exps.size();i++){
            SyntaxWriter.print(seps.get(i).toString());
            exps.get(i).output();
            SyntaxWriter.print("<RelExp>");
        }
    }
}
