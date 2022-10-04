package Parser.decl.types;

import Config.IO;
import Lexer.Token;
import Parser.Output;
import Parser.expr.types.ConstExp;

import java.util.ArrayList;

public class Var implements Output {
    // Var -> Ident { '[' ConstExp ']' }
    private final Token ident;
    private final ArrayList<ConstExp> constExps;
    private final ArrayList<Token> bracks; // error check: right could be null
    private final boolean isConst;

    public Var(Token token, ArrayList<ConstExp> constExps, ArrayList<Token> bracks, boolean isConst) {
        this.ident = token;
        this.constExps = constExps;
        this.bracks = bracks;
        this.isConst = isConst;
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

    public ArrayList<ConstExp> getDims() {
        return constExps;
    }

    public boolean isConst() {
        return this.isConst;
    }

    @Override
    public void output() {
        IO.print(ident.toString());
        int index = 0;
        for (ConstExp constExp : constExps) {  // print arrays
            IO.print(bracks.get(index++).toString());
            constExp.output();
            IO.print(bracks.get(index++).toString());
        }
    }
}
