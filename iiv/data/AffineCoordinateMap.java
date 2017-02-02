package iiv.data;
import iiv.io.*;
import iiv.*;
import iiv.script.*;
import iiv.dialog.*;
import iiv.util.*;
import java.lang.*;
import java.io.*;
/**
 * Class that converts coordinates into another space usually scanner-anatomical coordinates
 * based on a affine transform.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.display.CNUDisplay
 * @since	iiV1.182
 */
public class AffineCoordinateMap implements CoordinateMap, iiVScriptable {
  private String name = null;
  private AffineMatrix affineMatrix = null;
  /* if shiftUnits are in PIXELS they must be scaled by corresponding resolutions */
  private int shiftUnits = PIXELS;
  /**
   * Constructs a new identy instance of AffineCoordinateMap.
   *
   */
  public AffineCoordinateMap() {
    initAffineCoordinateMap("identity", null, METERS);
  }
  /**
   * Constructs a new instance of AffineCoordinateMap.
   *
   * @param name name for this mapping
   * @param inMatrix matrix containing affine mapping
   * @param shiftUnits units the three shift components of the affine matrix are in.
   */
  public AffineCoordinateMap(String name, double[][] inMatrix, int shiftUnits) {
    initAffineCoordinateMap(name, inMatrix, shiftUnits);
  }
  /**
   * Constructs a new instance of AffineCoordinateMap.
   *
   * @param name name for this mapping
   * @param inMatrix matrix containing affine mapping
   */
  public AffineCoordinateMap(String name, double[][] inMatrix) {
    initAffineCoordinateMap(name, inMatrix, PIXELS);
  }
  /**
   * Constructs a new instance of AffineCoordinateMap.
   *
   * @param inMatrix matrix containing affine mapping
   */
  public AffineCoordinateMap(double[][] inMatrix) {
    this("noname", inMatrix, PIXELS);
  }
  /**
   * Initializes new instance of AffineCoordinateMap.
   *
   * @param name name for this mapping
   * @param inMatrix matrix containing affine mapping
   * @param shiftUnits units the three shift components of the affine matrix are in.
   */
  protected void initAffineCoordinateMap(String name,
					 double[][] inMatrix,
					 int shiftUnits) {
    double[][] matrix = inMatrix;
    this.name = name;
    switch(shiftUnits) {
    case MILLIMETERS:
      matrix = new double[3][4];
      for(int i=0; i<3; i++) {
	for(int j=0; j<4; j++) {
	  if((inMatrix != null) && (i < inMatrix.length) && (j < inMatrix[i].length)) {
	    matrix[i][j] = inMatrix[i][j];
	    if(j==3) matrix[i][j] *= 0.001d;  // all this just for this correction
	  }
	  else if(i==j) matrix[i][j] = 1.0d;
	  else matrix[i][j] = 0.0d;
	}
      }
      shiftUnits = METERS;
      break;
    case METERS:
    case PIXELS:
      break;
    default:
      System.err.println(getClass().getName() + " unknown shift units = " + shiftUnits + " setting to PIXEL units");
      shiftUnits=PIXELS;
    }

    this.shiftUnits = shiftUnits;
    affineMatrix = new AffineMatrix(matrix);
    affineMatrix.initInverseMatrix();
  }
  /**
   * Gets the name for this coordinate map.
   *
   * @return	base file name
   */
  public String getName() {
    return name;
  }
  /**
   * Gets the units the three shift components of the affine matrix are in.
   *
   * @return shift units (PIXELS or METERS)
   */
  public int getShiftUnits() {
    return shiftUnits;
  }
  /**
   * Converts a point (often voxel location) to the new coordinate space.
   *
   * @param pixelLocation	voxel location to convert
   * @param pixelResolution	distances between voxel centers
   *				may be <code>null</code>
   * @return			converted voxel location
   */
  public XYZDouble toSpace(XYZDouble pixelLocation,
			   XYZDouble pixelResolution ) {
    XYZDouble location = new XYZDouble(pixelLocation);
    XYZDouble shift = new XYZDouble(affineMatrix.value(0,3),
				    affineMatrix.value(1,3),
				    affineMatrix.value(2,3));
    if(pixelResolution != null) {
      // convert input location from pixels to meters
      location.scale(pixelResolution);
      // transform shift to meters if needed
      if(shiftUnits == PIXELS) shift.scale(pixelResolution);
    }
    // 3x3 transform plus shifts
    location.setValues(
       affineMatrix.value(0,0) * location.x +  affineMatrix.value(0,1) * location.y +  affineMatrix.value(0,2) * location.z + shift.x,
       affineMatrix.value(1,0) * location.x +  affineMatrix.value(1,1) * location.y +  affineMatrix.value(1,2) * location.z + shift.y,
       affineMatrix.value(2,0) * location.x +  affineMatrix.value(2,1) * location.y +  affineMatrix.value(2,2) * location.z + shift.z
       );

    return location;
  }

  /**
   * Inverts the toSpace conversion.  Converting new coordinate space
   * location to the original space.
   *
   * @param location		coordinate space location to convert
   * @param pixelResolution	distances between voxel centers
   *				may be <code>null</code>
   * @return			location in original space
   */
  public XYZDouble fromSpace(XYZDouble location,
			     XYZDouble pixelResolution ) {
    XYZDouble pixelLocation = new XYZDouble(location);
    if(affineMatrix == null || !affineMatrix.hasInverse()) return pixelLocation;

    // undo shifts first
    if(pixelResolution != null && shiftUnits == PIXELS) {
      pixelLocation.add(-affineMatrix.value(0,3)*pixelResolution.x,
			-affineMatrix.value(1,3)*pixelResolution.y,
			-affineMatrix.value(2,3)*pixelResolution.z);
    }
    else pixelLocation.add(-affineMatrix.value(0,3), -affineMatrix.value(1,3), -affineMatrix.value(2,3));

    // apply inverted 3x3 transform
    pixelLocation.setValues(
			    affineMatrix.inverse(0,0) * pixelLocation.x +  affineMatrix.inverse(0,1) * pixelLocation.y +  affineMatrix.inverse(0,2) * pixelLocation.z,
			    affineMatrix.inverse(1,0) * pixelLocation.x +  affineMatrix.inverse(1,1) * pixelLocation.y +  affineMatrix.inverse(1,2) * pixelLocation.z,
			    affineMatrix.inverse(2,0) * pixelLocation.x +  affineMatrix.inverse(2,1) * pixelLocation.y +  affineMatrix.inverse(2,2) * pixelLocation.z
			    );

    if(pixelResolution != null) {
      // convert input location from meters to pixels
      pixelLocation.scale(1.0d/pixelResolution.x, 1.0d/pixelResolution.y,
			  1.0d/pixelResolution.z);
    }
    return pixelLocation;
  }

  /**
   * Creates a script that may be used to recreate this coordinate map.
   *
   * @param scriptedObjects scripted objects list to add this object to
   *				(may be <code>null</code>).
   * @return  the script
   */
  public String toScript(CNUScriptObjects scriptedObjects) {
    String classname = getClass().getName();
    StringBuffer sb = new StringBuffer();
    sb.append("// -- start ").append(classname).append(" script\n");
    if(scriptedObjects == null) scriptedObjects = new CNUScriptObjects();
    String variableName = scriptedObjects.get(this);
    if(variableName == null) {
      variableName = scriptedObjects.addObject(this, "affinecoordinatemap");
      sb.append(variableName).append(" = new ").append(classname).append("(\n");
      sb.append("\"").append(getName()).append("\",");
      sb.append("\n  new double[][]{\n {");
      sb.append(affineMatrix.value(0,0)).append(",");
      sb.append(affineMatrix.value(0,1)).append(",");
      sb.append(affineMatrix.value(0,2)).append(",");
      sb.append(affineMatrix.value(0,3)).append("},\n {");
      sb.append(affineMatrix.value(1,0)).append(",");
      sb.append(affineMatrix.value(1,1)).append(",");
      sb.append(affineMatrix.value(1,2)).append(",");
      sb.append(affineMatrix.value(1,3)).append("},\n {");
      sb.append(affineMatrix.value(2,0)).append(",");
      sb.append(affineMatrix.value(2,1)).append(",");
      sb.append(affineMatrix.value(2,2)).append(",");
      sb.append(affineMatrix.value(2,3)).append("}\n},\n");
      sb.append(classname).append(".").append(units_to_string(getShiftUnits()));
      sb.append(");");
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
    sb.append("*** class ").append(getClass().getName()).append(" =\n");
    sb.append("name=").append(name).append("\n");
    sb.append("shiftUnits=").append(units_to_string(getShiftUnits())).append("\n");
    sb.append("affineMatrix=");
    sb.append(affineMatrix.toString());
    return sb.toString();
  }
  /**
   * Converts units to a string representation.
   *
   * @param units units
   * @return string representation of units
   */
  public static String units_to_string(int units) {
    switch(units) {
    default:
    case UNKNOWN_UNITS:
      return "UNKNOWN_UNITS";
    case PIXELS:
      return "PIXELS";
    case METERS:
      return "METERS";
    case MILLIMETERS:
      return "MILLIMETERS";
    case RADIANS:
      return "RADIANS";
    case DEGREES:
      return "DEGREES";
    }
  }

  /**
   * Create product of two affine coordinate maps.
   * @param leftMap coordinate map treated like the left side
   *                of a matrix multiplication
   * @param rightMap coordinate map treated like the right side
   *                 of a matrix multiplication
   * @return product
  public static AffineCoordinateMap affineMapsProduct(AffineCoordinateMap leftMap,
						      AffineCoordinateMap rightMap) {
    double[][] p = new double[4][4];
    double[][] lm = leftMap.affineMatrix.matrix;
    double[][] rm = rightMap.affineMatrix.matrix;

    p[0][0] = lm[0][0]*rm[0][0] + lm[0][1]*rm[1][0] + lm[0][2]*rm[2][0] + lm[0][3]*rm[3][0];
    p[0][1] = lm[0][0]*rm[0][1] + lm[0][1]*rm[1][1] + lm[0][2]*rm[2][1] + lm[0][3]*rm[3][1];
    p[0][2] = lm[0][0]*rm[0][2] + lm[0][1]*rm[1][2] + lm[0][2]*rm[2][2] + lm[0][3]*rm[3][2];
    p[0][3] = lm[0][0]*rm[0][3] + lm[0][1]*rm[1][3] + lm[0][2]*rm[2][3] + lm[0][3]*rm[3][3];

    p[1][0] = lm[1][0]*rm[0][0] + lm[1][1]*rm[1][0] + lm[1][2]*rm[2][0] + lm[1][3]*rm[3][0];
    p[1][1] = lm[1][0]*rm[0][1] + lm[1][1]*rm[1][1] + lm[1][2]*rm[2][1] + lm[1][3]*rm[3][1];
    p[1][2] = lm[1][0]*rm[0][2] + lm[1][1]*rm[1][2] + lm[1][2]*rm[2][2] + lm[1][3]*rm[3][2];
    p[1][3] = lm[1][0]*rm[0][3] + lm[1][1]*rm[1][3] + lm[1][2]*rm[2][3] + lm[1][3]*rm[3][3];

    p[2][0] = lm[2][0]*rm[0][0] + lm[2][1]*rm[1][0] + lm[2][2]*rm[2][0] + lm[2][3]*rm[3][0];
    p[2][1] = lm[2][0]*rm[0][1] + lm[2][1]*rm[1][1] + lm[2][2]*rm[2][1] + lm[2][3]*rm[3][1];
    p[2][2] = lm[2][0]*rm[0][2] + lm[2][1]*rm[1][2] + lm[2][2]*rm[2][2] + lm[2][3]*rm[3][2];
    p[2][3] = lm[2][0]*rm[0][3] + lm[2][1]*rm[1][3] + lm[2][2]*rm[2][3] + lm[2][3]*rm[3][3];

    p[3][0] = p[3][1] = p[3][2] = 0d; p[3][3] = 1d;

    return new AffineCoordinateMap(p);
  }
   */
}
