// package Middle.type;
//
// import Frontend.Parser.expr.types.LeafNode;
// import Frontend.Symbol.Symbol;
//
// public class UnaryExp extends BlockNode {
//     public enum UnaryOp {
//         DEF,  // int a = 1; int a = b;
//
//         NOT,
//     }
//
//     private LeafNode exp;
//     private int val;
//     private final Symbol res;
//     private final UnaryOp op;
//
//     public UnaryExp(LeafNode exp, Symbol res, UnaryOp op) {
//         this.exp = exp;
//         this.res = res;
//         this.op = op;
//     }
//
//     public UnaryExp(int val, Symbol res, UnaryOp op) {
//         this.val = val;
//         this.res = res;
//         this.op = op;
//     }
//
//     public LeafNode getExp() {
//         return exp;
//     }
//
//     public Symbol getRes() {
//         return res;
//     }
//
//     public UnaryOp getOp() {
//         return op;
//     }
// }