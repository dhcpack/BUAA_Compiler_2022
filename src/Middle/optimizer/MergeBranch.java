package Middle.optimizer;

import BackEnd.Translator;
import BackEnd.instructions.BranchInstr;
import Frontend.Symbol.Symbol;
import Middle.MiddleCode;
import Middle.type.BasicBlock;
import Middle.type.BlockNode;
import Middle.type.Branch;
import Middle.type.FourExpr;
import Middle.type.FuncBlock;
import Middle.type.Immediate;
import Middle.type.Operand;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class MergeBranch {


    public static void optimize(MiddleCode middleCode) {
        LinkedHashMap<FuncBlock, ArrayList<BasicBlock>> funcToSortedBlock = middleCode.getFuncToSortedBlock();
        for (Map.Entry<FuncBlock, ArrayList<BasicBlock>> fb : funcToSortedBlock.entrySet()) {
            ArrayList<BasicBlock> basicBlocks = fb.getValue();
            for (BasicBlock basicBlock : basicBlocks) {
                /*
                 *
                 *   EQ, a, b, 4  or NEQ  or LT or LE or GT or GE
                 *   Branch a ? IF_BODY_42 : IF_END_44
                 *
                 * Combine:
                 *   beq/bne/blt/ble/bgt/bge  $t1, $t2/imm label1
                 *   j label2
                 * */
                ArrayList<BlockNode> content = basicBlock.getContent();
                ArrayList<BlockNode> newContent = new ArrayList<>();
                // HashMap<Symbol, FourExpr> symbolToFourExpr
                for (int i = 0; i < content.size(); i++) {
                    BlockNode blockNode = content.get(i);
                    if (!(blockNode instanceof Branch) || i == 0) {
                        newContent.add(blockNode);
                        continue;
                    }
                    BlockNode formerBlock = content.get(i - 1);
                    if (!(formerBlock instanceof FourExpr)) {
                        newContent.add(blockNode);
                        continue;
                    }
                    FourExpr fourExpr = (FourExpr) formerBlock;
                    FourExpr.ExprOp op = fourExpr.getOp();
                    if (!(op == FourExpr.ExprOp.EQ || op == FourExpr.ExprOp.NEQ || op == FourExpr.ExprOp.LT ||
                            op == FourExpr.ExprOp.LE || op == FourExpr.ExprOp.GT || op == FourExpr.ExprOp.GE)) {
                        newContent.add(blockNode);
                        continue;
                    }
                    Operand left = fourExpr.getLeft();
                    Operand right = fourExpr.getRight();
                    BranchInstr.BranchType branchType = transfer(op);
                    if (left instanceof Immediate && right instanceof Immediate) {
                        newContent.add(blockNode);
                        continue;
                    }
                    if (left instanceof Immediate) {  // left转化为Symbol
                        Operand temp = right;
                        right = left;
                        left = temp;
                        branchType = getOppositeType(branchType);
                    }
                    Branch branch = (Branch) blockNode;
                    Branch newBranch = new Branch(branchType, (Symbol) left, right, branch.getThenBlock(), branch.getElseBlock(),
                            branch.isThenFirst());
                    newContent.remove(formerBlock);
                    newContent.add(newBranch);
                    // System.out.printf("Combine:\n\t%s\n\t%s\n==> %s\n", fourExpr, branch, newBranch);
                }
                basicBlock.setContent(newContent);
                fb.getKey().refreshBasicBlock();
            }
        }
    }

    // 求当前Cond交换左右操作数后的Cond符号
    private static BranchInstr.BranchType getOppositeType(BranchInstr.BranchType branchType) {
        if (branchType == BranchInstr.BranchType.beq) {
            return BranchInstr.BranchType.beq;
        } else if (branchType == BranchInstr.BranchType.bne) {
            return BranchInstr.BranchType.bne;
        } else if (branchType == BranchInstr.BranchType.blt) {
            return BranchInstr.BranchType.bgt;
        } else if (branchType == BranchInstr.BranchType.ble) {
            return BranchInstr.BranchType.bge;
        } else if (branchType == BranchInstr.BranchType.bgt) {
            return BranchInstr.BranchType.blt;
        } else if (branchType == BranchInstr.BranchType.bge) {
            return BranchInstr.BranchType.ble;
        } else {
            assert false;
            return null;
        }
    }


    private static BranchInstr.BranchType transfer(FourExpr.ExprOp op) {
        if (op == FourExpr.ExprOp.EQ) {
            return BranchInstr.BranchType.beq;
        } else if (op == FourExpr.ExprOp.NEQ) {
            return BranchInstr.BranchType.bne;
        } else if (op == FourExpr.ExprOp.LT) {
            return BranchInstr.BranchType.blt;
        } else if (op == FourExpr.ExprOp.LE) {
            return BranchInstr.BranchType.ble;
        } else if (op == FourExpr.ExprOp.GT) {
            return BranchInstr.BranchType.bgt;
        } else if (op == FourExpr.ExprOp.GE) {
            return BranchInstr.BranchType.bge;
        } else {
            assert false;
            return null;
        }
    }
}
