package iiv.util;
import javax.swing.*;
import java.lang.reflect.*;
/**
 * This abstract class adds a variable to the runnable class for
 * returning objects.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.CNUViewer
 * @see		java.lang.Runnable
 * @since	iiV1.15a
 */
public abstract class RunnableWithReturnObject implements Runnable {
  public Object returnObject = null;
  /**
   * Run this runnable in the event dispatch thread catching
   * and the InterrupedException and InvocationTargetException.
   */
  public void invokeAndWait() {
    try {
      SwingUtilities.invokeAndWait(this);
    } catch (InterruptedException ie) {
      ie.printStackTrace();
    } catch (InvocationTargetException ite) {
      ite.printStackTrace();
    }
  }
}
