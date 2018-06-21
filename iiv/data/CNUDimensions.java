package iiv.data;
import iiv.script.*;
import java.awt.*;
/**
 * CNUDimensions stores the dimensions of any multidimensional array.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUData
 * @since	iiV1.0
 */
public class CNUDimensions implements iiVScriptable, Cloneable {
  public final static int UNKNOWN = -1;
  public final static int TRANSVERSE = 0;
  public final static int CORONAL = 1;
  public final static int SAGITTAL = 2;
  public final static int XY_SLICE = 3;
  public final static int XZ_SLICE = 4;
  public final static int YZ_SLICE = 5;
  public final static int YX_SLICE = 6;
  public final static int ZX_SLICE = 7;
  public final static int ZY_SLICE = 8;
  public final static int ORIENTATIONS[] = {TRANSVERSE, CORONAL, SAGITTAL, XY_SLICE, XZ_SLICE, YZ_SLICE, YX_SLICE, ZX_SLICE, ZY_SLICE};
  public final static String ORIENTATION_NAMES[] = {"transverse","coronal", "sagittal", "xy_slice", "xz_slice", "yz_slice", "yx_slice", "zx_slice", "zy_slice"};

  public final static int LEFT_POSITIVE = 1;
  public final static int RIGHT_POSITIVE = 2;
  public final static int ANTERIOR_POSITIVE = 4;
  public final static int POSTERIOR_POSITIVE = 8;
  public final static int INFERIOR_POSITIVE = 16;
  public final static int SUPERIOR_POSITIVE = 32;

  private int[] dimensions = null;
  private int type = CNUTypes.UNKNOWN;
  private int offset = 0;
  private int[] increments = null;
  private double[] spatialResolutions = null;
  private int orientation = TRANSVERSE;
  private int orientationOrder =
  LEFT_POSITIVE | ANTERIOR_POSITIVE | INFERIOR_POSITIVE;
  private boolean incrementssettodefault = false;
  /**
   * Constructs a new blank instance of CNUDimensions.
   */
  public CNUDimensions() {}
  /**
   * Constructs a complete instance of any dimension.
   *
   * @param dimensions	array with one entry for each dimension
   * @param type	type of data stored in array.  May be one of the
   *			static type constants defined by this class
   * @param offset	offset in words of data from beginning of data array
   */
  public CNUDimensions(int [] dimensions, int type, int offset) {
    setValues(dimensions, type, offset);
  }
  /**
   * Returns a string representation of the dimensions.
   *
   * @return	string representation
   */
  public synchronized String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("*** class CNUDimensions\n");
    sb.append(super.toString()).append("\n");
    sb.append("dimensions=").append(CNUTypes.arrayToString(dimensions)).append("\n");
    sb.append("type=").append(CNUTypes.typeToString(type)).append("\n");
    sb.append("offset=").append(offset).append("\n");
    sb.append("increments=").append(CNUTypes.arrayToString(increments)).append("\n");
    sb.append("orientation=").append(orientationToString(orientation)).append("\n");
    sb.append("orientationOrder=").append(orientationOrderToString(orientationOrder)).append("\n");
    sb.append("spatialResolutions=").append(CNUTypes.arrayToString(spatialResolutions)).append("\n");
    sb.append("*** end class CNUDimensions\n");
    return sb.toString();
  }
  /**
   * Creates a script that may be used to recreate this display component.
   *
   * @param scriptedObjects scripted objects list to add this object to.
   * @return  the script
   */
  public String toScript(CNUScriptObjects scriptedObjects) {
    String classname=getClass().getName();
    StringBuffer sb = new StringBuffer("// -- start ").append(classname).append(" script\n");
    if(scriptedObjects == null) scriptedObjects = new CNUScriptObjects();
    String variableName = scriptedObjects.get(this);
    if(variableName == null) {
	variableName = scriptedObjects.addObject(this, "cnudimension");
	sb.append(variableName).append(" = ").append(CNUTypes.arrayToScript(dimensions)).append(";\n");
	sb.append(variableName).append(" = new ").append(classname);
	sb.append("(").append(variableName).append(", ");
	sb.append("iiv.data.CNUTypes.typeValueOf(\"");
	sb.append(CNUTypes.typeToString(getType())).append("\")");
	sb.append(", ");
	sb.append(getOffset()).append(");\n");

	if(! incrementsSetToDefault() ) {
	  sb.append(variableName).append(".setIncrements(");
	  sb.append(CNUTypes.arrayToScript(increments));
	  sb.append(");\n");
	}

	sb.append(variableName).append(".setOrientation(");
	sb.append(variableName).append(".orientationValueOf(\"");
	sb.append(orientationToString(getOrientation())).append("\")");
	sb.append(");\n");

	sb.append(variableName).append(".setOrientationOrder(");
	sb.append(variableName).append(".orientationOrderValueOf(\"");
	sb.append(orientationOrderToString(getOrientationOrder()));
	sb.append("\")");
	sb.append(");\n");

	if(spatialResolutions != null) {
	    sb.append(variableName).append(".setSpatialResolutions(");
	    sb.append(CNUTypes.arrayToScript(spatialResolutions));
	    sb.append(");\n");
	}
    }
    sb.append("script_rtn=").append(variableName).append(";\n");
    sb.append("// -- end ").append(classname).append(" script\n");
    return sb.toString();
  }
  /**
   * Creates a string representation for orientation values.
   *
   * @param orientation	contains either TRANSVERSE, CORONAL, SAGITTAL...
   */
  public final static String orientationToString( int orientation ) {
    if(orientation < 0 || orientation >= ORIENTATION_NAMES.length) return "unknown";
    return(ORIENTATION_NAMES[orientation]);
  }
  /**
   * Gets the orientation value from a string representation ignoring case.
   *
   * @param orientationStr	string representation of orientation as created
   *			   	by orietationToString
   * @return			either TRANSVERSE, CORONAL, SAGITTAL... or UNKNOWN
   */
  public final static int orientationValueOf( String orientationStr ) {
    orientationStr = orientationStr.trim();
    for(int i=0; i<ORIENTATION_NAMES.length; i++) {
      if(ORIENTATION_NAMES[i].equalsIgnoreCase(orientationStr)) return i;
    }
    return UNKNOWN;
  }
  /**
   * Creates a string representation for orientation order values.
   *
   * @param orientationOrder	contains combined (ored) value of left-right,
   *				anterior-posterior, and inferior-superior
   *				positive flags
   * @return			string representation for combined
   *				orientation orders
   */
  public final static String orientationOrderToString( int orientationOrder ) {
    return orientationOrderLRToString(orientationOrder) + "/" +
	orientationOrderAPToString(orientationOrder) + "/" +
	orientationOrderISToString(orientationOrder);
  }
  /**
   * Gets the orientation order value from a string representation ignoring
   * case.
   *
   * @param orientationOrderStr	string containing orientation order
   *				descriptions
   * @return			contains combined values of left-right
   *				anterior-posterior, and inferior-superior
   *				positive flags derived from
   *				orientationOrderStr
   */
  public final static int orientationOrderValueOf( String orientationOrderStr ) {
    if(orientationOrderStr == null) return 0;
    int value = 0;
    orientationOrderStr = orientationOrderStr.toLowerCase();
    if(orientationOrderStr.indexOf("right_positive") >= 0)
      value |= RIGHT_POSITIVE;
    else if(orientationOrderStr.indexOf("left_positive") >= 0)
      value |= LEFT_POSITIVE;
    if(orientationOrderStr.indexOf("posterior_positive") >= 0)
      value |= POSTERIOR_POSITIVE;
    else if(orientationOrderStr.indexOf("anterior_positive") >= 0)
      value |= ANTERIOR_POSITIVE;
    if(orientationOrderStr.indexOf("superior_positive") >= 0)
      value |= SUPERIOR_POSITIVE;
    else if(orientationOrderStr.indexOf("inferior_positive") >= 0)
      value |= INFERIOR_POSITIVE;
    return value;
  }
  /**
   * Creates a string representation for the right/left orientation value.
   *
   * @param orientationOrder	integer containing a left or right positive
   *				flag
   * @return			string representing left or right positive
   */
  public final static String orientationOrderLRToString( int orientationOrder ) {
    if( (orientationOrder & RIGHT_POSITIVE) != 0 ) return "right_positive";
    else return "left_positive";
  }
  /**
   * Create a string representation for the posterior/anterior orientation
   * value.
   *
   * @param orientationOrder	integer containing a anterior or posterior
   *				positive flag
   * @return			string representing anterior or posterior
   *				positive
   */
  public final static String orientationOrderAPToString( int orientationOrder )
  {
    if( (orientationOrder & POSTERIOR_POSITIVE) != 0 )
      return "posterior_positive";
    else return "anterior_positive";
  }
  /**
   * Create a string representation for the inferior/superior orientation
   * value.
   *
   * @param orientationOrder	integer containing a inferior or superior
   *				positive flag
   * @return			string representing inferior or superior
   *				positive
   */
  public final static String orientationOrderISToString( int orientationOrder ) {
    if( (orientationOrder & SUPERIOR_POSITIVE) != 0 )
      return "superior_positive";
    else return "inferior_positive";
  }
  /**
   * Creates a clone of this CNUDimensions.
   *
   * @return	CNUDimensions with all the same settings as this
   */
  public synchronized Object clone() {
    CNUDimensions d = new CNUDimensions(dimensions, type, offset);
    d.setOrientation(this.getOrientation());
    d.setOrientationOrder(this.getOrientationOrder());
    d.setSpatialResolutions(spatialResolutions);
    d.setIncrements(increments);
    return( (Object) d );
  }
  /**
   * Sets values for any dimension array.
   *
   * @param dimensions	array with one entry for each dimension
   * @param type	type of data stored in array.  May be one of the
   *			static type constants defined by this class
   * @param offset	offset in words of data from beginning of data array
   */
  public synchronized void setValues(int [] dimensions, int type, int offset) {
    this.dimensions = arrayDuplicate(dimensions);
    setDefaultIncrements();
    setType(type);
    setOffset(offset);
    setSpatialResolutions(null);
  }
  /**
   * Sets spatial resolutions.
   *
   * @param spatialResolutions	array with one entry for each dimension
   */
  public synchronized void setSpatialResolutions(double [] spatialResolutions) 
  {
    this.spatialResolutions = arrayDuplicate(spatialResolutions);
  }
  /**
   * Sets standard increments to get between points in a 1D array holding
   * data of this dimension.
  */
  public synchronized void setDefaultIncrements() {
    if(dimensions == null) {
      increments = null;
      incrementssettodefault = false;
    }
    else {
      increments = new int[dimensions.length];
      increments[0] = 1;
      for(int n = 1; n < increments.length; n++) {
	increments[n] = increments[n - 1] * dimensions[n - 1];
      }
      incrementssettodefault = true;
    }
  }
  /**
   * Checks if increments are set to the default values.
   *
   * @return	<code>true</code> if increments are set to default values
   */
  public synchronized boolean incrementsSetToDefault() {
    return incrementssettodefault;
  }
  /**
   * Checks then sets a flag if increments are set to the default values.
   *
   * @return	<code>true</code> if increments are set to default values
   */
  public synchronized boolean updateIncrementsSetToDefaultFlag() {
    incrementssettodefault = false;
    if(dimensions == null) return false;
    else if(increments == null) return false;
    else if(increments.length != dimensions.length) return false;
    else {
      if(increments[0] != 1) return false;
      for(int n = 1; n < increments.length; n++) {
	if(increments[n] != (increments[n - 1] * dimensions[n - 1]))
	  return false;
      }
    }
    incrementssettodefault = true;
    return true;
  }
  /**
   * Sets increments to get between points in a 1D array holding
   * data of this dimension - must be called after setting dimensions
   * or increments will get set back to defaults.
   *
   * @param nonStdIncrements	array containing one increment per dimension
   */
  public synchronized void setIncrements(int [] nonStdIncrements) {
    increments = arrayDuplicate(nonStdIncrements);
    // check and set the incrementssettodefault flag
    updateIncrementsSetToDefaultFlag();
  }
// shortcuts for 1D, 2D, 3D, and 4D arrays
  /**
   * Sets the array as a 1D array.
   *
   * @param x		size of a one dimensional array
   * @param type	type of data stored in array.  May be one of the
   *			static type constants defined by this class.
   * @param offset	offset in words of data from beginning of data array
   */
  public synchronized void set1DValues(int x, int type, int offset) {
    dimensions = new int[1];
    dimensions[0] = x;
    setDefaultIncrements();
    setType(type);
    setOffset(offset);
  }
  /**
   * Sets the array as a 2D array.
   *
   * @param x		size of first dimension
   * @param y		size of second dimension array
   * @param type	type of data stored in array.  May be one of the
   *			static type constants defined by this class.
   * @param offset	offset in words of data from beginning of data array
   */
  public synchronized void set2DValues(int x, int y, int type, int offset) {
    dimensions = new int[2];
    dimensions[0] = x;
    dimensions[1] = y;
    setDefaultIncrements();
    setType(type);
    setOffset(offset);
  }
  /**
   * Sets the array as a 3D array.
   *
   * @param x		size of first dimension
   * @param y		size of second dimension array
   * @param z		size of third dimension array
   * @param type	type of data stored in array.  May be one of the
   *			static type constants defined by this class.
   * @param offset	offset in words of data from beginning of data array
   */
  public synchronized void set3DValues(int x, int y, int z,
				       int type, int offset) {
    dimensions = new int[3];
    dimensions[0] = x;
    dimensions[1] = y;
    dimensions[2] = z;
    setDefaultIncrements();
    setType(type);
    setOffset(offset);
  }
  /**
   * Sets the array as a 4D array.
   *
   * @param x		size of first dimension
   * @param y		size of second dimension array
   * @param z		size of third dimension array
   * @param i		size of fourth dimension array
   * @param type	type of data stored in array.  May be one of the
   *			static type constants defined by this class.
   * @param offset	offset in words of data from beginning of data array
   */
  public synchronized void set4DValues(int x, int y, int z, int i,
				       int type, int offset) {
    dimensions = new int[4];
    dimensions[0] = x;
    dimensions[1] = y;
    dimensions[2] = z;
    dimensions[3] = i;
    setDefaultIncrements();
    setType(type);
    setOffset(offset);
  }
  /**
   * Sets the spatial resolutions for a 1D array.
   *
   * @param xres	meter distance between centers of pixels in
   *			the x dimension
   */
  public synchronized void set1DSpatialResolutions(double xres) {
    spatialResolutions = new double[1];
    spatialResolutions[0] = xres;
  }
  /**
   * Sets the spatial resolutions for a 2D array.
   *
   * @param xres	meter distance between centers of pixels in
   *			the x dimension
   * @param yres	meter distance between centers of pixels in
   *			the y dimension
   */
  public synchronized void set2DSpatialResolutions(double xres, double yres) {
    spatialResolutions = new double[2];
    spatialResolutions[0] = xres;
    spatialResolutions[1] = yres;
  }
  /**
   * Sets the spatial resolutions for a 3D array.
   *
   * @param xres	meter distance between centers of pixels in
   *			the x dimension
   * @param yres	meter distance between centers of pixels in
   *			the y dimension
   * @param zres	meter distance between centers of pixels in
   *			the z dimension
   */
  public synchronized void set3DSpatialResolutions(double xres, double yres,
						   double zres) {
    spatialResolutions = new double[3];
    spatialResolutions[0] = xres;
    spatialResolutions[1] = yres;
    spatialResolutions[2] = zres;
  }
  /**
   * Sets the spatial resolutions for a 4D array.
   *
   * @param xres	meter distance between centers of pixels in
   *			the x dimension
   * @param yres	meter distance between centers of pixels in
   *			the y dimension
   * @param zres	meter distance between centers of pixels in
   *			the z dimension
   * @param ires	meter(time?) distance between centers of pixels in
   *			the i dimension
   */
  public synchronized void set4DSpatialResolutions(double xres, double yres,
						   double zres, double ires) {
    spatialResolutions = new double[4];
    spatialResolutions[0] = xres;
    spatialResolutions[1] = yres;
    spatialResolutions[2] = zres;
    spatialResolutions[3] = ires;
  }
  /**
   * Sets the data type.
   *
   * @param type	type of data stored in array.  May be one of the
   *			static type constants defined by this class.
   */
  public synchronized void setType(int type) {
    this.type = type;
  }
  /**
   * Returns data type.
   *
   * @return type	type of data stored in array.  May be one of the
   *			static type constants defined by this class.
   */
  final public synchronized int getType() {return(type);}
  /**
   * Sets the offset.
   *
   * @param offset	offset in words of data from beginning of data array
   */
  public synchronized void setOffset(int offset) {
    this.offset = offset;
  }
  /**
   * Returns the offset.
   *
   * @return offset	offset in words of data from beginning of data array
   */
  final public synchronized int getOffset() { return offset; }
  /**
   * Sets the orientation.
   *
   * @param orientation	contains either TRANSVERSE, CORONAL SAGITTAL...
   */
  public synchronized void setOrientation( int orientation ) {
    this.orientation = orientation;
  }
  /**
   * Returns the orientation.
   *
   * @return	either TRANSVERSE, CORONAL, SAGITTAL...
   */
  final public synchronized int getOrientation() { return orientation; }
  /**
   * Sets the orientation order.
   *
   * @param orientationOrder	contains combined (ored) value of left-right,
   *				anterior-posterior, and inferior-superior
   *				positive flags
   */
  public synchronized void setOrientationOrder( int orientationOrder ) {
    this.orientationOrder = orientationOrder;
  }
  /**
   * Returns the orientation orders.
   *
   * @return	combined (ored) value of left-right,
   *		anterior-posterior, and inferior-superior
   *		positive flags
   */
  final public synchronized int getOrientationOrder() {
    return orientationOrder;
  }
  /**
   * Checks validity of fields.
   *
   * @return	<code>true</code> if fields are valid, false otherwise
   */
  public synchronized boolean valid() {
    if(dimensions == null)return false;
    if(offset < 0)return false;
    if(! CNUTypes.valid(type))return false;
    for(int i = dimensions.length - 1; i >= 0; i--) {
      if(dimensions[i] <= 0) return false;
    }
    if(spatialResolutions != null) {
      if(spatialResolutions.length != dimensions.length) return false;
      for(int i = spatialResolutions.length - 1; i >= 0; i--) {
        if(spatialResolutions[i] <= 0) return false;
      }
    }
    return true;
  }
  /**
   * Determines the equality of this and another CNUDimension.
   *
   * @param otherDimension	dimension to compare to.
   * @return			<code>true</code> if this and
   *				otherDimension are equivalent,
   *				<code>false</code> otherwise.
   */
  public synchronized boolean equals(CNUDimensions otherDimension) {
    if(otherDimension == null) return false;
    if(this.type != otherDimension.getType()) return false;
    if(this.offset != otherDimension.getOffset()) return false;
    if(! this.sameSpatialResolutions(otherDimension) ) return false;
    return this.sameSize(otherDimension);
  }
  /**
   * Determines if the sizes of this and another CNUDimension are the same
   * ignoring type, offset and spatial resolutions.
   *
   * @param otherDimension	dimension to compare to.
   * @return			<code>true</code> if this and
   *				otherDimension have equal sizes,
   *				<code>false</code> otherwise.
   *
   */
  public synchronized boolean sameSize(CNUDimensions otherDimension) {
    if(otherDimension == null) return false;
    int ndims = this.getNumberOfDimensions();
    if(ndims != otherDimension.getNumberOfDimensions())
      return false;
    return sameSize(ndims, otherDimension);
  }
  /**
   * Determines if first ndims of this and another CNUDimension are the same.
   *
   * @param ndims		number of dimensions to compare
   * @param otherDimension	dimension to compare to
   * @return			<code>true</code> if this and
   *				otherDimension have equal
   *				sizes for ndims, <code>false</code> otherwise
   */
  public synchronized boolean sameSize(int ndims,
				       CNUDimensions otherDimension) {
    if(otherDimension == null) return false;
    for(int n=0; n < ndims; n++) {
      if(this.getDim(n) != otherDimension.getDim(n))
	return false;
    }
    return true;		
  }
  /**
   * Determines if the spatial resolutions of this and another
   * CNUDimension are the same.
   *
   * @param otherDimension	dimension to compare to
   * @return			<code>true</code> if this and
   *				otherDimension have the
   *				same resolutions, <code>false</code> otherwise
   */
  public synchronized boolean sameSpatialResolutions(
					CNUDimensions otherDimension) {
    if(otherDimension == null) return false;
    if( (spatialResolutions == null) && 
        (otherDimension.spatialResolutions == null) ) return true;
    int ndims = this.getNumberOfDimensions();
    if(ndims != otherDimension.getNumberOfDimensions()) return false;
    for(int n=0; n < ndims; n++) {
      if(this.getSpatialRes(n) != otherDimension.getSpatialRes(n))
	return false;
    }
    return true;
  }
  /**
   * Returns number of dimensions.
   *
   * @return	the number of dimensions
   */
  final public synchronized int getNumberOfDimensions() {
    if(dimensions == null) return 0;
    else return dimensions.length;
  }
  /**
   * Returns a copy of dimensions.
   *
   * @return	a duplicate of the dimensions array
   */
  final public synchronized int[] getDimensions() {
    return arrayDuplicate(dimensions);
  }
  /**
   * Returns a dimension.
   *
   * @param n	dimension to return
   * @return	size of dimension n
   */
  final public synchronized int getDim(int n) {
    if(dimensions == null) return 0;
    if(n >= dimensions.length) return 1;
    return dimensions[n];
  }
  /**
   * Returns value of first dimension.
   *
   * @return	size of the x dimension
   */
  final public synchronized int xdim() {
    if(dimensions == null) return 0;
    return dimensions[0];
  }
  /**
   * Returns value of second dimension.
   *
   * @return	size of the y dimension
   */
  final public synchronized int ydim() {
    if(dimensions == null) return 0;
    else if(dimensions.length < 2) return 1;
    return dimensions[1];
  }
  /**
   * Returns value of third dimension.
   *
   * @return	size of the z dimension
   */
  final public synchronized int zdim() {
    if(dimensions == null) return 0;
    else if(dimensions.length < 3) return 1;
    return dimensions[2];
  }
  /**
   * Returns value of fourth dimension.
   *
   * @return	size of the i dimension
   */
  final public synchronized int idim() {
    if(dimensions == null) return 0;
    else if(dimensions.length < 4) return 1;
    return dimensions[3];
  }
  /**
   * Returns a copy of spatialResolutions.
   *
   * @return	a new array containing the spatial resolutions or
   *		<code>null</code> if not set
   */
  final public synchronized double[] getSpatialResolutions() {
    return arrayDuplicate(spatialResolutions);
  }
  /**
   * Returns a spatial resolution.
   *
   * @param n	dimension to retrieve spatial resolution for
   * @return	spatial resolution for dimension n
   */
  final public synchronized double getSpatialRes(int n) {
    if(spatialResolutions == null) return 1;
    if(n >= spatialResolutions.length) return 0;
    return spatialResolutions[n];
  }
  /**
   * Determines if a point falls inside the dimensions.
   *
   * @param point	array of indices for point
   * @return		<code>true</code> if point falls inside of dimensions,
   *			<code>false</code> otherwise
   */
  public synchronized boolean contains(int[] point) {
    if(point == null)return false;
    for(int n = point.length - 1; n >= 0; n--) {
      if(point[n] < 0) return false;
      if(getDim(n) <= point[n]) return false;
    }
    return true;
  }
  /**
   * Returns a copy of the increments values that could be used to access a
   * 1D array with this dimensions.
   *
   * @return a new array containing increments for accessing the data array
   */
  final public synchronized int[] getIncrements() {
    return arrayDuplicate(increments);
  }
  /**
   * Returns the increment value for a single dimension.
   *
   * @param n	dimension to retrieve increment for
   * @return	increment for dimension n
   */
  final public synchronized int getIncrement(int n) {
    if(increments == null) {
      setDefaultIncrements();
      if(increments == null) return 0;
    }
    if(n >= increments.length)
      return increments[increments.length-1];
    return increments[n];
  }
  /**
   * Calculates index to a 1D array holding data of this dimension.
   *
   * @param point	array of indices to calculate 1D index for
   * @return		1D index based on point
   */
  final public synchronized int getIndex(int[] point) {
    if(! contains(point))return(-1);
    int index = 0;
    for(int n = point.length - 1; n >= 0; n--) {
      index += getIncrement(n) * point[n];
    }
    return index + offset;
  }
  /**
   * Calculates a point from an index to the 1D array.
   *
   * @param index	index to 1D array
   * @return		array containing indices to multidimensional point
   */
  final public synchronized int[] getPoint(int index) {
    index = index - offset;
    int[] point = new int[dimensions.length];
    if(incrementssettodefault) {
      // default increments are always ordered
      // Calculate starting with the highest index
      for(int n = dimensions.length - 1; n >= 0; n--) {
        if(increments[n] > 0) {
	  point[n] = index / increments[n];
          index = index % increments[n];
        }
        else point[n] = 0;
      }
      if(! contains(point) || (index != 0)) return null;
      return point;
    }
    else {
      // increments may not be ordered
      // use same algorithm as tracing to get point
      int iindex = 0;
      for(point[3]=0; point[3]<dimensions[3];
	point[3]++, iindex += increments[3]) {
	int zindex = iindex;
        for(point[2]=0; point[2]<dimensions[2];
	  point[2]++, zindex += increments[2]) {
	  int yindex = zindex;
          for(point[1]=0; point[1]<dimensions[1];
	    point[1]++, yindex += increments[1]) {
	    int xindex = yindex;
            for(point[0]=0; point[0]<dimensions[0];
	      point[0]++, xindex += increments[0]) {
	      if(xindex == index) return point;
	    }
	  }
	}
      }
      // invalid index
      return null;
    }
  }
  /**
   * Calculates length in words of data fitting this dimension.
   *
   * @return	length in words
   */
  public synchronized int lengthInWords() {
    if(dimensions == null)return 0;
    int length = 1;
    for(int n = dimensions.length - 1; n >= 0; n--)
      length *= dimensions[n];
    return length + offset;
  }
  /**
   * Duplicates an integer array.
   *
   * @param array	integer array
   * @return		duplicate of integer array
   */
  public final static int[] arrayDuplicate(int[] array) {
    if(array == null) return null;
    return array.clone();
  }
  /**
   * Duplicates an double array.
   *
   * @param array	double array
   * @return 		duplicate of double array
   */
  public final static double[] arrayDuplicate(double[] array) {
    if(array == null) return null;
    return array.clone();
  }
}
