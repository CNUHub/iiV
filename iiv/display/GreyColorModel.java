package iiv.display;
import java.awt.image.*;
/**
 * GreyColorModel is a 256 index grey scale color model.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUColorModel
 * @since	iiV1.0
 */
public class GreyColorModel extends CNUColorModel {
  private static GreyColorModel solidVersion = null;
  private static GreyColorModel altTransparentVersion = null;
  private static byte[] greys = null;
  private static Object greyLock = new Object();
  /**
   * Creates a new instance of GreyColorModel.
   */
  private GreyColorModel() {
    super(greyBytes(), greyBytes(), greyBytes());
    setSaved(true);
  }
  /**
   * Creates a new instance of GreyColorModel with given transparent
   * color.
   *
   * @param trans	index to transparent color
   */
  private GreyColorModel(int trans) {
    super(greyBytes(), greyBytes(), greyBytes(), trans);
    setSaved(true);
  }
  /**
   * Gets a copy of the solid color map.
   *
   * @return	standard grey color map with no transparent color
   */
  public static GreyColorModel getGreyColorModel() {
    if(solidVersion == null) synchronized (greyLock) {
      if(solidVersion == null) solidVersion = new GreyColorModel();
    }
    return solidVersion;
  }
  /**
   * Gets a GreyColorModel with transparent color.
   *
   * @param	trans to transparent color
   * @return	grey color map with a transparent color
   */
  public static GreyColorModel getGreyColorModel( int trans ) {
    if(trans < 0) return getGreyColorModel();
    synchronized (greyLock) {
      if(altTransparentVersion != null) {
        if( trans == altTransparentVersion.getTransparentPixel() )
	  return altTransparentVersion;
      }
      altTransparentVersion = new GreyColorModel(trans);
      return altTransparentVersion;
    }
  }
  /**
   * Gets a copy of the color map with a given transparent color.
   */
  public CNUColorModel getTransparentColorModel( int trans ) {
    if(trans == this.getTransparentPixel()) return this;
    return getGreyColorModel(trans);
  }
  /**
   * Check if an object is associated with the same file.
   *
   * @param fileObject	object to check if associated with the same file
   * @return	<code>true</code> if object is an instance of GreyColorModel
   *		or is a String with the defaultColorMap name
   */
  public boolean sameFile( Object fileObject ) {
    if(fileObject instanceof GreyColorModel) return true;
    else if( fileObject instanceof String) {
      if(defaultColorMap.equals(fileObject)) return true;
    }
    return false;
  }
  /**
   * Gets the full name for this color model same as GetName.
   *
   * @return	name for this color model
   */
  public String getFullName() {
    return getName();
  }
  /**
   * Gets the name for this color model.
   *
   * @return	name for this color model
   */
  public String getName() {
    return defaultColorMap;
  }
  /**
   * Builds a 256 byte array with values equal to index.
   */
  private static byte[] greyBytes() {
    if(greys == null) synchronized (greyLock) {
      if(greys == null) {
        byte[] tmpgreys = new byte[256];
        for(int i = 0; i < 256; i++) tmpgreys[i]= (byte) i;
	greys = tmpgreys;
      }
    }
    return greys;
  }
}
