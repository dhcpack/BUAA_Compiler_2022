package Parser.expr.types;

import Config.IO;
import Parser.Output;

public class ConstExp implements Output {
    private final AddExp addExp;

    public ConstExp(AddExp addExp){
        this.addExp = addExp;
    }


    @Override
    public void output() {
        addExp.output();
        IO.print("<ConstExp>");
    }
}
