/**
 * This class represents a "point" on a 3-d grid with an extra parameters for amplitudes or other uses.
 */
public class XYZAPoint {
    
    double x;
    double y;
    double z;
    int ap;
    int out;
    double wgt;
    double dela;
    
    /**
     * Default Constructor.
     * @param x The X value
     * @param y The Y value
     */
    public void XYZAPoint() {
        this.x = 0.;
        this.y = 0.;
        this.z = 0.;
        this.ap = 0;
        this.out = 0;
        this.wgt = 0.;
        this.dela = 0.;
    }
    public void XYZAPoint(double x, double y, double z, int a, int out) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.ap = ap;
        this.out = out;
    }
    
    /**
     * Get the x value of the point.
     * @return double
     */
    public final double getX() {
        return x;
    }
    
    /**
     * Get the y value of the point.
     * @return double
     */
    public final double getY() {
        return y;
    }
}
