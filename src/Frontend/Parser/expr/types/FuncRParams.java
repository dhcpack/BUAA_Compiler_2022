package Frontend.Parser.expr.types;

import Config.Reader;
import Config.SyntaxWriter;
import Frontend.Lexer.Token;
import Config.Output;

import java.util.ArrayList;

// Warning: FuncRParams in FuncExp might be null
public class FuncRParams implements Output {
    // 函数实参表 FuncRParams → Exp { ',' Exp }
    private final ArrayList<Exp> exps;
    private final ArrayList<Token> seps;

    public FuncRParams(ArrayList<Exp> exps, ArrayList<Token> seps) {
        this.exps = exps;
        this.seps = seps;
    }

    public ArrayList<Exp> getExps() {
        return exps;
    }

    // already check size
    public int getLine() {
        return exps.get(exps.size() - 1).getLine();
    }

    @Override
    public void output() {
        int index = 0;
        exps.get(index++).output();

        for (Token sep : seps) {
            SyntaxWriter.print(sep.toString());
            exps.get(index++).output();
        }
        SyntaxWriter.print("<FuncRParams>");
    }
}
