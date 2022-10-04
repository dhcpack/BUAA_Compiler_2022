package Exceptions;

import Config.Config;

public class IllegalSymbolException implements MyException {
    private ErrorType errorType = ErrorType.ILLEGAL_SYMBOL;
    private final int line;

    public IllegalSymbolException(int line) {
        this.line = line;
        if (Config.debugMode) {
            System.out.printf("Line %d: illegal symbol in format string\n", line);
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
