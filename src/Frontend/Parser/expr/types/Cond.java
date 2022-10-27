package Frontend.Parser.expr.types;

import Config.Reader;
import Config.Output;
import Config.SyntaxWriter;

public class Cond implements Output {
    // Cond -> LOrExp
    private final LOrExp lOrExp;

    public int getLine(){
        return lOrExp.getLine();
    }

    public Cond(LOrExp lOrExp){
        this.lOrExp = lOrExp;
    }

    public LOrExp getLOrExp() {
        return lOrExp;
    }

    @Override
    public void output() {
        lOrExp.output();
        SyntaxWriter.print("<Cond>");
    }
}
