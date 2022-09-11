import java.util.ArrayList;
import java.util.List;

public class TokenList {
    private final List<Token> tokenList = new ArrayList<>();

    public void add(Token token) {
        this.tokenList.add(token);
    }

    public String toString() {
        StringBuilder res = new StringBuilder();
        for (Token token : tokenList) {
            res.append(String.format("%s %s\n", token.getTypeName(), token.getContent()));
        }
        return res.toString();
    }
}