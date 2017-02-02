package iiv.gui;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.Color;
import java.awt.event.*;
import java.awt.Dimension;
import java.awt.Toolkit;
/**
 * TextAndSlider creates a component for viewing and selecting integer values.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		javax.swing.JSlider
 * @see		javax.swing.BoundedRangeModel
 * @since	iiV1.15a
 */
public class TextAndSlider extends JPanel {
  private static final long serialVersionUID = -5243680103203359203L;
  private JTextField valueTextField;
  private Box topBox;
  private BoundedRangeModel boundedRangeModel;
  private int textInputMaximum;
  private int textInputMinimum;
  private JLabel textLBL;
  private JSlider slider;
  /**
   * Create a new instance of TextAndSlider.
   *
   * @param labelText label text
   * @param valueColumns size in columns to create value text field
   * @param model bounded range model to base slider on
   */
  public TextAndSlider(String labelText,
		       int valueColumns,
		       BoundedRangeModel model) {
    this.boundedRangeModel = model;

    // initialize variables
    textLBL = new JLabel(labelText);
    valueTextField =
      new JTextField(Integer.toString(model.getValue()), valueColumns) {
      private static final long serialVersionUID = -5243680103203359203L;
      public Dimension getMinimumSize() { return this.getPreferredSize(); }
      public Dimension getMaximumSize() { return this.getPreferredSize(); }
    };
    valueTextField.setHorizontalAlignment(JTextField.RIGHT);
    valueTextField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	int value = getValue();
	try {
	  value = Integer.valueOf(valueTextField.getText()).intValue();
	  if((value > boundedRangeModel.getMaximum()) &&
	     (value < textInputMaximum)) boundedRangeModel.setMaximum(value);
	  if((value < boundedRangeModel.getMinimum()) &&
	     (value < textInputMinimum)) boundedRangeModel.setMinimum(value);
	} catch (NumberFormatException nfe) {
	  Toolkit.getDefaultToolkit().beep();
	}
	setValue(value);
      }
    });
    model.addChangeListener(
      new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
	  setValue(boundedRangeModel.getValue());	
        }
      }
    );
    textInputMaximum = model.getMaximum();
    textInputMinimum = model.getMinimum();

    // layout components
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    topBox = Box.createHorizontalBox();
    add(topBox);
    topBox.add(textLBL);
    topBox.add(Box.createHorizontalGlue());
    topBox.add(valueTextField);
    slider = new JSlider(model);
    add(slider);
  }
  /**
   * Set the foreground color.
   *
   * @param c color to set to
   */
  public void setForeground(Color c) {
    if(textLBL != null) textLBL.setForeground(c);
    super.setForeground(c);
  }
  /**
   * Set the tool tip text.
   *
   * @param toolTipText tool tip text
   */
  public void setToolTipText(String toolTipText) {
    super.setToolTipText(toolTipText);
    textLBL.setToolTipText(toolTipText);
    valueTextField.setToolTipText(toolTipText);
    slider.setToolTipText(toolTipText);
  }
  /**
   * Gets the current value.
   *
   * @return current value
   */
  public int getValue() { return boundedRangeModel.getValue(); }
 /**
   * Gets the text for this component.
   *
   * @return current text
   */
  public String getText() {
    return textLBL.getText() + valueTextField.getText();
  }
  /**
   * Sets the current value.
   *
   * @param value new value
   */
  public void setValue(int value) {
    if(value != boundedRangeModel.getValue())
      boundedRangeModel.setValue(value);
    valueTextField.setText(Integer.toString(boundedRangeModel.getValue()));
  }
  /**
   * Gets the maximum text input value.
   *
   * @return maximum text input value
   */
  public int getTextInputMaximum() { return textInputMaximum; }
  /**
   * Sets the maximum text input value.
   *
   * @param max maximum text input value
   */
  public void setTextInputMaximum(int max) { textInputMaximum = max; }
  /**
   * Gets the minimum text input value.
   *
   * @return minimum text input value
   */
  public int getTextInputMinimum() { return textInputMinimum; }
  /**
   * Sets the minimum text input value.
   *
   * @param min minimum text input value
   */
  public void setTextInputMinimum(int min) { textInputMinimum = min; }
  /**
   * Overrides getPreferredSize to keep width constrained by text label
   * and input field not slider preferred size.
   *
   * @return preferred size
   */
  public Dimension getPreferredSize() {
    return new Dimension(topBox.getPreferredSize().width,
			 super.getPreferredSize().height);
  }
  /**
   * Overrides getMinimumSize to keep in sync with preferred size.
   *
   * @return minumum size
   */
  public Dimension getMinimumSize() { return getPreferredSize(); }
  /**
   * Overrides getMaximumSize to keep in sync with preferred size.
   *
   * @return maximum size
   */
  public Dimension getMaximumSize() { return getPreferredSize(); }
}
