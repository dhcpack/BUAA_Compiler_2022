package Parser.expr.types;

import Lexer.Token;

import java.util.ArrayList;

public class LVal implements PrimaryExpInterface {
    private final String tag = "<LVal>";
    private final Token ident;
    private final ArrayList<Exp> dims = new ArrayList<>();

    public LVal(Token token) {
        this.ident = token;
    }

    public void addDim(Exp exp) {
        dims.add(exp);
    }

    public int CountDim() {
        return dims.size();  // int--> dim = 0;
    }
}
