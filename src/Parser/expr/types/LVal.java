package Parser.expr.types;

import Config.IO;
import Lexer.Token;
import Parser.Output;

import java.util.ArrayList;

public class LVal implements PrimaryExpInterface, Output {
    // LVal → Ident {'[' Exp ']'} //1.普通变量 2.一维数组 3.二维数组
    // private final String tag = "<LVal>";
    private final Token ident;
    private final ArrayList<Exp> exps;
    private final ArrayList<Token> bracs;

    public LVal(Token token, ArrayList<Exp> exps, ArrayList<Token> bracs) {
        this.ident = token;
        this.exps = exps;
        this.bracs = bracs;
    }

    // public void addDim(Exp exp) {
    //     dims.add(exp);
    // }

    public int CountDim() {
        return exps.size();  // int--> dim = 0;
    }

    @Override
    public void output() {
        IO.print(ident.toString());
        int index = 0;
        for (Exp exp : exps) {
            IO.print(bracs.get(index++).toString());
            exp.output();
            IO.print(bracs.get(index++).toString());
        }
        IO.print("<LVal>");
    }
}
