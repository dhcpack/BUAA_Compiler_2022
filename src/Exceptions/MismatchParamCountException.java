package Exceptions;

import Config.Config;

public class MismatchParamCountException implements MyException{
    private ErrorType errorType = ErrorType.MISMATCH_PARAM_COUNT;
    private final int line;

    public MismatchParamCountException(int line) {
        this.line = line;
        if (Config.debugMode) {
            System.out.printf("Line %d: MisMatch param count\n", line);
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
