// package Middle.optimizer;
//
// import Frontend.Symbol.Symbol;
// import Middle.MiddleCode;
// import Middle.type.BasicBlock;
// import Middle.type.BlockNode;
// import Middle.type.FourExpr;
// import Middle.type.FuncBlock;
//
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.HashSet;
// import java.util.LinkedHashMap;
//
// public class ExtractCommonExpr {
//     public static void optimize(MiddleCode middleCode){
//         LinkedHashMap<FuncBlock, ArrayList<BasicBlock>> funcToSortedBlock = middleCode.getFuncToSortedBlock();
//         for (ArrayList<BasicBlock> basicBlocks:funcToSortedBlock.values()){
//             for (BasicBlock basicBlock:basicBlocks){
//                 HashMap<Symbol, Symbol> replaceSymbols = new HashMap<>();  // 符号 -> 被替换的符号
//                 /*
//                 * ADD, t1, t2, 0  t2->t1
//                 * SUB, t1, t2, 0  t2->t1
//                 * MUL, t1, t2, 1  t2->t1
//                 * */
//                 for (BlockNode blockNode:basicBlock.getContent()){
//                     if(!(blockNode instanceof FourExpr)){
//                         newContent.add(blockNode);
//                     } else {
//                         FourExpr curr = (FourExpr) blockNode;
//                         for (FourExpr former:formerExprs){
//                             if(curr.)
//                         }
//                     }
//                 }
//                 basicBlock.setContent(newContent);
//             }
//
//         }
//     }
// }
