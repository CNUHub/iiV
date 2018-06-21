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
 * based on a scale, translation and a rotation matrix.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.display.CNUDisplay
 * @since	iiV1.17a
 */
public class NiftiSFormCoordinateMap extends AffineCoordinateMap {
  private double units_to_meters = 1.0d;
  private XYZDouble row_res = new XYZDouble(1.0d, 1.0d, 1.0d);
  private double[] srow_x, srow_y, srow_z;
  /**
   * Constructs a new instance of NiftiSFormCoordinateMap.
   *
   * @param srow_x array containing 1st row of 4X4 transform matrix
   * @param srow_y array containing 2nd row of 4X4 transform matrix
   * @param srow_z array containing 3rd row of 4X4 transform matrix
   * @param row_res resolutions of x, y, and z components of srow arrays
   * @param units_to_meters factor to convert transformed units into meters
   * @param name name for this mapping
   */
  public NiftiSFormCoordinateMap(float[] srow_x, float[] srow_y, float[] srow_z,
				 XYZDouble row_res,
				 double units_to_meters, String name) {
    this(new double[] {srow_x[0], srow_x[1], srow_x[2], srow_x[3]},
	 new double[] {srow_y[0], srow_y[1], srow_y[2], srow_y[3]},
	 new double[] {srow_z[0], srow_z[1], srow_z[2], srow_z[3]},
	 row_res, units_to_meters, name);
  }
  /**
   * Constructs a new instance of NiftiSFormCoordinateMap.
   *
   * @param srow_x array containing 1st row of 4X4 transform matrix
   * @param srow_y array containing 2nd row of 4X4 transform matrix
   * @param srow_z array containing 3rd row of 4X4 transform matrix
   * @param row_res resolutions of x, y, and z components of srow arrays
   * @param units_to_meters factor to convert transformed units into meters
   * @param name name for this mapping
   */
  public NiftiSFormCoordinateMap(double[] srow_x, double[] srow_y, double[] srow_z,
				 XYZDouble row_res,
				 double units_to_meters, String name) {
    super(null);
    // 12/28/2017 convert row_res units to meters. Was this always wrong? 
    //this.row_res.setValues(row_res.x*units_to_meters, row_res.y*units_to_meters, row_res.z*units_to_meters);
    this.row_res.setValues(row_res);
    this.units_to_meters = units_to_meters;
    this.srow_x = new double[] {srow_x[0], srow_x[1], srow_x[2], srow_x[3]};
    this.srow_y = new double[] {srow_y[0], srow_y[1], srow_y[2], srow_y[3]};
    this.srow_z = new double[] {srow_z[0], srow_z[1], srow_z[2], srow_z[3]};

    initAffineCoordinateMap(name,
	  new double[][] {
	    {srow_x[0]/row_res.x, srow_x[1]/row_res.y,
	     srow_x[2]/row_res.z, srow_x[3]*units_to_meters},
	    {srow_y[0]/row_res.x, srow_y[1]/row_res.y,
	     srow_y[2]/row_res.z, srow_y[3]*units_to_meters},
	    {srow_z[0]/row_res.x, srow_z[1]/row_res.y,
	     srow_z[2]/row_res.z, srow_z[3]*units_to_meters}},
	  METERS);
  }
  /**
   * Creates a string representation.
   *
   * @return	string representation
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    String class_name=getClass().getName();
    String super_class_name=getClass().getSuperclass().getName();
    sb.append("*** class ").append(class_name).append(" ***\n");
    sb.append("** super class ").append(super_class_name).append("\n");
    sb.append(super.toString());
    sb.append("** end super class ").append(super_class_name).append("\n");
    sb.append("name=").append(getName()).append("\n");
    sb.append("srow_x={");
    sb.append(srow_x[0]).append(",").append(srow_x[1]).append(",");
    sb.append(srow_x[2]).append(",").append(srow_x[3]).append("}\n");
    sb.append("srow_y={");
    sb.append(srow_y[0]).append(",").append(srow_y[1]).append(",");
    sb.append(srow_y[2]).append(",").append(srow_y[3]).append("}\n");
    sb.append("srow_z={");
    sb.append(srow_z[0]).append(",").append(srow_z[1]).append(",");
    sb.append(srow_z[2]).append(",").append(srow_z[3]).append("}\n");
    sb.append("row_res=").append(row_res.toString()).append("\n");
    sb.append("units_to_meters=").append(units_to_meters).append("\n");
    sb.append("*** end class ").append(class_name).append(" ***\n");
    return sb.toString();
  }
  /**

   * Creates a script that may be used to recreate this coordinate map.
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
      variableName = scriptedObjects.addObject(this, "niftisformcoordinatemap");
      sb.append(variableName).append(" = new ").append(classname).append("(\n");
      sb.append("  new double[]{");
      sb.append(srow_x[0]).append(",").append(srow_x[1]).append(",");
      sb.append(srow_x[2]).append(",").append(srow_x[3]).append("},\n");
      sb.append("  new double[]{");
      sb.append(srow_y[0]).append(",").append(srow_y[1]).append(",");
      sb.append(srow_y[2]).append(",").append(srow_y[3]).append("},\n");
      sb.append("  new double[]{");
      sb.append(srow_z[0]).append(",").append(srow_z[1]).append(",");
      sb.append(srow_z[2]).append(",").append(srow_z[3]).append("},\n");
      sb.append("new ").append(XYZDouble.class.getName()).append("(");
      sb.append(row_res.x).append(", ").append(row_res.y).append(", ");
      sb.append(row_res.z).append("),\n");
      sb.append(units_to_meters).append(",\n");
      sb.append("\"").append(getName()).append("\");\n");
    }
    sb.append("script_rtn=").append(variableName).append(";\n");
    sb.append("// -- end ").append(classname).append(" script\n");
    return sb.toString();
  }
}

