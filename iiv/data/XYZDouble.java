package iiv.data;
/**
 * Class to hold x, y, and z double values.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.CNUViewer
 * @since	iiV1.0
 */
public class XYZDouble {
  public double x = 0;
  public double y = 0;
  public double z = 0;
  public XYZDouble() {}
  /**
   * Constructs a new instance of XYZDouble copying values from another
   * instance.
   *
   * @param X	values to copy
   */
  public XYZDouble(XYZDouble X) {
    setValues(X.x, X.y, X.z);
  }
  /**
   * Constructs a new instance of XYZDouble setting to given values.
   *
   * @param x	x value
   * @param y	y value
   * @param z	z value
   */
  public XYZDouble(double x, double y, double z) {
    setValues(x, y, z);
  }
  /**
   * Constructs a new instance of XYZDouble setting to values from an array.
   *
   * @param X		array containing values to intialize to
   * @param offset	offset to first (x) value in array
   */
  public XYZDouble(double[] X, int offset) {
    setValues(X, offset);
  }
  /**
   * Constructs a new instance of XYZDouble setting to values from an
   * int array.
   *
   * @param X		array containing values to intialize to
   * @param offset	offset to first (x) value in array
   */
  public XYZDouble(int[] X, int offset) {
    setValues(X, offset);
  }
  /**
   * Generates a string representation.
   *
   * @return	string representation
   */
  public synchronized String toString() {
    return("[" + x + ", " + y + ", " + z + "]");
  }
  /**
   * Sets values from another XYZDouble.
   *
   * @param X	values to set to
   */
  public synchronized void setValues(XYZDouble X) {
    setValues(X.x, X.y, X.z);
  }
  /**
   * Sets values to given.
   *
   * @param x	new x value
   * @param y	new y value
   * @param z	new z value
   */
  public synchronized void setValues(double x, double y, double z) {
    this.x = x; this.y = y; this.z = z;
  }
  /**
   * Sets values from an array.
   *
   * @param X		array containing values to intialize to
   * @param offset	offset to first (x) value in array
   */
  public synchronized void setValues(double X[], int offset) {
    if( X == null ) setValues(0.0, 0.0, 0.0);
    else if( (offset + 2) < X.length )
      setValues(X[offset], X[offset + 1], X[offset + 2]);
    else if( (offset + 1) < X.length)
      setValues(X[offset], X[offset + 1], 0.0);
    else if( offset < X.length)
      setValues(X[offset], 0.0, 0.0);
    else setValues(0.0, 0.0, 0.0);
  }
  /**
   * Sets values from an int array.
   *
   * @param X		array containing values to intialize to
   * @param offset	offset to first (x) value in array
   */
  public synchronized void setValues(int X[], int offset) {
    if( X == null ) setValues(0.0, 0.0, 0.0);
    else if( (offset + 2) < X.length )
      setValues(X[offset], X[offset + 1], X[offset + 2]);
    else if( (offset + 1) < X.length)
      setValues(X[offset], X[offset + 1], 0.0);
    else if( offset < X.length)
      setValues(X[offset], 0.0, 0.0);
    else setValues(0.0, 0.0, 0.0);
  }
  /**
   * Add to the x, y, and z values.
   *
   * @param X	values to add.
   */
  public synchronized void add(XYZDouble X) {
    this.x += X.x; this.y += X.y; this.z += X.z;
  }
  /**
   * Add to the x, y, and z values.
   *
   * @param x	x value to add
   * @param y	y value to add
   * @param z	z value to add
   */
  public synchronized void add(double x, double y, double z) {
    this.x += x; this.y += y; this.z += z;
  }
  /**
   * Multiply the x, y, and z values by the same factor.
   *
   * @param factor	factor to multiply by
   */
  public synchronized void scale(double factor) {
    this.x *= factor; this.y *= factor; this.z *= factor;
  }
  /**
   * Multiply the x, y, and z values by different factors.
   *
   * @param factor	factors to multiply by
   */
  public synchronized void scale(XYZDouble factor) {
    this.x *= factor.x; this.y *= factor.y; this.z *= factor.z;
  }
  /**
   * Multiply the x, y, and z values by different factors.
   *
   * @param xFactor	factor to multiply x by
   * @param yFactor	factor to multiply y by
   * @param zFactor	factor to multiply z by
   */
  public synchronized void scale(double xFactor, double yFactor,
				 double zFactor) {
    this.x *= xFactor; this.y *= yFactor; this.z *= zFactor;
  }
}
