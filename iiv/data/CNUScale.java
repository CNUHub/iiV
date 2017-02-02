package iiv.data;
import iiv.script.*;
/**
 * CNUScale contains parameters and tools for scaling voxel values
 * from one range of values to another with thresholding.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 */
public class CNUScale implements iiVScriptable {
  // current default scale
  static private Object defaultsLock = new Object();
  static private CNUScale defaultScale = new CNUScale(1.0);
  /**
   * Sets the default scale model.
   *
   * @param scale	new default scale model
   */
  public static void setDefaultScale(CNUScale scale) {
    synchronized (defaultsLock) {
      if(scale != null) defaultScale = scale.ScaleDuplicate();
    }
  }
  /**
   * Gets the default scale model.
   *
   * @return	current default scale model
   */
  public static CNUScale getDefaultScale() {
    synchronized (defaultsLock) {
      return defaultScale.ScaleDuplicate();
    }
  }
  private boolean identityState = true;
  private double scaleFactor = 1.0;
  private double translation = 0.0;
  private boolean threshMinState = false;
  private double threshMin = 0;
  private double threshMinValue = 0;
  private int threshMinIntValue = 0;
  private boolean threshMaxState = false;
  private double threshMax = 0;
  private double threshMaxValue = 0;
  private int threshMaxIntValue = 0;
  private boolean quantificationState = false;
  private double quantification = 1.0;
  /**
   * Constructs a new instance of CNUScale.
   *
   * @param scaleFactor	multiplication factor to apply to voxels
   */
  public CNUScale(double scaleFactor) {
    setScaleFactor(scaleFactor);
  }
  /**
   * Creates a duplicate instance.
   *
   * @return	duplicate CNUScale
   */
  public synchronized CNUScale ScaleDuplicate() {
    CNUScale cs = new CNUScale(scaleFactor);
    cs.setTranslation(translation);
    cs.setThreshMin(threshMin, threshMinValue);
    cs.setThreshMinState(threshMinState);
    cs.setThreshMax(threshMax, threshMaxValue);
    cs.setThreshMaxState(threshMaxState);
    cs.setQuantificationState( quantificationState );
    cs.setQuantification( quantification );
    return cs;
  }
  /**
   * Determines the equality of this and another object.
   *
   * @param cmp			object to compare to
   * @return			<code>true</code> if this and
   *				obj are equivalent,
   *				<code>false</code> otherwise.
   */
  public boolean equals(Object cmp) {
    if(! (cmp instanceof CNUScale)) return false;
    CNUScale ccmp = (CNUScale) cmp;
    if(ccmp.scaleFactor != scaleFactor) return false;
    if(ccmp.translation != translation) return false;
    if(ccmp.threshMinState != threshMinState) return false;
    if(ccmp.threshMin != threshMin) return false;
    if(ccmp.threshMinValue != threshMinValue) return false;
    if(ccmp.threshMinIntValue != threshMinIntValue) return false;
    if(ccmp.threshMaxState != threshMaxState) return false;
    if(ccmp.threshMax != threshMax) return false;
    if(ccmp.threshMaxValue != threshMaxValue) return false;
    if(ccmp.threshMaxIntValue != threshMaxIntValue) return false;
    if(ccmp.quantificationState != quantificationState) return false;
    if(ccmp.quantification != quantification) return false;
    return true;
  }
  /**
   * Generate a hash code that jives with equals.  Meaning 2 equal
   * equal objects will generate the same hash code.
   *
   * @return			hash code
   */
  public int hashCode() {
      return (new Double(scaleFactor)).hashCode()
	  + (new Double(translation)).hashCode();
  }
  /**
   * Creates a script that may be used to recreate this scale.
   *
   * @param scriptedObjects scripted objects list to add this object to.
   * @return  the script
   */
  public String toScript(CNUScriptObjects scriptedObjects) {
    String classname = getClass().getName();
    StringBuffer sb = new StringBuffer("// -- start ").append(classname).append(" script\n");
    if(scriptedObjects == null) scriptedObjects = new CNUScriptObjects();
    String variableName = scriptedObjects.get(this);
    if(variableName == null) {
	variableName = scriptedObjects.addObject(this, "cnuscale");
	sb.append(variableName).append(" = ");
	sb.append("new ").append(classname).append("(").append(scaleFactor).append(");\n");

	sb.append(variableName).append(".setTranslation(").append(translation).append(");\n");

	sb.append(variableName).append(".setThreshMin(").append(threshMin);
	sb.append(", ").append(threshMinValue).append(");\n");

	sb.append(variableName).append(".setThreshMinState(").append(threshMinState).append(");\n");

	sb.append(variableName).append(".setThreshMax(").append(threshMax);
	sb.append(", ").append(threshMaxValue).append(");\n");

	sb.append(variableName).append(".setThreshMaxState(").append(threshMaxState).append(");\n");

	sb.append(variableName).append(".setQuantification(").append(quantification).append(");\n");

	sb.append(variableName).append(".setQuantificationState(");
	sb.append(quantificationState).append(");\n");
    }
    sb.append("script_rtn=").append(variableName).append(";\n");
    sb.append("// -- end ").append(classname).append(" script\n");
    return sb.toString();
  }
  /**
   * Creates a duplicate instance adjusted to a
   * different quantification factor.  This allows keeping the same
   * quantified values from different sources scaled to the same
   * output values.
   *
   * @param quantification	quantification factor for duplicate
   * @return			duplicate adjusted to quantification factor
   */
  public synchronized CNUScale quantificationAdjusted( double quantification )
  {
    double adjustFactor = this.quantification/quantification;
    if( Double.isInfinite(adjustFactor) ) {
      adjustFactor = 1.0;
      quantification = this.quantification;
    }
    CNUScale cs = new CNUScale(scaleFactor / adjustFactor);
    cs.setTranslation(translation * adjustFactor);
    cs.setThreshMin(threshMin * adjustFactor, threshMinValue);
    cs.setThreshMinState(threshMinState);
    cs.setThreshMax(threshMax * adjustFactor, threshMaxValue);
    cs.setThreshMaxState(threshMaxState);
    cs.setQuantificationState( quantificationState );
    cs.setQuantification( quantification );
    return cs;
  }
  /**
   * Updates identity state to match all current values.  Called internally
   * when ever a value is changed.
   */
  public synchronized void updateIdentityState() {
    identityState = ((scaleFactor == 1.0) &&
		    (translation == 0.0) &&
		    (! threshMinState) &&
		    (! threshMaxState) );
  }
  /**
   * Tests this scale factor for identity.
   *
   * @return	<code>true</code> if this scaling is an identity transform
   */
  final public synchronized boolean identity() { return identityState; }
  /**
   * Sets the scale factor to given value.
   *
   * @param scaleFactor	new scale factor
   */
  final public synchronized void setScaleFactor(double scaleFactor) {
    this.scaleFactor = scaleFactor;
    updateIdentityState();
  }
  /**
   * Gets the scale factor.
   *
   * @return	the scale factor
   */
  final public synchronized double getScaleFactor() { return scaleFactor; }
  /**
   * Sets the translation amount.
   *
   * @param translation	amount to translate by
   */
  final public synchronized void setTranslation(double translation) {
    this.translation = translation;
    updateIdentityState();
  }
  /**
   * Gets the translation amount.
   *
   * @return	amount of translation
   */
  final public synchronized double getTranslation() { return translation; }
  /**
   * Sets the minimum threshold and threshold value.
   *
   * @param threshMin		minimum threshold
   * @param threshMinValue	value to use if number falls below
   *				the threshMin
   */
  final public synchronized void setThreshMin(double threshMin,
					      double threshMinValue) {
    this.threshMinState = true;
    this.threshMin = threshMin;
    this.threshMinValue = threshMinValue;
    this.threshMinIntValue = (int)(threshMinValue + .5);
    updateIdentityState();
  }
  /**
   * Gets the minimum threshold.
   *
   * @return	minimum threshold
   */
  final public synchronized double getThreshMin() { return threshMin; }
  /**
   * Gets the minimum threshold value.
   *
   * @return	value used if a number falls below the minimum threshold
   */
  final public synchronized double getThreshMinValue() {
     return threshMinValue;
  }
  /**
   * Sets the minimum threshold state.
   *
   * @param threshMinState	<code>true</code> to apply a min threshold
   */
  final public synchronized void setThreshMinState(boolean threshMinState) {
    this.threshMinState = threshMinState;
    updateIdentityState();
  }
  /**
   * Gets the minimum threshold state.
   *
   * @return	<code>true</code> if min threshold applies
   */
  final public synchronized boolean getThreshMinState() {
    return threshMinState;
  }
  /**
   * Sets the maximum threshold and threshold value.
   *
   * @param threshMax		the maximum threshold
   * @param threshMaxValue	value for numbers above the maximum
   */
  final public synchronized void setThreshMax(double threshMax,
    double threshMaxValue) {
    this.threshMaxState = true;
    this.threshMax = threshMax;
    this.threshMaxValue = threshMaxValue;
    this.threshMaxIntValue = (int)(threshMaxValue + .5);
    updateIdentityState();
  }
  /**
   * Gets the maximum threshold.
   *
   * @return	maximumn threshold
   */
  final public synchronized double getThreshMax() { return threshMax; }
  /**
   * Gets the maximum threshold value.
   *
   * @return	maximumm threshold value
   */
  final public synchronized double getThreshMaxValue() {
    return threshMaxValue;
  }
  /**
   * Sets the maximum threshold state.
   *
   * @param threshMaxState	<code>true</code> to apply maximum thresholding
   */
  final public synchronized void setThreshMaxState(boolean threshMaxState) {
    this.threshMaxState = threshMaxState;
    updateIdentityState();
  }
  /**
   * Gets the maximum threshold state.
   *
   * @return	<code>true</code> if maximumn thresholding applies
   */
  final public synchronized boolean getThreshMaxState() {
    return threshMaxState;
  }
  /**
   * Sets the quantification factor.
   *
   * @param quantification	quantification factor
   */
  public synchronized void setQuantification(double quantification) {
    this.quantification = quantification;
  }
  /**
   * Gets the quantification factor.
   *
   * @return the quantification factor
   */
  public synchronized double getQuantification() { return quantification; }
  /**
   * Sets the quantification mode.  This doesn't affect scaling but may
   * be used to decide how to display values.
   *
   * @param quantificationState	<code>true</code> to consider quantification on
   */
  public synchronized void setQuantificationState(boolean quantificationState) 
  {
    this.quantificationState = quantificationState;
  }
  /**
   * Returns quantification mode.
   *
   * @return	<code>true</code> to consider quantification on
   */
  public synchronized boolean getQuantificationState() {
    return quantificationState;
  }
  /**
   * Sets scale and translation to squeeze a range, taken from current
   * threshMin and threshMax, into an output range taken from current
   * threshMaxValue and threshMaxValue.
   *
   * @param inType	type of data to scale into range
   */
  public synchronized void setToFitDataInRange(int inType) {
    double minValue = getThreshMinValue();
    double maxValue = getThreshMaxValue();

    if(! CNUTypes.valid(inType) ) inType = CNUTypes.SHORT;
    double minInValue = CNUTypes.minValue(inType);
    if(getThreshMinState()) minInValue = getThreshMin();

    double maxInValue = CNUTypes.maxValue(inType);
    if(getThreshMaxState()) maxInValue = getThreshMax();

    setToFitDataInRange(minInValue, maxInValue, minValue, maxValue, true);
  }
  /**
   * Sets scale and translation to squeeze a range of input data into
   * an output range.
   *
   * @param inMin	minimum value of input data
   * @param inMax	maximum value of input data
   * @param outMin	minimum value for output data
   * @param outMax	maximum value for output data
   * @param threshold   <code>true</code> to enable thresholding
   */
  public synchronized void setToFitDataInRange(double inMin, double inMax,
				  double outMin, double outMax,
				  boolean threshold) {

    boolean signCorrection = false;
    if(inMax < inMin) {
      signCorrection = !signCorrection;
      double tmp = inMin; inMin = inMax; inMax = tmp;
    }
    if(outMax < outMin) {
      signCorrection = !signCorrection;
      double tmp = outMin; outMin = outMax; outMax = tmp;
    }
    // calculate needed scale
    double outRange = outMax - outMin;
    double inRange = inMax - inMin;
    double scale = outRange / inRange;
    // depend on Java to convert division by near zero to infinity
    if( Double.isInfinite(scale) )scale = 1.0;
    if( ! signCorrection ) {
      setScaleFactor(scale);
      // calculate needed translation
      setTranslation((outMin/scale) - inMin);
      setThreshMin(inMin, outMin);
      setThreshMax(inMax, outMax);
    }
    else {
      scale = -scale;
      setScaleFactor(scale);
      // calculate needed translation
      setTranslation((outMin/scale) - inMax);
      setThreshMin(inMin, outMax);
      setThreshMax(inMax, outMin);
    }
    if(! threshold) {
      setThreshMinState(false);
      setThreshMaxState(false);
    }
  }
  /**
   * Sets scale and translation to default for displaying with 8-bit display.
   *
   * @param inType	type of data to scale into range
   */
  public synchronized void setToDisplayDefault(int inType) {
    double inMin = CNUTypes.minValue(inType);
    double inMax = CNUTypes.maxValue(inType);
    // clamp values for float an integer which whould generate unusable ranges
    // no good guess for larger types but these values should
    // cause no scaling for outtype of integer or larger
    if(inMax > 65535.0) inMax = 65535.0;
    if(inMin < -65535.0) inMin = -65535.0;
    setToFitDataInRange(inMin, inMax, 0, 255, false);
  }
  /**
   * Sets scale and translation to default for displaying only positive values
   * with 8-bit display.
   *
   * @param inType	type of data to scale into range
   */
  public synchronized void setToDisplayPositive(int inType) {
    double inMax = CNUTypes.maxValue(inType);
    // clamp values for float an integer which whould generate unusable ranges
    // no good guess for larger types but these values should
    // cause no scaling for outtype of integer or larger
    if(inMax > 65535.0) inMax = 65535.0;
    setToFitDataInRange(0, inMax, 0, 255, true);
  }
  /**
   * Sets scale and translation to default for displaying only negative values
   * with 8-bit display.
   *
   * @param inType	type of data to scale into range
   */
  public synchronized void setToDisplayNegative(int inType) {
    double inMin = CNUTypes.minValue(inType);
    // clamp values for float an integer which whould generate unusable ranges
    // no good guess for larger types but these values should
    // cause no scaling for outtype of integer or larger
    if(inMin < -65535.0) inMin = -65535.0;
    setToFitDataInRange(0, inMin, 0, 255, true);
  }
  /**
   * Sets the scale factor to maximumn factor to store a given data type
   * as a given output data type.
   *
   * @param inType	type of data to scale into range
   * @param outType	type of data to determine output range for
   */
  public synchronized void setScaleFactor_for_max_res(int inType,
    int outType) {
    double inMin = CNUTypes.maxValue(inType);
    double inMax = CNUTypes.minValue(inType);
    // clamp values for float an integer which whould generate unusable ranges
    // no good guess for larger types but these values should
    // cause no scaling for outtype of integer or larger
    if(inMax > 65535.0) inMax = 65535.0;
    if(inMin < -65535.0) inMin = -65535.0;
    setScaleFactor_for_max_res(inMin, inMax, outType);
  }
  /**
   * Sets the scale factor to maximumn factor to store a range of values
   * as a given data type.
   *
   * @param inMin	minimum value of input data
   * @param inMax	maximum value of input data
   * @param outType	type of data to determine output range for
   */
  public synchronized void setScaleFactor_for_max_res(double inMin,
    double inMax, int outType) {
    double inMaxabs;
    double scale;
    double signCorrection = 1;
    if(inMax < inMin) {
      signCorrection = -1;
      double tmp=inMin; inMin=inMax; inMax=tmp;
    }
    if(inMax <= 0){
      //abs(inMin) must be >= abs(inMax)
      inMaxabs = Math.abs(inMin);
      //all negative numbers reverse sign for unsigned types
      inMax=inMin;
    }
    else {
      inMin = Math.abs(inMin);
      inMaxabs = (inMin > inMax)? inMin : inMax;
    }
    switch(outType) {
    case CNUTypes.BYTE:
      scale = 127.0/inMaxabs;
      break;
    case CNUTypes.UNSIGNED_BYTE:
      scale = 255.0/inMax;
      break;
    case CNUTypes.SHORT:
      scale = 32767.0/inMaxabs;
      break;
    case CNUTypes.UNSIGNED_SHORT:
      scale = 65535.0/inMax;
      break;
    case CNUTypes.INTEGER:
      //no real reason for this value
      if(inMaxabs<65535)
	scale=65535.0/inMaxabs;
      else scale=1;
      break;
    default:
      scale=1;
    }//end switch

    // depend on Java to convert division by near zero to infinity
    if( Double.isInfinite(scale) )scale = 1.0;
    
    setScaleFactor(scale * signCorrection);
  }
  /**
   * Applies this scaling to a double value returning a double value.
   *
   * @param value	value to apply to
   * @return		scaled value
   */
  public synchronized final double convert(double value){
    if(identityState)return value;
    if(threshMinState) {
      if(value < threshMin) return threshMinValue;
    }
    if(threshMaxState) {
      if(value > threshMax) return threshMaxValue;
    }
    return (value + translation) * scaleFactor;
  }
  /**
   * Applies the inverse of this scale settings to a double value
   * returning a double value.
   *
   * @param value	value to apply to
   * @return		inverse scaled value
   */
  public synchronized final double applyInverse(double value){
    if(identityState) return value;
    return (value / scaleFactor) - translation;
  }
  /**
   * Applies this scaling to a double value returning an integer value.
   *
   * @param value	value to apply to
   * @return		scaled value
   */
  public synchronized final int convert_to_int(double value) {
    if(identityState) return (int) (value + 0.5);
    if(threshMinState) {
      if(value < threshMin) return threshMinIntValue;
    }
    if(threshMaxState) {
      if(value > threshMax) return threshMaxIntValue;
    }
    return (int)( (value + translation) * scaleFactor + 0.5 );
  }
  /**
   * Applies the inverse of this scaling to an integer returning a
   * double value.
   *
   * @param iValue	value to apply to
   * @return		inverse scaled value
   */
  public synchronized final double applyInverse(int iValue) {
    if(identityState) return (double) iValue;
    return (( (double) iValue ) / scaleFactor) - translation;
  }
}
