import java.util.ArrayList;
import java.util.List;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Arrays;
import java.util.Scanner;;
import javax.swing.*;
import javax.swing.border.*;
import javax.accessibility.*;


/**
 * This code displays probability of detection curves for an ideal and a degraded array
 * given array degradation in dB and array type (active or passive)
 * 
 * @author Eleanor Wilson
 * @version October 10, 2015
 */
class pdplot_utils
{
    static JPanel changingPanel;
    static JPanel staticPanel;
    private int[] flags;
    private int n_title_lines;
    private String[] title_lines;
    private int npts;
    private double[] pdse;
    private double[] pd;
    private double degradation;
    private boolean active;
    private BasicFrame frame;
    private display_panel_apps disp;
    private JFrame jframe;
    private int frameWid;
    private int frameHgt;
    
    public pdplot_utils() {
        this.flags = new int[4];
    }
    public int get_inputs(String filename){
        try{
            Scanner in = new Scanner(new FileReader(filename)); 
            for (int i = 0; i < 4; i++) this.flags[i] = in.nextInt();
            this.n_title_lines = in.nextInt();
            String dummy = in.nextLine();  // to read to the end of the current line
            this.title_lines = new String[this.n_title_lines];
            for (int i = 0; i < this.n_title_lines; i++) {
                this.title_lines[i] = in.nextLine();
            }
            this.degradation = in.nextDouble();
            this.active = in.nextBoolean();
            this.npts = in.nextInt();
            if (this.npts > 0) {
                this.pdse = new double[this.npts];
                this.pd = new double[this.npts];
                for (int i = 0; i < this.npts; i++) {
                    this.pdse[i] = in.nextDouble();
                    this.pd[i] = in.nextDouble();
                }
            }
            else {
                List<XYPoint> xy = ProbDetection.getRocCurveModifiedUrick(this.active,false);
                this.npts = xy.size();
                this.pdse = new double[this.npts];
                this.pd = new double[this.npts];
                for (int i = 0; i < this.npts; i++ ) { //this.npts; i++) {
                    this.pdse[i] = xy.get(i).getX();
                    this.pd[i] = xy.get(i).getY();
                }
            }
            in.close(); 
            return (0);
        }
        catch (FileNotFoundException e)
        {
            return(1);
        }
    }
    public int display(int x, int y) {
        int frameWid = 1200;
        int frameHgt = 1000;
        if (x < 0) {
        this.frame = new BasicFrame(this.degradation, this.npts, this.pdse, this.pd, this.n_title_lines, 
                                    this.title_lines, this.flags, frameWid, frameHgt, this.active);
        frame.addMouseListener(new MyMouseListener(frame));
        }
        if (x > 0 && y > 0) frame.addCursorInfo(x,y);
        frame.setSize(frameWid,frameHgt);
        frame.setVisible(true);
        return(0);
    }
    public int display_panels() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dim = toolkit.getScreenSize();
        frameWid = (int)(dim.width * .8);
        frameHgt = (int)(dim.height * .9);
        this.disp = new display_panel_apps(this.degradation, this.npts, this.pdse, this.pd, this.n_title_lines, 
                                           this.title_lines, this.flags, frameWid, frameHgt, this.active);
        changingPanel = new JPanel() 
        {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g;
                g2.setColor(Color.RED);
            }
        };
        changingPanel.setOpaque(false);
        staticPanel = new JPanel()  
        {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                disp.paint_panel(g);
            }
        };
        staticPanel.setLayout(new BorderLayout());

        jframe = new JFrame();
        jframe.addMouseListener (new MyMouseListenerP(jframe));
        jframe.add(staticPanel);

        jframe.setSize(frameWid, frameHgt);
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setVisible(true);
        jframe.add(staticPanel);
        // in case we need them later, here are the widths of the borders around the frame
        //        System.out.format("insets: left %d, right %d, top %d, bottom %d%n", jframe.getInsets().left, 
        //              jframe.getInsets().right, jframe.getInsets().top, jframe.getInsets().bottom);
        return(0);
    }
    public class MyMouseListenerP extends MouseAdapter {
        private JFrame jframe;
        public MyMouseListenerP(JFrame frame_in) {
           this.jframe = frame_in;
        }
        public void mouseClicked(MouseEvent me) {
            
            Rectangle clientRect = jframe.getBounds();
            int xoffset = jframe.getInsets().left + clientRect.x;
            int yoffset = jframe.getInsets().top + clientRect.y;
            Point p = MouseInfo.getPointerInfo().getLocation();
            disp.addCursorInfo(p.x - xoffset,p.y - yoffset);
            
            staticPanel.remove(changingPanel);
            changingPanel = new JPanel() {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                disp.paint_cursor(g);
            }
        };
        changingPanel.setOpaque(false);
        staticPanel.add(changingPanel);
        jframe.add(staticPanel);
        jframe.setVisible(true);
        jframe.repaint();
        }
    }
    public class MyMouseListener extends MouseAdapter {
        private BasicFrame frame;
        public MyMouseListener(BasicFrame frame_in) {
            this.frame = frame_in;
        }
        public void mouseClicked(MouseEvent me) {
            Point p = MouseInfo.getPointerInfo().getLocation();
            int x = p.x;
            int y = p.y;
            x = me.getX();
            frame.addCursorInfo(x,y);
            frame.setVisible(false);
            display(x,y);
        }
        private JLabel createColoredLabel(String text,Color color, Point origin) {
        JLabel label = new JLabel(text);
        label.setVerticalAlignment(JLabel.TOP);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setOpaque(true);
        label.setBackground(color);
        label.setForeground(Color.black);
        label.setBorder(BorderFactory.createLineBorder(Color.black));
        label.setBounds(origin.x, origin.y, 140, 140);
        return label;
        }
    }
}
public class pdplot
{
    public static void main() {
        pdplot_utils pdp = new pdplot_utils() ;  //("pdplot.txt");
        int ier_inputs = pdp.get_inputs("pdplot.txt");
        int ier_disp = pdp.display_panels();
    }
    public static void main_doesnt_work()
    {
        pdplot_utils pdp = new pdplot_utils() ;  //("pdplot.txt");
        int ier_inputs = pdp.get_inputs("pdplot.txt");
        int ier_display = pdp.display(-1,-1);
    }
}
