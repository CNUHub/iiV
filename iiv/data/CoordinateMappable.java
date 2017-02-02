package iiv.data;
/**
 * CoordinateMappable defines routines to allow handling coordinate
 * maps for a component.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CoordinateMap
 * @see		iiv.display.CNUDisplay
 * @since	iiV1.132
 */
public interface CoordinateMappable {
  /**
   * Sets the coordinate map.
   *
   * @param coorMap	coordinate map
   */
  public void setCoordinateMap(CoordinateMap coorMap);
  /**
   * Gets the coordinate map.
   *
   * @return	the coordinate map
   */
  public CoordinateMap getCoordinateMap();
  /**
   * Gets the coordinate resolutions.
   *
   * @return the coordinate resolutions
   */
  public XYZDouble getCoordinateResolutions();
}
