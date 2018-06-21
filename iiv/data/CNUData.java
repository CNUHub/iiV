package iiv.data;
import java.awt.*;
import java.awt.image.*;
/**
 * Class to store a multidimensional data array.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUDimensions
 * @since	iiV1.0
 */
public class CNUData {
  private Object dataArray = null;
  private CNUDimensions dims = null;
  private double factor=1;
  /**
   * Constructs a new instance of CNUData.
   */
  public CNUData() { }
  /**
   * Sets the dimensions and dataArray.
   *
   * @param dims	Dimensions of the data array
   * @param dataArray	The array containing the data.  The array is kept
   *			without	copying the data.
   */
  public void setDataArray( CNUDimensions dims, Object dataArray ) {
    setDimensions(dims);
    setDataArray(dataArray);
  }
  /**
   * Sets the dataArray.
   *
   * @param dataArray	The array containing the data.  The array is kept
   *			without	copying the data.
   */
  public synchronized void setDataArray( Object dataArray ) {
    this.dataArray = dataArray;
  }
  /**
   * Gets the data array.
   *
   * @return	the dataArray without copying it
   */
  public synchronized Object getDataArray() {
    return(dataArray);
  }
  /**
   * Initializes a blank data array with given dimensions.
   *
   * @param dims	dimensions of data array to create
   */
  public void initDataArray( CNUDimensions dims ) {
    setDataArray(dims, CNUTypes.arrayOf(dims.getType(),	dims.lengthInWords()));
  }
  /**
   * Creates a string representation.
   *
   * @return	a string representation
   */
  public String toString() {
    String s = getName();
    if(s == null) s = "no name";
    s += "\n";
    synchronized (this) {
      if(dims != null) s += dims;
      s += "factor=" + factor;
    }
    return s;
  }
  /**
   * Sets the dimensions.
   *
   * @param dims	The dimensions of the data.  A local clone is created.
   */
  public synchronized void setDimensions( CNUDimensions dims ) {
    if(dims == null) this.dims = null;
    else this.dims = (CNUDimensions) dims.clone();
  }
  /**
   * Gets a copy of the dimensions.
   *
   * @return	a copy of the dimensions, may be <code>null</code>
   */
  public synchronized CNUDimensions getDimensions() {
    if(dims == null) return null;
    return (CNUDimensions) dims.clone();
  }
  /**
   * Checks if this data has the same dimensions as the given dimensions.
   *
   * @param dims2 dimension to compare too.
   * @ return <code>true</code> if the same dimensions and not <code>null</code>.
   */
  public synchronized boolean sameDimensions(CNUDimensions dims2) {
    if(dims == null) return false;
    return dims.equals(dims2);
  }
  /**
   * Checks if this data has the same dimensions as the given data.
   *
   * @param data data to compare too.
   * @ return <code>true</code> if the same dimensions and not <code>null</code> dimensions.
   */
  public synchronized boolean sameDimensions(CNUData data2) {
    if(data2 == null) return false;
    if(dims == null) return false;
    return data2.sameDimensions(dims);
  }
  /**
   * Sets a pixel at point from a int value.
   *
   * @param point	array containing indices to pixel to set
   * @param value	value to set pixel to
   */
  final synchronized public void setPixel(int[] point, int value) {
    CNUTypes.setArrayValue(value, getDataArray(),
			   dims.getIndex(point), dims.getType());
  }
  /**
   * Returns a pixel value at point as an integer.
   *
   * @param point	array containing indices to pixel to get
   * @return		pixel value as an integer
   */
  final synchronized public int getPixelAsInt(int[] point) {
    return CNUTypes.getArrayValueAsInt(getDataArray(), dims.getIndex(point),
				       dims.getType());
  }
  /**
   * Sets a pixel at point from a double value.
   *
   * @param point	array containing indices to pixel to get
   * @param value	value to set pixel to
   */
  final synchronized public void setPixel(int[] point, double value) {
    CNUTypes.setArrayValue(value, getDataArray(), dims.getIndex(point),
			   dims.getType());
  }
  /**
   * Returns a pixel value at point location as a double.
   *
   * @param point	array containing indices to pixel to get
   * @return		pixel value as a double
   */
  final synchronized public double getPixelAsDouble(int[] point) {
    return CNUTypes.getArrayValueAsDouble(getDataArray(), dims.getIndex(point),
					  dims.getType());
  }
  /**
   * Sets the quantification factor.
   *
   * @param factor	quantification factor for the data
   */
  public synchronized void setFactor(double factor) {
    this.factor = factor;
  }
  /**
   * Gets the quantification factor.
   *
   * @return	quantification factor for the data
   */
  public synchronized double getFactor() {
    return(factor);
  }
  /**
   * Gets the name for this data.  Must be overriden to return real info.
   *
   * @return	<code>null</code>.
   */
  public String getName() { return null; }
  /**
   * Gets the coordinate mapping that should be the default for this data.
   * May be overriden to return real info.
   *
   * @return	coordinate mapping object or <code>null</code>
   */
  public CoordinateMap getCoordinateMap() {
    return null;
  }
  /**
   * Gets the coordinate mapping that should be the default for this data.
   * Defaults to calling getCoordinateMap().
   * @param sliceViewMode	the slice orientation to display
   *				<code>CNUDimensions.TRANSVERSE</code>,
   *				<code>CNUDimensions.CORONAL</code>,
   *				<code>CNUDimensions.SAGITTAL</code>...
   * @param slice		the slice number in the range from 0 to
   *				the number of slices for the sliceViewMode
   * @param ivalue		the 4th or i dimension to use for displaying
   *				a slice from 4 dimensional data
   *
   * @return	coordinate mapping object or <code>null</code>
   */
    public CoordinateMap getCoordinateMap(int sliceViewMode, int slice, int ivalue) {
      return getCoordinateMap();
  }
  /**
   * Gets the colormodel that should be the default for this data.
   * May be overriden to return real info.
   *
   * @return	colormodel or <code>null</code>
   */
  public ColorModel getColorModel() { return null; }
}


