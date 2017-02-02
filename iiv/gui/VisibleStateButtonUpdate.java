package iiv.gui;
import java.awt.Component;
import javax.swing.*;
import java.awt.event.*;
/**
 * This class adds a simple tool for building a check box, radio
 * button or standard button to control and track the visibility
 * of a component.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.CNUViewer
 * @see		javax.swing.ButtonModel
 * @since	iiV1.15a
 */
public class VisibleStateButtonUpdate extends EasyAddAbstractAction
  implements ComponentListener, ItemListener {
  private static final long serialVersionUID = 1329451386905306530L;
  private Component component = null;
  /**
   * Constructs a new instance of VisibleStateButtonUpdate.
   * @param checkboxName name checkbox items will show
   * @param buttonName name push button items will show
   * @param toolTip automaticly set as buttons tooltip text when adding
   */
  public VisibleStateButtonUpdate(String checkboxName,
				  String buttonName, String toolTip) {
    this((Component) null, checkboxName, buttonName, toolTip, NO_MNEMONIC);
  }
  /**
   * Constructs a new instance of VisibleStateButtonUpdate.
   * @param comp component whose visibity tracks the button model
   * @param checkboxName name checkbox items will show
   * @param buttonName name push button items will show
   * @param toolTip automaticly set as buttons tooltip text when adding
   */
  public VisibleStateButtonUpdate(Component comp, String checkboxName,
				   String buttonName, String toolTip) {
    this(comp, checkboxName, buttonName, toolTip, NO_MNEMONIC);
  }
  /**
   * Constructs a new instance of VisibleStateButtonUpdate.
   * @param comp component whose visibity tracks the button model
   * @param checkboxName name checkbox items will show
   * @param buttonName name push button items will show
   * @param toolTip automaticly set as buttons tooltip text when adding
   * @param Mnemonic automaticly set as buttons Mnemonic
   */
  public VisibleStateButtonUpdate(Component comp, String checkboxName,
				   String buttonName, String toolTip,
				   int Mnemonic) {
    super(buttonName, toolTip, Mnemonic, checkboxName);
    if(comp != null) {
      component = comp;
      comp.addComponentListener(this);
    }
  }
  /**
   * Overrides setCheckboxButtonModel to add this as an item listener.
   *
   * @param bm button model to attach to
   */
  public void setCheckboxButtonModel(ButtonModel bm) {
    if(bm != null) {
      super.setCheckboxButtonModel(bm);
      bm = getCheckboxButtonModel();
      if(bm != null) bm.addItemListener(this);
    }
  }
  /**
   * Sets the component and add this as a component listener.
   *
   * @param comp component to attach to
   */
  public void setComponent(Component comp) {
    if((comp != null) && (comp != component)) {
      component = comp;
      comp.addComponentListener(this);
      updateButtonState();
    }
  }
  /**
   * Gets the component.
   */
  public Component getComponent() { return component; }
  /**
   * Handles ActionListener events.
   *
   * @param ae	action event
   */
  public void actionPerformed(ActionEvent ae) {
    Component comp = getComponent();
    if(comp != null) {
      comp.setVisible(true);
      updateButtonState();
    }
  }
  /**
   * Called when a component action occurs that may require updating
   * the button state.
   */
  public void updateButtonState() {
    if(getNumberOfButtonModels() < 1) return;

    boolean enabled = false;
    Component comp = getComponent();
    boolean visible = false;
    if(comp != null) {
      visible = comp.isVisible();
      enabled = true;
    }
    ButtonModel bm = getCheckboxButtonModel();
    if((bm != null) && (bm.isSelected() != visible)) bm.setSelected(visible);
    if(isEnabled() != enabled) setEnabled(enabled);
  }
  /**
   * Called when a button item occurs that may require updating
   * the component visibility.
   */
  public void updateComponentVisibility() {
    ButtonModel bm = getCheckboxButtonModel();
    if(bm != null) {
      // keep the button state because getting the component
      // may reset it
      boolean b = bm.isSelected();
      Component comp = getComponent();
      if(comp != null) {
	// in case getting the component reset state
	if(bm.isSelected() != b) bm.setSelected(b);
	if(comp.isVisible() != b) comp.setVisible(b);
      }
    }
  }
  /**
   * Implements component action routine to call updateUpdateButtonState
   * when the component is moved because component shown isn't often
   * called but resize and moved often seem to get called when a
   * component is shown.
   *
   * @param e component event
   */
  public void componentMoved(ComponentEvent e) { updateButtonState(); }
  /**
   * Implements component action routine to call updateButtonState
   * when the component is resized because component shown isn't often
   * called but resize and moved often seem to get called when a
   * component is shown.
   *
   * @param e component event
   */
  public void componentResized(ComponentEvent e) { updateButtonState(); }
  /**
   * Implements component action routine to call updateButtonState
   * when the component is shown.
   *
   * @param e component event
   */
  public void componentShown(ComponentEvent e) { updateButtonState(); }
  /**
   * Implements component action routine to call updateButtonState
   * when the component is hidden.
   *
   * @param e component event
   */
  public void componentHidden(ComponentEvent e) { updateButtonState(); }
  /**
   * Handles item listener events from the button model by calling
   * updateComponentVisibilty.
   *
   * @param e	item event
   */
  public void itemStateChanged(ItemEvent e){ updateComponentVisibility(); }
}
