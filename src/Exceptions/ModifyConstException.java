package Exceptions;

import Config.Config;

public class ModifyConstException implements MyException{
    private ErrorType errorType = ErrorType.MODIFY_CONST;
    private final int line;

    public ModifyConstException(int line) {
        this.line = line;
        if (Config.debugMode) {
            System.out.printf("Line %d: modify const\n", line);
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
