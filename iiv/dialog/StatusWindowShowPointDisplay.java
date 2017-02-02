package iiv.dialog;
import iiv.display.*;
import iiv.data.*;
import iiv.util.*;
/**
 * StatusWindowShowPointDisplay shows point info in a StatusWindow object.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		ShowPointDisplay
 * @since	iiV1.114b
 */
public class StatusWindowShowPointDisplay implements ShowPointDisplay {
  private ShowStatus showStatus;
  /**
   * Creates a new instance of StatusWindowShowPointDisplay.
   *
   * @param showStatus window to display in.
   */
   public StatusWindowShowPointDisplay(ShowStatus showStatus) {
     this.showStatus = showStatus;
   }
  /**
   * Displays information about a point.
   *
   * @param point	array of indices specifying the point location
   * @param value	value of voxel at point location
   * @param factor	quantification factor for voxel value
   * @param name	name of object source of the voxel
   * @return		<code>true</code> if info displayed,
   *			<code>false</code> otherwise
   * @see	ShowPointDialog
   */
  public boolean showPoint(int[] point, double value,
			   double factor, String name) {
    return showPoint(point, value, factor, name, null, null);
  }
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
   * @see	ShowPointDialog
   * @see	CoordinateMap
   */
  public boolean showPoint(int[] point, double value, double factor,
			   String name,
			   XYZDouble xyzpt, String mapName) {
    if(showStatus == null) return false;
    String s = name + " (";
    if(point == null) s += "null point";
    else if(point.length == 0) s += "length 0 point";
    else {
      s += Integer.toString(point[0]);
      for(int i=1; i < point.length; i++) s += ", " + Integer.toString(point[i]);
    }
    s +=") ";
    s += FormatTools.formatEngineering(value);
    s += " X" + FormatTools.formatEngineering(factor);
    s += "=" + FormatTools.formatEngineering(value*factor);
    s += " (";
    if(xyzpt == null) s += "no coordinate mapping";
    else {
      s += FormatTools.formatEngineering(xyzpt.x * 1e3)
	   + ((xyzpt.x < 0) ? "L" : "R") + ",";
      s += FormatTools.formatEngineering(xyzpt.y * 1e3)
	   + ((xyzpt.y < 0) ? "P" : "A") + ",";
      s += FormatTools.formatEngineering(xyzpt.z * 1e3)
	+ ((xyzpt.z < 0) ? "I" : "S");
    }
    s +=")";
    if(mapName == null) s += " no map name";
    else s += " " + mapName;
    showStatus.showStatus(s);
    return true;
  }
}
