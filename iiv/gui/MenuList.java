package iiv.gui;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
/**
 * MenuList creates and maintains a menu listing items from a ListModel.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.CNUViewer
 * @since	iiV1.15a
 */
public class MenuList extends JMenu implements ListDataListener {
  private static final long serialVersionUID = -770204340671144387L;
  private String title = "List";
  private boolean textLocked = false;
  private boolean checkBox = false;
  private ListModel listModel = null;
  private ActionListener extActionListener = null;
  private ActionListener intActionListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      JMenuItem jmi = (JMenuItem) e.getSource();
      if(! jmi.isSelected()) jmi.setSelected(true);
      if(! jmi.equals(((ComboBoxModel) listModel).getSelectedItem()))
        ((ComboBoxModel) listModel).setSelectedItem(
	((AttachedObject) jmi).getAttachedObject());
    }
  };
  private ButtonGroup buttonGroup = new ButtonGroup();
  public interface AttachedObject {
    public Object getAttachedObject();
  }
  public class JCBMIWithElement extends JCheckBoxMenuItem
    implements AttachedObject {
    private static final long serialVersionUID = 3026478418113900476L;
    public Object attachedObj;
    public JCBMIWithElement(Object item) {
      super(item.toString());
      attachedObj = item;
    }
    public Object getAttachedObject() { return attachedObj; }
  }
  public class JMIWithElement extends JMenuItem
    implements AttachedObject {
    private static final long serialVersionUID = 8430973425394917566L;
    public Object attachedObj;
    public JMIWithElement(Object item) {
      super(item.toString());
      attachedObj = item;
    }
    public Object getAttachedObject() { return attachedObj; }
  }
  /**
   * Set text locked state.
   *
   * @param textLocked <code>true</code> to keep initial menu text locked.
   */
  public void setTextLocked(boolean textLocked) {
    this.textLocked = textLocked;
  }
  /**
   * Constructs a new instance of MenuList.
   * @param s menu name
   * @param lm list to track
   * @param al action listener to be notified by buttons when selected
   * @param cb <code>true</code> to display check box on buttons
   */
  public MenuList(String s, ListModel lm, ActionListener al, boolean cb) {
    super(s);
    title = s;
    listModel = lm;
    extActionListener = al;
    checkBox = cb;
    // tell list model to notify us of changes
    listModel.addListDataListener(this);
    // build initial menu
    int size = listModel.getSize();
    for(int i=0; i<size; i++) {
      JMenuItem jmi = createMenuItem(listModel.getElementAt(i));
      buttonGroup.add(jmi);
      add(jmi);
    }
    updateSelected();
  }
  /**
   * Create the proper menu item.
   *
   * @param 
   * @return the proper menu item
   */
  private JMenuItem createMenuItem(final Object item) {
    JMenuItem jmi;
    if(checkBox) jmi = new JCBMIWithElement(item);
    else jmi = new JMIWithElement(item);
    if(listModel instanceof ComboBoxModel)
      jmi.addActionListener(intActionListener);
    if(extActionListener != null)
      jmi.addActionListener(extActionListener);
    return jmi;
  }
  /**
   * Update the currently selected check box according to the list model.
   */
  public final void updateSelected() {
    if(listModel instanceof ComboBoxModel) {
      Object selectedItem = ((ComboBoxModel) listModel).getSelectedItem();
      if(selectedItem != null) {
        int size = getItemCount();
        for(int i=0; i<size; i++) {
	  JMenuItem jmi = getItem(i);
	  if(selectedItem.equals(((AttachedObject) jmi).getAttachedObject())) {
	    if(! textLocked) setText(title + jmi.getText());
	    ButtonModel bm = jmi.getModel();
	    if(! buttonGroup.isSelected(bm)) buttonGroup.setSelected(bm, true);
	    break;
          }
        }
      }
    }
  }
  /**
   * Called by ListModel when list contents have changed.
   *
   * @param lde	event describing the change
   */
  public void contentsChanged(ListDataEvent lde) {
    int lowerIndex = lde.getIndex0();
    int upperIndex = lde.getIndex1();
    if((lowerIndex < 0) || (lowerIndex > upperIndex)) {
      updateSelected();
      return;
    }
    for(int i=lowerIndex; i<=upperIndex; i++) {
      if(i < getItemCount()) {
	buttonGroup.remove(getItem(i));
	remove(i);
      }
      if(i < listModel.getSize()) {
        JMenuItem jmi = createMenuItem(listModel.getElementAt(i));
        buttonGroup.add(jmi);
        if(i < getItemCount()) insert(jmi, i);
	else add(jmi);
      }
    }
    updateSelected();
  }
  /**
   * Called by ListModel when list contents have been added.
   *
   * @param lde	event describing the adds
   */
  public void intervalAdded(ListDataEvent lde) {
    int lowerIndex = lde.getIndex0();
    int upperIndex = lde.getIndex1();
    if((lowerIndex < 0) || (lowerIndex > upperIndex)) return;
    for(int i=lowerIndex; i<=upperIndex; i++) {
      if(i < listModel.getSize()) {
	JMenuItem jmi = createMenuItem(listModel.getElementAt(i));
        buttonGroup.add(jmi);
        if(i < getItemCount()) insert(jmi, i);
	else add(jmi);
      }
    }
    updateSelected();
  }
  /**
   * Called by ListModel when list contents have been removed.
   *
   * @param lde	event describing the removes
   */
  public void intervalRemoved(ListDataEvent lde) {
    int lowerIndex = lde.getIndex0();
    int upperIndex = lde.getIndex1();
    if((lowerIndex < 0) || (lowerIndex > upperIndex)) return;
    for(int i=upperIndex; i>=lowerIndex; i--) {
      if(i < getItemCount()) {
	buttonGroup.remove(getItem(i));
	remove(i);
      }
    }
    updateSelected();
  }
}
