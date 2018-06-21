package iiv;
import iiv.display.*;
import iiv.gui.*;
import iiv.data.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;
import javax.swing.*;
/**
 * CNUViewerActions stores addable actions for CNUViewer.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUViewer
 * @since	iiV1.155
 */
public class CNUViewerActions {
  public int fileTypesCH_originalCount;

  public EasyAddAbstractAction browseAction;
  public EasyAddAbstractAction readDisplaysCheckboxAction;
  public EasyAddAbstractAction readAnewCheckboxAction;
  public EasyAddAbstractAction displayAction;
  public EasyAddAbstractAction printAction;
  public DefaultComboBoxModel fileTypesChoiceModel =
    new DefaultComboBoxModel();
  public String fileTypesTipText =
    "Pull to select file type(s) used for reading new images";
  public EasyAddAbstractAction quitAction;
  public EasyAddAbstractAction helpAction;
  public EasyAddAbstractAction overlayAction;
  public EasyAddAbstractAction overlayNgroupAction;
  public EasyAddAbstractAction intensityProjectAction;
  public EasyAddAbstractAction displayOrthogonalViewsAction;
  public EasyAddAbstractAction groupAction;
  public EasyAddAbstractAction groupOverlappingAction;
  public EasyAddAbstractAction ungroupAction;
  public EasyAddAbstractAction selectAllAction;
  public EasyAddAbstractAction selectTopAction;
  public EasyAddAbstractAction selectBottomAction;
  public EasyAddAbstractAction selectSameDataAction;
  public EasyAddAbstractAction selectAdditionsCheckboxAction;
  public EasyAddAbstractAction copyAction;
  public EasyAddAbstractAction copyScriptAction;
  public EasyAddAbstractAction copyImageAction;
  public EasyAddAbstractAction copyTextAction;
  public EasyAddAbstractAction deleteAction;
  public EasyAddAbstractAction clearAction;
  public EasyAddAbstractAction pasteAction;
  public EasyAddAbstractAction pasteScriptAction;
  public EasyAddAbstractAction pasteImageAction;
  public EasyAddAbstractAction pasteTextAction;
  public EasyAddAbstractAction showInsertLocationCheckboxAction;
  public EasyAddAbstractAction relayoutAction;
  public EasyAddAbstractAction refreshAction;
  public EasyAddAbstractAction limitIDimCheckboxAction;
  public EasyAddAbstractAction limitSlicesCheckboxAction;
  public EasyAddAbstractAction cropCheckboxAction;
  public EasyAddAbstractAction zoomCheckboxAction;
  public EasyAddAbstractAction rotateCheckboxAction;

  public EasyAddAbstractAction breakAction;

  public Document filenameDoc = new PlainDocument();
  public ActionListener filenameActionListener;
  public DefaultComboBoxModel fileNameList = new DefaultComboBoxModel();
  public DefaultListSelectionModel fileNameListSelectionModel =
      new DefaultListSelectionModel();

  public EasyAddAbstractAction orientationLabelsCheckboxAction;
  public EasyAddAbstractAction sliceLabelsCheckboxAction;
  public EasyAddAbstractAction iValueLabelsCheckboxAction;
  public EasyAddAbstractAction applyLabelsAction;

  public EasyAddAbstractAction applyFlipsAction;
  public EasyAddAbstractAction flipHCheckboxAction;
  public EasyAddAbstractAction flipVCheckboxAction;
  public VisibleStateButtonUpdate toolMenuVSBU;
  public VisibleStateButtonUpdate fileTypeDialogVSBU;
  public VisibleStateButtonUpdate controlDialogVSBU;
  public VisibleStateButtonUpdate statusWindowVSBU;
  public VisibleStateButtonUpdate showPointDialogVSBU;
  public VisibleStateButtonUpdate gotoPointDialogVSBU;
  public VisibleStateButtonUpdate textDialogVSBU;
  public VisibleStateButtonUpdate scaleDialogVSBU;
  public VisibleStateButtonUpdate formatDialogVSBU;
  public VisibleStateButtonUpdate shapeDialogVSBU;
  public VisibleStateButtonUpdate colorDialogVSBU;
  public VisibleStateButtonUpdate editColorDialogVSBU;
  public VisibleStateButtonUpdate cropDialogVSBU;
  public VisibleStateButtonUpdate saveDialogVSBU;
  public VisibleStateButtonUpdate regionDialogVSBU;
  public VisibleStateButtonUpdate showMemoryDialogVSBU;
  public VisibleStateButtonUpdate coordinateMapDialogVSBU;
  public VisibleStateButtonUpdate gridDialogVSBU;
  public VisibleStateButtonUpdate filterDialogVSBU;
  public VisibleStateButtonUpdate dataSlicerDialogVSBU;

  public EasyAddAbstractAction infoAction;
  public EasyAddAbstractAction frontAction;
  public EasyAddAbstractAction backAction;


  private CNUViewer cnuv;
  private CNUDisplay cnud;
  public CNUViewerActions(CNUViewer cnuv, CNUDisplay cnud) {
      this.cnuv = cnuv;
      this.cnud = cnud;
      init();
  }

  /**
   * Intializes action variables.
   */
  private void init() {
    fileTypesChoiceModel.addElement(CNUViewer.STANDARD_FILE_TYPES);
    for(int i=0; i<CNUViewer.stdFileClassTypes.length; i++)
      fileTypesChoiceModel.addElement(CNUViewer.stdFileClassTypes[i]);
    fileTypesChoiceModel.addElement(CNUViewer.RAW_FILE_TYPE);
    fileTypesChoiceModel.addElement("iiv.io.ThreeDSSPFile");
    fileTypesCH_originalCount = fileTypesChoiceModel.getSize();

    // build the reusable actions
    breakAction = new LocalAction(
       "Break",
       "Select to stop current display operation",
       BREAK);
    readDisplaysCheckboxAction =
      new LocalAction("Read Displays",
		      "If selected files are displayed immediately on reading",
		      true, NOCMD);
    readAnewCheckboxAction =
      new LocalAction("Read Anew",
        "<html><font size=2>If checked read operations cause images to be read anew<p> even if an already existing copy is in memory</font>",
		      NOCMD);
    browseAction =
      new LocalAction("Browse...",
		      "Invokes a browser to locate and read files",
		      KeyEvent.VK_B,  BROWSE);
    cnuv.addToDisplayWaiters(browseAction, "setEnabled", false);
    displayAction =
      new LocalAction("Display", "Displays the last image read",
		      KeyEvent.VK_D,  DISPLAY);
    cnuv.addToDisplayWaiters(displayAction, "setEnabled", false);
    printAction =
      new LocalAction("Print...", "Prints the display area",
		      KeyEvent.VK_P, PRINT);
    helpAction=new EasyAddAbstractAction(
      "Help", "Select to display help in a browser") {
	private static final long serialVersionUID = 2989312886495609909L;
	  public void actionPerformed(ActionEvent ae) { cnuv.showHelp(); }
	};
    //    helpAction.setEnabled(false);
    quitAction =
      new LocalAction("Quit", "Quits iiV", QUIT);
    overlayAction =
      new LocalAction("Overlay",
	"<html><font size=2>Overlays associated slice(s) of the last image read on<p> top of each selected (or all) currently displayed slice(s)</font>",
		      KeyEvent.VK_O, OVERLAY);
    cnuv.addToDisplayWaiters(overlayAction, "setEnabled", false);
    overlayNgroupAction =
      new LocalAction("Overlay&Group",
	"<html><font size=2>Overlays associated slice(s) of the last image read on each selected<p>(or all) currently displayed slice(s) grouping the associated slices</font>",
		      OVERLAY_N_GROUP);
    cnuv.addToDisplayWaiters(overlayNgroupAction, "setEnabled", false);

    intensityProjectAction =
	new LocalAction("Intensity Project",
			"<html><font size=2>Creates an intensity projection of last data read</font>",
			INTENSITY_PROJECT);
    cnuv.addToDisplayWaiters(intensityProjectAction, "setEnabled", false);

    displayOrthogonalViewsAction =
	new LocalAction("Display Orthogonal Slices",
			"<html><font size=2>Display orthogonal slices of last data read</font>",
			DISPLAY_ORTHOGONAL);
    cnuv.addToDisplayWaiters(displayOrthogonalViewsAction, "setEnabled", false);

    groupAction =
      new LocalAction("Group",
		      "Groups selected images", KeyEvent.VK_G, GROUP);
    groupOverlappingAction =
      new LocalAction("Group Overlapping",
		      "Groups overlapping images", GROUP_OVERLAPPING);
    ungroupAction =
      new LocalAction("Ungroup", "Ungroups selected groups", UNGROUP);
    selectAllAction =
      new LocalAction("Select All",
		      "Selects all currently displayed objects", SELECT_ALL);
    selectTopAction =
      new LocalAction("Select Top",
	"Selects objects currently displayed with no others in front of them",
		      SELECT_TOP_COMPONENTS);
    selectBottomAction =
      new LocalAction("Select Bottom",
	"Selects objects currently displayed with no others behind them",
		      SELECT_BOTTOM_COMPONENTS);
    selectSameDataAction =
      new LocalAction("Select Same Data",
	"Selects objects referencing the same data as the current object",
		      SELECT_SAME_DATA_COMPONENTS);

    copyAction =
      new LocalAction("Copy", "Copies selected components to clipboard", COPY);
    copyScriptAction =
      new LocalAction("Script Only Copy",
	"Copies selected components to clipboard as an iiV script only",
		  COPY_SCRIPT_ONLY);
    copyImageAction =
      new LocalAction("Image Only Copy",
	"Copies selected components to clipboard as an image only",
		  COPY_IMAGE_ONLY);
    copyTextAction =
      new LocalAction("Text Only Copy",
        "Copies selected components to clipboard as text only",
		      COPY_TEXT_ONLY);
    deleteAction =
      new LocalAction("Delete", "Deletes selected displayed objects", DELETE);
    clearAction =
      new LocalAction("Clear", "Clears all displayed objects", CLEAR);
    pasteAction =
      new LocalAction("Paste", "Pastes clipboard contents to display", PASTE);
    pasteScriptAction =
      new LocalAction("Script Only Paste", "Pastes script only from clipboar to display", PASTE_SCRIPT_ONLY);
    pasteImageAction =
      new LocalAction("Image Only Paste", "Pastes image only from clipboard to display", PASTE_IMAGE_ONLY);
    pasteTextAction =
      new LocalAction("Text Only Paste", "Pastes text only from clipboard to display", PASTE_TEXT_ONLY);
    relayoutAction =
	new LocalAction("Relayout",
	  "<html><font size=2>Arranges displayed objects according to<p>the current number of columns setting</font>",
			RELAYOUT);
    refreshAction =
      new LocalAction("Refresh", "Refreshes the display", REFRESH);
    infoAction =
      new LocalAction("Info", "Displays info about currently selected object",
		      INFO);
    frontAction = new LocalAction("Front",
        "Moves selected objects in front others and last in the layout order",
				 FRONT);
    backAction = new LocalAction("Back",
        "Moves selected objects behind others and first in the layout order",
				 BACK);
    applyLabelsAction =
      new LocalAction("Apply Default Labels",
       "Applies the label choices to currently selected or all images",
		      APPLY_LABELS);

    applyFlipsAction =
      new LocalAction("Apply Default Flips",
	"Applies flip choices to currently selected or all images",
		      APPLY_FLIPS);
    toolMenuVSBU =
      new VisibleStateButtonUpdate("Menu Bar", "View Menu Bar",
				   "Check to show menu bar") {
	private static final long serialVersionUID = -339520642531492915L;
	public void updateComponentVisibility() {
	  ButtonModel bm = getCheckboxButtonModel();
	  if(bm != null) cnuv.setMenuBarVisible(bm.isSelected());
	}
      };
    fileTypeDialogVSBU =
      new LocalVisibleStateButtonUpdate("fileTypeDialog",
					"File Types/Raw Settings Dialog",
					"Add File Types/Raw Settings...",
	"<html><font size=2>Invokes a dialog to choose new file types<p>and set raw file type parameters</font>");
    controlDialogVSBU =
      new LocalVisibleStateButtonUpdate("controlDialog", "Main Control Panel",
					"Control Panel...",
					"Select to show the main control panel");
    statusWindowVSBU =
      new LocalVisibleStateButtonUpdate("statusWindow",
					"Status Window", "Status Window...",
					"Select to show the status window");
    showPointDialogVSBU =
      new LocalVisibleStateButtonUpdate("showPointDialog", "Show Point",
					"Show point...",
	"<html><font size=2>Shows a dialog window for showing, tracking,<p>and recording pixel values and locations selected<p>via the mouse with or crosshairs over images</font>");
    gotoPointDialogVSBU =
      new LocalVisibleStateButtonUpdate("gotoPointDialog", "Goto Point",
					"Goto point...",
	"<html><font size=2>Shows a dialog window making data objects<p>display specific locations</font>");
    textDialogVSBU =
      new LocalVisibleStateButtonUpdate("textDialog","Text Dialog",
					"Text...",
        "Shows the dialog window for adding text messges to the display area");
    scaleDialogVSBU =
      new LocalVisibleStateButtonUpdate("scaleDialog",
					"Scale Dialog",
					"Scale...",
       "<html><font size=2>Shows the dialog window for setting the scale<p>that maps raw voxels to color model indices</font>");
    formatDialogVSBU =
      new LocalVisibleStateButtonUpdate("formatDialog",
					"Labels Format Dialog",
					"Labels Format...",
	 "<html><font size=2>Shows a dialog window for controlling formatting<p>of numbers displayed in automatic labels</font>");
    shapeDialogVSBU =
      new LocalVisibleStateButtonUpdate("shapeDialog", "Shape Dialog",
					"Shapes...",
      "Shows the dialog window for adding basic shapes to the display area");
    colorDialogVSBU =
      new LocalVisibleStateButtonUpdate("colorDialog","Color Dialog",
					"Color...",
      "<html><font size=2>Shows the dialog window for selecting color models,<p>adding color bars and setting background color</font>");
    editColorDialogVSBU =
      new LocalVisibleStateButtonUpdate("editColorDialog", "Edit Color Dialog",
					"Edit Color...",
      "Shows the dialog window for editing color maps");
    cropDialogVSBU =
      new LocalVisibleStateButtonUpdate("cropDialog", "Crop/Limits Dialog",
					"Crop/Limits...",
      "<html><font size=2>Shows a dialog window setting and applying cropping and<p>for limitting the number of slices added by a display action</font>");
    saveDialogVSBU =
      new LocalVisibleStateButtonUpdate("saveDialog","Save Dialog",
					"Save...",
		  "Invokes a browser for saving the display area or settings",
					KeyEvent.VK_S);
    regionDialogVSBU =
      new LocalVisibleStateButtonUpdate("regionDialog", "Region Dialog",
					"Select Region...",
	 "Invokes the browser for selecting a region", KeyEvent.VK_R);
    showMemoryDialogVSBU =
      new LocalVisibleStateButtonUpdate("memoryDialog", "Show Memory Dialog",
					"Memory...",
					"Shows this programs memory usage");

    coordinateMapDialogVSBU =
      new LocalVisibleStateButtonUpdate("coordinateMapDialog",
					"Coordinate Map Dialog",
					"Coordinate mapping...",
	 "Shows the dialog for controlling coordinate mapping");
    gridDialogVSBU =
      new LocalVisibleStateButtonUpdate("gridDialog", "Grid Dialog",
					"Grid...",
"<html><font size=2>Show the dialog window for controlling the background<p>grid and snapping objects to the grid</font>");
    filterDialogVSBU =
      new LocalVisibleStateButtonUpdate("filterDialog",
					"Image Filter Dialog",
					"Image Filtering...",
      "Shows a dialog window for controlling image zoom and rotation");

    dataSlicerDialogVSBU =
      new LocalVisibleStateButtonUpdate("dataSlicerDialog",
					"Data Slicer Dialog",
					"Data Slicing...",
      "Shows a dialog window for controlling data slicing");

    orientationLabelsCheckboxAction = new LocalAction(
       "Orientation",
       "Select to display orientation labels on newly displayed slices",
       NOCMD);
    sliceLabelsCheckboxAction = new LocalAction(
       "Slice",
       "Select to display slice labels on newly displayed slices",
       NOCMD);
    iValueLabelsCheckboxAction = new LocalAction(
       "iValue",
       "Select to display iValue labels on newly displayed slices",
       NOCMD);
    flipHCheckboxAction = new LocalAction("Horizontal",
      "Select to flip newly displayed slices horizontally", NOCMD);
    flipVCheckboxAction = new LocalAction("Vertical",
      "Select to flip newly displayed slices vertically", NOCMD);
    flipVCheckboxAction.setCheckboxButtonModel(
      DisplayComponentDefaults.getFlipVStateButtonModel());
    limitIDimCheckboxAction =
      new LocalAction("Limit i dim",
		      DisplayComponentDefaults.limitIDimTipText,
		      LIMIT_IDIM_MODE);
    limitIDimCheckboxAction.setCheckboxButtonModel(
      DisplayComponentDefaults.getIDimLimitButtonModel());
    limitSlicesCheckboxAction =
      new LocalAction("Limit slices",
		      DisplayComponentDefaults.limitSlicesTipText,
		      LIMIT_SLICE_MODE);
    limitSlicesCheckboxAction.setCheckboxButtonModel(
      DisplayComponentDefaults.getSliceLimitButtonModel());
    cropCheckboxAction = new LocalAction("Crop new",
     "<html><font size=2>When checked newly displayed images are cropped by<p> the crop box region set in the Limits/Crop dialog</font>",
							  CROPMODE);
    cropCheckboxAction.setCheckboxButtonModel(
      DisplayComponentDefaults.getCropStateButtonModel());
    zoomCheckboxAction = new LocalAction("Zoom",
      "<html><font size=2>When checked newly displayed images are zoomed by<p> the values set in the image/filtering dialog</font>", NOCMD);
    zoomCheckboxAction.setCheckboxButtonModel(
      DisplayComponentDefaults.getZoomStateButtonModel());
    rotateCheckboxAction = new LocalAction("Rotate",
       "<html><font size=2>When checked newly displayed images are rotated by<p> the value set in the image/filtering dialog</font>",
							 NOCMD);
    rotateCheckboxAction.setCheckboxButtonModel(
      DisplayComponentDefaults.getRotateStateButtonModel());
    selectAdditionsCheckboxAction =
      new LocalAction("Select Adds",
  "Select to have objects automatically selected when added to display area",
		      NOCMD);
    showInsertLocationCheckboxAction =
      new LocalAction("Show Insert Cursor",
		      "Select to show flashing cursor at current display insert location",
		      SHOW_INSERT_LOCATION);
    updateActionStates();
  }

  public static final int NOCMD = 0;
    //  public static final int FILE_TYPE = 1;
  public static final int BROWSE = 2;
  public static final int DISPLAY = 3;
    //  public static final int SAVE = 4;
  public static final int PRINT = 5;
  public static final int OVERLAY = 6;
  public static final int OVERLAY_N_GROUP = 7;
  public static final int GROUP = 8;
  public static final int UNGROUP = 9;
    //  public static final int COLOR = 10;
    //  public static final int SCALE = 11;
    //  public static final int TEXT = 12;
    //  public static final int SHAPES = 13;
    //  public static final int GRID = 14;
  public static final int SELECT_ALL = 15;
  public static final int DELETE = 16;
  public static final int CLEAR = 17;
  public static final int RELAYOUT = 18;
  public static final int REFRESH = 19;
  public static final int UNDO = 20;
  public static final int REDO = 21;
    //  public static final int FORMAT = 22;
    //  public static final int LIMIT = 23;
    //  public static final int FILTER = 24;
    //  public static final int SHOW_POINT = 25;
    //  public static final int MEMORY = 26;
    //  public static final int STATUS = 27;
  public static final int APPLY_FLIPS = 28;
  public static final int APPLY_LABELS = 29;
  public static final int QUIT = 30;
  public static final int PASTE = 31;
  public static final int SELECT_TOP_COMPONENTS = 32;
  public static final int SELECT_BOTTOM_COMPONENTS = 33;
  public static final int GROUP_OVERLAPPING = 34;
  public static final int COPY = 35;
  public static final int COPY_SCRIPT_ONLY = 36;
  public static final int COPY_IMAGE_ONLY = 37;
  public static final int COPY_TEXT_ONLY = 38;
    //  public static final int REGION = 39;
  public static final int CROPMODE = 40;
  public static final int SHOW_INSERT_LOCATION = 41;
  public static final int DISMISS = 42;
  public static final int BREAK = 43;
  public static final int PASTE_SCRIPT_ONLY = 44;
  public static final int PASTE_IMAGE_ONLY = 45;
  public static final int PASTE_TEXT_ONLY = 46;
  public static final int INFO = 47;
  public static final int FRONT = 48;
  public static final int BACK = 49;
  public static final int LIMIT_SLICE_MODE = 50;
  public static final int LIMIT_IDIM_MODE = 51;
  public static final int SELECT_SAME_DATA_COMPONENTS = 52;
  public static final int INTENSITY_PROJECT = 53;
  public static final int DISPLAY_ORTHOGONAL = 54;

  public class LocalAction extends EasyAddAbstractAction {
    private static final long serialVersionUID = -8105833995005365152L;
    private int command;
    public LocalAction(String name, int cmd) {
      super(name);
      command = cmd;
    }
    public LocalAction(String name, String toolTip, int cmd) {
      super(name, toolTip);
      command = cmd;
    }
    public LocalAction(String name, String toolTip, boolean defaultState,
		       int cmd) {
      super(name, toolTip, NO_MNEMONIC, null, defaultState);
      command = cmd;
    }
    public LocalAction(String name, String toolTip,
		       int Mnemonic, int cmd) {
     super(name, toolTip, Mnemonic);
     command = cmd;
    }
    public void setEnabled(boolean t) { super.setEnabled(t); }
    public void actionPerformed(ActionEvent ae) {
      cnuv.setWaitCursor();
      try {
	switch(command) {
	case COPY:
	  cnud.copySelectedToClipboard();
	  break;
	case COPY_SCRIPT_ONLY:
	  cnud.copySelectedToClipboard(CNUDisplay.SCRIPT_ONLY_COPY_MODE);
	  break;
	case COPY_IMAGE_ONLY:
	  cnud.copySelectedToClipboard(CNUDisplay.IMAGE_ONLY_COPY_MODE);
	  break;
	case COPY_TEXT_ONLY:
	  cnud.copySelectedToClipboard(CNUDisplay.TEXT_ONLY_COPY_MODE);
	  break;
	case PASTE:
	  cnud.pasteClipboardToDisplay();
	  break;
	case PASTE_SCRIPT_ONLY:
	  cnud.pasteScript();
	  break;
	case PASTE_IMAGE_ONLY:
	  cnud.pasteImage();
	  break;
	case PASTE_TEXT_ONLY:
	  cnud.pasteText();
	  break;
        case BROWSE:
	  cnuv.browseFiles();
	  break;
	case BREAK:
	  cnuv.stopDisplayThread();
	  break;
        case DISPLAY:
	  cnuv.threadDisplayImageData(cnuv.getViewObject(),CNUViewer.NORMAL_DATA_DISPLAY);
	  break;
        case INTENSITY_PROJECT:
	  cnuv.threadDisplayImageData(cnuv.getViewObject(),CNUViewer.INTENSITY_PROJECT_DATA);
	  break;
        case DISPLAY_ORTHOGONAL:
	  cnuv.threadDisplayImageData(cnuv.getViewObject(),CNUViewer.ORTHOGONAL_DATA_DISPLAY);
	  break;
        case PRINT:
	  cnuv.print();
	  break;
        case OVERLAY:
	  cnuv.threadOverlayImageData(cnuv.getViewObject(), false);
	  break;
        case OVERLAY_N_GROUP:
          cnuv.threadOverlayImageData(cnuv.getViewObject(), true);
	  break;
        case GROUP:
          cnud.groupSelectedComponents();
	  break;
	case GROUP_OVERLAPPING:
	  cnud.groupOverlappingComponents();
	  break;
        case UNGROUP:
	  cnud.unGroup();
	  break;
        case SELECT_ALL:
	  cnud.selectAll();
	  break;
	case SELECT_TOP_COMPONENTS:
	  cnud.selectTopComponents();
	  break;
	case SELECT_BOTTOM_COMPONENTS:
	  cnud.selectBottomComponents();
	  break;
	case SELECT_SAME_DATA_COMPONENTS:
	  cnud.selectSameObjectComponents("getData");
	  break;
        case DELETE:
	  cnud.removeSelections();
	  break;
        case CLEAR:
	  cnud.removeAll();
	  break;
        case RELAYOUT:
	  cnud.relayout();
	  break;
        case REFRESH:
	  cnud.invalidate();
	  cnuv.invalidate(); cnuv.getParent().invalidate();
	  Window w = SwingUtilities.windowForComponent(cnuv);
	  w.invalidate();
	  Window w2 = SwingUtilities.windowForComponent(cnud);
	  if(w != w2) { w2.invalidate(); w2.validate(); }
	  w.validate();
	  break;
        case APPLY_FLIPS:
	  cnud.apply(CNUDisplay.FLIPS);
	  break;
        case APPLY_LABELS:
	  cnud.apply(CNUDisplay.LABELS);
	  break;
	case DISMISS:
	  cnuv.hideToolPanel();
	  break;
	case INFO:
	  cnud.showInfo();
	  break;
	case FRONT:
	  cnud.selectedToFront();
	  break;
	case BACK:
	  cnud.selectedToBack();
	  break;
        case QUIT:
	  cnuv.destroy();
	  break;
        default:
	  break;
        }
      } finally {
        cnuv.setNormalCursor();
      }
    }
    /**
     * Sets the button model and add this as an item listener.
     *
     * @param bm button model to attach to
     */
    public void setCheckboxButtonModel(ButtonModel bm) {
      super.setCheckboxButtonModel(bm);
      if(bm != null) bm.addItemListener(this);
    }
    /**
     * Handles ItemListener events from checkbox items
     *
     * @param e event from checkbox items
     */
    public void itemStateChanged(ItemEvent e){
      switch(command) {
      case SHOW_INSERT_LOCATION:
	cnud.setShowInsertCursor(getCheckboxButtonModel().isSelected());
	break;
      default:
	break;
      }
    }
  }
  /**
   * Updates action enabled states according to currently selected
   * or displayed components.
   * Should only be called from event dispatch thread or at init.
   */
  public void updateActionStates() {
    boolean hasComponents = false;
    boolean hasSelected = false;
    boolean twoOrMoreSelected = false;
    boolean containsGroups = false;
    Component[] components = cnud.getSelectedComponentsOrdered();
    if(components != null) {
      hasSelected = true;
      if(components.length > 1) twoOrMoreSelected = true;
    }
    else components = cnud.getComponents();
    if(components != null) {
      if(components.length > 0) {
	hasComponents = true;
	for(int i = 0; i < components.length; i++) {
	  if(components[i] instanceof ComponentGroup) {
	    containsGroups = true;
	    break;
	  }
	}
      }
    }
    // file menu
    displayAction.setEnabled(cnuv.getViewObject() != null);
    overlayAction.setEnabled(hasComponents);
    overlayNgroupAction.setEnabled(hasComponents);
    intensityProjectAction.setEnabled(cnuv.getViewObject() instanceof iiv.data.CNUData);
    displayOrthogonalViewsAction.setEnabled(cnuv.getViewObject() instanceof iiv.data.CNUData);
    // edit menu
    selectAllAction.setEnabled(hasComponents);
    selectTopAction.setEnabled(hasComponents);
    selectBottomAction.setEnabled(hasComponents);
    selectSameDataAction.setEnabled(hasSelected);
    copyAction.setEnabled(hasComponents);
    copyScriptAction.setEnabled(hasComponents);
    copyImageAction.setEnabled(hasComponents);
    copyTextAction.setEnabled(hasComponents);
    clearAction.setEnabled(hasComponents);
    deleteAction.setEnabled(hasSelected);
    groupAction.setEnabled(twoOrMoreSelected);
    groupOverlappingAction.setEnabled(hasComponents);
    ungroupAction.setEnabled(containsGroups);
    // display area menu
    infoAction.setEnabled(hasSelected);
    frontAction.setEnabled(hasSelected);
    backAction.setEnabled(hasSelected);
  }

  private class LocalVisibleStateButtonUpdate
    extends VisibleStateButtonUpdate {
    private static final long serialVersionUID = -7734659272398667755L;
    private boolean triedGettingNamedDialog = false;
    private String dialogName;
    public LocalVisibleStateButtonUpdate(String dn,
					  String checkboxName,
					  String buttonName,
					  String toolTip) {
      super(null, checkboxName, buttonName, toolTip);
      dialogName = dn;
    }
    public LocalVisibleStateButtonUpdate(String dn,
					  String checkboxName,
					  String buttonName,
					  String toolTip,
					  int Mneomonic ) {
      super(null, checkboxName, buttonName, toolTip, Mneomonic);
      dialogName = dn;
    }
    public Component getComponent() {
      Component component = super.getComponent();
      if((component == null) && (dialogName != null) &&
	 ! triedGettingNamedDialog) {
	component = cnuv.getNamedDialog(dialogName);
	triedGettingNamedDialog = true;
        if(component != null) setComponent(component);
	else updateButtonState();
      }
      return component;
    }
  }
}
