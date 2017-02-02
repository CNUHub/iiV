package iiv.data;
/**
 * ScaleInterface defines routines to deal with scaling.  Any component
 * that wants to be updated with new CNUScale values needs to implement
 * this interface.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUScale
 * @since	iiV1.0
 */
public interface ScaleInterface {
  /**
   * Gets the current scale values.
   *
   * @return	current scale values
   */
  public CNUScale getScale();
  /**
   * Sets the current scale values.
   */
  public void setScale(CNUScale sc);
  /**
   * Updates scale.
   */
  public void updateScale();
  /**
   * Gets the data type.
   *
   * @return	data type
   */
  public int getType();
  /**
   * Gets the quantification factor for scaling.
   *
   * @return	quantification factor
   */ 
  public double getFactor();
}
