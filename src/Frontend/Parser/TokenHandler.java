package Frontend.Parser;

import Frontend.Lexer.Token;
import Frontend.Lexer.TokenList;

public class TokenHandler {
    private final TokenList tokenList;
    private int pointer;

    public TokenHandler(TokenList tokenList) {
        this.tokenList = tokenList;
        this.pointer = 0;
    }

    public TokenList getTokenList() {
        return tokenList;
    }

    public Token getForwardToken() {
        assert !reachEnd();
        return tokenList.get(pointer);
    }

    public int getPointer() {
        return pointer;
    }

    public void setPointer(int pointer) {
        this.pointer = pointer;
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
