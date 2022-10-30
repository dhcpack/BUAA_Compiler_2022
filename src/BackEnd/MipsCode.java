package BackEnd;

import BackEnd.instructions.Instruction;
import BackEnd.instructions.Label;
import Config.MipsWriter;
import Config.Output;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class MipsCode implements Output {
    private ArrayList<Integer> globalWords = new ArrayList<>();
    private HashMap<String, String> globalStrings = new HashMap<>();  // label, string
    private final ArrayList<Instruction> instructions = new ArrayList<>();

    public void setGlobalWords(ArrayList<Integer> globalWords) {
        this.globalWords = globalWords;
    }

    public void setGlobalStrings(HashMap<String, String> globalStrings) {
        this.globalStrings = globalStrings;
    }

    public void addInstr(Instruction instruction) {
        this.instructions.add(instruction);
    }

    @Override
    public void output() {
        MipsWriter.print("# Yuelin's Compiler", "title");
        MipsWriter.print("\n");
        MipsWriter.print(".data", "segment");
        MipsWriter.print("global:");
        StringJoiner wordJoiner = new StringJoiner(" ");
        for (Integer globalWord : globalWords) {
            wordJoiner.add(String.valueOf(globalWord));
        }
        MipsWriter.print(String.valueOf(wordJoiner));
        MipsWriter.print(".space 4");
        for (Map.Entry<String, String> globalString : globalStrings.entrySet()) {
            MipsWriter.print(String.format("%s: .asciiz \"%s\"", globalString.getKey(), globalString.getValue()));
        }

        MipsWriter.print(".text", "segment");
        MipsWriter.print("la $gp, global");
        MipsWriter.print("j FUNC_main");
        for (Instruction instruction : instructions) {
            if (instruction instanceof Label) {
                MipsWriter.print(instruction.toString(), "label");
            } else {
                MipsWriter.print(instruction.toString());
            }
        }
        try {
            MipsWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
