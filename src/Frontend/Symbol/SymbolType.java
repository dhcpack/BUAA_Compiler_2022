package Frontend.Symbol;

public enum SymbolType {
    INT,
    ARRAY,
    FUNCTION,
    VOID,
    POINTER;

    @Override
    public String toString() {
        return this.name();
    }
}
