package Parser.decl.types;

import Config.IO;
import Parser.Output;
import Parser.expr.types.ConstExp;
import Parser.expr.types.Exp;

import java.util.ArrayList;

public class InitVal implements Output {
    private ArrayList<InitVal> initVals = new ArrayList<>();
    private final Exp exp;
    private final ConstExp constExp;
    private final boolean isConst;

    public InitVal(boolean isConst) {  // empty constructor and addInitVal
        this.exp = null;
        this.constExp = null;
        this.isConst = isConst;
    }

    public InitVal(Exp exp) {  // only expr
        this.exp = exp;
        this.constExp = null;
        this.isConst = false;
    }

    public InitVal(ConstExp constExp) {  // only const expr
        this.constExp = constExp;
        this.exp = null;
        this.isConst = true;
    }

    public void addInitVal(InitVal initVal) {
        initVals.add(initVal);
    }

    public boolean hasNext() {
        return this.initVals.size() != 0;
    }

    @Override
    public void output() {
        if (constExp == null && exp == null) {
            for (InitVal initVal : initVals) {
                initVal.output();
            }
        } else {
            if (isConst) {
                constExp.output();
            } else {
                exp.output();
            }
        }
        IO.print("<InitVal>");
    }
}
