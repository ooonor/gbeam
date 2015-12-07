import java.awt.*;
import java.io.*;
//import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;;
import javax.swing.*;
//import java.io.FileWriter;
//import java.io.PrintWriter;

/**
 * Write a description of class GbeamTester here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class GbeamTester
{
    // instance variables - replace the example below with your own
    private static double RAD = .017453293;
    private static double PI = 3.14159265;
    
    public static void main()
    {
        // decide whether we want to do the full 3-d pattern here, in case the HLA is buckled
        int flag3D = 0;
        // set previous to 0 to do it the one-dimensional way
        String title = "Blank Title";
        double theta = 15.;
        double phi = 30.;
        double rzero = 0.;
        int lmn = 9;
        int isteer = 0;
        double wlamda = 50.;
        double frequency = 50.;
        double soundSpeed = 5000.;
        int idelay = 1;
        double[] xpos = new double[2000];
        double[] ypos = new double[2000];
        double[] zpos = new double[2000];
        double[] dela = new double[2000];
        double[] rshad = new double[2000];
        double[] rshadFull = new double[2000];
        
        // read inputs from file
        try{
            String filename = "aphf_25out.txt";
            Scanner in = new Scanner(new FileReader(filename)); 
            title = in.nextLine();   
            theta = in.nextDouble();
            phi = in.nextDouble();
            frequency = in.nextDouble();
            soundSpeed = in.nextDouble();
            //wlamda = in.nextDouble();
            rzero = in.nextDouble();
            lmn = in.nextInt();
            for(int ctr = 0; ctr < lmn; ctr++) 
            {
                xpos[ctr] = in.nextDouble();
                ypos[ctr] = in.nextDouble();
                zpos[ctr] = in.nextDouble();
                dela[ctr] = in.nextDouble();
                rshad[ctr] = in.nextDouble();
                rshadFull[ctr] = in.nextDouble();
            }
            System.out.println("");
        }
        catch (FileNotFoundException e)
        {
            System.out.println("File not found");
            lmn = 9;
            for(int ctr = 0; ctr < 9; ctr++)
            {
                xpos[ctr] = ctr*250.;
                ypos[ctr] = 0.;
                zpos[ctr] = 0.;
                rshad[ctr] = 1.;
                dela[ctr] = 0.;
            }
        }
        
        wlamda = soundSpeed/frequency;
        double ymax = 0.;
        
        // do the full array first
        Gbeam gbeamFull = new Gbeam(lmn, 0, idelay, xpos, ypos, zpos, dela, rshadFull);
        double levelFull = gbeamFull.get_gbsum(theta,phi, rzero, wlamda);
        double bpyFull[][] = new double[385][1];
        double bpxFull[] = new double[385];
        double th[] = new double[385];
        int i;
        double ph;
        int nPhi = 361;
        double ymaxFull = 0;
        for (i = 0; i < nPhi; i++) {
            ph = i-90.;
            bpxFull[i] = ph;
            bpyFull[i][0] = gbeamFull.get_gbsum(theta, ph, rzero, wlamda);
            if (ymaxFull < bpyFull[i][0]) ymaxFull = bpyFull[i][0];
        }

        // now do it all again, but for the partially populated array
        for (int ctr = 0; ctr < lmn; ctr++)dela[ctr] = 0.;
        Gbeam gbeam = new Gbeam(lmn, 0, idelay, xpos, ypos, zpos, dela, rshad);
        double level = gbeam.get_gbsum(theta,phi, rzero, wlamda);
        double bpy[][] = new double[385][1];
        double bpx[] = new double[385];
        for (i = 0; i < nPhi; i++) {
            ph = i-90.;
            bpx[i] = ph;
            bpy[i][0] = gbeam.get_gbsum(theta, ph, rzero, wlamda);
            if (ymax < bpy[i][0]) ymax = bpy[i][0];           
        }
            
        for (i = 0; i < nPhi; i++) {
            bpy[i][0] = bpy[i][0] / ymax;
            bpyFull[i][0] = bpyFull[i][0] / ymaxFull;
        }
        
        // now compute comparable DI values for both arrays
        // we are assuming a line array, so the integration is simple and can use the arrays
        // we have already computed
        double sumOmni = 0.;
        double sumFull = 0.;
        double sum = 0.;
        double area = 0.;
        double hLast = 0.;
        double h;
        for (i = 0; i < 180; i++){
            // compute area using A = 2*pi*h : https://en.wikipedia.org/wiki/Spherical_cap
            // h = 1-sin(ph)   ( = 1-cos(90-ph)   )
            h = 1 + Math.sin(bpx[i+1]*RAD);
            area = 2.*PI*(h - hLast);
            hLast = h;
            sumOmni = sumOmni + area;
            sumFull = sumFull + area*(bpyFull[i][0] + bpyFull[i+1][0])/2.;
            sum = sum + area*(bpy[i][0] + bpy[i+1][0])/2.;
        }
        double di_full = 10.*Math.log(sumOmni/sumFull)/Math.log(10.);
        double di_partial = 10.*Math.log(sumOmni/sum)/Math.log(10.);
        double di_diff = 10.*Math.log(sum/sumFull)/Math.log(10.);
        double bw_check = wlamda/(bpx[lmn]-bpx[0])/RAD;  // in degrees
        double bw_di = 10.*Math.log(180./bw_check)/Math.log(10.);
        BasicFrame frame = new BasicFrame(bpx, th, bpy, bpyFull,nPhi, title, theta, phi, frequency);

        
        // write outputs to a file
         try {

             FileWriter outFile = new FileWriter("gbeam_out.txt");
             PrintWriter out = new PrintWriter(outFile);
             
             // Also could be written as follows on one line
             // Printwriter out = new PrintWriter(new FileWriter(args[0]));
         
             // Write text to file
             out.println("Computed DI values:");
             out.println("    Actual array DI = " + di_partial);
             out.println("    Full array DI = " + di_full);
             out.println("    Back-of-envelope DI = "  + bw_di);
             out.println("    DI degradation = " + di_diff); 
             out.println("INPUTS:");
             out.println("    Title = " + title);
             out.println("    Theta = " + theta);
             out.println("    Phi = " + phi);
             out.println("    Frequency = " + frequency);
             out.println("    Sound Speed = " + soundSpeed);
             out.println("    Rzero = " + rzero);
             out.println("    Number of elements = " + lmn);
             out.println("    X      ,     Y    ,       Z     ,   SHADING   , SHADING (Full array)");
             for(int ctr = 0; ctr < lmn; ctr++) 
             {
                 out.println("    " + xpos[ctr] + "    ,    " + ypos[ctr] + "    ,    "+ zpos[ctr] + "   ,    " + rshad[ctr] + "   , "  + rshadFull[ctr]);
             } 
             out.println("OUTPUTS:");
             out.println("    Wavelength = " + wlamda);
             i = 0;
             out.println("    BEAM PATTERN");
             out.println("    PHI   ,    LEVEL  ,   LEVEL (Full)");
             double dB;
             double dbFull;
             double bpc;
             while (i < 181)
             {
                 bpc = Math.max(bpy[i][0], 1.e-30);
                 dB = 10*Math.log(bpc)/Math.log(10.);
                 dbFull = 10.*Math.log(Math.max(bpyFull[i][0], 1.e-30))/Math.log(10.);
                 out.println("    " + bpx[i] + "   ,    " + dB + "  , " + dbFull);
                 i++;
             }
             out.close();
         } catch (IOException e){
             e.printStackTrace();
         }
        
        frame.setSize(1200,1000);

        frame.setVisible(true);
        
        


        
    }
}