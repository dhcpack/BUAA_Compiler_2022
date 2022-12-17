package Middle.optimizer;

import BackEnd.optimizer.ConflictGraph;
import Frontend.Symbol.Symbol;
import Middle.MiddleCode;
import Middle.type.BasicBlock;
import Middle.type.BlockNode;
import Middle.type.Branch;
import Middle.type.FourExpr;
import Middle.type.FuncBlock;
import Middle.type.Immediate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class DeleteUselessMiddleCode {
    public static void optimize(MiddleCode middleCode) {
        LinkedHashMap<FuncBlock, ArrayList<BasicBlock>> funcToSortedBlock = middleCode.getFuncToSortedBlock();
        for (Map.Entry<FuncBlock, ArrayList<BasicBlock>> fb : funcToSortedBlock.entrySet()) {
            ConflictGraph conflictGraph = new ConflictGraph(fb.getKey(), fb.getValue(), fb.getKey().getParams(), false);
            ArrayList<BasicBlock> basicBlocks = fb.getValue();
            for (BasicBlock basicBlock : basicBlocks) {
                /*
                 * FourExpr: Can calculate while compiling  --> RES
                 *
                 * */
                ArrayList<BlockNode> newContent = new ArrayList<>();
                ArrayList<BlockNode> content = basicBlock.getContent();
                for (int i = 0; i < content.size(); i++) {
                    BlockNode blockNode = content.get(i);
                    if (i == content.size() - 1) {
                        newContent.add(blockNode);
                        continue;
                    }
                    if (!(blockNode instanceof FourExpr && ((FourExpr) blockNode).isImmediate())) {
                        newContent.add(blockNode);
                        continue;
                    }
                    Symbol res = ((FourExpr) blockNode).getRes();
                    if (!(res.getScope() == Symbol.Scope.TEMP)) {
                        newContent.add(blockNode);
                        continue;
                    }
                    if (!fb.getKey().getSymbolUsageMap().containsKey(res) || fb.getKey().getSymbolUsageMap().get(res) != 1) {
                        newContent.add(blockNode);
                        continue;
                    }
                    // System.err.println(res);
                    Immediate immediate = calc((FourExpr) (blockNode));
                    BlockNode nextNode = content.get(i + 1);
                    if (nextNode instanceof FourExpr) {
                        FourExpr fourExpr = (FourExpr) nextNode;
                        if (fourExpr.isSingle()) {
                            if (fourExpr.getLeft() == res) {
                                fourExpr.setLeft(immediate);
                                System.err.printf("DELETE %s\n", blockNode);
                            } else {
                                newContent.add(blockNode);
                                continue;
                            }
                        } else {
                            if (fourExpr.getLeft() == res) {
                                fourExpr.setLeft(immediate);
                                System.err.printf("DELETE %s\n", blockNode);
                            } else if (fourExpr.getRight() == res) {
                                fourExpr.setRight(immediate);
                                System.err.printf("DELETE %s\n", blockNode);
                            } else {
                                newContent.add(blockNode);
                                continue;
                            }
                        }
                    } else if (nextNode instanceof Branch) {
                        Branch branch = (Branch) nextNode;
                        if (branch.getCond() == res) {
                            branch.setCond(immediate);
                            System.err.printf("DELETE %s\n", blockNode);
                        } else {
                            newContent.add(blockNode);
                            continue;
                        }
                    } else {
                        newContent.add(blockNode);
                        continue;
                    }
                }
                basicBlock.setContent(newContent);
                fb.getKey().refreshBasicBlock();
            }
        }
    }

    private static Immediate calc(FourExpr fourExpr) {
        FourExpr.ExprOp op = fourExpr.getOp();
        if (fourExpr.isSingle()) {
            int leftVal = ((Immediate) fourExpr.getLeft()).getNumber();
            if (op == FourExpr.ExprOp.DEF) {
                return new Immediate(leftVal);
            } else if (op == FourExpr.ExprOp.ASS) {
                return new Immediate(leftVal);
            } else if (op == FourExpr.ExprOp.NOT) {
                return new Immediate(leftVal != 0 ? 0 : 1);
            } else if (op == FourExpr.ExprOp.NEG) {
                return new Immediate(-leftVal);
            } else {
                assert false;
            }
            return null;
        } else {
            int leftVal = ((Immediate) fourExpr.getLeft()).getNumber();
            int rightVal = ((Immediate) fourExpr.getRight()).getNumber();
            if (op == FourExpr.ExprOp.ADD) {  //  零个寄存器，两个立即数
                return new Immediate(leftVal + rightVal);
            } else if (op == FourExpr.ExprOp.SUB) {
                return new Immediate(leftVal - rightVal);
            } else if (op == FourExpr.ExprOp.MUL) {
                return new Immediate(leftVal * rightVal);
            } else if (op == FourExpr.ExprOp.DIV) {
                return new Immediate(leftVal / rightVal);
            } else if (op == FourExpr.ExprOp.MOD) {
                return new Immediate(leftVal % rightVal);
            } else if (op == FourExpr.ExprOp.GT) {
                return new Immediate(leftVal > rightVal ? 1 : 0);
            } else if (op == FourExpr.ExprOp.GE) {
                return new Immediate(leftVal >= rightVal ? 1 : 0);
            } else if (op == FourExpr.ExprOp.LT) {
                return new Immediate(leftVal < rightVal ? 1 : 0);
            } else if (op == FourExpr.ExprOp.LE) {
                return new Immediate(leftVal <= rightVal ? 1 : 0);
            } else if (op == FourExpr.ExprOp.EQ) {
                return new Immediate(leftVal == rightVal ? 1 : 0);
            } else if (op == FourExpr.ExprOp.NEQ) {
                return new Immediate(leftVal != rightVal ? 1 : 0);
            } else if (op == FourExpr.ExprOp.OR) {
                return new Immediate(leftVal == 0 && rightVal == 0 ? 0 : 1);
            } else if (op == FourExpr.ExprOp.AND) {
                return new Immediate(leftVal == 0 || rightVal == 0 ? 0 : 1);
            } else {
                assert false;
            }
            return null;
        }
    }
}
