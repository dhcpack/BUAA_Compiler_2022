package BackEnd.optimizer;

import BackEnd.MipsCode;
import BackEnd.instructions.Comment;
import BackEnd.instructions.Instruction;
import BackEnd.instructions.J;
import BackEnd.instructions.Label;

import java.util.ArrayList;
import java.util.function.Predicate;

public class DeleteUselessJump {
    public static void optimize(MipsCode mipsCode) {
        ArrayList<Instruction> instructions = mipsCode.getInstructions();
        instructions.removeIf(new Predicate<Instruction>() {
            @Override
            public boolean test(Instruction instruction) {
                if (!(instruction instanceof J)) return false;
                String label = ((J) instruction).getLabel();
                int index = instructions.indexOf(instruction);
                index++;
                while (index < instructions.size()) {
                    if (instructions.get(index) instanceof Comment) {
                        index++;
                        continue;
                    }
                    if (!(instructions.get(index) instanceof Label)) return false;
                    Label nextInstr = (Label) instructions.get(index);
                    if (nextInstr.getLabel().equals(label)) {
                        return true;
                    }
                    index++;
                }
                return false;
            }
        });
    }
}
