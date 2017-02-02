package iiv.gui;
import javax.swing.text.*;
import javax.swing.event.*;

/**
 * Class to note when document changes occur on a text field
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.CNUViewer
 * @since	iiV1.1
 */
public class JTextComponentChangeListener implements DocumentListener {
    private boolean changed = true;
    private Document doc = null;
    public JTextComponentChangeListener(JTextComponent tc) {
      doc = tc.getDocument();
    }
    /**
     * Called on any type of text changes.
     */
    public void setChangedState(boolean changed) {
      synchronized(this) {
	if(changed == this.changed) ; // do nothing
	else {
	  this.changed = changed;
	  if(changed) doc.removeDocumentListener(this);
	  else doc.addDocumentListener(this);
	}
      }
    }
    public boolean getChangedState() { return changed; }
    public void insertUpdate(DocumentEvent e) { setChangedState(true); }
    public void removeUpdate(DocumentEvent e) { setChangedState(true); }
    public void changedUpdate(DocumentEvent e) { setChangedState(true); }
  }
