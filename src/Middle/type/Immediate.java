package Middle.type;

public class Immediate implements Operand {
    private final int num;

    public Immediate(int num) {
        this.num = num;
    }

    public int getNumber() {
        return num;
    }

    @Override
    public String toString() {
        return String.valueOf(this.num);
    }
}
