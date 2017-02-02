package iiv.util;
import java.text.*;
/**
 * Class to format numbers with exponentials.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.CNUViewer
 * @see		java.awt.Container
 * @since	iiV1.142
 */
public class FormatTools {
  public static DecimalFormat decimalFormat = new DecimalFormat();
  private static String defaultExponentSymbol="E";
  final public static int LEFT = 0;
  final public static int RIGHT = 1;
  /**
   * Formats the number to have exponent powers of 3 using default
   * DecimalFormat for non-exponent portion.
   *
   * @param d number to format
   * @return  number as a formatted string
   */
  public static String formatEngineering(double d) {
    return formatEngineering(d, decimalFormat);
  }
  /**
   * Formats the number to have exponent powers of 3.
   *
   * @param d number to format
   * @param df formatter for non-exponent part of number
   * @return  number as a formatted string
   */
  public static String formatEngineering(double d, NumberFormat df) {
    String expString = "";
    int sign = 1;
    if(d < 0) { sign = -1; d *= sign; }
    int exp = 0;
    if(d > Double.MIN_VALUE) {
      while(d > 1000) { d *= 1e-3; exp += 3; }
      while(d < 1) { d *= 1e3; exp -= 3; }
      if(exp != 0) expString = defaultExponentSymbol + exp;
    }
    return df.format(sign * d) + expString;
  }
  /**
   * Formats the number to have exponent powers of 1 with default
   * DecimalFormat for non-exponent portion.
   *
   * @param d number to format
   * @return  number as a formatted string
   */
  public static String formatScientific(double d) {
    return formatScientific(d, decimalFormat);
  }
  /**
   * Formats the number to have exponent powers of 1.
   *
   * @param d number to format
   * @param df formatter for non-exponent part of number
   * @return  number as a formatted string
   */
  public static String formatScientific(double d, NumberFormat df) {
    String expString = "";
    int sign = 1;
    if(d < 0) { sign = -1; d *= sign; }
    int exp = 0;
    if(d > Double.MIN_VALUE) {
      while(d > 1) { d *= 0.1d; exp += 1; }
      while(d < 1) { d *= 10d; exp -= 1; }
      if(exp != 0) expString = defaultExponentSymbol + exp;
    }
    return df.format(sign * d) + expString;
  }
  /**
   * Formats the number to have exponent powers of p.
   *
   * @param d number to format
   * @param p exponent powers, if < 1 no exponent
   * @param df formatter for non-exponent part of number
   * @return  number as a formatted string
   */
  public static String formatScientific(double d, int p, NumberFormat df) {
    return formatScientific(d, p, df, defaultExponentSymbol);
  }
  /**
   * Formats the number to have exponent powers of p.
   *
   * @param d number to format
   * @param p exponent powers, if < 1 no exponent
   * @param df formatter for non-exponent part of number
   * @param exponentSymbol string containing the exponent symbol
   * @return  number as a formatted string
   */
  public static String formatScientific(double d, int p, NumberFormat df,
	String exponentSymbol) {
    String expString = "";
    int sign = 1;
    if(d < 0) { sign = -1; d *= sign; }
    int exp = 0;
    if((p > 0) && (d > Double.MIN_VALUE)) {
      double positivePower = Math.pow(10d, p);
      double inversePower = 1.0/positivePower;
      while(d > positivePower) { d *= inversePower; exp += p; }
      while(d < 1) { d *= positivePower; exp -= p; }
      if(exp != 0) {
	if(exponentSymbol == null) exponentSymbol = defaultExponentSymbol;
	expString = exponentSymbol + exp;
      }
    }
    return df.format(sign * d) + expString;
  }
  /**
   * Pads a string with spaces.
   *
   * @param s		string to pad
   * @param toLength	length to grow string to
   * @param mode	RIGHT or LEFT padding
   * @return		padded string
   */
  final public static String padString(String s, int toLength, int mode) {
    int padding = toLength - s.length();
    if(padding > 0) {
      StringBuffer sb = new StringBuffer(toLength);
      if(mode == RIGHT) sb.append(s);
      for(int i=0; i<padding; i++) sb = sb.append(" ");
      if(mode == LEFT) sb.append(s);
      s = sb.toString();
    }
    return s;
  }
}
