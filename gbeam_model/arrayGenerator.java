import java.io.*;
import java.util.Random;



/**
 * Write a description of class arrayGenerator here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class arrayGenerator
{
    private static double RAD = .017453293;

    public static void main(){
        int nAp = 3;
        double[] arrayLength = new double[nAp];
        arrayLength[0] = 1250.;
        arrayLength[1] = 1250./3.;
        arrayLength[2] = 1250./6.;
        int nDegradations = 3;
        int[] nOut = new int[nDegradations];
        nOut[0] = 0;
        nOut[1] = 10;
        nOut[2] = 25;
        double[] maxFreq = new double[nAp];
        String[] ap = {"lf", "mf", "hf"};
        maxFreq[0] = 200.;
        maxFreq[1] = 400.;
        maxFreq[2] = 1200.;
        String filename;
        String title;
        int n;  // number of elements
        double wavelength;
        double spacing;
        int i = 0;
        double[] xpos = new double[2000];
        double[] ypos = new double[2000];
        double[] zpos = new double[2000];
        double[] dela = new double[2000];
        double[] rshad = new double[2000];
        double[] rshadFull = new double[2000];
        double angle;
        int edge;
        // inputs
        double soundSpeed = 5000.;
        for (int iap = 0; iap < nAp; iap++){
            wavelength = soundSpeed/maxFreq[iap];
            spacing = .9*.5*wavelength;
            n = (int)(arrayLength[iap]/spacing) + 1;
            edge = n/8;
            System.out.println("number of elements in this aperture = " + n); //HI from array generator");
            for (int ideg = 0; ideg < nDegradations; ideg++){
                System.out.println("   number of elements out = " + nOut[ideg]);
                title = "Aperture: " + ap[iap] + " Bad Elements: " + nOut[ideg]; 
                filename = "ap" + ap[iap] + "_" + nOut[ideg] + "out.txt";
                for (i = 0; i <n; i++){
                    xpos[i] = 0.;
                    ypos[i] = 0.;
                    zpos[i] = i*spacing;
                    dela[i] = 0.;
                    rshad[i] = 1.;
                    if (i < edge){
                        angle = 90.*(1. - (double)i/(double)edge);
                        rshad[i] = Math.cos(angle*RAD)*Math.cos(angle*RAD);
                    }
                    if (i > n-edge){
                        angle = 90.*(double)(i-edge)/(double)edge;
                        rshad[i] = Math.cos(angle*RAD)*Math.cos(angle*RAD);
                    }
                    rshadFull[i] = rshad[i];
                }
                if (nOut[ideg] > 0){
                    Random diceRoller = new Random();
                    int nLeft = n;
                    System.out.println(" removing " + nOut[ideg] + " elements");
                    for (int ctr = 0; ctr < nOut[ideg]; ctr++){
                        int roll = diceRoller.nextInt(nLeft);
                        nLeft = nLeft - 1;
                        // delete the roll-th element of the non-deleted elements
                        int ctr2 = 0;
                        int ctr3 = 0;
                        for (ctr2 = 0; ctr2 < roll+1; ctr2++){
                            if (rshad[ctr3] == 0.)ctr3++; 
                            ctr3++;
                        }
                        rshad[ctr3-1] = 0.;
                    }
                }
                // write output file for this degradation and aperture combo
                try {
                     FileWriter outFile = new FileWriter(filename);
                     PrintWriter out = new PrintWriter(outFile);
                     out.println(title);
                     out.println("0.");
                     out.println("45.");
                     out.println(" " + maxFreq[iap]);
                     out.println(" " + soundSpeed);
                     out.println("  0.0");
                     out.println("  " + n);
                     for(int ctr = 0; ctr < n; ctr++) {
                         out.println("  " + xpos[ctr] + "  " + ypos[ctr] + "  "+ 
                         zpos[ctr] + "  " + dela[i] + "  " + rshad[ctr] + "  " + rshadFull[ctr]);
                     }
                     out.close();
                 } catch (IOException e){
                     e.printStackTrace();
                 }
            }
        }
    }
}
