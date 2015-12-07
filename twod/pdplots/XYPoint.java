/**
 * This class represents a "point" on a grid.
 */
public class XYPoint {
    
    private double x;
    private double y;
    
    /**
     * Default Constructor.
     * @param x The X value
     * @param y The Y value
     */
    public XYPoint(final double x, final double y) {
        this.x = x;
        this.y = y;
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