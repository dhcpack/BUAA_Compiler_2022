package Exceptions;

import Config.Config;

public class IllegalReturnException implements MyException{
    private ErrorType errorType = ErrorType.ILLEGAL_RETURN;
    private final int line;

    public IllegalReturnException(int line) {
        this.line = line;
        if (Config.debugMode) {
            System.out.printf("Line %d: return stmt exist in a void function\n", line);
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
