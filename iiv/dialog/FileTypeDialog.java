package iiv.dialog;
import iiv.data.*;
import iiv.*;
import iiv.io.*;
import iiv.util.*;
import iiv.display.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.lang.*;

/**
 * Dialog that displays and gets file types and raw image information
 * from the user.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNURawImgFile
 */
public class FileTypeDialog extends CNUDialog
  implements ActionListener, ItemListener, CNUConversionTypes {
  private static final long serialVersionUID = 4187691632203929919L;
  private JButton addClassB = new JButton("Add File Type Class");
  private JTextField addClassTF = new JTextField(25);

  private JButton setToCurrentB = new JButton("Set to Current");
  private JButton applyToDefaultsB = new JButton("Apply to Defaults");
  private JComboBox dataTypeCH = new JComboBox();
  private JComboBox dataConversionCH = new JComboBox(CONVERSION_NAMES);
  private JTextField skipBytesTF = new JTextField(4);
  private JTextField xDimTF = new JTextField(4);
  private JTextField yDimTF = new JTextField(4);
  private JTextField zDimTF = new JTextField(4);
  private JTextField iDimTF = new JTextField(4);

  private JCheckBox incrementsCB = new JCheckBox("Manual increments(words):");
  private JButton calculateIncsB =
    new JButton("Calculate Increments From Dimensions");
  private JTextField xIncTF = new JTextField(4);
  private JTextField yIncTF = new JTextField(4);
  private JTextField zIncTF = new JTextField(4);
  private JTextField iIncTF = new JTextField(4);

  private JComboBox orientationCH = new JComboBox();
  private JComboBox leftRightCH = new JComboBox();
  private JComboBox anteriorPosteriorCH = new JComboBox();
  private JComboBox inferiorSuperiorCH = new JComboBox();

  private JTextField xResTF = new JTextField(4);
  private JTextField yResTF = new JTextField(4);
  private JTextField zResTF = new JTextField(4);
  private JTextField iResTF = new JTextField(4);

  private JTextField quantificationFactorTF = new JTextField(10);

  private JButton dismissB = new JButton("Dismiss");

  private QueryDialog QD = null;

  /**
   * Constructs a new instance of FileTypeDialog.
   *
   * @param parentFrame	parent frame needed for building a window
   */
  public FileTypeDialog(Frame parentFrame) { this(parentFrame, null); }
  /**
   * Constructs a new instance of FileTypeDialog.
   *
   * @param parentFrame	parent frame
   * @param cnuv	CNUViewer to work with
   */
  public FileTypeDialog(Frame parentFrame, CNUViewer cnuv ) {
    super(parentFrame, "File Types", false, cnuv);

    Container contentPane = getContentPane();
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    contentPane.add(Box.createVerticalStrut(5));

    Box box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(addClassB);
    addClassB.addActionListener(this);
    box.add(addClassTF);
    box.add(Box.createHorizontalStrut(5));

    contentPane.add(Box.createVerticalStrut(15));
    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add((new JLabel("Raw file type settings (CNURawImgFile)")));
    box.add(Box.createHorizontalGlue());
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    JLabel warningL = new JLabel(
      "Note - settings must be applied to defaults before reading raw image");
    warningL.setForeground(Color.yellow);
    box.add(warningL);
    box.add(Box.createHorizontalGlue());
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(new JLabel("Data Type:  "));
    for(int i = 0; i < CNUTypes.DATA_TYPES.length; i++)
      dataTypeCH.addItem(CNUTypes.typeToString(CNUTypes.DATA_TYPES[i]));
    dataTypeCH.setSelectedIndex(1);
    box.add(dataTypeCH);
    box.add(Box.createHorizontalGlue());
    box.add(Box.createHorizontalStrut(5));
    box.add(new JLabel("Data Conversion:  "));
    dataConversionCH.setSelectedIndex(0);
    box.add(dataConversionCH);
    box.add(Box.createHorizontalGlue());
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(new JLabel("Skip Bytes:  "));
    box.add(skipBytesTF);
    box.add(Box.createHorizontalGlue());
    box.add(Box.createHorizontalStrut(5));
    box.add(new JLabel("Dimensions:  "));
    box.add(xDimTF);
    box.add(yDimTF);
    box.add(zDimTF);
    box.add(iDimTF);
    box.add(Box.createHorizontalGlue());
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(incrementsCB);
    incrementsCB.addItemListener(this);
    box.add(Box.createHorizontalStrut(5));
    box.add(xIncTF);
    box.add(yIncTF);
    box.add(zIncTF);
    box.add(iIncTF);
    box.add(Box.createHorizontalGlue());
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(calculateIncsB);
    calculateIncsB.addActionListener(this);
    box.add(Box.createHorizontalGlue());
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(new JLabel("Orientation:  "));
    for(int i = 0; i < CNUDimensions.ORIENTATIONS.length; i++)
      orientationCH.addItem(
	CNUDimensions.orientationToString(CNUDimensions.ORIENTATIONS[i]));
    box.add(orientationCH);
    leftRightCH.addItem(
      CNUDimensions.orientationOrderLRToString(CNUDimensions.LEFT_POSITIVE));
    leftRightCH.addItem(
      CNUDimensions.orientationOrderLRToString(CNUDimensions.RIGHT_POSITIVE));
    box.add(leftRightCH);
    anteriorPosteriorCH.addItem(
      CNUDimensions.orientationOrderAPToString(CNUDimensions.ANTERIOR_POSITIVE));
    anteriorPosteriorCH.addItem(
      CNUDimensions.orientationOrderAPToString(CNUDimensions.POSTERIOR_POSITIVE));
    box.add(anteriorPosteriorCH);
    inferiorSuperiorCH.addItem(
      CNUDimensions.orientationOrderISToString(CNUDimensions.INFERIOR_POSITIVE));
    inferiorSuperiorCH.addItem(
      CNUDimensions.orientationOrderISToString(CNUDimensions.SUPERIOR_POSITIVE));
    box.add(inferiorSuperiorCH);
    box.add(Box.createHorizontalGlue());
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(new JLabel("Spatial Resolutions:  "));
    box.add(xResTF);
    box.add(yResTF);
    box.add(zResTF);
    box.add(iResTF);
    box.add(Box.createHorizontalGlue());
    box.add(Box.createHorizontalStrut(5));
    box.add(new JLabel("Quantification Factor:  "));
    box.add(quantificationFactorTF);
    box.add(Box.createHorizontalGlue());
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(setToCurrentB);
    setToCurrentB.addActionListener(this);
    box.add(applyToDefaultsB);
    applyToDefaultsB.addActionListener(this);
    box.add(Box.createHorizontalGlue());
    box.add(dismissB);
    dismissB.addActionListener(this);
    box.add(Box.createHorizontalStrut(5));

    contentPane.add(Box.createVerticalStrut(5));

    CNUDimensions dims = CNURawImgFile.getDefaultDimensions();
    setRawFileFields(
      dims, CNURawImgFile.getDefaultQuantificationFactor(),
      CNURawImgFile.getDefaultSkipBytes(),
      CNURawImgFile.getDefaultCNUDataConversions().getConvert(dims.getType())
    );
    pack();
  }
  /**
   * Applies the current raw field settings to CNURawImgFile defaults.
   *
   * @return	<code>true</code> if successfull at getting all values from
   *		the fields, <code>false</code> otherwise
   */
  public boolean applyRawFieldsToDefaults() {
    // set the CNURawImgFile defaults from this dialogs fields
    CNUDimensions rawDims = getRawDimensions();
    if(rawDims != null) {
      CNURawImgFile.setDefaultDimensions(rawDims);
      // only applies the conversion for the current data type
      CNUDataConversions cnuDataConv =
	CNURawImgFile.getDefaultCNUDataConversions();
	cnuDataConv.setConvert(rawDims.getType(), getConversionChoice());
      CNURawImgFile.setDefaultCNUDataConversions(cnuDataConv);
      CNURawImgFile.setDefaultSkipBytes(getRawSkipBytes());
      CNURawImgFile.setDefaultQuantificationFactor(
	getRawQuantificationFactor());
      return true;
    }
    return false;
  }
  /**
   * Gets the current conversion choice.
   *
   * @return	the current conversion choice
   */
  public int getConversionChoice() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() {
	  returnObject = new Integer(getConversionChoice());
	}
      };
      runWithReturn.invokeAndWait();
      return ((Integer) runWithReturn.returnObject).intValue();
    }
    else { return dataConversionCH.getSelectedIndex(); }
  }
  /**
   * Sets the raw file increments fields.
   *
   * @param dims dimensions containing desired increments.
   */
  private void setRawFileIncs(final CNUDimensions dims) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setRawFileIncs(dims); }
      } );
    }
    else {
      xIncTF.setText(Integer.toString(dims.getIncrement(0)));
      yIncTF.setText(Integer.toString(dims.getIncrement(1)));
      zIncTF.setText(Integer.toString(dims.getIncrement(2)));
      iIncTF.setText(Integer.toString(dims.getIncrement(3)));
    }
  }
  /**
   * Sets the raw file fields.
   *
   * @param dims dimensions containing most raw field settings
   * @param quantificationFactor	value for quantification field
   * @param skipBytes	value for skip bytes field
   * @param convertType	value for conversion type field
   */
  public void setRawFileFields(final CNUDimensions dims,
			       final double quantificationFactor,
			       final long skipBytes, final int convertType) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setRawFileFields(dims, quantificationFactor,
					     skipBytes, convertType); }
      } );
    }
    else {
      skipBytesTF.setText(Long.toString(skipBytes));

      if(dims != null) {
        int type = dims.getType();
        for(int i = 0; i < CNUTypes.DATA_TYPES.length; i++) {
	  if(CNUTypes.DATA_TYPES[i] == type) {
	    dataTypeCH.setSelectedIndex(i);
	    break;
	  }
        }
	dataConversionCH.setSelectedIndex(convertType);

        xDimTF.setText(Integer.toString(dims.xdim()));
        yDimTF.setText(Integer.toString(dims.ydim()));
        zDimTF.setText(Integer.toString(dims.zdim()));
        iDimTF.setText(Integer.toString(dims.idim()));

	setManualIncrementsEnabled(! dims.incrementsSetToDefault());
	setRawFileIncs(dims);

        int orientation = dims.getOrientation();
        for(int i = 0; i < CNUDimensions.ORIENTATIONS.length; i++) {
	  if(orientation == CNUDimensions.ORIENTATIONS[i]) {
	    orientationCH.setSelectedIndex(i);
	    break;
	  }
        }

        int orientationOrder = dims.getOrientationOrder();
        if((orientationOrder & CNUDimensions.LEFT_POSITIVE) != 0)
	  leftRightCH.setSelectedIndex(0);
        else leftRightCH.setSelectedIndex(1);
        if((orientationOrder & CNUDimensions.ANTERIOR_POSITIVE) != 0)
	  anteriorPosteriorCH.setSelectedIndex(0);
        else anteriorPosteriorCH.setSelectedIndex(1);
        if((orientationOrder & CNUDimensions.INFERIOR_POSITIVE) != 0)
	  inferiorSuperiorCH.setSelectedIndex(0);
        else inferiorSuperiorCH.setSelectedIndex(1);


        xResTF.setText(Double.toString(dims.getSpatialRes(0)));
        yResTF.setText(Double.toString(dims.getSpatialRes(1)));
        zResTF.setText(Double.toString(dims.getSpatialRes(2)));
        iResTF.setText(Double.toString(dims.getSpatialRes(3)));

      }
      quantificationFactorTF.setText(Double.toString(quantificationFactor));
    }
  }
  /**
   * Sets the raw field with values from the currently selected component.
   */
  public void setRawFieldsToCurrent() {
    // set to default
    CNUDimensions dims = CNURawImgFile.getDefaultDimensions();
    double quant = CNURawImgFile.getDefaultQuantificationFactor();
    long skipBytes = CNURawImgFile.getDefaultSkipBytes();
    CNUDataConversions cnuDataConv =
      CNURawImgFile.getDefaultCNUDataConversions();
    int convertType = cnuDataConv.getConvert(dims.getType());
    // unless a proper display component is selected
    Component comp = getCNUDisplay().getCurrentComponent();
    if(comp instanceof SingleImg) {
      CNUData data = ((SingleImg) comp).getData();
      if(data != null) {
	CNUDimensions dimstmp = data.getDimensions();
	if(dimstmp != null) {
	  dims = dimstmp;
	  quant = data.getFactor();
	  skipBytes = 0;
          convertType = cnuDataConv.getConvert(dims.getType());
          if(data instanceof CNUImgFile) {
	    skipBytes = ((CNUImgFile) data).getSkipBytes();
	    cnuDataConv = ((CNUImgFile) data).getCNUDataConversions();
            convertType = cnuDataConv.getConvert(dims.getType());
          }
	}
      }
    }
    setRawFileFields(dims, quant, skipBytes, convertType);
  }
  /**
   * Calculates and sets the raw increments to standard values
   * based on the raw dimensions.
   */
  public void setIncsFromDims() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setIncsFromDims(); }
      } );
    }
    else {
      int dims[] = getRawDims();
      if(dims != null) {
	int type = CNUTypes.DATA_TYPES[dataTypeCH.getSelectedIndex()];
	setRawFileIncs(new CNUDimensions(dims, type, 0));
      }
      else Toolkit.getDefaultToolkit().beep();
    }
  }
  /**
   * Gets the raw skip bytes from the skip bytes text field.
   *
   * @return	the number of bytes to skip
   */
  public long getRawSkipBytes() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = new Long(getRawSkipBytes());	}
      };
      runWithReturn.invokeAndWait();
      return ((Long) runWithReturn.returnObject).longValue();
    }
    else {
      long oldSkipBytes = CNURawImgFile.getDefaultSkipBytes();
      long skipBytes;
      try {
        skipBytes = Long.parseLong(skipBytesTF.getText());
      } catch (NumberFormatException e1) {
        Toolkit.getDefaultToolkit().beep();
	skipBytes = oldSkipBytes;
        skipBytesTF.setText(Long.toString(oldSkipBytes));
      }
      return skipBytes;
    }
  }
  /**
   * Gets the raw dimensions only, excluding increments, etc.  Returns
   * <code>null</code> if one or more fields is bad and resets the bad
   * fields to their previous values.
   * Should only be called from the event dispatching thread.
   *
   * @return	array containing the raw dimensions or <code>null</code>
   *		if an error occured reading the dimension fields
   */
  private int[] getRawDims() {
    boolean error_happened = false;
    CNUDimensions oldDim = CNURawImgFile.getDefaultDimensions();
    int dims[] = new int[4];
    try {
      dims[0] = Integer.parseInt(xDimTF.getText());
    } catch (NumberFormatException e1) {
      xDimTF.setText(Integer.toString(oldDim.xdim()));
      error_happened = true;
    }
    try {
      dims[1] = Integer.parseInt(yDimTF.getText());
    } catch (NumberFormatException e1) {
      yDimTF.setText(Integer.toString(oldDim.ydim()));
      error_happened = true;
    }
    try {
      dims[2] = Integer.parseInt(zDimTF.getText());
    } catch (NumberFormatException e1) {
      zDimTF.setText(Integer.toString(oldDim.zdim()));
      error_happened = true;
    }
    try {
      dims[3] = Integer.parseInt(iDimTF.getText());
    } catch (NumberFormatException e1) {
      iDimTF.setText(Integer.toString(oldDim.idim()));
      error_happened = true;
    }
    if(error_happened) return null;
    return dims;
  }
  /**
   * Gets the raw cnu dimensions from the fields.
   *
   * @return dimensions
   */
  public CNUDimensions getRawDimensions() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = getRawDimensions(); }
      };
      runWithReturn.invokeAndWait();
      return (CNUDimensions) runWithReturn.returnObject;
    }
    else {
      CNUDimensions oldDim = CNURawImgFile.getDefaultDimensions();
      boolean error_happened = false;
      CNUDimensions newDim;
      int type = CNUTypes.DATA_TYPES[dataTypeCH.getSelectedIndex()];

      int dims[] = getRawDims();
      if(dims == null) {
	error_happened = true;
	dims = new int[4];
	dims[0] = oldDim.xdim(); dims[1] = oldDim.ydim();
	dims[2] = oldDim.zdim(); dims[3] = oldDim.idim();
      }
      newDim = new CNUDimensions(dims, type, 0);
      if(incrementsCB.isSelected()) {
        int incs[] = new int[4];
        try {
          incs[0] = Integer.parseInt(xIncTF.getText());
        } catch (NumberFormatException e1) {
          incs[0] = oldDim.getIncrement(0);
          xIncTF.setText(Integer.toString(incs[0]));
          error_happened = true;
        }
        try {
          incs[1] = Integer.parseInt(yIncTF.getText());
        } catch (NumberFormatException e1) {
          incs[1] = oldDim.getIncrement(1);
          yIncTF.setText(Integer.toString(incs[1]));
          error_happened = true;
        }
        try {
          incs[2] = Integer.parseInt(zIncTF.getText());
        } catch (NumberFormatException e1) {
          incs[2] = oldDim.getIncrement(2);
          zIncTF.setText(Integer.toString(incs[2]));
          error_happened = true;
        }
        try {
          incs[3] = Integer.parseInt(iIncTF.getText());
        } catch (NumberFormatException e1) {
          incs[3] = oldDim.getIncrement(3);
          iIncTF.setText(Integer.toString(incs[3]));
          error_happened = true;
        }
        newDim.setIncrements(incs);
      }

      newDim.setOrientation(
        CNUDimensions.ORIENTATIONS[orientationCH.getSelectedIndex()]);

      int orientationOrder = 0;
      if(leftRightCH.getSelectedIndex() == 0)
        orientationOrder |= CNUDimensions.LEFT_POSITIVE;
      else orientationOrder |= CNUDimensions.RIGHT_POSITIVE;
      if(anteriorPosteriorCH.getSelectedIndex() == 0)
        orientationOrder |= CNUDimensions.ANTERIOR_POSITIVE;
      else orientationOrder |= CNUDimensions.POSTERIOR_POSITIVE;
      if(inferiorSuperiorCH.getSelectedIndex() == 0)
        orientationOrder |= CNUDimensions.INFERIOR_POSITIVE;
      else orientationOrder |= CNUDimensions.SUPERIOR_POSITIVE;

      newDim.setOrientationOrder(orientationOrder);

      double ress[] = new double[4];
      try {
        ress[0] = (Double.valueOf(xResTF.getText())).doubleValue();
      } catch (NumberFormatException e1) {
        ress[0] = oldDim.getSpatialRes(0);
        xResTF.setText(Double.toString(ress[0]));
        error_happened = true;
      }
      try {
        ress[1] = (Double.valueOf(yResTF.getText())).doubleValue();
      } catch (NumberFormatException e1) {
        ress[1] = oldDim.getSpatialRes(1);
        yResTF.setText(Double.toString(ress[1]));
        error_happened = true;
      }
      try {
        ress[2] = (Double.valueOf(zResTF.getText())).doubleValue();
      } catch (NumberFormatException e1) {
        ress[2] = oldDim.getSpatialRes(2);
        zResTF.setText(Double.toString(ress[2]));
        error_happened = true;
      }
      try {
        ress[3] = (Double.valueOf(iResTF.getText())).doubleValue();
      } catch (NumberFormatException e1) {
        ress[3] = oldDim.getSpatialRes(3);
        iResTF.setText(Double.toString(ress[3]));
        error_happened = true;
      }
      newDim.setSpatialResolutions(ress);
      if(error_happened) {
	Toolkit.getDefaultToolkit().beep();
	return null;
      }
      return newDim;
    }
  }
  /**
   * Gets the raw quantification factor from the quantification field.
   *
   * @return quantification factor
   */
  public double getRawQuantificationFactor() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() {
	  returnObject = new Double(getRawQuantificationFactor());
	}
      };
      runWithReturn.invokeAndWait();
      return ((Double) runWithReturn.returnObject).doubleValue();
    }
    else {
      double quant = CNURawImgFile.getDefaultQuantificationFactor();
      double oldQuant = quant;
      try {
        quant =
	  (Double.valueOf(quantificationFactorTF.getText())).doubleValue();
      } catch (NumberFormatException e1) {
        Toolkit.getDefaultToolkit().beep();
        quant = oldQuant;
        quantificationFactorTF.setText(Double.toString(quant));
      }
      return quant;
    }
  }
  /**
   * Sets wether increments are set manual or automaticly based on dims.
   *
   * @param state <code>true</code> to enable manaul increments.
   */
  public void setManualIncrementsEnabled(boolean state) {
    if(! SwingUtilities.isEventDispatchThread()) {
      final boolean fstate = state;
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setManualIncrementsEnabled(fstate); }
      } );
    }
    else {
      if(incrementsCB.isSelected() != state) incrementsCB.setSelected(state);
      state = state & incrementsCB.isEnabled();
      calculateIncsB.setEnabled(state);
      xIncTF.setEnabled(state);
      yIncTF.setEnabled(state);
      zIncTF.setEnabled(state);
      iIncTF.setEnabled(state);
    }
  }
  /**
   * Interprets action events over this dialog.
   *
   * @param e	event to interpret
   */
  public void actionPerformed(ActionEvent e) {
    getCNUViewer().setWaitCursor();
    try {
      Object source = e.getSource();
      if (source == applyToDefaultsB) {
        if(! applyRawFieldsToDefaults()) Toolkit.getDefaultToolkit().beep();
      }
      else if(source == setToCurrentB) setRawFieldsToCurrent();
      else if(source == calculateIncsB) setIncsFromDims();
      else if(source == addClassB) {
	if(! getCNUViewer().addFileClass(addClassTF.getText()))
	  Toolkit.getDefaultToolkit().beep();
      }
      else if(source == dismissB) setVisible(false);
    } finally {
      getCNUViewer().setNormalCursor();
    }
  }
  /**
   * Handles ItemListener events.
   *
   * @param e	event to handle
   */
  public void itemStateChanged(ItemEvent e){
    getCNUViewer().setWaitCursor();
    try {
      if (e.getSource() == incrementsCB)
	setManualIncrementsEnabled(incrementsCB.isSelected());
    } finally {
      getCNUViewer().setNormalCursor();
    }
  }
}
