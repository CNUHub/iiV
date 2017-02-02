package iiv.display;
import java.awt.*;
/**
 * ShowPointImage defines methods for a component to display voxel values.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		ShowPointDisplay
 * @see		DisplayComponent
 * @since	iiV1.1
 */
public interface ShowPointImage {
  public static int NO_CROSSHAIR = 0;
  public static int TRACKING_CROSSHAIR = 1;
  public static int FIXED_CROSSHAIR = 2;
  /**
   * Displays the value of the data associated with this component
   * at the given indice in the given show point display.
   *
   * @param indices	the indices into the data
   * @param spd		the display to show the point data in
   */
  public void showPoint(int[] indices, ShowPointDisplay spd);
  /**
   * Gets the indices corresponding to the original raw data this display
   * component is created from based on a point relative to the component.
   *
   * @param pt	point 	location relative to component
   * @return	indices indices to original raw data which may have
   *			any number of dimensions or <code>null</code>
   *			if invalid point
   */
  public int[] getIndices(Point pt);
 /**
   * Gets the point relative to the component given the indices
   * corresponding to the original raw data this display component
   * is created from.
   *
   * @param	indices indices to original raw data which may have
   *		any number of dimensions
   * @return	location relative to the component.  Indices that map
   *		off the image are forced onto the image.
   */
  public Point getPoint(int[] indices);
  /**
   * Erases the old crosshair a draws a new one on this component.
   *
   * @param indices		indices the crosshair points to
   * @param crosshairColor	color of crosshair
   */
  public void setCrosshair(int indices[], Color crosshairColor);
  /**
   * Gets a copy of the currently displayed crosshair indices.
   *
   * @return	indices to currently displayed crosshair
   */
   public int[] getCrosshairIndices();
  /**
   * Gets the currently displayed crosshair color
   *
   * @return	color of currently displayed crosshair
   */
   public Color getCrosshairColor();
  /**
   * Erases the old tracking crosshair a draws a new one on this component with
   * exclusive-or for fast drawing an erasing.
   *
   * @param indices		indices the crosshair points to
   * @param crosshairColor	color of crosshair
   */
  public void drawTrackingCrosshair(int[] indices, Color crosshairColor);
}
