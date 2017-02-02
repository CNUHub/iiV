package iiv.io;
import java.io.*;
import java.awt.image.*;
/**
 * Reads ANALYZE formatted image files.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		AnalyzeHeader
 * @since	iiV1.0
 */
public class CNUAnalyzeImgFile extends CNUImgFile {
  private AnalyzeHeader ah;
  private ColorModel colorModel = null;
  /*
   * Constructs an instance of CNUAnalyzeImgFile.
   *
   * @param filename	the file to read from with or without ".img" prefix
   * @exception	IOException	thrown if error reading from file
   */
  public CNUAnalyzeImgFile(String filename) throws IOException {
    ah = new AnalyzeHeader(filename);
    setCNUDataConversions(ah.getCNUDataConversions());
    setDimensions(ah.getDimensions());
    setFileName(ah.getImgFileName());
    setFactor(ah.getFactor());
  }
  /*
   * Constructs an instance of CNUAnalyzeImgFile.
   *
   * @param filename	the file to read from with or without ".img" prefix
   * @param ignored	added for compatibility
   * @exception	IOException	thrown if error reading from file
   */
  public CNUAnalyzeImgFile(String filename, Object ignored)
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
   * Creates a string representation.
   *
   * @return	string representation
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(super.toString());
    if(ah != null) sb.append("\n").append(ah.toString());
    if(colorModel != null) sb.append("\n").append(colorModel.toString());
    return sb.toString();
  }
  /**
   * Reads and prints an ANALYZE header as a standalone java program.
   *
   * @param args	array of arguments from the command line
   */
  static public void main(String[] args) throws IOException {
    try {
      CNUAnalyzeImgFile img = new CNUAnalyzeImgFile(args[0]);
      System.out.println(img.toString());
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    System.exit(0);
  }
} // end CNUAnalyzeImgFile class
