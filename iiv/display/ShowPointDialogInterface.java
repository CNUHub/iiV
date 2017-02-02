package iiv.display;
import iiv.script.*;
import java.awt.*;
/**
 * ShowPointDialogInterface defines methods from ShowPointDialog to allow
 * running with ShowPointDialog absent.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		ShowPointImage
 * @see		CNUDisplay
 * @since	iiV1.1
 */
public interface ShowPointDialogInterface {
  /**
   * Select all ShowPointDisplayLines.
   */
    public void selectAllLines();
  /**
   * Gets the number of ShowPointDisplayLines.
   *
   * @return number of ShowPointDisplayLines
   */
    public int getLineCount();
 /**
   * Sets select additions mode. If <code>true</code> further
   * added objects will be selected.
   *
   * @param selectAdditions	<code>true</code> to select
   *				or <code>false</code> to not select
   *				further additions
   */
    public void setSelectAdditions(boolean selectAdditions);
  /**
   * Adds a ShowPointDisplayLine to the selected list.
   *
   * @param spdl ShowPointDisplayLine to add
   */
    public void addSelection(ShowPointDisplayLine spdl);
  /**
   * Unselect all ShowPointDisplayLines.
   */
    public void clearAllSelections();
  /**
   * Query whether this container contains a ShowPointDisplayLine.
   *
   * @param spdl	ShowPointDisplayLine to check for
   * @return		<code>true</code> if spdl is contained
   */
    public boolean containsShowPointLine(ShowPointDisplayLine spdl);
  /**
   * Adds a range of line numbers to the selected list.
   *
   * @param start first show point display line number to add to list
   * @param stop  last show point display line number to add to list
   */
    public int addSelections(int start, int stop);
  /**
   * Removes selected show point lines.
   */
    public void removeShowPointLines();
  /**
   * Adds selected components to list of images to show point for.
   */
    public void addShowPointLines();
  /**
   * Adds a component to list of images to show point for.
   *
   * @param spdl	ShowPointDisplayLine to try adding.  If <code>null</code>
   *			a new line will be created.
   * @param spi		image to try adding a ShowPointDisplayLine for
   * @return ShowPointDisplayLine added or <code>null</code> if
   *	     image <code>null</code> or already in list
   */
  public ShowPointDisplayLine addShowPointLine(ShowPointImage spi,
					       ShowPointDisplayLine spdl,
					       Container container);
}
