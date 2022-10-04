package Parser.decl.types;

import Config.IO;
import Lexer.Token;
import Parser.Output;
import Parser.expr.types.ConstExp;
import Parser.expr.types.Exp;

import java.util.ArrayList;

public class InitVal implements Output {
    //  常量初值 ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    //  变量初值 InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'   // const or not
    private final Token left;
    private final ArrayList<InitVal> initVals;
    private final ArrayList<Token> seps;
    private final Token right;
    private final Exp exp;
    private final ConstExp constExp;
    private final boolean isConst;

    public InitVal(Token left, Token right, ArrayList<InitVal> initVals, ArrayList<Token> seps,
                   boolean isConst) {  // initial value
        this.exp = null;
        this.constExp = null;
        this.isConst = isConst;
        this.left = left;
        this.right = right;
        this.initVals = initVals;
        this.seps = seps;
    }

    public InitVal(Exp exp) {  // only expr
        this.exp = exp;
        this.constExp = null;
        this.isConst = false;
        this.left = null;
        this.initVals = null;
        this.seps = null;
        this.right = null;
    }

    public InitVal(ConstExp constExp) {  // only const expr
        this.constExp = constExp;
        this.exp = null;
        this.isConst = true;
        this.left = null;
        this.initVals = null;
        this.seps = null;
        this.right = null;
    }

    public ArrayList<InitVal> getInitVals() {
        return initVals;
    }

    public Exp getExp() {
        return exp;
    }

    public ConstExp getConstExp() {
        return constExp;
    }

    @Override
    public void output() {
        if (this.left != null) {
            IO.print(this.left.toString());
            int index = 0;
            if (initVals.size() != 0) {
                initVals.get(index++).output();
            }
            for (Token sep : seps) {
                IO.print(sep.toString());
                initVals.get(index++).output();
            }
            IO.print(this.right.toString());
        } else if (this.exp != null) {
            this.exp.output();
        } else if (this.constExp != null) {
            this.constExp.output();
        }
        if(isConst){
            IO.print("<ConstInitVal>");
        } else {
            IO.print("<InitVal>");
        }
    }
}
