package iiv.display;
import iiv.*;
import iiv.filter.*;
import iiv.data.*;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import java.awt.event.*;


/**
 * This is the popup menu for images displayed by iiV.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUDisplay
 * @since	iiV1.55
 */
public class DisplayImagePopupMenu extends JPopupMenu {
  private static final long serialVersionUID = -4280539424822051693L;
  //  private JMenuItem cropMI = null;
  private JMenu zoomM = new JMenu("Zoom");
  private JMenu flipM = new JMenu("Flip");
  private JMenu labelsM = new JMenu("Labels");
  private JMenuItem uncropMI = new JMenuItem("uncrop");
  private JMenuItem clarifyMI = new JMenuItem("clarify");
  private JMenuItem unclarifyMI = new JMenuItem("unclarify");
  //  private JMenuItem scaleMI = null;
  private CNUViewer cnuv;
  private CNUDisplay cnud;
  private CNUViewerActions cnuviewerActions;

  public DisplayImagePopupMenu(CNUViewer cnuv) {
      super("Image");
      this.cnuv = cnuv;
      this.cnud = cnuv.getCNUDisplay();
      this.cnuviewerActions = cnuv.getCNUViewerActions();

      JMenu viewMenu = new JMenu("View");
      this.add(viewMenu);

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
      viewMenu.addSeparator();
      cnuviewerActions.showPointDialogVSBU.addCheckboxTo(viewMenu);
      cnuviewerActions.gotoPointDialogVSBU.addCheckboxTo(viewMenu);
      cnuviewerActions.coordinateMapDialogVSBU.addCheckboxTo(viewMenu);
      cnuviewerActions.showMemoryDialogVSBU.addCheckboxTo(viewMenu);
      cnuviewerActions.statusWindowVSBU.addCheckboxTo(viewMenu);

      JMenuItem MI;
      MI = new JMenuItem("1x"); MI.addActionListener(al); zoomM.add(MI);
      MI = new JMenuItem("2x"); MI.addActionListener(al); zoomM.add(MI);
      MI = new JMenuItem("3x"); MI.addActionListener(al); zoomM.add(MI);
      MI = new JMenuItem("4x"); MI.addActionListener(al); zoomM.add(MI);
      MI = new JMenuItem(".25x"); MI.addActionListener(al); zoomM.add(MI);
      MI = new JMenuItem(".5x"); MI.addActionListener(al); zoomM.add(MI);
      this.add(zoomM);
      MI = new JMenuItem("No Flip"); MI.addActionListener(al); flipM.add(MI);
      MI = new JMenuItem("Flip Vertical"); MI.addActionListener(al);
      flipM.add(MI);
      MI = new JMenuItem("Flip Horizontal"); MI.addActionListener(al);
      flipM.add(MI);
      MI = new JMenuItem("Flip Vertical & Horizontal");
      MI.addActionListener(al);
      flipM.add(MI);
      this.add(flipM);
      MI = new JMenuItem("No Labels"); MI.addActionListener(al);
      labelsM.add(MI);
      MI = new JMenuItem("Slice Labels"); MI.addActionListener(al);
      labelsM.add(MI);
      MI = new JMenuItem("Orientation Labels"); MI.addActionListener(al);
      labelsM.add(MI);
      MI = new JMenuItem("Slice & Orientation Labels");
      MI.addActionListener(al);
      labelsM.add(MI); this.add(labelsM);
      clarifyMI.addActionListener(al); this.add(clarifyMI);
      unclarifyMI.addActionListener(al); this.add(unclarifyMI);
      uncropMI.addActionListener(al); this.add(uncropMI);
      this.addSeparator();
      cnuviewerActions.infoAction.addTo(this);
      cnuviewerActions.frontAction.addTo(this);
      cnuviewerActions.backAction.addTo(this);
      this.addSeparator();
      cnuviewerActions.selectAllAction.addTo(this);
      cnuviewerActions.selectSameDataAction.addTo(this);
      cnuviewerActions.copyAction.addTo(this);
      cnuviewerActions.deleteAction.addTo(this);
      cnuviewerActions.pasteAction.addTo(this);
      this.addSeparator();
      cnuviewerActions.groupAction.addTo(this);
      cnuviewerActions.ungroupAction.addTo(this);
  }
  /**
   * Override JPopupMenu show to enable or disable menu items
   * according to selections before showing.
   *
   * @param invoker component popup is relative to
   * @param x column position over invoker
   * @param y row position over invoker
   */
  public void show(Component invoker, int x, int y) {
    ComponentGroup group = cnud.getCurrentGroup();
    if(group != null) updateStates(group);
    else {
	Component comp = cnud.getCurrentComponent();
	updateStates(comp);
    }
    super.show(invoker, x, y);
  }
  /**
   *  Update the menu item states based on component and display selection states.
   *
   * @param image component to base updates on
   */
  private void updateStates(Component image) {
    // enable or disable items to fit current component
    boolean isComponentGroup = image instanceof ComponentGroup;

    //    groupMI.setEnabled(cnud.getNumberOfSelectedComponents() > 1);
    //    ungroupMI.setEnabled(isComponentGroup);
    uncropMI.setEnabled((image instanceof Croppable) ?
      (((Croppable) image).getCrop() != null) : false);
    zoomM.setEnabled((image instanceof Zoomable) ||
		     (image instanceof FilterSampling));
    flipM.setEnabled((image instanceof Flippable) ||
		     isComponentGroup);
    labelsM.setEnabled((image instanceof SingleImg) ||
		       (image instanceof DisplayColorMap) ||
		       isComponentGroup);
    boolean enableClarify = isComponentGroup;
    boolean enableUnclarify = isComponentGroup;
    boolean nonNull = isComponentGroup;
    if(! isComponentGroup) {
      if(image != null) {
	nonNull =true;
	ColorModel cm = image.getColorModel();
	if(cm instanceof IndexColorModel) {
	  if(cm.equals(CNUColorModel.getTransparentColorModel((IndexColorModel)cm, 0)))
	    enableUnclarify = true;
	  else enableClarify = true;
	}
      }
    }
    clarifyMI.setEnabled(enableClarify);
    unclarifyMI.setEnabled(enableUnclarify);
  }

  private ActionListener al = new ActionListener() {
    /**
     * Handles action events that occur when a menu item is selected.
     *
     * @param evt	action event
     */
     public void actionPerformed(ActionEvent evt) {
       cnuv.setWaitCursor();
       try {
	   String actionCommand = evt.getActionCommand();
	   if("1x".equals(actionCommand))
	       { cnuv.setDefaultZoom(1); cnud.apply(cnud.ZOOM); }
	   else if("2x".equals(actionCommand))
	       { cnuv.setDefaultZoom(2); cnud.apply(cnud.ZOOM); }
	   else if("3x".equals(actionCommand))
	       { cnuv.setDefaultZoom(3); cnud.apply(cnud.ZOOM); }
	   else if("4x".equals(actionCommand))
	       { cnuv.setDefaultZoom(4); cnud.apply(cnud.ZOOM); }
	   else if(".25x".equals(actionCommand))
	       { cnuv.setDefaultZoom((float) .25); cnud.apply(cnud.ZOOM); }
	   else if(".5x".equals(actionCommand))
	       { cnuv.setDefaultZoom((float) .5); cnud.apply(cnud.ZOOM); }
	   else if("No Flip".equals(actionCommand))
	       cnud.updateFlips(false, false);
	   else if("Flip Vertical".equals(actionCommand))
	       cnud.updateFlips(true, false);
	   else if("Flip Horizontal".equals(actionCommand))
	       cnud.updateFlips(false, true);
	   else if("Flip Vertical & Horizontal".equals(actionCommand))
	       cnud.updateFlips(true, true);
	   else if("No Labels".equals(actionCommand))
	       cnud.updateLabels(false, false);
	   else if("Slice Labels".equals(actionCommand))
	       cnud.updateLabels(true, false);
	   else if("Orientation Labels".equals(actionCommand))
	       cnud.updateLabels(false, true);
	   else if("Slice & Orientation Labels".equals(actionCommand))
	       cnud.updateLabels(true, true);
	   else if("clarify".equals(actionCommand))
	       cnud.updateTransparentColor(0);
	   else if("unclarify".equals(actionCommand))
	       cnud.updateTransparentColor(-1);
	   else if("uncrop".equals(actionCommand)) cnud.uncrop();
       } finally {
	   cnuv.setNormalCursor();
       }
     }
 };
}
