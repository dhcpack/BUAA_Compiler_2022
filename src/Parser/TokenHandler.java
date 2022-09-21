package Parser;

import Lexer.Token;
import Lexer.TokenList;

public class TokenHandler {
    private final TokenList tokenList;
    private int pointer;

    public TokenHandler(TokenList tokenList) {
        this.tokenList = tokenList;
        this.pointer = 0;
    }

    public Token getForwardToken() {
        assert !reachEnd();
        return tokenList.get(pointer);
    }

    public boolean moveForward(int step) {
        pointer = Math.min(pointer + step, tokenList.getSize());
        return reachEnd();
    }

    public Token getTokenAndMove() {
        return tokenList.get(pointer++);
    }

    public void retract(int step) {
        moveForward(-step);
    }

    public boolean reachEnd() {
        return this.pointer == tokenList.getSize();
    }
}
