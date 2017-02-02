package iiv.io;
import iiv.script.*;
import java.io.*;

/**
 * Reads raw formatted image files.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUImgFile
 */
public class CNUEcat7ImgFile extends CNUImgFile {
  private CNUEcat7Header cnuEcat7Header = null;
  /**
   * Constructs an instance of CNUEcat7ImgFile with default values.
   *
   * @param filename	name of file to read image data from.
   * @exception  IOException thrown on error reading file or wrong file type
   */
  public CNUEcat7ImgFile(String filename) throws IOException {
    setFileName(filename);
    cnuEcat7Header = CNUEcat7Header.read(filename);
    setDimensions(cnuEcat7Header.getDimensions());
    setFactor(cnuEcat7Header.getQuantificationFactor());
    setSkipBytes(cnuEcat7Header.getDataOffset());
    setCNUDataConversions(cnuEcat7Header.getDataConversions());
  }
  /**
   * Constructs a new instance of CNUEcat7ImgFile with ignored object.
   *
   * @param filename	name of file to read image data from
   * @param ignored	ignored - allowed dynamic creation of extra parameter
   *			of CNUViewer object that is not needed
   * @exception  IOException thrown on error reading file or wrong file type
   */
  public CNUEcat7ImgFile(String filename, Object ignored) throws IOException {
    this(filename);
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
      variableName = scriptedObjects.addObject(this, "cnue7imgf");
      String fileName = CNUFile.quoteSlashes(getCNUFile().toString());
      sb.append(variableName).append(" = newFileObject \"").append(fileName);
      sb.append("\", \"").append(classname).append("\", \"").append(fileName).append("\");\n");
    }
    sb.append("script_rtn=").append(variableName).append(";\n");
    sb.append("// -- end ").append(classname).append(" script\n");
    return sb.toString();
  }
  /**
   * Creates a string representation of the object
   *
   * @return the string representation
   */
  public String toString() {
    String s = super.toString() + "\n";
    if( cnuEcat7Header != null ) s += cnuEcat7Header.toString() + "\n";
    else s += "null ECAT 7 header\n";
    return s;
  }
  /**
   * Reads and prints an ECAT7 header as a standalone java program.
   *
   * @param args	array of arguments from the command line
   */
  static public void main(String[] args) throws IOException {
    try {
      CNUEcat7ImgFile img = new CNUEcat7ImgFile(args[0]);
      System.out.println(img.toString());
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    System.exit(0);
  }
} // end CNUEcat7ImgFile class
