package Parser.decl.types;

import Config.IO;
import Lexer.Token;
import Parser.Output;
import Parser.expr.types.ConstExp;
import Parser.expr.types.Exp;

import java.util.ArrayList;

public class Var implements Output {
    // Var -> Ident { '[' ConstExp ']' }
    private final Token ident;
    private final ArrayList<ConstExp> constExps;
    private final ArrayList<Token> bracks;

    public Var(Token token, ArrayList<ConstExp> constExps, ArrayList<Token> bracks) {
        this.ident = token;
        this.constExps = constExps;
        this.bracks = bracks;
    }

    @Override
    public void output() {
        IO.print(ident.toString());
        int index = 0;
        for (ConstExp constExp : constExps) {
            IO.print(bracks.get(index++).toString());
            constExp.output();
            IO.print(bracks.get(index++).toString());
        }
    }
}
