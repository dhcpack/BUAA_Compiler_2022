package Exceptions;

import Config.Config;

public class MismatchPrintfException implements MyException{
    private ErrorType errorType = ErrorType.MISSMATCH_PRINTF;
    private final int line;

    public MismatchPrintfException(int line) {
        this.line = line;
        if (Config.debugMode) {
            System.out.printf("Line %d: Printf count not match\n", line);
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
