package iiv.display;
import iiv.*;
import iiv.filter.*;
import iiv.data.*;
import iiv.io.*;
import iiv.util.*;
import iiv.script.*;
import iiv.dialog.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.lang.reflect.*;
//import Acme.Fmt;

/**
 * Component to display a single slice of CNUData as an image with
 * flip, zoom and show point abilities.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUViewer
 * @see		java.awt.Container
 * @since	iiV1.0
 */
public class SingleImg extends DisplayComponent
implements ScaleInterface, CoordinateMappable, SliceNumbering,
  CNUFileObject, Overlayable, NumberFormattable, iiVScriptable {
  private static final long serialVersionUID = 1869080597785066256L;
  final static int FONTPRESPACE = 2;
  private Object labelLock = new Object();
  private boolean orientationLabelsOn = false;
  private String north = "A";
  private String south = "P";
  private String west = "R";
  private String east = "L";
  // label values after 2-d image filters (flip & rotation)
  private String fnorth = north;
  private String fsouth = south;
  private String fwest = west;
  private String feast = east;

  private String sliceLabel = null;
  private boolean sliceLabelOn = false;

  private String iValueLabel = null;
  private boolean iValueLabelOn = false;

  private Object stateParameterLock = new Object();
  private CoordinateMap coorMap = null;
  private boolean coordinateMapDataViewDependent = false;
  private CNUScale sc = null;
  private int slice = 0;
  private int iValue = 0;
  //  private int[] sliceOrig = null;
  //  private int sliceDim = 0;
  private NumberFormat numberFormat =
    DisplayNumberFormat.getDefaultNumberFormat();

  // stuff set only in constructor therefore no sync needed
  //  private int sliceViewMode = CNUDimensions.TRANSVERSE;
  //  private CNUDimensions inDims = null;
  //  private int[] inInc = null;
  //  private CNUDimensions sliceDims = null;
  private CNUData inImg = null;
  private CNUDataSlicer dataSlicer = null;

  /**
   * Constructs a new instance of SingleImg with no initialization.  The init routine
   * should be called soon after.
   */
  public SingleImg() {}
  /**
   * Constructs a new instance of SingleImg.
   *
   * @param inImg		the data to retrieve image pixels from
   * @param sliceViewMode	the slice orientation to display
   *				<code>CNUDimensions.TRANSVERSE</code>,
   *				<code>CNUDimensions.CORONAL</code>,
   *				<code>CNUDimensions.SAGITTAL</code>...
   * @param slice		the slice number in the range from 0 to
   *				the number of slices for the sliceViewMode
   * @param iValue		the 4th or i dimension to use for displaying
   *				a slice from 4 dimensional data
   * @param sc			the scale object for mapping pixel values
   *				to indices in the color lookup table
   */
  public SingleImg(CNUData inImg, int sliceViewMode, int slice,
		   int iValue, CNUScale sc) {
    init(inImg, sliceViewMode, slice, iValue, sc);
  }
  /**
   * Constructs a new instance of SingleImg with default scaling.
   *
   * @param inImg		the data to retrieve image pixels from
   * @param sliceViewMode	the slice orientation to display
   *				<code>CNUDimensions.TRANSVERSE</code>,
   *				<code>CNUDimensions.CORONAL</code>,
   *				<code>CNUDimensions.SAGITTAL</code>...
   * @param slice		the slice number in the range from 0 to
   *				the number of slices for the sliceViewMode
   * @param iValue		the 4th or i dimension to use for displaying
   *				a slice from 4 dimensional data
   */
  public SingleImg(CNUData inImg, int sliceViewMode, int slice, int iValue) {
    init(inImg, sliceViewMode, slice, iValue, CNUScale.getDefaultScale());
  }
  /**
   * Initializes an instance of SingleImg.
   * Seperated from construction routines so subclasses can override functions
   * called here and by using the no parameter constructer and this routine
   * not fear thier local variables being available.
   * (local subclass variables may not be initialized until the superclass
   *  constructor gets done).
   *
   * @param inImg		the data to retrieve image pixels from
   * @param sliceViewMode	the slice orientation to display
   *				<code>CNUDimensions.TRANSVERSE</code>,
   *				<code>CNUDimensions.CORONAL</code>,
   *				<code>CNUDimensions.SAGITTAL</code>...
   * @param slice		the slice number in the range from 0 to
   *				the number of slices for the sliceViewMode
   * @param iValue		the 4th or i dimension to use for displaying
   *				a slice from 4 dimensional data
   * @param sc			the scale object for mapping pixel values
   *				to indices in the color lookup table
   */
  public void init(CNUData inImg, int sliceViewMode, int slice,
	      int iValue, CNUScale sc) {
    this.inImg = inImg;
    //    this.sliceViewMode = sliceViewMode;
    //    inDims = inImg.getDimensions();
/* doesn't work tool tip text takes away mouse control
    // if the data has a predefined file name display it as tool tip text
    try {
      Method method = inImg.getClass().getMethod("getFileName", new Class[0]);
      String filename = (String) method.invoke(inImg, new Object[0]);
      if(filename != null) setToolTipText(filename);
    } catch (NoSuchMethodException nsme) { // ignore
    } catch (SecurityException se) { // ignore
    } catch (IllegalAccessException iae) { // ignore
    } catch (IllegalArgumentException iarge) { // ignore
    } catch (InvocationTargetException ite) { // ignore
    } catch (ClassCastException cce) { // ignore
    }
*/
    // if the data has a predefined color model use it instead
    // of DisplayComponentDefaults
    ColorModel cmt = inImg.getColorModel();
    if(cmt != null) initColorModel(cmt);
    cmt = getColorModel();
    // if the data has a predefined coordinate map use it instead
    // of the default
    // also check if map depends on view mode
    coorMap = inImg.getCoordinateMap(sliceViewMode, slice, iValue);
    CoordinateMap cmtmp = inImg.getCoordinateMap();
    if(coorMap != null) {
      if(cmtmp != coorMap) coordinateMapDataViewDependent = true;
    }
    else coorMap = cmtmp;
    if(coorMap == null) coorMap = LinearCoordinateMap.getDefaultCoordinateMap();

    int displayType = CNUTypes.UNSIGNED_INTEGER;
    if(cmt != null && cmt.getPixelSize() <= 8) displayType = CNUTypes.UNSIGNED_BYTE;

    dataSlicer = new PrimaryOrthoDataSlicer(inImg.getDimensions(), sliceViewMode, displayType);

    // make sure iValue within data range
    int isize = inImg.getDimensions().idim();
    iValue = iValue % isize;
    if(iValue < 0) iValue += isize;
    this.iValue = iValue;

    // make sure slice within resliced range
    int numslices = dataSlicer.getNumberOfSlices();
    slice = slice % numslices;
    if(slice < 0) slice += numslices;
    this.slice = slice;

    setScale(sc);
    setOrientationLabels();
    setSliceLabel();
    setIValueLabel();
    createRawIp();
  }
  /**
   * Checks and updates the coordinate map if it depends on current data slice view
   * -- such ThreeDSSPFile data.
   */
  public void checkUpdateDataCoordinateMap() {
    if(coordinateMapDataViewDependent) {
      CoordinateMap cm = inImg.getCoordinateMap(getSliceViewMode(), getSlice(), getIValue());
      synchronized (stateParameterLock) {
	this.coorMap = cm;
	coordinateMapDataViewDependent = true;
      }
      setOrientationLabels();
      setSliceLabel();
      setIValueLabel();
    }
  }
  /**
   * Sets the coordinate mapping and updates labels to correspond.
   *
   * @param coorMap	coordinate mapping object
   */
  public void setCoordinateMap(CoordinateMap coorMap) {
    synchronized (stateParameterLock) {
      this.coorMap = coorMap;
    }
    setOrientationLabels();
    setSliceLabel();
    setIValueLabel();
  }
  /**
   * Gets the coordinate mapping object.
   *
   * @return	coordinate mapping object
   */
  public CoordinateMap getCoordinateMap() {
    synchronized (stateParameterLock) {
      return(coorMap);
    }
  }
  /**
   * Creates a component based on different data valid for
   * overlaying on top of this component.  Returns <code>null</code>
   * if the data isn't valid for overlaying.
   * Duplicates information needed to create the component
   * showing related data at the same location relative
   * to the top left corner as this component.
   * Information such as color model and scaling are not duplicated
   * but instead set to creation defaults that may be
   * preset to provide see through.
   *
   * @param obj		data object to base new overlay component on
   * @return		component that was created for overlaying or
   *			<code>null</code> if invalid object
   */
  public Component createOverlay(Object obj) {
    // can only create overlay for same type data with the same dimensions
    if( ! (obj instanceof CNUData) ) return null;
    CNUData inImg = (CNUData) obj;
    CNUDimensions inDims = inImg.getDimensions();
    if(! inDims.sameSize(getData().getDimensions() ) ) return null;

    CNUViewer cnuv = null;
    Container parent = getParent();
    if(parent instanceof CNUDisplay)
      cnuv = ((CNUDisplay) parent).getCNUViewer();

    // Scale set according to default or data and CNUViewer settings
    CNUScale sc = CNUScale.getDefaultScale();
    if(cnuv != null) sc = cnuv.getModeScale(inImg.getDimensions().getType());

    // construct duplicating slice and view mode
    SingleImg si = new SingleImg(inImg, getSliceViewMode(),
				 getSlice(), getIValue(), sc);
    // duplicate slicer
    si.setDataSlicer(getDataSlicer());
    // duplicate filter options
    si.setZoom(getZoomV(), getZoomH());
    si.setRotation(getRotation());
    si.setFlips(getFlipV(), getFlipH());
    si.setCrop(getCrop());

    // duplicate label display modes
    si.setSliceLabelOn(getSliceLabelOn());
    si.setOrientationLabelsOn(getOrientationLabelsOn());
    si.setIValueLabelOn(getIValueLabelOn());
    si.setOrientationLabelsOn(getOrientationLabelsOn());

    // duplicate other features
    si.setFont(getFont());
    si.setNumberFormat(getNumberFormat());
    si.setCoordinateMap(getCoordinateMap());
    si.setCrosshair(getCrosshairIndices(), getCrosshairColor());

    if(cnuv != null) {
      // add slice tracking if were slice tracking
      ShowPointTracker spt = (ShowPointTracker) cnuv.getShowPointDialog();
      if(spt != null) {
	if(spt.isSliceTracker(this)) spt.addSliceTracker(si);
	if(spt.isCrosshairTracker(this)) spt.addCrosshairTracker(si);
      }
    }
    return si;
  }
  /**
   * Produces a string representation of this image.
   *
   * @return	a string representation
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(super.toString()).append("\n");
    if(orientationLabelsOn) {
      sb.append("orientation labels=(").append(north).append(',').append(south);
      sb.append(',').append(west).append(',').append(east).append(")\n");
    }
    else sb.append("orientation labels off\n");
    if(sliceLabelOn) sb.append("sliceLabel=").append(sliceLabel).append('\n');
    else sb.append("slice labels off\n");
    if(iValueLabelOn) sb.append("iValueLabel=").append(iValueLabel).append('\n');
    else sb.append("iValue labels off\n");
    sb.append("coorMap=");
    sb.append((coorMap == null) ? "null" : coorMap.toString());
    sb.append('\n');
    sb.append("coordinateMapDataViewDependent=").append(coordinateMapDataViewDependent);
    sb.append('\n');
    sb.append("dataSlicer=");
    sb.append((dataSlicer == null) ? "null" : dataSlicer.toString());
    sb.append('\n');
    /*
    if(sliceOrig != null) {
      sb.append("sliceOrig=");
      for(int i=0; i<sliceOrig.length; i++)
	sb.append('[').append(sliceOrig[i]).append(']');
      sb.append('\n');
    }
    */
    //    sb.append("sliceDim=").append(sliceDim).append('\n');
    sb.append("numberFormat=").append(numberFormat.toString()).append('\n');
    sb.append("sliceViewMode=");
    sb.append(CNUDimensions.orientationToString(getSliceViewMode())).append('\n');
    sb.append(getDataSlicer().toString());
    /*
    if(inDims != null) sb.append("inDims=").append(inDims.toString());
    if(inInc != null) {
      sb.append("inInc=");
      for(int i=0; i<inInc.length; i++) sb.append('[').append(inInc[i]).append(']');
      sb.append('\n');
    }
    */
    //    if(sliceDims != null) sb.append("sliceDims=").append(sliceDims.toString());
    if(inImg != null) sb.append("inImg=").append(inImg.toString()).append('\n');

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
      if(inImg instanceof CNUImgFile) {
	variableName = scriptedObjects.addObject(this, "singleimg");
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
	sb.append(getSlice()).append(", ").append(getIValue());
	sb.append(", scaletmp);\n");

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
   * Creates a script to recreate the settings on an existing object.
   *
   * @param scriptedObjects scripted objects list this object already
   *                        exists in.
   * @return	script to recreate this component
   */
  public String postObjectToScript(CNUScriptObjects scriptedObjects) {
    String objectVariableName = scriptedObjects.get(this);
    StringBuffer sb = new StringBuffer();
    // data slicer
    CNUDataSlicer dataSlicer = getDataSlicer();
    sb.append(dataSlicer.toScript(scriptedObjects));
    sb.append(objectVariableName).append(".setDataSlicer(script_rtn);\n");
    // coordinate map
    CoordinateMap coorMap = getCoordinateMap();
    if(coorMap == null) {
       sb.append(objectVariableName).append(".setCoordinateMap(null);\n");
    }
    else {
      String s = coorMap.toScript(scriptedObjects);
      if(s != null) {
	sb.append(s);
	sb.append(objectVariableName).append(".setCoordinateMap(script_rtn);\n");
      }
    }
    synchronized (labelLock) {
      // labels on or off
      sb.append(objectVariableName);
      sb.append(".setSliceLabelOn(").append(sliceLabelOn).append(");\n");

      sb.append(objectVariableName);
      sb.append(".setSliceLabel(\"").append(sliceLabel).append("\");\n");

      sb.append(objectVariableName);
      sb.append(".setIValueLabelOn(").append(iValueLabelOn).append(");\n");

      sb.append(objectVariableName);
      sb.append(".setIValueLabel(\"").append(iValueLabel).append("\");\n");

      sb.append(objectVariableName);
      sb.append(".setOrientationLabelsOn(").append(orientationLabelsOn);
      sb.append(");\n");

      sb.append(objectVariableName);
      sb.append(".setOrientationLabels(\"");
      sb.append(north).append("\", \"").append(south).append("\", \"");
      sb.append(west).append("\", \"").append(east).append("\");\n");
    }
    sb.append(DisplayNumberFormat.numberFormatToScript(scriptedObjects, getNumberFormat()));
    sb.append(objectVariableName).append(".setNumberFormat(script_rtn);\n");
    sb.append(super.postObjectToScript(scriptedObjects));
    return sb.toString();
  }
  /**
   * Converts the input image into a raw image producer.
   */
  protected void createRawIp() {
    CNUData singlePlane = new CNUData();
    synchronized (stateParameterLock) {
      setImage(null);
      //      singlePlane.initDataArray(sliceDims);
      //      CNUTypes.copyRegion(inImg.getDataArray(),
      //			  inDims.getIndex(sliceOrig),
      //			  inDims.getType(), inInc,
      //			  singlePlane.getDataArray(), 0, sliceDims.getType(),
      //			  sliceDims.getDimensions(),
      //			  sliceDims.getNumberOfDimensions() - 1, getScale());
      dataSlicer.grabSliceData(inImg, slice, iValue, singlePlane, getScale());
      ColorModel cm = getColorModel();
      if(cm == null) cm = DisplayComponentDefaults.getDefaultColorModel();
      CNUDimensions singlePlaneDims = singlePlane.getDimensions();
      if(CNUTypes.bytesPerWord(singlePlaneDims.getType()) == 1)
	setImageProducer(new MemoryImageSource(singlePlaneDims.xdim(),
					       singlePlaneDims.ydim(), cm,
					       (byte[])singlePlane.getDataArray(),
					       singlePlaneDims.getOffset(),
					       singlePlaneDims.xdim()));
      else
	setImageProducer(new MemoryImageSource(singlePlaneDims.xdim(),
					       singlePlaneDims.ydim(), cm,
					       (int[])singlePlane.getDataArray(),
					       singlePlaneDims.getOffset(),
					       singlePlaneDims.xdim()));
      initColorModel(cm);  // sets color map filter not needed
    }
  }
  /**
   * Sets both slice and orientation labels mode.
   *
   * @param sliceLabelOn	<code>true</code> to display the slice label
   * @param orientationLabelsOn	<code>true</code> to display orientation labels
   */
  public void setSliceAndOrientationLabels(boolean sliceLabelOn,
					   boolean orientationLabelsOn) {
    setSliceLabelOn(sliceLabelOn);
    setOrientationLabelsOn(orientationLabelsOn);
  }
  /**
   * Sets the orientation labels show or hide state.
   *
   * @param mode	<code>true</code> to display orientation labels
   */
  public void setOrientationLabelsOn(boolean mode) {
    synchronized (labelLock) {
      if(orientationLabelsOn != mode) {
        orientationLabelsOn = mode;
	invalidate();
      }
    }
  }
  /**
   * Gets the orientation labels show or hide state.
   *
   * @return	<code>true</code> if orientation labels are shown,
   *		<code>false</code> otherwise
   */
  public boolean getOrientationLabelsOn() {
    synchronized (labelLock) {
      return orientationLabelsOn;
    }
  }
  /**
   * Sets orientation labels
   *
   * @param north	label to designate orientation of top of image
   * @param south	label to designate orientation of bottom of image
   * @param west	label to designate orientation on right side of image
   * @param east	label to designate orientation on left side of image
   */
  public void setOrientationLabels(String north, String south,
				   String west, String east) {
    synchronized (labelLock) {
      this.north = north;
      this.south = south;
      this.west = west;
      this.east = east;
      invalidate();
    }
  }
  /**
   * Sets the default orientations labels according to the
   * sliceViewMode and the orientationOrder or coordinateMap.
   */
  public void setOrientationLabels() {
    String westLR = "R"; String eastLR = "L";
    String northAP = "A"; String southAP = "P";
    String northSI = "S"; String southSI = "I";
    String west = westLR; String east = eastLR;
    String north = northAP; String south = southAP;

    CoordinateMap coorM = getCoordinateMap();
    if(coorM != null) {
      XYZDouble res = getCoordinateResolutions();
      int xend = 1; int yend = 1;
      if(dataSlicer != null) {
	CNUDimensions sliceDims = dataSlicer.getSliceDimensions();
	if(sliceDims != null) { xend = sliceDims.xdim() - 1; yend = sliceDims.ydim() - 1;}
      }
      XYZDouble pt0 =
	coorM.toSpace(new XYZDouble(getIndicesFromNonfilteredPoint(new Point(0, 0)), 0), res);

      XYZDouble ptx =
	coorM.toSpace(new XYZDouble(getIndicesFromNonfilteredPoint(new Point(xend, 0)), 0), res);
      ptx.add(-pt0.x, -pt0.y, -pt0.z);
      if(Math.abs(ptx.z) > Math.abs(ptx.x) && Math.abs(ptx.z) > Math.abs(ptx.y)) {
	if(ptx.z < 0) { westLR = "S"; eastLR = "I"; }
	else { westLR = "I"; eastLR = "S"; }
      }
      else if(Math.abs(ptx.y) > Math.abs(ptx.x)) {
	if(ptx.y < 0) { westLR = "A"; eastLR = "P"; }
	else { westLR = "P"; eastLR = "A"; }
      }
      else {
	if(ptx.x < 0) { westLR = "R"; eastLR = "L"; }
	else { westLR = "L"; eastLR = "R"; }
      }
      XYZDouble pty =
	coorM.toSpace(new XYZDouble(getIndicesFromNonfilteredPoint(new Point(0, yend)), 0), res);
      pty.add(-pt0.x, -pt0.y, -pt0.z);
      if(Math.abs(pty.x) > Math.abs(pty.y) && Math.abs(pty.x) > Math.abs(pty.z)) {
	if(pty.x < 0) { northAP = "R"; southAP = "L"; }
	else { northAP = "L"; southAP = "R"; }
      }
      else if(Math.abs(pty.z) > Math.abs(pty.y)) {
	if(pty.z < 0) { northAP = "S"; southAP = "I"; }
	else { northAP = "I"; southAP = "S"; }
      }
      else {
	if(pty.y < 0) { northAP = "A"; southAP = "P"; }
	else { northAP = "P"; southAP = "A"; }
      }
      west = westLR; east = eastLR;
      north = northAP; south = southAP;
    }
    else {
      int orientationOrder = inImg.getDimensions().getOrientationOrder();
      if((orientationOrder & CNUDimensions.RIGHT_POSITIVE) != 0) {
        westLR = "L"; eastLR = "R";
      }
      if((orientationOrder & CNUDimensions.ANTERIOR_POSITIVE) != 0) {
        northAP = "P"; southAP = "A";
      }
      if((orientationOrder & CNUDimensions.SUPERIOR_POSITIVE) != 0) {
        northSI = "I"; southSI = "S";
      }
      switch(getSliceViewMode()) {
      default:
      case CNUDimensions.TRANSVERSE:
      case CNUDimensions.XY_SLICE:
	west = westLR; east = eastLR;
	north = northAP; south = southAP;
	break;
      case CNUDimensions.YX_SLICE:
	west = northAP; east = southAP;
	north = westLR; south = eastLR;
	break;
      case CNUDimensions.CORONAL:
      case CNUDimensions.XZ_SLICE:
	west = westLR; east = eastLR;
	north = northSI; south = southSI;
	break;
      case CNUDimensions.ZX_SLICE:
	west = northSI; east = southSI;
	north = westLR; south = eastLR;
	break;
      case CNUDimensions.SAGITTAL:
      case CNUDimensions.YZ_SLICE:
	west = northAP; east = southAP;
	north = northSI; south = southSI;
	break;
      case CNUDimensions.ZY_SLICE:
	west = northSI; east = southSI;
	north = northAP; south = southAP;
	break;
      }
    }
    setOrientationLabels(north, south, west, east);
  }
  /**
   * Sets the slice label show or hide state.
   *
   * @param mode	<code>true</code> to display the slice label
   */
  public void setSliceLabelOn(boolean mode) {
    synchronized (labelLock) {
      if(sliceLabelOn != mode) {
        sliceLabelOn = mode;
	invalidate();
      }
    }
  }
  /**
   * Gets the slice label show or hide state.
   *
   * @return	<code>true</code> if the slice label is shown,
   *		<code>false</code> otherwise
   */
  public boolean getSliceLabelOn() {
    synchronized (labelLock) {
      return sliceLabelOn;
    }
  }
  /**
   * Sets slice label.
   *
   * @param sliceLabel	label to display at bottom center of image
   */
  public void setSliceLabel(String sliceLabel) {
    synchronized (labelLock) {
      if(sliceLabel == null) {
	if(this.sliceLabel != null) {
          this.sliceLabel = sliceLabel;
          invalidate();
	}
      }
      else if(! sliceLabel.equals(this.sliceLabel)) {
        this.sliceLabel = sliceLabel;
        invalidate();
      }
    }
  }


  /**
   * Sets iValue label.
   *
   * @param iValueLabel	label to display at bottom left center of image
   */
  public void setIValueLabel(String iValueLabel) {
    synchronized (labelLock) {
      if(iValueLabel == null) {
	if(this.iValueLabel != null) {
          this.iValueLabel = iValueLabel;
          invalidate();
	}
      }
      else if(! iValueLabel.equals(this.iValueLabel)) {
        this.iValueLabel = iValueLabel;
        invalidate();
      }
    }
  }
  /**
   * Sets the default iValue label based on iValue and coordinate map.
   */
  public void setIValueLabel() {
    synchronized (stateParameterLock) {
      if(inImg.getDimensions().getNumberOfDimensions() < 3) setIValueLabel(null);
      else setIValueLabel("i=" + Integer.toString(getIValue()));
    }
  }
  /**
   * Sets the iValue label show or hide state.
   *
   * @param mode	<code>true</code> to display the iValue label
   */
  public void setIValueLabelOn(boolean mode) {
    synchronized (labelLock) {
      if(iValueLabelOn != mode) {
        iValueLabelOn = mode;
	invalidate();
      }
    }
  }
  /**
   * Gets the iValue label on status
   *
   * @return	<code>true</code> if the iValue label is shown,
   *		<code>false</code> otherwise
   */
  public boolean getIValueLabelOn() {
    synchronized (labelLock) {
      return iValueLabelOn;
    }
  }
  /**
   * Sets the number format for converting numbers to strings.
   *
   * @param numberFormat	number format tool
   */
  public void setNumberFormat(NumberFormat numberFormat) {
    if(numberFormat != null) {
      this.numberFormat = (NumberFormat) numberFormat.clone();
      setSliceLabel();
    }
  }
  /**
   * Gets the number format.
   *
   * @return	number format
   */
  public NumberFormat getNumberFormat() {
    return (NumberFormat) numberFormat.clone();
  }
  /**
   * Sets the default slice label based on slice and coordinate map.
   */
  public void setSliceLabel() {
    CoordinateMap coorM = getCoordinateMap();
    synchronized (stateParameterLock) {
      if(coorM == null) setSliceLabel( Integer.toString(getSlice()) );
      else {
	// JTL 3/8/2018 changed to determine value from least changing and average or center of image
	XYZDouble res = getCoordinateResolutions();
	CNUDimensions sliceDims = dataSlicer.getSliceDimensions();
        XYZDouble xyzpt = coorM.toSpace(new XYZDouble(getIndicesFromNonfilteredPoint( new Point(0, 0) ),0), res);
        XYZDouble xyzpt2 = coorM.toSpace(new XYZDouble(getIndicesFromNonfilteredPoint( new Point(sliceDims.xdim()-1, sliceDims.ydim()-1) ),0), res);
	double value;
	double abs_diff_x = Math.abs(xyzpt.x - xyzpt2.x);
	double abs_diff_y = Math.abs(xyzpt.y - xyzpt2.y);
	double abs_diff_z = Math.abs(xyzpt.z - xyzpt2.z);
	double smallest_diff = abs_diff_z;
	if(abs_diff_x < abs_diff_z) {
	  if(abs_diff_x < abs_diff_y) { value = (xyzpt.x + xyzpt2.x)/2; smallest_diff = abs_diff_x; }
	  else { value = (xyzpt.y + xyzpt2.y)/2; smallest_diff = abs_diff_y; }
	}
	else if(abs_diff_y < abs_diff_z) { value = (xyzpt.y + xyzpt2.y)/2; smallest_diff = abs_diff_y; }
	else value = (xyzpt.z + xyzpt2.z)/2;

	/* slice view modes like XY may not correspond to transverse, coronal or sagittal
	switch(getSliceViewMode()) {
	default:
	case CNUDimensions.TRANSVERSE:
	case CNUDimensions.XY_SLICE:
	case CNUDimensions.YX_SLICE:
	  value = xyzpt.z;
	  break;
	case CNUDimensions.CORONAL:
	case CNUDimensions.XZ_SLICE:
	case CNUDimensions.ZX_SLICE:
	  value = xyzpt.y;
	  break;
	case CNUDimensions.SAGITTAL:
	case CNUDimensions.YZ_SLICE:
	case CNUDimensions.ZY_SLICE:
	  value = xyzpt.x;
	  break;
	}
	*/
	if(smallest_diff > 1e-16) setSliceLabel("<" + numberFormat.format(value * 1e3) + "mm>");
	else setSliceLabel(numberFormat.format(value * 1e3) + "mm");
      }
    }
  }
  /**
   * Gets the input data.
   *
   * @return	input image data
   */
  public CNUData getData() { return inImg; }
  /**
   * Gets the scale object that is used for converting voxel values to
   * lookup table indices.
   *
   * @return	the scaling object
   */
  public CNUScale getScale() {
    synchronized (stateParameterLock) {
      return sc;
    }
  }
  /**
   * Sets the scale object for converting voxel values to lookup table indices.
   *
   * @param sc	the scaling object
   */
  public void setScale(CNUScale sc) {
    synchronized (stateParameterLock) {
      if( sc != null && inImg != null ) {
        if( sc.getQuantificationState() )
	  sc = sc.quantificationAdjusted(inImg.getFactor());
        else {
	  sc = sc.ScaleDuplicate();
	  sc.setQuantification(inImg.getFactor());
        }
      }
      this.sc = sc;
    }
  }
  /**
   * Applies a new CNUScale factor.
   */
  public void updateScale() {
    synchronized (stateParameterLock) {
      createRawIp();
      invalidateFilters();
    }
    Component parent = getParent();
    if(parent != null) {
      Rectangle bounds = getBounds();
      parent.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
    }
  }
  /**
   * Gets the data slicer.
   */
  public CNUDataSlicer getDataSlicer() {
    return dataSlicer;
  }
  /**
   * Sets the data slicer.
   *
   * @param dataSlicer new data slicer to use
   */
  public void setDataSlicer(CNUDataSlicer dataSlicer) {
    synchronized (stateParameterLock) {
      int numslices = dataSlicer.getNumberOfSlices();
      if(slice >= numslices) return;  // new data slicer doesn't support current slice and don't want to reset slice
      if(! inImg.sameDimensions(dataSlicer.getInDimensions())) return; // data slicers only work with set input dimensions
      this.dataSlicer = dataSlicer;
    }
    checkUpdateDataCoordinateMap(); // data changing data slicer might change coordinate map
    setSliceLabel();
    setIValueLabel();
    setCrosshair(getCrosshairIndices(), getCrosshairColor());
    createRawIp();
    invalidateFilters();
  }
  /**
   * Gets the I value.
   *
   * @return	the I value
   */
  public int getIValue() {
    return iValue;
  }
  /**
   * Sets and displays a new iValue of the input data.
   *
   * @param iValue	the I value number
   */
  public void setIValue( int iValue ) {
    synchronized (stateParameterLock) {
      // wrap i value to within range of data
      int isize = inImg.getDimensions().idim();
      iValue = iValue % isize;
      if(iValue < 0) iValue += isize;
      this.iValue = iValue;
    }
    checkUpdateDataCoordinateMap();// data changing iValue might change coordinate map
    setSliceLabel();
    setIValueLabel();
    setCrosshair(getCrosshairIndices(), getCrosshairColor());
    createRawIp();
    invalidateFilters();
  }
  /**
   * Gets the slice view mode.
   *
   * @return	the slice view mode
   */
  public int getSliceViewMode() {
    synchronized (stateParameterLock) {
      return dataSlicer.getSliceViewMode();
    }
  }
  /**
   * Gets a copy of the slice origin indices relative to the input data.
   *
   * @return slice origin indices
  public int[] getSliceOrigin() {
    return CNUDimensions.arrayDuplicate(sliceOrig);
  }
   */
  /**
   * Gets the slice number associated with a raw data point location
   * for this images view mode.
   *
   * @param point	raw data location to determine slice number for
   * @return		slice number or <code>-1</code> if not contained in data.
   */
  public int getSlice(int[] point) {
    return dataSlicer.getSliceNumberFromDataIndices(point);
  }
  /**
   * Gets the displayed slice number relative to the input data.
   *
   * @return	the slice number
   */
  public int getSlice() {
    return slice;
  }
  /**
   * Sets and displays a new slice of the input data.
   *
   * @param slice	the slice number
   */
  public void setSlice( int slice ) {
    synchronized (stateParameterLock) {
      if(dataSlicer == null) return;
      // wrap slice number to within range of data slicer
      int numslices = dataSlicer.getNumberOfSlices();
      slice = slice % numslices;
      if(slice < 0) slice += numslices;
      this.slice = slice;
    }
    checkUpdateDataCoordinateMap();// data changing slice might change coordinate map
    setSliceLabel();
    setIValueLabel();
    setCrosshair(getCrosshairIndices(), getCrosshairColor());
    createRawIp();
    invalidateFilters();
  }
  /**
   * Calculates and resets the preferred size for this component.
   */
  public void updateSize() {
    super.updateSize();

    fnorth = north; fsouth = south;
    fwest = west; feast = east;
    // move labels do to flips
    if(getFlipV()) { fnorth = south; fsouth = north; }
    if(getFlipH()) { fwest = east; feast = west; }
    // move labels do to rotation
    double a = getRotation() * Math.PI / 180.0d;
    double c = Math.cos(a); double s = Math.sin(a);
    if(c < 0.7071) {
      // angle between 45 degrees and 315 degrees
      String t;
      if(s > 0.7071) {
	// angle between 45 and 135
	t = fnorth; fnorth = feast; feast = fsouth; fsouth = fwest; fwest = t;
      }
      else if(s > -0.7071) {
	// angle between 135 and 225
	t = feast; feast = fwest; fwest = t;
	t = fnorth; fnorth = fsouth; fsouth = t;
      }
      else {
	// angle berween 225 and 315
	t = fnorth; fnorth = fwest; fwest = fsouth; fsouth = feast; feast = t;
      }
    }

    Dimension displayImageSize = getImageSize();
    int min_width = displayImageSize.width;
    int min_height = displayImageSize.height;
    synchronized (labelLock) {
      if(orientationLabelsOn || sliceLabelOn || iValueLabelOn) {
        FontMetrics fm = getFontMetrics(getFont());
	int ncharhigh = 0;
	int bottomStringWidth = 0;
        if(orientationLabelsOn) {
	  // append fnorth label to width and number of characters high
	  if(fnorth != null) {
	    ncharhigh++;
	    min_width = Math.max(min_width,
			  displayImageSize.width + FONTPRESPACE +
			  fm.stringWidth(fnorth));
	  }
	  // append fsouth label to width and number of characters high
	  if(fsouth != null) {
	    ncharhigh++;
	    min_width = Math.max(min_width,
			  displayImageSize.width + FONTPRESPACE +
			  fm.stringWidth(fsouth));
	  }
	  // append feast and fwest to length of bottom string
	  if((fwest != null) || (feast != null)) {
	    if(fwest != null) bottomStringWidth += fm.stringWidth(fwest);
	    if(feast != null) bottomStringWidth += fm.stringWidth(feast);
	  }
        }
	// append sliceLabel to length of bottom string
	if(sliceLabelOn && (sliceLabel != null))
	  bottomStringWidth += fm.stringWidth(sliceLabel);
	// total bottom width sets a minimum width
	min_width = Math.max(min_width, bottomStringWidth);
	if(bottomStringWidth != 0) {
	  min_height += fm.getHeight();
	  ncharhigh++;
	}
	// add ivalue line below
	if(iValueLabelOn && (iValueLabel != null)) {
	  int subbottomStringWidth = fm.stringWidth(iValueLabel);
	  min_width = Math.max(min_width, subbottomStringWidth);
	  if(subbottomStringWidth != 0) {
	    min_height += fm.getHeight();
	    ncharhigh++;
	  }
	}
	// number of characters high sets a minimum height
	min_height = Math.max(min_height, ncharhigh * fm.getHeight());
      }
    }
    setPreferredSize(min_width, min_height);
  }
  /**
   * Gets the present input image data type.
   *
   * @return	the image data CNUtype
   */
  public int getType() {
    if(inImg == null) return CNUTypes.UNKNOWN;
    return inImg.getDimensions().getType();
  }
  /**
   * Gets the coordinate resolutions.
   *
   * @return the coordinate resolutions
   */
  public XYZDouble getCoordinateResolutions() {
    double[] spatialRes = inImg.getDimensions().getSpatialResolutions();
    if( spatialRes == null ) return null;
    return new XYZDouble(spatialRes, 0);
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
    return dataSlicer.getDataIndicesFromSlicePoint(pt, getSlice(), getIValue());
  }
 /**
   * Gets the point relative to the non-filtered
   * (no flip, rotation, zoom, or offset) image given the indices
   * corresponding to the original raw data this display component
   * is created from.
   *
   * @param	indices indices to original raw data which may have
   *		any number of dimensions
   * @return	location relative to non-filtered image (negative value
   *		for x or y indicates the indice doesn't map to that dimension)
   */
  public Point getNonfilteredPoint(int[] indices) {
    return dataSlicer.getSlicePointFromDataIndices(indices);
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
    return dataSlicer.inSlice(slice, indices);
  }
  /**
   * Gets the value at the given data indice.
   *
   * @param indices	indice to get value for
   * @return		value at point
   */
  public double getValue(int[] indices) {
    if(inImg.getDimensions().contains(indices)) return inImg.getPixelAsDouble(indices);
    else return Double.NaN;
  }
  /**
   * Gets the quantificaton factor for this image.
   *
   * @return	the quantification factor
   */
  public double getFactor() { return inImg.getFactor(); }
  /**
   * Gets the name for this image.
   *
   * @return	the name of this image
   */
  public String getName() {
    if(inImg == null) return null;
    else return inImg.getName();
  }
  /** 
   * Gets the input data if it represents the same file.
   *
   * @param sameFileAsObj	file object to check compare with
   * @return	input data if it represents the same file as sameFileObj,
   *		<code>null</code> otherwise
   */
  public Object getFileObject(Object sameFileAsObj) {
    if(inImg instanceof CNUFileObject)
      return ((CNUFileObject) inImg).getFileObject(sameFileAsObj);
    else return super.getFileObject(sameFileAsObj);
  }
  /**
   * Paints the image to the screen.
   *
   * @param g	Graphics area to paint to
   */
  public void paint(Graphics g) {
    super.paint(g);
    synchronized (labelLock) {
      if(orientationLabelsOn || sliceLabelOn || iValueLabelOn) {
        g.setColor(getForeground());
        g.setFont(getFont());
        FontMetrics fm = g.getFontMetrics();
        Dimension displayImageSize = getImageSize();
	int bottom; // not initialized so compiler will flag if missing a conditional below
        if(orientationLabelsOn) {
	  int left = displayImageSize.width + FONTPRESPACE;
	  if(fnorth != null) {
	    bottom = fm.getAscent();
	    g.drawString(fnorth, left, bottom);
	  }
	  else bottom = 0;
	  if(fsouth != null) {
	    // put south near bottom of image but below upper text
	    bottom = Math.max(displayImageSize.height,
			      bottom + fm.getAscent());
	    g.drawString(fsouth, left, bottom);
	  }
	  bottom = fm.getAscent() + Math.max(bottom, displayImageSize.height);
	  left = 0;
	  if(fwest != null) {
	    g.drawString(fwest, left, bottom);
	    left += fm.stringWidth(fwest);
	  }
   	  if(sliceLabelOn && (sliceLabel != null)) {
	    int sliceWidth = fm.stringWidth(sliceLabel);
	    left = Math.max((displayImageSize.width - sliceWidth)/2, left);
	    g.drawString(sliceLabel, left, bottom);
	    left += sliceWidth;
	  }
	  if(feast != null) {
	    left = Math.max(left, displayImageSize.width - fm.stringWidth(feast));
	    g.drawString(feast, left, bottom);
	  }
        }
        else if(sliceLabelOn && (sliceLabel != null)) {
	  bottom = fm.getAscent() + displayImageSize.height;
	  g.drawString(sliceLabel,
		       Math.max(0,(displayImageSize.width - fm.stringWidth(sliceLabel))/2), bottom);
        }
	else bottom = displayImageSize.height;
	if(iValueLabelOn && (iValueLabel != null)) {
	  bottom += fm.getAscent();
	  g.drawString(iValueLabel,
		       Math.max(0,(displayImageSize.width - fm.stringWidth(iValueLabel))/2), bottom);
	}
      }
    }
  }
  /**
   * Determines the dimension (x, y or z) associated with the slice view.
   *
   * @param inDims		dimensions of data
   * @param sliceViewMode	TRANSVERSE, CORONAL, SAGITTAL...
   * @return			dimension number (0, 1, or 2)
   */
  public static int getSliceNumberDimension(CNUDimensions inDims,
					    int sliceViewMode) {
    return iiv.data.PrimaryOrthoDataSlicer.getSliceNumberDimension(inDims, sliceViewMode);
  }
  /*
    // Determine dimensions associated with slice
    int sliceDim = 2;
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
  }
  */
  /**
   * Calculates the display dimensions for viewing.
   *
   * @param inDims	dimensions of data
   * @param sliceViewMode	TRANSVERSE, CORONAL, or SAGITTAL
   * @return		dimensions for a single slice
  public static CNUDimensions getSliceDimensions(CNUDimensions inDims,
						 int sliceViewMode,
						 ColorModel colorModel) {
    CNUDimensions outdims = new CNUDimensions();
    int displayType = CNUTypes.UNSIGNED_INTEGER;
    if(colorModel != null) {
      if(colorModel.getPixelSize() <= 8) displayType = CNUTypes.UNSIGNED_BYTE;
    }
    outdims.set2DValues(inDims.xdim(), inDims.ydim(), displayType, 0);
    switch (inDims.getOrientation()) {
    case CNUDimensions.TRANSVERSE:      // x=R-L, y=P-A, z=S-I
      if(sliceViewMode == CNUDimensions.CORONAL)
	outdims.set2DValues(inDims.xdim(), inDims.zdim(),
			    displayType, 0);
      else if(sliceViewMode == CNUDimensions.SAGITTAL)
	outdims.set2DValues(inDims.ydim(), inDims.zdim(),
			    displayType, 0);
      break;
    case CNUDimensions.CORONAL:      // x=R-L, y=S-I, z=P-A
      if(sliceViewMode == CNUDimensions.SAGITTAL)
	outdims.set2DValues(inDims.zdim(), inDims.ydim(),
			    displayType, 0);
      else if(sliceViewMode == CNUDimensions.TRANSVERSE)
	outdims.set2DValues(inDims.xdim(), inDims.zdim(),
			    displayType, 0);
      break;
    case CNUDimensions.SAGITTAL:      // x=P-A, y=S-I, z=R-L
      if(sliceViewMode == CNUDimensions.TRANSVERSE)
	outdims.set2DValues(inDims.zdim(), inDims.xdim(),
			    displayType, 0);
      else if(sliceViewMode == CNUDimensions.CORONAL)
	outdims.set2DValues(inDims.zdim(), inDims.ydim(),
			    displayType, 0);
      break;
    }
    return outdims;
  }
   */
  /** 
   * Calculates the input increments to get a display slice for viewing.
   *
   * @param inDims	dimensions of data
   * @param sliceViewMode	TRANSVERSE, CORONAL, or SAGITTAL
   * @return		increments to transverse a single slice
  public static int[] getInputToDisplayIncrements(CNUDimensions inDims,
						  int sliceViewMode)
  {
    int [] inInc = inDims.getIncrements();
    switch (inDims.getOrientation()) {
    case CNUDimensions.TRANSVERSE:      // x=R-L, y=P-A, z=S-I
      if(sliceViewMode == CNUDimensions.CORONAL) {
	int tmp = inInc[1]; inInc[1] = inInc[2]; inInc[2] = tmp;
      }
      else if(sliceViewMode == CNUDimensions.SAGITTAL) {
	int tmp = inInc[0]; inInc[0] = inInc[1];
	inInc[1] = inInc[2]; inInc[2] = tmp;
      }
      break;
    case CNUDimensions.CORONAL:      // x=R-L, y=S-I, z=P-A
      if(sliceViewMode == CNUDimensions.SAGITTAL) {
	int tmp = inInc[0]; inInc[0] = inInc[2]; inInc[2] = tmp;
      }
      else if(sliceViewMode == CNUDimensions.TRANSVERSE) {
	int tmp = inInc[1]; inInc[1] = inInc[2]; inInc[2] = tmp;
      }
      break;
    case CNUDimensions.SAGITTAL:      // x=P-A, y=S-I, z=R-L
      if(sliceViewMode == CNUDimensions.TRANSVERSE) {
	int tmp = inInc[0]; inInc[0] = inInc[2];
	inInc[2] = inInc[1]; inInc[1] = tmp;
      }
      else if(sliceViewMode == CNUDimensions.CORONAL) {
	int tmp = inInc[0]; inInc[0] = inInc[2]; inInc[2] = tmp;
      }
      break;
    }
    return inInc;
  }
   */
} // end SingleImg class
