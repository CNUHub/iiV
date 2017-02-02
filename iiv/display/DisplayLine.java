package iiv.display;
import iiv.script.*;
import java.awt.*;
import java.awt.image.*;
/**
 * Component to display a line.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		DisplayShape
 * @since	iiV1.0
 */
public class DisplayLine extends DisplayShape {
  private static final long serialVersionUID = -4082953719373617585L;
  public final static int SIMPLE_ARROW = 1;
  static private Object defaultsLock = new Object();
  static private int defaultLineLength = 100;
  static private int defaultStartArrow = NONE;
  static private int defaultEndArrow = NONE;
  static private int defaultArrowLength = 24; // points
  static private int defaultArrowWidth = 16; // points

  private Object lineLock = new Object();
  private int lineLength = 1;
  private int startArrow = NONE;
  private int endArrow = NONE;
  private int arrowLength = 24; 
  private int arrowWidth = 16; // points

  /**
   * Constructs a new instance of DisplayLine with all defaults.
   */
  public DisplayLine() {
    synchronized (defaultsLock) {
      lineLength = defaultLineLength;
      startArrow = defaultStartArrow;
      endArrow = defaultEndArrow;
      arrowLength = defaultArrowLength; 
      arrowWidth = defaultArrowWidth;
    }
  }
  /**
   * Construct a new instance of DisplayLine with given length.
   *
   * @param lineLength	length of line in pixels
   */
  public DisplayLine(int lineLength) {
    this();
    setLineLength(lineLength);
  }
  /**
   * Construct a new instance of DisplayLine with simple options.
   *
   * @param lineLength	length of line in pixels
   * @param thickness	line thickness in pixels
   * @param lineType	SOLID or DASH
   */
  public DisplayLine(int lineLength, int thickness, int lineType) {
    super(thickness, lineType, false);
    synchronized (defaultsLock) {
      startArrow = defaultStartArrow;
      endArrow = defaultEndArrow;
      arrowLength = defaultArrowLength; 
      arrowWidth = defaultArrowWidth;
    }
    setLineLength(lineLength);
  }
  /**
   * Constructs a new instance of DisplayLine with arrow options.
   *
   * @param lineLength	length of line in pixels
   * @param thickness	line thickness in pixels
   * @param lineType	SOLID or DASH
   * @param rightArrowType	the right arrow type (SIMPLE_ARROW or NONE)
   * @param leftArrowType	the left arrow type (SIMPLE_ARROW or NONE)
   * @param arrowLength	length in pixels of arrow(s) along the line
   * @param arrowWidth	width in pixels of arrow(s)
   */
  public DisplayLine(int lineLength, int thickness, int lineType,
		     int rightArrowType, int leftArrowType,
		     int arrowLength, int arrowWidth) {
    super(thickness, lineType, false);
    setLineLength(lineLength);
    setRightArrow(rightArrowType);
    setLeftArrow(leftArrowType);
    setArrowLength(arrowLength);
    setArrowWidth(arrowWidth);
  }
  /**
   * Sets the default line length.
   *
   * @param length	new default line length in pixels
   */
  static public void setDefaultLineLength(int length) {
    synchronized (defaultsLock) {
      if(length >= 0) defaultLineLength = length;
    }
  }
  /**
   * Gets the default line length.
   *
   * @return	default line length in pixels
   */
  static public int getDefaultLineLength() {
    synchronized (defaultsLock) {
      return defaultLineLength;
    }
  }
  /**
   * Sets the line length.
   *
   * @param length	new line length in pixels
   */
  public void setLineLength(int length) {
    synchronized (lineLock) {
      if((length < 0) || (length == lineLength)) return;
      lineLength = length;
    }
    invalidateFilters();
  }
  /**
   * Gets the line length.
   *
   * @return	line length in pixels
   */
  public int getLineLength() {
    synchronized (lineLock) {
      return lineLength;
    }
  }
  /**
   * Sets default the arrow length.
   *
   * @param length	new default arrow length in pixels along the line
   */
  static public void setDefaultArrowLength(int length) {
    synchronized (defaultsLock) {
      if(length >= 0) defaultArrowLength = length;
    }
  }
  /**
   * Gets the default arrow length.
   *
   * @return	default arrow length in pixels along the line
   */
  static public int getDefaultArrowLength() {
    synchronized (defaultsLock) {
      return defaultArrowLength;
    }
  }
  /**
   * Sets the arrow length.
   *
   * @param length new arrow length in pixels along the line
   */
  public void setArrowLength(int length) {
    synchronized (lineLock) {
      if((length < 0) || (length == arrowLength)) return;
      arrowLength = length;
    }
    invalidateFilters();
  }
  /**
   * Gets the arrow length.
   *
   * @return	arrow length in pixels along line
   */
  public int getArrowLength() {
    synchronized (lineLock) {
      return arrowLength;
    }
  }
  /**
   * Sets the default arrow width.
   *
   * @param width	new default arrow width in pixels
   */
  static public void setDefaultArrowWidth(int width) {
    synchronized (defaultsLock) {
      if(width >= 0) defaultArrowWidth = width;
    }
  }
  /**
   * Gets the default arrow width.
   *
   * @return	default arrow width in pixels
   */
  static public int getDefaultArrowWidth() {
    synchronized (defaultsLock) {
      return defaultArrowWidth;
    }
  }
  /**
   * Sets the arrow width.
   *
   * @param width	new arrow width in pixels
   */
  public void setArrowWidth(int width) {
    synchronized (lineLock) {
      if((width < 0) || (width == arrowWidth)) return;
      arrowWidth = width;
    }
    invalidateFilters();
  }
  /**
   * Gets the arrow width.
   *
   * @return	arrow width in pixels
   */
  public int getArrowWidth() {
    synchronized (lineLock) {
      return arrowWidth;
    }
  }
  /**
   * Sets the default right arrow type.
   *
   * @param arrowType	the new default arrow type (SIMPLE_ARROW or NONE)
   */
  static public void setDefaultRightArrow(int arrowType) {
    synchronized (defaultsLock) {
      // accept only valid types
      switch (arrowType) {
      case SIMPLE_ARROW:
      case NONE:
	defaultEndArrow = arrowType;
	break;
      default:
	break;
      }
    }
  }
  /**
   * Gets the default right arrow type.
   *
   * @return	the default arrow type (SIMPLE_ARROW or NONE)
   */
  static public int getDefaultRightArrow() {
    synchronized (defaultsLock) {
      return defaultEndArrow;
    }
  }
  /**
   * Sets the right arrow type.
   *
   * @param arrowType	the new arrow type (SIMPLE_ARROW or NONE)
   */
  public void setRightArrow(int arrowType) {
    synchronized (lineLock) {
      // accept only valid types
      switch (arrowType) {
      case SIMPLE_ARROW:
      case NONE:
	break;
      default:
	return;
      }
      if(arrowType == endArrow) return;
      endArrow = arrowType;
    }
    invalidateFilters();
  }
  /**
   * Gets the right arrow type.
   *
   * @return	the arrow type (SIMPLE_ARROW or NONE)
   */
  public int getRightArrow() {
    synchronized (lineLock) {
      return endArrow;
    }
  }
  /**
   * Sets the default left arrow type.
   *
   * @param arrowType	the new default arrow type (SIMPLE_ARROW or NONE)
   */
  static public void setDefaultLeftArrow(int arrowType) {
    synchronized (defaultsLock) {
      // accept only valid types
      switch (arrowType) {
      case SIMPLE_ARROW:
      case NONE:
	defaultStartArrow = arrowType;
	break;
      default:
	break;
      }
    }
  }
  /**
   * Gets the default left arrow type.
   *
   * @return	the default arrow type (SIMPLE_ARROW or NONE)
   */
  static public int getDefaultLeftArrow() {
    synchronized (defaultsLock) {
      return defaultStartArrow;
    }
  }
  /**
   * Sets the left arrow type.
   *
   * @param arrowType	the new arrow type (SIMPLE_ARROW or NONE)
   */
  public void setLeftArrow(int arrowType) {
    synchronized (lineLock) {
      // accept only valid types
      switch (arrowType) {
      case SIMPLE_ARROW:
      case NONE:
	break;
      default:
	return;
      }
      if(startArrow == arrowType) return;
      startArrow = arrowType;
    }
    invalidateFilters();
  }
  /**
   * Gets the left arrow type.
   *
   * @return	the arrow type (SIMPLE_ARROW or NONE)
   */
  public int getLeftArrow() {
    synchronized (lineLock) {
      return startArrow;
    }
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
    String variableName = scriptedObjects.get(this);
    if(variableName == null) {
      variableName = scriptedObjects.addObject(this, "displayline");
      sb.append(variableName);
      sb.append(" = new ").append(className).append("(").append(getLineLength()).append(");\n");
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
    String objectVariableName = scriptedObjects.get(this);
    StringBuffer sb = new StringBuffer();
    sb.append(objectVariableName);
    sb.append(".setLineThickness(").append(getLineThickness()).append(");\n");
    sb.append(objectVariableName).append(".setLeftArrow(").append(className);
    switch(getLeftArrow()) {
      case SIMPLE_ARROW:
	  sb.append(".SIMPLE_ARROW);\n");
	  break;
      case NONE:
      default:
	  sb.append(".NONE);\n");
	  break;
    }
    sb.append(objectVariableName).append(".setRightArrow(").append(className);
    switch(getRightArrow()) {
      case SIMPLE_ARROW:
	  sb.append(".SIMPLE_ARROW);\n");
	  break;
      case NONE:
      default:
	  sb.append(".NONE);\n");
	  break;
    }
    sb.append(objectVariableName).append(".setArrowLength(").append(getArrowLength()).append(");\n");
    sb.append(objectVariableName).append(".setArrowWidth(").append(getArrowWidth()).append(");\n");
    sb.append(super.postObjectToScript(scriptedObjects));
    return sb.toString();
  }
  /**
   * Gets a string for showpoint.
   *
   * @return	name for this object
   */
  public String getName() {
    return "a line";
  }
  /**
   * Calculates the current needed size.
   */
  public void updateDrawImageSize() {
    synchronized (lineLock) {
      int width = getLineLength();
      width += 2 * border;
      int height = getLineThickness();
      if((startArrow != NONE) || (endArrow != NONE)) height += arrowWidth;
      height += 2 * border;
      setDrawImageSize(width, height);
    }
  }
  /**
   * Draws the line to a graphics context.
   *
   * @param g	graphics context to draw on
   */
  public void paintDrawing(Graphics g) {
    synchronized (lineLock) {
      g.setColor(getForeground());
      int thickness = getLineThickness();
      for(int i = 0; i < thickness; i++) {
        int startx = border;
        int starty = i + border;
        if((startArrow != NONE) || (endArrow != NONE)) {
	  int arrowHalfWidth = arrowWidth/2;
	  starty += arrowHalfWidth;
          int[] xPoints = new int[3];
          int[] yPoints = new int[3];
          if(startArrow != NONE) {
	    // point 1 = startPoint
	    xPoints[1] = startx;
	    yPoints[1] = starty;
	    // point 0
	    xPoints[0] = xPoints[1] + arrowLength;
	    yPoints[0] = yPoints[1] + arrowHalfWidth - 1;
	    xPoints[2] = xPoints[1] + arrowLength;
	    yPoints[2] = yPoints[1] - arrowHalfWidth + 1;
	    if(getFillMode()) g.fillPolygon(xPoints, yPoints, xPoints.length);
	    else g.drawPolyline(xPoints, yPoints, xPoints.length);
	  }
          if(endArrow != NONE) {
	    // point 1 = startPoint
	    xPoints[1] = startx + lineLength;
	    yPoints[1] = starty;
	    // point 0
	    xPoints[0] = xPoints[1] - arrowLength;
	    yPoints[0] = yPoints[1] + arrowHalfWidth - 1;
	    xPoints[2] = xPoints[1] - arrowLength;
	    yPoints[2] = yPoints[1] - arrowHalfWidth + 1;
	    if(getFillMode()) g.fillPolygon(xPoints, yPoints, xPoints.length);
            else g.drawPolyline(xPoints, yPoints, xPoints.length);
	  }
        }
        if(getLineType() == DASH) {
	  int dashLength = getDashLength();
	  int dashSpace = getDashSpace();
	  drawDashedLine(g, startx, starty, startx + lineLength-1, starty,
			 dashLength, dashSpace, 0);
	}
	else g.drawLine(startx, starty, startx + lineLength-1, starty);
      }
    }
  }
}


/* reference garbage
        double halfWidth = ((double)arrowWidth) * .5;
        double[] unitDirection = new double[2];
        unitDirection[0] = (double)(startPoint.x - endPoint.x);
        unitDirection[1] = (double)(startPoint.y - endPoint.y);
        double length = Math.sqrt((unitDirection[0] * unitDirection[0]) +
				(unitDirection[1] * unitDirection[1]));
        unitDirection[0] /= length;  // length never 0
        unitDirection[1] /= length;
        // rotate by 90 degrees to get perpendicular offset
        double[] perpOff = new double[2];
        perpOff[0] = unitDirection[1] * halfWidth;
        perpOff[1] = -unitDirection[0] * halfWidth;
        double[] arrowBase = new double[2];
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];
        if(startArrow != NONE) {
	  arrowBase[0] = startPoint.x + arrowLength * unitDirection[0];
	  arrowBase[1] = startPoint.y + arrowLength * unitDirection[1];
	  // pt0 = arrowBase + perpOff;
	  xPoints[0] = (int) Math.round(arrowBase[0] + perpOff[0]);
	  yPoints[0] = (int) Math.round(arrowBase[1] + perpOff[1]);
	  // pt1 = startPoint
	  xPoints[1] = startPoint.x; yPoints[1] = startPoint.y;
 	  // pt2 = arrowBase - perpOff;
	  xPoints[2] = (int) Math.round(arrowBase[0] - perpOff[0]);
	  yPoints[2] = (int) Math.round(arrowBase[1] - perpOff[1]);
	  g.drawPolyline(xPoints, yPoints, xPoints.length);
        }
        if(endArrow != NONE) {
	  arrowBase[0] = startPoint.x - arrowLength * unitDirection[0];
	  arrowBase[1] = startPoint.y - arrowLength * unitDirection[1];
	  // pt0 = arrowBase + perpOff;
	  xPoints[0] = (int) Math.round(arrowBase[0] + perpOff[0]);
	  yPoints[0] = (int) Math.round(arrowBase[1] + perpOff[1]);
	  // pt1 = startPoint
	  xPoints[1] = endPoint.x; yPoints[1] = endPoint.y;
 	  // pt2 = arrowBase - perpOff;
	  xPoints[2] = (int) Math.round(arrowBase[0] - perpOff[0]);
	  yPoints[2] = (int) Math.round(arrowBase[1] - perpOff[1]);
	  g.drawPolyline(xPoints, yPoints, xPoints.length);
        }
      }
*/
