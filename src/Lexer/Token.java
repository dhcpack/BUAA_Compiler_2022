package Lexer;

public class Token {
    private final Type type;
    private final int line;
    private final String content;

    public Token(Type type, String content, int line) {
        this.type = type;
        this.content = content;
        this.line = line;
    }

    public Type getType() {
        return this.type;
    }

    public String getTypeName() {
        return this.type.toString();
    }

    public String getContent() {
        return this.content;
    }

    public int getLine() {
        return line;
    }

    public String toString() {
        return String.format("%s %s\n", this.type.toString(), this.content);
    }
}
