package iiv.display;
import iiv.script.*;
import java.awt.*;
import java.awt.image.*;
/**
 * Abstract class representing basics for displaying a shape.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		DisplayDraw
 * @since	iiV1.0
 */
public abstract class DisplayShape extends DisplayDraw
  implements iiVScriptable {
  public final static int SOLID = 0;
  public final static int DASH = 1;
  public final static int NONE = 0;

  static private Object defaultsLock = new Object();
  static private int defaultThickness = 3;
  static private int defaultLineType = SOLID;
  static private int defaultDashLength = 10;
  static private int defaultDashSpace = 4;
  static private boolean defaultFillMode = false;

  private Object parameterLock = new Object();
  private int thickness = 3;
  private int lineType = SOLID;
  private int dashLength = 10;
  private int dashSpace = 4;
  private boolean fillMode = false;
  static final int border = 3;

  /**
   * Constructs a new instance of DisplayShape.
   */
  public DisplayShape() {
    synchronized (defaultsLock) {
      thickness = defaultThickness;
      lineType = defaultLineType;
      dashLength = defaultDashLength;
      dashSpace = defaultDashSpace;
      fillMode = defaultFillMode;
    }
  }
  /**
   * Constructs a new instance of DisplayShape with nondefault parameters.
   *
   * @param thickness	line thickness
   * @param lineType	line type (SOLID or DASH)
   * @param fillMode	<code>true</code> to fill shape
   */
  public DisplayShape(int thickness, int lineType, boolean fillMode) {
    this();
    setLineThickness(thickness);
    setLineType(lineType);
    setFillMode(fillMode);
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

    sb.append(objectVariableName);
    sb.append(".setLineThickness(").append(getLineThickness()).append(");\n");

    sb.append(objectVariableName);
    switch(getLineType()) {
    case DASH:
      sb.append(".setLineType(").append(className).append(".DASH);\n");
      break;
    default:
      sb.append(".setLineType(").append(className).append(".SOLID);\n");
      break;
    }

    sb.append(objectVariableName);
    sb.append(".setDashLength(").append(getDashLength()).append(");\n");

    sb.append(objectVariableName);
    sb.append(".setDashSpace(").append(getDashSpace()).append(");\n");

    sb.append(objectVariableName);
    sb.append(".setFillMode(").append(getFillMode()).append(");\n");

    sb.append(super.postObjectToScript(scriptedObjects));
    return sb.toString();
  }
  /**
   * Sets the default line thickness.
   *
   * @param thickness	new default line thickness
   */
  static public void setDefaultLineThickness(int thickness) {
    synchronized (defaultsLock) {
      if((thickness >= 0) && (thickness <= 10)) defaultThickness = thickness;
    }
  }
  /**
   * Gets the default line thickness.
   *
   * @return	default line thickness
   */
  static public int getDefaultLineThickness() {
    synchronized (defaultsLock) {
      return defaultThickness;
    }
  }
  /**
   * Sets the line thickness.
   *
   * @param thickness	new line thickness
   */
  public void setLineThickness(int thickness) {
    synchronized (parameterLock) {
      if((thickness >= 0) && (thickness <= 10)) this.thickness = thickness;
    }
  }
  /**
   * Gets the line thickness.
   *
   * @return	the line thickness
   */
  public int getLineThickness() {
    synchronized (parameterLock) {
      return thickness;
    }
  }
  /**
   * Sets default the line type.
   *
   * @param type	line type one of SOLID or DASH
   */
  static public void setDefaultLineType(int type) {
    synchronized (defaultsLock) {
      switch (type) {
	case DASH:
	  defaultLineType = type;
	  break;
	case SOLID:
	default:
	  defaultLineType = type;
	  break;
      }
    }
  }
  /**
   * Sets the default line type from a string.
   *
   * @param type	line type "SOLID" or "DASH"
   */
  public void setDefaultLineType(String type) {
    synchronized (defaultsLock) {
      if("dash".equalsIgnoreCase(type)) defaultLineType = DASH;
      else defaultLineType = SOLID;
    }
  }
  /**
   * Gets the default line type.
   *
   * @return	SOLID or DASH
   */
  public int getDefaultLineType() {
    synchronized (defaultsLock) {
      return defaultLineType;
    }
  }
  /**
   * Sets the line type.
   *
   * @param type SOLID or DASH
   */
  public void setLineType(int type) {
    synchronized (parameterLock) {
      switch (type) {
	case DASH:
	  lineType = DASH;
	  break;
	case SOLID:
	default:
	  lineType = SOLID;
	  break;
      }
    }
  }
  /**
   * Sets the line type from a string.
   *
   * @param type	"SOLID" or "DASH"
   */
  public void setLineType(String type) {
    synchronized (parameterLock) {
      if("dash".equalsIgnoreCase(type)) lineType = DASH;
      else lineType = SOLID;
    }
  }
  /**
   * Gets the line type.
   *
   * @return	line type
   */
  public int getLineType() {
    synchronized (parameterLock) {
      return lineType;
    }
  }
  /**
   * Sets the default fill mode.
   *
   * @param mode	<code>true</code> to fill shape
   */
  static public void setDefaultFillMode(boolean mode) {
    synchronized (defaultsLock) {
      defaultFillMode = mode;
    }
  }
  /**
   * Gets the default fill mode.
   *
   * @return	<code>true</code> if defaults to shape filled
   */
  static public boolean getDefaultFillMode() {
    synchronized (defaultsLock) {
      return defaultFillMode;
    }
  }  
  /**
   * Sets the fill mode.
   *
   * @param mode	<code>true</code> to fill shape
   */
  public void setFillMode(boolean mode) {
    synchronized (parameterLock) {
      fillMode = mode;
    }
  }
  /**
   * Gets the fill mode.
   *
   * @return	<code>true</code> if shape filled
   */
  public boolean getFillMode() {
    synchronized (parameterLock) {
      return fillMode;
    }
  }
  /**
   * Sets the default length of line dashes.
   *
   * @param dashLength	length of solid part of dashes
   */
  static public void setDefaultDashLength(int dashLength) {
    synchronized (defaultsLock) {
      if(dashLength >= 1) defaultDashLength = dashLength;
    }
  }
  /**
   * Gets the default length of line dashes.
   *
   * @return	length of solid part of dashes
   */
  static public int getDefaultDashLength() {
    synchronized (defaultsLock) {
      return defaultDashLength;
    }
  }
  /**
   * Sets the length of line dashes.
   *
   * @param dashLength	length of solid part of dashes
   */
  public void setDashLength(int dashLength) {
    synchronized (parameterLock) {
      if(dashLength >= 1) this.dashLength = dashLength;
    }
  }    
  /**
   * Gets the length of line dashes.
   *
   * @return	length of solid part of dashes
   */
  public int getDashLength() {
    synchronized (parameterLock) {
      return dashLength;
    }
  }
  /**
   * Sets the default length of the spaces between line dashes.
   *
   * @param dashSpace	length of spaces between solid part of line dashes
   */
  static public void setDefaultDashSpace(int dashSpace) {
    synchronized (defaultsLock) {
      if(dashSpace >= 1) defaultDashSpace = dashSpace;
    }
  }    
  /**
   * Gets the default length of the space between line dashes.
   *
   * @return	length of spaces between solid part of line dashes
   */
  static public int getDefaultDashSpace() {
    synchronized (defaultsLock) {
      return defaultDashSpace;
    }
  }
  /**
   * Sets the length of the spaces between line dashes.
   *
   * @param dashSpace	length of spaces between solid part of line dashes
   */
  public void setDashSpace(int dashSpace) {
    synchronized (parameterLock) {
      if(dashSpace >= 1) this.dashSpace = dashSpace;
    }
  }    
  /**
   * Gets the length of the space between line dashes.
   *
   * @return	length of spaces between solid part of line dashes
   */
  public int getDashSpace() {
    synchronized (parameterLock) {
      return dashSpace;
    }
  }
  /**
   * Draws a dashed line and returns the length of undrawn dash.  The returned
   * undrawn length can be used as a dashStart for another call to
   * drawDashedLine to keep dashes consistent between connected lines.
   *
   * @param g		graphics context to draw to
   * @param xbeg	beginning x position draw line at
   * @param ybeg	beginning y position draw line at
   * @param xend	ending x position draw line to
   * @param yend	ending y position draw line to
   * @param dashLength	length of solid part of dashed line
   * @param dashSpace	length between solid parts of dashed line
   * @param dashStart	length of first dash to draw
   * @return		remaining length not drawn of last dash drawn
   */
  static public int drawDashedLine(Graphics g, int xbeg, int ybeg,
	                            int xend, int yend,
		 		    int dashLength, int dashSpace,
				    int dashStart) {
    if( (dashSpace <= 0) || (dashLength <= 0) ||
        ((xbeg == xend) && (ybeg == yend)) ) {
      g.drawLine(xbeg, ybeg, xend, yend);
      return 0;
    }

    int startSkip = dashStart;
    int nextLength = dashLength;

    if(dashStart < 0) {
      startSkip = 0;
      nextLength = -dashStart;
      if(nextLength > dashLength) nextLength = dashLength;
    }
    else if(startSkip > dashSpace) startSkip = 0;

    if(xbeg == xend) {
      // vertical line
      if(ybeg < yend) {
	ybeg += startSkip;
	int ye = yend;
	for(int y = ybeg; y < yend; ) {
	  ye = Math.min(y + nextLength, yend);
	  g.drawLine(xbeg, y, xend, ye);
	  nextLength -= (ye - y);
	  if(nextLength > 0) return(-nextLength);
	  nextLength = dashLength;
	  y = ye + dashSpace + 1;
	}
	// return the length of unused space
	return(dashSpace - (yend - ye));
      }
      else {
	ybeg -= startSkip;
	int ye = yend;
	for(int y = ybeg; y > yend; ) {
	  ye = Math.max(y - nextLength, yend);
	  g.drawLine(xbeg, ye, xend, y);
	  nextLength -= (y - ye);
	  if(nextLength > 0) return(-nextLength);
	  nextLength = dashLength;
	  y = ye - dashSpace - 1;
	}
	// return the length of unused space
	return(dashSpace - (ye - yend));
      }
    }
    else if(ybeg == yend) {
      // horizontal line
      if(xbeg < xend) {
	xbeg += startSkip;
	int xe = xend;
	for(int x = xbeg; x < xend; ) {
	  xe = Math.min(x + nextLength, xend);
	  g.drawLine(x, ybeg, xe, yend);
	  nextLength -= (xe - x);
	  if(nextLength > 0) return(-nextLength);
	  nextLength = dashLength;
	  x = xe + dashSpace + 1;
	}
	// return the length of unused space
	return(dashSpace - (xend - xe));
      }
      else {
	xbeg -= startSkip;
	int xe = xend;
	for(int x = xbeg; x > xend; ) {
	  xe = Math.max(x - nextLength, xend);
	  g.drawLine(x, ybeg, xe, yend);
	  nextLength -= (x - xe);
	  if(nextLength > 0) return(-nextLength);
	  nextLength = dashLength;
	  x = xe - dashSpace - 1;
	}
	// return the length of unused space
	return(dashSpace -(xe - xend));
      }
    }
    else {
      g.drawLine(xbeg, ybeg, xend, yend);
      return(0);
    }
  }
}
