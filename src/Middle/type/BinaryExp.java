// package Middle.type;
//
// import Frontend.Parser.expr.types.LeafNode;
// import Frontend.Symbol.Symbol;
//
// public class BinaryExp extends BlockNode{
//     public enum BinaryOp {
//         DEF,  // int a = b + c;
//
//         ADD,
//         SUB,
//         MUL,
//         DIV,
//
//         AND,
//         OR,
//
//         GT,
//         GE,
//         LT,
//         LE,
//         EQ,
//         NE,
//     }
//
//     private final LeafNode left;
//     private final LeafNode right;
//     private final Symbol res;
//     private final BinaryOp op;
//
//     public BinaryExp(LeafNode left, LeafNode right, Symbol res, BinaryOp op) {
//         this.left = left;
//         this.right = right;
//         this.res = res;
//         this.op = op;
//     }
//
//     public LeafNode getLeft() {
//         return left;
//     }
//
//     public LeafNode getRight() {
//         return right;
//     }
//
//     public Symbol getRes() {
//         return res;
//     }
//
//     public BinaryOp getOp() {
//         return op;
//     }
// }
