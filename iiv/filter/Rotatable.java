package iiv.filter;
/**
 * Rotatable defines routines to allow handling rotation values
 * for a component.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.display.CNUDisplay
 * @see		iiv.display.DisplayComponent
 * @since	iiV1.132
 */
public interface Rotatable {
  /**
   * Sets the rotation angle (degrees).
   *
   * @param rotation		rotation angle in degrees
   */
  public void setRotation(double rotation);
  /**
   * Gets the rotation angle.
   *
   * @return	rotation angle in degrees
   */
  public double getRotation();
}
