package iiv.dialog;
import java.awt.*;
/**
 * Class to display message and wait for continue or cancel selection
 * from user.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 */
public class ContinueDialog extends QueryDialog {
  private static final long serialVersionUID = 6271637761638982362L;
  static String[] answers = {"Continue", "Cancel"};
  /**
   * Constructs a new instance of ContinueDialog.
   *
   * @param parentFrame	parent frame
   * @param title	title for this ContinueDialog
   * @param quest	question desiring a continue or cancel response
   */
  public ContinueDialog(Frame parentFrame, String title, String quest) {
    super(parentFrame, title, quest, answers);
  }
  /**
   * Gets the the user response.
   *
   * @return <code>true</code> if user wishes to continue
   */
  public boolean getContinueFlag() {
    if(getSelection() == 0) return true;
    return false;
  }
}
