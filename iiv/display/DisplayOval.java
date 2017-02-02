package iiv.display;
import iiv.script.*;
import java.awt.*;
import java.awt.image.*;
import java.lang.reflect.*;
/**
 * Component to display an oval.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		DisplayShape
 * @since	iiV1.0
 */
public class DisplayOval extends DisplayShape implements iiVScriptable {
  private static final long serialVersionUID = 5200619208531703969L;
  private Object ovalLock = new Object();
  private int ovalWidth = 1;
  private int ovalHeight = 1;

  /**
   * Construct a new instance of DisplayOval.
   *
   * @param width	width of oval in pixels
   * @param height	height of oval in pixels
   */
  public DisplayOval(int width, int height) {
    if(width >= 1) this.ovalWidth = width;
    if(height >= 1) this.ovalHeight = height;
  }
  /**
   * Gets the oval width.
   *
   * @return	oval width in pixels
   */
  public int getOvalWidth() {
    return ovalWidth;
  }
  /**
   * Gets the oval height.
   *
   * @return 	oval height in pixels
   */
  public int getOvalHeight() {
    return ovalHeight;
  }
  /**
   * Gets a string for showpoint.
   *
   * @return	name for this object
   */
  public String getName() {
    return "an oval";
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
      sb.append(" = new ").append(className).append("(").append(getOvalWidth()).append(", ");
      sb.append(getOvalHeight()).append(");\n");
      sb.append(postObjectToScript(scriptedObjects));
    }
    sb.append("script_rtn=").append(variableName).append(";\n");
    sb.append("// -- end ").append(className).append(" script\n");
    return sb.toString();
  }
  /**
   * Calculates the current needed size.
   */
  public void updateDrawImageSize() {
    synchronized (ovalLock) {
      setDrawImageSize(ovalWidth + 2*border, ovalHeight + 2*border);
    }
  }
  /**
   * Overrides filtersNeeded() return true when drawing clear color.
   *
   * @return	<code>true</code> if filters are needed
   */
  public boolean filtersNeeded() {
    if((getLineThickness() > 1) && ! getFillMode()) return true;
    if(getLineType() == DASH) return true;
    else return super.filtersNeeded();
  }
  /**
   * Draws the oval to a graphics context.
   *
   * @param g	graphics context to draw on
   */
  public void paintDrawing(Graphics g) {
    synchronized (ovalLock) {
      g.setColor(getForeground());
      if(getFillMode()) {
	g.fillOval(border, border, ovalWidth, ovalHeight);
      }
      else if(getLineType() == DASH) {
	double radius = Math.max(ovalWidth, ovalHeight)/2;
	int arcLength = (int) Math.round(
				(180 * getDashLength())/(radius * Math.PI));
	int arcSpace = (int) Math.round(
				(180 * getDashSpace())/(radius * Math.PI));
	int arcStep = arcLength + arcSpace;
        int thickness = getLineThickness();
	if(thickness == 1) {
	  int arcStart = 0;
	  for(; (arcStart+arcLength) < 360;
	      arcStart += arcStep) {
	    g.drawArc(border, border, ovalWidth, ovalHeight,
		      arcStart, arcLength);
          }
	  // don't leave to large of space
	  if(arcStart < 360) {
	    arcLength = 360 - arcStart - arcSpace;
	    if(arcLength > (arcSpace/2))
	      g.drawArc(border, border, ovalWidth, ovalHeight,
		        arcStart, arcLength);
	  }
	}
	else {
	  int arcStart = 0;
	  for(; (arcStart+arcLength) < 360;
	      arcStart += arcStep) {
	    g.fillArc(border, border, ovalWidth, ovalHeight,
		      arcStart, arcLength);
	  }
	  // don't leave to large of space
	  if(arcStart < 360) {
	    arcLength = 360 - arcStart - arcSpace;
	    if(arcLength > (arcSpace/2))
	      g.fillArc(border, border, ovalWidth, ovalHeight,
		        arcStart, arcLength);
	  }
	  g.setColor(CLEAR_COLOR);
	  g.fillOval(border + thickness, border + thickness,
		     ovalWidth - (2 * thickness),
		     ovalHeight - (2 * thickness));
	}
      }
      else {
        int thickness = getLineThickness();
	if(thickness == 1)
	  g.drawOval(border, border, ovalWidth, ovalHeight);
	else {
	  g.fillOval(border, border, ovalWidth, ovalHeight);
	  g.setColor(CLEAR_COLOR);
	  g.fillOval(border + thickness, border + thickness,
		     ovalWidth - (2 * thickness),
		     ovalHeight - (2 * thickness));
	}
      }
    }
  }
}
