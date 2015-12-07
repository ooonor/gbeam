import java.awt.*;
import java.io.*;
//import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;;
import javax.swing.*;
//import java.io.FileWriter;
//import java.io.PrintWriter;

/**
 * Write a description of class GbeamTester3D here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class GbeamTester3D
{
    // instance variables - replace the example below with your own
    private static double RAD = .017453293;
    private static double PI = 3.14159265;
    
    public static void main()
    {
        // decide whether we want to do the full 3-d pattern here, in case the HLA is buckled
        int nTheta = 91;  // 4 degree increments, and repeat the first value
        // set previous to 0 to do it the one-dimensional way
        String title = "Blank Title";
        String dummy = "didn't read it?";
        double theta = 15.;
        double phi = 30.;
        double rzero = 0.;
        int lmn = 9;
        int isteer = 0;
        double wlamda = 50.;
        double frequency = 50.;
        double soundSpeed = 5000.;
        int idelay = 1;
        int maxlmn = 2000;
        double[] xpos = new double[maxlmn];
        double[] ypos = new double[maxlmn];
        double[] zpos = new double[maxlmn];
        double[] dela = new double[maxlmn];
        double[] rshad = new double[maxlmn];
        double[] xposFull = new double[maxlmn];
        double[] yposFull = new double[maxlmn];
        double[] zposFull = new double[maxlmn];
        double[] rshadFull = new double[maxlmn];
        
        // read inputs from file
        try{
            String filename = "aphf_0out.txt";
            Scanner in = new Scanner(new FileReader(filename)); 
            title = in.nextLine();
            theta = in.nextDouble();
            phi = in.nextDouble();
            frequency = in.nextDouble();
            soundSpeed = in.nextDouble();
            rzero = in.nextDouble();
            lmn = in.nextInt();
            dummy = in.nextLine();  // one just to get past end of current line
            dummy = in.nextLine();
            for(int ctr = 0; ctr < lmn; ctr++) 
            {
                xpos[ctr] = in.nextDouble();
                ypos[ctr] = in.nextDouble();
                zpos[ctr] = in.nextDouble();
                dela[ctr] = in.nextDouble();
                rshad[ctr] = in.nextDouble();
                xposFull[ctr] = in.nextDouble();
                yposFull[ctr] = in.nextDouble();
                zposFull[ctr] = in.nextDouble();
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
        Gbeam gbeamFull = new Gbeam(lmn, 0, idelay, xposFull, yposFull, zposFull, dela, rshadFull);
        int nPhi = 361;
        double levelFull = gbeamFull.get_gbsum(theta,phi, rzero, wlamda);
        double bpyFull[][] = new double[365][nTheta];
        double bpx[] = new double[365];
        double bpth[] = new double[nTheta+1];
        int i;
        double ph;
        int itheta;
        double ymaxFull = 0;
        double dth = 360./(double)(nTheta-1);
        // need to define theta angles only once, outside phi loop
        for (itheta = 0; itheta < nTheta; itheta++){
            bpth[itheta] = itheta*dth;
        }
        for (i = 0; i < nPhi; i++) {
            ph = i-90.;
            bpx[i] = ph;
            for (itheta = 0; itheta < nTheta; itheta++){
                bpyFull[i][itheta] = gbeamFull.get_gbsum(bpth[itheta], ph, rzero, wlamda);
                if (ymaxFull < bpyFull[i][itheta]) ymaxFull = bpyFull[i][itheta];
            }
        }

        // now do it all again, but for the partially populated array
        for (int ctr = 0; ctr < lmn; ctr++)dela[ctr] = 0.;
        Gbeam gbeam = new Gbeam(lmn, 0, idelay, xposFull, yposFull, zposFull, dela, rshad);
        double level = gbeam.get_gbsum(theta,phi, rzero, wlamda);
        gbeam.gbeam_updatePos(xpos, ypos, zpos);
        double bpy[][] = new double[365][nTheta];
        for (i = 0; i < nPhi; i++) {
            ph = i-90.;
            for (itheta = 0; itheta < nTheta; itheta++){
                bpy[i][itheta] = gbeam.get_gbsum(bpth[itheta], ph, rzero, wlamda);
                if (ymax < bpy[i][itheta]) ymax = bpy[i][itheta];  
            }
        }
        // nnormalize   
        for (i = 0; i < nPhi; i++) {
            for (itheta=0; itheta < nTheta; itheta++){
                bpy[i][itheta] = bpy[i][itheta] / ymax;
                bpyFull[i][itheta] = bpyFull[i][itheta] / ymaxFull;
            }
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
            area = 2.*PI*(h - hLast)/nTheta;
            hLast = h;
            for (itheta=0; itheta< nTheta-1; itheta++){
                sumOmni = sumOmni + area;
                sumFull = sumFull + area*(bpyFull[i][itheta] + bpyFull[i+1][itheta])/2.;
                sum = sum + area*(bpy[i][itheta] + bpy[i+1][itheta])/2.;
            }
        }
        double di_full = 10.*Math.log(sumOmni/sumFull)/Math.log(10.);
        double di_partial = 10.*Math.log(sumOmni/sum)/Math.log(10.);
        double di_diff = 10.*Math.log(sum/sumFull)/Math.log(10.);
        double bw_check = wlamda/(bpx[lmn]-bpx[0])/RAD;  // in degrees
        double bw_di = 10.*Math.log(180./bw_check)/Math.log(10.);
        BasicFrame frame = new BasicFrame(bpx,bpth, bpy,bpyFull,nPhi, title, theta, phi, frequency);
        double dB;
        double bpc;

         // write 3d beampattern file (different format from excel, so different file
        try {
             double dB1;
             double dB2;
             double dB3;
             double dB4;
             FileWriter outFile = new FileWriter("gbeam3D.txt");
             PrintWriter out = new PrintWriter(outFile);
             out.println((nTheta-1)*180);
             for (int iphi = 0; iphi < 180; iphi++){
                 for (int ith = 0; ith < nTheta-1; ith++){
                     dB1 = 10*Math.log(Math.max(bpy[iphi][ith], 1.e-30))/Math.log(10.);
                     dB2 = 10*Math.log(Math.max(bpy[iphi+1][ith], 1.e-30))/Math.log(10.);
                     dB3 = 10*Math.log(Math.max(bpy[iphi][ith+1], 1.e-30))/Math.log(10.);
                     dB4 = 10*Math.log(Math.max(bpy[iphi+1][ith+1], 1.e-30))/Math.log(10.);
                     out.format("%6d  %6d  %6d  %6d  %10.3f %10.3f %10.3f %10.3f %n",
                                iphi-90, iphi-89, (int)(ith*dth), (int)((ith+1)*dth), dB1, dB2, dB3, dB4);
                    }
                }
             out.close();
         } catch (IOException e){
             e.printStackTrace();
        }
         // write excel outputs to a csv file
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
             out.println("    X      ,    Y     ,       Z   ,  SHADING  ,  X(Full)  ,  Y(Full)  ,   Z(Full) , SHADING (Full)");
             for(int ctr = 0; ctr < lmn; ctr++) 
             {
                 out.format("%10.3f ,%10.3f, %10.3f, %10.3f, %10.3f, %10.3f, %10.3f, %10.3f%n",
                            xpos[ctr], ypos[ctr], zpos[ctr], rshad[ctr],xposFull[ctr],yposFull[ctr],zposFull[ctr], rshadFull[ctr]);
             } 
             out.println("OUTPUTS:");
             out.println("    Wavelength = " + wlamda);
             out.println("    BEAM PATTERN");
             out.println("    PHI   ,    LEVEL  ,   LEVEL (Full)");
             double dbFull;
             i = 0;
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