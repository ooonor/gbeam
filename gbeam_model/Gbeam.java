
/**
 * Translation of the GBEAM code from VB to Java.
 * 
 * @author (Eleanor Wilson) 
 * @version (19 August 2015)
 */
import java.lang.Math;
import java.util.ArrayList;

public class Gbeam
{
    // instance variables - replace the example below with your own
    private static double RAD = .017453293;
    private static double PI = 3.14159265;
    private int lmn;
    private int isteer;
    private int idelay;
    private double[] xpos;
    private double[] ypos;
    private double[] zpos;
    private double[] dela;
    private double[] rshad;
    
    /**
       Constructor for objects of class gbeam
       @param  theta vertical incident angle (double)
               phi horizontal indicent angle (double)
               rzero, set to 0 for plane wavefront, radius of curvature for spherical wavefront (double)
               wlamda, wavelength (double)
               lmn, number of elements (int)
               idelay, flag (need to remember exactly how this works) (int)
               isteer, flag (should this be internal to the class?)
               xpos[], ypos[], zpos[] element positions (in VB this was xyz() As dpt)
               dela() array of length lmn with delays for each element (double)
               rshad() array of length lmn with shading for each element (double)
       @return     the beam pattern level for the input theta and phi 
     */
    /*
    public Gbeam(int lmn, int isteer, int idelay,         
                 double[] xpos, double[] ypos, double[] zpos, double[] rshad)
    {
        this.lmn = lmn;
        this.isteer = isteer;
        this.idelay = idelay;
        this.xpos = xpos;
        this.ypos = ypos;
        this.zpos = zpos;
        for (int i = 0; i < lmn; i++)dela[i] = 0.;
        this.rshad = rshad;
    }
    */
    public Gbeam(int lmn, int isteer, int idelay,         
                 double[] xpos, double[] ypos, double[] zpos, double[] dela, double[] rshad)
    {
        this.lmn = lmn;
        this.isteer = isteer;
        this.idelay = idelay;
        this.xpos = xpos;
        this.ypos = ypos;
        this.zpos = zpos;
        this.dela = dela;
        this.rshad = rshad;
    }
    public void gbeam_updatePos(double[] xpos, double[] ypos, double[] zpos)
    {
        /*
         * This routine changes the positions of the elements without affecting the computed
         * delays.  It is used to simulate a mismatch between the assumed element positions
         * and the actual element positions
         */
        this.xpos = xpos;
        this.ypos = ypos;
        this.zpos = zpos;
    }
    public double get_gbsum(double theta, double phi, double rzero, double wlamda)
    {
        double th;
        double ph;
        double arg;
        double alpha;
        double beta;
        double gamma;
        double real = 0.;
        double xim = 0.;
        double x;
        int i = 0;
        
        th = RAD * theta;
        ph = RAD * phi;
        alpha = Math.cos(ph) * Math.sin(th) * 2.0;
        beta = Math.cos(ph) * Math.cos(th) * 2.0; 
        gamma = -Math.sin(ph) * 2.0;

        if (isteer == 0)
        {
            /**
            *       CHECK TO ADD NEW ARRAY CONFIGURATION
            *       THE RESULTS WILL BE WHAT ONE WOULD GET IF ONE ASSUMED THAT HE WERE
            *       USING THE FIRST ARRAY CONFIGURATION BUT ACTUALLY HAD THE SECOND.
            */
            while (i < lmn)
            {
                arg = PI * (xpos[i] * alpha + ypos[i] * beta + zpos[i] * gamma) / wlamda;
        
                if (idelay == 0)
                   x = dela[i];
                else if(idelay == 2)
                   x = arg;
                else
                {
                   x = arg + dela[i];
                   real = real + rshad[i] * Math.cos(x);
                   xim = xim + rshad[i] * Math.sin(x);
                   dela[i] = dela[i] - arg;
                }
                i++;
            }
            double gbsum_v = real * real + xim * xim;
            isteer = 1;
            return gbsum_v;
        }
        if (rzero <= 0.)
        {
            /**
             * **** PLANE WAVE FRONT
            */ 
            while (i < lmn)
            {
                arg = PI * (xpos[i] * alpha + ypos[i] * beta + zpos[i] * gamma) / wlamda;
                arg = arg + dela[i];
                real = real + rshad[i] * Math.cos(arg);
                xim = xim + rshad[i] * Math.sin(arg);
                i++;
            }
        }
        else
        {
            /**
             * Compute Steering Directions
             * Spherical wavefront
             */
            alpha = alpha * rzero;
            beta = beta * rzero;
            gamma = gamma * rzero;
            while (i < lmn)
            {
                arg = Math.pow((xpos[i] - alpha),2) + Math.pow((ypos[i] - beta),2) + Math.pow((zpos[i] - gamma),2);                
                arg = PI * (rzero - arg*arg);
                arg = arg + dela[i];
                real = real + rshad[i] * Math.cos(arg);
                xim = xim + rshad[i] * Math.sin(arg);
                i++;
            }
        }
        double gbsum_v = real * real + xim * xim;
        return gbsum_v;
    }
}
    
