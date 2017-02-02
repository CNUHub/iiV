package iiv.io;
import iiv.display.DisplayComponent;
import iiv.script.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
/**
 * CNUStdImgFile creates a displayable object from standard 2d image formats
 * supported by CNUFile.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUFile
 * @see		DisplayComponent
 * @since	iiV1.0
 */
public class CNUStdImgFile extends DisplayComponent
implements iiVScriptable, CNUFileObject, Cloneable {
  private static final long serialVersionUID = 8292167925620831449L;
  private CNUFile cnufile = null;
  private Object cnufileLock = new Object();

  /**
   * Constructs a new instance of CNUStdImgFile.
   */
  public CNUStdImgFile() {
    // color model not set comes from image
    initColorModel(null);
    setGrabColorModelState(true);
  }
  /**
   * Constructs a new instance of CNUStdImgFile with a given filename.
   *
   * @param filename	name of file to read image from
   * @param parent	ignored - allows standard construction
   */
  public CNUStdImgFile(String filename, Component parent) {
    this();
    //    setParent(parent);
    setFileName(filename);
  }
  /**
   * Constructs a new instance of CNUStdImgFile with a given filename.
   *
   * @param filename	name of file to read image from
   */
  public CNUStdImgFile(String filename) {
    this();
    setFileName(filename);
  }
  /**
   * Creates a duplicate of this DisplayComponent for displaying the same
   * image more then once.
   */
  public Object clone() {
    CNUStdImgFile csif = new CNUStdImgFile();
    csif.setFileName(getCNUFile());
    csif.setImage(getImage());
    return csif;
  }
  /**
   * Sets the file name.
   *
   * @param filename	filename to read image from
   */
  public void setFileName(String filename) {
    setFileName(new CNUFile(filename));
  }
  /**
   * Sets the file name based on a CNUFile object.
   *
   * @param cnufile	CNUFile object to read image with
   */
  public void setFileName(CNUFile cnufile) {
    synchronized (cnufileLock) {
      this.cnufile = cnufile;
    }
  }
  /**
   * Gets the CNUFile object.
   *
   * @return	file object this component uses to read the image form
   */
  public CNUFile getCNUFile() {
    synchronized (cnufileLock) {
      return cnufile;
    }
  }
  /**
   * Gets the base name of the image file.
   *
   * @return	base name of image file with no path information
   */
  public String getName() {
    synchronized (cnufileLock) {
      if(cnufile != null)return cnufile.getName();
      else return null;
    }
  }
  /**
   * Determines out if an object represents the same file.
   *
   * @param fileObject	object that represents a file either by implementing
   *			CNUFileObject or as a filename string
   * @return		<code>true</code> if this CNUStdImgFile represents the
   *			same file.
   */
  public boolean sameFile( Object fileObject ) {
    CNUFile localcnufile = getCNUFile();
    if(localcnufile == null) return false;
    else if( fileObject instanceof CNUStdImgFile )
      return ((CNUStdImgFile) fileObject).sameFile(localcnufile);
    else return localcnufile.sameFile(fileObject);
  }
  /**
   * Gets this object if it represents the same file as sameFileAsObj
   *
   * @param sameFileAsObj	object that represents a file either
   *				by implementing CNUFileObject or
   *				as a filename string
   * @return		this object if it represents the same file,
   *			otherwise <code>null</code>
   */
  public Object getFileObject(Object sameFileAsObj) {
    if(sameFile( sameFileAsObj )) return this;
    else return super.getFileObject(sameFileAsObj);
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
      variableName = scriptedObjects.addObject(this, "cnustdimgfile");
      String filename = CNUFile.quoteSlashes(getCNUFile().toString());
      sb.append(variableName).append(" = newFileObject \"").append(filename);
      sb.append("\", \"").append(classname).append("\", \"");
      sb.append(filename).append("\", CNUDISPLAY);\n");
      sb.append(postObjectToScript(scriptedObjects));
    }
    sb.append("script_rtn=").append(variableName).append(";\n");
    sb.append("// -- end ").append(classname).append(" script\n");
    return sb.toString();
  }
  /**
   * Creates a string representation of this object.
   *
   * @return	string representation
   */
  public String toString() {
    String s = super.toString() + "\n";
    CNUFile localcnufile = getCNUFile();
    if( localcnufile != null ) s += localcnufile.toString() + "\n";
    return s;
  }
  /**
   * Overrides updateFilters to ensure image read.
   */
  public void updateFilters() {
    if(getImage() == null) try {
      readImage();
    } catch (IOException ioe) {
      showStatus(ioe);
    }
    super.updateFilters();
  }
  /**
   * Reads and sets the image.
   *
   * @exception IOException	thrown on errors reading from file.
   */
  public void readImage() throws IOException {
    CNUFile localcnufile = getCNUFile();
    Image image = null;
    if(localcnufile != null) {
      image = localcnufile.getImage();
      // completely load to make sure image is valid
      if(image != null) if( ! track(image) ) image = null;

      //if(image == null) {
      //   image = localcnufile.getPpmImage();
      // completely load to make sure image is valid
      //   if(image != null) if( ! track(image) ) image = null;
      //}
      setImage(image);
      if(image == null)
	throw new IOException("Error reading image from file " +
				localcnufile.getName());
    }
  }
  /**
   * Gets the value at a given point.
   *
   * @param indices	indices ignored
   * @return		always returns 0
   */
  public double getValue(int[] indices) { return 0; }
}
