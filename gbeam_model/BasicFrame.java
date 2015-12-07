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
    private static double RAD = .017453293;
    double[] xp;
    double[][] yp;
    double[][] ypFull;
    double dB;
    double yb;
    int np;
    String title;
    double theta;
    double phi;
    double frequency;

    /**
     * Constructor for objects of class BasicFrame
     */
    public BasicFrame(double[] x, double th[], double[][] y, double[][] yFull, int n, String title, double theta, double phi, double frequency)
    {
        // initialise instance variables
        super();
        this.xp = x;
        this.yp = y;
        this.ypFull = yFull;
        this.np = n;
        this.title = title;
        this.theta = theta;
        this.phi = phi;
        this.frequency = frequency;
    }

    /**
     * An example of a method - replace this comment with your own
     */
    public void paint(Graphics g)
    {
       // g.drawLine(10,10,150,150); // Draw a line from (10,10) to (150,150)
       int minx = 20;
       int maxx = 1150;
       int miny = 50;
       int maxy = 950;
       int centerx = (minx+maxx)/2;
       int centery = (miny+maxy)/2;
       int radius = centery - miny;
       g.drawLine(minx,centery,maxx,centery);
       g.drawLine(centerx,miny,centerx,maxy);
       int i = 0;
       int xprev = 0;
       int yprev = 0;
       double x;
       double y;
       double tmp_cx = centerx;
       double tmp_cy = centery;
       double tmp_r = radius;
       for (i = 0; i < np; i++)       {
           yb = Math.max(yp[i][0], 1.e-30);
           dB = 10.*Math.log(yb)/Math.log(10.);
           // rescale to something between 0 and 1
           dB = dB + 30.;
           dB = dB/30.;
           dB = Math.max(dB, 0.);
           x = tmp_cx + Math.cos(xp[i]*RAD) * dB*tmp_r;
           y = tmp_cy - Math.sin(xp[i]*RAD) * dB*radius;
           if(i > 0)
           {
               g.drawLine(xprev,yprev,(int)x,(int)y);
           }
           xprev = (int)x;
           yprev = (int)y;
       }
       Color customColor = new Color(10,10,255);
       g.setColor(customColor);
       for (i = 0; i < np; i++)       {
           yb = Math.max(ypFull[i][0], 1.e-30);
           dB = 10.*Math.log(yb)/Math.log(10.);
           // rescale to something between 0 and 1
           dB = dB + 30.;
           dB = dB/30.;
           dB = Math.max(dB, 0.);
           x = tmp_cx + Math.cos(xp[i]*RAD) * dB*tmp_r;
           y = tmp_cy - Math.sin(xp[i]*RAD) * dB*radius;
           if(i > 0)
           {
               g.drawLine(xprev,yprev,(int)x,(int)y);
           }
           xprev = (int)x;
           yprev = (int)y;
       }
       customColor = new Color(10,10,255);
       g.setColor(customColor);
       Font font = new Font("Serif", Font.PLAIN, 36);
       g.setFont(font);
       centery = centery - 120;
       g.drawString(title,minx, centery);
       g.drawString("Theta = " + theta, minx, centery + 40);
       g.drawString("Phi = " + phi, minx, centery + 80);
       g.drawString("Frequency = " + frequency, minx, centery + 120);
    }
}

