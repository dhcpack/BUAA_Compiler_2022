package Middle.optimizer;

import BackEnd.optimizer.ConflictGraph;
import Frontend.Symbol.Symbol;
import Middle.MiddleCode;
import Middle.type.BasicBlock;
import Middle.type.BlockNode;
import Middle.type.Branch;
import Middle.type.FourExpr;
import Middle.type.FuncBlock;
import Middle.type.FuncCall;
import Middle.type.GetInt;
import Middle.type.Immediate;
import Middle.type.Jump;
import Middle.type.Memory;
import Middle.type.Operand;
import Middle.type.Pointer;
import Middle.type.PrintInt;
import Middle.type.PrintStr;
import Middle.type.Return;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExtractCommonExpr {
    private static HashMap<Symbol, HashSet<Symbol>> replaceToSymbols;  // 符号 -> 被替换的符号们
    private static HashMap<Symbol, Symbol> symbolToReplace;  // 被替换的符号 -> 符号
    private static ArrayList<BlockNode> newContent;
    private static BasicBlock currentBlock;

    public static void optimize(MiddleCode middleCode) {
        LinkedHashMap<FuncBlock, ArrayList<BasicBlock>> funcToSortedBlock = middleCode.getFuncToSortedBlock();
        for (Map.Entry<FuncBlock, ArrayList<BasicBlock>> fb : funcToSortedBlock.entrySet()) {
            ArrayList<BasicBlock> basicBlocks = fb.getValue();
            for (BasicBlock basicBlock : basicBlocks) {
                /*
                 * ADD, t1, t2, 0  t2->t1
                 * SUB, t1, t2, 0  t2->t1
                 * MUL, t1, t2, 1  t2->t1
                 * MOVE, t1, t2    t2->t1
                 * */
                replaceToSymbols = new HashMap<>();  // 符号 -> 被替换的符号们
                symbolToReplace = new HashMap<>();  // 被替换的符号 -> 符号
                newContent = new ArrayList<>();
                currentBlock = basicBlock;
                for (BlockNode blockNode : basicBlock.getContent()) {
                    if (blockNode instanceof FourExpr) {
                        FourExpr fourExpr = (FourExpr) blockNode;
                        if (((fourExpr.getOp() == FourExpr.ExprOp.ADD || fourExpr.getOp() == FourExpr.ExprOp.SUB) && fourExpr.getRight() instanceof Immediate && ((Immediate) fourExpr.getRight()).getNumber() == 0 && fourExpr.getLeft() instanceof Symbol) ||
                                ((fourExpr.getOp() == FourExpr.ExprOp.MUL || fourExpr.getOp() == FourExpr.ExprOp.DIV) && fourExpr.getRight() instanceof Immediate && ((Immediate) fourExpr.getRight()).getNumber() == 1 && fourExpr.getLeft() instanceof Symbol) ||
                                ((fourExpr.getOp() == FourExpr.ExprOp.DEF || fourExpr.getOp() == FourExpr.ExprOp.ASS) && fourExpr.getLeft() instanceof Symbol && fourExpr.getRight() instanceof Symbol)) {
                            Symbol left = (Symbol) fourExpr.getLeft();
                            Symbol res = fourExpr.getRes();
                            if (symbolToReplace.containsKey(left)) {
                                Symbol replace = symbolToReplace.get(left);
                                symbolToReplace.put(res, replace);
                                replaceToSymbols.get(replace).add(res);
                            } else {
                                symbolToReplace.put(res, left);
                                replaceToSymbols.put(left, new HashSet<>());
                                replaceToSymbols.get(left).add(res);
                            }
                            continue;  // 替换
                        }
                    }
                    if (blockNode instanceof Branch) {
                        // "Branch " + cond + " ? " + thenBlock + " : " + elseBlock;
                        ((Branch) blockNode).setCond(refreshRead(((Branch) blockNode).getCond()));
                        newContent.add(blockNode);
                    } else if (blockNode instanceof FourExpr) {
                        // this.op.name() + ", " + this.res + ", " + this.left + ", " + this.right;
                        // this.op.name() + ", " + this.res + ", " + this.left;
                        ((FourExpr) blockNode).setLeft(refreshRead(((FourExpr) blockNode).getLeft()));
                        refreshWrite(((FourExpr) blockNode).getRes());
                        if (!((FourExpr) blockNode).isSingle()) {
                            ((FourExpr) blockNode).setRight(refreshRead(((FourExpr) blockNode).getRight()));
                        }
                        newContent.add(blockNode);
                    } else if (blockNode instanceof FuncBlock) {
                        assert false : "不会出现FuncBlock";
                    } else if (blockNode instanceof FuncCall) {
                        // "Call %s; Params: %s"
                        ArrayList<Operand> rParams = ((FuncCall) blockNode).getrParams();
                        ArrayList<Operand> nRParams = new ArrayList<>();
                        for (Operand operand : rParams) {
                            nRParams.add(refreshRead(operand));
                        }
                        ((FuncCall) blockNode).setrParams(nRParams);
                        if (((FuncCall) blockNode).saveRet()) {
                            refreshWrite(((FuncCall) blockNode).getRet());
                        }
                        newContent.add(blockNode);
                    } else if (blockNode instanceof GetInt) {
                        // "GETINT " + target;
                        GetInt getInt = (GetInt) blockNode;
                        refreshWrite(getInt.getTarget());
                        newContent.add(blockNode);
                    } else if (blockNode instanceof Jump) {
                        newContent.add(blockNode);
                    } else if (blockNode instanceof Memory) {
                        // "OFFSET (" + base + "+" + offset + ")->" + res;
                        ((Memory) blockNode).setBase((Symbol) refreshRead(((Memory) blockNode).getBase()));
                        ((Memory) blockNode).setOffset(refreshRead(((Memory) blockNode).getOffset()));
                        refreshWrite(((Memory) blockNode).getRes());
                        newContent.add(blockNode);
                    } else if (blockNode instanceof Pointer) {
                        Pointer pointer = (Pointer) blockNode;
                        pointer.setPointer((Symbol) refreshRead(pointer.getPointer()));
                        if (pointer.getOp() == Pointer.Op.LOAD) {
                            // "LOAD " + pointer + ", " + load;
                            refreshWrite(pointer.getLoad());
                        } else if (pointer.getOp() == Pointer.Op.STORE) {
                            // "STORE " + pointer + ", " + store;
                            pointer.setStore(refreshRead(pointer.getStore()));
                        }
                        newContent.add(blockNode);
                    } else if (blockNode instanceof PrintInt) {
                        // "PRINT_INT " + val;
                        ((PrintInt) blockNode).setVal(refreshRead(((PrintInt) blockNode).getVal()));
                        newContent.add(blockNode);
                    } else if (blockNode instanceof PrintStr) {
                        newContent.add(blockNode);
                        return;
                    } else if (blockNode instanceof Return) {
                        // "RETURN " + returnVal;
                        if (((Return) blockNode).hasReturnVal()) {
                            ((Return) blockNode).setReturnVal(refreshRead(((Return) blockNode).getReturnVal()));
                        }
                        newContent.add(blockNode);
                    } else {
                        assert false;
                    }
                }
                if (newContent.size() != 0) {
                    ConflictGraph conflictGraph = new ConflictGraph(fb.getKey(), fb.getValue(), fb.getKey().getParams(), false);
                    BlockNode lastNode = newContent.get(newContent.size() - 1);
                    for (Symbol symbol : symbolToReplace.keySet()) {
                        if (conflictGraph.checkActive(symbol, lastNode) && symbol != symbolToReplace.get(symbol)) {
                            BlockNode newBlock = new FourExpr(symbolToReplace.get(symbol), symbol, FourExpr.ExprOp.ASS);
                            newBlock.setBelongBlock(currentBlock);
                            newContent.add(newContent.size() - 1, newBlock);
                            // System.out.printf("ADD %s\n", newBlock);
                        }
                    }
                }
                basicBlock.setContent(newContent);
                fb.getKey().refreshBasicBlock();
            }
        }
    }

    private static Operand refreshRead(Operand read) {
        if (!(read instanceof Symbol)) {
            return read;
        }
        Symbol readSymbol = (Symbol) read;
        if (symbolToReplace.containsKey(readSymbol)) {
            // System.out.printf("replace %s with %s\n", readSymbol, symbolToReplace.get(readSymbol));
            return symbolToReplace.get(readSymbol);
        }
        return readSymbol;
    }

    private static void refreshWrite(Symbol writeSymbol) {
        if (replaceToSymbols.containsKey(writeSymbol)) {
            // System.out.printf("free replace %s\n", writeSymbol);
            HashSet<Symbol> symbols = replaceToSymbols.get(writeSymbol);
            Symbol next = null;
            for (Symbol symbol : symbols) {
                next = symbol;
                BlockNode newNode = new FourExpr(writeSymbol, next, FourExpr.ExprOp.ASS);
                newNode.setBelongBlock(currentBlock);
                break;
            }
            replaceToSymbols.remove(writeSymbol);
            replaceToSymbols.put(next, new HashSet<>());
            for (Symbol symbol : symbols) {
                if (symbol != next) {
                    symbolToReplace.put(symbol, next);
                    replaceToSymbols.get(next).add(symbol);
                } else {
                    symbolToReplace.remove(symbol);
                }
            }
        } else if (symbolToReplace.containsKey(writeSymbol)) {
            // System.out.printf("free replace %s\n", writeSymbol);
            Symbol replace = symbolToReplace.get(writeSymbol);
            symbolToReplace.remove(writeSymbol);
            replaceToSymbols.get(replace).remove(writeSymbol);
        }
    }
}
