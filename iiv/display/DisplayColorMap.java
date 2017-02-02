package iiv.display;
import iiv.data.*;
import iiv.util.*;
import iiv.script.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.text.*;
/**
 * Component to display a color map as a horizontal or vertical bar.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.CNUViewer
 * @see		java.awt.Container
 * @since	iiV1.0
 */
public class DisplayColorMap extends DisplayComponent
  implements iiVScriptable, ScaleInterface, NumberFormattable {
  private static final long serialVersionUID = -1415291893425987416L;
  private NumberFormat numberFormat =
    DisplayNumberFormat.getDefaultNumberFormat();
  private Object localLock = new Object();
  private int stripSize = 35;
  private int numberOfColors = 256;
  private byte[] colorStripData = null;
  private int horizontal_line_length = 15;
  private int vertical_line_length = 10;
  public final static int HORIZONTAL = 0;
  public final static int VERTICAL = 1;
  private int orientation = HORIZONTAL;
  private boolean ticsOn = true;
  private boolean labelsOn = true;
  private String topLeftLabel = "255";
  private String middleLabel = "127";
  private String bottomRightLabel = "0";
  private int maxLabelWidth;
  private CNUScale sc = new CNUScale(1.0);

  /**
   * Construct a new instance of DisplayColorMap.
   *
   * @param cm	Color model to display
   */
  public DisplayColorMap( ColorModel cm ) {
    colorStripData = new byte[numberOfColors * stripSize];
    if(cm != null) initColorModel(cm);
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
	variableName = scriptedObjects.addObject(this, "dcm");
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
    String className = getClass().getName();
    StringBuffer sb = new StringBuffer();
    String objectVariableName = scriptedObjects.get(this);
    synchronized (localLock) {
      sb.append(objectVariableName).append(".setOrientation(").append(className);
      if(orientation == VERTICAL) sb.append(".VERTICAL);\n");
      else sb.append(".HORIZONTAL);\n");

      sb.append(objectVariableName);
      if(getLabelsOn()) sb.append(".setLabelsOn(true);\n");
      else sb.append(".setLabelsOn(false);\n");

      if(sc == null) {
	  sb.append(objectVariableName).append(".setScale(null);\n");
      }
      else {
        sb.append(sc.toScript(scriptedObjects));
        sb.append(objectVariableName).append(".setScale(script_rtn);\n");
      }
    }
    NumberFormat nf = getNumberFormat();
    sb.append(DisplayNumberFormat.numberFormatToScript(scriptedObjects, nf));
    sb.append(objectVariableName);
    sb.append(".setNumberFormat(script_rtn);\n");
    sb.append(super.postObjectToScript(scriptedObjects));
    return sb.toString();
  }
  /**
   * Creates the raw image producer.
   */
  final private void createRawIp() {
    synchronized (localLock) {
      // build colormap strip
      for( int color = 0; color < numberOfColors; color++ ) {
        for( int strip = 0; strip < stripSize; strip++ ) {
	  int index;
	  if(orientation == HORIZONTAL)
	    index = (strip * numberOfColors) + color;
	  else index = (color * stripSize) + strip;
	  colorStripData[index] = (byte)(255 - color);
        }
      }
      int width = stripSize;
      int height = numberOfColors;
      if(orientation == HORIZONTAL) {
        width = numberOfColors;
        height = stripSize;
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
   * Sets the orientation of the color bar.
   *
   * @param orientation		HORIZONTAL or VERTICAL
   */
  public void setOrientation(int orientation) {
    synchronized (localLock) {
      if(this.orientation == orientation) return;
      this.orientation = orientation;
    }
    createRawIp();
    invalidateFilters();
  }
  /**
   * Gets the scaling object.
   *
   * @return	the scaling object
   */
  public CNUScale getScale() {
    synchronized (localLock) {
      return sc;
    }
  }
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
   * Gets the quantification factor for implementing ScaleInterface
   *
   * @return	the quantificaton factor
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
    if(orientation == HORIZONTAL) indices[0] = pt.x;
    else indices[0] = pt.y;
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
    int value = -1;
    if(indices != null) if(indices.length > 0) value = indices[0];
    Point pt = new Point(-1, -1);
    if(orientation == HORIZONTAL) pt.x = value;
    else pt.y = value;
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
   * Gets a value.
   *
   * @param indices	indices
   * @return		value to display via showpoint
   */
  public double getValue(int[] indices) {
    double value = 0;
    if(indices == null) return value;
    value = 255 - indices[0];
    CNUScale sc = this.sc;
    if(sc != null) value = sc.applyInverse(value);
    return value;
  }
/*
  public double getValue(int[] indices) {
    double value = 0;
    if(indices == null) return value;
    int index = indices[0];
    if(orientation == VERTICAL) index *= stripSize;
    synchronized (localLock) {
      if(index < 0 || index >= colorStripData.length) return value;
      if(sc == null) value = colorStripData[index];
      else {
        value =
          sc.applyInverse(
	    CNUTypes.UnsignedByteToInt(colorStripData[index]));
      }
    }
    return value;
  }
*/
  /**
   * Gets the name for showpoint.
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
      if((labelsOn != mode) || (ticsOn != mode)) {
        labelsOn = mode;
        ticsOn = mode;
        invalidate();
      }
    }
  }
  /**
   * Gets the label visible status
   *
   * @return	<code>true</code> if showing labels
   */
  public boolean getLabelsOn() {
    synchronized (localLock) {
      return labelsOn;
    }
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
    Point pt = getImageLocation();
    Dimension imageSize = getImageSize();
    synchronized (localLock) {
      // adjust point for labels on the bottom or left
      if(orientation == HORIZONTAL) pt.y += imageSize.height - 1;
      else pt.x += imageSize.width - 1;
      topLeftLabel = numberFormat.format(getValue(pt) * getFactor());
      if(orientation == HORIZONTAL) pt.x += imageSize.width - 1;
      else pt.y += imageSize.height - 1;
      bottomRightLabel = numberFormat.format(getValue(pt) * getFactor());
      if(orientation == HORIZONTAL) pt.x -= imageSize.width/2;
      else pt.y -= imageSize.height/2;
      middleLabel = numberFormat.format(getValue(pt) * getFactor());
    }
    invalidate();
  }
  /**
   * Calculates the current needed size.
   */
  public void updateSize() {
    super.updateSize();
    synchronized (localLock) {
      Dimension displayImageSize = getImageSize();
      int width = displayImageSize.width;
      int height = displayImageSize.height;
      if(ticsOn) {
        if(orientation == HORIZONTAL) height += vertical_line_length;
        else width += horizontal_line_length;
      }
      if(labelsOn) {
        updateLabels();
        FontMetrics fontM = getFontMetrics(getFont());
        if(orientation == HORIZONTAL) height += fontM.getAscent();
        else {
          maxLabelWidth = Math.max(fontM.stringWidth(topLeftLabel),
				   fontM.stringWidth(middleLabel));
          maxLabelWidth = Math.max(maxLabelWidth,
				   fontM.stringWidth(bottomRightLabel));
          width += maxLabelWidth;
        }
      }
      setPreferredSize(width, height);
    }
  }
  /**
   * Paints the image to a graphics context.
   *
   * @param g	graphics context to paint to
   */
  public void paint(Graphics g) {
    super.paint(g);
    synchronized (localLock) {
    int xloc;
    int yloc;
    Dimension d = getSize();
    g.setColor(getForeground());
    // lets adds some text and tics
      if(labelsOn) {
        g.setFont(getFont());
        FontMetrics fontM = g.getFontMetrics();
        xloc = 0;
        yloc = fontM.getAscent();
        if(orientation == HORIZONTAL) yloc = d.height;
        else xloc = d.width - maxLabelWidth;
        g.drawString(topLeftLabel, xloc, yloc);
        if(orientation == HORIZONTAL)
          xloc = d.width - fontM.stringWidth(bottomRightLabel);
        else yloc = d.height;
        g.drawString(bottomRightLabel, xloc, yloc);
        if(orientation == HORIZONTAL)
          xloc = (d.width - fontM.stringWidth(middleLabel))/ 2;
        else yloc = (yloc + fontM.getAscent())/2;
        g.drawString(middleLabel, xloc, yloc);
      }
      if(ticsOn) {
        int xend;
        int yend;
        Dimension displayImageSize = getImageSize();
        if(orientation == HORIZONTAL) {
          yloc = displayImageSize.height;
          yend = yloc +  vertical_line_length;
          xend = xloc = 0;
          g.drawLine(xloc, yloc, xend, yend);
          xend = xloc = d.width/2;
          g.drawLine(xloc, yloc, xend, yend);
          xend = xloc = d.width - 1;
          g.drawLine(xloc, yloc, xend, yend);
        }
        else {
          xloc = displayImageSize.width;
          xend = xloc + horizontal_line_length - 2;
          yend = yloc = 0;
          g.drawLine(xloc, yloc, xend, yend);
          yend = yloc = d.height/2;
          g.drawLine(xloc, yloc, xend, yend);
          yend = yloc = d.height - 1;
          g.drawLine(xloc, yloc, xend, yend);
        }
      }
    }
  }
}
