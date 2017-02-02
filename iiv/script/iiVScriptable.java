package iiv.script;
/**
 * iiVScriptable defines routines to allow setting the slice
 * of data displayed by a component.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.display.CNUDisplay
 * @see		iiv.display.DisplayComponent
 * @see		iiv.display.SingleImg
 * @since	iiV1.132
 */
public interface iiVScriptable {
  /**
   * Creates a script that may be used to recreate this object
   * and store it in a script variable.
   *
   * @param scriptedObjects	contains objects with thier variable names
   *                            that are already scripted that will be used
   *                            for creating this or sub-objects from
   *                            (and/or adding to).  If an object or sub-object
   *                            is contained in scriptedObjects the variable
   *                            listed there will be referenced instead of
   *                            building the object from scratch.  If not
   *                            contained a new script
   *                            is generated with the object(s) and new
   *                            variable(s) added to scriptedObjects.  The
   *                            final object of the script is also left
   *                            in the status variable.
   *				if <code>null</code> final object only left
   *                            in status variable.
   * @return script to re-create this object
   */
  public String toScript(CNUScriptObjects scriptedObjects);
}
