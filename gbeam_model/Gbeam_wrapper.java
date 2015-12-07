import java.awt.*;
import java.io.*;
//import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;;
import javax.swing.*;
import java.nio.file.StandardOpenOption;
import java.lang.Enum;
//import java.io.FileWriter;
//import java.io.PrintWriter;

/**
 * Write a description of class GbeamTester3D here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 * Inputs:
     Output Filename (this is new, but I think it's a good idea for multi-threaded applications)
     Flags:
       [0] - output DI and effective beamwidths vs. frequency.  Set to 0 for no outputs, 
             1 for space-separated, 2 for comma-separated
       [1] - set to 1 to output ALL computed data, with one file for each frequency and array.
       [2] - set to 1 to output 3-d beam patterns, one for each frequency, array and type (ideal or actual)
       [3] - set to 1 to output 3-d element positions, and degradation levels, for each array - one file per array
     Number of frequencies of interest
     Frequencies of interest
     sound speed
    * number of arrays (for active, this may be two). 
    *For each array:
     phi, theta steer directions
     number of apertures.
     For each aperture:
     freq min and max for each aperture 
     rzero, radius of curvature for array focussing
     array element data (for both ideal and degraded arrays)
 */
class gbeam_wrapper_inputs
{
    private static double RAD = .017453293;
    private static double PI = 3.14159265;

    private String outputfile;
    private int[] flags;
    private int dTheta;
    private int dPhi;
    private int nFreq;
    private double[] freq;
    private double soundSpeed;
    private int nArrays;
    private int[] arrayType;
    private double[] phi;
    private double[] theta;
    private int[] nAp;
    private double[][] fmin;
    private double[][] fmax;
    private double[][] rzero;
    private int[][] lmn;
    private double[][][] idealXpos;
    private double[][][] idealYpos;
    private double[][][] idealZpos;
    private double [][][] idealShad;
    private double[][][] xpos;
    private double[][][] ypos;
    private double[][][] zpos;
    private double[][][] shad;
    // computed rather than input
    private int nTheta;
    private int nPhi;
    
    public gbeam_wrapper_inputs(String filename){
        try{
            Scanner in = new Scanner(new FileReader(filename)); 
            this.outputfile = in.nextLine();
            this.flags = new int[4];
            for (int ctr = 0; ctr < 4; ctr++){this.flags[ctr] = in.nextInt();}
            this.dTheta = in.nextInt();
            this.dPhi = in.nextInt();
            this.nTheta = 360/this.dTheta + 1;
            this.nPhi = 180/this.dPhi + 1;
            this.nFreq = in.nextInt();
            this.freq = new double[nFreq];
            for (int ctr = 0; ctr < nFreq; ctr++){this.freq[ctr] = in.nextDouble();}
            this.soundSpeed = in.nextDouble();
            this.nArrays = in.nextInt();
            int maxAp = in.nextInt();
            int maxlmn = in.nextInt();
            this.fmin = new double[nArrays][maxAp];
            this.fmax = new double[nArrays][maxAp];
            this.rzero = new double[nArrays][maxAp];
            this.lmn = new int[nArrays][maxAp];
            this.idealXpos = new double[nArrays][maxAp][maxlmn];
            this.idealYpos = new double[nArrays][maxAp][maxlmn];
            this.idealZpos = new double[nArrays][maxAp][maxlmn];
            this.idealShad = new double[nArrays][maxAp][maxlmn];
            this.xpos = new double[nArrays][maxAp][maxlmn];
            this.ypos = new double[nArrays][maxAp][maxlmn];
            this.zpos = new double[nArrays][maxAp][maxlmn];
            this.shad = new double[nArrays][maxAp][maxlmn];
            this.arrayType = new int[nArrays];
            this.phi = new double[nArrays];
            this.theta = new double[nArrays];
            this.nAp = new int[nArrays];
            for (int iArray = 0; iArray < nArrays; iArray++){
                this.arrayType[iArray] = in.nextInt();
                this.phi[iArray] = in.nextDouble();
                this.theta[iArray] = in.nextDouble();
                this.nAp[iArray] = in.nextInt();
                System.out.println(" Number of apertures = " + this.nAp[iArray]);
                for (int iAp = 0; iAp < nAp[iArray]; iAp++){
                    this.fmin[iArray][iAp] = in.nextDouble();
                    this.fmax[iArray][iAp] = in.nextDouble();
                    System.out.println(" ap, fmin, fmax = " + iAp + ", " + this.fmin[iArray][iAp] + ", " + this.fmax[iArray][iAp]);
                    this.rzero[iArray][iAp] = in.nextDouble();
                    this.lmn[iArray][iAp] = in.nextInt();
                    for (int ilmn = 0; ilmn < lmn[iArray][iAp]; ilmn++){
                        this.idealXpos[iArray][iAp][ilmn] = in.nextDouble();
                        this.idealYpos[iArray][iAp][ilmn] = in.nextDouble();
                        this.idealZpos[iArray][iAp][ilmn] = in.nextDouble();
                        this.idealShad[iArray][iAp][ilmn] = in.nextDouble();
                        this.xpos[iArray][iAp][ilmn] = in.nextDouble();
                        this.ypos[iArray][iAp][ilmn] = in.nextDouble();
                        this.zpos[iArray][iAp][ilmn] = in.nextDouble();
                        this.shad[iArray][iAp][ilmn] = in.nextDouble();
                    }
                }
            }
       
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Wrapper input File not found");
        }
    }
    public int get_flag(int iflag) { return (this.flags[iflag]);  }
    public int get_narrays()    {        return (this.nArrays);    }
    public int get_nfreq()    {        return (this.nFreq);    }
    public double get_freq(int ifreq)    {        return (this.freq[ifreq]);    }
    public int get_nap(int iarray)    {        return (this.nAp[iarray]);    }
    public double get_theta(int iarray)    {        return (this.theta[iarray]);    }
    public double get_phi(int iarray)    {        return (this.theta[iarray]);    }
    public double get_fmin(int iarray, int iap)    {        return (this.fmin[iarray][iap]);    }
    public double get_fmax(int iarray, int iap)    {        return (this.fmax[iarray][iap]);    }
    public double get_rzero(int iarray, int iap)   {        return (this.rzero[iarray][iap]);    }
    public String get_outputfile()    {        return (this.outputfile);    }
    public int get_lmn(int iarray, int iap)    {        return(this.lmn[iarray][iap]);    }
    public double[] get_idealxpos(int iarray, int iap)    {
        double[] x = new double[this.lmn[iarray][iap]];
        for (int i = 0; i < this.lmn[iarray][iap]; i++){
            x[i] = this.idealXpos[iarray][iap][i];
        }
        return (x);
    }
    public double[] get_idealypos(int iarray, int iap)    {
        double[] x = new double[this.lmn[iarray][iap]];
        for (int i = 0; i < this.lmn[iarray][iap]; i++){
            x[i] = this.idealYpos[iarray][iap][i];
        }
        return (x);
    }
    public double[] get_xpos(int iarray, int iap)    {
        double[] x = new double[this.lmn[iarray][iap]];
        for (int i = 0; i < this.lmn[iarray][iap]; i++){
            x[i] = this.xpos[iarray][iap][i];
        }
        return (x);
    }
    public double[] get_ypos(int iarray, int iap)    {
        double[] x = new double[this.lmn[iarray][iap]];
        for (int i = 0; i < this.lmn[iarray][iap]; i++){
            x[i] = this.ypos[iarray][iap][i];
        }
        return (x);
    }
    public double[] get_zpos(int iarray, int iap)    {
        double[] x = new double[this.lmn[iarray][iap]];
        for (int i = 0; i < this.lmn[iarray][iap]; i++){
            x[i] = this.zpos[iarray][iap][i];
        }
        return (x);
    }
    public double[] get_idealzpos(int iarray, int iap)    {
        double[] x = new double[this.lmn[iarray][iap]];
        for (int i = 0; i < this.lmn[iarray][iap]; i++){
            x[i] = this.idealZpos[iarray][iap][i];
        }
        return (x);
    }
    public double[] get_idealshad(int iarray, int iap)    {
        double[] x = new double[this.lmn[iarray][iap]];
        for (int i = 0; i < this.lmn[iarray][iap]; i++){
            x[i] = this.idealShad[iarray][iap][i];
        }
        return (x);
    }
    public double[] get_shad(int iarray, int iap)    {
        double[] x = new double[this.lmn[iarray][iap]];
        for (int i = 0; i < this.lmn[iarray][iap]; i++){
            x[i] = this.shad[iarray][iap][i];
        }
        return (x);
    }
    public double[] init_dela(int iarray, int iap)    {
        double[] x = new double[this.lmn[iarray][iap]];
        for (int i = 0; i < this.lmn[iarray][iap]; i++)x[i] = 0.;
        return (x);
    }
    public double get_wlamda(int ifreq)    {
        return (this.soundSpeed/this.freq[ifreq]);
    }
    public double compute_bw(double di)  { 
        if (di > -998.){
            return (180./Math.pow(10., di/10.));  
        }
        else {
            return (di);    // return the same "flagged" value
        }
    }
    public int get_dth() { return (this.dTheta); }
    public int get_ntheta() {           return (this.nTheta);        }
    public int get_nphi() {                 return (this.nPhi);      }
    public double[] get_thangles() {
        double[] x = new double[this.nTheta];
        for (int i = 0; i < this.nTheta; i++) x[i] = (double)i*this.dTheta;
        return (x);
    }
    public double[] get_phangles() {
        double[] x = new double[this.nPhi];
        for (int i = 0; i < this.nPhi; i++) x[i] = (double)(-90+i*this.dPhi);
        return (x);
    }
    public double compute_di(double[][] bp, double[] bpx){
                // now compute comparable DI values for both arrays
        // we are assuming a line array, so the integration is simple and can use the arrays
        // we have already computed
        double sumOmni = 0.;
        double sumFull = 0.;
        double sum = 0.;
        double area = 0.;
        double hLast = 0.;
        double h;
        for (int iph = 0; iph < this.nPhi-1; iph++){
            // compute area using A = 2*pi*h : https://en.wikipedia.org/wiki/Spherical_cap
            // h = 1-sin(ph)   ( = 1-cos(90-ph)   )
            h = 1 + Math.sin(bpx[iph+1]*RAD);
            area = 2.*PI*(h - hLast)/(this.nTheta-1);
            hLast = h;
            for (int ith=0; ith< this.nTheta-1; ith++){
                sumOmni = sumOmni + area;
                sum = sum + area*(bp[iph][ith] + bp[iph+1][ith])/2.;
            }
        }
        double di = 10.*Math.log(sumOmni/sum)/Math.log(10.);
        return (di);
    }
    public int write_di(double[][]diFull, double[][]di, int flag) {
        int ier = 0;
        try {
             FileWriter outFile = new FileWriter(this.outputfile+"_di.txt");     //,true);  (for append)
             PrintWriter out = new PrintWriter(outFile);
             out.format("Number of arrays = %d %n", this.nArrays);
             out.format("Number of Frequencies = %d %n", this.nFreq);
             String sep = " ";
             if (flag == 2) sep = " , ";
             for (int iarray = 0; iarray < this.nArrays; iarray++) 
             {
                 out.format(" Array number = %d%n", iarray);
                 out.format("Frequency%s     DI(Ideal)%s DI (Degraded)%s Difference %s BW (Ideal)%s BW (Degraded)%n",sep,sep,sep,sep,sep);
                 for (int ifreq = 0; ifreq < this.nFreq; ifreq++)
                 {
                     out.format(" %10.3f%s%10.3f%s%10.3f%s%10.3f%s %10.3f%s %10.3f%n", 
                                 this.freq[ifreq], sep, diFull[iarray][ifreq],sep, di[iarray][ifreq],
                                 sep, diFull[iarray][ifreq] - di[iarray][ifreq],
                                 sep,compute_bw(diFull[iarray][ifreq]), sep,compute_bw(di[iarray][ifreq]));
                 }
             }
             out.close();
        } 
        catch (IOException e){
            ier = 1;
            e.printStackTrace();
        }
        return (ier);
    }
    public int write_array_elements() {
        int ier = 0;
        int sum_elem;
        double dpct;
        double tol = .01;
        int icolor = 0;
        for (int iarray = 0; iarray < this.nArrays; iarray++){
            try {
                 FileWriter outFile = new FileWriter(this.outputfile+"_a"+iarray+"_elements.txt");
                 PrintWriter out = new PrintWriter(outFile);
                 // for nested arrays, add up all the elements
                 sum_elem = 0;
                 for (int iap = 0; iap < this.nAp[iarray]; iap++) {
                     sum_elem = sum_elem + this.lmn[iarray][iap];
                 }
                 out.println("   " + sum_elem);
                 
                 for (int iap = 0; iap < this.nAp[iarray]; iap++) {
                     for (int ilmn = 0; ilmn < this.lmn[iarray][iap]; ilmn++){
                         /*
                          * determine icolor: if element shading is less than 99% of ideal, red
                          *             else
                          *               if this.arrayType = 2 (active transmit), then green
                          *               otherwise, blue
                          */
                         if(Math.abs(this.idealShad[iarray][iap][ilmn] - this.shad[iarray][iap][ilmn]) > tol){
                             icolor = 2;  // bad, red
                         }
                         else if (this.arrayType[iarray] < 2) {
                             icolor = 1;   // passive element, blue
                            }
                         else icolor = 0;   // active element, green
                         out.format("%10.3f %10.3f %10.3f %d %n ", this.xpos[iarray][iap][ilmn],
                                    this.ypos[iarray][iap][ilmn],this.zpos[iarray][iap][ilmn], icolor);
                     }
                 }
                 out.close();
            } catch (IOException e){
                 e.printStackTrace();
            }
        }
        return (ier);
            
    }
    public int write_bad_outputs(int iarray, int ifreq) {
        int ier = 0;
        try {
             FileWriter outFile = new FileWriter(this.outputfile+"_a"+iarray+"_f"+ifreq+".txt");
             PrintWriter out = new PrintWriter(outFile);
             out.format("Array Number = %d %n", iarray);
             out.format("Frequency Number = %d %n", ifreq);
             out.format("Frequency = %10.1f %n", this.freq[ifreq]);
             out.format("DI and Beamwidth = %10.3f  %10.3f%n", -999., -999.);
             out.format("NO DATA.   Frequency is outside the range of any array aperture%n");
             out.close();
        } catch (IOException e){
             e.printStackTrace();
        }
        return (ier);
            
    }
    public int write_outputs(int iarray, int ifreq, double diFull, double di, double[][] bpyFull, double[][]bpy,
                             double[] bpx, double[] bpth, int iflag)    {
        //  iflag = 1 for full output, 2 for just the ideal beam patterh, 3 for just the actual beam pattern
        int ier = 0;
        try {
             FileWriter outFile;
             PrintWriter out;
             if (iflag == 1)   {
                 outFile = new FileWriter(this.outputfile+"_a"+iarray+"_f"+ifreq+".txt");
                 out = new PrintWriter(outFile);
                 out.format("Array Number = %d %n", iarray);
                 out.format("Frequency Number = %d %n", ifreq);
                 out.format("Frequency = %10.1f %n", this.freq[ifreq]);
                 out.format("DI and Beamwidth = %10.3f  %10.3f%n", di, compute_bw(di));
                 out.format("Ideal DI and Beamwidth = %10.3f %10.3f%n", diFull, compute_bw(diFull));
                 out.format("Array Beam Pattern for 3-d Plotting:%n");
                 out.format("Number of Rectangles = %d %n",(this.nTheta-1)*(this.nPhi-1));
             }
             else if (iflag ==2)  {
                 outFile = new FileWriter(this.outputfile+"_a"+iarray+"_f"+ifreq+"_bpFull.txt");
                 out = new PrintWriter(outFile);
                 out.format("%d %n",(this.nTheta-1)*(this.nPhi-1));
             }
             else    {
                 outFile = new FileWriter(this.outputfile+"_a"+iarray+"_f"+ifreq+"_bp.txt");
                 out = new PrintWriter(outFile);
                 out.format("%d %n",(this.nTheta-1)*(this.nPhi-1));
             }
             if (iflag ==1 || iflag ==2) {
                 for (int iph = 0; iph < this.nPhi - 1; iph++){
                     for (int ith = 0; ith < this.nTheta-1; ith++){
                         double dB1 = 10*Math.log(Math.max(bpy[iph][ith], 1.e-30))/Math.log(10.);
                         double dB2 = 10*Math.log(Math.max(bpy[iph+1][ith], 1.e-30))/Math.log(10.);
                         double dB3 = 10*Math.log(Math.max(bpy[iph][ith+1], 1.e-30))/Math.log(10.);
                         double dB4 = 10*Math.log(Math.max(bpy[iph+1][ith+1], 1.e-30))/Math.log(10.);
                         out.format("%6d  %6d  %6d  %6d  %10.3f %10.3f %10.3f %10.3f %n",
                                    (int)bpx[iph], (int)bpx[iph+1], (int)bpth[ith], (int)bpth[ith+1], dB1, dB2, dB3, dB4);
                     }
                 }
             }
             if (iflag ==1) {
                 out.format("Ideal Array Beam Pattern for 3-d Plotting:%n");
                 out.format("Number of Rectangles = %d %n", (this.nTheta-1)*(this.nPhi-1));
             }
             if(iflag == 1 || iflag == 3) {
                 for (int iph = 0; iph < this.nPhi - 1; iph++){
                     for (int ith = 0; ith < this.nTheta-1; ith++){
                         double dB1 = 10*Math.log(Math.max(bpyFull[iph][ith], 1.e-30))/Math.log(10.);
                         double dB2 = 10*Math.log(Math.max(bpyFull[iph+1][ith], 1.e-30))/Math.log(10.);
                         double dB3 = 10*Math.log(Math.max(bpyFull[iph][ith+1], 1.e-30))/Math.log(10.);
                         double dB4 = 10*Math.log(Math.max(bpyFull[iph+1][ith+1], 1.e-30))/Math.log(10.);
                         out.format("%6d  %6d  %6d  %6d  %10.3f %10.3f %10.3f %10.3f %n",
                                    (int)bpx[iph], (int)bpx[iph+1], (int)bpth[ith], (int)bpth[ith+1], dB1, dB2, dB3, dB4);
                     }
                 }
             }
             out.close();
         } catch (IOException e){
             e.printStackTrace();
        }
        return (ier);
    }
}
public class Gbeam_wrapper
{
    // instance variables - replace the example below with your own
    private static double RAD = .017453293;
    private static double PI = 3.14159265;
    public static void main()
    {
        gbeam_wrapper_inputs gbi = new gbeam_wrapper_inputs("wrapper.txt");
        // right away, if the user asks for it, write the output file for array elememt display
        int ier;
        if (gbi.get_flag(3) != 0) ier = gbi.write_array_elements();
        double di[][] = new double[gbi.get_narrays()][gbi.get_nfreq()];
        double diFull[][] = new double[gbi.get_narrays()][gbi.get_nfreq()];
        System.out.println("  Starting program: number of arrays = " + gbi.get_narrays());
        double[] bpth = new double[gbi.get_ntheta()];
        bpth = gbi.get_thangles();
        double[] bpx = new double[gbi.get_nphi()];
        bpx = gbi.get_phangles();
        double[][] bpyFull = new double[gbi.get_nphi()][gbi.get_ntheta()];
        double[][] bpy = new double[gbi.get_nphi()][gbi.get_ntheta()];
        double ymaxFull = 0.;
        double ymax = 0.;
        // outermost loop is on frequencies
        for (int ifreq = 0; ifreq < gbi.get_nfreq(); ifreq++){
            double freq = gbi.get_freq(ifreq);
            // loop on arrays
            for (int iarray = 0; iarray < gbi.get_narrays(); iarray++){
                // now, find the aperture that goes with this frequency
                int iap = -1;
                for (int iapTst = 0; iapTst < gbi.get_nap(iarray); iapTst++){
                    if (gbi.get_fmin(iarray, iapTst) <= freq && gbi.get_fmax(iarray,iapTst) >= freq) iap = iapTst;
                }
                if (iap >= 0){
                    // for the "ifound'th" aperture of this array, do all computations
                    Gbeam gbeamFull = new Gbeam(gbi.get_lmn(iarray, iap), 0, 1, gbi.get_idealxpos(iarray,iap), 
                                                gbi.get_idealypos(iarray,iap), gbi.get_idealzpos(iarray,iap),
                                                gbi.init_dela(iarray, iap), gbi.get_idealshad(iarray,iap));
                    double ideallvl = gbeamFull.get_gbsum(gbi.get_theta(iarray), gbi.get_phi(iarray),
                                                gbi.get_rzero(iarray, iap), gbi.get_wlamda(ifreq));
                    ymaxFull = 0.;
                    ymax = 0.;
                    for (int iph = 0; iph < gbi.get_nphi(); iph++) {
                        for (int ith = 0; ith < gbi.get_ntheta(); ith++){
                            bpyFull[iph][ith] = gbeamFull.get_gbsum(bpth[ith], bpx[iph],gbi.get_rzero(iarray, iap), gbi.get_wlamda(ifreq));
                            if (ymaxFull < bpyFull[iph][ith]) ymaxFull = bpyFull[iph][ith];
                        }
                    }
                    Gbeam gbeam = new Gbeam(gbi.get_lmn(iarray, iap), 0, 1, gbi.get_idealxpos(iarray,iap), 
                                            gbi.get_idealypos(iarray,iap), gbi.get_idealzpos(iarray,iap),
                                            gbi.init_dela(iarray, iap), gbi.get_shad(iarray,iap));
                    double lvl = gbeam.get_gbsum(gbi.get_theta(iarray), gbi.get_phi(iarray),
                                                 gbi.get_rzero(iarray, iap), gbi.get_wlamda(ifreq));
                    gbeam.gbeam_updatePos(gbi.get_xpos(iarray, iap), gbi.get_ypos(iarray, iap), gbi.get_zpos(iarray, iap));  
                    for (int iph = 0; iph < gbi.get_nphi(); iph++) {
                        for (int ith = 0; ith < gbi.get_ntheta(); ith++){
                            bpy[iph][ith] = gbeam.get_gbsum(bpth[ith], bpx[iph],gbi.get_rzero(iarray, iap), gbi.get_wlamda(ifreq));
                            if (ymax < bpy[iph][ith]) ymax = bpy[iph][ith];
                        }
                    }
                    for (int iph = 0; iph < gbi.get_nphi(); iph++) {
                        for (int ith=0; ith < gbi.get_ntheta(); ith++){
                            bpy[iph][ith] = bpy[iph][ith] / ymax;
                            bpyFull[iph][ith] = bpyFull[iph][ith] / ymaxFull;
                        }
                    }
                    diFull[iarray][ifreq] = gbi.compute_di(bpyFull, bpx);
                    di[iarray][ifreq] = gbi.compute_di(bpy, bpx);
                    if(gbi.get_flag(1) == 1) {
                        ier = gbi.write_outputs(iarray, ifreq, diFull[iarray][ifreq], di[iarray][ifreq], bpyFull, bpy, bpx, bpth, 1); 
                        if (ier != 0)System.out.println(" Problem writing file");
                    }
                    if(gbi.get_flag(2) == 1) {
                        ier = gbi.write_outputs(iarray, ifreq, diFull[iarray][ifreq], di[iarray][ifreq], bpyFull, bpy, bpx, bpth, 2); 
                        if (ier != 0)System.out.println(" Problem writing beampattern file");
                        ier = gbi.write_outputs(iarray, ifreq, diFull[iarray][ifreq], di[iarray][ifreq], bpyFull, bpy, bpx, bpth, 3); 
                        if (ier != 0)System.out.println(" Problem writing beampattern file");
                    }
                }
                else
                {
                    diFull[iarray][ifreq] = -999.;
                    di[iarray][ifreq] = -999.;
                    System.out.println(" no aperture for array # " + iarray + " contains frequency " + freq);
                    if(gbi.get_flag(1) == 1) {
                        ier = gbi.write_bad_outputs(iarray, ifreq) ;  //, diFull[iarray][ifreq], di[iarray][ifreq], bpyFull, bpy, bpx, bpth); 
                        if (ier != 0)System.out.println(" Problem writing file");
                    }
                }
            }
        } 
        if(gbi.get_flag(0) != 0) {
            ier = gbi.write_di(diFull, di, gbi.get_flag(0));
        }
    }
}