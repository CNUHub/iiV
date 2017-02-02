package iiv.display;
import iiv.script.*;
import iiv.data.*;
import iiv.dialog.*;
import iiv.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.text.*;
/**
 * ShowPointDisplayLine displays show point info as a single formatted line.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		ShowPointDisplay
 * @see         ShowPointDialog
 * @see		CNUDisplay
 * @since	iiV1.142
 */
public class ShowPointDisplayLine extends DisplayDraw
  implements ShowPointDisplay, iiVScriptable, NumberFormattable {
  private static final long serialVersionUID = -6066555265828106250L;
  //  private ShowPointDialogInterface showPointDialog = null;
  private ShowPointDisplay recordShowPointDisplay = null;
  private int border = 4;
  private Dimension textSize = new Dimension(0, 0);
  private NumberFormat numberFormat =
    DisplayNumberFormat.getDefaultNumberFormat();

  private String name = null;
  private int[] point = null;
  private double value = 0;
  private double factor = 1.0;
  private XYZDouble xyzpt = null;
  private String mapName = null;
  private boolean freezeState = false;
  private boolean recordState = false;
  private boolean deprecatedCrosshairState = false;
  private boolean deprecatedTrackingState = false;

  private int nameRowY;
  private int startNameX;

  private int startMapNameX;
  private int mapNameRowY;

  private int pointRowY;
  private int startPointXB, startPointXE;
  private int startPointX0, startPointX1, startPointX2, startPointX3;

  private int valueRowY;
  private int startValueX, startValueXX;
  private int factorRowY;
  private int resultRowY;
  private int startFactorX, startFactorXEQ, startResultX;

  private int mappedRowY;
  private int startMappedLocationX0, startMappedLocationX1;
  private int startMappedLocationX2;

  public static Component getTitleComponent() {
    return new ShowPointDisplayLine() {
      private static final long serialVersionUID = -3995880921886515436L;
	    {
		setCrop(null); setFlips(false, false);
		setZoom(1,1); setRotation(0);
	    }
      public void paintDrawing(Graphics g) {
	g.setColor(getForeground());
	g.setFont(getFont());
	g.drawString("Name", super.startNameX, super.nameRowY);
	g.drawString("Value", super.startValueX, super.valueRowY);
	g.drawString("Factor", super.startFactorX, super.factorRowY);
	g.drawString("Result", super.startResultX, super.resultRowY);
	g.drawString("/Map Name", super.startMapNameX, super.mapNameRowY);
	g.drawString("/Indice(x,y,z,i)", super.startPointX0, super.pointRowY);
	g.drawString("/Mapped Location(x,y,z)",
		     super.startMappedLocationX0, super.mappedRowY);
      }
      public boolean showPoint(int[] point, double value,
			       double factor, String name) { return false; }
      public boolean showPoint(int[] point, double value, double factor,
			       String name,
			       XYZDouble xyzpt, String mapName) { return false; }
    };
  }
  public int[] getPoint() {
      if(point == null) return null;
      else return point.clone();
  }
  public XYZDouble getXYZPoint() {
      if(xyzpt == null) return null;
      else return new XYZDouble(xyzpt);
  }

  /**
   * Creates a new instance of ShowPointDisplayLine.
   */
  public ShowPointDisplayLine() { }
  /**
   * Creates a new instance of ShowPointDisplayLine.
   *
   * @param spd	ShowPointDialog to associate with
   * @Deprecated need for old script capatibility
   */
  public ShowPointDisplayLine(Object spd) {
    //    setLayout(null);
    //    final ShowPointDisplayLine thisspdl = this;
    //    this.showPointDialog = spd;
  }
  /**
   * Sets the number format for converting numbers to strings.
   *
   * @param numberFormat	number format tool
   */
  public void setNumberFormat(NumberFormat numberFormat) {
    if(numberFormat != null) {
      this.numberFormat = (NumberFormat) numberFormat.clone();
      invalidateFilters();
      setFilterChanged();
      //      repaint();
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
   * Updates the size of the line.  Only needs to be called when font changes.
   */
  public void updateDrawImageSize() {
    FontMetrics fm = getFontMetrics(getFont());

    int maxNumberWidth = 0;
    for(int i=0; i<10; i++)
      maxNumberWidth=Math.max(maxNumberWidth,
			      fm.stringWidth(Integer.toString(i)));
    maxNumberWidth=Math.max(maxNumberWidth, fm.charWidth('.'));
    maxNumberWidth=Math.max(maxNumberWidth, fm.charWidth(','));
    maxNumberWidth=Math.max(maxNumberWidth, fm.charWidth('E'));
    maxNumberWidth=Math.max(maxNumberWidth, fm.charWidth('-'));
    maxNumberWidth=Math.max(maxNumberWidth, fm.charWidth(' '));

    int startRowY0 = border + fm.getLeading() + fm.getAscent();
    int startRowY1 = startRowY0 + fm.getLeading() + fm.getAscent();
    // first row
    int nextx = border;
    nameRowY = startRowY0;
    startNameX = nextx;
    valueRowY = startRowY0;
    startValueX = startNameX + (21 * maxNumberWidth);
    startValueXX = startValueX + (10 * maxNumberWidth);
    factorRowY = startRowY0;
    startFactorX = startValueXX + fm.charWidth('X');
    resultRowY = startRowY0;
    startFactorXEQ = startFactorX + (10 * maxNumberWidth);
    startResultX = startFactorXEQ + fm.charWidth('=');
    int row1lastx = startResultX + (10 * maxNumberWidth) + border;

    // secondRow
    nextx = border;
    mapNameRowY = startRowY1;
    startMapNameX = nextx;
    nextx = startMapNameX  + (21 * maxNumberWidth);

    pointRowY = startRowY1;
    startPointXB = nextx;
    startPointX0 = startPointXB + fm.charWidth('(');
    startPointX1 = startPointX0 + (4 * maxNumberWidth);
    startPointX2 = startPointX1 + (4 * maxNumberWidth);
    startPointX3 = startPointX2 + (4 * maxNumberWidth);
    startPointXE = startPointX3 + (4 * maxNumberWidth);
    nextx = startPointXE + fm.charWidth(')') + maxNumberWidth;

    mappedRowY = startRowY1;
    startMappedLocationX0 = nextx;
    startMappedLocationX1 = startMappedLocationX0 + (11 * maxNumberWidth);
    startMappedLocationX2 = startMappedLocationX1 + (11 * maxNumberWidth);
    nextx = startMappedLocationX2 + (11 * maxNumberWidth) + border;
    int row2lastx = nextx;

    nextx = Math.max(row1lastx, row2lastx);

    // width from first row
    textSize.width = nextx;
    textSize.height = Math.max(startRowY1 + fm.getMaxDescent() + border,
			       textSize.height);
    setDrawImageSize(textSize.width, textSize.height);
  }
  /**
   * Returns the currently displayed text.
   *
   * @return text
   */
  public String getText() {
    StringBuffer sb = new StringBuffer();
    if(name == null) sb.append("no name");
    else sb.append(name);

    sb.append(" \t");
    if(mapName == null) sb.append("no map");
    else sb.append(mapName);

    sb.append(" \t");
    if(point == null) sb.append("(null point)");
    else if(point.length == 0) sb.append("length 0 point");
    else {
      sb.append("(");
      sb.append(FormatTools.padString(Integer.toString(point[0]),
				      3, FormatTools.LEFT));
      if(point.length > 1) {
	sb.append(",");
	sb.append(FormatTools.padString(Integer.toString(point[1]),
					3, FormatTools.LEFT));
	if(point.length > 2) {
	  sb.append(",");
	  sb.append(FormatTools.padString(Integer.toString(point[2]),
					  3, FormatTools.LEFT));
	  if(point.length > 3) {
	    sb.append(",");
	    sb.append(FormatTools.padString(Integer.toString(point[3]),
					    3, FormatTools.LEFT));
	  }
	}
      }
      sb.append(")");
    }

    sb.append(" \t");
    sb.append(numberFormat.format(value));
    sb.append(" X ");
    sb.append(numberFormat.format(factor));
    sb.append("=");
    sb.append(numberFormat.format(value * factor));

    sb.append(" \t");
    if(xyzpt == null)
      sb.append("(No Coordinate Mapping)");
    else {
      sb.append("(" + numberFormat.format(xyzpt.x * 1e3)
		+ ((xyzpt.x < 0) ? "L" : "R") + ",");
      sb.append(numberFormat.format(xyzpt.y * 1e3)
		+ ((xyzpt.y < 0) ? "P" : "A") + ",");
      sb.append(numberFormat.format(xyzpt.z * 1e3)
		+ ((xyzpt.z < 0) ? "I" : "S") + ")");
    }
    return sb.toString();
  }
  /**
   * Paints this component on the graphics context.
   *
   * @param g graphics context to paint on
   */
  public void paintDrawing(Graphics g) {
    g.setColor(getForeground());
    g.setFont(getFont());
    if(name == null) g.drawString("no name", startNameX, nameRowY);
    else {
      String str = name;
      if(str.length() > 20) str = str.substring(0, 19);
      g.drawString(str, startNameX, nameRowY);
    }

    if(mapName == null) g.drawString("no map", startMapNameX, mapNameRowY);
    else {
      String str = mapName;
      if(str.length() > 20) str = str.substring(0, 19);
      g.drawString(str, startMapNameX, mapNameRowY);
    }

    String str;
    if(point == null) g.drawString("(null point)", startPointXB, pointRowY);
    else if(point.length == 0)
      g.drawString("length 0 point", startPointXB, pointRowY);
    else {
      g.drawString("(", startPointXB, pointRowY);
      int endBracketLocation = startPointX1;
      str = FormatTools.padString(Integer.toString(point[0]),
				  3, FormatTools.LEFT);
      if(point.length > 1) str += ",";
      g.drawString(str, startPointX0, pointRowY);
      endBracketLocation = startPointX1;
      if(point.length > 1) {
	str = FormatTools.padString(Integer.toString(point[1]),
				    3, FormatTools.LEFT);
	if(point.length > 2) str += ",";
	g.drawString(str, startPointX1, pointRowY);
	endBracketLocation = startPointX2;
	if(point.length > 2) {
	  str = FormatTools.padString(Integer.toString(point[2]),
				      3, FormatTools.LEFT);
	  if(point.length > 3) str += ",";
	  g.drawString(str, startPointX2, pointRowY);
	  endBracketLocation = startPointX3;
	  if(point.length > 3) {
	  str = FormatTools.padString(Integer.toString(point[3]),
				      3, FormatTools.LEFT);
	  g.drawString(str, startPointX3, pointRowY);
	  endBracketLocation = startPointXE;
	  }
	}
      }
      g.drawString(")", endBracketLocation, pointRowY);
    }

    g.drawString(numberFormat.format(value), startValueX, valueRowY);
    g.drawString("X", startValueXX, factorRowY);
    g.drawString(numberFormat.format(factor),
		 startFactorX, factorRowY);
    g.drawString("=", startFactorXEQ, resultRowY);
    //    g.drawString(FormatTools.formatEngineering(value * factor), startResultX,
    //		 resultRowY);
    g.drawString(numberFormat.format(value * factor), startResultX,
		 resultRowY);

    if(xyzpt == null)
      g.drawString("(No Coordinate Mapping)", startMappedLocationX0, mappedRowY);
    else {
      str = "(" + numberFormat.format(xyzpt.x * 1e3)
	+ ((xyzpt.x < 0) ? "L" : "R") + ",";
      g.drawString(str, startMappedLocationX0, mappedRowY);
      str = numberFormat.format(xyzpt.y * 1e3)
	+ ((xyzpt.y < 0) ? "P" : "A") + ",";
      g.drawString(str, startMappedLocationX1, mappedRowY);
      str = numberFormat.format(xyzpt.z * 1e3)
	+ ((xyzpt.z < 0) ? "I" : "S") + ")";
      g.drawString(str, startMappedLocationX2, mappedRowY);

    }
    //    super.paint(g);
  }
  /**
   * Validates this component.
  public void validate() {
    if(! isValid()) {
      super.validate();
      updateSize();
    }
  }
   */
  /**
   * Gets the preferred size of this component.
   *
   * @return	preferred size
  public Dimension getPreferredSize() {
    validate();
    return(textSize.getSize());
  }
   */
  /**
   * Gets the minimum size of this component.
   *
   * @return	the minimum size
   public Dimension getMininumSize() { return getPreferredSize(); }
   */
  /**
   * Gets the maximum size of this component.
   *
   * @return	the maximum size
  public Dimension getMaximumSize() { return getPreferredSize(); }
   */
  /**
   * Displays information about a point.
   *
   * @param point	array of indices specifying the point location
   * @param value	value of voxel at point location
   * @param factor	quantification factor for voxel value
   * @param name	name of object source of the voxel
   * @return		<code>true</code> if info displayed,
   *			<code>false</code> otherwise
   * @see	ShowPointDialog
   */
  public boolean showPoint(int[] point, double value,
			   double factor, String name) {
    return showPoint(point, value, factor, name, null, null);
  }
  /**
   * Display information about a point including Talairach coordinates.
   *
   * @param point	array of indices specifying the point location
   * @param value	value of voxel at point location
   * @param factor	quantification factor for voxel value
   * @param name	name of object source of the voxel
   * @param xyzpt	point location in talairach coordinates
   * @param mapName	name of map used convert to talairach coordinates
   * @return		<code>true</code> if info displayed,
   *			<code>false</code> otherwise
   * @see	ShowPointDialog
   * @see	CoordinateMap
   */
  public boolean showPoint(final int[] point, final double value,
			   final double factor, final String name,
			   final XYZDouble xyzpt, final String mapName) {
    if(! SwingUtilities.isEventDispatchThread()) {
      RunnableWithReturnObject runWithReturn =
	new RunnableWithReturnObject() {
	  public void run() {
	    returnObject =
	      new Boolean(showPoint(point, value, factor,
				    name, xyzpt, mapName));
	  }
	};
      runWithReturn.invokeAndWait();
      return ((Boolean) runWithReturn.returnObject).booleanValue();
    }
    else {
      if( getFreezeState() ) return false;
      this.point = point;
      this.value = value;
      this.factor = factor;

      setFileName(name);

      this.xyzpt = xyzpt;
      invalidateFilters();
      setFilterChanged();
      repaint();
      ShowPointDisplay rspd = null;
      if(getRecordState() && ((rspd = getRecordShowPointDisplay()) != null))
	rspd.showPoint(point, value, factor, name, xyzpt, mapName);
      this.mapName = mapName;

      return true;
    }
  }
  /**
   * Sets the displayed file name
   *
   * @param name file name to display
   */
  private void setFileName(String name) {
    if(name != this.name) {
      if((this.name != null) || (! name.equals(this.name))) {
        this.name = name;
	//        setToolTipText(name); // caused mouse problems
      }
    }
  }
  /**
   * Does nothing.  Previously set whether the freeze option is visible.
   *
   * @param visible <code>true</code> to show freeze option
   * @Deprecated
   */
    public void setFreezeStateVisible(final boolean visible) { }
  /**
   * Returns false.  Priviously got whether the freeze option is visible.
   *
   * @return <code>true</code> if freeze option is visible
   * @Deprecated
   */
  public boolean isFreezeStateVisible() { return false; }
  /**
   * Sets whether the current displayed location is frozen.
   *
   * @param freeze	<code>true</code> to freeze the current displayed
   * location 
   */
  public void setFreezeState(final boolean freeze) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() { setFreezeState(freeze); }
	} );
    }
    else freezeState = freeze;
  }
  /**
   * Gets the freeze state.
   *
   * @return	<code>true</code> if current location is frozen
   */
  public boolean getFreezeState() { return freezeState; }
  /**
   * Deprecated - ignored.
   *
   * @param visible <code>true</code> to show crosshair option
   * @Deprecated this routine ignored
   */
  public void setCrosshairStateVisible(final boolean visible) {}
  /**
   * Deprecated - always returns false
   *
   * @return <code>true</code> if crosshair option is visible
   * @Deprecated this routine ignored
   */
  public boolean isCrosshairStateVisible() { return false; }
  /**
   * Deprecated - only for use by old scripts.
   *
   * @param show	<code>true</code> if crosshairs shown during showpoint
   * @Deprecated this routine ignored
   */
  public void setCrosshairState(boolean show) {
      deprecatedCrosshairState = show;
  }
  /**
   * Deprecated - only for use by old scripts.
   *
   * @return	<code>true</code> to show crosshairs during showpoint
   * @Deprecated returns false
   */
  public boolean getCrosshairState() { return deprecatedCrosshairState; }
  /**
   * Sets the ShowPointDialog for recording points shown.
   *
   * @param rspd ShowPointDialog to echo points to if record is on
   */
  public void setRecordShowPointDisplay(final ShowPointDisplay rspd) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() { setRecordShowPointDisplay(rspd); }
	} );
    }
    else this.recordShowPointDisplay = rspd;
  }
  /**
   * Gets the ShowPointDialog used for recording shown points.
   *
   * @return ShowPointDialog that echoes points if record is on
   */
  public ShowPointDisplay getRecordShowPointDisplay() {
    return recordShowPointDisplay;
  }
  /**
   * Records the currently displayed point to the
   * record show point display.
   *
   */
  public void record() { record(null); }
  /**
   * Records the currently displayed point to a
   * show point display.
   *
   * @param rspd show point display to record to.
   *        if <code>null</code> records to default
   */
  public void record(ShowPointDisplay rspd) {
      if(rspd == null) rspd = getRecordShowPointDisplay();
      if((rspd != null) && (point != null))
	  rspd.showPoint(point, value, factor, name, xyzpt, mapName);
  }
  /**
   * Sets whether point recording should be performed.
   *
   * @param record	<code>true</code> to record points
   */
  public void setRecordState(final boolean record) {
    if(! SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater( new Runnable() {
	  public void run() { setRecordState(record); }
	} );
    }
    else recordState = record;
  }
  /**
   * Gets whether point recording should is performed.
   *
   * @return	<code>true</code> if points recorded during showpoint
   */
  public boolean getRecordState() { return recordState; }
  /**
   * Sets whether the tracking option is visible.
   *
   * @param visible <code>true</code> to show tracking option
   */
   public void setTrackingStateVisible(boolean visible) {
     setTrackStateVisible(visible);
   }
  /**
   * Deprecated - returns false.
   *
   * @return <code>true</code> if tracking option is visible
   * @Deprecated
   */
   public boolean isTrackingStateVisible() { return false; }
  /**
   * Deprecated - does nothing.
   *
   * @param visible <code>true</code> to show track option
   * @Deprecated this routine ignored
   */
   public void setTrackStateVisible(final boolean visible) {}
  /**
   * Deprecated - returns false
   *
   * @return <code>true</code> if track option is visible
   * @Deprecated returns false
   */
   public boolean isTrackStateVisible() { return false; }
  /**
   * Deprecated - used only by old scripts
   *
   * @Deprecated ignored
   */
  public void setTrackingState(final boolean state) {
      deprecatedTrackingState=state;
  }
  /**
   * Deprecated - returns false
   *
   * @return	<code>true</code> if image slice should track point.
   * @Deprecated
   */
  public boolean getTrackingState() { return deprecatedTrackingState; }
  /**
   * Produces a string representation of this image.
   *
   * @return	a string representation
   */
  public String toString() {
    return getText();
  }
  /**
   * Creates a script to recreate this ShowPointLine.
   *
   *
   * @param scriptedObjects scripted objects list to add this object to.
   *				If <code>null</code> the object
   *				is left in <code>status</code> variable.
   * @return	script that can recreate the ShowPointLine
   */
  public String toScript(CNUScriptObjects scriptedObjects) {
    if(! SwingUtilities.isEventDispatchThread()) {
      final CNUScriptObjects fscriptedObjects = scriptedObjects;
      RunnableWithReturnObject runWithReturn =
	new RunnableWithReturnObject() {
	  public void run() { returnObject = toScript(fscriptedObjects); }
	};
      runWithReturn.invokeAndWait();
      return (String) runWithReturn.returnObject;
    }
    else {
      String className = getClass().getName();
      StringBuffer sb = new StringBuffer(256);
      sb.append("// -- start ").append(className).append(" script\n");

      if(scriptedObjects == null) scriptedObjects = new CNUScriptObjects();
      String variableName = scriptedObjects.get(this);
      if(variableName == null) {
	variableName = scriptedObjects.addObject(this, "showpointdisplayline");
	sb.append(variableName).append(" = new ").append(className).append("();\n");
	if(getRecordShowPointDisplay() != null) {
	  sb.append(variableName).append(".setRecordShowPointDisplay(\n");
	  sb.append("CNUVIEWER.getShowPointDialog().getStatusWindowShowPointDisplay()\n");
	  sb.append(");\n");
	}

	if(point != null) {
	  sb.append("spdlpointtmp = new int[] {");
	  for(int i = 0; i < point.length; i++) {
	    if(i != 0) sb.append(", ");
	    sb.append(point[i]);
	  }
	  sb.append("};\n");
	}
	else sb.append("spdlpointtmp=null;\n");

	sb.append(variableName).append(".showPoint(spdlpointtmp, ").append(value).append(", ");
	sb.append(factor).append(", \"").append(name).append("\", ");

	if(xyzpt != null) {
	  sb.append("new ").append(XYZDouble.class.getName()).append("(").append(xyzpt.x).append(", ");
	  sb.append(xyzpt.y).append(", ").append(xyzpt.z).append(")");
	}
	else sb.append("null");

	sb.append(", ");
	sb.append((mapName == null)? "null" : "\"" + mapName + "\"").append(");\n");
	sb.append(variableName);
	sb.append(".setFreezeState(").append(getFreezeState()).append(");\n");

	NumberFormat nf = getNumberFormat();
	sb.append(DisplayNumberFormat.numberFormatToScript(scriptedObjects, nf));
	sb.append(variableName).append(".setNumberFormat(script_rtn);\n");

	sb.append("unset(\"spdlpointtmp\");\n");

	sb.append(postObjectToScript(scriptedObjects));
      }
      sb.append("script_rtn=").append(variableName).append(";\n");
      sb.append("// -- end ").append(className).append(" script\n");
      return sb.toString();
    }
  }
}
