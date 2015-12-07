import java.io.*;
import java.util.Random;


/**
 * Write a description of class arrayGen3D here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class arrayGen3D
{
    private static double RAD = .017453293;
    private static double PI = 3.1415926535;
    private static double soundSpeed = 5000.;
    private static int maxlmn = 2000;
    private static XYZAPoint[] elem = new XYZAPoint[maxlmn];
    private static XYZAPoint[] ideal = new XYZAPoint[maxlmn];
    private static int nElem;
    private static int arrayType;
    private static int nAp;
    private static double[] xyWiggle = new double[10];
    private static double[] arrayLength = new double[10];
    private static double[] maxFreq = new double[10];
    private static String[] ap = {"lf", "mf", "hf"};

    private static int findElement(double z, double[] zAll, int nAll){
        double tol = .00001;
        for (int i = 0; i < nAll; i++){
            if (z < zAll[i] + tol) return(i);
        }
        return(nAll-1);
    }
    private static void build_hull_array(double arrayWid, double arrayHgt, double radius, double maxF, double soundspeed) {
        double wavelength = soundspeed/maxF;
        double spacing = .9*.5*wavelength;
        int nz = (int)(arrayWid/spacing) + 1;
        int nv = (int)(arrayHgt/spacing) + 1;
        nElem = 0;
        nAp = 1;
        for (int inz = 0; inz < nz; inz++){
            for (int inv = 0; inv < nv; inv++){
                double angle = (inv*spacing - arrayHgt/2.)/(2.*PI*radius)*360.;
                angle = angle * RAD;
                XYZAPoint p = new XYZAPoint();
                XYZAPoint p_ideal = new XYZAPoint();
                p.x = arrayHgt*Math.cos(angle);
                p_ideal.x = p.x;
                p.y = arrayHgt*Math.sin(angle);
                p_ideal.y = p.y;
                p.z = spacing*(double)inz - arrayWid/2.;
                p_ideal.z = p.z;
                p_ideal.out = 0;
                p_ideal.wgt = 1.;
                p.out = 0;
                p.wgt = 1.;
                if(Math.random() > .9) {
                    p.out = 2;
                    p.wgt = 0.;
                }
                p.ap = 0;
                p_ideal.ap = 0;
                elem[nElem] = p;
                
                ideal[nElem] = p_ideal;
                nElem++;
            }
        }
    }
    private static void build_line_array(double soundSpeed)    {
        nAp = 3;
        xyWiggle[0] = 1.5;
        xyWiggle[1] = .5;
        xyWiggle[2] = .25;
        arrayLength[0] = 1250.;
        arrayLength[1] = 1250./2.;
        arrayLength[2] = 1250./6.;
        maxFreq[0] = 200.;
        maxFreq[1] = 400.;
        maxFreq[2] = 1200.;
        double zmax = -1.;
        double[] zAll = new double[maxlmn];
        int[] icolor = new int[maxlmn];
        nElem = 0;
        // build the all-aperture array first, and go from HF to LF
        for (int iap = nAp-1; iap >= 0; iap--){
            double wavelength = soundSpeed/maxFreq[iap];
            double spacing = .9*.5*wavelength;
            int n = (int)(arrayLength[iap]/spacing) + 1;
            for (int i = 0; i <n; i++){
                double z = i * spacing;
                if (z > zmax){
                    zmax = z;
                    zAll[nElem] = z;
                    icolor[nElem] = iap;
                    nElem++;
                }
                else {
                    // determine index
                    int index = findElement(z,zAll, nElem);
                    icolor[index] = iap;
                }
            }
        }
        // for now, make the wiggle a single sine wave with amplitude xywiggle[0]
        double arg;
        for (int i = 0; i < nElem; i++){
            XYZAPoint p = new XYZAPoint();
            p.z = zAll[i];
            p.ap = icolor[i];
            arg = zAll[i]/zAll[nElem-1]*180.*RAD*5;
            p.x = xyWiggle[0]*Math.random()*Math.sin(arg)*4.;
            p.y = xyWiggle[0]*Math.random()*Math.cos(arg)*4.;
            p.out = 0;
            if(Math.random() > .9) p.out = 2;
            elem[i] = p;
        }
    }
    private static void build_cylinder(int nStaves, int nPerStave, double radius, double maxFreq, double soundspeed) {
        double wavelength = soundspeed/maxFreq;
        double spacing = .9*.5*wavelength;
        double yOffset = spacing*(double)nPerStave/2.;
        nElem = 0;
        nAp = 1;
        boolean staveout;
        for (int iStave = 0; iStave < nStaves; iStave++){
            double angle = iStave*360./(double)nStaves;
            double xtmp = radius*Math.sin(angle*RAD);
            double ztmp = radius*Math.cos(angle*RAD);
            staveout = false;
            if(Math.random() > .8) staveout = true;
            for (int iy = 0; iy < nPerStave; iy++){
                XYZAPoint p = new XYZAPoint();
                XYZAPoint p_ideal = new XYZAPoint();
                p.x = xtmp;
                p_ideal.x = xtmp;
                p.y = spacing*(double)iy - yOffset;
                p_ideal.y = p.y;
                p.z = ztmp;
                p_ideal.z = ztmp;
                p.ap = 0;
                p_ideal.ap = 0;
                p_ideal.out = 0;
                p_ideal.wgt = 1.;
                if(staveout || Math.random() > .9) {
                    p.out = 2;
                    p.wgt = 0.;
                }
                else {
                    p.out = 0;
                    p.wgt = 1.;
                }
                elem[nElem] = p;
                ideal[nElem] = p_ideal;
                nElem++;
                }
            }
    }
    private static void write_array_display_file(){
        try {
             FileWriter outFile = new FileWriter("nestedArray.txt");
             PrintWriter out = new PrintWriter(outFile);
             out.format("%d %n", nElem);
             for (int i = 0; i < nElem; i++){
                 out.format("%10.3f %10.3f %10.3f %d %d %n ", elem[i].x, elem[i].y, elem[i].z, 
                                             elem[i].out, elem[i].ap);
                }
             out.close();
        } 
        catch (IOException e){
             e.printStackTrace();
        }
    }
    private static void write_gbeam_wrapper_file(int iap, int nOut, double maxFreq) {
        try {
            String filename = "wrapper_ap" + ap[iap] + "_" + nOut + "out.txt";
            FileWriter outFile = new FileWriter(filename);
            PrintWriter out = new PrintWriter(outFile);
            out.println("gbeamTest");   // base name for output file
            out.println("1 1 1 1");     // flags (unused for now - all outputs are written to file
            out.println("4 1");  // dtheta, tphi
            out.println("1");  // nfreq
            out.println(maxFreq);  // frequency for beamforming (can be anything < maxFreq)
            out.println("5000.");   // sound speed
            out.println("1");  // number of arrays
            out.format("%d   %d %n", 1, nElem);  // max number of apertores, max number of elements
            out.println("0");  // 0 = passive receive element in passive system, 1 = pasive receiver in active system, 2 = active transmit element
            out.println("45.  45.   1");  // phi, theta, nap
            out.println("0.  " + (int)(maxFreq+1.) + "  0. " + nElem);  // fmin, fmax, rzero, lmn
            for(int ctr = 0; ctr < nElem; ctr++) {
                out.format("%10.3f %10.3f %10.3f %10.3f %10.3f %10.3f %10.3f %10.3f%n", 
                           ideal[ctr].x, ideal[ctr].y, ideal[ctr].z, ideal[ctr].wgt,
                           elem[ctr].x, elem[ctr].y, elem[ctr].z, elem[ctr].wgt);
            }
            out.close();
        } 
        catch (IOException e){
            e.printStackTrace();
        }
    }
    public static void main(){
        // select array type: 0=hull or 1=line, 2 =cylindrical, and then use that to branch later
        arrayType = 2;
        // change this to wiggle the array elements x,y
        int nDegradations = 3;
        int[] nOut = new int[nDegradations];
        nOut[0] = 0;
        nOut[1] = 10;   // make this mean percent
        nOut[2] = 25;   // 25%, not 25 elements
        String filename;
        String title;
        int n;  // number of elements
        double wavelength;
        double spacing;
        int i = 0;
        double[] xpos = new double[maxlmn];
        double[] ypos = new double[maxlmn];
        double[] zpos = new double[maxlmn];
        double[] dela = new double[maxlmn];
        double[] rshad = new double[maxlmn];
        double[] xposFull = new double[maxlmn];
        double[] yposFull = new double[maxlmn];
        double[] zposFull = new double[maxlmn];
        double[] rshadFull = new double[maxlmn];
        double angle;
        int edge;
        // inputs
        double[] xAll = new double[maxlmn];
        double[] yAll = new double[maxlmn];
        double[] zAll = new double[maxlmn];
        double[] sAll = new double[maxlmn];
        int[] icolor = new int[maxlmn];
        int nAll = 0;
        double zmax = -1.;
        double z;
        int index;
        if (arrayType == 0){
            double maxF = 1800.;
            build_hull_array(60., 20., 18., maxF, 5000.);        
            write_array_display_file();    
            write_gbeam_wrapper_file(0, 10, maxF); }
        else if (arrayType ==1) {
            build_line_array(soundSpeed);        
            write_array_display_file();   }
        else  {
            double maxF = 2000.;
            build_cylinder(72,8,4.17, maxF, soundSpeed);
            write_array_display_file();
            write_gbeam_wrapper_file(0, 10, maxF);
        }
        if(arrayType == 1) {
        for (int iap = 0; iap < nAp; iap++){
            wavelength = soundSpeed/maxFreq[iap];
            spacing = .9*.5*wavelength;
            n = (int)(arrayLength[iap]/spacing) + 1;
            if (n > maxlmn) n = maxlmn;
            edge = n/8;
            for (int ideg = 0; ideg < nDegradations; ideg++){
                title = "Aperture: " + ap[iap] + " Bad Elements: " + nOut[ideg]; 
                filename = "ap" + ap[iap] + "_" + nOut[ideg] + "out.txt";
                for (i = 0; i <n; i++){
                    xpos[i] = (Math.random()-.5)*2.*xyWiggle[iap];
                    xposFull[i] = 0.;
                    ypos[i] = (Math.random() - .5)*2.*xyWiggle[iap];
                    yposFull[i] = 0.;
                    zpos[i] = i*spacing;
                    zposFull[i] = i*spacing;
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
                     out.println("Actual:  X         Y         Z        DELAY    SHADING  Ideal:  X         Y         Z       SHADING");
                     for(int ctr = 0; ctr < n; ctr++) {
                         out.format("%10.3f %10.3f %10.3f %10.3f %10.3f %10.3f %10.3f %10.3f %10.3f%n", 
                                       xpos[ctr], ypos[ctr],zpos[ctr], dela[ctr],rshad[ctr],
                                       xposFull[ctr],yposFull[ctr],zposFull[ctr],rshadFull[ctr]);
                     }
                     out.close();
                } catch (IOException e){
                     e.printStackTrace();
                }
                // NOW write output file for gbeam_wrapper
                try {
                     filename = "wrapper_ap" + ap[iap] + "_" + nOut[ideg] + "out.txt";
                     FileWriter outFile = new FileWriter(filename);
                     PrintWriter out = new PrintWriter(outFile);
                     out.println("gbeamTest");   // base name for output file
                     out.println("1 1 1 1");     // flags (unused for now - all outputs are written to file
                     out.println("4 1");  // dtheta, tphi
                     out.println("1");  // nfreq
                     out.println(maxFreq[iap]);  // frequency for beamforming (can be anything < maxFreq)
                     out.println("5000.");   // sound speed
                     out.println("1");  // number of arrays
                     out.println("1  " + n);  // max number of apertores, max number of elements
                     out.println("0");  // 0 = passive receive element in passive system, 1 = pasive receiver in active system, 2 = active transmit element
                     out.println("45.  45.   1");  // phi, theta, nap
                     out.println("0.  " + (int)(maxFreq[iap]+1) + "  0. " + n);  // fmin, fmax, rzero, lmn
                     for(int ctr = 0; ctr < n; ctr++) {
                         out.format("%10.3f %10.3f %10.3f %10.3f %10.3f %10.3f %10.3f %10.3f%n", 
                                       xposFull[ctr],yposFull[ctr],zposFull[ctr],rshadFull[ctr],
                                       xpos[ctr], ypos[ctr],zpos[ctr], rshad[ctr]);
                     }
                     out.close();
                 } catch (IOException e){
                     e.printStackTrace();
                 }
            }
        }
        }
    }
}
