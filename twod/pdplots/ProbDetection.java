import java.util.ArrayList;
import java.util.List;

/**
 * Class encapsulating the methods needed to convert signal excess
 * to *Instantaneous* Probability of detection (as opposed to Cumulative PD)
 * @author laurie.gainey
 * 
 */
public final class ProbDetection {

   /**
    * Private constructor to prevent instantiation
    */
   private ProbDetection() {
   }

   /**
    * Get the Receiver Operating (ROC) Curve.  
    * This is the Urick table, modified by Dave Heming
    * @param active is this active SE?
    * @param inSitu is this an insitu measurement?
    * @return xyRoc the modifed Urick ROC Curve Table
    */
   public static List<XYPoint>  getRocCurveModifiedUrick(boolean active, boolean inSitu) {
       List<XYPoint> xyRoc = new ArrayList<XYPoint>();
       double stdDeviation;

       if (active) {
          stdDeviation = 12.0;
       } else {
          stdDeviation = 8.0;
       }
       if (inSitu) {
          stdDeviation = stdDeviation - 2.0;
       }

       xyRoc.add(new XYPoint((double)(-3*stdDeviation),(double).001));
       xyRoc.add(new XYPoint((double)(-2.9*stdDeviation),(double)0.002));
       xyRoc.add(new XYPoint((double)(-2.8*stdDeviation),(double)0.003));
       xyRoc.add(new XYPoint((double)(-2.7*stdDeviation),(double)0.004));
       xyRoc.add(new XYPoint((double)(-2.6*stdDeviation),(double) 0.005));
       xyRoc.add(new XYPoint((double)(-2.5*stdDeviation),(double)0.006));
       xyRoc.add(new XYPoint((double)(-2.4*stdDeviation),(double)0.008));
       xyRoc.add(new XYPoint((double)(-2.3*stdDeviation),(double)0.011));
       xyRoc.add(new XYPoint((double)(-2.2*stdDeviation),(double)0.014));
       xyRoc.add(new XYPoint((double)(-2.1*stdDeviation),(double)0.018));
       xyRoc.add(new XYPoint((double)(-2.0*stdDeviation),(double) 0.023));
       xyRoc.add(new XYPoint((double)(-1.9*stdDeviation),(double)0.029));
       xyRoc.add(new XYPoint((double)(-1.8*stdDeviation),(double)0.036));
       xyRoc.add(new XYPoint((double)(-1.7*stdDeviation),(double)0.045));
       xyRoc.add(new XYPoint((double)(-1.6*stdDeviation),(double)0.055));
       xyRoc.add(new XYPoint((double)(-1.5*stdDeviation),(double)0.067));
       xyRoc.add(new XYPoint((double)(-1.4*stdDeviation),(double)0.081));
       xyRoc.add(new XYPoint((double)(-1.3*stdDeviation),(double) 0.097));
       xyRoc.add(new XYPoint((double)(-1.2*stdDeviation),(double)0.115));
       xyRoc.add(new XYPoint((double)(-1.1*stdDeviation),(double)0.136));
       xyRoc.add(new XYPoint((double)(-1.0*stdDeviation),(double)0.159));
       xyRoc.add(new XYPoint((double)(-0.9*stdDeviation),(double)0.184));
       xyRoc.add(new XYPoint((double)(-0.8*stdDeviation),(double)0.212));
       xyRoc.add(new XYPoint((double)(-0.7*stdDeviation),(double)0.242));
       xyRoc.add(new XYPoint((double)(-0.6*stdDeviation),(double)0.274));
       xyRoc.add(new XYPoint((double)(-0.5*stdDeviation),(double)0.308));
       xyRoc.add(new XYPoint((double)(-0.4*stdDeviation),(double)0.345));
       xyRoc.add(new XYPoint((double)(-0.3*stdDeviation),(double)0.382));
       xyRoc.add(new XYPoint((double)(-0.2*stdDeviation),(double)0.421));
       xyRoc.add(new XYPoint((double)(-0.1*stdDeviation),(double)0.46));
       xyRoc.add(new XYPoint((double)(0.0*stdDeviation),(double) 0.5));
       xyRoc.add(new XYPoint((double)(0.1*stdDeviation),(double)0.54));
       xyRoc.add(new XYPoint((double)(0.2*stdDeviation),(double)0.579));
       xyRoc.add(new XYPoint((double)(0.3*stdDeviation),(double)0.618));
       xyRoc.add(new XYPoint((double)(0.4*stdDeviation),(double)0.655));
       xyRoc.add(new XYPoint((double)(0.5*stdDeviation),(double)0.692));
       xyRoc.add(new XYPoint((double)(0.6*stdDeviation),(double)0.726));
       xyRoc.add(new XYPoint((double)(0.7*stdDeviation),(double)0.758));
       xyRoc.add(new XYPoint((double)(0.8*stdDeviation),(double)0.78));
       xyRoc.add(new XYPoint((double)(0.9*stdDeviation),(double)0.816));
       xyRoc.add(new XYPoint((double)(1.0*stdDeviation),(double)0.841));
       xyRoc.add(new XYPoint((double)(1.1*stdDeviation),(double)0.864));
       xyRoc.add(new XYPoint((double)(1.2*stdDeviation),(double)0.885));
       xyRoc.add(new XYPoint((double)(1.3*stdDeviation),(double)0.903));
       xyRoc.add(new XYPoint((double)(1.4*stdDeviation),(double)0.919));
       xyRoc.add(new XYPoint((double)(1.5*stdDeviation),(double)0.933));
       xyRoc.add(new XYPoint((double)(1.6*stdDeviation),(double)0.945));
       xyRoc.add(new XYPoint((double)(1.7*stdDeviation),(double)0.955));
       xyRoc.add(new XYPoint((double)(1.8*stdDeviation),(double)0.964));
       xyRoc.add(new XYPoint((double)(1.9*stdDeviation),(double)0.971));
       xyRoc.add(new XYPoint((double)(2.0*stdDeviation),(double)0.977));
       xyRoc.add(new XYPoint((double)(2.1*stdDeviation),(double)0.982));
       xyRoc.add(new XYPoint((double)(2.2*stdDeviation),(double)0.986));
       xyRoc.add(new XYPoint((double)(2.3*stdDeviation),(double)0.989));
       xyRoc.add(new XYPoint((double)(2.4*stdDeviation),(double)0.992));
       xyRoc.add(new XYPoint((double)(2.5*stdDeviation),(double)0.994));
       xyRoc.add(new XYPoint((double)(2.6*stdDeviation),(double)0.995));
       xyRoc.add(new XYPoint((double)(2.7*stdDeviation),(double)0.996));
       xyRoc.add(new XYPoint((double)(2.8*stdDeviation),(double)0.997));
       xyRoc.add(new XYPoint((double)(2.9*stdDeviation),(double)0.998));
       xyRoc.add(new XYPoint((double)(3.0*stdDeviation),(double)0.999));

       return xyRoc;
   }
   /**
    * Send in a Signal Excess (SE) value and get back a Probability of Detection (PD) value 
    * @param signalExcess Signal Excess in dB
    * @param active is this active SE?
    * @param inSitu is this an inSitu (measured) SE
    * @return probability of detection (pd)
    */
   public double  signalExcessToProbDetection(double signalExcess) {
      return signalExcessToProbDetection(signalExcess,false,false);
  }

   public double  signalExcessToProbDetection(double signalExcess, boolean active, boolean inSitu) {
      List<XYPoint> xyRoc = new ArrayList<XYPoint>();
      
      double pd;
      //TODO Post 7.1r1  Laurie:  Would rather not have to construct xyRoc on every call
      //       It would be preferable to have some kind of initilization call
      xyRoc = getRocCurveModifiedUrick(active, inSitu);
      pd = Interpolate.interp(signalExcess, xyRoc);

      return pd;
  }
   /**
    * Send in a Signal Excess (SE) value and get back a Probability of Detection (PD) value
    * @param seData SE vs Range 
    * @param active flag for active calculations
    * @param inSitu flag for in-situ (measured) SE
    * @return probability of detection (Pd) curve
    */
   public List<XYPoint>  signalExcessToProbDetectionCurve(List<XYPoint> seData, boolean active, boolean inSitu) {
      List<XYPoint> xyRoc = new ArrayList<XYPoint>();
      List<XYPoint> xyPd = new ArrayList<XYPoint>();
      double pd;
      xyRoc = getRocCurveModifiedUrick(active, inSitu);
      for (int i=0; i<seData.size(); i++) {
         pd = Interpolate.interp(seData.get(i).getY(), xyRoc);
         XYPoint xy = new XYPoint(seData.get(i).getX(), pd);
         xyPd.add(xy);
      }
      return xyPd;
  }
   /**
    * Compute the SE Threshold based on a Probability of Detection (0 - 1.0)
    * @param pd the Probability of detection (between 0 and 1)
    * @param active Is this an active sensor?
    * @param inSitu Is this measured PD?
    * @return the SE threshold
    */
   public double  probDetectionToSEThreshold(double pd, boolean active, boolean inSitu) {
      List<XYPoint> xyRoc = new ArrayList<XYPoint>();
      XYPoint[] yxData;
      
      double seThreshold;
      xyRoc = getRocCurveModifiedUrick(active, inSitu);
      int npts = xyRoc.size();
      yxData = new XYPoint[npts];
      for (int i=0; i<npts; i++) {
        yxData[i] = new XYPoint(xyRoc.get(i).getY(), xyRoc.get(i).getX());
      }
      seThreshold = Interpolate.interp(npts, yxData, pd, false);

      return seThreshold;
  }

}