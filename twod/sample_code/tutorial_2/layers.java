import javax.swing.*;
import javax.swing.border.*;
import javax.accessibility.*;
import java.awt.*;
import java.awt.event.*;
/**
 * Write a description of class layers here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class layers
{
    static JPanel changingPanel;
    static JPanel staticPanel;
    /**
     * Constructor for objects of class layers
     */
    public static void main() {
        changingPanel = new JPanel() {
            //@Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g;
                g2.setColor(Color.RED);
                g2.drawLine(20, 20, 80, 80);
                //g.fillRect(100, 100, 100, 100);
            }
        };
        changingPanel.setOpaque(false);

        staticPanel = new JPanel()  {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g;
                g2.setColor(Color.YELLOW);
                g2.drawLine(20, 20, 180, 280);
            }
        };
        staticPanel.setBackground(Color.BLUE);
        staticPanel.setLayout(new BorderLayout());
        staticPanel.add(changingPanel);

        JFrame frame = new JFrame();
        frame.addMouseListener (new MyMouseListener(frame));
        frame.add(staticPanel);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    public static class MyMouseListener extends MouseAdapter {
        private JFrame frame;
        private JPanel panel;
        public MyMouseListener(JFrame frame_in) {   //JFrame frame_in) {
           this.frame = frame_in;
        }
        public void mouseClicked(MouseEvent me) {
            Point p = MouseInfo.getPointerInfo().getLocation();
            final int x = p.x;
            final int y = p.y;

            System.out.println(" x, y = " + x + ", " + y);//            frame.addCursorInfo(x,y);
            //this.frame.removeAll();   // does not work
            staticPanel.remove(changingPanel);
            changingPanel = new JPanel() {
            //@Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g;
                g2.setColor(Color.RED);
                g2.drawLine(x, y, 80, 80);
                g.fillRect(100, 100, 100, 100);
            }
        };
        changingPanel.setOpaque(false);
        staticPanel.add(changingPanel);
        frame.add(staticPanel);
        frame.setVisible(true);
            frame.repaint();
            /*Point origin = new Point(10, 20);
            JLayeredPane layeredPane = new JLayeredPane();
            layeredPane.setPreferredSize(new Dimension(300, 310));
            JLabel label = createColoredLabel("A label", Color.BLUE, origin);
            layeredPane.add(label, 0);
            frame.add(layeredPane);
            frame.repaint();*/
        }
    }
}