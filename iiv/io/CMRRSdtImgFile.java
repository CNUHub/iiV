package iiv.io;
import iiv.data.*;
import java.io.*;
/**
 * Reads the Center for Magnetic Resonance Research Stimulate Sdt
 * formatted image files.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		AnalyzeHeader
 * @since	iiV1.0
 */
public class CMRRSdtImgFile extends CNUImgFile {
  private CNUFile sprFile = null;
  private CNUFile sdtFile = null;
  int numDim = -1;
  int dataType = CNUTypes.UNKNOWN;
  int[] dims = null;
  double[] origin = null;
  double[] fov = null;
  double[] interval = null;
  double[] displayRange = null;
  double[] extent = null;
  int orient = CNUDimensions.TRANSVERSE;
  String fidName = null;
  boolean real2WordScaleSet = false;
  double real2WordScale = 0;
  /*
   * Constructs an instance of CMRRSdtImgFile.
   *
   * @param filename	the file to read from with or without ".img" prefix
   * @exception	IOException	thrown if error reading from file
   */
  public CMRRSdtImgFile(String filename) throws IOException {
    sprFile = new CNUFile(buildSprFileName(filename));
    readSprHeader();
    CNUDimensions cnudims = new CNUDimensions(dims, dataType, 0);
    cnudims.setOrientation(orient);
    if(interval != null) cnudims.setSpatialResolutions(interval);  
    setDimensions(cnudims);
//    setCNUDataConversions(ah.getCNUDataConversions());
    if(real2WordScaleSet) {
      if(real2WordScale != 0.0) setFactor(1.0/real2WordScale);
    }
    setFileName(buildSdtFileName(filename));
  }
  /*
   * Reads the spr header file.
   *
   * @exception IOException thrown if an error occurs reading the header
   */
  private void readSprHeader() throws IOException {
    StreamTokenizer tokenizer = sprFile.getStreamTokenizer();
    tokenizer.whitespaceChars(':', ':');
    tokenizer.wordChars('.', '.');
    tokenizer.wordChars('?', '?');
    tokenizer.wordChars('{', '{');
    tokenizer.wordChars('}', '}');
    tokenizer.wordChars('<', '<');
    tokenizer.wordChars('>', '>');
    tokenizer.wordChars('(', '(');
    tokenizer.wordChars(')', ')');
    tokenizer.wordChars('$', '$');
    tokenizer.wordChars('/', '/');
    tokenizer.wordChars('+', '+');
    tokenizer.wordChars('-', '-');
    tokenizer.wordChars('*', '*');
    tokenizer.wordChars('@', '@');
    tokenizer.wordChars('[', '[');
    tokenizer.wordChars(']', ']');
    tokenizer.wordChars('&', '&');
    tokenizer.wordChars('_', '_');

    int c = tokenizer.nextToken();
    while(c != StreamTokenizer.TT_EOF) {
      if(c == StreamTokenizer.TT_WORD) {
	if("numDim".equalsIgnoreCase(tokenizer.sval)) {
	  c = tokenizer.nextToken();
	  if(c == StreamTokenizer.TT_NUMBER) numDim = (int) tokenizer.nval;
	  else throw new IOException("invalid spr file - bad numDim");
	  if(numDim < 1)
	    throw new IOException("invalid spr file - invalid numDim");
	}
	else if("dim".equalsIgnoreCase(tokenizer.sval)) {
	  if(numDim > 0) {
	    dims = new int[numDim];
	    for(int i = 0; i < numDim; i++) {
	      c = tokenizer.nextToken();
	      if(c == StreamTokenizer.TT_NUMBER) dims[i] = (int) tokenizer.nval;
	      else throw new IOException("invalid spr file - missing dim(s)");
	      if(dims[i] < 1)
		throw new IOException("invalid spr file - invalid dim");
	    }
	  }
	  else throw new IOException("invalid spr file - declared dim before numDims");
	}
	else if("dataType".equalsIgnoreCase(tokenizer.sval)) {
	  c = tokenizer.nextToken();
	  if("BYTE".equalsIgnoreCase(tokenizer.sval))
	    dataType = CNUTypes.UNSIGNED_BYTE;
	  else if("WORD".equalsIgnoreCase(tokenizer.sval))
	    dataType = CNUTypes.UNSIGNED_SHORT;
	  else if("LWORD".equalsIgnoreCase(tokenizer.sval))
	    dataType = CNUTypes.INTEGER;
	  else if("REAL".equalsIgnoreCase(tokenizer.sval))
	    dataType = CNUTypes.FLOAT;
	  else if("COMPLEX".equalsIgnoreCase(tokenizer.sval))
	    throw new IOException("don't know how to handle sdt COMPLEX type");
	  else throw new IOException("invalid spr file - invalid dataType word");
	}
        else if("sdtOrient".equalsIgnoreCase(tokenizer.sval)) {
	  c = tokenizer.nextToken();
	  if("ax".equalsIgnoreCase(tokenizer.sval))
	    orient = CNUDimensions.TRANSVERSE;
	  else if("sag".equalsIgnoreCase(tokenizer.sval))
	    orient = CNUDimensions.SAGITTAL;
	  else if("cor".equalsIgnoreCase(tokenizer.sval))
	    orient = CNUDimensions.CORONAL;
	  else throw new IOException("invalid spr file - invalid orientation");
        }
	else if("origin".equalsIgnoreCase(tokenizer.sval)) {
	  if(numDim > 0) {
	    origin = new double[numDim];
	    for(int i = 0; i < numDim; i++) {
	      c = tokenizer.nextToken();
	      if(c == StreamTokenizer.TT_NUMBER) origin[i] = tokenizer.nval;
	      else throw new IOException("invalid spr file - missing origin(s)");
	    }
	  }
	  else throw new IOException("invalid spr file - declared origin before numDims");
	}
	else if("fov".equalsIgnoreCase(tokenizer.sval)) {
	  if(numDim > 0) {
	    fov = new double[numDim];
	    for(int i = 0; i < numDim; i++) {
	      c = tokenizer.nextToken();
	      if(c == StreamTokenizer.TT_NUMBER) fov[i] = tokenizer.nval;
	      else throw new IOException("invalid spr file - missing fov(s)");
	    }
	  }
	  else throw new IOException("invalid spr file - declared fov before numDims");
	}
	else if("interval".equalsIgnoreCase(tokenizer.sval)) {
	  if(numDim > 0) {
	    interval = new double[numDim];
	    for(int i = 0; i < numDim; i++) {
	      c = tokenizer.nextToken();
	      if(c == StreamTokenizer.TT_NUMBER) interval[i] = tokenizer.nval;
	      else throw new IOException("invalid spr file - missing interval(s)");
	    }
	  }
	  else throw new IOException("invalid spr file - declared interval before numDims");
	}
	else if("displayRange".equalsIgnoreCase(tokenizer.sval)) {
	  displayRange = new double[2];
	  c = tokenizer.nextToken();
	  if(c == StreamTokenizer.TT_NUMBER) displayRange[0] = tokenizer.nval;
	  else throw new IOException("invalid spr file - dislayRange value missing");
	  c = tokenizer.nextToken();
	  if(c == StreamTokenizer.TT_NUMBER) displayRange[1] = tokenizer.nval;
	  else throw new IOException("invalid spr file - dislayRange value missing");
	}
	else if("fidName".equalsIgnoreCase(tokenizer.sval)) {
	  c = tokenizer.nextToken();
	  if(c == StreamTokenizer.TT_WORD) fidName = tokenizer.sval;
	  else throw new IOException("invalid spr file - invalid fidName");
	}
	else if("extent".equalsIgnoreCase(tokenizer.sval)) {
	  if(numDim > 0) {
	    extent = new double[numDim];
	    for(int i = 0; i < numDim; i++) {
	      c = tokenizer.nextToken();
	      if(c == StreamTokenizer.TT_NUMBER) extent[i] = tokenizer.nval;
	      else throw new IOException("invalid spr file - missing extent(s)");
	    }
	  }
	  else throw new IOException("invalid spr file - declared extent before numDims");
	}
	else if("Real2WordScale".equalsIgnoreCase(tokenizer.sval)) {
	  c = tokenizer.nextToken();
	  if(c == StreamTokenizer.TT_NUMBER) {
	    real2WordScale = tokenizer.nval;
	    real2WordScaleSet = true;
	  }
	  else throw new IOException("invalid spr file - invalid Real2WordScale");
	}
        else System.out.println("CMRRSdtImgFile - unknown spr parameter=" +
				tokenizer.sval);
      }
      else {
	throw new IOException("invalid spr file - nonword parameter");
      }
      // get token for next time through loop
      c = tokenizer.nextToken();
    }
    if(numDim < 1)
      throw new IOException("invalid spr file - missing numDim");
    if(dims == null)
      throw new IOException("invalid spr file - missing dim(s)");
    if(dataType == CNUTypes.UNKNOWN)
      throw new IOException("invalid spr file - missing dataType");
    if(fov == null) {
      if(interval != null) {
	fov = new double[numDim];
	for(int i = 0; i < numDim; i++) fov[i] = interval[i] * dims[i];
      }
    }
    else if(interval == null) {
      if(fov != null) {
	interval = new double[numDim];
	for(int i = 0; i < numDim; i++) interval[i] = fov[i] / dims[i];
      }
    }
  }
  /*
   * Constructs an instance of CMRRSdtImgFile.
   *
   * @param filename	the file to read from with or without ".img" prefix
   * @param ignored	added for compatibility
   * @exception	IOException	thrown if error reading from file
   */
  public CMRRSdtImgFile(String filename, Object ignored)
       throws IOException {
    this(filename);
  }
  /**
   * Creates a string representation.
   *
   * @return	string representation
   */
  public String toString() {
    String str = "numDim: " + numDim + "\n";
    switch(dataType) {
      case CNUTypes.UNSIGNED_BYTE:
      case CNUTypes.BYTE:
	str += "dataType: BYTE\n";
        break;
      case CNUTypes.UNSIGNED_SHORT:
      case CNUTypes.SHORT:
	str += "dataType: WORD\n";
        break;
      case CNUTypes.INTEGER:
	str += "dataType: LWORD\n";
        break;
      case CNUTypes.FLOAT:
	str += "dataType: REAL\n";
        break;
    }
    if(dims != null) {
      str += "dim:";
      for(int i = 0; i < dims.length; i++) str += " " + dims[i];
      str += "\n";
    }
    if(origin != null) {
      str += "origin:";
      for(int i = 0; i < origin.length; i++) str += " " + origin[i];
      str += "\n";
    }
    if(fov != null) {
      str += "fov:";
      for(int i = 0; i < fov.length; i++) str += " " + fov[i];
      str += "\n";
    }
    if(interval != null) {
      str += "interval:";
      for(int i = 0; i < interval.length; i++) str += " " + interval[i];
      str += "\n";
    }
    if(extent != null) {
      str += "extent:";
      for(int i = 0; i < extent.length; i++) str += " " + extent[i];
      str += "\n";
    }
    if(displayRange != null) {
      str += "displayRange:";
      for(int i = 0; i < displayRange.length; i++) str += " " + displayRange[i];
      str += "\n";
    }
    switch(orient) {
      case CNUDimensions.TRANSVERSE:
	str += "sdtOrient: ax\n";
        break;
      case CNUDimensions.SAGITTAL:
	str += "sdtOrient: sag\n";
        break;
      case CNUDimensions.CORONAL:
	str += "sdtOrient: cor\n";
        break;
    }
    if(fidName != null) str += "fidName: " + fidName + "\n";
    if(real2WordScaleSet) str += "Real2WordScale: " + real2WordScale + "\n";
    return super.toString() + "\n" + str;
  }
  /**
   * Builds a header file name (ie filename with .spr extension).
   * Maintains ".gz" extension after ".spr" on compressed file names.
   *
   * @param filename to base header filename on
   * @return filename with .spr extension
   */
  static public String buildSprFileName(String filename) {
    if(filename != null) {
      String extension = "";
      if( filename.endsWith(".gz") ) {
	filename = filename.substring(0, filename.lastIndexOf('.'));
	extension = ".gz";
      }
      if( ! filename.endsWith(".spr") ) {
	if( filename.endsWith(".sdt") || filename.endsWith(".log") )
	  filename = filename.substring(0, filename.lastIndexOf('.'));
        filename = filename + ".spr";
      }
      filename += extension;
    }
    return filename;
  }
  /**
   * Builds a data file name (ie filename with .sdt extension).
   * Maintains ".gz" extension after ".sdt" on compressed file names.
   *
   * @param filename to base header filename on
   * @return filename with .sdt extension
   */
  static public String buildSdtFileName(String filename) {
    if(filename != null) {
      String extension = "";
      if( filename.endsWith(".gz") ) {
	filename = filename.substring(0, filename.lastIndexOf('.'));
	extension = ".gz";
      }
      if( ! filename.endsWith(".sdt") ) {
	if( filename.endsWith(".spr") || filename.endsWith(".log") )
	  filename = filename.substring(0, filename.lastIndexOf('.'));
        filename = filename + ".sdt";
      }
      filename += extension;
    }
    return filename;
  }
  /**
   * Reads and prints an CMRR SDT header as a standalone java program.
   *
   * @param args	array of arguments from the command line
   */
  static public void main(String[] args) throws IOException {
    try {
      if(args.length < 1) {
	System.out.println("Usage: java iiv.io.CMRRSdtImgFile filename");
	System.exit(1);
      }
      CMRRSdtImgFile img = new CMRRSdtImgFile(args[0]);
      System.out.println(img.toString());
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    System.exit(0);
  }
} // end CMRRSdtImgFile class
