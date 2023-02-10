package Exceptions;

public enum ErrorType {
    ILLEGAL_SYMBOL("a"),
    REDEFINED_TOKEN("b"),
    UNDEFINED_TOKEN("c"),
    MISMATCH_PARAM_COUNT("d"),
    MISMATCH_PARAM_TYPE("e"),
    ILLEGAL_RETURN("f"),
    MISS_RETURN("g"),
    MODIFY_CONST("h"),
    MISS_SEMICN("i"),
    MISS_RPARENT("j"),
    MISS_RBRACK("k"),
    MISSMATCH_PRINTF("l"),
    ILLEGAL_BREAK_CONTINUE("m");

    private final String code;

    ErrorType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
