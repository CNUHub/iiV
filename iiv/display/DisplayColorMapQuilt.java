package iiv.display;
import iiv.data.*;
import iiv.util.*;
import iiv.script.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.text.*;
/**
 * Component to display a color map as a quilt.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.CNUViewer
 * @see		java.awt.Container
 * @since	iiV1.0
 */
public class DisplayColorMapQuilt extends DisplayComponent
  implements iiVScriptable, ScaleInterface, NumberFormattable {
  private static final long serialVersionUID = 427320118389668473L;
  private NumberFormat numberFormat =
    DisplayNumberFormat.getDefaultNumberFormat();
  private Object localLock = new Object();
  protected int patchSize = 16;
  protected int numberOfColors = 256;
  protected int numberOfColumns = 16;
  protected byte[] colorStripData = null;
  private int horizontal_line_length = 15;
  private int vertical_line_length = 10;
  protected boolean ticsOn = true;
  protected boolean labelsOn = true;
  protected String topLeftLabel = "";
  protected String topRightLabel = "";
  protected String bottomLeftLabel = "";
  protected String bottomRightLabel = "";
  private CNUScale sc = new CNUScale(1.0);

  /**
   * Construct a new instance of DisplayColorMapQuilt.
   *
   * @param cm	Color model to display
   */
  public DisplayColorMapQuilt( ColorModel cm ) {
    colorStripData = new byte[numberOfColors * patchSize * patchSize];
    if(cm == null) {
      byte[] grey = new byte[256];
      for(int i = 0; i < 256; i++)grey[i]=(byte)i;
      cm = new IndexColorModel(8, 256, grey, grey, grey);
    }
    setColorModel(cm);
    createRawIp();
  }
  /**
   * Creates a script that may be used to recreate this display component.
   *
   * @param scriptedObjects scripted objects list to add this object to.
   * @return  the script
   */
  public String toScript(CNUScriptObjects scriptedObjects) {
    String className = getClass().getName();
    StringBuffer sb = new StringBuffer();
    sb.append("// -- start ").append(className).append(" script\n");
    if(scriptedObjects == null) scriptedObjects = new CNUScriptObjects();
    String variableName = scriptedObjects.get(this);
    if(variableName == null) {
      variableName = scriptedObjects.addObject(this, "dcmq");
      sb.append(variableName).append(" = new ").append(className).append("(null);\n");
      sb.append(postObjectToScript(scriptedObjects));
    }
    sb.append("script_rtn=").append(variableName).append(";\n");
    sb.append("// -- end ").append(className).append(" script\n");
    return sb.toString();
  }
  /**
   * Creates a script to recreate the settings on an existing object.
   *
   * @param scriptedObjects scripted objects list this object already
   *                        exists in.
   * @return	script to recreate this component
   */
  public String postObjectToScript(CNUScriptObjects scriptedObjects) {
    StringBuffer sb = new StringBuffer();
    String objectVariableName = scriptedObjects.get(this);
    synchronized (localLock) {
      sb.append(objectVariableName);
      if(getLabelsOn()) sb.append(".setLabelsOn(true);\n");
      else sb.append(".setLabelsOn(false);\n");

      if(sc == null) {
	sb.append(objectVariableName).append(".setScale(null);\n");
      }
      else {
	sb.append(sc.toScript(null));
	sb.append(objectVariableName).append(".setScale(script_rtn);\n");
      }
    }
    NumberFormat nf = getNumberFormat();
    sb.append(DisplayNumberFormat.numberFormatToScript(scriptedObjects, nf));
    sb.append(objectVariableName).append(".setNumberFormat(script_rtn);\n");
    sb.append(super.postObjectToScript(scriptedObjects));
    return sb.toString();
  }
  /**
   * Creates the raw image producer.
   */
  private void createRawIp() {
    synchronized (localLock) {
      // build colormap strip
      int width = patchSize * numberOfColumns;
      int height = (int) (( colorStripData.length / (double) width) + 0.5);

      int i = 0;
      for(int y = 0; y < height; y++) {
        int row = y / patchSize;
        for(int x = 0; (x < width) && (i < colorStripData.length); x++, i++) {
	  int col = x / patchSize;
	  int color = row * numberOfColumns + col;
	  if(color > numberOfColors) color = 0;
	  colorStripData[i] = (byte)(255 - color);
        }
      }
      setImage(null);
      ColorModel cm = getColorModel();
      if(cm == null) cm = DisplayComponentDefaults.getDefaultColorModel();
      setImageProducer(new MemoryImageSource(width, height, cm,
					     colorStripData, 0, width));
      initColorModel(cm);
    }
  }
  /**
   * Gets the rectangle bordering a constant value area.
   *
   * @param index	value to find bordering rectangle for
   * @return		bordering rectangle
   */
  public Rectangle getIndexBorder(int index) {
    int color = 255 - index;
    synchronized (localLock) {
      int row = color / numberOfColumns;
      int col = color - (row * numberOfColumns);
      return new Rectangle(col * patchSize, row * patchSize,
			   patchSize - 1, patchSize - 1);
    }
  }
  /**
   * Gets the scaling object.
   *
   * @return	the scaling object
   */
  public CNUScale getScale() { synchronized (localLock) { return sc; } }
  /**
   * Sets the scaling object
   *
   * @param sc	the scaling object
   */
  public void setScale( CNUScale sc ) {
    synchronized (localLock) {
      this.sc = sc;
    }
    invalidate();
  }
  /**
   * Applies the scale.
   */
  public void updateScale() { }
  /**
   * Gets the data type.
   *
   * @return	CNUTypes.UNSIGNED_BYTE
   */
  public int getType() { return CNUTypes.UNSIGNED_BYTE; }
  /**
   * Gets the quantification factor.
   *
   * @return		the quantificaton factor
   */
  public double getFactor() {
    CNUScale sc = this.sc;
    if(sc != null)
      if(sc.getQuantificationState()) return sc.getQuantification();
    return 1.0d;
  }
  /**
   * Gets the indices corresponding to the original raw data this display
   * component is created from based on a point relative to the non-filtered
   * (no flip, rotation, zoom, or offset) image.
   *
   * @param pt	point 	location relative to non-filtered image
   * @return	indices indices to original raw data which may have
   *			any number of dimensions
   */
  public int[] getIndicesFromNonfilteredPoint(Point pt) {
    if(pt == null) return null;
    int[] indices = new int[1];
    synchronized (localLock) {
      int row = pt.y / patchSize;
      int col = pt.x / patchSize;
      indices[0] = row * numberOfColumns + col;
    }
    return indices;
  }
 /**
   * Gets the point relative to the non-filtered
   * (no flip, rotation, zoom, or offset) image given the indices
   * corresponding to the original raw data this display component
   * is created from.  This default implementation returns the
   * indices stored in a Point object.
   *
   * @param	indices indices to original raw data which may have
   *		any number of dimensions
   * @return	location relative to non-filtered image (negative value
   *		for x or y indicates the indice doesn't map to that dimension)
   */
  public Point getNonfilteredPoint(int[] indices) {
    Point pt = new Point(-1, -1);
    if(indices != null) if(indices.length > 0) synchronized (localLock) {
      int col = indices[0] % numberOfColumns;
      int row = indices[0] / numberOfColumns;
      pt.x = col * patchSize + patchSize / 2;
      pt.y = row * patchSize + patchSize / 2;
    }
    return pt;
  }
  /**
   * Determines if the given indices relative to the raw data are located
   * on the display image before cropping.
   *
   * @param	indices indices to original raw data which may have
   *		any number of dimensions
   * @return	<code>true</code> if indices are located on the image this component
   *		represents.
   */
  public boolean onImage(int[] indices) {
    if(indices == null) return false;
    if(indices.length < 1) return false;
    if(indices[0] < 0 || indices[0] >= 255) return false;
    for(int k = 1; k < indices.length; k++) {
      if(indices[k] != 0) return false;
    }
    return true;
  }
  /**
   * Gets a value at the given indice
   *
   * @param indices	the indices
   * @return		the value
   */
  public double getValue(int[] indices) {
    double value = 0;
    if(indices == null) return value;
    value = 255 - indices[0];
    CNUScale sc = this.sc;
    if(sc != null) value = sc.applyInverse(value);
    return value;
  }
  /**
   * Gets the name.
   *
   * @return	a name for this component
   */
  public String getName() {
    ColorModel cm = getColorModel();
    if(cm instanceof CNUColorModel) return ((CNUColorModel) cm).getName();
    else return "Unknown Color Map";
  }
  /**
   * Sets labels to be shown or hidden.
   *
   * @param mode	<code>true</code> to show labels
   */
  public void setLabelsOn(boolean mode) {
    synchronized (localLock) {
      labelsOn = mode;
      ticsOn = mode;
    }
    invalidate();
  }
  /**
   * Gets the label visible status
   *
   * @return	<code>true</code> if showing labels
   */
  public boolean getLabelsOn() {
    return labelsOn;
  }
  /**
   * Sets the number format for converting numbers to strings.
   *
   * @param numberFormat	number format tool
   */
  public void setNumberFormat(NumberFormat numberFormat) {
    if(numberFormat != null) {
      this.numberFormat = (NumberFormat) numberFormat.clone();
      updateLabels();
    }
  }
  /**
   * Gets the number format.
   *
   * @return	number format
   */
  public NumberFormat getNumberFormat() {
    return (NumberFormat) numberFormat.clone();
  }
  /**
   * Determines labels based on pixel values.
   */
  public void updateLabels() {
    synchronized (localLock) {
      Point pt = getImageLocation();
      Dimension imageSize = getImageSize();
      topLeftLabel = numberFormat.format(getValue(pt) * getFactor());
      pt.x += imageSize.width - 1;
      topRightLabel = numberFormat.format(getValue(pt) * getFactor());
      pt.x -= imageSize.width - 1;
      pt.y += imageSize.height - 1;
      bottomLeftLabel =	numberFormat.format(getValue(pt) * getFactor());
      pt.x += imageSize.width - 1;
      bottomRightLabel = numberFormat.format(getValue(pt) * getFactor());
    }
    invalidate();
  }
  /**
   * Calculates the current needed size.
   */
  public void updateSize() {
    synchronized (localLock) {
      super.updateSize();
      Dimension displayImageSize = getImageSize();
      int width = displayImageSize.width;
      int height = displayImageSize.height;
      Point imageLocation = new Point(0, 0);
      if(ticsOn) {
        height += 2 * vertical_line_length;
        imageLocation.translate(0, vertical_line_length);
      }
      if(labelsOn) {
        updateLabels();
        FontMetrics fontM = getFontMetrics(getFont());
        height += 2 * fontM.getAscent();
        imageLocation.translate(0, fontM.getAscent());
      }
      setImageLocation(imageLocation);
      setPreferredSize(width, height);
    }
  }
  /**
   * Paints the image to a graphics context.
   *
   * @param g	graphics context to paint to
   */
  public void paint(Graphics g) {
    // lets adds some text and tics
    int xloc = 0;
    int yloc = 0;
    synchronized (localLock) {
      Dimension d = getSize();
      g.setColor(getForeground());
      FontMetrics fontM = null;
      if(labelsOn) {
        g.setFont(getFont());
        fontM = g.getFontMetrics();
        yloc = +fontM.getAscent();
        g.drawString(topLeftLabel, xloc, yloc);
        xloc = d.width - fontM.stringWidth(topRightLabel);
        g.drawString(topRightLabel, xloc, yloc);
      }
      int xend;
      int yend;
      if(ticsOn) {
        xloc = 0;
        xend = xloc;
        yend = yloc;
        yloc += vertical_line_length;;
        g.drawLine(xloc, yloc, xend, yend);
        xend = xloc = d.width - 1;
        g.drawLine(xloc, yloc, xend, yend);
      }
      super.paint(g);
      yloc += getImageSize().height;
      if(ticsOn) {
        xloc = 0;
        xend = xloc;
        yend = yloc;
        yloc += vertical_line_length;;
        g.drawLine(xloc, yloc, xend, yend);
        xend = xloc = d.width - 1;
        g.drawLine(xloc, yloc, xend, yend);
      }
      if(labelsOn) {
        xloc = 0;
        yloc += fontM.getAscent();
        g.drawString(bottomLeftLabel, xloc, yloc);
        xloc = d.width - fontM.stringWidth(bottomRightLabel);
        g.drawString(bottomRightLabel, xloc, yloc);
      }
    }
  }
}
