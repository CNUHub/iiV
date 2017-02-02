package iiv.dialog;
import iiv.*;
import iiv.display.*;
import iiv.util.*;
import iiv.data.*;
import iiv.script.*;
import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.lang.*;
import java.net.*;
import java.text.*;
import java.util.*;
import iiv.gui.*;
import iiv.script.*;
/**
 * Dialog to display coordinate values and locations.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUDisplay
 * @since	iiV1.0
 */
public class ShowPointDialog extends CNUDialog
implements ShowPointDialogInterface,
	   ShowPointTracker, ShowPointDisplay,
	   ContainerListener, NumberFormattable, 
	   iiVScriptable
{
  private static final long serialVersionUID = 2747393585697230989L;
  private boolean packed = false;


  public static final int SELECT_ALL_LINES = 31;
  public static final int SELECT_ADDITIONS = 32;
  public static final int DONT_SELECT_ADDITIONS = 33;
  public static final int DELETE_SHOW_POINT_LINES = 34;
  public static final int CLEAR_SHOW_POINT_LINES = 35;
  public static final int APPLY_DEFAULT_NUMBER_FORMAT = 36;

  public static final int DISMISS = 99;

  private class LocalAction extends EasyAddAbstractAction {
    private static final long serialVersionUID = -7763642124741775195L;
    private int command;
    public LocalAction(String name, int cmd) {
      super(name);
      command = cmd;
    }
    public LocalAction(String name, String toolTip, int cmd) {
      super(name, toolTip);
      command = cmd;
    }
    public LocalAction(String name, String toolTip,
		       int Mnemonic, int cmd) {
     super(name, toolTip, Mnemonic);
     command = cmd;
    }
    public void setCommand(int cmd) { command = cmd; }
    public void actionPerformed(ActionEvent ae) {
      getCNUViewer().setWaitCursor();
      try {
	switch(command) {
	case DELETE_SHOW_POINT_LINES:
          removeShowPointLines();
	  break;
	case SELECT_ALL_LINES:
          selectAllLines();
	  break;
	case SELECT_ADDITIONS:
          setSelectAdditions(selectAdditionsCBMI.isSelected());
	  break;
	case DONT_SELECT_ADDITIONS:
          setSelectAdditions(false);
	  break;
	case CLEAR_SHOW_POINT_LINES:
	  removeAllLines();
	  break;
	case APPLY_DEFAULT_NUMBER_FORMAT:
	  applyDefaultNumberFormat();
	  break;
	case DISMISS:
          setVisible(false);
	  break;
        default:
	  break;
        }
      } finally {
        getCNUViewer().setNormalCursor();
      }
    }
  }

  private Object localSetLock = new Object();
  private ShowPointController showPointController = null;

    //  private LocalAction selectAdditionsAction = null;
  JCheckBoxMenuItem selectAdditionsCBMI;

  private Component showPointDisplayLineTitle =
    ShowPointDisplayLine.getTitleComponent();
  private ShowPointDisplayLine showPointDisplayLine = new ShowPointDisplayLine();
  private StatusWindowShowPointDisplay statusWindowShowPointDisplay = null;

    //  private Vector crosshairTrackers = new Vector();
    //  private Vector sliceTrackers = new Vector();

    //  private Vector showPointPairs = new Vector();
    //  private Vector showPointImages = new Vector();
    //  private Vector showPointDisplays = new Vector();

  private ShowPointContainer showPointContainer = new ShowPointContainer();
  public class ShowPointContainer extends CNUContainer {
    private static final long serialVersionUID = 635195506209153322L;
    /** Should only be called from the event dispatching thread. */
    public void remove(Component comp, UndoRedo ur) {
      // don't allow removal of top lines
      if((comp != showPointDisplayLineTitle) &&
	 (comp != showPointDisplayLine)) {
	super.remove(comp, ur);
	relayout();
      }
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
	comp.setFont(getFont());
	if(comp instanceof DisplayComponent) {
	    DisplayComponent dc = (DisplayComponent) comp;
	    dc.setCrop(null); dc.setFlips(false, false);
	    dc.setZoom(1, 1); dc.setRotation(0);
	}
	super.addImpl(comp, constraints, pos);
    }

    /** Should only be called from the event dispatching thread. */
    public void addSelection(Component comp) {
      if((comp != showPointDisplayLineTitle) &&
	 (comp != showPointDisplayLine)) super.addSelection(comp);
    }
    /** Should only be called from the event dispatching thread. */
    public void showPopupMenu(Component currentComponent,
			      Component evtComponent, int evtX, int evtY) {
      if((currentComponent != showPointDisplayLineTitle) &&
	 (currentComponent != showPointDisplayLine))
	super.showPopupMenu(currentComponent, evtComponent, evtX, evtY);
    }
  }
  /**
   * Constructs a new instance of ShowPointDialog.
   *
   * @param parentFrame	parent frame
   */
  public ShowPointDialog(Frame parentFrame) {
    this(parentFrame, null);
  }
  /**
   * Constructs a new instance of ShowPointDialog.
   *
   * @param parentFrame	parent frame
   * @param cnuv	CNUViewer to work with
   */
  public ShowPointDialog(Frame parentFrame, CNUViewer cnuv) {
    super(parentFrame, "Show Point", false, cnuv);
    if(cnuv != null) init();
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
    init();
  }
  private boolean initialized = false;
  /**
   * Initialize local parameters.  Not callable until CNUViewer set.
   *
   */
  private void init() {
    synchronized(this) {
      if(initialized) return;
      initialized = true;
    }
    Container contentPane = getContentPane();
    contentPane.setLayout(new BorderLayout());
    CNUDisplay cnud = getCNUDisplay();
    if(cnud != null) cnud.addBackgroundShowPointDisplay(this);

    // listen for removal of ShowPointimages or ShowPointDisplays
    //if(cnud != null) cnud.addContainerListener(getShowPointController());

    JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);

    JMenu imageFeatureMenu = new JMenu("Image Features");
    imageFeatureMenu.setToolTipText(
      "Pull this menu down to select image options");
    menuBar.add(imageFeatureMenu);

    getShowPointController().coordinateMappingAction.addTo(imageFeatureMenu);

    getShowPointController().mapTrackingAction.addCheckboxTo(imageFeatureMenu);
    imageFeatureMenu.addSeparator();

    getShowPointController().crosshairAction.addCheckboxTo(imageFeatureMenu);

    MenuList ml = new MenuList("Crosshair color:  ",
			       getShowPointController().crosshairColorBoxModel,
			       null, false);
    ml.setToolTipText("Pull to select the color for drawing crosshairs");
    imageFeatureMenu.add(ml);
    getShowPointController().unfreezeCrosshairAction.addTo(imageFeatureMenu);
    getShowPointController().freezeCrosshairAction.addTo(imageFeatureMenu);
    getShowPointController().deleteCrosshairAction.addTo(imageFeatureMenu);
    imageFeatureMenu.addSeparator();
    getShowPointController().startSliceTrackingAction.addTo(imageFeatureMenu);
    getShowPointController().stopSliceTrackingAction.addTo(imageFeatureMenu);

    JMenu showPointLinesMenu = new JMenu("Show Point Lines");
    showPointLinesMenu.setToolTipText(
      "Pull this menu down to select show point line options");
    menuBar.add(showPointLinesMenu);
    getShowPointController().recordAction.addCheckboxTo(showPointLinesMenu);
    showPointLinesMenu.addSeparator();
    getShowPointController().addShowPointLinesAction.addTo(showPointLinesMenu);
    showPointLinesMenu.addSeparator();
    getShowPointController().freezeShowPointLinesAction.addTo(showPointLinesMenu);
    getShowPointController().unfreezeShowPointLinesAction.addTo(showPointLinesMenu);
    getShowPointController().startRecordingAction.addTo(showPointLinesMenu);
    getShowPointController().stopRecordingAction.addTo(showPointLinesMenu);
    showPointLinesMenu.addSeparator();

    getShowPointController().addUnrelatedShowPointLineAction.addTo(showPointLinesMenu);
    getShowPointController().addShowPointLinesToDisplayAction.addTo(showPointLinesMenu);
    showPointLinesMenu.addSeparator();

    getShowPointController().freezeDisplayShowPointLinesAction.addTo(showPointLinesMenu);
    getShowPointController().unfreezeDisplayShowPointLinesAction.addTo(showPointLinesMenu);
    getShowPointController().startRecordingDisplayShowPointLinesAction.addTo(showPointLinesMenu);
    getShowPointController().stopRecordingDisplayShowPointLinesAction.addTo(showPointLinesMenu);

    JMenu editMenu = new JMenu("Edit");
    editMenu.setToolTipText("Pull this menu down to access edit commands.");
    editMenu.setMnemonic(KeyEvent.VK_E);
    menuBar.add(editMenu);

    new LocalAction("Select all Show Point Lines",
		    "Selects all show point lines",
		    SELECT_ALL_LINES).addTo(editMenu);
    selectAdditionsCBMI = new JCheckBoxMenuItem("Select Line Adds");
    selectAdditionsCBMI.setToolTipText("Check to automaticly select newly added lines");
    selectAdditionsCBMI.addActionListener(new LocalAction("select adds",
							  SELECT_ADDITIONS));
    editMenu.add(selectAdditionsCBMI);
    new LocalAction("Delete Show Point Line(s)",
		    "Deletes selected show point lines",
		    DELETE_SHOW_POINT_LINES).addTo(editMenu);
    new LocalAction("Clear Show Point Lines",
		    "Delete all show point lines",
		    CLEAR_SHOW_POINT_LINES).addTo(editMenu);
    editMenu.addSeparator();
    new LocalAction("Apply Number Format",
		    "Applies the default number format to selected or all lines",
		    APPLY_DEFAULT_NUMBER_FORMAT).addTo(editMenu);
    editMenu.addSeparator();

    UndoRedo ur = getUndoRedo();
    getUndoRedo().getUndoAction().addTo(editMenu);
    getUndoRedo().getRedoAction().addTo(editMenu);
    getUndoRedo().getUndoEnableAction().addTo(editMenu);
    getUndoRedo().getUndoDisableAction().addTo(editMenu);

    JMenu dismissMenu = new JMenu("Dismiss");
    dismissMenu.setToolTipText(
      "Pull this menu down to dismiss this window");
    menuBar.add(dismissMenu);
    new LocalAction("Dismiss",
		    "Hides this window",
		    DISMISS).addTo(dismissMenu);

    //    menuBar.add(Box.createHorizontalStrut(5));

    showPointContainer.setBackground(Color.white);
    showPointContainer.setForeground(Color.blue);
    showPointContainer.setFont(getFont());

    showPointContainer.setNumberOfColumns(1);
    showPointDisplayLineTitle.setForeground(Color.black);
    showPointContainer.add(showPointDisplayLineTitle);

    showPointDisplayLine.setForeground(Color.red);
    showPointDisplayLine.setFreezeState(false);
    //    showPointDisplayLine.setFreezeStateVisible(false);
    showPointDisplayLine.setTrackingStateVisible(false);
    showPointDisplayLine.setRecordShowPointDisplay(getStatusWindowShowPointDisplay());
    showPointContainer.add(showPointDisplayLine);
    showPointContainer.setUndoRedo(getUndoRedo());
    showPointContainer.setCNUViewer(getCNUViewer());
    // listen for removal of ShowPointDisplays
    
    showPointContainer.addContainerListener(getShowPointController());
    contentPane.add(new JScrollPane(showPointContainer), BorderLayout.CENTER);
  }
  /**
   * Overridden to pack this dialog before going visible the first time.
   * This dialog became the wrong size if additions were made after
   * packing and before displaying so packing postponed to just before
   * displaying.
   *
   * @param visible if <code>true</code> made visible else hidden
   */
  public void setVisible(boolean visible) {
    if(visible && !packed) synchronized (getTreeLock()) {
      if(!packed) { pack(); packed = true; }
    }
    super.setVisible(visible);
  }
  /**
   * Gets the show point controller to use with this show point dialog.
   *
   * @return    Show point controller used by this dialog
   * @see	ShowPointController
   */
  public ShowPointController getShowPointController() {
    if(showPointController == null) synchronized (localSetLock) {
	if(showPointController == null) {
	  showPointController =
	    (ShowPointController) getCNUViewer().getShowPointController();
	  if(showPointController != null) {
	    showPointController.setShowPointContainer(showPointContainer);
//	  showPointController.addShowPointPair(this, null, false);
	    showPointController.addShowPointPair(showPointDisplayLine,
						 null, false);
	  }
	}
    }
    return showPointController;
  }
  /**
   * Gets the UndoRedo handler for this display.
   *
   * @return UndoRedo handler for this display
   */
  public UndoRedo getUndoRedo() {
    return getShowPointController().getUndoRedo();
  }
  /**
   * Sets the UndoRedo handler for this display.
   *
   * @param ur new UndoRedo handler for this display.
   *	       if <code>null</code> a default handler will be created
   */
  public void setUndoRedo(UndoRedo ur) {
    getShowPointController().setUndoRedo(ur);
  }
  /**
   * Gets the StatusWindowShowPointDisplay for use with this dialog.
   *
   * @return the StatusWindowShowPointDisplay
   */
  public StatusWindowShowPointDisplay getStatusWindowShowPointDisplay() {
    return getShowPointController().getStatusWindowShowPointDisplay();
  }
  /**
   * Update display line visible features if the associated component
   * is added to CNUDisplay.  Part of ContainerListener.
   * Should only be called from the event dispatch thread.
   *
   * @param evt	container event
   */
  public void componentAdded(ContainerEvent evt) {
      // updateDisplayLineStates(evt.getChild(), true);
  }
  /**
   * Use to update tracking features if the associated component
   * is removed from CNUDisplay.  Part of ContainerListener.
   * Should only be called from the event dispatch thread.
   *
   * @param evt	container event
   */
   public void componentRemoved(ContainerEvent evt) {
     getShowPointController().componentRemoved(evt);
   }
  /**
   * Adds a show point display and show point image as a pair
   * that show points with undo/redo.
   *
   * @param spd	ShowPointDisplay to try adding.
   * @param spi	image to associate with spd
   */
  public void addShowPointPair(ShowPointDisplay spd,
			       ShowPointImage spi) {
    getShowPointController().addShowPointPair(spd, spi);
  }
  /**
   * Adds a show point display and show point image as a pair
   * that show points with possible undo/redo.
   *
   * @param spd	ShowPointDisplay to try adding.
   * @param spi	image to associate with spd
   * @param undoFlag if <code>true</code> creates undo/redo history
   */
  public void addShowPointPair(ShowPointDisplay spd,
			       ShowPointImage spi,
			       boolean undoFlag) {
    getShowPointController().addShowPointPair(spd, spi, undoFlag);
  }
  /**
   * Adds a show point display and show point image as a pair
   * from a hashtable listing pairs with possible undo/redo.
   *
   * @param pairs	hashtable of display and image pairs
   */
  public void addShowPointPairs(final Hashtable pairs) {
    getShowPointController().addShowPointPairs(pairs);
  }
  /**
   * Removes a show point display and show point image pair
   * from list that shows points with possible undo/redo.
   *
   * @param spd	ShowPointDisplay to try removing.
   */
  public void removeShowPointPair(ShowPointDisplay spd) {
    getShowPointController().removeShowPointPair(spd);
  }
  /**
   * Removes a show point display and show point image pair
   * from list that shows points with possible undo/redo.
   *
   * @param spd	ShowPointDisplay to try removing.
   * @param undoFlag if <code>true</code> creates undo/redo history
   */
  public void removeShowPointPair(ShowPointDisplay spd,
				  boolean undoFlag) {
    getShowPointController().removeShowPointPair(spd, undoFlag);
  }
  /**
   * Removes an array of components from list of show point pairs.
   *
   * @param components array of components to try removing
   */
  public void removeShowPointPairs(Component[] components) {
    getShowPointController().removeShowPointPairs(components);
  }
  /**
   * Gets the show point image related to a show point display.
   *
   * @param spd show point display to get related show point image for
   * @return related show point image or <code>null</code> if none found
   */
  public ShowPointImage getRelatedShowPointImage(ShowPointDisplay spd) {
    return getShowPointController().getRelatedShowPointImage(spd);
  }
  /**
   * Sends location to all show point pairs.
   *
   * @param point	array of indices specifying the point location
   * @param xyzpt	array of indices specifying the point location in
   *                    mapped coordinates which override point if
   *                    image implements CoordinateMappable
   * @param src_spi	source show point image
   */
  public void showPointViaPairs(int[] point,
				XYZDouble xyzpt,
				ShowPointImage src_spi) {
    getShowPointController().showPointViaPairs(point, xyzpt, src_spi);
  }
  /**
   * Queries whether a ShowPointDisplay is in the show point display
   * list.
   *
   * @param spd ShowPointDisplay to check list for
   * @return <code>true</code> if spd is in list otherwise <code>false</code>
   */
  public boolean isShowPointDisplay(ShowPointDisplay spd) {
    return getShowPointController().isShowPointDisplay(spd);
  }
 /**
   * Creates vector of components or subcomponents that are also current
   * show point displays.
   *
   * @param components array of components to search for show point displays in
   * @param list vector to add found show point displays to.  If <code>null</code>
   *             a new vector will be created if any displays are found
   * @return the input list or a new Vector with found displays added to it
   *         or <code>null</code> if no input list was <code>null</code> and no
   *         displays were found.
   */
  public Hashtable<ShowPointDisplay,ShowPointImage> whichAreShowPointDisplays(Component[] components,
					     Hashtable<ShowPointDisplay,ShowPointImage> list) {
    return getShowPointController().whichAreShowPointDisplays(components,
							      list);
  }
  /**
   * Adds a component to list of images that listens for crosshair positions
   * with undo/redo.
   * @param spi ShowPointImage to add to list
   */
  public void addCrosshairTracker(ShowPointImage spi) {
      getShowPointController().addCrosshairTracker(spi);
  }
  /**
   * Adds a component to list of images that listens for crosshair positions
   * with optional undo/redo.
   *
   * @param spi ShowPointImage to add to list
   * @param undoFlag	if <code>true</code> undo commands will be generated
   */
  public void addCrosshairTracker(ShowPointImage spi, boolean undoFlag) {
    getShowPointController().addCrosshairTracker(spi, undoFlag);
  }
  /**
   * Removes a component from list of images that listens for
   * crosshair positions with undo/redo.
   *
   * @param spi ShowPointImage to remove from list
   */
  public void removeCrosshairTracker(final ShowPointImage spi) {
    getShowPointController().removeCrosshairTracker(spi);
  }
  /**
   * Removes a component from list of images that listens for
   * crosshair positions with optional undo/redo.
   *
   * @param spi ShowPointImage to remove from list
   * @param undoFlag	if <code>true</code> undo commands will be generated
   */
  public void removeCrosshairTracker(ShowPointImage spi, boolean undoFlag) {
    getShowPointController().removeCrosshairTracker(spi, undoFlag);
  }
  /**
   * Queries whether a ShowPointImage is in the crosshair trackers
   * list.
   *
   * @param spi ShowPointImage to check list for
   * @return <code>true</code> if spi is in list otherwise <code>false</code>
   */
  public boolean isCrosshairTracker(ShowPointImage spi) {
    return getShowPointController().isCrosshairTracker(spi);
  }
 /**
   * Creates vector of components or subcomponents that are also current
   * crosshair trackers.
   *
   * @param components array of components to search for crosshair trackers in
   * @param list vector to add found crosshair trackers to.  If <code>null</code>
   *             a new vector will be created if any crosshair trackers are found
   * @return the input list or a new Vector with found crosshair trackers added to it
   *         or <code>null</code> if no input list was <code>null</code> and no
   *         crosshair trackers were found.
   */
  public Vector whichAreCrosshairTrackers(Component[] components, Vector<ShowPointImage> list) {
    return getShowPointController().whichAreCrosshairTrackers(components,
							      list);
  }
  /**
   * Adds currently selected components to list of crosshair trackers.
   */
  public void addCrosshairTrackers() {
    getShowPointController().addCrosshairTrackers();
  }
  /**
   * Adds an array of objects to list of crosshair trackers.
   *
   * @param objects array of possible ShowPointImages to try adding
   */
  public void addCrosshairTrackers(Object[] objects) {
    getShowPointController().addCrosshairTrackers(objects);
  }
  /**
   * Removes currently selected components from list of crosshair trackers.
   */
  public void removeCrosshairTrackers() {
    getShowPointController().removeCrosshairTrackers();
  }
  /**
   * Removes an array of components from list of crosshair trackers.
   *
   * @param components array of components to try removing
   */
  public void removeCrosshairTrackers(Component[] components) {
    getShowPointController().removeCrosshairTrackers(components);
  }
  /**
   * Sends tracking location to all crosshair trackers.
   *
   * @param point	array of indices specifying the point location
   * @param xyzpt	array of indices specifying the point location in
   *                    mapped coordinates which override point if
   *                    crosshair tracker implements CoordinateMappable
   */
  public void drawTrackingCrosshairs(int[] point,
				     XYZDouble xyzpt) {
    getShowPointController().drawTrackingCrosshairs(point, xyzpt);
  }
  /**
   * Sends new location to all crosshair trackers.
   *
   * @param point	array of indices specifying the point location
   * @param xyzpt	array of indices specifying the point location in
   *                    mapped coordinates which override point if
   *                    crosshair tracker implements CoordinateMappable
   */
  public void updateCrosshairTrackers(int[] point,
				      XYZDouble xyzpt) {
    getShowPointController().updateCrosshairTrackers(point, xyzpt);
  }
  /**
   * Removes currently selected components from list of crosshair trackers
   * and clears their displayed crosshairs.
   */
  public void deleteCrosshairs() {
    getShowPointController().deleteCrosshairs();
  }
  /**
   * Adds a component to list of images that tracks slice positions
   * with undo/redo.
   *
   * @param tracker slice tracker to add to list
   */
  public void addSliceTracker(SliceNumbering tracker) {
    getShowPointController().addSliceTracker(tracker);
  }
  /**
   * Adds a component to list of images that tracks slice positions
   * with optional undo/redo.
   *
   * @param tracker slice tracker to add to list
   * @param undoFlag	if <code>true</code> undo commands will be generated
   */
  public void addSliceTracker(SliceNumbering tracker, boolean undoFlag) {
    getShowPointController().addSliceTracker(tracker, undoFlag);
  }
  /**
   * Removes a component from list of images that track
   * slice positions with undo/redo.
   *
   * @param tracker slice tracker to remove from list
   */
  public void removeSliceTracker(final SliceNumbering tracker) {
    getShowPointController().removeSliceTracker(tracker);
  }
  /**
   * Removes a component from list of images that track
   * slice positions with undo/redo.
   *
   * @param tracker slice tracker to remove from list
   * @param undoFlag if <code>true</code> undo commands will be generated
   */
  public void removeSliceTracker(SliceNumbering tracker, boolean undoFlag) {
    getShowPointController().removeSliceTracker(tracker, undoFlag);
  }
  /**
   * Queries whether a SliceNumbering is in the slice trackers
   * list.
   *
   * @param tracker SliceNumbering to check list for
   * @return <code>true</code> if tracker is in list otherwise <code>false</code>
   */
  public boolean isSliceTracker(SliceNumbering tracker) {
    return  getShowPointController().isSliceTracker(tracker);
  }
  /**
   * Creates vector of components or subcomponents that are also current
   * slice trackers.
   *
   * @param components array of components to search for slice trackers in
   * @param list vector to add found slice trackers to.  If <code>null</code>
   *             a new vector will be created if any slice trackers are found
   * @return the input list or a new Vector with found slice trackers added to it
   *         or <code>null</code> if no input list was <code>null</code> and no
   *         slice trackers were found.
   */
  public Vector whichAreSliceTrackers(Component[] components, Vector<SliceNumbering> list) {
    return  getShowPointController().whichAreSliceTrackers(components, list);
  }
  /**
   * Adds selected components to list of slice trackers.
   */
  public void addSliceTrackers() {
    getShowPointController().addSliceTrackers();
  }
  /**
   * Adds components to list of slice trackers.
   *
   * @param components array of components to try adding
   */
  public void addSliceTrackers(Component[] components) {
    getShowPointController().addSliceTrackers(components);
  }
  /**
   * Removes selected components from list of slice trackers.
   */
  public void removeSliceTrackers() {
    getShowPointController().removeSliceTrackers();
  }
  /**
   * Removes selected components from list of slice trackers.
   *
   * @param components array of components to try removing
   */
  public void removeSliceTrackers(Component[] components) {
    getShowPointController().removeSliceTrackers(components);
  }
  /**
   * Sends new location to all slice trackers.
   *
   * @param point	array of indices specifying the point location
   * @param xyzpt	array of indices specifying the point location in
   *                    mapped coordinates which override point if
   *                    slice tracker implements CoordinateMappable
   */
  public void updateSliceTrackers(int[] point, XYZDouble xyzpt) {
    getShowPointController().updateSliceTrackers(point, xyzpt);
  }
  /**
   * Stops slice, showpoint and display trackers for given components
   * and returns an object that can be used to restart the trackers.
   */
  public Object stopComponentTrackers(Component[] components) {
    return getShowPointController().stopComponentTrackers(components);
  }
  /**
   * Restarts slice, showpoint and display trackers for given components
   * that where stopped using stopComponentTrackers.
   */
  public void restartComponentTrackers(Object obj) {
    getShowPointController().restartComponentTrackers(obj);
  }
  /**
   * Gets a mapped point if possible or returns default point.
   *
   * @param obj   Object that might implement CoordinateMappable
   * @param xyzpt mapped coordinate to get point for may be <code>null</code>
   * @param defaultPoint point to return if mapping not possible
   */
  static int[] getMappedPoint(Object obj, XYZDouble xyzpt, int[] defaultPoint) {
    return ShowPointController.getMappedPoint(obj, xyzpt, defaultPoint);
  }
  /**
   * Adds selected components to list of images to show point
   * over display for.
   */
  public void addShowPointLinesToDisplay() {
    getShowPointController().addShowPointLinesToDisplay();
  }
  /**
   * Adds selected components to list of images to show point for.
   */
  public void addShowPointLines() {
    getShowPointController().addShowPointLines();
  }
  /**
   * Adds components to list of images to show point for.
   *
   * @param components array of components to try adding
   */
  public void addShowPointLines(Component[] components) {
    getShowPointController().addShowPointLines(components, showPointContainer);
    //    validate();
  }
  /**
   * Adds components to list of images to show point for.
   *
   * @param components array of components to try adding
   */
  public void addShowPointLines(Component[] components,
				Container container) {
    getShowPointController().addShowPointLines(components, container);
  }
  /**
   * Adds a show point display line to the container.
   *
   * @param spdl to add to the container
   */
  public void addShowPointDisplayLine(final ShowPointDisplayLine spdl) {
    if(spdl == null) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	      public void run() { showPointContainer.add(spdl); }
	  } );
    }
    else showPointContainer.add(spdl);
  }
  /**
   * Adds a component to list of images to show point for.
   *
   * @param spi ShowPointImage to try adding a ShowPointDisplayLine for
   * @return ShowPointDisplayLine added or <code>null</code> if
   *	     image <code>null</code> or already in list
   */
  public ShowPointDisplayLine addShowPointLine(ShowPointImage spi) {
    return addShowPointLine(spi, null);
  }
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
					       ShowPointDisplayLine spdl) {
    return getShowPointController().addShowPointLine(spi, spdl,
						     showPointContainer);
  }
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
					       Container container) {
    return getShowPointController().addShowPointLine(spi, spdl, container);
  }
  /**
   * Sets whether the main display selected show point display lines record.
   *
   * @param state if <code>true</code> selected lines will be
   *              set to record.
   */
  public void setMainShowPointDisplayLinesRecordState(boolean state) {
    getShowPointController().setMainShowPointDisplayLinesRecordState(state);
  }
  /**
   * Sets whether selected show point display lines record.
   *
   * @param state if <code>true</code> selected lines will be
   *              set to record.
   */
  public void setShowPointDisplayLinesRecordState(boolean state) {
    getShowPointController().setShowPointDisplayLinesRecordState(state);
  }
  /**
   * Sets whether show point display lines, contained in array
   * of components, record.
   *
   * @param state if <code>true</code> selected lines will be
   *              set to record.
   * @param components array of 
   */
  public void setShowPointDisplayLinesRecordState(boolean state,
						  Component[] components) {
    getShowPointController().setShowPointDisplayLinesRecordState(state,
								 components);
  }
  /**
   * Sets whether a show point display line records.
   *
   * @param state if <code>true</code> selected lines will be
   *              set to record.
   * @param spdl show point display line to set record state for
   */
  public void setShowPointDisplayLineRecordState(boolean state,
						 ShowPointDisplayLine spdl) {
    getShowPointController().setShowPointDisplayLineRecordState(state,
								spdl);
  }
  /**
   * Sets whether the main display selected show point display lines
   * are frozen.
   *
   * @param freeze if <code>true</code> selected lines will be
   *              frozen.
   */
  public void setMainShowPointDisplayLinesFreezeState(boolean freeze) {
    getShowPointController().setMainShowPointDisplayLinesFreezeState(freeze);
  }
  /**
   * Sets whether selected show point display lines are frozen.
   *
   * @param freeze if <code>true</code> selected lines will be
   *              frozen.
   */
  public void setShowPointDisplayLinesFreezeState(boolean freeze) {
    getShowPointController().setShowPointDisplayLinesFreezeState(freeze,
      showPointContainer.getSelectedComponentsOrdered());
  }
  /**
   * Sets whether show point display lines, contained in array
   * of components, are frozen.
   *
   * @param freeze if <code>true</code> selected lines will be
   *              frozen.
   * @param components array of 
   */
  public void setShowPointDisplayLinesFreezeState(boolean freeze,
						  Component[] components) {
    getShowPointController().setShowPointDisplayLinesFreezeState(freeze,
								 components);
  }
  /**
   * Sets whether a show point display line are frozen.
   *
   * @param freeze if <code>true</code> selected lines will be
   *              frozen.
   * @param spdl show point display line to set freeze state for
   */
  public void setShowPointDisplayLineFreezeState(boolean freeze,
						 ShowPointDisplayLine spdl) {
      getShowPointController().setShowPointDisplayLineFreezeState(freeze,
								  spdl);
  }
  /**
   * Deprecated - now does nothing.  Use to update the state of display lines.
   * 
   * @param comp component to update
   * @param state
   * @Deprecated does nothing
   */
  private void updateDisplayLineStates(Component comp, boolean state) {}
  /**
   * Select all ShowPointDisplayLines.
   */
  public void selectAllLines() {
    showPointContainer.selectAll();
  }
  /**
   * Unselect all ShowPointDisplayLines.
   */
  public void clearAllSelections() {
    showPointContainer.clearAllSelections();
  }
  /**
   * Adds a ShowPointDisplayLine to the selected list.
   *
   * @param spdl ShowPointDisplayLine to add
   */
  public void addSelection(final ShowPointDisplayLine spdl) {
      if(! SwingUtilities.isEventDispatchThread()) {
	  SwingUtilities.invokeLater( new Runnable() {
		  public void run() { addSelection(spdl); }
	      } );
      }
      else showPointContainer.addSelection(spdl);
  }
  /**
   * Adds a range of line numbers to the selected list.
   *
   * @param start first show point display line number to add to list
   * @param stop  last show point display line number to add to list
   */
  public int addSelections(final int start, final int stop) {
      if((start > stop) || (stop < 0)) return 0;
      if(! SwingUtilities.isEventDispatchThread()) {
	  RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
		  public void run() {
		      returnObject = new Integer(addSelections(start, stop));
		  }
	      };
	  runWithReturn.invokeAndWait();
	  return ((Integer) runWithReturn.returnObject).intValue();
      }
      else {
	  int cnt = 0;
	  int index = start;
	  if(index < 0) index = 0;
	  for(; (index <= stop) && (index < getLineCount()); index++) {
	      addSelection(getShowPointDisplayLine(index));
	      cnt++;
	  }
	  return cnt;
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
  public void setSelectAdditions(final boolean selectAdditions) {
      if(! SwingUtilities.isEventDispatchThread()) {
	  SwingUtilities.invokeLater( new Runnable() {
		  public void run() { setSelectAdditions(selectAdditions); }
	      } );
      }
      else {
	  showPointContainer.setSelectAdditions(selectAdditions);
	  if(selectAdditions != selectAdditionsCBMI.isSelected())
	      selectAdditionsCBMI.setSelected(selectAdditions);
      }
  }
  /**
   * Gets select additions mode.
   *
   * @return	 If <code>true</code> further
   *             added objects are automatically selected.
   */
  public boolean isSelectAdditions() {
      return showPointContainer.isSelectAdditions();
  }
  /**
   * Gets the number of ShowPointDisplayLines.
   *
   * @return number of ShowPointDisplayLines
   */
  public int getLineCount() {
      return showPointContainer.getComponentCount() - 2;
  }
  /**
   * Gets the ShowPointDisplayLine by its number in the container.
   *
   * @param number relative to the container
   * @return corresponding ShowPointDisplayLine or <code>null</code>
   */
  public ShowPointDisplayLine getShowPointDisplayLine(final int number) {
      synchronized (showPointContainer.getTreeLock()) {
	  if((number + 2) < showPointContainer.getComponentCount())
	      return (ShowPointDisplayLine)
		  showPointContainer.getComponent(number+2);
	  else return null;
      }
  }
  /**
   * Query whether this container contains a ShowPointDisplayLine.
   *
   * @param spdl	ShowPointDisplayLine to check for
   * @return		<code>true</code> if spdl is contained
   */
  public boolean containsShowPointLine(final ShowPointDisplayLine spdl) {
    // don't worry about synchronizing because if called from
    // another thread the state will be unknown by the time
    // returned anyways
    return showPointContainer.contains(spdl);
  }
  /**
   * Removes selected show point lines.
   */
  public void removeShowPointLines() {
      if(! SwingUtilities.isEventDispatchThread()) {
	  SwingUtilities.invokeLater( new Runnable() {
		  public void run() { removeShowPointLines(); }
	      } );
      }
      else showPointContainer.removeSelections();
  }
  /**
   * Removes all show point lines.
   */
  public void removeAllLines() {
      if(! SwingUtilities.isEventDispatchThread()) {
	  SwingUtilities.invokeLater( new Runnable() {
		  public void run() { removeAllLines(); }
	      } );
      }
      else showPointContainer.removeAll();
  }
  /**
   * Shows the point for all currently listed images.
   *
   * @param spi		primary ShowPointImage to display point for
   * @param point	array of indices specifying the point location
   */
  public void trackPoint(final ShowPointImage spi, final int[] point) {
      getShowPointController().trackPoint(spi, point);
  }
  /**
   * Sets the crosshair for all currently listed images
   *
   * @param spi		primary ShowPointImage to set crosshair for
   * @param point	array of indices specifying the point location
   */
  public void setCrosshairs(final ShowPointImage spi, final int[] point) {
    getShowPointController().setCrosshairs(spi, point);
  }
  /**
   * Sets a components crosshair and displays the value in a
   * ShowPointDialogLine while creating an undo/redo history.
   *
   * @param spi		ShowPointImage to set crosshair for.
   *			If <code>null</code> determines from spdl or returns
   * @param spd	        ShowPointDisplay to set values of
   *			If <code>null</code> determines from spi or ignores
   * @param newIndices	Indices to set crosshair to
   * @param newColor    Color to set crosshair to
   * @param undoFlag	<code>true</code> to create undo/redo history
   */
  public void setComponentCrosshair(ShowPointImage spi, ShowPointDisplay spd,
				    int[] newIndices, Color newColor,
				    boolean undoFlag) {
      getShowPointController().setComponentCrosshair(spi, spd,
						     newIndices, newColor,
						     undoFlag);
  }
  /**
   * Sets the showPoint values to crosshair values
   * for all currently listed images.
   */
  public void syncToCrosshairs() {
    getShowPointController().syncToCrosshairs();
  }
  /**
   * Sets the point for an image associated with a ShowPointDisplayLine.
   *
   * @param spdl	ShowPointDisplayLine to set associated image for
   * @param point	array of indices specifying the point location
   */
  public void setPoint(ShowPointDisplayLine spdl, int[] point) {
    getShowPointController().setPoint(spdl, point);
  }
  /**
   * Displays information about a point.
   *
   * @param point	array of indices specifying the point location
   * @param value	value of voxel at point location
   * @param factor	quantification factor for voxel value
   * @param name	name of object source of the voxel
   * @return		<code>true</code> if info displayed,
   *			<code>false</code> otherwise
   * @see	ShowPointDialog
   */
  public boolean showPoint(int[] point, double value, double factor, String name) {
    return showPoint(point, value, factor, name, null, null);
  }
  /**
   * Display information about a point including Talairach coordinates.
   *
   * @param point	array of indices specifying the point location
   * @param value	value of voxel at point location
   * @param factor	quantification factor for voxel value
   * @param name	name of object source of the voxel
   * @param xyzpt	point location in talairach coordinates
   * @param mapName	name of map used convert to talairach coordinates
   * @return		<code>true</code> if info displayed,
   *			<code>false</code> otherwise
   * @see	ShowPointDialog
   * @see	CoordinateMap
   */
  public boolean showPoint(final int[] point,
			   final double value, final double factor, final String name,
			   final XYZDouble xyzpt, final String mapName) {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() {
	    returnObject = new Boolean(showPoint(point, value, factor, name, xyzpt, mapName));
	}
      };
      runWithReturn.invokeAndWait();
      return ((Boolean) runWithReturn.returnObject).booleanValue();
    }
    if(! isVisible()) return false;
    return showPointDisplayLine.showPoint(point, value,
					  factor, name, xyzpt, mapName);
  }
  /**
   * Sets whether show point lines track mapped location or point locations.
   *
   * @param mapTracking	<code>true</code> if lines track mapped location
   */
  public void setMapTracking(final boolean mapTracking) {
    getShowPointController().setMapTracking(mapTracking);
  }
  /**
   * Gets whether show point lines track mapped location or point locations.
   *
   * @return mapTracking	<code>true</code> if lines track mapped location
   */
  public boolean getMapTracking() {
    return getShowPointController().getMapTracking();
  }
  /**
   * Sets whether the crosshair should be visible while showing points.
   *
   * @param show	<code>true</code> to show crosshairs during showpoint
   */
   public void setCrosshairState(final boolean show) {
     getShowPointController().setCrosshairState(show);
   }
  /**
   * Gets the crosshair visible state.
   *
   * @return	<code>true</code> to show crosshairs during showpoint
   */
   public boolean getCrosshairState() {
     return getShowPointController().getCrosshairState();
   }
  /**
   * Sets the crosshair color.
   *
   * @param color crosshair color
   */
   public void setCrosshairColor(Color color) {
     getShowPointController().setCrosshairColor(color);
   }
  /**
   * Gets the crosshair color.
   *
   * @return	crosshair color
   */
   public Color getCrosshairColor() {
     return getShowPointController().getCrosshairColor();
   }
  /**
   * Sets whether the selected ShowPointLines should be frozen.
   *
   * @param freeze	<code>true</code> to freeze selected ShowPointLines
   */
   public void setLinesFreeze(boolean freeze) {
     getShowPointController().setLinesFreeze(freeze);
   }
  /**
   * Sets whether the to record points from the showpoint source.
   *
   * @param record	<code>true</code> to record during showpoint
   */
   public void setRecordState(boolean record) {
     getShowPointController().setRecordState(record);
   }
  /**
   * Gets the source showpoint record state.
   *
   * @return	<code>true</code> if recording during showpoint
   */
   public boolean getRecordState() {
     return getShowPointController().getRecordState();
   }
  /**
   * Sets whether the selected ShowPointLines should record points.
   *
   * @param record	<code>true</code> to record points of selected ShowPointLines
   */
   public void setLinesRecord(boolean record) {
     getShowPointController().setLinesRecord(record);
   }
  /**
   * Sets whether the crosshair should be visible for a related
   * show point image.
   *
   * @param spd 	show point display to set crosshair state for
   * @param show	<code>true</code> to show crosshairs during showpoint
   */
    public void setRelatedCrosshairState(ShowPointDisplay spd,
					 boolean show) {
     getShowPointController().setRelatedCrosshairState(spd, show);
    }
  /**
   * Sets whether the crosshair should be visible for selected ShowPointLines.
   *
   * @param show	<code>true</code> to show crosshairs during showpoint
   */
   public void setLinesCrosshairState(boolean show) {
     getShowPointController().setLinesCrosshairState(show);
   }
  /**
   * Sets whether the related show point images should track
   * the current slice.
   *
   * @param spd 	show point display to set tracking
   *                    for related show point image
   * @param show	<code>true</code> to track slice
   */
    public void setRelatedSliceTrackingState(ShowPointDisplay spd,
					     boolean show) {
      getShowPointController().setRelatedSliceTrackingState(spd, show);
    }
  /**
   * Sets whether the selected ShowPointLines should track the current point.
   *
   * @param track	<code>true</code> to track current point
   */
   public void setLinesTrackingState(boolean track) {
     getShowPointController().setLinesTrackingState(track);
   }
  /**
   * Converts basic settings into a CNU script.
   */
  public String toScript() { return settingsToScript(); }
  /**
   * Converts basic settings into a CNU script.
   *
   * @return iiV script commands to recreate current settings
   */
  public String settingsToScript() {
    String classname = getClass().getName();
    StringBuffer sb=new StringBuffer("// -- start ").append(classname).append(" settings script\n");
    sb.append("showpointdialogtmp = CNUVIEWER.getShowPointDialog();\n");
    sb.append(getShowPointController().settingsToScript());
    sb.append("showpointdialogtmp.setSelectAdditions(").append(isSelectAdditions()).append(");\n");
    sb.append(DisplayNumberFormat.numberFormatToScript(null, getNumberFormat()));
    sb.append("showpointdialogtmp.setNumberFormat(script_rtn);\n");
    sb.append("script_rtn=showpointdialogtmp;\n");
    sb.append("unset(\"showpointdialogtmp\");\n");

    sb.append("// -- end ").append(classname).append(" settings script\n");
    return sb.toString();
  }
  /**
   * Converts show point displays with associated images and their
   * crosshair and slice tracking traits into a CNU script
   * Re-uses any predefined scripted values in scriptedListName.
   *
   * @param scriptedObjects list of objects already scripted
   * @return	script that can recreate the ShowPointDisplayLines
   */
  public String toScript(CNUScriptObjects scriptedObjects) {
    if(! SwingUtilities.isEventDispatchThread()) {
      final CNUScriptObjects fscriptedObjects = scriptedObjects;
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	    public void run() { returnObject = toScript(fscriptedObjects); }
	  };
      runWithReturn.invokeAndWait();
      return (String) runWithReturn.returnObject;
    }
    else {
      String classname = getClass().getName();
      StringBuffer sb = new StringBuffer(256);
      if(scriptedObjects == null) scriptedObjects = new CNUScriptObjects();
      sb.append("// -- start ").append(classname).append(" display lines scripts\n");
      sb.append("showpointdialogtmp = CNUVIEWER.getShowPointDialog();\n");

      int nLines = getLineCount();
      for(int i = 0; i < nLines; i++) {
	ShowPointDisplayLine spdl = getShowPointDisplayLine(i);
	sb.append(spdl.toScript(scriptedObjects));
	sb.append("showpointdialogtmp.addShowPointDisplayLine(script_rtn);\n");
      } // end for(int i = 0; i < nLines; i++)

      sb.append("script_rtn=showpointdialogtmp;\n");
      sb.append("unset(\"showpointdialogtmp\");\n");
      sb.append("// -- end ").append(classname).append(" display lines scripts\n");
      return sb.toString();
    }
  }
  /**
   * Applies the default number format to components.
   *
   * @param nf	number format tool
   * @param comps	components to apply number format to
   * @param ur	undo/redo controller to track changes
   */
  public void applyNumberFormat(final NumberFormat nf,
				final Component comps[],
				final UndoRedo ur) {
    if((comps == null) || (nf == null)) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() { applyNumberFormat(nf, comps, ur); }
	} );
      return;
    }
    if(ur != null) ur.startSteps();
    for(int i = 0; i < comps.length; i++) {
      Component comp = comps[i];
      if(comp instanceof NumberFormattable) {
	NumberFormattable formattable = (NumberFormattable) comp;
	NumberFormat oldFormat = formattable.getNumberFormat();
	NumberFormat newFormat =
	  DisplayNumberFormat.getDefaultNumberFormat();
	  if(newFormat.equals(oldFormat)) break;
	  formattable.setNumberFormat(newFormat);
	  if(ur != null) {
	    Class[] undoParams = new Class[1];
	    undoParams[0] = NumberFormat.class;
	    Object[] undoArgs = new Object[1];
	    undoArgs[0] = oldFormat;
	    DoCommand undo =
	      new DoCommand(comp, "setNumberFormat", undoParams, undoArgs);
	    Object[] redoArgs = new Object[1];
	    redoArgs[0] = newFormat;
	    DoCommand redo =
	      new DoCommand(comp, "setNumberFormat", undoParams, redoArgs);
	    ur.addUndo(undo, redo, comp, "number format");
	  }
      }
    }
    if(ur != null) ur.finishUndoSteps("number format");
  }
  /**
   * Applies the default number format to selected or all show point
   * lines.
   */
  public void applyDefaultNumberFormat() {
    NumberFormat nf = DisplayNumberFormat.getDefaultNumberFormat();
    if(nf != null) {
      Component[] comps = showPointContainer.getSelectedComponentsOrdered();
      if(comps == null) comps = showPointContainer.getComponents();
      applyNumberFormat(nf, comps, getUndoRedo());
    }
  }
  /**
   * Sets the number format for converting numbers to strings.
   *
   * @param numberFormat	number format tool
   */
  public void setNumberFormat(NumberFormat numberFormat) {
    if(numberFormat != null) {
      Component[] comps = showPointContainer.getComponents();
      applyNumberFormat(numberFormat, comps, getUndoRedo());
    }
  }
  /**
   * Gets the number format.
   *
   * @return	number format
   */
  public NumberFormat getNumberFormat() {
    return showPointDisplayLine.getNumberFormat();
  }
  /**
   * A routine to build a script for retrieving a component already
   * scripted and listed in a vector of scripted object names.
   *
   * @param searchForObj object to search for pre-existing script
   * @param scriptedImages list of already scripted objects
   * @param scriptedListName name of script vector which will contain prescripted objects
   * @param scriptUnscripted if <code>true</code> create a script
   *                         for unscripted scriptable objects
   * @return script for retrieving the previously scripted object, or recreating
   *         the object if scriptable and scriptUnscripted set to true, or
   *         <code>null</code>
  public static String buildScriptToPrescriptedObject(Object searchForObj,
						      Vector scriptedImages,
						      String scriptedListName,
						      boolean scriptUnscripted)
      if(scriptedImages != null && scriptedListName != null) {
	  for(int j=0; j < scriptedImages.size(); j++) {
	      Object testObj = scriptedImages.elementAt(j);
	      if(testObj == searchForObj) {
		  return scriptedListName +
		      ".elementAt(" + j + ");\n";
	      }
	      else if((testObj instanceof Container) &&
		      (searchForObj instanceof Component)) {
		  Container container = (Container) testObj;
		  if(container.isAncestorOf((Component) searchForObj)) {
		      return "scriptedListName +
			  ".elementAt(" + j + ");\n" + 
			  subcomponentsScript(container.getComponents(),
					      (Component) searchForObj, "$_");
		  } // end if(container.isAncestorOf ... )
	      }
	  } // end while(e.hasMoreElements())
      }
      if(scriptUnscripted && (searchForObj instanceof iiVScriptable)) {
	  return ((iiVScriptable) searchForObj).toScript(null);
      }
      return null;
  }
   */
  /**
   * A recursive routine to build a script for walking down
   * a container tree retrieving a component object.
   *
   * @param comps 	components in the current level container.
   * @param comp	component to get from tree
   * @param containerName script variable name for current level container
  public static String subcomponentsScript(Component[] comps,
					   Component comp,
					   String containerName) {
    for(int l = 0; l < comps.length; l++) {
      if(comps[l] == comp)
	return containerName + ".getComponent(" + l + ");\n";
      else if(comps[l] instanceof Container) {
	Container container = (Container) comps[l];
	if(container.isAncestorOf(comp))
	  return "containerName +
		 ".getComponent(" + l + ");\n" +
	  	  subcomponentsScript(container.getComponents(), comp, "$_");
      }
    }
    return "null;\n";
  }
   */
}
