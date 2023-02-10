package BackEnd.optimizer;

import BackEnd.MipsCode;
import BackEnd.instructions.ALUDouble;
import BackEnd.instructions.Instruction;
import BackEnd.instructions.MoveInstr;

import java.util.ArrayList;
import java.util.function.Predicate;

public class DeleteUselessMips {
    public static void optimize(MipsCode mipsCode) {
        ArrayList<Instruction> instructions = mipsCode.getInstructions();
        instructions.removeIf(new Predicate<Instruction>() {
            @Override
            public boolean test(Instruction instruction) {
                if (instruction instanceof ALUDouble && ((ALUDouble) instruction).getAluDoubleType() == ALUDouble.ALUDoubleType.addiu) {
                    ALUDouble addiu = (ALUDouble) instruction;
                    return addiu.getImmediate() == 0 && addiu.getrOperand() == addiu.getrResult();
                } else if (instruction instanceof MoveInstr) {
                    MoveInstr moveInstr = (MoveInstr) instruction;
                    return moveInstr.getSource() == moveInstr.getTarget();
                }
                return false;
            }
        });
    }
}
