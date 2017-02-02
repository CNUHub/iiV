package iiv.display;
import java.awt.*;
/**
 * LocationMapping defines routines to allow handling display location mapping
 * for a component.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUDisplay
 * @see		DisplayComponent
 * @since	iiV1.132
 */
public interface LocationMapping {
  /**
   * Calculates point location in the components primary display
   * object such as an image from a component relative location
   * correcting for objects location and/or filters
   * (ie flip, zoom and crop)
   *
   * @param pt	location in component
   * @return	location in original image with no filtering
   */
  public Point trueLocation(Point pt);
  /**
   * Calculates a components display location converting from
   * a components primary display objects such as an image relative location
   * correcting for objects location and/or filters (ie flip, zoom and crop)
   *
   * @param pt	location in original image with no filtering
   * @return	location in component
   */
  public Point displayLocation(Point pt);
}
