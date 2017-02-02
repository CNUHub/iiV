package iiv.util;
import iiv.io.CNUFileObject;
import iiv.gui.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.lang.*;
import java.lang.reflect.*;
import java.util.Vector;
import java.util.Enumeration;

/**
 * UndoRedo is a class to track and perform undo/redo tasks.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		DoCommand
 * @see		UndoButtons
 * @see		ShowStatus
 */
public class UndoRedo implements ShowStatus, CNUFileObject { 
  private Object doListLock = new Object();
  private boolean enabled = true;
  private int redoCnt = 0;
  private DoLink undoList = null;
  private DoLink redoList = null;
  private int stepLevel = 0;
  private DoLink stepList = null;
  private UndoButtons undoButtons = null;
  private ShowStatus ss = null;
  private boolean onlyUndoRedoFromEventThread = false;
  public static final int UNDO = 1;
  public static final int REDO = 2;
  public static final int ENABLE_UNDOREDO = 3;
  public static final int DISABLE_UNDOREDO = 4;
  private class LocalAction extends EasyAddAbstractAction {
      private static final long serialVersionUID = 745064722466502307L;
      private int command;
      public LocalAction(String name, String toolTip, int cmd) {
	  super(name, toolTip);
	  command = cmd;
      }
      public void setCommand(int cmd) { command = cmd; }
      public void actionPerformed(ActionEvent ae) {
	  //setWaitCursor();
	  try {
	      switch(command) {
	      case UNDO:
		  undo();
		  break;
	      case REDO:
		  redo();
		  break;
	      case ENABLE_UNDOREDO:
		  UndoRedo.this.setEnabled(true);
		  break;
	      case DISABLE_UNDOREDO:
		  UndoRedo.this.setEnabled(false);
		  break;
	      default:
		  break;
	      }
	  } finally {
	      //  setNormalCursor();
	  }
      }
  }
  private LocalAction undoAction = new LocalAction("Undo",
						   "Click to undo last change",
						   UNDO);
  private LocalAction redoAction = new LocalAction("Redo",
						   "Click to redo last undo",
						   REDO);
  private LocalAction undoEnableAction = new LocalAction("Enable undo",
						   "Click to enable undo/redo",
						   ENABLE_UNDOREDO);
  private LocalAction undoDisableAction = new LocalAction("Disable undo(Clears history)",
						   "Click to disable and clear undo/redo history",
						   DISABLE_UNDOREDO);
  /**
   * Construct a new instance of UndoRedo.
   *
   * @param ss	object to send status messages too.  If <code>null</code>,
   *		messages sent to System.out.
   */
  public UndoRedo( ShowStatus ss ) {
    this.ss = ss;
    updateUndoButtons();
  }
  /**
   * Construct a new instance of UndoRedo.
   *
   * @param undoButtons	object to notify when a change occurs in the
   *			undo/redo list.  May be <code>null</code>.
   * @param ss	object to send status messages too.  If <code>null</code>,
   *		messages sent to System.out.
   */
  public UndoRedo( UndoButtons undoButtons, ShowStatus ss ) {
    this.undoButtons = undoButtons;
    this.ss = ss;
    updateUndoButtons();
  }
  /**
   * Construct a new instance of UndoRedo.
   *
   * @param ss	object to send status messages too.  If <code>null</code>,
   *		messages sent to System.out.
   * @param oetb <code>true</code> to force undo/redo to be performed
   *		 only from the event processing thread.
   */
  public UndoRedo( ShowStatus ss, boolean oetb ) {
    this.ss = ss;
    updateUndoButtons();
    onlyUndoRedoFromEventThread = oetb;
  }
  /**
   * Construct a new instance of UndoRedo.
   *
   * @param undoButtons	object to notify when a change occurs in the
   *			undo/redo list.  May be <code>null</code>.
   * @param ss	object to send status messages too.  If <code>null</code>,
   *		messages sent to System.out.
   * @param oetb <code>true</code> to force undo/redo to be performed
   *		 only from the event processing thread.
   */
  public UndoRedo( UndoButtons undoButtons, ShowStatus ss, boolean oetb ) {
    this.undoButtons = undoButtons;
    this.ss = ss;
    updateUndoButtons();
    onlyUndoRedoFromEventThread = oetb;
  }
  /**
   * Gets the event thread forced flag.
   *
   * @return <code>true</code> if undo/redo commands always forced
   *		   to occur in the event processing thread.
   */
   public boolean getEventThreadForced() {
     return onlyUndoRedoFromEventThread;
   }
  /**
   * Undo or Redo a single command.
   *
   * @param docmd	primary command to invoke
   * @param postCommand	command to invoke after primary command.
   *			May be <code>null</code>.
   * @param repaintDc	Component to repaint bounds of from before
   *			and after invoking commands.
   *			May be <code>null</code>.
   * @return		<code>true</code> if successful
   */
  public boolean Do(DoCommand docmd, DoCommand postCommand,
		    Component repaintDc) {
    boolean success = false;
    try {
      Rectangle repaintArea = null;
      if(repaintDc != null) repaintArea = repaintDc.getBounds();
      docmd.invoke();
      if(postCommand != null) postCommand.invoke();
      if(repaintArea != null) {
        Component parent = repaintDc.getParent();
	if(parent != null) {
	  repaintArea.add(repaintDc.getBounds());
	  repaintArea.grow(1, 1);
	  parent.repaint(repaintArea.x, repaintArea.y, repaintArea.width, repaintArea.height);
        }
      }
      success = true;
    } catch (InvocationTargetException ite) {
      showStatus("Error trying to invoke a undo or redo command");
      showStatus(docmd.toString());
      showStatus(ite);
    } catch (IllegalAccessException  iae) {
      showStatus("Error trying to invoke a undo or redo command");
      showStatus(docmd.toString());
      showStatus(iae);
    } catch (NoSuchMethodException nme) {
      showStatus("Error trying to invoke a undo or redo command");
      showStatus(docmd.toString());
      showStatus(nme);
    } finally {
    }
    return success;
  }
  /**
   * Gets the undo/redo list synchronization lock.
   * To allow undo/redos to occur in multithreaded
   * enviroment all tasks that add to the undo/redo list should synchronize
   * this lock before invoking commands that will require additions to undo/redo.
   * Synchronization order is important because executing an undo first
   * synchronizes this lock then any sync locks the command requires.  If
   * a command performs its own syncs first then tries to add an undo which
   * requires this lock a grid lock can occur.
   *
   * @return	the synchronized locking object
   */
  public Object getDoListLock() { return doListLock; }
  /**
   * Adds a command to the undo list.
   *
   * @param undoCommand		undo command to add.
   * @param inverseCommand	command to redo undo command.
   */
  public void addUndo(DoCommand undoCommand, DoCommand inverseCommand) {
    if(enabled) addUndo(new DoLink(undoCommand, inverseCommand));
  }
  /**
   * Adds a command to the undo list.
   *
   * @param undoCommand		undo command to add.
   * @param inverseCommand	command to redo undo command.
   * @param presentationName	name for presenting on undo/redo buttons
   */
  public void addUndo(DoCommand undoCommand, DoCommand inverseCommand,
		      String presentationName) {
    if(enabled) addUndo(new DoLink(undoCommand, inverseCommand,
				   presentationName));
  }
  /**
   * Adds a command to the undo list.
   *
   * @param undoCommand		undo command to add.
   * @param inverseCommand	command to redo undo command.
   * @param repaintDc		Component to repaint after performing undo or redo
   */
  public void addUndo(DoCommand undoCommand, DoCommand inverseCommand,
		      Component repaintDc) {
    if(enabled) addUndo(new DoLink(undoCommand, inverseCommand, repaintDc));
  }
  /**
   * Adds a command to the undo list.
   *
   * @param undoCommand		undo command to add.
   * @param inverseCommand	command to redo undo command.
   * @param repaintDc		Component to repaint after performing undo or redo
   * @param presentationName	name for presenting on undo/redo buttons
   */
  public void addUndo(DoCommand undoCommand, DoCommand inverseCommand,
		      Component repaintDc, String presentationName) {
    if(enabled) addUndo(new DoLink(undoCommand, inverseCommand,
				   repaintDc, presentationName));
  }
  /**
   * Adds a command to the undo list.
   *
   * @param undoCommand		undo command to add.
   * @param inverseCommand	command to redo undo command.
   * @param postCommand		command to perform after either the undo or
   *				or redo command.
   * @param repaintDc		Component to repaint after performing undo or redo
   */
  public void addUndo(DoCommand undoCommand, DoCommand inverseCommand,
		      DoCommand postCommand, Component repaintDc) {
    if(enabled) addUndo(new DoLink(undoCommand, inverseCommand,
			postCommand, repaintDc));
  }
  /**
   * Adds a command to the undo list.
   *
   * @param undoCommand		undo command to add.
   * @param inverseCommand	command to redo undo command.
   * @param postCommand		command to perform after either the undo or
   *				or redo command.
   * @param repaintDc		Component to repaint after performing undo or redo
   * @param presentationName	name for presenting on undo/redo buttons
   */
  public void addUndo(DoCommand undoCommand, DoCommand inverseCommand,
		      DoCommand postCommand, Component repaintDc,
		      String presentationName) {
    if(enabled) addUndo(new DoLink(undoCommand, inverseCommand,
			postCommand, repaintDc, presentationName));
  }
  /**
   * Adds a DoLink to the undo list or the step list if startSteps
   * was called and has not been closed with a call to finishUndoSteps.
   *
   * @param undoLink	link to add
   * @see #startSteps()
   * @see #finishUndoSteps()
   */
  public void addUndo(final DoLink undoLink) {
    if( onlyUndoRedoFromEventThread &&
       (! SwingUtilities.isEventDispatchThread()) ) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { addUndo(undoLink); }
      } );
    }
    else synchronized (doListLock) {
      if(! enabled) return;
//      System.out.println("in UndoRedo("+this+").addUndo("+undoLink+") stepLevel="+stepLevel);
      if( stepLevel == 0 ) {
        if(redoCnt <= 0) redoList = null;  // redo list no longer valid
        else redoCnt--;
        undoLink.next = undoList;
        undoList = undoLink;
      } else {
        undoLink.next = stepList;
        stepList = undoLink;
      }
      updateUndoButtons();
    }
  }
  /**
   * Adds a command to the redo list.
   *
   * @param redoCommand		redo command to add.
   * @param inverseCommand	command to undo the redo command.
   */
  public void addRedo(DoCommand redoCommand, DoCommand inverseCommand) {
    if(enabled) addRedo(new DoLink(redoCommand, inverseCommand));
  }
  /**
   * Adds a command to the redo list.
   *
   * @param redoCommand		redo command to add.
   * @param inverseCommand	command to undo the redo command.
   * @param repaintDc		Component to repaint after performing undo or redo
   */
  public void addRedo(DoCommand redoCommand, DoCommand inverseCommand,
		      Component repaintDc) {
    if(enabled) addRedo(new DoLink(redoCommand, inverseCommand, repaintDc));
  }
  /**
   * Adds a DoLink to the redo list or the step list if startSteps
   * was called and has not been closed with a call to finishRedoSteps.
   *
   * @param redoLink	link to add
   * @see #startSteps()
   * @see #finishRedoSteps()
   */
  public void addRedo(final DoLink redoLink) {
    if( onlyUndoRedoFromEventThread &&
       (! SwingUtilities.isEventDispatchThread()) ) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { addRedo(redoLink); }
      } );
    }
    else synchronized (doListLock) {
      if(! enabled) return;
      if( stepLevel == 0 ) {
        redoLink.next = redoList;
        redoList = redoLink;
      } else {
        redoLink.next = stepList;
        stepList = redoLink;
      }
      updateUndoButtons();
    }
  }
  /**
   * Initializes the building of a single undo or redo command as sequence
   * of multiple undo or redo steps.  After calling this routine all calls
   * to add undos or redos will get added to a step list instead of the
   * regular lists.
   * This steplist is maintained until either finishUndoSteps or
   * finishRedoSteps is called.
   * Multiple calls increase the number of times the finish routines must
   * be called before the step list is transfered to the regular undo or
   * redo list.
   * The sequence of startSteps and finishUndoSteps (finishRedoSteps)
   * should be preformed synchronized with getUndoLock() to ensure
   * another thread does not insert unrelated undo or redo steps.
   * Or, with the enforce event processing thread only flag, steps
   * should be performed in the event processing thread.
   *
   * @see #addUndo(DoCommand undoCommand, DoCommand inverseCommand)
   * @see #addRedo(DoCommand redoCommand, DoCommand inverseCommand)
   * @see #getDoListLock()
   */
  public void startSteps() {
    if( onlyUndoRedoFromEventThread &&
       (! SwingUtilities.isEventDispatchThread()) ) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { startSteps(); }
      } );
    }
    else synchronized (doListLock) {
      // multiple calls to startSteps without calling finish will cause
      // the steps to be flattened into one main step
      stepLevel++;
    }
  }
  /**
   * Finishes building undo steps by transfering the step list to the undoList
   * if only at one level of steps.  The level is a function of how many times
   * startSteps has been called without calling finishUndoSteps or
   * finishRedoSteps.
   */
  public void finishUndoSteps() {
      finishUndoSteps("steps");
  }
  /**
   * Finishes building undo steps by transfering the step list to the undoList
   * if only at one level of steps.  The level is a function of how many times
   * startSteps has been called without calling finishUndoSteps or
   * finishRedoSteps.
   */
  public void finishUndoSteps(final String commandName) {
    if( onlyUndoRedoFromEventThread &&
       (! SwingUtilities.isEventDispatchThread()) ) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { finishUndoSteps(commandName); }
      } );
    }
    else synchronized (doListLock) {
      // only finish if at level 1
      stepLevel--;
      if(stepLevel < 0) stepLevel = 0;
      if((stepLevel == 0) && (stepList != null)) {
	if(enabled) addUndo(new DoLink(stepList, commandName)); 
        stepList = null;
      }
      updateUndoButtons();
    }
  }
  /**
   * Finishes building redo steps by transfering the step list to the redoList
   * if only at one level of steps.  The level is a function of how many times
   * startSteps has been called without calling finishUndoSteps or
   * finishRedoSteps.
   */
    public void finishRedoSteps() { finishRedoSteps("steps"); }
  /**
   * Finishes building redo steps by transfering the step list to the redoList
   * if only at one level of steps.  The level is a function of how many times
   * startSteps has been called without calling finishUndoSteps or
   * finishRedoSteps.
   */
  public void finishRedoSteps(String commandName) {
    if( onlyUndoRedoFromEventThread &&
       (! SwingUtilities.isEventDispatchThread()) ) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { finishRedoSteps(); }
      } );
    }
    else synchronized (doListLock) {
      // only finish if at level 1
      stepLevel--;
      if(stepLevel < 0) stepLevel = 0;
      if((stepLevel == 0) && (stepList != null)) {
	if(enabled) addRedo(new DoLink(stepList, commandName)); 
        stepList = null;
      }
      updateUndoButtons();
    }
  }
  /**
   * Undoes one step from the undo list.
   */
  public void undo() {
    if( onlyUndoRedoFromEventThread &&
       (! SwingUtilities.isEventDispatchThread()) ) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { undo(); }
      } );
    }
    else synchronized (doListLock) {
      if(undoList == null) return;
      DoLink currentUndo = undoList; // make a copy of the current undo
      undoList = undoList.next;      // and remove it from the list
      if(currentUndo.command instanceof DoLink)
	undo((DoLink) currentUndo.command,
	     currentUndo.commandPresentationName);
      else if(currentUndo.command instanceof DoCommand) {
	Do((DoCommand) currentUndo.command,
	   (DoCommand) currentUndo.postCommand, currentUndo.repaintDc);
	if(currentUndo.inverse != null) {
	  // inverse should only be non-null if undoing the
	  // command doesn't automaticly add a redo
	  currentUndo.swap();
	  addRedo(currentUndo);
        }
      }
      else showStatus("invalid undo object = " + undoList.command);
      updateUndoButtons();
    }
  }
  /**
   * Undoes a link list of steps.
   * Should only be called from already synchronized code.
   */
  private void undo(DoLink partialSteps, String commandName) {
    startSteps();
    while( partialSteps != null) {
      DoLink currentUndo = partialSteps;
      partialSteps = partialSteps.next;
      if(currentUndo.command instanceof DoCommand) {
	Do((DoCommand) currentUndo.command,
	   (DoCommand) currentUndo.postCommand, currentUndo.repaintDc);
	if(currentUndo.inverse != null) {
	  // inverse should only be non-null if undoing the
	  // command doesn't automaticly add a redo
	  currentUndo.swap();
	  addRedo(currentUndo);
	}
      }
      else showStatus("invalid undo object = " + currentUndo.command);
    }
    finishRedoSteps(commandName);
  }
  /**
   * Redoes one step from the redo list.
   */
  public void redo() {
    if( onlyUndoRedoFromEventThread &&
       (! SwingUtilities.isEventDispatchThread()) ) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { redo(); }
      } );
    }
    else synchronized (doListLock) {
      if(redoList == null) return;
      DoLink currentRedo = redoList; // make a copy of the current redo
      redoList = redoList.next;   // and remove it from the list
      if(currentRedo.command instanceof DoLink)
	redo((DoLink) currentRedo.command,
	     currentRedo.commandPresentationName);
      else if(currentRedo.command instanceof DoCommand) {
        redoCnt++;  // add an undo without deleting redos
        Do((DoCommand) currentRedo.command,
	   (DoCommand) currentRedo.postCommand, currentRedo.repaintDc);
	if(currentRedo.inverse != null) {
	  // inverse should only be non-null if redoing the
	  // command doesn't automaticly add an undo
	  currentRedo.swap();
	  addUndo(currentRedo);
	}
      }
      else showStatus("invalid redo object = " + redoList.command);
      updateUndoButtons();
    }
  }
  /**
   * Redoes a link list of steps.
   */
  private void redo(DoLink partialSteps, String commandName) {
    synchronized (doListLock) {
      startSteps();
      while( partialSteps != null) {
	DoLink currentRedo = partialSteps;
	partialSteps = partialSteps.next;
	if(currentRedo.command instanceof DoCommand) {
	  Do((DoCommand) currentRedo.command,
	     (DoCommand) currentRedo.postCommand, currentRedo.repaintDc);
	  if(currentRedo.inverse != null) {
	    // inverse should only be non-null if undoing the
	    // command doesn't automaticly add a redo
	    currentRedo.swap();
	    addUndo(currentRedo);
	  }
        }
        else showStatus("invalid undo object = " + currentRedo.command);
      }
      redoCnt++; // add an undo without deleting redos
      // finish will updateUndoButtons
      finishUndoSteps(commandName);
    }
  }
  /**
   * Gets the undo action associated and updated by this UndoRedo.
   *
   * @return action associated and updated by this UndoRedo
   */
  public EasyAddAbstractAction getUndoAction() { return undoAction; }
  /**
   * Gets the redo action associated and updated by this UndoRedo.
   *
   * @return action associated and updated by this UndoRedo
   */
  public EasyAddAbstractAction getRedoAction() { return redoAction; }
  /**
   * Gets the undo enable action associated and updated by this UndoRedo.
   *
   * @return action associated and updated by this UndoRedo
   */
  public EasyAddAbstractAction getUndoEnableAction() {
      return undoEnableAction;
  }
  /**
   * Gets the undo disable action associated and updated by this UndoRedo.
   *
   * @return action associated and updated by this UndoRedo
   */
  public EasyAddAbstractAction getUndoDisableAction() {
      return undoDisableAction;
  }
  /**
   * Updates the status of buttons by calling function in
   * UndoButtons interface.
   */
  public void updateUndoButtons() {
    if( onlyUndoRedoFromEventThread &&
       (! SwingUtilities.isEventDispatchThread()) ) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { updateUndoButtons(); }
      } );
    }
    else synchronized (doListLock) {
      if(undoList != null) {
	  undoAction.setEnabled(true);
	  undoAction.putValue(Action.NAME, undoList.getUndoPresentationName());
      }
      else {
	  undoAction.setEnabled(false);
	  undoAction.putValue(Action.NAME, "Undo");
      }
      if(redoList != null) {
	  redoAction.setEnabled(true);
	  redoAction.putValue(Action.NAME, redoList.getRedoPresentationName());
      }
      else {
	  redoAction.setEnabled(false);
	  redoAction.putValue(Action.NAME, "Redo");
      }
      undoEnableAction.setEnabled(! enabled);
      undoDisableAction.setEnabled(enabled);
      if(undoButtons != null)
	undoButtons.updateUndoButtons(enabled,
				      undoList == null,
				      redoList == null);
    }
  }
  /**
   * Clears undo and redo lists
   */
  public void clearUndos() {
    if( onlyUndoRedoFromEventThread &&
       (! SwingUtilities.isEventDispatchThread()) ) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { clearUndos(); }
      } );
    }
    else synchronized (doListLock) {
      undoList = null;
      redoList = null;
      redoCnt = 0;
      stepLevel = 0;
      stepList = null;
      updateUndoButtons();
    }
  }
  /**
   * Searches undo and redo lists for a given filename object.
   *
   * @param sameAsFileObj	object that is associated with a file
   * @return			object form the lists that is associate with the
   *				same file
   */
  public Object getFileObject(Object sameAsFileObj) {
    Object fileObj = null;
    synchronized (doListLock) {
      if(undoList != null) {
	fileObj = getFileObject(undoList, sameAsFileObj);
	if(fileObj != null) return fileObj;
      }
      else if(redoList != null) {
	fileObj = getFileObject(redoList, sameAsFileObj);
	if(fileObj != null) return fileObj;
      }
    }
    return null;
  }
  /**
   * Searches for a given filename object in a DoLink list.
   *
   * @param list		DoLink list to search
   * @param sameAsFileObj	object that is associated with a file
   * @return			object form the list that is associate with the
   *				same file
   */
  static public Object getFileObject(DoLink list, Object sameAsFileObj) {
    while(list != null) {
      Object fileObj = list.getFileObject(sameAsFileObj);
      if( fileObj != null) return fileObj;
      list = list.next;
    }
    return null;
  }
  /**
   * Sets the enabled or disabled status and updates buttons accordingly
   *
   * @param enabled	<code>true</code> to enable undo/redo
   */
  public void setEnabled(final boolean enabled) {
    if( onlyUndoRedoFromEventThread &&
       (! SwingUtilities.isEventDispatchThread()) ) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setEnabled(enabled); }
      } );
    }
    else synchronized (doListLock) {
      this.enabled = enabled;
      if(! enabled) clearUndos();
      else updateUndoButtons();
    }
  }
  /**
   * Gets the enabled or disabled status.
   *
   * @return enabled	<code>true</code> if undo/redo enabled
   */
  public boolean isEnabled() { return enabled; }
  /**
   * Shows status and error messages.
   *
   * @param s	string to show
   */
  public void showStatus(String s) {
    if(ss != null) ss.showStatus(s);
    else System.out.println(s);
  }
  /**
   * Shows throwable objects.
   *
   * @param t Throwable object to show stack trace for.
   */
  public void showStatus(Throwable t) {
    if(ss != null) ss.showStatus(t);
    else t.printStackTrace();
  }
  /**
   * Class to link undo and redo commands.
   *
   * @author	Joel T. Lee
   */
  private class DoLink implements CNUFileObject {
    Object command = null;
    String commandPresentationName = null;
    Object inverse = null;
    Object postCommand = null;
    Component repaintDc;
    DoLink next = null;
    DoLink(Object command) {
      this.command = command;
    }
    /**
     * Constructs a new instance of DoLink.
     *
     * @param command	command or step list
     * @param  commandPresentationName name for presenting on undo/redo buttons
     */
    DoLink(DoLink command, String commandPresentationName) {
      this.command = command;
      this.commandPresentationName = commandPresentationName;
    }
    /**
     * Constructs a new instance of DoLink.
     *
     * @param command	command or step list
     * @param inverse	inverse command or inverse step list
     */
    DoLink(Object command, Object inverse) {
      this.command = command;
      this.inverse = inverse;
    }
    /**
     * Constructs a new instance of DoLink.
     *
     * @param command	command or step list
     * @param inverse	inverse command or inverse step list
     * @param  commandPresentationName name for presenting on undo/redo buttons
     */
    DoLink(Object command, Object inverse,  String commandPresentationName) {
      this.command = command;
      this.inverse = inverse;
      this.commandPresentationName = commandPresentationName;
    }
    /**
     * Constructs a new instance of DoLink.
     *
     * @param command	command or step list
     * @param inverse	inverse command or inverse step list
     * @param repaintDc	component to repaint after command
     */
    DoLink(Object command, Object inverse, Component repaintDc) {
      this.command = command;
      this.inverse = inverse;
      this.repaintDc = repaintDc;
    }
    /**
     * Constructs a new instance of DoLink.
     *
     * @param command	command or step list
     * @param inverse	inverse command or inverse step list
     * @param repaintDc	component to repaint after command
     * @param  commandPresentationName name for presenting on undo/redo buttons
     */
    DoLink(Object command, Object inverse, Component repaintDc,
	   String commandPresentationName) {
      this.command = command;
      this.inverse = inverse;
      this.repaintDc = repaintDc;
      this.commandPresentationName = commandPresentationName;
    }
    /**
     * Constructs a new instance of DoLink.
     *
     * @param command	command or step list
     * @param inverse	inverse command or inverse step list
     * @param postCommand	command to execute after command
     * @param repaintDc	component to repaint after command
     */
    DoLink(Object command, Object inverse, Object postCommand,
	   Component repaintDc) {
      this.command = command;
      this.inverse = inverse;
      this.postCommand = postCommand;
      this.repaintDc = repaintDc;
    }
    /**
     * Constructs a new instance of DoLink.
     *
     * @param command	command or step list
     * @param inverse	inverse command or inverse step list
     * @param postCommand	command to execute after command
     * @param repaintDc	component to repaint after command
     * @param  commandPresentationName name for presenting on undo/redo buttons
     */
    DoLink(Object command, Object inverse, Object postCommand,
	   Component repaintDc, String commandPresentationName) {
      this.command = command;
      this.inverse = inverse;
      this.postCommand = postCommand;
      this.repaintDc = repaintDc;
      this.commandPresentationName = commandPresentationName;
    }
    public String getUndoPresentationName() {
      if(commandPresentationName != null) 
	  return "Undo " + commandPresentationName;
      else if(inverse instanceof DoCommand)
	  return "Undo " + ((DoCommand) inverse).getPresentationName();
      else if(command instanceof DoLink)
	  return ((DoLink) command).getUndoPresentationName();
      else return "Undo";
    }
    public String getRedoPresentationName() {
      if(commandPresentationName != null) 
	  return "Redo " + commandPresentationName;
      else if(inverse instanceof DoCommand)
	  return "Redo " + ((DoCommand) inverse).getPresentationName();
      else if(command instanceof DoLink)
	  return ((DoLink) command).getRedoPresentationName();
      else return "Redo";
    }
public String toString() {
  return "\"" + getUndoPresentationName() + "\",\"" + getRedoPresentationName();
}
    /**
     * Swaps the primary command and inverse command effectively converting
     * an undo into a redo or visa-versa.
     */
    public void swap() {
      Object tmp = command;
      command = inverse;
      inverse = tmp;
    }
    /**
     * Searches DoLink for a given filename object.
     *
     * @param sameAsFileObj	object that is associated with a file
     * @return			object form the DoLink that is associate with the
     *				same file
     */
    public Object getFileObject(Object sameAsFileObj) {
      Object fileObj=null;
      if(repaintDc instanceof CNUFileObject) {
	fileObj = ((CNUFileObject) repaintDc).getFileObject(sameAsFileObj);
        if(fileObj != null) return fileObj;
      }
      if(command instanceof DoCommand) {
	fileObj = ((DoCommand) command).getFileObject(sameAsFileObj);
	if( fileObj != null) return fileObj;
      }
      else if(command instanceof DoLink) {
	// loop through list of DoLinks
	fileObj = UndoRedo.getFileObject((DoLink) command, sameAsFileObj);
	if( fileObj != null) return fileObj;
      }
      if(inverse instanceof DoCommand) {
	fileObj = ((DoCommand) inverse).getFileObject(sameAsFileObj);
	if( fileObj != null) return fileObj;
      }
      else if(inverse instanceof DoLink) {
	fileObj = UndoRedo.getFileObject((DoLink) inverse, sameAsFileObj);
	if( fileObj != null) return fileObj;
      }
      if(postCommand instanceof DoCommand) {
	fileObj = ((DoCommand) postCommand).getFileObject(sameAsFileObj);
	if( fileObj != null) return fileObj;
      }
      else if(postCommand instanceof DoLink) {
	fileObj = UndoRedo.getFileObject((DoLink) postCommand, sameAsFileObj);
	if( fileObj != null) return fileObj;
      }
      return null;
    }
  }
}
