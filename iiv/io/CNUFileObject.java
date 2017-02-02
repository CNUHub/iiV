package iiv.io;
/**
 * CNUFileObject defines routines to query wether an object
 * is associated with the same file as another object.  Often
 * the object to test is just a filename but it may be another
 * object that impliment CNUFileObject or an object recognized
 * only by the class implementing CNUFileObject.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.CNUViewer
 * @since	iiV1.0
 */
public interface CNUFileObject {
  /**
   * Searches for and returns an object associated with a file or
   * returns <code>null</code> if no object is found.
   *
   * @param sameFileAsObj	an object that implements CNUFileObject or
   *				a String containing the name of a file or
   *				any other object that might be recognized
   *				by this as associated with a file.
   * @return			Object associated with the sameFileAsObject
   *				or <code>null</code> if the object is not
   *				found
   */
  public Object getFileObject(Object sameFileAsObj);
}
