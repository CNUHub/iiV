package iiv.dialog;
import iiv.*;
import iiv.util.*;
import iiv.gui.*;
import iiv.display.*;
import iiv.io.*;
import iiv.data.*;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.*;
import java.lang.*;
import java.net.*;

/**
 * Dialog to interactively edit color maps.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUViewer
 * @since	iiV1.1
 */
public class EditColorDialog extends CNUDialog
implements ActionListener, ItemListener, MouseListener
{
  private static final long serialVersionUID = -6022610835667523171L;
  private JButton ASetB = new JButton("Set Color A Index");

  private DefaultBoundedRangeModel AIndexModel =
    new DefaultBoundedRangeModel(0, 0, 0, 255);
  private TextAndSlider AIndexSlider =
    new TextAndSlider("A Index:  ", 4, AIndexModel);

  private DefaultBoundedRangeModel ARedModel =
    new DefaultBoundedRangeModel(0, 0, 0, 255);
  private TextAndSlider ARedSlider =
    new TextAndSlider("Red Intensity:  ", 4, ARedModel);
  private DefaultBoundedRangeModel AGreenModel =
    new DefaultBoundedRangeModel(0, 0, 0, 255);
  private TextAndSlider AGreenSlider =
    new TextAndSlider("Green Intensity:  ", 4, AGreenModel);
  private DefaultBoundedRangeModel ABlueModel =
    new DefaultBoundedRangeModel(0, 0, 0, 255);
  private TextAndSlider ABlueSlider =
    new TextAndSlider("Blue Intensity:  ", 4, ABlueModel);


  private DefaultBoundedRangeModel AAlphaModel =
    new DefaultBoundedRangeModel(0, 0, 0, 255);
  private TextAndSlider AAlphaSlider =
    new TextAndSlider("Alpha value:  ", 4, AAlphaModel);

  private JButton BSetB = new JButton("Set Color B Index");
  private DefaultBoundedRangeModel BIndexModel =
    new DefaultBoundedRangeModel(0, 0, 0, 255);
  private TextAndSlider BIndexSlider =
    new TextAndSlider("B Index:  ", 4, BIndexModel);

  private DefaultBoundedRangeModel BRedModel =
    new DefaultBoundedRangeModel(0, 0, 0, 255);
  private TextAndSlider BRedSlider =
    new TextAndSlider("Red Intensity:  ", 4, BRedModel);
  private DefaultBoundedRangeModel BGreenModel =
    new DefaultBoundedRangeModel(0, 0, 0, 255);
  private TextAndSlider BGreenSlider =
    new TextAndSlider("Green Intensity:  ", 4, BGreenModel);
  private DefaultBoundedRangeModel BBlueModel =
    new DefaultBoundedRangeModel(0, 0, 0, 255);
  private TextAndSlider BBlueSlider =
    new TextAndSlider("Blue Intensity:  ", 4, BBlueModel);

  private DefaultBoundedRangeModel BAlphaModel =
    new DefaultBoundedRangeModel(0, 0, 0, 255);
  private TextAndSlider BAlphaSlider =
    new TextAndSlider("Alpha value:  ", 4, BAlphaModel);

  private JButton interpolateB = new JButton("Interpolate A to B");
  private JButton incrementB = new JButton("Increment A to B");
  private JButton decrementB = new JButton("Decrement A to B");
  private JCheckBox redCB = new JCheckBox("Reds", true);
  private JCheckBox greenCB = new JCheckBox("Greens", true);
  private JCheckBox blueCB = new JCheckBox("Blues", true);
  private JCheckBox alphaCB = new JCheckBox("Alphas", false);
  private JCheckBox enableAlphaCB = new JCheckBox("Enable Alphas", false);
  private JCheckBox ATransparentCB = new JCheckBox("Transparent", false);
  private JCheckBox BTransparentCB = new JCheckBox("Transparent", false);

  private JButton acceptB = new JButton("Accept");
  private JButton saveB = new JButton("Save");

  public final static String[] saveTypes = { "Script", "Analyze" };
  private JComboBox saveTypeCH = new JComboBox(saveTypes);
  private JButton dismissB = new JButton("Dismiss");

  private Object dialogCreateLock = new Object();
  private ColorDialog colorD = null;
  private QueryDialog SQD = null;

  private UndoRedo undoRedoObj = null;

  private ColorMapQuiltCanvas lkup = null;
  private DisplayColorMapQuilt dcmq = null;
  private Object colorModelLock = new Object();
  private ColorModel cm = null;
  private int indexA = 255;
  private int indexB = 0;
  private Rectangle indexAbox = null;
  private Rectangle indexBbox = null;
  private int editnumber = 1;

  /**
   * Constructs a new instance of EditColorDialog.
   *
   * @param parentFrame	parent frame needed for building a window
   */
  public EditColorDialog(Frame parentFrame) {
    this(parentFrame, null, null);
  }
  /**
   * Constructs a new instance of EditColorDialog.
   *
   * @param parentFrame	parent frame needed for building a window
   * @param cnuv	CNUViewer that this window interacts with
   * @param cm		color model to initialize with
   */
  public EditColorDialog(Frame parentFrame, CNUViewer cnuv, ColorModel cm) {
    super(parentFrame, "Edit Color", false, cnuv);

    AAlphaSlider.setVisible(false);
    BAlphaSlider.setVisible(false);
    alphaCB.setEnabled(false);
    if(cm != null) noUndoSetColorModel(cm);
    else noUndoSetColorModel(CNUColorModel.getGreyColorModel());
    setIndexA(getIndexA());
    setIndexB(getIndexB());

    Container contentPane = getContentPane();

    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
    contentPane.add(Box.createHorizontalStrut(5));

    lkup = new ColorMapQuiltCanvas(getColorModel());
    lkup.setFont(getFont());

    dcmq = (DisplayColorMapQuilt) lkup.getDisplayComponent();
    contentPane.add(lkup);
    lkup.addMouseListener(this);

    contentPane.add(Box.createHorizontalStrut(5));
    Container rightBox = Box.createVerticalBox();
    contentPane.add(rightBox);
    contentPane.add(Box.createHorizontalStrut(5));
    rightBox.add(Box.createVerticalStrut(5));

    Container topRightBox = new Container() {private static final long serialVersionUID = -3772444254928154191L;};
    topRightBox.setLayout(new GridLayout(0, 2, 4, 3));
    rightBox.add(topRightBox);

    ChangeListener cl = new ChangeListener() {
	public void stateChanged(ChangeEvent e) {
	  Object source = e.getSource();
	  if(source == AIndexModel)
	    setIndexANotScrollbar(AIndexModel.getValue());
	  else if(source == BIndexModel)
	    setIndexBNotScrollbar(BIndexModel.getValue());
	  else if((source == ARedModel) || (source == ABlueModel) ||
		  (source == AGreenModel))
	    setAdjustedColorA(ARedModel.getValue(), AGreenModel.getValue(),
			      ABlueModel.getValue());
	  else if ((source == BRedModel) || (source == BGreenModel) ||
	 	   (source == BBlueModel))
	    setAdjustedColorB(BRedModel.getValue(), BGreenModel.getValue(),
			      BBlueModel.getValue());

	}
      };

    topRightBox.add(ASetB);
    ASetB.addActionListener(this);
    ASetB.setForeground(Color.red);
    topRightBox.add(BSetB);
    BSetB.addActionListener(this);
    BSetB.setForeground(Color.blue);

    topRightBox.add(AIndexSlider);
    AIndexModel.addChangeListener(cl);
    topRightBox.add(BIndexSlider);
    BIndexModel.addChangeListener(cl);

    topRightBox.add(ARedSlider);
    ARedModel.addChangeListener(cl);
    ARedSlider.setForeground(Color.white);
    topRightBox.add(BRedSlider);
    BRedModel.addChangeListener(cl);
    BRedSlider.setForeground(Color.white);

    topRightBox.add(AGreenSlider);
    AGreenModel.addChangeListener(cl);
    AGreenSlider.setForeground(Color.white);
    topRightBox.add(BGreenSlider);
    BGreenModel.addChangeListener(cl);
    BGreenSlider.setForeground(Color.white);

    topRightBox.add(ABlueSlider);
    ABlueModel.addChangeListener(cl);
    ABlueSlider.setForeground(Color.white);
    topRightBox.add(BBlueSlider);
    BBlueModel.addChangeListener(cl);
    BBlueSlider.setForeground(Color.white);

    Container twoComponent = new Container() {private static final long serialVersionUID = 4257765486245646571L;};
    twoComponent.setLayout(new BorderLayout());
    topRightBox.add(twoComponent);
    twoComponent.add(ATransparentCB, BorderLayout.WEST);
    ATransparentCB.addItemListener(this);
    twoComponent.add(AAlphaSlider, BorderLayout.CENTER);

    twoComponent = new Container() {private static final long serialVersionUID = 4422144891300994100L;};
    twoComponent.setLayout(new BorderLayout());
    topRightBox.add(twoComponent);
    twoComponent.add(BTransparentCB, BorderLayout.WEST);
    BTransparentCB.addItemListener(this);
    twoComponent.add(BAlphaSlider, BorderLayout.CENTER);

    Container bottomRightBox = Box.createVerticalBox();
    rightBox.add(bottomRightBox);
    rightBox.add(Box.createVerticalStrut(5));

    Container rowBox = Box.createHorizontalBox();
    bottomRightBox.add(rowBox);
    rowBox.add(enableAlphaCB);
    enableAlphaCB.addItemListener(this);
    rowBox.add(Box.createHorizontalGlue());

    rowBox = Box.createHorizontalBox();
    bottomRightBox.add(rowBox);
    rowBox.add(new JLabel("Interpolate/Inc/Dec only:  "));
    rowBox.add(redCB);
    rowBox.add(greenCB);
    rowBox.add(blueCB);
    rowBox.add(alphaCB);
    rowBox.add(Box.createHorizontalGlue());


    rowBox = Box.createHorizontalBox();
    bottomRightBox.add(rowBox);
    rowBox.add(interpolateB);
    interpolateB.addActionListener(this);
    rowBox.add(incrementB);
    incrementB.addActionListener(this);
    rowBox.add(decrementB);
    decrementB.addActionListener(this);
    rowBox.add(Box.createHorizontalGlue());

    JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolbar.setFloatable(false);
    bottomRightBox.add(toolbar);
    getUndoRedo().getUndoAction().addTo(toolbar);
    getUndoRedo().getRedoAction().addTo(toolbar);
    getUndoRedo().getUndoEnableAction().addTo(toolbar);
    getUndoRedo().getUndoDisableAction().addTo(toolbar);
    toolbar.add(Box.createHorizontalGlue());

    rowBox = Box.createHorizontalBox();
    bottomRightBox.add(rowBox);
    rowBox.add(acceptB);
    acceptB.addActionListener(this);
    rowBox.add(Box.createHorizontalGlue());
    rowBox.add(saveB);
    saveB.addActionListener(this);
    rowBox.add(saveTypeCH);
    rowBox.add(Box.createHorizontalGlue());
    rowBox.add(dismissB);
    dismissB.addActionListener(this);

    updateAlphaStates();

    if(cnuv != null) {
      if(cnuv.runningAsApplet()) {
	saveB.setVisible(false);
	saveTypeCH.setVisible(false);
      }
      colorD = (ColorDialog) cnuv.getColorDialog();
    }
    CNUDisplay cnud = getCNUDisplay();
    if(cnud != null) {
      cnud.addDisplayBackgroundColorComponent(lkup);
      lkup.setBackground(cnud.getBackground());
    }

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
    boolean savemode = false;
    if(cnuviewer != null) {
      colorD = (ColorDialog) cnuviewer.getColorDialog();
      savemode = ! cnuviewer.runningAsApplet();
      CNUDisplay cnud = getCNUDisplay();
      if(cnud != null) {
	lkup.setBackground(cnud.getBackground());
	cnud.addDisplayBackgroundColorComponent(lkup);
      }
      saveB.setVisible(savemode);
      saveTypeCH.setVisible(savemode);
    }
  }
  /**
   * Gets the SaveQueryDialog to be used with this EditColorDialog.
   *
   * @return	save query dialog
   */
  public QueryDialog getSaveQueryDialog() {
    String answers[] = {"Yes", "No"};
    if(SQD == null) synchronized (dialogCreateLock) {
      if(SQD == null)
        SQD = new QueryDialog(getParentFrame(), "Save Color Model",
		   "Required color model not saved. Save?", answers);
    }
    return SQD;
  }
  /**
   * Sets the lookup background color.
   *
   * @param c	color
   */
  public void setLookupBackground(final Color c) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { lkup.setBackground(c); }
      } );
    }
    else lkup.setBackground(c);
  }
  /**
   * Query before saving color map to a file - currently defeated.
   */
  public void querySaveColorModel(ColorModel cm) {
    /*
    QueryDialog sqd = getSaveQueryDialog();
    sqd.beep();
    sqd.setVisible(true);
    if( sqd.getSelection() < 1) {
      setColorModel(cm);
      saveColorModel(cm);
    }
    */
  }
  /**
   * Saves a color map to a file after getting the filename from the user.
   *
   * @param cm	color model to save
   */
  public void saveColorModel(ColorModel cm) {
    if(! (cm instanceof IndexColorModel)) return;
    String filename=null;
    if(cm instanceof CNUColorModel) filename=((CNUColorModel) cm).getFullName();
    FileDialog SFD;
    CNUViewer cnuv = getCNUViewer();
    if(cnuv != null) SFD = getCNUViewer().getSaveFileDialog(filename);
    else SFD = new FileDialog(getParentFrame(),
			      "Select Save File", FileDialog.SAVE);
    SFD.setVisible(true);
    if(SFD.getFile() != null) {
      try {
	String newFileName = SFD.getDirectory() + SFD.getFile();
	if(cm instanceof IndexColorModel) {
	  if( "Analyze".equals(saveTypeCH.getSelectedItem()) ) {
	    //	    if( ! newFileName.endsWith(".lkup") )
	    //	      newFileName = newFileName + ".lkup";
            if( (((IndexColorModel) cm).getTransparentPixel() >= 0) ||
	        CNUColorModel.alphasNeeded((IndexColorModel) cm) ) {
	      throw new ColorMapException(
		"Unable to save as Analyze Color Map - contains transparent values");
	    }
	    AnalyzeColorMap.WriteAnalyzeColorMap(newFileName,
		getCNUViewer().getContinueDialog(),
		(IndexColorModel) cm);
	  }
	  else {
	    CNUColorModel.WriteColorMapScript(new CNUFile(newFileName),
		getCNUViewer().getContinueDialog(),
		(IndexColorModel) cm);
	  }
	  if(cm instanceof CNUColorModel) {
            CNUColorModel ccm = (CNUColorModel) cm;
	    ccm.setSaved(true);
	    ccm.setCNUFile(newFileName);
	  }
	}
	else throw new ColorMapException("Not IndexColorModel - unable to save");
        colorD.replaceColorFileName(cm);
	colorD.setColorModel(cm);
      } catch (IOException ioe) {
        showStatus("Failed writing colormap to file");
        showStatus(ioe);
      } catch (ColorMapException cme) {
        showStatus("Failed writing colormap to file");
        showStatus(cme);
      }
    }
  }
  /**
   * Gets the current color model.
   *
   * @return	current color model
   */
  public ColorModel getColorModel() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = cm; }
      };
      runWithReturn.invokeAndWait();
      return (ColorModel) runWithReturn.returnObject;
    }
    else return cm;
  }
  /**
   * Sets the current color model.
   *
   * @param cm	color model
   */
  public void setColorModel( final ColorModel cm ) {
    if(cm == null) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setColorModel(cm); }
      } );
    }
    else {
      ColorModel oldColorModel = getColorModel();
      if(oldColorModel == cm) return;
      noUndoSetColorModel(cm);
      Class[] undoParams = { java.awt.image.ColorModel.class };
      Object[] undoArgs = { (Object) oldColorModel };
      DoCommand undo = new DoCommand(this, "noUndoSetColorModel",
				     undoParams, undoArgs);
      Object[] redoArgs = { (Object) cm };
      DoCommand redo = new DoCommand(this, "noUndoSetColorModel",
				     undoParams, redoArgs);
      getUndoRedo().addUndo(undo, redo, "set color");
    }
  }
  /**
   * Sets the current color model with no undo.
   *
   * @param cm	color model
   */
  public void noUndoSetColorModel( final ColorModel cm ) {
    if(cm == null) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { noUndoSetColorModel(cm); }
      } );
    }
    else {
      this.cm = cm;
      if(lkup != null) {
	lkup.setColorModel(cm);
	lkup.repaint();
      }
      setIndexA(getIndexA());
      setIndexB(getIndexB());
      updateAlphaStates();
    }
  }
  /**
   * Updates the alpha settings.
   */
  public void updateAlphaStates() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { updateAlphaStates(); }
      } );
    }
    else {
      if( cm instanceof IndexColorModel) {
        if(CNUColorModel.alphasNeeded((IndexColorModel) cm)) {
	  if( ! enableAlphaCB.isSelected() ) enableAlphaCB.setSelected(true);
	}
      }
      boolean state = enableAlphaCB.isSelected();
      alphaCB.setEnabled(state);

      if(state) {
	if(BTransparentCB.isVisible()) {
          BTransparentCB.setVisible(false);
          ATransparentCB.setVisible(false);
          AAlphaSlider.setVisible(true);
          BAlphaSlider.setVisible(true);
	}
      }
      else {
	if(AAlphaSlider.isVisible()) {
          AAlphaSlider.setVisible(false);
          BAlphaSlider.setVisible(false);
          ATransparentCB.setVisible(true);
          BTransparentCB.setVisible(true);
	}
      }
      this.invalidate();
      this.validate();
    }
  }
  /**
   * Gets the undo dialog to be used with this editor.
   *
   * @return	undo/redo object
   */
  public UndoRedo getUndoRedo() {
    if(undoRedoObj == null) synchronized (dialogCreateLock) {
      if(undoRedoObj == null)
	undoRedoObj = new UndoRedo(this, true);
    }
    return undoRedoObj;
  }
  /**
   * Sets the current A index.
   *
   * @param index	new A index
   */
  public void setIndexA(final int index) {
   if((index < 0) || (index > 255)) return;
   if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setIndexA(index); }
      } );
    }
    else {
      AIndexModel.setValue(index);
      setIndexANotScrollbar(index);
      ColorModel cm = getColorModel();
      if(cm instanceof IndexColorModel) {
	IndexColorModel icm = (IndexColorModel) cm;
	int mapSize = icm.getMapSize();
	if(index < mapSize)
	  setAdjustedValuesA(icm.getRed(index), icm.getGreen(index),
			     icm.getBlue(index), icm.getAlpha(index));
      }
    }
  }
  /**
   * Sets the current A index but not the scroll bar.
   *
   * @param index	new A index
   */
  public void setIndexANotScrollbar(final int index) {
   if((index < 0) || (index > 255)) return;
   if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setIndexANotScrollbar(index); }
      } );
    }
    else {
      this.indexA = index;
      if(lkup != null) lkup.setBox1(index);
    }
  }
  /**
   * Gets the current A index.
   *
   * @return current A index
   */
  public int getIndexA() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = new Integer(indexA); }
      };
      runWithReturn.invokeAndWait();
      return ((Integer) runWithReturn.returnObject).intValue();
    }
    else return indexA;
  }
  /**
   * Sets the current adjustment color A.
   *
   * @param red		red scrollbar value
   * @param green	green scrollbar value
   * @param blue	blue scrollbar value
   */
  public void setAdjustedColorA(final int red, final int green,
			        final int blue) {
   if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setAdjustedColorA(red, green, blue); }
      } );
    }
    else {
      AIndexSlider.setBackground(new Color(red, green, blue));
      AIndexSlider.setForeground(
        ((red + green + blue) > 383) ? Color.black : Color.white);
      ARedSlider.setBackground(new Color(red, 0, 0));
      AGreenSlider.setBackground(new Color(0, green, 0));
      ABlueSlider.setBackground(new Color(0, 0, blue));
    }
  }
  /**
   * Sets the current scroll adjustment values A.
   *
   * @param red		red scrollbar value
   * @param green	green scrollbar value
   * @param blue	blue scrollbar value
   * @param alpha	alpha scrollbar value
   */
  public void setAdjustedValuesA(final int red, final int green,
				 final int blue, final int alpha) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setAdjustedValuesA(red, green, blue, alpha); }
      } );
    }
    else {
      ARedModel.setValue(red);
      AGreenModel.setValue(green);
      ABlueModel.setValue(blue);
      AAlphaModel.setValue(alpha);
      if(ATransparentCB.isSelected() != (alpha == 0))
        ATransparentCB.setSelected(alpha == 0);
      setAdjustedColorA(red, green, blue);
    }
  }
  /**
   * Sets the current B index.
   *
   * @param index	new B index
   */
  public void setIndexB(final int index) {
    if((index < 0) || (index > 255)) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setIndexB(index); }
      } );
    }
    else {
      BIndexModel.setValue(index);
      setIndexBNotScrollbar(index);
      ColorModel cm = getColorModel();
      if(cm instanceof IndexColorModel) {
	IndexColorModel icm = (IndexColorModel) cm;
	int mapSize = icm.getMapSize();
	if(index < mapSize)
	  setAdjustedValuesB(icm.getRed(index), icm.getGreen(index),
			     icm.getBlue(index), icm.getAlpha(index));
      }
    }
  }
  /**
   * Sets the current B index but not scrollbar value
   *
   * @param index	new B index
   */
  public void setIndexBNotScrollbar(final int index) {
    if((index < 0) || (index > 255)) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setIndexBNotScrollbar(index); }
      } );
    }
    else {
      this.indexB = index;
      if(lkup != null) lkup.setBox2(index);
    }
  }
  /**
   * Gets the current B index.
   *
   * @return	current B index
   */
  public int getIndexB() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = new Integer(indexB); }
      };
      runWithReturn.invokeAndWait();
      return ((Integer) runWithReturn.returnObject).intValue();
    }
    else return indexB;
  }
  /**
   * Sets the current adjustment color B.
   *
   * @param red		red scrollbar value
   * @param green	green scrollbar value
   * @param blue	blue scrollbar value
   */
  public void setAdjustedColorB(final int red, final int green,
				final int blue) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setAdjustedColorB(red, green, blue); }
      } );
    }
    else {
      BIndexSlider.setBackground(new Color(red, green, blue));
      BIndexSlider.setForeground(
        ((red + green + blue) > 383) ? Color.black : Color.white);
      BRedSlider.setBackground(new Color(red, 0, 0));
      BGreenSlider.setBackground(new Color(0, green, 0));
      BBlueSlider.setBackground(new Color(0, 0, blue));
    }
  }
  /**
   * Sets the current scroll adjustment values B.
   *
   * @param red		red scrollbar value
   * @param green	green scrollbar value
   * @param blue	blue scrollbar value
   * @param alpha	alpha scrollbar value
   */
  public void setAdjustedValuesB(final int red, final int green,
				 final int blue, final int alpha) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setAdjustedValuesB(red, green, blue, alpha); }
      } );
    }
    else {
      BRedModel.setValue(red);
      BGreenModel.setValue(green);
      BBlueModel.setValue(blue);
      BAlphaModel.setValue(alpha);
      if(BTransparentCB.isSelected() != (alpha == 0))
        BTransparentCB.setSelected(alpha == 0);
      setAdjustedColorB(red, green, blue);
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
      if (e.getSource() == ATransparentCB) {
	int alpha = 255;
	if( ATransparentCB.isSelected() ) alpha = 0;
	AAlphaModel.setValue(alpha);
      }
      else if (e.getSource() == BTransparentCB) {
	int alpha = 255;
	if( BTransparentCB.isSelected() ) alpha = 0;
	BAlphaModel.setValue(alpha);
      }
      else if (e.getSource() == enableAlphaCB) {
	if(! enableAlphaCB.isSelected()) {
	  ColorModel cm = getColorModel();
	  if(cm instanceof IndexColorModel) {
	    // remove alphas from current color model
	    IndexColorModel icm = (IndexColorModel) cm;
	    if(CNUColorModel.alphasNeeded(icm)) {
	      int mapSize = icm.getMapSize();
	      byte[] reds = new byte[mapSize]; icm.getReds(reds);
	      byte[] greens = new byte[mapSize]; icm.getGreens(greens);
	      byte[] blues = new byte[mapSize]; icm.getBlues(blues);
	      int trans = icm.getTransparentPixel();
	      setColorModel(new CNUColorModel(reds, greens, blues, trans));
	    }
	  }
	}
	updateAlphaStates();
      }
    } finally {
      getCNUViewer().setNormalCursor();
    }
  }
  /**
   * Interprets mouse events over this dialog.
   *
   * @param e	action event
   */
  public void actionPerformed(ActionEvent e) {
    getCNUViewer().setWaitCursor();
    try {
      Object source = e.getSource();
      if (source == saveB) saveColorModel(getColorModel());
      else if (source == acceptB) {
	if(colorD != null) {
	  ColorModel cm = getColorModel();
	  if(cm != null) {
	    if(cm instanceof CNUColorModel) {
	      CNUColorModel ccm = (CNUColorModel) cm;
	      if( ccm.getCNUFile() == null )
	        ccm.setCNUFile("edited" + editnumber++);
	    }
	    colorD.setColorModel(cm);
	    colorD.setVisible(true);
	  }
	}
	setVisible(false);
      }
      else if (source == ASetB) {
	setColorValue(getIndexA(), (byte) ARedModel.getValue(),
		      (byte) AGreenModel.getValue(),
		      (byte) ABlueModel.getValue(),
		      (byte) AAlphaModel.getValue());
      }
      else if (source == BSetB) {
	setColorValue(getIndexB(), (byte) BRedModel.getValue(),
		      (byte) BGreenModel.getValue(),
		      (byte) BBlueModel.getValue(),
		      (byte) BAlphaModel.getValue());
      }
      else if (source == interpolateB) interpolate();
      else if (source == incrementB) increment(1);
      else if (source == decrementB) increment(-1);
      else if(source == dismissB) setVisible(false);
    } finally {
      getCNUViewer().setNormalCursor();
    }
  }
  /**
   * Changes a value of the color table.
   *
   * @param index	index of value to change
   * @param red		new red value
   * @param green	new green value
   * @param blue	new blue value
   * @param alpha	new alpha value
   */
  public void setColorValue(final int index, final byte red,
			    final byte green, final byte blue,
			    final byte alpha) {
    if(index < 0 || index > 255) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setColorValue(index, red, green, blue, alpha); }
      } );
    }
    else {
      ColorModel cm = getColorModel();
      if(cm instanceof IndexColorModel) {
        IndexColorModel icm = (IndexColorModel) cm;
        int mapSize = icm.getMapSize();
        if(index >= mapSize) return;
        byte[] reds = new byte[mapSize]; icm.getReds(reds);
        byte[] greens = new byte[mapSize]; icm.getGreens(greens);
        byte[] blues = new byte[mapSize]; icm.getBlues(blues);
        byte[] alphas = new byte[mapSize]; icm.getAlphas(alphas);
        reds[index] = red; greens[index] = green; blues[index] = blue;
        alphas[index] = alpha;
        // check and possibly set the trans value
        int trans = icm.getTransparentPixel();
        if(alpha == 0) {
	  if(trans < 0) trans = index; // this index becoming transparent
        }
        else if(trans == index) trans = -1; // this index no longer transparent

        if(CNUColorModel.alphasNeeded(alphas, trans)) {
          if(! enableAlphaCB.isSelected()) {
	    Toolkit.getDefaultToolkit().beep();
	    if(index == getIndexA()) setIndexA(getIndexA());
	    if(index == getIndexB()) setIndexB(getIndexB());
	  }
	  else {
            if((trans >= 0) && (trans < mapSize)) alphas[trans] = 0;
	    setColorModel(new CNUColorModel(reds, greens, blues, alphas));
	  }
        }
        else setColorModel(new CNUColorModel(reds, greens, blues, trans));
      }
    }
  }
  /**
   * Sets colors to incremented values.
   *
   * @param amount	amount of increment
   */
  public void increment(final int amount) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { increment(amount); }
      } );
      return;
    }
    ColorModel cm = getColorModel();
    if(! (cm instanceof IndexColorModel)) return;
    IndexColorModel icm = (IndexColorModel) cm;
    int mapSize = icm.getMapSize();
    if(mapSize <= 0) return;
    byte[] reds = new byte[mapSize]; icm.getReds(reds);
    byte[] greens = new byte[mapSize]; icm.getGreens(greens);
    byte[] blues = new byte[mapSize]; icm.getBlues(blues);
    byte[] alphas = new byte[mapSize]; icm.getAlphas(alphas);

    int indexA = getIndexA(); int indexB = getIndexB();
    if(indexA > indexB) {
      int tmp = indexA; indexA = indexB; indexB = tmp;
    }
    boolean changed = false;
    if( redCB.isSelected() )
      changed |= incrementUnsignedBytes(reds, indexA, indexB, amount);
    if( greenCB.isSelected() )
      changed |= incrementUnsignedBytes(greens, indexA, indexB, amount);
    if( blueCB.isSelected() )
      changed |= incrementUnsignedBytes(blues, indexA, indexB, amount);
    if( alphaCB.isSelected() && enableAlphaCB.isSelected() )
      changed |= incrementUnsignedBytes(alphas, indexA, indexB, amount);
    if(changed) {
      getUndoRedo().startSteps();
      int trans = icm.getTransparentPixel();
      if(CNUColorModel.alphasNeeded(alphas, trans)) {
        if((trans >= 0) && (trans < mapSize)) alphas[trans] = 0;
	setColorModel(new CNUColorModel(reds, greens, blues, alphas));
      }
      else setColorModel(new CNUColorModel(reds, greens, blues, trans));
      getUndoRedo().finishUndoSteps((amount < 0) ? "decrement" : "increment");
    }
  }
  /**
   * Increments values in an array by an amount.
   *
   * @param bytes	array of bytes to increment
   * @param start	offset to first byte in array to increment
   * @param stop	offset to last byte in array to increment
   * @param amount	amount to increment
   */
  public static boolean incrementUnsignedBytes(byte[] bytes,
					       int start, int stop,
					       int amount) {
    boolean changed = false;
    if( (start >= bytes.length) || (stop >= bytes.length) ||
	(start < 0) || (stop < 0) ) return changed;
    for(int i = start; i <= stop; i++) {
      int value = CNUTypes.UnsignedByteToInt(bytes[i]);
      byte newByteValue = CNUTypes.IntToUnsignedByte(value + amount);
      if(newByteValue != bytes[i]) {
	bytes[i] = newByteValue; changed = true;
      }
    }
    return changed;
  }
  /**
   * Sets colors to interpolated range of values.
   */
  public void interpolate() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { interpolate(); }
      } );
      return;
    }
    ColorModel cm = getColorModel();
    if(! (cm instanceof IndexColorModel)) return;
    IndexColorModel icm = (IndexColorModel) cm;
    int mapSize = icm.getMapSize();
    if(mapSize <= 0) return;
    byte[] reds = new byte[mapSize]; icm.getReds(reds);
    byte[] greens = new byte[mapSize]; icm.getGreens(greens);
    byte[] blues = new byte[mapSize]; icm.getBlues(blues);
    byte[] alphas = new byte[mapSize]; icm.getAlphas(alphas);

    int indexA = getIndexA(); int indexB = getIndexB();

    boolean changed = false;
    if( redCB.isSelected() ) {
      byte redA = (byte) ARedModel.getValue();
      byte redB = (byte) BRedModel.getValue();
      changed |= interpolateUnsignedBytes(reds, indexA, indexB, redA, redB);
    }
    if( greenCB.isSelected() ) {
      byte greenA = (byte) AGreenModel.getValue();
      byte greenB = (byte) BGreenModel.getValue();
      changed |= interpolateUnsignedBytes(greens, indexA, indexB,
					  greenA, greenB);
    }
    if( blueCB.isSelected() ) {
      byte blueA = (byte) ABlueModel.getValue();
      byte blueB = (byte) BBlueModel.getValue();
      changed |= interpolateUnsignedBytes(blues, indexA, indexB,
					  blueA, blueB);
    }
    if( alphaCB.isSelected() && enableAlphaCB.isSelected() ) {
      byte alphaA = (byte) AAlphaModel.getValue();
      byte alphaB = (byte) BAlphaModel.getValue();
      changed |= interpolateUnsignedBytes(alphas, indexA, indexB,
					 alphaA, alphaB);
    }
    if(changed) {
      getUndoRedo().startSteps();
      int trans = icm.getTransparentPixel();
      if(CNUColorModel.alphasNeeded(alphas, trans)) {
        if((trans >= 0) && (trans < mapSize)) alphas[trans] = 0;
	setColorModel(new CNUColorModel(reds, greens, blues, alphas));
      }
      else setColorModel(new CNUColorModel(reds, greens, blues, trans));
      getUndoRedo().finishUndoSteps("interpolate");
    }
  }
  /**
   * Interpolate a range of values in an array of unsigned bytes.
   *
   * @param bytes		array of bytes to interpolate
   * @param start		offset to first byte in array to interpolate
   * @param stop		offset to last byte in array to interpolate
   * @param startByteValue	value to set first byte in array to
   * @param stopByteValue	value to set last byte in array to
   */
  public static boolean interpolateUnsignedBytes(byte[] bytes,
						 int start, int stop,
						 byte startByteValue,
						 byte stopByteValue) {
    boolean changed = false;
    if( (start >= bytes.length) || (stop >= bytes.length) ||
	(start < 0) || (stop < 0) ) return changed;
    int stopValue = CNUTypes.UnsignedByteToInt(stopByteValue);
    int startValue = CNUTypes.UnsignedByteToInt(startByteValue);
    if(start > stop) {
      int tmp = start; start = stop; stop = tmp;
      tmp = startValue; startValue = stopValue; stopValue = tmp;
    }
    if(start == stop)
      bytes[start] = (byte) Math.round((stopValue - startValue)/2.0);
    else {
      double inc = (double) (stopValue - startValue) / (double) (stop - start);
      double value = startValue;
      for(int i = start; i <= stop; i++, value += inc) {
	byte newByteValue = (byte) Math.round(value);
        if(newByteValue != bytes[i]) {
	  bytes[i] = newByteValue; changed = true;
        }
      }
    }
    return changed;
  }
  /**
   * Catches mouse down events to set index.
   *
   * @param evt	mouse event
   */
  public void mousePressed(MouseEvent evt){
    Point pt = evt.getPoint();
    int modifiers = evt.getModifiers();
    if( ! dcmq.getImageBounds().contains(pt) ) return;
    int index = (int) dcmq.getValue(pt);
    if( ((modifiers & InputEvent.BUTTON1_MASK) == 0) ||
	evt.isAltDown() || evt.isControlDown()) setIndexB(index);
    else setIndexA(index);
  }
  /**
   * Called when the mouse is clicked - ignored.
   *
   * @param evt	mouse event
   */
  public void mouseClicked(MouseEvent evt) {};
  /**
   * Called when the mouse moves over this -ignored.
   *
   * @param evt	mouse event
   */
  public void mouseEntered(MouseEvent evt) {};
  /**
   * Called when the mouse leave this - ignored.
   *
   * @param evt	mouse event
   */
  public void mouseExited(MouseEvent evt) {};
  /**
   * Catches mouse up events - ignored.
   *
   * @param evt	mouse event
   */
  public void mouseReleased(MouseEvent evt) {};
}
