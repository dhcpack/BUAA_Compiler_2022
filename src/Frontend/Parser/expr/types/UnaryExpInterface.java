package Frontend.Parser.expr.types;

// UnaryExp → PrimaryExp | FuncExp | UnaryOp UnaryExp
// 用于对两类左值的管理
public interface UnaryExpInterface {
    int getLine();
}
