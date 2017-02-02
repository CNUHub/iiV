package iiv.display;
import iiv.*;
import iiv.io.*;
import iiv.data.*;
import iiv.script.*;
import iiv.dialog.*;
import java.awt.image.*;
import java.io.*;
/**
 * CNUColorMap extends IndexColorModel to define some standard tools.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		AnalyzeColorMap
 * @see		java.awt.image.IndexColorModel
 * @since	iiV1.0
 */
public class CNUColorModel extends IndexColorModel
  implements CNUFileObject, iiVScriptable {
  public final static String defaultColorMap = "default grey";
  private Object localLock = new Object();
  private CNUColorModel altTransparentVersion = null;
  private CNUFile cnufile = null;
  private boolean saved = false;
  /*
   * Constructs a new instance of CNUColorModel.
   *
   * @param reds	256 byte array of red values.
   * @param greens	256 byte array of green values.
   * @param blues	256 byte array of blue values.
   */
  public CNUColorModel(byte[] reds, byte[] greens, byte[] blues) {
    this(8, 256, reds, greens, blues, -1);
  }
  /*
   * Constructs a new instance of CNUColorModel.
   *
   * @param nbits	number of bits per color index.
   * @param size	number of color indices.
   * @param reds	byte array of red values of length size.
   * @param greens	byte array of green values of length size.
   * @param blues	byte array of blue values of length size.
   */
  public CNUColorModel(int nbits, int size,
		       byte[] reds, byte[] greens, byte[] blues) {
    super(nbits, size, reds, greens, blues);
  }
  /*
   * Constructs a new instance of CNUColorModel.
   *
   * @param reds	256 byte array of red values.
   * @param greens	256 byte array of green values.
   * @param blues	256 byte array of blue values.
   * @param alphas	256 byte array of alpha values.
   */
  public CNUColorModel(byte[] reds, byte[] greens,
		       byte[] blues, byte[] alphas) {
    this(8, 256, reds, greens, blues, alphas);
  }
  /*
   * Constructs a new instance of CNUColorModel.
   *
   * @param nbits	number of bits per color index.
   * @param size	number of color indices.
   * @param reds	byte array of red values of length size.
   * @param greens	byte array of green values of length size.
   * @param blues	byte array of blue values of length size.
   * @param alphas	byte array of alpha values of length size.
   */
  public CNUColorModel(int nbits, int size,
		       byte[] reds, byte[] greens,
		       byte[] blues, byte[] alphas) {
    super(nbits, size, reds, greens, blues, alphas);
  }
  /*
   * Constructs a new instance of CNUColorModel.
   *
   * @param reds	256 byte array of red values.
   * @param greens	256 byte array of green values.
   * @param blues	256 byte array of blue values.
   * @param trans	indice of transparent color.
   */
  public CNUColorModel(byte[] reds, byte[] greens, byte[] blues, int trans) {
    this(8, 256, reds, greens, blues, trans);
  }
  /*
   * Constructs a new instance of CNUColorModel.
   *
   * @param nbits	number of bits per color index.
   * @param size	number of color indices.
   * @param reds	byte array of red values of length size.
   * @param greens	byte array of green values of length size.
   * @param blues	byte array of blue values of length size.
   * @param trans	indice of transparent color.
   */
  public CNUColorModel(int nbits, int size,
		       byte[] reds, byte[] greens, byte[] blues, int trans) {
    super(nbits, size, reds, greens, blues, trans);
  }
  /*
   * Constructs a new instance of CNUColorModel.
   *
   * @param cmap	3 X 256 byte array of color values.
   */
  public CNUColorModel(byte[] cmap) {
    this(8, 256, cmap, 0, false, -1);
  }
  /*
   * Constructs a new instance of CNUColorModel.
   *
   * @param nbits	number of bits per color index.
   * @param size	number of color indices.
   * @param cmap	3 X 256 byte array of color values with no alpha or
   *			4 X 256 byte array of color values with with alpha.
   * @param start	offset in cmap to start of color values.
   * @param hasAlpha	flags whether or not cmap contains alpha values.
   * @param trans	indice of transparent color.
   */
  public CNUColorModel(int nbits, int size, byte[] cmap,
		       int start, boolean hasAlpha, int trans) {
    super(nbits, size, cmap, start, hasAlpha, trans);
  }
  /*
   * Constructs a new instance of CNUColorModel.
   *
   * @param cmap	3 X 256 byte array of color values.
   * @param trans	indice of transparent color.
   */
  public CNUColorModel(byte[] cmap, int trans) {
    this(8, 256, cmap, 0, false, trans);
  }
  /*
   * Constructs a new instance of CNUColorModel.
   *
   * @param icm		IndexColorModel to duplicate.
   * @param trans	indice of transparent color.
   */
  public CNUColorModel(IndexColorModel icm, int trans) {
    this(icm.getPixelSize(), icm.getMapSize(), getColorMapArray(icm),
	 0, true, trans);
  }
  /*
   * Constructs a new instance of CNUColorModel.
   *
   * @param icm		IndexColorModel to duplicate.
   */
  public CNUColorModel(IndexColorModel icm) {
    this(icm, icm.getTransparentPixel());
  }
  /*
   * Constructs a new instance of CNUColorModel reading from a file.
   *
   * @param filename	file to read from.
   * @exception	IOException	thrown if error reading from filename.
   */
  public CNUColorModel(String filename) throws IOException {
    this(ReadColorMapScript(filename));
    setCNUFile(filename);
    setSaved(true);
  }
  /**
   * Determines the equality of this and another object.
   *
   * @param cmp			object to compare to
   * @return			<code>true</code> if this and
   *				obj are equivalent,
   *				<code>false</code> otherwise.
   */
  public boolean equals(Object cmp) {
    boolean equalstatus = false;
    if(cmp instanceof CNUColorModel) {
      if(cnufile == null) {
        if( ((CNUColorModel) cmp).cnufile == null )
	  equalstatus = indexColorModelsEqual(this, (CNUColorModel) cmp);
      }
      else if( cnufile.equals(((CNUColorModel) cmp).cnufile) )
	equalstatus = indexColorModelsEqual(this, (CNUColorModel) cmp);
    }
    return equalstatus;
  }
  /**
   * Compares two IndexColorModels for equality.  Needed because
   * JDK 1.4 seems to fail testing this equality which sometimes
   * causes problems building scripts.  Hope this problem doesn't
   * pop up with IndexColorModels that are not CNUColorModels.
   *
   * @param icm1 first index color model to compare to second
   * @param icm2 second index color model to compare to first
   * @return true if icm1 and icm2 would return all the same values
   * for the same indices.
   */
  public static boolean indexColorModelsEqual(IndexColorModel icm1,
					      IndexColorModel icm2) {
    if(icm1 == null) return false;
    if(icm2 == null) return false;
    if(icm1 == icm2) return true;
    int mapsize = icm1.getMapSize();
    if(icm2.getMapSize() != mapsize) return false;
    for(int index=0; index < mapsize; index++) {
      if(icm1.getRed(index) != icm2.getRed(index)) return false;
      if(icm1.getGreen(index) != icm2.getGreen(index)) return false;
      if(icm1.getBlue(index) != icm2.getBlue(index)) return false;
      if(icm1.getAlpha(index) != icm2.getAlpha(index)) return false;
    }
    return true;
  }
  /**
   * Retrieves a color map array from an IndexColorModel.
   *
   * @param icm		IndexColorModel to get array from
   * @return 		4 X mapsize byte array of color values
   */
  public static byte[] getColorMapArray(IndexColorModel icm) {
    byte[] cmap = new byte[icm.getMapSize() * 4];
    byte[] values = new byte[icm.getMapSize()];
    icm.getReds(values);
    for(int i = 0; i < values.length; i++) cmap[i*4] = values[i];
    icm.getGreens(values);
    for(int i = 0; i < values.length; i++) cmap[(i*4) + 1] = values[i];
    icm.getBlues(values);
    for(int i = 0; i < values.length; i++) cmap[(i*4) + 2] = values[i];
    icm.getAlphas(values);
    for(int i = 0; i < values.length; i++) cmap[(i*4) + 3] = values[i];
    return cmap;
  }
  /**
   * Retrieves the reds from an IndexColorModel.
   * @param icm		IndexColorModel to get array from
   * @return 		mapsize byte array of red values
   */
  public static byte[] getReds(IndexColorModel icm) {
    byte[] values = new byte[icm.getMapSize()];
    icm.getReds(values);
    return values;
  }
  /**
   * Retrieves the greens from an IndexColorModel.
   * @param icm		IndexColorModel to get array from
   * @return 		mapsize byte array of green values
   */
  public static byte[] getGreens(IndexColorModel icm) {
    byte[] values = new byte[icm.getMapSize()];
    icm.getGreens(values);
    return values;
  }
  /**
   * Retrieves the blues from an IndexColorModel.
   * @param icm		IndexColorModel to get array from
   * @return 		mapsize byte array of blue values
   */
  public static byte[] getBlues(IndexColorModel icm) {
    byte[] values = new byte[icm.getMapSize()];
    icm.getBlues(values);
    return values;
  }
  /**
   * Retrieves the alphas from an IndexColorModel.
   * @param icm		IndexColorModel to get array from
   * @return 		mapsize byte array of alpha values
   */
  public static byte[] getAlphas(IndexColorModel icm) {
    byte[] values = new byte[icm.getMapSize()];
    icm.getAlphas(values);
    return values;
  }
  /**
   * Determines if alphas are truely needed to represent this combination
   * of alphas and transparent color.
   *
   * @param alphas	byte array of alpha values
   * @param trans	indice to transparent color
   * @return		whether alphas are needed
   */
  public static boolean alphasNeeded(byte[] alphas, int trans) {
    boolean alphasNeeded = false;
    for(int i=0; i<alphas.length && (! alphasNeeded); i++) {
      if(CNUTypes.UnsignedByteToInt(alphas[i]) != 255) {
	if(alphas[i] != 0) alphasNeeded = true;
	else if(i != trans) alphasNeeded = true;
      }
    }
    return alphasNeeded;
  }
  /**
   * Determines if alphas are truely needed by this index color model.
   *
   * @param icm		IndexColorModel to test
   * @return		<code>true</code> if alphas are needed
   */ 
  public static boolean alphasNeeded(IndexColorModel icm) {
    int mapSize = icm.getMapSize();
    int trans = icm.getTransparentPixel();
    byte[] alphas = new byte[mapSize];
    icm.getAlphas(alphas);
    return alphasNeeded(alphas, trans);
  }

  /**
   * Reads the color model from a lookup file.
   *
   * @param lookupname	name of file that contains a lookup model
   * @param cnuv	CNUViewer object to search for already read files
   * @return		color model read from file
   */
  public final static ColorModel readLookupFile(String lookupname,
						CNUViewer cnuv) {
    // set the colormap
    ColorModel cm = null;
    if(lookupname != null) lookupname = lookupname.trim();
    if("".equals(lookupname)) lookupname = null;
    if(lookupname == null || defaultColorMap.equals(lookupname))
       cm = getGreyColorModel();
    else {
      // Check if any displayed components are associated with this file
      if((cm == null) && (cnuv != null)) {
	Object obj = cnuv.getFileObject(lookupname);
	if(obj instanceof ColorModel) cm = (ColorModel) obj;
      }
      if(cm != null) { 
	// in memory but we may need to remove transparent color
	if(cm instanceof AnalyzeColorMap) cm =
	  getTransparentColorModel((IndexColorModel)cm, -1 );
      }
      else {
	CNUFile filetmp = new CNUFile(lookupname);
	if( filetmp.isDirectory() ) return null;
	// not in memory so read the colormap file
	try {
	  cm = (ColorModel) ReadColorMapScript(filetmp);
	} catch (Exception e1) {
	  try {
	    cm = (ColorModel) new AnalyzeColorMap(lookupname);
	  } catch (Exception e2) {
	    cm = null;
	    if(cnuv != null) {
	      cnuv.showStatus("Error reading color map file " + lookupname);
	      cnuv.showStatus((Throwable) e1);
	      cnuv.showStatus((Throwable) e2);
	    }
	  }
	}
      }
    } // end if(lookupname != null ...
    return cm;
  }


  /**
   * Reads a color map from a script.
   *
   * @param filename		file to read from
   * @return			the color model
   * @exception	IOException	thrown on error reading from filename
   */
  public static CNUColorModel ReadColorMapScript(String filename)
	throws IOException {
    return ReadColorMapScript(new CNUFile(filename));
  }
  /**
   * Reads a color map from a script.
   *
   * @param cnufile		CNUFile to read from
   * @return			the color model
   * @exception	IOException	thrown if error reading from cnufile
   */
  public static CNUColorModel ReadColorMapScript(CNUFile cnufile)
	throws IOException {
    if(cnufile == null) throw new IOException("Missing cnufile");
    Reader rd = null;
    try {
      Object obj = null;
      rd = cnufile.getReader();
      if(CNUDisplayScript.isCNUDisplayScript(rd) ) {
	rd.close();
	rd = cnufile.getReader(); // start with a new reader at beginning of file
	obj =
	  CNUDisplayScript.readComponentsFromScript(rd, null, null, null, null);
      } else {
	rd.close();
	rd = cnufile.getReader(); // start with a new reader at beginning of file
	if(iiVBshScript.isiiVBshScript(rd) ) {
	  rd.close();
	  rd = cnufile.getReader(); // start with a new reader at beginning of file
	  obj = iiVBshScript.readComponentsFromScript(rd, null, null, null, null);
	}
	else throw new IOException("File does not contain an iiV script");
      }
      if(! (obj instanceof CNUColorModel))
        throw new IOException("iiV script did not contain a CNUColorModel");
      ((CNUColorModel) obj).setCNUFile(cnufile);
      ((CNUColorModel) obj).setSaved(true);
      return (CNUColorModel) obj;
    } finally {
      if(rd != null) rd.close();
    }
  }
  /**
   * Writes a color map to a script file.
   *
   * @param cnufile		CNUFile to write to.
   * @param cd		Determines how to handle write overs if file
   *			exists.
   *			If cd is <code>null</code>, the file is written
   *			over with no warning,
   *			else if cd <code>instanceof ContinueDialog</code>, the
   *			dialog is used to query the user before writting over,
   *			else no write over is performed.
   * @param icm		IndexColorModel to write to file.
   * @exception	IOException	thrown if error writing to cnufile.
   */
  public static void WriteColorMapScript(CNUFile cnufile, Object cd,
				         IndexColorModel icm)
	throws IOException {
    if(cnufile == null) throw new IOException("Missing cnufile");
    if(icm == null) throw new IOException("null color model");
    if( cnufile.exists() ) {
      if(cd == null) throw
	new IOException("WriteColorMap - attempt to write over existing file "
	+ cnufile);
      if(cd instanceof ContinueDialog) {
        ((ContinueDialog)cd).beep();
        ((ContinueDialog)cd).setVisible(true);
        if( ! ((ContinueDialog)cd).getContinueFlag() ) throw
	  new IOException("WriteColorMap - did not write file " + cnufile);
      }
    }
    Writer wr = null;
    try {
      wr = cnufile.getWriter();
      wr.write(iiVBshScript.getHeaderScript());
      wr.write(CNUColorModel.toScript(icm, null));
    } finally {
      if(wr != null) wr.close();
    }
  }
  /**
   * Gets whether the colormodel is saved or not.
   *
   * @return	<code>true</code> if the colormodel is saved
   */
  public boolean getSaved() { 
    synchronized (localLock) { return saved; }
  }
  /**
   * Sets the colormodel saved status.
   *
   * @param saved	<code>true</code> if the colormodel is saved
   */
  public void setSaved(boolean saved) {
    synchronized (localLock) { this.saved = saved; }
  }
  /**
   * Sets the CNUFile for this color model base on a string.
   *
   * @param filename	file name
   */
  public void setCNUFile(String filename) {
    if(filename != null) {
      filename = filename.trim();
      if("".equals(filename)) filename = null;
    }
    if(filename == null) setCNUFile((CNUFile) null);
    else setCNUFile(new CNUFile(filename));
  }
  /**
   * Sets the CNUFile for this color model base on a CNUFile.
   *
   * @param cnufile	the CNUFile
   */
  public void setCNUFile(CNUFile cnufile) {
    synchronized (localLock) {
      this.cnufile = cnufile;
    }
  }
  /**
   * Returns the CNUFile for this color model.
   *
   * @return	the CNUfile
   */
  public CNUFile getCNUFile() {
    synchronized (localLock) {
      return cnufile;
    }
  }
  /**
   * Returns the full name for this color model.
   *
   * @return	the full file name
   */
  public String getFullName() {
    synchronized (localLock) {
      if(cnufile == null) return null;
      return cnufile.toString();
    }
  }
  /**
   * Returns the name for this color model.
   *
   * @return	the base file name
   */
  public String getName() {
    synchronized (localLock) {
      if(cnufile == null) return null;
      return cnufile.getName();
    }
  }
  /**
   * Checks if an object is associated with the same file.
   *
   * @param fileObject	object that may reference the same file
   * @return		<code>true</code> if object assiated with
   *			the same file, otherwise <code>false</code>
   */
  public boolean sameFile(Object fileObject) {
    CNUFile lcnufile = getCNUFile();
    if(lcnufile == null) return false;
    else if(fileObject instanceof CNUColorModel)
      return ((CNUColorModel) fileObject).sameFile( lcnufile );
    else return lcnufile.sameFile(fileObject);
  }
  /**
   * Gets this object if it represents the same file.
   *
   * @param sameFileAsObj object that may reference the same file
   * @return 	this object if it represents the same file,
   *		otherwise <code>null</code>
   */
  public Object getFileObject(Object sameFileAsObj) {
    if(sameFile(sameFileAsObj)) return this;
    else return null;
  }
  /**
   * Gets a copy of the color map with a given transparent color.
   *
   * @param icm		IndexColorModel to get transparent version of
   * @param trans	indice of transparent color
   * @return		IndexColorModel with given transparent color
   */
  public final static IndexColorModel getTransparentColorModel(
    IndexColorModel icm, int trans) {
    if(icm == null) return null;
    if(icm instanceof CNUColorModel)
      return ((CNUColorModel)icm).getTransparentColorModel(trans);
    return createTransparentCopy(icm, trans);
  }
  /**
   * Creates a copy of a given color map with a given transparent color.
   *
   * @param icm		IndexColorModel to create transparent copy of
   * @param trans	indice that needs to be transparent
   * @return		IndexColorModel with given transparent color
   */
  public final static IndexColorModel createTransparentCopy(IndexColorModel icm,
							    int trans) {
    if(icm != null) {
      int oldtrans = getTransparentPixel(icm);
      if((oldtrans == trans) || ((trans < 0) && (oldtrans < 0))); // nothing to do
      else {
	byte[] alphas = new byte[icm.getMapSize()];
	icm.getAlphas(alphas);
	boolean changed = false;
	if((oldtrans >= 0) && (oldtrans < alphas.length)) {
	  if(CNUTypes.UnsignedByteToInt(alphas[oldtrans]) != 255) {
	    alphas[oldtrans] = (byte) 255; changed = true;
	  }
	}
	if((trans >= 0) && (trans < alphas.length)) {
	  if(alphas[trans] != 0) {
	    alphas[trans] = 0; changed = true;
	  }
	}
	if(changed) {
	  byte[] reds = new byte[icm.getMapSize()]; icm.getReds(reds);
	  byte[] greens = new byte[icm.getMapSize()]; icm.getGreens(greens);
	  byte[] blues = new byte[icm.getMapSize()]; icm.getBlues(blues);
	  if(CNUColorModel.alphasNeeded(alphas, trans))
	    icm = new CNUColorModel(reds, greens, blues, alphas);
	  else icm = new CNUColorModel(reds, greens, blues, trans);
	}
      }
    }
    return icm;
  }
  /**
   * Wraps around IndexColorModel getTransparentPixel to fix invalid value.
   *
   * @param icm	IndexColorModel to get transparent pixel from
   * @return	transparent pixel indice or <code>-1</code> if not set
   */
  public static int getTransparentPixel(IndexColorModel icm) {
    int trans = icm.getTransparentPixel();
    // IndexColorModel sometimes returns 0 even though not transparent
    if(trans > -1) { if(icm.getAlpha(trans) != 0) trans = -1; }
    return trans;
  }
  /**
   * Gets a copy of this color map with a given transparent color.
   *
   * @param trans	indice to be transparent
   * @return		CNUColorModel with given transparent color
   */
  public CNUColorModel getTransparentColorModel( int trans ) {
    if(trans == getTransparentPixel(this)) return this;
    synchronized (localLock) {
      if(altTransparentVersion != null) {
        if(trans == getTransparentPixel(altTransparentVersion))
	  return altTransparentVersion;
      }
      CNUColorModel newcm = (CNUColorModel) createTransparentCopy(this, trans);
      if(newcm != this) {
	newcm.setAlternateTransparentVersion(this);
        altTransparentVersion = newcm;
      }
      return newcm;
    }
  }
  /**
   * Sets the alternate transparent version.
   *
   * @param cnucm	CNUColorModel with an different transparent indice
   */
  public void setAlternateTransparentVersion(CNUColorModel cnucm) {
    synchronized (localLock) {
      altTransparentVersion = cnucm;
    }    
  }
  /**
   * Gets the grey color model.
   *
   * @return grey color model
   */
  public static GreyColorModel getGreyColorModel() {
    return GreyColorModel.getGreyColorModel();
  }
  /**
   * Creates a script to recreate this object.
   *
   * @param scriptedObjects scripted objects list to add this object to.
   * @return	script that can recreate object
   */
  public String toScript(CNUScriptObjects scriptedObjects) {
    String classname = getClass().getName();
    StringBuffer sb = new StringBuffer("// -- start ").append(classname).append(" script\n");
    if(scriptedObjects == null) scriptedObjects = new CNUScriptObjects();
    String variableName = scriptedObjects.get(this);
    int trans = -1;
    if(variableName == null) {
      String filename = CNUFile.quoteSlashes(getFullName());
      if(filename == null)
	sb.append(toScript((IndexColorModel) this, scriptedObjects));
      else if(filename.equals(defaultColorMap)) {
	variableName = scriptedObjects.addObject(this, "greycolormodel");
	sb.append(variableName).append(" = ");
	sb.append(classname).append(".getGreyColorModel();\n");
	sb.append("script_rtn=").append(variableName).append(";\n");
	trans = getTransparentPixel();
      }
      else if(! getSaved()) {
	sb.append(toScript((IndexColorModel) this, scriptedObjects));
      }
      else {
	variableName = scriptedObjects.addObject(this, "cnucolormodelfile");
	sb.append(variableName).append(" = ");
	sb.append("newFileObject(\"").append(filename).append("\", ");
	sb.append(getClass().getName()).append(", \"").append(filename).append("\");\n");
	sb.append("script_rtn=").append(variableName).append(";\n");
	trans = getTransparentPixel();
      }
      if( trans > -1 ) {
	sb.append(variableName).append(" = ");
	sb.append(classname).append(".getTransparentColorModel(");
	sb.append(variableName).append(", ");
	sb.append(trans).append(");\n");
	sb.append("script_rtn=").append(variableName).append(";\n");
      }
    } else sb.append("script_rtn=").append(variableName).append(";\n");
    sb.append("// -- end ").append(classname).append(" script\n");
    return sb.toString();
  }
  /**
   * Returns a script for a general IndexColorModel.
   *
   * @param icm			IndexColorModel
   * @param scriptedObjects scripted objects list to add this object to.
   * @return			script that will recreate the color model
   */
  public static String toScript(IndexColorModel icm,
				CNUScriptObjects scriptedObjects) {
    String classname = CNUColorModel.class.getName();
    int mapSize = icm.getMapSize();
    // Wrap IndexColorModels in CNUColorModel because equals seems
    // broke for IndexColorModels in Java 2.
    if(! (icm instanceof CNUColorModel)) icm = new CNUColorModel(icm);
    StringBuffer sb = new StringBuffer(mapSize * 4 * 4 + 100);
    sb.append("// -- start ").append(classname).append(" IndexColorModel script\n");
    if(scriptedObjects == null) scriptedObjects = new CNUScriptObjects();
    String variableName = scriptedObjects.get(icm);
    if(variableName == null) {
      variableName = scriptedObjects.addObject(icm, "indexcolormodel");
      int trans = icm.getTransparentPixel();
      byte[] levels = new byte[mapSize];
      icm.getReds(levels);
      sb.append("{\n"); // makes declared tmp variables local
      sb.append("redstmp = new byte[] {");
      for(int i=0; i<mapSize; i++) {
	if(i > 0) sb.append(',');
	if( (i % 16) == 0) sb.append("\n");
	sb.append(" ");
	sb.append(CNUTypes.UnsignedByteToInt(levels[i]));
      }
      sb.append("};\n");

      icm.getGreens(levels);
      sb.append("byte[] greenstmp = new byte[] {");
      for(int i=0; i<mapSize; i++) {
	if(i > 0) sb.append(',');
	if( (i % 16) == 0) sb.append("\n");
	sb.append(" ");
	sb.append(CNUTypes.UnsignedByteToInt(levels[i]));
      }
      sb.append("};\n");

      icm.getBlues(levels);
      sb.append("byte[] bluestmp = new byte[] {");
      for(int i=0; i<mapSize; i++) {
	if(i > 0) sb.append(',');
	if( (i % 16) == 0) sb.append("\n");
	sb.append(" ");
	sb.append(CNUTypes.UnsignedByteToInt(levels[i]));
      }
      sb.append("};\n");

      icm.getAlphas(levels);
      if(alphasNeeded(levels, trans)) {
//      if((trans >= 0) && (trans < mapSize)) levels[trans] = 0;
	sb.append("byte[] alphastmp = new byte[] {");
	for(int i=0; i<mapSize; i++) {
	  if(i > 0) sb.append(',');
	  if( (i % 16) == 0) sb.append("\n");
	  sb.append(" ");
	  sb.append(CNUTypes.UnsignedByteToInt(levels[i]));
	}
	sb.append("};\n");

	sb.append(variableName).append(" = new ").append(classname).append("(");
	sb.append(icm.getPixelSize()).append(", ").append(mapSize);
	sb.append(", redstmp, greenstmp, bluestmp, alphastmp);\n");
      }
      else {
	sb.append(variableName).append(" = new ").append(classname).append("(");
	sb.append(icm.getPixelSize()).append(", ").append(mapSize);
	sb.append(", redstmp, greenstmp, bluestmp, ");
	sb.append(icm.getTransparentPixel()).append(");\n");
      }
      sb.append("}\n"); // declared tmp variables no longer needed
    }
    sb.append("script_rtn=").append(variableName).append(";\n");
    sb.append("// -- end ").append(classname).append(" IndexColorModel script\n");
    return sb.toString();
  }
}
