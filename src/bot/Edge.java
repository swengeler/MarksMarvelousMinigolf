package bot;

import physics.ShotData;

public class Edge {

    private Node previous, next;
    private boolean explored;

    private ShotData data;

    public Edge(Node previous, Node next, ShotData data) {
        this.previous = previous;
        this.next = next;
        this.data = data;
    }

    public Edge(Node previous, Node next) {
        this.previous = previous;
        this.next = next;
    }

    public Node getOther(Node one) {
        if (one == previous)
            return next;
        else if (one == next)
            return previous;
        return null;
    }

    public Node getPrevious() {
        return previous;
    }

    public void setPrevious(Node previous) {
        this.previous = previous;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public boolean isExplored() {
        return explored;
    }

    public void setExplored(boolean explored) {
        this.explored = explored;
    }

    public ShotData getData() {
        return data;
    }

    public void setData(ShotData data) {
        this.data = data;
    }

}
