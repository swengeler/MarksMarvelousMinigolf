package bot;

import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;

public class Node {

    private ArrayList<Edge> previous, next;

    private Vector3f vbPosition;

    public Node(ArrayList<Edge> previous, ArrayList<Edge> next, Vector3f vbPosition) {
        this.previous = previous;
        this.next = next;
        this.vbPosition = new Vector3f(vbPosition.x, vbPosition.y, vbPosition.z);
    }

    public Node(Vector3f vbPosition) {
        previous = new ArrayList<Edge>();
        next = new ArrayList<Edge>();
        this.vbPosition = new Vector3f(vbPosition.x, vbPosition.y, vbPosition.z);
    }

    public ArrayList<Edge> getAllPrevious() {
        return previous;
    }

    public void setAllPrevious(ArrayList<Edge> previous) {
        this.previous = previous;
    }

    public void addPrevious(Edge e) {
        previous.add(e);
    }

    public ArrayList<Edge> getAllNext() {
        return next;
    }

    public void setAllNext(ArrayList<Edge> next) {
        this.next = next;
    }

    public void addNext(Edge e) {
        next.add(e);
    }

    public Vector3f getVbPosition() {
        return vbPosition;
    }

    public void setVbPosition(Vector3f vbPosition) {
        this.vbPosition.set(vbPosition.x, vbPosition.y, vbPosition.z);
    }
}
