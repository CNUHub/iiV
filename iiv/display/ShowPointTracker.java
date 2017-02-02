package iiv.display;
import iiv.script.*;
import java.awt.*;
/**
 * ShowPointTracker defines methods for tracking values at a voxel location.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		ShowPointImage
 * @see		CNUDisplay
 * @since	iiV1.1
 */
public interface ShowPointTracker {
  /**
   * Stops trackers for given components and returns an object
   * that can be used to restart the trackers.
   *
   * @return object that can be used to restart trackers
   */
  public Object stopComponentTrackers(Component[] components);
  /**
   * Restarts trackers for components that where stopped
   * using stopComponentTrackers.
   *
   * @param obj object from stopComponentTrackers
   */
  public void restartComponentTrackers(Object obj);
  /**
   * Queries whether a SliceNumbering is in the slice trackers
   * list.
   *
   * @param tracker SliceNumbering to check list for
   * @return <code>true</code> if tracker is in list otherwise <code>false</code>
   */
  public boolean isSliceTracker(SliceNumbering tracker);
  /**
   * Adds a component to list of images that tracks slice positions
   * with undo/redo.
   *
   * @param tracker slice tracker to add to list
   */
  public void addSliceTracker(SliceNumbering tracker);
 /**
   * Tracks and/or shows the point for all current tracking images.
   *
   * @param spi		primary ShowPointImage to display point for
   * @param point	array of indices specifying the point location
   */
  public void trackPoint(ShowPointImage spi, int[] point);
  /**
   * Sets whether show point lines track mapped location or point locations.
   *
   * @param mapTracking	<code>true</code> if lines track mapped location
   */
  public void setMapTracking(boolean mapTracking);
  /**
   * Queries whether a ShowPointImage is in the crosshair trackers
   * list.
   *
   * @param spi ShowPointImage to check list for
   * @return <code>true</code> if spi is in list otherwise <code>false</code>
   */
  public boolean isCrosshairTracker(ShowPointImage spi);
  /**
   * Adds an array of objects to list of crosshair trackers.
   *
   * @param objects array of possible ShowPointImages to try adding
   */
  public void addCrosshairTrackers(Object[] objects);
  /**
   * Adds a component to list of images that listens for crosshair positions
   * with undo/redo.
   * @param spi ShowPointImage to add to list
   */
  public void addCrosshairTracker(ShowPointImage spi);
  /**
   * Sets the crosshair for current crosshair tracking images. 
   *
   * @param spi		primary ShowPointImage to set crosshair for
   * @param point	array of indices specifying the point location
   */
  public void setCrosshairs(ShowPointImage spi, int[] point);
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
  /**
   * Sets the showPoint values to crosshair values
   * for all currently listed images.
   */
  public void syncToCrosshairs();
}
