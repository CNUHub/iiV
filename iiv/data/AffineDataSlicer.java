package iiv.data;
import iiv.script.*;
import java.awt.Point;
/**
 * Class to convert data array into slices.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUDimensions
 * @since	iiV1.1.8
 */
public class AffineDataSlicer implements CNUDataSlicer {
  private CNUDimensions inDataDims = null;
  private AffineMatrix affineMatrix = null;
  private boolean reslicedDataDimsSpecified = false;
  private CNUDimensions reslicedDataDims = null;
  private boolean reslicedOrigSpecified = false;
  private XYZDouble reslicedOrig = new XYZDouble(0d,0d,0d);
  private int[] inc = null;

  private int sliceViewMode;
  private int outType = CNUTypes.UNSIGNED_INTEGER;
  private int sliceDim = 0;
  private CNUDimensions sliceDims = null;

  private XYZDouble inputxstep;
  private XYZDouble inputystep;

  /**
   * Constructs a new instance of PrimaryOrthoDataSlicer.
   *
   * @param inDataDims dimensions of data this data slicer will handle
   * @param inMatrix matrix representing the affine transform
   * @param sliceViewMode orientations of slices to create
   * @param output data type.
   */
  public AffineDataSlicer(CNUDimensions inDataDims, double[][] inMatrix, int sliceViewMode, int outType) {
    this(inDataDims, new AffineMatrix(inMatrix), sliceViewMode, outType);
  }
  /**
   * Constructs a new instance of PrimaryOrthoDataSlicer.
   *
   * @param inDataDims dimensions of data this data slicer will handle
   * @param affineMatrix affine matrix that defines the reslicing
   * @param sliceViewMode orientations of slices to create
   * @param output data type.
   */
  public AffineDataSlicer(CNUDimensions inDataDims, AffineMatrix affineMatrix, int sliceViewMode, int outType) {
    this(inDataDims, affineMatrix, (CNUDimensions) null, (XYZDouble) null, sliceViewMode, outType);
  }
  /**
   * Constructs a new instance of PrimaryOrthoDataSlicer.
   *
   * @param inDataDims dimensions of data this data slicer will handle
   * @param affineMatrix affine matrix that defines the reslicing
   * @param sliceViewMode orientations of slices to create
   * @param output data type.
   */
  public AffineDataSlicer(CNUDimensions inDataDims, AffineMatrix affineMatrix,
			  CNUDimensions inReslicedDataDims, XYZDouble inReslicedOrig,
			  int sliceViewMode, int outType) {
    this.inDataDims = (CNUDimensions) inDataDims.clone();
    this.affineMatrix = affineMatrix;
    reslicedDataDimsSpecified = (inReslicedDataDims != null);
    reslicedOrigSpecified = (inReslicedOrig != null);
    this.sliceViewMode = sliceViewMode;
    this.outType = outType;
    if(reslicedDataDimsSpecified) {
      this.reslicedDataDims = (CNUDimensions) inReslicedDataDims.clone();
      if(reslicedOrigSpecified) this.reslicedOrig.setValues(inReslicedOrig);
    }
    else if(reslicedOrigSpecified) {
      this.reslicedDataDims = calcMinBoundingDimensions(inDataDims, affineMatrix, null);
      this.reslicedOrig.setValues(inReslicedOrig);
    }
    else this.reslicedDataDims = calcMinBoundingDimensions(inDataDims, affineMatrix, this.reslicedOrig);

    sliceDim = PrimaryOrthoDataSlicer.getSliceNumberDimension(this.reslicedDataDims, sliceViewMode);
    sliceDims = PrimaryOrthoDataSlicer.getSliceDimensions(this.reslicedDataDims, sliceViewMode, outType);
    inc = PrimaryOrthoDataSlicer.getInputToSliceIncrements(this.reslicedDataDims, sliceViewMode);

    int[] xNormVector = this.reslicedDataDims.getPoint(inc[0]);
    inputxstep = affineMatrix.inverseProduct(xNormVector, (XYZDouble) null);
    inputxstep.x -= affineMatrix.inverse(0,3); inputxstep.y -= affineMatrix.inverse(1,3); inputxstep.z -= affineMatrix.inverse(2,3);

    int[] yNormVector = this.reslicedDataDims.getPoint(inc[1]);
    inputystep = affineMatrix.inverseProduct(yNormVector, (XYZDouble) null);
    inputystep.x -= affineMatrix.inverse(0,3); inputystep.y -= affineMatrix.inverse(1,3); inputystep.z -= affineMatrix.inverse(2,3);
  }
  /**
   * Gets an equivalent data slicer that will work with a new set of dimensions.
   *
   * @param newDims dimensions to get equivalent data slicer for
   * @return an equivalent data slicer for the new dimensions
   **/
  public CNUDataSlicer getEquivalentDataSlicer(CNUDimensions newDims) {
    if(newDims == null) return null;
    if(newDims.equals(inDataDims)) return this;
    return new AffineDataSlicer(newDims, affineMatrix, reslicedDataDimsSpecified ? reslicedDataDims : null,
				reslicedOrigSpecified ? reslicedOrig : null, sliceViewMode, outType);
  }
  /**
   * Returns the affine matrix used by this data slicer.
   *
   * @return the affine matrix used by this data slicer.
   */
  public AffineMatrix getAffineMatrix() {
    return affineMatrix;
  }
  /**
   * Returns a copy of the input data dimensions the data slicer uses.
   *
   * @return the input data dimensions the data slicer uses
   */
  public CNUDimensions getInDimensions() {
    return (CNUDimensions) inDataDims.clone();
  }
  /**
   * Returns the slice view mode the data slicer uses.
   *
   * @return  the slice view mode the data slicer uses
   */
  public int getSliceViewMode() {
    return sliceViewMode;
  }
  /**
   * Returns the dimension of slices this data slicer creates.
   *
   * @return the slice dimensions this data slicer creates
   */
  public CNUDimensions getSliceDimensions() {
    return (CNUDimensions) sliceDims.clone();
  }
  /**
   * Returns number of slices supported by the data and slice view.
   *
   * @return  valid slice number for this data slicer.
   */
  public int getNumberOfSlices() {
    return reslicedDataDims.getDim(sliceDim);
  }
  /**
   * Gets the indices corresponding to the original data
   * based on a point relative to this slice.
   * Note - dims beyond the 3rd not handled by this.
   *
   * @param pt	point to get indice for
   * @param slice slice the point is on
   * @return	the indices to the data
   */
  public int[] getDataIndicesFromSlicePoint(Point pt, int slice) {
    if(pt == null) return null;

    double[] sliceOrig = new double[reslicedDataDims.getNumberOfDimensions()];
    for(int i=0; i<sliceOrig.length; i++) sliceOrig[i] = 0;
    if(slice > 0) sliceOrig[sliceDim] = slice;

    sliceOrig[0] += reslicedOrig.x; sliceOrig[1] += reslicedOrig.y; sliceOrig[2] += reslicedOrig.z;

    // calc new inputstart
    XYZDouble xyzDindices = affineMatrix.inverseProduct(sliceOrig, (XYZDouble) null);

    xyzDindices.x += (inputxstep.x * pt.x) + (inputystep.x * pt.y);
    xyzDindices.y += (inputxstep.y * pt.x) + (inputystep.y * pt.y);
    xyzDindices.z += (inputxstep.z * pt.x) + (inputystep.z * pt.y);

    int[] indices = new int[3];
    indices[0] = (int) Math.round(xyzDindices.x);
    indices[1] = (int) Math.round(xyzDindices.y);
    indices[2] = (int) Math.round(xyzDindices.z);

    return indices;
  }
 /**
   * Gets the point relative to this slice given the indices
   * corresponding to the original data.
   *
   * @param	indices indices to original data which may have
   *		any number of dimensions
   * @return	location relative to slice (negative value
   *		for x or y indicates the indice does not map to that dimension)
   */
  public Point getSlicePointFromDataIndices(int[] indices) {
    Point pt = new Point(-1, -1);
    if(indices == null) return pt;
    // to do matrix multiply indices.length must be at least 3
    if(indices.length < 3) {
      int[] tmpindices = new int[3];
      for(int i=0; i<3; i++) {
	if(i < indices.length) tmpindices[i] = indices[i];
	else tmpindices[i] = 0;
      }
      indices = tmpindices;
    }

    double[] reslicedindices = affineMatrix.product(indices, (double[]) null); // why indices have to be at least 3
    reslicedindices[0] -= reslicedOrig.x; reslicedindices[1] -= reslicedOrig.y; reslicedindices[2] -= reslicedOrig.z;

    int [] inIncOrig = reslicedDataDims.getIncrements();
    int lesserlength = (inIncOrig.length < reslicedindices.length) ? inIncOrig.length : reslicedindices.length;
    for(int k = 0; k < lesserlength; k++) {
      if(inc[0] == inIncOrig[k]) {
	pt.x = (int) Math.round(reslicedindices[k]);
      }
      else if(inc[1] == inIncOrig[k]) {
	pt.y = (int) Math.round(reslicedindices[k]);
      }
    }
    return pt;
  }
  /**
   * Maps indices relative to input data to output indices via the affine matrix.
   *
   * @param origIndices indices relative to underlying data
   * @return            indices relative to output slices.  Output indices might not be on the current slice.
   */
  public int[] mapIndices(int[] indices) {
    if(indices == null) return null;
    // to do matrix multiply indices.length must be at least 3
    if(indices.length < 3) {
      int[] tmpindices = new int[3];
      for(int i=0; i<3; i++) {
	if(i < indices.length) tmpindices[i] = indices[i];
	else tmpindices[i] = 0;
      }
      indices = tmpindices;
    }
    double[] reslicedindices = affineMatrix.product(indices, (double[]) null);
    reslicedindices[0] -= reslicedOrig.x; reslicedindices[1] -= reslicedOrig.y; reslicedindices[2] -= reslicedOrig.z;

    int[] outIndices = new int[indices.length];
    for(int k=0; k < outIndices.length; k++) {
      if(k < reslicedindices.length) outIndices[k] = (int) Math.round(reslicedindices[k]);
      else outIndices[k] = indices[k];
    }
    return outIndices;
  }
  /**
   * Gets the slice number associated with a data indices location.
   *
   * @param indices	raw data location to determine slice number for
   * @return		slice number or <code>-1</code> if not contained in data.
   */
  public int getSliceNumberFromDataIndices(int[] indices) {
    if(! inDataDims.contains(indices)) return -1;
    int[] mappedIndices = mapIndices(indices);
    if(sliceDim < mappedIndices.length) return mappedIndices[sliceDim];
    return 0;
  }
  /**
   * Determines if the given indices relative to the input data are located
   * in the given slice.
   *
   * @param     slice   slice to test for
   * @param	indices indices to original raw data
   * @return	<code>true</code> if indices are located in the given slice.
   */
  public boolean inSlice(int slice, int[] indices) {
    if(indices == null) return false;
    return (slice == getSliceNumberFromDataIndices(indices));
  }
  /**
   * Copies single slice into a 2-dimensional CNUData array.
   *
   * @param inData data to copy from.
   * @param slice slice value relative to this resliced object to copy.
   * @param iValue i dimension relative to input data to copy slice from
   * @param singlePlane plane to copy data into.  if <code>null</code> creates and copies into a new CNUData object.
   * @param sc scale object for converting input data type to output data type.  May be <code>null</code>.
   * @return CNUData object with scaled copy of slice data.
   */
  public CNUData grabSliceData(CNUData inData, int slice, int iValue, CNUData singlePlane, CNUScale sc) {
    // check input data
    if(inData == null || ! inData.sameDimensions(inDataDims)) return null;
    // make sure output plane is sized correctly
    CNUDimensions outdims = null;
    if(singlePlane == null) {
      singlePlane = new CNUData();
      singlePlane.initDataArray(sliceDims);
      outdims = sliceDims;
    }
    else {
      outdims = singlePlane.getDimensions();
      if(outdims == null) {
	singlePlane.initDataArray(sliceDims);
	outdims = sliceDims;
      }
      else if(! outdims.sameSize(sliceDims)) return null;
    }

    double[] sliceOrig = new double[reslicedDataDims.getNumberOfDimensions()];
    for(int i=0; i<sliceOrig.length; i++) sliceOrig[i] = 0;
    if(slice > 0) sliceOrig[sliceDim] = slice;

    sliceOrig[0] += reslicedOrig.x; sliceOrig[1] += reslicedOrig.y; sliceOrig[2] += reslicedOrig.z;

    // calc inputstart
    XYZDouble inputxloc = affineMatrix.inverseProduct(sliceOrig, (XYZDouble) null);


    XYZDouble inputyloc = new XYZDouble();
    int[] inpoint = new int[4];
    if(iValue > 0) inpoint[3] = iValue;
    else inpoint[3] = 0;
    int[] outpoint = new int[2];
    int xdim = sliceDims.xdim();
    int ydim = sliceDims.ydim();
    for(outpoint[0]=0; outpoint[0]<xdim;  outpoint[0]++, inputxloc.add(inputxstep)) {
      //System.out.println("AffineDataSlicer.grabSliceData outpoint[0]=" + outpoint[0] + " inputxloc=" + inputxloc + "\n");
      inputyloc.setValues(inputxloc);
      for(outpoint[1]=0;  outpoint[1]<ydim;  outpoint[1]++, inputyloc.add(inputystep)) {
	inpoint[0] = (int) Math.round(inputyloc.x);
	inpoint[1] = (int) Math.round(inputyloc.y);
	inpoint[2] = (int) Math.round(inputyloc.z);
	if(inDataDims.contains(inpoint)) {
	  if(sc == null) singlePlane.setPixel(outpoint, inData.getPixelAsDouble(inpoint));
	  else singlePlane.setPixel(outpoint, sc.convert(inData.getPixelAsDouble(inpoint)));
	}
	else singlePlane.setPixel(outpoint, 0);
      }
    }
    return singlePlane;
  }
  /**
   * Determines a minimum dimension (x, y or z) to cover the whole area from a given input dimension transformed by an affine matrix.
   *
   * @param inDims input dimensions that need to be covered
   * @param affineMatrix transform for converting input locations to output locations
   * @param reslicedMinOrig if not null used to return transformed minimum values associated with inDims
   * @return dimensions required for output
   */
  public final static CNUDimensions calcMinBoundingDimensions(CNUDimensions inDims, AffineMatrix affineMatrix,
							      XYZDouble rtn_min_origin) {
    // find extremes by searching all 8 input corners
    XYZDouble max = new XYZDouble();
    XYZDouble min = new XYZDouble();
    int[] xends = {0, inDims.xdim() - 1};
    int[] yends = {0, inDims.ydim() - 1};
    int[] zends = {0, inDims.zdim() - 1};
    // first corner
    int[] corner = new int[3];
    corner[0] = 0; corner[1] = 0; corner[2] = 0;
    double[] resliced_corner = new double[3];
    affineMatrix.product(corner, resliced_corner); // transform origin corner to resliced space
    max.x = resliced_corner[0]; min.x = resliced_corner[0];   // initialize max, min value
    max.y = resliced_corner[1]; min.y = resliced_corner[1];
    max.z = resliced_corner[2]; min.z = resliced_corner[2];
    // search remaining 7 corners for max, min values
    for(int n=1; n<8; n++) {       //  xy z        xy z
      corner[0] = xends[n&1];      // 01234567 -> 01010101
      corner[1] = yends[(n>>1)&1]; // 01234567 -> 00110011
      corner[2] = zends[(n>>2)&1]; // 01234567 -> 00001111
      affineMatrix.product(corner, resliced_corner);  // transform corner to resliced space
      if(resliced_corner[0] > max.x) max.x = resliced_corner[0];
      else if(resliced_corner[0] < min.x) min.x = resliced_corner[0];
      if(resliced_corner[1] > max.y) max.y = resliced_corner[1];
      else if(resliced_corner[1] < min.y) min.y = resliced_corner[1];
      if(resliced_corner[2] > max.z) max.z = resliced_corner[2];
      else if(resliced_corner[2] < min.z) min.z = resliced_corner[2];
    } // end for(n ... )
    // use max, min values to calculate resliced dimensions
    int[] tmp = new int[3];
    tmp[0] = (int) Math.ceil(max.x - min.x) + 1;
    tmp[1] = (int) Math.ceil(max.y - min.y) + 1;
    tmp[2] = (int) Math.ceil(max.z - min.z) + 1;
    CNUDimensions reslicedDataDims = new CNUDimensions(tmp, inDims.getType(), 0);
    if(rtn_min_origin != null) rtn_min_origin.setValues(min); // use min as shift -- becoming origin (0, 0, 0) in resliced space
    // Ok, so what happens to the resolutions?  The original res may not be square.
    // The transform may scale one or more dimensions.
    // The resliced res is not used by singleimg because it always maps the points back to original
    // data indices before coordinate mapping.
    // But, I like to have this slicer complete so maybe it should be calculated.
    reslicedDataDims.setSpatialResolutions(calculateOutputResolutions(inDims.getSpatialResolutions(), affineMatrix));
    return reslicedDataDims;
  }
  /**
   * Calculates the transformed output resolutions given input resolutions and an the transform's affine matrix.
   *
   * @param inRes input resolutions
   * @param affineMatrix transform as an affine matrix
   * @return resolution as distances between integer values in transformed space
   */
  public final static double[] calculateOutputResolutions(double[] inres, AffineMatrix affineMatrix) {
    if(inres != null && inres.length > 2) {
      double[] outres = new double[3];
      int[] corner = new int[3];
      double[] resliced_corner = new double[3];
      corner[0] = 0; corner[1] = 0; corner[2] = 0;
      double[] inverted_orig = affineMatrix.inverseProduct(corner, (double[]) null);
      // when the out location moves one unit in x how far does the associated input move?
      corner[0] = 1; corner[1] = 0; corner[2] = 0;
      resliced_corner =  affineMatrix.inverseProduct(corner, resliced_corner);
      resliced_corner[0] = (resliced_corner[0] - inverted_orig[0]) * inres[0]; 
      resliced_corner[1] = (resliced_corner[1] - inverted_orig[1]) * inres[1]; 
      resliced_corner[2] = (resliced_corner[2] - inverted_orig[2]) * inres[2]; 
      outres[0] = Math.sqrt(resliced_corner[0]*resliced_corner[0] + resliced_corner[1]*resliced_corner[1] +
			    resliced_corner[2]*resliced_corner[2]);
      // when the out location moves one unit in y how far does the associated input move?
      corner[0] = 0; corner[1] = 1; corner[2] = 0;
      resliced_corner =  affineMatrix.inverseProduct(corner, resliced_corner);
      resliced_corner[0] = (resliced_corner[0] - inverted_orig[0]) * inres[0]; 
      resliced_corner[1] = (resliced_corner[1] - inverted_orig[1]) * inres[1]; 
      resliced_corner[2] = (resliced_corner[2] - inverted_orig[2]) * inres[2]; 
      outres[1] = Math.sqrt(resliced_corner[0]*resliced_corner[0] + resliced_corner[1]*resliced_corner[1] +
			    resliced_corner[2]*resliced_corner[2]);
      // when the out location moves one unit in z how far does the associated input move?
      corner[0] = 0; corner[1] = 0; corner[2] = 1;
      resliced_corner =  affineMatrix.inverseProduct(corner, resliced_corner);
      resliced_corner[0] = (resliced_corner[0] - inverted_orig[0]) * inres[0]; 
      resliced_corner[1] = (resliced_corner[1] - inverted_orig[1]) * inres[1]; 
      resliced_corner[2] = (resliced_corner[2] - inverted_orig[2]) * inres[2]; 
      outres[2] = Math.sqrt(resliced_corner[0]*resliced_corner[0] + resliced_corner[1]*resliced_corner[1] +
			    resliced_corner[2]*resliced_corner[2]);
      return outres;
    }
    return null;
  }
  /**
   * Returns a string representation of this data slicer.
   *
   * @return	string representation
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(super.toString()).append("\n");
    sb.append("inDataDims=").append(inDataDims.toString());
    sb.append("affineMatrix=").append(affineMatrix.toString());
    sb.append("sliceViewMode=");
    sb.append(CNUDimensions.orientationToString(sliceViewMode)).append('\n');
    sb.append("outType=");
    sb.append(CNUTypes.typeToString(outType)).append('\n');
    sb.append("reslicedDataDimsSpecified=").append(reslicedDataDimsSpecified).append('\n');
    sb.append("reslicedDataDims=").append(reslicedDataDims.toString());
    sb.append("reslicedOrigSpecified=").append(reslicedOrigSpecified).append('\n');
    sb.append("reslicedOrig=").append(reslicedOrig.toString()).append('\n');
    sb.append("sliceDim=").append(sliceDim).append('\n');
    sb.append("sliceDims=").append(sliceDims.toString());
    sb.append("inc=").append(CNUTypes.arrayToString(inc)).append("\n");
    sb.append("inputxstep=").append(inputxstep.toString()).append("\n");
    sb.append("inputystep=").append(inputystep.toString()).append("\n");
    return sb.toString();
  }
  /**
   * Creates a script that may be used to recreate this data slicer.
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
      variableName = scriptedObjects.addObject(this, "affinedataslicer");
      // dimensions
      sb.append(inDataDims.toScript(scriptedObjects));
      sb.append("indimstmp=script_rtn;\n");
      sb.append(affineMatrix.toScript(scriptedObjects));
      sb.append("affinematrixtmp=script_rtn;\n");
      if(reslicedDataDimsSpecified) sb.append(reslicedDataDims.toScript(scriptedObjects));
      else sb.append("script_rtn = null;\n");
      sb.append(variableName).append(" = new ").append(className).append("(");
      sb.append("indimstmp, affinematrixtmp, script_rtn, ");
      if(reslicedOrigSpecified)
	sb.append("new iiv.data.XYZDouble(").append(reslicedOrig.x).append(',').append(reslicedOrig.y).append(',').append(reslicedOrig.z).append("), ");
      else sb.append("(iiv.data.XYZDouble) null, ");
      sb.append("iiv.data.CNUDimensions.orientationValueOf(\"");
      sb.append(CNUDimensions.orientationToString(sliceViewMode));
      sb.append("\"), ");
      sb.append("iiv.data.CNUTypes.typeValueOf(\"");
      sb.append(CNUTypes.typeToString(outType));
      sb.append("\"));\n");
      sb.append("unset(\"indimstmp\");\n");
    }
    if(variableName != null) sb.append("script_rtn=").append(variableName).append(";\n");
    else sb.append("script_rtn=null;\n");
    sb.append("// -- end ").append(className).append(" script\n");
    return sb.toString();
  }
}
