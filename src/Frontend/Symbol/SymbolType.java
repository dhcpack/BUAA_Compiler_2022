package Frontend.Symbol;

public enum SymbolType {
    INT,
    ARRAY,
    FUNCTION,
    VOID,
    POINTER,
    SPECIAL_SP;

    @Override
    public String toString() {
        return this.name();
    }
}
