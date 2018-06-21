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
public class PrimaryOrthoDataSlicer implements CNUDataSlicer {
  private CNUDimensions dims = null;
  private int sliceViewMode;
  private int outType = CNUTypes.UNSIGNED_INTEGER;

  private int[] inc = null;
  private int sliceDim = 0;
  private CNUDimensions sliceDims = null;

  /**
   * Constructs a new instance of PrimaryOrthoDataSlicer.
   *
   * @param dims dimension of data this slicer will handle
   * @param sliceViewMode orientations of slices to create
   * @param output data type
   */
  public PrimaryOrthoDataSlicer(CNUDimensions dims, int sliceViewMode, int outType) {
    if(dims != null) this.dims = (CNUDimensions) dims.clone();
    else dims = new CNUDimensions(new int[] {128,128,60}, 0, CNUTypes.UNSIGNED_SHORT);

    this.outType = outType;
    this.sliceViewMode = sliceViewMode;

    sliceDim = getSliceNumberDimension(dims, sliceViewMode);
    sliceDims = getSliceDimensions(dims, sliceViewMode, outType);
    inc = getInputToSliceIncrements(dims, sliceViewMode);

  }
  /**
   * Gets an equivalent data slicer that will work with a new set of dimensions.
   *
   * @param newDims dimensions to get equivalent data slicer for
   * @return an equivalent data slicer for the new dimensions
   **/
  public CNUDataSlicer getEquivalentDataSlicer(CNUDimensions newDims) {
    if(newDims == null) return null;
    if(newDims.equals(dims)) return this;
    return new PrimaryOrthoDataSlicer(newDims, sliceViewMode, outType);
  }
  /**
   * Returns the input data dimensions the data slicer uses.
   *
   * @return the input data dimensions the data slicer uses
   */
  public CNUDimensions getInDimensions() {
    return (CNUDimensions) dims.clone();
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
   * Returns the slice dimension the data slicer uses.
   *
   * @return  the slice dimension the data slicer uses
   */
  public int getSliceDimension() {
    return sliceDim;
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
    return dims.getDim(sliceDim);
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
    int[] sliceOrig = new int[dims.getNumberOfDimensions()];
    for(int i=0; i<sliceOrig.length; i++) sliceOrig[i] = 0;
    sliceOrig[sliceDim] = slice;
    int index = dims.getIndex(sliceOrig) + pt.x * inc[0] + pt.y * inc[1];
    return dims.getPoint(index);
  }
 /**
   * Gets the point relative to this slice given the indices
   * corresponding to the original data.
   *
   * @param	indices indices to original data
   * @return	location relative to slice (negative value
   *		for x or y indicates the indice does not map to that dimension)
   */
  public Point getSlicePointFromDataIndices(int[] indices) {
    Point pt = new Point(-1, -1);
    if(indices == null) return pt;
    int [] inIncOrig = dims.getIncrements();
    int lesserlength = (inIncOrig.length < indices.length) ? inIncOrig.length : indices.length;
    for(int k = 0; k < lesserlength; k++) {
      if(inc[0] == inIncOrig[k]) pt.x = indices[k];
      else if(inc[1] == inIncOrig[k]) pt.y = indices[k];
    }
    return pt;
  }
  /**
   * Gets the slice number associated with a data indices location.
   *
   * @param indices	indices to original data
   * @return		slice number or <code>-1</code> if not contained in data.
   */
  public int getSliceNumberFromDataIndices(int[] indices) {
    if(! dims.contains(indices)) return -1;
    if(sliceDim < indices.length) return indices[sliceDim];
    return 0;
  }
  /**
   * Determines if the given indices relative to the input data are located
   * in the given slice.
   *
   * @param     slice   slice to test for
   * @param	indices indices to original raw data
   * @return	<code>true</code> if indices are located in 
   */
  public boolean inSlice(int slice, int[] indices) {
    if(indices == null) return false;
    if(! dims.contains(indices) ) return false;

    if(indices.length > sliceDim) {
      // check if same slice
      if(slice != indices[sliceDim]) return false;
    }
    else if(slice != 0) return false;

    int [] inIncOrig = dims.getIncrements();
    for(int k = 0; k < inIncOrig.length; k++) {
      if(k == sliceDim) {
	// sliceDim already handled
      }
      else if(inc[0] == inIncOrig[k]) {
	// original k dimension is the same as displayed dimension 0
	// inDims.contains already handled this
      }
      else if(inc[1] == inIncOrig[k]) {
	// original k dimension is the same as displayed dimension 0
	// inDims.contains already handled this
      }
      else {
	// loop really handles remaining dimensions which all need to be 0
	if(indices.length > k)
	  if(indices[k] != 0) return false;
      }
    }
    return true;
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
    if(inData == null || ! inData.sameDimensions(dims)) return null;
    CNUDimensions outdims = null;
    // make sure output plane is sized correctly
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

    int localoffset;
    int[] sliceOrig = new int[dims.getNumberOfDimensions()];
    for(int i=0; i<sliceOrig.length; i++) sliceOrig[i] = 0;
    sliceOrig[sliceDim] = slice;
    if(sliceOrig.length > 3 && iValue > 0) sliceOrig[3] = iValue;
    localoffset = dims.getIndex(sliceOrig);

    CNUTypes.copyRegion(inData.getDataArray(),
			localoffset, dims.getType(), inc,
			singlePlane.getDataArray(), 0, outdims.getType(),
			outdims.getDimensions(),
			outdims.getNumberOfDimensions() - 1, sc);
    return singlePlane;
  }
  /**
   * Returns a string representation of this data slicer.
   *
   * @return	string representation
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    String class_string=getClass().getName();
    sb.append("*** class ").append(class_string).append("\n");
    sb.append(super.toString()).append("\n");
    sb.append("inDataDims=");
    if(dims == null) sb.append("null\n");
    else sb.append(dims.toString());
    sb.append("sliceViewMode=");
    sb.append(CNUDimensions.orientationToString(sliceViewMode)).append('\n');
    sb.append("outType=");
    sb.append(CNUTypes.typeToString(outType)).append('\n');
    sb.append("sliceDim=").append(sliceDim).append('\n');
    sb.append("sliceDims=").append(sliceDims.toString());
    sb.append("inc=").append(CNUTypes.arrayToString(inc)).append("\n");
    sb.append("*** end class ").append(class_string).append("\n");

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
      variableName = scriptedObjects.addObject(this, "primaryorthodataslicer");
      // dimensions
      if(dims != null) sb.append(dims.toScript(scriptedObjects));
      else sb.append("script_rtn=null;\n");
      sb.append(dims.toScript(scriptedObjects));
      sb.append(variableName).append(" = new ").append(className).append("(");
      sb.append("script_rtn, ");
      sb.append("iiv.data.CNUDimensions.orientationValueOf(\"");
      sb.append(CNUDimensions.orientationToString(sliceViewMode));
      sb.append("\"), ");
      sb.append("iiv.data.CNUTypes.typeValueOf(\"");
      sb.append(CNUTypes.typeToString(outType));
      sb.append("\"));\n");
    }
    if(variableName != null) sb.append("script_rtn=").append(variableName).append(";\n");
    else sb.append("script_rtn=null;\n");
    sb.append("// -- end ").append(className).append(" script\n");
    return sb.toString();
  }
  /**
   * Determines the dimension (x, y or z) associated with a slice view.
   *
   * @param inDims		dimensions of data
   * @param sliceViewMode	TRANSVERSE, CORONAL, or SAGITTAL
   * @return			dimension number (0, 1, or 2)
   */
  public static int getSliceNumberDimension(CNUDimensions inDims,
					    int sliceViewMode) {
    // note xyz slice view modes don't depend on input dimensions
    int sliceDim; // don't initialize here so compiler will flag as not intialized if switchess below miss a default
    switch (sliceViewMode) {
    case CNUDimensions.XY_SLICE:
    case CNUDimensions.YX_SLICE:
      sliceDim = 2;
      break;
    case CNUDimensions.YZ_SLICE:
    case CNUDimensions.ZY_SLICE:
      sliceDim = 0;
      break;
    case CNUDimensions.ZX_SLICE:
    case CNUDimensions.XZ_SLICE:
      sliceDim = 1;
      break;
    default:
      switch(((sliceViewMode & 0xFFFF) << 16) | (inDims.getOrientation() & 0xFFFF)) {
      default:
      case (CNUDimensions.TRANSVERSE << 16) | CNUDimensions.TRANSVERSE:
      case (CNUDimensions.CORONAL << 16) | CNUDimensions.CORONAL:
      case (CNUDimensions.SAGITTAL << 16) | CNUDimensions.SAGITTAL:
	sliceDim = 2;
	break;
      case (CNUDimensions.CORONAL << 16) | CNUDimensions.TRANSVERSE:
      case (CNUDimensions.TRANSVERSE << 16) | CNUDimensions.CORONAL:
      case (CNUDimensions.TRANSVERSE << 16) | CNUDimensions.SAGITTAL:
	sliceDim = 1;
	break;
      case (CNUDimensions.SAGITTAL << 16) | CNUDimensions.TRANSVERSE:
      case (CNUDimensions.SAGITTAL << 16) | CNUDimensions.CORONAL:
      case (CNUDimensions.CORONAL << 16) | CNUDimensions.SAGITTAL:
	sliceDim = 0;
	break;
      }
      break;
    }
    return sliceDim;

    // Determine dimensions associated with slice
    /*
    int sliceDim = 2;
    // if inDims orientation differs from sliceViewMode need to change
    switch (inDims.getOrientation()) {
    case CNUDimensions.TRANSVERSE: // x=R-L, y=P-A, z=S-I
      if(sliceViewMode == CNUDimensions.CORONAL) sliceDim = 1;
      else if(sliceViewMode == CNUDimensions.SAGITTAL) sliceDim = 0;
      break;
    case CNUDimensions.CORONAL: // x=R-L, y=S-I, z=P-A
      if(sliceViewMode == CNUDimensions.SAGITTAL) sliceDim = 1;
      else if(sliceViewMode == CNUDimensions.TRANSVERSE) sliceDim = 0;
      break;
    case CNUDimensions.SAGITTAL: // x=P-A, y=S-I, z=R-L
      if(sliceViewMode == CNUDimensions.TRANSVERSE) sliceDim = 1;
      else if(sliceViewMode == CNUDimensions.CORONAL) sliceDim = 0;
      break;
    }
    return sliceDim;
    */
  }
  /**
   * Calculates the 2-D slice dimensions.
   *
   * @param inDims	dimensions of data
   * @param sliceViewMode	TRANSVERSE, CORONAL, or SAGITTAL
   * @return		dimensions for a single 2-D slice
   */
  public static CNUDimensions getSliceDimensions(CNUDimensions inDims,
						 int sliceViewMode,
						 int outType) {
    CNUDimensions outdims = new CNUDimensions();
    outdims.setOrientation(sliceViewMode);
    outdims.setOrientationOrder(inDims.getOrientationOrder());
    // default should happen below    outdims.set2DValues(inDims.xdim(), inDims.ydim(), outType, 0);

    switch (sliceViewMode) {
    case CNUDimensions.XY_SLICE:
      outdims.set2DValues(inDims.xdim(), inDims.ydim(), outType, 0);
      break;
    case CNUDimensions.YX_SLICE:
      outdims.set2DValues(inDims.ydim(), inDims.xdim(), outType, 0);
      break;
    case CNUDimensions.YZ_SLICE:
      outdims.set2DValues(inDims.ydim(), inDims.zdim(), outType, 0);
      break;
    case CNUDimensions.ZY_SLICE:
      outdims.set2DValues(inDims.zdim(), inDims.ydim(), outType, 0);
      break;
    case CNUDimensions.ZX_SLICE:
      outdims.set2DValues(inDims.zdim(), inDims.xdim(), outType, 0);
      break;
    case CNUDimensions.XZ_SLICE:
      outdims.set2DValues(inDims.xdim(), inDims.zdim(), outType, 0);
      break;
    default:
      switch(((sliceViewMode & 0xFFFF) << 16) | (inDims.getOrientation() & 0xFFFF)) {
      default: // keeps input orientation when inDims is a slice orientation
      case (CNUDimensions.TRANSVERSE << 16) | CNUDimensions.TRANSVERSE:
      case (CNUDimensions.CORONAL << 16) | CNUDimensions.CORONAL:
      case (CNUDimensions.SAGITTAL << 16) | CNUDimensions.SAGITTAL:
	outdims.set2DValues(inDims.xdim(), inDims.ydim(), outType, 0);
	break;
      case (CNUDimensions.CORONAL << 16) | CNUDimensions.TRANSVERSE:
      case (CNUDimensions.TRANSVERSE << 16) | CNUDimensions.CORONAL:
	outdims.set2DValues(inDims.xdim(), inDims.zdim(), outType, 0);
	break;
      case (CNUDimensions.SAGITTAL << 16) | CNUDimensions.TRANSVERSE:
	outdims.set2DValues(inDims.ydim(), inDims.zdim(), outType, 0);
	break;
      case (CNUDimensions.SAGITTAL << 16) | CNUDimensions.CORONAL:
      case (CNUDimensions.CORONAL << 16) | CNUDimensions.SAGITTAL:
	outdims.set2DValues(inDims.zdim(), inDims.ydim(), outType, 0);
	break;
      case (CNUDimensions.TRANSVERSE << 16) | CNUDimensions.SAGITTAL:
	outdims.set2DValues(inDims.zdim(), inDims.xdim(), outType, 0);
	break;
      }
      break;
    }

    // if inDims orientation differs from sliceViewMode need to change
    /*
    switch (inDims.getOrientation()) {
    case CNUDimensions.TRANSVERSE:      // x=R-L, y=P-A, z=S-I
      if(sliceViewMode == CNUDimensions.CORONAL) // sliceDim=1; order=x,z,y
	outdims.set2DValues(inDims.xdim(), inDims.zdim(), outType, 0);
      else if(sliceViewMode == CNUDimensions.SAGITTAL) // sliceDim=0; order=y,z,x
	outdims.set2DValues(inDims.ydim(), inDims.zdim(), outType, 0);
      break;
    case CNUDimensions.CORONAL:      // x=R-L, y=S-I, z=P-A
      if(sliceViewMode == CNUDimensions.SAGITTAL) // sliceDim=1 order=x,z,y
	outdims.set2DValues(inDims.xdim(), inDims.zdim(), outType, 0);
      else if(sliceViewMode == CNUDimensions.TRANSVERSE) // sliceDim=0 order=z,y,x
	outdims.set2DValues(inDims.zdim(), inDims.ydim(), outType, 0);
      break;
    case CNUDimensions.SAGITTAL:      // x=P-A, y=S-I, z=R-L
      if(sliceViewMode == CNUDimensions.TRANSVERSE) // sliceDim=1 order=z,x,y
	outdims.set2DValues(inDims.zdim(), inDims.xdim(), outType, 0);
      else if(sliceViewMode == CNUDimensions.CORONAL) // sliceDim=0 order=z,y,x
	outdims.set2DValues(inDims.zdim(), inDims.ydim(), outType, 0);
      break;
    }
    */
      /* these were swapped?
    case CNUDimensions.CORONAL:      // x=R-L, y=S-I, z=P-A
      if(sliceViewMode == CNUDimensions.SAGITTAL)
	outdims.set2DValues(inDims.zdim(), inDims.ydim(),
			    outType, 0);
      else if(sliceViewMode == CNUDimensions.TRANSVERSE)
	outdims.set2DValues(inDims.xdim(), inDims.zdim(),
			    outType, 0);
      break;
    }
      */
    return outdims;
  }
  /** 
   * Calculates the input increments to get a display slice for viewing.
   *
   * @param inDims	dimensions of data
   * @param sliceViewMode	TRANSVERSE, CORONAL, or SAGITTAL
   * @return		increments to transverse a single slice
   */
  public static int[] getInputToSliceIncrements(CNUDimensions inDims,
						  int sliceViewMode)
  {
    int [] inInc = inDims.getIncrements(); // initialized with input increments

    int tmp;
    switch (sliceViewMode) {
    case CNUDimensions.XY_SLICE:
      // increments same as inDims
      break;
    case CNUDimensions.YX_SLICE:
      tmp = inInc[0]; inInc[0] = inInc[1]; inInc[1] = tmp;
      break;
    case CNUDimensions.YZ_SLICE:
      tmp = inInc[0]; inInc[0] = inInc[1]; inInc[1] = inInc[2]; inInc[2] = tmp;
      break;
    case CNUDimensions.ZY_SLICE:
      tmp = inInc[0]; inInc[0] = inInc[2]; inInc[2] = tmp;
      break;
    case CNUDimensions.ZX_SLICE:
      tmp = inInc[0]; inInc[0] = inInc[2]; inInc[2] = inInc[1]; inInc[1] = tmp;
      break;
    case CNUDimensions.XZ_SLICE:
      tmp = inInc[1]; inInc[1] = inInc[2]; inInc[2] = tmp;
      break;
    default:
      switch(((sliceViewMode & 0xFFFF) << 16) | (inDims.getOrientation() & 0xFFFF)) {
      default:
      case (CNUDimensions.TRANSVERSE << 16) | CNUDimensions.TRANSVERSE:
      case (CNUDimensions.CORONAL << 16) | CNUDimensions.CORONAL:
      case (CNUDimensions.SAGITTAL << 16) | CNUDimensions.SAGITTAL:
	// increments same as inDims
	break;
      case (CNUDimensions.CORONAL << 16) | CNUDimensions.TRANSVERSE:
      case (CNUDimensions.TRANSVERSE << 16) | CNUDimensions.CORONAL:
	tmp = inInc[1]; inInc[1] = inInc[2]; inInc[2] = tmp;
	break;
      case (CNUDimensions.SAGITTAL << 16) | CNUDimensions.TRANSVERSE:
	tmp = inInc[0]; inInc[0] = inInc[1]; inInc[1] = inInc[2]; inInc[2] = tmp;
	break;
      case (CNUDimensions.SAGITTAL << 16) | CNUDimensions.CORONAL:
      case (CNUDimensions.CORONAL << 16) | CNUDimensions.SAGITTAL:
	tmp = inInc[0]; inInc[0] = inInc[2]; inInc[2] = tmp;
	break;
      case (CNUDimensions.TRANSVERSE << 16) | CNUDimensions.SAGITTAL:
        tmp = inInc[0]; inInc[0] = inInc[2]; inInc[2] = inInc[1]; inInc[1] = tmp;
	break;
      }
      break;
    }
    return inInc;

    /*
    switch (inDims.getOrientation()) {
    case CNUDimensions.TRANSVERSE:      // x=R-L, y=P-A, z=S-I
      if(sliceViewMode == CNUDimensions.CORONAL) { // sliceDim=1; order=x,z,y
	int tmp = inInc[1]; inInc[1] = inInc[2]; inInc[2] = tmp;
      }
      else if(sliceViewMode == CNUDimensions.SAGITTAL) { // sliceDim=0; order=y,z,x
	int tmp = inInc[0]; inInc[0] = inInc[1];
	inInc[1] = inInc[2]; inInc[2] = tmp;
      }
      break;
    case CNUDimensions.CORONAL:      // x=R-L, y=S-I, z=P-A
      if(sliceViewMode == CNUDimensions.SAGITTAL) { // sliceDim=1 order=x,z,y
	int tmp = inInc[1]; inInc[1] = inInc[2]; inInc[2] = tmp;
	//	int tmp = inInc[0]; inInc[0] = inInc[2]; inInc[2] = tmp;
      }
      else if(sliceViewMode == CNUDimensions.TRANSVERSE) { // sliceDim=0 order=z,y,x
	int tmp = inInc[0]; inInc[0] = inInc[2]; inInc[2] = tmp;
	//	int tmp = inInc[1]; inInc[1] = inInc[2]; inInc[2] = tmp;
      }
      break;
    case CNUDimensions.SAGITTAL:      // x=P-A, y=S-I, z=R-L
      if(sliceViewMode == CNUDimensions.TRANSVERSE) { // sliceDim=1 order=z,x,y
	int tmp = inInc[0]; inInc[0] = inInc[2];
	inInc[2] = inInc[1]; inInc[1] = tmp;
      }
      else if(sliceViewMode == CNUDimensions.CORONAL) { // sliceDim=0 order=z,y,x
	int tmp = inInc[0]; inInc[0] = inInc[2]; inInc[2] = tmp;
      }
      break;
    }
    return inInc;
    */
  }
}
