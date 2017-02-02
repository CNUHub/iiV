package iiv.display;
import iiv.filter.*;
import java.awt.*;
import java.awt.image.*;
/**
 * 
 * DisplayDraw is an abstract class that defines standard routines
 * for graphics drawing display components.
 * displayed by iiV.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUDisplay
 * @since	iiV1.0
 */
abstract public class DisplayDraw extends DisplayComponent {

  static public Color CLEAR_COLOR = Color.white;
  static public Color SOLID_COLOR = Color.black;

  private Object drawingLock = new Object();
  private Dimension drawImageSize = new Dimension(10,10);
  private Image drawImage = null;

  /**
   * Constructs a new instance of DisplayDraw.
   */
  DisplayDraw() {
    initColorModel(null); // color model not used
  }
  /**
   * Sets a new color model - overridden to do nothing.
   *
   * @param cm	Color model - ignored
   */
  public void setColorModel(ColorModel cm) {}
  /**
   * Sets the font - overridden to setFilterChanged.
   *
   * @param font new font
   */
  public void setFont(Font font) {
    if(font != getFont()) {
      super.setFont(font);
      invalidateFilters();
      setFilterChanged();
    }
  }
  /**
   * Sets the foreground color - overridden to invalidate filters.
   *
   * @param foregroundColor	new foreground color
   */
  public void setForeground(Color foregroundColor) {
    if(getForeground() != foregroundColor) {
      super.setForeground(foregroundColor);
      invalidateFilters();
      setFilterChanged();
    }
  }
  /**
   * Gets a value.
   * @return	always returns <code>0</code>
   */
  public double getValue(int[] indices) { return 0; }
  /**
   * Overrides DisplayComponet updateFilters to update drawing size and
   * drawing image.
   */
  public void updateFilters() {
    updateDrawImageSize();
    updateDrawImage();
    super.updateFilters(); // calls updateSize()
  }
  /**
   * Calculates the current needed component size.
   */
  public void updateSize() {
    synchronized (drawingLock) {
      if(drawImage != null) super.updateSize();
      else setPreferredSize(drawImageSize);
    }
  }
  /**
   * Calculate the current needed drawing size.
   */
  abstract public void updateDrawImageSize();
  /**
   * Draws to a graphics context.
   */
  abstract public void paintDrawing(Graphics g);
  /**
   * sets the drawing image needed size.
   */
  protected void setDrawImageSize(int width, int height) {
    synchronized (drawingLock) {
      drawImageSize.setSize(width, height);
    }
  }
  /**
   * Restricts a box to fit on the image producer.
   *
   * @param cropBox	box to restrict
   * @return		box that intersects image producer
   */
  public Rectangle restrictCropBox(Rectangle cropBox) {
    if(cropBox == null) return cropBox;
    return cropBox.intersection(new Rectangle(drawImageSize));
  }
  /**
   * Updates the drawing image.
   */
  private void updateDrawImage() {
    synchronized (drawingLock) {
      setImage(null); // resets image and imageproducer to null
      if(! filtersNeeded()) drawImage = null;
      else {
        if(drawImage != null) {
	  if( (drawImageSize.width != drawImage.getWidth(null)) ||
	      (drawImageSize.height != drawImage.getHeight(null)) )
		drawImage = null;
        }
        if(drawImage == null) {
	  setFilterChanged();
	  drawImage = createImage(drawImageSize.width, drawImageSize.height);
        }
        if(drawImage != null) {
	  Graphics g = drawImage.getGraphics();
          // some paint algorithms have trouble if clipRect is not set
	  g.clipRect(0, 0, drawImageSize.width, drawImageSize.height);
	  g.clearRect(0, 0, drawImageSize.width, drawImageSize.height);
	  // paint the image background white and the object black
	  // to make ShapeColorFilter work on PC
	  g.setColor(CLEAR_COLOR);
	  g.fillRect(0, 0, drawImageSize.width, drawImageSize.height);

	  Color c = getForeground();
	  setForeground(SOLID_COLOR);
	  paintDrawing(g);
	  g.dispose(); g = null;
	  setForeground(c);
	  // Get the image producer
       	  ImageProducer ip = drawImage.getSource();
	  // Color filter to clarify background
	  ShapeColorFilter scf = new ShapeColorFilter(c, Color.black);
	  ip = new FilteredImageSource(ip, scf);

	  setImageProducer(ip); // resets imageproducer
	  //  setImageLocation(2, 2); // don't know why offset is needed
        }
      }
    }
  }
  /**
   * Paints the image to the graphics context.
   *
   * @param g	graphics context
   */
  public void paint(Graphics g) {
    synchronized (drawingLock) {
      if(drawImage != null) super.paint(g);
      else paintDrawing(g);
    }
  }
}
