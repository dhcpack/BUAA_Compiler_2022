package Exceptions;

import Config.Config;

public class MissReturnException implements MyException{
    private ErrorType errorType = ErrorType.MISS_RETURN;
    private final int line;

    public MissReturnException(int line) {
        this.line = line;
        if (Config.debugMode) {
            System.out.printf("Line %d: Miss a return stmt\n", line);
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
