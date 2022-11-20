// package BackEnd.optimizer;
//
// import BackEnd.MipsCode;
// import BackEnd.instructions.Comment;
// import BackEnd.instructions.Instruction;
// import BackEnd.instructions.J;
// import BackEnd.instructions.Label;
//
// import java.util.ArrayList;
// import java.util.function.Predicate;
//
// public class DeleteUselessJump {
//     public static void optimize(MipsCode mipsCode) {
//         ArrayList<Instruction> instructions = mipsCode.getInstructions();
//         instructions.removeIf(new Predicate<Instruction>() {
//             @Override
//             public boolean test(Instruction instruction) {
//                 if (!(instruction instanceof J)) return false;
//                 String label = ((J) instruction).getLabel();
//                 int index = instructions.indexOf(instruction);
//                 index++;
//                 while (index < instructions.size()) {
//                     if (instructions.get(index) instanceof Comment) {
//                         index++;
//                         continue;
//                     }
//                     if (!(instructions.get(index) instanceof Label)) return false;
//                     Label nextInstr = (Label) instructions.get(index);
//                     if (nextInstr.getLabel().equals(label)) {
//                         return true;
//                     }
//                     index++;
//                 }
//                 return false;
//             }
//         });
//         // for (int i = 0; i < instructions.size(); i++) {
//         //     Instruction instruction = instructions.get(i);
//         //     if (!(instruction instanceof J)) continue;
//         //     String label = ((J) instruction).getLabel();
//         //
//         //     // for (Map.Entry<FuncBlock, ArrayList<BasicBlock>> funcAndBlock : funcToSortedBlock.entrySet()) {
//         //     //     ArrayList<BasicBlock> blocks = funcAndBlock.getValue();
//         //     //     for (int i = 1; i < blocks.size(); i++) {
//         //     //         BasicBlock formerBlock = blocks.get(i - 1);
//         //     //         BasicBlock currentBlock = blocks.get(i);
//         //     //         if (formerBlock.getLastContent() != null) {
//         //     //             // 如果前一个基本块的最后一句是跳转语句并跳转到当前块，则跳转语句多余，删除该跳转语句
//         //     //             BlockNode lastNode = formerBlock.getLastContent();
//         //     //             if (lastNode instanceof Jump && ((Jump) lastNode).getTarget() == currentBlock) {
//         //     //                 formerBlock.getContent().remove(lastNode);
//         //     //             }
//         //     //         }
//         //     //     }
//         // }
//     }
// }
