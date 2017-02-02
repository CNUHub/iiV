package iiv.dialog;
import iiv.display.*;
import iiv.*;
import iiv.gui.*;
import iiv.util.*;
import iiv.data.*;
import java.awt.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.lang.*;

/**
 * Dialog to get crop and other image limits from user.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUDisplay
 * @see		DisplayComponent
 * @since	iiV1.1
 */
public class GotoPointDialog extends CNUDialog
implements ShowPointDisplay, CoordinateMappable, NumberFormattable {
  private static final long serialVersionUID = 6949990702215206146L;
  private JToolBar topToolBar = null;
  private BoundedRangeModel xLocationModel = DisplayComponentDefaults.getXGotoLocationModel();
//    new AutoExtendBoundedRangeModel(0, 0, 0, 127);
  private BoundedRangeModel yLocationModel = DisplayComponentDefaults.getYGotoLocationModel();
//    new AutoExtendBoundedRangeModel(0, 0, 0, 127);
  private BoundedRangeModel zLocationModel = DisplayComponentDefaults.getZGotoLocationModel();
//    new AutoExtendBoundedRangeModel(0, 0, 0, 127);
  private JLabel talMapLabel = new JLabel("none");
  private JLabel xTalLabel = new JLabel("n/a");
  private JLabel yTalLabel = new JLabel("n/a");
  private JLabel zTalLabel = new JLabel("n/a");
  private CoordinateMap coorMap = LinearCoordinateMap.getDefaultCoordinateMap();
  private XYZDouble coorRes = null;
  private NumberFormat numberFormat =
    DisplayNumberFormat.getDefaultNumberFormat();

  public final int DISMISS = 0;
  public final int APPLY_TO_TRACKERS = 1;
  public final int APPLY_TO_SELECTED = 2;
  public final int SET_TO_CURRENT = 3;
  public final int TRACK_MOUSE = 4;
  public final int AUTO_UPDATE_TRACKERS = 5;
  public final int AUTO_UPDATE_SELECTED = 6;
  public final int SET_NUMBER_FORMAT_TO_DEFAULT = 7;
  public final int SET_COORDINATE_MAP_TO_DEFAULT = 8;
  private LocalAction trackMouseCheckboxAction =
    new LocalAction("Track mouse",
		    "Select to update goto location when the mouse is drepressed over an image",
		    false, TRACK_MOUSE);
  private LocalAction autoUpdateTrackersCheckboxAction =
    new LocalAction("Auto Update Trackers",
		    "Select to automatically update trackers when slider is released",
		    false, AUTO_UPDATE_TRACKERS);

  private LocalAction autoUpdateSelectedCheckboxAction =
    new LocalAction("Auto Update Selected",
		    "Select to automatically update selected when slider is released",
		    false, AUTO_UPDATE_SELECTED);
  private LocalAction applyDefaultCoordinateMapAction =
    new LocalAction("Set Coordinate Map",
		    "Set the coordinate map for displaying mapped points to the current default",
		    false, SET_COORDINATE_MAP_TO_DEFAULT);
  private LocalAction applyDefaultNumberFormatAction =
    new LocalAction("Set Number Format",
		    "Set the number format for displaying mapped points to the current default",
		    false, SET_NUMBER_FORMAT_TO_DEFAULT);
  private LocalAction applyToTrackersAction =
    new LocalAction("Apply to Trackers",
		    "Apply location to currently registered trackers",
		    false, APPLY_TO_TRACKERS);
  private LocalAction applyToSelectedAction =
    new LocalAction("Apply to Selected",
		    "Apply location to currently selected objects",
		    false, APPLY_TO_SELECTED);
  private LocalAction currentAction =
    new LocalAction("Set to Current",
		    "Set location and coordinate mapfile to that of currently selected object",
		    false, SET_TO_CURRENT);
  private LocalAction dismissAction =
    new LocalAction("Dismiss", "Hides this window",
		    false, DISMISS);
  /**
   * Constructs a new instance of GotoPointDialog.
   *
   * @param parentFrame	parent frame needed for building a window
   */
  public GotoPointDialog(Frame parentFrame) { this(parentFrame, null); }
  /**
   * Shows the mapped point.
   * Should only be called from event processing thread.
   *
   */
  private void updateMappedPoint() {
    int[] point = getDisplayedIndices();
    CoordinateMap coorM = getCoordinateMap();
    XYZDouble coorRes = getCoordinateResolutions();
    if(coorM != null && coorRes != null) {
      XYZDouble xyzpt = coorM.toSpace(new XYZDouble(point, 0), coorRes);
      xTalLabel.setText(numberFormat.format(xyzpt.x*1e3));
      yTalLabel.setText(numberFormat.format(xyzpt.y*1e3));
      zTalLabel.setText(numberFormat.format(xyzpt.z*1e3)+ " mm");
    }
    else {
      xTalLabel.setText("n/a");
      yTalLabel.setText("n/a");
      zTalLabel.setText("n/a");
    }
  }
  /**
   * Constructs a new instance of GotoPointDialog.
   *
   * @param parentFrame	parent frame
   * @param cnuv	primary display controller
   */
  public GotoPointDialog(Frame parentFrame, CNUViewer cnuv ) {
    super(parentFrame, "Goto Point", false, cnuv);

    Container contentPane = getContentPane();
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    contentPane.add(Box.createVerticalStrut(5));

    JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
    topToolBar = toolbar;
    toolbar.setFloatable(false);
    contentPane.add(toolbar);
    toolbar.add(Box.createHorizontalStrut(5));
    trackMouseCheckboxAction.addCheckboxTo(toolbar);
    autoUpdateTrackersCheckboxAction.addCheckboxTo(toolbar);
    autoUpdateSelectedCheckboxAction.addCheckboxTo(toolbar);

    toolbar.add(Box.createHorizontalGlue());
    toolbar.add(Box.createHorizontalStrut(5));

    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolbar.setFloatable(false);
    contentPane.add(toolbar);
    toolbar.add(Box.createHorizontalStrut(5));

    ChangeListener cl = new ChangeListener() {
	public void stateChanged(ChangeEvent e) {
	  BoundedRangeModel brm = (BoundedRangeModel) e.getSource();
	  int[] point = getDisplayedIndices();
	  if(brm.getValueIsAdjusting()) trackPoint(point);
	  else {
	    trackPoint(null);
	    if(getAutoUpdateTrackersState()) applyToTrackers();
	    if(getAutoUpdateSelectedState()) applyToSelected();
	  }
	  updateMappedPoint();
	}
      };
    toolbar.add(new TextAndSlider("x Location:  ", 4, xLocationModel));
    toolbar.add(Box.createHorizontalGlue());
    toolbar.add(new TextAndSlider("y Location:  ", 4, yLocationModel));
    toolbar.add(Box.createHorizontalGlue());
    toolbar.add(new TextAndSlider("z Location:  ", 4, zLocationModel));
    xLocationModel.addChangeListener(cl);
    yLocationModel.addChangeListener(cl);
    zLocationModel.addChangeListener(cl);

    toolbar.add(Box.createHorizontalStrut(5));

    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolbar.setFloatable(false);
    contentPane.add(toolbar);
    toolbar.add(Box.createHorizontalStrut(5));
    toolbar.add(new JLabel("Mapping File: "));
    toolbar.add(Box.createHorizontalStrut(5));
    toolbar.add(talMapLabel);
    toolbar.add(Box.createHorizontalGlue());
    toolbar.add(Box.createHorizontalStrut(5));

    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolbar.setFloatable(false);
    contentPane.add(toolbar);
    toolbar.add(Box.createHorizontalStrut(5));

    toolbar.add(xTalLabel);
    toolbar.add(Box.createHorizontalGlue());
    toolbar.add(yTalLabel);
    toolbar.add(Box.createHorizontalGlue());
    toolbar.add(zTalLabel);
    toolbar.add(Box.createHorizontalStrut(5));

    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolbar.setFloatable(false);
    contentPane.add(toolbar);
    toolbar.add(Box.createHorizontalStrut(5));
    currentAction.addTo(toolbar);
    applyDefaultCoordinateMapAction.addTo(toolbar);
    applyDefaultNumberFormatAction.addTo(toolbar);
    toolbar.add(Box.createHorizontalGlue());
    toolbar.add(Box.createHorizontalStrut(5));

    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolbar.setFloatable(false);
    contentPane.add(toolbar);
    toolbar.add(Box.createHorizontalStrut(5));

    applyToTrackersAction.addTo(toolbar);
    applyToSelectedAction.addTo(toolbar);
    toolbar.add(Box.createHorizontalGlue());
    dismissAction.addTo(toolbar);
    toolbar.add(Box.createHorizontalStrut(5));


    contentPane.add(Box.createVerticalStrut(5));
    needsCNUViewerInit();
    pack();
  }
  /**
   * Sets the CNUViewer.
   *
   * @param cnuviewer the new CNUViewer for this dialog to use
   *		Should not be <code>null</code>.
   */
  public void setCNUViewer(CNUViewer cnuviewer) {
    super.setCNUViewer(cnuviewer);
    needsCNUViewerInit();
    pack();
  }
  private boolean needsCNUViewerInitDone = false;
  /**
   * Initializes stuff that needs a CNUViewer to initialize.
   *
   */
  private void needsCNUViewerInit() {
    if(! needsCNUViewerInitDone) {
      CNUViewer cnuv = getCNUViewer();
      if(cnuv != null) {
	needsCNUViewerInitDone = true;
	ShowPointControllerInterface spc = cnuv.getShowPointController();
	if(spc != null) {
	  EasyAddAbstractAction eaaa = spc.getMapTrackingAction();
	  eaaa.addCheckboxTo(topToolBar);
	}
      }
    }
  }
  /*
   * Retrieve the currently displayed indices
   *
   * @return current displayed indices
   */
  public int[] getDisplayedIndices() {
      return DisplayComponentDefaults.getGotoLocationIndices();
/*    return new int[] {
      xLocationModel.getValue(),
      yLocationModel.getValue(),
      zLocationModel.getValue()
    };
*/
  }
  /*
   * Set the currently displayed indices
   *
   * @param  new indices
   */
  public void setDisplayedIndices(int[] indices) {
    DisplayComponentDefaults.setGotoLocationIndices(indices);
/*
    int x = 0; int y = 0; int z = 0;
    if(indices != null) {
      if(indices.length > 0) {
	x = indices[0];
	if(indices.length > 1) {
	  y = indices[1];
	  if(indices.length > 2) z = indices[2];
	}
      }
    }
    if(xLocationModel.getValue() != x) xLocationModel.setValue(x);
    if(yLocationModel.getValue() != y) yLocationModel.setValue(y);
    if(zLocationModel.getValue() != z) zLocationModel.setValue(z);
*/
  }
  /**
   * Sets the coordinate map.
   *
   * @param coorMap	coordinate map
   */
  public void setCoordinateMap(final CoordinateMap coorMap) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() { setCoordinateMap(coorMap); }
	} );
      return;
    }
    this.coorMap = coorMap;
    if(coorMap == null) talMapLabel.setText("none");
    else talMapLabel.setText(coorMap.getName());
    updateMappedPoint();    
  }
  /**
   * Gets the coordinate map.
   *
   * @return	the coordinate map
   */
  public CoordinateMap getCoordinateMap() { return coorMap; }
  /**
   * Gets the coordinate resolutions.
   *
   * @return the coordinate resolutions
   */
  public XYZDouble getCoordinateResolutions() {
    if(coorRes == null) return null;
    else return new XYZDouble(coorRes);
  }
  /**
   * Gets the coordinate resolutions.
   *
   * @param resolutions the coordinate resolutions
   */
  public void setCoordinateResolutions(XYZDouble resolutions) {
    if(resolutions == null) coorRes = null;
    else coorRes = new XYZDouble(resolutions);
  }
  /**
   * Updates tracking crosshairs and slices for current trackers to
   * goto point.
   */
  private void trackPoint(int[] indices) {
    ShowPointTracker spt = getCNUDisplay().getShowPointTracker();
    if(spt != null) {
      Component comp = getCNUDisplay().getCurrentComponent();
      ShowPointImage spi = null;
      if(comp instanceof ShowPointImage) spi = (ShowPointImage) comp;
      spt.trackPoint(spi, indices);
    }
  }
  /**
   * Sets crosshairs and slices for current trackers to
   * goto point.
   */
  public void applyToTrackers() {
    ShowPointTracker spt = getCNUDisplay().getShowPointTracker();
    if(spt != null) {
      Component comp = getCNUDisplay().getCurrentComponent();
      ShowPointImage spi = null;
      if(comp instanceof ShowPointImage) spi = (ShowPointImage) comp;
      spt.setCrosshairs(spi, getDisplayedIndices());
    }
  }
  /**
   * Sets crosshairs and slices for current selected components.
   */
  public void applyToSelected() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { applyToSelected(); }
      } );
      return;
    }
    int[] point = getDisplayedIndices();
    Component[] components = getCNUDisplay().getSelectedComponentsOrdered();
    if(components != null) {
      Component comp = getCNUDisplay().getCurrentComponent();
      XYZDouble xyzpt = null;
      ShowPointControllerInterface spc = getCNUViewer().getShowPointController();
      if((comp instanceof CoordinateMappable) && spc.getMapTracking()) {
	CoordinateMap coorM = getCoordinateMap();
	if(coorM != null) {
	  XYZDouble res = getCoordinateResolutions();
	  xyzpt = coorM.toSpace(new XYZDouble(point, 0), res);
	}
      }
      UndoRedo ur = getCNUDisplay().getUndoRedo();
      ur.startSteps();
      for(int i=0; i<components.length; i++) {
	int[] newPoint =
	  ShowPointController.getMappedPoint(components[i],
					     xyzpt, point);
	if(components[i] instanceof SliceNumbering) {
	  SliceNumbering sliceNumbering = (SliceNumbering) components[i];
	  int slice = sliceNumbering.getSlice(newPoint);
	  if(slice >= 0) getCNUDisplay().setSlice(sliceNumbering, slice);
	}
	if((components[i] instanceof ShowPointImage) && (spc != null)) {
	  ShowPointImage spi = (ShowPointImage) components[i];
	  if(spi.getCrosshairIndices() != null)
	    spc.setComponentCrosshair(spi, null, newPoint, null, true);
	}
      }
      ur.finishUndoSteps("apply goto to current");
    }
  }
  /**
   * Sets goto point based on current selected component.
   */
  public void setToCurrent() {
    Component comp = getCNUDisplay().getCurrentComponent();
    ShowPointImage spi = null;
    CoordinateMap coorM = null;
    XYZDouble coorRes = null;
    if(comp instanceof CoordinateMappable) {
      coorM = ((CoordinateMappable) comp).getCoordinateMap();
      setCoordinateMap(coorM);
      coorRes = ((CoordinateMappable) comp).getCoordinateResolutions();
      setCoordinateResolutions(coorRes);
    }
    int[] indices = null;
    if(comp instanceof ShowPointImage) {
      spi = (ShowPointImage) comp;
      indices = spi.getCrosshairIndices();
    }
    if(indices != null) setDisplayedIndices(indices);
/*
      if(indices.length > 0) {
	xLocationModel.setValue(indices[0]);
	if(indices.length > 1) {
	  yLocationModel.setValue(indices[1]);
	  if(indices.length > 2)
	    zLocationModel.setValue(indices[2]);
	}
      }
    }
*/
    else if(comp instanceof SliceNumbering) {
      zLocationModel.setValue(((SliceNumbering) comp).getSlice());
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
  public boolean showPoint(int[] point, double value,
			   double factor, String name){
    return showPoint(point, value, factor, name, null, null);
  }
 /**
   * Sets the track mouse state.
   *
   * @param state	<code>true</code> to set track mouse state on
   *			<code>false</code> to set track mouse state off
   */
  public void setTrackMouseState(final boolean state) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setTrackMouseState(state); }
      } );
    }
    else {
      ButtonModel bm =
	trackMouseCheckboxAction.getCheckboxButtonModel();
      if(bm.isSelected() != state) bm.setSelected(state);
    }
  }
  /**
   * Retrieves the track mouse state.
   *
   * @return	<code>true</code> if track mouse state is on
   *		<code>false</code> if track mouse state is off
   */
  public boolean getTrackMouseState() {
    return
      trackMouseCheckboxAction.getCheckboxButtonModel().isSelected();
  }
  /**
   * Retrieves the auto update trackers state.
   *
   * @return	<code>true</code> if to auto update trackers
   *		<code>false</code> if to not auto update trackers
   */
  public boolean getAutoUpdateTrackersState() {
    return
      autoUpdateTrackersCheckboxAction.getCheckboxButtonModel().isSelected();
  }
  /**
   * Retrieves the auto update selected state.
   *
   * @return	<code>true</code> if to auto update selected objects
   *		<code>false</code> if to not auto update selected objects
   */
  public boolean getAutoUpdateSelectedState() {
    return
      autoUpdateSelectedCheckboxAction.getCheckboxButtonModel().isSelected();
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
  public boolean showPoint(int[] point, double value, double factor,
			   String name, XYZDouble xyzpt, String mapName)
  {
    setDisplayedIndices(ShowPointController.getMappedPoint(this, xyzpt,
							   point));
    return true;
  }
/*
  public class LocalBoundedRangeModel extends DefaultBoundedRangeModel {
    public LocalBoundedRangeModel(int value, int extent, int min, int max) {
      super(value, extent, min, max);
    }
    public void setValue(int n) {
      if(n > getMaximum()) setMaximum(n);
      super.setValue(n);
    }
  }
*/
  public class LocalAction extends EasyAddAbstractAction {
    private static final long serialVersionUID = -6754628040824736332L;
    private int command;
    public LocalAction(String name, String toolTip, boolean defaultState,
		       int cmd) {
      super(name, toolTip, NO_MNEMONIC, null, defaultState);
      command = cmd;
    }
    public void actionPerformed(ActionEvent ae) {
      CNUViewer cnuv = getCNUViewer();
      cnuv.setWaitCursor();
      try {
	switch(command) {
	case DISMISS:
	  setVisible(false);
	  break;
	case APPLY_TO_TRACKERS:
	  applyToTrackers();
	  break;
	case APPLY_TO_SELECTED:
	  applyToSelected();
	  break;
	case SET_NUMBER_FORMAT_TO_DEFAULT:
	  setNumberFormat(DisplayNumberFormat.getDefaultNumberFormat());
	  break;
	case SET_COORDINATE_MAP_TO_DEFAULT:
	  setCoordinateMap(LinearCoordinateMap.getDefaultCoordinateMap());
	  break;
	case SET_TO_CURRENT:
	  setToCurrent();
	  break;
	}
      } finally {
	cnuv.setNormalCursor();
      }
    }
    /**
     * Sets the button model and add this as an item listener.
     *
     * @param bm button model to attach to
     */
    public void setCheckboxButtonModel(ButtonModel bm) {
      super.setCheckboxButtonModel(bm);
      if(bm != null) bm.addItemListener(this);
    }
    /**
     * Handles ItemListener events from checkbox items
     *
     * @param e	item event
     */
    public void itemStateChanged(ItemEvent e){
      switch(command) {
      case TRACK_MOUSE:
	CNUViewer cnuv = getCNUViewer();
	if(cnuv != null) {
	  ShowPointControllerInterface spc =
	    getCNUViewer().getShowPointController();
	  if(spc != null) {
	    if(getTrackMouseState())
	      spc.addShowPointPair(GotoPointDialog.this, null, false);
	    else spc.removeShowPointPair(GotoPointDialog.this, false);
	  }
	}
	break;
      }
    }
  }
  /**
   * Sets the number format for converting numbers to strings.
   *
   * @param numberFormat	number format tool
   */
  public void setNumberFormat(final NumberFormat numberFormat) {
    if(numberFormat != null) {
      if(! SwingUtilities.isEventDispatchThread()) {
	SwingUtilities.invokeLater( new Runnable() {
	    public void run() { setNumberFormat(numberFormat); }
	  } );
	return;
      }
      this.numberFormat = (NumberFormat) numberFormat.clone();
      // update currently displayed point
      updateMappedPoint();
    }
  }
  /**
   * Gets the number format.
   *
   * @return	number format
   */
  public NumberFormat getNumberFormat() {
    return (NumberFormat) numberFormat.clone();
  }
  /**
   * Converts basic settings into a CNU script.
   *
   * @return iiV script commands to recreate current settings
   */
  public String toScript() {
    String classname=getClass().getName();
    StringBuffer sb=new StringBuffer("// -- start ");
    sb.append(classname).append(" settings script\n");
    sb.append("gotopointdialogtmp = CNUVIEWER.getGotoPointDialog();\n");

    sb.append(DisplayNumberFormat.numberFormatToScript(null, getNumberFormat()));
    sb.append("gotopointdialogtmp.setNumberFormat(script_rtn);\n");
    sb.append("script_rtn=gotopointdialogtmp;\n");
    sb.append("unset(\"gotopointdialogtmp\");\n");

    sb.append("// -- end ").append(classname).append(" settings script\n");
    return sb.toString();
  }
}
