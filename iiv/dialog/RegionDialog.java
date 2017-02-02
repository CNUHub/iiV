package iiv.dialog;
import iiv.*;
import iiv.display.*;
import iiv.gui.*;
import iiv.util.*;
import iiv.script.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.image.*;
import java.io.*;
import java.lang.*;
import Acme.JPM.Encoders.*;

/**
 * Dialog to get help select a region.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUViewer
 * @since	iiV1.0
 */
public class RegionDialog extends CNUDialog
implements ActionListener, MouseListener, MouseMotionListener,
ItemListener {
  private static final long serialVersionUID = -5134983716248992911L;
  private JCheckBox selectRegionCB = new JCheckBox("Select Region");
  private JButton clearSelectionB = new JButton("Clear Selection");

  private JButton dismissB = new JButton("Dismiss");

  private DefaultBoundedRangeModel xbegModel =
    new DefaultBoundedRangeModel(0, 0, 0, 1023);
  private DefaultBoundedRangeModel xendModel =
    new DefaultBoundedRangeModel(0, 0, 0, 1023);
  private DefaultBoundedRangeModel ybegModel =
    new DefaultBoundedRangeModel(0, 0, 0, 1023);
  private DefaultBoundedRangeModel yendModel =
    new DefaultBoundedRangeModel(0, 0, 0, 1023);

  private boolean draggingBox = false;
  private Point startPoint = null;
  private Rectangle dragBox = null;
  private Rectangle regionBox = null;

  private boolean changeListenerOn = false;
  private ChangeListener regionTrackChangeListener =
    new ChangeListener() {
      public void stateChanged(ChangeEvent e) { regionTrackScrollBar(); }
    };

  /**
   * Constructs a new instance of RegionDialog.
   *
   * @param parentFrame	parent frame needed for building a window
   */
  public RegionDialog(Frame parentFrame) { this(parentFrame, null); }
  /**
   * Constructs a new instance of RegionDialog.
   *
   * @param parentFrame	parent frame needed for building a window
   * @param cnuv	CNUViewer that this window interacts with
   */
  public RegionDialog(Frame parentFrame, CNUViewer cnuv ) {
    super(parentFrame, "Region Selection", false, cnuv);

    Container contentPane = getContentPane();

    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    contentPane.add(Box.createVerticalStrut(5));
    Container mainBox = Box.createVerticalBox();
    contentPane.add(mainBox);
    contentPane.add(Box.createVerticalStrut(5));

    mainBox.add(Box.createHorizontalStrut(5));
    Container box = Box.createHorizontalBox();
    mainBox.add(box);

    box.add(selectRegionCB);
    selectRegionCB.addItemListener(this);
    box.add(clearSelectionB);
    clearSelectionB.addActionListener(this);
    box.add(dismissB);
    dismissB.addActionListener(this);
    box.add(Box.createHorizontalGlue());

    box = new Container() {private static final long serialVersionUID = -3144002068853380195L;};
    box.setLayout(new GridLayout(2, 2, 5, 5));
    mainBox.add(box);

    TextAndSlider xbegSlider =
      new TextAndSlider("Horizontal begin:  ", 4, xbegModel);
    box.add(xbegSlider);
    TextAndSlider xendSlider =
      new TextAndSlider("Horizontal end:  ", 4, xendModel);
    box.add(xendSlider);
    TextAndSlider ybegSlider =
      new TextAndSlider("Vertical begin:  ", 4, ybegModel);
    box.add(ybegSlider);
    TextAndSlider yendSlider =
      new TextAndSlider("Vertical end:  ", 4, yendModel);
    box.add(yendSlider);
    setRegionSliderTrackingMode(true);

    mainBox.add(Box.createHorizontalStrut(5));

    pack();
  }
  /**
   * Get the current selection region.
   *
   * @return selected region or <code>null</code> if none selected
   */
  public Rectangle getSelectedRegion() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = getSelectedRegion(); }
      };
      runWithReturn.invokeAndWait();
      return (Rectangle) runWithReturn.returnObject;
    }
    if(regionBox == null) return null;
    return regionBox.getBounds();
  }
  /**
   * Set the current selection region.
   *
   * @param region to set selection to
   */
  public void setSelectedRegion(Rectangle region) {
      return;
  }
  /**
   * Sets whether the region storage parameter tracks the sliders.
   *
   * @param mode	<code>true</code> to have the save
   *                    region to track slider changes.
   */
  private void setRegionSliderTrackingMode(boolean mode) {
    if(mode != changeListenerOn) {
      changeListenerOn = mode; // save mode to not add changelistener multiply
      if(mode) {
	xbegModel.addChangeListener(regionTrackChangeListener);
	xendModel.addChangeListener(regionTrackChangeListener);
	ybegModel.addChangeListener(regionTrackChangeListener);
	yendModel.addChangeListener(regionTrackChangeListener);
      }
      else {
	xbegModel.removeChangeListener(regionTrackChangeListener);
	xendModel.removeChangeListener(regionTrackChangeListener);
	ybegModel.removeChangeListener(regionTrackChangeListener);
	yendModel.removeChangeListener(regionTrackChangeListener);
      }
    }
  }
  /**
   * Interprets action events over this dialog.
   *
   * @param e	action event
   */
  public void actionPerformed(ActionEvent e) {
    getCNUViewer().setWaitCursor();
    try {
      Object source = e.getSource();
      if(source == clearSelectionB) {
	updateScrollValues(null);
	if(selectRegionCB.isSelected()) getCNUDisplay().selectRegionUpdate(null);
	// must clear regionBox last because regionTrackScrollbar might set it.
	regionBox = null;
      }
      else if(source == dismissB) {
        setVisible(false);
      }
    } finally {
      getCNUViewer().setNormalCursor();
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
    if(regionBox != null) sb = regionBox.getBounds();
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
      return;
    }
    getCNUDisplay().restoreMouseListeners(this);
    if(selectRegionCB.isSelected()) selectRegionCB.setSelected(false);
    getCNUDisplay().selectRegionUpdate(null);
  }
  /**
   * Updates scroll values that represent the save box.
   *
   * @param box	region box to set scroll values to
   */
  public void updateScrollValues(Rectangle box) {
    if(! SwingUtilities.isEventDispatchThread()) {
      final Rectangle fbox = box;
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { updateScrollValues(fbox); }
      } );
    }
    else {
      Dimension maxBox = getCNUDisplay().getSize();
      if(box == null) box = new Rectangle();
      if((box.x != xbegModel.getValue()) ||
	 (xbegModel.getMaximum() != maxBox.width)) {
	if(box.x > maxBox.width) maxBox.setSize(box.x+1, maxBox.height);
        xbegModel.setMaximum(maxBox.width);
        xbegModel.setValue(box.x);
      }
      int xend = box.x + box.width - 1;
      if((xend != xendModel.getValue()) ||
	 (xendModel.getMaximum() != maxBox.width)) {
	if(xend > maxBox.width) maxBox.setSize(xend+1, maxBox.height);
	xendModel.setValue(xend);
	xendModel.setMaximum(maxBox.width);
      }
      if((box.y != ybegModel.getValue()) ||
	 (ybegModel.getMaximum() != maxBox.height)) {
	if(box.y > maxBox.height) maxBox.setSize(maxBox.width, box.y+1);
	ybegModel.setValue(box.y);
	ybegModel.setMaximum(maxBox.height);
      }
      int yend = box.y + box.height - 1;
      if((yend != yendModel.getValue()) ||
	 (yendModel.getMaximum() != maxBox.height)) {
	if(yend > maxBox.height) maxBox.setSize(maxBox.width, yend+1);
	yendModel.setValue(yend);
	yendModel.setMaximum(maxBox.height);
      }
    }
  }
  /**
   * Updates the displayed region box to track the current scroll bar values.
   */
  public void regionTrackScrollBar() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { regionTrackScrollBar(); }
      } );
    }
    else {
      int xbeg = xbegModel.getValue();
      int xend = xendModel.getValue();
      if(xend < xbeg) {int tmp = xend; xend = xbeg; xbeg = tmp; }
      int ybeg = ybegModel.getValue();
      int yend = yendModel.getValue();
      if(yend < ybeg) {int tmp = yend; yend = ybeg; ybeg = tmp; }
      regionBox = new Rectangle(xbeg, ybeg, xend - xbeg + 1,
			      yend - ybeg + 1);
      if(selectRegionCB.isSelected()) {
	Rectangle displayRegion = regionBox.getBounds();
	if(displayRegion != null)
	  getCNUDisplay().selectRegionUpdate(displayRegion);
      }
    }
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
   * Called when mouse moves while a mouse button is down.
   *
   * @param evt	mouse event
   */
  public void mouseDragged(MouseEvent evt) {
    CNUDisplay cnud = getCNUDisplay();
    if(dragBox == null) {
      dragBox = new Rectangle(startPoint);
      dragBox.add(evt.getPoint());
    }
    else {
      cnud.xorDrawBox(dragBox);  // erase previous box
      if(draggingBox && (regionBox != null)) {
        Point amountMoved = evt.getPoint().getLocation();
	amountMoved.translate(-startPoint.x, -startPoint.y);
	dragBox = regionBox.getBounds();
	dragBox.translate(amountMoved.x, amountMoved.y);
      }
      else {
	dragBox.setBounds(startPoint.x, startPoint.y, 0, 0);
	dragBox.add(evt.getPoint());
      }
    }
    cnud.xorDrawBox(dragBox);  // draw new box
    updateScrollValues(dragBox);
  }
  /**
   * Catches mouse up events to finish region selection.
   *
   * @param evt	mouse event
   */
  public void mouseReleased(MouseEvent evt){
    if(dragBox != null) {
      getCNUDisplay().xorDrawBox(dragBox); // erase temp box
      regionBox = dragBox; // keep box for saving
      dragBox = null;
      updateScrollValues(regionBox);
      getCNUDisplay().selectRegionUpdate(regionBox);
    }
    draggingBox = false;
    setRegionSliderTrackingMode(true);
  }
  /**
   * Catches mouse down events to begin region selection.
   *
   * @param evt	mouse event
   */
  public void mousePressed(MouseEvent evt){
    setRegionSliderTrackingMode(false);
    startPoint = evt.getPoint().getLocation();
    if( ((evt.getModifiers() & InputEvent.BUTTON2_MASK) != 0) ||
	evt.isAltDown() || evt.isControlDown()) {
      if(regionBox != null) {
	dragBox = regionBox.getBounds();
	getCNUDisplay().xorDrawBox(dragBox); // draw box
	draggingBox = true;
      }
    }
    else if(regionBox != null) {
      dragBox = regionBox.getBounds();
      // adjusting size of dragBox
      // then reset the startPoint to the opposite side of the dragBox
      // drags will occur as if initiated from this opposite side
      Point midPt = new Point( dragBox.x + (dragBox.width/2),
			       dragBox.y + (dragBox.height/2) );
      if(startPoint.x > midPt.x) midPt.x = dragBox.x;
      else midPt.x = dragBox.x + dragBox.width - 1;
      if(startPoint.y > midPt.y) midPt.y = dragBox.y;
      else midPt.y = dragBox.y + dragBox.height - 1;
      // set select box to include this start point
      dragBox.setBounds(startPoint.x, startPoint.y, 0, 0);
      // then adjust the dragBox to include this opposite side
      dragBox.add(midPt); // for left & top additions
      // for right & bottom additions
      dragBox.add(midPt.x + 1, midPt.y + 1);
      startPoint = midPt;
      getCNUDisplay().xorDrawBox(dragBox); // draw new box
    }
  }
  /**
   * Called when the mouse is moved to a new point - ignored.
   *
   * @param evt	mouse event
   */
  public void mouseMoved(MouseEvent evt) {}
  /**
   * Called when the mouse is clicked - ignored.
   *
   * @param evt	mouse event
   */
  public void mouseClicked(MouseEvent evt) {};
  /**
   * Called when the mouse moves over this - ignored.
   *
   * @param evt	mouse event
   */
  public void mouseEntered(MouseEvent evt) {};
  /**
   * Called when the mouse leaves this - ignored.
   *
   * @param evt	mouse event
   */
  public void mouseExited(MouseEvent evt) {};
}
