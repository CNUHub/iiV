package iiv.display;
/**
 * Class to represent an exception generated reading a color map.
 *
 * @author	Joel T. Lee
 */
public class ColorMapException extends Exception{
  private static final long serialVersionUID = 8439102604918604570L;
  /**
   * Constructs a new ColorMapException.
   */
  public ColorMapException() { super(); }
  /**
   * Generates string form of exception.
   *
   * @param s	string value
   */
  public ColorMapException(String s) { super(s); }
};
