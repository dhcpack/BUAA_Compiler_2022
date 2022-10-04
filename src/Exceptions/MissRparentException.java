package Exceptions;

import Config.Config;

public class MissRparentException implements MyException{
    private ErrorType errorType = ErrorType.MISS_RPARENT;
    private final int line;

    public MissRparentException(int line) {
        this.line = line;
        if (Config.debugMode) {
            System.out.printf("Line %d: Miss a Rparent\n", line);
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
