package Parser.expr.types;

import Config.IO;
import Parser.Output;

public class Cond implements Output {
    private final LOrExp lOrExp;

    public Cond(LOrExp lOrExp){
        this.lOrExp = lOrExp;
    }


    @Override
    public void output() {
        lOrExp.output();
        IO.print("<Cond>");
    }
}
