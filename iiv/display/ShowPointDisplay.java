package iiv.display;
import iiv.data.*;
/**
 * ShowPointDisplay defines methods for displaying values at a voxel location.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		ShowPointImage
 * @see		CNUDisplay
 * @since	iiV1.1
 */
public interface ShowPointDisplay {
  /**
   * Displays information about a point.
   *
   * @param point	array of indices specifying the point location
   * @param value	value of voxel at point location
   * @param factor	quantification factor for voxel value
   * @param name	name of object source of the voxel
   * @return		<code>true</code> if info displayed,
   *			<code>false</code> otherwise
   * @see	iiv.dialog.ShowPointDialog
   */
  public boolean showPoint(int[] point, double value,
			   double factor, String name);
  /**
   * Display information about a point including Talairach coordinates.
   *
   * @param point	array of indices specifying the point location
   * @param value	value of voxel at point location
   * @param factor	quantification factor for voxel value
   * @param name	name of object source of the voxel
   * @param xyzpt	point location in talairach coordinates
   * @param mapName	name of map used convert to talairach coordinates
   * @return		<code>true</code> if info displayed,
   *			<code>false</code> otherwise
   * @see	iiv.dialog.ShowPointDialog
   * @see	CoordinateMap
   */
  public boolean showPoint(int[] point, double value, double factor,
			   String name,
			   XYZDouble xyzpt, String mapName);

}
