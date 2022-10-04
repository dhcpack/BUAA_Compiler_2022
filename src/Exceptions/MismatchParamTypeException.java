package Exceptions;

import Config.Config;

public class MismatchParamTypeException implements MyException{
    private final ErrorType errorType = ErrorType.MISMATCH_PARAM_TYPE;
    private final int line;

    public MismatchParamTypeException(int line) {
        this.line = line;
        if (Config.debugMode) {
            System.out.printf("Line %d: MisMatch param type\n", line);
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
