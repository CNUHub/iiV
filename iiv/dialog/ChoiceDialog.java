package iiv.dialog;
import iiv.*;
import java.awt.*;
import java.awt.event.*;
/**
 * Class to display message, checkbox choices and wait for user response.
 * from user.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 */
public class ChoiceDialog extends Dialog implements ActionListener {
  private static final long serialVersionUID = -8478825032875613075L;
  private Frame parentFrame;
  private Checkbox[] checkboxArray = null;
  private Button[] buttonArray = null;
  private TextField commentField = new TextField(80);
  private int doneSelection = -1;
  /**
   * Constructs a new instance of ChoiceDialog.
   *
   * @param parentFrame	parent frame
   * @param title	title to display on dialog
   * @param quest	question desiring a response
   * @param choices	list of selectable answers to question
   * @param doneChoices	list of possible continue choices
   * @param comments 
   */
  public ChoiceDialog(Frame parentFrame, String title, String quest,
	      String[] choices, String commentTitle, String[] doneChoices) {
    super(parentFrame, title, true);
    this.parentFrame = parentFrame;

    GridBagLayout gbl = new GridBagLayout();
    setLayout(gbl);
    int col = 0; int row = 0;
    int totalCols =
      (choices.length > doneChoices.length)? choices.length : doneChoices.length;
    int stdfill = GridBagConstraints.NONE;

    CNUDialog.addtogridbag(new Label(quest), col, row, 1, totalCols, stdfill, gbl, this);
    row++; col = 0;

    checkboxArray = new Checkbox[choices.length];
    for(int i=0; i < choices.length; i++) {
      checkboxArray[i] = new Checkbox(choices[i]);
      CNUDialog.addtogridbag(checkboxArray[i], col, row, 1, 1, stdfill, gbl, this);
      col++;
    }
    row++; col = 0;

    if(commentTitle != null) {
      if(commentTitle.length() > 0) {
	CNUDialog.addtogridbag(new Label(commentTitle), col, row, 1, totalCols, stdfill, gbl, this);
	row++;
      }
      CNUDialog.addtogridbag(commentField, col, row, 1, totalCols, stdfill, gbl, this);
      row++;
    }

    buttonArray = new Button[doneChoices.length];
    for(int i=0; i < doneChoices.length; i++) {
      buttonArray[i] = new Button(doneChoices[i]);
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
	doneSelection = i;
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
   * Sets a choice as selected or not.
   *
   * @param indice index to choice to set
   * @param selected <code>true</code> to select choice,
   *    <code>false</code> to deselect choice.
   */
  public void setChoiceSelected(int indice, boolean selected) {
    checkboxArray[indice].setState(selected);
  }
  /**
   * Sets all choices as selected or not.
   *
   * @param selected <code>true</code> to select choices,
   *    <code>false</code> to deselect choices.
   */
  public void setAllChoices(boolean selected) {
    for(int indice=0; indice<checkboxArray.length; indice++)
      checkboxArray[indice].setState(selected);
  }
  /**
   * Gets the selection state of a choice.
   *
   * @param indice index of choice to query
   * @return <code>true</code> if choice is selected
   */
  public boolean getChoiceSelected(int indice) {
    return checkboxArray[indice].getState();
  }
  /**
   * Gets the result.
   *
   * @return	index to answer selected
   */
  public int getDoneSelection() { return doneSelection; }
  /**
   * Gets the text in from the comment field.
   *
   * @return text from comment field
   */
  public String getComment() { return commentField.getText(); }
  /**
   * Beeps.
   */
  public void beep() {
    Toolkit.getDefaultToolkit().beep();
  }
}
