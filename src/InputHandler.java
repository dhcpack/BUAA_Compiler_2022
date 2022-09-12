import java.util.ArrayList;

public class InputHandler {
    private final ArrayList<String> lines = new ArrayList<>();
    private int line = 0;
    private int pointer = 0;

    public void add(String line) {
        lines.add(line);
    }

    private int currentLength() {
        return lines.get(line).length();
    }

    public boolean moveForward(int step) {
        if (pointer + step < currentLength()) {
            pointer += step;
            return true;
        }
        step = step - (currentLength() - pointer);
        pointer = 0;
        line++;
        while ((!reachEnd()) && (pointer + step >= currentLength())) {
            step = step - (currentLength() - pointer);
            line++;
        }
        pointer += step;
        return !reachEnd();
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

    public boolean skipBlanks() {
        while (!reachEnd() && getCurrentLine().length() == 0){
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

    public boolean moveNextLine() {
        line++;
        pointer = 0;
        moveForward(0);
        return !reachEnd();
    }

    public int getLine() {
        return line + 1;
    }
}
