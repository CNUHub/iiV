package iiv.data;
import iiv.script.*;
import java.awt.Point;
/**
 * CNUDataSlicer defines routines to deal with creating data slices.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUScale
 * @since	iiV1.1.8
 */
public interface CNUDataSlicer {
  /**
   * Returns the input data dimensions the data slicer uses.
   *
   * @return the input data dimensions the data slicer uses
   */
  public CNUDimensions getInDimensions();
  /**
   * Returns the slice view mode the data slicer uses.
   *
   * @return  the slice view mode the data slicer uses
   */
  public int getSliceViewMode();
  /**
   * Returns the dimension of slices this data slicer creates.
   *
   * @return the slice dimensions this data slicer creates
   */
  public CNUDimensions getSliceDimensions();
  /**
   * Returns number of slices supported by the data and slice view.
   *
   * @return  valid slice number for this data slicer
   */
  public int getNumberOfSlices();
  /**
   * Gets the indices corresponding to the original data
   * based on a point relative to this slice.
   * Note - dims beyond the 3rd not handled by this.
   *
   * @param pt	point to get indice for
   * @param slice slice the point is on
   * @param iValue i dimension the point is on
   * @return	the indices to the data
   */
  public int[] getDataIndicesFromSlicePoint(Point pt, int slice, int iValue);
 /**
   * Gets the point relative to the resliced data given the indices
   * corresponding to the original data.
   *
   * @param	indices indices to original data
   * @return	location relative to slice (negative value
   *		for x or y indicates the indice does not map to that dimension)
   */
  public Point getSlicePointFromDataIndices(int[] indices);
  /**
   * Gets the slice number associated with a data point location.
   *
   * @param	indices indices to original data
   * @return		slice number or <code>-1</code> if not contained in data.
   */
  public int getSliceNumberFromDataIndices(int[] indices);
  /**
   * Determines if the given indices relative to the input data are located
   * in the given slice.
   *
   * @param     slice   slice to test for
   * @param	indices indices to original raw data
   * @return	<code>true</code> if indices are located in 
   */
  public boolean inSlice(int slice, int[] indices);
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
  public CNUData grabSliceData(CNUData inData, int slice, int iValue, CNUData singlePlane, CNUScale sc);
  /**
   * Gets an equivalent data slicer that will work with a new set of dimensions.
   *
   * @param newDims dimensions to get equivalent data slicer for
   * @return an equivalent data slicer for the new dimensions
   **/
  public CNUDataSlicer getEquivalentDataSlicer(CNUDimensions newDims);
  /**
   * Creates a script that may be used to recreate this data slicer.
   *
   * @param scriptedObjects scripted objects list to add this object to.
   * @return  the script
   */
  public String toScript(CNUScriptObjects scriptedObjects);
}
