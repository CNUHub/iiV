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
 * Dialog to allow user to display shapes.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUDialog
 * @see		CNUViewer
 * @since	iiV1.1
 */
public class ShapeDialog extends CNUDialog
implements ActionListener, ItemListener {
  private static final long serialVersionUID = -6963406862957870457L;
  String thicknessChoices[] = {
    "1 pixel thick",
    "2 pixels thick",
    "3 pixels thick",
    "4 pixels thick",
    "5 pixels thick",
    "6 pixels thick",
    "7 pixels thick",
    "8 pixels thick" };
  JComboBox thicknessCH = new JComboBox(thicknessChoices);
    
  String continuityChoices[] = { "Solid lines","Dashed lines" };
  JComboBox continuityCH = new JComboBox(continuityChoices);
  JCheckBox fillCB = new JCheckBox("Fill", false);
  ButtonGroup shapeBG = new ButtonGroup();
  JCheckBox lineCB = new JCheckBox("Line", true);
  JCheckBox leftArrowCB = new JCheckBox("Left arrow", false);
  JCheckBox rightArrowCB = new JCheckBox("Right arrow", false);
  JCheckBox ovalCB = new JCheckBox("Oval", false);
  JCheckBox boxCB = new JCheckBox("Box", false);
  JLabel dashLengthL = new JLabel("Dash Length (pixels):");
  JTextField dashLengthTF = new JTextField(3);
  JLabel dashSpaceL = new JLabel("Dash Space (pixels):");
  JTextField dashSpaceTF = new JTextField(3);
  JLabel lineLengthL = new JLabel("Line Length(pixels):");
  JTextField lineLengthTF = new JTextField(3);
  JLabel arrowLengthL = new JLabel("Arrow Length (pixels):");
  JTextField arrowLengthTF = new JTextField(3);
  JLabel arrowWidthL = new JLabel("Arrow Width (pixels):");
  JTextField arrowWidthTF = new JTextField(3);
  JLabel boxWidthL = new JLabel("Width (pixels):");
  JTextField boxWidthTF = new JTextField(3);
  JLabel boxHeightL = new JLabel("Heigth (pixels):");
  JTextField boxHeightTF = new JTextField(3);
  JComboBox colorCH = new JComboBox(colorNames);
  JButton applyColorB = new JButton("apply color");
  JButton addShapeB = new JButton("add shape");
  JButton setToCurrentB = new JButton("set to current");
  JButton dismissB = new JButton("dismiss");

  private Object lastLock = new Object();
  private int lastDashLength = DisplayShape.getDefaultDashLength();
  private int lastDashSpace = DisplayShape.getDefaultDashSpace();
  private int lastLineLength = DisplayLine.getDefaultLineLength();
  private int lastArrowLength = DisplayLine.getDefaultArrowLength();
  private int lastArrowWidth = DisplayLine.getDefaultArrowWidth();
  private int lastBoxWidth = 50;
  private int lastBoxHeight = 30;

  /**
   * Constructs a new instance of ShapeDialog.
   *
   * @param parentFrame	parent frame needed for building a window
   */
  public ShapeDialog(Frame parentFrame) {
    this(parentFrame, null);
  }
  /**
   * Constructs a new instance of ShapeDialog.
   *
   * @param parentFrame	parent frame needed for building a window
   * @param cnuv	CNUViewer that this window interacts with
   */
  public ShapeDialog(Frame parentFrame, CNUViewer cnuv) {
    super(parentFrame, "Set Shape", false, cnuv);

    dashLengthTF.setText(Integer.toString(lastDashLength) + "   ");
    dashSpaceTF.setText(Integer.toString(lastDashSpace) + "   ");
    lineLengthTF.setText(Integer.toString(lastLineLength) + "   ");
    arrowLengthTF.setText(Integer.toString(lastArrowLength) + "    ");
    arrowWidthTF.setText(Integer.toString(lastArrowWidth) + "    ");
    boxWidthTF.setText(Integer.toString(lastBoxWidth) + "    ");
    boxHeightTF.setText(Integer.toString(lastBoxHeight) + "    ");

    shapeBG.add(lineCB);
    shapeBG.add(ovalCB);
    shapeBG.add(boxCB);

    Container contentPane = getContentPane();

    GridBagLayout gbl = new GridBagLayout();
    contentPane.setLayout(gbl);
    int col = 0; int row = 0;
    int stdfill = GridBagConstraints.NONE;
    int horzfill = GridBagConstraints.HORIZONTAL;

    addtogridbag(thicknessCH, col, row, 1, 1, stdfill, gbl, contentPane);
    col++;
    addtogridbag(fillCB, col, row, 1, 1, stdfill, gbl, contentPane);
    col--; row++;
    addtogridbag(continuityCH, col, row, 1, 1, stdfill, gbl, contentPane);
    continuityCH.addItemListener(this);
    col++;
    addtogridbag(dashLengthL, col, row, 1, 1, stdfill, gbl, contentPane);
    col++;
    addtogridbag(dashLengthTF, col, row, 1, 1, horzfill, gbl, contentPane);
    dashLengthL.setEnabled(false);
    dashLengthTF.setEnabled(false);
    col--; row++;
    addtogridbag(dashSpaceL, col, row, 1, 1, stdfill, gbl, contentPane);
    col++;
    addtogridbag(dashSpaceTF, col, row, 1, 1, horzfill, gbl, contentPane);
    dashSpaceL.setEnabled(false);
    dashSpaceTF.setEnabled(false);
    col -= 2; row++;

    addtogridbag(lineCB, col, row, 1, 1, stdfill, gbl, contentPane);
    lineCB.addItemListener(this);
    col++;
    addtogridbag(lineLengthL, col, row, 1, 1, stdfill, gbl, contentPane);
    col++;
    addtogridbag(lineLengthTF, col, row, 1, 1, horzfill, gbl, contentPane);
    col--; row++;

    addtogridbag(leftArrowCB, col, row, 1, 1, stdfill, gbl, contentPane);
    leftArrowCB.addItemListener(this);
    col++;
    addtogridbag(rightArrowCB, col, row, 1, 1, stdfill, gbl, contentPane);
    rightArrowCB.addItemListener(this);
    col --; row++;

    addtogridbag(arrowLengthL, col, row, 1, 1, stdfill, gbl, contentPane);
    col++;
    addtogridbag(arrowLengthTF, col, row, 1, 1, horzfill, gbl, contentPane);
    arrowLengthTF.setEnabled(false);
    arrowLengthL.setEnabled(false);
    col--; row++;

    addtogridbag(arrowWidthL, col, row, 1, 1, stdfill, gbl, contentPane);
    col++;
    addtogridbag(arrowWidthTF, col, row, 1, 1, horzfill, gbl, contentPane);
    arrowWidthTF.setEnabled(false);
    arrowWidthL.setEnabled(false);
    col -= 2; row++;

    addtogridbag(boxCB, col, row, 1, 1, stdfill, gbl, contentPane);
    boxCB.addItemListener(this);
    col++;
    addtogridbag(boxWidthL, col, row, 1, 1, stdfill, gbl, contentPane);
    col++;
    addtogridbag(boxWidthTF, col, row, 1, 1, horzfill, gbl, contentPane);
    boxWidthTF.setEnabled(false);
    boxWidthL.setEnabled(false);
    col -= 2; row++;

    addtogridbag(ovalCB, col, row, 1, 1, stdfill, gbl, contentPane);
    ovalCB.addItemListener(this);
    col++;
    addtogridbag(boxHeightL, col, row, 1, 1, stdfill, gbl, contentPane);
    col++;
    addtogridbag(boxHeightTF, col, row, 1, 1, horzfill, gbl, contentPane);
    boxHeightTF.setEnabled(false);
    boxHeightL.setEnabled(false);
    col -= 2; row++;

    addtogridbag(colorCH, col, row, 1, 1, stdfill, gbl, contentPane);
    col++;
    addtogridbag(applyColorB, col, row, 1, 1, stdfill, gbl, contentPane);
    applyColorB.addActionListener(this);
    col--; row++;

    addtogridbag(addShapeB, col, row, 1, 1, stdfill, gbl, contentPane);
    addShapeB.addActionListener(this);
    col++;

    addtogridbag(setToCurrentB, col, row, 1, 1, stdfill, gbl, contentPane);
    setToCurrentB.addActionListener(this);
    col++;

    addtogridbag(dismissB, col, row, 1, 1, stdfill, gbl, contentPane);
    dismissB.addActionListener(this);

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
    // normally only called after creation so do standard
    // creation init
    setToCurrent();
  }
  /**
   * Called when an item event occurs.
   *
   * @param e	item event
   */
  public void itemStateChanged(ItemEvent e) { updateEnabledStates(); }
  /**
   * Updates item enabled states.
   */
  public void updateEnabledStates() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { updateEnabledStates(); }
      } );
      return;
    }
    // enable fields according to current selected items
    CNUDisplay cnud = getCNUDisplay();
    boolean mode = lineCB.isSelected();
    leftArrowCB.setEnabled(mode);
    rightArrowCB.setEnabled(mode);
    lineLengthL.setEnabled(mode);
    lineLengthTF.setEnabled(mode);

    mode &= (leftArrowCB.isSelected() || rightArrowCB.isSelected());
    arrowLengthTF.setEnabled(mode);
    arrowLengthL.setEnabled(mode);
    arrowWidthTF.setEnabled(mode);
    arrowWidthL.setEnabled(mode);

    mode = boxCB.isSelected() || ovalCB.isSelected();
    boxWidthTF.setEnabled(mode);
    boxWidthL.setEnabled(mode);
    boxHeightTF.setEnabled(mode);
    boxHeightL.setEnabled(mode);

    mode = "Dashed lines".equals(continuityCH.getSelectedItem());
    dashLengthL.setEnabled(mode);
    dashLengthTF.setEnabled(mode);
    dashSpaceL.setEnabled(mode);
    dashSpaceTF.setEnabled(mode);
  }
  /**
   * Sets everything to current values.
   */
  public void setToCurrent() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setToCurrent(); }
      } );
      return;
    }
    Component comp = getCNUDisplay().getCurrentComponent();
    if(comp instanceof DisplayShape) {
      DisplayShape ds = (DisplayShape) comp;
      int thicknessIndex = ds.getLineThickness() - 1;
      if(thicknessIndex < thicknessCH.getItemCount()) 
        thicknessCH.setSelectedIndex(thicknessIndex);
      // get and set current color
      Color c = ds.getForeground();
      for( int i=0; i < colorValues.length; i++) {
        if(colorValues[i] == c) {
	  colorCH.setSelectedIndex(i);
	  break;
        }
      }
      if(ds.getLineType() == DisplayShape.DASH) {
	continuityCH.setSelectedIndex(1);
	setDashLength(ds.getDashLength());
	setDashSpace(ds.getDashSpace()); 
      }
      else continuityCH.setSelectedIndex(0);
      boolean mode = ds.getFillMode();
      if(fillCB.isSelected() != mode) fillCB.setSelected(mode);
    }
    if(comp instanceof DisplayLine) {
      if(! lineCB.isSelected()) lineCB.setSelected(true);
      DisplayLine dl = (DisplayLine) comp;
      setLineLength(dl.getLineLength());
      boolean arrow = false;
      if(dl.getRightArrow() != DisplayShape.NONE) {
	if(! rightArrowCB.isSelected()) rightArrowCB.setSelected(true);
	arrow = true;
      }
      else if(rightArrowCB.isSelected()) rightArrowCB.setSelected(false);
      if(dl.getLeftArrow() != DisplayShape.NONE) {
	if(! leftArrowCB.isSelected()) leftArrowCB.setSelected(true);
	arrow = true;
      }
      else if(leftArrowCB.isSelected()) leftArrowCB.setSelected(false);
      if(arrow) {
	setArrowLength(dl.getArrowLength());
	setArrowWidth(dl.getArrowWidth());
      }
    }
    else if(comp instanceof DisplayBox) {
      if(! boxCB.isSelected()) boxCB.setSelected(true);
      DisplayBox db = (DisplayBox) comp;
      setBoxWidth(db.getBoxWidth());
      setBoxHeight(db.getBoxHeight());
    }
    else if(comp instanceof DisplayOval) {
      if(! ovalCB.isSelected()) ovalCB.setSelected(true);
      DisplayOval dOval = (DisplayOval) comp;
      setBoxWidth(dOval.getOvalWidth());
      setBoxHeight(dOval.getOvalHeight());
    }
    updateEnabledStates();
  }
  /**
   * Sets the current line length.
   *
   * @param length	length in pixels
   */
  public void setLineLength(final int length) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setLineLength(length); }
      } );
    }
    else {
      if(length >= 1) lastLineLength = length;
      else Toolkit.getDefaultToolkit().beep();
      lineLengthTF.setText(Integer.toString(lastLineLength));
    }
  }
  /**
   * Gets the current line length.
   *
   * @return	length in pixels
   */
  public int getLineLength()  throws NumberFormatException {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = new Integer(getLineLength()); }
      };
      runWithReturn.invokeAndWait();
      return ((Integer) runWithReturn.returnObject).intValue();
    }
    else {
      try {
        setLineLength(Integer.parseInt(lineLengthTF.getText().trim()));
      } catch (NumberFormatException e) {
        setLineLength(lastLineLength);
        Toolkit.getDefaultToolkit().beep();
	throw e;
      }
      return lastLineLength;
    }
  }
  /**
   * Sets the current arrow length.
   *
   * @param length	length in pixels
   */
  public void setArrowLength(final int length) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setArrowLength(length); }
      } );
    }
    else {
      if(length >= 1) lastArrowLength = length;
      else Toolkit.getDefaultToolkit().beep();
      arrowLengthTF.setText(Integer.toString(lastArrowLength));
    }
  }
  /**
   * Gets the current arrow length.
   *
   * @return	length in pixels
   */
  public int getArrowLength()  throws NumberFormatException {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = new Integer(getArrowLength()); }
      };
      runWithReturn.invokeAndWait();
      return ((Integer) runWithReturn.returnObject).intValue();
    }
    else {
      try {
        setArrowLength(Integer.parseInt(arrowLengthTF.getText().trim()));
      } catch (NumberFormatException e) {
        setArrowLength(lastArrowLength);
        Toolkit.getDefaultToolkit().beep();
	throw e;
      }
      return lastArrowLength;
    }
  }
  /**
   * Sets the current arrow width.
   *
   * @param width	width in pixels
   */
  public void setArrowWidth(final int width) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setArrowWidth(width); }
      } );
    }
    else {
      if(width >= 1) lastArrowWidth = width;
      else Toolkit.getDefaultToolkit().beep();
      arrowWidthTF.setText(Integer.toString(lastArrowWidth));
    } 
  }
  /**
   * Gets the current arrow width.
   *
   * @return	width in pixels
   */
  public int getArrowWidth()  throws NumberFormatException {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = new Integer(getArrowWidth()); }
      };
      runWithReturn.invokeAndWait();
      return ((Integer) runWithReturn.returnObject).intValue();
    }
    else {
      try {
        setArrowWidth(Integer.parseInt(arrowWidthTF.getText().trim()));
      } catch (NumberFormatException e) {
        Toolkit.getDefaultToolkit().beep();
	setArrowWidth(lastArrowWidth);
	throw e;
      }
      return lastArrowWidth;
    }
  }
  /**
   * Sets the current dash length.
   *
   * @param length	length in pixels
   */
  public void setDashLength(final int length) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setDashLength(length); }
      } );
    }
    else {
      if(length >= 1) lastDashLength = length;
      else Toolkit.getDefaultToolkit().beep();
      dashLengthTF.setText(Integer.toString(lastDashLength));
    }
  }
  /**
   * Gets the current dash length.
   *
   * @return	length in pixels
   */
  public int getDashLength() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = new Integer(getDashLength()); }
      };
      runWithReturn.invokeAndWait();
      return ((Integer) runWithReturn.returnObject).intValue();
    }
    else {
      try {
        setDashLength(Integer.parseInt(dashLengthTF.getText().trim()));
      } catch (NumberFormatException e) {
        setDashLength(lastDashLength);
        Toolkit.getDefaultToolkit().beep();
      }
      return lastDashLength;
    }
  }
  /**
   * Sets the current dash space.
   *
   * @param length	length in pixels
   */
  public void setDashSpace(final int length) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setDashSpace(length); }
      } );
    }
    else {
      if(length >= 1) lastDashSpace = length;
      else Toolkit.getDefaultToolkit().beep();
      dashSpaceTF.setText(Integer.toString(lastDashSpace));
    }
  }
  /**
   * Gets the current dash space.
   *
   * @return	length in pixels
   */
  public int getDashSpace() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = new Integer(getDashSpace()); }
      };
      runWithReturn.invokeAndWait();
      return ((Integer) runWithReturn.returnObject).intValue();
    }
    else {
      try {
        setDashSpace(Integer.parseInt(dashSpaceTF.getText().trim()));
      } catch (NumberFormatException e) {
        setDashSpace(lastDashSpace);
        Toolkit.getDefaultToolkit().beep();
      }
      return lastDashSpace;
    }
  }
  /**
   * Sets the current box width.
   *
   * @param width	width in pixels
   */
  public void setBoxWidth(final int width) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setBoxWidth(width); }
      } );
    }
    else {
      if(width >= 1) lastBoxWidth = width;
      else Toolkit.getDefaultToolkit().beep();
      boxWidthTF.setText(Integer.toString(lastBoxWidth));
    }
  }
  /**
   * Gets the current box width.
   *
   * @return	width in pixels
   */
  public int getBoxWidth() throws NumberFormatException {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = new Integer(getBoxWidth()); }
      };
      runWithReturn.invokeAndWait();
      return ((Integer) runWithReturn.returnObject).intValue();
    }
    else {
      try {
        setBoxWidth(Integer.parseInt(boxWidthTF.getText().trim()));
      } catch (NumberFormatException e) {
        setBoxWidth(lastBoxWidth);
        Toolkit.getDefaultToolkit().beep();
	throw e;
      }
      return lastBoxWidth;
    }
  }
  /**
   * Sets the current box height.
   *
   * @param height	height in pixels
   */
  public void setBoxHeight(final int height) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setBoxHeight(height); }
      } );
    }
    else {
      if(height >= 1) lastBoxHeight = height;
      else Toolkit.getDefaultToolkit().beep();
      boxHeightTF.setText(Integer.toString(lastBoxHeight));
    }
  }
  /**
   * Gets the current box height.
   *
   * @return	height in pixels
   */
  public int getBoxHeight() throws NumberFormatException {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = new Integer(getBoxHeight()); }
      };
      runWithReturn.invokeAndWait();
      return ((Integer) runWithReturn.returnObject).intValue();
    }
    else {
      try {
        setBoxHeight(Integer.parseInt(boxHeightTF.getText().trim()));
      } catch (NumberFormatException e) {
        setBoxHeight(lastBoxHeight);
        Toolkit.getDefaultToolkit().beep();
	throw e;
      }
      return lastBoxHeight;
    }
  }
  /**
   * Adds a line to the CNUDisplay based on current settings.
   */
  public void addALine() {
    DisplayLine dl;
    try {
      dl = new DisplayLine(getLineLength());
      dl.setArrowLength(getArrowLength());
      dl.setArrowWidth(getArrowWidth());
      if(leftArrowCB.isSelected()) dl.setLeftArrow(DisplayLine.SIMPLE_ARROW);
      if(rightArrowCB.isSelected()) dl.setRightArrow(DisplayLine.SIMPLE_ARROW);
      dl.setLineThickness(thicknessCH.getSelectedIndex() + 1);
      dl.setFillMode(fillCB.isSelected());
      if(continuityCH.getSelectedIndex() == 1) {
        dl.setLineType(DisplayLine.DASH);
        dl.setDashLength(getDashLength());
        dl.setDashSpace(getDashSpace());
      }
      else dl.setLineType(DisplayLine.SOLID);
      dl.setForeground( colorValues[colorCH.getSelectedIndex()] );
      getCNUDisplay().addAndRepaint(dl);
    } catch (NumberFormatException e) {
      return;
    }
  }
  /**
   * Adds a box to the CNUDisplay based on current settings.
   */
  public void addABox() {
    DisplayBox db;
    try {
      db = new DisplayBox(getBoxWidth(), getBoxHeight());
      db.setLineThickness(thicknessCH.getSelectedIndex() + 1);
      db.setFillMode(fillCB.isSelected());
      if(continuityCH.getSelectedIndex() == 1) {
        db.setLineType(DisplayLine.DASH);
        db.setDashLength(getDashLength());
        db.setDashSpace(getDashSpace());
      }
      else db.setLineType(DisplayLine.SOLID);
      db.setForeground( colorValues[colorCH.getSelectedIndex()] );
      getCNUDisplay().addAndRepaint(db);
    } catch (NumberFormatException e1) {
      // do nothing
    }
  }
  /**
   * Add an oval to the CNUDisplay based on current settings.
   */
  public void addAnOval() {
    DisplayOval dOval;
    try {
      dOval = new DisplayOval(getBoxWidth(), getBoxHeight());
      dOval.setLineThickness(thicknessCH.getSelectedIndex() + 1);
      dOval.setFillMode(fillCB.isSelected());
      dOval.setForeground( colorValues[colorCH.getSelectedIndex()] );
      if(continuityCH.getSelectedIndex() == 1) {
        dOval.setLineType(DisplayLine.DASH);
        dOval.setDashLength(getDashLength());
        dOval.setDashSpace(getDashSpace());
      }
      else dOval.setLineType(DisplayLine.SOLID);
      getCNUDisplay().addAndRepaint(dOval);
    } catch (NumberFormatException e1) {
      // do nothing
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
      if(source == addShapeB) {
	if(lineCB.isSelected()) addALine();
	else if(boxCB.isSelected()) addABox();
	else if(ovalCB.isSelected()) addAnOval();
      }
      else if(source == applyColorB) {
        getCNUDisplay().updateForegroundColor(colorValues[colorCH.getSelectedIndex()]);
      }
      else if(source == setToCurrentB) setToCurrent();
      else if(source == continuityCH) updateEnabledStates();
      else if(source == dismissB) {
        this.setVisible(false);
      }
    } finally {
      getCNUViewer().setNormalCursor();
    }
  }
}
