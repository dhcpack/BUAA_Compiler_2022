package Parser.expr.types;

import Config.IO;
import Parser.Output;

public class Cond implements Output {
    // Cond -> LOrExp
    private final LOrExp lOrExp;

    public int getLine(){
        return lOrExp.getLine();
    }

    public Cond(LOrExp lOrExp){
        this.lOrExp = lOrExp;
    }

    @Override
    public void output() {
        lOrExp.output();
        IO.print("<Cond>");
    }
}
