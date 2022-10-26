package Middle.type;

public abstract class BlockNode {
    private BlockNode prev;
    private BlockNode next;

    public BlockNode getPrev() {
        return prev;
    }

    public void setPrev(BlockNode prev) {
        this.prev = prev;
    }

    public BlockNode getNext() {
        return next;
    }

    public void setNext(BlockNode next) {
        this.next = next;
    }
}
