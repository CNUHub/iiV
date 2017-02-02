package iiv.dialog;
import iiv.io.*;
import iiv.*;
import iiv.data.*;
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
 * Dialog to get coordinate map factors from user.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUViewer
 * @since	iiV1.0
 */
public class CoordinateMapDialog extends CNUDialog
implements CNUFileObject, ActionListener, ItemListener {
  private static final long serialVersionUID = 1416867920437143154L;
  private Object fieldsLock = new Object();
  private JTextField mapFileTF = new JTextField(30);
  private JButton browseMapFileB = new JButton("Browse");
  private JButton currentB = new JButton("Set to Current");
  private JButton setNullB = new JButton("Set to null");
  private class MapListElement {
    public CoordinateMap cm = null;
    public MapListElement() {}
    public MapListElement(CoordinateMap cm) { this.cm = cm; }
    public String toString() {
      StringBuffer s = new StringBuffer();
      s.append("[").append((cm == null) ? "null" : cm.toString()).append("]");
      if(cm instanceof LinearCoordinateMap)
	s.append(((LinearCoordinateMap) cm).getFullName());
      return s.toString();
    }
    public boolean equals(Object cmp) {
      if(! (cmp instanceof MapListElement)) return false;
      return ((MapListElement) cmp).cm.equals(cm);
    }
  }
  private DefaultComboBoxModel mapFileList = new DefaultComboBoxModel();
  private JList mapFileL = new JList(mapFileList);
  private JCheckBox enableEditsCB = new JCheckBox("Enable Edits", false);
  private JButton saveB = new JButton("Save");
  private JButton acceptB = new JButton("Accept");
  private JComboBox originUnitsCH = new JComboBox();
  private JTextField originXTF = new JTextField();
  private JTextField originYTF = new JTextField();
  private JTextField originZTF = new JTextField();
  private JTextField scaleXTF = new JTextField();
  private JTextField scaleYTF = new JTextField();
  private JTextField scaleZTF = new JTextField();
  private JComboBox rotationUnitsCH = new JComboBox();
  private JTextField rotationXTF = new JTextField();
  private JTextField rotationYTF = new JTextField();
  private JTextField rotationZTF = new JTextField();
  private JButton applyMapB = new JButton("Apply Coordinate Map");
  private JButton dismissB = new JButton("Dismiss");

  private XYZDouble displayedOrigin = null;
  private String displayedOriginXText = "";
  private String displayedOriginYText = "";
  private String displayedOriginZText = "";
  private int displayedOriginUnits = CoordinateMap.METERS;
  private XYZDouble displayedScale = null;
  private String displayedScaleXText = "";
  private String displayedScaleYText = "";
  private String displayedScaleZText = "";
  private XYZDouble displayedRotation = null;
  private String displayedRotationXText = "";
  private String displayedRotationYText = "";
  private String displayedRotationZText = "";
  private int displayedRotationUnits = CoordinateMap.DEGREES;
  private CoordinateMap coorM = LinearCoordinateMap.getDefaultCoordinateMap();
  /**
   * Constructs a new instance of CoordinateMapDialog.
   *
   * @param parentFrame	parent frame needed for building a window
   */
  public CoordinateMapDialog(Frame parentFrame) { this(parentFrame, null); }
  /**
   * Constructs a new instance of CoordinateMapDialog.
   *
   * @param parentFrame	parent frame needed for building a window
   * @param cnuv	CNUViewer that this window interacts with
   */
  public CoordinateMapDialog(Frame parentFrame, CNUViewer cnuv) {
    super(parentFrame, "Coordinate Mapping", false, cnuv);
    Container contentPane = getContentPane();
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    contentPane.add(Box.createVerticalStrut(5));

    Box box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(browseMapFileB);
    browseMapFileB.addActionListener(this);
    box.add(Box.createHorizontalGlue());
    box.add(currentB);
    currentB.addActionListener(this);
    box.add(setNullB);
    setNullB.addActionListener(this);
    box.add(Box.createHorizontalGlue());
    box.add(applyMapB);
    applyMapB.addActionListener(this);
    box.add(saveB);
    saveB.addActionListener(this);
    box.add(Box.createHorizontalStrut(5));

    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(mapFileTF);
    mapFileTF.addActionListener(this);
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    mapFileL.setVisibleRowCount(4);
    box.add(new JScrollPane(mapFileL));
    mapFileL.addMouseListener(
      new MouseAdapter() {
        public void mouseClicked(MouseEvent event) {
	  if(event.getClickCount() == 2) {
	    int index = mapFileL.locationToIndex(event.getPoint());
	    if((index >= 0) && (index < mapFileList.getSize())) {
	      setCoordinateMap(
		((MapListElement) mapFileList.getElementAt(index)).cm);
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
    box.add(enableEditsCB);
    enableEditsCB.addItemListener(this);
    box.add(acceptB);
    acceptB.addActionListener(this);
    box.add(Box.createHorizontalGlue());
    box.add(Box.createHorizontalStrut(5));

    Container grid = new Container() {private static final long serialVersionUID = 5195136637926294098L;};
    grid.setLayout(new GridLayout(0, 5, 4, 3));
    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(grid);
    box.add(Box.createHorizontalGlue());
    box.add(Box.createHorizontalStrut(5));

    grid.add(new JLabel("Origin:  "));
    grid.add(originXTF);
    grid.add(originYTF);
    grid.add(originZTF);
    originUnitsCH.addItem("pixels");
    originUnitsCH.addItem("millimeters");
    grid.add(originUnitsCH);

    grid.add(new JLabel("Scale:  "));
    grid.add(scaleXTF);
    grid.add(scaleYTF);
    grid.add(scaleZTF);
    grid.add(Box.createHorizontalGlue());

    grid.add(new JLabel("Rotation:  "));
    grid.add(rotationXTF);

    rotationXTF.setEditable(false);
    grid.add(rotationYTF);
    rotationYTF.setEditable(false);
    grid.add(rotationZTF);
    rotationZTF.setEditable(false);

    rotationUnitsCH.addItem("degrees");
    rotationUnitsCH.addItem("radians");
    grid.add(rotationUnitsCH);
    rotationUnitsCH.addItemListener(this);

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
	browseMapFileB.setVisible(false);
    }
    pack();
  }
  /**
   * Gets the current CoordinateMap.
   *
   * @return	the current coordinate map
   */
  public CoordinateMap getCoordinateMap() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = coorM; }
      };
      runWithReturn.invokeAndWait();
      return (CoordinateMap) runWithReturn.returnObject;
    }
    else return coorM;
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
	browseMapFileB.setVisible(false);
    }
    else {
	saveB.setVisible(true);
	browseMapFileB.setVisible(true);
    }
  }
  /**
   * Sets the current coordinate map.
   *
   * @param coorM	new current coordinate map
   */
  public void setCoordinateMap(final CoordinateMap coorM) {
    if(coorM == null) Toolkit.getDefaultToolkit().beep();
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setCoordinateMap(coorM); }
      } );
    }
    else {
      this.coorM = coorM;
      updateFields();
      addToMaps(coorM);
    }
  }
  /**
   * Updates the fields to reflect current coordinate map.
   * Should only be called from event dispatch thread.
   */
  private void updateFields() {
    XYZDouble origin = null;
    int originUnits = CoordinateMap.UNKNOWN_UNITS;
    XYZDouble scale = null;
    XYZDouble rotation = null;
    CoordinateMap coorM = getCoordinateMap();
    LinearCoordinateMap LCM = null;
    if(coorM instanceof LinearCoordinateMap) LCM = (LinearCoordinateMap) coorM;
    else setEnableEdits(false);
    if(LCM != null) {
      origin = LCM.getOrigin();
      originUnits = LCM.getOriginUnits();
      scale = LCM.getScale();
      rotation = LCM.getRotation();
    }
    setOriginFields(origin, originUnits);
    setScaleFields(scale);
    setRotationFields(rotation);
    if(coorM instanceof LinearCoordinateMap) {
      String filename =((LinearCoordinateMap) coorM).getFullName();
      if(filename != null) mapFileTF.setText(filename);
    }
    else mapFileTF.setText("");
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
   * Gets the origin fields units.
   * Should only be called from event dispatch thread.
   *
   * @return	origin fields units
   */
  private int getOriginFieldsUnits() {
    int units = CoordinateMap.METERS;
    if("pixels".equals(originUnitsCH.getSelectedItem()))
      units = CoordinateMap.PIXELS;
    return units;
  }
  /**
   * Set the origin fields.
   * Should only be called from event dispatch thread.
   *
   * @param origin	value to set to
   * @param units	units to display in
   */
  private void setOriginFields(XYZDouble origin, int units) {
    if(origin == null) {
      displayedOriginXText = "";
      displayedOriginYText = "";
      displayedOriginZText = "";
      displayedOrigin = null;
    }
    else if(units == CoordinateMap.PIXELS) {
      originUnitsCH.setSelectedItem("pixels");
      displayedOriginXText = Double.toString(origin.x);
      displayedOriginYText = Double.toString(origin.y);
      displayedOriginZText = Double.toString(origin.z);
      displayedOriginUnits = CoordinateMap.PIXELS;
      displayedOrigin = new XYZDouble(origin);
    }
    else { // default to METERS
      originUnitsCH.setSelectedItem("millimeters");
      displayedOrigin = new XYZDouble(origin);
      displayedOrigin.scale(1e3);  // convert to millimeters for display
      displayedOriginXText = Double.toString(displayedOrigin.x);
      displayedOriginYText = Double.toString(displayedOrigin.y);
      displayedOriginZText = Double.toString(displayedOrigin.z);
      displayedOriginUnits = CoordinateMap.METERS;
    }
    originXTF.setText(displayedOriginXText);
    originYTF.setText(displayedOriginYText);
    originZTF.setText(displayedOriginZText);
  }
  /**
   * Gets the origin fields values.
   * Should only be called from event dispatch thread.
   *
   * @return	origin field values in meters
   */
  private XYZDouble getOriginFields() {
    XYZDouble origin = null;
    int units = getOriginFieldsUnits();
    String xtext = originXTF.getText().trim();
    String ytext = originYTF.getText().trim();
    String ztext = originZTF.getText().trim();
    if( displayedOriginXText.equals(xtext) &&
	displayedOriginYText.equals(ytext) &&
	displayedOriginZText.equals(ztext) &&
	units == displayedOriginUnits) {
      if(displayedOrigin == null) return null;
      else return new XYZDouble(displayedOrigin);
    }
    // get values from strings
    origin = getXYZDouble(xtext, ytext, ztext, displayedOrigin);
    if((origin != null) && (units == CoordinateMap.METERS))
      origin.scale(1e-3); // convert to meters for return
    setOriginFields(origin, units);
    return origin;
  }
  /**
   * Sets the scale text fields.
   * Should only be called from event dispatch thread.
   *
   * @param scale	values to set to
   */
  private void setScaleFields(XYZDouble scale) {
    if(scale == null) {
      displayedScaleXText = "";
      displayedScaleYText = "";
      displayedScaleZText = "";
      displayedScale = null;
    }
    else {
      displayedScaleXText = Double.toString(scale.x);
      displayedScaleYText = Double.toString(scale.y);
      displayedScaleZText = Double.toString(scale.z);
      displayedScale = new XYZDouble(scale);
    }
    scaleXTF.setText(displayedScaleXText);
    scaleYTF.setText(displayedScaleYText);
    scaleZTF.setText(displayedScaleZText);
  }
  /**
   * Gets the scale fields values.
   * Should only be called from event dispatch thread.
   *
   * @return	scale fields values
   */
  private XYZDouble getScaleFields() {
    XYZDouble scale = null;
    String xtext = scaleXTF.getText().trim();
    String ytext = scaleYTF.getText().trim();
    String ztext = scaleZTF.getText().trim();
    if( displayedScaleXText.equals(xtext) &&
	displayedScaleYText.equals(ytext) &&
	displayedScaleZText.equals(ztext) ) {
      if(displayedScale == null) return null;
      else return new XYZDouble(displayedScale);
    }
    // get values from strings
    scale = getXYZDouble(xtext, ytext, ztext, displayedScale);
    setScaleFields(scale);
    return scale;
  }
  /**
   * Gets the rotation choice units.
   *
   * @return	rotation units
   */
  public int getRotationFieldsUnits() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() {
	  returnObject = new Integer(getRotationFieldsUnits());
	}
      };
      runWithReturn.invokeAndWait();
      return ((Integer) runWithReturn.returnObject).intValue();
    }
    if("degrees".equals(rotationUnitsCH.getSelectedItem()))
      return CoordinateMap.DEGREES;
    return CoordinateMap.RADIANS;
  }
  /**
   * Sets the rotation fields.
   * Should only be called from event dispatch thread.
   *
   * @param rotation	rotation values
   */
  private void setRotationFields(XYZDouble rotation) {
    if(rotation == null) {
      displayedRotationXText = ""; displayedRotationYText = "";
      displayedRotationZText = "";
      displayedRotation = null;
    }
    else {
      double factor = 1.0;
      displayedRotationUnits = getRotationFieldsUnits();
      if(displayedRotationUnits == CoordinateMap.DEGREES)
	factor =  CoordinateMap.RADIANS_2_DEGREES;
      displayedRotation = new XYZDouble(rotation);
      displayedRotationXText = Double.toString(rotation.x * factor);
      displayedRotationYText = Double.toString(rotation.y * factor);
      displayedRotationZText = Double.toString(rotation.z * factor);
    }
    rotationXTF.setText(displayedRotationXText);
    rotationYTF.setText(displayedRotationYText);
    rotationZTF.setText(displayedRotationZText);
  }
  /**
   * Gets the rotation fields values.
   * Should only be called from event dispatch thread.
   *
   * @return	rotation values
   */
  private XYZDouble getRotationFields() {
    return getRotationFields(getRotationFieldsUnits());
  }
  /**
   * Gets the rotation fields values.
   * Should only be called from event dispatch thread.
   *
   * @param units	units
   * @return		rotation values
   */
  private XYZDouble getRotationFields(int units) {
    double value = 0;
    XYZDouble rotation = null;
    String xtext = rotationXTF.getText().trim();
    String ytext = rotationYTF.getText().trim();
    String ztext = rotationZTF.getText().trim();
    if( displayedRotationXText.equals(xtext) &&
	displayedRotationYText.equals(ytext) &&
	displayedRotationZText.equals(ztext) ) {
      if(displayedRotation == null) return null;
      else {
	if(units != getRotationFieldsUnits()) {
	  setRotationFields(displayedRotation); // corrects displayed units
	}
	return new XYZDouble(displayedRotation);
      }
    }
    // get values from strings
    rotation = getXYZDouble(xtext, ytext, ztext, displayedRotation);
    if(rotation != null) {
      if(units == CoordinateMap.DEGREES)
	rotation.scale(CoordinateMap.DEGREES_2_RADIANS);
    }
    setRotationFields(rotation);
    return rotation;
  }
  /**
   * Gets the coordinate map from the fields.
   * Should only be called from event dispatch thread.
   *
   * @return	the coordinate map
   */
  private LinearCoordinateMap getMapFromFields() {
    int originUnits = getOriginFieldsUnits();
    XYZDouble origin = getOriginFields();
    if(origin == null) return null;
    XYZDouble scale = getScaleFields();
    if(scale == null) return null;
    XYZDouble rotation = getRotationFields();
    if(rotation == null) return null;
    LinearCoordinateMap lcm = new LinearCoordinateMap();
    lcm.setOrigin(origin, originUnits);
    lcm.setScale(scale);
    lcm.setRotation(rotation);
    return lcm;
  }
  /**
   * Adds new maps to the list.
   *
   * @param coorM	map to add to list
   */
  public void addToMaps(final CoordinateMap coorM) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { addToMaps(coorM); }
      } );
    }
    else {
      if(coorM == null) { mapFileL.clearSelection(); return; }
      MapListElement mle = new MapListElement(coorM);
      int index = mapFileList.getIndexOf(mle);
      if(index < 0) {
	// add only new coordinate map
	index = mapFileList.getSize();
	mapFileList.addElement(mle);
      }
      mapFileL.setSelectedIndex(index);
      mapFileL.ensureIndexIsVisible(index);
    }
  }
  /**
   * Changes a file name in the filename list.
   *
   * @param coorM	map with new name
   */
  public void replaceCoordinateFileName(final CoordinateMap coorM) {
    if(coorM == null) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { replaceCoordinateFileName(coorM); }
      } );
    }
    else {
      MapListElement mle = new MapListElement(coorM);
      int index = mapFileList.getIndexOf(mle);
      if(index >= 0) {
	// really just need to fire contents changed event
	mapFileList.removeElementAt(index);
	mapFileList.insertElementAt(mle, index);
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
      for(int i = mapFileList.getSize() - 1; i >= 0; i--) {
        CoordinateMap coorM =
	  ((MapListElement) mapFileList.getElementAt(i)).cm;
	if(coorM instanceof CNUFileObject) {
	  Object obj = ((CNUFileObject) coorM).getFileObject(sameFileObj);
	  if(obj != null) return obj;
	}
      }
      return null;
    }
  }
  /**
   * Reads the coordinate map from a file.
   *
   * @param filename	file name
   * @return		coordinate map read from file
   */
  public CoordinateMap readCoordinateMap(String filename) {
    CNUViewer cnuv = getCNUViewer();
    return LinearCoordinateMap.readCoordinateMap(filename, cnuv, cnuv);
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
      originUnitsCH.setEnabled(state);
      originXTF.setEditable(state); originYTF.setEditable(state);
      originZTF.setEditable(state);
      scaleXTF.setEditable(state); scaleYTF.setEditable(state);
      scaleZTF.setEditable(state);
      rotationXTF.setEditable(state); rotationYTF.setEditable(state);
      rotationZTF.setEditable(state);
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
      if (e.getSource() == enableEditsCB)
	setEnableEdits(enableEditsCB.isSelected());
      else if (e.getSource() == rotationUnitsCH) {
	int newUnits = getRotationFieldsUnits();
	if(newUnits != displayedRotationUnits)
	  getRotationFields(displayedRotationUnits);
      }
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
      String path = mapFileTF.getText();
      if(path != null) {
	path = path.trim();
	if(path.equals("")) path = null;
      }
      if(path == null)
	path = System.getProperty("CNU.CoordinateMapDialog.path", "");
      FileDialog fd = getCNUViewer().getFileDialog(path);
      fd.setVisible(true);
      if(fd.getFile() != null) {
	mapFileTF.setText(fd.getDirectory() + fd.getFile());
	setCoordinateMap(readCoordinateMap(mapFileTF.getText()));
      }
    }
  }
  /**
   * Saves a coordinate map to a file.
   *
   * @param coorM	coordinate map to save
   */
  public void saveMapFile(final CoordinateMap coorM) {
    if(coorM instanceof LinearCoordinateMap) {
      if(! SwingUtilities.isEventDispatchThread()) {
	SwingUtilities.invokeLater( new Runnable() {
	  public void run() { saveMapFile(coorM); }
	} );
      }
      else {
	String filename = mapFileTF.getText();
	FileDialog SFD = getCNUViewer().getSaveFileDialog(filename);
	SFD.setVisible(true);
	filename = SFD.getFile();
	if(filename != null) {
	  CNUFile cnufile = new CNUFile(SFD.getDirectory() + filename);
	  try {
	    LinearCoordinateMap.writeMap(cnufile,
					 getCNUViewer().getContinueDialog(),
					 (LinearCoordinateMap) coorM);
	    ((LinearCoordinateMap) coorM).setCNUFile(cnufile);
	    ((LinearCoordinateMap) coorM).setSaved(true);
	    replaceCoordinateFileName(coorM);
	  } catch (IOException ioe) {
	    showStatus("Failed writing coordinate map to file");
	    showStatus(ioe);
	    Toolkit.getDefaultToolkit().beep();
	  }
	}
	else {
	  showStatus("Coordinate map not written to file");
	  Toolkit.getDefaultToolkit().beep();
	}
      }
    }
    else {
      showStatus("Coordinate map not written to file");
      Toolkit.getDefaultToolkit().beep();
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
      if (e.getSource() == mapFileTF)
	setCoordinateMap(readCoordinateMap(mapFileTF.getText()));
      else if (e.getSource() == currentB) {
	CoordinateMap coorM = getCNUDisplay().getCoordinateMap();
        setCoordinateMap(coorM);
      }
      else if (e.getSource() == setNullB) {
        mapFileTF.setText("");
        setCoordinateMap(null);
      }
      else if (e.getSource() == acceptB) setCoordinateMap(getMapFromFields());
      else if (e.getSource() == saveB) saveMapFile(getCoordinateMap());
      else if (e.getSource() == applyMapB) {
        LinearCoordinateMap.setDefaultCoordinateMap(getCoordinateMap());
        getCNUDisplay().updateCoordinateMap();
      }
      else if (e.getSource() == browseMapFileB) browseFiles();
      else if(e.getSource() == dismissB) {
        setVisible(false);
      }
    } finally {
      getCNUViewer().setNormalCursor();
    }
  }
}
