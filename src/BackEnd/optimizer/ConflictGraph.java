// package BackEnd.optimizer;
//
// import Frontend.Symbol.Symbol;
// import Middle.type.BasicBlock;
//
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.HashSet;
// import java.util.LinkedHashMap;
//
// public class ConflictGraph {
//     private final ArrayList<BasicBlock> basicBlocks = new ArrayList<>();
//     private final LinkedHashMap<BasicBlock, HashSet<Symbol>> inSymbols = new LinkedHashMap<>();
//     private final LinkedHashMap<BasicBlock, HashSet<Symbol>> outSymbols = new LinkedHashMap<>();
//
//     // 保存图着色法的寄存器分配结果
//     // TODO: CHECK!!!三种结果：1.冲突图中分配寄存器symbolRegisterMap；2.冲突图中溢出overflowSymbol；3.不在冲突图中，可以任意分配寄存器
//     private final HashMap<Symbol, Integer> symbolRegisterMap = new HashMap<>();
//     private final HashSet<Symbol> overflowSymbol = new HashSet<>();
//     // private final HashSet<Integer> freeRegisters = new HashSet<>();  // 未参与着色的寄存器
//
//
//     public ConflictGraph(ArrayList<BasicBlock> basicBlocks, ArrayList<Symbol> params) {
//         for (int i = basicBlocks.size() - 1; i >= 0; i--) {
//             this.basicBlocks.add(basicBlocks.get(i));
//             BasicBlock block = basicBlocks.get(i);
//             inSymbols.put(block, new HashSet<>());
//             outSymbols.put(block, new HashSet<>());
//         }
//         //
//         // for (Symbol param : params) {
//         //
//         // }
//         getActiveVariableStream();
//     }
//
//     // 活跃变量数据流分析
//     // OUT[B] = U(B的后继p)(IN[p])
//     // IN[B] = USE[B] U (OUT[B] - DEF[B])
//     private void getActiveVariableStream() {
//         boolean flag = false;
//         for (BasicBlock block : basicBlocks) {
//             if (block.toString().equals("L_AND_EXP_13")) {
//                 System.out.println(1);
//             }
//             int outSize = outSymbols.get(block).size();
//             outSymbols.get(block).clear();
//             for (BasicBlock nextBlock : block.getNextBlock()) {
//                 outSymbols.get(block).addAll(inSymbols.get(nextBlock));
//             }
//             int inSize = inSymbols.get(block).size();
//             inSymbols.get(block).clear();
//             inSymbols.get(block).addAll(outSymbols.get(block));
//             inSymbols.get(block).removeAll(block.getDef());
//             inSymbols.get(block).addAll(block.getUse());
//             if (outSize != outSymbols.get(block).size() || inSize != inSymbols.get(block).size()) {
//                 flag = true;
//             }
//         }
//         if (flag) {
//             getActiveVariableStream();
//         }
//     }
// }