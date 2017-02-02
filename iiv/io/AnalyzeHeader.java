package iiv.io;
import iiv.data.*;
import java.io.*;
import java.lang.*;
import java.net.*;

/**
 * Reads and stores an ANALYZE header file information.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUAnalyzeImgFile
 * @since	iiV1.0
 */
public class AnalyzeHeader {
  private Object localLock = new Object();
  private CNUFile cnufile;
  private CNUDataConversions cnuDataConv = new CNUDataConversions();
  private header_key hk = null;
  private image_dimension dime = null;
  private data_history hist = null;
  /**
   * Constructs an new instance of AnalyzeHeader leaving fields empty.
   *
   */
  public AnalyzeHeader() {
  }
  /**
   * Constructs an new instance of AnalyzeHeader reading info from filename.
   *
   * @param filename	the file to read header information from
   * @exception	IOException	thrown if error reading from file
   */
  public AnalyzeHeader(String filename) throws IOException {
    setFile(filename);
  }
  /**
   * Sets the filename adding the appropriate extension ".hdr" if not present.
   *
   * @param filename	file name with or withoud ".hdr" extension
   */
  public void setFile(String filename) throws IOException {
    synchronized (localLock) {
      filename = CNUImgFile.buildHdrFileName(filename);
      cnufile = new CNUFile(filename);
      ReadHeader();
    }
  }
  /**
   * Gets the file name.
   *
   * @return	name of header file with extension
   */
  public String getHdrFileName() {
    String filename = null;
    synchronized (localLock) {
      if(cnufile != null) filename = cnufile.getPath();
    }
    return filename;
  }
  /**
   * Gets the image file name (filename with .img extension).
   *
   * @return	filename with .img extension
   */
  public String getImgFileName() {
    String filename = null;
    synchronized (localLock) {
      if(cnufile != null) filename = cnufile.toString();
    }
    return CNUImgFile.buildImgFileName(filename);
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
   * Reads the header information from the CNUFile setting appropriate fields.
   */
  public void ReadHeader() throws IOException {
    InputStream in = null;
    ConvertDataInputStream inData = null;
    try {
      synchronized (localLock) {
        if( cnufile != null ) {
          in = cnufile.getInputStream();
          inData = new ConvertDataInputStream(in);
          hk = new header_key(inData);
          dime = new image_dimension(inData);
          hist = new data_history(inData);
	  setCNUDataConversions(inData.getCNUDataConversions());
        }
      }
    } finally {
      if(inData != null) inData.close();
      if(in != null) in.close();
    }
  }
  /**
   * Outputs the Analyze dimensions as CNUDimensions.
   *
   * @return	a copy of dimensions for this analyze file
   */
  public CNUDimensions getDimensions() {
    CNUDimensions lsize = new CNUDimensions();
    synchronized (localLock) {
      lsize.set4DValues(dime.dim[1], dime.dim[2],
		        dime.dim[3], dime.dim[4],
		        analyzeToCNUType(dime.datatype), 0);
      lsize.set4DSpatialResolutions(dime.pixdim[1] * 1e-3,
				    dime.pixdim[1] * 1e-3,
				    dime.pixdim[3] * 1e-3,
				    dime.pixdim[4] * 1e-3);
      lsize.setOrientation(analyzeToCNUOrientation(hist.orient));
    }
    return(lsize);
  }
  /**
   * Gets the quantification factor for this analyze header.
   *
   * @return	quantification factor
   */
  public double getFactor() {
    synchronized (localLock) {
      double factor = dime.pixdim[0]; // cnu storage of word resolution
      if(Math.abs(factor) <=  Float.MIN_VALUE) {
        factor = dime.funused1;  // spm storage of word resolution
        if(Math.abs(factor) <=  Float.MIN_VALUE) factor=1.0;  // default
      }
      return factor;
    }
  }
  /**
   * Converts the header information into a readable string form.
   *
   * @return	header information as a string
   */
  public String toString() {
    return("** Header Key =\n" + hk +
	   "** Image Dimension =\n" + dime +
	   "** Data History =\n" + hist);
  }
  /**
   * Converts an ANALYZE header type into CNU type.
   *
   * @return	CNU data type
   */
  public final static int analyzeToCNUType(int analyzeType) {
    switch (analyzeType) {
    case DT_UNSIGNED_CHAR:
      return CNUTypes.UNSIGNED_BYTE;
    case DT_SIGNED_SHORT:
      return CNUTypes.SHORT;
    case DT_SIGNED_INT:
      return CNUTypes.INTEGER;
    case DT_FLOAT:
      return CNUTypes.FLOAT;
    case DT_DOUBLE:
      return CNUTypes.DOUBLE;
    case DT_UNKNOWN:
    case DT_BINARY:
    case DT_COMPLEX:
    case DT_RGB:
    default:
      return CNUTypes.UNKNOWN;
    }
  }
  /**
   * Converts an ANALYZE orientation into CNU orientation.
   *
   * @return	CNU orientation
   */
  public final static int analyzeToCNUOrientation(byte analyzeOrientation) {
    //0=transverse,1=coronal,2=sagittal,
    //3=transverse flipped,4=coronal flipped,5=sagittal flipped
    switch (analyzeOrientation) {
    case 0:
    case 3:
      return CNUDimensions.TRANSVERSE;
    case 1:
    case 4:
      return CNUDimensions.CORONAL;
    case 2:
    case 5:
      return CNUDimensions.SAGITTAL;
    default:
      return CNUDimensions.UNKNOWN;
    }
  }
  /**
   * Reads a character string correcting for null characters.
   *
   * @return	non-null character
   */
  final static char readCharacter( DataInputStream inData )
  throws IOException {
    return CNUTypes.byteToChar(inData.readByte());
  }

  /**
   * Reads and stores the header key portion of an ANALYZE header from
   * a DataInputStream.
   *
   * @author	Joel T. Lee
   * @version %I%, %G%
   * @see		AnalyzeHeader
   * @see		java.io.DataInputStream
   * @since	iiV1.0
   */
  class header_key {
    /* off + size*/
    int sizeof_hdr;			/* 0 + 4     */
    char[] 	data_type=new char[10];	/* 4 + 10    */
    char[] db_name=new char[18];	        /* 14 + 18   */
    int extents;			        /* 32 + 4    */
    short session_error;		        /* 36 + 2    */
    char regular;			        /* 38 + 1    */
    char hkey_un0;			/* 39 + 1    */
					/* total=40  */
    /**
     * Constructs a new instance of header_key.
     *
     * @param inData	DataInputStream to read from
     * @exception	IOException	thrown if error reading from inData
     */
    public header_key(ConvertDataInputStream inData) throws IOException {
      byte[] inBytes = new byte[4];
      if(inData.read(inBytes, 0, 4) != 4)
	throw new IOException("error reading analyze header_key");
      CNUDataConversions cnuDataConversions = inData.getCNUDataConversions();
      sizeof_hdr = cnuDataConversions.bytesToInt(inBytes, 0);
      if(sizeof_hdr != 348) {
	// try swapping bytes 0,1,2,3 -> 3,2,1,0
	cnuDataConversions = CNUDataConversions.getStandardReverseBytesConversions();
	sizeof_hdr = cnuDataConversions.bytesToInt(inBytes, 0);
	if(sizeof_hdr != 348)
	  throw new IOException(
				"invalid header_key - reverse byte sizeof_hdr="
				+ sizeof_hdr + " should be 348");
	inData.setCNUDataConversions(cnuDataConversions);
      }
      for(int i=0; i<10; i++)
	data_type[i] = CNUTypes.byteToChar(inData.readByte());
      for(int i=0; i<18; i++)
      db_name[i] = CNUTypes.byteToChar(inData.readByte());
      extents = inData.readInt();
      session_error = inData.readShort();
      regular = CNUTypes.byteToChar(inData.readByte());
      if(regular != 'r')
	throw new IOException(
	    "invalid header_key value for regular should be 'r'");
      hkey_un0 = CNUTypes.byteToChar(inData.readByte());
    }
    /**
     * Converts the header key information into a readable string form.
     *
     * @return	String form of header_key information
     */
    public String toString() {
      return(
	     "sizeof_hdr=" + sizeof_hdr + "\n" +
	     "data_type=" + data_type + "\n" +
	     "db_name=" + db_name + "\n" +
	     "extents=" + extents + "\n" +
	     "session_error=" + session_error + "\n" +
	     "regular=" + regular + "\n" +
	     "hkey_un0=" + hkey_un0 + "\n"
	     );
    }
  }
  
  final static int DT_UNKNOWN =		0;
  final static int DT_BINARY =		1;
  final static int DT_UNSIGNED_CHAR =	2;
  final static int DT_SIGNED_SHORT =	4;
  final static int DT_SIGNED_INT = 	8;
  final static int DT_FLOAT = 		16;
  final static int DT_COMPLEX =		32;
  final static int DT_DOUBLE = 		64;
  final static int DT_RGB =		128;
  /**
   * Converts an short array into a readable string.
   *
   * @param array	the array to be converted
   * @return	String form of short array information
   */
  public static String ShortArrayToString(short[] array) {
    String S="";
    for(int i = 0; i < array.length; i++)
      S = S + "[" + i + "]=" + array[i] + " ";
    return(S);
  }
  /**
   * Converts an float array into a readable string.
   *
   * @param array	the array to be converted
   * @return	String form of float array information
   */
  public static String FloatArrayToString(float[] array) {
    String S="";
    for(int i = 0; i < array.length; i++)
      S = S + "[" + i + "]=" + array[i] + " ";
    return(S);
  }
  /**
   * Reads and stores the image dimension portion of an ANALYZE header
   * from a DataInputStream.
   *
   * @author	Joel T. Lee
   * @version %I%, %G%
   * @see		AnalyzeHeader
   * @see		java.io.DataInputStream
   * @since	iiV1.0
   */
  class image_dimension {
    
    short[] dim=new short[8];	        /* 0 + 16    */
    char[] vox_units=new char[4];	        /* 16 + 4    */
    char[] cal_units=new char[8];	        /* 20 + 4    */
    short unused1;			/* 24 + 2    */
    short datatype;			/* 30 + 2    */
    short bitpix;			        /* 32 + 2    */
    short dim_un0;			/* 34 + 2    */
    float[] pixdim=new float[8];	        /* 36 + 32   */
    /* 
       pixdim[] specifies the voxel dimensions:
       pixdim[0] - quantification - my own invent jtl
       pixdim[1] - voxel width
       pixdim[2] - voxel height
       pixdim[3] - interslice distance
       ..etc
    */
    float vox_offset;		        /* 68 + 4    */
    float funused1;			/* 72 + 4    */
    float funused2;			/* 76 + 4    */
    float funused3;			/* 80 + 4    */
    float cal_max; 			/* 84 + 4    */
    float cal_min;			/* 88 + 4    */
    int compressed;			/* 92 + 4    */
    int verified;			        /* 96 + 4    */
    int glmax, glmin;
    /**
     * Constructs a new instance of image_dimension.
     *
     * @param inData	DataInput to read from
     * @exception	IOException	thrown if error reading from inData
     */
    public image_dimension(DataInput inData)
      throws IOException {
      for(int i=0; i<8; i++) dim[i] = inData.readShort();
      for(int i=0; i<4; i++)
	vox_units[i] = CNUTypes.byteToChar(inData.readByte());
      for(int i=0; i<8; i++)
	cal_units[i] = CNUTypes.byteToChar(inData.readByte());
      unused1 = inData.readShort();
      datatype = inData.readShort();
      bitpix =  inData.readShort();
      dim_un0 =  inData.readShort();
      for(int i=0; i<8; i++) pixdim[i] = inData.readFloat();
      vox_offset = inData.readFloat();
      funused1 = inData.readFloat();
      funused2 = inData.readFloat();
      funused3 = inData.readFloat();
      cal_max = inData.readFloat();
      cal_min = inData.readFloat();
      compressed = inData.readInt();
      verified = inData.readInt();
      glmax = inData.readInt();
      glmin = inData.readInt();
    }
    /**
     * Converts the image dimension information into a readable string form.
     *
     * @return	String form of image_dimension
     */
    public String toString() {
      return(
	     "dim" + ShortArrayToString(dim) + "\n" +
	     "vox_units=" + vox_units + "\n" +
	     "cal_units=" + cal_units + "\n" +
	     "unused1=" + unused1 + "\n" +
	     "datatype=" + datatype + "\n" +
	     "bitpix=" + bitpix + "\n" +
	     "dim_un0=" + dim_un0 + "\n" +
	     "pixdim" + FloatArrayToString(pixdim) + "\n" +
	     "vox_offset=" + vox_offset + "\n" +
	     "funused1=" + funused1 + "\n" +
	     "funused2=" + funused2 + "\n" +
	     "funused3=" + funused3 + "\n" +
	     "cal_max=" + cal_max + "\n" +
	     "cal_min=" + cal_min + "\n" +
	     "compressed=" + compressed + "\n" +
	     "verified=" + verified + "\n" +
	     "glmax=" + glmax + "\n" +
	     "glmin=" + glmin + "\n"
	     );
    }
  }
  
  /**
   * Reads and stores the data history portion of an ANALYZE header
   * from a DataInput.
   *
   * @author	Joel T. Lee
   * @version %I%, %G%
   * @see		AnalyzeHeader
   * @see		java.io.DataInputStream
   * @since	iiV1.0
   */
  class data_history {
    char[] descrip=new char[80];		/* 0 + 80    */
    char[] aux_file=new char[24];		/* 80 + 24   */
    byte orient;				/* 104 + 1   */
    char[] originator=new char[10];	/* 105 + 10  */
    char[] generated=new char[10];	/* 115 + 10  */
    char[] scannum=new char[10];		/* 125 + 10  */
    char[] patient_id=new char[10];	/* 135 + 10  */
    char[] exp_date=new char[10];		/* 145 + 10  */
    char[] exp_time=new char[10];		/* 155 + 10  */
    char[] hist_un0=new char[3];		/* 165 + 3   */
    int views; 				/* 168 + 4   */
    int vols_added;			/* 172 + 4   */
    int start_field;			/* 176 + 4   */
    int field_skip;			/* 180 + 4   */
    int omax,omin;			/* 184 + 8   */
    int smax,smin;			/* 192 + 8   */
    
    /**
     * Constructs a new instance of data_history.
     *
     * @param inData	DataInput to read from
     * @exception	IOException	thrown if error reading from inData
     */
    public data_history(DataInput inData) throws IOException {
      for(int i=0; i<80; i++)
	descrip[i] = CNUTypes.byteToChar(inData.readByte());
      for(int i=0; i<24; i++)
	aux_file[i] = CNUTypes.byteToChar(inData.readByte());
      orient = inData.readByte();
      for(int i=0; i<10; i++)
	originator[i] = CNUTypes.byteToChar(inData.readByte());
      for(int i=0; i<10; i++)
	generated[i] = CNUTypes.byteToChar(inData.readByte());
      for(int i=0; i<10; i++)
	scannum[i] = CNUTypes.byteToChar(inData.readByte());
      for(int i=0; i<10; i++)
	patient_id[i] = CNUTypes.byteToChar(inData.readByte());
      for(int i=0; i<10; i++)
	exp_date[i] = CNUTypes.byteToChar(inData.readByte());
      for(int i=0; i<10; i++)
	exp_time[i] = CNUTypes.byteToChar(inData.readByte());
      for(int i=0; i<3; i++)
	hist_un0[i] = CNUTypes.byteToChar(inData.readByte());
      views = inData.readInt();
      vols_added = inData.readInt();
      start_field = inData.readInt();
      field_skip = inData.readInt();
      omax = inData.readInt();
      omin = inData.readInt();
      smax = inData.readInt();
      smin = inData.readInt();
    }
    /**
     * Converts the data history information into a readable string form.
     *
     * @return	String form of data_history
     */
    public String toString() {
      return(
	     "descrip=" + descrip + "\n" +
	     "aux_file=" + aux_file + "\n" +
	     "orient=" + orient + "\n" +
	     "originator=" + originator + "\n" +
	     "generated=" + generated + "\n" +
	     "scannum=" + scannum + "\n" +
	     "patient_id=" + patient_id + "\n" +
	     "exp_date=" + exp_date + "\n" +
	     "exp_time=" + exp_time + "\n" +
	     "hist_un0=" + hist_un0 + "\n" +
	     "views=" + views + "\n" +
	     "vols_added=" + vols_added + "\n" +
	     "start_field=" + start_field + "\n" +
	     "field_skip=" + field_skip + "\n" +
	     "omax=" + omax + "\n" +
	     "omin=" + omin + "\n" +
	     "smax=" + smax + "\n" +
	     "smin=" + smin
	     );
    }
  }
}
