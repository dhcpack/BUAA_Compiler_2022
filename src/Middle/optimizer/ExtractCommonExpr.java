// package Middle.optimizer;
//
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
//                 ArrayList<BlockNode> newContent = new ArrayList<>();
//                 HashSet<FourExpr> formerExprs = new HashSet<>();
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
