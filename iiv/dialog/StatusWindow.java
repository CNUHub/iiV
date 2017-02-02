package iiv.dialog;
import iiv.*;
import iiv.data.*;
import iiv.util.*;
import iiv.io.*;
import iiv.script.*;
import iiv.gui.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.*;
import java.awt.datatransfer.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.undo.*;
import javax.swing.event.*;
import bsh.*;
import bsh.util.*;

/**
 * Dialog Window to show status messages, error messages and allow user
 * script command inputs.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUViewer
 * @since	iiV1.0
 */
public class StatusWindow extends CNUDialog
//  implements KeyListener
{
  private static final long serialVersionUID = 398575580740496543L;
  private iiVBshJConsole jConsole = new iiVBshJConsole();
  private JTextComponent statusTextComponent = null;

  private UndoManager undoManager = new UndoManager();
  private JMenu editMenu = new JMenu("Edit");
  private LocalAction clearAction = new LocalAction("Clear",
    "Clears all status text", CLEAR);
  private LocalAction saveAction =
    new LocalAction("Save...",
		    "Invokes a browser for saving the text displayed in this window",
		    KeyEvent.VK_S, SAVE);

  private Action copyAction =
      new javax.swing.text.DefaultEditorKit.CopyAction();
  private Action cutAction =
      new javax.swing.text.DefaultEditorKit.CutAction();
  private Action pasteAction =
      new javax.swing.text.DefaultEditorKit.PasteAction();
  private LocalAction undoAction = new LocalAction("Undo",
    "Undoes the last change to the text area", UNDO);
  private LocalAction redoAction = new LocalAction("Redo",
    "Redoes the last undo", REDO);

  private LocalAction enableEditAction = new LocalAction("Enable Input",
    "Enables editting and interpreting lines as script commands",
     ENABLE_EDIT);
  private LocalAction disableEditAction = new LocalAction("Disable Input(clears undos)",
    "Disables editting and interpreting lines as script commands",
     DISABLE_EDIT);
  private JCheckBoxMenuItem autoShowCBMI = new JCheckBoxMenuItem("Auto Show", true);
  private LocalAction dismissAction = new LocalAction("Dismiss",
    "Hides this window", DISMISS);
  private PipedWriter pipedWriter = null;
  private PipedReader pipedReader = null;
  private Thread pipeThread = null;
  private Object pwLock = new Object();

  private PrintStream statusPrintStream = null;
  private OutputStream statusOutputStream = null;
  private ByteArrayOutputStream byteArrayStream = null;
  private Object streamLock = new Object();

  public static final int DISMISS=1;
  public static final int CLEAR=2;
  public static final int SAVE=3;
  public static final int ENABLE_EDIT=4;
  public static final int DISABLE_EDIT=5;
  public static final int COPY=6;
  public static final int CUT=7;
  public static final int PASTE=8;
  public static final int UNDO=9;
  public static final int REDO=10;
  public class LocalAction extends EasyAddAbstractAction {
    private static final long serialVersionUID = -3267293700375209904L;
    private int command;
    public LocalAction(String name, int cmd) {
      super(name);
      command = cmd;
    }
    public LocalAction(String name, String toolTip, int cmd) {
      super(name, toolTip);
      command = cmd;
    }
    public LocalAction(String name, String toolTip,
		       int Mnemonic, int cmd) {
     super(name, toolTip, Mnemonic);
     command = cmd;
    }
    public void actionPerformed(ActionEvent ae) {
      switch(command) {
      case DISMISS:
	setVisible(false);
	break;
      case CLEAR:
	clearText();
	break;
      case SAVE:
	saveText();
	break;
      case UNDO:
	undoLastChange();
	break;
      case REDO:
	redoLastUndo();
	break;
      case ENABLE_EDIT:
	enableEdit();
	break;
      case DISABLE_EDIT:
	disableEdit();
	clearUndoHistory();
	break;
      default:
	break;
      }
    }
  }
  /**
   * Constructs a new instance of StatusWindow.
   *
   * @param parent	parent frame needed for building a window
   * @param title	title to put a top of the status window
   * @param runningAsApplet	if <code>true</code> save option not presented
   * @param cnuv	CNUViewer that this window interacts with
   */
  public StatusWindow(Frame parent, String title, boolean runningAsApplet,
	       CNUViewer cnuv) {
    super(parent, title, false, cnuv);

    //  statusTextComponent = new JTextArea(10, 80);
    //    statusTextComponent = new JTextPane();
    statusTextComponent = (JTextComponent) jConsole.getViewport().getView();

    // edits come up disabled
    statusTextComponent.setEnabled(false);
    enableEditAction.setEnabled(true);
    disableEditAction.setEnabled(false);
    cutAction.setEnabled(false);
    pasteAction.setEnabled(false);
    undoAction.setEnabled(false);
    redoAction.setEnabled(false);

    statusTextComponent.getDocument().addUndoableEditListener(
      new UndoableEditListener() {
	/**
	 * Messaged when the Document has created an edit, the edit is
	 * added to <code>undo</code>, an instance of UndoManager.
	 */
        public void undoableEditHappened(UndoableEditEvent e) {
	    undoManager.addEdit(e.getEdit());
	    updateUndoAction();
	    updateRedoAction();
	}
      }
    );
    //    statusTextComponent.setLineWrap(true);
    //    statusTextComponent.setWrapStyleWord(false);

    JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);
    JMenu fileMenu = new JMenu("File");
    fileMenu.setToolTipText("Pull this menu down to save or dismiss.");
    fileMenu.setMnemonic(KeyEvent.VK_F);
    menuBar.add(fileMenu);

    saveAction.addTo(fileMenu).setAccelerator(
      KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
    fileMenu.add(autoShowCBMI);
    autoShowCBMI.setToolTipText("When checked this window automatically appears when text is added");
    dismissAction.addTo(fileMenu);

    editMenu.setToolTipText("Pull this menu down for edit commands.");
    editMenu.setMnemonic(KeyEvent.VK_E);
    menuBar.add(editMenu);

    copyAction.putValue(Action.NAME, "Copy");
    copyAction.putValue(Action.LONG_DESCRIPTION,
			"Copies selected text to clipboard");
    copyAction.putValue(Action.SHORT_DESCRIPTION,
			"Copies selected text to clipboard");
    JMenuItem jmi = editMenu.add(copyAction);
    jmi.setMnemonic(KeyEvent.VK_C);
    jmi.setAccelerator(
      KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
    jmi.setToolTipText("Copies selected text to clipboard");

    pasteAction.putValue(Action.NAME, "Paste");
    pasteAction.putValue(Action.LONG_DESCRIPTION,
			 "Pastes text from clipboard");
    pasteAction.putValue(Action.SHORT_DESCRIPTION,
			 "Pastes text from clipboard");
    jmi = editMenu.add(pasteAction);
    jmi.setMnemonic(KeyEvent.VK_P);
    jmi.setAccelerator(
      KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
    jmi.setToolTipText("Pastes text from clipboard");

    cutAction.putValue(Action.NAME, "Cut");
    cutAction.putValue(Action.LONG_DESCRIPTION,
		       "Cuts selected text to clipboard");
    cutAction.putValue(Action.SHORT_DESCRIPTION,
		       "Cuts selected text to clipboard");
    jmi = editMenu.add(cutAction);
    jmi.setMnemonic(KeyEvent.VK_T);
    jmi.setAccelerator(
      KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
    jmi.setToolTipText("Cuts selected text to clipboard");

    clearAction.addTo(editMenu);

    editMenu.addSeparator();

    undoAction.addTo(editMenu);
    redoAction.addTo(editMenu);

    editMenu.addSeparator();

    enableEditAction.addTo(editMenu);
    disableEditAction.addTo(editMenu);

    Container contentPane = getContentPane();
    contentPane.setLayout(new BorderLayout(5, 5));

    jConsole.setPreferredSize(new Dimension(400,200));
    contentPane.add(jConsole, BorderLayout.CENTER);

    if(runningAsApplet) { saveAction.setEnabled(false); }
    pack();
  }
  /**
   * Appends a message to the text area.
   *
   * @param s message to append.
   */
  public void appendMessage(final String s) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { appendMessage(s); }
      } );
    }
    else {
      // show before appending or scrollbars may not update
      if(getAutoShow() && ! isVisible()) setVisible(true);
      jConsole.println(s);
    }
  }
  /**
   * Appends a message to the text area.
   *
   * @param s message to append.
   */
  public void appendError(final String s) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { appendError(s); }
      } );
    }
    else {
      // show before appending or scrollbars may not update
      if(getAutoShow() && ! isVisible()) setVisible(true);
      jConsole.error(s);
    }
  }
  /**
   * Appends a throwable trace to the text area.
   *
   * @param t	Throwable object to append trace from
   */
  public void appendTrace(Throwable t) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    t.printStackTrace(new PrintWriter(baos, true));
    appendError(t.toString() + "\n" + baos.toString());
  }
  /**
   * Shows error messages(same as standard message except red) in this window.
   *
   * @param s String to print in window
   */
  public void showError(String s) { appendError(s); }
  /**
   * Shows status messages(actually any string) in this window.
   *
   * @param s String to print in window
   */
  public void showStatus(String s) { appendMessage(s); }
  /**
   * Shows the stack trace for throwable objects in this window.
   *
   * @param t	throwable object to print trace for
   */
  public void showStatus(Throwable t) { appendTrace(t); }
  /**
   * Sets the auto show state.
   * @param state <code>true</code> if the window should automaticly
   * show itself when written to, <code>false</code> otherwise.
   */
  public void setAutoShow(boolean state) { autoShowCBMI.setState(state); }
  /**
   * Gets the auto show state.
   *
   * @return <code>true</code> if the window is set to automaticly
   * show itself when written to, <code>false</code> otherwise.
   */
  public boolean getAutoShow() { return autoShowCBMI.getState(); }
  /**
   * Clears all text from this window.
   */
  public void clearText() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { clearText(); }
      } );
    }
    else statusTextComponent.setText("");
  }
  /**
   * Gets the text displayed in this window.
   *
   * @return text displayed.
   */
  public String getText() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	public void run() {
	  returnObject = statusTextComponent.getText();
	}
      };
      runWithReturn.invokeAndWait();
      return (String) runWithReturn.returnObject;
    }
    else return statusTextComponent.getText();
  }
  /**
   * Saves the displayed status text to a file selected through a file dialog.
   */
  public void saveText() {
    FileDialog fd = getCNUViewer().getSaveFileDialog(null);
    fd.setVisible(true);
    if(fd.getFile() != null) {
      saveText( fd.getDirectory() + fd.getFile() );
    }
  }
  /**
   * Saves the displayed status text to a given file.
   *
   * @param filename	file to save text to
   */
  public void saveText(String filename) {
    if(filename == null) return;
    filename = filename.trim();
    if(filename.equals("")) return;
    PrintWriter pw = null;
    try {
      CNUFile cnufile = new CNUFile(filename);
      if( cnufile.exists() ) {
        ContinueDialog CD = getCNUViewer().getContinueDialog();
        CD.beep(); CD.setVisible(true);
        if(! CD.getContinueFlag() ) {
	  showStatus("Did not write file " + filename);
	  return;
        }
      }
      pw = cnufile.getPrintWriter();
      pw.print( getText() );
    } catch (IOException e1) {
      showStatus(e1);
    } finally {
      if(pw != null) pw.close();
    }
  }
  /**
   * Undoes the last text change.
   */
  public void undoLastChange() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { undoLastChange(); }
      } );
    }
    else {
	try {
	    undoManager.undo();
	} catch (CannotUndoException ex) {
	    showStatus("Unable to undo: " + ex);
	    showStatus(ex);
	}
	updateUndoAction();
	updateRedoAction();
    }
  }
  /**
   * Redoes the last undo.
   */
  public void redoLastUndo() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { redoLastUndo(); }
      } );
    }
    else {
	try {
	    undoManager.redo();
	} catch (CannotRedoException ex) {
	    showStatus("Unable to redo: " + ex);
	    showStatus(ex);
	}
	updateRedoAction();
	updateUndoAction();
    }
  }
  /**
   * Updates undo action to current undo state.
   */
  private void updateUndoAction() {
      if(undoManager.canUndo() && statusTextComponent.isEditable()) {
	  undoAction.setEnabled(true);
	  undoAction.putValue(Action.NAME,
			      undoManager.getUndoPresentationName());
      }
      else {
	  undoAction.setEnabled(false);
	  undoAction.putValue(Action.NAME, "Undo");
      }
  }
  /**
   * Updates redo action to current undo state.
   */
  private void updateRedoAction() {
      if(undoManager.canRedo() && statusTextComponent.isEditable()) {
	  redoAction.setEnabled(true);
	  redoAction.putValue(Action.NAME,
			      undoManager.getRedoPresentationName());
      }
      else {
	  redoAction.setEnabled(false);
	  redoAction.putValue(Action.NAME, "Redo");
      }
  }
  /**
   * Clears the undo/redo history.
   */
  public void clearUndoHistory() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { clearUndoHistory(); }
      } );
    }
    else {
	undoManager.discardAllEdits();
	updateUndoAction();
	updateRedoAction();
    }
  }
  /**
   * Enables editting via user inputs over the status window.
   */
  public void enableEdit() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { enableEdit(); }
      } );
    }
    else {
       enableEditAction.setEnabled(false);
       disableEditAction.setEnabled(true);
       cutAction.setEnabled(true);
       pasteAction.setEnabled(true);
       //       statusTextComponent.addKeyListener(this);
       statusTextComponent.setEnabled(true);
       statusTextComponent.requestFocus();
       updateUndoAction();
       updateRedoAction();
       createCheckInterpreter();
    }
  }
  /**
   * Disables editting via user inputs over the status window.
   */
  public void disableEdit() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { disableEdit(); }
      } );
    }
    else {
       enableEditAction.setEnabled(true);
       disableEditAction.setEnabled(false);
       cutAction.setEnabled(false);
       pasteAction.setEnabled(false);
       statusTextComponent.setEnabled(false);
       //       statusTextComponent.removeKeyListener(this);
       updateUndoAction();
       updateRedoAction();
    }
  }
  /**
    * Handles key pressed events.  This seems to be the most
    * consistent accross platform.  Always seems to be generated
    * and always before the character is inserted into the text area.
    * On some machines the command echo still occurs before the
    * carriage return.
    *
    * @param e	key event
   public void keyPressed(KeyEvent e){
     if(e.getKeyCode() == KeyEvent.VK_ENTER) {
       String command = getCurrentLineCommand();
       if(command != null) sendCommand(command);
     }
   }
    */
  /**
    * Handles key released events.  Key releases are ignored
    * because of inconsistencies on wether generated or not
    * and wether the character is inserted post or prior to this
    * event.
    *
    * @param e	key event
  public void keyReleased(KeyEvent e){} // ignored
    */
  /**
    * Handles key typed events.  Key types are ignored because
    * of inconsistencies accross platforms of where the
    * caret occurs relative to the new character.
    *
    * @param e	key event
  public void keyTyped(KeyEvent e){} // ignored
    */
  /**
    * Gets the command line based on the current caret position.
    *
    * @return 	the command or <code>null</code>;
  public String getCurrentLineCommand() {
    String text = getText();
    int caret = statusTextComponent.getCaretPosition();

    if(caret < 1) return null; // ignore enters before first character
    if(caret > text.length()) caret = text.length();
    // locate previous enter or beginning of string
    int previousEnter = text.lastIndexOf('\n', caret - 1);
    if( previousEnter < 0) previousEnter = -1;  // beginning of string
    // locate next enter or end of string
    int nextEnter = text.indexOf('\n', caret - 1);
    if(nextEnter < 0) nextEnter = text.length(); // end of string
    if(nextEnter <= previousEnter) return "";
    return text.substring(previousEnter+1, nextEnter).trim();
  }
    */
  /**
   * Creates and checks if interpreter is running.
   *
   */
  private void createCheckInterpreter() {
    synchronized (pwLock) {
      if(pipeThread != null) if(! pipeThread.isAlive()) pipeThread = null;
      if(pipeThread == null) {
	pipeThread = new Thread( new Runnable() {
	    public void run() {
	      try {
		iiVBshScript.readComponentsFromScript(jConsole, getCNUDisplay(),
						      getCNUViewer());
	      } catch (IOException ioe) {
		appendTrace(ioe);
	      }
	    }
	  });
	pipeThread.start();
      }
    }
  }
}
