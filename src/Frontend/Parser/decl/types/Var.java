package Frontend.Parser.decl.types;

import Frontend.Lexer.Token;
import Frontend.Parser.expr.types.ConstExp;

import java.util.ArrayList;

public class Var {
    // Var -> Ident { '[' ConstExp ']' }
    private final Token ident;
    private final ArrayList<ConstExp> constExps;
    private ArrayList<Integer> dimSize;
    private final ArrayList<Token> bracks; // error check: right could be null
    private final boolean isConst;

    public Var(Token token, ArrayList<ConstExp> constExps, ArrayList<Token> bracks, boolean isConst) {
        this.ident = token;
        this.constExps = constExps;
        this.bracks = bracks;
        this.isConst = isConst;
        // for (ConstExp constExp : constExps) {
        //     dimSize.add(CalcUtil.calcConstExp(constExp));
        // }
    }

    public ArrayList<ConstExp> getConstExps() {
        return constExps;
    }

    public boolean missRBrack() {
        for (Token token : bracks) {
            if (token == null) return true;
        }
        return false;
    }

    public ArrayList<Token> getBracks() {
        return bracks;
    }

    public Token getIdent() {
        return this.ident;
    }

    public void setDimSize(ArrayList<Integer> dimSize) {
        this.dimSize = dimSize;
    }

    public ArrayList<Integer> getDimSize() {
        return dimSize;
    }

    // 0->int 1+->array
    public int getDimCount() {
        if (constExps == null) {
            return 0;
        }
        return constExps.size();
    }

    public ArrayList<ConstExp> getDimExp() {
        return this.constExps;
    }

    public boolean isConst() {
        return this.isConst;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ident);
        int index = 0;
        for (ConstExp constExp : constExps) {  // print arrays
            stringBuilder.append(bracks.get(index++)).append(constExp).append(bracks.get(index++));
        }
        return stringBuilder.toString();
    }
}
