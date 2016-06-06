package bot;

import java.util.ArrayList;

public class Graph {

    private ArrayList<Node> nodes; // might not be necessary to keep track of all nodes/edges but could be useful for some operation
    private ArrayList<Edge> edges;

    private Node root; // starting node

    public Graph(Node root) {
        this.root = root;
    }

    public Node getRoot() {
        return root;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }

    public void addNode(Node n) {
        nodes.add(n);
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public void setEdges(ArrayList<Edge> edges) {
        this.edges = edges;
    }

    public void addEdge(Edge e) {
        edges.add(e);
    }

    /*
     * Methods to follow
     */

}
