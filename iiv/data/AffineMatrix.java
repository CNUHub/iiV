package iiv.data;
import iiv.script.*;
import java.awt.Point;
/**
 * Class to store an affine matrix and handle affine matrix math.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUDimensions
 * @since	iiV1.1.8.2
 */
public class AffineMatrix {
  public final static int X_AXIS = 0;
  public final static int Y_AXIS = 1;
  public final static int Z_AXIS = 2;

  public final static int UNKNOWN = 0;
  public final static int IDENTITY = 1;
  public final static int SCALE = 2;
  public final static int TRANSLATION = 3;
  public final static int ROTATION = 4;
  public final static int COMPOSITE = 5;

  public final static double DEGREES_2_RADIANS = Math.PI / 180.0d;
  public final static double RADIANS_2_DEGREES = 180.0d / Math.PI;

  /**
   * Gets a string representation for a matrix type.
   *
   * @param matrixType matrix type
   * @return string represetation
   */
  public static String matrixTypeToString(int matrixType) {
    switch(matrixType) {
    default:
    case UNKNOWN:
	return "UNKNOWN";
    case IDENTITY:
      return "IDENTITY";
    case SCALE:
      return "SCALE";
    case TRANSLATION:
      return "TRANSLATION";
    case ROTATION:
      return "ROTATION";
    case COMPOSITE:
      return "COMPOSITE";
    }
  }

  private int matrixType = UNKNOWN;
  private Object typeParams = null;

  private double[][] matrix = null;
  private double[][] invMatrix = null;
  /**
   * Constructs a new unitialized instance of AffineMatrix.
   *
   */
  private AffineMatrix() {
  }
  /**
   * Constructs a new instance of AffineMatrix.
   *
   * @param matrix matrix to build affine matrix from.
   *               Internal matrix last row always 0,0,0,1 (only 3X4 portion of input matrix used.)
   *               If matrix is <code>null</code> creates an identity matrix.
   *               Also, missing parts of 3X4 matrix filled in as an identity would be.
   */
  public AffineMatrix(double[][] matrix) {
    this.matrix = new double[4][4];
    for(int i=0; i<3; i++) {
      for(int j=0; j<4; j++) {
	if((matrix != null) && (i < matrix.length) && (j < matrix[i].length)) {
	  this.matrix[i][j] = matrix[i][j];
	}
	else if(i==j) this.matrix[i][j] = 1.0d;
	else this.matrix[i][j] = 0.0d;
      }
    }
    this.matrix[3][0] = 0.0d; this.matrix[3][1] = 0.0d; this.matrix[3][2] = 0.0d; this.matrix[3][3] = 1.0d;
  }
  /**
   * Constructs a matrix that is the combined product of the input matrices.
   *
   * @param matrices array of matrices to combine into one
   * @return single matrix that combines the input matrices
   */
  public static AffineMatrix combineAffineMatrices(AffineMatrix[] matrices) {
    AffineMatrix amatrix = new AffineMatrix();
    amatrix.matrixType = COMPOSITE;
    amatrix.matrix = identityMatrix();
    AffineMatrix[] savearray = new AffineMatrix[matrices.length];
    for(int i=0; i<matrices.length; i++) {
      savearray[i] = matrices[i];
      amatrix.matrix = mat4X4product(amatrix.matrix, matrices[i].matrix);
    }
    amatrix.typeParams = savearray;
    return amatrix;
  }
  /**
   * Constructs a scale affine matrix class object.
   *
   * @param xscale scale along x axis
   * @param yscale scale along y axis
   * @param zscale scale along z axis
   * @return affine matrix for performing translations.
   */
  public static AffineMatrix buildScale(double xscale, double yscale, double zscale) {
    AffineMatrix amatrix = new AffineMatrix();
    amatrix.matrixType = SCALE;
    amatrix.typeParams = new double[] {xscale, yscale, zscale};
    amatrix.matrix = identityMatrix();
    amatrix.matrix[0][0] = xscale;
    amatrix.matrix[1][1] = yscale;
    amatrix.matrix[2][2] = zscale;
    return amatrix;
  }
  /**
   * Constructs a translation affine matrix class object.
   *
   * @param xtrans translation in x direction
   * @param ytrans translation in y direction
   * @param ztrans translation in z direction
   * @return affine matrix for performing translations.
   */
  public static AffineMatrix buildTranslation(double xtrans, double ytrans, double ztrans) {
    AffineMatrix amatrix = new AffineMatrix();
    amatrix.matrixType = TRANSLATION;
    amatrix.typeParams = new double[] {xtrans, ytrans, ztrans};
    amatrix.matrix = identityMatrix();
    amatrix.matrix[0][3] = xtrans;
    amatrix.matrix[1][3] = ytrans;
    amatrix.matrix[2][3] = ztrans;
    return amatrix;
  }
  /**
   * Constructs a rotation affine matrix class object.
   *
   * @param angle angle in radians to rotate by
   * @param xdir x component of vector to rotate around
   * @param ydir y component of vector to rotate around
   * @param zdir z component of vector to rotate around
   * @return affine matrix for performing rotation.
   */
  public static AffineMatrix buildRotation(double angle, double xdir, double ydir, double zdir) {
    AffineMatrix amatrix = new AffineMatrix();
    amatrix.matrixType = ROTATION;
    amatrix.typeParams = new double[] {xdir, ydir, zdir, angle};
    amatrix.matrix = rotationAngleVectorToMatrix(angle, xdir, ydir, zdir);
    return amatrix;
  }
  /**
   * Creates an internal inverse matrix.
   *
   */
  public void initInverseMatrix() {
    if(invMatrix != null) return;
    synchronized (this) {
      if(invMatrix != null) return;
      invMatrix = invertAffineMatrix(matrix);
    }
  }
  /**
   * Validates wether matrix has an inverse.
   *
   * @return <code>true</code> if able to calculate matrix inverse.
   */
  public boolean hasInverse() {
    initInverseMatrix();
    return (invMatrix != null);
  }
  /**
   * Gets the matrix value at the given indices.
   *
   * @param row row to get value for
   * @param column column to get value for
   * @return value from matrix at given row and column
   */
  public double value(int row, int column) {
    return matrix[row][column];
  }
  /**
   * Gets the inverse matrix value at the given indices.
   *
   * @param row row to get value for
   * @param column column to get value for
   * @return value from inverse matrix at given row and column
   */
  public double inverse(int row, int column) {
    if(invMatrix != null) initInverseMatrix();
    return invMatrix[row][column];
  }
  /**
   * Calculates the product of this affine matrix with an double x,y,z value.
   *
   * @param point               array containing (x,y,z) point stored in order as (point[0],point[1],point[2])
   * @param results             array to contain results of product ordered as above.  If <code>null</code> a new array is allocated.
   * @return			array containing results of product.  If results was non-null this will be the same array otherwise a newly allocated array.
   */
  public XYZDouble product(XYZDouble xyz, XYZDouble results) {
    return affineMatProduct(matrix, xyz, results);
  }
  /**
   * Calculates the inverse product of this affine matrix with an double x,y,z value.
   *
   * @param point               array containing (x,y,z) point stored in order as (point[0],point[1],point[2])
   * @param results             array to contain results of product ordered as above.  If <code>null</code> a new array is allocated.
   * @return			array containing results of product.  If results was non-null this will be the same array otherwise a newly allocated array.
   */
  public XYZDouble inverseProduct(XYZDouble xyz, XYZDouble results) {
    return affineMatProduct(invMatrix, xyz, results);
  }
  /**
   * Calculates the product of this affine matrix with an double x,y,z point.
   *
   * @param point               array containing (x,y,z) point stored in order as (point[0],point[1],point[2])
   * @param results             array to contain results of product ordered as above.  If <code>null</code> a new array is allocated.
   * @return			array containing results of product.  If results was non-null this will be the same array otherwise a newly allocated array.
   */
  public double[] product(double[] point, double[] results) {
    return affineMatProduct(matrix, point, results);
  }
  /**
   * Calculates the product of this affine matrix with an integer x,y,z point.
   *
   * @param point               array containing (x,y,z) point stored in order as (point[0],point[1],point[2])
   * @param results             array to contain results of product ordered as above.  If <code>null</code> a new array is allocated.
   * @return			array containing results of product.  If results was non-null this will be the same array otherwise a newly allocated array.
   */
  public double[] product(int[] point, double[] results) {
    return affineMatProduct(matrix, point, results);
  }
  /**
   * Calculates the product of an affine matrix with an double x,y,z point.
   *
   * @param point               array containing (x,y,z) point stored in order as (point[0],point[1],point[2])
   * @param results             array to contain results of product ordered as above.  If <code>null</code> a new array is allocated.
   * @return			array containing results of product.  If results was non-null this will be the same array otherwise a newly allocated array.
   */
  public XYZDouble product(double[] point, XYZDouble results) {
    return affineMatProduct(matrix, point, results);
  }
  /**
   * Calculates the product of an affine matrix with an integer x,y,z point.
   *
   * @param point               array containing (x,y,z) point stored in order as (point[0],point[1],point[2])
   * @param results             array to contain results of product ordered as above.  If <code>null</code> a new array is allocated.
   * @return			array containing results of product.  If results was non-null this will be the same array otherwise a newly allocated array.
   */
  public XYZDouble product(int[] point, XYZDouble results) {
    return affineMatProduct(matrix, point, results);
  }
  /**
   * Calculates the product of the inverse of this affine matrix with an double x,y,z point.
   *
   * @param point               array containing (x,y,z) point stored in order as (point[0],point[1],point[2])
   * @param results             array to contain results of product ordered as above.  If <code>null</code> a new array is allocated.
   * @return			array containing results of product.  If results was non-null this will be the same array otherwise a newly allocated array.
   */
  public double[] inverseProduct(double[] point, double[] results) {
    if(invMatrix == null) initInverseMatrix();
    return affineMatProduct(invMatrix, point, results);
  }
  /**
   * Calculates the product of the inverse of this affine matrix with an integer x,y,z point.
   *
   * @param point               array containing (x,y,z) point stored in order as (point[0],point[1],point[2])
   * @param results             array to contain results of product ordered as above.  If <code>null</code> a new array is allocated.
   * @return			array containing results of product.  If results was non-null this will be the same array otherwise a newly allocated array.
   */
  public double[] inverseProduct(int[] point, double[] results) {
    if(invMatrix == null) initInverseMatrix();
    return affineMatProduct(invMatrix, point, results);
  }
  /**
   * Calculates the product of the inverse of this affine matrix with an double x,y,z point.
   *
   * @param point               array containing (x,y,z) point stored in order as (point[0],point[1],point[2])
   * @param results             array to contain results of product ordered as above.  If <code>null</code> a new array is allocated.
   * @return			array containing results of product.  If results was non-null this will be the same array otherwise a newly allocated array.
   */
  public XYZDouble inverseProduct(double[] point, XYZDouble results) {
    if(invMatrix == null) initInverseMatrix();
    return affineMatProduct(invMatrix, point, results);
  }
  /**
   * Calculates the product of the inverse of this affine matrix with an integer x,y,z point.
   *
   * @param point               array containing (x,y,z) point stored in order as (point[0],point[1],point[2])
   * @param results             array to contain results of product ordered as above.  If <code>null</code> a new array is allocated.
   * @return			array containing results of product.  If results was non-null this will be the same array otherwise a newly allocated array.
   */
  public XYZDouble inverseProduct(int[] point, XYZDouble results) {
    if(invMatrix == null) initInverseMatrix();
    return affineMatProduct(invMatrix, point, results);
  }
  /**
   * Creates a string representation.
   *
   * @return	string representation
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(super.toString()).append("\n");
    sb.append("matrix=");
    sb.append(matrixToString(matrix));
    sb.append("\ninvMatrix=");
    sb.append(matrixToString(invMatrix));
    sb.append("\n");
    sb.append("matrixType=").append(matrixTypeToString(matrixType)).append('\n');
    switch(matrixType) {
    default:
    case UNKNOWN:
    case IDENTITY:
      break;
    case SCALE:
      sb.append("scale factors=").append(CNUTypes.arrayToString(typeParams)).append('\n');
      break;
    case TRANSLATION:
      sb.append("translations=").append(CNUTypes.arrayToString(typeParams)).append('\n');
      break;
    case ROTATION:
      sb.append("rotation angle=").append(((double[]) typeParams)[3]).append(" radians (").append(((double[]) typeParams)[3] * RADIANS_2_DEGREES).append("degrees)\n");
      sb.append("rotate around vector=").append(CNUTypes.arrayToString(typeParams,0,2)).append('\n');
      break;
    case COMPOSITE:
      AffineMatrix[] affineMatrices = (AffineMatrix[]) typeParams;
      for(int i=0; i<affineMatrices.length; i++) {
	sb.append("affineMatrices[").append(i).append("]=\n");
	if(affineMatrices[i] == null) sb.append("null");
	else sb.append(affineMatrices[i].toString());
      }
      break;
    }
    return sb.toString();
  }
  /**
   * Creates a script that may be used to recreate this affine matrix.
   *
   * @param scriptedObjects scripted objects list to add this object to.
   * @return  the script
   */
  public String toScript(CNUScriptObjects scriptedObjects) {
    String className = getClass().getName();
    StringBuffer sb = new StringBuffer();
    sb.append("// -- start ").append(className).append(" script\n");
    if(scriptedObjects == null) scriptedObjects = new CNUScriptObjects();
    String variableName = scriptedObjects.get(this);
    if(variableName == null) {
      variableName = scriptedObjects.addObject(this, "affinematrix");
      switch(matrixType) {
      default:
      case UNKNOWN:
      case IDENTITY:
	sb.append(variableName).append(" = new ").append(className).append("(");
	sb.append(matrixToScript(matrix));
	sb.append(");\n");
	break;
      case SCALE:
	sb.append(variableName).append(" = ").append(className).append(".buildScale(");
	sb.append(((double[])typeParams)[0]).append(',');
	sb.append(((double[])typeParams)[1]).append(',');
	sb.append(((double[])typeParams)[2]).append(");\n");
	break;
      case TRANSLATION:
	sb.append(variableName).append(" = ").append(className).append(".buildTranslation(");
	sb.append(((double[])typeParams)[0]).append(',');
	sb.append(((double[])typeParams)[1]).append(',');
	sb.append(((double[])typeParams)[2]).append(");\n");
	break;
      case ROTATION:
	sb.append(variableName).append(" = ").append(className).append(".buildRotation(");
	sb.append(((double[])typeParams)[0]).append(',');
	sb.append(((double[])typeParams)[1]).append(',');
	sb.append(((double[])typeParams)[2]).append(',');
	sb.append(((double[])typeParams)[3]).append(");\n");
	break;
      case COMPOSITE:	
	AffineMatrix[] affineMatrices = (AffineMatrix[]) typeParams;
	String tmpName = scriptedObjects.addObject(affineMatrices, "affineMatrices");
	sb.append(tmpName).append(" = new ").append(className).append("[").append(affineMatrices.length).append("];\n");
	for(int i=0; i<affineMatrices.length; i++) {
	  if(affineMatrices[i] == null) sb.append(tmpName).append("[").append(i).append("]=null\n");
	  else {
	    sb.append(affineMatrices[i].toScript(scriptedObjects));
	    sb.append(tmpName).append("[").append(i).append("]=script_rtn;\n");
	  }
	}
	sb.append(variableName).append(" = ").append(className).append("combineAffineMatrices(").append(tmpName).append(");\n");
      }
    }
    if(variableName != null) sb.append("script_rtn=").append(variableName).append(";\n");
    else sb.append("script_rtn=null;\n");
    sb.append("// -- end ").append(className).append(" script\n");
    return sb.toString();
  }
  /**
   * Converts an matrix array of double[][] to a bsh script to recreate the matrix.
   *
   * @param matrix matrix to create string representation of
   * @return script
   */
  public final static String matrixToScript(double[][] matrix) {
    if(matrix == null) return "null";
    return "new double[][] " + matrixToString(matrix);
  }
  /**
   * Creates a string representation of a double[][] matrix.
   *
   * @param matrix matrix to create string representation of
   * @return string representation
   */
  public final static String matrixToString(double[][] matrix) {
    StringBuffer sb = new StringBuffer();
    if(matrix == null) sb.append("{null}");
    else {
      sb.append("{\n");
      for(int i=0; i<matrix.length; i++) {
	if(i != 0) sb.append(",\n");
	if(matrix[i] == null) sb.append(" {null}");
	else {
	  sb.append(" {");
	  for(int j=0; j<matrix[i].length; j++) {
	    if(j != 0) sb.append(",");
	    sb.append(" ");
	    sb.append(matrix[i][j]);
	  } // end for(int j=0...
	  sb.append("}");
	}
      } // end for(int i=0....
      sb.append("\n}");
    }
    return sb.toString();
  }
  /**
   * Calculates the inverse of a 3X3 matrix or affine 4X4 matrix assuming the last
   * row is 0 0 0 1.
   *
   * @return	4X4 inverse of input matrix or <code>null</code>.
   */
  public final static double[][] invertAffineMatrix(double[][] rm) {

    double[][] inverseMatrix = null;
    if(rm.length > 2 && rm[0].length > 2 && rm[1].length > 2 && rm[2].length > 2) {
      int size = 3;
      if(rm.length > 3 || rm[0].length > 3 || rm[1].length > 3 || rm[2].length > 3)
	size = 4;
      double[][] tm = new double[size][size];
      // calc upper left 3X3 matrix inversion if determinate not too small
      // Inverse formula Inv(M) = Adj(M)/Det(M)
      // Adj(M) = Cofactors(Transpose(M)) with alternating sign change (-1)^(row+col)
      // calc adjoint
      tm[0][0] = rm[1][1]*rm[2][2] - rm[1][2]*rm[2][1];
      tm[1][0] = rm[1][2]*rm[2][0] - rm[1][0]*rm[2][2]; // -cofactor(rm[0][1])
      tm[2][0] = rm[1][0]*rm[2][1] - rm[1][1]*rm[2][0]; // cofactor(rm[0][2])
      tm[0][1] = rm[0][2]*rm[2][1] - rm[0][1]*rm[2][2]; // -cofactor(rm[1][0])
      tm[1][1] = rm[0][0]*rm[2][2] - rm[0][2]*rm[2][0];
      tm[2][1] = rm[0][1]*rm[2][0] - rm[0][0]*rm[2][1]; // -cofactor(rm[2][1])
      tm[0][2] = rm[0][1]*rm[1][2] - rm[0][2]*rm[1][1]; // cofactor(rm[2][0])
      tm[1][2] = rm[0][2]*rm[1][0] - rm[0][0]*rm[1][2]; // -cofactor(rm[2][1])
      tm[2][2] = rm[0][0]*rm[1][1] - rm[0][1]*rm[1][0];
      // full determinate
      double D = rm[0][0]*tm[0][0] + rm[0][1]*tm[1][0] + rm[0][2]*tm[2][0];
      // can not calculate invert if determinate is too small
      if(Math.abs(D) > 1e-16) {
	double det_inv = 1.0d/D;
	tm[0][0] *= det_inv; tm[0][1] *= det_inv; tm[0][2] *= det_inv;
	tm[1][0] *= det_inv; tm[1][1] *= det_inv; tm[1][2] *= det_inv;
	tm[2][0] *= det_inv; tm[2][1] *= det_inv; tm[2][2] *= det_inv;
	if(size > 3) {
	  double xs = 0d; double ys = 0d; double zs = 0d;
	  if(rm[0].length > 2) xs = -rm[0][3];
	  if(rm[1].length > 2) ys = -rm[1][3];
	  if(rm[2].length > 2) zs = -rm[2][3];
	  // inverse shifts
	  // and calculating inverse shifts with inverse matrix
	  tm[0][3] = tm[0][0]*xs + tm[0][1]*ys + tm[0][2]*zs;
	  tm[1][3] = tm[1][0]*xs + tm[1][1]*ys + tm[1][2]*zs;
	  tm[2][3] = tm[2][0]*xs + tm[2][1]*ys + tm[2][2]*zs;
	  // fill in last row
	  tm[3][0] = tm[3][1] = tm[3][2] = 0d; tm[3][3]=1d;
	}
	inverseMatrix = tm;
      }
    }
    return inverseMatrix;
  }
  /**
   * Create product of two affine matrix assuming last row identity.
   * @param leftMatrix   left side affine matrix
   * @param rightMatrix  right side affine matrix
   * @return affine matrix product
   */
  public final static AffineMatrix affineMatrixProduct(AffineMatrix leftMatrix,
						 AffineMatrix rightMatrix) {
    double[][] p = new double[4][4];
    double[][] lm = leftMatrix.matrix;
    double[][] rm = rightMatrix.matrix;

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

    p[3][0] = 0d; p[3][1] = 0d; p[3][2] = 0d; p[3][3] = 1d;

    return new AffineMatrix(p);
  }

  /**
   * Create product of two 4X4 matrices
   * @param leftMat coordinate map treated like the left side
   *                of a matrix multiplication
   * @param rightMat coordinate map treated like the right side
   *                 of a matrix multiplication
   * @return product
   */
  public final static double[][] mat4X4product(double[][] leftMat,
					       double[][] rightMat) {
    double[][] p = new double[4][4];
    double[][] lm = leftMat;
    double[][] rm = rightMat;

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

    p[3][0] = lm[3][0]*rm[0][0] + lm[3][1]*rm[1][0] + lm[3][2]*rm[3][0] + lm[3][3]*rm[3][0];
    p[3][1] = lm[3][0]*rm[0][1] + lm[3][1]*rm[1][1] + lm[3][2]*rm[3][1] + lm[3][3]*rm[3][1];
    p[3][2] = lm[3][0]*rm[0][2] + lm[3][1]*rm[1][2] + lm[3][2]*rm[3][2] + lm[3][3]*rm[3][2];
    p[3][3] = lm[3][0]*rm[0][3] + lm[3][1]*rm[1][3] + lm[3][2]*rm[3][3] + lm[3][3]*rm[3][3];

    return p;
  }
  /**
   * Calculates the product of an affine matrix with an double x,y,z point.
   *
   * @param affineMat           affine matrix which must be at least 4x3 -- last row of 4x4 not used
   * @param point               array containing (x,y,z) point stored in order as (point[0],point[1],point[2])
   * @param results             array to contain results of product ordered as above.  If <code>null</code> a new array is allocated.
   * @return			array containing results of product.  If results was non-null this will be the same array otherwise a newly allocated array.
   */
  public final static double[] affineMatProduct(double[][] affineMatrix, double[] point, double[] results) {
    double[] prod;
    if(results != null) prod=results;
    else prod = new double[3];
    prod[0] = affineMatrix[0][0] * point[0] +  affineMatrix[0][1] * point[1] +  affineMatrix[0][2] * point[2] + affineMatrix[0][3];
    prod[1] = affineMatrix[1][0] * point[0] +  affineMatrix[1][1] * point[1] +  affineMatrix[1][2] * point[2] + affineMatrix[1][3];
    prod[2] = affineMatrix[2][0] * point[0] +  affineMatrix[2][1] * point[1] +  affineMatrix[2][2] * point[2] + affineMatrix[2][3];
    return prod;
  }
  /**
   * Calculates the product of an affine matrix with an integer x,y,z point.
   *
   * @param affineMat           affine matrix which must be at least 4x3 -- last row of 4x4 not used
   * @param point               array containing (x,y,z) point stored in order as (point[0],point[1],point[2])
   * @param results             array to contain results of product ordered as above.  If <code>null</code> a new array is allocated.
   * @return			array containing results of product.  If results was non-null this will be the same array otherwise a newly allocated array.
   */
  public final static double[] affineMatProduct(double[][] affineMatrix, int[] point, double[] results) {
    double[] prod;
    if(results != null) prod=results;
    else prod = new double[3];
    prod[0] = affineMatrix[0][0] * point[0] +  affineMatrix[0][1] * point[1] +  affineMatrix[0][2] * point[2] + affineMatrix[0][3];
    prod[1] = affineMatrix[1][0] * point[0] +  affineMatrix[1][1] * point[1] +  affineMatrix[1][2] * point[2] + affineMatrix[1][3];
    prod[2] = affineMatrix[2][0] * point[0] +  affineMatrix[2][1] * point[1] +  affineMatrix[2][2] * point[2] + affineMatrix[2][3];
    return prod;
  }
  /**
   * Calculates the product of an affine matrix with an double x,y,z point.
   *
   * @param affineMat           affine matrix which must be at least 4x3 -- last row of 4x4 not used
   * @param point               array containing (x,y,z) point stored in order as (point[0],point[1],point[2])
   * @param results             array to contain results of product ordered as above.  If <code>null</code> a new array is allocated.
   * @return			array containing results of product.  If results was non-null this will be the same array otherwise a newly allocated array.
   */
  public final static XYZDouble affineMatProduct(double[][] affineMatrix, double[] point, XYZDouble results) {
    XYZDouble prod;
    if(results != null) prod=results;
    else prod = new XYZDouble();
    prod.x = affineMatrix[0][0] * point[0] +  affineMatrix[0][1] * point[1] +  affineMatrix[0][2] * point[2] + affineMatrix[0][3];
    prod.y = affineMatrix[1][0] * point[0] +  affineMatrix[1][1] * point[1] +  affineMatrix[1][2] * point[2] + affineMatrix[1][3];
    prod.z = affineMatrix[2][0] * point[0] +  affineMatrix[2][1] * point[1] +  affineMatrix[2][2] * point[2] + affineMatrix[2][3];
    return prod;
  }
  /**
   * Calculates the product of an affine matrix with an integer x,y,z point.
   *
   * @param affineMat           affine matrix which must be at least 4x3 -- last row of 4x4 not used
   * @param point               array containing (x,y,z) point stored in order as (point[0],point[1],point[2])
   * @param results             array to contain results of product ordered as above.  If <code>null</code> a new array is allocated.
   * @return			array containing results of product.  If results was non-null this will be the same array otherwise a newly allocated array.
   */
  public final static XYZDouble affineMatProduct(double[][] affineMatrix, int[] point, XYZDouble results) {
    XYZDouble prod;
    if(results != null) prod=results;
    else prod = new XYZDouble();
    prod.x = affineMatrix[0][0] * point[0] +  affineMatrix[0][1] * point[1] +  affineMatrix[0][2] * point[2] + affineMatrix[0][3];
    prod.y = affineMatrix[1][0] * point[0] +  affineMatrix[1][1] * point[1] +  affineMatrix[1][2] * point[2] + affineMatrix[1][3];
    prod.z = affineMatrix[2][0] * point[0] +  affineMatrix[2][1] * point[1] +  affineMatrix[2][2] * point[2] + affineMatrix[2][3];
    return prod;
  }
  /**
   * Calculates the product of an affine matrix with an XYZDouble.
   *
   * @param affineMat           affine matrix which must be at least 4x3 -- last row of 4x4 not used
   * @param xyz                 xyz values to multiply
   * @param results             array to contain results of product ordered as above.  If <code>null</code> a new array is allocated.
   * @return			array containing results of product.  If results was non-null this will be the same array otherwise a newly allocated array.
   */
  public final static XYZDouble affineMatProduct(double[][] affineMatrix, XYZDouble xyz, XYZDouble results) {
    XYZDouble prod;
    if(results != null) prod=results;
    else prod = new XYZDouble();
    prod.x = affineMatrix[0][0] * xyz.x +  affineMatrix[0][1] * xyz.y +  affineMatrix[0][2] * xyz.z + affineMatrix[0][3];
    prod.y = affineMatrix[1][0] * xyz.x +  affineMatrix[1][1] * xyz.y +  affineMatrix[1][2] * xyz.z + affineMatrix[1][3];
    prod.z = affineMatrix[2][0] * xyz.x +  affineMatrix[2][1] * xyz.y +  affineMatrix[2][2] * xyz.z + affineMatrix[2][3];
    return prod;
  }
  /**
   * Builds a 4X4 identity matrix.
   *
   */
  public final static double[][] identityMatrix() {
    // first build identity matrix
    double[][] results = new double[4][4];
    for(int i=0; i<results.length; i++) {
      for(int j=0; j<results[i].length; j++) {
	if(i == j) results[i][j] = 1d;
	else results[i][j] = 0d;
      }
    }
    return results;
  }
  /**
   * Builds a 4X4 rotation matrix of given angle around given axis.
   *
   * @param axis one of X_AXIS, Y_AXIS, or Z_AXIS
   * @param angle rotation angle in radians
   */
  public final static double[][] rotationMatrix(int axis, double angle) {
    // first build identity matrix
    double[][] results = identityMatrix();
    double cos_angle = Math.cos(angle);
    double sin_angle = Math.sin(angle);
    switch(axis) {
    default:
    case X_AXIS:
      results[1][1] =  cos_angle;  results[1][2] = sin_angle;
      results[2][1] = -sin_angle;  results[2][2] = cos_angle;
      break;
    case Y_AXIS:
      results[0][0] = cos_angle;  results[0][2] = -sin_angle;
      results[2][0] = sin_angle;  results[2][2] = cos_angle;
      break;
    case Z_AXIS:
      results[0][0] = cos_angle;   results[0][1] = sin_angle;
      results[1][0] = -sin_angle;   results[1][1] =  cos_angle;
      break;
    }
    return results;
  }
  /**
   * Constructs a 4X4 rotation matrix.
   *
   * @param angle angle in radians to rotate by
   * @param xdir x component of vector to rotate around
   * @param ydir y component of vector to rotate around
   * @param zdir z component of vector to rotate around
   * @return matrix for performing rotation
   */
  public final static double[][] rotationAngleVectorToMatrix(double angle, double xdir, double ydir, double zdir) {
    // make sure vector is normalized -- norm close to zero would cause problems -- hopefully caller wouldn't do that
    double nf = Math.sqrt(xdir*xdir + ydir*ydir + zdir*zdir);
    double e1 = xdir/nf; double e2 = ydir/nf; double e3 = zdir/nf;
    // form cos and sin values
    double cos_a = Math.cos(angle);
    double sin_a = Math.sin(angle);
    double one_m_cos_a = 1d - cos_a;
    // form vector cross product elements
    double e1e1 = e1 * e1; double e2e2 = e2 * e2; double e3e3 = e3 * e3;
    double e1e2 = e1 * e2; double e1e3 = e1 * e3;
    double e2e3 = e2 * e3;
    // using Rodrigues' rotation formula from http://en.wikipedia.org/wiki/Rotation_representation_(mathematics)
    double[][] matrix = new double[4][4];
    matrix[0][0] = cos_a              + one_m_cos_a * e1e1 - 0d;
    matrix[0][1] = 0d                 + one_m_cos_a * e1e2 - (sin_a * (-e3));
    matrix[0][2] = 0d                 + one_m_cos_a * e1e3 - (sin_a * e2);

    matrix[1][0] = 0d                 + one_m_cos_a * e1e2 - (sin_a * e3);
    matrix[1][1] = cos_a              + one_m_cos_a * e2e2 - 0d;
    matrix[1][2] = 0d                 + one_m_cos_a * e2e3 - (sin_a * (-e1));

    matrix[2][0] = 0d                 + one_m_cos_a * e1e3 - (sin_a * (-e2));
    matrix[2][1] = 0d                 + one_m_cos_a * e2e3 - (sin_a * e1);
    matrix[2][2] = cos_a              + one_m_cos_a * e3e3 - 0d;

    // fill in remainder of 4x4 matrix
    matrix[0][3] = 0d; matrix[1][3] = 0d; matrix[2][3] = 0d;
    matrix[3][0] = 0d; matrix[3][1] = 0d; matrix[3][2] = 0d; matrix[3][3] = 1d;

    return matrix;
  }
}
