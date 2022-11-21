package Middle.optimizer;

import Frontend.Symbol.Symbol;
import Middle.type.BlockNode;
import Middle.type.Branch;
import Middle.type.FourExpr;
import Middle.type.FuncBlock;
import Middle.type.FuncCall;
import Middle.type.GetInt;
import Middle.type.Jump;
import Middle.type.Memory;
import Middle.type.Operand;
import Middle.type.Pointer;
import Middle.type.PrintInt;
import Middle.type.PrintStr;
import Middle.type.Return;

import java.util.HashSet;

/*
 * 得到每个BlockNode的DEF集和USE集
 * */
public class DefUseCalcUtil {
    private static HashSet<Symbol> defSet;
    private static HashSet<Symbol> useSet;

    // 记录全局变量（LOCAL变量）
    // DEF
    private static void addToDefSet(Operand operand) {
        if (!(operand instanceof Symbol && ((Symbol) operand).getScope() == Symbol.Scope.LOCAL)) {
            return;
        }
        Symbol localSymbol = (Symbol) operand;
        if (useSet.contains(localSymbol)) {
            return;
        }
        defSet.add(localSymbol);
    }

    // USE
    private static void addToUseSet(Operand operand) {
        if (!(operand instanceof Symbol && ((Symbol) operand).getScope() == Symbol.Scope.LOCAL)) {
            return;
        }
        Symbol localSymbol = (Symbol) operand;
        if (defSet.contains(localSymbol)) {
            return;
        }
        useSet.add(localSymbol);
    }

    public static void calcDefUse(BlockNode blockNode) {
        defSet = blockNode.getDefSet();
        useSet = blockNode.getUseSet();
        if (blockNode instanceof Branch) {
            // "Branch " + cond + " ? " + thenBlock + " : " + elseBlock;
            addToUseSet(((Branch) blockNode).getCond());
        } else if (blockNode instanceof FourExpr) {
            // this.op.name() + ", " + this.res + ", " + this.left + ", " + this.right;
            // this.op.name() + ", " + this.res + ", " + this.left;
            addToUseSet(((FourExpr) blockNode).getLeft());
            addToUseSet(((FourExpr) blockNode).getRight());
            addToDefSet(((FourExpr) blockNode).getRes());
        } else if (blockNode instanceof FuncBlock) {
            assert false : "不会出现FuncBlock";
            return;
        } else if (blockNode instanceof FuncCall) {
            // "Call %s; Params: %s"
            for (Operand operand : ((FuncCall) blockNode).getrParams()) {
                addToUseSet(operand);
            }
        } else if (blockNode instanceof GetInt) {
            // "GETINT " + target;
            GetInt getInt = (GetInt) blockNode;
            addToDefSet(getInt.getTarget());
        } else if (blockNode instanceof Jump) {
            // add To nextBlock
            return;
        } else if (blockNode instanceof Memory) {
            // "OFFSET (" + base + "+" + offset + ")->" + res;
            addToUseSet(((Memory) blockNode).getOffset());
            addToUseSet(((Memory) blockNode).getBase());
            addToDefSet(((Memory) blockNode).getRes());
        } else if (blockNode instanceof Pointer) {
            Pointer pointer = (Pointer) blockNode;
            addToUseSet(pointer.getPointer());
            if (pointer.getOp() == Pointer.Op.LOAD) {
                // "LOAD " + pointer + ", " + load;
                addToDefSet(pointer.getLoad());
            } else if (pointer.getOp() == Pointer.Op.STORE) {
                // "STORE " + pointer + ", " + store;
                addToUseSet(pointer.getStore());
            }
        } else if (blockNode instanceof PrintInt) {
            // "PRINT_INT " + val;
            addToUseSet(((PrintInt) blockNode).getVal());
        } else if (blockNode instanceof PrintStr) {
            return;
        } else if (blockNode instanceof Return) {
            // "RETURN " + returnVal;
            if (((Return) blockNode).hasReturnVal()) {
                addToUseSet(((Return) blockNode).getReturnVal());
            }
        } else {
            assert false;
        }
    }
}
