package iiv.dialog;
import iiv.*;
import iiv.display.*;
import iiv.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.lang.*;

/**
 * Dialog to dislplay and get scale factors from the user.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 */
public class GridDialog extends CNUDialog
implements ActionListener,  ItemListener {
  private static final long serialVersionUID = -33130260381029377L;
  private JCheckBox gridCB = new JCheckBox("Grid On", false);
  private JCheckBox paperCB = new JCheckBox("Paper Outline On", false);
  private JButton applyB = new JButton("Apply");
  private JTextField screenResCorrectionTF = new JTextField(8);
  private JTextField gridSpacingTF = new JTextField(4);
  private JTextField gridOffsetX_TF = new JTextField(4);
  private JTextField gridOffsetY_TF = new JTextField(4);
  private JTextField paperWidthTF = new JTextField(4);
  private JTextField paperHeightTF = new JTextField(4);
  private JTextField paperOffsetX_TF = new JTextField(4);
  private JTextField paperOffsetY_TF = new JTextField(4);
  private JComboBox gridColorCH = new JComboBox(colorNames);
  public static String[] unitTypes = {"inches", "cm", "pixels"};
  public static int[] unitValues =
     {CNUDisplay.INCHES, CNUDisplay.CM, CNUDisplay.PIXELS};
  private JComboBox unitsCH = new JComboBox(unitTypes);
  private JComboBox paperColorCH = new JComboBox(colorNames);
  private JButton snapTopB = new JButton("Snap Top");
  private JButton snapLeftB = new JButton("Snap Left");
  private JButton snapRightB = new JButton("Snap Right");
  private JButton snapBottomB = new JButton("Snap Bottom");
  private JButton setToCurrentB = new JButton("Set to Current");
  private JButton dismissB = new JButton("dismiss");

  private Object localLock = new Object();
  private int screenResolution = 0;
  private double screenResCorrection = 1.0;
  private double gridSpacing = -1;
  private double gridOffsetX = 0;
  private double gridOffsetY = 0;
  private double paperWidth = -1;
  private double paperHeight = -1;
  private double paperOffsetX = 0;
  private double paperOffsetY = 0;
  private int currentUnits = CNUDisplay.INCHES;
  /**
   * Constructs a new instance of GridDialog.
   *
   * @param parentFrame	parent frame
   */
  public GridDialog(Frame parentFrame) { this(parentFrame, null); }
  /**
   * Constructs a new instance of GridDialog.
   *
   * @param parentFrame	parent frame
   * @param cnuv	CNUViewer to work with
   */
  public GridDialog(Frame parentFrame, CNUViewer cnuv) {
    super(parentFrame, "Grid Dialog", false, cnuv);

    Container contentPane = getContentPane();
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    contentPane.add(Box.createVerticalStrut(5));

    Box box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(new JLabel("Units:  "));
    box.add(Box.createHorizontalGlue());
    box.add(unitsCH);
    unitsCH.addItemListener(this);
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(new JLabel("Resolution correction:  "));
    box.add(Box.createHorizontalGlue());
    box.add(screenResCorrectionTF);
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(gridCB);
    gridCB.addItemListener(this);
    box.add(Box.createHorizontalGlue());
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(new JLabel("Grid Spacing:  "));
    box.add(Box.createHorizontalGlue());
    box.add(gridSpacingTF);
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(new JLabel("Grid Offsets:  "));
    box.add(Box.createHorizontalGlue());
    box.add(gridOffsetX_TF);
    box.add(gridOffsetY_TF);
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(new JLabel("Grid Color:  "));
    box.add(Box.createHorizontalGlue());
    box.add(gridColorCH);
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(paperCB);
    paperCB.addItemListener(this);
    box.add(Box.createHorizontalGlue());
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(new JLabel("Paper Size:  "));
    box.add(Box.createHorizontalGlue());
    box.add(paperWidthTF);
    box.add(paperHeightTF);
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(new JLabel("Paper Offsets:  "));
    box.add(Box.createHorizontalGlue());
    box.add(paperOffsetX_TF);
    box.add(paperOffsetY_TF);
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(new JLabel("Paper Color:  "));
    box.add(Box.createHorizontalGlue());
    box.add(paperColorCH);
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(applyB);
    applyB.addActionListener(this);
    box.add(Box.createHorizontalGlue());
    box.add(setToCurrentB);
    setToCurrentB.addActionListener(this);
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(snapTopB);
    snapTopB.addActionListener(this);
    box.add(Box.createHorizontalGlue());
    box.add(snapBottomB);
    snapBottomB.addActionListener(this);
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(snapLeftB);
    snapLeftB.addActionListener(this);
    box.add(Box.createHorizontalGlue());
    box.add(snapRightB);
    snapRightB.addActionListener(this);
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(Box.createHorizontalGlue());
    box.add(dismissB);
    dismissB.addActionListener(this);
    box.add(Box.createHorizontalGlue());
    box.add(Box.createHorizontalStrut(5));

    contentPane.add(Box.createVerticalStrut(5));
    //    setToCurrent();
    pack();
  }
  /**
   * Gets the corrected screen resolution.
   *
   * @return	corrected screen resolution
   */
  public double getCorrectedResolution() {
    if(screenResolution < 1) synchronized (localLock) {
      if(screenResolution < 1)
        screenResolution = getCNUDisplay().getScreenResolution();
    }
    return screenResolution * getScreenResCorrection();
  }
  /**
   * Gets the screen resolution correction from the text field.
   *
   * @return	screen resolution correction
   */
  public double getScreenResCorrection() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() {
	  returnObject = new Double(getScreenResCorrection());
	}
      };
      runWithReturn.invokeAndWait();
      return ((Double) runWithReturn.returnObject).doubleValue();
    }
    else {
      try {
        screenResCorrection =
	  Double.valueOf(screenResCorrectionTF.getText()).doubleValue();
        if(screenResCorrection <= 1e-3 || screenResCorrection > 1e3) {
	  screenResCorrection = 1;
          Toolkit.getDefaultToolkit().beep();
          updateScreenResCorrectionText();
        }
      }  catch (NumberFormatException e3) {
        Toolkit.getDefaultToolkit().beep();
        updateScreenResCorrectionText();
      }
      return screenResCorrection;
    }
  }
  /**
   * Converts pixel distance value to a string in current selected
   * units value.
   *
   * @param value	value in pixel units
   * @return		value in current selection units
   */
  public String pixelDistanceToDisplayString(double value) {
    switch ( getCurrentUnits() ) {
    case CNUDisplay.PIXELS:
      break;
    case CNUDisplay.CM:
      value *= 2.54;
      value /= getCorrectedResolution();
      break;
    case CNUDisplay.INCHES:
      value /= getCorrectedResolution();
      break;
    }
    return Double.toString(value);
  }
  /**
   * Converts a string in current selected units to a pixel distance.
   *
   * @param strValue	input string value
   * @return		value in pixel distances
   * @exception	NumberFormatException thrown on invalid number string
   */
  public double displayStringToPixelDistance(String strValue)
    throws NumberFormatException {
    double value = Double.valueOf(strValue).doubleValue();
    switch ( getCurrentUnits() ) {
    case CNUDisplay.PIXELS:
      break;
    case CNUDisplay.CM:
      value *= getCorrectedResolution();
      value /= 2.54;
      break;
    case CNUDisplay.INCHES:
      value *= getCorrectedResolution();
      break;
    }
    return value;
  }
  /**
   * Gets the current display units.
   *
   * @return	units
   */
  public int getCurrentUnits() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = new Integer(currentUnits); }
      };
      runWithReturn.invokeAndWait();
      return ((Integer) runWithReturn.returnObject).intValue();
    }
    else { return currentUnits; }
  }
  /**
   * Gets the grid spacing from the text field.
   *
   * @return	grid spacing
   */
  public double getGridSpacing() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = new Double(getGridSpacing()); }
      };
      runWithReturn.invokeAndWait();
      return ((Double) runWithReturn.returnObject).doubleValue();
    }
    else {
      try {
        gridSpacing = displayStringToPixelDistance(gridSpacingTF.getText());
      }  catch (NumberFormatException e3) {
        Toolkit.getDefaultToolkit().beep();
        updateGridSpacingText();
      }
      return gridSpacing;
    }
  }
  /**
   * Gets the grid offset x from the text field.
   *
   * @return grid offset x
   */
  public double getGridOffsetX() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = new Double(getGridOffsetX()); }
      };
      runWithReturn.invokeAndWait();
      return ((Double) runWithReturn.returnObject).doubleValue();
    }
    else {
      try {
        gridOffsetX = displayStringToPixelDistance(gridOffsetX_TF.getText());
      }  catch (NumberFormatException e3) {
        Toolkit.getDefaultToolkit().beep();
        updateGridOffsetXText();
      }
      return gridOffsetX;
    }
  }
  /**
   * Gets the grid offset y from the text field.
   *
   * @return	grid offset y
   */
  public double getGridOffsetY() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = new Double(getGridOffsetY()); }
      };
      runWithReturn.invokeAndWait();
      return ((Double) runWithReturn.returnObject).doubleValue();
    }
    else {
      try {
        gridOffsetY = displayStringToPixelDistance(gridOffsetY_TF.getText());
      }  catch (NumberFormatException e3) {
        Toolkit.getDefaultToolkit().beep();
        updateGridOffsetYText();
      }
      return gridOffsetY;
    }
  }
  /**
   * Gets the paper width from the text field.
   *
   * @return paper width
   */
  public double getPaperWidth() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = new Double(getPaperWidth()); }
      };
      runWithReturn.invokeAndWait();
      return ((Double) runWithReturn.returnObject).doubleValue();
    }
    else {
      try {
        paperWidth = displayStringToPixelDistance(paperWidthTF.getText());
      }  catch (NumberFormatException e3) {
        Toolkit.getDefaultToolkit().beep();
        updatePaperWidthText();
      }
      return paperWidth;
    }
  }
  /**
   * Gets the paper height from the text field.
   *
   * @return	paper height
   */
  public double getPaperHeight() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = new Double(getPaperHeight()); }
      };
      runWithReturn.invokeAndWait();
      return ((Double) runWithReturn.returnObject).doubleValue();
    }
    else {
      try {
        paperHeight = displayStringToPixelDistance(paperHeightTF.getText());
      }  catch (NumberFormatException e3) {
        Toolkit.getDefaultToolkit().beep();
        updatePaperHeightText();
      }
      return paperHeight;
    }
  }
  /**
   * Gets the paper offset x  from the text field.
   *
   * @return	paper offset x
   */
  public double getPaperOffsetX() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = new Double(getPaperOffsetX()); }
      };
      runWithReturn.invokeAndWait();
      return ((Double) runWithReturn.returnObject).doubleValue();
    }
    else {
      try {
        paperOffsetX = displayStringToPixelDistance(paperOffsetX_TF.getText());
      }  catch (NumberFormatException e3) {
        Toolkit.getDefaultToolkit().beep();
        updatePaperOffsetXText();
      }
      return paperOffsetX;
    }
  }
  /**
   * Gets the paper offset y from the text field.
   *
   * @return paper offset y
   */
  public double getPaperOffsetY() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = new Double(getPaperOffsetY()); }
      };
      runWithReturn.invokeAndWait();
      return ((Double) runWithReturn.returnObject).doubleValue();
    }
    else {
      try {
        paperOffsetY = displayStringToPixelDistance(paperOffsetY_TF.getText());
      }  catch (NumberFormatException e3) {
        Toolkit.getDefaultToolkit().beep();
        updatePaperOffsetYText();
      }
      return paperOffsetY;
    }
  }
  /**
   * Updates screenResCorrection displayed text.
   * Should only be called from event dispatch thread.
   */
  protected void updateScreenResCorrectionText() {
    screenResCorrectionTF.setText(Double.toString(screenResCorrection));
  }
  /**
   * Updates the grid spacing displayed text.
   * Should only be called from event dispatch thread.
   */
  protected void updateGridSpacingText() {
    gridSpacingTF.setText(pixelDistanceToDisplayString(gridSpacing));
  }
  /**
   * Updates the grid offset x displayed text.
   * Should only be called from event dispatch thread.
   */
  protected void updateGridOffsetXText() {
    gridOffsetX_TF.setText(pixelDistanceToDisplayString(gridOffsetX));
  }
  /**
   * Updates the grid offset y displayed text.
   * Should only be called from event dispatch thread.
   */
  protected void updateGridOffsetYText() {
    gridOffsetY_TF.setText(pixelDistanceToDisplayString(gridOffsetY));
  }
  /**
   * Update the paper width displayed text.
   * Should only be called from event dispatch thread.
   */
  protected void updatePaperWidthText() {
    paperWidthTF.setText(pixelDistanceToDisplayString(paperWidth));
  }
  /**
   * Updates the paper heigth displayed text.
   * Should only be called from event dispatch thread.
   */
  protected void updatePaperHeightText() {
    double paperHeightShow = paperHeight;
    paperHeightTF.setText(pixelDistanceToDisplayString(paperHeight));
  }
  /**
   * Updates the paper offset x displayed text.
   * Should only be called from event dispatch thread.
   */
  protected void updatePaperOffsetXText() {
    double paperOffsetX_Show = paperOffsetX;
    paperOffsetX_TF.setText(pixelDistanceToDisplayString(paperOffsetX));
  }
  /**
   * Updates the paper offset y displayed text.
   * Should only be called from event dispatch thread.
   */
  protected void updatePaperOffsetYText() {
    paperOffsetY_TF.setText(pixelDistanceToDisplayString(paperOffsetY));
  }
  /**
   * Updates the displayed all the text fields.
   */
  public void updateText() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { updateText(); }
      } );
    }
    else {
      updateScreenResCorrectionText();
      updateGridSpacingText();
      updateGridOffsetXText();
      updateGridOffsetYText();
      updatePaperWidthText();
      updatePaperHeightText();
      updatePaperOffsetXText();
      updatePaperOffsetYText();
    }
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
    setToCurrent();
  }
  /**
   * Sets values to current CNUDisplay settings.
   */
  public void setToCurrent() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setToCurrent(); }
      } );
    }
    else {
      CNUDisplay cnud = getCNUDisplay();
      screenResCorrection = cnud.getScreenResCorrection();
      gridSpacing = cnud.getGridSpacing();
      gridOffsetX = cnud.getGridOffsetX();
      gridOffsetY = cnud.getGridOffsetY();
      paperWidth = cnud.getPaperWidth();
      paperHeight = cnud.getPaperHeight();
      paperOffsetX = cnud.getPaperOffsetX();
      paperOffsetY = cnud.getPaperOffsetY();
//      currentUnits = unitValues[unitsCH.getSelectedIndex()];
      updateText();
      // get and set current grid color
      Color c = cnud.getGridColor();
      for( int i=0; i < colorValues.length; i++) {
        if(colorValues[i] == c) {
          gridColorCH.setSelectedIndex(i);
          break;
        }
      }
      c = cnud.getPaperColor();
      for( int i=0; i < colorValues.length; i++) {
        if(colorValues[i] == c) {
          paperColorCH.setSelectedIndex(i);
          break;
        }
      }
      gridCB.setSelected(cnud.getGridState());
      paperCB.setSelected(cnud.getPaperState());
    }
  }
  /**
   * Applies the settings to the display.
   */
  public void apply() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { apply(); }
      } );
    }
    else {
      CNUDisplay cnud = getCNUDisplay();
      UndoRedo ur = getCNUViewer().getUndoRedo();
      ur.startSteps();
      cnud.setScreenResCorrection(getScreenResCorrection());
      cnud.setGridSpacing(getGridSpacing(), CNUDisplay.PIXELS);
      cnud.setGridOffset(getGridOffsetX(), getGridOffsetY(),
		         CNUDisplay.PIXELS);
      cnud.setPaperSize(getPaperWidth(), getPaperHeight(),
		        CNUDisplay.PIXELS);
      cnud.setPaperOffset(getPaperOffsetX(), getPaperOffsetY(),
		          CNUDisplay.PIXELS);
      cnud.setGridColor(colorValues[gridColorCH.getSelectedIndex()]);
      cnud.setPaperColor(colorValues[paperColorCH.getSelectedIndex()]);
      cnud.setGridState(gridCB.isSelected());
      cnud.setPaperState(paperCB.isSelected());
      ur.finishUndoSteps("grid/paper change");
    }
  }
  /**
   * Interprets mouse events over this dialog.
   *
   * @param e	action event
   */
  public void actionPerformed(ActionEvent e){
    getCNUViewer().setWaitCursor();
    try {
      CNUDisplay cnud = getCNUDisplay();
      Object source = e.getSource();
      if(source == applyB) apply();
      else if(source == snapTopB) cnud.apply(CNUDisplay.SNAP_TOP);
      else if(source == snapBottomB) cnud.apply(CNUDisplay.SNAP_BOTTOM);
      else if(source == snapLeftB) cnud.apply(CNUDisplay.SNAP_LEFT);
      else if(source == snapRightB) cnud.apply(CNUDisplay.SNAP_RIGHT);
      else if(source == setToCurrentB) setToCurrent();
      else if(source == dismissB) setVisible(false);
    } finally {
      getCNUViewer().setNormalCursor();
    }
  }
  /**
   * Handles ItemListener events.
   *
   * @param e	item event
   */
  public void itemStateChanged(ItemEvent e){
    getCNUViewer().setWaitCursor();
    try {
      if (e.getSource() == unitsCH) {
	// read text fields with old units
	getGridSpacing();
	getGridOffsetX();
	getGridOffsetY();
	getPaperWidth();
	getPaperHeight();
	getPaperOffsetX();
	getPaperOffsetY();
	// set new units
        currentUnits = unitValues[unitsCH.getSelectedIndex()];
	// update text fields with new units
        updateText();
      }
    } finally {
      getCNUViewer().setNormalCursor();
    }
  }
}
