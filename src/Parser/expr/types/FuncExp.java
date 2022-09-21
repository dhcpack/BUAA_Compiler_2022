package Parser.expr.types;

import Lexer.Token;

import java.util.ArrayList;

public class FuncExp implements UnaryExpInterface {
    private final Token ident;
    private final ArrayList<Exp> params = new ArrayList<>();

    public FuncExp(Token token) {
        this.ident = token;
    }

    public void addParam(Exp exp) {
        this.params.add(exp);
    }
}
