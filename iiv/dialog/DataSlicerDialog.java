package iiv.dialog;
import iiv.io.*;
import iiv.*;
import iiv.data.*;
import iiv.script.*;
import iiv.util.*;
import iiv.gui.*;
import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.lang.*;
import java.net.*;
import java.util.Vector;

/**
 * Dialog to allow users to control data slicing.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUViewer
 * @since	iiV1.183
 */
public class DataSlicerDialog extends CNUDialog
implements CNUFileObject, ActionListener, ItemListener {
  private static final long serialVersionUID = -6861772105597903446L;
  private Object fieldsLock = new Object();
  private JTextField dataSlicerTF = new JTextField(30);
  private JButton browseDataSlicerB = new JButton("Browse");
  private JButton currentB = new JButton("Set to Current");
  private JButton setNullB = new JButton("Set to null");
  private class SlicerListElement {
    public CNUDataSlicer ds = null;
    public SlicerListElement() {}
    public SlicerListElement(CNUDataSlicer ds) { this.ds = ds; }
    public String toString() {
      StringBuffer s = new StringBuffer();
      s.append("[");
      if(ds == null)s.append("null"); else s.append(ds.toString());
      s.append("]");
      return s.toString();
    }
    public boolean equals(Object dsp) {
      if(! (dsp instanceof SlicerListElement)) return false;
      return ((SlicerListElement) dsp).ds.equals(ds);
    }
  }
  private DefaultComboBoxModel slicerFileList = new DefaultComboBoxModel();
  private JList sliceFileL = new JList(slicerFileList);
  private JTextArea sliceTextA = new JTextArea();

  private class SlicerValueComponent extends Box {
    private static final long serialVersionUID = 2340815103877216930L;
    private JLabel dataSlicerType = new JLabel();
    private JComboBox sliceViewModeCH = new JComboBox();

    private JTextField r1c1TF = new JTextField(4);
    private JTextField r1c2TF = new JTextField(4);
    private JTextField r1c3TF = new JTextField(4);
    private JTextField r1c4TF = new JTextField(4);

    private JTextField r2c1TF = new JTextField(4);
    private JTextField r2c2TF = new JTextField(4);
    private JTextField r2c3TF = new JTextField(4);
    private JTextField r2c4TF = new JTextField(4);

    private JTextField r3c1TF = new JTextField(4);
    private JTextField r3c2TF = new JTextField(4);
    private JTextField r3c3TF = new JTextField(4);
    private JTextField r3c4TF = new JTextField(4);
 
    private JTextField r4c1TF = new JTextField("0", 4);
    private JTextField r4c2TF = new JTextField("0", 4);
    private JTextField r4c3TF = new JTextField("0", 4);
    private JTextField r4c4TF = new JTextField("1", 4);

    Box matrixBox = Box.createVerticalBox();
    

    public SlicerValueComponent() {super(BoxLayout.Y_AXIS);}
    public SlicerValueComponent(CNUDataSlicer ds) {
      super(BoxLayout.Y_AXIS);
      for(int i=0; i<CNUDimensions.ORIENTATIONS.length; i++)
	sliceViewModeCH.addItem(CNUDimensions.orientationToString(CNUDimensions.ORIENTATIONS[i]));
      sliceViewModeCH.setSelectedIndex(0);
      this.add(dataSlicerType);
      this.add(sliceViewModeCH);
      this.add(matrixBox);
      Box box = Box.createHorizontalBox();
      matrixBox.add(box);
      box.add(Box.createHorizontalStrut(5));
      box.add(r1c1TF); box.add(r1c2TF); box.add(r1c3TF); box.add(r1c4TF);
      box.add(Box.createHorizontalGlue());
      box = Box.createHorizontalBox();
      matrixBox.add(box);
      box.add(Box.createHorizontalStrut(5));
      box.add(r2c1TF); box.add(r2c2TF); box.add(r2c3TF); box.add(r2c4TF);
      box.add(Box.createHorizontalGlue());
      box = Box.createHorizontalBox();
      matrixBox.add(box);
      box.add(Box.createHorizontalStrut(5));
      box.add(r3c1TF); box.add(r3c2TF); box.add(r3c3TF); box.add(r3c4TF);
      box.add(Box.createHorizontalGlue());
      box = Box.createHorizontalBox();
      matrixBox.add(box);
      box.add(Box.createHorizontalStrut(5));
      box.add(r4c1TF); r4c1TF.setEditable(false);
      box.add(r4c2TF); r4c2TF.setEditable(false);
      box.add(r4c3TF); r4c3TF.setEditable(false);
      box.add(r4c4TF); r4c4TF.setEditable(false);
      box.add(Box.createHorizontalGlue());

      init(ds);
    }
    public void init(CNUDataSlicer ds) {
      int sliceViewMode = CNUDimensions.ORIENTATIONS[0];
      if(ds == null) {
	dataSlicerType.setVisible(false);
	sliceViewModeCH.setVisible(false);
	matrixBox.setVisible(false);
      }
      else {
	dataSlicerType.setText(ds.getClass().getName());
	if(ds instanceof PrimaryOrthoDataSlicer) {	
	  sliceViewMode = ((PrimaryOrthoDataSlicer) ds).getSliceViewMode();
	  sliceViewModeCH.setVisible(true);
	  matrixBox.setVisible(false);
	}
	else if(ds instanceof AffineDataSlicer) {
	  sliceViewMode = ((AffineDataSlicer) ds).getSliceViewMode();
	  sliceViewModeCH.setVisible(true);
	  AffineMatrix am = ((AffineDataSlicer) ds).getAffineMatrix();
	  r1c1TF.setText(Double.toString(am.value(0,0)));
	  r1c2TF.setText(Double.toString(am.value(0,1)));
	  r1c3TF.setText(Double.toString(am.value(0,2)));
	  r1c4TF.setText(Double.toString(am.value(0,3)));

	  r2c1TF.setText(Double.toString(am.value(1,0)));
	  r2c2TF.setText(Double.toString(am.value(1,1)));
	  r2c3TF.setText(Double.toString(am.value(1,2)));
	  r2c4TF.setText(Double.toString(am.value(1,3)));

	  r3c1TF.setText(Double.toString(am.value(2,0)));
	  r3c2TF.setText(Double.toString(am.value(2,1)));
	  r3c3TF.setText(Double.toString(am.value(2,2)));
	  r3c4TF.setText(Double.toString(am.value(2,3)));
	  matrixBox.setVisible(true);
	}
	else {
	  sliceViewModeCH.setVisible(false);
	  matrixBox.setVisible(false);
	}
	dataSlicerType.setVisible(true);
      }

      for(int i=0; i<CNUDimensions.ORIENTATIONS.length; i++) {
	if(CNUDimensions.ORIENTATIONS[i] == sliceViewMode) {
	  sliceViewModeCH.setSelectedIndex(i);
	  break;
	}
      }
      sliceViewModeCH.setEnabled(false);
    }

  }
  private Box slicerValuesBox = Box.createVerticalBox();
  private JCheckBox enableEditsCB = new JCheckBox("Enable Edits", false);
  private JButton saveB = new JButton("Save");
  private JButton acceptB = new JButton("Accept");

  private JButton applySlicerB = new JButton("Apply Data Slicer");
  private JButton dismissB = new JButton("Dismiss");

  private CNUDataSlicer currentDataSlicer = null;
  /**
   * Constructs a new instance of DataSlicerDialog.
   *
   * @param parentFrame	parent frame needed for building a window
   */
  public DataSlicerDialog(Frame parentFrame) { this(parentFrame, null); }
  /**
   * Constructs a new instance of DataSlicerDialog.
   *
   * @param parentFrame	parent frame needed for building a window
   * @param cnuv	CNUViewer that this window interacts with
   */
  public DataSlicerDialog(Frame parentFrame, CNUViewer cnuv) {
    super(parentFrame, "Data Slicing", false, cnuv);
    Container contentPane = getContentPane();
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    contentPane.add(Box.createVerticalStrut(5));

    Box box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(browseDataSlicerB);
    browseDataSlicerB.addActionListener(this);
    box.add(Box.createHorizontalGlue());
    box.add(currentB);
    currentB.addActionListener(this);
    box.add(setNullB);
    setNullB.addActionListener(this);
    box.add(Box.createHorizontalGlue());
    box.add(applySlicerB);
    applySlicerB.addActionListener(this);
    box.add(saveB);
    saveB.addActionListener(this);
    box.add(Box.createHorizontalStrut(5));

    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(dataSlicerTF);
    dataSlicerTF.addActionListener(this);
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    sliceFileL.setVisibleRowCount(4);
    box.add(new JScrollPane(sliceFileL));
    sliceFileL.addMouseListener(
      new MouseAdapter() {
        public void mouseClicked(MouseEvent event) {
	  if(event.getClickCount() == 2) {
	    int index = sliceFileL.locationToIndex(event.getPoint());
	    if((index >= 0) && (index < slicerFileList.getSize())) {
	      setDataSlicer(
		((SlicerListElement) slicerFileList.getElementAt(index)).ds);
	    }
	    else Toolkit.getDefaultToolkit().beep();
          }
	}
      }
    );
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(new JScrollPane(sliceTextA));
    box.add(Box.createRigidArea(new Dimension(0, 100)));
    box.add(Box.createHorizontalStrut(5));

    contentPane.add(slicerValuesBox);

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(enableEditsCB);
    enableEditsCB.addItemListener(this);
    box.add(acceptB);
    acceptB.addActionListener(this);
    box.add(Box.createHorizontalGlue());
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(dismissB);
    dismissB.addActionListener(this);
    box.add(Box.createHorizontalGlue());
    box.add(Box.createHorizontalStrut(5));

    setEnableEdits(false);
    updateFields();
    contentPane.add(Box.createVerticalStrut(5));

    if((cnuv == null) || cnuv.runningAsApplet()) {
	saveB.setVisible(false);
	browseDataSlicerB.setVisible(false);
    }
    pack();
  }
  /**
   * Gets the current DataSlicer.
   *
   * @return	the current data slicer
   */
  public CNUDataSlicer getDataSlicer() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = currentDataSlicer; }
      };
      runWithReturn.invokeAndWait();
      return (CNUDataSlicer) runWithReturn.returnObject;
    }
    else return currentDataSlicer;
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
    if((cnuviewer == null) || cnuviewer.runningAsApplet()) {
	saveB.setVisible(false);
	browseDataSlicerB.setVisible(false);
    }
    else {
	saveB.setVisible(true);
	browseDataSlicerB.setVisible(true);
    }
  }
  /**
   * Sets the current data slicer.
   *
   * @param dataSlicer	new current coordinate map
   */
  public void setDataSlicer(final CNUDataSlicer dataSlicer) {
    if(dataSlicer == null) Toolkit.getDefaultToolkit().beep();
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setDataSlicer(dataSlicer); }
      } );
    }
    else {
      this.currentDataSlicer = dataSlicer;
      updateFields();
      addToSlicers(dataSlicer);
    }
  }
  /**
   * Updates the fields to reflect current coordinate map.
   * Should only be called from event dispatch thread.
   */
  private void updateFields() {
    CNUDataSlicer dataSlicer = getDataSlicer();
    slicerValuesBox.removeAll();
    if(dataSlicer == null) {
      sliceTextA.setText("");
      slicerValuesBox.setVisible(false);
    }
    else {
      if(dataSlicer != null) sliceTextA.setText(dataSlicer.toScript(null));
      slicerValuesBox.add(new SlicerValueComponent(dataSlicer));
      slicerValuesBox.setVisible(true);
    }
    
    slicerValuesBox.validate();
    validate();
  }
  /**
   * Gets an XYZDouble from the text values.
   *
   * @param xtext	text representing x value
   * @param ytext	text representing y value
   * @param ztext	text representing z value
   * @param defaults	values to assign if format exception occurs
   * @return		xyz double value
   */
  public static XYZDouble getXYZDouble(String xtext, String ytext,
				       String ztext, XYZDouble defaults) {
    // set up null or error settings
    XYZDouble xyznumber = null;
    double x = 0; double y = 0; double z = 0;
    if(defaults != null) { x = defaults.x; y = defaults.y; z = defaults.z; }
    boolean isNull = true;
    if("".equals(xtext)) x = 0;
    else {
      isNull = false;
      try { x = Double.valueOf(xtext).doubleValue();
      } catch (NumberFormatException e1) {
	Toolkit.getDefaultToolkit().beep();
      }
    }
    if("".equals(ytext)) y = 0;
    else {
      isNull = false;
      try { y = Double.valueOf(ytext).doubleValue();
      } catch (NumberFormatException e1) {
	Toolkit.getDefaultToolkit().beep();
      }
    }
    if("".equals(ztext)) z = 0;
    else {
      isNull = false;
      try { z = Double.valueOf(ztext).doubleValue();
      } catch (NumberFormatException e1) {
	Toolkit.getDefaultToolkit().beep();
      }
    }
    if(! isNull) xyznumber = new XYZDouble(x, y, z);
    return xyznumber;
  }
  /**
   * Adds new maps to the list.
   *
   * @param dataSlicer	map to add to list
   */
  public void addToSlicers(final CNUDataSlicer dataSlicer) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { addToSlicers(dataSlicer); }
      } );
    }
    else {
      if(dataSlicer == null) { sliceFileL.clearSelection(); return; }
      SlicerListElement mle = new SlicerListElement(dataSlicer);
      int index = slicerFileList.getIndexOf(mle);
      if(index < 0) {
	// add only new coordinate map
	index = slicerFileList.getSize();
	slicerFileList.addElement(mle);
      }
      sliceFileL.setSelectedIndex(index);
      sliceFileL.ensureIndexIsVisible(index);
    }
  }
  /**
   * Changes a file name in the filename list.
   *
   * @param dataSlicer	map with new name
   */
  public void replaceCoordinateFileName(final CNUDataSlicer dataSlicer) {
    if(dataSlicer == null) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { replaceCoordinateFileName(dataSlicer); }
      } );
    }
    else {
      SlicerListElement mle = new SlicerListElement(dataSlicer);
      int index = slicerFileList.getIndexOf(mle);
      if(index >= 0) {
	// really just need to fire contents changed event
	slicerFileList.removeElementAt(index);
	slicerFileList.insertElementAt(mle, index);
      }
    }
  }
  /**
   * Gets the coordinate map associated with a file object.
   *
   * @param  sameFileObj	file object to check file name against
   * @return			object associated with same file
   */
  public Object getFileObject(final Object sameFileObj) {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = getFileObject(sameFileObj); }
      };
      runWithReturn.invokeAndWait();
      return runWithReturn.returnObject;
    }
    else {
      for(int i = slicerFileList.getSize() - 1; i >= 0; i--) {
        CNUDataSlicer dataSlicer =
	  ((SlicerListElement) slicerFileList.getElementAt(i)).ds;
	if(dataSlicer instanceof CNUFileObject) {
	  Object obj = ((CNUFileObject) dataSlicer).getFileObject(sameFileObj);
	  if(obj != null) return obj;
	}
      }
      return null;
    }
  }
  /**
   * Reads the data slicer from a file.
   *
   * @param filename	file name
   * @return		data slicer read from file
   */
  public CNUDataSlicer readDataSlicer(String filename) {
    try {
      return  ReadDataSlicerScript(new CNUFile(filename));
    } catch (IOException ioe) {
      showStatus("Failed reading data slicer from file " + filename);
      showStatus(ioe);
      Toolkit.getDefaultToolkit().beep();
    }
    return null;
  }
  /**
   * Enables or disables edits.
   *
   * @param state	<code>true</code> to enable edits
   */
  public void setEnableEdits(final boolean state) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setEnableEdits(state); }
      } );
    }
    else {
      if(state != enableEditsCB.isSelected()) enableEditsCB.setSelected(state);
      acceptB.setEnabled(state);
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
      if (e.getSource() == enableEditsCB) setEnableEdits(enableEditsCB.isSelected());
    } finally {
      getCNUViewer().setNormalCursor();
    }
  }
  /**
   * Displays a file dialog to browse files.
   */
  public void browseFiles() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { browseFiles(); }
      } );
    }
    else {
      String path = dataSlicerTF.getText();
      if(path != null) {
	path = path.trim();
	if(path.equals("")) path = null;
      }
      if(path == null)
	path = System.getProperty("CNU.DataSlicerDialog.path", "");
      FileDialog fd = getCNUViewer().getFileDialog(path);
      fd.setVisible(true);
      if(fd.getFile() != null) {
	dataSlicerTF.setText(fd.getDirectory() + fd.getFile());
	setDataSlicer(readDataSlicer(dataSlicerTF.getText()));
      }
    }
  }
  /**
   * Saves a data slicer to a file.
   *
   * @param dataSlicer	data slicer to save
   */
  public void saveDataSlicer(final CNUDataSlicer dataSlicer) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() { saveDataSlicer(dataSlicer); }
	} );
    }
    else {
      String filename = dataSlicerTF.getText();
      FileDialog SFD = getCNUViewer().getSaveFileDialog(filename);
      SFD.setVisible(true);
      filename = SFD.getFile();
      if(filename != null) {
	CNUFile cnufile = new CNUFile(SFD.getDirectory() + filename);
	try {
	  writeDataSlicerScript(cnufile, getCNUViewer().getContinueDialog(), dataSlicer);
	  replaceCoordinateFileName(dataSlicer);
	} catch (IOException ioe) {
	  showStatus("Failed writing data slicer to file");
	  showStatus(ioe);
	  Toolkit.getDefaultToolkit().beep();
	}
      }
      else {
	showStatus("Data slicer not written to file");
	Toolkit.getDefaultToolkit().beep();
      }
    }
  }
  /**
   * Writes a data slicer to a script file.
   *
   * @param cnufile	CNUFile to write to.
   * @param cd		Determines how to handle write overs if file
   *			exists.
   *			If cd is <code>null</code>, the file is written
   *			over with no warning,
   *			else if cd <code>instanceof ContinueDialog</code>, the
   *			dialog is used to query the user before writting over,
   *			else no write over is performed.
   * @param dataSlicer	data slicer to write to file.
   * @exception	IOException	thrown if error writing to cnufile.
   */
  public static void writeDataSlicerScript(CNUFile cnufile, Object cd,
					   CNUDataSlicer dataSlicer)
	throws IOException {
    if(cnufile == null) throw new IOException("Missing cnufile");
    if(dataSlicer == null) throw new IOException("null data slicer");
    if( cnufile.exists() ) {
      if(cd == null) throw
	new IOException("WriteDataSlicerScript - attempt to write over existing file "
	+ cnufile);
      if(cd instanceof ContinueDialog) {
        ((ContinueDialog)cd).beep();
        ((ContinueDialog)cd).setVisible(true);
        if( ! ((ContinueDialog)cd).getContinueFlag() ) throw
	  new IOException("WriteDataSlicerScript - did not write file " + cnufile);
      }
    }
    Writer wr = null;
    try {
      wr = cnufile.getWriter();
      wr.write(iiVBshScript.getHeaderScript());
      wr.write(dataSlicer.toScript(null));
    } finally {
      if(wr != null) wr.close();
    }
  }
  /**
   * Reads a data slicer from a script.
   *
   * @param cnufile		CNUFile to read from
   * @return			the color model
   * @exception	IOException	thrown if error reading from cnufile
   */
  public static CNUDataSlicer ReadDataSlicerScript(CNUFile cnufile)
	throws IOException {
    if(cnufile == null) throw new IOException("Missing cnufile");
    Reader rd = null;
    try {
      Object obj = null;
      rd = cnufile.getReader();
      if(CNUDisplayScript.isCNUDisplayScript(rd) ) {
	rd.close();
	rd = cnufile.getReader(); // start with a new reader at beginning of file
	obj =
	  CNUDisplayScript.readComponentsFromScript(rd, null, null, null, null);
      } else {
	rd.close();
	rd = cnufile.getReader(); // start with a new reader at beginning of file
	if(iiVBshScript.isiiVBshScript(rd) ) {
	  rd.close();
	  rd = cnufile.getReader(); // start with a new reader at beginning of file
	  obj = iiVBshScript.readComponentsFromScript(rd, null, null, null, null);
	}
	else throw new IOException("File does not contain an iiV script");
      }
      if(! (obj instanceof CNUDataSlicer))
        throw new IOException("iiV script did not contain a CNUDataSlicer");
      //      ((CNUDataSlicer) obj).setCNUFile(cnufile);
      //      ((CNUDataSlicer) obj).setSaved(true);
      return (CNUDataSlicer) obj;
    } finally {
      if(rd != null) rd.close();
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
      if (e.getSource() == dataSlicerTF)
	setDataSlicer(readDataSlicer(dataSlicerTF.getText()));
      else if (e.getSource() == currentB) {
	CNUDataSlicer dataSlicer = getCNUDisplay().getCurrentDataSlicer();
        setDataSlicer(dataSlicer);
      }
      else if (e.getSource() == setNullB) {
        dataSlicerTF.setText("");
        setDataSlicer(null);
      }
      //      else if (e.getSource() == acceptB) setDataSlicer(getMapFromFields());
      else if (e.getSource() == saveB) saveDataSlicer(getDataSlicer());
      else if (e.getSource() == applySlicerB) {
        getCNUDisplay().setFeature("DataSlicer", getDataSlicer());
      }
      else if (e.getSource() == browseDataSlicerB) browseFiles();
      else if(e.getSource() == dismissB) {
        setVisible(false);
      }
    } finally {
      getCNUViewer().setNormalCursor();
    }
  }
}
