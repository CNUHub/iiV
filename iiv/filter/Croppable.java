package iiv.filter;
import java.awt.*;
/**
 * Croppable defines routines to allow handling the crop box
 * for a component.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.display.CNUDisplay
 * @see		iiv.display.DisplayComponent
 * @since	iiV1.132
 */
public interface Croppable {
  /**
   * Crops the input image by a given box.
   *
   * @param cropBox	new crop box
   */
  public void setCrop(Rectangle cropBox);
  /**
   * Gets the current crop box.
   *
   * @return	the crop box
   */
  public Rectangle getCrop();
  /**
   * Restricts a box to fit on the croppable portion of the component.
   *
   * @param cropBox	box to restrict
   * @return		box that intersects croppable portion
   */
  public Rectangle restrictCropBox(Rectangle cropBox);
}
