package iiv.display;
import iiv.*;
import iiv.util.*;
import iiv.gui.*;
import iiv.io.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.util.Enumeration;
import java.util.Vector;
import java.lang.reflect.*;
/**
 * This is a basic display container window for iiV.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUViewer
 * @see		java.awt.Container
 * @since	iiV1.0
 */
public class CNUContainer extends Container
implements MouseListener, MouseMotionListener, ActionListener
{
  private static final long serialVersionUID = 1372126794737688679L;
  // popup menus
  private PopupMenu overComponentPM = new PopupMenu("Image");
  private MenuItem infoMI = new MenuItem("info");
  private MenuItem deleteMI = new MenuItem("delete");
  private MenuItem selectAllMI = new MenuItem("select all");

  private CNUViewer cnuvPtr = null;

  private UndoRedo undoRedo = null;

  public final static int EVENT_RELATIVE_TO_COMPONENT_ON_SCREEN = 0;
  public final static int EVENT_RELATIVE_TO_START_COMPONENT_ON_SCREEN = 1;
  // location component was at beginning of drag
  private Point saveEventComponentOrigin = new Point();
  // mode for calculating absolute location of drag
  private int dragEventPositioningMode = EVENT_RELATIVE_TO_START_COMPONENT_ON_SCREEN;

  private boolean selectAdditions = false;
  private Color selectColor = Color.blue;
  private Vector<Component> selectedObjects = new Vector<Component>(4, 4);

  private Component currentComp = null;
  private Point selectPoint = null;
  private Rectangle selectBox = null;


  /** 
   * Constructs a new instance of this class with no image displayed.
   */
  public CNUContainer() {
    setLayout(new CNURowLayoutManager());
    overComponentPM.add(infoMI);
    infoMI.addActionListener(this);
    overComponentPM.add(selectAllMI);
    selectAllMI.addActionListener(this);
    overComponentPM.add(deleteMI);
    deleteMI.addActionListener(this);
    add(overComponentPM);
    addMouseListener(this);
    addMouseMotionListener(this);
  }
  /**
   * Allows transversing of focus. Canvas don't normally transverse focus
   *
   * @return	<code>true</code> to all focus transversing
   */
  public boolean isFocusTraversable() { return true; }
  /**
   * Gets the first component currently selected.
   *
   * @return	current selected component or <code>null</code>
   */
  public Component getCurrentComponent() {
    // return current selection
    Component comp = currentComp;
    if(comp != null) return comp;
    synchronized (getTreeLock()) {
      if(! selectedObjects.isEmpty()) {
        comp = selectedObjects.firstElement();
	return comp;
      }
    }
    return null;
  }
  /**
   * Gets the font of the currently selected object.
   *
   * @return	font of selected object or default font
   */
  public Font getCurrentFont() {
    Font f = null;
    Component comp = getCurrentComponent();
    if(comp != null) f = comp.getFont();
    if(f != null) return f;
    // defaults to standard getFont()
    return getFont();
  }
  /**
   * Gets the foreground color of the currently selected object or the
   * default foreground color.
   *
   * @return a color object
   */
  public Color getCurrentForeground() {
    Component comp = getCurrentComponent();
    if(comp != null) return comp.getForeground();
    // defaults to foreground color()
    return getForeground();
  }
  /**
   * Sets the number of columns to display when adding new components or
   * relaying out existing components.
   *
   * @param numberOfColumns	number of columns
   */
  public void setNumberOfColumns(int numberOfColumns) {
    LayoutManager lm = getLayout();
    if(lm instanceof CNURowLayoutManager)
      ((CNURowLayoutManager) lm).setNumberOfColumns(numberOfColumns, this);
  }
  /**
   * Returns the number of columns.
   *
   * @return	number of columns
   */
  public int getNumberOfColumns() {
    LayoutManager lm = getLayout();
    if(lm instanceof CNURowLayoutManager)
      return ((CNURowLayoutManager) lm).getNumberOfColumns(this);
    return -1;
  }
  /**
   * Sets the control panel.
   *
   * @param cnuv the CNUViewer or <code>null</code>
   */
  public void setCNUViewer(CNUViewer cnuv) { cnuvPtr = cnuv; }
  /**
   * Gets the control panel.
   *
   * @return the CNUViewer or <code>null</code>
   */
  public CNUViewer getCNUViewer() { return cnuvPtr; }
  /**
   * Sets the undo/redo object
   *
   * @param ur the UndoRedo object or <code>null</code>
   */
  public void setUndoRedo(UndoRedo ur) { this.undoRedo = ur; }
  /**
   * Gets the undo/redo object
   *
   * @return the UndoRedo object or <code>null</code>
   */
  public UndoRedo getUndoRedo() { return undoRedo; }
  /**
   * Gets the parent who is an instance of Frame.
   *
   * @return	parent frame or <code>null</code>
   */
  public Frame getParentFrame() {
    synchronized (getTreeLock()) {
      Component c = this;
      while( !(c instanceof Frame) ) {
        c = c.getParent();
        if(c == null)break;
      }
      return (Frame) c;
    }
  }
  /**
   * Gets the parent who is an instance of ScrollPane.
   *
   * @return	parent ScrollPane or <code>null</code>
   */
  public ScrollPane getParentScrollPane() {
    synchronized (getTreeLock()) {
      Component c = this;
      while( !(c instanceof ScrollPane) ) {
        c = c.getParent();
        if(c == null) break;
      }
      return (ScrollPane) c;
    }
  }
  /**
   * Returns the first object associated with a file object from the
   * the component list.
   *
   * @return	object associated with the file object or <code>null</code>
   */
  public Object getFileObject(Object sameFileObj) {
    synchronized (getTreeLock()) {
      int ncmps = getComponentCount();
      for(int i = 0; i < ncmps; i++) {
        Component comp = getComponent(i);
	if(comp instanceof CNUFileObject) {
	  Object obj = ((CNUFileObject) comp).getFileObject(sameFileObj);
	  if(obj != null) return obj;
	}
      }
    }
    return null;
  }
  /**
   * Gets currently selected components ordered by display level.
   *
   * @return	array of sorted selected object
   */
  public Component[] getSelectedComponentsOrdered() {
    Component[] selectedComponents = null;
    synchronized (getTreeLock()) {
      if( ! selectedObjects.isEmpty() ) {
	int nSelected = selectedObjects.size();
        selectedComponents = new Component[nSelected];
        int nComponents = getComponentCount();
	int j = 0;
        for(int i=0; (i < nComponents) && (j < nSelected); i++) {
	  Component comp = getComponent(i);
	  if(selectedObjects.contains(comp)) selectedComponents[j++] = comp;
        }
      }
    }
    return selectedComponents;
  }
  /**
   * Adds a component for displaying and repaints its bounds.
   *
   * @param dc	component to add
   */
  public void addAndRepaint(Component dc) {
    add(dc);
    Rectangle repaintArea = dc.getBounds();
    if(getSelectAdditions()) repaintArea.grow(1, 1);
    repaint(repaintArea.x, repaintArea.y,
	    repaintArea.width, repaintArea.height);
  }
  /**
   * Returns <code>true</code> if this display contains the Component.
   *
   * @param comp	component to check for
   * @return	<code>true</code> if this display contains comp
   */
  public boolean contains(Object comp) {
    if(! (comp instanceof Component)) return false;    
    synchronized(getTreeLock()) {
      return isAncestorOf((Component) comp);
    }
  }
  /**
   * Sets select additions mode. If <code>true</code> further
   * added objects will be selected.
   *
   * @param selectAdditions	<code>true</code> to select
   *				or <code>false</code> to not select
   *				further additions
   */
  public void setSelectAdditions(boolean selectAdditions) {
    this.selectAdditions = selectAdditions;
  }
  /**
   * Checks if added objects are being automaticly selected.
   *
   * @return	<code>true</code> if additions are selected
   *		or <code>false</code> if additions are not selected
   */
  public boolean getSelectAdditions() { return isSelectAdditions(); }
  /**
   * Checks if added objects are being automaticly selected.
   *
   * @return	<code>true</code> if additions are selected
   *		or <code>false</code> if additions are not selected
   */
  public boolean isSelectAdditions() { return selectAdditions; }
  /**
   * Overides add implemenation to allow special processing.
   *
   * @param comp	Component to add
   * @param constraints	CNUDisplayConstraints or <code>null</code>
   * @param pos		where in list components to insert this one
   * @see	java.awt.Container
   */
  protected void addImpl(Component comp, Object constraints, int pos) {
    // do list synchronize should always occur before component synchronize
    // since undo and redo will always perform syncs in that order
    UndoRedo ur = getUndoRedo();
    Object outerLock;
    if(ur == null) outerLock = getTreeLock();
    else outerLock = ur.getDoListLock();
    synchronized (outerLock) {
      synchronized (getTreeLock()) {
	// make sure components foreground color is explicitly set before
	// call to super.addImpl otherwise components always get foreground
	// color from this container
	if(comp.getForeground() == null) comp.setForeground(getForeground());
	boolean select = getSelectAdditions();
	if(constraints instanceof CNUDisplayConstraints) {
	  CNUDisplayConstraints cnudconstraints =
	    (CNUDisplayConstraints) constraints;
	  if(! cnudconstraints.getUndoRedoFlag()) ur = null;
	  select = cnudconstraints.getSelect();
	}
	Object layoutState = null;
	LayoutManager lm = null;
	if(ur != null) {
	  lm = getLayout();
	  if(lm instanceof CNURowLayoutManager)
	    layoutState = ((CNURowLayoutManager) lm).getLayoutState(this);
        }
        super.addImpl(comp, null, pos);
	if(select) addSelection(comp);
	if(ur != null) {
	  ur.startSteps();
	  Class[] undoParams = { java.awt.Component.class, UndoRedo.class };
	  Object[] undoArgs = { (Object) comp, null };
	  DoCommand undo = new DoCommand(this, "remove", undoParams, undoArgs);
	  Class[] redoParams = { java.awt.Component.class,
				 java.lang.Object.class,
				 Integer.TYPE};
	  Object[] redoArgs = { (Object) comp,
	    new CNUDisplayConstraints(comp.getLocation(), false),
				      new Integer(pos) };
	  DoCommand redo = new DoCommand(this, "add", redoParams,
					 redoArgs);
	  ur.addUndo(undo, redo, comp, "add");
	  if(layoutState != null) {
	    Object newLayoutState =
	      ((CNURowLayoutManager) lm).getLayoutState(this);
	    if( ! newLayoutState.equals(layoutState)) {
	      undoParams = new Class[] { Object.class, Container.class };
	      undoArgs = new Object[] { layoutState, this };
	      undo = new DoCommand(lm, "setLayoutState", undoParams, undoArgs);
	      redoParams = undoParams;
	      redoArgs = new Object[] { newLayoutState, this };
	      redo = new DoCommand(lm, "setLayoutState", redoParams, redoArgs);
	      ur.addUndo(undo, redo, comp, "reset layout");
	    }
	  }
	  ur.finishUndoSteps("add");
	}
      }
    }
  }
  /**
   * Remove a component from the display with undo commands.
   *
   * @param comp	Component to remove
   */
  public void remove(Component comp) {
    remove(comp, getUndoRedo());
  }
  /**
   * Remove a component with undo specified by a flag.
   *
   * @param comp	Component to remove
   * @param ur		if not <code>null</code> undo commands will be generated
   */
  public void remove(Component comp, UndoRedo ur) {
    // do list synchronize should always occur before component synchronize
    // since undo and redo will always perform syncs in that order
    
    Object outerLock;
    if(ur == null) outerLock = getTreeLock();
    else outerLock = ur.getDoListLock();
    synchronized (outerLock) {
      synchronized (getTreeLock()) {
      if( contains(comp) ) {
	Rectangle compBounds = comp.getBounds();
	int level = getDisplayComponentLevel(comp);
	boolean select = selectedObjects.contains(comp);
        if(select) clearSelection(comp);
	super.remove(comp);
	invalidate();
        repaint(compBounds.x, compBounds.y,
		compBounds.width, compBounds.height);
	if(ur != null) {
	  Class[] undoParams = { java.awt.Component.class,
				 java.lang.Object.class,
				 Integer.TYPE};
	  Object[] undoArgs = { (Object) comp,
	    new CNUDisplayConstraints(compBounds.getLocation(), false, select),
	    new Integer(level) };
	  DoCommand undo = new DoCommand(this, "add", undoParams, undoArgs);
          Class[] redoParams = { java.awt.Component.class, UndoRedo.class };
	  Object[] redoArgs = {(Object) comp, null };
	  DoCommand redo = new DoCommand(this, "remove", redoParams, redoArgs);
	  ur.addUndo(undo, redo, comp, "remove");
	}
      }
    } }
  }
  /**
   * Remove all components currently selected.
   */
  public void removeSelections() {
    // Note - cannot utilize apply because this rearranges componentList
    // do list synchronize should always occur before component synchronize
    // since undo and redo will always perform syncs in that order
    UndoRedo ur = getUndoRedo();
    Object outerLock;
    if(ur == null) outerLock = getTreeLock();
    else outerLock = ur.getDoListLock();
    synchronized (outerLock) {
      synchronized (getTreeLock()) {
        if(! selectedObjects.isEmpty() ) {
	  if(ur != null) ur.startSteps();
	  for(int i = selectedObjects.size() - 1; i >= 0; i--) {
	    Component comp = selectedObjects.elementAt(i);
	    remove(comp, ur);
          }
          if(ur != null) ur.finishUndoSteps("remove");
	}
      }
    }
  }
  /**
   * Removes all components.
   */
  public void removeAll() {
    // do list synchronize should always occur before component synchronize
    // since undo and redo will always perform syncs in that order
    UndoRedo ur = getUndoRedo();
    Object outerLock;
    if(ur == null) outerLock = getTreeLock();
    else outerLock = ur.getDoListLock();
    synchronized (outerLock) {
      synchronized (getTreeLock()) {
	if(ur != null) ur.startSteps();
        Component[] dcList = getComponents();
	for(int i = dcList.length - 1; i >= 0; i--) remove(dcList[i], ur);
	relayout();
	if(ur != null) ur.finishUndoSteps("remove all");
      }
    }
  }
  /**
   * Returns the last (top most) display Component containing a point location.
   *
   * @param pt	point
   * @return	Component containing the point or <code>null</code>
   */
  public Component getDisplayComponent(Point pt) {
    synchronized (getTreeLock()) {
      int ncmps = getComponentCount();
      for(int i= ncmps - 1; i >= 0; i--) {
        Component comp = getComponent(i);
        Rectangle r = comp.getBounds();
        if(r.contains(pt)) return comp;
      }
    }
    return null;
  }
  /**
   * Gets a Components display level.
   *
   * @param comp	component to get level of
   * @return		level of given component or <code>-1</code> if
   *			component not in this container
   */
  public int getDisplayComponentLevel(Component comp) {
    synchronized (getTreeLock()) {
      int max = getComponentCount();
      for(int i=0; i < max; i++) {
	if(getComponent(i) == comp) return i;
      }
    }
    return -1;  
  }
  /**
   * Moves a DisplayComponent to the front.
   *
   * @param comp	component to move to the front
   */
  public void displayComponentToFront(Component comp) {
    synchronized (getTreeLock()) {
      displayComponentToLevel(comp, getComponentCount()-1); // last level
    }
  }
  /**
   * Moves a DisplayComponent to the back.
   *
   * @param comp	component to move to the back
   */
  public void displayComponentToBack(Component comp) {
    displayComponentToLevel(comp, 0);
  }
  /**
   * Moves a DisplayComponent to a given level.
   *
   * @param comp	component to move to the given level
   * @param newLevel	level to move component to
   */
  public void displayComponentToLevel(Component comp, int newLevel) {
    // do list synchronize should always occur before component synchronize
    // since undo and redo will always perform syncs in that order
    UndoRedo ur = getUndoRedo();
    Object outerLock;
    if(ur == null) outerLock = getTreeLock();
    else outerLock = ur.getDoListLock();
    synchronized (outerLock) {
      synchronized (getTreeLock()) {
        int oldLevel = getDisplayComponentLevel(comp);
        if(oldLevel < 0) return;
	Point loc = comp.getLocation();
	boolean select = selectedObjects.contains(comp);
	remove(comp);
	add(comp, new CNUDisplayConstraints(loc, ur != null, select), newLevel);
      }
    }
  }
  /**
   * Moves the selected images to the front of the other images.
   */
  public void selectedToFront() {
    Rectangle repaintArea = null;
    // do list synchronize should always occur before component synchronize
    // since undo and redo will always perform syncs in that order
    UndoRedo ur = getUndoRedo();
    Object outerLock;
    if(ur == null) outerLock = getTreeLock();
    else outerLock = ur.getDoListLock();
    synchronized (outerLock) {
      synchronized (getTreeLock()) {
	Component[] components = getSelectedComponentsOrdered();
	if(components != null) {
	  if(ur != null) ur.startSteps();
	  for(int i = 0; i < components.length; i++) {
	    Component comp = components[i];
	    displayComponentToFront(comp);
	    if(repaintArea == null) repaintArea = comp.getBounds();
	    else repaintArea.add(comp.getBounds());
	  }
          if(ur != null) ur.finishUndoSteps("front");
	}
      }
    }
    if(repaintArea != null) {
      repaintArea.grow(1, 1);
      repaint(repaintArea.x, repaintArea.y, repaintArea.width,
	      repaintArea.height);
    }
  }
  /**
   * Moves the selected images to behind the other images.
   */
  public void selectedToBack() {
    // Note - cannot utilize apply because this rearranges componentList
    Rectangle repaintArea = null;
    // do list synchronize should always occur before component synchronize
    // since undo and redo will always perform syncs in that order
    UndoRedo ur = getUndoRedo();
    Object outerLock;
    if(ur == null) outerLock = getTreeLock();
    else outerLock = ur.getDoListLock();
    synchronized (outerLock) {
      synchronized (getTreeLock()) {
	Component[] components = getSelectedComponentsOrdered();
	if(components != null) {
	  if(ur != null) ur.startSteps();
	  for(int i = components.length -1; i >= 0; i--) {
	    Component comp = components[i];
            displayComponentToBack(comp);
            if(repaintArea == null)repaintArea = comp.getBounds();
            else repaintArea.add(comp.getBounds());
	  }
          if(ur != null) ur.finishUndoSteps("back");
        }
      }
    }
    if(repaintArea != null) {
      repaintArea.grow(1, 1);
      repaint(repaintArea.x, repaintArea.y, repaintArea.width,
	      repaintArea.height);
    }
  }
  /**
   * Updates the graphics area.
   *
   * @param g graphics to update.
   */
  public void update(Graphics g) {
    if(! isValid()) validate();
    synchronized (getTreeLock()) {
      // First clear the area
      g.setColor(getBackground());
      Rectangle r = g.getClipBounds();
      if(r == null) {
        Dimension d = getMaximumSize();
        r = new Rectangle(d.width, d.height);
      } else if(! selectedObjects.isEmpty() ) {
	// grow clip bounds to possibly include selection outline
        Dimension d = getMaximumSize();
	r.setBounds( (r.x == 0)? r.x : r.x - 1, (r.y == 0)? r.y : r.y - 1,
		     (r.width == d.width)? r.width : r.width + 2,
		     (r.height == d.height)? r.height : r.height + 2);
	g.setClip(r.x, r.y, r.width, r.height);
      }
      g.fillRect(r.x, r.y, r.width, r.height);
      Component[] components = getComponents();
      for(int i=0; i<components.length; i++) {
        Component comp = components[i];
	boolean selected = selectedObjects.contains(comp);
        Rectangle compBounds = comp.getBounds();
        if(r.intersects(compBounds)) {
	  g.translate(compBounds.x, compBounds.y);
	  comp.paint(g);
	  g.translate(-compBounds.x, -compBounds.y);
        }
        // draw a box if an element is selected
        if(selected) {
	  g.setColor(selectColor);
	  compBounds.grow(1, 1);
	  if(r.intersects(compBounds))
	    g.drawRect(compBounds.x, compBounds.y,
		       compBounds.width-1, compBounds.height-1);
        }
      }
    }
  }
  /**
   * Paints this display onto the graphics context.
   *
   * @param g	graphics context to paint on
   */
  public void paint(Graphics g) { update(g); }
  /**
   * Validates the correct parent.
   */
  public void validateProperParent() {
    synchronized (getTreeLock()) {
      ScrollPane sp = getParentScrollPane();
      if(sp != null) sp.validate();
      else getParentFrame().validate();
    }
  }
  /**
   * Does a layout of all components.
   */
  public void relayout() {
    doLayout();
    LayoutManager lm = getLayout();
    if( ! (lm instanceof CNURowLayoutManager) ) return;
    CNURowLayoutManager cnurlm = (CNURowLayoutManager) lm;
    // do list synchronize should always occur before component synchronize
    // since undo and redo will always perform syncs in that order
    UndoRedo ur = getUndoRedo();
    Object outerLock;
    if(ur == null) outerLock = getTreeLock();
    else outerLock = ur.getDoListLock();
    synchronized (outerLock) { synchronized (getTreeLock()) {
      Object layoutState = null;
      if(ur != null) layoutState = cnurlm.getLayoutState(this);
      cnurlm.setLayoutState(null, this);
      cnurlm.layoutContainer(this);
      if(layoutState != null) {
        Object newLayoutState = cnurlm.getLayoutState(this);
        if( ! newLayoutState.equals(layoutState)) {
	  Class[] undoParams = { Object.class, Container.class };
	  Object[] undoArgs = { layoutState, this };
	  DoCommand undo =
	    new DoCommand(lm, "setLayoutState", undoParams, undoArgs);
	  Class[] redoParams = undoParams;
	  Object[] redoArgs = { newLayoutState, this };
	  DoCommand redo =
	    new DoCommand(lm, "setLayoutState", redoParams, redoArgs);
	  ur.addUndo(undo, redo, "relayout");
        }
      }
    } }
    repaint();
  }
  /**
   * Sets the next layout position to upper left display corner with optional
   * undo/redo history.
   *
   * @param ur		if not <code>null</code> undo commands will be generated
   */
  public void resetLayout(UndoRedo ur) {
    LayoutManager lm = getLayout();
    if(! (lm instanceof CNURowLayoutManager) ) return;
    CNURowLayoutManager cnurlm = (CNURowLayoutManager) lm;
    Object outerLock;
    if(ur == null) outerLock = getTreeLock();
    else outerLock = ur.getDoListLock();
    synchronized (outerLock) { synchronized (getTreeLock()) {
      Object layoutState = null;
      if(ur != null) layoutState = cnurlm.getLayoutState(this);
      // nextCol of -1 causes locations to be reset to default during layout
      cnurlm.resetLayout(0, 0, -1, 0, this);
      cnurlm.layoutContainer(this);
      if(layoutState != null) {
        Object newLayoutState = cnurlm.getLayoutState(this);
        if( ! newLayoutState.equals(layoutState)) {
	  Class[] undoParams = { Object.class, Container.class };
	  Object[] undoArgs = { layoutState, this };
	  DoCommand undo =
	    new DoCommand(lm, "setLayoutState", undoParams, undoArgs);
	  Class[] redoParams = undoParams;
	  Object[] redoArgs = { newLayoutState, this };
	  DoCommand redo =
	    new DoCommand(lm, "setLayoutState", redoParams, redoArgs);
	  ur.addUndo(undo, redo, "reset layout");
        }
      }
    } }
  }
  /**
   * Move a component to a new location with undo/redo.
   *
   * comp  component to move
   * @param x     x location to move to
   * @param y     y location to move to
   */
  public void move(Component comp, int x, int y) {
    move(comp, x, y, getUndoRedo());
  }
  /**
   * Moves a component to a new location optionally
   * creating an undo/redo history.
   *
   * @param comp	Component to move
   * @param x		x pixel position to move to
   * @param y		y pixel position to move to
   * @param ur		if not <code>null</code> undo commands will be generated
   */
  public void move(Component comp, int x, int y, UndoRedo ur) {
    Object outerLock;
    if(ur == null) outerLock = getTreeLock();
    else outerLock = ur.getDoListLock();
    synchronized (outerLock) {
      synchronized (getTreeLock()) {
        Point oldLocation = comp.getLocation();
        if( (oldLocation.x == x) && (oldLocation.y == y)) return;
        LayoutManager lm = getLayout();
        if(lm instanceof CNURowLayoutManager) {
	  ((CNURowLayoutManager) lm).addLayoutComponent(comp,
		new CNUDisplayConstraints(new Point(x, y), false));
	}
        comp.setLocation(x, y);
	if(ur != null) {
	  Class[] undoParams = { java.awt.Component.class,
			         Integer.TYPE, Integer.TYPE, Boolean.TYPE };
          Object[] undoArgs = { comp, new Integer(oldLocation.x),
				new Integer(oldLocation.y),
				new Boolean(false) };
          DoCommand undo = new DoCommand(this, "move", undoParams, undoArgs);
          Object[] redoArgs = { comp, new Integer(x), new Integer(y),
				new Boolean(false) };
          DoCommand redo = new DoCommand(this, "move", undoParams, redoArgs);
          ur.addUndo(undo, redo, comp, "move");
	}
      }
    }
  }
  /**
   * Reorganizes all existing components in rows.
   */
  //  public void doRowLayout() {}
  /**
   * Handles action events that occur when a menu item is selected.
   *
   * @param evt	action event
   */
  public void actionPerformed(ActionEvent evt) {
    setWaitCursor();
    try {
      String actionCommand = evt.getActionCommand();
      if("info".equals(actionCommand)) showInfo();
      else if("delete".equals(actionCommand)) removeSelections();
      else if("select all".equals(actionCommand)) selectAll();
    } finally {
      setNormalCursor();
    }
  }
  /**
   * Sets the wait cursor over all applicable windows.
   */
  public void setWaitCursor() {}
  /**
   * Sets the normal cursor over all applicable windows.
   */
  public void setNormalCursor() {}
  /**
   * Gets the location of an mouse event relative to the screen based on
   * the current drag event positioning mode.  This routine is needed
   * because this container may reposition components or itself during
   * drag operations.  Does not work on all platforms.  And no way to
   * determine which mode of operation is best for current platform.
   *
   * @param evt		mouse event
   * @param start	should be <code>true</code> if this is the
   *			initial call generated by a mouse press
   * @return 		location of mouse event relative to the screen
   */
  public Point getEventPointOnScreen(MouseEvent evt, boolean start) {
    Point origin;
    Point screenLocation;
    switch (dragEventPositioningMode) {
    default:
    case EVENT_RELATIVE_TO_START_COMPONENT_ON_SCREEN:
      if(start)
	saveEventComponentOrigin.setLocation(
	  evt.getComponent().getLocationOnScreen());
      origin = saveEventComponentOrigin;
      break;
    case EVENT_RELATIVE_TO_COMPONENT_ON_SCREEN:
      origin = evt.getComponent().getLocationOnScreen();
      break;
    }
    screenLocation = new Point(origin.x + evt.getX(), origin.y + evt.getY());
    return screenLocation;
  }
  /**
   * Sets the drag event positioning mode.  Because of inconsistencies
   * across platforms, this flag is needed to specify how to calculate
   * the position of an mouse event on the screen during a drag.
   *
   * @param mode	new drag event positioing mode
   *			(EVENT_RELATIVE_TO_START_COMPONENT_ON_SCREEN or
   *			EVENT_RELATIVE_TO_COMPONENT_ON_SCREEN)
   */
   public void setDragEventPositioningMode(int mode) {
     dragEventPositioningMode = mode;
   }
  /**
   * Gets the drag event positioning mode.  Because of inconsistencies
   * across platforms, this flag is needed to specify how to calculate
   * the position of a mouse event on the screen during a drag.
   *
   * @return	drag event positioing mode
   *			(EVENT_RELATIVE_TO_START_COMPONENT_ON_SCREEN or
   *			EVENT_RELATIVE_TO_COMPONENT_ON_SCREEN)
   */
   public int getDragEventPositioningMode() {
     return dragEventPositioningMode;
   }
  /**
   * Changes the scroll position by the givent amount.
   *
   * @param amount		amount to scroll by
   * @param scrollPosition	starting scroll position to change by amount
   *				if <code>null</code> uses current position
   */
   public void translateScrollPosition(Point amount, Point scrollPosition) {
     ScrollPane scrollPane = getParentScrollPane();
     if(scrollPane == null) return;
     if(scrollPosition == null)
       scrollPosition = scrollPane.getScrollPosition();
     else scrollPosition = scrollPosition.getLocation();
     scrollPosition.translate(-amount.x, -amount.y);
     scrollPosition.move(Math.max(0, scrollPosition.x),
			 Math.max(0, scrollPosition.y));
     scrollPane.setScrollPosition(scrollPosition);
   }
  /**
   * Called when the mouse moves while a mouse button is down.
   * Should only be called from the event processing thread.
   *
   * @param evt	mouse event
   */
  public void mouseDragged(MouseEvent evt) {
    Point eventPoint = evt.getPoint().getLocation();
    if(selectPoint != null) {
      if(selectBox != null) {
	xorDrawBox(selectBox); // clear the currently drawn box
	selectBox.setBounds(selectPoint.x, selectPoint.y, 0, 0);
      } else selectBox = new Rectangle(selectPoint);
      // for left and top additions
      selectBox.add(eventPoint);
      // for right and bottom additions
      selectBox.add(eventPoint.x + 1, eventPoint.y + 1);
      // draw this new box
      xorDrawBox(selectBox);
    }
  }
  /**
   * Catches mouse up events to display a submenu.
   * Should only be called from the event processing thread.
   *
   * @param evt	mouse event
   */
  public void mouseReleased(MouseEvent evt){
    if(selectBox != null) {
      xorDrawBox(selectBox); // erase the currently drawn box
      if(currentComp == null) selectBoxedObjects(selectBox);
      selectBox = null;  // select box not redrawn
    }
    selectPoint = null;
    if(evt.isPopupTrigger()) {
      showPopupMenu(currentComp, evt.getComponent(), evt.getX(), evt.getY());
    }
  }
  /**
   * Catches mouse down events to display pixel values from slices.
   * Should only be called from the event processing thread.
   *
   * @param evt	mouse event
   */
  public void mousePressed(MouseEvent evt){
    requestFocus();
    int modifiers = evt.getModifiers();
    currentComp = evt.getComponent();
    Point eventPoint = evt.getPoint().getLocation();
    if(currentComp == this) currentComp = getDisplayComponent(eventPoint);

    // first handle current selection status
    if(currentComp == null) {
      // selection off COMP
      if( ! evt.isShiftDown() ) clearAllSelections();
    }
    else {
      // selection status of currentComp
      if( selectedObjects.contains(currentComp) ) {
	// shift toggle - allows removing selection while maintaining others
	// don't toggle if menu or move button selected
	// note - BUTTON1_MASK not working on sun
	if( evt.isShiftDown() &&
	  ( (modifiers & InputEvent.BUTTON2_MASK) == 0 ) &&
	  ( (modifiers & InputEvent.BUTTON3_MASK) == 0 ) &&
	  ! evt.isPopupTrigger()
	 ) {
	  clearSelection(currentComp);
	  currentComp = null;
	  return;   // no further processing on removed selection
        }
      }
      else {
	// select currentComp and remove others if not shift down
	if(! evt.isShiftDown() ) clearAllSelections();
	addSelection(currentComp);
      }
    }
    // now what to do with the press
    if(evt.isPopupTrigger())
      showPopupMenu(currentComp, evt.getComponent(), evt.getX(), evt.getY());
    else if(currentComp == null) {
      // off a component start a selection box
      selectPoint = eventPoint;
    }
  }
  /**
   * Show popup menu over a component.
   *
   * @param currentComponent component to show popup menu for
   * @param evtComponent     component event location is relative to
   * @param evtX	     event location x
   * @param evtY	     event location y
   */
  public void showPopupMenu(Component currentComponent,
			    Component evtComponent, int evtX, int evtY) {
    if(currentComp != null) overComponentPM.show(evtComponent, evtX, evtY);
  }
  /**
   * Called when the mouse is moved to a new point.
   *
   * @param evt	mouse event
   */
  public void mouseMoved(MouseEvent evt) {}
  /**
   * Called when the mouse is clicked.
   *
   * @param evt	mouse event
   */
  public void mouseClicked(MouseEvent evt) {};
  /**
   * Called when the mouse moves over this.
   *
   * @param evt	mouse event
   */
  public void mouseEntered(MouseEvent evt) {};
  /**
   * Called when the mouse leave this.
   *
   * @param evt	mouse event
   */
  public void mouseExited(MouseEvent evt) {};
  /**
   * Selects all components.
   */
  public void selectAll() {
    synchronized (getTreeLock()) {
      Component[] components = getComponents();
      for(int i=0; i<components.length; i++) addSelection(components[i]);
    }
  }
  /**
   * Selects components that fall within a box.
   *
   * @param selectBox	box components must fall within to be selected
   */
  public void selectBoxedObjects(Rectangle selectBox) {
    synchronized (getTreeLock()) {
      Component[] components = getComponents();
      for(int i=0; i<components.length; i++) {
        Component comp = components[i];
        if( selectBox.equals(selectBox.union( comp.getBounds() )) )
	   addSelection(comp);
      }
    }
  }
  /**
   * Add a component to the list of selected components.
   *
   * @param comp	the component to add
   */
  public void addSelection(Component comp) {
    if(comp != null) synchronized (getTreeLock()) {
      if( contains(comp) && (! selectedObjects.contains(comp)) ) {
	selectedObjects.addElement(comp);
	Rectangle repaintArea = comp.getBounds();
	repaintArea.grow(1, 1);
	repaint(repaintArea.x, repaintArea.y, repaintArea.width,
		repaintArea.height);
      }
    }
  }
  /**
   * Clears a component from the selection list.
   *
   * @param comp	the component to clear
   */
  public void clearSelection( Component comp ) {
    if(comp != null) synchronized (getTreeLock()) {
      if( selectedObjects.contains(comp) ) {
	Rectangle repaintArea = comp.getBounds();
	selectedObjects.removeElement(comp);
	repaintArea.grow(1, 1);
	repaint(repaintArea.x, repaintArea.y, repaintArea.width,
		repaintArea.height);
      }
    }
  }
  /**
   * Clears all selections from the selection list.
   */
  public void clearAllSelections() {
    Rectangle repaintArea = null;
    synchronized (getTreeLock()) {
      if(! selectedObjects.isEmpty() ) {
        Enumeration e = selectedObjects.elements();
        while(e.hasMoreElements()) {
	  Component comp = (Component) e.nextElement();
	  if(repaintArea == null)repaintArea = comp.getBounds();
	  else repaintArea.add(comp.getBounds());
        }
        selectedObjects.removeAllElements();
      }
    }
    if(repaintArea != null) {
      repaintArea.grow(1, 1);
      repaint(repaintArea.x, repaintArea.y,
      repaintArea.width, repaintArea.height);
    }
  }
  /**
   * Draws a box with exclusive or'ing (xor) to allow erasing the same way.
   *
   * @param box	box to draw
   */
  public void xorDrawBox(Rectangle box) {
    Graphics drawBoxG = getGraphics();
    drawBoxG.setXORMode(Color.red);
    drawBoxG.drawRect(box.x, box.y, box.width, box.height);
  }
  /**
   * Shows info about the current component.
   */
  public void showInfo() {
    String info = null;
    Component comp = getCurrentComponent();
    if(comp != null) {
      ShowStatus ss = (ShowStatus) getCNUViewer();
      if(ss != null) ss.showStatus(comp.toString());
      else System.out.println(comp.toString());
    }
  }
}  // end CNUDisplay class
