package iiv.gui;
import java.awt.Container;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
/**
 * This abstract class adds some simple tools to AbstractAction for
 * automatic creation of JComponents with tool tips and Mnemonics.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.CNUViewer
 * @see		javax.swing.AbstractAction
 * @since	iiV1.15a
 */
public abstract class EasyAddAbstractAction extends AbstractAction
    implements ItemListener
{
  public static int NO_MNEMONIC = KeyEvent.CHAR_UNDEFINED;

  private Vector<ButtonModel> buttonModels = new Vector<ButtonModel>();
  private ButtonModel checkboxButtonModel = null;
  private String name = null;
  private String checkboxName = null;
  private boolean defaultCheckboxState = false;

  protected String toolTipText = null;
  protected boolean MnemonicSet = false;
  protected int Mnemonic = NO_MNEMONIC;

  /**
   * Creates a new instance of EasyAddAbstractAction with given button
   * name.
   *
   * @param name button name
   */
  public EasyAddAbstractAction(String name) { this(name, null); }
  /**
   * Creates a new instance of EasyAddAbstractAction with given button
   * name and tooltip text.
   *
   * @param name button name
   * @param toolTip automaticly set as buttons tooltip text when adding
   */
  public EasyAddAbstractAction(String name, String toolTip) {
    this(name, toolTip, NO_MNEMONIC, null);
  }
  /**
   * Creates a new instance of EasyAddAbstractAction with given button
   * name, tooltip text, and Mnemonic.
   *
   * @param name button name
   * @param toolTip automaticly set as buttons tooltip text when adding
   * @param Mnemonic automaticly set as buttons Mnemonic
   */
  public EasyAddAbstractAction(String name, String toolTip,
			       int Mnemonic) {
    this(name, toolTip, Mnemonic, null);
  }
  /**
   * Creates a new instance of EasyAddAbstractAction with given button
   * name, tooltip text, Mnemonic, and checkbox name.
   *
   * @param name button name
   * @param toolTip automaticly set as buttons tooltip text when adding
   * @param Mnemonic automaticly set as buttons Mnemonic
   * @param checkboxName button name (allows check box to have different name)
   */
  public EasyAddAbstractAction(String name, String toolTip,
			       int Mnemonic, String checkboxName) {
    this(name, toolTip, Mnemonic, checkboxName, false);
  }
  /**
   * Creates a new instance of EasyAddAbstractAction with given button
   * name, tooltip text, Mnemonic, checkbox name and default checkbox state.
   *
   * @param name button name
   * @param toolTip automaticly set as buttons tooltip text when adding
   * @param Mnemonic automaticly set as buttons Mnemonic
   * @param checkboxName button name (allows check box to have different name)
   * @param defaultCheckboxState default state for self created checkbox models
   */
  public EasyAddAbstractAction(String name, String toolTip,
			       int Mnemonic, String checkboxName,
			       boolean defaultCheckboxState) {
    super(name);
    this.name = name;
    toolTipText = toolTip;
    setMnemonic(Mnemonic);
    if(checkboxName == null) this.checkboxName = name;
    else this.checkboxName = checkboxName;
    this.defaultCheckboxState = defaultCheckboxState;
  }
  /**
   * Sets a Mnemonic to be set for any newly created buttons.
   *
   * @param Mnemonic automaticly set as buttons Mnemonic
   */
  public void setMnemonic(int Mnemonic) {
    if(this.Mnemonic != Mnemonic) {
      MnemonicSet = true;
      this.Mnemonic = Mnemonic;
    }
  } 
  /**
   * Adds button models to list of models to be enabled/disabled
   * if this action is enabled/disabled
   *
   * @param bm button model to attach to
   */
  public void addButtonModel(ButtonModel bm) {
    if(bm != null) synchronized (buttonModels) {
      if(! buttonModels.contains(bm)) buttonModels.addElement(bm);
    }
  }
  /**
   * Gets the number of button models.
   *
   * @return number of button models
   */
  public int getNumberOfButtonModels() {
    synchronized(buttonModels) { return buttonModels.size(); }
  }
  /**
   * Gets the button models.
   *
   * @return button model this is attach to
   */
  public ButtonModel[] getButtonModels() {
    synchronized(buttonModels) {
      ButtonModel[] models = new ButtonModel[buttonModels.size()];
      buttonModels.copyInto(models);
      return models;
    }
  }
  /**
   * Enables or disables this action.  Overridden to enable or disable all buttonModels.
   *
   * @param newState <code>true</code> to enable, <code>false</code> to disable
   */
  public void setEnabled(boolean newState) {
    super.setEnabled(newState);
    if(checkboxButtonModel != null) {
      if(checkboxButtonModel.isEnabled() != newState)
	checkboxButtonModel.setEnabled(newState);
    }
    ButtonModel[] models = getButtonModels();
    // enable or disable all button models
    for(int i=0; i<models.length; i++) {
      if(models[i].isEnabled() != newState) models[i].setEnabled(newState);
    }
  }
  /**
   * Sets the button model and add this as an item listener.
   *
   * @param bm button model to attach to
   */
  public void setCheckboxButtonModel(ButtonModel bm) {
    if(bm != null) synchronized (buttonModels) {
      checkboxButtonModel = bm;
      addButtonModel(bm);
    }
  }
  /**
   * Gets the checkbox button model.
   *
   * @return button model this is attach to
   */
  public ButtonModel getCheckboxButtonModel() {
    if(checkboxButtonModel == null) synchronized (buttonModels) {
      if(checkboxButtonModel == null)
	checkboxButtonModel =
	  (new JCheckBox(checkboxName, defaultCheckboxState)).getModel();
    }
    return checkboxButtonModel;
  }
  /**
   * Handles ItemListener events from checkbox items
   *
   * @param e	item event
   */
  public void itemStateChanged(ItemEvent e){}
  /**
   * Adds this abstract button as a JMenuItem to a JMenu and sets
   * its the tool tip and Mnemonic if defined.
   *
   * @param jMenu JMenu to add to
   * @return JMenuItem that was created and added to the JMenu
   */
  public JMenuItem addTo(JMenu jMenu) {
    JMenuItem jmi = jMenu.add(this);
    addButtonModel(jmi.getModel());
    if(toolTipText != null) jmi.setToolTipText(toolTipText);
    if(MnemonicSet) jmi.setMnemonic(Mnemonic);
    jmi.setEnabled(isEnabled());
    return jmi;
  }
  /**
   * Adds this abstract button as a JMenuItem to a JPopupMenu and sets
   * its the tool tip and Mnemonic if defined.
   *
   * @param jMenu JPopupMenu to add to
   * @return JMenuItem that was created and added to the JMenu
   */
  public JMenuItem addTo(JPopupMenu jMenu) {
    JMenuItem jmi = jMenu.add(this);
    addButtonModel(jmi.getModel());
    if(toolTipText != null) jmi.setToolTipText(toolTipText);
    if(MnemonicSet) jmi.setMnemonic(Mnemonic);
    jmi.setEnabled(isEnabled());
    return jmi;
  }
  /**
   * Adds this abstract button as a JButton to a JToolBar and sets
   * its tool tip if defined.
   *
   * @param jToolBar JToolBar to add to
   * @return JButton that was created and added to the JToolBar
   */
  public JButton addTo(JToolBar jToolBar) {
    JButton jb = jToolBar.add(this);
    addButtonModel(jb.getModel());
    if(toolTipText != null) jb.setToolTipText(toolTipText);
    //if(MnemonicSet) jb.setMnemonic(Mnemonic);
    jb.setEnabled(isEnabled());
    return jb;
  }
  /**
   * Adds this abstract button as a JButton to a JMenuBar and sets
   * its tool tip if defined.
   *
   * @param jMenuBar JMenuBar to add to
   * @return JButton that was created and added to the JToolBar
   */
  public JButton addTo(JMenuBar jMenuBar) {
    JButton jb = new JButton(name);
    addButtonModel(jb.getModel());
    jb.setBorder(null);
    jb.setBorderPainted(false);
    if(toolTipText != null) jb.setToolTipText(toolTipText);
    if(MnemonicSet) jb.setMnemonic(Mnemonic);
    jMenuBar.add(jb);
    jb.setEnabled(isEnabled());
    return jb;
  }
  /**
   * Adds this abstract button as a JButton to a Container and sets
   * its tool tip if defined.
   *
   * @param container Container to add to
   * @return JButton that was created and added to the JToolBar
   */
  public JButton addTo(Container container) {
    JButton jb = new JButton(name);
    addButtonModel(jb.getModel());
    if(toolTipText != null) jb.setToolTipText(toolTipText);
    if(MnemonicSet) jb.setMnemonic(Mnemonic);
    container.add(jb);
    jb.setEnabled(isEnabled());
    return jb;
  }
  /**
   * Adds this abstract button as a JMenuItem to a JMenu and sets
   * its the tool tip and Mnemonic if defined.
   *
   * @param jMenu JMenu to add to
   * @return JMenuItem that was created and added to the JMenu
   */
  public JCheckBoxMenuItem addCheckboxTo(JMenu jMenu) {
    JCheckBoxMenuItem jcbmi =
      new JCheckBoxMenuItem(checkboxName, defaultCheckboxState);
    synchronized(buttonModels) {
      // don't use the getCheckboxButtonModel() because it would build one
      ButtonModel bm = checkboxButtonModel;
      if(bm != null) jcbmi.setModel(bm);
      else setCheckboxButtonModel(jcbmi.getModel());
    }
    jMenu.add(jcbmi);
    if(toolTipText != null) jcbmi.setToolTipText(toolTipText);
    if(MnemonicSet) jcbmi.setMnemonic(Mnemonic);
    jcbmi.setEnabled(isEnabled());
    return jcbmi;
  }
  /**
   * Adds this abstract button as a JButton to a container and sets
   * its tool tip if defined.
   *
   * @param container container to add to
   * @return JButton that was created and added to the JToolBar
   */
  public JCheckBox addCheckboxTo(Container container) {
    JCheckBox jcb = new JCheckBox(checkboxName, defaultCheckboxState);
    container.add(jcb);
    synchronized(buttonModels) {
      // don't use the getCheckboxButtonModel() because it would build one
      ButtonModel bm = checkboxButtonModel;
      if(bm != null) jcb.setModel(bm);
      else setCheckboxButtonModel(jcb.getModel());
    }
    if(toolTipText != null) jcb.setToolTipText(toolTipText);
    if(MnemonicSet) jcb.setMnemonic(Mnemonic);
    jcb.setEnabled(isEnabled());
    return jcb;
  }
}
