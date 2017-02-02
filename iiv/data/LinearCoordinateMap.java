package iiv.data;
import iiv.io.*;
import iiv.*;
import iiv.script.*;
import iiv.dialog.*;
import iiv.util.*;
import java.lang.*;
import java.io.*;
/**
 * Class that converts coordinates into Talairach space based on a simple
 * linear mapping.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.display.CNUDisplay
 * @since	iiV1.0
 */
public class LinearCoordinateMap extends AffineCoordinateMap implements CNUFileObject {
  // class static stuff - current default map
  static private Object defaultsLock = new Object();
  static private CoordinateMap defaultCoordinateMap = null;
  /**
   * Sets the default coordinate map.
   *
   * @param coorMap	new default coordinate map
   */
  public static void setDefaultCoordinateMap(CoordinateMap coorMap) {
    synchronized (defaultsLock) {
      defaultCoordinateMap = coorMap;
    }
  }
  /**
   * Gets the default coordinate map.
   *
   * @return	default coordinate map
   */
  public static CoordinateMap getDefaultCoordinateMap() {
    synchronized (defaultsLock) {
      return defaultCoordinateMap;
    }
  }
  // object stuff
  private CNUFile cnufile = null;
  private boolean saved = false;
  // Origin location for image
  private XYZDouble origin = null;
  private int originUnits = UNKNOWN_UNITS;
  // Linear scale factor
  private XYZDouble scale = null;
  private XYZDouble rotation = null;
  private XYZDouble sinRotation = null;
  private XYZDouble cosRotation = null;
  private double[][] rotationMatrix=null;

  /**
   * Constructs a new instance of LinearCoordinateMap.
   */
  public LinearCoordinateMap() { super(null); }
  /**
   * Constructs a new instance of LinearCoordinateMap reading parameters
   * from a file.
   *
   * @param filename	file to retrieve prameters from
   */
  public LinearCoordinateMap(String filename) throws IOException {
    super(null);
    setCNUFile(filename);
    readMap();
    setSaved(true);
  }
  /**
   * Gets the saved to file state of this coordinate map.
   *
   * @return	<code>true</code> if this coordinate map is saved in a file
   */
  public boolean getSaved() { return saved; }
  /**
   * Sets the saved to file state of this coordinate map.
   *
   * @param saved	<code>true</code> to specify this map is saved in a
   *			file
   */
  public void setSaved(boolean saved) { this.saved = saved; }
  /**
   * Sets the CNUFile from a filename for this coordinate map.
   *
   * @param filename	the file name
   */
  public void setCNUFile(String filename) {
    if(filename != null) {
      filename = filename.trim();
      if("".equals(filename)) filename = null;
    }
    if(filename == null) setCNUFile((CNUFile) null);
    else setCNUFile(new CNUFile(filename));
  }
  /**
   * Sets the CNUFile for this coordinate map.
   *
   * @param cnufile	file designator object
   */
  public void setCNUFile(CNUFile cnufile) { this.cnufile = cnufile; }
  /**
   * Gets the CNUFile for this coordinate map.
   *
   * @return	file designator for this map
   */
  public CNUFile getCNUFile() { return cnufile; }
  /**
   * Gets the full name for this coordinate map.
   *
   * @return file file name
   */
  public String getFullName() {
    CNUFile localcnufile = cnufile;
    if(localcnufile == null) return null;
    return cnufile.toString();
  }
  /**
   * Gets the name for this coordinate map.
   *
   * @return	base file name
   */
  public String getName() {
    CNUFile localcnufile = cnufile;
    if(localcnufile == null) return null;
    return cnufile.getName();
  }
  /**
   * Checks if an object is associated with the same file.
   *
   * @param fileObject	object associated with a file
   * @return		<code>true</code> if fileObject is associated with
   *			the same file as this
   */
  public boolean sameFile(Object fileObject) {
    CNUFile lcnufile = getCNUFile();
    if(lcnufile == null) return false;
    else if(fileObject instanceof LinearCoordinateMap)
      return ((LinearCoordinateMap) fileObject).sameFile( lcnufile );
    else return lcnufile.sameFile(fileObject);
  }
  /**
   * Gets this object if it represents the same file as sameFileAsObj.
   *
   * @param sameFileAsObj	object associated with a file
   * @return			<code>this</code> if associated with the
   *				same file else <code>null</code>
   */
  public Object getFileObject(Object sameFileAsObj) {
    if(sameFile( sameFileAsObj )) return this;
    else return null;
  }
  /**
   * Creates a script that may be used to recreate this display component.
   *
   * @param scriptedObjects scripted objects list to add this object to.
   * @return  the script
   */
  public String toScript(CNUScriptObjects scriptedObjects) {
    String classname = getClass().getName();
    StringBuffer sb = new StringBuffer();
    sb.append("// -- start ").append(classname).append(" script\n");
    if(scriptedObjects == null) scriptedObjects = new CNUScriptObjects();
    String variableName = scriptedObjects.get(this);
    if(variableName == null) {
	variableName = scriptedObjects.addObject(this, "linearcoordinatemap");

	String filename = CNUFile.quoteSlashes(getFullName());
	sb.append(variableName);
	if((filename != null) && getSaved()) {
	  sb.append(" =  newFileObject(\"");
	  sb.append(filename).append("\", \"").append(classname).append("\", \"");
	  sb.append(filename).append("\");\n");
	}
	else {
	  sb.append(" = new ").append(classname).append("();\n");
	  String unitsString;
	  switch (originUnits) {
	  default:
	  case UNKNOWN_UNITS:
		unitsString = classname + ".UNKNOWN_UNITS";
		break;
	  case PIXELS:
		unitsString = classname + ".PIXELS";
		break;
	  case METERS:
		unitsString = classname + ".METERS";
		break;
	  case MILLIMETERS:
		unitsString = classname + ".MILLIMETERS";
		break;
	  }
	  sb.append(variableName);
	  sb.append(".setOrigin(");
	  sb.append(origin.x).append(", ");
	  sb.append(origin.y).append(", ");
	  sb.append(origin.z).append(", ");
	  sb.append(unitsString).append(");\n");

	  sb.append(variableName);
	  sb.append(".setScale(");
	  sb.append(scale.x).append(", ").append(scale.y).append(", ");
	  sb.append(scale.z).append(");\n");

	  sb.append(variableName);
	  sb.append(".setRotation(");
	  sb.append(rotation.x).append(", ").append(rotation.y).append(", ");
	  sb.append(rotation.z).append(");\n");
	}
    }
    sb.append("script_rtn=").append(variableName).append(";\n");
    sb.append("// -- end ").append(classname).append(" script\n");
    return sb.toString();
  }
  /**
   * Creates a string representation.
   *
   * @return	string representation
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("*** class").append(getClass().getName()).append(" =\n");
    sb.append("saved=").append(saved).append("\n");
    sb.append("originUnits=").append(units_to_string(originUnits)).append("\n");
    sb.append("origin=").append(origin).append("\n");
    sb.append("scale=").append(scale).append("\n");
    sb.append("rotation=").append(rotation).append("\n");
    sb.append("sinRotation=").append(sinRotation).append("\n");
    sb.append("cosRotation=").append(cosRotation).append("\n");

    sb.append("rotationMatrix={");
    if(rotationMatrix == null) sb.append("null}\n");
    else {
      sb.append("\n {");
      sb.append(rotationMatrix[0][0]).append(",");
      sb.append(rotationMatrix[0][1]).append(",");
      sb.append(rotationMatrix[0][2]).append("},\n");
      sb.append(" {");
      sb.append(rotationMatrix[1][0]).append(",");
      sb.append(rotationMatrix[1][1]).append(",");
      sb.append(rotationMatrix[1][2]).append("},\n");
      sb.append(" {");
      sb.append(rotationMatrix[2][0]).append(",");
      sb.append(rotationMatrix[2][1]).append(",");
      sb.append(rotationMatrix[2][2]).append("}\n}\n");
    }
    sb.append("super:\n");
    sb.append(super.toString());
    return sb.toString();
  }
  /**
   * Sets the origin for converting to new coordinate space.
   *
   * @param origin	origin of coordinate map
   * @param originUnits	units origin is in (MILLIMETERS or METERS)
   */
  public void setOrigin(XYZDouble origin, int originUnits) {
    setOrigin(origin.x, origin.y, origin.z, originUnits);
  }
  /**
   * Sets the origin for converting to new coordinate space.
   *
   * @param xOrigin	x origin
   * @param yOrigin	y origin
   * @param zOrigin	z origin
   * @param originUnits	units origin is in (MILLIMETERS or METERS)
   */
  public void setOrigin(double xOrigin, double yOrigin,
			double zOrigin, int originUnits) {
    if( originUnits == MILLIMETERS ) {
      xOrigin *= 0.001d; yOrigin *= 0.001d; zOrigin *= 0.001d;
      this.originUnits = METERS;
    }
    else this.originUnits = originUnits;
    XYZDouble lorigin = origin;
    if(lorigin == null) origin = new XYZDouble(xOrigin, yOrigin, zOrigin);
    else lorigin.setValues(xOrigin, yOrigin, zOrigin);
    resetAffineMatrix();
  }
  /**
   * Gets the origin values.
   *
   * @return	a new copy of the origin or <code>null</code>
   */
  public XYZDouble getOrigin() {
    XYZDouble lorigin = this.origin;
    if(lorigin == null) return null;
    return new XYZDouble(lorigin);
  }
  /**
   * Gets the origin units.
   *
   * @return	units origin is in (MILLIMETERS or METERS)
   */
  public int getOriginUnits() { return originUnits; }
  /**
   * Sets the scaling factors for converting to new coordinate space.
   *
   * @param scale	scale factors
   */
  public void setScale(XYZDouble scale) {
     setScale(scale.x, scale.y, scale.z);
  }
  /**
   * Sets the scaling factors for converting to new  coordinate space.
   *
   * @param xScale	x scale
   * @param yScale	y scale
   * @param zScale	z scale
   */
  public void setScale(double xScale, double yScale, double zScale) {
    XYZDouble lscale = scale;
    if(lscale == null) scale = new XYZDouble(xScale, yScale, zScale);
    else lscale.setValues(xScale, yScale, zScale);
    resetAffineMatrix();
  }
  /**
   * Gets the scaling values.
   *
   * @return	a new copy of the scaling factors or <code>null</code>
   */
  public XYZDouble getScale() {
    XYZDouble lscale = scale;
    if(lscale == null) return null;
    return new XYZDouble(lscale);
  }
  /**
   * Sets the rotation angle for converting to the new coordinate space.
   *
   * @param rotation	rotation angles in radians
   */
  public void setRotation(XYZDouble rotation) {
    setRotation(rotation.x, rotation.y, rotation.z);
  }
  /**
   * Sets the rotation angles for converting to the new coordinate space.
   *
   * @param xRotation	x rotation in radians
   * @param yRotation	y rotation in radians
   * @param zRotation	z rotation in radians
   */
  public void setRotation(double xRotation, double yRotation,
			  double zRotation) {
    XYZDouble lrotation = rotation;

    if(lrotation == null)
        lrotation = new XYZDouble(xRotation, yRotation, zRotation);
    else lrotation.setValues(xRotation, yRotation, zRotation);

    XYZDouble lsinRotation = sinRotation;
    if(lsinRotation == null) lsinRotation = new XYZDouble();

    XYZDouble lcosRotation = cosRotation;
    if(lcosRotation == null) lcosRotation = new XYZDouble();

    lsinRotation.setValues(Math.sin(lrotation.x), Math.sin(lrotation.y),
      Math.sin(lrotation.z));
    lcosRotation.setValues(Math.cos(lrotation.x), Math.cos(lrotation.y),
      Math.cos(lrotation.z));

    double[][] lrotationMatrix = new double[3][3];
    // create rotation matrix
    lrotationMatrix[0][0] = lcosRotation.z * lcosRotation.y;
    lrotationMatrix[0][1] = (lcosRotation.z * lsinRotation.y * lsinRotation.x) - (lsinRotation.z * lcosRotation.x);
    lrotationMatrix[0][2] = (lcosRotation.z * lsinRotation.y * lcosRotation.x) + (lsinRotation.z * lsinRotation.x);

    lrotationMatrix[1][0] = lsinRotation.z * lcosRotation.y;
    lrotationMatrix[1][1] = (lsinRotation.z * lsinRotation.y * lsinRotation.x) + (lcosRotation.z * lcosRotation.x);
    lrotationMatrix[1][2] = (lsinRotation.z * lsinRotation.y * lcosRotation.x) - (lcosRotation.z * lsinRotation.x);

    lrotationMatrix[2][0] = -lsinRotation.y;
    lrotationMatrix[2][1] = lcosRotation.y * lsinRotation.x;
    lrotationMatrix[2][2] = lcosRotation.y * lcosRotation.x;

    rotation = lrotation;
    sinRotation = lsinRotation;
    cosRotation = lcosRotation;
    rotationMatrix = lrotationMatrix;
    resetAffineMatrix();
  }
  /**
   * Gets the scaling factors.
   *
   * @return	the scaling factors
   */
  public XYZDouble getRotation() {
    XYZDouble lrotation = rotation;
    if(lrotation == null) return null;
    return new XYZDouble(lrotation);
  }
  /**
   * Recalculates affine matrix based on current rotation, scale and origin.
   */
  private void resetAffineMatrix() {
    double[][] localMatrix=new double[3][4];
    // init to identity
    for(int i=0; i<3; i++) {
      for(int j=0; j<4; j++) {
	if(i == j) localMatrix[i][j] = 1d;
	else localMatrix[i][j] = 0d;
      }
    }
    // set rotations
    double[][] lrotationMatrix = rotationMatrix;
    if(lrotationMatrix != null) {
      for(int i=0; i<3; i++) {
	for(int j=0; j<3; j++) {
	  if(j == 2) localMatrix[i][j] = -lrotationMatrix[i][j]; // reverse z sign
	  else localMatrix[i][j] = lrotationMatrix[i][j];
	}
      }
    }
    // set scale
    XYZDouble lscale = scale;
    if(scale != null) {
      localMatrix[0][0] *= scale.x; localMatrix[1][0] *= scale.x; localMatrix[2][0] *= scale.x;
      localMatrix[0][1] *= scale.y; localMatrix[1][1] *= scale.y; localMatrix[2][1] *= scale.y;
      localMatrix[0][2] *= scale.z; localMatrix[1][2] *= scale.z; localMatrix[2][2] *= scale.z;
    }
    // set shifts
    XYZDouble lorigin = this.origin;
    if(lorigin != null) {
      localMatrix[0][3]= localMatrix[0][0]*(-lorigin.x) +
	localMatrix[0][1]*(-lorigin.y) + localMatrix[0][2]*(-lorigin.z);
      localMatrix[1][3]= localMatrix[1][0]*(-lorigin.x) +
	localMatrix[1][1]*(-lorigin.y) + localMatrix[1][2]*(-lorigin.z);
      localMatrix[2][3]= localMatrix[2][0]*(-lorigin.x) +
	localMatrix[2][1]*(-lorigin.y) + localMatrix[2][2]*(-lorigin.z);
    }
    // initialize the affine matrix
    initAffineCoordinateMap(getName(), localMatrix, getOriginUnits());
  }
  /**
   * Converts a point (voxel location) to the new coordinate space.
   *
   * @param pixelLocation	voxel location to convert
   * @param pixelResolution	distances between voxel centers
   *				may be <code>null</code>
   * @return			converted voxel location
   */
  /*
  public XYZDouble toSpace(XYZDouble pixelLocation,
			   XYZDouble pixelResolution ) {
    XYZDouble location = new XYZDouble(pixelLocation);
    XYZDouble lorigin = this.origin;
    if(pixelResolution != null) {
      // convert input location from pixels to meters
      location.scale(pixelResolution);
      // convert origin from pixels to meters if needed
      if(originUnits == PIXELS) {
        lorigin = new XYZDouble(lorigin);
	lorigin.scale(pixelResolution);
      }
    }
    // shift origin
    location.add(-lorigin.x, -lorigin.y, -lorigin.z);
    // reverse z sign
    location.scale(1.0, 1.0, -1.0);
    // at this point location is the same as indice2tomo output
    // scale
    location.scale(scale);

    // rotate via matrix
    location.setValues(
       rotationMatrix[0][0] * location.x +  rotationMatrix[0][1] * location.y +  rotationMatrix[0][2] * location.z,
       rotationMatrix[1][0] * location.x +  rotationMatrix[1][1] * location.y +  rotationMatrix[1][2] * location.z,
       rotationMatrix[2][0] * location.x +  rotationMatrix[2][1] * location.y +  rotationMatrix[2][2] * location.z
       );
    // test against super
XYZDouble tmp = super.toSpace(pixelLocation, pixelResolution);
if((tmp.x != location.x) || (tmp.y != location.y) || (tmp.z != location.z)) {
  System.out.print("differ ");
}    
  System.out.println("linear=" + location.toString() + " affine=" + tmp.toString());
    return location;
  }
  */
  /**
   * Inverts the toSpace conversion.  Converting new coordinate space
   * location to the original voxel space.
   *
   * @param location		cooridinate space location to convert
   * @param pixelResolution	distances between voxel centers
   *				may be <code>null</code>
   * @return			location in original space
   */
  /*
  public XYZDouble fromSpace(XYZDouble location,
			     XYZDouble pixelResolution ) {
    XYZDouble pixelLocation = new XYZDouble(location);
    XYZDouble lorigin = this.origin;
    if(pixelResolution != null) {
      // convert origin from pixels to meters if needed
      if(originUnits == PIXELS) {
        lorigin = new XYZDouble(lorigin);
	lorigin.scale(pixelResolution);
      }
    }

    // rotate via matrix - inverse rotation matrix = trasnposed rotation matrix
    pixelLocation.setValues(
      rotationMatrix[0][0] * pixelLocation.x +  rotationMatrix[1][0] * pixelLocation.y +  rotationMatrix[2][0] * pixelLocation.z,
      rotationMatrix[0][1] * pixelLocation.x +  rotationMatrix[1][1] * pixelLocation.y +  rotationMatrix[2][1] * pixelLocation.z,
      rotationMatrix[0][2] * pixelLocation.x +  rotationMatrix[1][2] * pixelLocation.y +  rotationMatrix[2][2] * pixelLocation.z
       );

    // invert scaling
    pixelLocation.scale(1.0d/scale.x, 1.0d/scale.y, 1.0d/scale.z);
    // reverse z sign
    pixelLocation.scale(1.0, 1.0, -1.0);
    // shift origin
    pixelLocation.add(lorigin.x, lorigin.y, lorigin.z);
    if(pixelResolution != null) {
      // convert input location from meters to pixels
      pixelLocation.scale(1.0d/pixelResolution.x, 1.0d/pixelResolution.y,
		     1.0d/pixelResolution.z);
    }
    return pixelLocation;
  }
  */
  /**
   * Reads the coordinate map from a file.
   *
   * @param filename	file name
   * @return		coordinate map read from file
   */
  public static CoordinateMap readCoordinateMap(String filename,
						CNUViewer cnuv,
						ShowStatus ss) {
    CoordinateMap coorM = null;
    if(filename != null) {
      filename = filename.trim();
      if("".equals(filename)) filename = null;
    }
    if(filename == null) return null;
    CNUFile filetmp = new CNUFile(filename);
    if( filetmp.isDirectory() ) return null;
    // check if in memory
    if(cnuv != null) {
      Object obj = cnuv.getFileObject(filename);
      if(obj instanceof CoordinateMap) coorM = (CoordinateMap) obj;
    }
    // if not found in memory read file
    if(coorM == null) {
      try {
        coorM = new LinearCoordinateMap(filename);
      } catch (IOException e) {
	coorM = null;
	if(ss != null) ss.showStatus((Throwable) e);
      }
    }
    if((coorM == null) && (ss != null))
        ss.showStatus("Unable to read coordinate map file: "
		      + filename);
    return coorM;
  }
  /**
   * Reads the linear coordinate map from the file.
   */
  protected void readMap() throws IOException {
    CNUFile cnufile = getCNUFile();
    if(cnufile == null) throw new IOException("File not set");
    Reader reader = cnufile.getReader();
    if(reader == null)  throw new IOException("Failed openning reader");
    // Origin location for current images
    XYZDouble origin = null;
    int originUnits = UNKNOWN_UNITS;
    // Linear scale factor
    XYZDouble scale = null;
    XYZDouble rotation = null;
    StreamTokenizer tokens = new StreamTokenizer( reader );
    tokens.wordChars('/', '/');
    tokens.wordChars('\\', '\\');
    tokens.wordChars('.', '.');
    tokens.wordChars(':', ':');
    tokens.wordChars('_', '_');
    tokens.wordChars('.', '.');
    tokens.ordinaryChars(';', ';');
    tokens.whitespaceChars('=', '=');
    tokens.whitespaceChars(',', ',');
    tokens.whitespaceChars('(', ')');
    tokens.quoteChar('"');
    // script type quoting
    tokens.commentChar('#');
    // set C++ type quoting
    tokens.slashSlashComments(true);
    tokens.slashStarComments(true);
    tokens.eolIsSignificant(true);
    tokens.parseNumbers();
    int c = StreamTokenizer.TT_EOL; // arbitrary to enter loop
    out:
    while(c != StreamTokenizer.TT_EOF) {
      try {
	c = tokens.nextToken();
	int line = tokens.lineno();
	switch (c) {
	case '"':
	case StreamTokenizer.TT_WORD:
	  String currentName = tokens.sval;
	  double[] numbers = new double[3];
	  if( "TomoOrig".equals(tokens.sval) ) {
	    c = tokens.nextToken();
	    if( "pixels".equals(tokens.sval) ) originUnits = PIXELS;
	    else if( "mm".equals(tokens.sval) ) originUnits = MILLIMETERS;
	    else throw new IOException("parsing line=[" + line + "]" + " "
					+ currentName +
			" requires pixels or mm units specified");
	    int cnt = CNUDisplayScript.getNumberTokens(tokens, numbers);
	    if(cnt != 3)
	      throw new IOException("parsing line=[" + line + "]" + " "
			+ currentName + " requires 3 numbers not " + cnt);
	    origin = new XYZDouble(numbers, 0);
	    if(originUnits == MILLIMETERS) {
	      origin.scale(0.001);
	      originUnits = METERS;
	    }
	  }
	  else if( "Scale".equals(currentName) ) {
	    int cnt = CNUDisplayScript.getNumberTokens(tokens, numbers);
	    if(cnt != 3)
	      throw new IOException("parsing line=[" + line + "]"
				 + " " + currentName +
				 " requires 3 numbers not " + cnt);
	    scale = new XYZDouble(numbers, 0);
	  }
	  else if( "RelRot".equals(tokens.sval) ) {
	    c = tokens.nextToken();
	    int rotationUnits = UNKNOWN_UNITS;
	    if( "deg".equals(tokens.sval) ) rotationUnits = DEGREES;
	    else if( "radians".equals(tokens.sval) )
	      rotationUnits = RADIANS;
	    else throw new IOException("parsing line=[" + line + "]" +
				 " " + currentName +
				 " requires deg or radians units specified");
	    int cnt = CNUDisplayScript.getNumberTokens(tokens, numbers);
	    if(cnt != 3) throw new IOException("parsing line=[" + line + "]"
				 + " " + currentName +
				 " requires 3 numbers not " + cnt);
	    rotation = new XYZDouble(numbers, 0);
	    if(rotationUnits == DEGREES) rotation.scale(DEGREES_2_RADIANS);
	  }
	  else throw new IOException("parsing line=[" + line + "]" +
				  " unknown word = "
				  + tokens.sval);
	  break;
	case StreamTokenizer.TT_NUMBER:
	  throw new IOException("parsing line=[" + line + "]" +
			" unknown number = " + tokens.nval);
	case StreamTokenizer.TT_EOL:
	case StreamTokenizer.TT_EOF:
	  break;
	default:
	  throw new IOException("[" + line + "]" +
				" invalid character = " + c);
        }
      } catch (IllegalArgumentException iae) {
        iae.printStackTrace();
	throw new IOException("IllagelArgumentException occurred");
      }
    }
    if(origin != null) setOrigin(origin, originUnits); else setOrigin(0d,0d,0d, PIXELS);
    if(scale != null) setScale(scale); else setScale(1.0d,1.0d,1.0d);
    if(rotation != null) setRotation(rotation); else setRotation(0d,0d,0d);
  }
  /**
   * Writes a linear coordinate map to a file.
   *
   * @param cnufile	file to write to
   * @param cd		Continue dialog to invoke if file already exists.
   *			If <code>null</code> no write over premitted,
   *			if not <code>null</code> and not a
   *			<code>ContinueDialog</code> writes over with no
   *			prompt.
   * @param lcm		linear coordinate map to save
   */
  public static void writeMap(CNUFile cnufile, Object cd,
			      LinearCoordinateMap lcm)
	throws IOException {
    if(cnufile == null) throw new IOException("Missing cnufile");
    if(lcm == null) throw new IOException("null coordinate map");
    if( cnufile.exists() ) {
      if(cd == null) throw
	new IOException(
	  "WriteLinearCoordinateMap - attempt to write over existing file "
	  + cnufile);
      if(cd instanceof ContinueDialog) {
        ((ContinueDialog)cd).beep();
        ((ContinueDialog)cd).setVisible(true);
        if( ! ((ContinueDialog)cd).getContinueFlag() ) throw
	  new IOException("WriteLinearCoordinateMap - did not write file "
			  + cnufile);
      }
    }
    XYZDouble origin = lcm.getOrigin();
    int originUnits = lcm.getOriginUnits();
    XYZDouble scale = lcm.getScale();
    XYZDouble rotation = lcm.getRotation();
    if( (origin == null) || (scale == null) || (rotation == null) ||
	! ((originUnits == METERS) || (originUnits == MILLIMETERS) ||
	   (originUnits == PIXELS)) )
      throw new IOException(
	"WriteLinearCoordinateMap - LinearCoordinateMap missing values");
    Writer wr = null;
    try {
      wr = cnufile.getWriter();
      wr.write("# this map file generated by " + iiV.programTitle + " " +
		iiV.version + "\n");
      if(originUnits == PIXELS) {
	wr.write("TomoOrig(pixels)=(" +
		 origin.x + "," + origin.y + "," + origin.z + ")\n");
      } else if(originUnits == METERS) {
	origin.scale(1000.0);
	wr.write("TomoOrig(mm)=(" +
		 origin.x + "," + origin.y + "," + origin.z + ")\n");
      } else if(originUnits == MILLIMETERS) {
	wr.write("TomoOrig(mm)=(" +
		 origin.x + "," + origin.y + "," + origin.z + ")\n");
      }
      wr.write("Scale=(" + scale.x + "," + scale.y + "," + scale.z + ")\n");
      rotation.scale(RADIANS_2_DEGREES);
      wr.write("RelRot(deg)=(" +
	       rotation.x + "," + rotation.y + "," + rotation.z + ")\n");
    } finally {
      if(wr != null) wr.close();
    }
  }
}
