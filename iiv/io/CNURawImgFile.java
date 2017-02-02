package iiv.io;
import iiv.data.*;
import iiv.script.*;
import java.io.*;

/**
 * Reads raw formatted image files.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUImgFile
 */
public class CNURawImgFile extends CNUImgFile {
  static private Object defaultsLock = new Object();
  static private int tmpDims[] = {128, 128, 31, 1};
  static private CNUDimensions defaultDimensions =
    new CNUDimensions(tmpDims, CNUTypes.UNSIGNED_SHORT, 0);
  static private CNUDataConversions defaultCNUDataConversions = 
    new CNUDataConversions();
  static private double defaultQuantificationFactor = 1.0;
  static private long defaultSkipBytes = 0;

  /**
   * Constructs an instance of CNURawImgFile with default values.
   *
   * @param filename	name of file to read image data from.
   */
  public CNURawImgFile(String filename) throws IOException {
    this(filename, getDefaultDimensions(), getDefaultQuantificationFactor(),
	 getDefaultSkipBytes());
  }
  /**
   * Constructs a new instance of CNURawImgFile with ignored object.
   *
   * @param filename	name of file to read image data from
   * @param ignored	ignored - allowed dynamic creation of extra parameter
   *			of CNUViewer object that is not needed
   */
  public CNURawImgFile(String filename, Object ignored) throws IOException {
    this(filename);
  }
  /**
   * Constructs a new instance of CNURawImgFile.
   *
   * @param filename	name of file to read image data from
   * @param dims	dimensions of the image data
   * @param factor	quantification factor for data words
   * @param skipBytes	number of bytes to skip at beginning of file before
   *			finding the image data.
   */
  public CNURawImgFile(String filename, CNUDimensions dims, double factor,
		       long skipBytes)
	throws IOException {
    setFileName(filename);
    setDimensions(dims);
    setFactor(factor);
    setSkipBytes(skipBytes);
    setCNUDataConversions(getDefaultCNUDataConversions());
  }
  /**
   * Creates a script that may be used to recreate this display component.
   *
   * @param scriptedObjects scripted objects list to add this object to.
   * @return  the script
   */
  public String toScript(CNUScriptObjects scriptedObjects) {
    String classname = getClass().getName();
    StringBuffer sb = new StringBuffer("// -- start ").append(classname).append(" script\n");
    if(scriptedObjects == null) scriptedObjects = new CNUScriptObjects();
    String variableName = scriptedObjects.get(this);
    if(variableName == null) {
	variableName = scriptedObjects.addObject(this, "cnurawimgfile");
	sb.append(getDimensions().toScript(scriptedObjects));
	String fileName = CNUFile.quoteSlashes(getCNUFile().toString());
	sb.append(variableName).append(" = newFileObject(\"").append(fileName);
	sb.append("\", \"").append(classname).append("\", \"");
	sb.append(fileName).append("\", script_rtn, ");
	sb.append(getFactor()).append(", ");
	sb.append(getSkipBytes()).append(");\n");

	CNUDataConversions cnuDataConv = getCNUDataConversions();
	sb.append(cnuDataConv.toScript(scriptedObjects));
	sb.append(variableName);
	sb.append(".setCNUDataConversions(script_rtn);\n");
    }
    sb.append("script_rtn=").append(variableName).append(";\n");
    sb.append("// -- end ").append(classname).append(" script\n");
    return sb.toString();
  }
  /**
   * Creates a string representation of this object.
   *
   * @return the string representation
   */
  public String toString() {
    return super.toString();
  }
  /**
   * Creates a script to recreate the current defaults.
   *
   * @return the script
   */
  static public String defaultsToScript() {
    String classname=CNURawImgFile.class.getName();
    String script = "// -- start " + classname + " defaults script\n";
    synchronized (defaultsLock) {
      script += getDefaultDimensions().toScript(null);

      script += classname + ".setDefaultDimensions(script_rtn);\n";
      script += classname + ".setDefaultQuantificationFactor(" +
	         getDefaultQuantificationFactor() + ");\n";
      script += classname + ".setDefaultSkipBytes(" +
	         getDefaultSkipBytes() + ");\n";

      script += getDefaultCNUDataConversions().toScript(null);

      script += classname + ".setDefaultCNUDataConversions(script_rtn);\n";
    }
    script += "// -- end " + classname + " defaults script\n";
    return script;
  }
  /** Sets the default data conversions.
   *
   * @param cnuDataConv new default data conversions
   */
   static public void setDefaultCNUDataConversions(
     CNUDataConversions cnuDataConv) {
     synchronized (defaultsLock) {
	defaultCNUDataConversions = new CNUDataConversions(cnuDataConv);
     }
   }
  /**
   * Gets the default data conversions.
   *
   * @return the present default data conversions
   */
  static public CNUDataConversions getDefaultCNUDataConversions() {
    synchronized (defaultsLock) {
      return new CNUDataConversions(defaultCNUDataConversions);
    }
  }
  /** Sets the default dimensions.
   *
   * @param dimensions new default dimensions
   */
  static public void setDefaultDimensions(CNUDimensions dimensions) {
    if(dimensions != null) {
      synchronized (defaultsLock) {
        defaultDimensions = (CNUDimensions) dimensions.clone();
      }
    }
  }
  /**
   * Sets the default dimensions from integer values.
   *
   * @param xdim new default x dimenesion
   * @param ydim new default y dimenesion
   * @param zdim new default z dimenesion
   * @param idim new default i dimenesion
   */
  static public void setDefaultDimensions(int xdim, int ydim,
					  int zdim, int idim) {
    if(xdim > 0 && ydim > 0 && zdim > 0 && idim > 0) {
      synchronized (defaultsLock) {
	defaultDimensions.set4DValues(xdim, ydim, zdim, idim,
	  defaultDimensions.getType(), defaultDimensions.getOffset());
      }
    }
  }
  /**
   * Gets the default dimensions.
   *
   * @return the present default dimensions
   */
  static public CNUDimensions getDefaultDimensions() {
    synchronized (defaultsLock) {
      return (CNUDimensions) defaultDimensions.clone();
    }
  }
  /**
   * Sets the default data type.
   *
   * @param type	the new default data type
   */
  static public void setDefaultType(int type) {
    if( CNUTypes.valid(type) ) synchronized (defaultsLock) {
      defaultDimensions.setType(type);
    }
  }
  /**
   * Gets the current default data type.
   *
   * @return the default data type
   */
  static public int getDefaultType() {
    synchronized (defaultsLock) {
      return defaultDimensions.getType();
    }
  }
  /**
   * Sets the default data offset in words.
   *
   * @param offset	the new default data offset
   */
  static public void setDefaultOffset(int offset) {
    synchronized (defaultsLock) {
      defaultDimensions.setOffset(offset);
    }
  }
  /**
   * Gets the current default data offset in words.
   *
   * @return the default data offset
   */
  static public int getDefaultOffset() {
    synchronized (defaultsLock) {
      return defaultDimensions.getOffset();
    }
  }
  /**
   * Sets the default orientation.
   *
   * @param orientation the new default orientation
   */
  static public void setDefaultOrientation(int orientation) {
    synchronized (defaultsLock) {
      defaultDimensions.setOrientation(orientation);
    }
  }
  /**
   * Gets the current default orientation.
   *
   * @return the default orientation
   */
  static public int getDefaultOrientation() {
    synchronized (defaultsLock) {
      return defaultDimensions.getOrientation();
    }
  }
  /**
   * Sets the default orientation order from a string representation.
   *
   * @param orientationOrderString string representation of the
   * new default orientation order
   */
  static public void setDefaultOrientationOrder(String orientationOrderString) 
  {
    synchronized (defaultsLock) {
      defaultDimensions.setOrientationOrder(
	defaultDimensions.orientationOrderValueOf(orientationOrderString));
    }
  }
  /**
   * Sets the default orientation order from an integer constant.
   *
   * @param orientationOrder orientation order
   */
  static public void setDefaultOrientationOrder(int orientationOrder) {
    synchronized (defaultsLock) {
      defaultDimensions.setOrientationOrder(orientationOrder);
    }
  }
  /**
   * Gets the default orientation order.
   *
   * @return the default orientation order
   */
  static public int getDefaultOrientationOrder() {
    synchronized (defaultsLock) {
      return defaultDimensions.getOrientationOrder();
    }
  }
  /**
   * Sets the default quantification factor.
   *
   * @param quantificationFactor the new default quantification factor
   */
  static public void setDefaultQuantificationFactor(
    double quantificationFactor) {
    synchronized (defaultsLock) {
      defaultQuantificationFactor = quantificationFactor;
    }
  }
  /**
   * Gets the default quantification factor.
   *
   * @return the current default quantification factor
   */
  static public double getDefaultQuantificationFactor() {
    synchronized (defaultsLock) {
      return defaultQuantificationFactor;
    }
  }
  /**
   * Sets the default skip bytes.
   *
   * @param skipBytes the new default skip bytes
   */
  static public void setDefaultSkipBytes(long skipBytes) {
    synchronized (defaultsLock) {
      defaultSkipBytes = skipBytes;
    }
  }
  /**
   * Gets the default skip bytes.
   *
   * @return the current default skip bytes
   */
  static public long getDefaultSkipBytes() {
    synchronized (defaultsLock) {
      return defaultSkipBytes;
    }
  }
} // end CNURawImgFile class
