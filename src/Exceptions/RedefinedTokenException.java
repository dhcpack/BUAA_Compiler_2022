package Exceptions;

import Config.Config;

public class RedefinedTokenException implements MyException{
    private ErrorType errorType = ErrorType.REDEFINED_TOKEN;
    private final int line;

    public RedefinedTokenException(int line) {
        this.line = line;
        if (Config.debugMode) {
            System.out.printf("Line %d: redefined a token\n", line);
        }
    }

    @Override
    public ErrorType getType() {
        return this.errorType;
    }

    @Override
    public int getLine() {
        return this.line;
    }
}
