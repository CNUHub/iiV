package iiv;
import iiv.display.*;
import iiv.gui.*;
import iiv.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

/**
 * CNUViewerquMenuBar is the standard menu bar for the top of CNUViewer.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUViewer
 * @since	iiV1.155
 */
public class CNUViewerMenuBar extends JMenuBar {
  private static final long serialVersionUID = -1191717265714576514L;
  // menu to track visibility of dialogs
  private JMenu viewMenu = new JMenu("View");

  /**
   * Constructs a new instance of CNUViewerMenuBar.
   *
   * @param cnuv CNUViewer this menu bar is for.
   */
  public CNUViewerMenuBar(final CNUViewer cnuv) {
    CNUViewerActions cnuviewerActions = cnuv.getCNUViewerActions();
    JMenu fileMenu = new JMenu("File");
    fileMenu.setToolTipText(
      "Pull this menu down to read, display, and write files.");
    fileMenu.setMnemonic(KeyEvent.VK_F);
    add(fileMenu);

    JMenu toolsMenu = new JMenu("Tools");
    toolsMenu.setToolTipText("Pull this menu down to access tools.");
    toolsMenu.setMnemonic(KeyEvent.VK_T);
    add(toolsMenu);

    JMenu editMenu = new JMenu("Edit");
    editMenu.setToolTipText("Pull this menu down to access edit commands.");
    editMenu.setMnemonic(KeyEvent.VK_E);
    add(editMenu);

    viewMenu.setToolTipText("Pull this menu down to select visible windows.");
    viewMenu.setMnemonic(KeyEvent.VK_V);
    add(viewMenu);

    JMenu breakMenu = new JMenu("Break");
    breakMenu.setToolTipText("Pull this menu down to access break option.");
    breakMenu.setForeground(Color.red);
    breakMenu.setVisible(false);
    add(breakMenu);
    cnuv.addToDisplayWaiters(breakMenu, "setVisible", true);

    // put items in file menu
    cnuviewerActions.browseAction.addTo(fileMenu).setAccelerator(
      KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.ALT_MASK));

    cnuviewerActions.readDisplaysCheckboxAction.addCheckboxTo(fileMenu);
    cnuviewerActions.readDisplaysCheckboxAction.getCheckboxButtonModel().setSelected(true);

    cnuviewerActions.readAnewCheckboxAction.addCheckboxTo(fileMenu);

    cnuviewerActions.displayAction.addTo(fileMenu);

    ActionListener al =  new ActionListener() {
      public void actionPerformed(ActionEvent event) {
	String cmd=event.getActionCommand();
	try {
	  FileListElement fle = FileListElement.valueOf(cmd);
	  Object[] classTypes = new Object[1];
	  classTypes[0] = fle.getFileClass();
	  cnuv.threadSetFile(fle.getFileName(), classTypes);
	} catch (IllegalArgumentException iae) {
	   cnuv.showStatus("failed parsing file string=" + cmd);
           cnuv.showStatus(iae);
           Toolkit.getDefaultToolkit().beep();
	}
      }
    };
    MenuList menuFileList =
      new MenuList("files", cnuviewerActions.fileNameList, al, true);
    menuFileList.setTextLocked(true);

    fileMenu.add(menuFileList);

    cnuviewerActions.fileTypeDialogVSBU.addTo(fileMenu);

    MenuList fileTypeML = new MenuList("Read File Type: ",
				       cnuviewerActions.fileTypesChoiceModel,
				       null, false);
    fileTypeML.setToolTipText(cnuviewerActions.fileTypesTipText);
    fileMenu.add(fileTypeML);

    MenuList viewMenuList =
      new MenuList("View mode:  ", DisplayComponentDefaults.getSliceViewChoiceModel(), null, false);
    viewMenuList.setToolTipText(DisplayComponentDefaults.sliceViewChoiceTipText);
    fileMenu.add(viewMenuList);
    MenuList scaleML =
      new MenuList("Scale mode:  ",
		   DisplayComponentDefaults.getScaleModeChoiceModel(),
		   null, false);
    scaleML.setToolTipText(DisplayComponentDefaults.scaleChoiceTipText);
    fileMenu.add(scaleML);

    fileMenu.addSeparator();
    cnuviewerActions.overlayAction.addTo(fileMenu);
    cnuviewerActions.overlayNgroupAction.addTo(fileMenu);
    cnuviewerActions.intensityProjectAction.addTo(fileMenu);
    cnuviewerActions.displayOrthogonalViewsAction.addTo(fileMenu);
    fileMenu.addSeparator();
    cnuviewerActions.saveDialogVSBU.addTo(fileMenu);
    fileMenu.addSeparator();
    cnuviewerActions.printAction.addTo(fileMenu);
    fileMenu.addSeparator();
    cnuviewerActions.helpAction.addTo(fileMenu);
    fileMenu.addSeparator();
    cnuviewerActions.quitAction.addTo(fileMenu);

    // put items in tools menu
    cnuviewerActions.colorDialogVSBU.addTo(toolsMenu);
    cnuviewerActions.scaleDialogVSBU.addTo(toolsMenu);
    cnuviewerActions.textDialogVSBU.addTo(toolsMenu);
    cnuviewerActions.shapeDialogVSBU.addTo(toolsMenu);
    cnuviewerActions.gridDialogVSBU.addTo(toolsMenu);
    toolsMenu.addSeparator();
    cnuviewerActions.formatDialogVSBU.addTo(toolsMenu);

    toolsMenu.add(new JLabel("Default Labels:"));
    cnuviewerActions.orientationLabelsCheckboxAction.addCheckboxTo(toolsMenu);
    cnuviewerActions.sliceLabelsCheckboxAction.addCheckboxTo(toolsMenu);

    cnuviewerActions.applyLabelsAction.addTo(toolsMenu);
    toolsMenu.addSeparator();

    toolsMenu.add(new JLabel("Default Flips:"));

    cnuviewerActions.flipHCheckboxAction.setCheckboxButtonModel(
      DisplayComponentDefaults.getFlipHStateButtonModel());

    cnuviewerActions.flipHCheckboxAction.addCheckboxTo(toolsMenu);

    cnuviewerActions.flipVCheckboxAction.addCheckboxTo(toolsMenu);

    cnuviewerActions.applyFlipsAction.addTo(toolsMenu);
    toolsMenu.addSeparator();
    cnuviewerActions.cropDialogVSBU.addTo(toolsMenu);

    cnuviewerActions.limitIDimCheckboxAction.addCheckboxTo(toolsMenu);
    cnuviewerActions.limitSlicesCheckboxAction.addCheckboxTo(toolsMenu);

    cnuviewerActions.cropCheckboxAction.addCheckboxTo(toolsMenu);

    toolsMenu.addSeparator();

    cnuviewerActions.filterDialogVSBU.addTo(toolsMenu);

    cnuviewerActions.zoomCheckboxAction.addCheckboxTo(toolsMenu);

    cnuviewerActions.rotateCheckboxAction.addCheckboxTo(toolsMenu);

    toolsMenu.addSeparator();
    cnuviewerActions.showPointDialogVSBU.addTo(toolsMenu);
    cnuviewerActions.gotoPointDialogVSBU.addTo(toolsMenu);
    cnuviewerActions.showMemoryDialogVSBU .addTo(toolsMenu);
    cnuviewerActions.statusWindowVSBU.addTo(toolsMenu);

    // put items in edit menu
    cnuviewerActions.selectAllAction.addTo(editMenu);
    cnuviewerActions.selectTopAction.addTo(editMenu);
    cnuviewerActions.selectBottomAction.addTo(editMenu);
    cnuviewerActions.selectSameDataAction.addTo(editMenu);

    cnuviewerActions.selectAdditionsCheckboxAction.addCheckboxTo(editMenu);

    cnuviewerActions.regionDialogVSBU.addTo(editMenu);

    editMenu.addSeparator();

    cnuviewerActions.copyAction.addTo(editMenu);

    JMenu copymenu = new JMenu("Copy Special");
    editMenu.add(copymenu);

    cnuviewerActions.copyScriptAction.addTo(copymenu);
    cnuviewerActions.copyImageAction.addTo(copymenu);
    cnuviewerActions.copyTextAction.addTo(copymenu);


    cnuviewerActions.deleteAction.addTo(editMenu);
    cnuviewerActions.clearAction.addTo(editMenu);
    cnuviewerActions.pasteAction.addTo(editMenu);

    JMenu pastemenu = new JMenu("Paste Special");
    editMenu.add(pastemenu);

    cnuviewerActions.pasteScriptAction.addTo(pastemenu);
    cnuviewerActions.pasteImageAction.addTo(pastemenu);
    cnuviewerActions.pasteTextAction.addTo(pastemenu);

    cnuviewerActions.showInsertLocationCheckboxAction.addCheckboxTo(editMenu);

    editMenu.addSeparator();

    cnuviewerActions.groupAction.addTo(editMenu);
    cnuviewerActions.groupOverlappingAction.addTo(editMenu);
    cnuviewerActions.ungroupAction.addTo(editMenu);

    editMenu.addSeparator();

    cnuviewerActions.relayoutAction.addTo(editMenu);
    TextAndSlider tasMenu =
      new TextAndSlider("Columns:  ", 3,
			cnuv.getCNUDisplay().getNumberOfColumnsModel());
    tasMenu.setTextInputMaximum(Integer.MAX_VALUE);
    editMenu.add(tasMenu);

    cnuviewerActions.refreshAction.addTo(editMenu);

    editMenu.addSeparator();

    cnuv.getUndoRedo().getUndoAction().addTo(editMenu);
    cnuv.getUndoRedo().getRedoAction().addTo(editMenu);
    cnuv.getUndoRedo().getUndoEnableAction().addTo(editMenu);
    cnuv.getUndoRedo().getUndoDisableAction().addTo(editMenu);

    // put items in view menu

    cnuviewerActions.toolMenuVSBU.addCheckboxTo(viewMenu);
    cnuviewerActions.controlDialogVSBU.addCheckboxTo(viewMenu);
    viewMenu.addSeparator();
    cnuviewerActions.fileTypeDialogVSBU.addCheckboxTo(viewMenu);
    viewMenu.addSeparator();
    cnuviewerActions.saveDialogVSBU.addCheckboxTo(viewMenu);
    cnuviewerActions.regionDialogVSBU.addCheckboxTo(viewMenu);
    viewMenu.addSeparator();
    cnuviewerActions.colorDialogVSBU.addCheckboxTo(viewMenu);
    cnuviewerActions.editColorDialogVSBU.addCheckboxTo(viewMenu);
    cnuviewerActions.scaleDialogVSBU.addCheckboxTo(viewMenu);
    cnuviewerActions.textDialogVSBU.addCheckboxTo(viewMenu);
    cnuviewerActions.shapeDialogVSBU.addCheckboxTo(viewMenu);
    cnuviewerActions.gridDialogVSBU.addCheckboxTo(viewMenu);
    viewMenu.addSeparator();
    cnuviewerActions.formatDialogVSBU.addCheckboxTo(viewMenu);
    cnuviewerActions.cropDialogVSBU.addCheckboxTo(viewMenu);
    cnuviewerActions.filterDialogVSBU.addCheckboxTo(viewMenu);
    cnuviewerActions.dataSlicerDialogVSBU.addCheckboxTo(viewMenu);
    viewMenu.addSeparator();
    cnuviewerActions.showPointDialogVSBU.addCheckboxTo(viewMenu);
    cnuviewerActions.gotoPointDialogVSBU.addCheckboxTo(viewMenu);
    cnuviewerActions.coordinateMapDialogVSBU.addCheckboxTo(viewMenu);
    cnuviewerActions.showMemoryDialogVSBU.addCheckboxTo(viewMenu);
    cnuviewerActions.statusWindowVSBU.addCheckboxTo(viewMenu);
    viewMenu.addSeparator();

    // put items in break menu

    JMenuItem breakMenuB = cnuviewerActions.breakAction.addTo(breakMenu);
    breakMenuB.setForeground(Color.red);
    breakMenuB.setVisible(false);
    breakMenuB.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.ALT_MASK));
    cnuv.addToDisplayWaiters(breakMenuB, "setVisible", true);
  }
  /**
   * Add a VisibleStateButtonUpdate to the view menu.
   *
   * @param vsbu	The VisibleStateButtonUpdate to add.
   * @see VisibleStateButtonUpdate
   */
  public void addToViewMenu(final VisibleStateButtonUpdate vsbu) {
    if(SwingUtilities.isEventDispatchThread()) vsbu.addCheckboxTo(viewMenu);
    else {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { vsbu.addCheckboxTo(viewMenu); }
      } );
    }
  }
  /**
   * Add a component to the view menu.
   *
   * @param comp	The component to add.
   */
  public void addToViewMenu(final Component comp) {
    if(SwingUtilities.isEventDispatchThread()) viewMenu.add(comp);
    else {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { viewMenu.add(comp); }
      } );
    }
  }
}
