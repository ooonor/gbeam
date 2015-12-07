import java.awt.*;
import java.io.*;
import java.util.Random;
import java.util.Arrays;
import java.util.Scanner;;
import javax.swing.*;
import java.nio.file.StandardOpenOption;
import java.lang.Enum;

/**
 * Write a description of class Gbeam_stats here.
 * 
 * @author Eleanor Wilson
 * @version 10/18/2015
 * Inputs:
 *    Max number of elements, max number of frequencies for any array, max # apertures
 *    Output filename
 *    Sound speed
 *    Number of degradations, min and max percent elements out
 *    Number of samples for each degradation
 *    Number of arrays
 *       for each array....
 *           array type (hull, line, cyl)
 *           min, max theta
 *           min, max phi
 *           array dimensions
 *                      width, # width elem, height, # height elem, radius, min and max frequency
 *                      # ap, length, fmin and fmax for each ap, for line
 *                      # staves, # elem/stave, stave hgt, # elem vertical, radius, # of degrees around, min and max frequency
 *   Number of beam patterns to write to output files
 *   Output file:
 *   for each array
 *        for each degradation
 *               average DI degradation over all samples and all frequencies
 *               standard deviation of DI degradation over all samples and all frequencies
 *               for each sample
 *                    DI degradation
 *   Method:
 *      Outer loop on arrays...
 *          dimension avg_di and di_sigma by: [number of degradations]
 *          dimension di_diff [number of degradations][number of samples]
 *          define array element locations and shading for ideal array (this may be 2-dimensional for a multi-aperture array)
 *          Loop on number of samples....
 *                for each sample...
 *                    select the following randomly:
 *                        aperture
 *                        phi
 *                        theta
 *                        frequency within the randomly-selected apertrue
 *                    compute DI for the ideal array
 *                    loop on degradation percentages.  For each...
 *                        randomly disable some fraction of the elements using another array for shading
 *                        compute DI
 *                        di_diff = ideal DI - the DI computed for this degradation (store this in di_diff array)
 *              finish loop on degradations
 *          finish loop on samples
 *          Loop on degradations...
 *              initialize for average on DI
 *              loop on samples....
 *                accumulate sum of DIs
 *              outside loop, divide by the number of samples
 *              store this average avg_di (dimensioned by number of degradations)
 *              finish loop on samples
 *              in another loop on samples, ompute sigma on di_diff array
 */
class gbeam_stats_utils
{
    private static double RAD = .017453293;
    private static double PI = 3.14159265;
    private static int N_PHI = 91;
    private static int N_THETA = 91;
    private static double D_PHI = 2.;
    private static double D_THETA = 4.;
   
    private int max_elem;
    private int max_ap;
    private int numbp_out;
    private String outputfile;
    private double sound_speed;
    private int n_deg;
    private boolean bunched;  // consecutive elements out
    private double min_deg;  // .01, for example
    private double max_deg;   // max of .99
    private int n_samples;
    private boolean nonrandom = false;
    int n_arrays;
    private int[] array_type;
    private double[] thmin;
    private double[] thmax;
    private double[] phmin;
    private double[] phmax;
    private int[] n_ap;
    private double[][] fmin;
    private double[][] fmax;
    private int[] hull_n_wid;
    private int[] hull_n_hgt;
    private double[] hull_radius;
    private double[] hull_wid;
    private double[] hull_hgt;
    private double[][] line_len;
    private int[] cyl_n_staves;
    private double[] cyl_stave_hgt;
    private int[] cyl_n_rows;
    private double[] cyl_radius;
    private double[] cyl_degrees;
    // local variables (not input)
    private PrintWriter out;
    private double[] di;
    private double[] di_diff_avg;
    private double[] di_diff_sigma;
    private double[][] di_diff;
    private double[] sample_freq;
    private double[] sample_theta;
    private double[] sample_phi;
    private boolean[] deform;  // set to true when wiggles or bends in array are allowed (line only(
    private double[] sample_wiggle;
    private double[] sample_bend;
    private double[][] wiggle_stat;
    private double[] wiggle_stat_avg;
    private double[] wiggle_stat_sigma;
    private double frac_ok;
    private int n_elem[];
    private double[][] xpos;
    private double[][] ypos;
    private double[][] zpos;
    private double[][] shad;
    private double[] shad_bad;
    private double[] wiggle_min;
    private double[] wiggle_max;
    private double[] bend_min;
    private double[] bend_max;
    private double[] xpos_wiggle;
    private double[] ypos_wiggle;
    private double[] zpos_wiggle;
    private double[][] dela;
    private double[] bpth;
    private double[] bpx;
    private double[][] bp;
    private double[] deg;
   
    private void dimension_locals() {
        this.n_elem = new int[this.max_ap];
        this.xpos = new double[this.max_ap][this.max_elem];
        this.ypos = new double[this.max_ap][this.max_elem];
        this.zpos = new double[this.max_ap][this.max_elem];
        this.shad = new double[this.max_ap][this.max_elem];
        this.shad_bad = new double[this.max_elem];
        this.xpos_wiggle = new double[this.max_elem];
        this.ypos_wiggle = new double[this.max_elem];
        this.zpos_wiggle = new double[this.max_elem];
        this.dela = new double[this.max_ap][this.max_elem];
        this.bpth = new double[N_THETA];
        this.bpx = new double[N_PHI];
        this.bp = new double[N_PHI][N_THETA];
        this.deg = new double[this.n_deg];
    }
    private void dimension_outputs() {
        this.di_diff_avg = new double[this.n_deg];
        this.di = new double[this.n_samples];
        this.di_diff_sigma = new double[this.n_deg];
        this.di_diff = new double[this.n_deg][this.n_samples];
        this.wiggle_stat = new double[this.n_deg][this.n_samples];
        this.wiggle_stat_avg = new double[this.n_deg];
        this.wiggle_stat_sigma = new double[this.n_deg];
    }
    private void dimension_arrays(){
        this.array_type = new int[this.n_arrays];
        this.deform = new boolean[this.n_arrays];
        this.thmin = new double[this.n_arrays];
        this.thmax = new double[this.n_arrays];
        this.phmin = new double[this.n_arrays];
        this.phmax = new double[this.n_arrays];
        this.hull_n_wid = new int[this.n_arrays];
        this.hull_n_hgt = new int[this.n_arrays];
        this.hull_radius = new double[this.n_arrays];
        this.hull_wid  = new double[this.n_arrays];
        this.hull_hgt  = new double[this.n_arrays];
        this.n_ap = new int[this.n_arrays];
        this.line_len = new double[this.n_arrays][this.max_ap];
        this.wiggle_min = new double[this.n_arrays];
        this.wiggle_max = new double[this.n_arrays];
        this.bend_min = new double[this.n_arrays];
        this.bend_max = new double[this.n_arrays];
        this.fmin = new double[this.n_arrays][this.max_ap];
        this.fmax = new double[this.n_arrays][this.max_ap];
        this.cyl_n_staves = new int[this.n_arrays];
        this.cyl_stave_hgt = new double[this.n_arrays];
        this.cyl_n_rows = new int[this.n_arrays];
        this.cyl_radius = new double[this.n_arrays];
        this.cyl_degrees = new double[this.n_arrays]; 
        dimension_outputs();
        dimension_locals();
    }
    public int get_inputs(String filename){
        try{
            Scanner in = new Scanner(new FileReader(filename)); 
            this.outputfile = in.nextLine();
            this.max_elem = in.nextInt();
            this.max_ap = in.nextInt();
            this.numbp_out = in.nextInt();
            this.sound_speed = in.nextDouble();
            this.n_deg = in.nextInt();  // make this negative for bunched-up elements
            this.bunched = false;
            if(this.n_deg < 0) {
                this.bunched = true;
                this.n_deg = -this.n_deg;
            }
            this.min_deg = in.nextDouble();
            this.max_deg = in.nextDouble();
            this.n_samples = in.nextInt();  // make this negative to indicate NON-randomness, and read in samples
            if(this.n_samples < 0) {
                this.n_samples = -this.n_samples;
                nonrandom = true;
                boolean combos = false;
                combos = in.nextBoolean();
                if(combos){
                    int nFreq = in.nextInt();
                    double[] lclFreq = new double[nFreq];
                    for (int ifreq = 0; ifreq < nFreq; ifreq++) {
                        lclFreq[ifreq] = in.nextDouble();
                    }
                    System.out.println(" number of freqs = " + nFreq);
                    int nTheta = in.nextInt();
                    double[] lclTheta = new double[nTheta];
                    for (int itheta = 0; itheta < nTheta; itheta++) {
                        lclTheta[itheta] = in.nextDouble();
                    }
                    int nPhi = in.nextInt();
                    double[] lclPhi = new double[nPhi];
                    for (int iphi = 0; iphi < nPhi; iphi++) {
                        lclPhi[iphi] = in.nextDouble();
                    }
                    // now form all the combos
                    int ctr = 0;
                    this.n_samples = nFreq*nTheta*nPhi;
                    this.sample_freq = new double[this.n_samples];
                    this.sample_theta = new double[this.n_samples];
                    this.sample_phi = new double[this.n_samples];
                    this.sample_wiggle = new double[this.n_samples];
                    this.sample_bend = new double[this.n_samples];
                    for (int ifreq = 0; ifreq < nFreq; ifreq++) {
                        for (int itheta= 0; itheta < nTheta; itheta++) {
                            for (int iphi = 0; iphi < nPhi; iphi++) {
                                this.sample_freq[ctr] = lclFreq[ifreq];
                                this.sample_theta[ctr] = lclTheta[itheta];
                                this.sample_phi[ctr] = lclPhi[iphi];
                                ctr++;
                            }
                        }
                    }
                }
                else {
                    this.sample_freq = new double[this.n_samples];
                    this.sample_theta = new double[this.n_samples];
                    this.sample_phi = new double[this.n_samples];
                    this.sample_wiggle = new double[this.n_samples];
                    for (int isample = 0; isample < this.n_samples; isample++) {
                        this.sample_freq[isample] = in.nextDouble();
                        this.sample_theta[isample] = in.nextDouble();
                        this.sample_phi[isample] = in.nextDouble();
                    }
                }
            }
            else {
                this.sample_freq = new double[this.n_samples];
                this.sample_theta = new double[this.n_samples];
                this.sample_phi = new double[this.n_samples];
                this.sample_wiggle = new double[this.n_samples];
                this.sample_bend = new double[this.n_samples];
            }
            this.n_arrays = in.nextInt();
            dimension_arrays();
            for (int iarray = 0; iarray < this.n_arrays; iarray++) {
                this.array_type[iarray] = in.nextInt();   
                this.thmin[iarray] = in.nextDouble();  // this and the next three parameters are ignored for nonrandom cases
                this.thmax[iarray] = in.nextDouble();
                this.phmin[iarray] = in.nextDouble();
                this.phmax[iarray] = in.nextDouble();
                if (this.array_type[iarray] == 0) {   // hull
                    this.hull_wid[iarray] = in.nextDouble();
                    this.hull_n_wid[iarray] = in.nextInt();
                    this.hull_hgt[iarray] = in.nextDouble();
                    this.hull_n_hgt[iarray] = in.nextInt();
                    this.hull_radius[iarray] = in.nextDouble();
                    this.fmin[iarray][0] = in.nextDouble();
                    this.fmax[iarray][0] = in.nextDouble();
                }
                else if(this.array_type[iarray] == 1 || this.array_type[iarray] == -1) {  // line
                    this.deform[iarray] = false;
                    if(this.array_type[iarray] == -1 )  {
                        this.deform[iarray] = true;
                        this.wiggle_min[iarray] = in.nextDouble();
                        this.wiggle_max[iarray] = in.nextDouble();
                        this.bend_min[iarray] = in.nextDouble();
                        this.bend_max[iarray] = in.nextDouble();
                        this.array_type[iarray] = 1;
                    }
                    this.n_ap[iarray] = in.nextInt();
                    for (int iap = 0; iap < this.n_ap[iarray]; iap++) {
                        this.line_len[iarray][iap] = in.nextDouble();
                        this.fmin[iarray][iap] = in.nextDouble();
                        this.fmax[iarray][iap] = in.nextDouble();
                    }
                }
                else {   // cylinder
                    this.cyl_n_staves[iarray] = in.nextInt();
                    this.cyl_stave_hgt[iarray] = in.nextDouble();
                    this.cyl_n_rows[iarray] = in.nextInt();
                    this.cyl_radius[iarray] = in.nextDouble();
                    this.cyl_degrees[iarray] = in.nextDouble();
                    this.fmin[iarray][0] = in.nextDouble();
                    this.fmax[iarray][0] = in.nextDouble();
                }
            }
            in.close();
           return (0);
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Wrapper input File not found");
            return (1);
        }
    }
    public double compute_di_for_beampattern(double[][] bp){
        // now compute comparable DI values for both arrays
        double sumOmni = 0.;
        double sumFull = 0.;
        double sum = 0.;
        double area = 0.;
        double hLast = 0.;
        double h;
        for (int iph = 0; iph < this.N_PHI-1; iph++){
            // compute area using A = 2*pi*h : https://en.wikipedia.org/wiki/Spherical_cap
            // h = 1-sin(ph)   ( = 1-cos(90-ph)   )
            h = 1. + Math.sin(this.bpx[iph+1]*RAD);
            area = 2.*PI*(h - hLast)/(double)(this.N_THETA-1);
            hLast = h;
            for (int ith=0; ith< this.N_THETA-1; ith++){
                sumOmni = sumOmni + area;
                sum = sum + area*(bp[iph][ith] + bp[iph+1][ith])/2.;
            }
        }
        double di = 10.*Math.log(sumOmni/sum)/Math.log(10.);
        return (di);
    }
    public int open_output_file() {
        int ier = 0;
        try {
            FileWriter outFile = new FileWriter(this.outputfile);
            out = new PrintWriter(outFile);
            out.format("Number of Arrays, %d %n", this.n_arrays);
        } 
        catch (IOException e){
             e.printStackTrace();
             ier = 1;
        }
        return (ier);
    }
    public int close_output_file() {
        out.close();
        return (0);
    }
    public void build_ideal_array(int iarray) {
        if (this.array_type[iarray] == 0) {
            build_ideal_hull_array(iarray);
        }
        else if(this.array_type[iarray] == 1) {
            build_ideal_line_array(iarray);
        }
        else {
            build_ideal_cyl_array(iarray);
        }
    }
    private void build_ideal_hull_array(int iarray) {
        this.n_ap[iarray] = 1;
    }
    private void build_ideal_line_array(int iarray) {
        for (int iap = 0; iap < this.n_ap[iarray]; iap++) {
            double wavelength = this.sound_speed / this.fmax[iarray][iap];
            double spacing = .9*wavelength/2.;
            this.n_elem[iap] = (int)(this.line_len[iarray][iap]/spacing + 1.);
            int edge = this.n_elem[iap]/8;
            if (this.n_elem[iap] > this.max_elem) this.n_elem[iap] = this.max_elem;
            for (int ielem = 0; ielem < this.n_elem[iap]; ielem++) {
                xpos[iap][ielem] = 0.;
                ypos[iap][ielem] = 0.;
                zpos[iap][ielem] = spacing*ielem - this.line_len[iarray][iap]/2.;
                shad[iap][ielem] = 1.;
                dela[iap][ielem] = 0.;
                if (ielem < edge){
                    double angle = 90.*(1. - (double)ielem/(double)edge);
                    shad[iap][ielem] = Math.cos(angle*RAD)*Math.cos(angle*RAD);
                }
                if (ielem > this.n_elem[iap]-edge){
                    double angle = 90.*(double)(ielem-edge)/(double)edge;
                    shad[iap][ielem] = Math.cos(angle*RAD)*Math.cos(angle*RAD);
                }
            }
        }
    }
    private void build_ideal_cyl_array(int iarray) {
        this.n_ap[iarray] = 1;
        this.n_elem[0] = this.cyl_n_staves[iarray]*this.cyl_n_rows[iarray];
        int ielem = 0;
        double y_spacing = this.cyl_stave_hgt[iarray]/(double)(this.cyl_n_rows[iarray]-1);
        double d_angle = this.cyl_degrees[iarray]/(double)(this.cyl_n_staves[iarray]-1);
        for (int istave = 0; istave < this.cyl_n_staves[iarray]; istave++) {
            double angle = 180. - this.cyl_degrees[iarray]/2. + istave*d_angle;
            double ytmp = this.cyl_radius[iarray]*Math.sin(angle*RAD);
            double xtmp = this.cyl_radius[iarray]*Math.cos(angle*RAD);
            for (int irow = 0; irow < this.cyl_n_rows[iarray]; irow++) {
                zpos[0][ielem] = y_spacing*irow - this.cyl_stave_hgt[iarray]/2.;
                xpos[0][ielem] = xtmp;
                ypos[0][ielem] = ytmp;
                shad[0][ielem] = 1.;
                ielem++;
            }
        }
    }
    private double bounded_random(double x, double y, int iRound) {
        double outcome = x+(y-x)*Math.random();
        if(iRound > 0) outcome = (double)(iRound*(int)(outcome/(double)iRound));
        return(outcome);
    }
    private void wiggle_elements(int iarray, int iap, int isample) {
        if(nonrandom) {
            this.sample_wiggle[isample] = .5*wiggle_min[iarray] + .5*wiggle_max[iarray];
            this.sample_bend[isample] = .5*bend_min[iarray] + .5*bend_max[iarray];
        }
        else {
            double r = Math.random();
            this.sample_wiggle[isample] = r*this.wiggle_min[iarray] + (1.-r)*this.wiggle_max[iarray];
            r = Math.random();
            this.sample_bend[isample] = r*this.bend_min[iarray] + (1.-r)*this.bend_max[iarray];
        }
        for (int ielem = 0; ielem < this.n_elem[iap];ielem++) {
            if (ielem == 0){
                xpos_wiggle[ielem] = xpos[iap][ielem];
                ypos_wiggle[ielem] = ypos[iap][ielem];
                zpos_wiggle[ielem] = zpos[iap][ielem];
            }
            else {
                // wiggle with respect to previous element
                double angle = Math.random()*PI*2.;
                double wiggle = Math.random()*this.sample_wiggle[isample];
                xpos_wiggle[ielem] = xpos_wiggle[ielem-1] + Math.cos(angle)*wiggle;
                ypos_wiggle[ielem] = xpos_wiggle[ielem-1] + Math.sin(angle)*wiggle;
                double dz = zpos[iap][ielem] - zpos[iap][ielem-1];
                double arg = dz*dz - wiggle*wiggle;
                if(arg <= 0.)arg = 0.;
                zpos_wiggle[ielem] = zpos_wiggle[ielem-1] + Math.sqrt(arg);
            }
        }
        if(this.sample_bend[isample] > 0.) {
            double angle = this.sample_bend[isample]/360.*PI*2.;
            int ibase = this.n_elem[iap]/2;
            for (int ielem = ibase+1; ielem < this.n_elem[iap];ielem++) {
                double zfactor = Math.cos(angle);
                double xfactor = Math.sin(angle);
                double dz = zpos_wiggle[ielem] - zpos_wiggle[ibase];
                xpos_wiggle[ielem] = xpos_wiggle[ielem] + xfactor*dz;
                zpos_wiggle[ielem] = zpos_wiggle[ibase] + dz*zfactor;
            }
        }
    }
    public void compute_array_stats(int iarray) {
        Random diceRoller = new Random();
        // loop on samples
        for (int isample = 0; isample < this.n_samples; isample++) {
            System.out.format(" Sample %d of %d%n",isample+1, n_samples);
            // for this sample, randomly select aperture, theta, phi, and frequency
            int iap = 0;
            int nbad;
            if(this.n_ap[iarray] > 1) iap = diceRoller.nextInt(this.n_ap[iarray]);
            if(!nonrandom){
                this.sample_freq[isample] = bounded_random(this.fmin[iarray][iap], this.fmax[iarray][iap],1);
                this.sample_theta[isample] = bounded_random(this.thmin[iarray], this.thmax[iarray], (int)(D_THETA+.1));
                this.sample_phi[isample] = bounded_random(this.phmin[iarray], this.phmax[iarray], (int)(D_PHI+.1));
            }
            di[isample] = compute_di_for_array(this.sound_speed/this.sample_freq[isample], this.sample_theta[isample], 
                                   this.sample_phi[isample], this.n_elem[iap], 
                                   get_dim1(xpos, iap, this.n_elem[iap]), get_dim1(ypos, iap, this.n_elem[iap]),
                                   get_dim1(zpos, iap, this.n_elem[iap]), get_dim1(dela, iap, this.n_elem[iap]),
                                   get_dim1(shad, iap, this.n_elem[iap]),false, 0.);
            // save the beam pattern level at the MRA
            int mr_phi = closest_element(this.sample_phi[isample], bpx, N_PHI);
            double lvl_compare = bp[mr_phi][0]; // theta should not matter with the line array

            if (isample < this.numbp_out) write_bp_outputs(iarray,isample, iap, -1, this.sample_freq[isample],
                                        this.sample_theta[isample], this.sample_phi[isample], false);
            // loop on degradations
            for (int ideg = 0; ideg < this.n_deg; ideg++) {
                nbad = 0;
                for (int ielem = 0; ielem < this.n_elem[iap]; ielem++) {
                    this.shad_bad[ielem] = this.shad[iap][ielem];
                }
                nbad = (int)(this.deg[ideg]/100. * (double)this.n_elem[iap]);
                disable_elements(nbad, this.n_elem[iap]);
                double di_deg;
                if(this.deform[iarray]) {
                    if(isample==0 || !nonrandom) {
                        wiggle_elements(iarray, iap, isample);
                    }
                    else  {
                        this.sample_wiggle[isample] = this.sample_wiggle[0];
                        this.sample_bend[isample] = this.sample_bend[0];
                    }
                }
                di_deg = compute_di_for_array(this.sound_speed/this.sample_freq[isample],this.sample_theta[isample], 
                                       this.sample_phi[isample], this.n_elem[iap], 
                                       get_dim1(xpos, iap, this.n_elem[iap]), get_dim1(ypos, iap, this.n_elem[iap]),
                                       get_dim1(zpos, iap, this.n_elem[iap]), get_dim1(dela, iap, this.n_elem[iap]),
                                       shad_bad, this.deform[iarray], lvl_compare);
                this.di_diff[ideg][isample] = di[isample] - di_deg;
                this.wiggle_stat[ideg][isample] = frac_ok;
                if (isample < this.numbp_out) write_bp_outputs(iarray, isample, iap, ideg,this.sample_freq[isample],
                                        this.sample_theta[isample], this.sample_phi[isample], this.deform[iarray]);
            }
        }
        // average
        for (int ideg = 0; ideg < this.n_deg; ideg++) {
            di_diff_avg[ideg] = 0.;
            wiggle_stat_avg[ideg] = 0.;
            for (int isample = 0; isample < this.n_samples; isample++) {
                di_diff_avg[ideg] = di_diff_avg[ideg] + di_diff[ideg][isample]/(double)this.n_samples;
                wiggle_stat_avg[ideg] = wiggle_stat_avg[ideg] + wiggle_stat[ideg][isample]/(double)this.n_samples;
            }
            // standard deviation
            di_diff_sigma[ideg] = 0.;
            wiggle_stat_sigma[ideg] = 0.;
            for (int isample = 0; isample < this.n_samples; isample++) {
                di_diff_sigma[ideg] = di_diff_sigma[ideg] + (di_diff_avg[ideg] - di_diff[ideg][isample])*(di_diff_avg[ideg] - di_diff[ideg][isample]);
                wiggle_stat_sigma[ideg] = wiggle_stat_sigma[ideg] + (wiggle_stat_avg[ideg] - wiggle_stat[ideg][isample])*(wiggle_stat_avg[ideg] - wiggle_stat[ideg][isample]);
            }
            di_diff_sigma[ideg] = Math.sqrt(di_diff_sigma[ideg]/this.n_samples);
            wiggle_stat_sigma[ideg] = Math.sqrt(wiggle_stat_sigma[ideg]/this.n_samples);
        }
    }
    private void disable_elements(int nbad, int n) {
        if (nbad > 0){
            Random diceRoller = new Random();
            if(this.bunched) {
                int nleft = n - nbad;
                int roll = diceRoller.nextInt(nleft);
                // zero all elements starting with roll
                for (int ctr = 0; ctr < nbad; ctr++) {
                    shad_bad[ctr+roll] = 0.;
                }
            }
            else {
                int nLeft = n;
                for (int ctr = 0; ctr < nbad; ctr++){
                    int roll = diceRoller.nextInt(nLeft);
                    nLeft = nLeft - 1;
                    // delete the roll-th element of the non-deleted elements
                    int ctr2 = 0;
                    int ctr3 = 0;
                    for (ctr2 = 0; ctr2 < roll+1; ctr2++){
                        if (shad_bad[ctr3] == 0.)ctr3++; 
                        ctr3++;
                    }
                    shad_bad[ctr3-1] = 0.;
                }
            }
        }
    }
    public int print_array_outputs(int iarray) {
        out.format("Array:, %d%n", iarray);
        out.format("#_of_degradations, %d%n", this.n_deg);
        out.format("Degradation(pct), Avg_DI_Degradation(dB), +Sigma, +2-Sigma, Sigma_DI_Degradation (dB) %n");
        for (int ideg = 0; ideg < this.n_deg; ideg++) {
            out.format("%10.3f, %10.3f, %10.3f, %10.3f, %10.3f %n", this.deg[ideg], this.di_diff_avg[ideg], 
                       this.di_diff_avg[ideg]+this.di_diff_sigma[ideg],this.di_diff_avg[ideg]+2.*this.di_diff_sigma[ideg], 
                       this.di_diff_sigma[ideg]);
        }
        if(this.deform[iarray]) {
            out.format(" Array deformation Statistics:%n");
            out.format(" Degradation(pct), AvgCovPenalty(pct), Sigma%n");
            for (int ideg = 0; ideg < this.n_deg; ideg++) {
                out.format("%10.3f, %d, %d%n", this.deg[ideg], (int)(100.*(1.-this.wiggle_stat_avg[ideg])), (int)(100.*this.wiggle_stat_sigma[ideg]));
            }
        }
        // now detailed per-sample data
        out.format("Detailed Sample Data:%nfreq,theta,phi,wiggle,di,bw,data....%n");
        for (int isample = 0; isample < this.n_samples; isample++) {
            out.format("%d, %d, %d, %10.3f, %10.3f, %10.3f",(int)this.sample_freq[isample],
                       (int)this.sample_theta[isample], 
                       (int)this.sample_phi[isample], this.sample_wiggle[isample], 
                       this.di[isample], compute_bw(iarray,this.di[isample]));
            for (int ideg = 0; ideg < this.n_deg; ideg++) {
                out.format(", %8.3f, %8.1f",this.di_diff[ideg][isample], compute_bw(iarray,this.di[isample]-this.di_diff[ideg][isample]));
            }
            out.format("%n");
        }
        // deformation statistics
        if(this.deform[iarray]){
            out.format(" Detailed Deformation Sample Data:%n freq phi wiggle bend  penalties...%n");
            for (int isample = 0; isample < this.n_samples; isample++) {
                out.format("%d, %d, %10.3f, %10.3f",(int)this.sample_freq[isample],  
                           (int)this.sample_phi[isample], this.sample_wiggle[isample],
                           this.sample_bend[isample]);
                for (int ideg = 0; ideg < this.n_deg; ideg++) {
                    out.format(", %10.3f",(100.*(1. - this.wiggle_stat[ideg][isample])));
                }
                out.format("%n");
            }
        }
        return (0);
        
    }
    private double compute_bw(int iarray,double di)  { 
        if (di > -998.){
            if (this.array_type[iarray] == 1) {
                return (180./Math.pow(10., di/10.));  
            }
            else {
                double one_dim = 180./Math.pow(10., di/10.);
                return (Math.sqrt(360.*one_dim));
            }
        }
        else {
            return (di);    // return the same "flagged" value
        }
    }
    private double[] get_dim1(double[][] a, int iap, int n) {
        double[] b = new double[n];
        for (int i = 0; i < this.n_elem[iap]; i++) {
            b[i] = a[iap][i];
        }
        return (b);
    }
    private double compute_di_for_array(double wlamda, double theta, double phi, int n, 
                         double[] x, double[] y, double[] z, double[] dela, double[] shad, 
                         boolean deformed, double lvl_compare) {
        Gbeam g = new Gbeam(n, 0, 1, x, y, z,dela, shad);
        double lvl = g.get_gbsum(theta, phi,0., wlamda);
        // lvl is important.  It is the level at the max response axis with UNWIGGLED elements
        if (deformed) g.gbeam_updatePos(xpos_wiggle, ypos_wiggle, zpos_wiggle);
        double ymax = 0.;
        for (int iph = 0; iph < N_PHI; iph++) {
            for (int ith = 0; ith < N_THETA; ith++){
                bp[iph][ith] = g.get_gbsum(bpth[ith], bpx[iph],0., wlamda);
                if (ymax < bp[iph][ith]) ymax = bp[iph][ith];
            }
        }
        // normalize
        lvl = lvl / ymax;
        for (int iph = 0; iph < N_PHI; iph++) {
            for (int ith = 0; ith < N_THETA; ith++){
                bp[iph][ith] = bp[iph][ith]/ ymax;
            }
        }
        double di = compute_di_for_beampattern(bp);
        
        if (lvl_compare > 0.) {
            // for this single value of phi, and all values of theta
            int mr_phi = closest_element(phi, bpx, N_PHI);
            frac_ok = 0.;
            for (int ith = 0; ith < N_THETA; ith++) {
                if(bp[mr_phi][ith] >= lvl_compare*.9) {
                    frac_ok = frac_ok + 1./N_THETA;
                }
                else  {
                    //frac_ok = frac_ok + bp[mr_phi][ith]/lvl_compare/(double)N_THETA;
                }
                //if(bp[mr_phi][ith] > lvl_compare*.9) frac_ok = frac_ok + 1./N_THETA;
            }
        }
        return (di);
    }
    private int closest_element(double x, double[] xa, int n) {
        int iclosest = 0;
        double diff = Math.abs(x-xa[0]);
        for (int i = 1; i < n; i++) {
            if(Math.abs(x-xa[i]) < diff) {
                diff = Math.abs(x-xa[i]);
                iclosest = i;
            }
        }
        return(iclosest);
    }
    public void get_bp_angles() {
        for (int i = 0; i < N_THETA; i++) bpth[i] = (double)i*D_THETA;
        for (int i = 0; i < N_PHI; i++)bpx[i] = (double)(i*D_PHI-90);
    }
    public void get_degradation_array() {
        double ddeg = 0.;
        if(this.n_deg > 1) ddeg = (this.max_deg - this.min_deg)/(double)(this.n_deg - 1);
        for (int i = 0; i < this.n_deg; i++) {
            this.deg[i] = this.min_deg + i*ddeg;
        }
    }
    public int write_bp_outputs(int iarray, int isample, int iap, int ideg, double freq, double theta, double phi, boolean deform)    {
        int ier = 0;
        int icolor = 0;
        try {
             String filename = "bp" + isample;
             if(ideg >= 0)filename = filename + ".d" + ideg;
             FileWriter outFile = new FileWriter(filename);
             
             PrintWriter out = new PrintWriter(outFile);
             out.format("Freq: %d, Theta: %d, Phi: %d%n",(int)freq, (int)theta, (int)phi);
             out.format("%d %d %8.0f %8.0f %8.0f %d%n",-(this.N_THETA-1)*(this.N_PHI-1), 
                        this.n_elem[iap], this.sample_theta[isample], this.sample_phi[isample], 
                        this.sample_freq[isample], this.array_type[iarray]);
             for (int iph = 0; iph < this.N_PHI - 1; iph++){
                 for (int ith = 0; ith < this.N_THETA-1; ith++){
                     double dB1 = 10*Math.log(Math.max(this.bp[iph][ith], 1.e-30))/Math.log(10.);
                     double dB2 = 10*Math.log(Math.max(this.bp[iph+1][ith], 1.e-30))/Math.log(10.);
                     double dB3 = 10*Math.log(Math.max(this.bp[iph][ith+1], 1.e-30))/Math.log(10.);
                     double dB4 = 10*Math.log(Math.max(this.bp[iph+1][ith+1], 1.e-30))/Math.log(10.);
                     out.format("%6d  %6d  %6d  %6d  %10.3f %10.3f %10.3f %10.3f %n",
                               (int)bpx[iph], (int)bpx[iph+1], (int)bpth[ith], (int)bpth[ith+1], dB1, dB2, dB3, dB4);
                 }
             }
             for (int ielem = 0; ielem < this.n_elem[iap]; ielem++) {
                 icolor = 0;
                 if(this.shad_bad[ielem] < .999*this.shad[iap][ielem] && ideg >= 0) icolor = 2;
                 if(ideg >= 0 && deform) {
                     out.format("%10.3f %10.3f %10.3f %d %d %n",this.xpos_wiggle[ielem], this.ypos_wiggle[ielem], this.zpos_wiggle[ielem], icolor, 0);
                 }
                 else  {
                     out.format("%10.3f %10.3f %10.3f %d %d %n",this.xpos[iap][ielem], this.ypos[iap][ielem], this.zpos[iap][ielem], icolor, 0);
                 }
             }
             out.close();
         } catch (IOException e){
             e.printStackTrace();
        }
        return (ier);
    }
}
public class Gbeam_stats
{
    // instance variables - replace the example below with your own
    private static double RAD = .017453293;
    private static double PI = 3.14159265;
    public static void main()
    {
                // read inputs from file
        Scanner sc = new Scanner(System.in);
        System.out.println(" Enter input filename ");
        String Filename = sc.nextLine();

        gbeam_stats_utils gb = new gbeam_stats_utils();
        int ier = gb.get_inputs(Filename);
        gb.get_bp_angles();
        gb.get_degradation_array();
        ier = gb.open_output_file();
        for (int iarray = 0; iarray < gb.n_arrays; iarray++) {
            gb.build_ideal_array(iarray);
            gb.compute_array_stats(iarray);
            ier = gb.print_array_outputs(iarray);
        }
        ier = gb.close_output_file();
        System.out.println(" done");
    }
    public static void process_tda()
    {
        Scanner sc = new Scanner(System.in);
        System.out.println(" Enter a filename ");
        String Filename = sc.nextLine();
        tda_utils t = new tda_utils();
        t.read_file(Filename);
        t.process_tda_stats();
        t.write_outputs(Filename + ".csv");
    }
    public static void compute_skew()
    {
        Scanner sc = new Scanner(System.in);
        System.out.println(" Enter a filename ");
        String Filename = sc.nextLine();
        tda_utils t = new tda_utils();
        t.compute_skewness(Filename);
    }
}