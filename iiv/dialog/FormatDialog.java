package iiv.dialog;
import iiv.*;
import iiv.display.*;
import iiv.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.lang.*;
import java.text.*;
import java.util.*;

/**
 * FormatDialog is a dialog window for building text components to
 * add to the display window.
 * 
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.display.CNUDisplay
 * @since	iiV1.0
 */
public class FormatDialog extends CNUDialog
implements ActionListener {
  private static final long serialVersionUID = -4862303747590822462L;
  public final static String[] minimumDigitsNames = {
    "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
  public final static String[] maximumDigitsNames = {
    "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "BIG"};
  public final static int BIG_INDICE = maximumDigitsNames.length - 1;
  private JComboBox maxFractionDigitsCH = new JComboBox(maximumDigitsNames);
  private JComboBox minFractionDigitsCH = new JComboBox(minimumDigitsNames);
  private JComboBox maxIntegerDigitsCH = new JComboBox(maximumDigitsNames);
  private JComboBox minIntegerDigitsCH = new JComboBox(minimumDigitsNames);
  public final static String[] exponentMultipleNames = {
    "No Exp", "Zero Int", "1", "2", "3", "4", "5", "6" };
  int[] exponentMultipleValues = {
    DisplayNumberFormat.NO_EXPONENT,
    DisplayNumberFormat.ZERO_INTEGER_EXPONENT_MULTIPLE,
    1, 2, 3, 4, 5, 6 };
  private JComboBox exponentMultiplesCH = new JComboBox(exponentMultipleNames);
  private JCheckBox commasCB = new JCheckBox("Commas");
  private JComboBox exponentSymbolCH = new JComboBox();
  private Vector<String> exponentSymbols = new Vector<String>(3);
  public final static String[] defaultExponentSymbols = {"E", "e", "x10^"};

  private JButton applyB = new JButton("apply");
  private JButton setToCurrentB = new JButton("set to current");
  private JButton applyToDefaultB = new JButton("apply to default only");
  private JButton dismissB = new JButton("dismiss");

  JButton bestFactorB = new JButton("Set Defaults");
  /**
   * Constructs a new instance of FormatDialog.
   *
   * @param parentFrame	parent frame
   */
  public FormatDialog(Frame parentFrame) { this(parentFrame, null); }
  /**
   * Constructs a new instance of FormatDialog.
   *
   * @param parentFrame	parent frame
   * @param cnuv	primary display controller
   */
  public FormatDialog(Frame parentFrame, CNUViewer cnuv) {
    super(parentFrame, "Labels Number Format", false, cnuv);

    Container contentPane = getContentPane();
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    contentPane.add(Box.createVerticalStrut(5));

    Box box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(new JLabel("Integer digits Max:   "));
    maxIntegerDigitsCH.setToolTipText("Selects the maximum number of digits shown to left of the decimal point");
    box.add(maxIntegerDigitsCH);
    box.add(Box.createHorizontalGlue());
    box.add(new JLabel("Min:  "));
    minIntegerDigitsCH.setToolTipText("Selects the minimum number of digits shown to left of the decimal point");
    box.add(minIntegerDigitsCH);
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(new JLabel("Fraction digits Max:  "));
    maxFractionDigitsCH.setToolTipText("Selects the maximum number of digits shown to right of the decimal point");
    box.add(maxFractionDigitsCH);
    box.add(Box.createHorizontalGlue());
    box.add(new JLabel("Min:  "));
    minFractionDigitsCH.setToolTipText("Selects the minimum number of digits shown to right of the decimal point");
    box.add(minFractionDigitsCH);
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    commasCB.setToolTipText("Select to show commas at the powers of 3 locations to designate thousands, millions, etc.");
    box.add(commasCB);
    box.add(Box.createHorizontalGlue());
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(new JLabel("Exponent multiples:  "));
    exponentMultiplesCH.setToolTipText("Selects whether exponents are shown and if they are limitted to certain multples");
    box.add(exponentMultiplesCH);
    for(int i = 0; i < defaultExponentSymbols.length; i++) {
      exponentSymbols.addElement(defaultExponentSymbols[i]);
      exponentSymbolCH.addItem(defaultExponentSymbols[i]);
    }
    box.add(Box.createHorizontalGlue());
    box.add(new JLabel("Exponent Symbol:  "));
    exponentSymbolCH.setToolTipText("Selects the symbol used to designate exponents");
    box.add(exponentSymbolCH);
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    applyB.setToolTipText("Applies number format to selected or all displayed objects");
    box.add(applyB);
    applyB.addActionListener(this);
    applyToDefaultB.setToolTipText("Applies number format to default for newly displayed objects");
    box.add(applyToDefaultB);
    applyToDefaultB.addActionListener(this);
    setToCurrentB.setToolTipText("Grabs format from currently selected object");
    box.add(setToCurrentB);
    setToCurrentB.addActionListener(this);
    box.add(Box.createHorizontalGlue());
    dismissB.setToolTipText("Hides this dialog");
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
   * Sets the displayed number format to current values.
   */
  public void setToCurrent() {
    NumberFormat nf = null;
    Component comp = getCNUDisplay().getCurrentComponent();
    if(comp instanceof NumberFormattable)
      nf = ((NumberFormattable) comp).getNumberFormat();
    if(nf == null) nf = DisplayNumberFormat.getDefaultNumberFormat();
    setNumberFormat(nf);
  }
  /**
   * Sets the displayed number format to given values.
   *
   * @param nf NumberFormat to set to.
   */
  public void setNumberFormat(final NumberFormat nf) {
    if(nf == null) return;
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	public void run() { setNumberFormat(nf); }
      } );
      return;
    }
    int max = nf.getMaximumFractionDigits();
    if((max < 0) || (max > BIG_INDICE)) max = BIG_INDICE;
    maxFractionDigitsCH.setSelectedIndex(max);

    int min = nf.getMinimumFractionDigits();
    if((min < 0) || (min > minFractionDigitsCH.getItemCount())) min = 0;
    minFractionDigitsCH.setSelectedIndex(min);

    max = nf.getMaximumIntegerDigits();
    if((max < 0) || (max > BIG_INDICE)) max = BIG_INDICE;
    maxIntegerDigitsCH.setSelectedIndex(max);

    min = nf.getMinimumIntegerDigits();
    if((min < 0) || (min > minIntegerDigitsCH.getItemCount())) min = 0;
    minIntegerDigitsCH.setSelectedIndex(min);

    commasCB.setSelected(nf.isGroupingUsed());

    int exponentMultiples = DisplayNumberFormat.NO_EXPONENT;
    String exponentSymbol = null;
    if(nf instanceof DisplayNumberFormat) {
      DisplayNumberFormat dnf = (DisplayNumberFormat) nf;
      exponentMultiples = dnf.getExponentMultiples();
      exponentSymbol = dnf.getExponentSymbol();
    }
    exponentMultiplesCH.setSelectedIndex(0); // defaults to no exponent
    for(int i=0; i<exponentMultipleValues.length; i++) {
      if(exponentMultiples == exponentMultipleValues[i]) {
	exponentMultiplesCH.setSelectedIndex(i);
	break;
      }
    }
    if(exponentSymbol == null) exponentSymbolCH.setSelectedIndex(0);
    else {
      int index = exponentSymbols.indexOf(exponentSymbol);
      if(index < 0) synchronized (this) {
        index = exponentSymbolCH.getItemCount();
	exponentSymbols.addElement(exponentSymbol);
	exponentSymbolCH.addItem(exponentSymbol);
      }
      if(index > -1) exponentSymbolCH.setSelectedIndex(index);
    }
  }
  /**
   * Gets the number format.
   *
   * @return	number format
   */
  public NumberFormat getNumberFormat() {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn = new RunnableWithReturnObject() {
	  public void run() { returnObject = getNumberFormat(); }
      };
      runWithReturn.invokeAndWait();
      return (NumberFormat) runWithReturn.returnObject;
    }
    DisplayNumberFormat dnf = new DisplayNumberFormat();

    int max = maxFractionDigitsCH.getSelectedIndex();
    int min = minFractionDigitsCH.getSelectedIndex();
    if(max < min) { max = min; maxFractionDigitsCH.setSelectedIndex(max); }
    if(max >= BIG_INDICE) max = DisplayNumberFormat.BIG_VALUE;
    dnf.setMaximumFractionDigits(max);
    dnf.setMinimumFractionDigits(min);

    max = maxIntegerDigitsCH.getSelectedIndex();
    min = minIntegerDigitsCH.getSelectedIndex();
    if(max < min) { max = min; maxIntegerDigitsCH.setSelectedIndex(max); }
    if(max >= BIG_INDICE) max = DisplayNumberFormat.BIG_VALUE;
    dnf.setMaximumIntegerDigits(max);
    dnf.setMinimumIntegerDigits(min);

    dnf.setExponentMultiples(
      exponentMultipleValues[exponentMultiplesCH.getSelectedIndex()]);
    dnf.setExponentSymbol((String) exponentSymbolCH.getSelectedItem());
    dnf.setGroupingUsed(commasCB.isSelected());
    return dnf;
  }
  /**
   * Applies current selections to default.
   */
  public void applyToDefaults() {
    DisplayNumberFormat.setDefaultNumberFormat(getNumberFormat());
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
      if(source == setToCurrentB) setToCurrent();
      else if((source == applyToDefaultB) || (source == applyB)) {
	applyToDefaults();
	if(source == applyB) cnud.apply(cnud.NUMBER_FORMAT);
      }
      else if(source == dismissB) { this.setVisible(false); }
    } finally {
      getCNUViewer().setNormalCursor();
    }
  }
}
