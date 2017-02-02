package iiv.display;
import iiv.script.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
/**
 * Component to display text.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		DisplayDraw
 * @since	iiV1.0
 */
public class DisplayText extends DisplayDraw implements iiVScriptable {
  private static final long serialVersionUID = 8429171622586130072L;
  Object textLock = new Object();
  private String text = null;
  private int numberOfLines = 0;
  private String lineText[] = null;
  private int justification = LEFT;
  private Dimension textSize = new Dimension(0, 0);
  private int border = 5;
  public static final int LEFT = 0;
  public static final int RIGHT = 1;
  public static final int CENTERED = 2;

  /**
   * Constructs a new instance of DisplayText.
   *
   * @param Text	text to be displayed.  May include returns ("\n")
   *			to display multiple lines.
   */
  public DisplayText( String Text ) {
    initText(Text);
  }
  /**
   * Initializes the display text
   *
   * @param Text	text to be displayed.  May include returns ("\n")
   *			to display multiple lines.
   */
  private void initText(String Text) {
    if(Text == null) return;
    synchronized (textLock) {
      if(! Text.equals(text)) {
	if(this.text != null) {
 	  invalidateFilters();
	  setFilterChanged();
	}
        this.text = Text;
        numberOfLines = 1;
        int index = Text.indexOf('\n');
        while(index > 0) {
          numberOfLines++;
          index = Text.indexOf('\n', index+1);
        }
        lineText = new String[numberOfLines];
        int nline = 0;
        int startIndex = 0;
        index = Text.indexOf('\n');
        while(index > 0 && nline < numberOfLines) {
          lineText[nline] = Text.substring(startIndex, index);
          startIndex = index + 1;
          index = Text.indexOf('\n', startIndex);
          nline++;
        }
        if( nline < numberOfLines ) lineText[nline]= Text.substring(startIndex);
      }
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
      variableName = scriptedObjects.addObject(this, "displaytext");
      String str="";
      synchronized (textLock) {
	if(lineText != null) {
	  int nline = 0;
	  str = lineText[nline];
	  nline++;
	  while(nline < lineText.length) {
	    str += "\\" + "n" + lineText[nline];
	    nline++;
	  }
	}
      }
      sb.append(variableName);
      sb.append(" = new ").append(className).append("(\"").append(str).append("\");\n");
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
    sb.append(objectVariableName).append(".setJustification(").append(className);
    switch(getJustification()) {
    default:
    case LEFT:
      sb.append(".LEFT);\n");
      break;
    case CENTERED:
      sb.append(".CENTERED);\n");
      break;
    case RIGHT:
      sb.append(".RIGHT);\n");
      break;
    }
    sb.append(super.postObjectToScript(scriptedObjects));
    return sb.toString();
  }
  /**
   * Convert a justification value to a script string for setting the default.
   *
   * @param  justification	one of LEFT, RIGHT or CENTERED
   * @return			string represent the justfication
   */
  final static public String justificationToScript(int justification) {
    switch(justification) {
    case CENTERED:
      return "justification(\"centered\");\n";
    case RIGHT:
      return "justification(\"right\");\n";
    default:
      return "justification(\"left\");\n";
    }
  }
 /**
  * Gets a string for showpoint.
  *
  * @return	a name for this component
  */
  public String getName() {
    synchronized (textLock) {
      return text;
    }
  }
  /**
   * Sets the text string.
   *
   * @param text 	the text to display
   */
  public void setText(String text) {
    initText(text);
  }
  /**
   * Gets the text string.
   *
   * @return	the text displayed
   */
  public String getText() {
    synchronized (textLock) {
      return text;
    }
  }
  /**
   * Sets the justification.
   *
   * @param justification	one of LEFT, RIGHT or CENTERED
   */
  public void setJustification(int justification) {
    synchronized (textLock) {
      if(justification != this.justification) {
	invalidateFilters();
	setFilterChanged();
        this.justification = LEFT;
        switch(justification) {
        case RIGHT:
          this.justification = RIGHT;
          break;
        case CENTERED:
          this.justification = CENTERED;
          break;
        default:
          break;
        }
      }
    }
  }
  /**
   * Gets the justification.
   *
   * @return	one of LEFT, RIGHT or CENTERED
   */
  public int getJustification() {
    synchronized (textLock) {
      return justification;
    }
  }
  /**
   * Calculates the current needed size.
   */
  public void updateDrawImageSize() {
    synchronized (textLock) {
      //      FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(getFont());
      FontMetrics fm = getFontMetrics(getFont());
      int nline = 0;
      textSize.height = fm.getHeight();
      textSize.width = fm.stringWidth(lineText[nline]);
      nline++;
      while( nline < lineText.length ) {
        textSize.height += fm.getHeight();
        textSize.width = Math.max(textSize.width,
				  fm.stringWidth(lineText[nline]));
        nline++;
      }
      // add room for error
      setDrawImageSize(textSize.width + 2*border, textSize.height + 2*border); 
    }
  }
  /**
   * Draws the text to a graphics context.
   *
   * @param g	the graphics context to draw on
   */
  public void paintDrawing(Graphics g) {
    synchronized (textLock) {
      g.setColor(getForeground());
      g.setFont(getFont());
      //      FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(getFont());
      FontMetrics fm = getFontMetrics(getFont());
      int nline = 0;
      int xStart = border;  
      int yStart = border;
      while(nline < lineText.length) {
	xStart = border; // defaults to left justification
        switch(justification) {
          case RIGHT:
            xStart += textSize.width - fm.stringWidth(lineText[nline]);
            break;
          case CENTERED:
            xStart += (textSize.width - fm.stringWidth(lineText[nline]))/2;
            break;
          default:
	    break;
        }
        g.drawString(lineText[nline], xStart,
		     yStart + fm.getLeading() + fm.getAscent());
        yStart += fm.getHeight();
        nline++;
      }
    }
  }
}
