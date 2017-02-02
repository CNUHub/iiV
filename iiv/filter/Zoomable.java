package iiv.filter;
/**
 * Zoomable defines routines to allow handling zoom values
 * for a component.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.display.CNUDisplay
 * @see		iiv.display.DisplayComponent
 * @since	iiV1.132
 */
public interface Zoomable {
  /**
   * Sets independent horizontal and vertical zoom factors.
   *
   * @param zoomV		vertical zoom
   * @param zoomH		horizontal zoom
   */
  public void setZoom(double zoomV, double zoomH);
  /**
   * Gets the vertical zoom value.
   *
   * @return	vertical zoom
   */
  public double getZoomV();
  /**
   * Gets the horizontal zoom value.
   *
   * @return	horizontal zoom
   */
  public double getZoomH();
}
