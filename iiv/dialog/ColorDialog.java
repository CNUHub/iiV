package iiv.dialog;
import iiv.*;
import iiv.io.*;
import iiv.util.*;
import iiv.display.*;
import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.lang.*;
import java.net.*;
import java.util.Vector;
import java.util.Enumeration;

/**
 * Dialog to handle color model requests from user.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUColorModel
 * @see		CNUDialog
 */
public class ColorDialog extends CNUDialog
implements CNUFileObject, ActionListener
{
  private static final long serialVersionUID = -2313690831719342570L;
  private JTextField lookupTF = new JTextField(37) {
    private static final long serialVersionUID = 1691857464235684173L;
    public Dimension getMaximumSize() {
      return new Dimension(5000, getPreferredSize().height);
    }
  };

  private JButton applyBackgroundColorB = new JButton("Apply Background Color");
  private JButton browseLookupB = new JButton("Browse");
  private JButton currentB = new JButton("Set to Current");
  private JButton clarifyB = new JButton("Clarify");
  private JButton unclarifyB = new JButton("Unclarify");
  private JButton editB = new JButton("Edit");
  private JButton HorzColorBarB = new JButton("Add Horz Color Bar");
  private JButton VertColorBarB = new JButton("Add Vert Color Bar");
  private JButton ColorQuiltB = new JButton("Add Color Quilt");
  private JButton applyColorMapB = new JButton("Apply Color Map");
  private JButton applyToDefaultsB = new JButton("Apply to Default Only");
  private JButton dismissB = new JButton("Dismiss");

  private JComboBox backgroundColorCH = new JComboBox(colorNames);
  private class ColorListElement {
    public ColorModel cm = null;
    public ColorListElement() {}
    public ColorListElement(ColorModel cm) { this.cm = cm; }
    public String toString() {
      if(cm instanceof CNUColorModel) {
	String fullname=((CNUColorModel) cm).getFullName();
	if(fullname != null) return fullname;
      }
      StringBuffer s = new StringBuffer();
      s.append("[");
      s.append((cm == null) ? "null" : cm.toString());
      s.append("]");
      return s.toString();
    }
    public boolean equals(Object cmp) {
      if(! (cmp instanceof ColorListElement)) return false;
      if(cm == null) {
	if(((ColorListElement) cmp).cm == null) return true;
	else return false;
      }
      return cm.equals(((ColorListElement) cmp).cm);
    }
  }
  private DefaultComboBoxModel colorFileList = new DefaultComboBoxModel();
  private JList colorFileL = new JList(colorFileList);

  private ColorMapCanvas lkup = null;
  private Object cmLock = new Object();
  private ColorModel cm = null;
  /**
   * Constructs a new instance of ColorDialog.
   *
   * @param parentFrame	parent frame
   */
  public ColorDialog(Frame parentFrame) {
    this(parentFrame, null, null);
  }
  /**
   * Constructs a new instance of ColorDialog.
   *
   * @param parentFrame	parent frame
   * @param cnuv	CNUViewer to work with
   * @param cm		initial color model,
   *			if <code>null</code> sets to default grey
   */
  public ColorDialog(Frame parentFrame, CNUViewer cnuv, ColorModel cm) {
    super(parentFrame, "Color", false, cnuv);
    if(cm != null) setColorModel(cm);
    else setColorModel(DisplayComponentDefaults.getDefaultColorModel());

    Container contentPane = getContentPane();

    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    contentPane.add(Box.createVerticalStrut(5));

    Container box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));

    box.add(browseLookupB);
    browseLookupB.addActionListener(this);

    box.add(Box.createHorizontalGlue());
    box.add(currentB);
    currentB.addActionListener(this);

    box.add(Box.createHorizontalStrut(5));
    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(Box.createRigidArea(new Dimension(0, 80)));

    Box panel = Box.createVerticalBox();
    box.add(panel);

    panel.add(lookupTF);
    lookupTF.addActionListener(this);
    // keep list at least 4 long
    colorFileL.setVisibleRowCount(4);
    panel.add(new JScrollPane(colorFileL));
    colorFileL.addMouseListener(
      new MouseAdapter() {
        public void mouseClicked(MouseEvent event) {
	  if(event.getClickCount() == 2) {
	    int index = colorFileL.locationToIndex(event.getPoint());
	    if((index >= 0) && (index < colorFileList.getSize())) {
	      setColorModel(
		((ColorListElement) colorFileList.getElementAt(index)).cm);
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

    lkup = new ColorMapCanvas(getColorModel());
    lkup.setFont(getFont());

    box.add(lkup);
    box.add(Box.createHorizontalGlue());
    Container vbox = Box.createVerticalBox();
    box.add(vbox);
    vbox.add(clarifyB);
    clarifyB.addActionListener(this);
    vbox.add(unclarifyB);
    unclarifyB.addActionListener(this);
    box.add(editB);
    editB.addActionListener(this);


    box.add(Box.createHorizontalStrut(5));
    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(applyColorMapB);
    applyColorMapB.addActionListener(this);
    box.add(applyToDefaultsB);
    applyToDefaultsB.addActionListener(this);
    box.add(Box.createHorizontalGlue());

    box.add(Box.createHorizontalStrut(5));
    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(HorzColorBarB);
    HorzColorBarB.addActionListener(this);
    box.add(VertColorBarB);
    VertColorBarB.addActionListener(this);
    box.add(ColorQuiltB);
    ColorQuiltB.addActionListener(this);
    box.add(Box.createHorizontalGlue());

    box.add(Box.createHorizontalStrut(5));
    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(backgroundColorCH);
    box.add(applyBackgroundColorB);
    applyBackgroundColorB.addActionListener(this);
    box.add(Box.createHorizontalGlue());
    box.add(dismissB);
    dismissB.addActionListener(this);

    contentPane.add(Box.createVerticalStrut(5));

    if((cnuv == null) || cnuv.runningAsApplet()) {
      browseLookupB.setVisible(false);
    }
    if(cnuv != null) {
      CNUDisplay cnud = getCNUDisplay();
      if(cnud != null) {
	Color bg = cnud.getBackground();
	lkup.setBackground(bg);
	setBackgroundColorChoice(bg);
	cnud.addDisplayBackgroundColorComponent(lkup);
      }
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
    if((cnuviewer == null) || cnuviewer.runningAsApplet())
      browseLookupB.setVisible(false);
    else browseLookupB.setVisible(true);
    if(cnuviewer != null) {
      CNUDisplay cnud = getCNUDisplay();
      if(cnud != null) {
	  Color bg = cnud.getBackground();
	  lkup.setBackground(bg);
	  setBackgroundColorChoice(bg);
	  cnud.addDisplayBackgroundColorComponent(lkup);
      }
    }
  }
  /**
   * Gets the current color model.
   *
   * @return	current color model
   */
  public ColorModel getColorModel() { return cm; }
  /**
   * Sets the current color model.
   *
   * @param cm	new color model
   */
  public void setColorModel( final ColorModel cm ) {
    if(cm == null) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setColorModel(cm); }
      } );
    }
    else {
      boolean enableClarify = false;
      boolean enableUnclarify = false;
      this.cm = cm;
      if(lkup != null) lkup.setColorModel(cm);
      if(cm instanceof CNUColorModel) {
	lookupTF.setText(((CNUColorModel) cm).getFullName());
	if(cm.equals(CNUColorModel.getTransparentColorModel((IndexColorModel)cm, 0))) enableUnclarify = true;
	else enableClarify = true;
      }
      clarifyB.setVisible(enableClarify);
      unclarifyB.setVisible(enableUnclarify);
      addToColorModels(cm);
      invalidate();
      validate();
    }
  }
  /**
   * Sets the background color choice.
   *
   * @param c	background color for color model display
   */
  public void setBackgroundColorChoice(final Color c) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setBackgroundColorChoice(c); }
      } );
    }
    int i=0;
    for(; i<colorValues.length; i++) { if( c == colorValues[i] ) break; }
    if(i >= colorValues.length) i = 6; // default to lightGray
    backgroundColorCH.setSelectedIndex(i);
  }
  /**
   * Adds to the color model list.
   *
   * @param cm	color model to add to list
   */
  public void addToColorModels(final ColorModel cm) {
    if(cm == null) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { addToColorModels(cm); }
      } );
    }
    else {
      ColorListElement cle = new ColorListElement(cm);
      int index = colorFileList.getIndexOf(cle);
      if(index < 0) {
	// only new color models added
	index = colorFileList.getSize();
	colorFileList.addElement(cle);
      }
      colorFileList.setSelectedItem(cle);
      colorFileL.ensureIndexIsVisible(index);
    }
  }
  /**
   * Changes a file name in the filename list.
   *
   * @param cm	color model with new name to update list to
   */
  public void replaceColorFileName(final ColorModel cm) {
    if(cm == null) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { replaceColorFileName(cm); }
      } );
    }
    else {
      ColorListElement cle = new ColorListElement(cm);
      int index = colorFileList.getIndexOf(cle);
      if(index >= 0) {
	// really just need to fire contents changed event
	colorFileList.removeElementAt(index);
	colorFileList.insertElementAt(cle, index);
      }
    }
  }
  /**
   * Returns the last coordinate map associated with a file object.
   *
   * @param sameFileObj	object to compare files with
   * @return		color model representing the same file or <code>null</code>
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
      ColorModel cm = getColorModel();
      if(cm instanceof CNUFileObject) {
	Object obj = ((CNUFileObject) cm).getFileObject(sameFileObj);
	if(obj != null) return obj;
      }
      for(int i = colorFileList.getSize() - 1; i >= 0; i--) {
        cm = ((ColorListElement) colorFileList.getElementAt(i)).cm;
	if(cm instanceof CNUFileObject) {
	  Object obj = ((CNUFileObject) cm).getFileObject(sameFileObj);
	  if(obj != null) return obj;
	}
      }
      return null;
    }
  }
  /**
   * Checks for unsaved color maps.
   *
   * @return	<code>true</code> if any unsaved color models exist
   */
  public boolean unsavedColorMaps() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = new Boolean(unsavedColorMaps()); }
      };
      runWithReturn.invokeAndWait();
      return ((Boolean) runWithReturn.returnObject).booleanValue();
    }
    else {
      for(int i = colorFileList.getSize() - 1; i >= 0; i--) {
	ColorModel cm = ((ColorListElement) colorFileList.getElementAt(i)).cm;
	if( cm instanceof CNUColorModel )
	  if( ! ((CNUColorModel) cm).getSaved()) return true;
	else return true;
      }
    }
    return false;
  }
  /**
   * Reads the color model from a lookup file.
   *
   * @param lookupname	name of file that contains a lookup model
   * @return		color model read from file
   */
  public ColorModel readLookupFile(String lookupname) {
    ColorModel cm = null;
    if(lookupname != null) lookupname = lookupname.trim();
    if("".equals(lookupname)) lookupname = null;
    if(lookupname == null || CNUColorModel.defaultColorMap.equals(lookupname))
      cm = CNUColorModel.getGreyColorModel();
    else {
      cm = CNUColorModel.readLookupFile(lookupname, getCNUViewer());
      if(cm == null) Toolkit.getDefaultToolkit().beep();
    }
    return cm;
  }
  /**
   * Reads the color model from a lookup file.
   *
   * @param lookupname	name of file that contains a lookup model
   * @param cnuv	CNUViewer object to search for already read files
   * @return		color model read from file
  public final static ColorModel readLookupFile(String lookupname,
						CNUViewer cnuv) {
    return CNUColorModel.readLookupFile(lookupname, cnuv);
  }
   */
  /**
   * Invokes browser to select a lookup file.
   */
  public void  browseLookupFile() {
    String path = lookupTF.getText();
    if(path != null) {
      path = path.trim();
      if(path.equals("")) path = null;
      else if(path.equals(CNUColorModel.defaultColorMap)) path = null;
    }
    if(path == null) path = System.getProperty("CNU.ColorDialog.path", "");
    FileDialog fd = getCNUViewer().getFileDialog(path);
    fd.setVisible(true);
    if(fd.getFile() != null) {
      String file = fd.getDirectory() + fd.getFile();
      setColorModel(readLookupFile(file));
    }
  }
  /**
   * Interprets mouse events over this dialog.
   *
   * @param e	action event
   */
  public void actionPerformed(ActionEvent e){
    CNUViewer cnuv = getCNUViewer();
    cnuv.setWaitCursor();
    try {
      Object source = e.getSource();
      if(source == applyBackgroundColorB) {
	Color c = colorValues[backgroundColorCH.getSelectedIndex()];
	getCNUDisplay().setDisplayBackground(c);
      }
      else if (source == lookupTF)
        setColorModel( readLookupFile(lookupTF.getText()) );
      else if (source == currentB) {
        setColorModel(getCNUDisplay().getCurrentColorModel());
	setBackgroundColorChoice(getCNUDisplay().getBackground());
      }
      else if (source == browseLookupB) browseLookupFile();
      else if (source == clarifyB) {
	ColorModel cm = getColorModel();
	if(cm instanceof IndexColorModel) {
	  setColorModel(
	    CNUColorModel.getTransparentColorModel((IndexColorModel) cm, 0));
	}
      }
      else if (source == unclarifyB) {
	ColorModel cm = getColorModel();
	if(cm instanceof IndexColorModel) {
	  setColorModel(
	    CNUColorModel.getTransparentColorModel((IndexColorModel) cm, -1));
	}
      }
      else if (source == editB) {
	if(cnuv != null) {
	  EditColorDialog ecd = (EditColorDialog) cnuv.getEditColorDialog();
	  if(ecd != null) {
	    ecd.setColorModel(getColorModel());
	    ecd.setVisible(true);
	  }
	}
      }
      else if (source == HorzColorBarB)
        getCNUDisplay().addDisplayColorMap(getColorModel(),
	  DisplayColorMap.HORIZONTAL);
      else if (source == VertColorBarB)
        getCNUDisplay().addDisplayColorMap(getColorModel(),
	  DisplayColorMap.VERTICAL);
      else if (source == ColorQuiltB)
        getCNUDisplay().addDisplayColorMapQuilt(getColorModel());
      else if (source == applyColorMapB) {
        ColorModel cm = getColorModel();
        getCNUDisplay().updateColorModel(getColorModel());
      }
      else if(source == applyToDefaultsB) {
        ColorModel cm = getColorModel();
        cnuv.setDefaultColorModel(getColorModel());
      }
      else if(source == dismissB) setVisible(false);
    } finally {
      cnuv.setNormalCursor();
    }
  }
}
