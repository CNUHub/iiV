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
import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.*;
import java.lang.*;
import java.util.*;
import Acme.JPM.Encoders.*;

/**
 * Dialog to get save options from the user.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUViewer
 * @since	iiV1.0
 */
public class SaveDialog extends CNUDialog
  implements ActionListener {
  private static final long serialVersionUID = -5523407796140383147L;
  private JButton browseB = new JButton("Browse");
  private JTextField filenameTF = new JTextField(50) {
    private static final long serialVersionUID = 2787190019761778907L;
    public Dimension getMaximumSize() {
      return new Dimension(5000, getPreferredSize().height);
    }
  };
  private JButton saveSelectedB = new JButton("Save Selected Region or Selected Components");
  private JButton selectRegionB = new JButton("Select Region...");
  private JButton saveViewerB = new JButton("Save Viewer Settings as Script");
  //  public static String[] fileTypeChoices = { "Gif", "Script", "Ppm"};
  //  public static String[] fileTypeChoices = javax.imageio.ImageIO.getWriterFormatNames();
  //  public static String[] fileTypeChoices = javax.imageio.ImageIO.getWriterMIMETypes();
  public static String[] fileTypeChoices = buildFileTypeChoices();
  private final static String[] buildFileTypeChoices() {
    String[] list1 = javax.imageio.ImageIO.getWriterFormatNames();
    ArrayList<String> tmplist = new ArrayList<String>();
    tmplist.add("Script");
    for(int i=0; i<list1.length; i++) {
      String tmp = list1[i].toLowerCase();
      if(! tmplist.contains(tmp)) {
	// don't duplicate jpeg and jpg
	if("jpeg".equals(tmp)) { if(! tmplist.contains("jpg")) tmplist.add(tmp); }
	else if("jpg".equals(tmp)) { if(! tmplist.contains("jpeg")) tmplist.add(tmp); }
	else if("wbmp".equals(tmp)) ; //don't allow wbmp -- white/black bit maps failed first blush
	else if(! tmplist.contains(tmp)) tmplist.add(tmp);  // only add unique values
      }
    }
    // formats covered by Acme.JPM.Encoders
    if(! tmplist.contains("ppm")) tmplist.add("ppm");
    if(! tmplist.contains("gif")) tmplist.add("gif");
    return tmplist.toArray(new String[tmplist.size()]);
  }
  private JComboBox fileTypeC;
  /* = new JComboBox(fileTypeChoices) {
    public Dimension getMaximumSize() {
	return new Dimension(getPreferredSize());
    }
  };
  */
  private JButton saveB = new JButton("Save Display");
  private JButton dismissB = new JButton("Dismiss");

  /**
   * Constructs a new instance of SaveDialog.
   *
   * @param parentFrame	parent frame needed for building a window
   */
  public SaveDialog(Frame parentFrame) { this(parentFrame, null); }
  /**
   * Constructs a new instance of SaveDialog.
   *
   * @param parent	parent frame needed for building a window
   * @param cnuv	CNUViewer that this window interacts with
   */
  public SaveDialog(Frame parent, CNUViewer cnuv ) {
    super(parent, "Save", false, cnuv);

    Container contentPane = getContentPane();

    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    contentPane.add(Box.createVerticalStrut(5));

    Container box = Box.createHorizontalBox();
    contentPane.add(box);


    box.add(Box.createHorizontalStrut(5));
    box.add(browseB);
    browseB.addActionListener(this);
    box.add(filenameTF);
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));

    box.add(new JLabel("File Type: "));
    fileTypeC = new JComboBox(fileTypeChoices) {
	private static final long serialVersionUID = -8860293375101269367L;
	public Dimension getMaximumSize() {
	  return new Dimension(getPreferredSize());
	}
      };
    box.add(fileTypeC);

    box.add(saveB);
    saveB.addActionListener(this);
    box.add(Box.createHorizontalGlue());
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(selectRegionB);
    selectRegionB.addActionListener(this);
    box.add(saveSelectedB);
    saveSelectedB.addActionListener(this);
    box.add(Box.createHorizontalGlue());

    box = Box.createHorizontalBox();
    box.add(Box.createHorizontalStrut(5));
    contentPane.add(box);
    box.add(saveViewerB);
    saveViewerB.addActionListener(this);
    box.add(Box.createHorizontalGlue());
    box.add(dismissB);
    dismissB.addActionListener(this);
    box.add(Box.createHorizontalStrut(5));

    contentPane.add(Box.createVerticalStrut(5));
    pack();
  }
  /**
   * Saves the viewer current settings.
   *
   * @param cnuv	viewer to save settings of
   * @param filename	file to save settings to
   */
  public static void saveViewer(CNUViewer cnuv, String filename) {
    try {
      saveScriptToFile(cnuv.toScript(), filename, cnuv.getContinueDialog());
    } catch (IOException e1) {
	cnuv.showStatus("Save Failed");
	cnuv.showStatus(e1);
	Toolkit.getDefaultToolkit().beep();
    }
  }
  /**
   * Saves the selected region or components of the display as an image file.
   *
   * @param cnud	display to save
   * @param filename	file to save to
   * @param fileType	type of image to create in the file
   */
  public static void saveDisplaySelection(CNUDisplay cnud,
					  String filename, String fileType) {
    try {
      if(cnud == null) throw new IOException("null CNUDisplay");
      ContinueDialog cd = cnud.getCNUViewer().getContinueDialog();
      Rectangle region = cnud.getDisplayedSelectRegion();

      if( "Script".equals(fileType) ) {
	String script = null;
	if(region != null) script = cnud.toScript(region);
	else script = cnud.toScript(cnud.getSelectedComponentsOrdered());
	if(script == null)
	  throw new IOException("No region or components to save as script");
	saveScriptToFile(script, filename, cd);
      }
      else {
	Image image = null;
	if(region != null) image = cnud.toImage(region);
	else image = cnud.toImage(cnud.getSelectedComponentsOrdered());
	if(image == null)
	  throw new IOException("No region or components to save as image");
	saveImageToFile(image, filename, fileType, cd);
      }
    } catch (IOException e1) {
      cnud.showStatus("Save Failed");
      cnud.showStatus(e1);
      Toolkit.getDefaultToolkit().beep();
    }
  }
  /**
   * Saves given script to a file.
   *
   * @param script script to write to file
   * @param filename file to save to
   * @param cd dialog to prompt for overwrite
   *
   */
  public static void saveScriptToFile(String script, String filename,
				      ContinueDialog cd)
    throws IOException {
    if((filename != null) && (script != null)) {
      filename = filename.trim();
      if(! filename.equals("") ) {
	File file = new File(filename);
	if( file.exists() ) {
          Toolkit.getDefaultToolkit().beep();
	  if(cd == null) throw new IOException("file already exists");
	  cd.setVisible(true);
          if(! cd.getContinueFlag())
	    throw new IOException("overwrite denied by user");
	}
        FileOutputStream fos = null;
	PrintWriter pw = null;
        try {
	  fos = new FileOutputStream( file );
	  pw = new PrintWriter(fos);
	  pw.println(iiVBshScript.getHeaderScript());
	  pw.println(script);
	  pw.println("// end");
        } finally {
          if(pw != null) pw.close();
	  if(fos != null) fos.close();
        }
      } // end if(! filename.equals(""))
      else throw new IOException("blank file name");
    } // end if(filename != null)
    else throw new IOException("missing file name or image");
  }
  /**
   * Saves given image to a file.
   *
   * @param image image to save to file
   * @param filename file to save to
   * @param cd dialog to prompt for overwrite
   *
   */
  public static void saveImageToFile(Image image, String filename,
				     String fileType,
				     ContinueDialog cd)
    throws IOException {
    if((filename != null) && (image != null)) {
      filename = filename.trim();
      if(! filename.equals("") ) {
	File file = new File(filename);
	if( file.exists() ) {
          Toolkit.getDefaultToolkit().beep();
	  if(cd == null) throw new IOException("file already exists");
	  cd.setVisible(true);
          if(! cd.getContinueFlag()) throw new IOException("overwrite denied by user");
	}
        FileOutputStream fos = null;
	ImageEncoder ie = null;
        try {
	  fos = new FileOutputStream( file );
	  /*
	  Vector contructParams = new Vector(2);
	  constructParams.addElement(image);
	  constructParams.addElement(fos);
	  String encoderClassName = null;
	  */
	  // try ImageIO encoding first
	  if(! ImageIO.write((BufferedImage) image, fileType, fos)) {
	    // then try Acme.JPM.Encoders
	    if( "ppm".equalsIgnoreCase(fileType) )
	      ie = new PpmEncoder( image, fos );
	    else if( "jpeg".equalsIgnoreCase(fileType) || "jpg".equalsIgnoreCase(fileType))
	      ie = new JpegEncoder( image, fos );
	    else if( "gif".equalsIgnoreCase(fileType) )
	      ie = new GifEncoder( image, fos );
	    else throw new IOException("failed writing file type=" + fileType);
	    ie.encode();
	  }
        } finally {
	  if(fos != null) fos.close();
        }
      } // end if(! filename.equals(""))
      else throw new IOException("blank file name");
    } // end if(filename != null)
    else throw new IOException("missing file name or image");
  }
  /**
   * Saves the whole display or box area of the display as an image file.
   *
   * @param cnud	display to save
   * @param filename	file to save to
   * @param fileType	type of image to create in the file
   * @param box		box area of image to save,
   *			if <code>null</code> whole display is saved
   */
  public static void saveDisplay(CNUDisplay cnud,
			  String filename, String fileType,
    			  Rectangle box ) {
    try {
      if(cnud == null) throw new IOException("null CNUDisplay");
      ContinueDialog cd = cnud.getCNUViewer().getContinueDialog();
      if( "Script".equalsIgnoreCase(fileType) ) {
	String script = null;
	if(box != null) script = cnud.toScript(box);
	else script = cnud.toScript();
	saveScriptToFile(script, filename, cd);
      }
      else {
	Image image = cnud.toImage(box);
	if(image == null) throw new IOException("Failed building temporary image");
	saveImageToFile(image, filename, fileType, cd);
      }
    } catch (IOException e1) {
      cnud.showStatus("Save Failed");
      cnud.showStatus(e1);
      Toolkit.getDefaultToolkit().beep();
    }
  }
  private boolean monitorState = false;
  private ComponentListener cl = new ComponentListener() {
      /**
       * Implements component action routine
       *
       * @param e component event
       */
      public void componentMoved(ComponentEvent e) {}
      /**
       * Implements component action routine
       *
       * @param e component event
       */
      public void componentResized(ComponentEvent e) { }
      /**
       * Implements component action routine
       * when the component is shown.
       *
       * @param e component event
       */
      public void componentShown(ComponentEvent e) { }
      /**
       * Implements component action routine to call updateButtonState
       * when the component is hidden.
       *
       * @param e component event
       */
      public void componentHidden(ComponentEvent e) {
	showStatus("Warning - file selected but not saved to");
	setWarningState(false);
      }
    };
  /**
   * Monitors window state to warn if browsed file not saved to before
   * closing or starting a new browse.
   *
   * @param state warning state
   */
  private void setWarningState(boolean state) {
    if(state != monitorState) {
      monitorState = state;
      if(state) addComponentListener(cl);
      else removeComponentListener(cl);
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
      if (source == saveB) {
        saveDisplay(getCNUDisplay(), filenameTF.getText(),
		    (String) fileTypeC.getSelectedItem(), null);
	setWarningState(false);
      }
      else if (source == saveViewerB) {
        saveViewer(getCNUViewer(), filenameTF.getText());
	setWarningState(false);
      }
      else if(source == browseB) {
        FileDialog sfd = getCNUViewer().getSaveFileDialog(filenameTF.getText());
        sfd.setVisible(true);
        if(sfd.getFile() != null) {
	  filenameTF.setText(sfd.getDirectory() + sfd.getFile());
	  setWarningState(true);
	}
      }
      else if(source == saveSelectedB) {
	saveDisplaySelection(getCNUDisplay(),
			     filenameTF.getText(),
			     (String) fileTypeC.getSelectedItem());
	setWarningState(false);
      }
      else if(source == selectRegionB)
	getCNUViewer().getRegionDialog().setVisible(true);
      else if(source == dismissB) {
        setVisible(false);
      }
    } finally {
      getCNUViewer().setNormalCursor();
    }
  }
}
