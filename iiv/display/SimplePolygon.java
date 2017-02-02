package iiv.display;
import java.awt.*;
/**
 * Class that defines a simple polygon for drawing.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.CNUViewer
 * @since	iiV1.1
 */
public class SimplePolygon {
  public final int[] xpoints;
  public final int[] ypoints;
  public final int npoints;
  /**
   * Creates a new instance of SimplePolygon with given number of vertices
   *
   * @param npoints	number of vertices
   */
  public SimplePolygon(int npoints) {
    this.npoints = npoints;
    xpoints = new int[npoints];
    ypoints = new int[npoints];
  }
  /**
   * Draws this polygon on a graphics device.
   *
   * @param g	graphics context to draw on
   */
  public void draw(Graphics g) {
    if(xpoints != null && ypoints != null)
      g.drawPolygon(xpoints, ypoints, npoints);
  }
}
