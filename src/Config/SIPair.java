package Config;

public class SIPair implements Comparable<SIPair> {
    private final String name;
    private final Integer address;

    public SIPair(String name, Integer address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public Integer getAddress() {
        return address;
    }

    @Override
    public int compareTo(SIPair SIPair) {
        return this.address - SIPair.address;
    }
}
