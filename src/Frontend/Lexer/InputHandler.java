package Frontend.Lexer;

import java.util.ArrayList;

public class InputHandler {
    private final ArrayList<String> lines = new ArrayList<>();
    private int line = 0;
    private int pointer = 0;

    public void add(String line) {
        lines.add(line);
    }

    public ArrayList<String> getLines() {
        return lines;
    }

    private int currentLength() {
        return lines.get(line).length();
    }

    public boolean moveForward(int step) {
        if (pointer + step < currentLength()) {
            pointer += step;
            return false;
        }
        step = step - (currentLength() - pointer);
        pointer = 0;
        line++;
        while ((!reachEnd()) && (pointer + step >= currentLength())) {
            step = step - (currentLength() - pointer);
            line++;
        }
        pointer += step;
        return reachEnd();
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getForwardWord(int step) {
        return lines.get(line).substring(pointer, Math.min(currentLength(), pointer + step));
    }

    public String getCurrentLine() {
        return lines.get(line).substring(pointer);
    }

    public boolean reachEnd() {
        return line == lines.size();
    }

    public boolean moveNextLine() {
        line++;
        pointer = 0;
        if (reachEnd()) return false;
        moveForward(0);  // 跳过接下来的空行
        return !reachEnd();
    }

    public int getPointer() {
        return pointer;
    }

    public void setPointer(int pointer) {
        this.pointer = pointer;
    }

    public int getLineNumber() {
        return line + 1;
    }

    // skip blanks and return true if reach end
    public boolean skipBlanks() {
        while (!reachEnd() && getCurrentLine().length() == 0) {
            moveNextLine();
        }
        while (!reachEnd() && (getForwardWord(1).equals(" ") || getForwardWord(1).equals("\t"))) {
            moveForward(1);
            while (!reachEnd() && getCurrentLine().length() == 0) {
                moveNextLine();
            }
        }
        return reachEnd();
    }

    // skip comments and return true if encounter comments
    public boolean skipComments() {
        // encounter '//'
        if (getForwardWord(2).equals("//")) {
            moveNextLine();
            return true;
        }
        // encounter '/*'
        if (getForwardWord(2).equals("/*")) {
            moveForward(2);
            while (!getForwardWord(2).equals("*/")) {
                if (moveForward(1)) break;
            }
            moveForward(2);
            return true;
        }
        return false;
    }
}
