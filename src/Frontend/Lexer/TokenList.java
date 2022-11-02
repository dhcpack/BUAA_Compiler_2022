package Frontend.Lexer;

import java.util.ArrayList;

public class TokenList {
    private final ArrayList<Token> tokenList = new ArrayList<>();

    public void add(Token token) {
        this.tokenList.add(token);
    }

    public Token get(int index) {
        return tokenList.get(index);
    }

    public int getSize() {
        return tokenList.size();
    }

    public String toString() {
        StringBuilder res = new StringBuilder();
        for (Token token : tokenList) {
            res.append(String.format("%s %s\n", token.getTypeName(), token.getContent()));
        }
        return res.toString();
    }
}