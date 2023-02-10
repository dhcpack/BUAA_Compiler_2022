package BackEnd.optimizer;

import BackEnd.MipsCode;
import BackEnd.instructions.BranchInstr;
import BackEnd.instructions.Instruction;

import java.util.ArrayList;

public class OptimizeBranch {
    public static void optimize(MipsCode mipsCode) {
        ArrayList<Instruction> instructions = mipsCode.getInstructions();
        ArrayList<Instruction> newInstruction = new ArrayList<>();
        for (Instruction instruction : instructions) {
            if (instruction instanceof BranchInstr) {
                BranchInstr branchInstr = (BranchInstr) instruction;
                if (branchInstr.isCalcBranch() && branchInstr.isNumber() && branchInstr.getNumber() == 0) {
                    BranchInstr.BranchType branchType = branchInstr.getBranchType();
                    if (branchType == BranchInstr.BranchType.beq) {
                        newInstruction.add(new BranchInstr(BranchInstr.BranchType.beqz, branchInstr.getrOperand1(), branchInstr.getLabel()));
                    } else if (branchType == BranchInstr.BranchType.bne) {
                        newInstruction.add(new BranchInstr(BranchInstr.BranchType.bnez, branchInstr.getrOperand1(), branchInstr.getLabel()));
                    } else if (branchType == BranchInstr.BranchType.blt) {
                        newInstruction.add(new BranchInstr(BranchInstr.BranchType.bltz, branchInstr.getrOperand1(), branchInstr.getLabel()));
                    } else if (branchType == BranchInstr.BranchType.ble) {
                        newInstruction.add(new BranchInstr(BranchInstr.BranchType.blez, branchInstr.getrOperand1(), branchInstr.getLabel()));
                    } else if (branchType == BranchInstr.BranchType.bgt) {
                        newInstruction.add(new BranchInstr(BranchInstr.BranchType.bgtz, branchInstr.getrOperand1(), branchInstr.getLabel()));
                    } else if (branchType == BranchInstr.BranchType.bge) {
                        newInstruction.add(new BranchInstr(BranchInstr.BranchType.bgez, branchInstr.getrOperand1(), branchInstr.getLabel()));
                    } else {
                        assert false;
                    }
                    continue;
                }
            }
            newInstruction.add(instruction);
        }
        mipsCode.setInstructions(newInstruction);
    }
}