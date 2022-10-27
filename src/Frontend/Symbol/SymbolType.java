package Frontend.Symbol;

public enum SymbolType {
    INT,
    ARRAY,
    FUNCTION,
    VOID,
    POINTER,
    BOOL;

    @Override
    public String toString() {
        return this.name();
    }
}
