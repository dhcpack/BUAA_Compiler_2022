package Exceptions;

import Config.Config;

public class MissRbrackException implements MyException{
    private ErrorType errorType = ErrorType.MISS_RBRACK;
    private final int line;

    public MissRbrackException(int line) {
        this.line = line;
        if (Config.debugMode) {
            System.out.printf("Line %d: Miss a Rbrack\n", line);
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
