package iiv.dialog;
import iiv.*;
import iiv.display.*;
import iiv.util.*;
import iiv.filter.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.lang.*;
import Acme.Fmt;

/**
 * Dialog that interfaces with the user to display and get filter settings.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.data.CNUScale
 * @see		CNUDialog
 */
public class FilterDialog extends CNUDialog
implements ActionListener, ItemListener {
  private static final long serialVersionUID = -1796046619907471003L;
  private JButton dismissB = new JButton("Dismiss");

  public static double[] zoomChoices = {1, 2, 3, 4, .25, .5, -1};
  public static final int NODIRECTION = 0;
  public static final int WEST = 1;
  public static final int EAST = 2;
  public static final int NORTH = 4;
  public static final int SOUTH = 8;
  public static final int NORTHWEST = NORTH | WEST;
  public static final int NORTHEAST = NORTH | EAST;
  public static final int SOUTHWEST = SOUTH | WEST;
  public static final int SOUTHEAST = SOUTH | EAST;

  private JButton applyZoomB = new JButton("Apply Zoom");
  private JTextField zoomVValueTF = new JTextField("  1.00");
  private JComboBox zoomVCH = new JComboBox();
  private JTextField zoomHValueTF = new JTextField("  1.00");
  private JComboBox zoomHCH = new JComboBox();

  private JButton currentB = new JButton("Reset to Current");
  private JButton defaultsB = new JButton("Apply to Defaults Only");

  public static double[] rotationChoices =
    {0, 15, 45, 90, 135, 180, -135, -90, -45, -15};
  private JButton applyRotationB = new JButton("Apply Rotation");
  private JTextField rotationValueTF = new JTextField("  0.0");
  private JComboBox rotationCH = new JComboBox();

  private Object zoomLock = new Object();
  private double zoomV = 1;
  private double zoomH = 1;

  private Object rotationLock = new Object();
  private double rotation = 0;

  private JComboBox filterSampleTypeCH = new JComboBox();

  private JCheckBox mouseZoomCB = new JCheckBox("Mouse Zoom");
  private mlmml mouseZoomListener = null;
  /**
   * Constructs a new instance of FilterDialog.
   *
   * @param parentFrame	parent frame
   */
  public FilterDialog(Frame parentFrame) { this(parentFrame, null); }
  /**
   * Constructs a new instance of FilterDialog.
   *
   * @param parentFrame	parent frame
   * @param cnuv	CNUViewer to work with
   */
  public FilterDialog(Frame parentFrame, CNUViewer cnuv ) {
    super(parentFrame, "Image Filtering", false, cnuv);
    if(cnuv != null) init(cnuv.getCNUViewerActions());
  }
  /**
   * Sets the CNUViewer.
   *
   * @param cnuviewer the new CNUViewer for this dialog to use
   *		Should not be <code>null</code>.
   */
  public void setCNUViewer(CNUViewer cnuviewer) {
    super.setCNUViewer(cnuviewer);
    // normally only called after creation so do standard
    // creation init
    init(cnuviewer.getCNUViewerActions());
    setToCurrent();
  }
  private void init(CNUViewerActions cnuviewerActions) {
    for(int i=0; i<zoomChoices.length-1; i++) {
      zoomVCH.addItem(zoomChoices[i] + " X");
      zoomHCH.addItem(zoomChoices[i] + " X");
    }
    zoomHCH.addItem("= vert");
    zoomVCH.addItemListener(this);
    zoomHCH.addItemListener(this);

    for(int i=0; i<rotationChoices.length; i++)
      rotationCH.addItem(rotationChoices[i] + " degrees");
    rotationCH.addItemListener(this);

    for(int i=0; i<FilterSampling.SAMPLING_TYPES.length; i++)
      filterSampleTypeCH.addItem(LinearImageFilter.sampleTypeToString(FilterSampling.SAMPLING_TYPES[i]));

    Container contentPane = getContentPane();

    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    contentPane.add(Box.createVerticalStrut(5));

    Box box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(new JLabel("Pixel Sampling:  "));
    filterSampleTypeCH.setToolTipText("Selects how pixels are sampled");
    box.add(filterSampleTypeCH);
    box.add(Box.createHorizontalGlue());
    box.add(currentB);
    currentB.setToolTipText("Reset filter values to that of currently selected object");
    currentB.addActionListener(this);
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    JLabel rotationWarningL =
    new JLabel("Warning - rotation may invalidate orientation and value labels");
    rotationWarningL.setForeground(Color.yellow);
    box.add(rotationWarningL);
    box.add(Box.createHorizontalGlue());
    box.add(Box.createHorizontalStrut(5));


    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    Container grid = new Container() {private static final long serialVersionUID = -1124308851918640531L;};
    grid.setLayout(new GridLayout(0, 4, 5, 5));
    box.add(grid);
    //    grid.add(new JLabel("Rotation:  "));
    cnuviewerActions.rotateCheckboxAction.addCheckboxTo(grid);
    grid.add(rotationValueTF);
    rotationValueTF.addActionListener(this);
    rotationValueTF.setToolTipText("Enter desired rotation in degrees");
    grid.add(rotationCH);
    rotationCH.setToolTipText("Select from standard rotation angles");
    grid.add(applyRotationB);
    applyRotationB.addActionListener(this);
    applyRotationB.setToolTipText("Apply current rotation to selected objects");
    Box zbox = Box.createHorizontalBox();
    cnuviewerActions.zoomCheckboxAction.addCheckboxTo(zbox);
    zbox.add(Box.createHorizontalGlue());
    zbox.add(new JLabel("Vertical:  "));
    grid.add(zbox);
    grid.add(zoomVValueTF);
    zoomVValueTF.setToolTipText("Enter vertical zoom factor");
    zoomVValueTF.addActionListener(this);
    grid.add(zoomVCH);
    zoomVCH.setToolTipText("Select from standard vertical zoom factors");
    grid.add(applyZoomB);
    applyZoomB.setToolTipText("Apply current zoom values to selected objects");
    applyZoomB.addActionListener(this);
    zbox = Box.createHorizontalBox();
    zbox.add(Box.createHorizontalGlue());
    zbox.add(new JLabel("Horizontal:"));
    grid.add(zbox);
    grid.add(zoomHValueTF);
    zoomHValueTF.addActionListener(this);
    zoomHValueTF.setToolTipText("Enter horizontal zoom factor");
    grid.add(zoomHCH);
    zoomHCH.setToolTipText("Select from standard horizontal zoom factors");
    grid.add(mouseZoomCB);
    mouseZoomCB.setToolTipText("Select to zoom by dragging mouse over zoomable objects in the main display area");
    mouseZoomCB.addItemListener(this);
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(defaultsB);
    defaultsB.setToolTipText("Apply current zoom/rotate values to defaults");
    defaultsB.addActionListener(this);
    box.add(Box.createHorizontalGlue());
    box.add(dismissB);
    dismissB.setToolTipText("Hide this dialog");
    dismissB.addActionListener(this);
    box.add(Box.createHorizontalStrut(5));

    contentPane.add(Box.createVerticalStrut(5));

    pack();
  }
  /**
   * Handles ItemListener events.
   *
   * @param ie	item event
   */
  public void itemStateChanged(ItemEvent ie){
    Object source = ie.getSource();
    if (source == zoomVCH) setZoomV( getSelectedZoomV() );
    else if (source == zoomHCH) setZoomH( getSelectedZoomH() );
    else if (source == rotationCH)
      setRotation( getSelectedRotation() );
    else if(source == mouseZoomCB) {
      if(mouseZoomCB.isSelected()) grabCNUDisplaysMouse();
      else restoreCNUDisplaysMouse();
    }
  }
  /**
   * Internal class to handle mouse events from the main display panel.
   */
  private class mlmml implements MouseListener, MouseMotionListener {

    private int resizingDirection = NODIRECTION;
    private Component resizingComponent = null;
    private Rectangle resizingBox = null;
    private Rectangle resizingOriginalBox = null;
    private Point resizingOriginNWPoint = null;
    private Point resizingOriginSEPoint = null;
    private Point startPoint = null;
    private double originalZoomV = 0;
    private double originalZoomH = 0;

    /**
     * Handles mouse presses starting a resize operation if over
     * the right location of a zoomable component.
     *
     * @param evt mouse event.
     */
    public void mousePressed(MouseEvent evt){
      Point eventPoint = evt.getPoint().getLocation();
      Component comp = evt.getComponent();
      CNUDisplay cnud = getCNUDisplay();
      if(comp == cnud) comp = cnud.getDisplayComponent(eventPoint);
      if(comp instanceof Zoomable) {
	resizingDirection = updateCursor(comp, eventPoint);
	if(resizingDirection != NODIRECTION) {
	  resizingComponent = comp;
	  resizingOriginalBox = comp.getBounds();
	  resizingBox = resizingOriginalBox.getBounds();
	  originalZoomV = ((Zoomable) comp).getZoomV();
	  originalZoomH = ((Zoomable) comp).getZoomH();

	  resizingOriginNWPoint = resizingOriginalBox.getLocation();
	  resizingOriginSEPoint = resizingOriginalBox.getLocation();
	  resizingOriginSEPoint.translate(resizingOriginalBox.width - 1,
					  resizingOriginalBox.height - 1);
	  cnud.xorDrawBox(resizingBox);  // draw new box
	}
      }
    }
    /**
     * Display resizing box and new zooms when dragging.
     *
     * @param evt mouse event.
     */
    public void mouseDragged(MouseEvent evt) {
      if(resizingDirection != NODIRECTION) {
	Point evtPt = evt.getPoint().getLocation();
	CNUDisplay cnud = getCNUDisplay();
	cnud.xorDrawBox(resizingBox);  // erase previous box

	Point pt1;
	Point pt2;
	int x;
	int y;
	double zoomV;
	double zoomH;
	boolean sameZoom = false;

	switch(resizingDirection) {
	default:
	  pt1 = resizingOriginNWPoint;
	  pt2 = resizingOriginSEPoint;
	  break;
	case NORTHWEST:
	  pt1 = resizingOriginSEPoint;
	  pt2 = new Point(Math.min(pt1.x-1, evtPt.x),
			  Math.min(pt1.y-1, evtPt.y));
	  sameZoom = true;
	  break;
	case SOUTHWEST:
	  pt1 = new Point(resizingOriginSEPoint.x, resizingOriginNWPoint.y);
	  pt2 = new Point(Math.min(pt1.x-1, evtPt.x),
			  Math.max(pt1.y+1, evtPt.y));
	  sameZoom = true;
	  break;
	case NORTHEAST:
	  pt1 = new Point(resizingOriginNWPoint.x, resizingOriginSEPoint.y);
	  pt2 = new Point(Math.max(pt1.x+1, evtPt.x),
			  Math.min(pt1.y-1, evtPt.y));
	  sameZoom = true;
	  break;
	case SOUTHEAST:
	  pt1 = resizingOriginNWPoint;
	  pt2 = new Point(Math.max(pt1.x+1, evtPt.x),
			  Math.max(pt1.y+1, evtPt.y));
	  sameZoom = true;
	  break;
	case WEST:
	  pt1 = resizingOriginSEPoint;
	  pt2 = new Point(Math.min(pt1.x-1, evtPt.x),
			  resizingOriginNWPoint.y);
	  break;
	case EAST:
	  pt1 = resizingOriginNWPoint;
	  pt2 = new Point(Math.max(pt1.x+1, evtPt.x),
			  resizingOriginSEPoint.y);
	  break;
	case NORTH:
	  pt1 = resizingOriginSEPoint;
	  pt2 = new Point(resizingOriginNWPoint.x,
			  Math.min(pt1.y-1, evtPt.y));
	  break;
	case SOUTH:
	  pt1 = resizingOriginNWPoint;
	  pt2 = new Point(resizingOriginSEPoint.x,
			  Math.max(pt1.y+1, evtPt.y));
	  break;
	}
	resizingBox = new Rectangle(pt1);
	resizingBox.add(pt2);
	zoomV = (double) resizingBox.height/(double) resizingOriginalBox.height;
	zoomH = (double) resizingBox.width/(double) resizingOriginalBox.width;

	if(sameZoom) {
	  if(zoomV < zoomH) {
	    pt2.y = (int) Math.round(((double) pt1.y) + 
				     (((double) (pt2.y - pt1.y)) *
				      zoomH/zoomV));
	  }
	  else if(zoomH < zoomV) {
	    pt2.x = (int) Math.round(((double) pt1.x) + 
				     (((double) (pt2.x - pt1.x)) *
				      zoomV/zoomH));
	  }
	  resizingBox = new Rectangle(pt1);
	  resizingBox.add(pt2);
	  zoomV = (double) resizingBox.height/(double) resizingOriginalBox.height;
	  zoomH = (double) resizingBox.width/(double) resizingOriginalBox.width;
	}

	setZoom(zoomV * originalZoomV, zoomH * originalZoomH);

	cnud.xorDrawBox(resizingBox);  // draw new box
      }
    }
    /**
     * Perform task and any clean up after drag.
     */
    public void endDrag() {
      if(resizingDirection != NODIRECTION) {
	CNUDisplay cnud = getCNUDisplay();
	cnud.xorDrawBox(resizingBox);  // erase previous box
	// apply the zoom to the current component
	if(resizingComponent != null) {
	  getCNUViewer().setDefaultZoom(getZoomV(), getZoomH());
	  DisplayComponentDefaults.setDefaultFilterSampleType(
	    getFilterSampleType());
	  Component[] comps = new Component[1];
	  comps[0] = resizingComponent;
	  if(! cnud.applyToComponents(cnud.ZOOM, comps) )
	    Toolkit.getDefaultToolkit().beep();
	}
	resizingDirection = NODIRECTION;
	resizingComponent = null;
	resizingOriginalBox = null;
	resizingBox = null;
	resizingOriginNWPoint = null;
	resizingOriginSEPoint = null;
      }
    }
    /**
     * end the drag when the mouse is released.
     *
     * @param evt mouse event.
     */
    public void mouseReleased(MouseEvent evt){
      endDrag();
    }
    public void mouseClicked(MouseEvent evt){}
    public void mouseEntered(MouseEvent evt) {}
    /**
     * Update the cursor when the mouse leaves the main area.
     */
    public void mouseExited(MouseEvent evt) { updateCursor(null, null); }
    /**
     * Called when the mouse moves to update the cursor type
     * if near the edge of a zoommable object.
     *
     * @param evt mouse event associated with the mouse move.
     */
    public void mouseMoved(MouseEvent evt) {
      Point eventPoint = evt.getPoint().getLocation();
      Component comp = evt.getComponent();
      CNUDisplay cnud = getCNUDisplay();
      if(comp == cnud) comp = cnud.getDisplayComponent(eventPoint);
      updateCursor(comp, eventPoint);
    }
    private Cursor currentCursor = null;
    /**
     * Updates the cursor type displayed for a component depending
     * on the mouse location in the display container relative to
     * the component to signify the resize option possible.  Only
     * one component at a time may have a resize cursor.
     *
     * @param currentComp component mouse is currently over.
     * @param currentPt location of mouse in container.
     * @return relative direction of cursor drawn which is one of the following values
     * <code>NORTH, SOUTH, EAST, WEST, NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST or NODIRECTION</code>
     */
    public int updateCursor(Component currentComp, Point currentPt) {
      int compass = NODIRECTION;
      // no cursor for nonzoomable(includes null) components or null point
      if(((currentComp instanceof Zoomable)) && (currentPt != null)) {
	compass = getCompassBoxLocation(currentComp.getBounds(), currentPt);
      }
      
      Cursor newCursor = null;
      switch(compass) {
      default:
      case NODIRECTION:
      case NORTHWEST:
      case SOUTHWEST:
      case NORTHEAST:
      case WEST:
      case NORTH:
	compass = NODIRECTION;
	break;
      case SOUTHEAST:
	newCursor=Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
	break;
      case EAST:
	newCursor=Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
	break;
      case SOUTH:
	newCursor=Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
	break;
      }
      if((newCursor == null) && (currentCursor != null)) {
	getCNUDisplay().restoreCursors();
	currentCursor = null;
      }
      else if(newCursor != currentCursor) {
	CNUDisplay cnud = getCNUDisplay();
	cnud.restoreCursors();
	cnud.saveOldSetNewCursor(newCursor);
	currentCursor = newCursor;
      }

      return compass;
    }
    /**
     * Gets the relative directions related to the
     * sides and corners of a box given a point.
     *
     * @param box box to find points location relative to
     * @param currentPt point to find location relative to box
     * @return relative direction which is one of the following values
     * <code>NORTH, SOUTH, EAST, WEST, NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST or NODIRECTION</code>
     */
    public int getCompassBoxLocation(Rectangle box, Point currentPt) {
      if((box == null) || (currentPt == null)) return 0;
      
      int compass = 0;
      int radius = 2;
      
      int westDiff = Math.abs(currentPt.x - box.x);
      if(westDiff <= radius) compass = WEST;
      
      int xend = box.x + box.width - 1;
      int eastDiff = Math.abs(currentPt.x - xend);
      if(eastDiff <= radius) {
	if((compass == 0) || (westDiff < eastDiff)) compass = EAST;
      }

      int northSouthCompass = 0;
      int northDiff = Math.abs(currentPt.y - box.y);
      if(northDiff <= radius) northSouthCompass = NORTH;
      
      int yend = box.y + box.height - 1;
      int southDiff = Math.abs(currentPt.y - yend);
      if(southDiff <= radius) {
	if((northSouthCompass == 0) || (southDiff < northDiff))
	  northSouthCompass = SOUTH;
      }

      compass = compass | northSouthCompass;
      
      return compass;
    }
  }
  /**
   * Grabs the mouse input from over the CNUDisplay.
   */
  public void grabCNUDisplaysMouse() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { grabCNUDisplaysMouse(); }
      } );
      return;
    }
    if(mouseZoomListener == null)  mouseZoomListener = new mlmml();

    boolean gotMouse = getCNUDisplay().replaceMouseListeners(mouseZoomListener);
    if(mouseZoomCB.isSelected() != gotMouse)
      mouseZoomCB.setSelected(gotMouse);
  }
  /**
   * Restores mouse input to the CNUDisplay.
   */
  public void restoreCNUDisplaysMouse() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { restoreCNUDisplaysMouse(); }
      } );
      return;
    }
    if(mouseZoomListener != null) {
      mouseZoomListener.updateCursor(null, null);
      getCNUDisplay().restoreMouseListeners(mouseZoomListener);
    }
    if(mouseZoomCB.isSelected()) mouseZoomCB.setSelected(false);
  }
  /**
   * Overrides setVisible to restore mouse listener if dismissed.
   *
   * @param state	<code>true</code> to show this dialog
   */
  public void setVisible(boolean state) {
    if( ! state ) restoreCNUDisplaysMouse();
    super.setVisible(state);
  }
  /**
   * Interprets action events over this dialog.
   *
   * @param aevt	action event
   */
  public void actionPerformed(ActionEvent aevt) {
    CNUViewer cnuv = getCNUViewer();
    cnuv.setWaitCursor();
    try {
      Object source = aevt.getSource();
      if(source == dismissB) this.setVisible(false);
      else if(source == zoomVValueTF) getZoomV();
      else if(source == zoomHValueTF) getZoomH();
      else if (source == applyZoomB) {
	cnuv.setDefaultZoom(getZoomV(), getZoomH());
	DisplayComponentDefaults.setDefaultFilterSampleType(
	  getFilterSampleType());
	getCNUDisplay().apply(CNUDisplay.ZOOM);
      }
      else if (source == applyRotationB) {
	cnuv.setDefaultRotation(getRotation());
	DisplayComponentDefaults.setDefaultFilterSampleType(
	  getFilterSampleType());
	getCNUDisplay().apply(CNUDisplay.ROTATION);
      }
      else if(source == rotationValueTF) getRotation();
      else if(source == currentB) setToCurrent();
      else if(source == defaultsB) {
	cnuv.setDefaultRotation(getRotation());
	cnuv.setDefaultZoom(getZoomV(), getZoomH());
	DisplayComponentDefaults.setDefaultFilterSampleType(
	  getFilterSampleType());
      }
    } finally {
      cnuv.setNormalCursor();
    }
  }
  /**
   * Sets all values to the current viewer values.
   */
  public void setToCurrent() {
    CNUDisplay cnud = getCNUDisplay();
    setRotation(cnud.getCurrentRotation());
    double zoomV = cnud.getCurrentZoomV();
    double zoomH = cnud.getCurrentZoomH();
    if(zoomV == zoomH) zoomH = -1;
    setZoom(zoomV, zoomH);
    setFilterSampleType(cnud.getCurrentFilterSampleType());
  }
  /**
   * Sets the current rotation angle (degrees).
   *
   * @param angle	angle in degrees
   */
  public void setRotation(final double angle) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setRotation(angle); }
      } );
    }
    else {
      rotation = angle % 360.0;
      if(rotation < 0) rotation += 360.0;
      if(rotation > 180) rotation = rotation - 360.0;
      rotationValueTF.setText(Double.toString(rotation));
      setSelectedRotation(rotation);
    }
  }
  /**
   * Gets the current rotation angle (degrees).
   *
   * @return	angle in degrees
   */
  public double getRotation() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = new Double(getRotation()); }
      };
      runWithReturn.invokeAndWait();
      return ((Double) runWithReturn.returnObject).doubleValue();
    }
    else {
      try {
	double tmp = Double.valueOf(rotationValueTF.getText()).doubleValue();
	rotation = tmp;
      }  catch (NumberFormatException e) {
        Toolkit.getDefaultToolkit().beep();
      }
      setRotation(rotation);
      return rotation;
    }
  }
  /**
   * Sets current selected rotation (degrees).
   * Should only be called from event dispatch thread.
   *
   * @param angle	angle in degrees
   */
  private void setSelectedRotation(double angle) {
    // set to nearest rotation choice
    int nearestIndex = 0;
    double nearestError = Math.abs(rotationChoices[nearestIndex] - angle);
    for(int i=1; i<rotationChoices.length; i++) {
      double error = Math.abs(rotationChoices[i] - angle);
      if(error < nearestError) {
	nearestError = error;
	nearestIndex = i;
      }
    }
    if(rotationCH.getSelectedIndex() != nearestIndex)
      rotationCH.setSelectedIndex(nearestIndex);

  }
  /**
   * Gets current selected rotation (degrees).
   * Should only be called from event dispatch thread.
   *
   * @return	angle in degrees
   */
  private double getSelectedRotation() {
    return rotationChoices[rotationCH.getSelectedIndex()];
  }
  /**
   * Sets the current and selected vertical zoom.
   *
   * @param zoom	zoom
   */
  public void setZoomV(final double zoom) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setZoomV(zoom); }
      } );
    }
    else {
      // setting selected zoom may trigger an event that sets zoom to
      // selected value
      setSelectedZoomV(zoom);
      this.zoomV = zoom;
      zoomVValueTF.setText(Double.toString(zoom));
      if(! zoomHValueTF.isEnabled())
	zoomHValueTF.setText(Double.toString(zoom));
    }
  }
  /**
   * Sets the current and selected vertical zoom.
   *
   * @param zoom	zoom
   */
  public void setZoomH(double zoom) {
    if(! SwingUtilities.isEventDispatchThread()) {
      final double fzoom = zoom;
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setZoomH(fzoom); }
      } );
    }
    else {
      // setting selected zoom may trigger an event that sets zoom to
      // selected value
      setSelectedZoomH(zoom);
      this.zoomH = zoom;
      if(! zoomHValueTF.isEnabled()) zoom = getZoomV();
      zoomHValueTF.setText(Double.toString(zoom));
    }
  }
  /**
   * Sets the current and selected zoom.
   *
   * @param zoomV	vertical zoom
   * @param zoomH 	horizontal zoom
   */
  public void setZoom(double zoomV, double zoomH) {
    setZoomV(zoomV); setZoomH(zoomH);
  }
  /**
   * Gets current vertical zoom.
   *
   * @return	vertical zoom
   */
  public double getZoomV() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = new Double(getZoomV()); }
      };
      runWithReturn.invokeAndWait();
      return ((Double) runWithReturn.returnObject).doubleValue();
    }
    else {
      try {
        double tmp = Double.valueOf(zoomVValueTF.getText()).doubleValue();
        if(tmp > 0) zoomV = tmp;
        else Toolkit.getDefaultToolkit().beep();
      }  catch (NumberFormatException e) {
        Toolkit.getDefaultToolkit().beep();
      }
      setZoomV(zoomV);
      return zoomV;
    }
  }
  /**
   * Gets current horizontal zoom.
   *
   * @return	horizontal zoom
   */
  public double getZoomH() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = new Double(getZoomH()); }
      };
      runWithReturn.invokeAndWait();
      return ((Double) runWithReturn.returnObject).doubleValue();
    }
    else {
      if(zoomHValueTF.isEnabled()) {
        try {
          double tmp = Double.valueOf(zoomHValueTF.getText()).doubleValue();
          if(tmp > 0) zoomH = tmp;
          else Toolkit.getDefaultToolkit().beep();
        }  catch (NumberFormatException e) {
          Toolkit.getDefaultToolkit().beep();
        }
      }
      else zoomH = -1;
      setZoomH(zoomH);
      if(! zoomHValueTF.isEnabled() ) return getZoomV();
      else return zoomH;
    }
  }
  /**
   * Sets the vertical selected zoom.
   * Should only be called from event dispatch thread.
   *
   * @param zoom	new selected vertical zoom
   */
  private void setSelectedZoomV(double zoom) {
    // set nearest zoom choice
    int nearestIndex = 0;
    double nearestError = Math.abs(zoomChoices[nearestIndex] - zoom);
    for(int i=1; i<zoomChoices.length-1; i++) {
      double error = Math.abs(zoomChoices[i] - zoom);
      if(error < nearestError) {
	nearestError = error;
	nearestIndex = i;
      }
    }
    if(zoomVCH.getSelectedIndex() != nearestIndex)
      zoomVCH.setSelectedIndex(nearestIndex);
  }
  /**
   * Sets the horizontal selected zoom.
   * Should only be called from event dispatch thread.
   *
   * @param zoom	new selected horizontal zoom
   */
  private void setSelectedZoomH(double zoom) {
    int nearestIndex = 0;
    if(zoom <= 0) {
      nearestIndex = zoomChoices.length-1;
      zoomHValueTF.setEnabled(false);
    }
    else {
      // set nearest zoom choice
      double nearestError = Math.abs(zoomChoices[nearestIndex] - zoom);
      for(int i=1; i<zoomChoices.length-1; i++) {
        double error = Math.abs(zoomChoices[i] - zoom);
        if(error < nearestError) {
	  nearestError = error;
	  nearestIndex = i;
        }
      }
      zoomHValueTF.setEnabled(true);
    }
    if(zoomHCH.getSelectedIndex() != nearestIndex)
      zoomHCH.setSelectedIndex(nearestIndex);
  }
  /**
   * Gets current vertical selected zoom.
   * Should only be called from event dispatch thread.
   *
   * @return	current selected vertical zoom
   */
  private double getSelectedZoomV() {
    return zoomChoices[zoomVCH.getSelectedIndex()];
  }
  /**
   * Gets current horizintal selected zoom.
   * Should only be called from event dispatch thread.
   *
   * @return	current selected horizontal zoom
   */
  private double getSelectedZoomH() {
    synchronized(zoomLock) {
      double zoom = zoomChoices[zoomHCH.getSelectedIndex()];
      if(zoom <= 0) zoomHValueTF.setEnabled(false);
      else zoomHValueTF.setEnabled(true);
      return(zoom);
    }
  }
  /**
   * Sets current filter sample type.
   *
   * @param filterSampleType	one of FilterSampling.REPLICATE or
   *				FilterSampling.INTERPOLATE
   */
  public void setFilterSampleType(int filterSampleType) {
    for(int i=0; i<FilterSampling.SAMPLING_TYPES.length; i++) {
      if(filterSampleType == FilterSampling.SAMPLING_TYPES[i]) {
	if(filterSampleTypeCH.getSelectedIndex() != i) 
	  filterSampleTypeCH.setSelectedIndex(i);
	return;
      }
    }
  }
  /**
   * Gets current selected filter sample type.
   *
   * @return	one of FilterSampling.REPLICATE or FilterSampling.INTERPOLATE
   */
  public int getFilterSampleType() {
    int index = filterSampleTypeCH.getSelectedIndex();
    if(index < 0 || index >= FilterSampling.SAMPLING_TYPES.length)
      return FilterSampling.UNKNOWN_SAMPLE_TYPE;
    else return FilterSampling.SAMPLING_TYPES[index];
  }
}
