package iiv.io;
import iiv.data.*;
import iiv.script.*;
import java.io.*;
import java.net.*;

/**
 * Treats a file as raw image data.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNURawImgFile
 * @see		CNUAnalyzeImgFile
 * @see		CNUEcatImgFile
 * @see		CNUStdImgFile
 */
public class CNUImgFile extends CNUData
  implements CNUFileObject, CNUConversionTypes, iiVScriptable {

  private Object privateLock = new Object();
  private CNUFile cnufile = null;
  private long skipBytes = 0;
  private CNUDataConversions cnuDataConv = new CNUDataConversions();

  /**
   * Constructs an instance of CNUImgFile.
   */
  public CNUImgFile() {
  }
  /**
   * Constructs an instance of CNUImgFile with a given file and dimensions.
   *
   * @param filename	file to read data from
   * @param dims	dimensions of data
   * @exception IOException thrown on errors reading from the file.
   */
  public CNUImgFile(String filename, CNUDimensions dims) throws IOException {
    setDimensions(dims);
    setFileName(filename);
  }
  /**
   * Overrides getDataArray to read data from the file if not alraedy read.
   *
   * @return	array object containing the files data
   */
  public Object getDataArray() {
    synchronized (privateLock) {
      Object dataArray = super.getDataArray();
      if(dataArray != null) return dataArray;
      try {
        readData();
        return(super.getDataArray());
      } catch (IOException ioe) {
        System.out.println(ioe);
      }
    }
    return null;
  }
  /**
   * Sets the data conversions.
   *
   * @param cnudc	data conversions
   */
  public void setCNUDataConversions(CNUDataConversions cnudc) {
    cnuDataConv = new CNUDataConversions(cnudc);
  }
  /**
   * Gets the data conversions.
   *
   * @return	data conversions
   */
  public CNUDataConversions getCNUDataConversions() {
    return new CNUDataConversions(cnuDataConv);
  }
  /**
   * Open a new data input stream with conversions if needed.
   *
   * @return newly open data input stream
   * @exception IOException thrown on errors reading opening the file.
   */
  public DataInput getDataInput() throws IOException {
    return getDataInput(getCNUFile(), getCNUDataConversions());
  }
  /**
   * Reads data from the file.
   *
   * @exception IOException thrown on errors reading from the file.
   */
  public void readData() throws IOException {
    synchronized (privateLock) {
      CNUFile cnufile = getCNUFile();
      if(cnufile != null) {
        InputStream inS = null;
        try {
          inS = cnufile.getInputStream();
          if(inS != null)  {
	    if(cnuDataConv.getConversionsNeeded()) {
	      // replace inS with ConversionDataInputStream
	      inS = new ConvertDataInputStream(inS, cnuDataConv);
	    }
            setDataArray(ReadRawImg(inS, getDimensions(), getSkipBytes()));
          }
        } finally {
          if(inS != null)inS.close();
        }
      }
    }
  }
  /**
   * Sets the file name.
   *
   * @param filename	file name
   */
  public void setFileName(String filename) {
    synchronized (privateLock) {
      cnufile = new CNUFile(filename);
    }
  }
  /**
   * Gets the CNUfile object.
   *
   * @return	the CNUFile object
   */
  public CNUFile getCNUFile() {
    synchronized (privateLock) {
      return cnufile;
    }
  }
  /**
   * Gets the complete file name.
   *
   * @return the complete filename
   */
  public String getFileName() {
    synchronized (privateLock) {
      if(cnufile != null) return cnufile.toString();
      else return "";
    }
  }
  /**
   * Gets the base file name excluding directory path.
   *
   * @return the base file name
   */
  public String getName() {
    synchronized (privateLock) {
      if(cnufile != null) return cnufile.getName();
      else return null;
    }
  }
  /**
   * Sets the number of bytes to skip at the beginning of the file.
   *
   * @param skipBytes	number of bytes to skip
   */
  public void setSkipBytes(long skipBytes) { this.skipBytes = skipBytes; }
  /**
   * Gets the number of skip bytes.
   *
   * @return the number of skip bytes
   */
  public long getSkipBytes() { return skipBytes; }
  /**
   * Creates a string representation of the object
   *
   * @return the string representation
   */
  public String toString() {
    String s = super.toString() + "\n";
    CNUFile cnufile = getCNUFile();
    if( cnufile != null ) s += cnufile.toString() + "\n";
    else s += "null cnufile\n";
    s += "skipBytes=" + getSkipBytes() + "\n";
    s += "CNUDataConversions=\n" + getCNUDataConversions().toString() + "\n";
    return s;
  }
  /**
   * Creates a script that may be used to recreate this display component.
   *
   * @param scriptedObjects scripted objects list to add this object to.
   * @return  the script
   */
  public String toScript(CNUScriptObjects scriptedObjects) {
    String classname = getClass().getName();
    StringBuffer sb = new StringBuffer("// -- start ").append(classname).append(" script\n");
    if(scriptedObjects == null) scriptedObjects = new CNUScriptObjects();
    String variableName = scriptedObjects.get(this);
    if(variableName == null) {
	variableName = scriptedObjects.addObject(this, "cnuimgfile");
	String fileName = CNUFile.quoteSlashes(getCNUFile().toString());
	sb.append(variableName).append(" = newFileObject(\"").append(fileName).append("\", \"");
	sb.append(classname).append("\", \"");
	sb.append(fileName).append("\", CNUVIEWER);\n");
    }
    sb.append("script_rtn=").append(variableName).append(";\n");
    sb.append("// -- end ").append(classname).append(" script\n");
    return sb.toString();
  }
  /**
   * Finds out if an object represents the same file.
   *
   * @return <code>true</code> if the same file, <code>false</code> otherwise
   */
  public boolean sameFile( Object fileObject ) {
    CNUFile localcnufile = getCNUFile();
    if(localcnufile == null) return false;
    else if( fileObject instanceof CNUImgFile )
      return ( (CNUImgFile) fileObject ).sameFile(localcnufile);
    else return localcnufile.sameFile(fileObject);
  }
  /**
   * Gets this object it it represents the same file as another object.
   *
   * @param sameFileAsObj	object that may represent the same file
   * @return	this object if it represents the same file as sameFileAsObj,
   *		<code>null</code> otherwise
   */
  public Object getFileObject(Object sameFileAsObj) {
    if(sameFile( sameFileAsObj )) return this;
    else return null;
  }
  /**
   * Builds an image file name (ie filename with .img extension).
   * Maintains ".gz" extension after ".img" on compressed file names.
   *
   * @param filename to base image filename on
   * @return filename with .img extension
   */
  static public String buildImgFileName(String filename) {
    if(filename != null) {
      String extension = "";
      if( filename.endsWith(".gz") ) {
	filename =
	  filename.substring(0, filename.lastIndexOf('.'));
	extension = ".gz";
      }
      if( ! filename.endsWith(".img") ) {
	if( filename.endsWith(".hdr") || filename.endsWith(".log") )
	  filename = filename.substring(0, filename.lastIndexOf('.'));
        filename = filename + ".img";
      }
      filename += extension;
    }
    return filename;
  }
  /**
   * Builds a header file name (ie filename with .hdr extension).
   * Maintains ".gz" extension after ".hdr" on compressed file names.
   *
   * @param filename to base header filename on
   * @return filename with .hdr extension
   */
  static public String buildHdrFileName(String filename) {
    if(filename != null) {
      String extension = "";
      if( filename.endsWith(".gz") ) {
	filename = filename.substring(0, filename.lastIndexOf('.'));
	extension = ".gz";
      }
      if( ! filename.endsWith(".hdr") ) {
	if( filename.endsWith(".img") || filename.endsWith(".log") )
	  filename = filename.substring(0, filename.lastIndexOf('.'));
        filename = filename + ".hdr";
      }
      filename += extension;
    }
    return filename;
  }
  /**
   * Open a new data input stream with conversions if needed.
   *
   * @param  cnufile	file to read from
   * @param  cdc        data conversions to use
   * @return newly open data input stream
   * @exception IOException thrown on errors reading opening the file.
   */
  static public DataInput getDataInput(CNUFile cnufile, CNUDataConversions cdc) throws IOException {
    DataInput dataInput = null;
    if(cnufile != null) {
      InputStream inputStream = cnufile.getInputStream();
      // replace dataInputS with ConversionDataInputStream if needed
      if(cdc != null && cdc.getConversionsNeeded())
	  dataInput = new ConvertDataInputStream(inputStream, cdc);
      else if(inputStream instanceof DataInput) dataInput = (DataInput) inputStream;
      else dataInput = new DataInputStream(inputStream);
    }
    return dataInput;
  }
  /**
   * Reads raw data from a file.
   *
   * @param cnufile	file to read from
   * @param dims	dimensions of data in the file
   * @param skipBytes	number of bytes to skip at beginning of file
   * @return	array object containing data read
   * @exception IOException thrown on errors reading from the file.
   */
  static public Object ReadRawImg(CNUFile cnufile, CNUDimensions dims,
				  long skipBytes)
    throws IOException {
    InputStream inS = null;
    try {
      inS = cnufile.getInputStream();
      if(inS != null) return ReadRawImg(inS, dims, skipBytes);
    } finally {
      if(inS != null)inS.close();
    }
    return null;
  }
  /**
   * Reads raw data from an input stream.
   *
   * @param inS	Input stream to read from
   * @param dims	dimensions of data in the file
   * @param skipBytes	number of bytes to skip at beginning of file
   * @return	array object containing data read
   * @exception IOException thrown on errors reading from the file.
   */
  static public Object ReadRawImg(InputStream inS, CNUDimensions dims,
    long skipBytes) throws IOException {
    DataInput di = null;
    if(skipBytes != 0) inS.skip(skipBytes);
    if(inS instanceof DataInput) di = (DataInput) inS;
    int length=dims.lengthInWords();
    Thread currentThread = Thread.currentThread();
    int firstlooplength = length - 16;
    int nword = 0;

    switch( dims.getType() ) {
    case CNUTypes.BYTE:
    case CNUTypes.UNSIGNED_BYTE:
      byte[] data = new byte[length];
      int bytes_read = 0;
      int nbytes=0;
      while(nbytes != -1 && bytes_read < length && ! currentThread.isInterrupted()){
        nbytes = inS.read(data, bytes_read, length - bytes_read);
        if(nbytes > 0)bytes_read += nbytes;
      }
      if(bytes_read != length)
	throw new IOException("wrong number of bytes read");
      return((Object) data);
    case CNUTypes.SHORT:
    case CNUTypes.UNSIGNED_SHORT:
      if(di == null) di = new DataInputStream(inS);
      short[] sdata = new short[length];
      for( ; nword < firstlooplength && ! currentThread.isInterrupted(); ) {
        sdata[nword++] = di.readShort(); sdata[nword++] = di.readShort();
        sdata[nword++] = di.readShort(); sdata[nword++] = di.readShort();
        sdata[nword++] = di.readShort(); sdata[nword++] = di.readShort();
        sdata[nword++] = di.readShort(); sdata[nword++] = di.readShort();

        sdata[nword++] = di.readShort(); sdata[nword++] = di.readShort();
        sdata[nword++] = di.readShort(); sdata[nword++] = di.readShort();
        sdata[nword++] = di.readShort(); sdata[nword++] = di.readShort();
        sdata[nword++] = di.readShort(); sdata[nword++] = di.readShort();
      }
      if(currentThread.isInterrupted()) throw new IOException("Interrupted");
      for( ; nword < length; ) sdata[nword++] = di.readShort();
      return((Object) sdata);
    case CNUTypes.INTEGER:
      if(di == null) di = new DataInputStream(inS);
      int[] idata = new int[length];
      for( ; nword < firstlooplength && ! currentThread.isInterrupted(); ) {
        idata[nword++] = di.readInt(); idata[nword++] = di.readInt();
        idata[nword++] = di.readInt(); idata[nword++] = di.readInt();
        idata[nword++] = di.readInt(); idata[nword++] = di.readInt();
        idata[nword++] = di.readInt(); idata[nword++] = di.readInt();

        idata[nword++] = di.readInt(); idata[nword++] = di.readInt();
        idata[nword++] = di.readInt(); idata[nword++] = di.readInt();
        idata[nword++] = di.readInt(); idata[nword++] = di.readInt();
        idata[nword++] = di.readInt(); idata[nword++] = di.readInt();
      }
      if(currentThread.isInterrupted()) throw new IOException("Interrupted");
      for( ; nword < length; ) idata[nword++] = di.readInt();
      return((Object) idata);
    case CNUTypes.LONG:
      if(di == null) di = new DataInputStream(inS);
      long[] ldata = new long[length];
      for( ; nword < firstlooplength && ! currentThread.isInterrupted(); ) {
        ldata[nword++] = di.readLong(); ldata[nword++] = di.readLong();
        ldata[nword++] = di.readLong(); ldata[nword++] = di.readLong();
        ldata[nword++] = di.readLong(); ldata[nword++] = di.readLong();
        ldata[nword++] = di.readLong(); ldata[nword++] = di.readLong();

        ldata[nword++] = di.readLong(); ldata[nword++] = di.readLong();
        ldata[nword++] = di.readLong(); ldata[nword++] = di.readLong();
        ldata[nword++] = di.readLong(); ldata[nword++] = di.readLong();
        ldata[nword++] = di.readLong(); ldata[nword++] = di.readLong();
      }
      if(currentThread.isInterrupted()) throw new IOException("Interrupted");
      for( ; nword < length; ) ldata[nword++] = di.readLong();
      return((Object) ldata);
    case CNUTypes.FLOAT:
      if(di == null) di = new DataInputStream(inS);
      float[] fdata = new float[length];
      for( ; nword < firstlooplength && ! currentThread.isInterrupted(); ) {
        fdata[nword++] = di.readFloat(); fdata[nword++] = di.readFloat();
        fdata[nword++] = di.readFloat(); fdata[nword++] = di.readFloat();
        fdata[nword++] = di.readFloat(); fdata[nword++] = di.readFloat();
        fdata[nword++] = di.readFloat(); fdata[nword++] = di.readFloat();

        fdata[nword++] = di.readFloat(); fdata[nword++] = di.readFloat();
        fdata[nword++] = di.readFloat(); fdata[nword++] = di.readFloat();
        fdata[nword++] = di.readFloat(); fdata[nword++] = di.readFloat();
        fdata[nword++] = di.readFloat(); fdata[nword++] = di.readFloat();
      }
      if(currentThread.isInterrupted()) throw new IOException("Interrupted");
      for( ; nword < length; ) fdata[nword++] = di.readFloat();
      return((Object) fdata);
    case CNUTypes.DOUBLE:
      if(di == null) di = new DataInputStream(inS);
      double[] ddata = new double[length];
      for( ; nword < firstlooplength && ! currentThread.isInterrupted(); ) {
	ddata[nword++] = di.readDouble(); ddata[nword++] = di.readDouble();
	ddata[nword++] = di.readDouble(); ddata[nword++] = di.readDouble();
	ddata[nword++] = di.readDouble(); ddata[nword++] = di.readDouble();
	ddata[nword++] = di.readDouble(); ddata[nword++] = di.readDouble();

	ddata[nword++] = di.readDouble(); ddata[nword++] = di.readDouble();
	ddata[nword++] = di.readDouble(); ddata[nword++] = di.readDouble();
	ddata[nword++] = di.readDouble(); ddata[nword++] = di.readDouble();
	ddata[nword++] = di.readDouble(); ddata[nword++] = di.readDouble();
      }
      if(currentThread.isInterrupted()) throw new IOException("Interrupted");
      for( ; nword < length; ) ddata[nword++] = di.readDouble();
      return((Object) ddata);
    default:
      throw new IOException("Unable to read raw file of type = " +
			    CNUTypes.typeToString(dims.getType()));
    }
  }
}
