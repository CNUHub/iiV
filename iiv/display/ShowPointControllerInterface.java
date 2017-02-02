package iiv.display;
import iiv.script.*;
import iiv.gui.*;
import java.awt.*;
/**
 * ShowPointControllerInterface defines methods from ShowPointDialog to allow
 * running with ShowPointDialog absent.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		ShowPointImage
 * @see		CNUDisplay
 * @since	iiV1.1
 */
public interface ShowPointControllerInterface extends ShowPointTracker
{
  /**
   * Sets whether the main display selected show point display lines
   * are frozen.
   *
   * @param freeze if <code>true</code> selected lines will be
   *              frozen.
   */
  public void setMainShowPointDisplayLinesFreezeState(boolean freeze);
  /**
   * Sets whether the main display selected show point display lines record.
   *
   * @param state if <code>true</code> selected lines will be
   *              set to record.
   */
  public void setMainShowPointDisplayLinesRecordState(boolean state);
  /**
   * Adds a show point display and show point image as a pair
   * that show points and possibly creates undo/redo history for
   * the add.
   *
   * @param spd	ShowPointDisplay to try adding.
   * @param spi	image to associate with spd
   * @param undoFlag if <code>true</code> creates undo/redo history
   */
  public void addShowPointPair(final ShowPointDisplay spd,
			       final ShowPointImage spi,
			       final boolean undoFlag);
  /**
   * Removes a show point display and show point image pair
   * from list that shows points with possible undo/redo.
   *
   * @param spd	ShowPointDisplay to try removing.
   * @param undoFlag if <code>true</code> creates undo/redo history
   */
  public void removeShowPointPair(final ShowPointDisplay spd,
				  final boolean undoFlag);
  /**
   * Sets whether the selected ShowPointLines should be frozen.
   *
   * @param freeze	<code>true</code> to freeze selected ShowPointLines
   */
  public void setLinesFreeze(boolean freeze);
  /**
   * Sets whether the selected ShowPointLines should record points.
   *
   * @param record	<code>true</code> to record points of selected ShowPointLines
   */
  public void setLinesRecord(boolean record);
  /**
   * Sets the crosshair color.
   *
   * @param color crosshair color
   */
    public void setCrosshairColor(Color color);
  /**
   * Sets whether the crosshair should be visible while showing points.
   *
   * @param show	<code>true</code> to show crosshairs during showpoint
   */
    public void setCrosshairState(boolean show);
  /**
   * Sets whether the crosshair should be visible for selected ShowPointLines.
   *
   * @param show	<code>true</code> to show crosshairs during showpoint
   */
    public void setLinesCrosshairState(boolean show);
  /**
   * Sets whether the selected ShowPointLines should track the current point.
   *
   * @param track	<code>true</code> to track current point
   */
  public void setLinesTrackingState(boolean track);
  /**
   * Sets whether the to record points from the showpoint source.
   *
   * @param record	<code>true</code> to record during showpoint
   */
    public void setRecordState(boolean record);
  /**
   * Adds currently selected components to list of crosshair trackers.
   */
    public void addCrosshairTrackers();
  /**
   * Adds selected components to list of images to show point
   * over display for.
   */
    public void addShowPointLinesToDisplay();
  /**
   * Removes currently selected components from list of crosshair trackers.
   */
    public void removeCrosshairTrackers();
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
  /**
   * Adds selected components to list of slice trackers.
   */
    public void addSliceTrackers();
  /**
   * Removes selected components from list of slice trackers.
   */
    public void removeSliceTrackers();
  /**
   * Removes currently selected components from list of crosshair trackers
   * and clears their displayed crosshairs.
   */
    public void deleteCrosshairs();
  /**
   * Gets whether show point lines track mapped location or point locations.
   *
   * @return mapTracking	<code>true</code> if lines track mapped location
   */
  public boolean getMapTracking();
  /**
   * Gets selection action that chooses whether show point lines
   * track mapped location or point locations.
   *
   * @return map tracking action
   */
  public EasyAddAbstractAction getMapTrackingAction();
  /**
   * Sets a components crosshair and displays the value in a
   * ShowPointDialogLine while creating an undo/redo history.
   *
   * @param spi		ShowPointImage to set crosshair for.
   *			If <code>null</code> determines from spdl or returns
   * @param spd	ShowPointDisplay to set values of
   *			If <code>null</code> determines from spi or ignores
   * @param newIndices	Indices to set crosshair to
   * @param newColor	Color to set crosshair to
   * @param undoFlag	<code>true</code> to create undo/redo history
   */
  public void setComponentCrosshair(ShowPointImage spi,
				    ShowPointDisplay spd,
				    int[] newIndices, Color newColor,
				    boolean undoFlag);
}
