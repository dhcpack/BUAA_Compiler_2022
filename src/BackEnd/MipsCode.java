package BackEnd;

import BackEnd.instructions.Instruction;
import BackEnd.instructions.Label;
import Config.MipsWriter;
import Config.Output;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

public class MipsCode implements Output {
    private ArrayList<Integer> globalWords = new ArrayList<>();
    private LinkedHashMap<String, String> globalStrings = new LinkedHashMap<>();  // label, string
    private ArrayList<Instruction> instructions = new ArrayList<>();

    public void setGlobalWords(ArrayList<Integer> globalWords) {
        this.globalWords = globalWords;
    }

    public void setGlobalStrings(LinkedHashMap<String, String> globalStrings) {
        this.globalStrings = globalStrings;
    }

    public void addInstr(Instruction instruction) {
        this.instructions.add(instruction);
    }

    public void addInstrs(ArrayList<Instruction> instructions) {
        this.instructions.addAll(instructions);
    }

    public ArrayList<Instruction> getInstructions() {
        return this.instructions;
    }

    public void setInstructions(ArrayList<Instruction> instructions) {
        this.instructions = instructions;
    }

    public void output(PrintStream printStream) {
        printStream.println("# Yuelin's Compiler");
        printStream.println();
        printStream.println(".data");
        printStream.print("\tglobal:\n");
        StringJoiner wordJoiner = new StringJoiner(" ");
        for (Integer globalWord : globalWords) {
            wordJoiner.add(String.valueOf(globalWord));
        }
        printStream.printf("\t" + wordJoiner + "\n");
        printStream.print("\t.space 4\n");
        for (Map.Entry<String, String> globalString : globalStrings.entrySet()) {
            printStream.print("\t" + String.format("%s: .asciiz \"%s\"\n", globalString.getKey(), globalString.getValue()));
        }
        printStream.print("\n.text\n");
        printStream.print("\tla $gp, global\n");
        printStream.print("\tj FUNC_main\n");

        for (Instruction instruction : instructions) {
            if (instruction instanceof Label) {
                printStream.print(instruction.toString() + "\n");
            } else {
                printStream.print("\t" + instruction.toString());
            }
        }

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
        // clear!!!
        // MipsWriter.string = "";
        try {
            MipsWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
