package Lexer;

public class Token {
    private final TokenType tokenType;
    private final int line;
    private final String content;

    public Token(TokenType tokenType, String content, int line) {
        this.tokenType = tokenType;
        this.content = content;
        this.line = line;
    }

    public TokenType getType() {
        return this.tokenType;
    }

    public String getTypeName() {
        return this.tokenType.toString();
    }

    public String getContent() {
        return this.content;
    }

    public int getLine() {
        return line;
    }

    public String toString() {
        return String.format("%s %s\n", this.tokenType.toString(), this.content);
    }
}
