package Frontend.Parser.expr.types;

import Frontend.Lexer.Token;

import java.util.ArrayList;

// Warning: FuncRParams in FuncExp might be null
public class FuncRParams {
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

    public ArrayList<Token> getSeps() {
        return seps;
    }

    // already check size
    public int getLine() {
        return exps.get(exps.size() - 1).getLine();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        int index = 0;

        stringBuilder.append(exps.get(index++));
        for (Token sep : seps) {
            stringBuilder.append(sep).append(exps.get(index++));
        }
        stringBuilder.append("<FuncRParams>\n");
        return stringBuilder.toString();
    }
}
