package iiv.filter;
/**
 * Flippable defines routines to allow handling flip values
 * for a component.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.display.CNUDisplay
 * @see		iiv.display.DisplayComponent
 * @since	iiV1.132
 */
public interface Flippable {
  /**
   * Sets both flip options.
   *
   * @param flipV	<code>true</code> to flip image vertically
   * @param flipH	<code>true</code> to flip image horizontally
   */
  public void setFlips(boolean flipV, boolean flipH);
  /**
   * Gets the vertical flip option.
   *
   * @return	<code>true</code> if image is flipped vertically
   */
  public boolean getFlipV();
  /**
   * Gets the horizontal flip option.
   *
   * @return	<code>true</code> if image is flipped horizontally
   */
  public boolean getFlipH();
}
