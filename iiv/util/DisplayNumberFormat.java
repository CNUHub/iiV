package iiv.util;
import iiv.script.*;
import java.text.*;
/**
 * DisplayNumberFrom extends DecimalFormat to incorporate
 * exponents and handles number format defaults for most
 * iiV display components.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.display.NumberFormattable
 * @since	iiV1.142
 */
public class DisplayNumberFormat extends DecimalFormat
    implements iiVScriptable {
  private static final long serialVersionUID = 745064722466502307L;
  public final static int NO_EXPONENT = -1;
  public final static int MINIMUM_EXPONENT_MULTIPLE = NO_EXPONENT;
  public final static int INVALID_EXPONENT_MULTIPLE = MINIMUM_EXPONENT_MULTIPLE - 1;
  public final static int ZERO_INTEGER_EXPONENT_MULTIPLE = 0;
  // 127 chosen because it was the value from a default DecimalNumber
  public final static int BIG_VALUE = 127;

  private static Object defaultLock = new Object();
  private static NumberFormat defaultNumberFormat;
  private static int defaultExponentMultiples = NO_EXPONENT;
  private static String defaultExponentSymbol = "E";

  private int exponentMultiples = getDefaultExponentMultiples();
  private String exponentSymbol = getDefaultExponentSymbol();
  /**
   * Creates a new instance of DisplayNumberFormat with
   * current defaults.
   */
  public DisplayNumberFormat() {
    this(INVALID_EXPONENT_MULTIPLE, null);
  }
  /**
   * Creates a new instance of DisplayNumberFormat.
   */
  public DisplayNumberFormat(int pm, String es) {
    if(pm < MINIMUM_EXPONENT_MULTIPLE) pm = getDefaultExponentMultiples();
    exponentMultiples = pm;
    if(es == null) es = getDefaultExponentSymbol();
    exponentSymbol = es;
  }
  /**
   * Determines the equality of this and another object.
   *
   * @param obj			object to compare to
   * @return			<code>true</code> if this and
   *				obj are equivalent,
   *				<code>false</code> otherwise.
   */
  public boolean equals(Object obj) {
    if(! (obj instanceof DisplayNumberFormat)) return false;
    DisplayNumberFormat dnf = (DisplayNumberFormat) obj;
    if(dnf.exponentMultiples != exponentMultiples) return false;
    if(dnf.exponentSymbol != exponentSymbol) return false;
    return super.equals(obj);
  }
  /**
   * Sets default NumberFormat form non-exponent part of numbers
   *
   * @param numberFormat	number format tool
   */
  public static void setDefaultNumberFormat(NumberFormat numberFormat) {
    if(numberFormat != null) synchronized (defaultLock) {
      defaultNumberFormat = (NumberFormat) numberFormat.clone();
    }
  }
  /**
   * Gets the default number format.
   *
   * @return	number format
   */
  public static NumberFormat getDefaultNumberFormat() {
    synchronized (defaultLock) {
      if(defaultNumberFormat == null) {
	defaultNumberFormat = new DisplayNumberFormat();
      }
      return (NumberFormat) defaultNumberFormat.clone();
    }
  }
  /**
   * Sets the default exponent multiples of 10 for the exponent portion
   * of a number.
   * Exponents multiples of 1 is standard scientific notation and exponent
   * multiples of 3 is standard engineering notation.  A exponent multiple
   * 0 or less allows for no exponent.
   *
   * @param exponentMultiples exponent multiples
   */
  public static void setDefaultExponentMultiples(int exponentMultiples) {
    if(exponentMultiples < MINIMUM_EXPONENT_MULTIPLE) exponentMultiples = NO_EXPONENT;
    defaultExponentMultiples = exponentMultiples;
  }
  /**
   * Gets the default exponent multiple of 10 for the exponent portion of
   * a number.
   *
   * @return exponent multiple 0 for no exponent
   */
  public static int getDefaultExponentMultiples() {
    return defaultExponentMultiples;
  }
  /**
   * Sets the default exponent symbol.
   *
   * @param exponentSymbol exponent symbol
   */
  public static void setDefaultExponentSymbol(String exponentSymbol) {
    if(exponentSymbol == null) exponentSymbol="";
    defaultExponentSymbol = exponentSymbol;
  }
  /**
   * Gets the default exponent symbol.
   *
   * @return exponent symbol
   */
  public static String getDefaultExponentSymbol() {
    return defaultExponentSymbol;
   }
  /**
   * Sets the exponent multiples of 10 for the exponent portion of a number.
   * Exponents multiples of 1 is standard scientific notation and exponent
   * multiples of 3 is standard engineering notation.  A exponent multiple
   * 0 or less allows for no exponent.
   *
   * @param exponentMultiples exponent multiple. 
   *			  if <code>< 0</code> sets to current default
   */
  public void setExponentMultiples(int exponentMultiples) {
    if(exponentMultiples < MINIMUM_EXPONENT_MULTIPLE)
      exponentMultiples = getDefaultExponentMultiples();
    this.exponentMultiples = exponentMultiples;
  }
  /**
   * Gets the exponent multiple of 10 for the exponent portion of a number.
   *
   * @return exponent multiple 0 for no exponent
   */
  public int getExponentMultiples() { return exponentMultiples; }
  /**
   * Sets the exponent symbol.
   *
   * @param exponentSymbol exponent symbol.  if<code>null</code>
   *			   sets to current default
   */
  public void setExponentSymbol(String exponentSymbol) {
    if(exponentSymbol == null) exponentSymbol=getDefaultExponentSymbol();
    this.exponentSymbol = exponentSymbol;
  }
  /**
   * Gets the exponent symbol.
   *
   * @return exponent symbol
   */
  public String getExponentSymbol() { return exponentSymbol; }
  /**
   * Formats a number with possible exponent.
   *
   * @param d number to format
   * @return formatted number
   */
  public StringBuffer format(double d, StringBuffer toAppendTo,
			     FieldPosition pos) {
    String expString = "";
    int sign = 1;
    if(d < 0) { sign = -1; d *= sign; }
    int exp = 0;
    int p = getExponentMultiples();
    if(d > Double.MIN_VALUE) {
      if(p > 0) {
        double positiveExponent = Math.pow(10d, p);
        double inverseExponent = 1.0/positiveExponent;
        while(d > positiveExponent) { d *= inverseExponent; exp += p; }
        while(d < 1) { d *= positiveExponent; exp -= p; }
        if(exp != 0) {
	  expString = getExponentSymbol() + exp;
        }
      }
      else if(p == ZERO_INTEGER_EXPONENT_MULTIPLE) {
	// integer part always "0."
	p = 1;
        double positiveExponent = 10;
        double inverseExponent = 0.1;
        while(d < 0.1) { d *= positiveExponent; exp -= p; }
        while(d > 1.0) { d *= inverseExponent; exp += p; }
        if(exp != 0) {
	  expString = getExponentSymbol() + exp;
        }
      }
    }
    super.format(sign * d, toAppendTo, pos);
    toAppendTo.append(expString);
    return toAppendTo;
  }
  /**
   * Returns a string representations for an exponent multiple.
   *
   * @param exponentMultiple multiple to represent.
   */
  public static String exponentMultipleToString(int exponentMultiple) {
    if(exponentMultiple == ZERO_INTEGER_EXPONENT_MULTIPLE)
      return "ZERO_INTEGER_EXPONENT_MULTIPLE";
    if(exponentMultiple == NO_EXPONENT) return "NO_EXPONENT";
    if((exponentMultiple < MINIMUM_EXPONENT_MULTIPLE) ||
       (exponentMultiple == INVALID_EXPONENT_MULTIPLE))
      return "INVALID_EXPONENT_MULTIPLE";
    return Integer.toString(exponentMultiple);
  }
  /**
   * Returns a string representations for an exponent multiple.
   *
   * @param exponentMultiple multiple to represent.
   */
  public static String exponentMultipleToFullString(int exponentMultiple) {
    if(exponentMultiple == ZERO_INTEGER_EXPONENT_MULTIPLE)
      return DisplayNumberFormat.class.getName() + ".ZERO_INTEGER_EXPONENT_MULTIPLE";
    if(exponentMultiple == NO_EXPONENT) return DisplayNumberFormat.class.getName() + ".NO_EXPONENT";
    if((exponentMultiple < MINIMUM_EXPONENT_MULTIPLE) ||
       (exponentMultiple == INVALID_EXPONENT_MULTIPLE))
      return DisplayNumberFormat.class.getName() + ".INVALID_EXPONENT_MULTIPLE";
    return Integer.toString(exponentMultiple);
  }
  /**
   * Creates a script to recreate this object.
   *
   *
   * @param scriptedObjects scripted objects list to add this object to.
   * @return	script that can recreate this object
   */
  public String toScript(CNUScriptObjects scriptedObjects) {
    String classname = getClass().getName();
      StringBuffer sb = new StringBuffer();
      sb.append("// -- start ").append(classname).append(" script\n");
      if(scriptedObjects == null) scriptedObjects = new CNUScriptObjects();
      String variableName = scriptedObjects.get(this);
      if(variableName == null) {
	variableName = scriptedObjects.addObject(this, "displaynumberformat");
	sb.append(variableName).append(" = new ").append(classname).append("(");
	sb.append(exponentMultipleToFullString(getExponentMultiples()));
	sb.append(", \"").append(getExponentSymbol()).append("\");\n");
	sb.append(postObjectToScript(scriptedObjects));
      }
      sb.append("script_rtn=").append(variableName).append(";\n");
      sb.append("// -- end ").append(classname).append(" script\n");
      return sb.toString();
  }
  /**
   * Creates a script to recreate the parent NumberFormat
   * settings on an existing copy. This script
   * only deals with NumberFormat settings that are normally changed.
   * Other settings are left to locality defaults.
   *
   * @param scriptedObjects scripted objects list this object already
   *                        exists in.
   * @return	script to recreate this component
   */
  public String postObjectToScript(CNUScriptObjects scriptedObjects) {
      return numberFormatPostObjectToScript(scriptedObjects, this);
  }
  /**
   * Creates a script to recreate NumberFormat settings.
   * This script only deals with NumberFormat settings
   * that are normally changed.
   * Other settings are left to locality defaults.
   *
   * @param scriptedObjects scripted objects list this object already
   *                        exists in.
   * @param nf number format object to base settings on.
   * @return		    CNU script to change the objects settings
   */
  public static String numberFormatPostObjectToScript(
    CNUScriptObjects scriptedObjects, NumberFormat nf) {
      String objectVariableName = scriptedObjects.get(nf);
      StringBuffer sb = new StringBuffer();
      sb.append(objectVariableName);
      sb.append(".setMaximumFractionDigits(");
      sb.append(nf.getMaximumFractionDigits()).append(");\n");
      sb.append(objectVariableName);
      sb.append(".setMinimumFractionDigits(");
      sb.append(nf.getMinimumFractionDigits()).append(");\n");
      sb.append(objectVariableName);
      sb.append(".setMaximumIntegerDigits(");
      sb.append(nf.getMaximumIntegerDigits()).append(");\n");
      sb.append(objectVariableName);
      sb.append(".setMinimumIntegerDigits(");
      sb.append(nf.getMinimumIntegerDigits()).append(");\n");
      sb.append(objectVariableName);
      sb.append(".setGroupingUsed(");
      sb.append(nf.isGroupingUsed()).append(");\n");
      return sb.toString();
  }
  /**
   * Creates a script to recreate NumberFormat settings.
   * This script only deals with NumberFormat settings
   * that are normally changed.
   * Other settings are left to locality defaults.
   *
   * @param scriptedObjects scripted objects list this object already
   *                        exists in.
   * @param nf number format object to base settings on.
   * @return			CNU script to change the objects settings
   */
  public static String numberFormatToScript(CNUScriptObjects scriptedObjects,
					    NumberFormat nf) {
    if(scriptedObjects == null) scriptedObjects = new CNUScriptObjects();
    if(nf instanceof DisplayNumberFormat)
      return ((DisplayNumberFormat) nf).toScript(scriptedObjects);
    StringBuffer sb = new StringBuffer("// -- start java.text.NumberFormat script\n");
    String variableName = scriptedObjects.get(nf);
    if(variableName == null) {
	variableName = scriptedObjects.addObject(nf, "numberformat");

	sb.append(variableName).append(" = java.text.NumberFormat.getNumberInstance();\n");
	sb.append(numberFormatPostObjectToScript(scriptedObjects, nf));
    }
    sb.append("script_rtn=").append(variableName).append(";\n");
    sb.append("// -- end java.text.NumberFormat script\n");
    return sb.toString();
  }
}
