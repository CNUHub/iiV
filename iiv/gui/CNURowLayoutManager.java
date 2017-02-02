package iiv.gui;
import iiv.display.*;
import java.awt.*;
import java.util.*;
/**
 * This is a basic display container window for iiV.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.CNUViewer
 * @see		java.awt.Container
 * @since	iiV1.0
 */
public class CNURowLayoutManager implements LayoutManager2
{
  public static final CNUDisplayConstraints defaultConstraints =
    new CNUDisplayConstraints();

  private boolean valid = false;
  private int spacing = 5;
  // layout variables stored in a private class
  private class LayoutInfo implements Cloneable {
    private Dimension totalSize = new Dimension(0, 0);
    private int numberOfColumns = 1;
    private int nextCol = -1;
    private int rowBottom = 0;
    private Point nextLocation = new Point();
    private Hashtable<Component,Object> constrainedComponents = new Hashtable<Component,Object>();
    protected Object clone() {
      LayoutInfo li = new LayoutInfo();
      li.totalSize.setSize(totalSize);
      li.nextCol = nextCol;
      li.rowBottom = rowBottom;
      li.nextLocation.setLocation(nextLocation);
      //      li.constrainedComponents = (Hashtable) constrainedComponents.clone();
      li.constrainedComponents = new Hashtable<Component,Object>(constrainedComponents);
      return li;
    }
    public boolean equals(Object obj) {
      if(obj instanceof LayoutInfo) {
        LayoutInfo li = (LayoutInfo) obj;
        if(li.totalSize.equals(totalSize)) {
          if(li.nextCol == nextCol) {
	    if(li.rowBottom == rowBottom) {
	      if(li.nextLocation.equals(nextLocation)) {
		if(li.constrainedComponents.size() == constrainedComponents.size()) {
		  Enumeration e = li.constrainedComponents.keys();
		  while (e.hasMoreElements()) {
		    Object comp = e.nextElement();
		    if(li.constrainedComponents.get(comp) !=
		       constrainedComponents.get(comp)) return false;
		  }
		  return true;
		}
	      }
	    }
	  }
	}
      }
      return false;
    }
  }
  private LayoutInfo layoutInfo = new LayoutInfo();
  /** 
   * Constructs a new instance of this class with no images displayed.
   */
  public CNURowLayoutManager() {}
  /** 
   * Constructs a new instance of this class with no images displayed.
   *
   * @param numberOfColumns number of columns
   */
  public CNURowLayoutManager(int numberOfColumns) {
    layoutInfo.numberOfColumns = numberOfColumns;
  }
  /**
   * Adds a component to the layout manager's list of components with
   * no constraints as defined by old layout manager interface.
   *
   * @param name	ignored
   * @param comp	component to add to list
   */
  public void addLayoutComponent(String name, Component comp) {
    addLayoutComponent(comp, defaultConstraints);
  }
  /**
   * Adds a component to the layout manager's list of components.
   *
   * @param comp	component to add to list
   * @param constraints contstraints on component
   */
  public void addLayoutComponent(Component comp, Object constraints) {
    if(comp == null) return;
    synchronized (layoutInfo) {
      if(constraints instanceof CNUDisplayConstraints) {
        layoutInfo.constrainedComponents.put(comp, constraints);
      } else {
        layoutInfo.constrainedComponents.put(comp, defaultConstraints);
      }
    }
  }
  /**
   * Removes a component to the layout manager's list of components.
   *
   * @param comp	component to remove from list
   */
  public void removeLayoutComponent(Component comp) {
    if(comp == null) return;
    synchronized (layoutInfo) {
      layoutInfo.constrainedComponents.remove(comp);
    }
  }
  /**
   * Invalidates the layout manager.
   *
   * @param cont	non-null container using this layout
   */
  public void invalidateLayout(Container cont) {
    synchronized (layoutInfo) { valid = false; }
  }
  /**
   * Layout the container with the constrained components from the
   * hash list.
   *
   * @param cont	non-null container using this layout
   */
  public void layoutContainer(Container cont) {
    synchronized (cont.getTreeLock()) { synchronized (layoutInfo) {
      if(valid) return;
      Insets insets = cont.getInsets();
      // initialize the layout if first time through
      if(layoutInfo.nextCol < 0)
	resetLayout(insets.left, insets.top, 1, 0, cont);
      int totW = insets.left;
      int totH = insets.top;
      Component[] components = cont.getComponents();
      for(int i = 0; i < components.length; i++) {
	Component comp = components[i];
	if(comp.isVisible()) {
	  comp.validate(); // make sure component is validated
	  comp.setSize(comp.getPreferredSize());

	  CNUDisplayConstraints cnudc =
	    (CNUDisplayConstraints) layoutInfo.constrainedComponents.get(comp);
	  if(cnudc == null) cnudc = defaultConstraints;

	  if(cnudc.getLocationSet()) comp.setLocation(cnudc.getLocation());
	  else {
	    comp.setLocation(layoutInfo.nextLocation);
	    layoutInfo.constrainedComponents.put(comp,
	      new CNUDisplayConstraints(layoutInfo.nextLocation, false));
	    Dimension s = comp.getSize();
	    int newRowBottom = Math.max(layoutInfo.rowBottom,
				        layoutInfo.nextLocation.y + s.height);
	    int newNextCol = layoutInfo.nextCol + 1;
	    Point newNextLocation = new Point(layoutInfo.nextLocation.x,
					      layoutInfo.nextLocation.y);
	    if(newNextCol > layoutInfo.numberOfColumns) {
	      newNextCol = 1;
	      newNextLocation.x = insets.left;
	      newNextLocation.y = newRowBottom + spacing;
	      newRowBottom = 0;
	    }
	    else newNextLocation.x += s.width + spacing;
	    resetLayout(newNextLocation.x, newNextLocation.y,
		        newNextCol, newRowBottom, cont);
          }
          Rectangle r = components[i].getBounds();
          totW = Math.max(totW, r.x + r.width);
          totH = Math.max(totH, r.y + r.height);
	} // end if(comp.isVisible())
      } // end for(int i = 0; i < components.length ... 
      totW += insets.right;
      totH += insets.bottom;
      layoutInfo.totalSize.setSize(totW, totH);
      valid = true;
    } }
  }
  /**
   * Gets the x-alignment value which is ignored by this layout manager.
   *
   * @param cont	non-null container using this layout
   * @return	0.0 always
   */
  public float getLayoutAlignmentX(Container cont) { return 0; }
  /**
   * Gets the y-alignment value which is ignored by this layout manager.
   *
   * @return	0.0 always
   */
  public float getLayoutAlignmentY(Container cont) { return 0; }
  /**
   * Returns the preferred layout size with the current components
   *
   * @param cont	non-null container using this layout
   * @return	the preferred size
   */
  public Dimension preferredLayoutSize(Container cont) {
    synchronized (layoutInfo) {
      if(! valid) layoutContainer(cont);
      return new Dimension(layoutInfo.totalSize);
    }
  }
  /**
   * Returns the minimum layout size with the current components.
   *
   * @param cont	non-null container using this layout
   * @return	minimum layout size
   */
  public Dimension minimumLayoutSize(Container cont) {
    return preferredLayoutSize(cont);
  }
  /**
   * Returns the maximum layout size with the current components.
   *
   * @param cont	non-null container using this layout
   * @return	maximum layout size
   */
  public Dimension maximumLayoutSize(Container cont) {
    return preferredLayoutSize(cont);
  }
  /**
   * Gets an object that stores private info about the internal state
   * of this layout manager.  This can be used to reset the state
   * after an add or remove has been preformed.  Specifically designed
   * to allow a container to perform undo/redo since this manager component
   * positioning depends on the previous history of adds.
   *
   * @param cont	non-null container using this layout
   * @return	object describing the internal state of the layout manager
   */
  public Object getLayoutState(Container cont) {
    synchronized (layoutInfo) { return layoutInfo.clone(); }
  }
  /**
   * Sets the internal state of the manager from an object that was
   * generated by this layout manager.  If the object is the wrong type
   * or null the internal state is reset to its initial.
   *
   * @param obj		Object previously retrieved using getLayoutState
   * @param cont	non-null container using this layout
   */
  public void setLayoutState(Object obj, Container cont) {
    synchronized (layoutInfo) {
      if(obj instanceof LayoutInfo) {
        LayoutInfo li = (LayoutInfo) obj;
        layoutInfo.totalSize.setSize(li.totalSize);
        layoutInfo.numberOfColumns = li.numberOfColumns;
        layoutInfo.nextCol = li.nextCol;
        layoutInfo.rowBottom = li.rowBottom;
        layoutInfo.nextLocation.setLocation(li.nextLocation);
	//	layoutInfo.constrainedComponents = (Hashtable) li.constrainedComponents.clone();
	layoutInfo.constrainedComponents = new Hashtable<Component,Object>(li.constrainedComponents);
      }
      else {
        layoutInfo.totalSize.setSize(0, 0);
        layoutInfo.numberOfColumns = -1;
        layoutInfo.nextCol = -1;
        layoutInfo.rowBottom = 0;
        layoutInfo.nextLocation.setLocation(0, 0);
	layoutInfo.constrainedComponents.clear();
      }
      invalidateLayout(cont);
    }
  }
  /**
   * Sets the next layout position to a given location, the next column
   * to a given column and the current row bottom to a given position.
   *
   * @param newNextx	x pixel position to place left edge of next component
   * @param newNexty	y pixel position to place top edge of next component
   * @param newNextCol	column number next component will be positioned at
   *			negative values cause reset of all layout positions
   *			to defaults during layout
   * @param newRowBottom y pixel location of bottom edge of row
   * @param cont	non-null container using this layout
   */
  public void resetLayout(int newNextx, int newNexty, int newNextCol,
			  int newRowBottom, Container cont) {
    synchronized (layoutInfo) {
      layoutInfo.nextLocation.setLocation(newNextx, newNexty);
      layoutInfo.nextCol = newNextCol;
      layoutInfo.rowBottom = newRowBottom;
      invalidateLayout(cont);
    }
  }
  /**
   * Gets a copy of the current next location.
   *
   * @param cont	non-null container using this layout
   * @return	the next location
   */
  public Point getNextLocation(Container cont) {
    synchronized (layoutInfo) {
      return new Point(layoutInfo.nextLocation.x, layoutInfo.nextLocation.y);
    }
  }
  /**
   * Gets the height of the current row.
   *
   * @param cont	non-null container using this layout
   * @return	        the row height
   */
  public int getRowBottom(Container cont) { return layoutInfo.rowBottom; }
  /**
   * Gets the column number for the next added component.
   *
   * @param cont	non-null container using this layout
   * @return	        the next column
   */
  public int getNextColumn(Container cont) { return layoutInfo.nextCol; }
  /**
   * Sets the number of columns to display when adding new components or
   * relaying out existing components.
   *
   * @param numberOfColumns	number of columns
   * @param cont	non-null container using this layout
   */
  public void setNumberOfColumns(int numberOfColumns, Container cont) {
    synchronized (layoutInfo) {    
      layoutInfo.numberOfColumns = numberOfColumns;
    }
  }
  /**
   * Returns the number of columns.
   *
   * @param cont	non-null container using this layout
   * @return	number of columns
   */
  public int getNumberOfColumns(Container cont) {
    synchronized (layoutInfo) { return layoutInfo.numberOfColumns; }
  }
}  // end CNURowLayoutManager
