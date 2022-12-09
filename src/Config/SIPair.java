package Config;

// String--Integer Pair
public class SIPair implements Comparable<SIPair> {
    private final String string;
    private final Integer integer;

    public SIPair(String string, Integer integer) {
        this.string = string;
        this.integer = integer;
    }

    public String getString() {
        return string;
    }

    public Integer getInteger() {
        return integer;
    }

    @Override
    public int compareTo(SIPair SIPair) {
        return this.integer - SIPair.integer;
    }
}
