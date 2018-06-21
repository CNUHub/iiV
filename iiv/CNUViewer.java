package iiv;
import iiv.display.*;
import iiv.dialog.*;
import iiv.script.*;
import iiv.util.*;
import iiv.gui.*;
import iiv.io.*;
import iiv.data.*;

import java.applet.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.lang.*;
import java.net.*;
import java.util.*;
import java.lang.reflect.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
/**
 * CNUViewer is the primary control class for iiV.  It creates the display
 * window and tools for iiV.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUDisplay
 * @see		java.applet.Applet
 * @since	iiV1.0
 */
public class CNUViewer extends JApplet
implements ShowStatus
{
  private static final long serialVersionUID = 2328818982408283190L;
  /** Holds the program title */
  public static String programTitle = "iiV";
  /** Holds the current version number */
  public static String version = "1.19";
  /** Holds the current help version number */
  public static String helpVersion = "1181";
  /** specifies default scaling when converting data to a displayable image */
  public final static int DEFAULT_SCALING = 0;
  /** specifies using positive scaling
   * when converting data to a displayable image */
  public final static int POSITIVE_SCALING = 1;
  /** specifies using negative scaling
   * when converting data to a displayable image */
  public final static int NEGATIVE_SCALING = 2;
  /** specifies using the last scaling 
   * when converting data to a displayable image */
  public final static int LAST_SCALING = 3;
  /** specifies auto scaling to true data range 
   * when converting data to a displayable image */
  public final static int AUTO_SCALING = 4;
  /** specifies auto scaling to true data positive range 
   * when converting data to a displayable image */
  public final static int AUTO_POSITIVE_SCALING = 5;
  /** specifies auto scaling to true data negative range 
   * when converting data to a displayable image */
  public final static int AUTO_NEGATIVE_SCALING = 6;
  /** specifies display image data in normal slice mode */
  public final static int NORMAL_DATA_DISPLAY=0;
  /** specifies display image data as intensity projection */
  public final static int INTENSITY_PROJECT_DATA=1;
  /** specifies displaying image data as orthogonal views */
  public final static int ORTHOGONAL_DATA_DISPLAY=2;


  /** represents standard file types to file type choice menu */
  public final static String STANDARD_FILE_TYPES = "Standard File Types";
  /** represents raw file type to file type choice menu */
  public final static String RAW_FILE_TYPE = "Raw File Type";
  public static String stdFileClassTypes[]={ "iiv.script.iiVBshScript",
					     "iiv.script.CNUDisplayScript",
					     "iiv.io.CNUNiftiImgFile",
					     "iiv.io.CNUAnalyzeImgFile",
					     "iiv.io.CNUEcatImgFile",
					     "iiv.io.CNUEcat7ImgFile",
					     "iiv.io.CMRRSdtImgFile",
					     "iiv.io.DICOMImgFile",
					     "iiv.io.CNUStdImgFile" };
  private CNUViewer cnuv = this;
  private boolean runningAsApplet = false;
  private URL helpURL = null;

  private CNUViewerActions cnuvactions = null;

  public CNUViewerActions getCNUViewerActions() {
    if(cnuvactions == null) synchronized(dialogCreateLock) {
      if(cnuvactions == null) {
        cnuvactions = new CNUViewerActions(this, getCNUDisplay());
      }
    }
    return cnuvactions;
  }

  public Dimension getMaximumSize() { return this.getPreferredSize(); };

  private Cursor normalCursor = null;
  private Cursor waitCursor = null;

  // main manu bar
  private JMenuBar menubar = null;

  public void autotransversemenubar(String menupath) {
      JMenuBar menuBar = getMenuBar();
      if(menuBar != null) autotransversemenu(menuBar, menupath);
  }
  // String menupath (ie. "File>Read File Type:>Raw File Type")
  public static void autotransversemenu(Component menu, String menupath) {
    if(menupath == null) return;
    menupath = menupath.trim();
    if(menupath.equals("")) return;

    String menuName = "";
    Object valueObject = null;
    String valueCommand = null;

    int index = menupath.indexOf('>');
    if(index > 1) {
      menuName = menupath.substring(0, index);
      menupath = menupath.substring(index+1);
    }
    else {
      // only if there are no more '>' can there be a value
      index = menupath.indexOf('%');
      if(index > -1) {
	String valueString = menupath.substring(index+1);
	if(index > 0) valueCommand = menupath.substring(0,index);
	else valueCommand = "setValue";
	menupath = null;
	try {
	  valueObject = Integer.valueOf(valueString);
	} catch (NumberFormatException nfe) {
	  valueObject = valueString;
	}
      }
      else if(menupath != null) {
	menuName = menupath;
	menupath=null;
      }
    }

    // : is like a wild card in that only everything to it's left
    // needs to match the beginning of the menu name
    String startsWithName = ">nomatch"; // low odds of matching anything
    index = menuName.indexOf(':');
    if(index > 1) startsWithName = menuName.substring(0, index);
    if(menu instanceof JMenuBar) {
      JMenuBar bar = (JMenuBar) menu;
      int barCount = bar.getMenuCount();
      for(int imenu = 0; imenu < barCount; imenu++) {
	JMenu submenu = bar.getMenu(imenu);
	if(submenu != null) {
	  String subName = submenu.getText();
	  if(subName.equals(menuName) ||
	     subName.startsWith(startsWithName)) {
	    submenu.doClick(1000);
	    autotransversemenu(submenu, menupath);
	    submenu.setPopupMenuVisible(false);
	    submenu.setSelected(false);
	    break;
	  }
	}
      }
    }
    else if(menu instanceof JMenu) {
      Class[] noClasses = new Class[0];
      Object[] noParams = new Object[0];
      JMenu jmenu = (JMenu) menu;
      int menuCount = jmenu.getMenuComponentCount();
      for(int iitem = 0; iitem < menuCount; iitem++) {
	Component menuComponent = jmenu.getMenuComponent(iitem);
	if(menuComponent != null) {
	  String subName = null;
	  try {
	    Method method =
	      menuComponent.getClass().getMethod("getText",
						 noClasses);
	    subName =
	      (String) method.invoke(menuComponent, noParams);
	  } catch (NoSuchMethodException nsme) { // ignore
	  } catch (SecurityException se) { // ignore
	  } catch (IllegalAccessException iae) { // ignore
	  } catch (IllegalArgumentException iarge) { // ignore
	  } catch (InvocationTargetException ite) { // ignore
	  } catch (ClassCastException cce) { // ignore
	  }
	  if(subName == null) ; // ignore
	  else if(subName.equals(menuName) ||
		  subName.startsWith(startsWithName)) {
	    if(menuComponent instanceof JMenuItem)
	      ((JMenuItem) menuComponent).doClick(1000);
	    autotransversemenu(menuComponent, menupath);
	    if(menuComponent instanceof JMenu)
	      ((JMenu) menuComponent).setPopupMenuVisible(false);
	    break;
	  }
	}
      }
    }
    else if(menu instanceof JMenuItem) {
      ((JMenuItem) menu).doClick(2000);
    }
    else if(valueObject != null) {
      try {
	Vector<Object> args = new Vector<Object>(1);
	args.addElement(valueObject);
	CNUDisplayScript.callObjectMethod(menu, valueCommand, args, null);
      } catch (InvocationTargetException ite) {
      } catch (IllegalAccessException iae) {
      }
    }
  }

  private Object dialogCreateLock = new Object();
  private Vector<Dialog> dialogList = new Vector<Dialog>(17);

  private FileDialog FD = null;
  private FileDialog SFD = null;
  private ContinueDialog ContinueD = null;

  private CNUDialog controlDialog = null;
  private StatusWindow statusWindow = null;
  private CNUDialog SPD = null;
  private CNUDialog GPD = null;
  private CNUDialog TD = null;
  private CNUDialog FTD = null;
  private CNUDialog SD = null;
  private CNUDialog formatD = null;
  private CNUDialog shapeD = null;
  private CNUDialog CD = null;
  private CNUDialog ECD = null;
  private CNUDialog cropD = null;
  private CNUDialog saveD = null;
  private CNUDialog regionD = null;
  private CNUDialog memoryD = null;
  private CNUDialog CMD = null;
  private CNUDialog GD = null;
  private CNUDialog FLTRD = null;
  private CNUDialog dataSlicerD = null;

  private ShowPointControllerInterface SPC = null;




  private UndoRedo undoRedoObj = null;

  private CNUDisplay cnud = new CNUDisplay(this);
  private JScrollPane cnudSP = new JScrollPane(cnud);
  private Object viewObject = null;
  private Thread displayThread = null;
  private Object displayThreadLock = new Object();

  /**
   * Creates a new instance of CNUViewer.
   */
  public CNUViewer() {
    // add tool panel and display to this window
    Container contentPane = this.getContentPane();
    contentPane.setLayout(new BorderLayout());
    contentPane.add(cnudSP, BorderLayout.CENTER);
  }

  /**
   * Creates a new instance of CNUViewer with a give ShowPointController.
   */
  public CNUViewer(ShowPointControllerInterface spci) {
    this();
    setShowPointController(spci);
  }

  /**
   * Gets applet information.
   *
   * @return 	text information about this program
   */
  public String getAppletInfo()
  {
    return
      "Name:  " + programTitle + " " + version + "\r\n" +
      "Author:  Joel T. Lee\r\n" +
      "Group:  Cognitive Neuroimaging Unit\r\n" +
      "Institute:  VA Medical Center/University of Minnesota\r\n" +
      "Address:  1 Veterans Drive, Minneapolis, MN  55417\r\n";
    //      "Home Page:  http://james.psych.umn.edu/\r\n";
  }
  /**
   * Gets parameter information.
   *
   * @return	an array of String arrays containing parameter information
   */
  public String[][] getParameterInfo() {
    String[][] parameterInfo = {
      {"columns", "integer",
       "number of columns to display (defaults to 6)"},
      {"file", "string",
       "name of file to display (defaults to none)"},
      {"lookup", "string",
       "name of color map file (defaults to none)"},
    };
    return parameterInfo;
  }
  private DisplayComponentDefaults displaycomponentdefaults =
      new DisplayComponentDefaults();
  /**
   * Get the Display Component Defaults associated with this viewer.
   *
   * @return display component defaults
   */
  public DisplayComponentDefaults getDisplayComponentDefaults() {
    return displaycomponentdefaults;
  }
  /**
   * Shows a help window if available.
   */
   public void showHelp() {
     if(helpURL == null) {
       String cnuHelpUrl = null;
       String cnuHelpFile = System.getProperty("CNU.help.file");
       if(cnuHelpFile == null) {
	 cnuHelpUrl = System.getProperty("CNU.help.url");
	 if(cnuHelpUrl == null) {
	   cnuHelpFile = "iiV" + helpVersion + "Help.html";
	   cnuHelpUrl = "http://james.psych.umn.edu/iiV/doc/iiV" + helpVersion + "Help.html";
	 }
       }
       // check for local file
       if(cnuHelpFile != null) try {
	 File helpFile = new File(cnuHelpFile);
	 if(helpFile.exists() && helpFile.canRead()) {
	   helpURL = new URL("file:///" + helpFile.getAbsolutePath());
	 }
       } catch (SecurityException e2) { // ignore
       } catch (MalformedURLException e) { // ignore
       }
       // try the url
       if(helpURL == null && cnuHelpUrl != null) {
	 try {
	   helpURL = new URL(cnuHelpUrl);
	 } catch (MalformedURLException e) {} // ignore
       }
     }
     if(helpURL != null) {
       if(runningAsApplet)
	 getAppletContext().showDocument(helpURL, "iiV Help");
       else BrowserControl.displayURL(helpURL.toString());
     }
   }
  /**
   * Initialize this instance retrieving parameters as an applet.
   */
  public void init() {
    runningAsApplet = true;
    CNUFile.setUrlContext(getDocumentBase());
    // read and set the colormodel
    setDefaultColorModel(CNUColorModel.getGreyColorModel());
    // build menu bar
    getMenuBar();

    // initialize UndoRedo to updateUndoButtons;
    getUndoRedo();

    CNUViewerActions cnuviewerActions = getCNUViewerActions();
    // buttons not available as applet
    cnuviewerActions.quitAction.setEnabled(false);
    //    cnuviewerActions.saveDialogVSBU.setEnabled(false);
    //    cnuviewerActions.browseAction.setEnabled(false);
    //    cnuviewerActions.printAction.setEnabled(false);


    String colsString = getParameter("columns");
    if(colsString != null) {
      int cols = Integer.parseInt(colsString);
      if(cols > 0) setNumberOfColumns(cols);
    }
    String lookupFile = getParameter("lookup");
    if(lookupFile != null) {
      ColorModel cm = CNUColorModel.readLookupFile(getParameter("lookup"),
						   this);
      if(cm != null) setDefaultColorModel(cm);
    }
    String fileName = getParameter("file");
    if(fileName != null) threadSetFile(fileName); // 2-11-05
    String helpName = getParameter("help");
    if(helpName != null) try {
      helpURL = new URL(getDocumentBase(), helpName);
      //      cnuviewerActions.helpAction.setEnabled(true);
    } catch (MalformedURLException e) {} // ignore - no help
  }
  /**
   * Initialize this instance retrieving parameters as a standalone program.
   *
   * @param args	an array of argument Strings
   */
  public void init(String args[]) {
    runningAsApplet = false;

    // JTL 10/02/2013 changed to parse arguments before setting defaults
    String filename = null;
    String lookfile = null;
    int cols = -1;
    String usage = "Usage:  java " + programTitle + " [filename] [-version]" +
      " [-l lookupFilename] [-c numberOfColumns]";
    for(int i=0; i < args.length; i++) {
      if( "-help".equals(args[i]) ) {
        System.out.println(usage); System.exit(0);
      }
      else if( "-version".equals(args[i]) ) {
        System.out.println(programTitle + " " + version); System.exit(0);
      }
      else if( "-l".equals(args[i]) ) {
	i++;
	if(i < args.length) lookfile = args[i];
	else {
	  System.out.println("missing lookup file name with -l option");
	  System.out.println(usage); System.exit(0);
	}
      }
      else if( "-c".equals(args[i]) ) {
	i++;
	if(i < args.length) cols = Integer.parseInt(args[i]);
	else {
	  System.out.println("missing number of columns with -c option");
	  System.out.println(usage); System.exit(0);
	}
      }
      else if(filename == null) filename = args[i];
      else {
        System.out.println(usage); System.exit(0);
      }
    }

    // read and set the colormodel
    setDefaultColorModel(CNUColorModel.getGreyColorModel());
    // build menu bar
    getMenuBar();
    // initialize UndoRedo to updateUndoButtons;
    getUndoRedo();

    // set defaultsfile before setting parsed arguments
    // search local and home for defaults file
    boolean founddefaultsfile = false;
    // set to property specified defaults file or ".cnu"
    String defaultsFile = System.getProperty("CNU.defaults.file", ".cnu");
    // look under current directory
    founddefaultsfile = (new File(defaultsFile)).exists();
    if( ! founddefaultsfile ) {
      // try under home directory
      String homeDir = System.getProperty("user.home", null);
      if(homeDir != null) {
        // add a separator if needed
        if( ! homeDir.endsWith(File.separator) ) homeDir += File.separator;
        String tmpdefaultsFile = homeDir + defaultsFile;
	if( (new File(tmpdefaultsFile)).exists() ) {
          defaultsFile = tmpdefaultsFile;
          founddefaultsfile = true;
	}
      }
    }
    // JTL 10/02/2013 search for standard default file in class path i.e. jar files
    if( ! founddefaultsfile ) {
    	URL defaultURL=this.getClass().getClassLoader().getResource(defaultsFile);
    	if( defaultURL != null ) {
	  defaultsFile=defaultURL.toString();
          founddefaultsfile = true;
    	}
    }
    if( founddefaultsfile ) {
      threadSetFile(defaultsFile);
      //no longer does anything setFile(""); // clear the filename text field
    }

    if(cols > 0) setNumberOfColumns(cols);
    if(lookfile != null) {
      ColorModel cm = CNUColorModel.readLookupFile(lookfile, this);
      if(cm != null) setDefaultColorModel(cm);
    }
    if(filename != null) threadSetFile(filename);
  }
  /**
   * Check if this invocation of iiV is running as an applet.
   *
   * @return	<code>true</code> if running as an applet,
   *		<code>false</code> otherwise
   */
  public boolean runningAsApplet() { return runningAsApplet; }

  private JMenuBar getMenuBar() {
    if(menubar == null) synchronized(dialogCreateLock) {
      if(menubar == null) {
	Vector<Object> params = new Vector<Object>(1);
	params.addElement(this);
	menubar = (JMenuBar) CNUDisplayScript.getObject("iiv.CNUViewerMenuBar",
							params, null);
	if(menubar == null) Toolkit.getDefaultToolkit().beep();
	else {
	  setJMenuBar(menubar);
	  getCNUViewerActions().toolMenuVSBU.setComponent(menubar);
	}
      }
    }
    return menubar;
  }
  private JTextField filenameAccessTF = null;
  /**
   * Sets the text in the filename document.
   *
   * @param filename text to set filename to
   */
  public void setFilenameDocumentText(final String filename) {
    if(filename == null) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setFilenameDocumentText(filename); }
      } );
    }
    else {
      if(filenameAccessTF == null)
	filenameAccessTF =
	    new JTextField(getCNUViewerActions().filenameDoc, "", 37);
      filenameAccessTF.setText(filename);
    }
  }
  /**
   * Retrieves the text from the filename document.
   *
   * @return filename
   */
  public String getFilenameDocumentText() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = getFilenameDocumentText(); }
      };
      runWithReturn.invokeAndWait();
      return (String) runWithReturn.returnObject;
    }
    if(filenameAccessTF == null)
	filenameAccessTF =
	    new JTextField(getCNUViewerActions().filenameDoc, "", 37);
    return filenameAccessTF.getText();
  }
  /**
   * Print the display.
   */
  public void print() {
    // changed to run printing in seperate thread because things
    // locked up when trying to print from applet.
    // build anonymous thread and run printing in thread
    Thread printThread = new Thread() {
	public void run() {
	  PrintJob pj =
	    Toolkit.getDefaultToolkit().getPrintJob(getParentFrame(),
						    programTitle + " print",
						    null);
	  if(pj == null) showStatus("Print request canceled");
	  else {
	    Graphics g = pj.getGraphics();
	    // direct method may mess up because of front/back order problems
	    // and slow maybe because of font drawing
	    cnud.getParent().printComponents(g);
	    // intermediate image faster, no order problems but poorer fonts
	    //g.drawImage(cnud.toImage(null, null),0,0,null);
	    g.dispose();
	    pj.end();
	  }
	};
      };
    printThread.setDaemon(true);
    printThread.start();
  }
  /**
   * Destroys and quits this application.
   */
  public void destroy() {
    super.destroy();
    if(! runningAsApplet) System.exit(0);
  }
  /**
   * Detaches the tool panel from the main display frame.
   */
  public void detach() {}
  /**
   * Attaches the tool panel to the main display frame.
   */
  public void attach() {}
  /**
   * Creates a script for recreating current iiV settings.
   *
   * @return	script for recreating current iiV settings
   */
  public String toScript() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() { returnObject = toScript(); }
      };
      runWithReturn.invokeAndWait();
      return (String) runWithReturn.returnObject;
    }
    StringBuffer sb = new StringBuffer(256);
    sb.append("// -- start iiV settings script\n");
    CNUViewerActions cnuviewerActions = getCNUViewerActions();
    // add any nonstandard file types
    for(int i = cnuviewerActions.fileTypesChoiceModel.getSize();
        i > cnuviewerActions.fileTypesCH_originalCount; i--) {
      sb.append("fileType(\"");
      sb.append(cnuviewerActions.fileTypesChoiceModel.getElementAt(i-1));
      sb.append("\");\n");
    }
    // set the current file type choice
    String fileType = getFileTypesChoice();
    if(STANDARD_FILE_TYPES.equals(fileType))
      sb.append("fileType(\"standard\");\n");
    else if(RAW_FILE_TYPE.equals(fileType)) sb.append("fileType(\"raw\");\n");
    else sb.append("fileType(\"").append(fileType).append("\");\n");
    // default raw file settings
    sb.append(CNURawImgFile.defaultsToScript());
    // default read modes
    if(getReadDisplaysState()) sb.append("readdisplays(\"on\");\n");
    else  sb.append("readdisplays(\"off\");\n");
    if(getReadAnewState()) sb.append("readanew(\"on\");\n");
    else  sb.append("readanew(\"off\");\n");
    // default select mode
    if(getSelectAdditions()) sb.append("select(\"additions\");\n");
    else sb.append("unselect(\"additions\");\n");
    // default color model
    ColorModel cm = getDefaultColorModel();
    if(cm instanceof IndexColorModel) {
      if(cm instanceof CNUColorModel)
	sb.append(((CNUColorModel) cm).toScript(null));
      else sb.append(CNUColorModel.toScript((IndexColorModel) cm, null));
      sb.append("color(script_rtn);\n");
    }
    else showStatus("unable to save colormodel in script");
    // default background color
    sb.append("backgroundColor(\"");
    sb.append(CNUDialog.colorToString(getCNUDisplay().getBackground()));
    sb.append("\");\n");
    // default text font
    sb.append(iiVBshScript.fontToScript(getCNUDisplay().getFont()));
    // default text color
    sb.append("foregroundColor(\"");
    sb.append(CNUDialog.colorToString(getCNUDisplay().getForeground()));
    sb.append("\");\n");
    // default text justification
    sb.append(
      DisplayText.justificationToScript(getCNUDisplay().getJustification()));
    // default scale mode
    switch (getScaleMode()) {
    case LAST_SCALING:
      sb.append(iiVBshScript.scaleToScript(CNUScale.getDefaultScale()));
      break;
    case POSITIVE_SCALING:
      sb.append("scale(\"positive\");\n");
      break;
    case NEGATIVE_SCALING:
      sb.append("scale(\"negative\");\n");
      break;
    default:
      sb.append("scale(\"default\");\n");
      break;
    }
    // default view mode
    sb.append("view(\"");
    sb.append(CNUDimensions.orientationToString(getSliceViewMode()));
    sb.append("\");\n");
    // default i dim mode
    if( getIDimLimitMode() ) {
      sb.append("iRange(");
      sb.append(DisplayComponentDefaults.getDefaultFirstIDimLimit());
      sb.append(", ");
      sb.append(DisplayComponentDefaults.getDefaultLastIDimLimit());
      sb.append(");\n");
    }
    else sb.append("iRange(\"off\");\n");
    // default slice mode
    if( getSliceLimitMode() ) {
      sb.append("slices(");
      sb.append(DisplayComponentDefaults.getDefaultFirstSliceLimit());
      sb.append(", ");
      sb.append(DisplayComponentDefaults.getDefaultLastSliceLimit());
      sb.append(");\n");
    }
    else sb.append("slices(\"off\");\n");
    // default crop mode
    if( getCropBoxState() )
      sb.append(
  iiVBshScript.cropBoxToScript(DisplayComponentDefaults.getDefaultCrop()));
    else sb.append("crop(\"off\");\n");
    // default columns
    sb.append("columns(").append(getNumberOfColumns()).append(");\n");
    // default labels on or off
    if(getSliceLabelOn()) sb.append("sliceLabelOn(); ");
    else sb.append("sliceLabelOff(); ");
    if(getOrientationLabelsOn()) sb.append("orientationLabelsOn();\n");
    else sb.append("orientationLabelsOff();\n");
    // default flip
    if(DisplayComponentDefaults.getDefaultFlipV()) sb.append("flip(\"vertical\", ");
    else sb.append("flip(\"verticalOff\", ");
    if(DisplayComponentDefaults.getDefaultFlipH()) sb.append("\"horizontal\");\n");
    else sb.append("\"horizontalOff\");\n");
    // default filter sampling type
    sb.append(iiVBshScript.filterSampleTypeToScript(
		DisplayComponentDefaults.getDefaultFilterSampleType()));
    // default zoom
    double zoomV = DisplayComponentDefaults.getDefaultZoomV();
    double zoomH = DisplayComponentDefaults.getDefaultZoomH();
    if(zoomV == zoomH) sb.append("zoom(").append(zoomV).append(");\n");
    else
      sb.append("zoom(").append(zoomV).append(", ").append(zoomH).append(");\n");
    if(! DisplayComponentDefaults.getDefaultZoomState())
      sb.append("zoom(\"off\");\n");
    // default rotation
    sb.append(
      iiVBshScript.rotationToScript(DisplayComponentDefaults.getDefaultRotation()));
    if(! DisplayComponentDefaults.getDefaultRotateState())
      sb.append("rotate(\"off\");\n");
    // undo status
    if( getUndoRedo().isEnabled() ) sb.append("enableUndos();\n");
    else sb.append("disableUndos();\n");
    // tools attachment and visibility
    if( isMenuBarVisible() ) sb.append("show(\"menubar\");\n");
    else sb.append("hide(\"menubar\");\n");
    if( isToolPanelVisible() ) sb.append("showTools();\n");
    else sb.append("hideTools();\n");
    // default coordinate map
    CoordinateMap coorMap =  LinearCoordinateMap.getDefaultCoordinateMap();
    if(coorMap == null)  sb.append("spatialMap(\"off\");\n");
    else sb.append(coorMap.toScript(null)).append("spatialMap(script_rtn);\n");

    // the background of the display area
    sb.append(getCNUDisplay().backgroundToScript());

    // convert any dialogs that implement toScript into scripts
    existingDialogsToScript(sb);

    // known system properties that affect iiV
    String[] propList = {
      "browser",
      "BROWSER",
      "CNU.ColorDialog.path",
      "CNU.CoordinateMapDialog.path",
      "CNU.defaults.file",
      "CNU.help.file",
      "CNU.help.url",
      "ftpProxySet",
      "ftpProxyHost",
      "ftpProxyPort",
      "proxySet",
      "http.proxyHost",
      "http.proxyPort"
    };
    String prop = null;
    for(int i=0; i<propList.length; i++) {
      prop = System.getProperty(propList[i]);
      if(prop != null) {
	sb.append("java.lang.System.setProperty(\"");
	sb.append(propList[i]).append("\", \"").append(prop).append("\");\n");
      }
    }

    // note the end of this script
    sb.append("// -- end iiV settings script\n");
    return sb.toString();
  }
  /**
   * Gets the parent that is an instance of Frame.
   *
   * @return	The parent Frame or <code>null</code> if not found.
   */
  public Frame getParentFrame() {
    Container c = this;
    synchronized (c.getTreeLock()) {
      while( !(c instanceof Frame) ) {
        c = c.getParent();
        if(c == null)break;
      }
      return (Frame) c;
    }
  }
  /**
   * Gets the display panel.
   *
   * @return	The CNUDisplay associated with this control panel.
   */
  public CNUDisplay getCNUDisplay() { return cnud; }
  /**
   * Sets whether the menu bar is visible or not.
   *
   * @param b <code>true</code> to show menu bar or
   *	      <code>false</code> to hide menu bar
   */
  public void setMenuBarVisible(final boolean b) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setMenuBarVisible(b); }
      } );
    }
    else {
      JMenuBar menuBar = getMenuBar();
      if(menuBar == null) Toolkit.getDefaultToolkit().beep();
      else if(b != menuBar.isVisible()) {
	// just setting menuBar visible false didn't remove the bar
	// area and caused refresh problems
	if(b) { setJMenuBar(menuBar); menuBar.setVisible(b); }
	else { menuBar.setVisible(b); setJMenuBar(null); }
	validate();
      }
    }
  }
  /**
   * Gets whether the menu bar is visible or not.
   *
   * @return <code>true</code> if the menu bar is visible
   */
  public boolean isMenuBarVisible() {
    JMenuBar menuBar = getMenuBar();
    if(menuBar == null) return false;
    else return menuBar.isVisible();
  }
  /**
   * Sets whether the copy image button is enabled or not.
   *
   * @param b <code>true</code> to enable copy image or
   *	      <code>false</code> to disable copy image
   */
  public void setCopyImageEnabled(final boolean b) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setCopyImageEnabled(b); }
      } );
    }
    else {
      getCNUViewerActions().copyImageAction.setEnabled(b);
      getCNUViewerActions().pasteImageAction.setEnabled(b);
    }
  }
  /**
   * Shows the tool panel bringing it to the front if necessary.
   */
  public void showToolPanel() { setToolPanelVisible(true); }
  /**
   * Hide the tool panel.
   */
  public void hideToolPanel() { setToolPanelVisible(false); }
  /**
   * Sets whether the tool panel is visible or not.
   *
   * @param b <code>true</code> to show the tool panel or
   *	      <code>false</code> to hide the tool panel
   */
  public void setToolPanelVisible(final boolean b) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setToolPanelVisible(b); }
      } );
    }
    else if( (controlDialog != null) || (b == true) ) {
      CNUDialog cd = getControlDialog();
      if(cd != null && cd.isVisible() != b) cd.setVisible(b);
    }
  }
  /**
   * Gets whether the tool panel is visible or not.
   *
   * @return  <code>true</code> if the tool panel is showing
   */
  public boolean isToolPanelVisible() {
    if(controlDialog == null) return false;
    else return getControlDialog().isVisible();
  }
  /**
   * Gets a named dialog if it exists or after creating it if it knows how to.
   *
   * @param name name of dialog to get
   * @return	The Dialog associated with name or <code>null</code>
   *		if not found and unable to create.
   */
  public Dialog getNamedDialog(String name) {
    Dialog dialog = null;
    if("controldialog".equalsIgnoreCase(name)) dialog = getControlDialog();
    else if("status".equalsIgnoreCase(name) ||
       "statuswindow".equalsIgnoreCase(name)) dialog = getStatusWindow();
    else if("showpoint".equalsIgnoreCase(name) ||
      "showpointdialog".equalsIgnoreCase(name)) dialog = getShowPointDialog();
    else if("gotopoint".equalsIgnoreCase(name) ||
      "gotopointdialog".equalsIgnoreCase(name)) dialog = getGotoPointDialog();
    else if("textdialog".equalsIgnoreCase(name)) dialog = getTextDialog();
    else if("filetypedialog".equalsIgnoreCase(name) ||
            "filetypes".equalsIgnoreCase(name)) dialog = getFileTypeDialog();
    else if("shapedialog".equalsIgnoreCase(name)) dialog = getShapeDialog();
    else if("colordialog".equalsIgnoreCase(name)) dialog = getColorDialog();
    else if("editcolordialog".equalsIgnoreCase(name))
      dialog = getEditColorDialog();
    else if("scaledialog".equalsIgnoreCase(name)) dialog = getScaleDialog();
    else if("cropdialog".equalsIgnoreCase(name)) dialog = getCropDialog();
    else if("filterdialog".equalsIgnoreCase(name)) dialog = getFilterDialog();
    else if("dataslicerdialog".equalsIgnoreCase(name)) dialog = getDataSlicerDialog();
    else if("coordinatemapdialog".equalsIgnoreCase(name)) 
      dialog = getCoordinateMapDialog();
    else if("savedialog".equalsIgnoreCase(name)) dialog = getSaveDialog();
    else if("regiondialog".equalsIgnoreCase(name)) dialog = getRegionDialog();
    else if("griddialog".equalsIgnoreCase(name)) dialog = getGridDialog();
    else if("formatdialog".equalsIgnoreCase(name)) dialog = getFormatDialog();
    else if("showmemory".equalsIgnoreCase(name) ||
	    "showmemorydialog".equalsIgnoreCase(name) ||
	    "memorydialog".equalsIgnoreCase(name) ||
	    "memory".equalsIgnoreCase(name)) dialog = getShowMemoryDialog();
    else dialog = getExistingNamedDialog(name);
    return dialog;
  }
  /**
   * Gets a named dialog only if it exists.
   *
   * @param name name of dialog to get
   * @return	The Dialog associated with name or
   *		 <code>null</code> if not found.
   */
  public Dialog getExistingNamedDialog(String name) {
    Dialog d = null;
    if(name != null) {
      d = getExistingNamedDialog(name, false, false);
      if(d == null) {
	if(name.indexOf('.') < 0) {
	  if(d == null) d = getExistingNamedDialog(name, true, false);
	  if(d == null) d = getExistingNamedDialog(name, true, true);
	}
      }
    }
    return d;
  }
  /**
   * Gets a named dialog only if it exists.
   *
   * @param name class name of dialog to get
   * @param ignoreCase <code>true</code> to ignore case
   * @param checkBaseClassNames <code>true</code> to ignore case
   * @return	The Dialog associated with name or
   *		 <code>null</code> if not found.
   */
  public Dialog getExistingNamedDialog(String name,
				       boolean checkBaseClassNames,
				       boolean ignoreCase) {
    // search existing dialogs in dialogList
    Dialog dialog = null;
    if(name != null) synchronized (dialogList) {
      Enumeration e = dialogList.elements();
      while((dialog == null) && e.hasMoreElements()) {
	Dialog d = (Dialog) e.nextElement();
	String existingName = d.getClass().getName();
	if(ignoreCase) {
	  if(existingName.equalsIgnoreCase(name)) dialog = d;
	  else if(checkBaseClassNames) {
	    int eBaseOff = existingName.lastIndexOf('.') + 1;
	    if((eBaseOff > 0) && (eBaseOff < existingName.length()) &&
	       existingName.substring(eBaseOff).equalsIgnoreCase(name))
	      dialog = d;
	  }
	}
	else {
	  if(existingName.equals(name)) dialog = d;
	  else if(checkBaseClassNames) {
	    int eBaseOff = existingName.lastIndexOf('.') + 1;
	    if((eBaseOff > 0) && (eBaseOff < existingName.length()) &&
	       existingName.substring(eBaseOff).equals(name))
	      dialog = d;
	  }
	}
      }
    }
    return dialog;
  }
  private Vector<String> failedClassNames = new Vector<String>();
  /**
   * Gets a named dialog only if it exists.
   *
   * @param classname class name of dialog to get
   * @param createAddsToMenu <code>true</code> to add dialog
   *			  to view menu if created
   * @return	The Dialog associated with name or
   *		 <code>null</code> if not found.
   */
  public Dialog getClassNamedDialog(String classname, boolean createAddsToMenu) {
    // first see if it exists
    Dialog dialog = getExistingNamedDialog(classname);
    if(dialog == null) synchronized (dialogList) {
      dialog = getExistingNamedDialog(classname);
      if(dialog == null) {
	// only try if we haven't failed before
	if(! failedClassNames.contains(classname)) try {
	  Class dialogClass =
	    CNUDisplayScript.findClass(classname, new String[] {"iiv.dialog"});
	  if(Dialog.class.isAssignableFrom(dialogClass)) {
	    Vector<Object> constructParams = new Vector<Object>(1);
	    constructParams.addElement(getParentFrame());
	    Object obj = CNUDisplayScript.getObject(dialogClass, constructParams);
	    dialog = (Dialog) obj;
	    if(dialog != null) {
	      dialog.setModal(false);
	      if(dialog instanceof CNUDialog) {
		CNUDialog cnudialog = (CNUDialog) dialog;
		cnudialog.setCNUViewer(this);
	      }
	      addDialog(dialog, createAddsToMenu);
	    }
	  }
	} catch (ClassNotFoundException cnfe) {
	  failedClassNames.addElement(classname);
	  Toolkit.getDefaultToolkit().beep();
	} catch (ObjectConstructionException oce) {
	  failedClassNames.addElement(classname);
	  Toolkit.getDefaultToolkit().beep();
	}
      }
    }
    if(dialog == null) Toolkit.getDefaultToolkit().beep();
    return dialog;
  }
  /**
   * Creates a script for all existing dialogs that implement toScript().
   *
   * @return the script
   */
  public StringBuffer existingDialogsToScript(StringBuffer sb) {
    if(sb == null) sb = new StringBuffer();
    Class[] classes = new Class[0];
    Object[] params = new Object[0];
    Dialog dialog = null;
    synchronized (dialogList) {
      Enumeration e = dialogList.elements();
      while((dialog == null) && e.hasMoreElements()) {
        Dialog d = (Dialog) e.nextElement();
        try {
	  Method method = d.getClass().getMethod("toScript", classes);
	  sb.append((String) method.invoke(d, params));
	  sb.append("CNUVIEWER.addDialog(script_rtn);");
        } catch (NoSuchMethodException nsme) { // ignore
        } catch (SecurityException se) { // ignore
        } catch (IllegalAccessException iae) { // ignore
        } catch (IllegalArgumentException iarge) { // ignore
        } catch (InvocationTargetException ite) { // ignore
        } catch (ClassCastException cce) { // ignore
        }
      }
    }
    return sb;
  }
  /**
   * Add a dialog to the list of known dialogs.
   *
   * @param dialog	The Dialog component to add.
   */
  public void addDialog(Dialog dialog) { addDialog(dialog, true); }
  /**
   * Add a dialog to the list of known dialogs.
   *
   * @param dialog	The Dialog component to add.
   * @param addToViewMenu <code>true</code> if dialog should be
   *			  add to view menu
   */
  public void addDialog(Dialog dialog, boolean addToViewMenu) {
    if(dialog == null) return;
    VisibleStateButtonUpdate vsbu = null;
    synchronized (dialogList) {
      if(! dialogList.contains(dialog)) {
	dialogList.addElement(dialog);
	if(addToViewMenu) {
	  String name = dialog.getTitle();
          if((name == null) || "".equals(name))
	    name = dialog.getClass().getName();
	  vsbu = new VisibleStateButtonUpdate(dialog, name, name+"...",
					      "Select to show " + name);
	}
      }
    }
    if(vsbu != null) addToViewMenu(vsbu);
  }
  /**
   * Add an EasyAddAbstractAction to the view menu.
   *
   * @param eaaa action to add
   */
  public void addToViewMenu(final EasyAddAbstractAction eaaa) {
    if(SwingUtilities.isEventDispatchThread()) {
      JMenu viewMenu = null;
      JMenuBar menuBar = getMenuBar();
      if(menuBar != null) {
	// viewMenu should be last menu in menuBar
	// last menu should be getMenuCount() - 1 but this
	// number is one too large probably because of break button
	// search down from this max instead of trusting
	//	for(int i=menuBar.getMenuCount()-1; (i>=0) && (viewMenu==null); i--)
	//
	//viewMenu = menuBar.getMenu(i);
        try {   
	  Class[] classes = { eaaa.getClass() };
	  Object[] params = { eaaa };
	  Method method = menuBar.getClass().getMethod("addToViewMenu", classes);
	  method.invoke(menuBar, params);
        } catch (NoSuchMethodException nsme) { // ignore
        } catch (SecurityException se) { // ignore
        } catch (IllegalAccessException iae) { // ignore
        } catch (IllegalArgumentException iarge) { // ignore
        } catch (InvocationTargetException ite) { // ignore
        } catch (ClassCastException cce) { // ignore
        }
      }
      //      if(viewMenu != null) eaaa.addCheckboxTo(viewMenu);
      //      else Toolkit.getDefaultToolkit().beep(); 
    }
    else {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { addToViewMenu(eaaa); }
      } );
    }
  }
  /**
   * Gets the file dialog for reading files.
   *
   * @return	if the first time called a new FileDialog,
   *		otherwise the previously created FileDialog
   * @see	FileDialog
   */
  public FileDialog getFileDialog(String filename) {
    if(FD == null) synchronized( dialogCreateLock ) {
      if(FD == null) {
	FD = new FileDialog(getParentFrame(), "Select File", FileDialog.LOAD);
	FD.setDirectory("");
	FD.setFile("");
	addDialog(FD, false);
      }
    }
    if(filename != null) filename = filename.trim();
    if("".equals(filename)) filename = null;
    if(filename != null) {
      CNUFile file = new CNUFile(filename);
      String basename = null;
      String dirname = null;
      if(file.isDirectory()) dirname = file.getPath();
      else {
	dirname = file.getParent();
	basename = file.getName();
      }
      if(dirname == null) dirname = "";
      if(basename == null) basename = "";
      FD.setDirectory(dirname);
      FD.setFile(basename);
    }
    return FD;
  }
  /**
   * Gets the file dialog for saving files.
   *
   * @return	if the first time called a new FileDialog,
   *		otherwise the previously created FileDialog
   * @see	FileDialog
   */
  public FileDialog getSaveFileDialog(String filename) {
    if(SFD == null) synchronized (dialogCreateLock) {
      if(SFD == null) {
	SFD = new FileDialog(getParentFrame(),
			     "Select Save File", FileDialog.SAVE);
	SFD.setDirectory("");
	SFD.setFile("");
	addDialog(SFD, false);
      }
    }
    if(filename != null) filename = filename.trim();
    if("".equals(filename)) filename = null;
    if(filename != null) {
      CNUFile file = new CNUFile(filename);
      String basename = null;
      String dirname = null;
      if(file.isDirectory()) dirname = file.getPath();
      else {
	dirname = file.getParent();
	basename = file.getName();
      }
      if(dirname == null) dirname = "";
      if(basename == null) basename = "";
      SFD.setDirectory(dirname);
      SFD.setFile(basename);
    }
    return SFD;
  }
  /**
   * Gets the ContinueDialog for warning when writing over a file.
   *
   * @return	if the first time called a new ContineDialog,
   *		otherwise the previously created ContinueDialog
   * @see	ContinueDialog
   */
  public ContinueDialog getContinueDialog() {
    if(ContinueD == null) synchronized (dialogCreateLock) {
      if(ContinueD == null) {
        ContinueD = new ContinueDialog(getParentFrame(), "File Exists",
		   "You are about to write over an existing file!");
        addDialog(ContinueD, false);
      }
    }
    return ContinueD;
  }
  /**
   * Gets the control dialog to use with this iiV invocation.
   *
   * @return	if the first time called a new control dialog
   *		otherwise the previously created control dialog
   */
  public CNUDialog getControlDialog() {
    if(controlDialog == null) synchronized (dialogCreateLock) {
      if(controlDialog == null) {
	controlDialog = (CNUDialog) getClassNamedDialog("ControlDialog", false);
	getCNUViewerActions().controlDialogVSBU.setComponent(controlDialog);
      }
    }
    return controlDialog;
  }
  /**
   * Gets the text dialog to use with this invocation of iiV.
   *
   * @return	if the first time called a new TextDialog,
   *		otherwise the previously created TextDialog
   * @see	TextDialog
   */
  public CNUDialog getTextDialog() {
    if(TD == null) synchronized (dialogCreateLock) {
      if(TD == null) {
	TD = (CNUDialog) getClassNamedDialog("TextDialog", false);
	getCNUViewerActions().textDialogVSBU.setComponent(TD);
      }
    }
    return TD;
  }
  /**
   * Gets the format dialog to use with this invocation of iiV.
   *
   * @return	if the first time called a new FormDialog,
   *		otherwise the previously created FormDialog
   * @see	FormatDialog
   */
  public CNUDialog getFormatDialog() {
    if(formatD == null) synchronized (dialogCreateLock) {
      if(formatD == null) {
	formatD = (CNUDialog) getClassNamedDialog("FormatDialog", false);
	getCNUViewerActions().formatDialogVSBU.setComponent(formatD);
      }
    }
    return formatD;
  }
  /**
   * Gets the shape dialog to use with this invocation of iiV.
   *
   * @return	if the first time called a new ShapeDialog,
   *		otherwise the previously created ShapeDialog
   * @see	ShapeDialog
   */
  public CNUDialog getShapeDialog() {
    if(shapeD == null) synchronized (dialogCreateLock) {
      if(shapeD == null) {
	shapeD = (CNUDialog) getClassNamedDialog("ShapeDialog", false);
	getCNUViewerActions().shapeDialogVSBU.setComponent(shapeD);
      }
    }
    return shapeD;
  }
  /**
   * Gets the scale dialog to use with this invocation of iiV.
   *
   * @return	if the first time called a new ScaleDialog,
   *		otherwise the previously created ScaleDialog
   * @see	ScaleDialog
   */
  public CNUDialog getScaleDialog() {
    if(SD == null) synchronized (dialogCreateLock) {
      if(SD == null) {
	SD = (CNUDialog) getClassNamedDialog("ScaleDialog", false);
	getCNUViewerActions().scaleDialogVSBU.setComponent(SD);
      }
    }
    return SD;
  }
  /**
   * Gets the grid dialog to use with this invocation of iiV.
   *
   * @return	if the first time called a new GridDialog,
   *		otherwise the previously created GridDialog
   * @see	GridDialog
   */
  public CNUDialog getGridDialog() {
    if(GD == null) synchronized (dialogCreateLock) {
      if(GD == null) {
	GD = (CNUDialog) getClassNamedDialog("GridDialog", false);
	getCNUViewerActions().gridDialogVSBU.setComponent(GD);
      }
    }
    return GD;
  }
  /**
   * Gets the filter dialog to use with this invocation of iiV.
   *
   * @return	if the first time called a new FilterDialog,
   *		otherwise the previously created FilterDialog
   * @see	FilterDialog
   */
  public CNUDialog getFilterDialog() {
    if(FLTRD == null) synchronized (dialogCreateLock) {
      if(FLTRD == null) {
	FLTRD = (CNUDialog) getClassNamedDialog("FilterDialog", false);
	getCNUViewerActions().filterDialogVSBU.setComponent(FLTRD);
      }
    }
    return FLTRD;
  }
  /**
   * Gets the data slicer dialog to use with this invocation of iiV.
   *
   * @return	if the first time called a new DataSlicerDialog,
   *		otherwise the previously created FilterDialog
   * @see	FilterDialog
   */
  public CNUDialog getDataSlicerDialog() {
    if(dataSlicerD == null) synchronized (dialogCreateLock) {
      if(dataSlicerD == null) {
	dataSlicerD = (CNUDialog) getClassNamedDialog("DataSlicerDialog", false);
	getCNUViewerActions().dataSlicerDialogVSBU.setComponent(dataSlicerD);
      }
    }
    return dataSlicerD;
  }
  /**
   * Gets the coordinate mapping dialog to use with this invocation of iiV.
   *
   * @return	if the first time called a new CoordinateMapDialog,
   *		otherwise the previously created CoordinateMapDialog
   * @see	CoordinateMapDialog
   */
  public CNUDialog getCoordinateMapDialog() {
    if(CMD == null) synchronized (dialogCreateLock) {
      if(CMD == null) {
	CMD = (CNUDialog) getClassNamedDialog("CoordinateMapDialog", false);
	getCNUViewerActions().coordinateMapDialogVSBU.setComponent(CMD);
      }
    }
    return CMD;
  }
  /**
   * Gets the save dialog to to use with this invocation of iiV.
   *
   * @return	if the first time called a new SaveDialog,
   *		otherwise the previously created SaveDialog
   * @see	SaveDialog
   */
  public CNUDialog getSaveDialog() {
    if(saveD == null) synchronized (dialogCreateLock) {
      if(saveD == null) {
	saveD = (CNUDialog) getClassNamedDialog("SaveDialog", false);
	getCNUViewerActions().saveDialogVSBU.setComponent(saveD);
      }
    }
    return saveD;
  }
  /**
   * Gets the region dialog to to use with this invocation of iiV.
   *
   * @return	if the first time called a new RegionDialog,
   *		otherwise the previously created RegionDialog
   * @see	RegionDialog
   */
  public CNUDialog getRegionDialog() {
    if(regionD == null) synchronized (dialogCreateLock) {
      if(regionD == null) {
	regionD = (CNUDialog) getClassNamedDialog("RegionDialog", false);
	getCNUViewerActions().regionDialogVSBU.setComponent(regionD);
      }
    }
    return regionD;
  }
  /** Gets the show memory dialog to use with this invocation of iiV.
   *
   * @return	if the first time called a new ShowMemoryDialog,
   *		otherwise the previously created ShowMemoryDialog
   * @see	ShowMemoryDialog
   */
  public CNUDialog getShowMemoryDialog() {
    if(memoryD == null) synchronized (dialogCreateLock) {
      if(memoryD == null) {
	memoryD = (CNUDialog) getClassNamedDialog("ShowMemoryDialog", false);
	getCNUViewerActions().showMemoryDialogVSBU.setComponent(memoryD);
      }
    }
    return memoryD;
  }
  /**
   * Gets the file type dialog to use with this invocation iiV.
   *
   * @return	if the first time called a new FileTypeDialog,
   *		otherwise the previously created FileTypeDialog
   * @see	FileTypeDialog
   */
  public CNUDialog getFileTypeDialog() {
    if(FTD == null) synchronized (dialogCreateLock) {
      if(FTD == null) {
	FTD = (CNUDialog) getClassNamedDialog("FileTypeDialog", false);
	getCNUViewerActions().fileTypeDialogVSBU.setComponent(FTD);
      }
    }
    return FTD;
  }
  /**
   * Gets the crop dialog to use with this invocation of iiV.
   *
   * @return	if the first time called a new CropDialog,
   *		otherwise the previously created CropDialog
   * @see	CropDialog
   */
  public CNUDialog getCropDialog() {
    if(cropD == null) synchronized (dialogCreateLock) {
      if(cropD == null) {
	cropD = (CNUDialog) getClassNamedDialog("CropDialog", false);
	getCNUViewerActions().cropDialogVSBU.setComponent(cropD);
      }
    }
    return cropD;
  }
  /**
   * Gets the color dialog to use with this invocation of iiV.
   *
   * @return	if the first time called a new ColorDialog,
   *		otherwise the previously created ColorDialog
   * @see	ColorDialog
   */
  public CNUDialog getColorDialog() {
    if(CD == null) synchronized (dialogCreateLock) {
      if(CD == null) {
	CD = (CNUDialog) getClassNamedDialog("ColorDialog", false);
	getCNUViewerActions().colorDialogVSBU.setComponent(CD);
      }
    }
    return CD;
  }
  /**
   * Gets the edit color dialog to use with this invocaton of iiV.
   *
   * @return	if the first time called a new EditColorDialog,
   *		otherwise the previously created EditColorDialog
   * @see	EditColorDialog
   */
  public CNUDialog getEditColorDialog() {
    if(ECD == null) synchronized (dialogCreateLock) {
      if(ECD == null) {
	ECD = (CNUDialog) getClassNamedDialog("EditColorDialog", false);
	getCNUViewerActions().editColorDialogVSBU.setComponent(ECD);
      }
    }
    return ECD;
  }
  /**
   * Sets the show point controller to use with this invocation of iiV.
   *
   * @param spci new ShowPointController.
   */
  private void setShowPointController(ShowPointControllerInterface spci) {
    synchronized (dialogCreateLock) { SPC = spci; }
  }
  /**
   * Gets the show point controller to use with this invocation of iiV.
   *
   * @return	if ShowPointController not set, a new ShowPointController,
   *		otherwise the previous ShowPointController
   * @see	ShowPointController
   */
  public ShowPointControllerInterface getShowPointController() {
    if(SPC == null) synchronized (dialogCreateLock) {
      if(SPC == null) SPC = new ShowPointController(this);
    }
    return SPC;
  }
  /**
   * Gets the show point dialog to use with this invocation of iiV.
   *
   * @return	if the first time called a new ShowPointDialog,
   *		otherwise the previously created ShowPointDialog
   * @see	ShowPointDialog
   */
  public CNUDialog getShowPointDialog() {
    if(SPD == null) synchronized (dialogCreateLock) {
      if(SPD == null) {
	SPD = (CNUDialog) getClassNamedDialog("ShowPointDialog", false);
	getCNUViewerActions().showPointDialogVSBU.setComponent(SPD);
      }
    }
    return SPD;
  }
  /**
   * Gets the goto point dialog to use with this invocation of iiV.
   *
   * @return	if the first time called a new GotoPointDialog,
   *		otherwise the previously created GotoPointDialog
   * @see	GotoPointDialog
   */
  public CNUDialog getGotoPointDialog() {
    if(GPD == null) synchronized (dialogCreateLock) {
      if(GPD == null) {
	GPD = (CNUDialog) getClassNamedDialog("GotoPointDialog", false);
	getCNUViewerActions().gotoPointDialogVSBU.setComponent(GPD);
      }
    }
    return GPD;
  }
  /** Gets the undo control object to use with this invocation of iiV.
   *
   * @return	if the first time called a new UndoRedo,
   *		otherwise the previously created UndoRedo
   * @see	UndoRedo
   */
  public UndoRedo getUndoRedo() {
    if(undoRedoObj == null) synchronized (dialogCreateLock) {
      if(undoRedoObj == null)
	undoRedoObj = new UndoRedo(this, true);
    }
    return undoRedoObj;
  }
  /**
   * Sets the select additions boolean. If <code>true</code> further
   * added objects will be selected.
   *
   * @param state	<code>true</code> to select
   *				or <code>false</code> to not select
   *				further additions
   */
  public void setSelectAdditions(final boolean state) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setSelectAdditions(state); }
      } );
    }
    else {
      ButtonModel bm = getCNUViewerActions().selectAdditionsCheckboxAction.getCheckboxButtonModel();
	if(bm.isSelected() != state) bm.setSelected(state);
    }
  }
  /**
   * Checks if added objects are being automaticly selected.
   *
   * @return	<code>true</code> if additions are selected
   *		or <code>false</code> if additions are not selected
   */
  public boolean getSelectAdditions() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() {
	  returnObject = new Boolean(getSelectAdditions());
	}
      };
      runWithReturn.invokeAndWait();
      return ((Boolean) runWithReturn.returnObject).booleanValue();
    }
    else return getCNUViewerActions().selectAdditionsCheckboxAction.getCheckboxButtonModel().isSelected();
  }
  /**
   * Sets the show insert boolean. If <code>true</code> further
   * added objects will be selected.
   *
   * @param state	<code>true</code> to show
   *			or <code>false</code> to not show
   */
  public void setShowInsertCursor(final boolean state) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setShowInsertCursor(state); }
      } );
    }
    else {
      ButtonModel bm = getCNUViewerActions().showInsertLocationCheckboxAction.getCheckboxButtonModel();
      if(bm.isSelected() != state) {
	bm.setSelected(state);
      }
      getCNUDisplay().setShowInsertCursor(state);
    }
  }
  /**
   * Checks if insert location is shown.
   *
   * @return	<code>true</code> if insert location shown
   *		or <code>false</code> if not shown
   */
  public boolean getShowInsertCursor() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() {
	  returnObject = new Boolean(getShowInsertCursor());
	}
      };
      runWithReturn.invokeAndWait();
      return ((Boolean) runWithReturn.returnObject).booleanValue();
    }
    else return getCNUViewerActions().showInsertLocationCheckboxAction.getCheckboxButtonModel().isSelected();
  }
  /**
   * Gets the file types choice.
   *
   * @return	current file type choice
   */
  public String getFileTypesChoice() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() {
	  returnObject = getCNUViewerActions().fileTypesChoiceModel.getSelectedItem(); }
      };
      runWithReturn.invokeAndWait();
      return (String) runWithReturn.returnObject;
    }
    else return (String) getCNUViewerActions().fileTypesChoiceModel.getSelectedItem();
  }
  /**
   * Gets an array file types based on current file types choice.  Wraps
   * around getFileTypesChoice to create list for STANDARD_FILE_TYPES and
   * RAW_FILE_TYPES.
   *
   * @return	an Object array of file types
   */
  public Object[] getFileTypes() {
    String fileType = getFileTypesChoice();
    if(STANDARD_FILE_TYPES.equals(fileType)) return stdFileClassTypes;
    else if(RAW_FILE_TYPE.equals(fileType)) {
      String[] fileClassTypes = { "iiv.io.CNURawImgFile" };
      return fileClassTypes;
    }
    else {
      String[] fileClassTypes = { fileType };
      return fileClassTypes;
    }
  }
  /**
   * Add a class to the list of file class types.
   *
   * @param fileClass	file class to be added
   */
  public boolean addFileClass(Class fileClass) {
    // make sure class name is not already in list
    if(selectFileClass(fileClass)) return true;
    if( java.awt.Component.class.isAssignableFrom(fileClass)
        || CNUData.class.isAssignableFrom(fileClass)
	|| CNUDisplayScript.class.isAssignableFrom(fileClass) ) {
      final String fileClassF=fileClass.getName();
      if(! SwingUtilities.isEventDispatchThread()) {
        SwingUtilities.invokeLater( new Runnable() {
		public void run() {
		  CNUViewerActions cnuviewerActions = getCNUViewerActions();
		  cnuviewerActions.fileTypesChoiceModel.addElement(fileClassF);
		  cnuviewerActions.fileTypesChoiceModel.setSelectedItem(fileClassF);
		}
	    } );
      } else {
	CNUViewerActions cnuviewerActions = getCNUViewerActions();
	cnuviewerActions.fileTypesChoiceModel.addElement(fileClassF);
	cnuviewerActions.fileTypesChoiceModel.setSelectedItem(fileClassF);
      }
      return true;
    }
    else {
      showStatus("Class " + fileClass + " not a valid display type");
      Toolkit.getDefaultToolkit().beep();
      return false;
    }
  }
  /**
   * Add a class name to the list of class types.
   *
   * @param className	name of class to add
   */
  public boolean addFileClass(String className) {
    if(className == null) return false;
    className = className.trim();
    if("".equals(className)) return false;
    // check if already in list - includes testing for default names
    if(selectFileClass(className)) return true;
    try {
      Class testClass = CNUDisplayScript.findClass(className);
      return addFileClass(testClass);
    } catch (ClassNotFoundException cnf) {
      showStatus("Could not find requested class " + className);
      Toolkit.getDefaultToolkit().beep();
      return false;
    }
  }
  /**
   * Select a listed file class.
   *
   * @param fileclass	the class to be selected
   */
  public boolean selectFileClass(Class fileclass) {
    return selectFileClass(fileclass.getName());
  }
  /**
   * Select a listed file type.
   *
   * @param className	name of class to be selected
   */
  public boolean selectFileClass(String className) {
    if(className == null) return false;
    className = className.trim();
    if("".equals(className)) return false;
    if("standard".equalsIgnoreCase(className)) className = STANDARD_FILE_TYPES;
    else if("raw".equalsIgnoreCase(className)) className = RAW_FILE_TYPE;
    if(! SwingUtilities.isEventDispatchThread()) {
      final String runClassName = className;
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() {
	  returnObject = new Boolean(selectFileClass(runClassName));
	}
      };
      runWithReturn.invokeAndWait();
      return ((Boolean) runWithReturn.returnObject).booleanValue();
    }
    // locate file class and select
    CNUViewerActions cnuviewerActions = getCNUViewerActions();
    for(int i = cnuviewerActions.fileTypesChoiceModel.getSize() - 1;
	i >= 0; i--) {
      String filetype =
	(String) cnuviewerActions.fileTypesChoiceModel.getElementAt(i);
      if(className.equals(filetype)) {
	cnuviewerActions.fileTypesChoiceModel.setSelectedItem(filetype);
	return true;
      }
    }
    return false;
  }
  /**
   * Sets the scaling option.
   *
   * @param scaleOption one of DEFAULT_SCALING, LAST_SCALING,
   *			POSITIVE_SCALING or NEGATIVE_SCALING
   */
  public void setScaleMode(final int scaleOption) {
    DisplayComponentDefaults.setScaleMode(scaleOption);
  }
  /**
   * Gets the current scaling option.
   *
   * @return	 one of DEFAULT_SCALING, LAST_SCALING,
   *		POSITIVE_SCALING or NEGATIVE_SCALING
   */
  public int getScaleMode() {
    return DisplayComponentDefaults.getScaleMode();
  }
  /**
   * Returns a Scale based on current mode and a given type.
   *
   * @param type	data type to calculate scale for
   * @return		scale based on current mode and given data type
   */
  public CNUScale getModeScale(int type) {
    // sets scaling based on user choice
    CNUScale sc = new CNUScale(1.0);
    switch(getScaleMode()) {
    default:
    case DEFAULT_SCALING:
      sc.setToDisplayDefault(type);
      break;
    case POSITIVE_SCALING:
      sc.setToDisplayPositive(type);
      break;
    case NEGATIVE_SCALING:
      sc.setToDisplayNegative(type);
      break;
    case LAST_SCALING:
      sc = CNUScale.getDefaultScale();
      break;
    }
    return sc;
  }
  /**
   * Sets the i dim limit mode displaying it as a Checkbox component.
   *
   * @param mode	<code>true</code> if i dimensions should be limited
   * @see	CropDialog
   */
  public void setIDimLimitMode(final boolean mode) {
    DisplayComponentDefaults.setIDimLimitMode(mode);
  }
  /**
   * Gets the i dim limit mode.
   * @return	<code>true</code> if i dimensions should be limited
   * @see	CropDialog
   */
  public boolean getIDimLimitMode() {
    return DisplayComponentDefaults.getIDimLimitMode();
  }
  /**
   * Sets the slice limit mode displaying it as a Checkbox component
   *
   * @param mode	<code>true</code> if slices should be limited
   * @see	CropDialog
   */
  public void setSliceLimitMode(final boolean mode) {
    DisplayComponentDefaults.setSliceLimitMode(mode);
  }
  /**
   * Gets the slice limit mode.
   *
   * @return	<code>true</code> if slices should be limited
   * @see	CropDialog
   */
  public boolean getSliceLimitMode() {
    return DisplayComponentDefaults.getSliceLimitMode();
  }
  /**
   * Sets the slice limits.
   *
   * @param firstSlice	first slice to display
   * @param lastSlice	last slice to display
   * @param nSlices	number of slices that could possibly be displayed
   *			Used to increase slice scrollbar lengths when needed
   * @param limitSlices	<code>true</code> if slices should be limited
   * @see	CropDialog
   */
  public void setSlices( int firstSlice, int lastSlice,
			 int nSlices, boolean limitSlices) {
    DisplayComponentDefaults.setDefaultSlices(firstSlice, lastSlice, nSlices);
    setSliceLimitMode(limitSlices);
  }
  /**
   * Gets the crop box state.
   *
   * @return	<code>true</code> if cropping is enabled
   */
  public boolean getCropBoxState() {
    return DisplayComponentDefaults.getDefaultCropState();
  }
  /**
   * Sets the crop box state.
   *
   * @param state	<code>true</code> to enable cropping
   * @see	CropDialog
   */
  public void setDefaultCropState(final boolean state) {
    DisplayComponentDefaults.setDefaultCropState(state);
  }
  /**
   * Sets the crop box from begin and end values.
   *
   * @param xbeg	horizontal beginning location of crop box
   * @param ybeg	vertical beginning location of crop box
   * @param xend	horizontal ending location of crop box
   * @param yend	vertical ending location of crop box
   * @param cropFlag	<code>true</code> to enable cropping
   * @see	CropDialog
   */
  public void setCropBox(int xbeg, int ybeg, int xend, int yend,
			 boolean cropFlag) {
    if(xbeg >= 0 && ybeg >= 0 && xend >= xbeg && yend >= xbeg)
      setCropBox(new Rectangle(xbeg, ybeg, xend - xbeg + 1, yend - ybeg + 1),
		 cropFlag);
    else setDefaultCropState(cropFlag);
  }
  /**
   * Sets the crop box from a Rectangle.
   *
   * @param cropBox	Rectangle describing crop box
   * @param cropFlag	<code>true</code> to enable cropping
   * @see	CropDialog
   */
  public void setCropBox(Rectangle cropBox, boolean cropFlag) {
    // maintain the CropDialog settings
    // getCropDialog().setCropValues(cropBox);
    // maintain the display component default
    DisplayComponentDefaults.setDefaultCrop(cropBox);
    setDefaultCropState(cropFlag);
  }
  /**
   * Sets the default color model.
   *
   * @param cm	the ColorModel to set default to
   */
  public void setDefaultColorModel(final ColorModel cm) {
    if(cm == null) return;
    DisplayComponentDefaults.setDefaultColorModel(cm);
  }
  /**
   * Gets the current default color model.
   *
   * @return	the ColorModel default is set to
   */
  public ColorModel getDefaultColorModel() {
    return DisplayComponentDefaults.getDefaultColorModel();
  }
  /**
   * Sets the default transparent color of default color model if it is an
   * IndexColorModel.
   *
   * @param trans	index of index color model to set to transparent or
   *			<code>-1</code> to unset
   */
  public void setDefaultTransparentColor( int trans ) {
    ColorModel cm = getDefaultColorModel();
    if(cm instanceof IndexColorModel) {
      setDefaultColorModel(
        CNUColorModel.getTransparentColorModel((IndexColorModel)cm, trans));
    }
  }
  /**
   * Gets the default transparent color.
   *
   * @return	index of index color model set to transparent or
   *		 <code>-1</code> if no transparent pixel set
   */
  public int getDefaultTransparentColor() {
    ColorModel cm = getDefaultColorModel();
    if(cm instanceof IndexColorModel)
      return ((IndexColorModel) cm).getTransparentPixel();
    else return -1;
  }
  /**
   * Show status and error messages.  Required to implement ShowStatus.
   *
   * @param s	string to print in status window
   */
  public void showStatus(String s) {
    StatusWindow sw = getStatusWindow();
    if(sw != null) sw.showStatus(s);
    else if(runningAsApplet) super.showStatus(s);
    else System.out.println(s);
  }
  /**
   * Show throwable objects.  Required to implement ShowStatus.
   *
   * @param t	the Throwable object to print in the status window
   */
  public void showStatus(Throwable t) {
    StatusWindow sw = getStatusWindow();
    if(sw != null) sw.showStatus(t);
    else t.printStackTrace();
  }
  /**
   * Save the text in the status window.
   *
   * @param filename	file to save to
   */
  public void saveStatus(String filename) {
    StatusWindow sw = getStatusWindow();
    if(sw != null) sw.saveText(filename);
  }
  /**
   * Clears the text in the status window.
   */
  public void clearStatus() {
    StatusWindow sw = getStatusWindow();
    if(sw != null) sw.clearText();
  }
  /**
   * Shows the status window.
   */
  public void showStatus() {
    StatusWindow sw = getStatusWindow();
    if(sw != null) sw.setVisible(true);
  }
  /**
   * Hides the status window.
   */
  public void hideStatus() {
    StatusWindow sw = getStatusWindow();
    if(sw != null) sw.setVisible(false);
  }
  /**
   * Initializes the status window.
   */
  public StatusWindow getStatusWindow() {
    if(statusWindow == null) {
      synchronized (dialogCreateLock) {
        if(statusWindow == null) {
          Frame pf = getParentFrame();
          if(pf != null) {
	    statusWindow = new StatusWindow(pf, programTitle + " Messages",
					    runningAsApplet, this);
	    if(statusWindow != null) {
	      getCNUViewerActions().statusWindowVSBU.setComponent(statusWindow);
	      addDialog(statusWindow, false);
	    }
          }
        }
      }
    }
    return statusWindow;
  }
  /**
   * Sets the current file via a thread trying standard file types.
   *
   * @param filename	name of file to read
   */
  public void threadSetFile(String filename) { threadSetFile(filename, null); }
  /**
   * Sets the current file via a thread trying given file types.
   *
   * @param filename		name of file to read
   * @param fileClassTypes	array of file types to try including
   *				class name strings and/or Class objects
   */
  public void threadSetFile(String filename, Object[] fileClassTypes) {
    if(filename != null) {
      filename = filename.trim();
      if( ! "".equals(filename)) {
	if(filename.equals("-")) setFile(filename);
        else {
          synchronized (displayThreadLock) {
            if(displayThread != null) stopDisplayThread();
	    // keep parameters long enough to initialize anonymous thread
	    final String setFilename = filename;
	    final Object[] setFileClassTypes = fileClassTypes;

	    // build anonymous thread to run setFile
            displayThread = new Thread() {
	      private String filename = setFilename;
	      private Object[] fileClassTypes = setFileClassTypes;
              public void run() {
                try {
                  setWaitMode(true);
                  setFile(filename, fileClassTypes);
                } finally { setWaitMode(false); }
              }
            };

            displayThread.setDaemon(true);
            displayThread.start();
          }
	}
      }
    }
  }
  private Vector<Object[]> displayWaiters = new Vector<Object[]>();
  public final static Class[] booleanClasses = { boolean.class };
  public final static Object[] trueParams = {Boolean.TRUE};
  public final static Object[] falseParams = {Boolean.FALSE};
  /**
   * Adds an objects to list to disable while displaying.  
   *
   * @param obj object that has boolean function to call when
   *            the display is waiting during a file read
   *            and call again when with the inverse state when
   *            done waiting
   * @param booleanMethodName name of obj method to call with
   *                          boolean param for current wait mode.
   *                          A typical method is "setEnabled" with
   *                          <code>false</code> waitState.  To disable
   *                          a JComponent object while waiting.
   * @param waitState boolean value to pass to method when setting
   *                  wait mode.  The inverse is set when turning
   *                  off wait mode.
   */
  public void addToDisplayWaiters(Object obj,
				  String booleanMethodName,
				  boolean waitState) {
    if(obj == null) return;
    try {
      Method method = obj.getClass().getMethod(booleanMethodName,
					       booleanClasses);
      final Object[] waitElement = new Object[] {
	  obj, method,
	  waitState? trueParams : falseParams,
	  waitState? falseParams : trueParams };
      if(! SwingUtilities.isEventDispatchThread()) {
	  SwingUtilities.invokeLater( new Runnable() {
		  public void run() {
		      displayWaiters.addElement(waitElement);
		  }
	      } );
	  return;
      } displayWaiters.addElement(waitElement);
    } catch (NoSuchMethodException nsme) {
	System.out.println("in CNUViewer.addToDisplayWaiters Object="
			   + obj + " boolean method="
			   + booleanMethodName + " not found");
    }
  }
  /**
   * Sets the wait mode.
   *
   * @param mode	<code>true</code> for wait mode,
   *			<code>false</code> otherwise
   */
  public void setWaitMode(final boolean mode) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setWaitMode(mode);	}
      } );
      return;
    }
    Enumeration e = displayWaiters.elements();
    while(e.hasMoreElements()) {
      Object obj[] = (Object[]) e.nextElement();
      try {
	((Method) obj[1]).invoke(obj[0], (Object[]) (mode? obj[2]: obj[3]));
      } catch (SecurityException se) { // ignore
      } catch (IllegalAccessException iae) { // ignore
      } catch (IllegalArgumentException iarge) { // ignore
      } catch (InvocationTargetException ite) { // ignore
      } catch (ClassCastException cce) { // ignore
      }
    }
  }
  /**
   * Sets the current file trying standard file types.
   *
   * @param filename	file to set as current and read if not in memory
   */
  public void setFile(String filename) { setFile(filename, null); }
  /**
   * Sets the current file trying given file types.
   *
   * @param filename		file to set as current and read if not
   *				in memory
   * @param fileClassTypes	array of file types to try including
   *				class name strings and/or Class objects
   */
  public void setFile(final String filename, Object[] fileClassTypes) {
    if(filename != null) {
      /*  2-11-05
        if(! SwingUtilities.isEventDispatchThread()) {
	SwingUtilities.invokeLater( new Runnable() {
	public void run() { setFile(filename); }
	} );
	}
	else {
	
      */
      setFilenameDocumentText(filename);
      Object obj = getDataOrImage(filename, fileClassTypes);
      if(obj != null) {
	setViewObject(obj);
	Frame pf = getParentFrame();
	if(pf != null) pf.setTitle(programTitle + " "  +
				   version + ":  " + filename);
	// display the new image
	if( getReadDisplaysState() ) displayImageData(obj);
      }
    }
  }
//  }
  /**
   * Sets the read displays state.
   *
   * @param state	<code>true</code> to set read displays option on,
   *			<code>false</code> to set read displays option off
   */
  public void setReadDisplaysState(final boolean state) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setReadDisplaysState(state); }
      } );
    }
    else {
      ButtonModel bm =
	getCNUViewerActions().readDisplaysCheckboxAction.getCheckboxButtonModel();
      if(bm.isSelected() != state) bm.setSelected(state);
    }
  }
  /**
   * Retrieves the read displays state.
   *
   * @return	<code>true</code> if read displays option is on,
   *		<code>false</code> if read displays option is off
   */
  public boolean getReadDisplaysState() {
    return
      getCNUViewerActions().readDisplaysCheckboxAction.getCheckboxButtonModel().isSelected();
  }
  /**
   * Sets the read anew state.
   *
   * @param state	<code>true</code> to set read anew option on,
   *			<code>false</code> to set read anew option off
   */
  public void setReadAnewState(final boolean state) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setReadAnewState(state); }
      } );
    }else {
      ButtonModel bm =
	getCNUViewerActions().readAnewCheckboxAction.getCheckboxButtonModel();
      if(bm.isSelected() != state) bm.setSelected(state);
    }
  }
  /**
   * Retrieves the read anew state.
   *
   * @return	<code>true</code> if read anew option is on,
   *		<code>false</code> if read anew option is off
   */
  public boolean getReadAnewState() {
    return 
      getCNUViewerActions().readAnewCheckboxAction.getCheckboxButtonModel().isSelected();
  }
  /**
   * Searches for and returns any object associated with a file
   * if read anew is off.  Returns <code>null</code> if no object is
   * found or read anew is on.  Searches current view object, current
   * color models, currently displayed objects, undo/redo lists and
   * all existing dialogs.  Objects searched for must be as noted by
   * parameter sameFileObject.  Dialogs searched must implement the
   * CNUFileObject interface.
   *
   * @param sameFileAsObject	Object that implements CNUFileObject or
   *				the name of file stored as a String or
   *				a CNUFile object or a File object or a URL
   *				object
   * @return			Object associated with the sameFileAsObject
   *				or <code>null</code> if the object is not found
   *				or the read anew mode is on
   * @see	CNUFileObject
   * @see	CNUFile
   * @see	java.io.File
   * @see	java.net.URL
   */
  public Object getFileObject(Object sameFileAsObject) {
    if(sameFileAsObject == null) return null;
    if( getReadAnewState() ) return null;
    if(sameFileAsObject instanceof String) {
      sameFileAsObject = ((String)sameFileAsObject).trim();
      if(sameFileAsObject.equals("")) return null;
    }
    Object obj = null;
    // check current viewObject
    if(obj == null) {
      Object vobj = getViewObject();
      if(vobj instanceof CNUFileObject)
        obj = ((CNUFileObject) vobj).getFileObject(sameFileAsObject);
    }
    // check current colorModel
    if(obj == null) {
      Object cobj = getDefaultColorModel();
      if(cobj instanceof CNUFileObject)
      obj = ((CNUFileObject) cobj).getFileObject(sameFileAsObject);
    }
    // check displayed components
    if(obj == null) obj = cnud.getFileObject(sameFileAsObject);    
    // check undo redo objects
    if(obj == null) obj = getUndoRedo().getFileObject(sameFileAsObject);
    if(obj == null) {
      // check with any appropriate dialog
      synchronized (dialogList) {
	Enumeration e = dialogList.elements();
        while((obj == null) && e.hasMoreElements()) {
          Object dobj = e.nextElement();
	  if(dobj instanceof CNUFileObject)
	    obj = ((CNUFileObject) dobj).getFileObject(sameFileAsObject);
        }
      }
    }
    return obj;
  }
  /**
   * Creates or retrieves a data or an image object associated with a file.
   *
   * @param filename		file associated with data or image object
   */
  public Object getDataOrImage(String filename) {
    return getDataOrImage(filename, null);
  }
  /**
   * Creates or retrieves a data or an image object associated with a file.
   *
   * @param filename		file associated with data or image object
   * @param fileClassTypes	array of file types to try reading file as
   *				if <code>null</code> getFileTypes() is
   *				is queried
   * @see #getFileTypes()
   */
  public Object getDataOrImage(String filename, Object[] fileClassTypes) {
    if(filename == null) return null;
    filename = filename.trim();
    if(filename.equals("")) return null;
    if("-".equals(filename)) {
      // read from standard in
      Thread thread1 = new Thread(new CNUDisplayScript(System.in, this));
      thread1.setDaemon(true);
      thread1.start();
      return null;
    }
    CNUFile filetmp = new CNUFile(filename);
    if( filetmp.isDirectory() ) {
      Toolkit.getDefaultToolkit().beep();
      return null;
    }
    Object obj = getFileObject(filename);
    if(obj == null) {
      if(fileClassTypes == null) fileClassTypes = getFileTypes();
      if((fileClassTypes == null) || (fileClassTypes.length == 0)) {
        Toolkit.getDefaultToolkit().beep();
	showStatus("Empty file class list");
	return null;
      }
      // params needed to build any image file data
      Vector<Object> fileClassConstParams = new Vector<Object>(1);
      fileClassConstParams.addElement(filename);
      //      fileClassConstParams.addElement(this);
      // Track exceptions
      Exception exceptions[] = new Exception[fileClassTypes.length];
      // try all file class types
      int i = 0;
      while((obj == null) && (i < fileClassTypes.length) &&
	    ! Thread.currentThread().isInterrupted()) {
        try {
	  Class objectClass = null;
	  if(fileClassTypes[i] instanceof String)
	    objectClass =
	       CNUDisplayScript.findClass((String) fileClassTypes[i]);
	  else if(fileClassTypes[i] instanceof Class)
	    objectClass = (Class) fileClassTypes[i];
	  if(objectClass != null) {
	    obj = CNUDisplayScript.getObject(objectClass,
					     fileClassConstParams);
	    // classes that need special attention
	    // read to check if valid file type
	    if(obj instanceof CNUDisplayScript)
	      ((CNUDisplayScript) obj ).setCNUViewer(this);
	    else if(obj instanceof iiVBshScript)
	      ((iiVBshScript) obj ).setCNUViewer(this);
	    else if(obj instanceof CNUStdImgFile)
	      ((CNUStdImgFile) obj ).readImage();
	    else if(obj instanceof CNUImgFile) ((CNUImgFile) obj ).readData();
	  }
        } catch (Exception e) {
	  obj = null;
	  exceptions[i] = e;
        }
        i++;
      }
      if(obj == null) {
        for(i=0; i<exceptions.length; i++) {
	  showStatus("Error reading as class " + fileClassTypes[i]);
	  if(exceptions[i] != null) showStatus((Throwable) exceptions[i]);
        }
      }
    }
    if(Thread.currentThread().isInterrupted()) {
      showStatus("Interrupted reading file=" + filename);
      obj = null;
    }
    else if(obj == null) showStatus("Error reading file=" + filename);
    else addToFilenames(filename, obj.getClass());
    return obj;
  }
  /**
   * Overlays image data via a thread.
   *
   * @param obj		Object representing data to overlay
   * @param groupFlag	if <code>true</code> overlayed objects will be
   *			grouped with the object they overlay
   */
  public void threadOverlayImageData(Object obj, boolean groupFlag) {
    if(obj != null) synchronized (displayThreadLock) {
      if(displayThread != null) stopDisplayThread();
      // keep parameters long enough to initialize anonymous thread
      final Object overlayObj = obj;
      final boolean localGroupFlag = groupFlag;

      // anonymous thread created to run overlay
      displayThread = new Thread() {
	private Object obj = overlayObj;
	private boolean groupFlag = localGroupFlag;
        public void run() {
	  int count = 0;
          try {
            setWaitMode(true);
	    count = getCNUDisplay().overlay(obj, groupFlag);
          } finally {
	    setWaitMode(false);
	    if(count < 1) Toolkit.getDefaultToolkit().beep();
	  }
        }
      };

      displayThread.setDaemon(true);
      displayThread.start();
    }
  }
  /**
   * Creates and displays an intensity projection of the current view object.
   *
   */
  public void intensityProject() {
    threadProjectData(getViewObject());
  }
  /**
   * Displays intensity projected image data via a thread.
   *
   * @param obj	Object representing data to display
   */
  public void threadProjectData(Object obj) {
      threadDisplayImageData(obj, INTENSITY_PROJECT_DATA);
  }
  /**
   * Displays image data via a thread.
   *
   * @param obj	Object representing data to display
   */
  public void threadDisplayImageData(Object obj) {
      threadDisplayImageData(obj, NORMAL_DATA_DISPLAY);
  }
  /**
   * Displays image data via a thread.
   *
   * @param obj	Object representing data to display
   * @param intensityProj if <code>true</code> create intensity projections
   */
  public void threadDisplayImageData(Object obj, boolean intensityProj) {
    threadDisplayImageData(obj, intensityProj ? INTENSITY_PROJECT_DATA : NORMAL_DATA_DISPLAY);
  }
  /**
   * Displays image data via a thread.
   *
   * @param obj	Object representing data to display
   * @param displayImageDataMode image data is display according to this mode
   */
  public void threadDisplayImageData(Object obj, int displayImageDataMode) {
    if(obj != null) synchronized (displayThreadLock) {
      if(displayThread != null) stopDisplayThread();
      // keep parameters long enough to initialize anonymous thread
      final Object displayObj = obj;
      final int dataMode = displayImageDataMode;

      // anonymous thread created to run displayImageData
      displayThread = new Thread() {
	private Object obj = displayObj;
        public void run() {
          try {
            setWaitMode(true);
            displayImageData(obj, dataMode);
          } finally { setWaitMode(false); }
        }
      };

      displayThread.setDaemon(true);
      displayThread.start();
    }
  }
  /**
   * Stops the display thread.
   *
   */
  public void stopDisplayThread() { 
   synchronized (displayThreadLock) {
      if(displayThread != null) {
	boolean continueWaiting = true;
	while(displayThread.isAlive() && continueWaiting) {
	  try {
	    //	displayThread.stop(); // not safe
	    displayThread.interrupt();
	    displayThread.join(5000);
	  } catch (InterruptedException ie) {
	    Thread.currentThread().interrupt(); // keep this threads interrupted status
	  }
	  if(displayThread.isAlive()) {
	    QueryDialog qd = new QueryDialog(getParentFrame(), "Not Responding",
					     "Display thread not responding to interrupt request.  Continue waiting?", new String[] {"Yes", "No"});
	    qd.beep();
	    qd.setVisible(true);
	    continueWaiting = (qd.getSelection() == 1);
	  }
	}
	displayThread = null;
      }
    }
  }
  /**
   * Displays a component at the given location.
   *
   * @param incomp	Component to display
   * @param location	Location to place component at.
   *			If <code>null</code> next layout position is used.
   */
  public void displayComponent(final Component incomp, final Point location) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { displayComponent(incomp, location); }
      } );
    }
    else {
      Component comp = incomp;
      if( cnud.isAncestorOf(comp) ) {
	// already displayed but we will try cloning
	try {
	  Method method = comp.getClass().getMethod("clone", (Class[]) null);
	  comp = (Component) method.invoke(comp, (Object[]) null);
	} catch (NoSuchMethodException nsme) {
	  showStatus(nsme); comp = null;
	} catch (SecurityException se) {
	  showStatus(se); comp = null;
	} catch (IllegalAccessException iae) {
	  showStatus(iae); comp = null;
	} catch (IllegalArgumentException iarge) {
	  showStatus(iarge); comp = null;
	} catch (InvocationTargetException ite) {
	  showStatus(ite); comp = null;
	}
	if(comp == null)
	  showStatus("Component already displayed and failed cloning");
      }
      if(comp != null) {
	if(location == null) cnud.addAndRepaint(comp);
	else cnud.add(comp, new CNUDisplayConstraints(location, true,
		      getSelectAdditions()));
      }
    }
  }
  /**
   * Displays image data.
   *
   * @param obj	Object to display which may be a java.awt.Component,
   *		CNUDisplayScript, or CNUData
   */
  public void displayImageData(Object obj) {
      displayImageData(obj, NORMAL_DATA_DISPLAY);
  }
  /**
   * Displays image data.
   *
   * @param obj	Object to display which may be a java.awt.Component,
   *		CNUDisplayScript, or CNUData
   * @param intensityProj if <code>true</code> create intensity projections
   */
    public void displayImageData(Object obj, boolean intensityProj) {
      displayImageData(obj, intensityProj ? INTENSITY_PROJECT_DATA : NORMAL_DATA_DISPLAY);
    }
  /**
   * Displays image data.
   *
   * @param obj	Object to display which may be a java.awt.Component,
   *		CNUDisplayScript, or CNUData
   * @param displayImageDataMode image data is display according to this mode
   */
    public void displayImageData(Object obj, int displayImageDataMode) {
    if(obj == null) return;
    if(obj instanceof CNUDisplayScript) {
	getUndoRedo().startSteps();
	((CNUDisplayScript) obj).display();
	getUndoRedo().finishUndoSteps("CNUDisplayScript");
    }
    else if(obj instanceof iiVBshScript) {
	getUndoRedo().startSteps();
   	((iiVBshScript) obj).display();
	getUndoRedo().finishUndoSteps("iiVBshScript");
    }
    else if(obj instanceof Component) displayComponent((Component) obj, null);
    else if(obj instanceof CNUData) {
      CNUData inData = (CNUData) obj;
      // sets slices and columns based on current image
      CNUDimensions inDatasize = inData.getDimensions();
      if(inDatasize == null) {
	showStatus("Data missing dimensions");
	return;
      }
      // get the max number of slices
      int maxSlices = inDatasize.zdim();
      maxSlices = Math.max(maxSlices, inDatasize.xdim());
      maxSlices = Math.max(maxSlices, inDatasize.ydim());
      // make sure slice limit range and number of columns as least as
      // large as values
      DisplayComponentDefaults.setDefaultSlices(-1, -1, maxSlices);
      setNumberOfColumns(getNumberOfColumns(), maxSlices);
      // get the first and last i dim numbers
      int firstIDim = -1;
      int lastIDim = -1;
      if( getIDimLimitMode() ) {
	firstIDim = DisplayComponentDefaults.getDefaultFirstIDimLimit();
	lastIDim = DisplayComponentDefaults.getDefaultLastIDimLimit();
      }

      // display data
      if(displayImageDataMode == ORTHOGONAL_DATA_DISPLAY) {
        int[] indices = DisplayComponentDefaults.getGotoLocationIndices();
        cnud.Display(inData, CNUDimensions.TRANSVERSE,
		   indices[2], indices[2], firstIDim, firstIDim, false);
        cnud.Display(inData, CNUDimensions.CORONAL,
		   indices[1], indices[1], firstIDim, firstIDim, false);
        cnud.Display(inData, CNUDimensions.SAGITTAL,
		   indices[0], indices[0], firstIDim, firstIDim, false);
      }
      else {
        // get the first and last slice numbers
        int firstSlice = -1;
        int lastSlice = -1;
        if( getSliceLimitMode() ) {
	  firstSlice = DisplayComponentDefaults.getDefaultFirstSliceLimit();
	  lastSlice = DisplayComponentDefaults.getDefaultLastSliceLimit();
        }
        cnud.Display(inData, getSliceViewMode(),
		   firstSlice, lastSlice, firstIDim, lastIDim, (displayImageDataMode==INTENSITY_PROJECT_DATA));
      }
    }
    cnudSP.validate();
  }
  /**
   * Add a name to, and/or select a name on, the filename list.
   *
   * @param filename	name of file to add and/or select
   * @param fileClass	Class associate with the file
   */
  public void addToFilenames(String filename, Class fileClass) {
    if(filename == null) return;
    filename = filename.trim();
    if( filename.equals("") ) return;
    FileListElement fle = new FileListElement(filename, fileClass);
    addToFileNames(fle);
  }
  /**
   * Add a file name element to, and/or select on, the filename list.
   * Kept private for now because I don't want to advertisze
   * FileListElement.
   *
   * @param fle	file name element
   */
  private void addToFileNames(final FileListElement fle) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { addToFileNames(fle); }
      } );
    }
    else {
      int index;
      CNUViewerActions cnuviewerActions = getCNUViewerActions();
      index = cnuviewerActions.fileNameList.getIndexOf(fle);
      if(index < 0) {
	cnuviewerActions.fileNameList.addElement(fle);
	index = cnuviewerActions.fileNameList.getIndexOf(fle);
      }
      cnuviewerActions.fileNameList.setSelectedItem(fle);
      cnuviewerActions.fileNameListSelectionModel.setSelectionInterval(index, index);
      //filenameL.ensureIndexIsVisible(index);
    }
  }
  /**
   * Sets the current file to the list value at index.
   *
   * @param index to file in list
   */
   public void setFileFromList(final int index) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setFileFromList(index); }
      } );
    }
    else {
      CNUViewerActions cnuviewerActions = getCNUViewerActions();
      if((index >= 0) && (index < cnuviewerActions.fileNameList.getSize())) {
	FileListElement fle = (FileListElement)
	  cnuviewerActions.fileNameList.getElementAt(index);
	Object[] classTypes = new Object[1];
	classTypes[0] = fle.getFileClass();
	threadSetFile(fle.getFileName(), classTypes);
      }
      else Toolkit.getDefaultToolkit().beep();
    }
   }
  /**
   * Sets the current view object.
   *
   * @param obj	current view object
   */
  public void setViewObject(final Object obj) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() { setViewObject(obj); }
	} );
    }
    else {
      viewObject = obj;
      getCNUViewerActions().updateActionStates();
    }
  }
  /**
   * Gets the current view object.
   *
   * @return	current view object
   */
  public Object getViewObject() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	  public void run() { returnObject = getViewObject(); }
	};
      runWithReturn.invokeAndWait();
      return runWithReturn.returnObject;
    }
    return viewObject;
  }
  /**
   * Sets the slice view mode.
   *
   * @param viewMode	CNUDimensions.TRANSVERSE, CNUDimensions.CORONAL or
   *			CNUDimensions.SAGITTAL
   */
  public void setSliceViewMode( final int viewMode ) {
    DisplayComponentDefaults.setSliceViewMode(viewMode);
  }
  /**
   * Gets the slice view mode.
   *
   * @return	CNUDimensions.TRANSVERSE, CNUDimensions.CORONAL or
   *		CNUDimensions.SAGITTAL
   */
  public int getSliceViewMode() {
    return DisplayComponentDefaults.getSliceViewMode();
  }
  /**
   * Sets the slice labels and orientation labels view mode.
   *
   * @param sliceLabelsOn	<code>true</code> to view slice labels,
   *				<code>false</code> otherwise
   * @param orientationLabelsOn	<code>true</code> to view orientation
   *				labels, <code>false</code> otherwise
   */
  public void setSliceAndOrientationLabels(final boolean sliceLabelsOn,
					   final boolean orientationLabelsOn) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() {
	  setSliceAndOrientationLabels(sliceLabelsOn, orientationLabelsOn);
	}
      } );
    }
    else {
      CNUViewerActions cnuviewerActions = getCNUViewerActions();
      ButtonModel sbm =
	cnuviewerActions.sliceLabelsCheckboxAction.getCheckboxButtonModel();
      if(sbm.isSelected() != sliceLabelsOn) sbm.setSelected(sliceLabelsOn);
      ButtonModel obm =
	cnuviewerActions.orientationLabelsCheckboxAction.getCheckboxButtonModel();
      if(obm.isSelected() != orientationLabelsOn)
	  obm.setSelected(orientationLabelsOn);
    }
  }
  /**
   * Gets the slice labels view mode.
   *
   * @return	<code>true</code> for viewing slice labels,
   *		<code>false</code> otherwise
   */
  public boolean getSliceLabelOn() {
    return getCNUViewerActions().sliceLabelsCheckboxAction.getCheckboxButtonModel().isSelected();
  }
  /**
   * Gets the orientation labels view mode.
   *
   * @return	<code>true</code> for viewing orientation
   *		labels, <code>false</code> otherwise
   */
  public boolean getOrientationLabelsOn() {
    return getCNUViewerActions().orientationLabelsCheckboxAction.getCheckboxButtonModel().isSelected();
  }
  /**
   * Gets the iValue labels view mode.
   *
   * @return	<code>true</code> for viewing iValue labels,
   *		<code>false</code> otherwise
   */
  public boolean getIValueLabelOn() {
    return getCNUViewerActions().iValueLabelsCheckboxAction.getCheckboxButtonModel().isSelected();
  }
  /**
   * Sets the iValue labels view mode.
   *
   * @param iValueLabelOn	<code>true</code> to view iValue labels,
   *				<code>false</code> otherwise
   */
  public void setIValueLabelOn(final boolean iValueLabelOn) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() {
	  setIValueLabelOn(iValueLabelOn);
	}
      } );
    }
    else {
      CNUViewerActions cnuviewerActions = getCNUViewerActions();
      ButtonModel sbm =
	cnuviewerActions.iValueLabelsCheckboxAction.getCheckboxButtonModel();
      if(sbm.isSelected() != iValueLabelOn) sbm.setSelected(iValueLabelOn);
    }
  }
  /**
   * Sets the default vertical flip mode and update corresponding check box.
   *
   * @param flipV	<code>true</code> to flip images vertically,
   *			<code>false</code> to not flip images vertically
   */
  public void setDefaultFlipV(final boolean flipV) {
      DisplayComponentDefaults.setDefaultFlipV(flipV);
  }
  /**
   * Sets the default horizontal flip mode and update corresponding check box.
   *
   * @param flipH	<code>true</code> to flip images horizontally,
   *			<code>false</code> to not flip images horizontally
   */
  public void setDefaultFlipH(final boolean flipH) {
      DisplayComponentDefaults.setDefaultFlipH(flipH);
  }
  /**
   * Sets and display the default rotation angle.
   *
   * @param angle	rotation angle in degrees
   * @see	DisplayComponent
   */
  public void setDefaultRotation(double angle) {
    DisplayComponentDefaults.setDefaultRotation(angle);
    updateDefaultRotationState();
  }
  /**
   * Updates the displayed default rotation state to correspond
   * with the current default rotations.
   */
  public void updateDefaultRotationState () {
    double angle = DisplayComponentDefaults.getDefaultRotation();
    setDefaultRotationState(angle != 0.0);
  }
  /**
   * Sets the default rotation state.
   *
   * @param state	<code>true</code> to rotate by default
   */
  public void setDefaultRotationState(final boolean state) {
    DisplayComponentDefaults.setDefaultRotateState(state);
  }
  /**
   * Sets and display the default vertical and horizontal zooms to the same
   * value.
   *
   * @param zoom	zoom factor
   */
  public void setDefaultZoom(double zoom) {
    setDefaultZoomV(zoom); setDefaultZoomH(zoom);
  }
  /**
   * Sets and display the default vertical and horizontal zooms.
   *
   * @param zoomV	vertical zoom factor
   * @param zoomH	horizontal zoom factor
   */
  public void setDefaultZoom(double zoomV, double zoomH) {
    setDefaultZoomV(zoomV); setDefaultZoomH(zoomH);
  }
  /**
   * Sets and display the default vertical zoom.
   *
   * @param zoom	vertical zoom factor
   */
  public void setDefaultZoomV(double zoom) {
    DisplayComponentDefaults.setDefaultZoomV(zoom);
    updateDefaultZoomState();
  }
  /**
   * Sets and display the default horizontal zoom.
   *
   * @param zoom	horizontal zoom factor
   */
  public void setDefaultZoomH(double zoom) {
    DisplayComponentDefaults.setDefaultZoomH(zoom);
    updateDefaultZoomState();
  }
  /**
   * Updates the displayed default zooms to correspond with the current
   * default zooms.
   */
  public void updateDefaultZoomState () {
    double zoomV = DisplayComponentDefaults.getDefaultZoomV();
    double zoomH = DisplayComponentDefaults.getDefaultZoomH();
    setDefaultZoomState((zoomV != 1.0) || (zoomH != 1.0));
  }
  /**
   * Sets the default zoom state.
   *
   * @param state	<code>true</code> to zoom by default
   */
  public void setDefaultZoomState(final boolean state) {
    DisplayComponentDefaults.setDefaultZoomState(state);
  }
  /**
   * Sets the number of columns to display and increase scrollbar maximum
   * to at least the given possible value.
   *
   * @param numberOfColumns	number of columns
   * @param possible		possible number of columns
   */
  public void setNumberOfColumns(int numberOfColumns, int possible) {
      getCNUDisplay().setNumberOfColumns(numberOfColumns, possible);
  }
  /**
   * Sets the number of columns to display.
   *
   * @param numberOfColumns	number of columns
   */
  public void setNumberOfColumns(final int numberOfColumns) {
    getCNUDisplay().setNumberOfColumns(numberOfColumns);
  }
  /**
   * Gets the current number of columns setting.
   *
   * @return	number of columns
   */
  public int getNumberOfColumns() {
    return getCNUDisplay().getNumberOfColumns();
  }
  /**
   * Sets the wait cursor over all applicable windows.
   */
  public void setWaitCursor() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setWaitCursor(); }
      } );
    }
    else {
      // get cursors before setting wait cursor the first time
      if(normalCursor == null) normalCursor = Cursor.getDefaultCursor();
      if(waitCursor == null) waitCursor = new Cursor(Cursor.WAIT_CURSOR);
      this.setCursor(waitCursor);
      cnudSP.setCursor(waitCursor);
      // sets cursor in all dialogs
      synchronized (dialogList) {
	if(controlDialog != null) controlDialog.setCursor(waitCursor);
	Enumeration e = dialogList.elements();
	while(e.hasMoreElements()) {
	  Object obj = e.nextElement();
	  // Windows version has problem setting curor on FileDialogs
	  if((obj instanceof Dialog) && !(obj instanceof FileDialog))
	    ((Dialog) obj).setCursor(waitCursor);
	}
      }
    }
  }
  /**
   * Sets the normal cursor over all applicable windows.
   */
  public void setNormalCursor() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setNormalCursor(); }
      } );
    }
    else {
      if(normalCursor == null) normalCursor = Cursor.getDefaultCursor();
      this.setCursor(normalCursor);
      cnudSP.setCursor(normalCursor);
      // sets cursor in all dialogs
      synchronized (dialogList) {
	if(controlDialog != null) controlDialog.setCursor(normalCursor);
	Enumeration e = dialogList.elements();
	while(e.hasMoreElements()) {
	  Object obj = e.nextElement();
	  // Windows version has problem setting cursor on FileDialogs
	  if((obj instanceof Dialog) && !(obj instanceof FileDialog))
	    ((Dialog) obj).setCursor(normalCursor);
	}
      }
    }
  }
  /**
   * Invokes file dialog to browse files.
   */
  public void browseFiles() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { browseFiles(); }
      } );
    }
    else {
      String path = getFilenameDocumentText();
      if(path != null) {
	path = path.trim();
	if(path.equals("")) path = null;
      }
      if(path == null) path = ".";
      FileDialog fd = getFileDialog(path);
      fd.setVisible(true);
      if(fd.getFile() != null) {
	String file = fd.getDirectory() + fd.getFile();
	threadSetFile(file);
      }
    }
  }
  /**
   * Runs CNUViewer as a standalone program setting up a
   * frame, adding an new instance of CNUViewer and initializing
   * and starting CNUViewer as an applet server would.
   *
   * @param args	array of arguments from the command line
   * @exception IOException thrown some how
   */
  static public void main(String[] args) throws IOException {
    JFrame f = new JFrame(programTitle + " "  + version);
    final CNUViewer cnuv = new CNUViewer();
    f.getContentPane().add("Center", cnuv);
    cnuv.init(args);
    f.addWindowListener(
      // build anonymous class to handle window closing events
      new WindowAdapter()  {
        public void windowClosing(WindowEvent e) { cnuv.destroy(); }
      }
    );
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    f.setSize(d.width-100, d.height-100);
    f.setVisible(true);
/*
    URL autoURL=cnuv.getClass().getClassLoader().getResource("iivautofile");
    if( autoURL != null ) {
      cnuv.setFile(autoURL.toString());
      cnuv.setFile(""); // clear the filename text field
    }
*/
    cnuv.start();
  }
} // end CNUViewer class
