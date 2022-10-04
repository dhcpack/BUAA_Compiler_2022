package Parser.expr.types;

import Config.IO;
import Lexer.Token;
import Parser.Output;
import Symbol.SymbolType;

import java.util.ArrayList;

public class LVal implements PrimaryExpInterface, Output, LeafNode {
    // LVal → Ident {'[' Exp ']'}
    private final Token ident;
    private final ArrayList<Exp> exps;
    private final ArrayList<Token> bracks;

    public LVal(Token token, ArrayList<Exp> exps, ArrayList<Token> bracks) {
        this.ident = token;
        this.exps = exps;
        this.bracks = bracks;
    }

    // public int CountDim() {
    //     return exps.size();  // int--> dim = 0;
    // }

    public boolean missRBrack() {
        for (Token token : bracks) {
            if (token == null) return true;
        }
        return false;
    }

    public Token getIdent() {
        return ident;
    }

    @Override
    public int getLine() {
        return this.ident.getLine();
    }

    @Override
    public void output() {
        IO.print(ident.toString());
        int index = 0;
        for (Exp exp : exps) {
            IO.print(bracks.get(index++).toString());
            exp.output();
            IO.print(bracks.get(index++).toString());
        }
        IO.print("<LVal>");
    }

    @Override
    public SymbolType getSymbolType() {
        if (getDims() == 0) {
            return SymbolType.INT;
        } else {
            return SymbolType.ARRAY;
        }
    }

    @Override
    public int getDims() {
        return exps.size();
    }
}
