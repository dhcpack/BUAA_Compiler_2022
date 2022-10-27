package Middle;

import Config.MiddleWriter;
import Config.Output;
import Middle.type.BasicBlock;
import Middle.type.BlockNode;
import Middle.type.Branch;
import Middle.type.FuncBlock;
import Middle.type.Jump;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.StringJoiner;

public class MiddleCode implements Output {
    private final HashMap<String, Integer> nameToAddr = new HashMap<>();
    private final HashMap<String, Integer> nameToVal = new HashMap<>();
    private final HashMap<String, String> nameToAsciiz = new HashMap<>();
    private final HashMap<String, String> AsciizToName = new HashMap<>();
    private final HashMap<String, ArrayList<Integer>> nameToArray = new HashMap<>();
    private final HashMap<String, FuncBlock> nameToFunc = new HashMap<>();
    private int AsciizNum = 1;

    public void addInt(String name, int addr, int initVal) {
        this.nameToAddr.put(name, addr);
        this.nameToVal.put(name, initVal);
    }

    public String addAsciiz(String content) {
        if (AsciizToName.containsKey(content)) {
            return AsciizToName.get(content);
        } else {
            String name = "STR_" + AsciizNum++;
            this.nameToAsciiz.put(name, content);
            this.AsciizToName.put(content, name);
            return name;
        }
    }

    public String getAsciizName(String content) {
        return this.AsciizToName.get(content);
    }

    public void addArray(String name, int addr, ArrayList<Integer> initVal) {
        this.nameToAddr.put(name, addr);
        this.nameToArray.put(name, initVal);
    }

    public void addFunc(FuncBlock funcBlock) {
        this.nameToFunc.put(funcBlock.getFuncName(), funcBlock);
    }

    public FuncBlock getFunc(String funcName) {
        return this.nameToFunc.get(funcName);
    }

    @Override
    public void output() {
        MiddleWriter.print("###### GLOBAL STRING ######");
        for (Map.Entry<String, String> nameAsciiz : nameToAsciiz.entrySet()) {
            MiddleWriter.print(String.format("%s : %s", nameAsciiz.getKey(), nameAsciiz.getValue()));
        }
        MiddleWriter.print("\n");

        MiddleWriter.print("###### GLOBAL ARRAY ######");
        for (Map.Entry<String, ArrayList<Integer>> nameArray : nameToArray.entrySet()) {
            String name = nameArray.getKey();
            StringJoiner initVal = new StringJoiner(" ");
            for (Integer val : nameArray.getValue()) {
                initVal.add(String.valueOf(val));
            }
            MiddleWriter.print(String.format("[0x%x]array %s: %s", nameToAddr.get(name), name, initVal));
        }
        MiddleWriter.print("\n");

        MiddleWriter.print("###### GLOBAL VAR ######");
        for (Map.Entry<String, Integer> nameVar : nameToVal.entrySet()) {
            MiddleWriter.print(String.format("[0x%x]%s: %d", nameToAddr.get(nameVar.getKey()), nameVar.getKey(),
                    nameVar.getValue()));
        }
        MiddleWriter.print("\n");

        MiddleWriter.print("###### TEXT ######");
        MiddleWriter.print("JUMP FUNC_main");

        HashSet<BasicBlock> visited = new HashSet<>();
        Queue<BasicBlock> queue = new LinkedList<>();

        for (FuncBlock funcBlock : nameToFunc.values()) {
            MiddleWriter.print(
                    String.format("# func %s : stack size 0x%x", funcBlock.getFuncName(), funcBlock.getStackSize()));
            queue.add(funcBlock.getBody());
            while (!queue.isEmpty()) {
                BasicBlock front = queue.poll();
                if (visited.contains(front)) {
                    continue;
                }
                visited.add(front);
                MiddleWriter.print(front.getLabel() + ":");
                ArrayList<BlockNode> blockNodes = front.getContent();
                for (BlockNode blockNode : blockNodes) {
                    if (blockNode instanceof Jump) {
                        BasicBlock target = ((Jump) blockNode).getTarget();
                        queue.add(target);
                    } else if (blockNode instanceof Branch) {
                        BasicBlock thenBlock = ((Branch) blockNode).getThenBlock();
                        queue.add(thenBlock);
                        BasicBlock elseBlock = ((Branch) blockNode).getElseBlock();
                        queue.add(elseBlock);
                    }
                    MiddleWriter.print("\t" + blockNode);
                }
                MiddleWriter.print("\n");
            }
        }
        try {
            MiddleWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
