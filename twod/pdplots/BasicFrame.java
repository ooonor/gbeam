import java.awt.*;
import javax.swing.*;

/**
 * Write a description of class BasicFrame here.
 * 
 * @author (your name) 
 * 
 */

public class BasicFrame extends JFrame
{
    // instance variables - replace the example below with your own
    //private static double RAD = .017453293;
    double degradation;
    int np;
    double[] xp;
    double[] yp;
    double[] yp_actual;
    int n_title;
    String[] title;
    int[] flags;
    int frameWid;
    int frameHgt;
    boolean active;
    // computed
    int tbot;
    int prev_line_width = 1;
    Color customBlue = new Color(0,0,255);
    Color customGreen = new Color(0,128,0);
    Color customRed = new Color(255,0,0);
    Color customBlack = new Color(0,0,0);
    Color gridColor = new Color(192,192,192);
    int idealWid = 3;
    int actualWid = 5;
    int ixcur;
    int iycur;
    int cur;  // 0 = none, 1 = pdse, 2 = pdpd
    
    grph g_pdse = new grph(0,0,0,0,0.,0.,0.,0.);
    grph g_pdpd = new grph(0,0,0,0,0.,0.,0.,0.);

    /**
     * Constructor for objects of class BasicFrame
     */
    public BasicFrame(double degradation_in, int n, double[] x, double[] y, int n_title_lines, String[] title, 
                      int[] flags_in, int frameWid_in, int frameHgt_in, boolean active_in)
    {
        // initialise instance variables
        super();
        this.degradation = degradation_in;
        this.active = active_in;
        this.np = n;
        this.xp = x;
        this.yp = y;
        this.n_title = n_title_lines;
        this.title = title;
        this.flags = flags_in;
        this.frameWid = frameWid_in;
        this.frameHgt = frameHgt_in;
    }
    private class grph {
        int minx;
        int miny;
        int maxx;
        int maxy;
        double xmin;
        double dx;
        double xmax;
        double ymin;
        double dy;
        double ymax;
        
        private grph(int minx_in, int miny_in, int maxx_in, int maxy_in) {
            this.minx = minx_in;
            this.miny = miny_in;
            this.maxx = maxx_in;
            this.maxy = maxy_in;
        }
        private grph(int minx_in, int miny_in, int maxx_in, int maxy_in, double xmin_in, double ymin_in,
                     double xmax_in, double ymax_in) {
            this.minx = minx_in;
            this.miny = miny_in;
            this.maxx = maxx_in;
            this.maxy = maxy_in;
            this.xmin = xmin_in;
            this.ymin = ymin_in;
            this.xmax = xmax_in;
            this.ymax = ymax_in;
        }
        private void fill_phys(double xmin_in, double dx_in, double xmax_in, double ymin_in, double dy_in, double ymax_in) {
            this.xmin = xmin_in;
            this.dx = dx_in;
            this.xmax = xmax_in;
            this.ymin = ymin_in;
            this.dy = dy_in;
            this.ymax = ymax_in;
        }
        private double xpixtophys(int ix) {
            double w2 = ((double)(ix - minx)/(double)(maxx-minx));
            return ((1.-w2)*xmin + w2*xmax);
        }
        private int getminx() { return (this.minx); }
        private int getminy() { return (this.miny); }
        private int getmaxx() { return (this.maxx); }
        private int getmaxy() { return (this.maxy); }
        private double getxmin() {return (this.xmin); }
        private double getymin() { return (this.ymin); }
        private double getxmax() { return (this.xmax); }
        private double getymax() { return (this.ymax); }
        private double getdx() { return (this.dx); }
        private double getdy() { return (this.dy); }
    }
    public int xy_inside_box(int x, int y, grph g) {
        if (x >= g.minx && x <= g.maxx && y >= g.miny && y <= g.maxy){
            return(1);
        }
        else {
            return(0);
        }
    }
    public void addCursorInfo(int x, int y) {
        // decide whether this is inside either of the plots
        this.cur = 0;
        this.ixcur = x;
        this.iycur = y;
        if(this.flags[0] != 0 ) {
            if (xy_inside_box(x,y,g_pdse)==1) this.cur = 1;
        }
        if (this.cur != 1){
            if(this.flags[1] != 0 ) {
                if (xy_inside_box(x,y,g_pdpd) == 1) this.cur = 2;
            }
        }
    }

    /**
     * An example of a method - replace this comment with your own
     */
    public void paint(Graphics g)
    {
        int bot1;
        int bot2;
        int miny = 100;
        int maxx = 1150;
        Graphics2D g2 = (Graphics2D)g;
        // define colors
        int chgt = 24;
        paint_titles(g2, 100, miny, chgt) ;
        // legend
        paint_legend(g2, maxx, miny, chgt);

        if (flags[0] != 0 && flags[1] != 0) {
            bot1 = (this.tbot + this.frameHgt)/2 - 60;
            this.g_pdse = new grph(100, tbot+40,maxx, bot1);
            paint_pdse(g2, g_pdse);
            this.g_pdpd = new grph(100,bot1+70, frameHgt - bot1 - 70  , frameHgt-100,0.,0.,0.,0.);
            paint_pdpd(g2, g_pdpd); 
        }
        else if(flags[0] != 0) {
            this.g_pdse = new grph(100,tbot+40,maxx,950,0.,0.,0.,0.);
            paint_pdse(g2, g_pdse); 
        }
        else if(flags[1] != 0) {
            this.g_pdpd = new grph(100,tbot+40,800,tbot+40+700,0.,0.,0.,0.);
            paint_pdpd(g2, g_pdpd); 
        }
    }
    private void paint_legend(Graphics2D g, int maxx, int miny, int chgt) {
        String txt1 = "Ideal";
        String txt2 = "Actual";
        Font font = new Font("Serif", Font.BOLD, chgt);
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics(font);
        int hgt = metrics.getHeight();
        int wid = metrics.stringWidth(txt2);
        int xpos = maxx - wid;
        int line_len = 70;
        int line_sep = 10;
        g.setStroke(new BasicStroke(idealWid));
        g.setColor(customGreen);
        g.drawString(txt1,xpos, miny);
        g.drawLine(xpos - line_len - line_sep, miny -hgt/4, xpos - line_sep, miny - hgt/4);
        g.setStroke(new BasicStroke(actualWid));
        g.setColor(customRed);
        g.drawString(txt2, xpos, miny + hgt);
        g.drawLine(xpos - line_len - line_sep, miny + (hgt*3)/4, xpos - line_sep, miny + (hgt*3)/4);
        g.setStroke(new BasicStroke(1));
    }
    private void paint_titles(Graphics2D g, int minx, int miny, int chgt) {
        g.setColor(this.customBlue);
        Font font = new Font("Serif", Font.BOLD, chgt);
        g.setFont(font);
        this.tbot = miny;
        for (int i = 0; i < this.n_title; i++){
           g.drawString(title[i],minx, this.tbot);
           this.tbot = this.tbot + chgt;
        }
    }
    private void paint_axes(Graphics2D g, grph gg,String xT, String yT, String xs, String ys, int grid) {
        g.setStroke(new BasicStroke(1));
        g.drawLine(gg.minx,gg.maxy,gg.maxx,gg.maxy);
        g.drawLine(gg.minx,gg.miny,gg.minx,gg.maxy);
        g.setColor(customBlack);
        Font font = new Font("Serif", Font.BOLD, 16);
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics(font);
        g.drawString(xT,((gg.minx+gg.maxx - metrics.stringWidth(xT))/2), gg.maxy+32);
        g.drawString(yT, gg.minx - 80, (gg.miny+gg.maxy)/2);
        // now axis numbering
        String ynum;
        int ny = (int)((gg.ymax - gg.ymin)/gg.dy + 1.1);
        int iy;
        for (int i = 0; i < ny; i++) {
            g.setColor(customBlack);
            ynum = (int)(gg.getymin() + i*gg.dy) + ys;
            iy =  gg.maxy - i*(gg.maxy - gg.miny)/(ny-1);
            g.drawString(ynum, gg.minx - 40, iy);
            if (grid == 1) {
                g.setColor(gridColor);
                g.drawLine(gg.minx, iy  , gg.maxx, iy);
            }
        }
        // x axis.  Depends on dx being set for now
        int nx = (int)((gg.xmax-gg.xmin)/gg.dx + 1.1);
        String xnum;
        int ix;
        for (int i = 0; i < nx; i++){
            xnum = (int)(gg.xmin + i*gg.dx) + xs;
            g.setColor(customBlack);
            ix = (int)(gg.minx + i*(double)((gg.maxx-gg.minx)/(nx-1)));
            g.drawString(xnum,ix , gg.maxy + 16);
            if(grid == 1) {
                g.setColor(gridColor);
                g.drawLine(ix, gg.miny  , ix, gg.maxy);
            }
        }
        g.setColor(customBlack);
    }
    private double interp(double[] x, double[] y, int n, double xnow) {
        if (n < 2) return (y[0]);
        if (xnow <= x[0]) return (y[0]);
        if (xnow >= x[n-1]) return (y[n-1]);
        int i = 1;
        while (xnow > x[i] && i < n) i++;
        double w2 = (xnow-x[i-1])/(x[i]-x[i-1]);
        double w1 = 1. - w2;
        return (w1*y[i-1] + w2*y[i]);
    }
    private void add_plot_title(String prefix, Graphics2D g, grph gg)  {
        String plot_title = "Passive Array";
        if(this.active)plot_title = "Active Array" ;
        plot_title = prefix + plot_title;
        Font font = new Font("Serif", Font.BOLD, 16);
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics(font);
        int hgt = metrics.getHeight();
        int wid = metrics.stringWidth(plot_title);
        g.drawString(plot_title,((gg.minx+gg.maxx - wid)/2), gg.miny);
    }
    private void paint_pdpd(Graphics2D g,grph gg) {
        gg.fill_phys(0.,25.,100., 0.,25., 100.);
        paint_axes(g,gg, "PD (Ideal)", "PD", "%", "%", 1);
        add_plot_title("PD Display - ", g, gg);

        double xtmp[] = new double[this.np];
        double ytmp[] = new double[this.np];
        // first a diagonal green line
        Color greenColor = new Color(0,128,0);
        for (int i = 0; i < np; i++) {
            ytmp[i] = yp[i]*100.;
            xtmp[i] = ytmp[i];
        }
        paint_curve(g,gg,np, xtmp, ytmp, greenColor, idealWid);
        // now the tricky part.  For each index, turn PD into SE, add the degradation, and turn back into SE
        for (int i = 0; i < np; i++) {
            // xp is SE, yp is PDdouble se_tmp = xp[i] - degradation;
            ytmp[i] = 100.*interp(xp, yp, np, xp[i] - this.degradation);
        }
        Color redColor = new Color(255,0,0);
        paint_curve(g, gg, np, xtmp, ytmp, redColor,actualWid);
        
        if (this.cur == 2) {
            g.drawLine(this.ixcur, gg.miny,this.ixcur,gg.maxy);
            // get the physical PD from x value of the cursor
            double xphys = gg.xpixtophys(this.ixcur);
            // now get degraded PD for this point.  FIrst, get ideal SE, then subtradt degradation, then get PD
            double se_tmp = interp(yp, xp, np, xphys/100.);
            double pd_tmp = 100.*interp(xp, yp, np, se_tmp - this.degradation);
            String testString = "PD (Ideal) = " + (int)xphys + ", PD (act) = " + (int)pd_tmp;
            g.drawString(testString,this.ixcur, this.iycur);
        }

        
    }
    private void paint_curve(Graphics2D g, grph gg, int np, double[] xp, double[] yp, 
                        Color clr, int wid){
        double x;
        double y;
        int xprev = 0;
        int yprev = 0;
        g.setColor(clr);
        BasicStroke bs = new BasicStroke(wid);
        g.setStroke(bs);
        g.setStroke(new BasicStroke(wid));
        for (int i = 0; i < np; i++)       {
            x = gg.minx + (gg.maxx - gg.minx)* (xp[i] - gg.xmin)/(gg.xmax-gg.xmin) ;
            y = gg.maxy - (gg.maxy - gg.miny)* (yp[i] - gg.ymin)/(gg.ymax-gg.ymin);
            if(i > 0 && xprev >= gg.minx && x <= gg.maxx) g.drawLine(xprev,yprev,(int)x,(int)y);
            xprev = (int)x;
            yprev = (int)y;
        }
        g.setStroke(new BasicStroke(1));
    }
                        
    private void paint_pdse(Graphics2D g, grph gg) {
        gg.fill_phys(-18.,3.,18., 0.,25.,100.);
        paint_axes(g, gg,"SE", "PD", " ", "%",1);
        add_plot_title("PD vs. SE - ",g, gg);

        
        double[] ytmp = new double[np];
        double[] xtmp = new double[np];
        for (int i = 0; i < np; i++) ytmp[i] = yp[i]*100.;
        Color greenColor = new Color(0,128,0);
        paint_curve(g, gg, np, xp,ytmp, greenColor, idealWid);
        Color redColor = new Color(255,0,0);
        for (int i = 0; i < np; i++)xtmp[i]  = xp[i] + this.degradation;
        paint_curve(g, gg, np, xtmp,ytmp, redColor,actualWid);
        
        if (this.cur == 1) {
            g.drawLine(this.ixcur, gg.miny,this.ixcur,gg.maxy);
            double se_ideal = gg.xpixtophys(this.ixcur);
            // now get degraded PD for this point.  FIrst, get ideal SE, then subtradt degradation, then get PD
            double pd_ideal = 100.*interp(xp, yp, np, se_ideal);
            double pd_tmp = 100.*interp(xp, yp, np, se_ideal - this.degradation);
            String testString = "PD (Ideal) = " + (int)pd_ideal + ", PD (act) = " + (int)pd_tmp;
            g.drawString(testString,this.ixcur, this.iycur);
        }
    }
}