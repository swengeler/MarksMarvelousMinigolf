package artificialIntelligence.viewer;

import artificialIntelligence.algorithms.HMPathing;
import artificialIntelligence.utils.Node;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class GraphPanel extends JPanel {

    private Node holeNode;
    private Node[][] graph;

    public GraphPanel(Node holeNode, Node[][] graph) {
        this.holeNode = holeNode;
        this.graph = graph;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        Node node;
        Rectangle2D.Double square = new Rectangle2D.Double();
        float curValue;
        int xStepSize = getWidth() / (graph[0].length - 1);
        System.out.println("width = " + getWidth() + ", graphwidth = " + graph[0].length +  ", xstepsize = " + xStepSize);
        int yStepSize = getHeight() / (graph.length - 1);
        xStepSize = 3;
        yStepSize = 3;
        for (int i = 0; i < graph.length; i++) {
            for (int j = 0; j < graph[0].length; j++) {
                node = graph[j][i];
                square.setRect((graph[0].length - 1 - j) * xStepSize, (graph.length - 1 - i) * yStepSize, xStepSize, yStepSize);
                if (node.equals(holeNode))
                    g2d.setColor(Color.BLUE);
                else if (node.getD() == 3000) {
                    g2d.setColor(Color.RED);
                } else {
                    curValue = (((float) ((int) (node.getD() >= HMPathing.maxDistance ? HMPathing.maxDistance : node.getD() * 10))) / 10 / HMPathing.maxDistance);
                    //System.out.println("curvalue for i = " + i + ", j = " + j + ": " + curValue + " " + ((float) ((int) (node.getD() * 10))) / 10);
                    g2d.setColor(new Color(curValue, curValue, curValue));
                }
                g2d.fill(square);
                //g2d.setColor(Color.BLACK);
                //g2d.draw(square);
            }
        }
    }

}
