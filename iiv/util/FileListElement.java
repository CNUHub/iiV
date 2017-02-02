package iiv.util;
import iiv.script.*;
/**
 * FileListElement stores file name and class pairs for use
 * as an element in a list.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.CNUViewer
 * @since	iiV1.15a
 */
public class FileListElement {
  private String fileName = null;
  private Class fileClass = null;
  /**
   * Constructs a new instance of FileListElement.
   *
   * @param fileName	name of file
   * @param fileClass	class associated with the file
   */
  public FileListElement(String fileName, Class fileClass) {
    this.fileName = fileName;
    this.fileClass = fileClass;
  }
  /**
   * Gets the file name.
   *
   * @return file name
   */
  public String getFileName() { return fileName; }
  /**
   * Gets the file class.
   *
   * @return file class
   */
  public Class getFileClass() { return fileClass; }
  /**
   * Compare to another object for equality.
   *
   * @return <code>true</code> if equal
   */
  public boolean equals(Object cmp) {
    if(! (cmp instanceof FileListElement)) return false;
    FileListElement fle = (FileListElement) cmp;
    if(fileName != null) {
      if(! fileName.equals(fle.fileName)) return false;
    }
    else if(fle.fileName != null) return false;
    if(fileClass != null) {
      if(! fileClass.equals(fle.fileClass)) return false;
    }
    else if(fle.fileClass != null) return false;
    return true;
  }
  /**
   * Generate a hash code that jives with equals.  Meaning 2 equal
   * equal objects generate the same hash code.
   *
   * @return			hash code
   */
  public int hashCode() {
      int hashcode = 0;
      if(fileName != null) hashcode += fileName.hashCode();
      if(fileClass != null) hashcode += fileClass.hashCode();
      return hashcode;
  }
  /**
   * Creates a string representation.
   *
   * @return string representation
   */
  public String toString() {
    StringBuffer s = new StringBuffer();
    s.append("[");
    s.append((fileClass == null)? "null" : fileClass.getName());
    s.append("]");
    if(fileName != null) s.append(fileName);
    return s.toString();
  }
  /**
   * Creates a new FileListElement based on a string representation.
   *
   * @param str string representation
   * @return FileListElement derived from the string representation
   * @exception IllegalArgumentException thrown on parsing error
   */
  public static FileListElement valueOf(String str)
    throws IllegalArgumentException {
    Class fileClass = null;
    String fileName=str.trim();
    if(fileName.startsWith("[")) {
      fileName = fileName.substring(1);
      int index = fileName.indexOf("]");
      if(index < 0) throw new IllegalArgumentException("missing end ]");
      String className = fileName.substring(0,index).trim();
      if("".equals(className)) throw new IllegalArgumentException("empty class name");
      try {
	fileClass = CNUDisplayScript.findClass(className);
      } catch (ClassNotFoundException cnf) {
	throw new IllegalArgumentException("Could not find class " +
				           className);
      }
      index++;
      if(index >= fileName.length())
        throw new IllegalArgumentException("missing file name");
      fileName = fileName.substring(index).trim();
      if("".equals(fileName)) throw new IllegalArgumentException("empty file name");
    }
    return new FileListElement(fileName, fileClass);
  }
}
