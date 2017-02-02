package iiv.display;
/**
 * SliceNumbering defines routines to allow setting the slice
 * of data displayed by a component.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUDisplay
 * @see		DisplayComponent
 * @see		SingleImg
 * @since	iiV1.132
 */
public interface SliceNumbering {
  /**
   * Sets and displays a new slice of the input data.
   *
   * @param slice	the slice number
   */
  public void setSlice( int slice );
  /**
   * Gets the displayed slice number relative to the input data.
   *
   * @return	the slice number
   */
  public int getSlice();
  /**
   * Gets the slice number associated with a raw data point location
   * for this images view mode.
   *
   * @param point	raw data location to determine slice number for
   * @return		slice number or <code>-1</code> if not on image
   */
  public int getSlice(int[] point);
}
