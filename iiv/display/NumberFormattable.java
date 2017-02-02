package iiv.display;
import java.text.*;
/**
 * NumberFormattable defines routines to allow setting number formatting
 * rules for numbers generated and displayed by a component.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		java.text.NumberFormat
 * @see		CNUDisplay
 * @see		DisplayComponent
 * @since	iiV1.142
 */
public interface NumberFormattable {
  /**
   * Sets the number format for converting numbers to strings.
   *
   * @param numberFormat	number format tool
   */
  public void setNumberFormat(NumberFormat numberFormat);
  /**
   * Gets the number format.
   *
   * @return	number format
   */
  public NumberFormat getNumberFormat();
}
