package Frontend.Parser.expr.types;

import Config.Reader;
import Config.Output;
import Config.SyntaxWriter;

public class ConstExp implements Output {
    // ConstExp -> Exp
    private final AddExp addExp;

    public AddExp getAddExp() {
        return addExp;
    }

    public ConstExp(AddExp addExp){
        this.addExp = addExp;
    }

    @Override
    public void output() {
        addExp.output();
        SyntaxWriter.print("<ConstExp>");
    }
}
