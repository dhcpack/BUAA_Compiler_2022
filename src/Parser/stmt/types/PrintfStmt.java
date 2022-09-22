package Parser.stmt.types;

import Config.IO;
import Lexer.Token;
import Parser.expr.types.Exp;

import java.util.ArrayList;

public class PrintfStmt implements StmtInterface {
    // 'printf''('FormatString{','Exp}')'';'
    private final Token printf;
    private final Token left;
    private final Token formatString;
    private final ArrayList<Token> seps;
    private final ArrayList<Exp> exps;
    private final Token right;

    public PrintfStmt(Token printf, Token left, Token formatString, ArrayList<Token> seps, ArrayList<Exp> exps,
                      Token right) {
        this.printf = printf;
        this.left = left;
        this.formatString = formatString;
        this.seps = seps;
        this.exps = exps;
        this.right = right;
    }

    @Override
    public void output() {
        IO.print(printf.toString());
        IO.print(left.toString());
        IO.print(formatString.toString());
        for (int i = 0; i < seps.size(); i++) {
            IO.print(seps.get(i).toString());
            exps.get(i).output();
        }
        IO.print(right.toString());
    }
}
