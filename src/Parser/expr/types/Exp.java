package Parser.expr.types;

import Config.IO;
import Parser.Output;

public class Exp implements Output {
    // Exp -> AddExp
    private final AddExp addExp;

    public Exp(AddExp addExp){
        this.addExp = addExp;
    }

    @Override
    public void output() {
        addExp.output();
        IO.print("<Exp>");
    }
}
