package iiv.script;
/**
 * Exception to throw on error while contstructing an object.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUDisplayScript
 * @since	iiV1.1
 */
public class ObjectConstructionException extends Exception{
  private static final long serialVersionUID = 8112942216390890505L;
  /**
   * Constructs a new ObjectConstructionException.
   */
  public ObjectConstructionException() { super(); }
  /**
   * Constructs a new instance with a message string.
   *
   * @param s	message string
   */
  public ObjectConstructionException(String s) { super(s); }
};
