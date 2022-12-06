package BackEnd.optimizer;

import BackEnd.MipsCode;
import BackEnd.instructions.ALUDouble;
import BackEnd.instructions.Instruction;

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
                    if (addiu.getImmediate() == 0 && addiu.getrOperand() == addiu.getrResult()) {
                        return true;
                    }
                }
                return false;
            }
        });
    }
}
