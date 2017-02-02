package iiv.dialog;
import iiv.*;
import iiv.display.*;
import iiv.util.*;
import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.awt.event.*;
/**
 * Extends java.awt.Dialog performing some standard tasks.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		java.awt.Dialog
 * @since	iiV?
 */
public class CNUDialog extends JDialog implements ShowStatus, WindowListener {
  private static final long serialVersionUID = -2285372971012334640L;
  private Frame parentFrame;
  private CNUViewer cnuv;
  private Object cnuvPtrLock = new Object();
  public static String[] colorNames =
  {"black", "blue", "cyan", "darkGray", "gray",
   "green", "lightGray", "magenta", "orange",
   "pink", "red", "white", "yellow"};
  public static Color[] colorValues =
  {Color.black, Color.blue, Color.cyan, Color.darkGray, Color.gray,
   Color.green, Color.lightGray, Color.magenta, Color.orange,
   Color.pink, Color.red, Color.white, Color.yellow};
  /**
   * Returns a color from a string representation.
   *
   * @param name	color name
   * @return		color
   */
  public final static Color stringToColor(String name) {
    for(int nc = 0; nc < colorNames.length; nc++)
      if(colorNames[nc].equalsIgnoreCase(name)) return colorValues[nc];
    // maybe 3 numbers
    int red=0; int green=0; int blue=0;
    name = name.trim();
    try {
      int cnt = 0;
      for(int index = name.length() - 1; index >= 0; index--) {
	if(index == 0) {
	  red = Integer.parseInt(name);
          if(cnt != 2) return null;
          else return new Color(red, green, blue);
	}
        else if(Character.isWhitespace(name.charAt(index))) {
	  if(cnt == 0) blue = Integer.parseInt(name.substring(index+1));
	  else if(cnt == 1) green = Integer.parseInt(name.substring(index+1));
	  else return null;
	  name = name.substring(0, index).trim();
	  index = name.length(); // don't subtract 1 because loop will
	  cnt++;
        }
      }
    } catch (NumberFormatException nfe) {
    }
    return null;
  }
  /**
   * Returns a string representation of color.
   *
   * @param c	color
   * @return	color name
   */
  public final static String colorToString(Color c) {
    if(c == null) return "";
    for(int nc = 0; nc < colorValues.length; nc++) {
      if( colorValues[nc].equals(c) ) {
	// only prints a text color if the appropriate one was found
	return colorNames[nc];
      }
    }
    // defaults to just RGB values
    return c.getRed() + " " + c.getGreen() + " " + c.getBlue();
  }
  /**
   * Constructs a new instance of CNUDialog.
   * @param parentFrame	parent frame
   * @param title	title for top of dialog
   * @param mode	<code>true</code> it the dialog blocks other
   *			user interaction
   * @param cnuv	CNUViewer 
   */
  public CNUDialog(Frame parentFrame, String title,
		   boolean mode, CNUViewer cnuv) {
    super(parentFrame, title, mode);
    addWindowListener(this);
    this.parentFrame = parentFrame;
    this.cnuv = cnuv;
  }
  /**
   * Constructs a new instance of CNUDialog.
   * @param parentFrame	parent frame
   * @param title	title for top of dialog
   * @param mode	<code>true</code> it the dialog blocks other
   *			user interaction
   */
  public CNUDialog(Frame parentFrame, String title,
		   boolean mode) {
    this(parentFrame, title, mode, null);
  }
  /**
   * Centers this dialog over the parent frame.
   */
  public void centerOnParentFrame() {
    Dimension myDim = getSize();
    Dimension frameDim = getParentFrame().getSize();
    Point loc = getParentFrame().getLocation();
    // Center the dialog w.r.t. the frame
    loc.translate((frameDim.width - myDim.width)/2,
		  (frameDim.height - myDim.height)/2);
    safeSetLocation(loc);
  }
  /**
   * Sets this dialog location ensuring the dialog is within screen bounds.
   *
   * @param loc	location on screen for this dialog
   */
  public void safeSetLocation(Point loc) {
    loc = new Point(loc);
    Dimension myDim = getSize();
    // ensure dialog is within screen bounds
    Dimension screenSize = getToolkit().getScreenSize();
    loc.x = Math.max(0, Math.min(loc.x, screenSize.width-myDim.width));
    loc.y = Math.max(0, Math.min(loc.y, screenSize.height-myDim.height));
    setLocation(loc);
  }
  /**
   * Overides pack to center dialog over frame by default.
   */
  public void pack() {
    super.pack();
    centerOnParentFrame();
  }
  /**
   * Shows status and error messages.
   *
   * @param s	message to show
   */
  public void showStatus(String s) {
    CNUViewer cnuv = getCNUViewer();
    if(cnuv != null) cnuv.showStatus(s);
    else System.out.println(s);
  }
  /**
   * Shows throwable objects.
   *
   * @param t	throwable object to print stack trace from
   */
  public void showStatus(Throwable t) {
    CNUViewer cnuv = getCNUViewer();
    if(cnuv != null) cnuv.showStatus(t);
    else t.printStackTrace();
  }
  /**
   * Gets the parent frame of this.
   *
   * @return	parent frame
   */
  public Frame getParentFrame() {
    return parentFrame;
  }
  /**
   * Sets the CNUViewer.
   *
   * @param cnuviewer the new CNUViewer for this dialog to use
   *		May be <code>null</code>.
   */
  public void setCNUViewer(CNUViewer cnuviewer) {
    synchronized (cnuvPtrLock) { this.cnuv = cnuviewer; }
  }
  /**
   * Gets the CNUViewer.
   *
   * @return	The CNUViewer either set or found from as parent.
   *		May return <code>null</code>.
   */
  public CNUViewer getCNUViewer() {
    CNUViewer localcnuv = cnuv;
    if(localcnuv == null) {
      Component c = this;
      synchronized (c.getTreeLock()) {
	while( !(c instanceof CNUViewer) ) {
	  c = c.getParent();
	  if(c == null)break;
	}
	if(c instanceof CNUViewer) localcnuv = (CNUViewer) c;
      }
      synchronized (cnuvPtrLock) {
	if(cnuv != null) localcnuv = cnuv;
	else cnuv = localcnuv;
      }
    }
    return localcnuv;
  }
  /**
   * Gets the current CNUDisplay.
   *
   * @return	The CNUDisplay as found from the CNUViewer.
   *		May return <code>null</code>.
   */
  public CNUDisplay getCNUDisplay() {
    CNUViewer cnuv = getCNUViewer();
    if(cnuv != null) return cnuv.getCNUDisplay();
    else return null;
  }
  /**
   * Converts basic settings into a CNU script.  Should be overridden
   * by dialogs that want settings saved by iiV.
   *
   * @return iiV script commands to recreate current settings
   */
  public String settingsToScript() { return ""; }
  /*
   * Implements WindowListener primarly to hide dialog when default
   * menu request a close.
   */
  /**
   * Called when window is about to be closed.  Overridden to hide dialog
   * upon closing.
   *
   * @param evt	window event
   */
  public void windowClosing(WindowEvent evt) { setVisible(false); }
  /**
   * Called after window is activated.
   *
   * @param evt window event
   */
  public void windowActivated(WindowEvent evt) { }
  /**
   * Called after window is closed.
   *
   * @param evt	window event
   */
  public void windowClosed(WindowEvent evt) { }
  /**
   * Called after window is deactivated.
   *
   * @param evt	window event
   */
  public void windowDeactivated(WindowEvent evt) { }
  /**
   * Called after window is deiconfied.
   *
   * @param evt	window event
   */
  public void windowDeiconified(WindowEvent evt) { }
  /**
   * Called after window is iconfied.
   *
   * @param evt	window event
   */
  public void windowIconified(WindowEvent evt) { }
  /**
   * Called after window has been shown for the first time.
   *
   * @param evt	window event
   */
  public void windowOpened(WindowEvent evt) { }
  /**
   * Add a component to a container using GridBagLayout.
   *
   * @param a		component to add
   * @param x		column to put component in
   * @param y		row to put component in
   * @param H		height of component in rows
   * @param W		width of component in columns
   * @param fill	fill constraint for component
   * @param gbl		GridBagLayout to add component to
   * @param C		Container to add componet to
   * @see		java.awt.GridBagLayout
   * @see		java.awt.GridBagConstraints
   */
  static public void addtogridbag(Component a, int x, int y,
			   int H, int W, int fill,
			   GridBagLayout gbl, Container C) {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets.top = 2;
    gbc.insets.bottom = 2;
    gbc.insets.left = 5;
    gbc.insets.right = 5;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.fill = fill;
    gbc.gridx = x;
    gbc.gridy = y;
    gbc.gridheight = H;
    gbc.gridwidth = W;
    gbl.setConstraints(a, gbc);
    C.add(a);
  }
}
