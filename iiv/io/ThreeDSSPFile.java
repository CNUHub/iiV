package iiv.io;
import iiv.data.*;
import iiv.script.*;
import java.io.*;
import java.awt.image.*;
/**
 * Reads 3D-SSP dat file creating a 3D view.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		NiftiHeader
 * @since	iiV1.17a
 */
public class ThreeDSSPFile extends CNUImgFile {
  private Object privateLock = new Object();
  private CNUData orthoIndices = null;
  private CNUDimensions orthoIndicesDims = null;
  private SatoshiLinearCoordinateMap sliceCoordinateMaps[] = null;

  private class SatoshiLinearCoordinateMap extends LinearCoordinateMap {
    private int slice = 0;
    private SatoshiLinearCoordinateMap(int slice) {
      setOrigin(63.5, 56.39, 32, PIXELS);
      setScale(-1.0, -1.0, 1.0);
      setRotation(0.0,0.0,0.0);
      this.slice = slice;
    };
    public XYZDouble toSpace(XYZDouble pixelLocation,
			     XYZDouble pixelResolution ) {
      int[] loc = get3DLocation((int) Math.round(pixelLocation.x), (int) Math.round(pixelLocation.y), (int) Math.round(pixelLocation.z));
      XYZDouble inspace = super.toSpace(new XYZDouble(loc[0],loc[1],loc[2]), pixelResolution);
      return inspace;
    }
    public XYZDouble fromSpace(XYZDouble location,
			       XYZDouble pixelResolution ) {
      XYZDouble threeDloc = super.fromSpace(location, pixelResolution);
      int[] index = getSurfaceLocation((int) Math.round(threeDloc.x), (int) Math.round(threeDloc.y), (int) Math.round(threeDloc.z), slice);
      return new XYZDouble(index[0], index[1], index[2]);
    }
    /**
     * Gets the name for this coordinate map.
     *
     * @return	base file name
     */
    public String getName() {
      return "ThreeDSSP_sl" + slice;
    }
    /**
     * Creates a script that may be used to recreate this display component.
     *
     * @param scriptedObjects scripted objects list to add this object to.
     * @return  the script
     */
    public String toScript(CNUScriptObjects scriptedObjects) {
      return null; // this map can't be scripted directly
    }
  };
  /*
   * Constructs an instance of ThreeDSSPFile.
   *
   * @param filename	the file to read from with or without ".img" prefix
   * @exception	IOException	thrown if error reading from file
   */
  public ThreeDSSPFile(String filename) throws IOException {
    setFileName(filename);
    CNUDimensions dims = new CNUDimensions();
    dims.set3DValues(128, 128, 8, CNUTypes.FLOAT, 0);
    dims.set3DSpatialResolutions(2.25e-3, 2.25e-3, 2.25e-3);
    setDimensions(dims);
    orthoIndicesDims = new CNUDimensions();
    orthoIndicesDims.set3DValues(128, 128, 8, CNUTypes.INTEGER, 0);
    orthoIndicesDims.set3DSpatialResolutions(2.25e-3, 2.25e-3, 2.25e-3);
    readData();
  }
  /*
   * Constructs an instance of ThreeDSSPFile.
   *
   * @param filename	the file to read from with or without ".img", ".hdr" or ".nii" prefix
   * @param ignored	added for compatibility
   * @exception	IOException	thrown if error reading from file
   */
  public ThreeDSSPFile(String filename, Object ignored)
       throws IOException {
    this(filename);
  }
  /**
   * Reads data from the file.
   *
   * @exception IOException thrown on errors reading from the file.
   */
  public void readData() throws IOException {
    synchronized (privateLock) {
      if(orthoIndices != null) return; // data already read
      CNUFile cnufile = getCNUFile();
      if(cnufile != null) {
	Reader reader = cnufile.getReader();
	if(reader == null)
	  throw new IOException("ThreeDSSPFile.readData() Failed opening reader");
	//CNUData orthoViews = new CNUData();
	CNUDimensions orthoViewsDims = getDimensions();
	//	  orthoViews.initDataArray(orthoViewsDims);
	initDataArray(orthoViewsDims);
	float[] orthoViewsArray = (float[]) getDataArray();
	orthoIndices = new CNUData();
	orthoIndices.initDataArray(orthoIndicesDims);
	int[] orthoIndicesArray = (int[]) orthoIndices.getDataArray();
	
	StreamTokenizer tokens = new StreamTokenizer( reader );
	tokens.wordChars('/', '/'); tokens.wordChars('\\', '\\'); tokens.wordChars('.', '.');
	tokens.wordChars(':', ':'); tokens.wordChars('_', '_'); tokens.wordChars('.', '.');
	tokens.ordinaryChars(';', ';'); tokens.whitespaceChars(',', ','); tokens.quoteChar('"');
	// script type quoting
	tokens.commentChar('#');
	// set C++ type quoting
	tokens.slashSlashComments(true); tokens.slashStarComments(true);
	tokens.eolIsSignificant(true); tokens.parseNumbers();
	int c = StreamTokenizer.TT_EOL; // arbitrary to enter loop
	while(c != StreamTokenizer.TT_EOF) {
	  try {
	    c = tokens.nextToken();
	    int line = tokens.lineno();
	    double numbers[] = new double[6];
	    switch (c) {
	    case StreamTokenizer.TT_EOL:
	    case StreamTokenizer.TT_EOF:
	      break;
	    default:
	      throw new IOException("[" + line + "]" +
				    " invalid character = " + c);
	    case StreamTokenizer.TT_NUMBER:
	      tokens.pushBack();
	      int cnt = CNUDisplayScript.getNumberTokens(tokens, numbers);
	      if(cnt != 4)
		throw new IOException("[" + line + "]" +
				      "line count=" + cnt +
				      " not 4");
	      // add point to all 8 views
	      // x, y, z, value
	      int x = (int) Math.round(numbers[0]);
	      int y = (int) Math.round(numbers[1]);
	      int z = (int) Math.round(numbers[2]);
	      if(x < 0 || x > 127 || y < 0 || y > 127 || z < 0 || z > 59) {
		throw new IOException("[" + line + "]" +
				      "invalid indice x=" + x + " y=" + y + " z=" + z + "\n");
	      }
	      float value = (float) numbers[3];
	      
	      int[] index = new int[3];
	      int oneDintIndex;
	      if(x <= 63) {
		// plane 0 right sagittal
		index[2] = 0; index[0] = 127 - y; index[1] = z + 31;
		oneDintIndex = orthoIndicesDims.getIndex(index);
		if(orthoIndicesArray[oneDintIndex] == 0 ||
		   x < orthoIndicesArray[oneDintIndex]) {
		  orthoIndicesArray[oneDintIndex] = x;
		  orthoViewsArray[orthoViewsDims.getIndex(index)] = value;
		}
	      }
	      else {
		// plane 1 left sagitall
		index[2] = 1; index[0] = y; index[1] = z + 31;
		oneDintIndex = orthoIndicesDims.getIndex(index);
		if(x > orthoIndicesArray[oneDintIndex]) {
		  orthoIndicesArray[oneDintIndex] = x;
		  orthoViewsArray[orthoViewsDims.getIndex(index)] = value;
		}
	      }
	      // plane 2 transverse top view
	      index[2] = 2; index[0] = 127 - x; index[1] = y;
	      oneDintIndex = orthoIndicesDims.getIndex(index);
	      if(orthoIndicesArray[oneDintIndex] == 0 ||
		 z < orthoIndicesArray[oneDintIndex]) {
		orthoIndicesArray[oneDintIndex] = z;
		orthoViewsArray[orthoViewsDims.getIndex(index)] = value;
	      }
	      // plane 3 transverse bottom view
	      index[2] = 3; index[0] = x; index[1] = y;
	      oneDintIndex = orthoIndicesDims.getIndex(index);
	      if(z > orthoIndicesArray[oneDintIndex]) {
		orthoIndicesArray[oneDintIndex] = z;
		orthoViewsArray[orthoViewsDims.getIndex(index)] = value;
	      }
	      // plane 4 coronal front
	      index[2] = 4; index[0] = x; index[1] = z + 31;
	      oneDintIndex = orthoIndicesDims.getIndex(index);
	      if(orthoIndicesArray[oneDintIndex] == 0 ||
		 y < orthoIndicesArray[oneDintIndex]) {
		orthoIndicesArray[oneDintIndex] = y;
		orthoViewsArray[orthoViewsDims.getIndex(index)] = value;
	      }
	      // plane 5 coronal rear
	      index[2] = 5; index[0] = 127 - x; index[1] = z + 31;
	      oneDintIndex = orthoIndicesDims.getIndex(index);
	      if(y > orthoIndicesArray[oneDintIndex]) {
		orthoIndicesArray[oneDintIndex] = y;
		orthoViewsArray[orthoViewsDims.getIndex(index)] = value;
	      }
	      if(x <= 63) {
		// plane 6 sagittal inner right
		index[2] = 6; index[0] = y; index[1] = z + 31;
		oneDintIndex = orthoIndicesDims.getIndex(index);
		if(x > orthoIndicesArray[oneDintIndex]) {
		  orthoIndicesArray[oneDintIndex] = x;
		  orthoViewsArray[orthoViewsDims.getIndex(index)] = value;
		}
	      }
	      else {
		// plane 7 sagittal inner left
		index[2] = 7; index[0] = 127 - y; index[1] = z + 31;
		oneDintIndex = orthoIndicesDims.getIndex(index);
		if(orthoIndicesArray[oneDintIndex] == 0 ||
		   x < orthoIndicesArray[oneDintIndex]) {
		  orthoIndicesArray[oneDintIndex] = x;
		  orthoViewsArray[orthoViewsDims.getIndex(index)] = value;
		}
	      }
	      break;
	    } // end switch (c)
	  }  catch (IllegalArgumentException iae) {
	    iae.printStackTrace();
	    throw new IOException("IllagelArgumentException occurred");
	  }
	} // end while(c != StreamTokenizer.TT_EOF)
      }
    }
  }
  /**
   * Gets the indices corresponding to the raw data
   * before conversion to surface.
   *
   * @param in_x x surface data location
   * @param in_y y surface data location
   * @param in_z z surface data location
   * @return	the indices to the data
   */
  public int[] get3DLocation(int in_x, int in_y, int in_z) {
    // clamp dimensions
    if(in_x < 0) in_x = 0; else if(in_x > 127) in_x = 127;
    if(in_y < 0) in_y = 0; else if(in_y > 127) in_y = 127;
    if(in_z < 0) in_z = 0; else if(in_z > 7) in_z = 7;

    int[] index = new int[3];
    index[0] = in_x; index[1] = in_y; index[2] = in_z;
    int[] orthoIndicesArray = (int[]) orthoIndices.getDataArray();
    int oneDintIndex = orthoIndicesDims.getIndex(index);

    int x, y, z;
    switch (index[2]) {
    default:
      return null;
    case 0:
      y = 127 - index[0]; z = index[1] - 31; x = orthoIndicesArray[oneDintIndex];
      break;
    case 1:
      y = index[0]; z = index[1] - 31; x = orthoIndicesArray[oneDintIndex];
      break;
    case 2:
      x = 127 - index[0]; y = index[1]; z = orthoIndicesArray[oneDintIndex];
      break;
    case 3:
      x = index[0]; y = index[1]; z = orthoIndicesArray[oneDintIndex];
      break;
    case 4:
      x = index[0]; z = index[1] - 31; y = orthoIndicesArray[oneDintIndex];
      break;
    case 5:
      x = 127 - index[0]; z = index[1] - 31; y = orthoIndicesArray[oneDintIndex];
      break;
    case 6:
      y = index[0]; z = index[1] - 31; x = orthoIndicesArray[oneDintIndex];
      break;
    case 7:      
      y = 127 - index[0]; z = index[1] - 31; x = orthoIndicesArray[oneDintIndex];
      break;
    }
    return new int[] {x, y, z};
  }
  /**
   * Gets a location corresponding the surface
   * from a raw 3D-SSP location.
   *
   * @param     in_x x location index in raw data before surface transpose space
   * @param     in_y y location index
   * @param     in_z z location index
   * @param     slice slice corresponding to surface to find nearest index on
   * @return	the indices to the data
   */
  public int[] getSurfaceLocation(int in_x, int in_y, int in_z, int slice) {
    // clamp dimensions
    if(in_x < 0) in_x = 0; else if(in_x > 127) in_x = 127;
    if(in_y < 0) in_y = 0; else if(in_y > 127) in_y = 127;
    if(in_z < 0) in_z = 0; else if(in_z > 59) in_z = 59;
    if(slice < 0) slice = 0; else if(slice > 7) slice = 7;
    // point could map to spot on any of 8 surfaces
    // try all eight picking closest along ortho index direction
    int[] orthoIndicesArray = (int[]) orthoIndices.getDataArray();

    int[] index = new int[3];
    index[2] = slice;
    switch (slice) {
    case 0:
	index[0] = 127 - in_y; index[1] = in_z + 31;
	if(orthoIndicesArray[ orthoIndicesDims.getIndex(index)] != in_x) index[2] = in_x;
	break;
    case 1:
	index[0] = in_y; index[1] = in_z + 31;
	if(orthoIndicesArray[ orthoIndicesDims.getIndex(index)] != in_x) index[2] = in_x;
	break;
    case 2:
	index[0] = 127 - in_x; index[1] = in_y;
	if(orthoIndicesArray[ orthoIndicesDims.getIndex(index)] != in_z) index[2] = in_z;
	break;
    case 3:
	index[0] = in_x; index[1] = in_y;
	if(orthoIndicesArray[ orthoIndicesDims.getIndex(index)] != in_z) index[2] = in_z;
	break;
    case 4:
	index[0] = in_x; index[1] = in_z + 31;
	if(orthoIndicesArray[ orthoIndicesDims.getIndex(index)] != in_y) index[2] = in_y;
	break;
    case 5:
	index[0] = 127 - in_x; index[1] = in_z + 31;
	if(orthoIndicesArray[ orthoIndicesDims.getIndex(index)] != in_y) index[2] = in_y;
	break;
    case 6:
	index[0] = in_y; index[1] = in_z + 31;
	if(orthoIndicesArray[ orthoIndicesDims.getIndex(index)] != in_x) index[2] = in_x;
	break;
    case 7:
	index[0] = 127 - in_y; index[1] = in_z + 31;
	if(orthoIndicesArray[ orthoIndicesDims.getIndex(index)] != in_x) index[2] = in_x;
	break;
    }
    return index;
    /*
    int[] saveIndex = new int[3];
    saveIndex[0] = 127 - in_y; saveIndex[1] = in_z + 31; saveIndex[2] = 0;
    int savediff = Math.abs(in_x - orthoIndicesArray[orthoIndicesDims.getIndex(saveIndex)]);

    index[0] = in_y; index[1] = in_z + 31; index[2] = 1;
    int newdiff = Math.abs(in_x - orthoIndicesArray[orthoIndicesDims.getIndex(index)]);
    if(newdiff < savediff) {
      savediff = newdiff;
      saveIndex[0] = index[0]; saveIndex[1] = index[1]; saveIndex[2] = index[2];
    }

    index[0] = 127 - in_x; index[1] = in_y; index[2] = 2;
    newdiff = Math.abs(in_z - orthoIndicesArray[orthoIndicesDims.getIndex(index)]);
    if(newdiff < savediff) {
      savediff = newdiff;
      saveIndex[0] = index[0]; saveIndex[1] = index[1]; saveIndex[2] = index[2];
    }

    index[0] = in_x; index[1] = in_y; index[2] = 3;
    newdiff = Math.abs(in_z - orthoIndicesArray[orthoIndicesDims.getIndex(index)]);
    if(newdiff < savediff) {
      savediff = newdiff;
      saveIndex[0] = index[0]; saveIndex[1] = index[1]; saveIndex[2] = index[2];
    }

    index[0] = in_x; index[1] = in_z + 31; index[2] = 4;
    newdiff = Math.abs(in_y - orthoIndicesArray[orthoIndicesDims.getIndex(index)]);
    if(newdiff < savediff) {
      savediff = newdiff;
      saveIndex[0] = index[0]; saveIndex[1] = index[1]; saveIndex[2] = index[2];
    }

    index[0] = 127 - in_x; index[1] = in_z + 31; index[2] = 5;
    newdiff = Math.abs(in_y - orthoIndicesArray[orthoIndicesDims.getIndex(index)]);
    if(newdiff < savediff) {
      savediff = newdiff;
      saveIndex[0] = index[0]; saveIndex[1] = index[1]; saveIndex[2] = index[2];
    }

    index[0] = in_y; index[1] = in_z + 31; index[2] = 6;
    newdiff = Math.abs(in_x - orthoIndicesArray[orthoIndicesDims.getIndex(index)]);
    if(newdiff < savediff) {
      savediff = newdiff;
      saveIndex[0] = index[0]; saveIndex[1] = index[1]; saveIndex[2] = index[2];
    }

    index[0] = 127 - in_y; index[1] = in_z + 31; index[2] = 7;
    newdiff = Math.abs(in_x - orthoIndicesArray[orthoIndicesDims.getIndex(index)]);
    if(newdiff < savediff) {
      saveIndex[0] = index[0]; saveIndex[1] = index[1]; saveIndex[2] = index[2];
    }

    return saveIndex;
    */
  }
  /**
   * Gets the coordinate mapping object that should be the default for this data.
   *
   * @return	coordinate mapping object
   */
  public CoordinateMap getCoordinateMap() {
    return null;
  }
  /**
   * Gets the coordinate mapping that should be the default for this
   * data, view and slice.
   * @param sliceViewMode	the slice orientation to display
   *				<code>CNUDimensions.TRANSVERSE</code>,
   *				<code>CNUDimensions.CORONAL</code>, or
   *				<code>CNUDimensions.SAGITTAL</code>
   * @param slice		the slice number in the range from 0 to
   *				the number of slices for the sliceViewMode
   * @param ivalue		the 4th or i dimension to use for displaying
   *				a slice from 4 dimensional data
   *
   * @return	coordinate mapping object or <code>null</code>
   */
  public CoordinateMap getCoordinateMap(int sliceViewMode, int slice, int ivalue) {
    if(sliceViewMode == CNUDimensions.TRANSVERSE) {
      if(slice < 0) slice = 0; else if(slice > 7) slice = 7;
      synchronized (privateLock) {
	if(sliceCoordinateMaps == null)
	  sliceCoordinateMaps = new SatoshiLinearCoordinateMap[8];
	if(sliceCoordinateMaps[slice] == null)
	  sliceCoordinateMaps[slice] = new SatoshiLinearCoordinateMap(slice);
      }
      return sliceCoordinateMaps[slice];
    }
    return null;
  }
  /**
   * Creates a string representation.
   *
   * @return	string representation
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(super.toString());
    return sb.toString();
  }
  /**
   * Reads and prints an 3D-SSP info as a standalone java program.
   *
   * @param args	array of arguments from the command line
   */
  static public void main(String[] args) throws IOException {
    try {
      ThreeDSSPFile img = new ThreeDSSPFile(args[0]);
      System.out.println(img.toString());
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    System.exit(0);
  }
} // end ThreeDSSPFile class
