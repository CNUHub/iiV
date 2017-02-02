package iiv.script;
/**
 * Exception generated parsing a line of script parameters.
 *
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUDisplayScript
 * @since	iiV1.1
 */
public class LineParseException extends Exception {
  private static final long serialVersionUID = 2601838108513155111L;
  /**
   * Constructs a new LineParseException.
   */
  public LineParseException() { super(); }
  /**
   * Constructs a new LineParseException with a message.
   */
  public LineParseException(String s) { super(s); }
}
