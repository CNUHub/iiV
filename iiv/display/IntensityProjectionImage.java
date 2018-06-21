package iiv.display;
import iiv.data.*;
import iiv.script.*;
import iiv.io.*;
import java.awt.image.*;
import java.awt.*;


/**
 * Component to display a maximum intensity projection of CNUData as an image with
 * flip, zoom and show point abilities.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.CNUViewer
 * @see		java.awt.Container
 * @since	iiV1.117a
 */
public class IntensityProjectionImage extends SingleImg implements CNUTypesConstants {
  private static final long serialVersionUID = 6097073335295722293L;
  private Object stateParameterLock = new Object();

  private CNUData singlePlane = null;
  private CNUDimensions sliceDims = null;
  private CNUData planeRawData = null;
  private CNUDimensions planeRawDataDims = null;
  private CNUData planeIndices = null;
  private CNUDimensions planeIndicesDims = null;

  private CNUDimensions inDims = null;
  private int sliceViewMode = CNUDimensions.TRANSVERSE;
  //  private int[] inInc = null;
  //  private int sliceDim = 2;

  private int firstslice = 0;
  private int lastslice = -1;
  private double factor = 1d;

  /**
   * Constructs a new instance of IntensityProjectionImage.
   *
   * @param inImg		the data to retrieve image pixels from
   * @param sliceViewMode	the slice orientation to display
   *				<code>CNUDimensions.TRANSVERSE</code>,
   *				<code>CNUDimensions.CORONAL</code>,
   *				<code>CNUDimensions.SAGITTAL</code>,,,
   * @param firstslice		slice number in the range from 0 to
   *				the number of slices for the sliceViewMode.
   *                            Intensity projection includes values only from
   *                            firstslice to lastslice.
   * @param lastslice		the slice number in the range from 0 to
   *				the number of slices for the sliceViewMode.
   *                            If less than 0 last slice for mode used instead.
   * @param iValue		the 4th or i dimension to use for displaying
   *				a slice from 4 dimensional data
   * @param sc			the scale object for mapping pixel values
   *				to indices in the color lookup table
   */
  public IntensityProjectionImage(CNUData inImg, int sliceViewMode,
				  int firstslice, int lastslice,
				  int iValue, CNUScale sc) {
    this(inImg, sliceViewMode, firstslice, lastslice, iValue, sc, (sc != null)?sc.getScaleFactor():1.0d);
  }
  /**
   * Constructs a new instance of IntensityProjectionImage.
   *
   * @param inImg		the data to retrieve image pixels from
   * @param sliceViewMode	the slice orientation to display
   *				<code>CNUDimensions.TRANSVERSE</code>,
   *				<code>CNUDimensions.CORONAL</code>,
   *				<code>CNUDimensions.SAGITTAL</code>...
   * @param firstslice		slice number in the range from 0 to
   *				the number of slices for the sliceViewMode.
   *                            Intensity projection includes values only from
   *                            firstslice to lastslice.
   * @param lastslice		the slice number in the range from 0 to
   *				the number of slices for the sliceViewMode.
   *                            If less than 0 last slice for mode used instead.
   * @param iValue		the 4th or i dimension to use for displaying
   *				a slice from 4 dimensional data
   * @param sc			the scale object for mapping pixel values
   *				to indices in the color lookup table
   * @param factor		if factor less than 0, creates minimum intensity projections.
   *				
   */
  public IntensityProjectionImage(CNUData inImg, int sliceViewMode,
				  int firstslice, int lastslice,
				  int iValue, CNUScale sc, double factor) {
    this.firstslice = firstslice;
    this.lastslice = lastslice;
    this.factor = factor;
    this.inDims = inImg.getDimensions();
    this.sliceViewMode = sliceViewMode;
    //    this.inInc = getInputToDisplayIncrements(inDims, sliceViewMode);
    super.init(inImg, sliceViewMode, firstslice, iValue, sc);
  }
  /**
   * Produces a string representation of this image.
   *
   * @return	a string representation
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(super.toString()).append("\n");
    sb.append("firstSlice=").append(firstslice);
    sb.append("lastSlice=").append(lastslice);
    return sb.toString();
  }
  /**
   * Creates a script that may be used to recreate this display component.
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
      CNUData inImg = getData();
      if(inImg instanceof CNUImgFile) {
	variableName = scriptedObjects.addObject(this, "intensityprojectionimage");
	sb.append(((CNUImgFile) inImg).toScript(scriptedObjects));
	String inImgVariableName = scriptedObjects.get(inImg);
	// scale
	CNUScale sc = getScale();
	if(sc == null) sb.append("scaletmp=null;\n");
	else {
	  sb.append(sc.toScript(scriptedObjects));
	  sb.append("scaletmp=script_rtn;\n");
	}

	sb.append(variableName).append(" = new ").append(className).append("(");
	sb.append(inImgVariableName).append(", ");
	sb.append("iiv.data.CNUDimensions.orientationValueOf(\"");
	sb.append(CNUDimensions.orientationToString(getSliceViewMode()));
	sb.append("\")");
	sb.append(", ");
	sb.append(firstslice).append(", ").append(lastslice).append(", ");
	sb.append(getIValue());
	sb.append(", scaletmp, ");
	sb.append(factor).append(");\n");

	sb.append("unset(\"scaletmp\");\n");
	sb.append(postObjectToScript(scriptedObjects));
      }
      else sb.append("// unknown image component= " + this + "\n");
    }
    if(variableName != null) sb.append("script_rtn=").append(variableName).append(";\n");
    else sb.append("script_rtn=null;\n");
    sb.append("// -- end ").append(className).append(" script\n");
    return sb.toString();
  }

  /**
   * Converts the input image into a raw image producer.
   */
  protected void createRawIp() {

    synchronized (stateParameterLock) {

      setImage(null);
      if(planeRawData == null) {
	CNUData inImg = getData();
	
	// sliceDims 2-D
	//	sliceDim = getSliceNumberDimension(inDims, sliceViewMode);
	//	sliceDims = getSliceDimensions(inDims, sliceViewMode, getColorModel());
	CNUDataSlicer dataSlicer = getDataSlicer();
	sliceDims = dataSlicer.getSliceDimensions();

	singlePlane = new CNUData();
	singlePlane.initDataArray(sliceDims);

	planeRawDataDims = (CNUDimensions) sliceDims.clone();
	planeRawDataDims.setType(inDims.getType());
	planeRawData = new CNUData();
	planeRawData.initDataArray(planeRawDataDims);

	planeIndicesDims = (CNUDimensions) sliceDims.clone();
	planeIndicesDims.setType(INTEGER);
	planeIndices = new CNUData();
	planeIndices.initDataArray(planeIndicesDims);

	//	int[] sliceOrig = getSliceOrigin();

	//	int numberOfSlices = inDims.getDim(sliceDim);
	int numberOfSlices = dataSlicer.getNumberOfSlices();

	if(firstslice < 0) firstslice = 0;
	else if(firstslice >= numberOfSlices) firstslice = numberOfSlices - 1;
	if(lastslice < 0 || lastslice >= numberOfSlices) lastslice = numberOfSlices - 1;
	int sliceinc = 1;
	if(lastslice < firstslice) sliceinc = -1;
	int slice = firstslice;

	// initialize plane to values from first slice
	//	sliceOrig[sliceDim] = slice;
	//	CNUTypes.copyRegion(inImg.getDataArray(),
	//			    inDims.getIndex(sliceOrig),
	//			    inDims.getType(), inInc,
	//			    planeRawData.getDataArray(), 0, planeRawDataDims.getType(),
	//			    planeRawDataDims.getDimensions(),
	//			    planeRawDataDims.getNumberOfDimensions() - 1, null);
	int iValue = getIValue();
	dataSlicer.grabSliceData(inImg, slice, iValue, planeRawData, null);

	CNUData currentSliceRawData = new CNUData();
	currentSliceRawData.initDataArray(planeRawDataDims);

	// search for greater or lesser values in remaining slices
	for(slice += sliceinc;
	    ((sliceinc < 0) ? slice >= lastslice : slice <= lastslice);
	    slice += sliceinc) {
	  dataSlicer.grabSliceData(inImg, slice, iValue, currentSliceRawData, null);

	  //	  sliceOrig[sliceDim] = slice;
	  findGreaterValues(currentSliceRawData.getDataArray(),
			    0,
			    planeRawDataDims.getType(), planeRawDataDims.getIncrements(),
			    planeRawData.getDataArray(), 0,
			    planeRawDataDims.getDimensions(),
			    planeRawDataDims.getNumberOfDimensions() - 1,
			    (int [])planeIndices.getDataArray(), slice,
			    factor);
	}
      }

      // scale maximum projection data to base raw ip on
      CNUTypes.copyRegion(planeRawData.getDataArray(),
			  0,
			  planeRawDataDims.getType(), planeRawDataDims.getIncrements(),
			  singlePlane.getDataArray(), 0, sliceDims.getType(),
			  sliceDims.getDimensions(),
			  sliceDims.getNumberOfDimensions() - 1, getScale());

      ColorModel cm = getColorModel();
      if(cm == null) cm = DisplayComponentDefaults.getDefaultColorModel();
      if(CNUTypes.bytesPerWord(sliceDims.getType()) == 1)
	setImageProducer(new MemoryImageSource(sliceDims.xdim(),
					       sliceDims.ydim(), cm,
					       (byte[])singlePlane.getDataArray(),
					       sliceDims.getOffset(),
					       sliceDims.xdim()));
      else
	setImageProducer(new MemoryImageSource(sliceDims.xdim(),
					       sliceDims.ydim(), cm,
					       (int[])singlePlane.getDataArray(),
					       sliceDims.getOffset(),
					       sliceDims.xdim()));
      initColorModel(cm);  // sets color map filter not needed
    }
  }

  /**
   * Searches a multi-dimensional square region from one data array to
   * fill a lesser dimensional array with peaks.  The amount of data searched
   * is governed by the array of output dimensions.  The output array is filled
   * from the output offset with no word skipping.
   * Uses a recursive routine - calling this routine to recurse to
   * lower dimensions until lowest dimension (currentDim = 0)
   * is reached and then calling compare fill.
   *
   * @param inArray	input data array
   * @param inOffset	location of input array to begin copying from
   * @param inType	CNU data type of input array
   * @param inInc	array containing one increment for each dimension.
   *			The increments specify the number of words to the
   *			next input word for the same dimension.
   * @param outArray	output data array
   * @param outOffset	location of output array to begin copying to
   * @param outDims	array of output dimensions which governs the amount
   *			of data copied for each dimension
   * @param currentDim	index of dimension recursive routine is currently
   *			working with
   * @param indiceSaveArray  int array to store indice where values are replaced
   * @param indice      indice value to store
   * @param factor      if negative factor array filled with valleys instead of peaks.
   * @return		the offset to the next location in the output array.
   */ 
  public final static int findGreaterValues(Object inArray, int inOffset,
					    int inType, int[] inInc,
					    Object outArray, int outOffset,
					    int[] outDims,
					    int currentDim,
					    int[] indiceSaveArray,
					    int indice, double factor) {
    if(currentDim == 0) {
      findGreaterInArray(inArray, inOffset, inType, inInc[currentDim],
			 outArray, outOffset, outDims[currentDim],
			 indiceSaveArray, indice, factor);
      return outDims[currentDim];
    } else {
      int wordsCopied = 0;
      for( int n = 0; n < outDims[currentDim];
	   n++, inOffset += inInc[currentDim]) {
	wordsCopied +=
	  findGreaterValues(inArray, inOffset, inType, inInc,
			    outArray, outOffset + wordsCopied,
			    outDims, currentDim - 1,
			    indiceSaveArray, indice, factor);
      }
      return wordsCopied;
    }
  }

  /**
   * Compares an array of values to another array replacing the second with
   * greater values and noting the current indice in yet another array when
   * a replacement is done.
   * Data from the input array can be undersampled or
   * reverse sampled by specifying an in increment other then one.
   *
   * @param inarray	input data array
   * @param inoffset	location of input array to begin compares
   * @param intype	CNU data type of input array
   * @param inInc	amount to increment to get to the next input word
   *			after comparing
   * @param outarray	output data array
   * @param outoffset	location of output array to begin compare
   * @param cnt		number of words to compare
   * @param indiceSaveArray  int array to store indice where values are replaced
   * @param indice      indice value to store
   * @param factor      if negative factor values replaced with lesser instead of greater.
   */
  public final static void findGreaterInArray( Object inarray, int inoffset,
					       int intype, int inInc,
					       Object outarray, int outoffset,
					       int cnt,
					       int[] indiceSaveArray, int indice,
					       double factor) {
    int outindex = outoffset;
    int endoutindex = outindex + cnt;
    int inindex = inoffset;
    int inInt;
    int outInt;
    int p_n_fac = 1;
    double p_n_fac_dbl = 1d;
    if(factor < 0) { p_n_fac = -1; p_n_fac_dbl = -1d; }

    switch (intype) {
    case BYTE:
      for(; outindex < endoutindex; outindex++,
	    inindex += inInc) {
	if( (p_n_fac * ((byte [])inarray)[inindex]) > (p_n_fac * ((byte [])inarray)[inindex]) ) {
	  ((byte [])outarray)[outindex] = ((byte [])inarray)[inindex];
	  indiceSaveArray[outindex] = indice;
	}
      }
      return;
    case UNSIGNED_BYTE:
      for(; outindex < endoutindex; outindex++,
	    inindex += inInc) {
	inInt = p_n_fac * CNUTypes.UnsignedByteToInt(((byte [])inarray)[inindex]);
	outInt = p_n_fac * CNUTypes.UnsignedByteToInt(((byte [])outarray)[outindex]);
	if(inInt > outInt) {
	  ((byte [])outarray)[outindex] = ((byte [])inarray)[inindex];
	  indiceSaveArray[outindex] = indice;
	}
      }
      return;
    case SHORT:
      for(; outindex < endoutindex; outindex++,
	    inindex += inInc) {
	if((p_n_fac * ((short [])inarray)[inindex]) > (p_n_fac * ((short []) outarray)[outindex])) {
	  ((short []) outarray)[outindex] = ((short [])inarray)[inindex];
	  indiceSaveArray[outindex] = indice;
	}
      }
      return;
    case UNSIGNED_SHORT:
      inInt = p_n_fac * CNUTypes.UnsignedShortToInt(((short [])inarray)[inindex]);
      outInt = p_n_fac * CNUTypes.UnsignedShortToInt(((short []) outarray)[outindex]);
      if(inInt > outInt) {
	((short []) outarray)[outindex] = ((short [])inarray)[inindex];
	indiceSaveArray[outindex] = indice;
      }
      return;
    case INTEGER:
      for(; outindex < endoutindex; outindex++,
	    inindex += inInc) {
	if((p_n_fac * ((int [])inarray)[inindex]) > (p_n_fac * ((int []) outarray)[outindex])) {
	  ((int []) outarray)[outindex] = ((int [])inarray)[inindex];
	  indiceSaveArray[outindex] = indice;
	}
      }
      return;
    case UNSIGNED_INTEGER:
      for(; outindex < endoutindex; outindex++,
	    inindex += inInc) {
	long inLong = p_n_fac * (((long)((int [])inarray)[inindex]) & 0xFFFFFFFF);
	long outLong = p_n_fac * (((long)((int [])outarray)[outindex]) & 0xFFFFFFFF);
	if(inLong > outLong) {
	  ((int []) outarray)[outindex] = ((int [])inarray)[inindex];
	  indiceSaveArray[outindex] = indice;
	}
      }
      return;
    case LONG:
      for(; outindex < endoutindex; outindex++,
	    inindex += inInc) {
	if((p_n_fac * ((long [])inarray)[inindex]) > (p_n_fac * ((long []) outarray)[outindex])) {
	  ((long []) outarray)[outindex] = ((long [])inarray)[inindex];
	  indiceSaveArray[outindex] = indice;
	}
      }
    return;
    case FLOAT:
      for(; outindex < endoutindex; outindex++,
	    inindex += inInc) {
	if((p_n_fac_dbl * ((float [])inarray)[inindex]) > (p_n_fac_dbl * ((float []) outarray)[outindex])) {
	  ((float []) outarray)[outindex] = ((float [])inarray)[inindex];
	  indiceSaveArray[outindex] = indice;
	}
      }
      return;
    case DOUBLE:
      for(; outindex < endoutindex; outindex++,
	    inindex += inInc) {
	if((p_n_fac_dbl * ((double [])inarray)[inindex]) > (p_n_fac_dbl * ((double []) outarray)[outindex])) {
	  ((double []) outarray)[outindex] = ((double [])inarray)[inindex];
	  indiceSaveArray[outindex] = indice;
	}
      }
      return;
    default:
      return;
    }
  }


  /**
   * Gets the indices corresponding to the original raw data this display
   * component is created from based on a point relative to the non-filtered
   * (no flip, rotation, zoom, or offset) image.
   *
   * @param pt	point to get indice for
   * @return	the indices to the data
   */
  public int[] getIndicesFromNonfilteredPoint(Point pt) {
    if(pt == null) return null;
    //    int[] sliceOrig = getSliceOrigin();

    int localslice = getSlice();
    synchronized (stateParameterLock) {
      if(planeIndices != null) localslice = planeIndices.getPixelAsInt(new int[] {pt.x, pt.y});
      //      int index = inDims.getIndex(sliceOrig) + pt.x * inInc[0] +
      //		  pt.y * inInc[1];
      //      return inDims.getPoint(index);
      return getDataSlicer().getDataIndicesFromSlicePoint(pt, localslice);
    }
  }
 /**
   * Gets the point relative to the non-filtered
   * (no flip, rotation, zoom, or offset) image given the indices
   * corresponding to the original raw data this display component
   * is created from.  This default implementation returns the
   * indices stored in a Point object.
   *
   * @param	indices indices to original raw data which may have
   *		any number of dimensions
   * @return	location relative to non-filtered image (negative value
   *		for x or y indicates the indice doesn't map to that dimension)
   */
  public Point getNonfilteredPoint(int[] indices) {
    return getDataSlicer().getSlicePointFromDataIndices(indices);

    //    Point pt = new Point(-1, -1);
    //    if(indices == null) return pt;

    //    int [] inIncOrig = inDims.getIncrements();

    //    for(int k = 0; k < inIncOrig.length; k++) {
    //      if(inInc[0] == inIncOrig[k]) {
    //	if(indices.length > k) pt.x = indices[k];
    //      }
    //      else if(inInc[1] == inIncOrig[k]) {
    //	if(indices.length > k) pt.y = indices[k];
     //     }
    //    }
     //   return pt;
  }

  /**
   * Determines if the given indices relative to the raw data are located
   * on the display image.
   *
   * @param	indices indices to original raw data which may have
   *		any number of dimensions
   * @return	<code>true</code> if indices are located on the image this component
   *		represents.
   */
  public boolean onImage(int[] indices) {
    if(indices == null) return false;
    Point pt =  getNonfilteredPoint(indices);
    int localslice = getSlice();
    if(planeIndices != null) {
      int[] point = new int[] {pt.x, pt.y};
      if(! planeIndicesDims.contains(point)) return false;
      localslice = planeIndices.getPixelAsInt(point);
    }
    if(localslice != getDataSlicer().getSliceNumberFromDataIndices(indices)) return false;
    return true;

    /*
    if(! inDims.contains(indices) ) return false;

    if(indices.length > sliceDim) {
      // check if same slice
      Point pt = getNonfilteredPoint(indices);
      if(indices[sliceDim] != planeIndices.getPixelAsInt(new int[] {pt.x, pt.y})) return false;
    }
    int [] inIncOrig = inDims.getIncrements();
    for(int k = 0; k < inIncOrig.length; k++) {
      if(k == sliceDim) {
	// sliceDim already handled
      }
      else if(inInc[0] == inIncOrig[k]) {
	// original k dimension is the same as displayed dimension 0
	// inDims.contains already handled this
      }
      else if(inInc[1] == inIncOrig[k]) {
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
    */
  }
  /**
   * Sets the default slice label.
   */
  public void setSliceLabel() {
    if(factor < 0) setSliceLabel("MinProj");
    else setSliceLabel("MaxProj");
  }
}
