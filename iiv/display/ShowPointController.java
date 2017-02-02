package iiv.display;
import iiv.*;
import iiv.dialog.*;
import iiv.util.*;
import iiv.data.*;
import iiv.script.*;
import iiv.gui.*;
import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.lang.*;
import java.net.*;
import java.text.*;
import java.util.*;
/**
 * Dialog to display coordinate values and locations.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUDisplay
 * @since	iiV1.0
 */
public class ShowPointController
    implements ContainerListener, ShowPointControllerInterface,
	       ShowPointDisplay, iiVScriptable
{

  public static final int COORDINATE_MAP = 1;

  public static final int ADD_SHOW_POINT_LINES = 2;
  public static final int FREEZE_SHOW_POINT_LINES = 3;
  public static final int UNFREEZE_SHOW_POINT_LINES = 4;
  public static final int START_SHOW_POINT_LINES_RECORDING = 5;
  public static final int STOP_SHOW_POINT_LINES_RECORDING = 6;

  public static final int ADD_UNRELATED_SHOW_POINT_LINE_TO_DISPLAY = 11;
  public static final int ADD_SHOW_POINT_LINES_TO_DISPLAY = 12;
  public static final int FREEZE_SHOW_POINT_LINES_IN_DISPLAY = 13;
  public static final int UNFREEZE_SHOW_POINT_LINES_IN_DISPLAY = 14;
  public static final int START_SHOW_POINT_LINES_IN_DISPLAY_RECORDING = 15;
  public static final int STOP_SHOW_POINT_LINES_IN_DISPLAY_RECORDING = 16;

  public static final int ADD_CROSSHAIR = 21;
  public static final int FREEZE_CROSSHAIR = 22;
  public static final int DELETE_CROSSHAIR = 23;
  public static final int ADD_SLICETRACKER = 24;
  public static final int DELETE_SLICETRACKER = 25;


  private class LocalAction extends EasyAddAbstractAction {
    private static final long serialVersionUID = -6202281483205981926L;
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
    public LocalAction(String name, String toolTip,
		       boolean defaultState) {
     super(name, toolTip, NO_MNEMONIC, null, defaultState);
     command = 0;
    }
    public void setCommand(int cmd) { command = cmd; }
    public void actionPerformed(ActionEvent ae) {
      getCNUViewer().setWaitCursor();
      try {
	switch(command) {
	case COORDINATE_MAP:
	  getCNUViewer().getCoordinateMapDialog().setVisible(true);
	  break;
	case ADD_SHOW_POINT_LINES:
	  addShowPointLines();
	  break;
	case FREEZE_SHOW_POINT_LINES :
	  setShowPointDisplayLinesFreezeState(true);
	  break;
	case UNFREEZE_SHOW_POINT_LINES:
	  setShowPointDisplayLinesFreezeState(false);
	  break;
	case START_SHOW_POINT_LINES_RECORDING:
	  setShowPointDisplayLinesRecordState(true);
	  break;
	case STOP_SHOW_POINT_LINES_RECORDING:
	  setShowPointDisplayLinesRecordState(false);
	  break;
	case ADD_UNRELATED_SHOW_POINT_LINE_TO_DISPLAY:
	  addShowPointLine(null, null, getCNUViewer().getCNUDisplay());
	  break;
	case ADD_SHOW_POINT_LINES_TO_DISPLAY:
	  addShowPointLinesToDisplay();
	  break;
	case FREEZE_SHOW_POINT_LINES_IN_DISPLAY :
	  setMainShowPointDisplayLinesFreezeState(true);
	  break;
	case UNFREEZE_SHOW_POINT_LINES_IN_DISPLAY:
	  setMainShowPointDisplayLinesFreezeState(false);
	  break;
	case START_SHOW_POINT_LINES_IN_DISPLAY_RECORDING:
	  setMainShowPointDisplayLinesRecordState(true);
	  break;
	case STOP_SHOW_POINT_LINES_IN_DISPLAY_RECORDING:
	  setMainShowPointDisplayLinesRecordState(false);
	  break;
	case ADD_CROSSHAIR:
	  addCrosshairTrackers();
	  break;
	case FREEZE_CROSSHAIR:
	  removeCrosshairTrackers();
	  break;
	case DELETE_CROSSHAIR:
	  deleteCrosshairs();
	  break;
	case ADD_SLICETRACKER:
	  addSliceTrackers();
	  break;
	case DELETE_SLICETRACKER:
	  removeSliceTrackers();
	  break;
        default:
	  break;
        }
      } finally {
        getCNUViewer().setNormalCursor();
      }
    }
  };

  public final EasyAddAbstractAction coordinateMappingAction =
      new LocalAction("Coordinate Map...",
		      "Invokes a browser to set an images coordinate map",
		      COORDINATE_MAP);
  public final EasyAddAbstractAction unfreezeCrosshairAction =
      new LocalAction("Add/Unfreeze Crosshair(s)",
		      "Adds or unfreezes a crosshair over selected image(s)",
		      ADD_CROSSHAIR);
  public final EasyAddAbstractAction freezeCrosshairAction =
      new LocalAction("Freeze Crosshair(s)",
		      "Freezes the crosshair over selected image(s)",
		      FREEZE_CROSSHAIR);
  public final EasyAddAbstractAction deleteCrosshairAction=
      new LocalAction("Delete Crosshair(s)",
		      "Deletes the crosshair from selected image(s)",
		      DELETE_CROSSHAIR);
  public final EasyAddAbstractAction startSliceTrackingAction =
      new LocalAction("Start Slicetracking",
		      "Starts slice tracking in selected image(s)",
		      ADD_SLICETRACKER);
  public final EasyAddAbstractAction stopSliceTrackingAction =
    new LocalAction("Stop Slicetracking",
		    "Stops slice tracking in selected image(s)",
		    DELETE_SLICETRACKER);
  public final EasyAddAbstractAction recordAction =
    new LocalAction("Record Points",
		    "Select to record data from what ever image mouse is pressed over",
		    false);
  public final EasyAddAbstractAction addShowPointLinesAction =
    new LocalAction("Add Show Point Line(s)",
		    "Adds show point lines for each selected show point image",
		    ADD_SHOW_POINT_LINES);
  public final EasyAddAbstractAction freezeShowPointLinesAction =
    new LocalAction("Freeze Line(s)",
		    "Freezes selected show point lines",
		    FREEZE_SHOW_POINT_LINES);
  public final EasyAddAbstractAction unfreezeShowPointLinesAction =
    new LocalAction("Unfreeze Line(s)",
		    "Unfreezes selected show point lines",
		    UNFREEZE_SHOW_POINT_LINES);
  public final EasyAddAbstractAction startRecordingAction =
    new LocalAction("Start Recording Line(s)",
		    "Starts recording data from selected show point lines",
		    START_SHOW_POINT_LINES_RECORDING);
  public final EasyAddAbstractAction stopRecordingAction =
    new LocalAction("Stop Recording Line(s)",
		    "Stops recording data from selected show point lines",
		    STOP_SHOW_POINT_LINES_RECORDING);

  public final EasyAddAbstractAction addUnrelatedShowPointLineAction =
    new LocalAction("Add Unrelated Show Point Line to Display",
		    "Adds a show point line unrelated to any show point image to main display",
		    ADD_UNRELATED_SHOW_POINT_LINE_TO_DISPLAY);
  public final EasyAddAbstractAction addShowPointLinesToDisplayAction =
    new LocalAction("Add Show Point Line(s) to Display",
		    "Adds show point lines for each selected show point image to main display",
		    ADD_SHOW_POINT_LINES_TO_DISPLAY);
  public final EasyAddAbstractAction freezeDisplayShowPointLinesAction =
    new LocalAction("Freeze Line(s) in Display",
		    "Freezes selected show point lines in main display",
		    FREEZE_SHOW_POINT_LINES_IN_DISPLAY);
  public final EasyAddAbstractAction unfreezeDisplayShowPointLinesAction =
    new LocalAction("Unfreeze Line(s) in Display",
		    "Unfreezes selected show point lines in main display",
		    UNFREEZE_SHOW_POINT_LINES_IN_DISPLAY);
  public final EasyAddAbstractAction startRecordingDisplayShowPointLinesAction =
    new LocalAction("Start Recording Line(s) in Display",
		    "Starts recording data from selected show point lines in main display",
		    START_SHOW_POINT_LINES_IN_DISPLAY_RECORDING);
  public final EasyAddAbstractAction stopRecordingDisplayShowPointLinesAction =
    new LocalAction("Stop Recording Line(s) in Display",
		    "Stops recording data from selected show point lines in main display",
		    STOP_SHOW_POINT_LINES_IN_DISPLAY_RECORDING);


  public final EasyAddAbstractAction mapTrackingAction =
    new LocalAction("Map Tracking",
		    "Select to enable tracking via map locations", false);
  public final EasyAddAbstractAction crosshairAction =
    new LocalAction("Show Tracking Crosshair",
		    "Select to show tracking crosshair over images", true);

  public final DefaultComboBoxModel crosshairColorBoxModel =
    new DefaultComboBoxModel(CNUDialog.colorNames);

  private Object localSetLock = new Object();
  private UndoRedo undoRedo = null;
  private StatusWindowShowPointDisplay statusWindowShowPointDisplay = null;

  private Vector<ShowPointImage> crosshairTrackers = new Vector<ShowPointImage>();
  private Vector<SliceNumbering> sliceTrackers = new Vector<SliceNumbering>();

    //  private Vector showPointPairs = new Vector();
  private Vector<ShowPointImage> showPointImages = new Vector<ShowPointImage>();
  private Vector<ShowPointDisplay> showPointDisplays = new Vector<ShowPointDisplay>();
  private CNUViewer cnuv = null;

  /**
   * Constructs a new instance of ShowPointController.
   *
   * @param cnuv	CNUViewer to work with
   */
  public ShowPointController(CNUViewer cnuv) {
    this.cnuv = cnuv;

    CNUDisplay cnud = cnuv.getCNUDisplay();
    // listen for removal of ShowPointimages or ShowPointDisplays
    if(cnud != null) {
      cnud.addContainerListener(this);
      cnud.addBackgroundShowPointDisplay(this);
    }

    addShowPointPair(this, null, false);
    setCrosshairColor(Color.red);

  }
  /**
   * Gets the CNUViewer.
   *
   * @return	The CNUViewer either set or found from as parent.
   *		May return <code>null</code>.
   */
  public CNUViewer getCNUViewer() { return cnuv; }

  private CNUContainer showPointContainer = null;
  /**
   * Sets the ShowPointContainer.
   *
   * @param container	The container.
   */
  public void setShowPointContainer(CNUContainer container) {
      this.showPointContainer = container;
  }
  /**
   * Gets the ShowPointContainer.
   *
   * @return	The container.
   *		May return <code>null</code>.
   */
  public CNUContainer getShowPointContainer() { return showPointContainer; }
  /**
   * Gets the UndoRedo handler for this display.
   *
   * @return UndoRedo handler for this display
   */
  public UndoRedo getUndoRedo() {
    if(undoRedo == null) {
      CNUViewer cnuv = getCNUViewer();
      synchronized(localSetLock) {
	if(undoRedo == null) {
	  if(cnuv != null) undoRedo = cnuv.getUndoRedo();
	  else undoRedo = new UndoRedo(null, null, true);
	  if(! undoRedo.getEventThreadForced())
	    throw new RuntimeException("Invalid UndoRedo object for ShowPointDialog");
	}
      }
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
    if(! ur.getEventThreadForced())
      throw new RuntimeException(
	"SetUndoRedo called with invalid UndoRedo object for ShowPointController");
    synchronized(localSetLock) { undoRedo = ur; }
  }
  /**
   * Gets the StatusWindowShowPointDisplay for use with this dialog.
   *
   * @return the StatusWindowShowPointDisplay
   */
  public StatusWindowShowPointDisplay getStatusWindowShowPointDisplay() {
    if(statusWindowShowPointDisplay == null) {
      if(! SwingUtilities.isEventDispatchThread()) {
	 RunnableWithReturnObject runnable = new  RunnableWithReturnObject() {
	  public void run() { getStatusWindowShowPointDisplay(); }
	};
	runnable.invokeAndWait();
      }
      else statusWindowShowPointDisplay =
	new StatusWindowShowPointDisplay(getCNUViewer());
    }
    return statusWindowShowPointDisplay;
  }
  /**
   * Update display line visible features if the associated component
   * is added to a container.  Part of ContainerListener.
   * Should only be called from the event dispatch thread.
   *
   * @param evt	container event
   */
  public void componentAdded(ContainerEvent evt) {
      // updateDisplayLineStates(evt.getChild(), true);
  }
  /**
   * Use to update tracking features if the associated component
   * is removed from a container.  Part of ContainerListener.
   * Should only be called from the event dispatch thread.
   *
   * @param evt	container event
   */
   public void componentRemoved(ContainerEvent evt) {
       Component comp = evt.getChild();
       if(comp instanceof ShowPointDisplay)
	   removeShowPointPair((ShowPointDisplay) comp);
       if(comp instanceof ShowPointImage)
	   removeCrosshairTracker((ShowPointImage) comp);
       if(comp instanceof SliceNumbering)
	   removeSliceTracker((SliceNumbering) comp);
       if(comp instanceof Container) {
	   Component[] comps = ((Container) comp).getComponents();
	   if(comps != null) {
	       getUndoRedo().startSteps();
	       removeCrosshairTrackers(comps);
	       removeSliceTrackers(comps);
	       removeShowPointPairs(comps);
	       getUndoRedo().finishUndoSteps("remove trackers");
	   }
       }
   }
  /**
   * Adds a show point display and show point image as a pair
   * that shows points and creates undo/redo history for the add.
   *
   * @param spd	ShowPointDisplay to try adding.
   * @param spi	image to associate with spd
   */
  public void addShowPointPair(ShowPointDisplay spd,
			       ShowPointImage spi) {
      addShowPointPair(spd, spi, true);
  }
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
			       final boolean undoFlag) {
      if(spd == null) return;
      if(! SwingUtilities.isEventDispatchThread()) {
	  SwingUtilities.invokeLater( new Runnable() {
		  public void run() { addShowPointPair(spd, spi, undoFlag); }
	      } );
      }
      else if(! showPointDisplays.contains(spd)){
	  showPointDisplays.addElement(spd);
	  showPointImages.addElement(spi);
	  if(undoFlag) {
	      Class[] undoParams = { ShowPointDisplay.class,
				     Boolean.TYPE };
	      Object[] undoArgs = { (Object) spd,
				    new Boolean(false) };
	      DoCommand undo = new DoCommand(this, "removeShowPointPair",
					     undoParams, undoArgs);
	      Class[] redoParams = { ShowPointDisplay.class,
				     ShowPointImage.class, Boolean.TYPE };
	      Object[] redoArgs = { (Object) spd, (Object) spi,
				    new Boolean(false) };
	      DoCommand redo = new DoCommand(this, "addShowPointPair",
					     redoParams, redoArgs);
	      getUndoRedo().addUndo(undo, redo, "add show point pair");
	  }
      }
  }
  /**
   * Adds a show point display and show point image as a pair
   * from a hashtable listing pairs with possible undo/redo.
   *
   * @param pairs	hashtable of display and image pairs
   */
  public void addShowPointPairs(final Hashtable pairs) {
    if(pairs == null) return;
    if(! SwingUtilities.isEventDispatchThread()) {
	SwingUtilities.invokeLater( new Runnable() {
	   public void run() { addShowPointPairs(pairs); }
	    } );
    }
    else {
	UndoRedo ur = getUndoRedo();
	ur.startSteps();
	Enumeration e = pairs.keys();
	while(e.hasMoreElements()) {
	    ShowPointDisplay spd = (ShowPointDisplay) e.nextElement();
	    ShowPointImage spi = (ShowPointImage) pairs.get(spd);
	    addShowPointPair(spd, spi);
	}
	ur.finishUndoSteps("add show point pairs");
    }
  }
  /**
   * Removes a show point display and show point image pair
   * from list that shows points with possible undo/redo.
   *
   * @param spd	ShowPointDisplay to try removing.
   */
  public void removeShowPointPair(ShowPointDisplay spd) {
      removeShowPointPair(spd, true);
  }
  /**
   * Removes a show point display and show point image pair
   * from list that shows points with possible undo/redo.
   *
   * @param spd	ShowPointDisplay to try removing.
   * @param undoFlag if <code>true</code> creates undo/redo history
   */
  public void removeShowPointPair(final ShowPointDisplay spd,
				  final boolean undoFlag) {
      if(spd == null) return;
      if(! SwingUtilities.isEventDispatchThread()) {
	  SwingUtilities.invokeLater( new Runnable() {
		  public void run() { removeShowPointPair(spd, undoFlag); }
	      } );
      }
      else if(showPointDisplays.contains(spd)){
	  int index = showPointDisplays.indexOf(spd);
	  if(index >= 0) {
	      ShowPointImage spi = showPointImages.elementAt(index);
	      showPointDisplays.removeElementAt(index);
	      showPointImages.removeElementAt(index);
	      if(undoFlag) {
		  Class[] undoParams = { ShowPointDisplay.class,
					 ShowPointImage.class,
					 Boolean.TYPE };
		  Object[] undoArgs = { spd, spi, new Boolean(false) };
		  DoCommand undo = new DoCommand(this, "addShowPointPair",
						 undoParams, undoArgs);
		  Class[] redoParams = { ShowPointDisplay.class,
					 Boolean.TYPE };
		  Object[] redoArgs = { spd, new Boolean(false) };
		  DoCommand redo = new DoCommand(this, "removeShowPointPair",
						 redoParams, redoArgs);
		  getUndoRedo().addUndo(undo, redo, "remove show point pair");
	      }
	  }
      }
  }
  /**
   * Removes an array of components from list of show point pairs.
   *
   * @param components array of components to try removing
   */
  public void removeShowPointPairs(final Component[] components) {
      if(components == null) return;
      if(! SwingUtilities.isEventDispatchThread()) {
	  SwingUtilities.invokeLater( new Runnable() {
		  public void run() { removeShowPointPairs(components); }
	      } );
      }
      else {
	  UndoRedo ur = getUndoRedo();
	  ur.startSteps();
	  for(int i=0; i<components.length; i++) {
	      if(components[i] instanceof ShowPointDisplay)
		  removeShowPointPair((ShowPointDisplay) components[i]);
	      else if(components[i] instanceof Container)
		  removeShowPointPairs(((Container) components[i]).getComponents());
	  }
	  ur.finishUndoSteps("remove showpoint pairs");
      }
  }
  /**
   * Gets the show point image related to a show point display.
   *
   * @param spd show point display to get related show point image for
   * @return related show point image or <code>null</code> if none found
   */
  public ShowPointImage getRelatedShowPointImage(final ShowPointDisplay spd) {
    if(spd == null) return null;
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	    public void run() {
	      returnObject = getRelatedShowPointImage(spd);
	    }
	  };
      runWithReturn.invokeAndWait();
      return (ShowPointImage) runWithReturn.returnObject;
    }
    else {
      int index = showPointDisplays.indexOf(spd);
      if(index >= 0) return showPointImages.elementAt(index);
      return null;
    }
  }
  /**
   * Sends location to all show point pairs.
   *
   * @param point	array of indices specifying the point location
   * @param xyzpt	array of indices specifying the point location in
   *                    mapped coordinates which override point if
   *                    image implements CoordinateMappable
   */
  public void showPointViaPairs(final int[] point,
				final XYZDouble xyzpt,
				final ShowPointImage src_spi) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	    public void run() { showPointViaPairs(point, xyzpt, src_spi); }
	  } );
    }
    else {
      Enumeration e = showPointDisplays.elements();
      while(e.hasMoreElements()) {
	ShowPointDisplay spd = (ShowPointDisplay) e.nextElement();
	ShowPointImage spi = getRelatedShowPointImage(spd);
	if(spi != null)
	  spi.showPoint(getMappedPoint(spi, xyzpt, point), spd);
	else if(src_spi != null) src_spi.showPoint(point, spd);
	else spd.showPoint(point, 0, 0, "no source");
      }
    }
  }
  /**
   * Queries whether a ShowPointDisplay is in the show point display
   * list.
   *
   * @param spd ShowPointDisplay to check list for
   * @return <code>true</code> if spd is in the show point display list.
   */
  public boolean isShowPointDisplay(ShowPointDisplay spd) {
    return showPointDisplays.contains(spd);
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
  public Hashtable<ShowPointDisplay,ShowPointImage> whichAreShowPointDisplays(final Component[] components,
					     final Hashtable<ShowPointDisplay,ShowPointImage> list) {
    if(components == null) return list;
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	    public void run() {
	      returnObject = whichAreShowPointDisplays(components, list);
	    }
	  };
      runWithReturn.invokeAndWait();
      return (Hashtable<ShowPointDisplay,ShowPointImage>) runWithReturn.returnObject;
    }
    else {
      Hashtable<ShowPointDisplay,ShowPointImage> returnList = list;
      for(int i=0; i<components.length; i++) {
	if(components[i] instanceof ShowPointDisplay) {
	  ShowPointDisplay spd = (ShowPointDisplay) components[i];
	  if(isShowPointDisplay(spd)) {
	    if(returnList == null) returnList = new Hashtable<ShowPointDisplay,ShowPointImage>();
	    returnList.put(spd, getRelatedShowPointImage(spd));
	  }
	}
	if(components[i] instanceof Container) {
	  returnList =
	    whichAreShowPointDisplays(((Container) components[i]).getComponents(),
				      returnList);
	}
      }
      return returnList;
    }
  }
  /**
   * Adds a component to list of images that listens for crosshair positions
   * with undo/redo.
   * @param spi ShowPointImage to add to list
   */
  public void addCrosshairTracker(ShowPointImage spi) {
    addCrosshairTracker(spi, true);
  }
  /**
   * Adds a component to list of images that listens for crosshair positions
   * with optional undo/redo.
   *
   * @param spi ShowPointImage to add to list
   * @param undoFlag	if <code>true</code> undo commands will be generated
   */
  public void addCrosshairTracker(final ShowPointImage spi,
				  final boolean undoFlag) {
      if(spi == null) return;
      if(! SwingUtilities.isEventDispatchThread()) {
	  SwingUtilities.invokeLater( new Runnable() {
		  public void run() { addCrosshairTracker(spi, undoFlag); }
	      } );
      }
      else if(! crosshairTrackers.contains(spi)) {
	  crosshairTrackers.addElement(spi);
	  /*
	  int[] indices = spi.getCrosshairIndices();
	  if(indices == null) {
	    setComponentCrosshair(spi, null,
			getMappedPoint(spi, showPointDisplayLine.getXYZPoint(),
				       showPointDisplayLine.getPoint()), 
				  getCrosshairColor(), undoFlag);
	  }
	  */
	  if(undoFlag) {
	      Class[] undoParams = { ShowPointImage.class, Boolean.TYPE };
	      Object[] undoArgs = { (Object) spi, new Boolean(false) };
	      DoCommand undo = new DoCommand(this, "removeCrosshairTracker",
					     undoParams, undoArgs);
	      DoCommand redo = new DoCommand(this, "addCrosshairTracker",
					     undoParams, undoArgs);
	      getUndoRedo().addUndo(undo, redo, "add crosshair tracker");
	  }
      }
  }
  /**
   * Removes a component from list of images that listens for
   * crosshair positions with undo/redo.
   *
   * @param spi ShowPointImage to remove from list
   */
  public void removeCrosshairTracker(final ShowPointImage spi) {
    removeCrosshairTracker(spi, true);
  }
  /**
   * Removes a component from list of images that listens for
   * crosshair positions with optional undo/redo.
   *
   * @param spi ShowPointImage to remove from list
   * @param undoFlag	if <code>true</code> undo commands will be generated
   */
  public void removeCrosshairTracker(final ShowPointImage spi, final boolean undoFlag) {
      if(spi == null) return;
      if(! SwingUtilities.isEventDispatchThread()) {
	  SwingUtilities.invokeLater( new Runnable() {
		  public void run() { removeCrosshairTracker(spi, undoFlag); }
	      } );
      }
      else if(crosshairTrackers.contains(spi)) {
	  crosshairTrackers.removeElement(spi);
	  if(undoFlag) {
	      Class[] undoParams = { ShowPointImage.class, Boolean.TYPE };
	      Object[] undoArgs = { (Object) spi, new Boolean(false) };
	      DoCommand undo = new DoCommand(this, "addCrosshairTracker",
					     undoParams, undoArgs);
	      DoCommand redo = new DoCommand(this, "removeCrosshairTracker",
					     undoParams, undoArgs);
	      getUndoRedo().addUndo(undo, redo, "remove crosshair tracker");
	  }
      }
  }
  /**
   * Queries whether a ShowPointImage is in the crosshair trackers
   * list.
   *
   * @param spi ShowPointImage to check list for
   * @return <code>true</code> if spi is in list otherwise <code>false</code>
   */
  public boolean isCrosshairTracker(ShowPointImage spi) {
    return crosshairTrackers.contains(spi);
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
  public Vector<ShowPointImage> whichAreCrosshairTrackers(final Component[] components, final Vector<ShowPointImage> list) {
      if(components == null) return list;
      if(! SwingUtilities.isEventDispatchThread()) {
	  RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
		  public void run() {
		      returnObject = whichAreCrosshairTrackers(components, list);
		  }
	      };
	  runWithReturn.invokeAndWait();
	  return (Vector<ShowPointImage>) runWithReturn.returnObject;
      }
      else {
	  Vector<ShowPointImage> returnList = list;
	  for(int i=0; i<components.length; i++) {
	      if(components[i] instanceof ShowPointImage) {
		  if(isCrosshairTracker((ShowPointImage) components[i])) {
		      if(returnList == null) returnList = new Vector<ShowPointImage>();
		      returnList.addElement((ShowPointImage) components[i]);
		  }
	      }
	      if(components[i] instanceof Container) {
		  returnList =
		      whichAreCrosshairTrackers(((Container) components[i]).getComponents(),
						returnList);
	      }
	  }
	  return returnList;
      }
  }
  /**
   * Adds currently selected components to list of crosshair trackers.
   */
  public void addCrosshairTrackers() {
    addCrosshairTrackers(getCNUViewer().getCNUDisplay().getSelectedComponentsOrdered());
  }
  /**
   * Adds an array of objects to list of crosshair trackers.
   *
   * @param objects array of possible ShowPointImages to try adding
   */
  public void addCrosshairTrackers(final Object[] objects) {
    if(objects == null) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() { addCrosshairTrackers(objects); }
	} );
    }
    else {
      UndoRedo ur = getUndoRedo();
      ur.startSteps();
      for(int i=0; i<objects.length; i++) {
	if(objects[i] instanceof ShowPointImage)
	  addCrosshairTracker((ShowPointImage) objects[i]);
	else if(objects[i] instanceof Container)
	  addCrosshairTrackers(((Container) objects[i]).getComponents());
      }
      ur.finishUndoSteps("add crosshair trackers");
    }
  }
  /**
   * Removes currently selected components from list of crosshair trackers.
   */
  public void removeCrosshairTrackers() {
    removeCrosshairTrackers(getCNUViewer().getCNUDisplay().getSelectedComponentsOrdered());
  }
  /**
   * Removes an array of components from list of crosshair trackers.
   *
   * @param components array of components to try removing
   */
  public void removeCrosshairTrackers(final Component[] components) {
    if(components == null) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() { removeCrosshairTrackers(components); }
	} );
    }
    else {
      UndoRedo ur = getUndoRedo();
      ur.startSteps();
      for(int i=0; i<components.length; i++) {
	if(components[i] instanceof ShowPointImage)
	  removeCrosshairTracker((ShowPointImage) components[i]);
	else if(components[i] instanceof Container)
	  removeCrosshairTrackers(((Container) components[i]).getComponents());
      }
      ur.finishUndoSteps("remove crosshair trackers");
    }
  }
  /**
   * Sends tracking location to all crosshair trackers.
   *
   * @param point	array of indices specifying the point location
   * @param xyzpt	array of indices specifying the point location in
   *                    mapped coordinates which override point if
   *                    crosshair tracker implements CoordinateMappable
   */
  public void drawTrackingCrosshairs(final int[] point,
				     final XYZDouble xyzpt) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() { drawTrackingCrosshairs(point, xyzpt); }
	} );
    }
    else {
      Enumeration e = crosshairTrackers.elements();
      while(e.hasMoreElements()) {
	ShowPointImage spi = (ShowPointImage) e.nextElement();
	spi.drawTrackingCrosshair(getMappedPoint(spi, xyzpt, point),
				  getCrosshairColor());
      }
    }
  }
  /**
   * Sends new location to all crosshair trackers.
   *
   * @param point	array of indices specifying the point location
   * @param xyzpt	array of indices specifying the point location in
   *                    mapped coordinates which override point if
   *                    crosshair tracker implements CoordinateMappable
   */
  public void updateCrosshairTrackers(final int[] point,
				      final XYZDouble xyzpt) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() { updateCrosshairTrackers(point, xyzpt); }
	} );
    }
    else {
      UndoRedo ur = getUndoRedo();
      ur.startSteps();
      Enumeration e = crosshairTrackers.elements();
      while(e.hasMoreElements()) {
	ShowPointImage spi = (ShowPointImage) e.nextElement();
	setComponentCrosshair(spi, null,
			      getMappedPoint(spi, xyzpt, point),
			      getCrosshairColor(), true);
      }
      ur.finishUndoSteps("update crosshairs");
    }
  }
  /**
   * Removes currently selected components from list of crosshair trackers
   * and clears their displayed crosshairs.
   */
  public void deleteCrosshairs() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() { deleteCrosshairs(); }
	} );
    }
    getUndoRedo().startSteps();
    removeCrosshairTrackers();
    getCNUViewer().getCNUDisplay().apply(CNUDisplay.CLEAR_CROSSHAIR);
    getUndoRedo().finishUndoSteps("delete crosshairs");
  }
  /**
   * Adds a component to list of images that tracks slice positions
   * with undo/redo.
   *
   * @param tracker slice tracker to add to list
   */
  public void addSliceTracker(final SliceNumbering tracker) {
    addSliceTracker(tracker, true);
  }
  /**
   * Adds a component to list of images that tracks slice positions
   * with optional undo/redo.
   *
   * @param tracker slice tracker to add to list
   * @param undoFlag	if <code>true</code> undo commands will be generated
   */
  public void addSliceTracker(final SliceNumbering tracker,
			      final boolean undoFlag) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() { addSliceTracker(tracker, undoFlag); }
	} );
    }
    else if(! sliceTrackers.contains(tracker)) {
      sliceTrackers.addElement(tracker);
      if(undoFlag) {
	Class[] undoParams = { SliceNumbering.class, Boolean.TYPE };
	Object[] undoArgs = { tracker, new Boolean(false) };
	DoCommand undo = new DoCommand(this, "removeSliceTracker",
				       undoParams, undoArgs);
	DoCommand redo = new DoCommand(this, "addSliceTracker",
				       undoParams, undoArgs);
	getUndoRedo().addUndo(undo, redo, "add slice tracker");
      }
    }
  }
  /**
   * Removes a component from list of images that track
   * slice positions with undo/redo.
   *
   * @param tracker slice tracker to remove from list
   */
  public void removeSliceTracker(final SliceNumbering tracker) {
    removeSliceTracker(tracker, true);
  }
  /**
   * Removes a component from list of images that track
   * slice positions with undo/redo.
   *
   * @param tracker slice tracker to remove from list
   * @param undoFlag if <code>true</code> undo commands will be generated
   */
  public void removeSliceTracker(final SliceNumbering tracker,
				 final boolean undoFlag) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() { removeSliceTracker(tracker, undoFlag); }
	} );
    }
    else if(sliceTrackers.contains(tracker)) {
      sliceTrackers.removeElement(tracker);
      Class[] undoParams = { SliceNumbering.class, Boolean.TYPE };
      Object[] undoArgs = { tracker, new Boolean(false) };
      DoCommand undo = new DoCommand(this, "addSliceTracker",
				     undoParams, undoArgs);
      DoCommand redo = new DoCommand(this, "removeSliceTracker",
				     undoParams, undoArgs);
      getUndoRedo().addUndo(undo, redo, "remove slice tracker");
    }
  }
  /**
   * Queries whether a SliceNumbering is in the slice trackers
   * list.
   *
   * @param tracker SliceNumbering to check list for
   * @return <code>true</code> if tracker is in list otherwise <code>false</code>
   */
  public boolean isSliceTracker(SliceNumbering tracker) {
    return sliceTrackers.contains(tracker);
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
  public Vector<SliceNumbering> whichAreSliceTrackers(final Component[] components,
				      final Vector<SliceNumbering> list) {
    if(components == null) return list;
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	  public void run() {
	    returnObject = whichAreSliceTrackers(components, list);
	  }
	};
      runWithReturn.invokeAndWait();
      return (Vector<SliceNumbering>) runWithReturn.returnObject;
    }
    else {
      Vector<SliceNumbering> returnList = list;
      for(int i=0; i<components.length; i++) {
	if(components[i] instanceof SliceNumbering) {
	  if(isSliceTracker((SliceNumbering) components[i])) {
	    if(returnList == null) returnList = new Vector<SliceNumbering>();
	    returnList.addElement((SliceNumbering) components[i]);
	  }
	}
	if(components[i] instanceof Container) {
	  returnList =
	    whichAreSliceTrackers(((Container) components[i]).getComponents(),
				  returnList);
	}
      }
      return returnList;
    }
  }
  /**
   * Adds selected components to list of slice trackers.
   */
  public void addSliceTrackers() {
    addSliceTrackers(getCNUViewer().getCNUDisplay().getSelectedComponentsOrdered());
  }
  /**
   * Adds components to list of slice trackers.
   *
   * @param components array of components to try adding
   */
  public void addSliceTrackers(final Component[] components) {
    if(components == null) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() { addSliceTrackers(components); }
	} );
    }
    else {
      UndoRedo ur = getUndoRedo();
      ur.startSteps();
      for(int i=0; i<components.length; i++) {
	if(components[i] instanceof SliceNumbering)
	  addSliceTracker((SliceNumbering) components[i]);
	else if(components[i] instanceof Container)
	  addSliceTrackers(((Container) components[i]).getComponents());
      }
      ur.finishUndoSteps("add slice trackers");
    }
  }
  /**
   * Removes selected components from list of slice trackers.
   */
  public void removeSliceTrackers() {
    removeSliceTrackers(getCNUViewer().getCNUDisplay().getSelectedComponentsOrdered());
  }
  /**
   * Removes selected components from list of slice trackers.
   *
   * @param components array of components to try removing
   */
  public void removeSliceTrackers(final Component[] components) {
    if(components == null) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() { removeSliceTrackers(components); }
	} );
    }
    else {
      UndoRedo ur = getUndoRedo();
      ur.startSteps();
      for(int i=0; i<components.length; i++) {
	if(components[i] instanceof SliceNumbering)
	  removeSliceTracker((SliceNumbering) components[i]);
	else if(components[i] instanceof Container)
	  removeSliceTrackers(((Container) components[i]).getComponents());
      }
      ur.finishUndoSteps("remove slice trackers");
    }
  }
  /**
   * Sends new location to all slice trackers.
   *
   * @param point	array of indices specifying the point location
   * @param xyzpt	array of indices specifying the point location in
   *                    mapped coordinates which override point if
   *                    slice tracker implements CoordinateMappable
   */
  public void updateSliceTrackers(final int[] point, final XYZDouble xyzpt) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() { updateSliceTrackers(point, xyzpt); }
	} );
    }
    else {
      UndoRedo ur = getUndoRedo();
      ur.startSteps();
      Enumeration e = sliceTrackers.elements();
      while(e.hasMoreElements()) {
	SliceNumbering sliceNumbering = (SliceNumbering) e.nextElement();
	int slice =
	  sliceNumbering.getSlice(getMappedPoint(sliceNumbering,
						 xyzpt, point));
	if(slice >= 0) getCNUViewer().getCNUDisplay().setSlice(sliceNumbering, slice);
      }
      ur.finishUndoSteps("update slices");
    }
  }
  /**
   * Stops slice, showpoint and display trackers for given components
   * and returns an object that can be used to restart the trackers.
   */
  public Object stopComponentTrackers(Component[] components) {
    Hashtable showPointDisplaysTracked =
	whichAreShowPointDisplays(components, null);
    Vector crosshairTracked = whichAreCrosshairTrackers(components, null);
    Vector sliceTracked = whichAreSliceTrackers(components, null);
    removeCrosshairTrackers(components);
    removeSliceTrackers(components);
    removeShowPointPairs(components);
    return
      new Object[]{showPointDisplaysTracked, crosshairTracked, sliceTracked};
  }
  /**
   * Restarts slice, showpoint and display trackers for given components
   * that where stopped using stopComponentTrackers.
   */
  public void restartComponentTrackers(Object obj) {
    Hashtable showPointDisplaysTracked = (Hashtable) ((Object[]) obj)[0];
    Vector crosshairTracked = (Vector) ((Object[]) obj)[1];
    Vector sliceTracked = (Vector) ((Object[]) obj)[2];
    if(sliceTracked != null) {
      Component[] sliceList = new Component[sliceTracked.size()];
      sliceTracked.copyInto(sliceList);
      addSliceTrackers(sliceList);
    }
    if(crosshairTracked != null) {
      Component[] crosshairList = new Component[crosshairTracked.size()];
      crosshairTracked.copyInto(crosshairList);
      addCrosshairTrackers(crosshairList);
    }
    if(showPointDisplaysTracked != null)
      addShowPointPairs(showPointDisplaysTracked);
  }
  /**
   * Gets a mapped point if possible or returns default point.
   *
   * @param obj   Object that might implement CoordinateMappable
   * @param xyzpt mapped coordinate to get point for may be <code>null</code>
   * @param defaultPoint point to return if mapping not possible
   */
  public static int[] getMappedPoint(Object obj, XYZDouble xyzpt,
				     int[] defaultPoint) {
    if((xyzpt != null) && (obj instanceof CoordinateMappable)) {
      CoordinateMappable mappable = (CoordinateMappable) obj;
      CoordinateMap coorM = mappable.getCoordinateMap();
      if(coorM != null) {
	XYZDouble res = mappable.getCoordinateResolutions();
	if(res != null) {
	  XYZDouble xyzpt2 = coorM.fromSpace(xyzpt, res);
	  int[] point = new int[3];
	  point[0] = (int) Math.round(xyzpt2.x);
	  point[1] = (int) Math.round(xyzpt2.y);
	  point[2] = (int) Math.round(xyzpt2.z);
	  return point;
	}
      }
    }
    return defaultPoint;
  }
  /**
   * Adds selected components to list of images to show point
   * over display for.
   */
  public void addShowPointLinesToDisplay() {
    addShowPointLines(getCNUViewer().getCNUDisplay().getSelectedComponentsOrdered(),
		      getCNUViewer().getCNUDisplay());
  }
  /**
   * Adds selected components to list of images to show point for.
   */
  public void addShowPointLines() {
    addShowPointLines(getCNUViewer().getCNUDisplay().getSelectedComponentsOrdered(),
		      getShowPointContainer());
  }
  /**
   * Adds components to list of images to show point for.
   *
   * @param components array of components to try adding
   */
  public void addShowPointLines(Component[] components,
				Container container) {
    if(components == null) return;
    getUndoRedo().startSteps();    
    for(int i=0; i<components.length; i++) {
      Component comp = components[i];
      if(comp instanceof ShowPointImage)
	addShowPointLine((ShowPointImage) comp, null, container);
      else if(comp instanceof Container)
	addShowPointLines(((Container) comp).getComponents(), container);
    }
    getUndoRedo().finishUndoSteps("add showpoint lines");    
  }
  /**
   * Adds a show point image to list of images to show point for.
   *
   * @param spi show point image to try adding a ShowPointDisplayLine for
   * @return ShowPointDisplayLine added or <code>null</code> if
   *	     image <code>null</code> or already in list
   */
  public ShowPointDisplayLine addShowPointLine(ShowPointImage spi) {
    return addShowPointLine(spi, null, getShowPointContainer());
  }
  /**
   * Adds a component to list of images to show point for.
   *
   * @param spi		image to try adding a ShowPointDisplayLine for
   * @param spdl	ShowPointDisplayLine to try adding.  If <code>null</code>
   *			a new line will be created.
   * @return ShowPointDisplayLine added or <code>null</code> if
   *	     image <code>null</code> or already in list
   */
  public ShowPointDisplayLine addShowPointLine(final ShowPointImage spi,
					       ShowPointDisplayLine spdl,
					       final Container container) {
    if(container == null) return null;
    if(! SwingUtilities.isEventDispatchThread()) {
      final ShowPointDisplayLine fspdl = spdl;
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	  public void run() { returnObject = addShowPointLine(spi, fspdl, container); }
	};
      runWithReturn.invokeAndWait();
      return (ShowPointDisplayLine) runWithReturn.returnObject;
    }
    else {
      if(spdl == null) {
	spdl = new ShowPointDisplayLine();
	//	spdl.setForeground(Color.blue);
	spdl.setRecordShowPointDisplay(getStatusWindowShowPointDisplay());
      }

      UndoRedo ur = getUndoRedo();
      ur.startSteps();

      container.add(spdl);

      addShowPointPair(spdl, spi);
      if(spi != null) {
	// for compatibility with old scripts
	int[] indices = spi.getCrosshairIndices();
	if(indices != null) {
	  setComponentCrosshair(spi, spdl, indices, null, true);
	}
      }
      ur.finishUndoSteps("add showpoint line");
    }
    return spdl;
  }
  /**
   * Sets whether the main display selected show point display lines record.
   *
   * @param state if <code>true</code> selected lines will be
   *              set to record.
   */
  public void setMainShowPointDisplayLinesRecordState(boolean state) {
    setShowPointDisplayLinesRecordState(state,
	getCNUViewer().getCNUDisplay().getSelectedComponentsOrdered());
  }
  /**
   * Sets whether selected show point display lines record.
   *
   * @param state if <code>true</code> selected lines will be
   *              set to record.
   */
  public void setShowPointDisplayLinesRecordState(boolean state) {
    CNUContainer showPointContainer = getShowPointContainer();
    if(showPointContainer != null)
	setShowPointDisplayLinesRecordState(state,
	  showPointContainer.getSelectedComponentsOrdered());
  }
  /**
   * Sets whether show point display lines, contained in array
   * of components, record.
   *
   * @param state if <code>true</code> selected lines will be
   *              set to record.
   * @param components array of 
   */
  public void setShowPointDisplayLinesRecordState(final boolean state,
						  final Component[] components) {
    if(components == null) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() {
	    setShowPointDisplayLinesRecordState(state, components);
	  }
	} );
    }
    else {
      getUndoRedo().startSteps();
      for(int i=0; i<components.length; i++) {
	Component comp = components[i];
	if(comp instanceof ShowPointDisplayLine)
	  setShowPointDisplayLineRecordState(state, (ShowPointDisplayLine) comp);
	else if(comp instanceof Container)
	  setShowPointDisplayLinesRecordState(state,
					      ((Container) comp).getComponents());
      }
      getUndoRedo().finishUndoSteps(state ? "start lines record" :
				    "stop lines record");    
    }
  }
  /**
   * Sets whether a show point display line records.
   *
   * @param state if <code>true</code> selected lines will be
   *              set to record.
   * @param spdl show point display line to set record state for
   */
  public void setShowPointDisplayLineRecordState(final boolean state,
						 final ShowPointDisplayLine spdl) {
    if(spdl == null) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() {
	    setShowPointDisplayLineRecordState(state, spdl);
	  }
	} );
    }
    else {
      if(state) spdl.record(); //record currently displayed point
      if(spdl.getRecordState() != state) {
	spdl.setRecordState(state);
	Class[] undoParams = { Boolean.TYPE };
	Object[] undoArgs = { new Boolean(! state) };
	DoCommand undo = new DoCommand(spdl, "setRecordState",
				       undoParams, undoArgs);
	Object[] redoArgs = { new Boolean(state) };
	DoCommand redo = new DoCommand(spdl, "setRecordState",
				       undoParams, redoArgs);
	getUndoRedo().addUndo(undo, redo, state ? "start record" :
			      "stop record");
      }
    }
  }
  /**
   * Sets whether the main display selected show point display lines
   * are frozen.
   *
   * @param freeze if <code>true</code> selected lines will be
   *              frozen.
   */
  public void setMainShowPointDisplayLinesFreezeState(boolean freeze) {
    setShowPointDisplayLinesFreezeState(freeze,
	getCNUViewer().getCNUDisplay().getSelectedComponentsOrdered());
  }
  /**
   * Sets whether selected show point display lines are frozen.
   *
   * @param freeze if <code>true</code> selected lines will be
   *              frozen.
   */
  public void setShowPointDisplayLinesFreezeState(boolean freeze) {
    CNUContainer showPointContainer = getShowPointContainer();
    if(showPointContainer != null) 
      setShowPointDisplayLinesFreezeState(freeze,
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
  public void setShowPointDisplayLinesFreezeState(final boolean freeze,
						  final Component[] components) {
    if(components == null) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() {
	    setShowPointDisplayLinesFreezeState(freeze, components);
	  }
	} );
    }
    else {
      getUndoRedo().startSteps();
      for(int i=0; i<components.length; i++) {
	Component comp = components[i];
	if(comp instanceof ShowPointDisplayLine)
	  setShowPointDisplayLineFreezeState(freeze,
					     (ShowPointDisplayLine) comp);
	else if(comp instanceof Container)
	  setShowPointDisplayLinesFreezeState(freeze,
					      ((Container) comp).getComponents());
      }
      getUndoRedo().finishUndoSteps(freeze ? "freeze lines" :
				    "unfreeze lines");    
    }
  }
  /**
   * Sets whether a show point display line are frozen.
   *
   * @param freeze if <code>true</code> selected lines will be
   *              frozen.
   * @param spdl show point display line to set freeze state for
   */
  public void setShowPointDisplayLineFreezeState(final boolean freeze,
						 final ShowPointDisplayLine spdl) {
    if(spdl == null) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() {
	    setShowPointDisplayLineFreezeState(freeze, spdl);
	  }
	} );
    }
    else {
      if(spdl.getFreezeState() != freeze) {
	spdl.setFreezeState(freeze);
	Class[] undoParams = { Boolean.TYPE };
	Object[] undoArgs = { new Boolean(! freeze) };
	DoCommand undo = new DoCommand(spdl, "setFreezeState",
				       undoParams, undoArgs);
	Object[] redoArgs = { new Boolean(freeze) };
	DoCommand redo = new DoCommand(spdl, "setFreezeState",
				       undoParams, redoArgs);
	getUndoRedo().addUndo(undo, redo, freeze ? "freeze line" :
			      "unfreeze line");
      }
    }
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
  public boolean showPoint(int[] point,
			   double value, double factor, String name,
			   XYZDouble xyzpt, String mapName) {
    ShowPointDisplay rspd =
      getRecordState() ? getStatusWindowShowPointDisplay() : null;
    if(rspd != null) return rspd.showPoint(point, value,
					   factor, name, xyzpt, mapName);
    else return false;
  }
  /**
   * Shows the point for all currently listed images.
   *
   * @param spi		primary ShowPointImage to display point for
   * @param point	array of indices specifying the point location
   */
  public void trackPoint(final ShowPointImage spi, final int[] point) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() { trackPoint(spi, point); }
	} );
    }
    else {
      XYZDouble xyzpt = null;
      if(spi != null) {
	if(getCrosshairState())
	  spi.drawTrackingCrosshair(point, getCrosshairColor());
	if(getMapTracking() && (spi instanceof CoordinateMappable)) {
	  CoordinateMap coorM =
	    ((CoordinateMappable) spi).getCoordinateMap();
	  if(coorM != null) {
	    XYZDouble res =
	      ((CoordinateMappable) spi).getCoordinateResolutions();
	    xyzpt = coorM.toSpace(new XYZDouble(point, 0), res);
	  }
	}
      }
      showPointViaPairs(point, xyzpt, spi);
      if(getCrosshairState()) drawTrackingCrosshairs(point, xyzpt);
    }
  }
  /**
   * Sets the crosshair for all currently listed images
   *
   * @param spi		primary ShowPointImage to set crosshair for
   * @param point	array of indices specifying the point location
   */
  public void setCrosshairs(ShowPointImage spi, int[] point) {
    XYZDouble xyzpt = null;
    if(spi != null) {
      if(getMapTracking() && (spi instanceof CoordinateMappable)) {
	CoordinateMap coorM =
	  ((CoordinateMappable) spi).getCoordinateMap();
	if(coorM != null) {
	  XYZDouble res =
	    ((CoordinateMappable) spi).getCoordinateResolutions();
	  xyzpt = coorM.toSpace(new XYZDouble(point, 0), res);
	}
      }
    }
    setCrosshairs(spi, point, xyzpt);
  }
  /**
   * Sets the crosshair for all currently listed images
   *
   * @param spi		primary ShowPointImage to set crosshair for
   * @param point	array of indices specifying the point location
   * @param xyzpt       point location in
   *                    mapped coordinates which override point if
   *                    slice tracker implements CoordinateMappable
   */
  public void setCrosshairs(final ShowPointImage spi,
			    final int[] point,
			    final XYZDouble xyzpt) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() { setCrosshairs(spi, point, xyzpt); }
	} );
    }
    else {
      UndoRedo ur = getUndoRedo();
      ur.startSteps();
      updateSliceTrackers(point, xyzpt);
      updateCrosshairTrackers(point, xyzpt);
      showPointViaPairs(point, xyzpt, spi);
      ur.finishUndoSteps("update point");
    }
  }
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
				    final ShowPointDisplay spd,
				    int[] newIndices, Color newColor,
				    final boolean undoFlag) {
    if(! SwingUtilities.isEventDispatchThread()) {
      final ShowPointImage fspi = spi;
      final int[] fnewIndices = newIndices;
      final Color fnewColor = newColor;
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() { setComponentCrosshair(fspi, spd,
						    fnewIndices,
						    fnewColor,
						    undoFlag); }
	} );
    }
    else {
      if((spi == null) && (spd != null))
	spi = getRelatedShowPointImage(spd);
      if(spi != null) {
	// update the show point display stuff doesn't require undo/redo
	if(spd != null) spi.showPoint(newIndices, spd);
	      
	int[] oldIndices = spi.getCrosshairIndices();
	Color oldColor = spi.getCrosshairColor();
	if(newColor == null) newColor = oldColor;
	if(newColor == null) newColor = getCrosshairColor();
	// don't set stuff if we don't have to - prevents excess undo/redo
	if(CNUDisplay.arraysEqual(newIndices, oldIndices) &&
	   newColor.equals(oldColor)) return;
	spi.setCrosshair(newIndices, newColor);
	if(undoFlag) {
	  // don't undo if setting did nothing
	  newColor = spi.getCrosshairColor();
	  if( (newColor == null) ? oldColor == null :
	      newColor.equals(oldColor) ) {
	    newIndices = spi.getCrosshairIndices();
	    if( (newIndices == null) ? oldIndices == null
		: CNUDisplay.arraysEqual(newIndices, oldIndices))
	      return; // both settings did nothing
	  }

	  Boolean undoFlagArg = new Boolean(false);
	  Class[] undoParams = { ShowPointImage.class, ShowPointDisplay.class,
				 int[].class, Color.class, Boolean.TYPE };
	  Object[] undoArgs = { spi, spd, oldIndices, oldColor, undoFlagArg };
	  DoCommand undo = new DoCommand(this, "setComponentCrosshair",
					 undoParams, undoArgs);
	  Object[] redoArgs = { spi, spd, newIndices, newColor, undoFlagArg };
	  DoCommand redo = new DoCommand(this, "setComponentCrosshair",
					 undoParams, redoArgs);
	  getUndoRedo().addUndo(undo, redo, "set crosshair");
	}
      }
    }
  }
  /**
   * Sets the showPoint values to crosshair values
   * for all currently listed images.
   */
  public void syncToCrosshairs() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() { syncToCrosshairs(); }
	} );
    }
    else {
      Enumeration e = showPointDisplays.elements();
      while(e.hasMoreElements()) {
	ShowPointDisplay spd = (ShowPointDisplay) e.nextElement();
	if(spd instanceof ShowPointDisplayLine) {
	  ShowPointDisplayLine spdl = (ShowPointDisplayLine) spd;
	  if(! spdl.getFreezeState()) {
	    ShowPointImage spi = getRelatedShowPointImage(spdl);
	    if(spi != null) {
	      int[] point = spi.getCrosshairIndices();
	      if(point != null) spi.showPoint(point, spdl);
	    }
	  }
	}
      }
    }
  }
  /**
   * Sets the point for an image associated with a ShowPointDisplayLine.
   *
   * @param spdl	ShowPointDisplayLine to set associated image for
   * @param point	array of indices specifying the point location
   */
  public void setPoint(final ShowPointDisplayLine spdl, final int[] point) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() { setPoint(spdl, point); }
	} );
    }
    else {
      ShowPointImage spi = getRelatedShowPointImage(spdl);
      if((spi instanceof SliceNumbering) &&
	 isSliceTracker((SliceNumbering) spi)) {
	SliceNumbering sliceNumbering = (SliceNumbering) spi;
	int slice = sliceNumbering.getSlice(point);
	if(slice >= 0)
	  getCNUViewer().getCNUDisplay().setSlice(sliceNumbering, slice);
      }
    }
  }
  /**
   * Sets whether show point lines track mapped location or point locations.
   *
   * @param mapTracking	<code>true</code> if lines track mapped location
   */
  public void setMapTracking(final boolean mapTracking) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() { setMapTracking(mapTracking); }
	} );
    }
    else mapTrackingAction.getCheckboxButtonModel().setSelected(mapTracking);
  }
  /**
   * Gets whether show point lines track mapped location or point locations.
   *
   * @return mapTracking	<code>true</code> if lines track mapped location
   */
  public boolean getMapTracking() {
    return mapTrackingAction.getCheckboxButtonModel().isSelected();
  }
  /**
   * Gets selection action that chooses whether show point lines
   * track mapped location or point locations.
   *
   * @return map tracking action
   */
  public EasyAddAbstractAction getMapTrackingAction() {
    return mapTrackingAction;
  }
  /**
   * Sets whether the crosshair should be visible while showing points.
   *
   * @param show	<code>true</code> to show crosshairs during showpoint
   */
   public void setCrosshairState(final boolean show) {
     if(! SwingUtilities.isEventDispatchThread()) {
       SwingUtilities.invokeLater( new Runnable() {
	   public void run() { setCrosshairState(show); }
	 } );
     }
     else crosshairAction.getCheckboxButtonModel().setSelected(show);
   }
  /**
   * Gets the crosshair visible state.
   *
   * @return	<code>true</code> to show crosshairs during showpoint
   */
   public boolean getCrosshairState() {
     return crosshairAction.getCheckboxButtonModel().isSelected();
   }
  /**
   * Sets the crosshair color.
   *
   * @param color crosshair color
   */
   public void setCrosshairColor(final Color color) {
     if(! SwingUtilities.isEventDispatchThread()) {
       SwingUtilities.invokeLater( new Runnable() {
	   public void run() { setCrosshairColor(color); }
	 } );
     }
     else {
       String colorName = CNUDialog.colorToString(color);
       if(crosshairColorBoxModel.getIndexOf(colorName) < 0)
	 crosshairColorBoxModel.addElement(colorName);
       crosshairColorBoxModel.setSelectedItem(colorName);
     }
   }
  /**
   * Gets the crosshair color.
   *
   * @return	crosshair color
   */
  public Color getCrosshairColor() {
    return CNUDialog.stringToColor((String) crosshairColorBoxModel.getSelectedItem());
  }
  /**
   * Sets whether the selected ShowPointLines should be frozen.
   *
   * @param freeze	<code>true</code> to freeze selected ShowPointLines
   */
  public void setLinesFreeze(final boolean freeze) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() { setLinesFreeze(freeze); }
	} );
    }
    else {
      CNUContainer showPointContainer = getShowPointContainer();
      if(showPointContainer != null) {
	Component[] components =
	  showPointContainer.getSelectedComponentsOrdered();
	int start = 0;
	if(components == null) {
	  components = showPointContainer.getComponents();
	  start = 2;
	}
	if(components != null) {
	  for(int i=start; i<components.length; i++) {
	    ShowPointDisplayLine spdl = (ShowPointDisplayLine) components[i];
	    spdl.setFreezeState(freeze);
	  }
	}
      }
    }
  }
  /**
   * Sets whether to record points from the showpoint source.
   *
   * @param record	<code>true</code> to record during showpoint
   */
  public void setRecordState(final boolean record) {
    ButtonModel bm = recordAction.getCheckboxButtonModel();
    if(bm.isSelected() != record) bm.setSelected(record);
  }
  /**
   * Gets the source showpoint record state.
   *
   * @return	<code>true</code> if recording during showpoint
   */
  public boolean getRecordState() {
    //       return showPointDisplayLine.getRecordState();
    return recordAction.getCheckboxButtonModel().isSelected();
  }
  /**
   * Sets whether the selected ShowPointLines should record points.
   *
   * @param record	<code>true</code> to record points of selected ShowPointLines
   */
  public void setLinesRecord(final boolean record) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() { setLinesRecord(record); }
	} );
    }
    else {
      CNUContainer showPointContainer = getShowPointContainer();
      if(showPointContainer != null) {
	Component[] components = showPointContainer.getSelectedComponentsOrdered();
	int start = 0;
	if(components == null) {
	  components = showPointContainer.getComponents();
	  start = 2;
	}
	if(components != null) {
	  for(int i=start; i<components.length; i++) {
	    ShowPointDisplayLine spdl = (ShowPointDisplayLine) components[i];
	    spdl.setRecordState(record);
	  }
	}
      }
    }
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
    ShowPointImage spi = getRelatedShowPointImage(spd);
    if(spi != null) {
      if(show) addCrosshairTracker(spi);
      else removeCrosshairTracker(spi);
    }
  }
  /**
   * Sets whether the crosshair should be visible for selected ShowPointLines.
   *
   * @param show	<code>true</code> to show crosshairs during showpoint
   */
  public void setLinesCrosshairState(final boolean show) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() { setLinesCrosshairState(show); }
	} );
    }
    else {
      CNUContainer showPointContainer = getShowPointContainer();
      if(showPointContainer != null) {
	Component[] components = showPointContainer.getSelectedComponentsOrdered();
	int start = 0;
	if(components == null) {
	  components = showPointContainer.getComponents();
	  start = 2;
	}
	if(components != null) {
	  getUndoRedo().startSteps();
	  for(int i=start; i<components.length; i++) {
	    ShowPointDisplayLine spdl = (ShowPointDisplayLine) components[i];
	    setRelatedCrosshairState(spdl, show);
	  }
	  getUndoRedo().finishUndoSteps(show ? "add crosshair trackers" :
					"remove crosshair trackers");
	}
      }
    }
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
    ShowPointImage spi = getRelatedShowPointImage(spd);
    if(spi instanceof SliceNumbering) {
      if(show) addSliceTracker((SliceNumbering) spi);
      else removeSliceTracker((SliceNumbering) spi);
    }
  }
  /**
   * Sets whether the selected ShowPointLines should track the current point.
   *
   * @param track	<code>true</code> to track current point
   */
  public void setLinesTrackingState(final boolean track) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() { setLinesTrackingState(track); }
	} );
    }
    else {
      CNUContainer showPointContainer = getShowPointContainer();
      if(showPointContainer != null) {
	Component[] components = showPointContainer.getSelectedComponentsOrdered();
	int start = 0;
	if(components == null) {
	  components = showPointContainer.getComponents();
	  start = 2;
	}
	if(components != null) {
	  getUndoRedo().startSteps();
	  for(int i=start; i<components.length; i++) {
	    ShowPointDisplayLine spdl =
	      (ShowPointDisplayLine) components[i];
	    setRelatedSliceTrackingState(spdl, track);
	  }
	  getUndoRedo().finishUndoSteps(track ? "add slice trackers" :
					"remove slice trackers");
	}
      }
    }
  }
  /**
   * Converts basic settings into a CNU script.
   *
   * @return script for resetting basic settings
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
    sb.append("showpointcontrollertmp  = CNUVIEWER.getShowPointController();\n");
    sb.append("showpointcontrollertmp.setMapTracking(").append(getMapTracking()).append(");\n");
    sb.append("showpointcontrollertmp.setCrosshairColor(");
    sb.append("newColorObject(\"").append(CNUDialog.colorToString(getCrosshairColor())).append("\")");
    sb.append(");\n");
    sb.append("showpointcontrollertmp.setCrosshairState(").append(getCrosshairState()).append(");\n");
    sb.append("showpointcontrollertmp.setRecordState(").append(getRecordState()).append(");\n");
    sb.append("unset(\"showpointcontrollertmp\");\n");

    sb.append("// -- end ").append(classname).append(" settings script\n");
    return sb.toString();
  }
  /**
   * Converts show point displays with associated images and their
   * crosshair and slice tracking traits into a CNU script
   * Re-uses any predefined scripted values in scriptedListName.
   *
   * @param scriptedObjects list of objects already scripted
   * @return	script that can recreate the ShowPointLines
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
      sb.append("// -- start ").append(classname).append(" script\n");
      sb.append("showpointcontrollertmp = CNUVIEWER.getShowPointController();\n");
      CNUContainer showPointContainer = getShowPointContainer();
      
      sb.append("// -- start ").append(classname).append(" display pairs list\n");
      Enumeration e = showPointDisplays.elements();
      while(e.hasMoreElements()) {
	ShowPointDisplay spd = (ShowPointDisplay) e.nextElement();
	if(spd == this) continue; // don't script this
	// determine the ShowPointDisplay variable
	String spdVariable = scriptedObjects.get(spd);
	if(spdVariable != null) {
	  // determine the ShowPointImage variable
	  String spiSubstitution="null";
	  ShowPointImage spi = getRelatedShowPointImage(spd);
	  if(spi != null) {
	    String spiVariable = scriptedObjects.get(spi);
	    if((spiVariable == null) && (spi instanceof iiVScriptable)) {
	      sb.append(((iiVScriptable) spi).toScript(scriptedObjects));
	      spiVariable = scriptedObjects.get(spi);
	    }
	    if(spiVariable != null) spiSubstitution = spiVariable;
	  }
	  sb.append("showpointcontrollertmp.addShowPointPair(");
	  sb.append(spdVariable).append(", ").append(spiSubstitution);
	  sb.append(");\n");
	}
      } // end while(e.hasMoreElements())
      sb.append("// -- end ").append(classname).append(" display pairs list\n");

      sb.append("// -- start ").append(classname).append(" crosshair tracker list\n");
      e = crosshairTrackers.elements();
      while(e.hasMoreElements()) {
	ShowPointImage spi = (ShowPointImage) e.nextElement();
	String spiVariable = scriptedObjects.get(spi);
	if(spiVariable != null) {
	  sb.append("showpointcontrollertmp.addCrosshairTracker(");
	  sb.append(spiVariable).append(");\n");
	}
      }
      sb.append("// -- end ").append(classname).append(" crosshair tracker list\n");

      sb.append("// -- start ").append(classname).append(" slice tracker list\n");
      e = sliceTrackers.elements();
      while(e.hasMoreElements()) {
	SliceNumbering sliceNumbering = (SliceNumbering) e.nextElement();
	String spiVariable = scriptedObjects.get(sliceNumbering);
	if(spiVariable != null) {
	  sb.append("showpointcontrollertmp.addSliceTracker(");
	  sb.append(spiVariable).append(");\n");
	}
      }
      sb.append("// -- end ").append(classname).append(" slice tracker list\n");
      
      sb.append("unset(\"showpointcontrollertmp\");\n");
      sb.append("// -- end ").append(classname).append(" script\n");
      return sb.toString();
    }
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
   *   public static String buildScriptToPrescriptedObject(Object searchForObj,
   *					      Vector scriptedImages,
						      String scriptedListName,
						      boolean scriptUnscripted)

						      {
    if(scriptedImages != null && scriptedListName != null) {
       for(int j=0; j < scriptedImages.size(); j++) {
	      Object testObj = scriptedImages.elementAt(j);
	      if(testObj == searchForObj) {
		  return "objectMethod ${" + scriptedListName +
		      "} elementAt " + j + ";\n";
	      }
	      else if((testObj instanceof Container) &&
		      (searchForObj instanceof Component)) {
		  Container container = (Container) testObj;
		  if(container.isAncestorOf((Component) searchForObj)) {
		      return "objectMethod ${" + scriptedListName +
			  "} elementAt " + j + ";\n" + 
			  subcomponentsScript(container.getComponents(),
					      (Component) searchForObj, "status");
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
	return "objectMethod ${" + containerName +
		"} getComponent " + l + ";\n";
      else if(comps[l] instanceof Container) {
	Container container = (Container) comps[l];
	if(container.isAncestorOf(comp))
	  return "objectMethod ${" + containerName +
		 "} getComponent " + l + ";\n" +
	  	  subcomponentsScript(container.getComponents(), comp, "status");
      }
    }
    return "echo null;\n";
  }
   */
}
