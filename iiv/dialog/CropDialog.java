package iiv.dialog;
import iiv.display.*;
import iiv.*;
import iiv.gui.*;
import iiv.util.*;
import iiv.filter.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.*;
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
public class CropDialog extends CNUDialog
implements ActionListener, MouseListener, MouseMotionListener,
ItemListener {
  private static final long serialVersionUID = -4480411139441505070L;

  private BoundedRangeModel firstIDimModel =
    DisplayComponentDefaults.getDefaultFirstIdimRangeModel();
  private BoundedRangeModel lastIDimModel =
    DisplayComponentDefaults.getDefaultLastIdimRangeModel();

  private BoundedRangeModel firstFrameModel =
    DisplayComponentDefaults.getDefaultFirstFrameRangeModel();
  private BoundedRangeModel lastFrameModel =
    DisplayComponentDefaults.getDefaultLastFrameRangeModel();

  JCheckBox selectRegionCB = new JCheckBox("Select via mouse", false);

  private BoundedRangeModel xBegModel =
    DisplayComponentDefaults.getDefaultCropXBeginRangeModel();
  private BoundedRangeModel xEndModel =
    DisplayComponentDefaults.getDefaultCropXEndRangeModel();
  private BoundedRangeModel yBegModel =
    DisplayComponentDefaults.getDefaultCropYBeginRangeModel();
  private BoundedRangeModel yEndModel =
    DisplayComponentDefaults.getDefaultCropYEndRangeModel();

  //  JButton applyToDefaultsB = new JButton("Apply To Defaults");
  JButton cropB = new JButton("Crop");
  JButton uncropB = new JButton("Uncrop");
  JButton currentB = new JButton("Reset crop to current");
  JButton dismissB = new JButton("Dismiss");

  private Component currentComp = null;
  private Point startPoint = null;
  private boolean draggingBox = false;
  private Rectangle selectBox = null;
  private SimplePolygon displayedPolygon = null;

  private Rectangle saveBox = null;
  private Component saveComp = null;
  private boolean xyChangeListenerOn = false;
  private ChangeListener xyChangeListener = new ChangeListener() {
      public void stateChanged(ChangeEvent e) {	cropBoxTrackScrollBar(); }
    };
  /**
   * Constructs a new instance of CropDialog.
   *
   * @param parentFrame	parent frame needed for building a window
   */
  public CropDialog(Frame parentFrame) { this(parentFrame, null); }
  /**
   * Constructs a new instance of CropDialog.
   *
   * @param parentFrame	parent frame
   * @param cnuv	primary display controller
   */
  public CropDialog(Frame parentFrame, CNUViewer cnuv ) {
    super(parentFrame, "Limits/Cropping", false, cnuv);
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
  }
  private void init(CNUViewerActions cnuviewerActions) {
    Container contentPane = getContentPane();
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    contentPane.add(Box.createVerticalStrut(5));

    Box box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(new JLabel("Limits for displaying new slices of data"));
    box.add(Box.createHorizontalGlue());
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    Container grid = new Container() {private static final long serialVersionUID = 4820271084033698563L;};
    box.add(grid);
    grid.setLayout(new GridLayout(0, 3, 5, 5));

    ChangeListener ziChangeListener = new ChangeListener() {
	public void stateChanged(ChangeEvent e) {
	  Object source = e.getSource();
	  if((source == firstIDimModel) || (source == lastIDimModel))
	    DisplayComponentDefaults.setIDimLimitMode(true);
	  else if((source == firstFrameModel) || (source == lastFrameModel))
	    getCNUViewer().setSliceLimitMode(true);
	}
      };

    cnuviewerActions.limitIDimCheckboxAction.addCheckboxTo(grid);
    TextAndSlider tands =
      new TextAndSlider("First i:  ", 4, firstIDimModel);
    tands.setToolTipText("Selects first i indice of sequence when newly displaying slices from 4D data");
    grid.add(tands);
    firstIDimModel.addChangeListener(ziChangeListener);
    tands = new TextAndSlider("Last i:  ", 4, lastIDimModel);
    tands.setToolTipText("Selects last i indice of sequence when newly displaying slices from 4D data");
    grid.add(tands);
    lastIDimModel.addChangeListener(ziChangeListener);

    cnuviewerActions.limitSlicesCheckboxAction.addCheckboxTo(grid);
    tands = new TextAndSlider("First slice:  ", 4, firstFrameModel);
    tands.setToolTipText("Selects first slice indice of sequence when newly displaying data");
    grid.add(tands);
    firstFrameModel.addChangeListener(ziChangeListener);
    tands = new TextAndSlider("Last slice:  ", 4, lastFrameModel);
    tands.setToolTipText("Selects last slice indice of sequence when newly displaying data");
    grid.add(tands);
    lastFrameModel.addChangeListener(ziChangeListener);

    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(new JLabel("Cropping"));
    box.add(Box.createHorizontalGlue());
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    grid = new Container() {private static final long serialVersionUID = 3440190551492098127L;};
    box.add(grid);
    grid.setLayout(new GridLayout(0, 3, 5, 5));

    cnuviewerActions.cropCheckboxAction.addCheckboxTo(grid);
    tands = new TextAndSlider("Horizontal begin:  ", 4, xBegModel);
    tands.setToolTipText("Selects first horizontal indice of crop box");
    grid.add(tands);
    tands = new TextAndSlider("Horizontal end:  ", 4, xEndModel);
    tands.setToolTipText("Selects last horizontal indice of crop box");
    grid.add(tands);

    grid.add(selectRegionCB);
    selectRegionCB.addItemListener(this);
    selectRegionCB.setToolTipText("Choose to select crop region via the mouse over displayed components");
    tands = new TextAndSlider("Vertical begin:  ", 4, yBegModel);
    tands.setToolTipText("Selects first vertical indice of crop box");
    grid.add(tands);
    tands = new TextAndSlider("Vertical end:  ", 4, yEndModel);
    tands.setToolTipText("Selects last vertical indice of crop box");
    grid.add(tands);
    setXYChangeTrackingMode(true);

    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    //    box.add(applyToDefaultsB);
    //    applyToDefaultsB.addActionListener(this);
    box.add(cropB);
    cropB.addActionListener(this);
    cropB.setToolTipText("Crop current selected or all components");
    box.add(uncropB);
    uncropB.addActionListener(this);
    uncropB.setToolTipText("Uncrop current selected or all components");
    box.add(currentB);
    currentB.setToolTipText("Resets to crop box of current selected component");
    currentB.addActionListener(this);
    box.add(Box.createHorizontalGlue());
    box.add(dismissB);
    dismissB.setToolTipText("Hide this window");
    dismissB.addActionListener(this);
    box.add(Box.createHorizontalStrut(5));

    contentPane.add(Box.createVerticalStrut(5));
    pack();
  }
  /**
   * Sets whether the save region storage parameter tracks the sliders.
   *
   * @param mode	<code>true</code> to have the save
   *                    region to track slider changes.
   */
  private void setXYChangeTrackingMode(boolean mode) {
    if(mode != xyChangeListenerOn) {
      xyChangeListenerOn = mode; // save mode to not add changelistener multiply
      if(mode) {
	xBegModel.addChangeListener(xyChangeListener);
	xEndModel.addChangeListener(xyChangeListener);
	yBegModel.addChangeListener(xyChangeListener);
	yEndModel.addChangeListener(xyChangeListener);
      }
      else {
	xBegModel.removeChangeListener(xyChangeListener);
	xEndModel.removeChangeListener(xyChangeListener);
	yBegModel.removeChangeListener(xyChangeListener);
	yEndModel.removeChangeListener(xyChangeListener);
      }
    }
  }
  /**
   * Sets the i dimension limits.
   *
   * @param firstIDim	first i dimension
   * @param lastIDim	last i dimension
   * @param nIDims	number of i dimensions
   */
  public void setIRange(int firstIDim, int lastIDim, int nIDims) {
    DisplayComponentDefaults.setDefaultIRange(firstIDim, lastIDim, nIDims);
  }
  /**
   * Get the first i dimension limit.
   *
   * @return	first i dimension limit
   */
  public int getFirstIDimLimit() {
    return DisplayComponentDefaults.getDefaultFirstIDimLimit();
  }
  /**
   * Gets the last i dimension limit.
   *
   * @return	last i dimension limit
   */
  public int getLastIDimLimit() {
    return DisplayComponentDefaults.getDefaultLastIDimLimit();
  }
  /**
   * Sets the slice limits.
   *
   * @param firstSlice	first slice
   * @param lastSlice	last slice
   */
  public void setSlices(int firstSlice,
		        int lastSlice, int nSlices) {
    DisplayComponentDefaults.setDefaultSlices(firstSlice, lastSlice, nSlices);
  }
  /**
   * Gets the default first slice number.
   *
   * @return first slice
   */
  public int getFirstSliceLimit() {
    return DisplayComponentDefaults.getDefaultFirstSliceLimit();
  }
  /**
   * Gets the default last slice number.
   *
   * @return last slice
   */
  public int getLastSliceLimit() {
    return DisplayComponentDefaults.getDefaultLastSliceLimit();
  }
  /**
   * Sets the scroll bar crop values.
   *
   * @param cropRect	crop values stored as a rectangle
   */
  public void setCropValues(Rectangle cropRect) {
    DisplayComponentDefaults.setDefaultCrop(cropRect);
  }
  /**
   * Sets the displayed crop values.
   *
   * @param xbeg	x begin
   * @param ybeg	y begin
   * @param xend	x end
   * @param yend	y end
   */
  public void setCropValues(int xbeg, int ybeg,
    int xend, int yend) {
    DisplayComponentDefaults.setDefaultCrop(xbeg, ybeg, xend, yend);
  }
  /**
   * Sets the text scroll bar crop values.
   *
   * @param cropRect	crop values stored as a rectangle
   */
  protected void setCropTextValues(Rectangle cropRect) {
    if(cropRect != null) { setCropValues(cropRect); }
  }
  /**
   * Gets the crop values as a rectangle.
   *
   * @return	crop values
   */
  public Rectangle getCropBounds() {
    return DisplayComponentDefaults.getDefaultCrop();
  }
  /**
   * Updates the displayed crop box to track the current scroll bar values.
   */
  public void cropBoxTrackScrollBar() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { cropBoxTrackScrollBar(); }
      } );
    }
    else {
      if((saveComp != null) && selectRegionCB.isSelected()) {
        // get box specified by scroll bar
        saveBox = getCropBounds();
        getCNUDisplay().cropBoxUpdate(saveBox, saveComp);
      }
    }
  }
  /**
   * Handles ItemListener events.
   *
   * @param ie	item event
   */
  public void itemStateChanged(ItemEvent ie){
    if(ie.getSource() == selectRegionCB) {
      if(selectRegionCB.isSelected()) grabCNUDisplaysMouse();
      else restoreCNUDisplaysMouse();
    }
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
      if (source == currentB) {
        setCropValues(getCNUDisplay().getCurrentCropBox());
      }
      else if (source == cropB) {
	cnuv.setCropBox(getCropBounds(), true);
        getCNUDisplay().apply(CNUDisplay.CROP);
        restoreCNUDisplaysMouse();
      }
      //      else if (source == applyToDefaultsB) {
      //	cnuv.setCropBox(getCropBounds(), true);
      //        restoreCNUDisplaysMouse();
      //      }
      else if (source == uncropB) getCNUDisplay().uncrop();
      else if(source == dismissB) {
        this.setVisible(false);
        restoreCNUDisplaysMouse();
      }
    } finally {
      cnuv.setNormalCursor();
    }
  }
  /**
   * Overrides setVisible to restore mouse listener if dismissed.
   *
   * @param state	<code>true</code> to make visible
   */
  public void setVisible(boolean state) {
    if( ! state ) restoreCNUDisplaysMouse();
    super.setVisible(state);
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
    Rectangle sb = null;
    if(saveBox != null) sb = saveBox.getBounds();
    boolean gotMouse = getCNUDisplay().replaceMouseListeners(this);
    if(selectRegionCB.isSelected() != gotMouse)
      selectRegionCB.setSelected(gotMouse);
    if(gotMouse) getCNUDisplay().selectRegionUpdate(sb);
  }
  /**
   * Restores mouse input to the CNUDisplay.
   */
  public void restoreCNUDisplaysMouse() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { restoreCNUDisplaysMouse(); }
      } );
    }
    else {
      getCNUDisplay().restoreMouseListeners(this);
      if(selectRegionCB.isSelected()) selectRegionCB.setSelected(false);
      saveBox = null; saveComp = null;
      getCNUDisplay().cropBoxUpdate(null, null);
    }
  }
  /**
   * Called when mouse moves while a mouse button is down to handle
   * crop box resizing or moving.
   *
   * @param evt	mouse dragged event
   */
  public void mouseDragged(MouseEvent evt) {
    Component comp = currentComp;
    if(! (comp instanceof Croppable) ) return;

    Point pt = evt.getPoint();

    CNUDisplay cnud = getCNUDisplay();
    if(displayedPolygon != null)
      cnud.xorDrawPoly(displayedPolygon); // erase previous polygon
    pt = trueLocationFromCNUD(pt, comp);
    if(startPoint != null) {
      if(draggingBox && (saveBox != null)) {
	selectBox = saveBox.getBounds();
	Point amountMoved = pt;
	amountMoved.translate(-startPoint.x, -startPoint.y);
	selectBox.translate(amountMoved.x, amountMoved.y);
      }
      else {
        if(selectBox == null) selectBox = new Rectangle(startPoint);
        else selectBox.setBounds(startPoint.x, startPoint.y, 0, 0);
	selectBox.add(pt); // for left & top additions
        selectBox.add(pt.x + 1, pt.y + 1); // for right & bottom additions
      }
      // restrict box to be on comp
      Rectangle currentCrop = ((Croppable) comp).getCrop();
      if(currentCrop != null) selectBox.intersection(currentCrop);
      else selectBox = ((Croppable) comp).restrictCropBox(selectBox);
      setCropTextValues(selectBox);
      // replace values in displayedPolygon with values from selectBox
      displayedPolygon = displayPoly(selectBox, comp, displayedPolygon);
      cnud.xorDrawPoly(displayedPolygon); // draw new polygon
    }
  }
  /**
   * Catches mouse up events to display crop box.
   *
   * @param evt	mouse released event
   */
  public void mouseReleased(MouseEvent evt){
    // erase previous polygon
    if(displayedPolygon != null) {
      getCNUDisplay().xorDrawPoly(displayedPolygon);
      displayedPolygon = null;
    }
    if(selectBox != null) {
      if(currentComp != null) {
	saveBox = selectBox; // keep box for later mods
	saveComp = currentComp;
	setCropValues(selectBox);
	getCNUDisplay().cropBoxUpdate(saveBox, saveComp);
      }
      selectBox = null;
      draggingBox = false;
    }
    setXYChangeTrackingMode(true);
  }
  /**
   * Corrects canvas point location for flip, zoom and crop.
   *
   * @param pt	location on canvas
   * @return	location in original image with no filtering
   */
  static public Point trueLocationFromCNUD(Point pt, Component comp) {
    Point tmpPt = new Point(pt);
    Point cnudLocation = comp.getLocation();
    tmpPt.translate(-cnudLocation.x, -cnudLocation.y);
    if(comp instanceof LocationMapping)
      tmpPt = ((LocationMapping) comp).trueLocation(tmpPt);
    return tmpPt;
  }
  /**
   * Catches mouse down events to display crop box.
   *
   * @param evt	mouse pressed event
   */
  public void mousePressed(MouseEvent evt){
    setXYChangeTrackingMode(false);
    Point pt = evt.getPoint();
    SimplePolygon newpoly = null;

    Component comp = getCNUDisplay().getDisplayComponent(pt);
    if(! (comp instanceof Croppable) ) {
      startPoint = null;
      currentComp = null;
    }
    else {
      // adjust point to true location over image
      pt = trueLocationFromCNUD(pt, comp);
      startPoint = pt;
      currentComp = comp;
      if( (currentComp == saveComp) && (saveBox != null) ) {
	if( ((evt.getModifiers() & InputEvent.BUTTON2_MASK) != 0) ||
	    evt.isAltDown() || evt.isControlDown()) {
	  draggingBox = true;
	  selectBox = saveBox.getBounds();
        }
        else {
          // adjusting size of saveBox
          // initialize the selectBox with this start point
          selectBox = new Rectangle(startPoint);
          // then reset the startPoint to the opposite side of the saveBox
          // drags will occur as if initiated from this opposite side
          Point midPt = new Point( saveBox.x + (saveBox.width/2),
			           saveBox.y + (saveBox.height/2) );
          if(startPoint.x > midPt.x) startPoint.x = saveBox.x;
          else startPoint.x = saveBox.x + saveBox.width - 1;
          if(startPoint.y > midPt.y) startPoint.y = saveBox.y;
          else startPoint.y = saveBox.y + saveBox.height - 1;
          // adjust the selectBox to include this opposite side
          selectBox.add(startPoint); // for left & top additions
	  // for right & bottom additions
          selectBox.add(startPoint.x + 1, startPoint.y + 1);
        }
        // draw the box
	newpoly = displayPoly(selectBox, currentComp, displayedPolygon);
      }
      else if( ((evt.getModifiers() & InputEvent.BUTTON2_MASK) != 0) ||
		evt.isAltDown() || evt.isControlDown()) {
        saveComp = currentComp;
        if(saveBox != null) {
          draggingBox = true;
          selectBox = saveBox.getBounds();
          newpoly = displayPoly(selectBox, currentComp, displayedPolygon);
        }
	cropBoxTrackScrollBar();
      }
    }
    if(newpoly != null) {
      displayedPolygon = newpoly;
      getCNUDisplay().xorDrawPoly(displayedPolygon); // draw new polygon
    }
  }
/** Called when the mouse is moved to a new point */
  public void mouseMoved(MouseEvent evt) {}
/** Called when the mouse is clicked */
  public void mouseClicked(MouseEvent evt) {};
/** Called when the mouse moves over this */
  public void mouseEntered(MouseEvent evt) {};
/** Called when the mouse leave this */
  public void mouseExited(MouseEvent evt) {};
  /**
   * Calculate the display polygon in a component containers space
   * from a true cropbox associated with a display component.
   *
   * @param box		box in components image space
   * @param comp	Component box is to be drawn for
   * @param poly	polygon that may be written over
   *			to save allocating a new polygon.
   *			If <code>null</code> a new SimplePolygon
   *			will be constructed.
   * @return		polygon representing the box in the
   *			components containers space
   *
   */
  public static SimplePolygon displayPoly(Rectangle box, Component comp,
     SimplePolygon poly) {
     if(poly == null) poly = new SimplePolygon(4);
     else if(poly.npoints != 4) poly = new SimplePolygon(4);
     // Get components location in display
     Point location = comp.getLocation();
     Point trueUL = box.getLocation();
     if(comp instanceof LocationMapping) {
       Point displayPt = ((LocationMapping) comp).displayLocation(trueUL);
       poly.xpoints[0] = displayPt.x + location.x;
       poly.ypoints[0] = displayPt.y + location.y;
       trueUL.move(box.x, box.y + box.height - 1);
       displayPt = ((LocationMapping) comp).displayLocation(trueUL);
       poly.xpoints[1] = displayPt.x + location.x;
       poly.ypoints[1] = displayPt.y + location.y;
       trueUL.move(box.x + box.width - 1, box.y + box.height - 1);
       displayPt = ((LocationMapping) comp).displayLocation(trueUL);
       poly.xpoints[2] = displayPt.x + location.x;
       poly.ypoints[2] = displayPt.y + location.y;
       trueUL.move(box.x + box.width - 1, box.y);
       displayPt = ((LocationMapping) comp).displayLocation(trueUL);
       poly.xpoints[3] = displayPt.x + location.x;
       poly.ypoints[3] = displayPt.y + location.y;
     }
     else {
       poly.xpoints[0] = trueUL.x + location.x;
       poly.ypoints[0] = trueUL.y + location.y;
       trueUL.move(box.x, box.y + box.height - 1);
       poly.xpoints[1] = trueUL.x + location.x;
       poly.ypoints[1] = trueUL.y + location.y;
       trueUL.move(box.x + box.width - 1, box.y + box.height - 1);
       poly.xpoints[2] = trueUL.x + location.x;
       poly.ypoints[2] = trueUL.y + location.y;
       trueUL.move(box.x + box.width - 1, box.y);
       poly.xpoints[3] = trueUL.x + location.x;
       poly.ypoints[3] = trueUL.y + location.y;
     }
     return poly;
  }
}
