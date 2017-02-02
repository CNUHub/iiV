package iiv.dialog;
import iiv.*;
import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.lang.*;
import java.net.*;

/**
 * ShowMemoryDialog is a Dialog window to get scale factors from user.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		Dialog
 */
public class ShowMemoryDialog extends CNUDialog
implements ActionListener {
  private static final long serialVersionUID = 82522196299783873L;
  private Object showLock = new Object();
  private Runtime rt = null;
  private long free = 0;
  private long newFree = 0;
  private long total = 0;
  private long newTotal = 0;
  private Thread smt = null;
  private boolean threadContinue = false;

  private JLabel freeMemoryL = new JLabel("              ", JLabel.RIGHT);
  private JLabel totalMemoryL = new JLabel("             ", JLabel.RIGHT);
  private JButton garbageCollectB = new JButton("Garbage Collect");
  private JButton dismissB = new JButton("Dismiss");

  /**
   * Constructs a new instance of ShowMemoryDialog.
   *
   * @param parentFrame	parent frame needed for building a window
   */
  public ShowMemoryDialog(Frame parentFrame) { this(parentFrame, null); }
  /**
   * Constructs a new instance of ShowMemoryDialog.
   *
   * @param parentFrame	parent frame
   * @param cnuv	CNUViewer to associate with
   */
  public ShowMemoryDialog(Frame parentFrame, CNUViewer cnuv) {
    super(parentFrame, "Show Memory", false, cnuv);

    Container contentPane = getContentPane();
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    contentPane.add(Box.createVerticalStrut(5));

    Box box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(new JLabel("free memory ="));
    box.add(Box.createHorizontalGlue());
    box.add(freeMemoryL);
    box.add(new JLabel("bytes"));
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(new JLabel("total memory ="));
    box.add(Box.createHorizontalGlue());
    box.add(totalMemoryL);
    box.add(new JLabel("bytes"));
    box.add(Box.createHorizontalStrut(5));

    box = Box.createHorizontalBox();
    contentPane.add(box);
    box.add(Box.createHorizontalStrut(5));
    box.add(garbageCollectB);
    garbageCollectB.addActionListener(this);
    box.add(Box.createHorizontalGlue());
    box.add(dismissB);
    dismissB.addActionListener(this);
    box.add(Box.createHorizontalStrut(5));

    contentPane.add(Box.createVerticalStrut(5));

    pack();
  }
  private Runnable runShowFree = new Runnable() {
    public void run() { freeMemoryL.setText(Long.toString(free)); }
  };
  private Runnable runShowTotal = new Runnable() {
    public void run() { totalMemoryL.setText(Long.toString(total)); }
  };
  /**
   * Displays the current memory usage.
   *
   */
  private void showMemory() {
  }
  /**
   * Overrides setVisible to start and stop showMemory thread.
   *
   * @param state <code>true</code> to show <code>false</code> to hide
   */
  public void setVisible(boolean state) {
    super.setVisible(state);
    synchronized (showLock) {
      threadContinue = state;
      if(state) {
        if(rt == null) rt = Runtime.getRuntime();
        // create an anonymous thread to continuely update displayed memory
        if(smt == null) smt = new Thread() {
	  public void run() {
	    while(threadContinue) {
	      synchronized (showLock) {
		newFree = rt.freeMemory();
		if(newFree != free) {
		  free = newFree;
		  SwingUtilities.invokeLater(runShowFree);
		  //freeMemoryL.setText(Long.toString(free));
		}
		newTotal = rt.totalMemory();
		if(newTotal != total) {
		  total = newTotal;
		  SwingUtilities.invokeLater(runShowTotal);
		  //totalMemoryL.setText(Long.toString(total));
		}
	      }
	      try {
	        Thread.sleep(1000);
	      } catch (InterruptedException ie) {} // ignore
	    }
	  }
        };
	if(! smt.isAlive()) smt.start();
      }
      else if(smt != null) {
        if( smt.isAlive() ) {
	  // kill the thread the nice way
          smt.interrupt();  // interrupts sleep
	  try {
            smt.join(100); // wait for thread to stop nicely
          } catch (InterruptedException ignored) {}
        }
        if(smt.isAlive()) smt.stop();  // stop thread the bad way
	smt = null;
      }
    }
  }
  /**
   * Interprets mouse events over this dialog.
   *
   * @param e action event
   */
  public void actionPerformed(ActionEvent e){
    getCNUViewer().setWaitCursor();
    try {
      if (e.getSource() == garbageCollectB) System.gc();
      else if(e.getSource() == dismissB) setVisible(false);
    } finally {
      getCNUViewer().setNormalCursor();
    }
  }
}
