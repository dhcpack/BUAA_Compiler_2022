package Parser.expr.types;

import Config.IO;
import Lexer.Token;
import Symbol.SymbolType;

public class Number implements PrimaryExpInterface, LeafNode {
    private final Token number;

    public Number(Token token) {
        this.number = token;
    }

    public int getLine() {
        return this.number.getLine();
    }

    @Override
    public void output() {
        IO.print(number.toString());
        IO.print("<Number>");
    }

    @Override
    public SymbolType getSymbolType() {
        return SymbolType.INT;
    }

    @Override
    public int getDims() {
        return 0;
    }
}
