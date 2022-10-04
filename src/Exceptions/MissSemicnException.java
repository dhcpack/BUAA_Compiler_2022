package Exceptions;

import Config.Config;

public class MissSemicnException implements MyException{
    private ErrorType errorType = ErrorType.MISS_SEMICN;
    private final int line;

    public MissSemicnException(int line) {
        this.line = line;
        if (Config.debugMode) {
            System.out.printf("Line %d: Miss a semicolon\n", line);
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
