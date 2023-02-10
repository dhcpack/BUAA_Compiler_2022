package Exceptions;

import Config.Config;

public class IllegalBreakContinueException implements MyException {
    private final ErrorType errorType = ErrorType.ILLEGAL_BREAK_CONTINUE;
    private final int line;

    public IllegalBreakContinueException(int line) {
        this.line = line;
        if (Config.debugMode) {
            System.out.printf("Line %d: Break or Continue out of loop block\n", line);
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
