package iiv.display;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.lang.reflect.*;
/**
 * Container to display a single component.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.CNUViewer
 * @see		java.awt.Container
 * @since	iiV1.0
 */
public class SingleComponentCanvas extends Container {
  private static final long serialVersionUID = 7336091441186882658L;
  private Component dc = null;
  private Dimension preferredSize = new Dimension(0,0);
  /**
   * Constructs a new instance of SingleComponentCanvas.
   *
   * @param dc	component this canvas will contain
   */
  public SingleComponentCanvas(Component dc) {
    setLayout(null);
    add(dc);
    this.dc = dc;
    dc.validate();
    dc.setSize(dc.getPreferredSize());
    setPreferredSize(dc.getSize());
  }
  /**
   * Sets the font of both the container and the component.
   *
   * @param f	the new font
   */
  public void setFont(Font f) {
    super.setFont(f);
    getDisplayComponent().setFont(f);
    getDisplayComponent().setSize(getDisplayComponent().getPreferredSize());
    setPreferredSize(getDisplayComponent().getSize());
    invalidate();
  }
  /**
   * Sets the color model of the component if it is a DisplayComponent.
   *
   * @param cm	new color model
   */
  public void setColorModel(ColorModel cm) {
    // find out if setColorModel exist by trying to invoke it
    // if we fail - oh well
    try {
      Class[] params = new Class[1];
      params[0] = java.awt.image.ColorModel.class;
      Method method = dc.getClass().getMethod("setColorModel", params);
      Object[] args = new Object[1];
      args[0] = cm;
      method.invoke(dc, args);
      dc.invalidate();
      validate();
      repaint();
    } catch (NoSuchMethodException nsme) { // ignore
    } catch (SecurityException se) { // ignore
    } catch (IllegalAccessException iae) { // ignore
    } catch (IllegalArgumentException iarge) { // ignore
    } catch (InvocationTargetException ite) { // ignore
    }
  }
  /**
   * Paints the component to the graphics context.
   *
   * @param g	graphics context
   */
  public void paint(Graphics g) {
    // First clear the area
    g.setColor(getBackground());
    Rectangle r = g.getClipBounds();
    if(r == null) {
      Dimension d = getSize();
      r = new Rectangle(d.width, d.height);
    }
    if(r != null) g.fillRect(r.x, r.y, r.width, r.height);
    dc.paint(g);
  }
  /**
   * Sets the preferred size.
   *
   * @param s	new preferred size
   */
  public void setPreferredSize(Dimension s) { preferredSize.setSize(s); }
  /**
   * Gets the preferred size of this container.
   *
   * @return the preferred size.
   */
  public Dimension getPreferredSize() { return preferredSize; }
  /**
   * Gets the minimum size of this container which is the same as the preferred
   *
   * @return the minimum size.
   */
  public Dimension getMinimumSize() { return preferredSize; }
  /**
   * Gets the maximum size of this container which is the same as the preferred
   *
   * @return the maximum size.
   */
  public Dimension getMaximumSize() { return preferredSize; }
  /**
   * Gets the display component.
   *
   * @return	the display component
   */
  public Component getDisplayComponent() { return dc; }
}
