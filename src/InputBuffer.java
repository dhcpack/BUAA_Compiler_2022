import java.util.ArrayList;

public class InputBuffer {
    private final ArrayList<String> lines = new ArrayList<>();
    private int line = 0;
    private int pointer = 0;

    public void add(String line) {
        lines.add(line);
    }

    private int currentLength() {
        return lines.get(line).length();
    }

    public void forward(int step) {
        if (pointer + step < currentLength()) {
            pointer += step;
            return;
        }
        step = step - (currentLength() - pointer);
        pointer = 0;
        line++;
        while (pointer + step >= currentLength()) {
            step = step - (currentLength() - pointer);
            line++;
        }
        pointer += step;
    }

    public String getForwardWord(int step) {
        if (pointer + step <= currentLength()) {
            return lines.get(line).substring(pointer, pointer+step)
        }
    }

}
