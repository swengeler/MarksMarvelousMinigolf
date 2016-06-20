package bot2_0;

import javax.swing.*;
import java.awt.*;

public class GraphFrame extends JFrame {

    GraphPanel drawPanel;

    public GraphFrame(Node holeNode, Node[][] graph) {
        super("Graph of the course created by the wavefront algorithm");
        setSize(new Dimension(900, 925));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
        draw(holeNode, graph);
    }

    private void draw(Node holeNode, Node[][] graph) {
        drawPanel = new GraphPanel(holeNode, graph);
        drawPanel.setMinimumSize(new Dimension(916, 916));
        drawPanel.setPreferredSize(new Dimension(916, 916));
        drawPanel.setMaximumSize(new Dimension(916, 916));
        add(drawPanel);
    }

}
