import java.util.Scanner;;
import java.io.*;
import java.lang.Object;
import java.util.Arrays;

/**
 * Write a description of class tda_utils here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class tda_utils
{
    // instance variables - replace the example below with your own
    private int debug_fl = 0;
    private long total_samples;
    private double binwidth;   // length of histogram bin in nm
    private int nbins;
    private int x;
    private int nsamples;
    private String prefix;
    private int nf;  // number of files
    private String[] f = new String[20];
    private int ne; // number of environments
    private String[] edir = new String[20];
    private int nz;
    private String[] zdir = new String[20];
    private int ns;
    private int nbinmax = 200;
    private String[][] sdir = new String[100][3];
    private double[][] ranges = new double[nbinmax][3];
    private double[][] binned = new double[nbinmax][3];
    private double[][] binned_min = new double[nbinmax][3];
    private double[][] binned_85 = new double[nbinmax][3];
    private int[] n_per_bin = new int[nbinmax];
    private int[] n_per_bin_rms = new int[nbinmax];
    private double[][] bin_rms = new double[nbinmax][3];
    private double[][][] binned_ranges = new double[nbinmax][3][5000];
    private int nbad;
    private int nbad_in;
    private int ngood;
    private int ngood_in;
    private int n_skew;
    private double[] x_skew = new double[5000];

    /**
     * Constructor for objects of class tda_utils
     */
    public tda_utils()
    {
        // initialise instance variables
        x = 0;
    }
    private void init_stats() {
        this.nsamples = 0;
    }
    private int get_ranges(String filename, int ideg, int isample_in) {
        String lineTxt = "";
        int isample = isample_in;
        try {
            Scanner in = new Scanner(new FileReader(filename));
            boolean match = false;
            while (!lineTxt.startsWith("begin data with files")) {
                lineTxt = in.nextLine();
            }
            lineTxt = in.nextLine();
            while (!lineTxt.startsWith("end data")) {
                String delims = "[ ]+";
                String[] tokens = lineTxt.split(delims);
                this.ranges[isample][ideg] = Double.valueOf(tokens[4]);
                isample++;
                lineTxt = in.nextLine();
            }
            in.close();
            return (isample);
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Input File not found: " + filename);
            return (0);
        }
    }
    private void accumulate_ranges_in_bins() {
        int ibin = 0;
        for (int ctr = 0; ctr < this.nsamples; ctr++) {
            // first, see if this is a bad one and don't contribute if it is
            if ((this.ranges[ctr][0] >= this.ranges[ctr][1]) && (this.ranges[ctr][1] >= this.ranges[ctr][2])) {
                ibin = (int)(this.ranges[ctr][0] / this.binwidth);
                for (int ideg = 0; ideg < 3; ideg++) {
                    this.binned[ibin][ideg] += this.ranges[ctr][ideg];
                    this.binned_ranges[ibin][ideg][this.n_per_bin[ibin]] = this.ranges[ctr][ideg];
                }
                this.n_per_bin[ibin]++;
                if (this.n_per_bin[ibin] == 1) {
                    for (int ideg = 0; ideg < 3; ideg++)this.binned_min[ibin][ideg] = this.ranges[ctr][ideg];
                }
                else   {
                    for (int ideg = 0; ideg < 3; ideg++)this.binned_min[ibin][ideg] = Math.min(this.binned_min[ibin][ideg], this.ranges[ctr][ideg]);
                }
                this.ngood_in++;
            }
            else {
                this.nbad_in++;
            }
        }
        this.nsamples = 0;
    }
    private void accumulate_stats_in_bins(int is) {
        int ibin = 0;
        double diff;
        for (int ctr = 0; ctr < this.nsamples; ctr++) {
            if ((this.ranges[ctr][0] >= this.ranges[ctr][1]) && (this.ranges[ctr][1] >= this.ranges[ctr][2])) {
                this.ngood++;
                ibin = (int)(this.ranges[ctr][0] / this.binwidth);
                for (int ideg = 1; ideg < 3; ideg++) {
                    diff = this.ranges[ctr][ideg] - this.binned[ibin][ideg];
                    if (diff < 0.) diff = -diff;
                    if(ibin > 0 && diff > this.binned[ibin][ideg]) {
                        System.out.format(" oops! ideg = %d, bin = %d, range= %10.3f, avg rng = %10.3f %n", ideg, ibin, this.ranges[ctr][ideg], this.binned[ibin][ideg]);
                        System.out.format("  binned undegraded avg range = %10.3f, unbinned undeg rng = %10.3f %n", this.binned[ibin][0], this.ranges[ctr][0]);
                        System.out.format("  ideg = %d, ctr %d%n",ideg,ctr);
                    }
                    this.bin_rms[ibin][ideg] = this.bin_rms[ibin][ideg] + diff*diff;
                }
            }
            else {
                this.nbad++;
            }
        }
        this.nsamples = 0;
    }
    private void normalize_rms() {
        for (int ctr = 0; ctr < this.nbins; ctr++) {
            if(this.n_per_bin[ctr] > 0) {
                for (int ideg = 0; ideg < 3; ideg++) {
                    this.bin_rms[ctr][ideg] = this.bin_rms[ctr][ideg] / this.n_per_bin[ctr];
                    this.bin_rms[ctr][ideg] = Math.sqrt(this.bin_rms[ctr][ideg]);
                }
            }
        }
    }
    public void process_tda_stats() {
        init_stats();
        int rctr = 0;
        this.total_samples = 0;
        double[] rtmp = new double[100];
        for (int iif = 0; iif < this.nf; iif++) {
            for (int ie = 0; ie < this.ne; ie++) {
                for (int iz = 0; iz < this.nz; iz++) {
                    for (int is = 0; is < this.ns; is++) {
                        // construct filename
                        String filename = this.prefix + this.edir[ie] + this.zdir[iz] + this.sdir[is][0] + this.f[iif];
                        String filename1 = this.prefix + this.edir[ie] + this.zdir[iz] + this.sdir[is][1] + this.f[iif];
                        String filename2 = this.prefix + this.edir[ie] + this.zdir[iz] + this.sdir[is][2] + this.f[iif];
                        // now read from each file the 
                        if (new File(filename).isFile()) {
                            int isample0 = get_ranges(filename,0, this.nsamples);
                            int isample1 = get_ranges(filename1, 1, this.nsamples);
                            this.nsamples = get_ranges(filename2, 2, this.nsamples);
                            if (isample0 > 0 && isample1 > 0 && this.nsamples > 0) {
                                this.total_samples += this.nsamples;
                                // now, IF we want to bin all the statistics, process the current set of ranges and
                                // then reset this.nsamples
                                if (this.nbins > 0) {
                                    accumulate_ranges_in_bins();
                                }
                            }
                        }
                    }
                }
            }
        }
        if (this.nbins > 0) {
            normalize_binned_ranges();
            // now get sigma for each point and each degradation.  Need to have the averages first since
            // arrays could get huge otherwise
            for (int iif = 0; iif < this.nf; iif++) {
                for (int ie = 0; ie < this.ne; ie++) {
                    for (int iz = 0; iz < this.nz; iz++) {
                        for (int is = 0; is < this.ns; is++) {
                            // construct filenames
                            String filename = this.prefix + this.edir[ie] + this.zdir[iz] + this.sdir[is][0] + this.f[iif];
                            String filename1 = this.prefix + this.edir[ie] + this.zdir[iz] + this.sdir[is][1] + this.f[iif];
                            String filename2 = this.prefix + this.edir[ie] + this.zdir[iz] + this.sdir[is][2] + this.f[iif];
                            // now read from each file the 
                            if (new File(filename).isFile()) {
                                int isample0 = get_ranges(filename,0, this.nsamples);
                                int isample1 = get_ranges(filename1, 1, this.nsamples);
                                this.nsamples = get_ranges(filename2, 2, this.nsamples);
                                // now, IF we want to bin all the statistics, process the current set of ranges and
                                // then reset this.nsamples
                                accumulate_stats_in_bins(is);
                            }
                        }
                    }
                }
            }
            normalize_rms();
            System.out.format(" ngood = %d %d, nbad = %d %d%n", ngood, ngood_in, nbad, nbad_in);
            System.out.format(" number of samples = %d", this.total_samples);
        }
    }
    public void write_outputs(String filename) {
        
        if (this.nbins > 0) {
            write_binned_outputs(filename);
        }
        else {
            write_raw_outputs(filename);
        }
    }
    private void normalize_binned_ranges() {
        for (int ctr = 0; ctr < this.nbins; ctr++) {
            if (this.n_per_bin[ctr] > 0) {
                for (int ideg = 0; ideg < 3; ideg++) this.binned[ctr][ideg] = this.binned[ctr][ideg] / this.n_per_bin[ctr];
            }
        }
    }
    private void read_skew_data(String filename) {
        try {
            Scanner in = new Scanner(new FileReader(filename));
            this.n_skew = in.nextInt();
            for (int ctr = 0; ctr < this.n_skew; ctr++) {
                this.x_skew[ctr] = in.nextDouble();
            }
            in.close();
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Input File not found");
        }
    }
    public void compute_skewness(String Filename) {
        read_skew_data(Filename);
        double s = skewness(this.n_skew, this.x_skew);
        System.out.format(" skewness = %10.3f %n", s);
    }
    private double average(int n, double[] x) {
        double sum = 0.;
        for (int ctr = 0; ctr < n; ctr++) {
            sum += x[ctr];
        }
        sum = sum / n;
        return (sum);
    }
    public double kurtosis(int n, double[] x) {
        double xavg = average(n,x);
        if (n < 4)return(-999.);
        double k;
        double sum4 = 0.;
        double sum2 = 0.;
        double diff;
        double diff2;
        for (int ctr = 0; ctr < n; ctr++) {
            diff = x[ctr] - xavg;
            diff2 = diff*diff;
            sum4 = sum4 + diff2*diff2;
            sum2 = sum2 + diff2;
        }
        sum2 = sum2*sum2;
        k = n*(n+1)*(n-1)/(n-2)/(n-3)*sum4/sum2;
        return (k);
    }
    public double skewness(int n, double[] x) {
        double xavg = average(n, x);
        if (n < 4) return(-999.);
        double s;
        double sum3 = 0.;
        double sum2 = 0.;
        for (int ctr = 0; ctr < n; ctr++) {
            sum3 = sum3 + (x[ctr]-xavg)*(x[ctr]-xavg)*(x[ctr]-xavg);
            sum2 = sum2 + (x[ctr] - xavg)*(x[ctr] - xavg);
        }
        sum2 = Math.pow(sum2, 1.5);
        s = n*Math.sqrt(n-1)/(n-2)*sum3/sum2;
        return (s);
    }
    public double get_percentile(int n, double[] x, double frac, boolean bottom_only) {
        // compute, instead of the median, the frac-percentile.
        // so if you have an array of numbers, take the top "frac" of the numbers and return the smallest
        // just sort and pick
        if(this.debug_fl < 5) {
            System.out.format("# pts, %d, x1, %9.2f, x2, %9.2f%n",n,x[1], x[2]);
        }
        double[] x_sorted = new double[n];
        for (int i = 0; i < n; i++) {
            x_sorted[i] = x[i];
        }
        Arrays.sort(x_sorted);
        int index = (int)((1.-frac)*(double)(n));
        if(this.debug_fl < 5) {
            for (int i = 0; i < 40; i++) System.out.format("%7.1f ",x_sorted[i]);
            System.out.format("%n");
            System.out.format("m = %d, index = %d%n",n,index);
        }
        this.debug_fl++;
        return (x_sorted[index]);
    }
    public void write_binned_outputs(String filename) {
        double rbin;
        double[] x1 = new double[9000];
        double[] x2 = new double[9000];
        try {
            FileWriter outFile = new FileWriter(filename);
            PrintWriter out = new PrintWriter(outFile);
            out.format("Range in Nm, Bin size, Range in Nm,Ideal,10%% avg, 40%% avg, Range in Nm, Ideal, 10%% 85, 40%% 85,Range in Nm,Ideal, 10%% min, 40%% min,Range in Nm, Ideal,10%%-sigma,40%%-sigma %n");
            for (int ctr = 0; ctr < this.nbins; ctr++) {
                if (this.n_per_bin[ctr] > 0) {
                    rbin = ((double)(ctr) + .5)*this.binwidth;
                    // get skew and kurtosis.  I see the problem
                    for (int i = 0; i < this.n_per_bin[ctr]; i++) {
                        x1[i] = this.binned_ranges[ctr][1][i];
                        x2[i] = this.binned_ranges[ctr][2][i];
                    }
                    double s1 = skewness(this.n_per_bin[ctr], x1);
                    double k1 = kurtosis(this.n_per_bin[ctr], x1);
                    double s2 = skewness(this.n_per_bin[ctr], x2);
                    double k2 = kurtosis(this.n_per_bin[ctr], x2);
                    double b85_1 = get_percentile(this.n_per_bin[ctr], x1, .85, true);
                    double b85_2 = get_percentile(this.n_per_bin[ctr], x2, .85, true);
                    out.format("%9.2f,%d,%9.2f,%9.2f,%9.2f,%9.2f,%9.2f,%9.2f,%9.2f,%9.2f,%9.2f,%9.2f,%9.2f,%9.2f,%9.2f,%9.2f,%9.2f,%9.2f,%9.2f,%9.2f,%9.2f,%9.2f%n", 
                                 rbin,this.n_per_bin[ctr],
                                 rbin,this.binned[ctr][0], this.binned[ctr][1], this.binned[ctr][2],
                                 rbin, rbin, b85_1, b85_2,
                                 rbin,this.binned_min[ctr][0], this.binned_min[ctr][1], this.binned_min[ctr][2],
                                 rbin,this.binned[ctr][0], this.binned[ctr][1] - this.bin_rms[ctr][1],
                                 this.binned[ctr][2] - this.bin_rms[ctr][2], s1, k1, s2, k2);
                }
            }
            out.format("Detailed range information");
            for (int ctr = 0; ctr < this.nbins; ctr++) {
                out.format(" Range bin = %10.3f %n", ((double)(ctr) + .5)*this.binwidth);
                for (int i = 0; i < this.n_per_bin[ctr]; i++) {
                    out.format("%10.3f, %10.3f, %10.3f %n",this.binned_ranges[ctr][0][i], 
                                 this.binned_ranges[ctr][1][i], this.binned_ranges[ctr][2][i]);
                }
            }
            out.close();
        }
        catch (IOException e){
            e.printStackTrace();
            System.out.println("Problem with output file");
        }
    }
    public void write_raw_outputs(String filename) {
        try {
            FileWriter outFile = new FileWriter(filename);
            PrintWriter out = new PrintWriter(outFile);
            out.format("Number of Points, %d %n", this.nsamples);
            for (int ctr = 0; ctr < this.nsamples; ctr++) {
                out.format("%10.3f, %10.3f, %10.3f %n",this.ranges[ctr][0],this.ranges[ctr][1],this.ranges[ctr][2]);
            }
            out.close();
        }
        catch (IOException e){
            e.printStackTrace();
            System.out.println("Problem with output file");
        }
        
    }
    public void read_file(String filename)    {
        try {
            Scanner in = new Scanner(new FileReader(filename));
            this.nbins = in.nextInt();
            this.binwidth = in.nextDouble();
            String dummy = in.nextLine();
            this.prefix = in.nextLine();
            this.nf = in.nextInt();
            dummy = in.nextLine();
            for (int ctr = 0; ctr < this.nf; ctr++) {
                this.f[ctr] = in.nextLine();
            }
            this.ne = in.nextInt();
            dummy = in.nextLine();
            for (int ctr = 0; ctr < this.ne; ctr++) {
                this.edir[ctr] = in.nextLine();
            }
            this.nz = in.nextInt();
            dummy = in.nextLine();
            for (int ctr = 0; ctr < this.nz; ctr++) {
                this.zdir[ctr] = in.nextLine();
            }
            this.ns = in.nextInt();
            dummy = in.nextLine();
            for (int ctr = 0; ctr < this.ns; ctr++) {
                this.sdir[ctr][0] = in.nextLine();
                this.sdir[ctr][1] = in.nextLine();
                this.sdir[ctr][2] = in.nextLine();
            }
            System.out.println(this.sdir[7][2]);
            in.close();
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Input File not found");
        }

    }

    /**
     * An example of a method - replace this comment with your own
     * 
     * @param  y   a sample parameter for a method
     * @return     the sum of x and y 
     */
    public int sampleMethod(int y)
    {
        // put your code here
        return x + y;
    }
}
