package iiv.display;
import iiv.script.*;
import java.awt.*;
import java.awt.image.*;
/**
 * DisplayBox represents a component to display a box.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		DisplayShape
 * @since	iiV1.0
 */
public class DisplayBox extends DisplayShape implements iiVScriptable {
  private static final long serialVersionUID = 475414317104372530L;
  private Object boxLock = new Object();
  private int boxWidth = 1;
  private int boxHeight = 1;

  /**
   * Constructs a new instance of DisplayBox.
   *
   * @param width	box width
   * @param height	box height
   */
  public DisplayBox(int width, int height) {
    if(width >= 1) this.boxWidth = width;
    if(height >= 1) this.boxHeight = height;
  }
  /**
   * Gets the box width.
   *
   * @return	box width
   */
  public int getBoxWidth() {
    return boxWidth;
  }
  /**
   * Gets the box height.
   *
   * @return	box height
   */
  public int getBoxHeight() {
    return boxHeight;
  }
  /**
   * Gets a string for showpoint.
   *
   * @return	string name
   */
  public String getName() {
    return "a box";
  }
  /**
   * Creates a script that may be used to recreate this display box
   * and store it in a script variable.
   *
   * @param scriptedObjects scripted objects list to add this object to.
   * @return  the script
   */
  public String toScript(CNUScriptObjects scriptedObjects) {
    String className = getClass().getName();
    StringBuffer sb = new StringBuffer("// -- start ");
    sb.append(className).append(" script\n");
    if(scriptedObjects == null) scriptedObjects = new CNUScriptObjects();
    String variableName = scriptedObjects.get(this);
    if(variableName == null) {
	variableName = scriptedObjects.addObject(this, "displaybox");
	sb.append(variableName).append(" = new ").append(className).append("(");
	sb.append(getBoxWidth()).append(", ").append(getBoxHeight()).append(");\n");
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
    synchronized (boxLock) {
      setDrawImageSize(boxWidth + 2*border, boxHeight + 2*border);
    }
  }
  /**
   * Draws the box to a graphics context.
   *
   * @param g	graphic context to dray on
   */
  public void paintDrawing(Graphics g) {
    synchronized (boxLock) {
      int startx = border;
      int starty = border;
      g.setColor(getForeground());
      if(getFillMode()) {
	g.fillRect(startx, starty, boxWidth, boxHeight);
      }
      else {
        int thickness = getLineThickness();
        for(int i = 0; i < thickness; i++) {
          if(getLineType() == DASH) {
	    int dashLength = getDashLength();
	    int dashSpace = getDashSpace();
	    int ns = 0;
	    ns = drawDashedLine(g, startx, starty + i,
		   startx + boxWidth-1, starty + i,
		   dashLength, dashSpace, ns);
	    ns = drawDashedLine(g, startx + boxWidth-1-(1 * i), starty,
		   startx + boxWidth-1-(1 * i), starty + boxHeight-1,
		   dashLength, dashSpace, ns);
	    ns = drawDashedLine(g, startx + boxWidth-1,
		  starty + boxHeight-1-(1*i),  startx,
		  starty + boxHeight-1-(1*i), dashLength, dashSpace, ns);
	    drawDashedLine(g, startx + i, starty + boxHeight-1,
			   startx + i, starty, dashLength, dashSpace, ns);
	  }
	  else 
	  {
	    g.drawRect(startx+i, starty+i, boxWidth - (2*i) - 1,
		       boxHeight - (2*i) - 1);
	  }
	}
      }
    }
  }
}
