package Frontend.Parser.expr.types;

import Frontend.Lexer.Token;
import Frontend.Symbol.SymbolType;

import java.util.ArrayList;

public class Number implements PrimaryExpInterface, LeafNode {
    private final Token number;

    public Number(Token token) {
        this.number = token;
    }

    public int getLine() {
        return this.number.getLine();
    }

    public int getNumber() {
        return Integer.parseInt(number.getContent());
    }

    @Override
    public String toString() {
        return number.toString() + "<Number>\n";
    }

    @Override
    public SymbolType getSymbolType() {
        return SymbolType.INT;
    }

    @Override
    public int getDimCount() {
        return 0;
    }

    @Override
    public ArrayList<Integer> getDimSize() {
        return new ArrayList<>();
    }
}
