package iiv.dialog;
import iiv.*;
import iiv.display.*;
import iiv.gui.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.lang.*;

/**
 * ControlDialog is a Dialog window to control iiV settings
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		Dialog
 */
public class ControlDialog extends CNUDialog {
  private static final long serialVersionUID = -4937036354586825841L;
  private boolean initialized = false;
  private JButton dismissB = new JButton("Dismiss");
  /**
   * Constructs a new instance of ControlDialog.
   *
   * @param parentFrame	parent frame needed for building a window
   */
  public ControlDialog(Frame parentFrame) { this(parentFrame, null); }
  /**
   * Constructs a new instance of ControlDialog.
   *
   * @param parentFrame	parent frame
   * @param cnuv	CNUViewer to associate with
   */
  public ControlDialog(Frame parentFrame, CNUViewer cnuv) {
    super(parentFrame,
	  CNUViewer.programTitle + " "  + CNUViewer.version + " Control Panel",
	  false, cnuv);
    if(cnuv != null) init(cnuv.getCNUViewerActions());
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
    init(cnuviewer.getCNUViewerActions());
  }
  private void init(CNUViewerActions cnuviewerActions) {

    if(initialized) return;
    initialized = true;

    Container contentPane = getContentPane();
    contentPane.setLayout(new BorderLayout());

    Box toolP = Box.createVerticalBox();
    contentPane.add(new JScrollPane(toolP), BorderLayout.CENTER);
   

    // add items to tool panel
    JToolBar toolbar;
    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolP.add(toolbar);

    cnuviewerActions.fileTypeDialogVSBU.addTo(toolbar);

    JComboBox fileTypesCH =
      new JComboBox(cnuviewerActions.fileTypesChoiceModel);
    fileTypesCH.setToolTipText(cnuviewerActions.fileTypesTipText);
    toolbar.add(fileTypesCH);
    cnuviewerActions.helpAction.addTo(toolbar);
    toolbar.add(Box.createHorizontalGlue());

    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolP.add(toolbar);
    cnuviewerActions.browseAction.addTo(toolbar);
    // same location
    JButton breakB = cnuviewerActions.breakAction.addTo(toolbar);
    breakB.setBackground(Color.red);
    breakB.setVisible(false);
    getCNUViewer().addToDisplayWaiters(breakB, "setVisible", true);
 
    cnuviewerActions.readDisplaysCheckboxAction.addCheckboxTo(toolbar);
    cnuviewerActions.readAnewCheckboxAction.addCheckboxTo(toolbar);

    toolbar.add(Box.createHorizontalGlue());

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setAlignmentX(JComponent.CENTER_ALIGNMENT);

    JTextField filenameTF =
      new JTextField(cnuviewerActions.filenameDoc, "", 37) {
	private static final long serialVersionUID = -8927848200058287782L;
	public Dimension getMaximumSize() { return this.getPreferredSize(); }
      };
    panel.add(filenameTF);
    filenameTF.addActionListener(cnuviewerActions.filenameActionListener);
    getCNUViewer().addToDisplayWaiters(filenameTF, "setEnabled", false);

    final JList filenameL = new JList(cnuviewerActions.fileNameList);
    filenameL.setSelectionModel(cnuviewerActions.fileNameListSelectionModel);
    // listen for double clicks to read a listed file
    filenameL.addMouseListener(
      new MouseAdapter() {
        public void mouseClicked(MouseEvent event) {
	  if(event.getClickCount() == 2) {
	    int index = filenameL.locationToIndex(event.getPoint());
	    getCNUViewer().setFileFromList(index);
          }
	}
      }
    );
    // keep list 4 long
    filenameL.setVisibleRowCount(4);
    panel.add(new JScrollPane(filenameL));
    getCNUViewer().addToDisplayWaiters(filenameL, "setEnabled", false);

    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolP.add(toolbar);
    toolbar.add(panel);

    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolP.add(toolbar);

    ColorMapCanvas lkup =
      new ColorMapCanvas(DisplayComponentDefaults.getDefaultColorModel());
    getCNUDisplay().addDisplayBackgroundColorComponent(lkup);
    DisplayComponentDefaults.addDefaultColorModelListener(lkup);

    toolbar.add(lkup);
    toolbar.add(Box.createHorizontalGlue());

    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolP.add(toolbar);
    cnuviewerActions.displayAction.addTo(toolbar);

    JComboBox sliceViewCH =
      new JComboBox(DisplayComponentDefaults.getSliceViewChoiceModel());
    sliceViewCH.setToolTipText(DisplayComponentDefaults.sliceViewChoiceTipText);
    toolbar.add(sliceViewCH);
    JComboBox scaleCH =
      new JComboBox(DisplayComponentDefaults.getScaleModeChoiceModel());
    toolbar.add(scaleCH);
    scaleCH.setToolTipText(DisplayComponentDefaults.scaleChoiceTipText);
    toolbar.add(Box.createHorizontalGlue());

    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolP.add(toolbar);
    cnuviewerActions.selectAllAction.addTo(toolbar);
    cnuviewerActions.selectTopAction.addTo(toolbar);
    cnuviewerActions.selectBottomAction.addTo(toolbar);

    cnuviewerActions.selectAdditionsCheckboxAction.addCheckboxTo(toolbar);

    cnuviewerActions.regionDialogVSBU.addTo(toolbar);

    toolbar.add(Box.createHorizontalGlue());

    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolP.add(toolbar);

    cnuviewerActions.copyAction.addTo(toolbar);
    cnuviewerActions.deleteAction.addTo(toolbar);
    cnuviewerActions.clearAction.addTo(toolbar);
    cnuviewerActions.pasteAction.addTo(toolbar);

    cnuviewerActions.showInsertLocationCheckboxAction.addCheckboxTo(toolbar);

    toolbar.add(Box.createHorizontalGlue());

    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolP.add(toolbar);
    cnuviewerActions.overlayAction.addTo(toolbar);
    cnuviewerActions.overlayNgroupAction.addTo(toolbar);
    cnuviewerActions.intensityProjectAction.addTo(toolbar);
    toolbar.add(Box.createHorizontalGlue());


    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolP.add(toolbar);
    cnuviewerActions.groupAction.addTo(toolbar);
    cnuviewerActions.groupOverlappingAction.addTo(toolbar);
    cnuviewerActions.ungroupAction.addTo(toolbar);
    toolbar.add(Box.createHorizontalGlue());

    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolP.add(toolbar);
    cnuviewerActions.colorDialogVSBU.addTo(toolbar);
    cnuviewerActions.scaleDialogVSBU.addTo(toolbar);
    cnuviewerActions.textDialogVSBU.addTo(toolbar);
    cnuviewerActions.shapeDialogVSBU.addTo(toolbar);
    cnuviewerActions.gridDialogVSBU.addTo(toolbar);
    toolbar.add(Box.createHorizontalGlue());

    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolP.add(toolbar);
    cnuviewerActions.cropDialogVSBU.addTo(toolbar);

    cnuviewerActions.limitIDimCheckboxAction.addCheckboxTo(toolbar);
    cnuviewerActions.limitSlicesCheckboxAction.addCheckboxTo(toolbar);

    cnuviewerActions.cropCheckboxAction.addCheckboxTo(toolbar);

    toolbar.add(Box.createHorizontalGlue());

    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolP.add(toolbar);
    cnuviewerActions.relayoutAction.addTo(toolbar);
    TextAndSlider tas =
      new TextAndSlider("Columns:  ", 3,
			getCNUDisplay().getNumberOfColumnsModel());
    tas.setTextInputMaximum(Integer.MAX_VALUE);
    toolbar.add(tas);
    toolbar.add(Box.createHorizontalGlue());

    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolP.add(toolbar);
    cnuviewerActions.formatDialogVSBU.addTo(toolbar);
    cnuviewerActions.orientationLabelsCheckboxAction.addCheckboxTo(toolbar);
    cnuviewerActions.sliceLabelsCheckboxAction.addCheckboxTo(toolbar);

    cnuviewerActions.applyLabelsAction.addTo(toolbar);
    toolbar.add(Box.createHorizontalGlue());

    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolP.add(toolbar);
    toolbar.add(new JLabel("Default Flips:  "));

    cnuviewerActions.flipHCheckboxAction.addCheckboxTo(toolbar);
    cnuviewerActions.flipVCheckboxAction.addCheckboxTo(toolbar);

    cnuviewerActions.applyFlipsAction.addTo(toolbar);
    toolbar.add(Box.createHorizontalGlue());

    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolP.add(toolbar);
    cnuviewerActions.filterDialogVSBU.addTo(toolbar);
    cnuviewerActions.zoomCheckboxAction.addCheckboxTo(toolbar);

    cnuviewerActions.rotateCheckboxAction.addCheckboxTo(toolbar);
    toolbar.add(Box.createHorizontalGlue());

    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolP.add(toolbar);
    getCNUViewer().getUndoRedo().getUndoAction().addTo(toolbar);
    getCNUViewer().getUndoRedo().getRedoAction().addTo(toolbar);
    getCNUViewer().getUndoRedo().getUndoEnableAction().addTo(toolbar);
    getCNUViewer().getUndoRedo().getUndoDisableAction().addTo(toolbar);
    toolbar.add(Box.createHorizontalGlue());

    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolP.add(toolbar);
    cnuviewerActions.showPointDialogVSBU.addTo(toolbar);
    cnuviewerActions.statusWindowVSBU.addTo(toolbar);
    cnuviewerActions.showMemoryDialogVSBU.addTo(toolbar);
    toolbar.add(Box.createHorizontalGlue());

    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolP.add(toolbar);

    cnuviewerActions.toolMenuVSBU.addCheckboxTo(toolbar);
    cnuviewerActions.saveDialogVSBU.addTo(toolbar);
    cnuviewerActions.printAction.addTo(toolbar);
    cnuviewerActions.refreshAction.addTo(toolbar);
    toolbar.add(Box.createHorizontalGlue());

    new EasyAddAbstractAction("Dismiss", "Hides this window") {
      private static final long serialVersionUID = 5985055304156954552L;
      public void actionPerformed(ActionEvent e){ setVisible(false); }
    }.addTo(toolbar);

    cnuviewerActions.quitAction.addTo(toolbar).setBackground(Color.red);

    pack();
  }
}
