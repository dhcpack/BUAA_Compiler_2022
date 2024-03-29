package Exceptions;

import Config.Config;

public class UndefinedTokenException implements MyException {
    private final ErrorType errorType = ErrorType.UNDEFINED_TOKEN;
    private final int line;

    public UndefinedTokenException(int line) {
        this.line = line;
        if (Config.debugMode) {
            System.out.printf("Line %d: using token undefined\n", line);
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
