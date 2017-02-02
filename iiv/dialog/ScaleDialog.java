package iiv.dialog;
import iiv.*;
import iiv.data.*;
import iiv.util.*;
import iiv.gui.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.*;
import java.lang.*;

/**
 * Dialog to get scale factors from user.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUViewer
 * @since	iiV1.1
 */
public class ScaleDialog extends CNUDialog
implements ActionListener, ItemListener {
  private static final long serialVersionUID = 2953424503968986924L;
  private double quantificationValue = 1.0;
  private double scaleFactorValue = 1.0;
  private double translationValue = 0.0;
  private double threshMinValue = 0.0;
  private double threshMinValueValue = 0.0;
  private double threshMaxValue = 0.0;
  private double threshMaxValueValue = 0.0;

  private JTextField scaleFactorTF = new JTextField(4);
  private JTextField translationTF = new JTextField(4);
  private JCheckBox threshMinCB = new JCheckBox("Minimum Threshold:");
  private JTextField threshMinTF = new JTextField(4);
  private JTextField threshMinValueTF = new JTextField(4);
  private JCheckBox threshMaxCB = new JCheckBox("Maximum Threshold:");
  private JTextField threshMaxTF = new JTextField(4);
  private JTextField threshMaxValueTF = new JTextField(4);
  private JCheckBox quantificationCB = new JCheckBox("Quantification:");
  private JTextField quantificationTF = new JTextField("1.00");
  // use change listeners to prevent lost of accuracy when not changed

  private JTextComponentChangeListener scaleFactorTFListener =
    new JTextComponentChangeListener(scaleFactorTF);
  private JTextComponentChangeListener translationTFListener =
    new JTextComponentChangeListener(translationTF);
  private JTextComponentChangeListener threshMinTFListener =
    new JTextComponentChangeListener(threshMinTF);
  private JTextComponentChangeListener threshMinValueTFListener =
    new JTextComponentChangeListener(threshMinValueTF);
  private JTextComponentChangeListener threshMaxTFListener =
    new JTextComponentChangeListener(threshMaxTF);
  private JTextComponentChangeListener threshMaxValueTFListener =
    new JTextComponentChangeListener(threshMaxValueTF);
  private JTextComponentChangeListener quantificationTFListener =
    new JTextComponentChangeListener(quantificationTF);

  private JComboBox dataTypeCH = new JComboBox();
  private JButton setCurrentB = new JButton("Reset to Current");
  private JButton setDefaultB = new JButton("Reset to Default");
  private JButton setPositiveB = new JButton("Set to Positive");
  private JButton setNegativeB = new JButton("Set to Negative");
  private JButton setRangeB = new JButton("Set to Range");
  private JButton applyB = new JButton("Apply");
  private JButton applyToDefaultB = new JButton("Apply to Default Only");
  private JButton dismissB = new JButton("Dismiss");

  private double quantificationUsedToDisplay = 1.0;
  private ScaleInterface sdi = null;


  /**
   * Constructs a new instance of ScaleDialog.
   *
   * @param parentFrame	parent frame needed for building a window
   */
  public ScaleDialog(Frame parentFrame) { this(parentFrame, null, null); }
  /**
   * Constructs a new instance of ScaleDialog.
   *
   * @param parentFrame	parent frame needed for building a window
   * @param cnuv	CNUViewer that this window interacts with
   * @param sdi		scale dialog user to interact with
   */
  public ScaleDialog(Frame parentFrame, CNUViewer cnuv, ScaleInterface sdi) {
    super(parentFrame, "Set Scale", false, cnuv);
    setScaleDialogUser(sdi);

    Container contentPane = getContentPane();

    contentPane.setLayout(new GridLayout(0, 2, 4, 3));
    contentPane.add(new JLabel("Scale Factor:"));
    scaleFactorTF.setToolTipText("<html><font size=1>Linear factor for mapping<p>voxels values to color indice</font>");
    contentPane.add(scaleFactorTF);
    contentPane.add(new JLabel("Translation:"));
    translationTF.setToolTipText("<html><font size=1>Linear translation for mapping<p>voxels values to color indice</font>");
    contentPane.add(translationTF);
    threshMinCB.setToolTipText("<html><font size=1>Select to map voxels less then the minimum<p>to color indice given by Minimum Value</font>");
    contentPane.add(threshMinCB);
    threshMinCB.addItemListener(this);
    threshMinTF.setToolTipText("<html><font size=1>Voxels less then this are mapped to the Minimum Value<p>color indice when Minimum Value checkbox is selected</font>");
    contentPane.add(threshMinTF);
    contentPane.add(new JLabel("Minimum Value:"));
    threshMinValueTF.setToolTipText("<html><font size=1>Color indice value voxels less then<p>the minimum are mapped to</font>");
    contentPane.add(threshMinValueTF);
    contentPane.add(threshMaxCB);
    threshMaxCB.setToolTipText("<html><font size=1>Select to map voxels greater then the minimum<p>to color indice given by Minimum Value</font>");
    threshMaxCB.addItemListener(this);
    threshMaxTF.setToolTipText("<html><font size=1>Voxels greater then this are mapped to the Minimum Value<p>color indice when Minimum Value checkbox is selected</font>");
    contentPane.add(threshMaxTF);
    contentPane.add(new JLabel("Maximum Value:"));
    threshMaxValueTF.setToolTipText("<html><font size=1>Color indice value voxels less then<p>the minimum are mapped to</font>");
    contentPane.add(threshMaxValueTF);
    quantificationCB.setToolTipText("<html><font size=1>Select to show quantified<p>values in threshold fields</font>");
    contentPane.add(quantificationCB);
    quantificationCB.addItemListener(this);
    quantificationTF.setToolTipText("<html><font size=1>Quantification factor that maps voxel<p>values to some real world units</font>");
    contentPane.add(quantificationTF);

    contentPane.add(new JLabel("Current Data type:"));
    for(int i = 0; i < CNUTypes.DATA_TYPES.length; i++)
      dataTypeCH.addItem(CNUTypes.typeToString(CNUTypes.DATA_TYPES[i]));
    dataTypeCH.setSelectedIndex(1);
    dataTypeCH.setToolTipText("<html><font size=1>Data type voxels are stored as and used when setting<p>scale to default, positive or negative ranges</font>");
    contentPane.add(dataTypeCH);

    setCurrentB.setToolTipText("<html><font size=1>Reset scale values to the<p>currently selected displayed object</font>");
    contentPane.add(setCurrentB);
    setCurrentB.addActionListener(this);
    setDefaultB.setToolTipText("<html><font size=1>Reset scale values to the<p>default for the data type</font>");
    contentPane.add(setDefaultB);
    setDefaultB.addActionListener(this);
    setPositiveB.setToolTipText("<html><font size=1>Reset scale values to show only<p>positive values for the data type</font>");
    contentPane.add(setPositiveB);
    setPositiveB.addActionListener(this);
    contentPane.add(setNegativeB);
    setNegativeB.setToolTipText("<html><font size=1>Reset scale values to show only<p>negative values for the data type</font>");
    setNegativeB.addActionListener(this);
    setRangeB.setToolTipText("<html><font size=1>Reset scale values to map voxels from the minimum to<p>maximum to the minimum to maximum color values</font>");
    contentPane.add(setRangeB);
    setRangeB.addActionListener(this);
    applyToDefaultB.setToolTipText("<html><font size=1>Apply scale values to the<p>defaults for displaying new data</font>");
    contentPane.add(applyToDefaultB);
    applyToDefaultB.addActionListener(this);
    applyB.setToolTipText("<html><font size=1>Apply scale values to the<p>selected or all displayed data</font>");
    contentPane.add(applyB);
    applyB.addActionListener(this);
    dismissB.setToolTipText("<html><font size=1>Hide this window</font>");
    contentPane.add(dismissB);
    dismissB.addActionListener(this);
    setToDefault();
    setToCurrent();
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
    if(cnuviewer != null) setScaleDialogUser(cnuviewer.getCNUDisplay());
    setToCurrent();
  }
  /**
   * Sets the object that this ScaleDialog
   * can recieve and give scale factors to.
   *
   * @param sdi	scale dialog user
   */
  public void setScaleDialogUser(ScaleInterface sdi) { this.sdi = sdi; }
  /**
   * Gets the class that this ScaleDialog can recieve and
   * give scale factors to.
   *
   * @return	scale dialog user
   */
  public ScaleInterface getScaleDialogUser() { return sdi; }
  /**
   * Sets the data type this dialog uses for range setting.
   * Should probably only be called from event dispatch thread.
   *
   * @param	data type
   */
  private void setDataType(int type) {
    for(int i = 0; i < CNUTypes.DATA_TYPES.length; i++) {
      if(CNUTypes.DATA_TYPES[i] == type) {
	dataTypeCH.setSelectedIndex(i);
	return;
      }
    }
  }
  /**
   * Gets the data type this dialog uses for range setting.
   *
   * @return	data type
   */
  public int getDataType() {
    return CNUTypes.DATA_TYPES[dataTypeCH.getSelectedIndex()];
  }
  /**
   * Sets values to those currently in the DisplayData class.
   */
  public void setToCurrent() {
    ScaleInterface sdi = getScaleDialogUser();
    if(sdi != null) {
      CNUScale sc = sdi.getScale();
      if(sc != null) setValues(sc);
      setDataType(sdi.getType());
    }
  }
  /**
   * Sets default display values based on the Scale Dialog User.
   */
  public void setToDefault() {
    CNUScale sc = new CNUScale(1.0);
    sc.setToDisplayDefault(getDataType());
    sc.setQuantification(getQuantification());
    setValues(sc);
  }
  /**
   * Sets to display positive values based on the Scale Dialog User.
   */
  public void setToPositive() {
    CNUScale sc = new CNUScale(1.0);
    sc.setToDisplayPositive(getDataType());
    sc.setQuantification(getQuantification());
    setValues(sc);
  }
  /**
   * Sets to display negative values based on the Scale Dialog User.
   */
  public void setToNegative() {
    CNUScale sc = new CNUScale(1.0);
    sc.setToDisplayNegative(getDataType());
    sc.setQuantification(getQuantification());
    setValues(sc);
  }
  /**
   * Sets default values based on the Scale Dialog User.
   */
  public void setToRange() {
    CNUScale sc = getScale();
    if(sc == null) {
      Toolkit.getDefaultToolkit().beep();
      return;
    }
    sc.setToFitDataInRange(getDataType());
    setValues(sc);
  }
  /**
   * Sets values to given CNUScale.
   *
   * @param sc	CNUScale to get values from
   */
  public void setValues(CNUScale sc) {
    if(! SwingUtilities.isEventDispatchThread()) {
      final CNUScale fsc = sc;
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setValues(fsc); }
      } );
      return;
    }
    if(sc == null) sc = new CNUScale(1.0);

    quantificationValue = sc.getQuantification();
    boolean state = sc.getQuantificationState();
    if(quantificationCB.isSelected() != state)
      quantificationCB.setSelected(state);
    if(state) quantificationUsedToDisplay = quantificationValue;
    else quantificationUsedToDisplay = 1.0;

    quantificationTF.setText(Double.toString(quantificationValue));
    quantificationTFListener.setChangedState(false);
    quantificationTF.setEditable(!quantificationCB.isSelected());

    scaleFactorValue = sc.getScaleFactor();
    scaleFactorTF.setText(Double.toString(scaleFactorValue));
    scaleFactorTFListener.setChangedState(false);

    translationValue = sc.getTranslation();
    translationTF.setText(
      Double.toString(translationValue*quantificationUsedToDisplay));
    translationTFListener.setChangedState(false);

    state = sc.getThreshMinState();
    if(threshMinCB.isSelected() != state) threshMinCB.setSelected(state);

    threshMinValue = sc.getThreshMin();
    threshMinTF.setText(Double.toString(threshMinValue*quantificationUsedToDisplay));
    threshMinTF.setEnabled(state);
    threshMinTFListener.setChangedState(false);

    threshMinValueValue = sc.getThreshMinValue();
    threshMinValueTF.setText(Double.toString(threshMinValueValue));
    threshMinValueTFListener.setChangedState(false);

    state = sc.getThreshMaxState();
    if(threshMaxCB.isSelected() != state) threshMaxCB.setSelected(state);

    threshMaxValue = sc.getThreshMax();
    threshMaxTF.setText(Double.toString(threshMaxValue*quantificationUsedToDisplay));
    threshMaxTF.setEnabled(state);
    threshMaxTFListener.setChangedState(false);

    threshMaxValueValue = sc.getThreshMaxValue();
    threshMaxValueTF.setText(Double.toString(threshMaxValueValue));
    threshMaxValueTFListener.setChangedState(false);
  }
  private boolean error_happened = false;
  /**
   * Gets the currently displayed scale values.
   *
   * @return	current scale values
   */
  public CNUScale getScale() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = getScale(); }
      };
      runWithReturn.invokeAndWait();
      return (CNUScale) runWithReturn.returnObject;
    }
    error_happened = false;
    CNUScale sc = new CNUScale(1.0);

    sc.setQuantificationState(quantificationCB.isSelected());

    sc.setQuantification(getQuantification());

    if( scaleFactorTFListener.getChangedState() ) {
      try {
	scaleFactorValue = Double.valueOf(scaleFactorTF.getText()).doubleValue();
      } catch (NumberFormatException e1) {
	scaleFactorTF.setText(Double.toString(scaleFactorValue));
	error_happened = true;
      }
      scaleFactorTFListener.setChangedState(false);
    }
    sc.setScaleFactor(scaleFactorValue);

    if( translationTFListener.getChangedState() ) {
      try {
	translationValue = Double.valueOf(translationTF.getText()).doubleValue()/
	  quantificationUsedToDisplay;
      } catch (NumberFormatException e2) {
	translationTF.setText(
	  Double.toString(translationValue*quantificationUsedToDisplay));
	error_happened = true;
      }
      translationTFListener.setChangedState(false);
    }
    sc.setTranslation(translationValue);

    if( threshMinTFListener.getChangedState() ) {
      try {
	threshMinValue =
	  Double.valueOf(threshMinTF.getText()).doubleValue()/quantificationUsedToDisplay;
      }  catch (NumberFormatException e3) {
	threshMinTF.setText(Double.toString(threshMinValue*quantificationUsedToDisplay));
	error_happened = true;
      }
      threshMinTFListener.setChangedState(false);
    }
    if( threshMinValueTFListener.getChangedState() ) {
      try {
	threshMinValueValue =
	  Double.valueOf(threshMinValueTF.getText()).doubleValue();
      }  catch (NumberFormatException e3) {
	threshMinValueTF.setText(Double.toString(threshMinValueValue));
	error_happened = true;
      }
      threshMinValueTFListener.setChangedState(false);
    }
    sc.setThreshMin(threshMinValue, threshMinValueValue);
    sc.setThreshMinState(threshMinCB.isSelected());

    if( threshMaxTFListener.getChangedState() ) {
      try {
	threshMaxValue =
	  Double.valueOf(threshMaxTF.getText()).doubleValue()/quantificationUsedToDisplay;
      }  catch (NumberFormatException e4) {
	threshMaxTF.setText(Double.toString(threshMaxValue*quantificationUsedToDisplay));
	error_happened = true;
      }
      threshMaxTFListener.setChangedState(false);
    }
    if( threshMaxValueTFListener.getChangedState() ) {
      try {
	threshMaxValueValue = 
	  Double.valueOf(threshMaxValueTF.getText()).doubleValue();
      }  catch (NumberFormatException e4) {
	threshMaxValueTF.setText(Double.toString(threshMaxValueValue));
	error_happened = true;
      }
      threshMaxValueTFListener.setChangedState(false);
    }
    sc.setThreshMax(threshMaxValue, threshMaxValueValue);
    sc.setThreshMaxState(threshMaxCB.isSelected());

    if(error_happened) return null;
    return sc;
  }
  /**
   * Gets the current quantification factor.
   * Should only be called from event processing thread.
   *
   * @return quantification factor
   */
  private double getQuantification() {
    if( quantificationTFListener.getChangedState() ) {
      try {
        quantificationValue =
	  Double.valueOf(quantificationTF.getText()).doubleValue();
      } catch (NumberFormatException e1) {
	error_happened = true;
	Toolkit.getDefaultToolkit().beep();
        quantificationTF.setText(Double.toString(quantificationValue));
      }
      quantificationTF.getDocument().addDocumentListener(quantificationTFListener);
      quantificationTFListener.setChangedState(false);
    }
    return quantificationValue;
  }
  /**
   * Applies current values to displayed images.
   */
  public void apply() {
    CNUScale sc = getScale();
    if(sc == null) Toolkit.getDefaultToolkit().beep();
    else {
      ScaleInterface sdi = getScaleDialogUser();
      if(sdi != null) {
	sdi.setScale(sc);
	sdi.updateScale();
      }
    }
  }
  /**
   * Applies current values to default scale.
   */
  public void applyToDefaultOnly() {
    CNUScale sc = getScale();
    ScaleInterface sdi = getScaleDialogUser();
    if(sc == null) Toolkit.getDefaultToolkit().beep();
    else if(sdi != null) {
      sdi.setScale(sc);
      getCNUViewer().setScaleMode(CNUViewer.LAST_SCALING);
    }
  }
  /**
   * Updates the scale values.
   */
  public void updateQuantification() {
    CNUScale sc = getScale();
    if(sc == null) Toolkit.getDefaultToolkit().beep();
    setValues(sc);
  }
  /**
   * Interprets state changes.
   *
   * @param e	item event
   */
  public void itemStateChanged(ItemEvent e) {
    Object source = e.getSource();
    if(source == threshMinCB)
      threshMinTF.setEnabled(threshMinCB.isSelected());
    else if(source == threshMaxCB)
      threshMaxTF.setEnabled(threshMaxCB.isSelected());
    else if(source == quantificationCB) {
      quantificationTF.setEditable(! quantificationCB.isSelected() );
      updateQuantification();
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
      Object source = e.getSource();
      if(source == setCurrentB) setToCurrent();
      else if(source == setDefaultB) setToDefault();
      else if(source == setPositiveB) setToPositive();
      else if(source == setNegativeB) setToNegative();
      else if(source == setRangeB) setToRange();
      else if(source == applyB) apply();
      else if(source == applyToDefaultB) applyToDefaultOnly();
      else if(source == dismissB) this.setVisible(false);
    } finally {
      getCNUViewer().setNormalCursor();
    }
  }
}
