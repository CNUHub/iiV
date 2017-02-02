package iiv.io;
import iiv.data.*;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
/**
 * Reads NIFTI formatted image files.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		NiftiHeader
 * @since	iiV1.17a
 */
public class CNUNiftiImgFile extends CNUImgFile {
  private NiftiHeader nh;
  private ColorModel colorModel = null;
  private CNUScale cnuScale = null;
  private CoordinateMap coordinateMap = null;
  /*
   * Constructs an instance of CNUNiftiImgFile.
   *
   * @param filename	the file to read from with or without ".img" prefix
   * @exception	IOException	thrown if error reading from file
   */
  public CNUNiftiImgFile(String filename) throws IOException {
    nh = new NiftiHeader(filename);
    CNUDataConversions cdc = nh.getCNUDataConversions();
    if(nh.getNiftiType() == nh.DT_RGB24) {
      cdc.setIntConvert(cdc.RGB24_TO_INT);
      setColorModel(ColorModel.getRGBdefault());
      setScale(new CNUScale(1.0d));
    }
    setCNUDataConversions(cdc);
    setDimensions(nh.getDimensions());
    setFileName(nh.getImgFileName());
    setFactor(nh.getFactor());
    setSkipBytes(nh.getVoxelOffset());
    CoordinateMap coormap = nh.getQFormCoordinateMap();
    if(coormap == null) coormap = nh.getSFormCoordinateMap();
    setCoordinateMap(coormap);
  }
  /*
   * Constructs an instance of CNUNiftiImgFile.
   *
   * @param filename	the file to read from with or without ".img", ".hdr" or ".nii" prefix
   * @param ignored	added for compatibility
   * @exception	IOException	thrown if error reading from file
   */
  public CNUNiftiImgFile(String filename, Object ignored)
       throws IOException {
    this(filename);
  }
  /**
   * Sets the color model.
   *
   * @param cm	a new color model to use with this data
   *            or <code>null</code> to use standard default
   */
  public void setColorModel(ColorModel cm) { colorModel = cm; }
  /**
   * Gets the color model that should be used to display this data.
   *
   * @return	the color model or <null> if default should be used.
   */
  public ColorModel getColorModel() { return colorModel; }
  /**
   * Sets the scale object for converting voxel values to lookup table indices.
   *
   * @param sc	the scaling object
   */
  public void setScale(CNUScale sc) { cnuScale = sc; }
  /**
   * Gets the scale object that is used for converting voxel values to
   * lookup table indices.
   *
   * @return	the scaling object
   */
  public CNUScale getScale() { return cnuScale; }
  /**
   * Sets the coordinate mapping object that should be the default for this data.
   *
   * @param coorMap	coordinate mapping object
   */
  public void setCoordinateMap(CoordinateMap coorMap) {
    this.coordinateMap = coorMap;
  }
  /**
   * Gets the coordinate mapping object that should be the default for this data.
   *
   * @return	coordinate mapping object
   */
  public CoordinateMap getCoordinateMap() {
    return(coordinateMap);
  }
  /**
   * Creates a string representation.
   *
   * @return	string representation
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(super.toString());
    if(nh != null) sb.append("\n").append(nh.toString());
    if(coordinateMap != null) sb.append("\n").append(coordinateMap.toString());
    if(colorModel != null) sb.append("\n").append(colorModel.toString());
    return sb.toString();
  }
  /**
   * Reads and prints an NIFTI header as a standalone java program.
   *
   * @param args	array of arguments from the command line
   */
  static public void main(String[] args) throws IOException {
    try {
      CNUNiftiImgFile img = new CNUNiftiImgFile(args[0]);
      System.out.println(img.toString());
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    System.exit(0);
  }
} // end CNUNiftiImgFile class
