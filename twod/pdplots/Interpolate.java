import java.util.ArrayList;
import java.util.List;

    /**
 * This class contains various functions for interpolation.
 * Unless otherwise marked, the code in this class was ported from Mathapps (Mathstuf.bas)
 * of the legacy VB code of PCIMAT.
 * 
 * @author Eric.Tavares
 * 
 */
public final class Interpolate {

   /**
    * Private constructor to prevent instantiation.
    */
   private Interpolate() {

   }

   /**
    * A simple linear interpolation.
    * 
    * @param numPoints
    *           The number of points in the XY curve
    * @param points
    *           The XY curve
    * @param xin
    *           The new X value
    * @param extrapolate
    *           True=Extrapolate off the ends of the curve, using the last
    *           gradient; False=Use the min or max value off the ends of the
    *           curve
    * @return The interpolated Y value
    */
   public static double interp(final int numPoints, final XYPoint[] points,
         final double xin, final boolean extrapolate) {

      int i; 
      double w2;
      double tol = 1E-30;
      double yout;

      if (numPoints < 1) {
         i = 0;
         yout = 0;

      } else if (numPoints < 2) {
         i = 0;
         yout = points[0].getY();

      } else {
         if (points[1].getX() > points[0].getX()) {
            i = 1;
            while ((points[i].getX() < xin) && (i < numPoints - 1)) {
               i = i + 1;
            }

            if (Math.abs(points[i].getX() - points[i - 1].getX()) < tol) {
               w2 = 0.5;
            } else {
               w2 = (xin - points[i - 1].getX()) / (points[i].getX() - points[i - 1].getX());
               if (!extrapolate) {
                  w2 = Math.max(0, Math.min(1, w2));
               }
            }

            yout = (1 - w2) * points[i - 1].getY() + w2 * points[i].getY();

         } else {
            i = 1;
            while ((points[i].getX() > xin) && (i < numPoints - 1)) {
               i = i + 1;
            }

            if (Math.abs(points[i].getX() - points[i - 1].getX()) < tol) {
               w2 = 0.5;
            } else {
               w2 = Math.max(0, Math.min(1, (xin - points[i - 1].getX()) / (points[i].getX() - points[i - 1].getX())));
            }

            yout = (1 - w2) * points[i - 1].getY() + w2 * points[i].getY();

         }
      }

      return yout;
   }

   /**
    * A simple linear interpolation which interpolates for x in an x-y array whose y values increase with array index.
    * 
    * @param numPoints
    *           The number of points in the XY curve
    * @param points
    *           The XY curve
    * @param yin
    *           The new Y value
    * @return The interpolated X value
    */
   public static double interpyx(int numPoints, final XYPoint[] points, final double yin) {
      // interpolate for x in an x-y array whose y values increase with array index.
      XYPoint[] pointsSwapped = new XYPoint[numPoints];

      for (int i = 0; i < numPoints; i++) {
         pointsSwapped[i] = new XYPoint(points[i].getY(), points[i].getX());
      }
      return interp(numPoints, pointsSwapped, yin, false);
   }
   /**
    * Interpolate into a List<XYPoint> array
    * @param xIn input X value (to interpolate to)
    * @param xyCurve Input curve
    * @return interpolated y value
    */
   public static double interp(double xIn, List<XYPoint> xyCurve) {
   int numPts = xyCurve.size();
   XYPoint[] xy = new XYPoint[numPts];
   for (int i = 0; i < numPts; i++) {
         XYPoint point = new XYPoint(0.0, 0.0);
         point = xyCurve.get(i);
         xy[i] = point;
      }
   double yOut = Interpolate.interp(numPts, xy , xIn, false);
   return yOut;
   }   
}
