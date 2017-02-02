package iiv.dialog;
import iiv.*;
import java.awt.*;
import java.awt.event.*;
/**
 * Class to display message and wait for user response.
 * from user.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 */
public class QueryDialog extends Dialog implements ActionListener {
  private static final long serialVersionUID = -1492407860194785783L;
  private Frame parentFrame;
  private Button[] buttonArray = null;
  private int selection = -1;
  /**
   * Constructs a new instance of QueryDialog.
   *
   * @param parentFrame	parent frame
   * @param title	title to display on dialog
   * @param quest	question desiring a response
   * @param answers	list of possible answers to question
   */
  public QueryDialog(Frame parentFrame, String title, String quest,
	      String[] answers) {
    super(parentFrame, title, true);
    this.parentFrame = parentFrame;

    GridBagLayout gbl = new GridBagLayout();
    setLayout(gbl);
    int col = 0; int row = 0;
    int stdfill = GridBagConstraints.NONE;

    CNUDialog.addtogridbag(new Label(quest), col, row, 1, 3, stdfill, gbl, this);
    row++;

    buttonArray = new Button[answers.length];
    for(int i=0; i < answers.length; i++) {
      buttonArray[i] = new Button(answers[i]);
      CNUDialog.addtogridbag(buttonArray[i], col, row, 1, 1, stdfill, gbl, this);
      buttonArray[i].addActionListener(this);
      col++;
    }
    pack();
    Dimension myDim = getSize();
    Dimension frameDim = parentFrame.getSize();
    Point loc = parentFrame.getLocation();
    // Center the dialog w.r.t. the frame
    loc.translate((frameDim.width - myDim.width)/2,
		  (frameDim.height - myDim.height)/2);
    // ensure dialog is within screen bounds
    Dimension screenSize = getToolkit().getScreenSize();
    loc.x = Math.max(0, Math.min(loc.x, screenSize.width-myDim.width));
    loc.y = Math.max(0, Math.min(loc.y, screenSize.height-myDim.height));
    setLocation(loc);
  }
  /**
   * Process key presses.
   *
   * @param evt	action event
   */
  public void actionPerformed(ActionEvent evt) {
    Object source = evt.getSource();
    for(int i=0; i < buttonArray.length; i++) {
      if(source == buttonArray[i]) {
	selection = i;
        this.setVisible(false);
	break;
      }
    }
    this.setVisible(false);
  }
  /**
   * Gets the parent frame of this dialog.
   *
   * @return	parent frame
   */
  public Frame getParentFrame() { return parentFrame; }
  /**
   * Gets the result.
   *
   * @return	index to answer selected
   */
  public int getSelection() { return selection; }
  /**
   * Beeps.
   */
  public void beep() {
    Toolkit.getDefaultToolkit().beep();
  }
}
