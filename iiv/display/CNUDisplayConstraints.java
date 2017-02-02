package iiv.display;
import java.awt.*;
/**
 * Defines contstraints passed to addImpl of CNUDisplay when adding components.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUDisplay
 * @see		Container
 * @since	iiV?
 */
public class CNUDisplayConstraints {
  private boolean undoRedoFlag = true;
  private boolean locationSet = false;
  private boolean select = false;
  private Point location = new Point(0, 0);
  /**
   * Constructs a new instance of this class.
   */
  public CNUDisplayConstraints() {}
  /**
   * Constructs a new instance of this class specifying a
   * layout location and choice of creating undo/redo history.
   *
   * @param location		container location for added component
   */
  public CNUDisplayConstraints(Point location) {
    if(location != null) {
      this.location.setLocation(location);
      locationSet = true;
    }
  }
  /**
   * Constructs a new instance of this class specifying a
   * layout location and choice of creating undo/redo history.
   *
   * @param location		container location for added component
   * @param undoRedoFlag	if <code>true</code> the add will be
   *				undoable
   */
  public CNUDisplayConstraints(Point location, boolean undoRedoFlag) {
    this(location);
    this.undoRedoFlag = undoRedoFlag;
  }
  /**
   * Constructs a new instance of this class specifying a
   * layout location and choice of creating undo/redo history.
   *
   * @param location		container location for added component
   * @param undoRedoFlag	if <code>true</code> the add will be
   *				undoable
   * @param select		if <code>true</code> the add will select
   *				this component.
   */
  public CNUDisplayConstraints(Point location, boolean undoRedoFlag,
			       boolean select) {
    this(location, undoRedoFlag);
    this.select = select;
  }
  /**
   * Gets undo/redo selection.
   *
   * @return	<code>true</code> if add should be undoable
   */
  public boolean getUndoRedoFlag() {return undoRedoFlag;};
  /**
   * Checks if location was set.
   *
   * @return	<code>true</code> if location was set.
   */
  public boolean getLocationSet() { return (locationSet); }
  /**
   * Gets the location.
   *
   * @return	the location
   */
  public Point getLocation() { return new Point(location); }
  /**
   * Checks if component is to be selected.
   *
   * @return	<code>true</code> if is to be selected
   */
  public boolean getSelect() { return (select); }
}
