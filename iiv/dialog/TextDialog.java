package iiv.dialog;
import iiv.*;
import iiv.display.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.lang.*;

/**
 * TextDialog is a dialog window for building text components to
 * add to the display window.
 * 
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUDisplay
 * @since	iiV1.0
 */
public class TextDialog extends CNUDialog
implements ActionListener {
  private static final long serialVersionUID = -8848956826996641394L;
  JTextArea textA = new JTextArea();
  JButton addTextB = new JButton("add text");
  JButton applyJustificationB = new JButton("apply justification");
  String[] justificationNames = {"Left", "Centered", "Right"};
  int[] justificationValues = { DisplayText.LEFT, DisplayText.CENTERED,
				DisplayText.RIGHT };
  JComboBox justificationCH = new JComboBox(justificationNames);
  JComboBox fontCH = new JComboBox();
  //  String[] fontNames;
  String[] styleNames = {"plain", "italic", "bold", "bold-italic"};
  int[] styleValues = {Font.PLAIN, Font.ITALIC, Font.BOLD,
		       Font.ITALIC|Font.BOLD};
  JComboBox styleCH = new JComboBox(styleNames);
  int[] sizeValues = {7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17,
		      18, 19, 20, 22, 24, 26, 30, 35, 40};
  JComboBox sizeCH = new JComboBox();
  JButton applyFontB = new JButton("apply font");
  JComboBox colorCH = new JComboBox();
  JButton applyColorB = new JButton("apply color");
  JButton setToCurrentB = new JButton("set to current");
  JButton applyToDefaultB = new JButton("apply to default only");
  JButton dismissB = new JButton("dismiss");

  JButton bestFactorB = new JButton("Set Defaults");
  /**
   * Constructs a new instance of TextDialog.
   *
   * @param parentFrame	parent frame
   */
  public TextDialog(Frame parentFrame) {
      this(parentFrame, null);
  }
  /**
   * Constructs a new instance of TextDialog.
   *
   * @param parentFrame	parent frame
   * @param cnuv	primary display controller
   */
  public TextDialog(Frame parentFrame, CNUViewer cnuv) {
    super(parentFrame, "Set Text", false, cnuv);
    Container contentPane = getContentPane();

    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    contentPane.add(Box.createVerticalStrut(5));

    Box box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(new JScrollPane(textA));
    box.add(Box.createRigidArea(new Dimension(0, 34)));

    box.add(Box.createHorizontalStrut(5));
    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(addTextB);
    addTextB.addActionListener(this);
    box.add(applyFontB);
    applyFontB.addActionListener(this);
    box.add(Box.createHorizontalGlue());

    box.add(Box.createHorizontalStrut(5));
    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    //    fontNames = Toolkit.getDefaultToolkit().getFontList();
    //    for(int i = 0; i < fontNames.length; i++) fontCH.addItem(fontNames[i]);
    Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    String lastname = "notafontabcd";
    for(int i = 0; i < fonts.length; i++) {
      String fontname = fonts[i].getFamily();
      if(fontname != null)
	if(! fontname.equals(lastname)) fontCH.addItem(fontname);
      lastname=fontname;
    }
    box.add(fontCH);

    box.add(styleCH);

    for(int i = 0; i < sizeValues.length; i++)
      sizeCH.addItem( Integer.toString(sizeValues[i]));
    sizeCH.setSelectedItem("16");
    box.add(sizeCH);
    box.add(Box.createHorizontalGlue());

    box.add(Box.createHorizontalStrut(5));
    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(justificationCH);

    box.add(applyJustificationB);
    applyJustificationB.addActionListener(this);
    box.add(Box.createHorizontalGlue());

    box.add(Box.createHorizontalStrut(5));
    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    for(int i = 0; i < colorNames.length; i++) colorCH.addItem(colorNames[i]);
    box.add(colorCH);
    box.add(applyColorB);
    applyColorB.addActionListener(this);
    box.add(Box.createHorizontalGlue());


    box.add(Box.createHorizontalStrut(5));
    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(setToCurrentB);
    setToCurrentB.addActionListener(this);

    box.add(applyToDefaultB);
    applyToDefaultB.addActionListener(this);
    box.add(Box.createHorizontalGlue());
    box.add(dismissB);
    dismissB.addActionListener(this);

    box.add(Box.createHorizontalStrut(5));
    contentPane.add(Box.createVerticalStrut(5));

    pack();
  }
  /**
   * Sets the CNUViewer.
   *
   * @param cnuviewer the new CNUViewer for this dialog to use
   *		Should not be <code>null</code>.
   */
  public void setCNUViewer(CNUViewer cnuviewer) {
    super.setCNUViewer(cnuviewer);
    // normally only called after creation so do standard
    // creation init
    setToCurrent();
  }
  /**
   * Sets text, font and color selections to current values.
   */
  public void setToCurrent() {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setToCurrent(); }
      } );
      return;
    }
    CNUDisplay cnud = getCNUDisplay();
    // get and set current font
    Font f = cnud.getCurrentFont();
    if(f != null) {
      String fontname = f.getName();
      fontCH.setSelectedItem(fontname);
      if(! fontname.equals(fontCH.getSelectedItem()) ) {
	fontCH.addItem(fontname);
	fontCH.setSelectedItem(fontname);
      }
      int style = f.getStyle();
      for( int i=0; i < styleValues.length; i++) {
	if(styleValues[i] == style) {
	  styleCH.setSelectedIndex(i);
	  break;
	}
      }
      int size = f.getSize();
      for( int i=0; i < sizeValues.length; i++) {
	if(sizeValues[i] == size) {
	  sizeCH.setSelectedIndex(i);
	  break;
	}
      }
    }
    // get and set current justification
    int justification = cnud.getCurrentJustification();
    for( int i=0; i < justificationValues.length; i++) {
      if(justificationValues[i] == justification) {
	justificationCH.setSelectedIndex(i);
	break;
      }
    }
    // get and set current text
    String s = cnud.getCurrentText();
    if(s != null) textA.setText(s);
    // get and set current color
    Color c = cnud.getCurrentForeground();
    for( int i=0; i < colorValues.length; i++) {
      if(colorValues[i] == c) {
	colorCH.setSelectedIndex(i);
	break;
      }
    }
  }
  /**
   * Interprets mouse events over this dialog.
   *
   * @param e	action event
   */
  public void actionPerformed(ActionEvent e){
    getCNUViewer().setWaitCursor();
    try {
      CNUDisplay cnud = getCNUDisplay();
      Object source = e.getSource();
      if(source == addTextB || source == textA) {
        String s = textA.getText();
        if( s != null) {
          s = s.trim();
          if( ! s.equals("") ) {
            DisplayText dt = new DisplayText(s);
            dt.setFont(new Font((String) fontCH.getSelectedItem(),
		      styleValues[styleCH.getSelectedIndex()],
		      sizeValues[sizeCH.getSelectedIndex()]));
            dt.setForeground( colorValues[colorCH.getSelectedIndex()] );
	    dt.setJustification( justificationValues[
				  justificationCH.getSelectedIndex()] );
            cnud.addAndRepaint(dt);
          }
        }
      }
      else if(source == setToCurrentB) setToCurrent();
      else if(source == applyFontB) {
	Font font = new Font((String) fontCH.getSelectedItem(),
		      styleValues[styleCH.getSelectedIndex()],
		      sizeValues[sizeCH.getSelectedIndex()]);
	DisplayComponentDefaults.setDefaultFont(font);
	cnud.setFont(font);
        cnud.updateFont();
      }
      else if(source == applyJustificationB) {
        cnud.setJustification(justificationValues[
				justificationCH.getSelectedIndex()]);
        cnud.updateJustification();
      }
      else if(source == applyColorB) {
        cnud.updateForegroundColor( colorValues[colorCH.getSelectedIndex()]);
      }
      else if(source == applyToDefaultB) {
	Font font = new Font((String) fontCH.getSelectedItem(),
		      styleValues[styleCH.getSelectedIndex()],
		      sizeValues[sizeCH.getSelectedIndex()]);
	DisplayComponentDefaults.setDefaultFont(font);
	cnud.setFont(font);
        cnud.setForeground( colorValues[colorCH.getSelectedIndex()]);
        cnud.setJustification(justificationValues[
				justificationCH.getSelectedIndex()]);
      }
      else if(source == dismissB) {
        this.setVisible(false);
      }
    } finally {
      getCNUViewer().setNormalCursor();
    }
  }
}
