package Frontend.Parser.decl.types;

import Frontend.Lexer.Token;
import Frontend.Parser.expr.types.ConstExp;
import Frontend.Parser.expr.types.Exp;

import java.util.ArrayList;

public class InitVal{
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

    public Token getLeft() {
        return left;
    }

    public ArrayList<Token> getSeps() {
        return seps;
    }

    public Token getRight() {
        return right;
    }

    public boolean isLeaf() {
        return this.exp != null || this.constExp != null;
    }

    public boolean isConst() {
        return isConst;
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
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (this.left != null) {
            stringBuilder.append(this.left);
            int index = 0;
            assert initVals != null;
            if (initVals.size() != 0) {
                stringBuilder.append(initVals.get(index++));
            }
            assert seps != null;
            for (Token sep : seps) {
                stringBuilder.append(sep);
                stringBuilder.append(initVals.get(index++));
            }
            stringBuilder.append(this.right);
        } else if (this.exp != null) {
            stringBuilder.append(this.exp);
        } else if (this.constExp != null) {
            stringBuilder.append(this.constExp);
        }
        if (isConst) {
            stringBuilder.append("<ConstInitVal>\n");
        } else {
            stringBuilder.append("<InitVal>\n");
        }
        return stringBuilder.toString();
    }
}
