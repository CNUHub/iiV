package iiv.display;
import iiv.*;
import iiv.data.*;
import iiv.util.*;
import iiv.filter.*;
import iiv.dialog.*;
import iiv.script.*;
import iiv.io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.lang.reflect.*;
import java.awt.datatransfer.*;

/**
 * This is the primary display container window for iiV.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUViewer
 * @see		java.awt.Container
 * @since	iiV1.0
 */
public class CNUDisplay extends JComponent
    implements ShowStatus, MouseListener,
	   MouseMotionListener, KeyListener,
	   ScaleInterface
{
  private static final long serialVersionUID = 7724568976915758204L;
  // popup menus
  private Object imagePMLock = new Object();
  private boolean triedImagePM;
  private JPopupMenu imagePM;

  private Object localLock = new Object();
  private CNUViewer cnuvPtr = null;
  private CNUDialog spdPtr = null;
  private ShowPointTracker sptPtr = null;

  // Text justification
  private int justification = DisplayText.LEFT;
  // transparent color for clarify/unclarify option
  int newTransparentColor = -2;

  // component that share this background color
  private Vector<Component> commonBackgroundColorList = new Vector<Component>(4);

  // holds undo redo handler
  private UndoRedo undoRedo = null;

  // layout variables
  public static final int DEFAULT_NUMBER_OF_COLUMNS = 6;
  private DefaultBoundedRangeModel numberOfColumnsModel =
      new DefaultBoundedRangeModel(DEFAULT_NUMBER_OF_COLUMNS,
				   0, 1, DEFAULT_NUMBER_OF_COLUMNS * 2);

  private int spacing = 5;
  private int nextCol = 1;
  private int rowHeight = 0;
  private Point nextLocation = new Point(spacing, spacing);

  private static Font cursorFont = null;
  private static Point fontOffsets = new Point(0,0);

  private class DisplayInsertCursor extends Rectangle {
      private static final long serialVersionUID = 8712024018163130857L;
      public int numberofcolumns = -1;
      public int nextcolumn = -1;
      public DisplayInsertCursor(int x, int y, int width, int height) {
	  super(x, y, width, height);
      };
      public DisplayInsertCursor(int x, int y, int width, int height,
				 int ncols, int nextcol) {
	  super(x, y, width, height);
	  numberofcolumns = ncols;
	  nextcolumn = nextcol;
      };
      public DisplayInsertCursor getCursor() {
	  return new DisplayInsertCursor(x,y,width,height,
					 numberofcolumns, nextcolumn);
      }
      public boolean equals(Object obj) {
	  if( obj instanceof DisplayInsertCursor) {
	    DisplayInsertCursor testObj = (DisplayInsertCursor) obj;
	    if((numberofcolumns == testObj.numberofcolumns) &&
	       (nextcolumn == testObj.nextcolumn)) return super.equals(obj);
	  }
	  return false;
      }
      public void setNumberOfColumns(int noc) {numberofcolumns = noc; }
      public void setNextColumn(int next) {nextcolumn = next; }

      public void paint(Graphics g) {
	g.drawLine(x, y, x + width, y);
	g.drawLine(x, y, x, y + height);
	if(numberofcolumns > 0) {
	  if(cursorFont == null) {
	    synchronized (fontOffsets) {
	      if(cursorFont == null) {
		cursorFont = getFont();
		FontMetrics fm = getFontMetrics(cursorFont);
		  //		    Toolkit.getDefaultToolkit().getFontMetrics(cursorFont);
		fontOffsets.setLocation(5, 5 + fm.getLeading() + fm.getAscent());
	      }
	    }
	  }
	  g.setFont(cursorFont);
	  g.drawString((numberofcolumns - 1) + "|" +
		       (numberofcolumns - nextcolumn + 1),
		       x + fontOffsets.x, y + fontOffsets.y);
	}
      }
  };
  private DisplayInsertCursor nextLocationCursor =
    new DisplayInsertCursor(spacing, spacing, 40, 40);
  // selection and mouse motion variables
  private Object mouseListenerLock = new Object();
  private MouseListener currentMouseListener = null;

  public final static int EVENT_RELATIVE_TO_COMPONENT_ON_SCREEN = 0;
  public final static int EVENT_RELATIVE_TO_START_COMPONENT_ON_SCREEN = 1;
  // location component was at beginning of drag
  private Point saveEventComponentOrigin = new Point();
  // mode for calculating absolute location of drag
  private int dragEventPositioningMode = EVENT_RELATIVE_TO_START_COMPONENT_ON_SCREEN;

  private Color selectColor = Color.blue;
  private Vector<Component> selectedObjects = new Vector<Component>(4, 4);

  private Component currentComp = null;
  private ComponentGroup currentGroup = null;

  private boolean itIsADrag = false;
  private Point startDragPt = new Point(0, 0);
  private Point startScrollPosition = new Point(0, 0);

  private boolean cursorChanged = false;
  private Cursor mainCursor = null;
  private Component[] cursorComponents = null;
  private Cursor[] componentCursors = null;

  private DisplayInsertCursor insertDragCursor = null;
  private Point startInsertPosition = new Point(0, 0);

  private Point dragAmount = new Point(0, 0);
  private Component[] dragList = null;
  private Point[] predragLocations = null;

  private Point selectPoint = null;
  private Rectangle selectBox = null;

  private Point translateAmount = new Point(0, 0);

  private ShowPointImage showPointComponent = null;
  private Point showPointComponentOffset = new Point(0, 0);
  private int[] crosshairIndices = null;

  // select region
  private Rectangle displayedSelectRegion = null;
  // crop box
  private Rectangle displayedCropBox = null;
  private Component displayedCropComp = null;
  // key press processing
  private String stepString = "";
  private int incrementStep = 1;
  private int arrowScrollStep = 1;
  // grid stuff
  private boolean gridState = false;
  private int screenResolution = 0;
  private double screenResCorrection = 1.0;
  private double gridOffsetX = 0;
  private double gridOffsetY = 0;
  private double gridSpacing = 0;
  private Color gridColor = Color.yellow;
  private boolean paperState = false;
  private double paperOffsetX = 0;
  private double paperOffsetY = 0;
  private double paperWidth = 0;
  private double paperHeight = 0;
  private Color paperColor = Color.red;
  /** bad paper units */
  public final static int BAD_UNITS = -1;
  /** pixel units */
  public final static int PIXELS = 0;
  /** inch units */
  public final static int INCHES = 1;
  /** centimeter units */
  public final static int CM = 2;
  /**
   * Converts a string representation of units into integer flag.
   *
   * @param s	string representation of units
   * @return	units flag
   */
  public final static int stringToDisplayUnitValue(String s) {
    if("pixels".equalsIgnoreCase(s)) return PIXELS;
    if("in".equalsIgnoreCase(s)) return INCHES;
    if("inches".equalsIgnoreCase(s)) return INCHES;
    if("cm".equalsIgnoreCase(s)) return CM;
    if("centimeters".equalsIgnoreCase(s)) return CM;
    return BAD_UNITS;
  }
  /** this display word type */
  public final static int DISPLAY_TYPE = CNUTypes.UNSIGNED_BYTE;
  //  static int DISPLAY_TYPE = CNUTypes.INTEGER;

  // constants associated with apply functions
  /** flag for applying a translation */
  public final static int TRANSLATE = 1;
  /** flag for applying cropping */
  public final static int CROP = 2;
  /** flag for uncropping */
  public final static int UNCROP = 3;
  /** flag for applying zoom */
  public final static int ZOOM = 4;
  /** flag for applying slice increment */
  public final static int INCREMENT = 5;
  /** flag for applying font */
  public final static int FONT = 6;
  /** flag for applying text justification */
  public final static int TEXT_JUSTIFICATION = 7;
  /** flag for applying text color */
  public final static int TEXT_COLOR = 8;
  /** flag for applying coordinate map */
  public final static int COORDINATE_MAP = 9;
  /** flag for applying scale */
  public final static int SCALE = 10;
  /** flag for applying flips */
  public final static int FLIPS = 11;
  /** flag for applying labels */
  public final static int LABELS = 12;
  /** flag for applying a color model */
  public final static int COLOR_MODEL = 13;
  /** flag for applying a transparent color */
  public final static int TRANSPARENT_COLOR = 14;
  /** flag for snapping to top */
  public final static int SNAP_TOP = 15;
  /** flag for snapping to bottom */
  public final static int SNAP_BOTTOM = 16;
  /** flag for snapping to left */
  public final static int SNAP_LEFT = 17;
  /** flag for snapping to right */
  public final static int SNAP_RIGHT = 18;
  /** flag for applying rotation */
  public final static int ROTATION = 19;
  /** flag for clearing crosshair */
  public final static int CLEAR_CROSSHAIR = 20;
  /** flag for applying number format */
  public final static int NUMBER_FORMAT = 21;
  /** flag for applying filter sampling */
  public final static int FILTER_SAMPLING = 22;
  /** flag for applying data slicer */
  public final static int DATA_SLICER = 23;
  /** flag for applying iValue increment */
  public final static int INCREMENT_IVALUE = 24;
  public final static String getApplyFunctionName(int function) {
      switch(function) {
      default:
	  return "unknown";
      case TRANSLATE:
	  return "translate";
      case CROP:
	  return "crop";
      case UNCROP:
	  return "uncrop";
      case ZOOM:
	  return "zoom";
      case INCREMENT:
	  return "increment";
      case FONT:
	  return "font";
      case TEXT_JUSTIFICATION:
	  return "justification";
      case TEXT_COLOR:
	  return "text color";
      case COORDINATE_MAP:
	  return "coordinate map";
      case SCALE:
	  return "scale";
      case FLIPS:
	  return "flips";
      case LABELS:
	  return "labels";
      case COLOR_MODEL:
	  return "color model";
      case TRANSPARENT_COLOR:
	  return "transparent color";
      case SNAP_TOP:
	  return "snap top";
      case SNAP_BOTTOM:
	  return "snap bottom";
      case SNAP_LEFT:
	  return "snap left";
      case SNAP_RIGHT:
	  return "snap right";
      case ROTATION:
	  return "rotation";
      case CLEAR_CROSSHAIR:
	  return "clear crosshair";
      case NUMBER_FORMAT:
	  return "number format";
      case FILTER_SAMPLING:
	  return "filter sampling";
      case DATA_SLICER:
	  return "data slicer";
      case INCREMENT_IVALUE:
	  return "increment ivalue";
      }
  }

  /** 
   * Constructs a new instance of this class with no image displayed.
   */
  public CNUDisplay(CNUViewer cnuv) {
    this.cnuvPtr = cnuv;
    setLayout(null);
    addKeyListener(this);
    restoreMouseListeners(null);
    addDisplayBackgroundColorComponent(this);
  }
  /**
   * Shows the image popup menu over a component.
   *
   * @param invoker component popup is relative to
   * @param x column position over invoker
   * @param y row position over invoker
   */
  private void showImagePopupMenu(Component comp, int x, int y) {
    if((imagePM == null) && (! triedImagePM)) synchronized (imagePMLock) {
      if((imagePM == null) && (! triedImagePM)) {
        triedImagePM = true;
	Vector<Object> params = new Vector<Object>(1);
	params.addElement(getCNUViewer());
	imagePM = (JPopupMenu) CNUDisplayScript.getObject("iiv.display.DisplayImagePopupMenu",
							  params, null);
      }
    }
    if(imagePM != null) imagePM.show(comp, x, y);
    else Toolkit.getDefaultToolkit().beep();
  }
  /**
   * Allows transversing of focus. Canvas don't normally transverse focus
   *
   * @return	<code>true</code> to all focus transversing
   */
  public boolean isFocusTraversable() { return true; }
  /**
   * Parse the current outstanding step string.
   * Should only be called from event processing thread.
   *
   * @param defaultStep	default number to return
   * @return 		number stored in stepString or default number
   *			on empty or error parsing stepString
   */
   private int parseStepString(int defaultStep) {
     int step = defaultStep;
     if(stepString != "") {
       try {
         step = Integer.parseInt( stepString );
       } catch (NumberFormatException e) {
       }
       stepString = "";
     }
     return step;
   }
  /**
   * Processes key typed events to allow incrementing of slices with +/-.
   * Should only be called from event processing thread.
   *
   * @param evt	key event.
   */
  public void keyTyped(KeyEvent evt) {
    endDrag(); // make sure no outstanding drags
    char c = evt.getKeyChar();
    if( (c >= '0') && (c <= '9') ) {
      stepString += c;
    }
    else if( (c == '+') || (c == '-') ) {
      int incrementStep = parseStepString(getIncrementStep());
      if(incrementStep == 0) {
	incrementStep = 1;
	Toolkit.getDefaultToolkit().beep();
      }
      incrementStep = Math.abs(incrementStep);
      if(c == '-') incrementStep = -incrementStep;
      if(evt.isAltDown())incrementIValue(incrementStep);
      else incrementSlices(incrementStep);
    }
    else {
      // step string canceled if other characters typed
      stepString = "";
      if(c == 'C') {
	dragEventPositioningMode = EVENT_RELATIVE_TO_COMPONENT_ON_SCREEN;
	System.out.println("EVENT_RELATIVE_TO_COMPONENT_ON_SCREEN");
      }
      else if(c == 'S') {
	dragEventPositioningMode = EVENT_RELATIVE_TO_START_COMPONENT_ON_SCREEN;
	System.out.println("EVENT_RELATIVE_TO_START_COMPONENT_ON_SCREEN");
      }
      else Toolkit.getDefaultToolkit().beep();
    }
  }
  /**
   * Processes key presses.
   * Should only be called from event processing thread.
   *
   * @param evt	key event.
   */
  public void keyPressed(KeyEvent evt) {
    Point amount;
    switch (evt.getKeyCode()) {
    case KeyEvent.VK_LEFT:
      endDrag(); // make sure no outstanding drags
      arrowScrollStep = Math.max(parseStepString(arrowScrollStep), 1);
      amount = new Point(-arrowScrollStep, 0);
      if(evt.isControlDown()) translateSelections(amount);
      else if(evt.isAltDown()) translateCurrentCrosshairs(amount);
      else translateScrollPosition(amount, null);
      break;
    case KeyEvent.VK_RIGHT:
      endDrag(); // make sure no outstanding drags
      arrowScrollStep = Math.max(parseStepString(arrowScrollStep), 1);
      amount = new Point(arrowScrollStep, 0);
      if(evt.isControlDown()) translateSelections(amount);
      else if(evt.isAltDown()) translateCurrentCrosshairs(amount);
      else translateScrollPosition(amount, null);
      break;
    case KeyEvent.VK_UP:
      endDrag(); // make sure no outstanding drags
      arrowScrollStep = Math.max(parseStepString(arrowScrollStep), 1);
      amount = new Point(0, -arrowScrollStep);
      if(evt.isControlDown()) translateSelections(amount);
      else if(evt.isAltDown()) translateCurrentCrosshairs(amount);
      else translateScrollPosition(amount, null);
      break;
    case KeyEvent.VK_DOWN:
      endDrag(); // make sure no outstanding drags
      arrowScrollStep = Math.max(parseStepString(arrowScrollStep), 1);
      amount = new Point(0, arrowScrollStep);
      if(evt.isControlDown()) translateSelections(amount);
      else if(evt.isAltDown()) translateCurrentCrosshairs(amount);
      else translateScrollPosition(amount, null);
      break;
    default:
      break;
    }
  }
  /**
   * Processes key releases by ignoring them.
   * Required to implement KeyListener.
   *
   * @param evt	key event.
   */
  public void keyReleased(KeyEvent evt) {}
  /** 
   * Sets the slice increment for +/- incrementing.
   * Should only be called from event processing thread.
   *
   * @param incrementStep	number of slices to increment by
   */
  public void setIncrementStep( int incrementStep ) {
    this.incrementStep = incrementStep;
  }
  /**
   * Gets the slice increment for +/- incrementing.
   *
   * @return	number of slices to increment by
   */
  public int getIncrementStep() { return incrementStep; }
  /**
   * Gets the group currently selected.
   *
   * @return	current current group or <code>null</code>
   */
  public ComponentGroup getCurrentGroup() { return currentGroup; }
  /**
   * Gets the first component currently selected.
   *
   * @return	current selected component or <code>null</code>
   */
  public Component getCurrentComponent() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = getCurrentComponent(); }
      };
      runWithReturn.invokeAndWait();
      return (Component) runWithReturn.returnObject;
    }
    // return current selection
    Component comp = currentComp;
    if(comp != null) return comp;
    if(! selectedObjects.isEmpty()) {
      comp = selectedObjects.firstElement();
      return comp;
    }
    return null;
  }
  /**
   * Gets the data slicer of the currently selected component.
   *
   * @return data slicer of current component or default slicer
   */
  public CNUDataSlicer getCurrentDataSlicer() {
    CNUDataSlicer ds = null;
    Component comp = getCurrentComponent();
    if(comp instanceof SingleImg) ds = ((SingleImg) comp).getDataSlicer();
    if(ds == null)
      ds = new PrimaryOrthoDataSlicer(null, DisplayComponentDefaults.getSliceViewMode(), CNUTypes.UNSIGNED_BYTE);
    return ds;
  }
  /**
   * Gets the color model of the currently selected component.
   *
   * @return color model of current component or default color model
   */
  public ColorModel getCurrentColorModel() {
    ColorModel cm = null;
    Component comp = getCurrentComponent();
    if(comp != null) cm = comp.getColorModel();
    if(cm == null) cm = DisplayComponentDefaults.getDefaultColorModel();
    return cm;
  }
  /**
   * Returns the current components coordinate mapping class or the default.
   *
   * @return	coordinate map or <code>null</code>
   */
  public CoordinateMap getCoordinateMap() {
    Component comp = getCurrentComponent();
    if(comp instanceof CoordinateMappable) {
      CoordinateMap cm = ((CoordinateMappable) comp).getCoordinateMap();
      if(cm != null) return cm;
    }
    return LinearCoordinateMap.getDefaultCoordinateMap();
  }
  /**
   * Gets all displayed color models that are flagged as unsaved.
   *
   * @return vector of unsaved colormodels or <code>null</code>
   */
  public Vector<ColorModel> getUnsavedColorModels() {
    Vector<ColorModel> unsavedList = null;
    Component[] components = getComponents();
    for(int i=0; i<components.length; i++) {
      Component comp = components[i];
      ColorModel cm = null;
      cm = comp.getColorModel();
      if(cm instanceof CNUColorModel) {
	CNUColorModel cnucm = (CNUColorModel) cm;
	if(! cnucm.getSaved()) {
	  if( unsavedList == null ) unsavedList = new Vector<ColorModel>(2, 2);
	  if( ! unsavedList.contains(cnucm) )
	    unsavedList.addElement(cnucm);
        }
      }
    }
    return unsavedList;
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
   * Sets the text justification.
   *
   * @param justification	DisplayText.LEFT, DisplayText.RIGHT, or
   *			DisplayText.CENTERED
   */
  public void setJustification(int justification) {
    this.justification = justification;
  }
  /**
   * Gets the text justification setting.
   *
   * @return	DisplayText.LEFT, DisplayText.RIGHT, or
   *		DisplayText.CENTERED
   */
  public int getJustification() { return justification; }
  /**
   * Gets the currently selected displayed text justification or
   * the current setting if there is no text displayed.
   *
   * @return	DisplayText.LEFT, DisplayText.RIGHT, or
   *		DisplayText.CENTERED
   */
  public int getCurrentJustification() {
    Component comp = getCurrentComponent();
    if(comp instanceof DisplayText)
      return ((DisplayText) comp).getJustification();
    // defaults to standard getJustification()
    return getJustification();
  }
  /**
   * Gets the text of the currently selected object or <code>null</code>
   * if current component doesn't implement getText().
   *
   * @return	text string or <code>null</code>
   */
  public String getCurrentText() {
    return getText(getCurrentComponent());
  }
  /**
   * Gets the text from a single component.
   *
   * @param comp component to get text for
   * @return	text from components getText or <code>null</code>
   */
  public String getText(Component comp) {
    if(comp == null) return null;
    try {
      Method method = comp.getClass().getMethod("getText", new Class[0]);
      return (String) method.invoke(comp, new Object[0]);
    } catch (NoSuchMethodException nsme) { // ignore
    } catch (SecurityException se) { // ignore
    } catch (IllegalAccessException iae) { // ignore
    } catch (IllegalArgumentException iarge) { // ignore
    } catch (InvocationTargetException ite) { // ignore
    } catch (ClassCastException cce) { // ignore
    }
    return null;
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
   * Gets the number of columns range model to allow control panels
   * to add gui components that control and track the number of columns.
   *
   * @return numberOfColumnsModel
   */
  public BoundedRangeModel getNumberOfColumnsModel() {
      return numberOfColumnsModel;
  }
  /**
   * Sets the number of columns to display and increase scrollbar maximum
   * to at least the given possible value.
   *
   * @param numberOfColumns	number of columns
   * @param possible		possible number of columns
   */
  public void setNumberOfColumns(int numberOfColumns, int possible) {
    BoundedRangeModel brm = getNumberOfColumnsModel();
    int maxNoOfCols = Math.max(brm.getMaximum(), possible);
    maxNoOfCols = Math.max(numberOfColumns, maxNoOfCols);
    brm.setMaximum(maxNoOfCols);
    setNumberOfColumns(numberOfColumns);
  }
  /**
   * Sets the number of columns to display when adding new components or
   * relaying out existing components.
   *
   * @param numberOfColumns	number of columns
   */
  public void setNumberOfColumns(int numberOfColumns) {
    if(numberOfColumns < 1) return;
    numberOfColumnsModel.setValue(numberOfColumns);
    if(numberOfColumns > numberOfColumnsModel.getMaximum())
	numberOfColumnsModel.setMaximum(numberOfColumns);
    if(numberOfColumnsModel.getValue() != numberOfColumns) {
	numberOfColumnsModel.setValue(numberOfColumns);
	nextLocationCursor.setNumberOfColumns(numberOfColumns);
    }
  }
  /**
   * Returns the number of columns.
   *
   * @return	number of columns
   */
  public int getNumberOfColumns() { return numberOfColumnsModel.getValue(); }
  /**
   * Returns the vertical zoom of current component or the default.
   *
   * @return	zoom value
   */
  public double getCurrentZoomV() {
    Component comp = getCurrentComponent();
    if(comp instanceof Zoomable) return ((Zoomable)comp).getZoomV();
    return DisplayComponentDefaults.getDefaultZoomV();
  }
  /**
   * Returns the horizontal zoom of current component or the default.
   *
   * @return	zoom value
   */
  public double getCurrentZoomH() {
    Component comp = getCurrentComponent();
    if(comp instanceof Zoomable) return ((Zoomable)comp).getZoomH();
    return DisplayComponentDefaults.getDefaultZoomH();
  }
  /**
   * Returns the rotation angle of current component or the default.
   *
   * @return	rotation angle in degrees
   */
  public double getCurrentRotation() {
    Component comp = getCurrentComponent();
    if(comp instanceof Rotatable) return ((Rotatable)comp).getRotation();
    return DisplayComponentDefaults.getDefaultRotation();
  }
  /**
   * Returns the filter sampling type of the current component.
   *
   * @return	FilterSampling type
   */
  public int getCurrentFilterSampleType() {
    Component comp = getCurrentComponent();
    if(comp instanceof FilterSampling)
      return ((FilterSampling) comp).getFilterSampleType();
    return DisplayComponentDefaults.getDefaultFilterSampleType();
  }
  /**
   * Sets the scaling class.  Only to implement ScaleInterface for the
   * ScaleDialog.
   *
   * @param sc	the CNUScale or <code>null</code>
   */
  public void setScale(CNUScale sc) { CNUScale.setDefaultScale(sc); }
  /**
   * Returns the scaling class of the current component
   * or the default scale.
   *
   * @return	a CNUScale or <code>null</code>
   */
  public CNUScale getScale() {
    Component comp = getCurrentComponent();
    if(comp instanceof ScaleInterface)
      return ((ScaleInterface) comp).getScale();
    return CNUScale.getDefaultScale();
  }
  /**
   * Returns a Scale based on current mode and given type.
   *
   * @return	a CNUScale or <code>null</code>
   */
  public CNUScale getModeScale(int type) {
    CNUViewer cnuv = getCNUViewer();
    if(cnuv != null) return cnuv.getModeScale(type);
    return CNUScale.getDefaultScale();
  }
  /**
   * Returns the current data type from the current component
   * or unknown.
   *
   * @return	data type
   */
  public int getType() {
    Component comp = getCurrentComponent();
    if(comp instanceof ScaleInterface)
      return ((ScaleInterface) comp).getType();
    return CNUTypes.UNKNOWN;
  }
  /**
   * Returns the current quantification factor for scaling.
   * Returns the current quatification factor from the first component it
   * finds that implements ScaleInterface while searching
   * the current component, selected components, and all
   * components, otherwise it returns <code>1.0</code>.
   *
   * @return	quantification factor
   */
  public double getFactor() {
    Component comp = getCurrentComponent();
    if(comp instanceof ScaleInterface)
      return ((ScaleInterface) comp).getFactor();
    return 1.0;
  }
  /**
   * Gets the control panel.
   *
   * @return the CNUViewer or <code>null</code>
   */
  public CNUViewer getCNUViewer() {
    if(cnuvPtr == null) synchronized (getTreeLock()) {
      if(cnuvPtr == null) {
        Component c = this;
	while( !(c instanceof CNUViewer) ) {
	  c = c.getParent();
	  if(c == null)break;
        }
        cnuvPtr = (CNUViewer) c;
      }
    }
    return cnuvPtr;
  }
  /**
   * Gets the show point dialog.
   *
   * @return the show point dialog or <code>null</code>
   */
  public CNUDialog getShowPointDialog() {
    if(spdPtr == null) spdPtr = getCNUViewer().getShowPointDialog();
    return spdPtr;
  }
  /**
   * Gets the show point tracker.
   *
   * @return the show point tracker or <code>null</code>
   */
  public ShowPointTracker getShowPointTracker() {
    if(sptPtr == null) synchronized (localLock) {
      if(sptPtr == null)
	sptPtr = (ShowPointTracker) getCNUViewer().getShowPointController();
    }
    return sptPtr;
  }
  /**
   * Shows the scale dialog.
   */
  public void showScaleDialog() {
    CNUViewer cnuv = getCNUViewer();
    if(cnuv != null) {
      Dialog SD = cnuv.getScaleDialog();
      if(SD != null) SD.setVisible(true);
    }
  }
  /**
   * Shows info about the current component.
   */
  public void showInfo() {
    String info = null;
    Component comp = getCurrentComponent();
    if(comp != null) showStatus(comp.toString());
  }
  /**
   * Shows status and error messages.  Part of the ShowStatus interface.
   *
   * @param s	message to show
   */
  public void showStatus(String s) {
    CNUViewer cnuv = getCNUViewer();
    if(cnuv != null) cnuv.showStatus(s);
    else System.out.println(s);
  }
  /**
   * Shows throwable objects.  Part of the ShowStatus interface.
   *
   * @param t	throwable object to show
   */
  public void showStatus(Throwable t) {
    CNUViewer cnuv = getCNUViewer();
    if(cnuv != null) cnuv.showStatus(t);
    else t.printStackTrace();
  }
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
  public JScrollPane getParentScrollPane() {
    synchronized (getTreeLock()) {
      Component c = this;
      while( !(c instanceof JScrollPane) ) {
        c = c.getParent();
        if(c == null)break;
      }
      return (JScrollPane) c;
    }
  }
  /**
   * Returns the first object associated with a file object from the
   * the component list.
   *
   * @return	object associated with the file object or <code>null</code>
   */
  public Object getFileObject(final Object sameFileObj) {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = getFileObject(sameFileObj); }
      };
      runWithReturn.invokeAndWait();
      return runWithReturn.returnObject;
    }
    int ncmps = getDisplayComponentCount();
    for(int i = 0; i < ncmps; i++) {
      Component comp = getDisplayComponent(i);
      if(comp instanceof CNUFileObject) {
	Object obj = ((CNUFileObject) comp).getFileObject(sameFileObj);
	if(obj != null) return obj;
      }
    }
    return null;
  }
  /**
   * Gets number of currently selected components
   *
   * @return number of selected components
   */
  public int getNumberOfSelectedComponents() {
      return selectedObjects.size();
  }
  /**
   * Gets currently selected components ordered by display level.
   *
   * @return	array of sorted selected object
   */
  public Component[] getSelectedComponentsOrdered() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = getSelectedComponentsOrdered(); }
      };
      runWithReturn.invokeAndWait();
      return (Component[]) runWithReturn.returnObject;
    }
    Component[] selectedComponents = null;
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
    return selectedComponents;
  }
  /**
   * Group selected components into one component.
   */
  public void groupSelectedComponents() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { groupSelectedComponents(); }
      } );
      return;
    }
    // need components ordered to keep relative display levels in group
    groupComponents(getSelectedComponentsOrdered(), true);
  }
  /**
   * Group components. 
   *
   * @param groupList list of components to group.
   * @param undoRedoFlag <code>true</code> to add changes to undo/redo steps
   */
  public void groupComponents(final Vector<Component[]> groupList,
			      final boolean undoRedoFlag) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { groupComponents(groupList, undoRedoFlag); }
      } );
      return;
    }
    if(undoRedoFlag) getUndoRedo().startSteps();
    //    int count = 0;
    Enumeration<Component[]> e = groupList.elements();
    while( e.hasMoreElements() ) {
      Object obj = e.nextElement();
      //	count++;
      Component[] components = (Component[]) obj;
      groupComponents(components, undoRedoFlag);
	/*
      else if((obj instanceof Component) && (count == 0)) {
	count++;
	Component[] components = new Component[groupList.size()];
	groupList.copyInto(components);
	groupComponents(components, undoRedoFlag);
	break;
      }
      else throw new IllegalArgumentException("bad element in group list");
	*/
    }
    if(undoRedoFlag) getUndoRedo().finishUndoSteps("group");
  }
 /**
   * Group components. 
   *
   * @param components_v vector list of components to group.
   * @param undoRedoFlag <code>true</code> to add changes to undo/redo steps
  public void groupComponents(final Vector<Component> components_v,
			      final boolean undoRedoFlag) {
    if(components_v == null) return;
    if(components_v.size() < 2) return;
    Component[] components = new Component[components_v.size()];
    components_v.copyInto(components);
    groupComponents(components, undoRedoFlag);
  }
   */
  /**
   * Group given components into one component.
   *
   * @param components		list of currently displayed components to group
   * @param undoRedoFlag	if <code>true</code> creates undo/redo history
   */
  public void groupComponents(final Component[] components,
			      final boolean undoRedoFlag) {
    if(components == null) return;
    if(components.length < 2) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { groupComponents(components, undoRedoFlag); }
      } );
      return;
    }
    if(undoRedoFlag) getUndoRedo().startSteps();

    // keep track of crosshair and slice trackers since removing components
    // removes these
    ShowPointTracker spt = getShowPointTracker();
    Object restartTrackersObject = null;
    if(spt != null)
      restartTrackersObject = spt.stopComponentTrackers(components);
    Point[] locations = new Point[components.length];
    int minLevel = getComponentCount() - 1;
    boolean selectGroup = selectedObjects.contains(components[0]);
    for(int i = 0; i < components.length; i++) {
      locations[i] = components[i].getLocation();
      int level = getDisplayComponentLevel(components[i]);
      if(level > -1) minLevel = Math.min(minLevel, level);
      remove(components[i], undoRedoFlag);
    }
    ComponentGroup group = new ComponentGroup(components, locations);
    add(group, new CNUDisplayConstraints(group.getLocation(),
					 undoRedoFlag, selectGroup),
	minLevel);
    if(selectGroup) addSelection(group);
    // re-add crosshair and slice trackers
    if(restartTrackersObject != null)
      spt.restartComponentTrackers(restartTrackersObject);

    if(undoRedoFlag) getUndoRedo().finishUndoSteps("group");
  }
  /**
   * Ungroup components from a group into individual components.
   * Does not ungroup subgroups.
   *
   * @param group		Group of components to ungroup
   * @param undoRedoFlag	if <code>true</code> creates undo/redo history
   */
  public void unGroup(final ComponentGroup group,
		      final boolean undoRedoFlag) {
    if(group == null) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { unGroup(group, undoRedoFlag); }
      } );
      return;
    }
    if(undoRedoFlag) getUndoRedo().startSteps();

    Component[] groupComponents = group.getComponents();
    Point origin = group.getLocation();
    int level = getDisplayComponentLevel(group);
    boolean selectComponents = selectedObjects.contains(group);
    // keep track of crosshair and slice trackers since removing components
    // removes these
    ShowPointTracker spt = getShowPointTracker();
    Object restartTrackersObject = null;
    if(spt != null)
      restartTrackersObject = spt.stopComponentTrackers(groupComponents);
    remove(group, undoRedoFlag);
    for(int i = groupComponents.length-1; i >= 0; i--) {
      Point location = groupComponents[i].getLocation();
      location.translate(origin.x, origin.y);
      add(groupComponents[i],
	  new CNUDisplayConstraints(location, undoRedoFlag,
				    selectComponents), level);
      if(level > -1) level++;
    }
    // re-add crosshair and slice trackers
    if(restartTrackersObject != null)
      spt.restartComponentTrackers(restartTrackersObject);

    if(undoRedoFlag) getUndoRedo().finishUndoSteps("ungroup");
  }
  /**
   * Ungroup selected or all grouped components into individual components.
   * Does not ungroup subgroups.
   */
  public void unGroup() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { unGroup(); }
      } );
      return;
    }
    Component[] components = getSelectedComponentsOrdered();
    if(components == null) components = getComponents();
    if(components == null) return;
    getUndoRedo().startSteps();
    for(int i = 0; i < components.length; i++) {
      if(components[i] instanceof ComponentGroup) {
	unGroup( (ComponentGroup) components[i], true);
      }
    }
    getUndoRedo().finishUndoSteps("ungroup");
  }
  /**
   * Overlay image data on top of an existing selected components.
   *
   * @param obj		image to overlay with the same view and location
   * @param group	if <code>true</code> overlayed objects will be
   *			grouped with the object they overlay
   * @return		number of overlays created
   */
  public int overlay(Object obj, boolean group) {
    int count = 0;
    Rectangle repaintArea = null;
    Vector<Object> addList = new Vector<Object>();
    Vector<Component[]> groupList = null;
    if(group) groupList = new Vector<Component[]>();
    Component[] components = getSelectedComponentsOrdered();
    if(components == null) components = getComponents();
    if(components != null) {
      // transverse in reverse order so overlays remain on top in next position
      // could have simply added to end of all components but this allows relayout
      // to show overlay right next to original
      for(int i=components.length - 1; i>=0; i--) {
	if(components[i] instanceof Overlayable) {
	  Component overlayImg =
	    ((Overlayable) components[i]).createOverlay(obj);
	  if(overlayImg != null) {
	    count++;
	    int level = getDisplayComponentLevel(components[i]);
	    if(level > -1) level++;
	    Object[] objects = new Object[3];
	    objects[0] = overlayImg;
	    objects[1] = new CNUDisplayConstraints(components[i].getLocation(),
					       true, getSelectAdditions());
	    objects[2] = new Integer(level);
	    addList.addElement(objects);
	    if(group) {
	      Component[] groupComponents = new Component[2];
	      groupComponents[0] = components[i];
	      groupComponents[1] = overlayImg;
	      groupList.addElement(groupComponents);
	    }
	  }
	}
      }
      if(count > 0) {
        getUndoRedo().startSteps();
	addComponents(addList);
	if(group) groupComponents(groupList, true);
        getUndoRedo().finishUndoSteps("overlay");
      }
    }
    return count;
  }
  /**
   * Displays slices of a CNUData image.
   *
   * @param inImg		the CNUData to display
   * @param sliceViewMode	the view to display
   * @param firstSlice		the first slice to display
   * @param lastSlice		the last slice to display
   * @param firstDimI		the first i dimension to display
   * @param lastDimI		the last i dimension to display
   */
  public void Display(CNUData inImg, int sliceViewMode,
		      int firstSlice, int lastSlice,
		      int firstDimI, int lastDimI) {
      Display(inImg, sliceViewMode, firstSlice, lastSlice,
	      firstDimI, lastDimI, false);
  }
  /**
   * Displays slices of a CNUData image.
   *
   * @param inImg		the CNUData to display
   * @param sliceViewMode	the view to display
   * @param firstSlice		the first slice to display
   * @param lastSlice		the last slice to display
   * @param firstDimI		the first i dimension to display
   * @param lastDimI		the last i dimension to display
   * @param intensityProj       if <code>true</code> create intensity projections
   */
  public void Display(CNUData inImg, int sliceViewMode,
		      int firstSlice, int lastSlice,
		      int firstDimI, int lastDimI,
		      boolean intensityProj) {
    // return if there is nothing to display
    if( inImg == null ) return;
    CNUDimensions inDims = inImg.getDimensions();
    if( inDims.zdim() == 0) return;
    // Determine dimensions associated with slices and needed increments
    int sliceDim = SingleImg.getSliceNumberDimension(inDims, sliceViewMode);

    // Calculate number of I dimensions
    if( firstDimI < 0 ) firstDimI = 0;
    if( firstDimI >= inDims.idim() ) firstDimI = inDims.idim() - 1;
    if( lastDimI < 0 ) lastDimI = inDims.idim() - 1;
    if( lastDimI >= inDims.idim() ) lastDimI = inDims.idim() - 1;
    int incI = 1;
    int cntI = lastDimI - firstDimI + 1;
    if(cntI < 1) {
      incI = -1;
      cntI = firstDimI - lastDimI + 1;
    }

    // Calculate number of slices
    int nslices = inDims.getDim(sliceDim);
    if( firstSlice < 0 ) firstSlice = 0;
    if( firstSlice >= nslices ) firstSlice = nslices - 1;
    if( lastSlice < 0 ) lastSlice = nslices - 1;
    if( lastSlice >= nslices ) lastSlice = nslices - 1;
    int inc = 1;
    int cnt = lastSlice - firstSlice + 1;
    if(cnt < 1) {
      inc = -1;
      cnt = firstSlice - lastSlice + 1;
    }
    // Get the scale factor according to current mode and type
    CNUScale sc = null;
    // if the data has a predefined scale use it instead
    // of DisplayComponentDefaults
    try {
      Method method = inImg.getClass().getMethod("getScale", new Class[0]);
      sc = (CNUScale) method.invoke(inImg, new Object[0]);
    } catch (NoSuchMethodException nsme) { // ignore
    } catch (SecurityException se) { // ignore
    } catch (IllegalAccessException iae) { // ignore
    } catch (IllegalArgumentException iarge) { // ignore
    } catch (InvocationTargetException ite) { // ignore
    } catch (ClassCastException cce) { // ignore
    }
    if(sc == null) sc = getModeScale(inDims.getType());

    // Set the default scale factor for re-use
    CNUScale.setDefaultScale(sc);
    // Get the current display values
    boolean sliceLabelOn = getCNUViewer().getSliceLabelOn();
    boolean iValueLabelOn = getCNUViewer().getIValueLabelOn();
    boolean orientationLabelsOn = getCNUViewer().getOrientationLabelsOn();
    // Create Single images for each slice and add them to this display
    Vector<Object> addList = new Vector<Object>();
    int iValue = firstDimI;
    for( int i=0; i<cntI; i++, iValue += incI) {
      int slice = firstSlice;
      if(! intensityProj) {
	  for( int z=0; z<cnt; z++, slice += inc) {
	      if(slice >= nslices || slice < 0)continue;
	      SingleImg si = new SingleImg(inImg, sliceViewMode,
					   slice, iValue, sc);
	      si.setSliceLabelOn(sliceLabelOn);
	      si.setOrientationLabelsOn(orientationLabelsOn);
	      si.setIValueLabelOn(iValueLabelOn);
	      addList.addElement(si);
	  }
      }
      else {
	  IntensityProjectionImage ipi =
	      new IntensityProjectionImage(inImg, sliceViewMode,
					   firstSlice, lastSlice,
					   iValue, sc);
	  ipi.setSliceLabelOn(sliceLabelOn);
	  ipi.setOrientationLabelsOn(orientationLabelsOn);
	  ipi.setIValueLabelOn(iValueLabelOn);
	  addList.addElement(ipi);
      }
    }
    addComponents(addList);
  }
  /**
   * Add a list of components to the display area.
   *
   * @param addList list to add
   */
  public void addComponents(final Vector<Object> addList) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { addComponents(addList); }
      } );
      return;
    }
    Rectangle repaintArea = null;
    getUndoRedo().startSteps();
    Enumeration e = addList.elements();
    while( e.hasMoreElements() ) {
      Component comp = null;
      Object constraints = null;
      int pos = -1;
      Object obj = e.nextElement();
      if(obj instanceof Component) { comp = (Component) obj; }
      else if(obj instanceof Object[]) {
	Object[] objArray = (Object[]) obj;
	comp = (Component) objArray[0];
	constraints = objArray[1];
	pos = ((Integer) objArray[2]).intValue();
      }
      else throw new IllegalArgumentException("bad element in add list");
      add(comp, constraints, pos);
      if(repaintArea == null) repaintArea = comp.getBounds();
      else repaintArea.add(comp.getBounds());
    }
    getUndoRedo().finishUndoSteps("add");
    // Calculate layout size and repaint
    if(repaintArea != null) {
      if(getSelectAdditions()) repaintArea.grow(1, 1);
      repaint(repaintArea.x, repaintArea.y, repaintArea.width,
	      repaintArea.height);
    }
  }
  /**
   * Displays a colormap as a new DisplayColorMap component.
   *
   * @param cm		ColorModel to display
   * @param orientation	selects horizontal (DisplayColorMap.HORIZONTAL)
   *			or vertical (DisplayColorMap.VERTICAL) display
   */
  public void addDisplayColorMap(ColorModel cm, int orientation) {
    DisplayColorMap dcm = new DisplayColorMap(cm);
    dcm.setOrientation(orientation);
    dcm.setLabelsOn(getCNUViewer().getOrientationLabelsOn());
    dcm.setScale(getModeScale(CNUTypes.UNSIGNED_BYTE));
    addAndRepaint(dcm);
  }
  /**
   * Displays a colormap as a new DisplayColorMapQuilt component.
   *
   * @param cm		ColorModel to display
   */
  public void addDisplayColorMapQuilt(ColorModel cm) {
    DisplayColorMapQuilt dcmq = new DisplayColorMapQuilt(cm);
    dcmq.setLabelsOn(getCNUViewer().getOrientationLabelsOn());
    dcmq.setScale(getModeScale(CNUTypes.UNSIGNED_BYTE));
    addAndRepaint(dcmq);
  }
  /**
   * Adds a component for displaying and repaints its bounds.
   *
   * @param dc	component to add
   */
  public void addAndRepaint(final Component dc) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { addAndRepaint(dc); }
      } );
      return;
    }
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
  public boolean isImmediateParentOf(Object comp) {
    if(! (comp instanceof Component)) return false;
    return (getDisplayComponentLevel((Component) comp) > -1);
  }
  /**
   * Returns <code>true</code> if this display contains the Component.
   *
   * @param comp	component to check for
   * @return	<code>true</code> if this display contains comp
  public boolean contains(Object comp) {
    if(! (comp instanceof Component)) return false;    
    synchronized(getTreeLock()) {
      return isAncestorOf((Component) comp);
    }
  }
   */
  /**
   * Sets select additions mode. If <code>true</code> further
   * added objects will be selected.
   *
   * @param selectAdditions	<code>true</code> to select
   *				or <code>false</code> to not select
   *				further additions
   */
  public void setSelectAdditions(boolean selectAdditions) {
    getCNUViewer().setSelectAdditions(selectAdditions);
  }
  /**
   * Checks if added objects are being automaticly selected.
   *
   * @return	<code>true</code> if additions are selected
   *		or <code>false</code> if additions are not selected
   */
  public boolean getSelectAdditions() {
    return getCNUViewer().getSelectAdditions();
  }
  /**
   * Gets the UndoRedo handler for this display.
   *
   * @return UndoRedo handler for this display
   */
  public UndoRedo getUndoRedo() {
    if(undoRedo == null) {
      CNUViewer cnuv = getCNUViewer();
      if(cnuv != null) undoRedo = cnuv.getUndoRedo();
      else undoRedo = new UndoRedo(null, this, true);
      if(! undoRedo.getEventThreadForced())
	throw new RuntimeException("Invalid UndoRedo object for CNUDisplay");
    }
    return undoRedo;
  }
  /**
   * Sets the UndoRedo handler for this display.
   *
   * @param ur new UndoRedo handler for this display.
   *	       if <code>null</code> a default handler will be created
   */
  public void setUndoRedo(UndoRedo ur) {
    if(ur != null) if(! ur.getEventThreadForced())
      throw new RuntimeException(
	"SetUndoRedo called with invalid UndoRedo object for CNUDisplay");
    undoRedo = ur;
  }
  /**
   * Overides add implemenation to allow special processing.
   *
   * @param comp	Component to add
   * @param constraints	CNUDisplayConstraints or <code>null</code>
   * @param pos		where in list components to insert this one
   * @see	java.awt.Container
   */
  protected void addImpl(Component comp, Object constraints, int pos) {
    if(! SwingUtilities.isEventDispatchThread()) {
      final Component fcomp = comp;
      final Object fconstraints = constraints;
      final int fpos = pos;
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { addImpl(fcomp, fconstraints, fpos); }
      } );
      return;
    }
    // make sure components foreground color is explicitly set before
    // call to super.addImpl otherwise components always get foreground
    // color from this container
    if(comp.getForeground() == null) comp.setForeground(getForeground());
    super.addImpl(comp, null, pos);
/* how should I do this?
    comp.addMouseListener(this);
    comp.addMouseMotionListener(this);
*/
    boolean select = getSelectAdditions();
    boolean needsUndo = true;
    boolean needsAddToLayout = true;
    if(constraints instanceof CNUDisplayConstraints) {
      CNUDisplayConstraints cnudconstraints =
	(CNUDisplayConstraints) constraints;
      needsUndo = cnudconstraints.getUndoRedoFlag();
      if(cnudconstraints.getLocationSet()) {
	// do a couple things addToLayout would normally do
	comp.validate();
	comp.setSize(comp.getPreferredSize());
	comp.setLocation(cnudconstraints.getLocation());
	needsAddToLayout = false;
      }
      select = cnudconstraints.getSelect();
    }
    if(select) addSelection(comp);
    if(! needsUndo) {
      if(needsAddToLayout) addToLayout(comp, needsUndo);
    }
    else {
      UndoRedo ur = getUndoRedo();
      ur.startSteps();
      if(needsAddToLayout) addToLayout(comp, needsUndo);
      Object[] undoArgs = { (Object) comp, new Boolean(false) };
      Class[] undoParams = { java.awt.Component.class, Boolean.TYPE};
      DoCommand undo = new DoCommand(this, "remove", undoParams,
				     undoArgs, "add");
      Class[] redoParams = { java.awt.Component.class,
			     java.lang.Object.class,
			     Integer.TYPE};
      Object[] redoArgs = { (Object) comp,
			    new CNUDisplayConstraints(comp.getLocation(),
						      false),
			    new Integer(pos)};
      DoCommand redo = new DoCommand(this, "add", redoParams,
				     redoArgs, "add");
      ur.addUndo(undo, redo, comp, "add");
      ur.finishUndoSteps("add");
    }
    updateActionStates();
  }
  /**
   * Remove a component from the display with undo commands.
   *
   * @param comp	Component to remove
   */
  public void remove(Component comp) { remove(comp, true); }
  /**
   * Remove a component with undo specified by a flag.
   *
   * @param comp	Component to remove
   * @param undoFlag	if <code>true</code> undo commands will be generated
   */
  public void remove(final Component comp, final boolean undoFlag) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { remove(comp, undoFlag); }
      } );
      return;
    }
    // level will be at least 0 if component is in this container
    int level = getDisplayComponentLevel(comp);
    if(level >= 0) {
      Rectangle compBounds = comp.getBounds();
      boolean select = selectedObjects.contains(comp);
      if(select) clearSelection(comp);
      super.remove(comp);
      repaint(compBounds.x, compBounds.y, compBounds.width, compBounds.height);
      if(undoFlag) {
	UndoRedo ur = getUndoRedo();
	ur.startSteps();
	Class[] undoParams = { java.awt.Component.class,
			       java.lang.Object.class,
			       Integer.TYPE};
	Object[] undoArgs = { (Object) comp,
	new CNUDisplayConstraints(compBounds.getLocation(), false),
	new Integer(level) };
	DoCommand undo = new DoCommand(this, "add", undoParams, undoArgs);
        Class[] redoParams = { java.awt.Component.class, Boolean.TYPE };
	Object[] redoArgs = {(Object) comp, new Boolean(false) };
	DoCommand redo = new DoCommand(this, "remove", redoParams, redoArgs);
	ur.addUndo(undo, redo, comp, "remove");
        ur.finishUndoSteps("remove");
      }
    }
    updateActionStates();
  }
  /**
   * Remove all components currently selected.
   */
  public void removeSelections() {
    // Note - cannot utilize apply because this rearranges componentList
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { removeSelections(); }
      } );
    }
    else if(! selectedObjects.isEmpty() ) {
      //      copySelectedToClipboard();
      getUndoRedo().startSteps();
      while(! selectedObjects.isEmpty()) {
	Component comp = selectedObjects.lastElement();
	remove(comp, true);
      }
      getUndoRedo().finishUndoSteps("remove");
    }
    updateActionStates();
  }
  /**
   * Removes all components.
   */
  public void removeAll() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { removeAll(); }
      } );
      return;
    }
    getUndoRedo().startSteps();
    Component[] dcList = getComponents();
    //    copyToClipboard(dcList);
    for(int i= dcList.length; i > 0; i--) remove(dcList[i-1], true);
    resetLayout(true);
    getUndoRedo().finishUndoSteps("remove");
    updateActionStates();
  }

  private Clipboard clipboard = null;
  private boolean triedFetchingClipboard = false;
  public final static int UNKNOWNCLIPBOARD = 0;
  public final static int STRINGSELECTIONONLYCLIPBOARD = 1;
  public final static int ANYCLASSCLIPBOARD = 2;
  public final static int LOCALCLIPBOARD = 3;
  public final static int TEXTCOMPONENTCLIPBOARD = 4;

  public final static int NORMAL_COPY_MODE = 0;
  public final static int SCRIPT_ONLY_COPY_MODE = 1;
  public final static int IMAGE_ONLY_COPY_MODE = 2;
  public final static int TEXT_ONLY_COPY_MODE = 3;

  private int clipboardMode = UNKNOWNCLIPBOARD;
  private Object clipboardLock = new Object();
  private JTextComponent textComponentClipboard = null;

  private Clipboard getClipboard() {
    //    javax.swing.TransferHandler th = new javax.swing.TransferHandler();
    if(clipboard == null && !triedFetchingClipboard) {
      synchronized (clipboardLock) {
	if(clipboard == null && !triedFetchingClipboard) {
	  triedFetchingClipboard = true;
	  try {
	    clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	  } catch (Exception e) {
	    // browsers in particular may not allow access to the system
	    // clipboard
	  }
	  if(clipboard == null) {
	    textComponentClipboard = new JTextArea();
	    clipboardMode = TEXTCOMPONENTCLIPBOARD;
	  /*
	    showStatus("can't access system clipboard");
	    showStatus("creating a local clipboard for cut and paste only within iiV");
	    clipboard = new Clipboard("iiV local clipboard");
	    clipboardMode = LOCALCLIPBOARD;
	  */
	  }
	}
      }
    }
    return clipboard;
  }
  /**
   * Copies currently selected components to the system clipboard.
   *
   */
  public void copySelectedToClipboard() {
    copySelectedToClipboard(NORMAL_COPY_MODE);
  }
  /**
   * Copies currently selected components to the system clipboard.
   *
   * @param copy_mode one of the following copy modes
   *  <code>NORMAL_COPY_MODE, SCRIPT_ONLY_COPY_MODE, IMAGE_ONLY_COPY_MODE, TEXT_ONLY_COPY_MODE </code>
   */
  public void copySelectedToClipboard(int copy_mode) {
      Rectangle region = getDisplayedSelectRegion();
      if(region != null) copyToClipboard(region, copy_mode);
      else {
	  Component[] components = getSelectedComponentsOrdered();
	  if(components != null)
	      copyToClipboard(getSelectedComponentsOrdered(), copy_mode);
	  else copyToClipboard((Rectangle) null, copy_mode);
      }
  }
  /**
   * Copies script, image and/or text to the system clipboard.
   *
   * @param script Script to copy to clipboard
   * @param image image to copy to clipboard
   * @param text text to copy to clipboard
   */
  public void copyToClipboard(String script, Image image, String text) {
    if( (script != null) || (image != null) || (text != null) ) {
      Clipboard clipboard = getClipboard();
      Transferable transfer = null;
      switch (clipboardMode) {
      case ANYCLASSCLIPBOARD:
	transfer = new iiVTransferable(image, script, text);
	clipboard.setContents(transfer, (ClipboardOwner) transfer);
	return;
      default:
      case UNKNOWNCLIPBOARD:
	transfer = new iiVTransferable(image, script, text);
	// synchronize on the clipboard hopefully preventing others from
	// changing its contents
	Transferable contents;
	synchronized (clipboard) {
	  contents = clipboard.getContents(this);
	  // test if clipboard will accept other then StringSelection
	  clipboard.setContents(transfer, (ClipboardOwner) transfer);
	  contents = clipboard.getContents(this);
	}
	if((contents == null) || (contents instanceof StringSelection)) {
	  clipboardMode = STRINGSELECTIONONLYCLIPBOARD;
	  getCNUViewer().setCopyImageEnabled(false);
	}
	else {
	  clipboardMode = ANYCLASSCLIPBOARD;
	  return;
	}
      case STRINGSELECTIONONLYCLIPBOARD:
	if(script != null) transfer = new StringSelection(script);
	else if(text != null) transfer = new StringSelection(text);
	else break;

	clipboard.setContents(transfer, (ClipboardOwner) transfer);
	return;
      case TEXTCOMPONENTCLIPBOARD:
	if(script != null) textComponentClipboard.setText(script);
	else if(text != null) textComponentClipboard.setText(text);
	else break;
	textComponentClipboard.selectAll();
	textComponentClipboard.copy();
	return;
      }
    }
    Toolkit.getDefaultToolkit().beep();
  }
  /**
   * Copies a list of components to the system clipboard.
   *
   * @param components list of components to copy
   */
  public void copyToClipboard(Component[] components) {
    copyToClipboard(components, NORMAL_COPY_MODE);
  }
  /**
   * Copies a list of components to the system clipboard.
   *
   * @param components list of components to copy
   * @param copy_mode one of the following copy modes
   *  <code>NORMAL_COPY_MODE, SCRIPT_ONLY_COPY_MODE, IMAGE_ONLY_COPY_MODE, TEXT_ONLY_COPY_MODE </code>
   */
  public void copyToClipboard(Component[] components, int copy_mode) {
      String script = null;
      String text = null;
      Image image = null;
      if((components != null) && (components.length > 0)) {
	  switch(copy_mode) {
	  default:
	  case NORMAL_COPY_MODE:
	      if(! (clipboardMode == STRINGSELECTIONONLYCLIPBOARD)) {
		  image = toImage(components);
		  text = getText(components);
	      }
	  case SCRIPT_ONLY_COPY_MODE:
	      script = iiVBshScript.getHeaderScript() +
		  toScript(components);
	      break;
	  case IMAGE_ONLY_COPY_MODE:
	      if(! (clipboardMode == STRINGSELECTIONONLYCLIPBOARD)) {
		  image = toImage(components);
	      }
	      break;
	  case TEXT_ONLY_COPY_MODE:
	      text = getText(components);
	      break;
	  }
      }
      copyToClipboard(script, image, text);
  }
  /**
   * Copies a region to the system clipboard.
   *
   * @param region of display area to copy
   * @param copy_mode one of the following copy modes
   *  <code>NORMAL_COPY_MODE, SCRIPT_ONLY_COPY_MODE, IMAGE_ONLY_COPY_MODE, TEXT_ONLY_COPY_MODE </code>
   */
  public void copyToClipboard(Rectangle region, int copy_mode) {
      String script = null;
      String text = null;
      Image image = null;
      switch(copy_mode) {
      default:
      case NORMAL_COPY_MODE:
	  if(! (clipboardMode == STRINGSELECTIONONLYCLIPBOARD)) {
	      image = toImage(region);
	      text = getText(region);
	  }
      case SCRIPT_ONLY_COPY_MODE:
	  script = iiVBshScript.getHeaderScript() +
	      toScript(region);
	  break;
      case IMAGE_ONLY_COPY_MODE:
	  if(! (clipboardMode == STRINGSELECTIONONLYCLIPBOARD)) {
	      image = toImage(region);
	  }
	  break;
      case TEXT_ONLY_COPY_MODE:
	  text = getText(region);
	  break;
      }
      copyToClipboard(script, image, text);
  }
/*
  System.out.print("flavor[" + j + "]= " + flavors[j]);
  System.out.print(" name= " + flavors[j].getHumanPresentableName());
  System.out.print(" mime= " + flavors[j].getMimeType());
  System.out.println(" class= " + flavors[j].getRepresentationClass().getName());
*/
  /**
   * Gets array of possible paste objects.
   *
   * @return array containing clipboard contents and pastable objects.
   */
  private Object[] getPasteOptions() {
    String script = null;
    DataFlavor imageFlavor = null;
    String string = null;
    Transferable contents = null;
    Clipboard clipboard = getClipboard();
    if(clipboardMode == TEXTCOMPONENTCLIPBOARD) {
      textComponentClipboard.setText("");
      textComponentClipboard.paste();
      string = textComponentClipboard.getText();
      if(string != null) {
	if(! "".equals(string)) {
	  StringReader stringreader = new StringReader(string);
	  if(iiVBshScript.isiiVBshScript(stringreader)) script = string;
	  else {
	    stringreader = new StringReader(string); // don't trust reseting reader
	    if(CNUDisplayScript.isCNUDisplayScript(stringreader)) script = string;
	  }
	}
      }
    }
    else if(clipboard != null) {
      contents = clipboard.getContents(this);
      if(contents != null) {
	DataFlavor[] flavors = contents.getTransferDataFlavors();
	for(int j=0; j < flavors.length; j++) {
	  if(flavors[j].getRepresentationClass() == java.lang.String.class) {
	    try {
	      String stringtmp = (String) contents.getTransferData(flavors[j]);
	      if((stringtmp != null) && (! "".equals(stringtmp))) {
		if(string == null) string = stringtmp;
		StringReader stringreader = new StringReader(stringtmp);
		if(iiVBshScript.isiiVBshScript(stringreader)) script = string;
		else {
		  stringreader = new StringReader(string);
		  if(CNUDisplayScript.isCNUDisplayScript(stringreader)) script = string;
		}
	      }
	    } catch (IOException ioe) {
	      System.out.println(ioe);
	    } catch (UnsupportedFlavorException udfe) {
	      System.out.println(udfe);
	    }
	  }
	  else if(flavors[j].getRepresentationClass() ==
		  java.awt.Image.class) {
	    if(imageFlavor == null) imageFlavor = flavors[j];
	  }
	}
      }
    }
    Object[] pasteObjects = null;
    if(script != null || imageFlavor != null || string != null) {
      pasteObjects = new Object[4];
      pasteObjects[0] = contents;
      pasteObjects[1] = script;
      pasteObjects[2] = imageFlavor;
      pasteObjects[3] = string;
    }
    return pasteObjects;
  }
  /**
   * Pastes a given script to the display.
   *
   * @param script String containing the script.
   */
  private void pasteScript(String script) throws IOException {
      getUndoRedo().startSteps();
      StringReader stringreader = new StringReader(script);
      if(iiVBshScript.isiiVBshScript(stringreader)) {
	iiVBshScript.readComponentsFromScript(script,
					      this, getCNUViewer(),
					      null, this);
      }
      else {
	//	stringreader = new StringReader(script); // don't trust reseting reader
	CNUDisplayScript.readComponentsFromScript(stringreader,
						  this, getCNUViewer(),
						  null, this);
      }
      getUndoRedo().finishUndoSteps("paste script");
      return;
  }
  /**
   * Pastes only a script from the clipboard to the display.
   *
   */
  public void pasteScript() {
    Object[] pasteObjects = getPasteOptions();
    if(pasteObjects != null) {
      if(pasteObjects[1] instanceof String) {
	try {
	  pasteScript((String) pasteObjects[1]);
	  return;
	} catch (IOException ioe) {
	  System.out.println(ioe);
	}
      }
    }
    Toolkit.getDefaultToolkit().beep();
  }
  /**
   * Pastes an image of the given DataFlavor to the display.
   *
   * @param imageFlavor DataFlavor of image in contents to display
   * @param  contents Transferable object to get image from
   * @param  DataFlavor of image in contents to display
   */
  private void pasteImage(Transferable contents, DataFlavor imageFlavor)
    throws IOException, UnsupportedFlavorException {
    Image image = (Image) contents.getTransferData(imageFlavor);
    DisplayComponent dc = new ScriptableDisplayComponent();
    dc.setImage(image);
    getUndoRedo().startSteps();
    addAndRepaint(dc);
    getUndoRedo().finishUndoSteps("paste image");
  }
  /**
   * Pastes only an image from the clipboard to the display.
   *
   */
  public void pasteImage() {
    Object[] pasteObjects = getPasteOptions();
    if(pasteObjects != null) {
      if(pasteObjects[2] instanceof DataFlavor) {
	try {
	  pasteImage((Transferable) pasteObjects[0],
		     (DataFlavor) pasteObjects[2]);
	  return;
	} catch (IOException ioe) {
	  System.out.println(ioe);
	} catch (UnsupportedFlavorException udfe) {
	  System.out.println(udfe);
	}
      }
    }
    Toolkit.getDefaultToolkit().beep();
  }
  /**
   * Pastes a given string to the display.
   *
   * @param string String to paste
   */
  private void pasteText(String string) {
    DisplayText dt = new DisplayText(string);
    getUndoRedo().startSteps();
    addAndRepaint(dt);
    getUndoRedo().finishUndoSteps("paste string");
  }
  /**
   * Pastes only a string from the clipboard to the display.
   *
   */
  public void pasteText() {
    Object[] pasteObjects = getPasteOptions();
    if(pasteObjects != null) {
      if(pasteObjects[3] instanceof String) {
	pasteText((String) pasteObjects[3]);
	return;
      }
    }
    Toolkit.getDefaultToolkit().beep();
  }
  /**
   * Pastes clipboard item to display.
   */
  public void pasteClipboardToDisplay() {
    Object[] pasteObjects = getPasteOptions();
    if(pasteObjects != null) {
      if(pasteObjects[1] instanceof String) {
	try {
	  pasteScript((String) pasteObjects[1]);
	  return;
	} catch (IOException ioe) {
	  System.out.println(ioe);
	}
      }
      else if(pasteObjects[2] instanceof DataFlavor) {
	try {
	  pasteImage((Transferable) pasteObjects[0],
		     (DataFlavor) pasteObjects[2]);
	  return;
	} catch (IOException ioe) {
	  System.out.println(ioe);
	} catch (UnsupportedFlavorException udfe) {
	  System.out.println(udfe);
	}
      }
      // display unknown string as display text
      else if(pasteObjects[3] instanceof String) {
	pasteText((String) pasteObjects[3]);
	return;
      }
    }
    Toolkit.getDefaultToolkit().beep();
  }
  /**
   * Returns the number of displayed Components.
   *
   * @return	the number of components
   */
  public int getDisplayComponentCount() { return getComponentCount(); }
  /**
   * Returns a displayed Component from the canvas.
   *
   * @param i	indice to desired component
   * @return	the i'th component or <code>null</code>
   */
  public Component getDisplayComponent(final int i) {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = getDisplayComponent(i); }
      };
      runWithReturn.invokeAndWait();
      return (Component) runWithReturn.returnObject;
    }
    if( (i < 0) || (i >= getComponentCount()) ) return null;
    return getComponent(i);
  }
  /**
   * Returns the last (top most) display Component containing a point location.
   *
   * @param pt	point
   * @return	Component containing the point or <code>null</code>
   */
  public Component getDisplayComponent(final Point pt) {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = getDisplayComponent(pt); }
      };
      runWithReturn.invokeAndWait();
      return (Component) runWithReturn.returnObject;
    }
    int ncmps = getDisplayComponentCount();
    boolean done = false;
    for(int i= ncmps - 1; i >= 0; i--) {
      Component comp = getDisplayComponent(i);
      Rectangle compBounds = comp.getBounds();
      if(compBounds.contains(pt)) {
	Point cpt = new Point(pt.x - compBounds.x, pt.y - compBounds.y);
	done = true;
	/* printed out rgb values for components that implement getImageRGB
	try {
	  Class[] classes = {cpt.getClass()};
	  Object[] objects = {cpt};
	  Method method = comp.getClass().getMethod("getImageRGB", classes);
	  int rgb = ((Integer) method.invoke(comp, objects)).intValue();
	  if(ColorModel.getRGBdefault().getAlpha(rgb) == 0) done = false;
	} catch (NoSuchMethodException nsme) { // ignore
	} catch (SecurityException se) { // ignore
	} catch (IllegalAccessException iae) { // ignore
	} catch (IllegalArgumentException iarge) { // ignore
	} catch (InvocationTargetException ite) { // ignore
	} catch (ClassCastException cce) { // ignore
	}
	*/
      }
      if(done) return comp;
    }
    return null;
  }
  /**
   * Returns the first (bottom most) display Component
   * containing a point location.
   *
   * @param pt	point
   * @return	Component containing the point or <code>null</code>
   */
  public Component getBottomComponent(final Point pt) {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn =
	new RunnableWithReturnObject() {
	  public void run() { returnObject = getBottomComponent(pt); }
	};
      runWithReturn.invokeAndWait();
      return (Component) runWithReturn.returnObject;
    }
    int ncmps = getDisplayComponentCount();
    for(int i=0; i<ncmps; i++) {
      Component comp = getDisplayComponent(i);
      if(comp.getBounds().contains(pt)) return comp;
    }
    return null;
  }
  /**
   * Gets all components under a point location.
   *
   * @param pt	point
   * @return array of components containing the point or <code>null</code>
   */
  public Component[] getDisplayComponents(final Point pt) {
    Component[] components = getComponents();
    Vector<Component> vector = new Vector<Component>();
    for(int i=0; i<components.length; i++) {
      if(components[i].getBounds().contains(pt))
	vector.addElement(components[i]);
    }
    Component[] array = new Component[vector.size()];
    vector.copyInto(array);
    return array;
  }
  /**
   * Gets a Components display level.
   *
   * @param comp	component to get level of
   * @return		level of given component or <code>-1</code> if
   *			component not in this container
   */
  public int getDisplayComponentLevel(final Component comp) {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() {
	  returnObject = new Integer(getDisplayComponentLevel(comp));
	}
      };
      runWithReturn.invokeAndWait();
      return ((Integer) runWithReturn.returnObject).intValue();
    }
    int max = getComponentCount();
    for(int i=0; i < max; i++) {
      if(getComponent(i) == comp) return i;
    }
    return -1;  
  }
  /**
   * Moves a DisplayComponent to the front.
   *
   * @param comp	component to move to the front
   */
  public void displayComponentToFront(Component comp) {
    DisplayComponentToLevel(comp, -1); // last level
  }
  /**
   * Moves a DisplayComponent to the back.
   *
   * @param comp	component to move to the back
   */
  public void displayComponentToBack(Component comp) {
    DisplayComponentToLevel(comp, 0);
  }
  /**
   * Moves a DisplayComponent to a given level.
   *
   * @param comp	component to move to the given level
   * @param newLevel	level to move component to
   */
  public void DisplayComponentToLevel(final Component comp,
				      final int newLevel) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { DisplayComponentToLevel(comp, newLevel); }
      } );
      return;
    }
    int oldLevel = getDisplayComponentLevel(comp);
    if(oldLevel < 0) return;
    Point loc = comp.getLocation();
    boolean select = selectedObjects.contains(comp);
    // removing stops component trackers
    ShowPointTracker spt = getShowPointTracker();
    Object restartTrackersObject = null;
    if(spt != null)
      restartTrackersObject = spt.stopComponentTrackers(new Component[]{comp});
    remove(comp);
      //	if(newLevel < 0) newLevel getComponentCount()-1
    add(comp, new CNUDisplayConstraints(loc, true, select), newLevel);
    // re-add crosshair and slice trackers
    if(restartTrackersObject != null)
      spt.restartComponentTrackers(restartTrackersObject);
  }
  /**
   * Moves the selected images to the front of the other images.
   */
  public void selectedToFront() {
    // Note - cannot utilize apply because this rearranges componentList
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { selectedToFront(); }
      } );
      return;
    }
    Rectangle repaintArea = null;
    Component[] components = getSelectedComponentsOrdered();
    if(components != null) {
      getUndoRedo().startSteps();
      for(int i = 0; i < components.length; i++) {
	Component comp = components[i];
	displayComponentToFront(comp);
	if(repaintArea == null) repaintArea = comp.getBounds();
	else repaintArea.add(comp.getBounds());
      }
      getUndoRedo().finishUndoSteps("front");
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
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { selectedToBack(); }
      } );
      return;
    }
    Rectangle repaintArea = null;
    Component[] components = getSelectedComponentsOrdered();
    if(components != null) {
      getUndoRedo().startSteps();
      for(int i = components.length -1; i >= 0; i--) {
	Component comp = components[i];
	displayComponentToBack(comp);
	if(repaintArea == null) repaintArea = comp.getBounds();
	else repaintArea.add(comp.getBounds());
      }
      getUndoRedo().finishUndoSteps("back");
    }
    if(repaintArea != null) {
      repaintArea.grow(1, 1);
      repaint(repaintArea.x, repaintArea.y, repaintArea.width,
	      repaintArea.height);
    }
  }
  /**
   * Increments slice of selected components.
   *
   * @param increment	amount to increment slice by
   */
  public void incrementSlices( int increment ) {
    setIncrementStep(increment);
    apply(INCREMENT);
  }
  /**
   * Increments slice of selected components.
   *
   * @param increment	amount to increment slice by
   */
  public void incrementIValue( int increment ) {
    setIncrementStep(increment);
    apply(INCREMENT_IVALUE);
  }
  /**
   * Updates flips of already displayed images.
   *
   * @param flipV	vertical flip
   * @param flipH	horizontal flip
   */
  public void updateFlips(boolean flipV, boolean flipH) {
    getCNUViewer().setDefaultFlipV(flipV);
    getCNUViewer().setDefaultFlipH(flipH);
    apply(FLIPS);
  }
  /**
   * Updates labels of already displayed images.
   *
   * @param sliceLabelOn	<code>true</code> to set slice labels on or
   *				<code>false</code> to set slice labels off
   * @param orientationLabelsOn	<code>true</code> to set orientation labels on
   *				or <code>false</code> to set orientation
   *				labels off
   */
  public void updateLabels(boolean sliceLabelOn,
			   boolean orientationLabelsOn) {
    getCNUViewer().setSliceAndOrientationLabels( sliceLabelOn,
						 orientationLabelsOn);
    apply(LABELS);
  }
  /**
   * Updates scale of already displayed images.  Required to implement
   * ScaleInterface.
   */
  public void updateScale() { apply(SCALE); }
  /**
   * Updates coordinate map of already displayed images.
   */
  public void updateCoordinateMap() { apply(COORDINATE_MAP); }
  /**
   * Updates color model of already displayed images.
   *
   * @param cm	ColorModel to apply
   */
  public void updateColorModel(ColorModel cm) {
    getCNUViewer().setDefaultColorModel(cm);
    apply(COLOR_MODEL);
  }
  /**
   * Updates the tranparent color of already displayed images.
   *
   * @param transparentColor	indice of transparent color to apply
   */
  public void updateTransparentColor(final int transparentColor) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { updateTransparentColor(transparentColor); }
      } );
      return;
    }
    newTransparentColor = transparentColor;
    apply(TRANSPARENT_COLOR);
    newTransparentColor = -2;
  }
  /**
   * Updates the text color of already displayed images.
   *
   * @param foregroundColor	color of text
   */
  public void updateForegroundColor(Color foregroundColor) {
    setForeground(foregroundColor);
    apply(TEXT_COLOR);
  }
  /**
   * Updates the text justification of already displayed text.
   */
  public void updateJustification() { apply(TEXT_JUSTIFICATION); }
  /**
   * Updates the fonts of already displayed images.
   */
  public void updateFont() { apply(FONT); }
  /**
   * Returns the crop box of the current, first selected, of first component.
   */
  public Rectangle getCurrentCropBox() {
    Component comp = getCurrentComponent();
    if(comp instanceof Croppable) {
      Rectangle tmpCropBox = ((Croppable) comp).getCrop();
      if(tmpCropBox != null) return tmpCropBox;
    }
    return (DisplayComponentDefaults.getDefaultCropState() ?
	DisplayComponentDefaults.getDefaultCrop() : null);
  }
  /**
   * Applies a function to selected or all components.
   *
   * @param function	function to apply
   * @return		<code>true</code> if any components
   */
  public boolean apply( int function ) {
    if( applyToSelectedComponents(function) ) return true;
    return applyToAllComponents(function);
  }
  /**
   * Applies a function to given components.  Should only be
   * called from event thread.
   *
   * @param function	function to apply
   * @param components  components to apply too
   * @return		<code>true</code> if any components
   */
  public boolean applyToComponents( final int function ,
				    final Component[] components) {
    if((components == null) || (components.length < 1)) return false;
    if(! SwingUtilities.isEventDispatchThread()) return false;
    Rectangle repaintArea = null;
    getUndoRedo().startSteps();
    for(int i=0; i<components.length; i++)
      repaintArea = apply( function, components[i], repaintArea );
    getUndoRedo().finishUndoSteps(getApplyFunctionName(function));
    if(repaintArea != null) {
      //include selection area
      repaintArea.grow(1, 1); // include selection boundary
      repaint(repaintArea.x, repaintArea.y, repaintArea.width,
	      repaintArea.height);
    }
    return true;
  }
  /**
   * Applies a function to all components.
   *
   * @param function	function to apply
   * @return		<code>true</code> if any components
   */
  public boolean applyToAllComponents( final int function ) {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() {
	  returnObject = new Boolean(applyToAllComponents(function));
	}
      };
      runWithReturn.invokeAndWait();
      return ((Boolean) runWithReturn.returnObject).booleanValue();
    }
    return applyToComponents(function, getComponents());
  }
  /**
   * Applies a function to selected components.
   *
   * @param function	function to apply
   * @return		<code>true</code> if any components selected
   */
  public boolean applyToSelectedComponents( final int function ) {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() {
	  returnObject = new Boolean(applyToSelectedComponents(function));
	}
      };
      runWithReturn.invokeAndWait();
      return ((Boolean) runWithReturn.returnObject).booleanValue();
    }
    return applyToComponents(function, getSelectedComponentsOrdered());
  }
  /**
   * Applies a function to a group component.  Only called from apply.
   *
   * @param group	component group to apply to
   * @param function	function to apply
   */
  private void applyToComponentGroup( ComponentGroup group, int function ) {
    // assume undo/redo steps controlled by calling process
    Component[] components = group.getComponents();
    Rectangle repaintArea = null;
    for(int i = 0; i < components.length; i++)
      repaintArea = apply( function, components[i], repaintArea );
  }
  /**
   * Applies a function to a single component.
   * Should only be called from event processing thread.
   *
   * @param function	function to apply
   * @param comp	component to apply to
   * @param repaintArea accumulater for repaint area.  May be <code>null</code>
   * @return		accumulated repaint area.  May be <code>null</code>
   *			if input repaintArea <code>null</code> and component
   *			did not get modified.
   *			
   */
  private Rectangle apply( int function, Component comp,
    Rectangle repaintArea ) {
    if(comp == null) return repaintArea;
    if(repaintArea == null) repaintArea = comp.getBounds();
    else repaintArea.add(comp.getBounds());
    UndoRedo ur = getUndoRedo();
    DoCommand undo = null;
    Class[] undoParams = null;
    Object[] undoArgs = null;
    DoCommand redo = null;
    Class[] redoParams = null;
    Object[] redoArgs = null;
    DoCommand post = null;
    Class[] postParams = null;
    Object[] postArgs = null;
    Rectangle newCropBox = null;
    Method method = null;
    ColorModel oldcm = null;
    ColorModel newcm = null;
    switch (function) {
    case TRANSLATE:
	Point oldLocation = comp.getLocation();
	Point newLocation = new Point(oldLocation.x, oldLocation.y);
        newLocation.translate(translateAmount.x, translateAmount.y);
	if( newLocation.equals(oldLocation) ) break;
        move(comp, newLocation.x, newLocation.y);
        cropBoxUpdate(null, null); // drags invalidate cropBox
        break;
    case SNAP_TOP:
	oldLocation = comp.getLocation();
	double spacing = getGridSpacing();
	double offset = getGridOffsetY();
	int newY = oldLocation.y;
	newY = (int) (Math.round(((double) newY - offset) / spacing)
		      * spacing + offset);
	if(newY == oldLocation.y) break;
	move(comp, oldLocation.x, newY);
        cropBoxUpdate(null, null); // drags invalidate cropBox
	break;
    case SNAP_BOTTOM:
        Rectangle bounds = comp.getBounds();
	newLocation = new Point(bounds.x, bounds.y);
	newY = bounds.y + bounds.height;
	spacing = getGridSpacing();
	offset = getGridOffsetY();
	newY = (int) (Math.round(((double) newY - offset) / spacing)
		      * spacing + offset);
	newY -= bounds.height;
	if(newY == bounds.y) break;
	move(comp, bounds.x, newY);
        cropBoxUpdate(null, null); // drags invalidate cropBox
	break;
    case SNAP_LEFT:
	oldLocation = comp.getLocation();
	spacing = getGridSpacing();
	offset = getGridOffsetX();
	int newX = oldLocation.x;
	newX = (int) (Math.round(((double) newX - offset) / spacing)
		      * spacing + offset);
	if(newX == oldLocation.x) break;
	move(comp, newX, oldLocation.y);
        cropBoxUpdate(null, null); // drags invalidate cropBox
	break;
    case SNAP_RIGHT:
        bounds = comp.getBounds();
	spacing = getGridSpacing();
	offset = getGridOffsetX();
	newX = bounds.x + bounds.width;
	newX = (int) (Math.round(((double) newX - offset) / spacing)
		      * spacing + offset);
	newX -= bounds.width;
	if(newX == bounds.x) break;
	move(comp, newX, bounds.y);
        cropBoxUpdate(null, null); // drags invalidate cropBox
	break;
    case FONT:
	if(comp instanceof ComponentGroup) {
	  applyToComponentGroup( (ComponentGroup) comp, function );
	  break;
	}
        Font oldFont = comp.getFont();
        Font newFont = getFont();
        if(! oldFont.equals(newFont) ) { // comp.getFont() should never be null
	  comp.setFont(newFont);
	  undoParams = new Class[1];
	  undoParams[0] = java.awt.Font.class;
	  undoArgs = new Object[1]; undoArgs[0] = oldFont;
	  undo = new DoCommand(comp, "setFont", undoParams, undoArgs);
	  redoArgs = new Object[1]; redoArgs[0] = newFont;
	  redo = new DoCommand(comp, "setFont", undoParams, redoArgs);
	  ur.addUndo(undo, redo, post, comp, getApplyFunctionName(function));
        }
        break;
    case TEXT_COLOR:
	if(comp instanceof ComponentGroup) {
	  applyToComponentGroup( (ComponentGroup) comp, function );
	  break;
	}
        Color oldForeground = comp.getForeground();
        Color newForeground = getForeground();
        if(! oldForeground.equals(newForeground)) {
          comp.setForeground(newForeground);
	  undoParams = new Class[1];
	  undoParams[0] = Color.blue.getClass();
	  undoArgs = new Object[1]; undoArgs[0] = oldForeground;
	  undo = new DoCommand(comp, "setForeground", undoParams, undoArgs);
	  redoArgs = new Object[1]; redoArgs[0] = newForeground;
	  redo = new DoCommand(comp, "setForeground", undoParams, redoArgs);
	  ur.addUndo(undo, redo, post, comp, getApplyFunctionName(function));
        }
        break;
    case TEXT_JUSTIFICATION:
	if(comp instanceof DisplayText) {
	  DisplayText dt = (DisplayText) comp;
	  int oldJust = dt.getJustification();
	  int newJust = getJustification();
	  if(oldJust != newJust) {
	    dt.setJustification(newJust);
	    undoParams = new Class[1]; undoParams[0] = Integer.TYPE;
	    undoArgs = new Object[1]; undoArgs[0] = new Integer(oldJust);
	    undo = new DoCommand(dt, "setJustification", undoParams, undoArgs);
	    redoArgs = new Object[1]; redoArgs[0] = new Integer(newJust);
	    redo = new DoCommand(dt, "setJustification", undoParams, redoArgs);
	    ur.addUndo(undo, redo, post, comp, getApplyFunctionName(function));
          }
        }
	else if(comp instanceof ComponentGroup)
	  applyToComponentGroup( (ComponentGroup) comp, function );
        break;
    case CROP:
	if(comp instanceof Croppable) {
          newCropBox = DisplayComponentDefaults.getDefaultCropState() ?
		       DisplayComponentDefaults.getDefaultCrop() : null;
	}
	/* ComponentGroup is now Croppable
	  else if(comp instanceof ComponentGroup) {
	  applyToComponentGroup( (ComponentGroup) comp, function );
	  break;
	  }
	*/
	// no break - must be followed by case UNCROP
    case UNCROP:  // uncrop retains newCropBox = null
	if(comp instanceof Croppable) {
	  Croppable croppable = (Croppable) comp;
	  Rectangle oldCropBox = croppable.getCrop();
	  if(newCropBox == null && oldCropBox == null) break;
	  if(newCropBox != null)
	    if(newCropBox.equals(oldCropBox)) break;
	  croppable.setCrop(newCropBox);
	  cropBoxUpdate(null, null); // cropBox no longer valid
	  undoParams = new Class[1];
	  undoParams[0] = new Rectangle().getClass();
	  undoArgs = new Object[1]; undoArgs[0] = (Object) oldCropBox;
	  undo = new DoCommand(comp, "setCrop", undoParams, undoArgs);
	  redoArgs = new Object[1]; redoArgs[0] = (Object) newCropBox;
	  redo = new DoCommand(comp, "setCrop", undoParams, redoArgs);
	  ur.addUndo(undo, redo, post, comp, getApplyFunctionName(function));
	}
	/* ComponentGroup is now Croppable
	   else if(comp instanceof ComponentGroup)
	   applyToComponentGroup( (ComponentGroup) comp, function );
	*/
        break;
    case ZOOM:
	ur.startSteps();
	if(comp instanceof Zoomable) {
	  Zoomable zoomable = (Zoomable) comp;
	  double oldZoomV = zoomable.getZoomV();
	  double newZoomV = DisplayComponentDefaults.getDefaultZoomState() ?
	    DisplayComponentDefaults.getDefaultZoomV() : 1.0;
	  double oldZoomH = zoomable.getZoomH();
	  double newZoomH = DisplayComponentDefaults.getDefaultZoomState() ?
	    DisplayComponentDefaults.getDefaultZoomH() : 1.0;
	  if( (newZoomV != oldZoomV) || (newZoomH != oldZoomH) ) {
	    zoomable.setZoom(newZoomV, newZoomH);
	    undoParams = new Class[2];
	    undoParams[0] = Double.TYPE; undoParams[1] = Double.TYPE;
	    undoArgs = new Object[2];
	    undoArgs[0] = new Double(oldZoomV);
	    undoArgs[1] = new Double(oldZoomH);
	    undo = new DoCommand(comp, "setZoom", undoParams, undoArgs);
	    redoArgs = new Object[2];
	    redoArgs[0] = new Double(newZoomV);
	    redoArgs[1] = new Double(newZoomH);
	    redo = new DoCommand(comp, "setZoom", undoParams, redoArgs);
	    ur.addUndo(undo, redo, post, comp, getApplyFunctionName(function));
	  }
	}
	// filter sampling may also need updating with new zoom
	apply(FILTER_SAMPLING, comp, repaintArea);
	ur.finishUndoSteps(getApplyFunctionName(function));
        break;
    case ROTATION:
	ur.startSteps();
	if(comp instanceof Rotatable) {
	  Rotatable rotatable = (Rotatable) comp;
	  double oldRotation = rotatable.getRotation();
	  double newRotation = 
	    DisplayComponentDefaults.getDefaultRotateState() ?
	    DisplayComponentDefaults.getDefaultRotation() : 0.0;
	  if(newRotation != oldRotation) {
	    rotatable.setRotation(newRotation);
	    undoParams = new Class[1];
	    undoParams[0] = Double.TYPE;
	    undoArgs = new Object[1];
	    undoArgs[0] = new Double(oldRotation);
	    undo = new DoCommand(comp, "setRotation", undoParams, undoArgs);
	    redoArgs = new Object[1];
	    redoArgs[0] = new Double(newRotation);
	    redo = new DoCommand(comp, "setRotation", undoParams, redoArgs);
	    ur.addUndo(undo, redo, post, comp, getApplyFunctionName(function));
	  }
        }
	// filter sampling may also need updating with new rotation
	apply(FILTER_SAMPLING, comp, repaintArea);
	ur.finishUndoSteps(getApplyFunctionName(function));
        break;
    case FILTER_SAMPLING:
	if(comp instanceof FilterSampling) {
	    FilterSampling filterSampling = (FilterSampling) comp;
	    int oldFilterSampleType = filterSampling.getFilterSampleType();
	    int newFilterSampleType =
		DisplayComponentDefaults.getDefaultFilterSampleType();
	    if(newFilterSampleType != oldFilterSampleType) {
		filterSampling.setFilterSampleType(newFilterSampleType);
		undoParams = new Class[1];
		undoParams[0] = Integer.TYPE;
		undoArgs = new Object[1];
		undoArgs[0] = new Integer(oldFilterSampleType);
		undo = new DoCommand(comp, "setFilterSampleType",
				     undoParams, undoArgs);
		redoArgs = new Object[1];
		redoArgs[0] = new Integer(newFilterSampleType);
		redo = new DoCommand(comp, "setFilterSampleType",
				     undoParams, redoArgs);
		ur.addUndo(undo, redo, post, comp, getApplyFunctionName(function));
	    }
        }
	else if(comp instanceof ComponentGroup)
	  applyToComponentGroup( (ComponentGroup) comp, function );
	break;
    case INCREMENT:
        if(comp instanceof SliceNumbering) {
	  SliceNumbering sliceNumbering = (SliceNumbering) comp;
	  int oldSlice = sliceNumbering.getSlice();
	  int newSlice = oldSlice + getIncrementStep();
	  setSlice(sliceNumbering, newSlice);
        }
	else if(comp instanceof ComponentGroup)
	  applyToComponentGroup( (ComponentGroup) comp, function );
        break;
    case INCREMENT_IVALUE:
        if(comp instanceof SingleImg) {
	  SingleImg singleImg = (SingleImg) comp;
	  int oldIValue = singleImg.getIValue();
	  int newIValue = oldIValue + getIncrementStep();
	  setIValue(singleImg, newIValue);
        }
	else if(comp instanceof ComponentGroup)
	  applyToComponentGroup( (ComponentGroup) comp, function );
        break;
    case SCALE:
        if(comp instanceof ScaleInterface) {
	  ScaleInterface si = (ScaleInterface) comp;
	  CNUScale oldScale = si.getScale();
	  CNUScale newScale = CNUScale.getDefaultScale();
	  if( oldScale == newScale ) break;
	  si.setScale(newScale);
	  si.updateScale();
	  undoParams = new Class[1];
	  undoParams[0] = CNUScale.class;
	  undoArgs = new Object[1]; undoArgs[0] = oldScale;
	  undo = new DoCommand(si, "setScale", undoParams, undoArgs);
	  redoArgs = new Object[1]; redoArgs[0] = newScale;
	  redo = new DoCommand(si, "setScale", undoParams, redoArgs);
	  post = new DoCommand(si, "updateScale",
			       (Class[]) null, (Object[]) null);
	  ur.addUndo(undo, redo, post, comp, getApplyFunctionName(function));
        }
	else if(comp instanceof ComponentGroup)
	  applyToComponentGroup( (ComponentGroup) comp, function );
        break;
    case FLIPS:
	if(comp instanceof Flippable) {
	  Flippable flippable = (Flippable) comp;
	  boolean oldFlipV = flippable.getFlipV();
	  boolean oldFlipH = flippable.getFlipH();
	  boolean newFlipV = DisplayComponentDefaults.getDefaultFlipV();
	  boolean newFlipH = DisplayComponentDefaults.getDefaultFlipH();
	  if( (oldFlipV == newFlipV) && (oldFlipH == newFlipH) ) break;
	  flippable.setFlips(newFlipV, newFlipH);
	  undoParams = new Class[2];
	  undoParams[0] = Boolean.TYPE; undoParams[1] = Boolean.TYPE;
	  undoArgs = new Object[2];
	  undoArgs[0] = new Boolean(oldFlipV);
	  undoArgs[1] = new Boolean(oldFlipH);
	  undo = new DoCommand(comp, "setFlips", undoParams, undoArgs);
	  redoArgs = new Object[2];
	  redoArgs[0] = new Boolean(newFlipV);
	  redoArgs[1] = new Boolean(newFlipH);
	  redo = new DoCommand(comp, "setFlips", undoParams, redoArgs);
	  ur.addUndo(undo, redo, post, comp, getApplyFunctionName(function));
	}
	else if(comp instanceof ComponentGroup)
	  applyToComponentGroup( (ComponentGroup) comp, function );
        break;
    case LABELS:
        if(comp instanceof SingleImg) {
	  SingleImg si = (SingleImg) comp;
          boolean oldSliceLabelOn = si.getSliceLabelOn();
          boolean oldOrientationLabelsOn = si.getOrientationLabelsOn();
          boolean oldIValueLabelOn = si.getIValueLabelOn();
          boolean newSliceLabelOn = getCNUViewer().getSliceLabelOn();
          boolean newOrientationLabelsOn = getCNUViewer().getOrientationLabelsOn();
          boolean newIValueLabelOn = getCNUViewer().getIValueLabelOn();
	  if( (oldSliceLabelOn == newSliceLabelOn) &&
	      (oldIValueLabelOn == newIValueLabelOn) &&
	      (oldOrientationLabelsOn == newOrientationLabelsOn) ) break;

	  undoParams = new Class[1];
	  undoParams[0] = Boolean.TYPE;
	  undoArgs = new Object[1];
	  redoArgs = new Object[1];

	  ur.startSteps();
          if( oldSliceLabelOn != newSliceLabelOn) {
	    si.setSliceLabelOn(newSliceLabelOn);
	    undoArgs[0] = new Boolean(oldSliceLabelOn);
	    redoArgs[0] = new Boolean(newSliceLabelOn);
	    undo = new DoCommand(si, "setSliceLabelOn",
				 undoParams, undoArgs);
	    redo = new DoCommand(si, "setSliceLabelOn",
				 undoParams, redoArgs);
	    ur.addUndo(undo, redo, post, si, getApplyFunctionName(function));
	  }
          if( oldIValueLabelOn != newIValueLabelOn) {
	    si.setIValueLabelOn(newIValueLabelOn);
	    undoArgs[0] = new Boolean(oldIValueLabelOn);
	    redoArgs[0] = new Boolean(newIValueLabelOn);
	    undo = new DoCommand(si, "setIValueLabelOn",
				 undoParams, undoArgs);
	    redo = new DoCommand(si, "setIValueLabelOn",
				 undoParams, redoArgs);
	    ur.addUndo(undo, redo, post, si, getApplyFunctionName(function));
	  }
          if( oldOrientationLabelsOn != newOrientationLabelsOn) {
	    si.setOrientationLabelsOn(newIValueLabelOn);
	    undoArgs[0] = new Boolean(oldOrientationLabelsOn);
	    redoArgs[0] = new Boolean(newOrientationLabelsOn);
	    undo = new DoCommand(si, "setOrientationLabelsOn",
				 undoParams, undoArgs);
	    redo = new DoCommand(si, "setOrientationLabelsOn",
				 undoParams, redoArgs);
	    ur.addUndo(undo, redo, post, si, getApplyFunctionName(function));
	  }
	  ur.finishUndoSteps(getApplyFunctionName(function));
        }
	else if( (comp instanceof DisplayColorMap) ||
	  (comp instanceof DisplayColorMapQuilt) ){
	  boolean oldLabelsOn;
	  boolean newLabelsOn = getCNUViewer().getOrientationLabelsOn();
	  if(comp instanceof DisplayColorMap) {
	    DisplayColorMap dcm = (DisplayColorMap) comp;
            oldLabelsOn = dcm.getLabelsOn();
            if(oldLabelsOn == newLabelsOn) break;
	    dcm.setLabelsOn(newLabelsOn);
	  } else {
	    DisplayColorMapQuilt dcmq = (DisplayColorMapQuilt) comp;
            oldLabelsOn = dcmq.getLabelsOn();
            if(oldLabelsOn == newLabelsOn) break;
	    dcmq.setLabelsOn(newLabelsOn);
	  }
	  undoParams = new Class[1];
	  undoParams[0] = Boolean.TYPE;
	  undoArgs = new Object[1];
	  undoArgs[0] = new Boolean(oldLabelsOn);
	  undo = new DoCommand(comp, "setLabelsOn", undoParams, undoArgs);
	  redoArgs = new Object[1];
	  redoArgs[0] = new Boolean(newLabelsOn);
	  redo = new DoCommand(comp, "setLabelsOn", undoParams, redoArgs);
	  ur.addUndo(undo, redo, post, comp, getApplyFunctionName(function));
	}
	else if(comp instanceof ComponentGroup)
	  applyToComponentGroup( (ComponentGroup) comp, function );
        break;
    case NUMBER_FORMAT:
	if(comp instanceof NumberFormattable) {
	  NumberFormattable formattable = (NumberFormattable) comp;
	  NumberFormat oldFormat = formattable.getNumberFormat();
	  NumberFormat newFormat =
	    DisplayNumberFormat.getDefaultNumberFormat();
	  if(newFormat.equals(oldFormat)) break;
	  formattable.setNumberFormat(newFormat);
	  undoParams = new Class[1];
	  undoParams[0] = NumberFormat.class;
	  undoArgs = new Object[1];
	  undoArgs[0] = oldFormat;
	  undo = new DoCommand(comp, "setNumberFormat", undoParams, undoArgs);
	  redoArgs = new Object[1];
	  redoArgs[0] = newFormat;
	  redo = new DoCommand(comp, "setNumberFormat", undoParams, redoArgs);
	  ur.addUndo(undo, redo, post, comp, getApplyFunctionName(function));
	}
	else if(comp instanceof ComponentGroup)
	  applyToComponentGroup( (ComponentGroup) comp, function );
        break;
    case TRANSPARENT_COLOR:
	if(comp instanceof ComponentGroup) {
	  applyToComponentGroup( (ComponentGroup) comp, function );
	  break;
	}
	oldcm = comp.getColorModel();
	if(! (oldcm instanceof IndexColorModel)) break;
	int newTransparentColor = this.newTransparentColor;
        if(newTransparentColor == -2)
	  newTransparentColor = getCNUViewer().getDefaultTransparentColor();
	newcm = CNUColorModel.getTransparentColorModel((IndexColorModel) oldcm,
						       newTransparentColor);
	// no break - must be followed by case COLOR_MODEL
    case COLOR_MODEL:
	if(comp instanceof ComponentGroup) {
	  applyToComponentGroup( (ComponentGroup) comp, function );
	  break;
	}
	// instead of checking class type applies only if method
	// setColorModel(ColorModel cm) is found
	if(oldcm == null) oldcm = comp.getColorModel();
	if(newcm == null) newcm = getCNUViewer().getDefaultColorModel();
	if(oldcm == newcm) break;
	undoParams = new Class[1];
	undoParams[0] = java.awt.image.ColorModel.class;
	try {
	  method = comp.getClass().getMethod("setColorModel", undoParams);
	  redoArgs = new Object[1]; redoArgs[0] = newcm;
	  method.invoke(comp, redoArgs);
	  undoArgs = new Object[1]; undoArgs[0] = oldcm;
	  undo = new DoCommand(comp, method, undoArgs);
	  redo = new DoCommand(comp, method, redoArgs);
	  ur.addUndo(undo, redo, post, comp, getApplyFunctionName(function));
	} catch (NoSuchMethodException nsme) { // ignore
	} catch (SecurityException se) { // ignore
	} catch (IllegalAccessException iae) { // ignore
	} catch (IllegalArgumentException iarge) { // ignore
	} catch (InvocationTargetException ite) { // ignore
	}
        break;
    case COORDINATE_MAP:
	if(comp instanceof CoordinateMappable) {
	  CoordinateMappable coordinateMappable = (CoordinateMappable) comp;
	  CoordinateMap oldCoorMap = coordinateMappable.getCoordinateMap();
	  CoordinateMap newCoorMap =
	    LinearCoordinateMap.getDefaultCoordinateMap();
	  if(oldCoorMap == newCoorMap) break;
	  coordinateMappable.setCoordinateMap(newCoorMap);
	  if(oldCoorMap == coordinateMappable.getCoordinateMap())
	    break; // map not settable
	  undoParams = new Class[1];
	  undoParams[0] = CoordinateMap.class;
	  undoArgs = new Object[1]; undoArgs[0] = oldCoorMap;
	  undo = new DoCommand(comp, "setCoordinateMap", undoParams, undoArgs);
	  redoArgs = new Object[1]; redoArgs[0] = newCoorMap;
	  redo = new DoCommand(comp, "setCoordinateMap", undoParams, redoArgs);
	  ur.addUndo(undo, redo, post, comp, getApplyFunctionName(function));
	}
	else if(comp instanceof ComponentGroup)
	  applyToComponentGroup( (ComponentGroup) comp, function );
        break;
    case CLEAR_CROSSHAIR:
	if(comp instanceof ShowPointImage) {
	  ShowPointTracker spt = getShowPointTracker();
	  if(spt != null)
	    spt.setComponentCrosshair((ShowPointImage) comp,
				      null, null, null, true);
	}
	else if(comp instanceof ComponentGroup)
	  applyToComponentGroup( (ComponentGroup) comp, function );
        break;
    }
    repaintArea.add(comp.getBounds());
    return repaintArea;
  }
  /**
   * Translates current showPointImage crosshairs by a given amount.
   * Called only from event processing thread.
   *
   * @param amount	amount to translate crosshairs by
   */
  private void translateCurrentCrosshairs( Point amount ) {
    if(amount == null) return;
    if((amount.x == 0) && (amount.y == 0)) return;
    Component comp = getCurrentComponent();
    if(comp instanceof ShowPointImage) {
      ShowPointImage spi = (ShowPointImage) comp;
      int[] oldIndices = spi.getCrosshairIndices();
      if(oldIndices != null) {
	Point crosshairPt = spi.getPoint(oldIndices);
	crosshairPt.translate(amount.x, amount.y);
	int[] newIndices = spi.getIndices(crosshairPt);
	while( arraysEqual(oldIndices, newIndices) &&
	       comp.contains(crosshairPt) ) {
	  crosshairPt.translate(amount.x, amount.y);
	  newIndices = spi.getIndices(crosshairPt);
	}
        if(newIndices != null) {
	  ShowPointTracker spt = getShowPointTracker();
	  if(spt != null) spt.setCrosshairs(spi, newIndices);
	}
      }
    }
  }
  /**
   * Translates the selected components.
   *
   * @param amount	amount to translate each component by
   */
  public void translateSelections( final Point amount ) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { translateSelections(amount); }
      } );
      return;
    }
    translateAmount.setLocation(amount.x, amount.y);
    applyToSelectedComponents( TRANSLATE );
  }
  /**
   * Adds a component to the list of components that wish to maintain
   * the same background color as this Container.
   *
   * @param comp	component to add
   */
  public void addDisplayBackgroundColorComponent(Component comp) {
    synchronized (commonBackgroundColorList) {
      if(! commonBackgroundColorList.contains(comp))
	commonBackgroundColorList.addElement(comp);
    }
  }
  /** Removes a component from the list of components that wish to maintain
   * the same background color as this Container.
   *
   * @param comp	component to remove
   */
  public void removeDisplayBackgroundColorComponent(Component comp) {
    synchronized (commonBackgroundColorList) {
      commonBackgroundColorList.removeElement(comp);
    }
  }
  /**
   * Sets the common display background color.
   *
   * @param c	color to set
   */
  public void setDisplayBackground(final Color c) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setDisplayBackground(c); }
      } );
      return;
    }
    synchronized (commonBackgroundColorList) {
      Enumeration<Component> e = commonBackgroundColorList.elements();
      while(e.hasMoreElements()) {
        Component comp = e.nextElement();
	comp.setBackground(c);
	comp.repaint();
      }
    }
  }
  /**
   * Removes cropping from selections or all components.
   */
  public void uncrop() { apply(UNCROP); }
  /**
   * Updates the graphics area.
   * Should only be called from event thread.
   *
   * @param g graphics to update.
   */
  public void update(final Graphics g) {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	  public void run() { update(g); }
	};
      runWithReturn.invokeAndWait();
      return;
    }
    //    if(! SwingUtilities.isEventDispatchThread())
    //      throw new RuntimeException(
    //	"CNUDisplay.update(g) called outside of the event thread");
    if(! isValid()) validate();
    // First clear the area
    g.setColor(getBackground());
    Rectangle r = g.getClipBounds();
    if(r == null) {
      Dimension d = getPreferredSize();
      r = new Rectangle(d.width, d.height);
    } else if(! selectedObjects.isEmpty() ) {
      // grow clip bounds to possibly include selection outline
      Dimension size = getSize();
      r.setBounds( (r.x == 0)? r.x : r.x - 1, (r.y == 0)? r.y : r.y - 1,
		   (r.width == size.width)? r.width : r.width + 2,
		   (r.height == size.height)? r.height : r.height + 2);
      g.setClip(r.x, r.y, r.width, r.height);
    }
    g.fillRect(r.x, r.y, r.width, r.height);
    if(gridState) drawGrid(g, r);
    if(paperState) drawPaper(g);
    Component[] components = getComponents();
    for(int i=0; i<components.length; i++) {
      Component comp = components[i];
      boolean selected = selectedObjects.contains(comp);
      Rectangle compBounds = comp.getBounds();
      if(r.intersects(compBounds)) {
	g.translate(compBounds.x, compBounds.y);
	comp.paint(g);
	g.translate(-compBounds.x, -compBounds.y);
	if((displayedCropComp == comp) && (displayedCropBox != null)) {
	  // also draw crop box
	  g.setColor(Color.blue);
	  SimplePolygon poly =
	    CropDialog.displayPoly(displayedCropBox, comp, null);
	  poly.draw(g);
	}
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
    if(displayedSelectRegion != null) {
      g.setColor(Color.blue);
      g.drawRect(displayedSelectRegion.x, displayedSelectRegion.y,
	         displayedSelectRegion.width, displayedSelectRegion.height);
    }
    if(oldNextLocationCursor != null) xorDrawNextLocation(g, oldNextLocationCursor); 
    if(insertDragCursor != null) xorDrawNextLocation(g, insertDragCursor);
  }
  /**
   * Exclusive or draw or erase the next location cursor.
   */
  private void xorDrawNextLocation(DisplayInsertCursor locationCursor) {
      xorDrawNextLocation(getGraphics(), locationCursor);
  }
  /**
   * Exclusive or draw or erase the next location cursor.
   *
   * @param g graphics object to draw on
   */
  private void xorDrawNextLocation(Graphics g, DisplayInsertCursor locationCursor) {
    g.setXORMode(Color.red);
    locationCursor.paint(g);
  }
  private DisplayInsertCursor drawNextLocationCursor = null;
  private DisplayInsertCursor oldNextLocationCursor = null;
  private boolean nextLocationCursorShow = false;
  private Thread nextLocationCursorThread = null;
  private Runnable runDrawNextLocationCursor = new Runnable() {
    public void run() {
	if(oldNextLocationCursor != null) {
	    xorDrawNextLocation(oldNextLocationCursor);
	    oldNextLocationCursor = null;
	}
	else if(drawNextLocationCursor != null) {
	    oldNextLocationCursor = drawNextLocationCursor;
	    xorDrawNextLocation(oldNextLocationCursor);
	}
    }
  };
  /**
   * Sets wether next insert cursor will be shown.
   *
   * @param state <code>true</code> to show
   */
  public void setShowInsertCursor(final boolean state) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(
	new Runnable() { public void run() { setShowInsertCursor(state); } }
      );
      return;
    }
    if(state == nextLocationCursorShow) return;
    nextLocationCursorShow = state;
    getCNUViewer().setShowInsertCursor(state);
    if(state) {
      // create an anonymous thread to continuely flash cursor
      if(nextLocationCursorThread == null) {
	nextLocationCursorThread = new Thread() {
	    public void run() {
	      try {
		while(nextLocationCursorShow && ! Thread.currentThread().isInterrupted()) {
		  Rectangle repaintArea = null;
		  if(! nextLocationCursor.equals(drawNextLocationCursor)) {
		    // make sure old location is cleared
		    if(drawNextLocationCursor != null)
		      repaintArea = drawNextLocationCursor.getBounds();
		    drawNextLocationCursor = nextLocationCursor.getCursor();
		  }
		  SwingUtilities.invokeLater(runDrawNextLocationCursor);
		  if(repaintArea != null)
		    repaint(repaintArea.x-1, repaintArea.y-1,
			    repaintArea.width+2, repaintArea.height+2);
		  Thread.sleep(500);
		  // erase old cursor
		  SwingUtilities.invokeLater(runDrawNextLocationCursor);
		  // do not sleep if done
		  if(nextLocationCursorShow) Thread.sleep(500);
		}
	      } catch (InterruptedException ie) {
		Thread.currentThread().interrupt(); // keep interrupted status
	      } finally {
		if(drawNextLocationCursor != null) {
		  Rectangle repaintArea =
		    drawNextLocationCursor.getBounds();
		  repaint(repaintArea.x-1, repaintArea.y-1,
			  repaintArea.width+2, repaintArea.height+2);
		}
	      }
	    };
	  };
	nextLocationCursorThread.start();
      }
    }
    else if(nextLocationCursorThread != null) {
        if( nextLocationCursorThread.isAlive() ) {
	    // kill the thread the nice way
	    nextLocationCursorThread.interrupt();  // interrupt if sleeping
	    try {
		// wait for thread to stop nicely
		nextLocationCursorThread.join(600);
	    } catch (InterruptedException ignored) {
	      Thread.currentThread().interrupt(); // keep interrupted status
	    }
        }
	/* Since stop isn't valid or safe just hope things worked
        if(nextLocationCursorThread.isAlive()) 
	    nextLocationCursorThread.stop();  // stop thread the bad way
	*/
	nextLocationCursorThread = null;
    }
    invalidate(); // may need to recalculate size
    validate();
  }
  /**
   * Checks if insert cursor is shown.
   *
   * @return	<code>true</code> if insert cursor shown
   *		or <code>false</code> if not shown
   */
  public boolean getShowInsertCursor() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() {
	  returnObject = new Boolean(nextLocationCursorShow);
	}
      };
      runWithReturn.invokeAndWait();
      return ((Boolean) runWithReturn.returnObject).booleanValue();
    }
    else return nextLocationCursorShow;
  }
  /**
   * Sets the screen resolution.
   *
   * @param screenResolution	screen resolution
   */
  public void setScreenResolution(int screenResolution) {
    if(screenResolution >= 1) this.screenResolution = screenResolution;
  }
  /**
   * Gets the screen resolution.
   *
   * @return the screen resolution
   */
  public int getScreenResolution() {
    if(screenResolution < 1)
      setScreenResolution(getToolkit().getScreenResolution());
    return screenResolution;
  }
  /**
   * Sets the screen resolution correction factor with undo/redo.
   *
   * @param correctionFactor	screen resolution correction factor
   */
  public void setScreenResCorrection(double correctionFactor) {
    setScreenResCorrection(correctionFactor, true);
  }
  /**
   *  Sets the screen resolution correction with optional undo/redo.
   *
   * @param correctionFactor	screen resolution correction factor
   * @param undoFlag		<code>true</code> to specify creating
   *				undo/redo commands
   */
  public void setScreenResCorrection(double correctionFactor, boolean undoFlag) {
    double oldFactor = screenResCorrection;
    if(oldFactor == correctionFactor) return;
    screenResCorrection = correctionFactor;
    if(undoFlag) {
      Class[] undoParams = { Double.TYPE, Boolean.TYPE };
      Object[] undoArgs = { new Double(oldFactor), new Boolean(false) };
      DoCommand undo = new DoCommand(this, "setScreenResCorrection",
				     undoParams, undoArgs);
      Object[] redoArgs = { new Double(correctionFactor), new Boolean(false) };
      DoCommand redo = new DoCommand(this, "setScreenResCorrection",
				     undoParams, redoArgs);
      getUndoRedo().addUndo(undo, redo, "screen resolution");
    }
  }
  /**
   * Gets the screen resolution correction factor.
   *
   * @return	screen resolution correction factor
   */
  public double getScreenResCorrection() { return screenResCorrection; }
  /**
   * Sets the grid on state with undo/redo commands.
   *
   * @param state	<code>true</code> to display a grid
   */
  public void setGridState(boolean state) { setGridState(state, true); }
  /**
   * Sets the grid on state with optional undo/redo commands.
   *
   * @param state	<code>true</code> to display a grid
   * @param undoFlag		<code>true</code> to specify creating
   *				undo/redo commands
   */
  public void setGridState(boolean state, boolean undoFlag) {
    if( gridState == state ) return;
    gridState = state;
    repaint();
    if(undoFlag) {
      Class[] undoParams = { Boolean.TYPE, Boolean.TYPE };
      Object[] undoArgs = { new Boolean(! state), new Boolean(false) };
      DoCommand undo = new DoCommand(this, "setGridState",
				     undoParams, undoArgs);
      Object[] redoArgs = { new Boolean(state), new Boolean(false) };
      DoCommand redo = new DoCommand(this, "setGridState",
				     undoParams, redoArgs);
      getUndoRedo().addUndo(undo, redo, "grid");
    }
  }
  /**
   * Gets the grid display state.
   *
   * @return	<code>true</code> if grid displayed
   */
  public boolean getGridState() { return gridState; }
  /**
   * Sets the color of the grid with undo/redo.
   *
   * @param c	new grid color
   */
  public void setGridColor(Color c) { setGridColor(c, true); }
  /**
   * Sets the color of the grid with optional undo/redo.
   *
   * @param c		new grid color
   * @param undoFlag	<code>true</code> to specify creating
   *			undo/redo commands
   */
  public void setGridColor(Color c, boolean undoFlag) {
    Color oldColor;
    if(c == null) return;
    oldColor = gridColor;
    if(c.equals(oldColor)) return;
    gridColor = c;
    repaint();
    if(undoFlag) {
      Class[] undoParams = { oldColor.getClass(), Boolean.TYPE };
      Object[] undoArgs = { (Object) oldColor, new Boolean(false) };
      DoCommand undo = new DoCommand(this, "setGridColor",
				     undoParams, undoArgs);
      Object[] redoArgs = { (Object) c, new Boolean(false) };
      DoCommand redo = new DoCommand(this, "setGridColor",
				     undoParams, redoArgs);
      getUndoRedo().addUndo(undo, redo, "grid color");
    }
  }
  /**
   * Gets the grid color.
   *
   * @return the current grid color
   */
  public Color getGridColor() { return gridColor; }
  /**
   * Sets the grid offsets with undo/redo commands.
   *
   * @param x		x offset of grid
   * @param y		y offset of grid
   * @param units	units of offsets
   */
  public void setGridOffset(double x, double y, int units) {
    setGridOffset(x, y, units, true);
  }
  /**
   * Sets the grid offsets with optional undo/redo commands.
   *
   * @param x		x offset of grid
   * @param y		y offset of grid
   * @param units	units of offsets
   * @param undoFlag	<code>true</code> to specify creating
   *			undo/redo commands
   */
  public void setGridOffset(double x, double y, int units, boolean undoFlag) {
    switch (units) {
    default:
    case PIXELS:
      break;
    case CM:
      x /= 2.54; // cm/inch
      y /= 2.54; // cm/inch
    case INCHES:
      double res = getScreenResolution() * getScreenResCorrection();
      x *= res; y *= res;
      break;
    }
    double oldX = gridOffsetX;
    double oldY = gridOffsetY;
    if((oldX == x) && (oldY == y)) return;
    gridOffsetX = x; gridOffsetY = y;
    repaint();
    if(undoFlag) {
      Class[] undoParams = { Double.TYPE, Double.TYPE,
			     Integer.TYPE, Boolean.TYPE };
      Object[] undoArgs = { new Double(oldX), new Double(oldY),
			    new Integer(PIXELS), new Boolean(false) };
      DoCommand undo = new DoCommand(this, "setGridOffset",
				     undoParams, undoArgs);
      Object[] redoArgs = { new Double(x), new Double(y),
			    new Integer(PIXELS), new Boolean(false) };
      DoCommand redo = new DoCommand(this, "setGridOffset",
				     undoParams, redoArgs);
      getUndoRedo().addUndo(undo, redo, "grid offset");
    }
  }
  /**
   * Gets the grid x offset.
   *
   * @return	x offset in pixels
   */
  public double getGridOffsetX() { return gridOffsetX; }
  /**
   * Gets the grid y offset.
   *
   * @return	y offset in pixels
   */
  public double getGridOffsetY() { return gridOffsetY; }
  /**
   * Sets the grid spacing with undo/redo commands.
   *
   * @param spacing	spacing
   * @param units	units spacing is in
   */
  public void setGridSpacing(double spacing, int units) {
    setGridSpacing(spacing, units, true);
  }
  /**
   * Sets the grid spacing with optional undo/redo commands.
   *
   * @param spacing	spacing
   * @param units	units spacing is in
   * @param undoFlag	<code>true</code> to specify creating
   *			undo/redo commands
   */
  public void setGridSpacing(double spacing, int units, boolean undoFlag) {
    switch (units) {
    default:
    case PIXELS:
      break;
    case CM:
      spacing /= 2.54; // cm/inch
    case INCHES:
      spacing *= getScreenResolution() * getScreenResCorrection();
      break;
    }
    if(spacing < 1) return;
    double oldSpacing = gridSpacing;
    if(oldSpacing == spacing) return;
    gridSpacing = spacing;
    repaint();
    if(undoFlag) {
      Class[] undoParams = { Double.TYPE, Integer.TYPE, Boolean.TYPE };
      Object[] undoArgs = { new Double(oldSpacing), new Integer(PIXELS),
			    new Boolean(false) };
      DoCommand undo = new DoCommand(this, "setGridSpacing",
				     undoParams, undoArgs);
      Object[] redoArgs = { new Double(spacing), new Integer(PIXELS),
			    new Boolean(false) };
      DoCommand redo = new DoCommand(this, "setGridSpacing",
				     undoParams, redoArgs);
      getUndoRedo().addUndo(undo, redo, "grid spacing");
    }
  }
  /**
   * Gets the grid spacing.
   *
   * @return	the grid spacing in pixels
   */
  public double getGridSpacing() {
    if(gridSpacing < 1) setGridSpacing(.5, INCHES, false);
    return gridSpacing;
  }
  /**
   * Sets the paper outline state creating an undo/redo history.
   *
   * @param state	<code>true</code> to display a paper outline,
   *			<code>false</code> otherwise
   */
  public void setPaperState(boolean state) { setPaperState(state, true); }
  /**
   * Sets the paper outline state with choice of building an undo/redo history.
   *
   * @param state	<code>true</code> to display a paper outline,
   *			<code>false</code> otherwise
   * @param undoFlag	<code>true</code> to create undo/redo history
   */
  public void setPaperState(boolean state, boolean undoFlag) {
    if(paperState == state) return;
    paperState = state;
    invalidate();
    repaint();
    if(undoFlag) {
      Class[] undoParams = { Boolean.TYPE, Boolean.TYPE };
      Object[] undoArgs = { new Boolean(! state), new Boolean(false) };
      DoCommand undo = new DoCommand(this, "setPaperState",
				     undoParams, undoArgs);
      Object[] redoArgs = { new Boolean(state), new Boolean(false) };
      DoCommand redo = new DoCommand(this, "setPaperState",
				     undoParams, redoArgs);
      getUndoRedo().addUndo(undo, redo, "paper state");
    }
  }
  /**
   * Gets the paper display state.
   *
   * @return	<code>true</code> if displaying a paper outline,
   *		<code>false</code> otherwise
   */
  public boolean getPaperState() { return paperState; }
  /**
   * Sets the paper outline color creating undo/redo history.
   *
   * @param c	paper outline color
   */
  public void setPaperColor(Color c) { setPaperColor(c, true); }
  /**
   * Sets the paper outline color with option to create undo/redo history.
   *
   * @param c	paper outline color
   * @param undoFlag	<code>true</code> to create undo/redo history
   */
  public void setPaperColor(Color c, boolean undoFlag) {
    if(c == null) return;
    Color oldColor = paperColor;
    if(c.equals(oldColor)) return;
    paperColor = c;
    repaint();
    if(undoFlag) {
      Class[] undoParams = { oldColor.getClass(), Boolean.TYPE };
      Object[] undoArgs = { (Object) oldColor, new Boolean(false) };
      DoCommand undo = new DoCommand(this, "setPaperColor",
				     undoParams, undoArgs);
      Object[] redoArgs = { (Object) c, new Boolean(false) };
      DoCommand redo = new DoCommand(this, "setPaperColor",
				     undoParams, redoArgs);
      getUndoRedo().addUndo(undo, redo, "paper color");
    }
  }
  /**
   * Gets the paper outline color.
   * @return	paper outline color
   */
  public Color getPaperColor() { return paperColor; }
  /**
   * Sets the paper outline offset with undo/redo history.
   *
   * @param x		offset in units to x origin of the paper outline
   * @param y		offset in units to y origin of the paper outline
   * @param units	units origins are specified in (PIXELS, CM, or INCHES)
   */
  public void setPaperOffset(double x, double y, int units) {
    setPaperOffset(x, y, units, true);
  }
  /**
   * Sets the paper outline offset with optional undo/redo history.
   *
   * @param x		offset in units to x origin of the paper outline
   * @param y		offset in units to y origin of the paper outline
   * @param units	units origins are specified in (PIXELS, CM, or INCHES)
   * @param undoFlag	<code>true</code> to create undo/redo history
   */
  public void setPaperOffset(double x, double y, int units, boolean undoFlag) {
    switch (units) {
    default:
    case PIXELS:
      break;
    case CM:
      x /= 2.54; // cm/inch
      y /= 2.54; // cm/inch
    case INCHES:
      double res = getScreenResolution() * getScreenResCorrection();
      x *= res; y *= res;
      break;
    }
    double oldOffsetX = paperOffsetX;
    double oldOffsetY = paperOffsetY;
    if((oldOffsetX == x) && (oldOffsetY == y)) return;
    paperOffsetX = x; paperOffsetY = y;
    invalidate();
    repaint();
    if(undoFlag) {
      Class[] undoParams = { Double.TYPE, Double.TYPE,
			     Integer.TYPE, Boolean.TYPE};
      Object[] undoArgs = { new Double(oldOffsetX),
			    new Double(oldOffsetY), new Integer(PIXELS),
			    new Boolean(false) };
      DoCommand undo = new DoCommand(this, "setPaperOffset",
				     undoParams, undoArgs);
      Object[] redoArgs = { new Double(x), new Double(y),
			    new Integer(PIXELS), new Boolean(false) };
      DoCommand redo = new DoCommand(this, "setPaperOffset",
				     undoParams, redoArgs);
      getUndoRedo().addUndo(undo, redo, "paper offset");
    }
  }
  /**
   * Sets the paper outline size with undo/redo history.
   *
   * @param width	width of the paper outline
   * @param height	height of the paper outline
   * @param units	units origins are specified in (PIXELS, CM, or INCHES)
   */
  public void setPaperSize(double width, double height, int units) {
    setPaperSize(width, height, units, true);
  }
  /**
   * Sets the paper outline size with optional undo/redo history.
   *
   * @param width	width of the paper outline
   * @param height	height of the paper outline
   * @param units	units origins are specified in (PIXELS, CM, or INCHES)
   * @param undoFlag	<code>true</code> to create undo/redo history
   */
  public void setPaperSize(double width, double height, int units,
			   boolean undoFlag) {
    switch (units) {
    default:
    case PIXELS:
      break;
    case CM:
      width /= 2.54; // cm/inch
      height /= 2.54; // cm/inch
    case INCHES:
      double res = getScreenResolution() * getScreenResCorrection();
      width *= res; height *= res;
      break;
    }
    if((width < 1) || (height < 1)) return;
    double oldWidth = paperWidth;
    double oldHeight = paperHeight;
    if((oldWidth == width) && (oldHeight == height)) return;
    paperWidth = width; paperHeight = height;
    invalidate();
    repaint();
    if(undoFlag) {
      Class[] undoParams = { Double.TYPE, Double.TYPE,
			     Integer.TYPE, Boolean.TYPE};
      Object[] undoArgs = { new Double(oldWidth), new Double(oldHeight),
			    new Integer(PIXELS), new Boolean(false)};
      DoCommand undo = new DoCommand(this, "setPaperSize",
				     undoParams, undoArgs);
      Object[] redoArgs = { new Double(width), new Double(height),
			    new Integer(PIXELS), new Boolean(false) };
      DoCommand redo = new DoCommand(this, "setPaperSize",
				     undoParams, redoArgs);
      getUndoRedo().addUndo(undo, redo, "paper size");
    }
  }
  /**
   * Gets the paper offset x in pixels.
   *
   * @return	paper x offset in pixels
   */
  public double getPaperOffsetX() { return paperOffsetX; }
  /**
   * Gets the paper offset y in pixels.
   *
   * @return	paper y offset in pixels
   */
  public double getPaperOffsetY() { return paperOffsetY; }
  /**
   * Gets the paper width in pixels.
   *
   * @return	paper width in pixels
   */
  public double getPaperWidth() {
    if(paperWidth < 1) setPaperSize(8.5, 11, INCHES, false);
    return paperWidth;
  }
  /**
   * Gets the paper height in pixels.
   *
   * @return	paper height in pixels
   */
  public double getPaperHeight() {
    if(paperHeight < 1) setPaperSize(8.5, 11, INCHES, false);
    return paperHeight;
  }
  /**
   * Draws a grid pattern.
   *
   * @param g	graphics context to draw on
   * @param r	rectangle to restrict drawing within
   */
  public void drawGrid(Graphics g, Rectangle r) {
    double gridSpacing = getGridSpacing();
    int screenResolution = getScreenResolution();
    if((screenResolution < 1) || (gridSpacing < 1)) return;
    g.setColor(getGridColor());
    double gridOffsetX = getGridOffsetX();
    double gridOffsetY = getGridOffsetY();
    // up down lines
    int line = (int) Math.ceil((r.x - gridOffsetX)/ gridSpacing);
    int endLine = (int) Math.floor((r.x + r.width - gridOffsetX)/ gridSpacing);
    for(; line <= endLine ; line++) {
      int x = (int) Math.rint((gridSpacing * line) + gridOffsetX);
      g.drawLine(x, r.y, x, r.y + r.height);
    }
    // left right lines
    line = (int) Math.ceil((r.y - gridOffsetY)/ gridSpacing);
    endLine = (int) Math.floor((r.y + r.height - gridOffsetY)/ gridSpacing);
    for(; line <= endLine ; line++) {
      int y = (int) Math.round((gridSpacing * line) + gridOffsetY);
      g.drawLine(r.x, y, r.x + r.width, y);
    }
  }
  /**
   * Draws the paper outline.
   *
   * @param g	graphics context to draw on
   */
  public void drawPaper(Graphics g) {
    double paperWidth = getPaperWidth();
    double paperHeight = getPaperHeight();
    if((paperWidth < 1) || (paperHeight < 1)) return;
    int width = (int) Math.round(paperWidth);
    int height = (int) Math.round(paperHeight);
    int x = (int) Math.round(getPaperOffsetX());
    int y = (int) Math.round(getPaperOffsetY());
    g.setColor(getPaperColor());
    g.drawRect(x, y, width, height);
  }
  /**
   * Paints this display onto the graphics context.
   *
   * @param g	graphics context to paint on
   */
  public void paint(Graphics g) { update(g); }
  /**
   * Does a layout of all components.
   */
  public void relayout() {
    doRowLayout();
    repaint();
  }
  /**
   * Validates the correct parent.
   */
  public void validateProperParent() {
    JScrollPane sp = getParentScrollPane();
    if(sp != null) sp.validate();
    else getParentFrame().validate();
  }
  /**
   * Sets this display containers state to invalide so validate will be
   * called when needed.
   */
  public void invalidate() { super.invalidate(); }
  /**
   * Validates this display container by calling doLayout to get the total
   * size and call super.validate() to set the validated flag.
   */
  public void validate() {
    if(isValid()) return;
    super.validate();
    doLayout();
  }
  /**
   * Resizes components to thier preferred sizes and
   * calculates total size for this display container.
   */
  public void doLayout() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(
	new Runnable() { public void run() { doLayout(); } }
      );
      return;
    }
    boolean validateNeeded = false;
    Dimension tot = new Dimension(0, 0);
    if(getPaperState()) {
      tot.width = (int) Math.round(getPaperOffsetX());
      tot.height = (int) Math.round(getPaperOffsetY());
      tot.width += (int) Math.round(getPaperWidth());
      tot.height += (int) Math.round(getPaperHeight());
    }
    if(nextLocationCursorShow) {
	tot.width = Math.max(tot.width, nextLocation.x +
			     Math.max(spacing, 5));
	tot.height = Math.max(tot.height,
			      nextLocation.y + Math.max(rowHeight, 5));
    }
    Component[] components = getComponents();
    for(int i=0; i<components.length; i++) {
      components[i].setSize(components[i].getPreferredSize());
      Rectangle r = components[i].getBounds();
      tot.width = Math.max(tot.width, r.x + r.width);
      tot.height = Math.max(tot.height, r.y + r.height);
    }
    tot.width += spacing; tot.height += spacing;
    Dimension size = getPreferredSize();
    if( (tot.width != size.width) || (tot.height != size.height) ) {
      setPreferredSize(tot);
      setMinimumSize(tot);
      setMaximumSize(tot);
      validateNeeded = true;;
    }
    if(validateNeeded) validateProperParent();
    super.doLayout();
  }
  /**
   * Sets the next layout position to upper left display corner with optional
   * undo/redo history.
   *
   * @param undoFlag	<code>true</code> to create undo/redo history
   */
  public void resetLayout(boolean undoFlag) {
    resetLayout(new Point(spacing, spacing), 1, 0, undoFlag);
  }
  /**
   * Sets the next layout position to a given location with optional
   * undo/redo history.
   *
   * @param x		x pixel position to place next component
   * @param y		y pixel position to place next component
   * @param undoFlag	<code>true</code> to create undo/redo history
   */
  public void resetLayout(int x, int y, boolean undoFlag) {
    resetLayout(new Point(x, y), nextCol, rowHeight, undoFlag);
  }
  /**
   * Sets the next layout position to a given location and the next column
   * to a given column with optional undo/redo history.
   *
   * @param x		x pixel position to place next component
   * @param y		y pixel position to place next component
   * @param nextCol	column number next component will be positioned at
   * @param undoFlag	<code>true</code> to create undo/redo history
   */
  public void resetLayout(int x, int y, int nextCol, boolean undoFlag) {
    resetLayout(new Point(x, y), nextCol, rowHeight, undoFlag);
  }
  /**
   * Sets the next layout position to a given location, the next column
   * to a given column and the current row height to a given height
   * with optional undo/redo history.
   *
   * @param newNextLocation   position to place next component
   * @param newNextCol	column number next component will be positioned at
   * @param newRowHeight height of current row for determining far down
   *			 next row should start
   * @param undoFlag	<code>true</code> to create undo/redo history
   */
  public void resetLayout(final Point newNextLocation, final int newNextCol,
			  final int newRowHeight, final boolean undoFlag) {
    if(! SwingUtilities.isEventDispatchThread()) {
      Runnable runnable = new Runnable() {
	 public void run() {
	   resetLayout(newNextLocation, newNextCol,
		       newRowHeight, undoFlag);
	 }
      };
      SwingUtilities.invokeLater(runnable);
   }
   endDrag(); // make sure no outstanding drags
   if( newNextLocation.equals(nextLocation) &&
      (newNextCol == nextCol)  && (newRowHeight == rowHeight) )
      return; // nothing to do

    Point oldNextLocation = nextLocation.getLocation();
    int oldNextCol = nextCol;
    int oldRowHeight = rowHeight;

    nextLocation.setLocation(newNextLocation.x, newNextLocation.y);
    nextCol = newNextCol;
    nextLocationCursor.setNextColumn(nextCol);
    rowHeight = newRowHeight;
    nextLocationCursor.setLocation(nextLocation.x, nextLocation.y);
    if(undoFlag) {
      Class[] undoParams = { oldNextLocation.getClass(), Integer.TYPE,
			       Integer.TYPE, Boolean.TYPE };
      Object[] undoArgs = { oldNextLocation, new Integer(oldNextCol),
			      new Integer(oldRowHeight),
			      new Boolean(false) };
      DoCommand undo = new DoCommand(this, "resetLayout",
				     undoParams, undoArgs);
      Object[] redoArgs = { nextLocation.getLocation(),
			    new Integer(nextCol), new Integer(rowHeight),
			    new Boolean(false) };
      DoCommand redo = new DoCommand(this, "resetLayout", undoParams,
				     redoArgs);
      getUndoRedo().addUndo(undo, redo, "reset layout");
    }
  }
  /**
   * Gets a copy of the current next location.
   *
   * @return	the next location
   */
  public Point getNextLocation() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = getNextLocation(); }
      };
      runWithReturn.invokeAndWait();
      return (Point) runWithReturn.returnObject;
    }
    return new Point(nextLocation.x, nextLocation.y);
  }
  public void move(Component comp, int x, int y) { move(comp, x, y, true); }
  /**
   * Moves a component to a new location optionally
   * creating an undo/redo history.
   *
   * @param comp	Component to move
   * @param x		x pixel position to move to
   * @param y		y pixel position to move to
   * @param undoFlag	<code>true</code> to create undo/redo history
   */
  public void move(final Component comp,
	           final int x, final int y, final boolean undoFlag) {
    if(! SwingUtilities.isEventDispatchThread()) {
      Runnable runnable = new Runnable() {
	 public void run() { move(comp, x, y, undoFlag); }
      };
      SwingUtilities.invokeLater(runnable);
    }
    endDrag(); // make sure no outstanding drags
    Point oldLocation = comp.getLocation();
    if( (oldLocation.x == x) && (oldLocation.y == y)) return;
    comp.setLocation(x, y);
    if(undoFlag) {
      Class[] undoParams = { java.awt.Component.class,
			     Integer.TYPE, Integer.TYPE, Boolean.TYPE };
      Object[] undoArgs = { comp, new Integer(oldLocation.x),
			    new Integer(oldLocation.y),
			    new Boolean(false) };
      DoCommand undo = new DoCommand(this, "move", undoParams, undoArgs);
      Object[] redoArgs = { comp, new Integer(x), new Integer(y),
			    new Boolean(false) };
      DoCommand redo = new DoCommand(this, "move", undoParams, redoArgs);
      getUndoRedo().addUndo(undo, redo, comp, "move");
    }
  }
  /**
   * Adds a component to the next column location with undo/redo history.
   *
   * @param comp	component to add
   */
  public void addToLayout(Component comp) { addToLayout(comp, true); }
  /**
   * Adds a component to the next column location with optional
   * undo/redo history.
   * Only called from event processing thread.
   *
   * @param comp	component to add
   * @param undoflag	<code>true</code> to create undo/redo history
   */
  private void addToLayout(Component comp, boolean undoFlag) {
    getUndoRedo().startSteps();
    comp.validate();
    move(comp, nextLocation.x, nextLocation.y, undoFlag);
    comp.setSize(comp.getPreferredSize());
    Dimension s = comp.getSize();
    int newRowHeight = Math.max(rowHeight, s.height);
    int newNextCol = nextCol + 1;
    Point newNextLocation = new Point(nextLocation.x, nextLocation.y);
    if(newNextCol > getNumberOfColumns()) {
      newNextCol = 1;
      newNextLocation.x = spacing;
      newNextLocation.y += newRowHeight + spacing;
      newRowHeight = 0;
    }
    else newNextLocation.x += s.width + spacing;
    resetLayout(newNextLocation, newNextCol, newRowHeight, undoFlag);
    getUndoRedo().finishUndoSteps("add to layout");
    invalidate();  // should cause doLayout to be called
  }
  /**
   * Reorganizes out all existing components in rows.
   */
  public void doRowLayout() {
    if(! SwingUtilities.isEventDispatchThread()) {
      Runnable runnable = new Runnable() {
	public void run() { doRowLayout(); }
      };
      SwingUtilities.invokeLater(runnable);
    }
    endDrag(); // make sure no outstanding drags
    getUndoRedo().startSteps();
    resetLayout(true);
    int ncmps = getDisplayComponentCount();
    for(int i=0; i < ncmps; i++) {
      Component comp = getDisplayComponent(i);
      addToLayout(comp);
    }
    getUndoRedo().finishUndoSteps("row layout");
  }
  /**
   * Determines if crop box is currently displayed.
   *
   * @return	<code>true</code> if crop box is displayed
   */
  public boolean cropBoxDisplayed() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = new Boolean(cropBoxDisplayed()); }
      };
      runWithReturn.invokeAndWait();
      return ((Boolean) runWithReturn.returnObject).booleanValue();
    }
    if((displayedCropBox != null) && (displayedCropComp != null)) return true;
    else return false;
  }
  /**
   * Updates the crop box.
   *
   * @param cropBox	new location and size for the crop box
   *			may be <code>null</code> to display no crop box
   * @param cropComp	display component to display crop box over
   *			may be <code>null</code> to display no crop box
   */
  public void cropBoxUpdate(final Rectangle cropBox,
			    final Component cropComp) {
    if(! SwingUtilities.isEventDispatchThread()) {
      Runnable runnable = new Runnable() {
	public void run() { cropBoxUpdate(cropBox, cropComp); }
      };
      SwingUtilities.invokeLater(runnable);
    }
    else {
      Rectangle repaintArea = null;
      // erase the previous crop box
      if( displayedCropComp != null )
	repaintArea = displayedCropComp.getBounds();
      if( (cropComp == null) || (cropBox == null) ||
	  (! isImmediateParentOf(cropComp) ) ) {
        displayedCropBox = null;
        displayedCropComp = null;
      }
      else {
        // save the new values
        displayedCropComp = cropComp;
        displayedCropBox = cropBox.getBounds();
        if(repaintArea == null) repaintArea = cropComp.getBounds();
        else repaintArea.add(cropComp.getBounds());
      }
      if(repaintArea != null) {
        repaintArea.grow(1, 1);
        repaint(repaintArea.x, repaintArea.y,
	   repaintArea.width, repaintArea.height);
      }
    }
  }
  /**
   * Updates the selection region.
   *
   * @param selectRegion	new location and size for select region
   *			may be <code>null</code> to display no select region
   */
  public void selectRegionUpdate(final Rectangle selectRegion) {
    if(! SwingUtilities.isEventDispatchThread()) {
      Runnable runnable = new Runnable() {
	public void run() { selectRegionUpdate(selectRegion); }
      };
      SwingUtilities.invokeLater(runnable);
    }
    else {
      Rectangle repaintArea = null;
      if( displayedSelectRegion != null ) {
	if(selectRegion == null) {
	  repaintArea = displayedSelectRegion.getBounds();
	  displayedSelectRegion = null;
	}
	else if(! displayedSelectRegion.equals(selectRegion)) {
	  repaintArea = displayedSelectRegion.getBounds();
	  repaintArea.add(selectRegion);
	  displayedSelectRegion = selectRegion.getBounds();
	}
      }
      else if(selectRegion != null) {
	repaintArea = selectRegion.getBounds();
	displayedSelectRegion = selectRegion.getBounds();
      }
      if(repaintArea != null) {
        repaint(repaintArea.x, repaintArea.y,
		repaintArea.width+1, repaintArea.height+1);
      }
    }
  }
  /**
   * Gets the currently displayed select region
   *
   * @return	current displayed select region
   *		may be <code>null</code> if no select region displayed
   */
  public Rectangle getDisplayedSelectRegion() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = getDisplayedSelectRegion(); }
      };
      runWithReturn.invokeAndWait();
      return (Rectangle) runWithReturn.returnObject;
    }
    if(displayedSelectRegion == null) return null;
    return displayedSelectRegion.getBounds();
  }
  /**
   * Restores the mouse listener to this display container object.
   *
   * @param oldListener	listener to be replaced by this display container.
   *			If the current listener is not the same as oldListener
   *			it will not be replaced and <code>false</coder>
   *			returned.
   * @return 		<code>true</code> if listener restored.
   */
  public boolean restoreMouseListeners(Object oldListener) {
    synchronized (mouseListenerLock) {
      if(oldListener == currentMouseListener) {
        removeMouseListener((MouseListener)oldListener);
        removeMouseMotionListener((MouseMotionListener)oldListener);
        addMouseListener(this);
        addMouseMotionListener(this);
        currentMouseListener = this;
        return true;
      }
    }
    return false; // restore failed
  }
  /**
   * Replaces the default mouse listener with different one.  Fails
   * if mouse listener has already been replaced and not restored.
   *
   * @param newListener	mouse listener to replace with
   * @return 		<code>true</code> if listener is replaced
   */
  public boolean replaceMouseListeners(Object newListener) {
    if(newListener instanceof MouseListener &&
       newListener instanceof MouseMotionListener)
      synchronized (mouseListenerLock) {
      // only replace null or this mouse listener
      if(currentMouseListener == this) {
        removeMouseListener(this);
        removeMouseMotionListener(this);
	currentMouseListener = null;
      }
      if(currentMouseListener == null) {
        addMouseListener((MouseListener)newListener);
        addMouseMotionListener((MouseMotionListener)newListener);
	currentMouseListener = (MouseListener) newListener;
	return true;
      }
    }
    return false; // replacement failed
  }
  /**
   * Sets the cursor for the display are and all components while
   * and keeps track cursors for restoring.
   * Should only be called from event processing thread.
   *
   * @param cursor new cursor to set
   */
  public void saveOldSetNewCursor(Cursor cursor) {
    if(! SwingUtilities.isEventDispatchThread()) return;
    if(! cursorChanged) {
      // changing everything because jvm are inconsistant
      mainCursor = getCursor();
      if(mainCursor != cursor) { 
	cursorChanged = true;
	cursorComponents = getComponents();
	if(cursorComponents != null) {
	  if(cursorComponents.length > 0) {
	    componentCursors = new Cursor[cursorComponents.length];
	    for(int i=0; i<cursorComponents.length; i++) {
	      Component comp = cursorComponents[i];
	      componentCursors[i] = comp.getCursor();
	      comp.setCursor(cursor);
	    }
	  } else cursorComponents = null;
	}
	this.setCursor(cursor);
      }
    }
  }
  /**
   * Restores original cursors.
   */
  public void restoreCursors() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { restoreCursors(); }
      } );
    }
    else if(cursorChanged) {
      this.setCursor(mainCursor);
      if(cursorComponents != null) {
	for(int i=0; i<cursorComponents.length; i++) {
	  Component comp = cursorComponents[i];
	  comp.setCursor(componentCursors[i]);
	}
	cursorComponents = null;
	componentCursors = null;
      }
      cursorChanged = false;
      mainCursor = null;
    }
  }
  /**
   * Starts dragging all selected components.
   * Should be called only from event processing thread.
   *
   * @param evt	mouse event
   */
  public void startDrag(MouseEvent evt) {
    if(itIsADrag) return; // someone else already started drag
    if(selectedObjects.size() < 1) return;
    // change cursor to reflect drag
    saveOldSetNewCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

    startDragPt.setLocation(getEventPointOnScreen(evt, true));
    dragList = new Component[selectedObjects.size()];
    selectedObjects.copyInto(dragList);
    predragLocations = new Point[dragList.length];
    for(int i=dragList.length-1; i>=0; i--)
      predragLocations[i] = dragList[i].getLocation();
    insertDragCursor = null;
    itIsADrag = true;
  }
  /**
   * Ends dragging of all selected components.
   * May be called from any thread.
   *
   * @return total amount dragged
   */
  public Point endDrag() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = endDrag(); }
      };
      runWithReturn.invokeAndWait();
      return (Point) runWithReturn.returnObject;
    }
    Rectangle repaintArea = null;
    Point rtnDragAmount = new Point(0,0);
    Point scrollDragAmount = null;

    restoreCursors();
    if(! itIsADrag) return rtnDragAmount;
    if(dragList != null) {
      rtnDragAmount.setLocation(dragAmount);
      for(int i=dragList.length-1; i>=0; i--) {
        Component comp = dragList[i];
        if(repaintArea == null) repaintArea = comp.getBounds();
        else repaintArea.add(comp.getBounds());
        comp.setLocation(predragLocations[i]);
        repaintArea.add(comp.getBounds());
      }
    }
    else if(insertDragCursor != null) {
      rtnDragAmount.setLocation(dragAmount);
      repaintArea = insertDragCursor.getBounds();
      insertDragCursor.setLocation(startInsertPosition);
      repaintArea.add(insertDragCursor.getBounds());
    }
    else {
      // dragging scroll pane rtnDragAmount left at (0,0)
    }
    dragList = null;
    insertDragCursor = null;
    predragLocations = null;
    dragAmount.setLocation(0,0);
    itIsADrag = false;
    if(repaintArea != null) {
      repaintArea.grow(1, 1);  // grow because selected
      repaint(repaintArea.x, repaintArea.y, repaintArea.width,
	      repaintArea.height);
    }
    return rtnDragAmount;
  }
  /**
   * Starts dragging insert point.
   * Should only be called from event processing thread.
   *
   * @param startPoint	location drag begins from
   */
  public void startInsertDrag(Point startPoint) {
    if(itIsADrag) return; // someone else already started drag

    // change cursor to reflect drag
    saveOldSetNewCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

    startDragPt.setLocation(startPoint);
    dragList = null;
    insertDragCursor = nextLocationCursor.getCursor();
    startInsertPosition.setLocation(insertDragCursor.getLocation());
    itIsADrag = true;
  }
  /**
   * Starts dragging the display area within the scroll pane.
   * Should only be called from event processing thread.
   *
   * @param startPoint	location drag begins from
   */
  public void startDisplayDrag(Point startPoint) {
    if(itIsADrag) return; // someone else already started drag

    // change cursor to reflect drag
    saveOldSetNewCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    startDragPt.setLocation(startPoint);
    startScrollPosition.setLocation(getScrollPosition());
    dragList = null;
    insertDragCursor = null;
    itIsADrag = true;
  }
  /**
   * Gets the current scroll bar positions.
   *
   * @return the current scroll bar positions
   */
  public Point getScrollPosition() {
    JScrollPane scrollPane = getParentScrollPane();
    return new Point(scrollPane.getHorizontalScrollBar().getValue(),
		     scrollPane.getVerticalScrollBar().getValue());
  }
  /**
   * Sets the scroll bar positions.
   *
   * @param p the new scroll bar positions
   */
  public void setScrollPosition(Point p) {
    JScrollPane scrollPane = getParentScrollPane();
    scrollPane.getHorizontalScrollBar().setValue(p.x);
    scrollPane.getVerticalScrollBar().setValue(p.y);
  }
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
   public void setDrageEventPositioningMode(int mode) {
     dragEventPositioningMode = mode;
   }
  /**
   * Gets the drag event positioning mode.  Because of inconsistencies
   * across platforms, this flag is needed to specify how to calculate
   * the position of an mouse event on the screen during a drag.
   *
   * @return	new drag event positioing mode
   *			(EVENT_RELATIVE_TO_START_COMPONENT_ON_SCREEN or
   *			EVENT_RELATIVE_TO_COMPONENT_ON_SCREEN)
   */
   public int getDragEventPositioningMode() {
     return dragEventPositioningMode;
   }
  /**
   * Continues dragging of all selected components.
   * Should only be called from the event processing thread.
   *
   * @param dragPoint	location drag has reached
   */
  public void drag(Point dragPoint) {
    Rectangle repaintArea = null;
    Point scrollDragAmount = null;
    if(! itIsADrag) return;  // someone else already ended drag
    dragAmount.setLocation(dragPoint.x - startDragPt.x,
			   dragPoint.y - startDragPt.y);
    if((dragAmount.x == 0) && (dragAmount.y == 0)) return;
    if(dragList != null) {
      for(int i=dragList.length-1; i>=0; i--) {
        Component comp = dragList[i];
        if(repaintArea == null) repaintArea = comp.getBounds();
        else repaintArea.add(comp.getBounds());
        comp.setLocation(predragLocations[i].x + dragAmount.x,
		         predragLocations[i].y + dragAmount.y);
        repaintArea.add(comp.getBounds());
      }
    }
    else if(insertDragCursor != null) {
      repaintArea = insertDragCursor.getBounds();
      insertDragCursor.setLocation(startInsertPosition.x + dragAmount.x,
				   startInsertPosition.y + dragAmount.y);
      repaintArea.add(insertDragCursor.getBounds());
    }
    else {
      scrollDragAmount = dragAmount.getLocation();
    }
    if(scrollDragAmount != null)
      translateScrollPosition(scrollDragAmount, startScrollPosition);
    else if(repaintArea != null) {
      repaintArea.grow(1, 1);  // grow because selected
      repaint(repaintArea.x, repaintArea.y, repaintArea.width,
	      repaintArea.height);
    }
  }
  /**
   * Changes the scroll position by the givent amount.
   *
   * @param amount		amount to scroll by
   * @param scrollPosition	starting scroll position to change by amound
   *				if <code>null</code> uses current position
   */
   public void translateScrollPosition(Point amount, Point scrollPosition) {
     if(scrollPosition == null) scrollPosition = getScrollPosition();
     else scrollPosition = scrollPosition.getLocation();
     scrollPosition.translate(-amount.x, -amount.y);
     scrollPosition.move(Math.max(0, scrollPosition.x),
			 Math.max(0, scrollPosition.y));
     setScrollPosition(scrollPosition);
   }
  /**
   * Sets a SliceNumbering objects slice number while
   * creating an undo/redo history.
   *
   * @param sliceNumbering	object to change slice number of
   * @param newSlice	new slice number
   */
  public void setSlice(SliceNumbering sliceNumbering, int newSlice) {
    int oldSlice = sliceNumbering.getSlice();
    if(oldSlice != newSlice) {
      sliceNumbering.setSlice( newSlice );
      newSlice = sliceNumbering.getSlice();
      if(oldSlice != newSlice) {
	Class[] undoParams = new Class[1]; undoParams[0] = Integer.TYPE;
	Object[] undoArgs = new Object[1]; undoArgs[0] = new Integer(oldSlice);
	DoCommand undo =
	  new DoCommand(sliceNumbering, "setSlice", undoParams, undoArgs);
	Object[] redoArgs = new Object[1]; redoArgs[0] = new Integer(newSlice);
	DoCommand redo =
	  new DoCommand(sliceNumbering, "setSlice", undoParams, redoArgs);
	Component comp = null;
	if(sliceNumbering instanceof Component) {
	  comp = (Component) sliceNumbering;
	  comp.repaint();
	}
	getUndoRedo().addUndo(undo, redo, null, comp, "set slice");
      }
    }
  }
  /**
   * Sets a SingleImg objects iValue number while
   * creating an undo/redo history.
   *
   * @param singleImg	object to change iValue of
   * @param newIValue	new iValue number
   */
  public void setIValue(SingleImg singleImg, int newIValue) {
    int oldIValue = singleImg.getIValue();
    if(oldIValue != newIValue) {
      singleImg.setIValue( newIValue );
      newIValue = singleImg.getIValue();
      if(oldIValue != newIValue) {
	Class[] undoParams = new Class[1]; undoParams[0] = Integer.TYPE;
	Object[] undoArgs = new Object[1]; undoArgs[0] = new Integer(oldIValue);
	DoCommand undo =
	  new DoCommand(singleImg, "setIValue", undoParams, undoArgs);
	Object[] redoArgs = new Object[1]; redoArgs[0] = new Integer(newIValue);
	DoCommand redo =
	  new DoCommand(singleImg, "setIValue", undoParams, redoArgs);
	Component comp = null;
	if(singleImg instanceof Component) {
	  comp = (Component) singleImg;
	  comp.repaint();
	}
	getUndoRedo().addUndo(undo, redo, null, comp, "set iValue");
      }
    }
  }
  /**
   * Sets the feature of an object while creating an undo/redo history.
   * The object must have a set and get command for the feature.
   *
   * @param object	object to change feature of
   * @param featureName name of feature as it would appear in set and get commands (i.e. Font for setFont(Font f) and Font getFont())
   * @param value	new value
   */
  public void setFeature(Object obj, String featureName, Object value) {
    setFeature(obj, featureName, value, null);
  }
  /**
   * Sets the feature of an object while creating an undo/redo history.
   * The object must have a set and get command for the feature.
   *
   * @param object	object to change feature of
   * @param featureName name of feature as it would appear in set and get commands (i.e. Font for setFont(Font f) and Font getFont())
   * @param value	new value
   * @param repaintArea accumulater for repaint area.  May be <code>null</code>
   * @return		accumulated repaint area.  May be <code>null</code>
   *			if input repaintArea <code>null</code> and object not a component
   *			or did not get modified.
   */
  public Rectangle setFeature(Object obj, String featureName, Object value, Rectangle repaintArea) {
    if(obj == null || featureName == null) return repaintArea;
    if(featureName.length() == 0) return repaintArea;
    if(! SwingUtilities.isEventDispatchThread()) return repaintArea;
    try {
      // first try to get the old value
      DoCommand getDoCommand = new DoCommand(obj, "get" + featureName, null);
      Object oldValue = getDoCommand.invoke();
      if(oldValue == null && value == null); // no setting needed
      else if(oldValue != null && oldValue.equals(value)); // no setting needed
      else {
	Class params[] = new Class[1];
	if(value != null) params[0] = value.getClass();
	else params[0] = oldValue.getClass();
	DoCommand redo =
	  new DoCommand(obj, "set" + featureName, params, new Object[] {value});
	redo.invoke();
	// check if setting actually did
	Object newValue = getDoCommand.invoke();
	if(oldValue == null && newValue == null); // no undo/redo needed
	else if(newValue != null && newValue.equals(oldValue)); // no undo/redo needed
	else {
	  DoCommand undo =
	    new DoCommand(obj, "set" + featureName, params, new Object[] {oldValue});
	  Component repaintComp = null;
	  if(obj instanceof Component) {
	    repaintComp = (Component) obj;
	    repaintComp.repaint();
	    if(repaintArea == null)repaintArea = repaintComp.getBounds();
	    else repaintArea.add(repaintComp.getBounds());
	  }
	  getUndoRedo().addUndo(undo, redo, null, repaintComp, "set " + featureName);
	}
      }
    } catch (NoSuchMethodException nsme) { // ignore
      if(obj instanceof ComponentGroup) setOnComponentGroup((ComponentGroup) obj, featureName, value);
      else showStatus(nsme);
    } catch (InvocationTargetException ite)  { // ignore
      showStatus(ite);
    } catch (IllegalAccessException iae) { // ignore
      showStatus(iae);
    }
    return repaintArea;
  }
  /**
   * Sets a feature on the given components.  Should only be
   * called from event thread.
   *
   * @param components  components to set feature for
   * @param featureName	name of feature to apply
   * @param featureValue	value for feature
   * @return		<code>true</code> if any components
   */
  public boolean setComponentsFeature(final Component[] components, final String featureName, Object featureValue) {
    if((components == null) || (components.length < 1)) return false;
    if(! SwingUtilities.isEventDispatchThread()) return false;
    Rectangle repaintArea = null;
    getUndoRedo().startSteps();
    for(int i=0; i<components.length; i++)
      repaintArea = setFeature(components[i], featureName, featureValue, repaintArea);
    getUndoRedo().finishUndoSteps("set " + featureName);
    if(repaintArea != null) {
      //include selection area
      repaintArea.grow(1, 1); // include selection boundary
      repaint(repaintArea.x, repaintArea.y, repaintArea.width,
	      repaintArea.height);
    }
    return true;
  }
  /**
   * Sets a feature on selected or all components.
   *
   * @param featureName name of feature as it would appear in set and get commands (i.e. Font for setFont(Font f) and Font getFont())
   * @param value	new value
   * @return		<code>true</code> if any components found
   */
  public boolean setFeature(String featureName, Object value) {
    if( setOnSelectedComponents(featureName, value) ) return true;
    return setOnAllComponents(featureName, value);
  }


  /**
   * Sets a feature on all components.
   *
   * @param featureName name of feature as it would appear in set and get commands (i.e. Font for setFont(Font f) and Font getFont())
   * @param featureValue	new value
   * @return		<code>true</code> if any components
   */
  public boolean setOnAllComponents(final String featureName, final Object featureValue) {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() {
	  returnObject = new Boolean(setOnAllComponents(featureName, featureValue));
	}
      };
      runWithReturn.invokeAndWait();
      return ((Boolean) runWithReturn.returnObject).booleanValue();
    }
    return setComponentsFeature(getComponents(), featureName, featureValue);
  }
  /**
   * Applies a function to selected components.
   *
   * @param featureName name of feature as it would appear in set and get commands (i.e. Font for setFont(Font f) and Font getFont())
   * @param featureValue	new value
   * @return		<code>true</code> if any components selected
   */
  public boolean setOnSelectedComponents(final String featureName, final Object featureValue) {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() {
	  returnObject = new Boolean(setOnSelectedComponents(featureName, featureValue));
	}
      };
      runWithReturn.invokeAndWait();
      return ((Boolean) runWithReturn.returnObject).booleanValue();
    }
    return setComponentsFeature(getSelectedComponentsOrdered(), featureName, featureValue);
  }
  /**
   * Applies a function to a group component.  Only called from apply.
   *
   * @param group	component group to apply to
   * @param function	function to apply
   */
  private void setOnComponentGroup(ComponentGroup group, final String featureName, final Object featureValue) {
    // assume undo/redo steps controlled by calling process
    Component[] components = group.getComponents();
    Rectangle repaintArea = null;
    for(int i = 0; i < components.length; i++)
      repaintArea = setFeature(components[i], featureName, featureValue, repaintArea);
  }
  /**
   * Called when the mouse moves while a mouse button is down.
   * Should only be called from the event processing thread.
   *
   * @param evt	mouse event
   */
  public void mouseDragged(MouseEvent evt) {
    Point eventPoint = evt.getPoint().getLocation();
    Component comp =  evt.getComponent();
    if(comp != this) {
      Point loc = comp.getLocation();
      eventPoint.translate(loc.x, loc.y);
    }
    ShowPointTracker spt = getShowPointTracker();
    if(spt != null) {
      if( showPointComponent != null ) {
	Point crosshairPoint = eventPoint.getLocation();
	crosshairPoint.translate(-showPointComponentOffset.x,
				 -showPointComponentOffset.y);
	crosshairIndices = showPointComponent.getIndices(crosshairPoint);
	spt.trackPoint(showPointComponent, crosshairIndices);
      }
      else showBackgroundPoint(eventPoint);
    }
    if(itIsADrag) drag(getEventPointOnScreen(evt, false)); // update drag
    else {
      if(selectPoint != null) {
        if(selectBox != null) {
	  xorDrawBox(selectBox); // clear the currently drawn box
	  selectBox.setBounds(selectPoint.x, selectPoint.y, 0, 0);
        } else selectBox = new Rectangle(selectPoint);
        selectBox.add(eventPoint); // for left and top additions
        // for right and bottom additions
        selectBox.add(eventPoint.x + 1, eventPoint.y + 1);
        xorDrawBox(selectBox); // draw this new box
      }
    }
  }
  /**
   * Catches mouse up events to display a submenu.
   * Should only be called from the event processing thread.
   *
   * @param evt	mouse event
   */
  public void mouseReleased(MouseEvent evt){
    if(showPointComponent != null) {
      ShowPointTracker spt = getShowPointTracker();
      if(spt != null) {
	// erasing tracking crosshairs
	spt.trackPoint(showPointComponent, null);
	// show permanent crosshairs
	if(crosshairIndices == null) spt.syncToCrosshairs();
	else spt.setCrosshairs(showPointComponent, crosshairIndices);
      }
      showPointComponent = null;
    }
    boolean endingInsertDrag = (insertDragCursor != null);
    Point endDragAmount = endDrag(); // end outstanding drags
    if((endDragAmount.x != 0) || (endDragAmount.y != 0)) {
	if(endingInsertDrag) {
	    Point pt = getNextLocation();
	    resetLayout(pt.x + endDragAmount.x, pt.y + endDragAmount.y, true);
	}
	else {
	    // translate all selected components
	    translateSelections(endDragAmount);
	}
    }
    selectPoint = null;
    if(selectBox != null) {
      xorDrawBox(selectBox); // erase the currently drawn box
      if(currentComp == null) selectBoxedObjects(selectBox);
      selectBox = null;  // select box not redrawn
    }
    //    if(evt.isPopupTrigger() && ( (currentComp != null) || (currentGroup != null) ) ) {
    if(evt.isPopupTrigger())
      showImagePopupMenu(evt.getComponent(), evt.getX(), evt.getY());
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
    Point eventPoint = evt.getPoint().getLocation();
    endDrag();  // clear any outstanding drags
    currentComp = evt.getComponent();
    currentGroup = null;
    if(currentComp == this) currentComp = getDisplayComponent(eventPoint);
    else {
      // determine location relative to this container
      Point loc = currentComp.getLocation();
      eventPoint.translate(loc.x, loc.y);
      // may be in a subcontainer such as a GroupComponent
      Component c = currentComp.getParent();
      while((c != this) && (c != null)) {
        loc = c.getLocation();
        eventPoint.translate(loc.x, loc.y);
	c = c.getParent();
      }
      if(c != this) {
        currentComp = null;
        return; // ignore invalid sources
      }
    }

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
    if(currentComp == null) {
      // over the background
      if( ((modifiers & InputEvent.BUTTON2_MASK) != 0) ||
	  evt.isAltDown() || evt.isControlDown()) {
	Point screenPoint = getEventPointOnScreen(evt, true);
	if(nextLocationCursorShow &&
	   nextLocationCursor.contains(eventPoint)) {
	  startInsertDrag(screenPoint);
	} else {
	  // start dragging display area in scroll pane
	  startDisplayDrag(screenPoint);
	}
      }
      else if( (modifiers & InputEvent.BUTTON3_MASK) != 0 ) {
	// if mouse button 3 is pressed over the background
	// find the menu bar, control panel or status window
	CNUViewer cnuv = getCNUViewer();
	cnuv.setMenuBarVisible(true);
	if(! cnuv.isMenuBarVisible()) {
	  cnuv.setToolPanelVisible(true);
	  if(! cnuv.isToolPanelVisible()) {
	    showStatus("neither the main menu nor the control panel are available");
	  }
	}
      }
      else {
	// outside of a component we show the point
	showBackgroundPoint(eventPoint);
	// and draw a selection box if dragged
	selectPoint = eventPoint;
	// change cursor to reflect selection mode
	saveOldSetNewCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      }
    }
    else {
      // over a component
      if(evt.isPopupTrigger()) {
	showImagePopupMenu(evt.getComponent(), evt.getX(), evt.getY());
      }
      else if( ((modifiers & InputEvent.BUTTON2_MASK) != 0) ||
	       evt.isAltDown() || evt.isControlDown()) {
	// start dragging selections
	startDrag(evt);
	// show background location for drags
	showBackgroundPoint(eventPoint);
      }
      else {
	Point componentLocation;
	showPointComponentOffset = new Point(0, 0);
	if(currentComp instanceof ComponentGroup) {
	  currentGroup = (ComponentGroup) currentComp;
	  while(currentComp instanceof ComponentGroup) {
	    componentLocation = currentComp.getLocation();
	    showPointComponentOffset.translate(componentLocation.x, componentLocation.y);
	    eventPoint.translate(-componentLocation.x, -componentLocation.y);
	    currentComp =
		((ComponentGroup) currentComp).getDisplayComponent(eventPoint);
	  }
	}
	if(currentComp instanceof ShowPointImage) {
	  componentLocation = currentComp.getLocation();
	  showPointComponentOffset.translate(componentLocation.x, componentLocation.y);
	  eventPoint.translate(-componentLocation.x, -componentLocation.y);
          // display point if available
	  showPointComponent = (ShowPointImage) currentComp;
          crosshairIndices = showPointComponent.getIndices(eventPoint);
	  ShowPointTracker spt = getShowPointTracker();
          if(spt != null) spt.trackPoint(showPointComponent,
					 crosshairIndices);
	}
      }
    }
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
  public void mouseExited(MouseEvent evt) {
//    endDrag(); // make sure no outstanding drags
  };
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
   * Draws a polygon with exclusive or'ing (xor) to allow erasing the same way.
   *
   * @param poly	polygon to draw
   */
  public void xorDrawPoly(SimplePolygon poly) {
    Graphics drawBoxG = getGraphics();
    drawBoxG.setXORMode(Color.red);
    poly.draw(drawBoxG);
  }
  /**
   * Selects all components.
   */
  public void selectAll() {
    Component[] components = getComponents();
    for(int i=0; i<components.length; i++) addSelection(components[i]);
  }
  /**
   * Selects components that fall within a box.
   *
   * @param selectBox	box components must fall within to be selected
   */
  public void selectBoxedObjects(Rectangle selectBox) {
    Component[] components = getComponents();
    for(int i=0; i<components.length; i++) {
      Component comp = components[i];
      if( selectBox.equals(selectBox.union( comp.getBounds() )) )
	 addSelection(comp);
    }
  }
  /**
   * Select top components that no overlapping components
   * in front of them.
   */
  public void selectTopComponents() {
    Component[] components = getTopComponents();
    for(int i=0; i<components.length; i++) addSelection(components[i]);
  }
  /**
   * Get top components that have no overlapping components
   * in front of them.
   *
   * @return array of top components.
   */
  public Component[] getTopComponents() {
    Component[] components = getComponents();
    Vector<Component> topVector = new Vector<Component>(components.length);
    for(int i=0; i<components.length; i++) {
	Component testcomp = components[i];
	Rectangle testbounds = testcomp.getBounds();
	boolean overlaps = false;
	for(int j=i+1; j<components.length; j++) {
	    if(testbounds.intersects(components[j].getBounds())) {
		overlaps = true;
		break;
	    }
	}
	if(! overlaps) topVector.addElement(testcomp);
    }
    Component[] topArray = new Component[topVector.size()];
    topVector.copyInto(topArray);
    return topArray;
  }
  /**
   * Select bottom components that no overlapping components
   * behind them.
   */
  public void selectBottomComponents() {
    Component[] components = getBottomComponents();
    for(int i=0; i<components.length; i++) addSelection(components[i]);
  }
  /**
   * Get bottom components that have no overlapping components
   * behind them.
   *
   * @return array of bottom components.
   */
  public Component[] getBottomComponents() {
    Component[] components = getComponents();
    Vector<Component> bottomVector = new Vector<Component>(components.length);
    for(int i=0; i<components.length; i++) {
	Component testcomp = components[i];
	Rectangle testbounds = testcomp.getBounds();
	boolean overlaps = false;
	for(int j=i-1; j>=0; j--) {
	    if(testbounds.intersects(components[j].getBounds())) {
		overlaps = true;
		break;
	    }
	}
	if(! overlaps) bottomVector.addElement(testcomp);
    }
    Component[] bottomArray = new Component[bottomVector.size()];
    bottomVector.copyInto(bottomArray);
    return bottomArray;
  }
  /**
   * Select components that relate to the same object.
   */
  public void selectSameObjectComponents(String probCommand) {
    Component comp = getCurrentComponent();
    if(comp == null) return;
    try {
      Method method = comp.getClass().getMethod(probCommand, new Class[0]);
      Object compObj = method.invoke(comp, new Object[0]);
      Component[] components = getSameObjectComponents(probCommand, compObj);
      if(components != null) 
	for(int i=0; i<components.length; i++) addSelection(components[i]);

    } catch (NoSuchMethodException nsme) { // ignore
    } catch (SecurityException se) { // ignore
    } catch (IllegalAccessException iae) { // ignore
    } catch (IllegalArgumentException iarge) { // ignore
    } catch (InvocationTargetException ite) { // ignore
    }
  }
  /**
   * Get components that have a related value.
   *
   * @param probCommand command to execute on components to retrieve
   *                    compare object
   * @param sameAsObject object to compare probed values to
   * @return array of related components.
   */
  public Component[] getSameObjectComponents(String probCommand,
					     Object sameAsObject) {
    if(probCommand == null) return null;
    probCommand = probCommand.trim();
    if(probCommand.length() == 0) return null;

    Component[] components = getComponents();
    Vector<Component> sameVector = new Vector<Component>(components.length);
    Class[] emptyClassArray = new Class[0];
    Object[] emptyObjectArray = new Object[0];
    for(int i=0; i<components.length; i++) {
      Component testcomp = components[i];
      try {
	Method method = testcomp.getClass().getMethod(probCommand, emptyClassArray);
	Object compObj = method.invoke(testcomp, emptyObjectArray);
	if( (sameAsObject == null) ?
	    (compObj == null) : sameAsObject.equals(compObj) )  
	  sameVector.addElement(testcomp);
      } catch (NoSuchMethodException nsme) { // ignore
      } catch (SecurityException se) { // ignore
      } catch (IllegalAccessException iae) { // ignore
      } catch (IllegalArgumentException iarge) { // ignore
      } catch (InvocationTargetException ite) { // ignore
      }
    }
    Component[] sameArray = new Component[sameVector.size()];
    sameVector.copyInto(sameArray);
    return sameArray;
  }
  /**
   * Group overlapping components.
   */
  public void groupOverlappingComponents() {
    Component[] components = getSelectedComponentsOrdered();
    if(components == null) components = getComponents();
    if(components == null) return;
    groupComponents(getOverlappingComponents(components), true);
  }
  /**
   * Get overlapping components
   *
   * @param components array of 
   * @return vector containing arrays of overlapping components.
   */
  public Vector<Component[]> getOverlappingComponents(Component[] components) {
    if(components == null) return null;
    Hashtable<Component, Integer> compgroup = new Hashtable<Component, Integer>(components.length);
    Vector<Vector<Component>> groups = new Vector<Vector<Component>>();
    int nextgrp = 0;
    for(int i=0; i<components.length; i++) {
      Component firstcomp = components[i];
      Integer firstgroup = compgroup.get(firstcomp);
      Rectangle firstbounds = firstcomp.getBounds();
      for(int j=i+1; j<components.length; j++) {
	Component secondcomp = components[j];
	if(firstbounds.intersects(secondcomp.getBounds())) {
	  Integer secondgroup = compgroup.get(secondcomp);
	  if(firstgroup == null) {
	    if(secondgroup == null) {
	      int groupnumber = nextgrp++;
	      Integer groupobj = new Integer(groupnumber);
	      Vector<Component> groupvector = new Vector<Component>();
	      groups.addElement(groupvector);
	      compgroup.put(firstcomp, groupobj);
	      groupvector.addElement(firstcomp);
	      compgroup.put(secondcomp, groupobj);
	      groupvector.addElement(secondcomp);
	      
	    }
	    else {
	      compgroup.put(firstcomp, secondgroup);
	      groups.elementAt(secondgroup.intValue()).addElement(firstcomp);
	    }
	  }
	  else {
	    if(secondgroup == null) {
	      groups.elementAt(firstgroup.intValue()).addElement(secondcomp);
	      compgroup.put(secondcomp, firstgroup);
	    }
	    else {
	      // move every component in the second group
	      // into the first
	      Vector<Component> firstgroupcomps = groups.elementAt(firstgroup.intValue());
	      Vector<Component> secondgroupcomps = groups.elementAt(secondgroup.intValue());
	      for(Enumeration e = secondgroupcomps.elements();
		  e.hasMoreElements(); ) {
		Component comp = (Component) e.nextElement();
		//		compgroup.put(firstgroup, comp); // how did this work
		compgroup.put(comp, firstgroup);
		firstgroupcomps.addElement(comp);
	      }
	      secondgroupcomps.removeAllElements();
	    }
	  }
	}
      }
    }
    // put results into a vector of arrays
    Vector<Component[]> groupedcomponents = new Vector<Component[]>();
    if(groups.size() > 0) {
      for(Enumeration<Vector<Component>> e=groups.elements(); e.hasMoreElements(); ) {
	Vector<Component> groupcomps = e.nextElement();
	if(groupcomps.size() > 1) {
	  Component[] comps = new Component[groupcomps.size()];
	  groupcomps.copyInto(comps);
	  groupedcomponents.addElement(comps);
	}
      }
    }
    return groupedcomponents;
  }
  private void updateActionStates() {
    getCNUViewer().getCNUViewerActions().updateActionStates();
  }
  /**
   * Add a component to the list of selected components.
   *
   * @param comp	the component to add
   */
  public void addSelection(final Component comp) {
    if(comp == null) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { addSelection(comp); }
      } );
    }
    else if( isImmediateParentOf(comp) && (! selectedObjects.contains(comp)) ) {
      selectedObjects.addElement(comp);
      updateActionStates();
      Rectangle repaintArea = comp.getBounds();
      repaintArea.grow(1, 1);
      repaint(repaintArea.x, repaintArea.y, repaintArea.width,
	      repaintArea.height);
    }
  }
  /**
   * Clears a component from the selection list.
   *
   * @param comp	the component to clear
   */
  public void clearSelection( final Component comp ) {
    if(comp == null) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { clearSelection(comp); }
      } );
      return;
    }
    endDrag(); // make sure no outstanding drags
    if( selectedObjects.contains(comp) ) {
      Rectangle repaintArea = comp.getBounds();
      selectedObjects.removeElement(comp);
      if(comp == displayedCropComp) cropBoxUpdate(null, null);
      updateActionStates();
      repaintArea.grow(1, 1);
      repaint(repaintArea.x, repaintArea.y, repaintArea.width,
	      repaintArea.height);
    }
  }
  /**
   * Clears all selections from the selection list.
   */
  public void clearAllSelections() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { clearAllSelections(); }
      } );
      return;
    }
    endDrag(); // make sure no outstanding drags
    Rectangle repaintArea = null;
    if(! selectedObjects.isEmpty() ) {
      Enumeration e = selectedObjects.elements();
      while(e.hasMoreElements()) {
	Component comp = (Component) e.nextElement();
	if(repaintArea == null)repaintArea = comp.getBounds();
	else repaintArea.add(comp.getBounds());
      }
      selectedObjects.removeAllElements();
      cropBoxUpdate(null, null);
      updateActionStates();
    }
    if(repaintArea != null) {
      repaintArea.grow(1, 1);
      repaint(repaintArea.x, repaintArea.y,
      repaintArea.width, repaintArea.height);
    }
  }
  /**
   * Converts everything displayed into a CNU script.
   *
   * @return	script that can recreate the displayed objects
   */
  public String toScript() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = toScript(); }
      };
      runWithReturn.invokeAndWait();
      return (String) runWithReturn.returnObject;
    }
    endDrag(); // make sure no outstanding drags

    String className = getClass().getName();
    CNUScriptObjects scriptedObjects = new CNUScriptObjects();
    StringBuffer sb = new StringBuffer(backgroundToScript());
    sb.append("// -- start ").append(className).append(" components scripts\n");

    int nComponents = getDisplayComponentCount();
    for(int i = 0; i < nComponents; i++) {
      Component comp = getDisplayComponent(i);
      if(comp instanceof iiVScriptable) {
	// position
	Point location = comp.getLocation();
	sb.append("position(").append(location.x).append(", ");
	sb.append(location.y).append(");\n");
	sb.append(((iiVScriptable) comp).toScript(scriptedObjects));
	sb.append("displayImageData(script_rtn);\n");
      }
      else showStatus("Warning: could not script component=" + comp);
    } // end for(int i = 0; i < nComponents; i++)
    // script the ShowPointDialog if it exists
    Dialog spd =
      getCNUViewer().getExistingNamedDialog("iiv.dialog.ShowPointDialog");
    if(spd instanceof iiVScriptable)
       sb.append(((iiVScriptable) spd).toScript(scriptedObjects));
    // script the ShowPointTracker
    ShowPointTracker spt = getShowPointTracker();
    if(spt instanceof iiVScriptable)
       sb.append(((iiVScriptable) spt).toScript(scriptedObjects));
    sb.append(scriptedObjects.buildUnsetAllScript());
    sb.append("// -- end ").append(className).append(" components scripts\n");
    return sb.toString();
  }
  /**
   * Converts everything displayed within a region into a CNU script.
   *
   * @param region	region to create script for
   * @return	script that can recreate the displayed objects
   */
  public String toScript(final Rectangle region) {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = toScript(region); }
      };
      runWithReturn.invokeAndWait();
      return (String) runWithReturn.returnObject;
    }

    endDrag(); // make sure no outstanding drags

    CNUScriptObjects scriptedObjects = new CNUScriptObjects();
    String className = getClass().getName();
    StringBuffer sb = new StringBuffer();

    sb.append("// -- start ").append(className).append(" region script\n");

    String componentListName = "regionscriptcomponentstmp";
    String locationListName = "regionscriptlocationstmp";

    boolean nothingScripted = true;
    int nComponents = getDisplayComponentCount();
    for(int i = 0; i < nComponents; i++) {
      Component comp = getDisplayComponent(i);
      if(comp instanceof iiVScriptable) {
	// position
	Rectangle bounds = comp.getBounds();
	boolean scriptThis = true;
	if(region != null) scriptThis = region.intersects(bounds);
	if(scriptThis) {
	  if(nothingScripted) {
	    nothingScripted = false;
	    sb.append(componentListName).append(" = new java.util.Vector();\n");
            sb.append(locationListName).append(" = new java.util.Vector();\n");
          }
	  sb.append(((iiVScriptable) comp).toScript(scriptedObjects));
	  sb.append(componentListName).append(".addElement(script_rtn);\n");
	  sb.append(locationListName).append(".addElement(");
	  sb.append("new java.awt.Point(").append(bounds.x).append(", ");
	  sb.append(bounds.y).append(")");
	  sb.append(");\n");
	}
      }
      else showStatus("Warning: could not script component=" + comp);
    } // end for(int i = 0; i < nComponents; i++)
    if(! nothingScripted) {
      sb.append("displayImageData(");
      if(region != null) {
        sb.append("new ").append(ComponentGroup.class.getName()).append("(");
	sb.append(componentListName).append(", ");
	sb.append(locationListName).append(", ");
        sb.append("new java.awt.Rectangle(");
	sb.append(region.x).append(", ");
	sb.append(region.y).append(", ");
	sb.append(region.width).append(", ");
	sb.append(region.height).append(")");
	sb.append(")");
      }
      else {
        sb.append("new ").append(ComponentGroup.class.getName()).append("(");
	sb.append(componentListName).append(", ");
	sb.append(locationListName).append(")");
      }
      sb.append(");\n");
      sb.append("unset(\"").append(componentListName).append("\");\n");
      sb.append("unset(\"").append(locationListName).append("\");\n");

      // script the ShowPointTracker to get show point dialogs, crosshairs,
      // tracking working
      // script the ShowPointTracker
      ShowPointTracker spt = getShowPointTracker();
      if(spt instanceof iiVScriptable)
	  sb.append(((iiVScriptable) spt).toScript(scriptedObjects));

      sb.append(scriptedObjects.buildUnsetAllScript());
    }
    sb.append("// -- end ").append(className).append(" region script\n");
    return sb.toString();
  }
  /**
   * Converts a list of components to a script.
   *
   * @param components	components to create script for
   * @return	script that can recreate the displayed objects
   */
  public String toScript(final Component[] components) {
    if(components == null) return null;
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = toScript(components); }
      };
      runWithReturn.invokeAndWait();
      return (String) runWithReturn.returnObject;
    }

    endDrag(); // make sure no outstanding drags

    CNUScriptObjects scriptedObjects = new CNUScriptObjects();
    String className = getClass().getName();
    StringBuffer sb = new StringBuffer();

    sb.append("// -- start ").append(className).append(" components script\n");

    String componentListName = "componentsscriptcomponentstmp";
    String locationListName = "componentsscriptlocationstmp";

    boolean nothingScripted = true;
    int nComponents = components.length;
    for(int i = 0; i < nComponents; i++) {
      Component comp = components[i];
      if(comp instanceof iiVScriptable) {
	// position
	Rectangle bounds = comp.getBounds();
	boolean scriptThis = true;
	if(nothingScripted) {
	    nothingScripted = false;
	    sb.append(componentListName).append(" = new java.util.Vector();\n");
            sb.append(locationListName).append(" = new java.util.Vector();\n");
	}
	sb.append(((iiVScriptable) comp).toScript(scriptedObjects));
	sb.append(componentListName).append(".addElement(script_rtn);\n");
	sb.append(locationListName).append(".addElement(");
	sb.append("new java.awt.Point(").append(bounds.x).append(", ");
	sb.append(bounds.y).append(")");
	sb.append(");\n");
      }
      else showStatus("Warning: could not script component=" + comp);
    } // end for(int i = 0; i < nComponents; i++)
    if(! nothingScripted) {
      sb.append("displayImageData(");
      sb.append("new ").append(ComponentGroup.class.getName()).append("(");
      sb.append(componentListName).append(", ");
      sb.append(locationListName).append(")");
      sb.append(");\n");
      sb.append("unset(\"").append(componentListName).append("\");\n");
      sb.append("unset(\"").append(locationListName).append("\");\n");

      // script the ShowPointTracker to get show point dialogs, crosshairs,
      // tracking working
      ShowPointTracker spt = getShowPointTracker();
      if(spt instanceof iiVScriptable)
	  sb.append(((iiVScriptable) spt).toScript(scriptedObjects));

      sb.append(scriptedObjects.buildUnsetAllScript());
    }
    sb.append("// -- end ").append(className).append(" components script\n");
    return sb.toString();
  }
  /**
   * Creates a script for selected region or selected components
   *
   * @return script to recreate region or selected components
   *
   */
  public String selectedToScript() {
    Rectangle region = getDisplayedSelectRegion();
    if(region != null) return toScript(region);
    else {
      Component[] components = getSelectedComponentsOrdered();
      if(components != null)
	return toScript(components);
    }
    return null;
  }
  /**
   * Converts a list of components to an image.
   *
   * @param components	components to create script for
   * @return	script that can recreate the displayed objects
   */
  public Image toImage(final Component[] components) {
    if(components == null) return null;
    if(components.length < 1) return null;
    return toImage(components, null);
  }
  /**
   * Converts a region to an image.
   *
   * @param region	area to restrict image to
   * @return	image showing given components over region
   */
  public Image toImage(final Rectangle region) {
    return toImage(null, region);
  }
  /**
   * Creates an image of a selected region or selected components
   *
   * @return image of selected region or components
   *
   */
  public Image selectedToImage() {
      Rectangle region = getDisplayedSelectRegion();
      if(region != null) return toImage(region);
      else {
	  Component[] components = getSelectedComponentsOrdered();
	  if(components != null)
	      return toImage(components);
      }
      return null;
  }
  /**
   * Converts components over a region to an image.
   *
   * @param components	components to create image of
   * @param region	area to restrict image to
   * @return	image showing given components over region
   */
  public Image toImage(final Component[] components, final Rectangle region) {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = toImage(components, region); }
      };
      runWithReturn.invokeAndWait();
      return (Image) runWithReturn.returnObject;
    }

    endDrag(); // make sure no outstanding drags

    Rectangle localRegion = region;
    if(localRegion == null) {
	if(components == null) localRegion=new Rectangle(getPreferredSize());
	else {
	  for(int i = 0; i < components.length; i++) {
	      if(components.length < 1) return null;
	      Component comp = components[i];
	      if(localRegion == null) localRegion = comp.getBounds();
	      else localRegion.add(comp.getBounds());
	  }
	}
    }

    //    Image tmpImage = createImage(localRegion.width, localRegion.height);
    BufferedImage tmpImage = new BufferedImage(localRegion.width, localRegion.height, BufferedImage.TYPE_INT_RGB);
    Graphics g = tmpImage.getGraphics();
    // some paint algorithms have trouble if clipRect is not set
    g.clipRect(0, 0, localRegion.width, localRegion.height);
    // reset drawing origin from region
    g.translate( -localRegion.x, -localRegion.y );

    if(components == null) {
	// paint all components and other normally painted graphics
	paintAll(g);
    }
    else {
	// paint only given components
	for(int i = 0; i < components.length; i++) {
	    Component comp = components[i];
	    Rectangle compBounds = comp.getBounds();
	    g.translate(compBounds.x, compBounds.y);
	    comp.paint(g);
	    g.translate(-compBounds.x, -compBounds.y);
	}
    }
    g.dispose(); g = null;
    return tmpImage;
  }
  /**
   * Gets the text from a list of components.
   *
   * @param components	components to create text for
   * @return	String that represents the components
   */
  public String getText(Component[] components) {
    if(components == null) return null;
    if(components.length < 1) return null;  
    StringBuffer sb=new StringBuffer();
    // get text only of components that implement getText
    for(int i = 0; i < components.length; i++) {
      if(sb.length() > 0) sb.append("\n");
      String comptext = getText(components[i]);
      if((comptext != null) && (comptext.length() > 0)) {
	if(sb.length() > 0) sb.append("\n");
	sb.append(comptext);
      }
    }
    return sb.toString();
  }
  /**
   * Converts everything displayed within a region into text.
   *
   * @param region	region to create text for
   * @return	text from components within region
   */
  public String getText(final Rectangle region) {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = getText(region); }
      };
      runWithReturn.invokeAndWait();
      return (String) runWithReturn.returnObject;
    }
    endDrag(); // make sure no outstanding drags

    Component[] components = getComponents();
    if(region == null) return getText(components);
    else {
      StringBuffer sb = new StringBuffer();
      for(int i=0; i<components.length; i++) {
	Component comp = components[i];
	if( region.equals(region.union( comp.getBounds() )) ) {
	  String comptext = getText(comp);
	  if((comptext != null) && (comptext.length() > 0)) {
	    if(sb.length() > 0) sb.append("\n");
	    sb.append(comptext);
	  }
	}
      }
      return sb.toString();
    }
  }
  /**
   * Converts the background of the display into a CNU script.
   *
   * @return	script that can recreate the background
   */
  public String backgroundToScript() {
    StringBuffer sb=new StringBuffer();
    String classname = getClass().getName();
    sb.append("// -- start ").append(classname).append(" background script\n");
    Color bg = getBackground();
    sb.append("backgroundColor(" + bg.getRed() + ", " + bg.getGreen() +
	      ", " + bg.getBlue() + ");\n");
    sb.append("screenResCorrection(" + getScreenResCorrection() + ");\n");
    Color gc = getGridColor();
    sb.append("gridColor(" + gc.getRed() + ", " + gc.getGreen() +
	      ", " + gc.getBlue() + ");\n");
    sb.append("grid(" + getGridSpacing() + ", \"pixels\");\n");
    sb.append("gridOffsets(" + getGridOffsetX() + ", " +
	       getGridOffsetY() + ", \"pixels\");\n");
    if(getGridState()) sb.append("grid(\"on\");\n");
    else sb.append("grid(\"off\");\n");
    Color pc = getPaperColor();
    sb.append("paperColor(" + pc.getRed() + ", " + pc.getGreen() +
	    ", " + pc.getBlue() + ");\n");
    sb.append("paper(" + getPaperWidth() + ", " + getPaperHeight() +
	      ", \"pixels\");\n");
    sb.append("paperOffsets(" + getPaperOffsetX() + ", " +
	       getPaperOffsetY() + ", \"pixels\");\n");
    if(getPaperState()) sb.append("paper(\"on\");\n");
    else sb.append("paper(\"off\");\n");
    sb.append("// -- end ").append(classname).append(" background script\n");
    return sb.toString();
  }
  /**
   * Determines if two int arrays are equal int length and values.
   *
   * @param ar1 first array
   * @param ar2 second array
   * @return	<code>true</code> if arrays are both null or are
   *		the same length and contain the same values.
   */
  public static boolean arraysEqual(int[] ar1, int[] ar2) {
    if(ar1 != ar2) {
      if(ar1 == null) return false;
      if(ar2 == null) return false;
      if(ar1.length != ar2.length) return false;
      for(int i = 0; i < ar1.length; i++) {
        if(ar1[i] != ar2[i]) return false;
      }
    }
    return true;
  }
  private Vector<ShowPointDisplay> backgroundShowPointDisplays = new Vector<ShowPointDisplay>(1);
  /**
   * Adds a ShowPointDisplay to list of displays called to show
   * background locations.
   *
   * @param spd ShowPointDisplay to add to list.
   */
  public void addBackgroundShowPointDisplay(final ShowPointDisplay spd) {
    if(spd == null) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	    public void run() { addBackgroundShowPointDisplay(spd); }
	  } );
    }
    else {
      if(! backgroundShowPointDisplays.contains(spd))
	backgroundShowPointDisplays.addElement(spd);
    }
  }
  /**
   * Shows the indices from a the background in
   * show point displays.
   *
   * @param pt location to show
   */
  public void showBackgroundPoint(final Point pt) {
    if(pt == null) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	    public void run() { showBackgroundPoint(pt); }
	  } );
    }
    else {
      int[] indices = new int[] {pt.x, pt.y};
      Enumeration<ShowPointDisplay> e = backgroundShowPointDisplays.elements();
      while(e.hasMoreElements()) {
	ShowPointDisplay spd = e.nextElement();
	spd.showPoint(indices, 0, 0, "background");
      }
    }
  }
  /**
   * Gets the indices corresponding to the original raw data this display
   * component is created from based on a point relative to the component.
   *
   * @param pt	point 	location relative to component
   * @return	indices indices to original raw data which may have
   *			any number of dimensions or <code>null</code>
   *			if invalid point
   */
  public int[] getIndices(Point pt) { return new int[] {pt.x, pt.y}; }
}  // end CNUDisplay class
