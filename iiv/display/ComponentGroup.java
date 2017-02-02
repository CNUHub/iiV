package iiv.display;
import iiv.script.*;
import iiv.filter.*;
import iiv.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
/**
 * Container to treat a group of components as one.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.CNUViewer
 * @see		java.awt.Container
 * @since	iiV1.0
 */
public class ComponentGroup extends JComponent
  implements CNUFileObject, iiVScriptable,
	     Croppable, LocationMapping, Zoomable {
  private static final long serialVersionUID = -870548951678457212L;
  private Dimension preferredSize = new Dimension(0,0);
  /** Keeps components so they may be re-added in doLayout */
  private Component[] components = null;
  /** Keeps component locations to position components in doLayout */
  private Point[] locations = null;
  /** Keeps constrained size */
  private Dimension constrainedSize = null;
  /** Keeps preferred size without cropping */
  private Dimension uncroppedSize = null;
  /** Keeps group zoom factors */
  private double zoomV = 1;
  private double zoomH = 1;
  private double[] originalVzooms = null;
  private double[] originalHzooms = null;
  /** Keeps crop box */
  private Rectangle cropBox = null;

    // private ShowPointImage trackingShowPointImage = null;
    // private ShowPointImage lastSetShowPointImage = null;

  /**
   * Constructs a new instance of ComponentGroup with given
   * components and locations in array lists.
   * To minimize the group size locations are translated setting
   * the minimum x and y location to (0,0) while maintaining
   * relative locations.  The inverse amount of the translation
   * may be queried with the method getLocation() right after
   * creating the group and before adding to a container.
   * This amount can be used to set the groups
   * location to maintain the components position relative to an
   * original container.
   *
   * @param components	components this group will contain
   * @param locations	corresponding location for each component
   *			within the group.
   */
  public ComponentGroup(Component[] components, Point[] locations) {
    this(components, locations, null);
  }
  /**
   * Constructs a new instance of ComponentGroup with given
   * components and locations in array lists.
   * To minimize the group size locations are translated
   * by the region origin maintaining
   * relative locations.  The inverse amount of the translation
   * may be queried with the method getLocation() right after
   * creating the group and before adding to a container.
   * This amount can be used to set the groups
   * location to maintain the components position relative to an
   * original container.
   *
   * @param components	components this group will contain
   * @param locations	corresponding location for each component
   *			within the group.
   * @param region	region that dictates the origin and size of the
   *			group relative to locations.
   *			If <code>null</code> origin will be set to minimum
   *			x and y location and size set to include all
   *			components.
   */
  public ComponentGroup(Component[] components, Point[] locations,
			Rectangle region) {
    // create local copies of arrays and initialize with them
    init( components.clone(), locations.clone(), region);
  }
  /**
   * Constructs a new instance of ComponentGroup with given
   * components and locations in vector lists.  Vectors are copied
   * to arrays for internal usage.
   * To minimize the group size locations are translated setting
   * the minimum x and y location to (0,0) while maintaining
   * relative locations.  The inverse amount of the translateion
   * may be queried with the method getLocation() right after creation
   * and before adding to a container.
   * This amount can be used to set the groups location to maintain the
   * components position relative to an original container.
   *
   * @param vComponents	vector of components this group will contain
   * @param vLocations	vector of corresponding locations for each component
   *			within the group.
   */
  public ComponentGroup(Vector vComponents, Vector vLocations) {
    this(vComponents, vLocations, null);
  }
  /**
   * Constructs a new instance of ComponentGroup with given
   * components and locations in vector lists.  Vectors are copied
   * to arrays for internal usage.
   * To minimize the group size locations are translated
   * by the region origin maintaining
   * relative locations.  The inverse amount of the translation
   * may be queried with the method getLocation() right after creation
   * and before adding to a container.
   * This amount can be used to set the groups location to maintain the
   * components position relative to an original container.
   *
   * @param vComponents	vector of components this group will contain
   * @param vLocations	vector of corresponding locations for each component
   *			within the group.
   * @param region	region that dictates the origin and size of the
   *			group relative to locations.
   *			If <code>null</code> origin will be set to minimum
   *			x and y location and size set to include all
   *			components.
   */
  public ComponentGroup(Vector vComponents, Vector vLocations,
			Rectangle region) {
    // create local copies of vectors into arrays and initialize with them
    Component[] components = new Component[vComponents.size()];
    vComponents.copyInto(components);
    Point[] locations = new Point[components.length];
    vLocations.copyInto(locations);
    init(components, locations, region);
  }
  /**
   * Preforms initialization tasks common to all constructors
   *
   * @param dcs		components this group will contain
   * @param locations	corresponding locations for each component
   *			within the group.
   * @param region	region that dictates the origin and size of the
   *			group relative to locations.
   *			If <code>null</code> origin will be set to minimum
   *			x and y location and size set to include all
   *			components.
   *			
   */
  private final void init(Component[] components, Point[] locations,
			  Rectangle region) {
    setLayout(null);
    invalidate();
    this.components = components;
    this.locations = locations;
    this.originalVzooms = new double[components.length];
    this.originalHzooms = new double[components.length];
    Point originOffset;
    if(region != null) {
      originOffset = region.getLocation();
      constrainedSize = region.getSize();
    }
    else {
      originOffset = locations[0].getLocation();
      for(int i = 1; i < components.length; i++) {
        originOffset.move( Math.min(originOffset.x, locations[i].x),
			   Math.min(originOffset.y, locations[i].y) );
      }
    }
     // don't know why negative order needed to keep front to back correct
    for(int i=components.length - 1; i >= 0; i--) {
      add(components[i]);
      //create local copy of all locations to remove chance of competition
      locations[i] = locations[i].getLocation();
      // translate the locations to relative to origin
      locations[i].translate(-originOffset.x, -originOffset.y);
      //create local copy of original zooms
      if(components[i] instanceof Zoomable) {
	  originalVzooms[i] = ((Zoomable) components[i]).getZoomV();
	  originalHzooms[i] = ((Zoomable) components[i]).getZoomH();
      }
      else {
	  originalVzooms[i] = 1.0; originalHzooms[i] = 1.0;
      }
    }
    setLocation(originOffset);
  }
  /**
   * Layout this container without calling a layout manager.
   * This group may have been ungrouped (all components added
   * back to main container which auto-majicly removes them from
   * this container) in which case it may
   * contain no components but the fact that doLayout is called
   * means we want the components back in this container so
   * re-add them if any are missing.
   */
  public void doLayout() {
    synchronized (getTreeLock()) {
      if(getComponentCount() < components.length) {
	removeAll();
        // don't know why negative order needed to keep front to back correct
        for(int i=components.length - 1; i >= 0; i--) add(components[i]);
      }
      Rectangle area = new Rectangle();
      Point shift = new Point(0,0);
      Dimension d = null;
      if(cropBox != null) {
	  shift.translate(-cropBox.x, -cropBox.y);
	  shift.x *= zoomH; shift.y *= zoomV;
	  d = cropBox.getSize();
	  d.height = (int) Math.ceil(zoomH * d.height);
	  d.width = (int) Math.ceil(zoomV * d.width);
      }
      for(int i=components.length - 1; i >= 0; i--) {
        components[i].setLocation((int) (zoomH * locations[i].x + 0.5) +
				  shift.x,
				  (int) (zoomV * locations[i].y + 0.5) +
				  shift.y);
	components[i].setSize(components[i].getPreferredSize());
        area.add(components[i].getBounds());
      }
      if(constrainedSize != null) uncroppedSize = constrainedSize;
      else uncroppedSize = area.getSize();
      if(d == null) d = uncroppedSize;
      setPreferredSize(d);
      setMinimumSize(d);
      setMaximumSize(d);
    }
  }
  /**
   * Overrides paint to ensure clipping rectangle is set to limit
   * component drawing when group is cropped.
   *
   * @param g graphics to paint to.
   */
  public void paint(Graphics g) {
    Rectangle r = g.getClipBounds();
    Rectangle myR = new Rectangle(getPreferredSize());
    g.clipRect(myR.x, myR.y, myR.width, myR.height);
    super.paint(g);
    g.setClip(r.x, r.y, r.width, r.height); // reset original clipping
  }
  /**
   * Calculates point location with respect to the uncropped unzoomed group.
   *
   * @param pt	location in component
   * @return	location in original group
   */
  public Point trueLocation(Point pt) {
    Point fixedPt = new Point(pt);
    synchronized (getTreeLock()) {
      // correct for zoom
      fixedPt.x /= zoomH;
      fixedPt.y /= zoomV;
      // correct for crop
      if(cropBox != null) fixedPt.translate(cropBox.x, cropBox.y);
      // make sure the point fits over the original group
      if(fixedPt.x < 0) fixedPt.x = 0;
      else if(fixedPt.x >= uncroppedSize.width)
	fixedPt.x = uncroppedSize.width - 1;
      if(fixedPt.y < 0) fixedPt.y = 0;
      else if(fixedPt.y >= uncroppedSize.height)
	fixedPt.y = uncroppedSize.height - 1;
    }
    return fixedPt;
  }
  /**
   * Converts an original image point to a component display location
   * correcting for crop and zoom
   *
   * @param pt	location in original group with no filtering
   * @return	location in component
   */
  public Point displayLocation(Point pt)
  {
    Point displayPt = new Point(pt);
    synchronized (getTreeLock()) {
      // make sure the point fits over the original group
      if(displayPt.x < 0) displayPt.x = 0;
      else if(displayPt.x >= uncroppedSize.width)
	displayPt.x = uncroppedSize.width - 1;
      if(displayPt.y < 0) displayPt.y = 0;
      else if(displayPt.y >= uncroppedSize.height)
	displayPt.y = uncroppedSize.height - 1;
      // correct for crop
      if(cropBox != null) displayPt.translate(-cropBox.x, -cropBox.y);
      // correct for zoom
      displayPt.x *= zoomH;
      displayPt.y *= zoomV;
    }
    return displayPt;
  }
  /**
   * Crops the input image by a given box.
   *
   * @param cropBox	new crop box
   */
  public void setCrop(Rectangle cropBox) {
    synchronized (getTreeLock()) {
      if(cropBox == null) {
        if(this.cropBox != null) {
	  this.cropBox = null; invalidate();
        }
      } else if(! cropBox.equals(this.cropBox)) {
	this.cropBox = cropBox.getBounds();
	invalidate();
      }
    }
  }
  /**
   * Gets the current crop box.
   *
   * @return	the crop box
   */
  public Rectangle getCrop() {
    synchronized (getTreeLock()) {
      if(cropBox == null) return null;
      else return cropBox.getBounds();
    }
  }
  /**
   * Restricts a box to fit on the image producer.
   *
   * @param cropBox	box to restrict
   * @return		box that intersects image producer
   */
  public Rectangle restrictCropBox(Rectangle cropBox) {
    synchronized (getTreeLock()) {
      if((uncroppedSize == null) || (cropBox == null)) return cropBox;
      return cropBox.intersection(new Rectangle(uncroppedSize));
    }
  }
  /**
   * Sets the original zooms for a subcomponent.  Only for
   * correcting scripted components original zoom.  While older
   * iiV versions will fail when this method is called from a
   * script they will still display the subcomponent scaled
   * correctly.
   *
   * @param compNum             component number
   * @param zoomV		vertical zoom
   * @param zoomH		horizontal zoom
   */
  public void setSubcomponentOriginalZoom(int compNum,
					  double zoomV, double zoomH) {
    if(compNum < 0 || compNum >  originalVzooms.length) return;
    if((zoomV < 1e-3) || (zoomV > 1e3) || (Math.abs(zoomV - 1) < 1e-3))
      zoomV = 1;
    if((zoomH < 1e-3) || (zoomH > 1e3) || (Math.abs(zoomH - 1) < 1e-3))
      zoomH = 1;
    synchronized (getTreeLock()) {
	originalVzooms[compNum] = zoomV;
	originalHzooms[compNum] = zoomH;
    }
  }
  /**
   * Sets independent horizontal and vertical zoom factors.
   *
   * @param zoomV		vertical zoom
   * @param zoomH		horizontal zoom
   */
  public void setZoom(double zoomV, double zoomH) {
   if((zoomV < 1e-3) || (zoomV > 1e3) || (Math.abs(zoomV - 1) < 1e-3))
      zoomV = 1;
    if((zoomH < 1e-3) || (zoomH > 1e3) || (Math.abs(zoomH - 1) < 1e-3))
      zoomH = 1;
    synchronized (getTreeLock()) {
      if((this.zoomH != zoomH) || (this.zoomV != zoomV)) {
        this.zoomH = zoomH; this.zoomV = zoomV;
	for(int i = 0; i < components.length; i++) {
	    if(components[i] instanceof Zoomable) {
		((Zoomable) components[i]).setZoom(zoomV * originalVzooms[i],
						   zoomH * originalHzooms[i]);
	    }
	}
	invalidate();
      }
    }
  }
  /**
   * Gets the vertical zoom value.
   *
   * @return	vertical zoom
   */
  public double getZoomV() {
      return zoomV;
  }
  /**
   * Gets the horizontal zoom value.
   *
   * @return	horizontal zoom
   */
  public double getZoomH() {
      return zoomH;
  }
  /**
   * Returns the last (top most) display Component containing a point location.
   *
   * @param pt	point
   * @return	Component containing the point or <code>null</code>
   */
  public Component getDisplayComponent(Point pt) {
    synchronized (getTreeLock()) {
      for(int i= components.length - 1; i >= 0; i--) {
        Rectangle r = components[i].getBounds();
        if(r.contains(pt)) return components[i];
      }
    }
    return null;
  }
  /**
   * Returns the first object associated with a file object from the
   * the component list.
   *
   * @return	object associated with the file object or <code>null</code>
   */
  public Object getFileObject(Object sameFileObj) {
    for(int i = 0; i < components.length; i++) {
      Component comp = components[i];
      if(comp instanceof CNUFileObject) {
	Object obj = ((CNUFileObject) comp).getFileObject(sameFileObj);
	if(obj != null) return obj;
      }
    }
    return null;
  }
  /**
   * Gets the preferred size of this container.
   *
   * @return the preferred size.
   */
  public Dimension getPreferredSize() {
    validate();
    return super.getPreferredSize();
  }
  /**
   * Creates a script that may be used to recreate this display component
   * and store it in a named script variable.
   *
   * @param scriptedObjects scripted objects list to add this object to.
   * @return			CNU script to recreate this object
   */
  public String toScript(CNUScriptObjects scriptedObjects) {
    String className = getClass().getName();
    StringBuffer sb = new StringBuffer("// -- start ").append(className).append(" script\n");
    if(scriptedObjects == null) scriptedObjects = new CNUScriptObjects();
    String variableName = scriptedObjects.get(this);
    if(variableName == null) {
      variableName = scriptedObjects.addObject(this, "compgroup");

      String componentsName = variableName + "_tmp";
      sb.append(componentsName).append(" = new java.util.Vector();\n");
      String locationsName = componentsName + "Locs";
      sb.append(locationsName).append(" = new java.util.Vector();\n");
      for(int i = 0; i < components.length; i++) {
	if(components[i] instanceof iiVScriptable) {
	  sb.append(((iiVScriptable) components[i]).toScript(scriptedObjects));
	  String componentName = scriptedObjects.get(components[i]);
	  sb.append(componentsName).append(".addElement(").append(componentName).append(");\n");
	  sb.append(locationsName).append(".addElement(");
	  sb.append("new java.awt.Point(").append(locations[i].x).append(", ");
	  sb.append(locations[i].y).append(")");
	  sb.append(");\n");
	}
      }
      if(constrainedSize != null) {
	sb.append(variableName).append(" = new ").append(className).append("(");
	sb.append(componentsName).append(", ");
	sb.append(locationsName).append(", ");
	sb.append("new java.awt.Rectangle(0, 0, ");
	sb.append(constrainedSize.width).append(", ");
	sb.append(constrainedSize.height).append(");\n");
	sb.append(");\n");
      }
      else {
	sb.append(variableName).append(" = new ").append(className).append("(");
	sb.append(componentsName).append(", ");
	sb.append(locationsName).append(");\n");
      }
      sb.append(postObjectToScript(scriptedObjects));
    }
    sb.append("script_rtn=").append(variableName).append(";\n");
    sb.append("// -- end ").append(className).append(" script\n");
    return sb.toString();
  }
  /**
   * Creates a script to recreate the settings on an existing object.
   *
   *
   * @param scriptedObjects scripted objects list this object already
   *                        exists in.
   * @return	script to recreate this component
   */
  public String postObjectToScript(CNUScriptObjects scriptedObjects) {
    StringBuffer sb = new StringBuffer();
    String objectVariableName = scriptedObjects.get(this);
    // crop
    Rectangle crop = getCrop();
    if(crop == null) sb.append(objectVariableName).append(".setCrop(null);\n");
    else {
      String cropVariable = scriptedObjects.get(crop);
      if(cropVariable == null) {
	cropVariable = scriptedObjects.addObject(crop, "crop");
	sb.append(cropVariable);
	sb.append(" = new java.awt.Rectangle(").append(crop.x).append(", ").append(crop.y).append(", ");
	sb.append(crop.width).append(", ").append(crop.height).append(");\n");
      }
      sb.append(objectVariableName).append(".setCrop(").append(cropVariable).append(");\n");
    }
    if((zoomV != 1.0) || (zoomH != 1.0)) {
      for(int i = 0; i < components.length; i++) {
	if((components[i] instanceof iiVScriptable) &&
	   (components[i] instanceof Zoomable)) {
	  sb.append(objectVariableName);
	  sb.append(".setSubcomponentOriginalZoom(");
	  sb.append(i).append(", ").append(originalVzooms[i]);
	  sb.append(", ").append(originalHzooms[i]).append(");\n");
	}
      }
      sb.append(objectVariableName);
      sb.append(".setZoom(").append(zoomV).append(", ").append(zoomH).append(");\n");
    }
    return sb.toString();
  }
}
