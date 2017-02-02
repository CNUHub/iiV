package iiv.util;
/**
 * ShowStatus defines methods for displaying status messages.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.display.CNUDisplay
 * @see		iiv.dialog.StatusWindow
 * @since	iiV1.1
 */
public interface ShowStatus {
  /**
   * Displays a messages.
   *
   * @param s	message to display
   */
  public void showStatus(String s);
  /**
   * Displays the trace of a throwable object
   *
   * @param t	throwable object to display trace of
   */
  public void showStatus(Throwable t);
}
