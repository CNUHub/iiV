package iiv.data;
import iiv.script.*;
import java.lang.*;
import java.io.*;

/**
 * Interface that describes routines for converting locations into Talairach
 * space and some handy Talairach constants.
 *
 * @author Joel T. Lee
 * @version %I%, %G%
 * @see		LinearCoordinateMap
 * @since	iiV1.0
 */
public interface CoordinateMap extends iiVScriptable {
  /** Standard 1988 Talairach brain width in meters */
  public final static double TALAIRACH_WIDTH = (.136d);
  /** Standard 1988 Talairach brain height in meters */
  public final static double TALAIRACH_HEIGHT = (.118d);
  /** Standard 1988 Talairach brain length in meters */
  public final static double TALAIRACH_LENGTH = (.172d);
  /** Standard 1988 Talairach brain distance from front to AC
   *  in meters */
  public final static double TALAIRACH_FRONT_AC_LENGTH = (.070d);
  /** Standard 1988 Talairach brain AC to PC length in meters */
  public final static double TALAIRACH_ACPC_LENGTH = (.024d);
  /** Standard 1988 Talairach brain height of AC-PC plane */
  public final static double TALAIRACH_ACPC_HEIGHT = (.043d);
  // old dimensions from older atlas
  //final static double TALAIRACH_HEIGHT = (.121d);
  //final static double TALAIRACH_LENGTH = (.171d);
  //final static double TALAIRACH_FRONT_AC_LENGTH = (.071d);
  //final static double TALAIRACH_ACPC_LENGTH = (.025d);
  /** Standard 1988 Talairach distance from brain front to center between AC
   *  and PC in meters */
  public final static double TALAIRACH_ACPC_CENTER =
    (TALAIRACH_FRONT_AC_LENGTH + TALAIRACH_ACPC_LENGTH/2.0d);
  /** Constant specifying unknown units */
  public final static int UNKNOWN_UNITS = 0;
  /** Constant specifying pixel units */
  public final static int PIXELS = 1;
  /** Constant specifying meter units */
  public final static int METERS = 2;
  /** Constant specifying millimeter units */
  public final static int MILLIMETERS = 3;
  /** Constant specifying radian units */
  public final static int RADIANS = 20;
  /** Constant specifying degree units */
  public final static int DEGREES = 21;
  /** Constant for converting degrees to radians */
  public final static double DEGREES_2_RADIANS = Math.PI / 180.0d;
  /** Constant for converting radians to degrees */
  public final static double RADIANS_2_DEGREES = 180.0d / Math.PI;
  /**
   * Gets the name for this coordinate map.
   *
   * @return	base file name
   */
  public String getName();
  /**
   * Converts a point (voxel location) to the new coordinate space.
   *
   * @param pixelLocation	voxel location to convert
   * @param pixelResolution	distances between voxel centers
   *				may be <code>null</code>
   * @return			converted voxel location
   */
  public XYZDouble toSpace(XYZDouble pixelLocation,
    			   XYZDouble pixelResolution );
 /**
   * Inverts the toSpace conversion.  Converting new coordinate space
   * location to the original voxel space.
   *
   * @param location		cooridinate space location to convert
   * @param pixelResolution	distances between voxel centers
   *				may be <code>null</code>
   * @return			location in original space
   */
  public XYZDouble fromSpace(XYZDouble location,
			     XYZDouble pixelResolution);
}
