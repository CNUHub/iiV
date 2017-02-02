package iiv.util;
/**
 * Interface to allow notification of change in undo/redo status for updating
 * button status.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		UndoRedo
 */
public interface UndoButtons {
  /**
   * Called when a change occurs in the state of the undo/redo list.
   *
   * @param enabled	<code>true</code> if undo/redos are enabled
   * @param undoEmpty	<code>true</code> if undo list is empty
   * @param redoEmpty	<code>true</code> if redo list is empty
   */
  public void updateUndoButtons(boolean enabled,
				boolean undoEmpty, boolean redoEmpty);
}
