package Exceptions;

public class MyAssert extends RuntimeException {
    private final String msg;
    public MyAssert() {
        msg = "assertion failed";
    }
    public MyAssert(String msg) {
        this.msg = msg;
    }
    public String toString() {
        return msg;
    }
    public static void ass(boolean b) throws MyAssert {
        if(!b)
            throw new MyAssert();
    }
    public static void ass(boolean b, String msg) throws MyAssert {
        if (!b)
            throw new MyAssert(msg);
    }
}
