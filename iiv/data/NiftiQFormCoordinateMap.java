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
public class NiftiQFormCoordinateMap  extends AffineCoordinateMap {
  private String name = null;
  private XYZDouble scale = null;
  private XYZDouble invscale = null;
  private XYZDouble qoffsets = null;
  private double[][] rotationMatrix=null;
  private double a, b, c, d;
  private double qfac;
  /**
   * Constructs a new instance of NiftiQFormCoordinateMap.
   */
  public NiftiQFormCoordinateMap(double quatern_b, double quatern_c, double quatern_d,
				 double qoffset_x, double qoffset_y, double qoffset_z,
				 double pixdim1, double pixdim2, double pixdim3,
				 double qfac, String name) {
    super(null);

    qoffsets = new XYZDouble(qoffset_x, qoffset_y, qoffset_z);

    // qfac must be 1 or -1  -- defaults to 1
    if(Math.abs(qfac + 1.0) <= 1e-16) this.qfac = -1.0;
    else this.qfac = 1.0;

    scale = new XYZDouble(pixdim1, pixdim2, this.qfac * pixdim3);
    invscale = new XYZDouble(1.0d/scale.x, 1.0d/scale.y, 1.0d/scale.z);

    b = quatern_b; c = quatern_c; d = quatern_d;
    a = 1.0-(b*b+c*c+d*d);
    if( a < 1e-7) {
      // special case as in http://nifthi.nimh.nig.gov/pub/dist/src/niftilib/nifti1_io.c example
      // normalize (b, c, d) vector a=0 --> 180 degree rotation
      double nf = 1.0 / Math.sqrt(b*b + c*c + d*d);
      b *= nf; c *= nf; d *= nf;
      a = 0;
    } else a = Math.sqrt(a);


    rotationMatrix = new double[3][4];
    rotationMatrix[0][0] = a*a+b*b-c*c-d*d;
    rotationMatrix[0][1] = 2*b*c-2*a*d;
    rotationMatrix[0][2] = (2*b*d+2*a*c) * this.qfac;

    rotationMatrix[1][0] = 2*b*c+2*a*d;
    rotationMatrix[1][1] = a*a+c*c-b*b-d*d;
    rotationMatrix[1][2] = (2*c*d-2*a*b) * this.qfac;

    rotationMatrix[2][0] = 2*b*d-2*a*c;
    rotationMatrix[2][1] = 2*c*d+2*a*b;
    rotationMatrix[2][2] = (a*a+d*d-c*c-b*b) * this.qfac;

    rotationMatrix[0][3] = qoffset_x;
    rotationMatrix[1][3] = qoffset_y;
    rotationMatrix[2][3] = qoffset_z;

    initAffineCoordinateMap(name, rotationMatrix, METERS);
  }
  /**
   * Creates a string representation.
   *
   * @return	string representation
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    String class_string=getClass().getName();
    sb.append("*** class ").append(class_string).append(" ***\n");
    sb.append(super.toString());
    sb.append("name=").append(name).append("\n");
    sb.append("quatern_a(calculated)=").append(a).append("\n");
    sb.append("quatern_b=").append(b);
    sb.append(" quatern_c=").append(c);
    sb.append(" quatern_d=").append(d).append("\n");
    sb.append("qoffsets=").append(qoffsets.toString()).append("\n");
    sb.append("pixdim1=").append(scale.x).append(" pixdim2=").append(scale.y).append(" pixdim3=").append(scale.z*qfac).append("\n");
    sb.append("qfac=").append(qfac).append("\n");
    sb.append("scale=").append(scale.toString()).append("\n");
    sb.append("rotation matrix(calculated)=").append("\n");
    sb.append("{ {").append(rotationMatrix[0][0]).append(",").append(rotationMatrix[0][1]).append(",").append(rotationMatrix[0][2]).append("}\n");
    sb.append("  {").append(rotationMatrix[1][0]).append(",").append(rotationMatrix[1][1]).append(",").append(rotationMatrix[1][2]).append("}\n");
    sb.append("  {").append(rotationMatrix[2][0]).append(",").append(rotationMatrix[2][1]).append(",").append(rotationMatrix[2][2]).append("} }\n");
    sb.append("*** end class ").append(class_string).append(" ***\n");

    return sb.toString();
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
      variableName = scriptedObjects.addObject(this, "niftiqformcoordinatemap");
      sb.append(variableName).append(" = new ").append(classname).append("(\n");
      sb.append(b).append(", ").append(c).append(", ").append(d).append(",\n");
      sb.append(qoffsets.x).append(", ").append(qoffsets.y).append(", ").append(qoffsets.z).append(",\n");
      sb.append(scale.x).append(", ").append(scale.y).append(", ").append(scale.z * qfac).append(",\n");
      sb.append(qfac).append(", \"");
      sb.append(getName());
      sb.append("\");\n");
    }
    sb.append("script_rtn=").append(variableName).append(";\n");
    sb.append("// -- end ").append(classname).append(" script\n");
    return sb.toString();
  }
}
