// need to handle scl_inter

package iiv.io;
import iiv.data.*;
import java.io.*;
import java.lang.*;
import java.net.*;

/**
 * Reads and stores an NIFTI header file information.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUAnalyzeImgFile
 * @since	iiV1.0
 */
public class NiftiHeader {
  private Object localLock = new Object();
  private CNUFile cnufile;
  private CNUDataConversions cnuDataConv = new CNUDataConversions();
  private nifti_1_header n1h = null;
  /**
   * Constructs an new instance of NiftiHeader leaving fields empty.
   *
   */
  public NiftiHeader() {
  }
  /**
   * Constructs an new instance of NiftiHeader reading info from filename.
   *
   * @param filename	the file to read header information from
   * @exception	IOException	thrown if error reading from file
   */
  public NiftiHeader(String filename) throws IOException {
    setFile(filename);
  }
  /**
   * Sets the filename adding the appropriate extension ".hdr" if not present.
   *
   * @param filename	file name with or withoud ".hdr" extension
   */
  public void setFile(String filename) throws IOException {
    String[] hdrnames = buildNiftiHdrFileNames(filename);
    synchronized (localLock) {
      IOException saveioe = new IOException("no header names");
      for(int i=0; i<hdrnames.length && saveioe != null; i++) {
	saveioe = null;
	cnufile = new CNUFile(hdrnames[i]);
	try {
	  ReadHeader();
	} catch(IOException io) {
	  saveioe = io;
	}
      }
      if(saveioe != null) throw saveioe;
    }
  }

  /**
   * Builds a header file name (ie filename with .hdr extension).
   * Maintains ".gz" extension after ".hdr" on compressed file names.
   *
   * @param filename to base header filename on
   * @return filename with .hdr extension
   */
  static public String [] buildNiftiHdrFileNames(String filename) {
    String[] hdrnames = null;
    if(filename != null) {
      String extension = "";
      if( filename.endsWith(".gz") ) {
	filename = filename.substring(0, filename.lastIndexOf('.'));
	extension = ".gz";
      }
      if(filename.endsWith(".hdr")
	 || filename.endsWith(".nii")) {
	hdrnames = new String[1];
	hdrnames[0] = filename + extension;
      }
      else if(filename.endsWith(".img") ) {
	hdrnames = new String[1];
	filename = filename.substring(0, filename.lastIndexOf('.'));
	hdrnames[0] = filename + ".hdr" + extension;
      }
      else if( filename.endsWith(".log") ) {
	hdrnames = new String[2];
	filename = filename.substring(0, filename.lastIndexOf('.'));
	hdrnames[0] = filename + ".nii" + extension;
	hdrnames[1] = filename + ".hdr" + extension;
      }
      else {
	hdrnames = new String[2];
	hdrnames[0] = filename + ".nii" + extension;
	hdrnames[1] = filename + ".hdr" + extension;
      }
    }
    return hdrnames;
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
   * Gets the image file name (filename with .img or .nii extension).
   *
   * @return	filename with .img or .nii extension
   */
  public String getImgFileName() {
    String filename = null;
    synchronized (localLock) {
      if(cnufile != null) filename = cnufile.toString();
      if(filename != null && n1h != null) {
	if(n1h.magic[1] != '+') { // not in same file must be in *.img file
	  String extension = "";
	  if( filename.endsWith(".gz") ) {
	    filename = filename.substring(0, filename.lastIndexOf('.'));
	    extension = ".gz";
	  }
	  // remove .hdr or .nii, add .img and possibly add .gz
	  filename = filename.substring(0, filename.lastIndexOf('.')) + ".img" + extension;
	}
      }
    }
    return filename;
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
          n1h = new nifti_1_header(inData);
	  setCNUDataConversions(inData.getCNUDataConversions());
        }
      }
    } finally {
      if(inData != null) inData.close();
      if(in != null) in.close();
    }
  }
  /**
   * Gets the nifti data type
   *
   * @return	nifti data type
   */
  public double getNiftiType() {
    return n1h.datatype;
  }

  /**
   * Gets the factor to convert the nifti location units into meters.
   * Nifti resolution factors may be for meters, millimeters or micrometers.
   *
   * @return	to meters factor
   */
  public double get_space_to_meters_factor() {
    switch(n1h.get_space_units_code()) {
    default:
    case NIFTI_UNITS_METER:
      return 1.0;
    case NIFTI_UNITS_MM:
      return 1e-3;
    case NIFTI_UNITS_MICRON:
      return 1e-6;
    }
  }

  /**
   * Gets the factor to convert the nifti time units into seconds.
   * Nifti resolution factors may be for seconds, milliseconds or microseconds.
   *
   * @return	to seconds factor
   */
  public double get_time_to_seconds_factor() {
    switch(n1h.get_time_units_code()) {
    default:
    case NIFTI_UNITS_SEC:
      return 1.0;
    case NIFTI_UNITS_MSEC:
      return 1e-3;
    case NIFTI_UNITS_USEC:
      return 1e-6;
    }
  }

  /**
   * Outputs the Nifti dimensions as CNUDimensions.
   *
   * @return	a copy of dimensions for this analyze file
   */
  public CNUDimensions getDimensions() {
    CNUDimensions lsize = new CNUDimensions();
    synchronized (localLock) {
      int[] dims = new int[n1h.dim[0]];
      double[] ress = new double[n1h.dim[0]];
      double res2metersfactor = get_space_to_meters_factor();
      for(int i=0; i<n1h.dim[0]; i++) {
	dims[i] = n1h.dim[i+1];
	ress[i] = n1h.pixdim[i+1];
	// nifti reserves first 3 dims for space and dim 4 for time
	if(i < 3) ress[i] *= res2metersfactor;
	else if(i == 3) ress[i] *= get_time_to_seconds_factor();
      }
      lsize.setValues(dims, niftiToCNUType(n1h.datatype), 0);
      lsize.setSpatialResolutions(ress);
      // nifti method 1 default orientation values
      lsize.setOrientationOrder(CNUDimensions.RIGHT_POSITIVE | CNUDimensions.ANTERIOR_POSITIVE | CNUDimensions.SUPERIOR_POSITIVE);
      lsize.setOrientation(CNUDimensions.TRANSVERSE);
    }
    return(lsize);
  }
  /**
   * Outputs the QForm coordinate map derived from the header.
   *
   * @return	a copy of the QForm coordinate map or <code>null</code> if not applicable
   */
  public NiftiQFormCoordinateMap getQFormCoordinateMap() {
      if(n1h.qform_code > 0) {
	  double res2metersfactor = get_space_to_meters_factor();
	  return new NiftiQFormCoordinateMap(n1h.quatern_b, n1h.quatern_c, n1h.quatern_d,
					     n1h.qoffset_x*res2metersfactor,
					     n1h.qoffset_y*res2metersfactor,
					     n1h.qoffset_z*res2metersfactor,
					     n1h.pixdim[1]*res2metersfactor,
					     n1h.pixdim[2]*res2metersfactor,
					     n1h.pixdim[3]*res2metersfactor,
					     n1h.get_qfac(), "qform");
      }
      else return null;
  }
  /**
   * Outputs the SForm coordinate map derived from the header.
   *
   * @return	a copy of the SForm coordinate map or <code>null</code> if not applicable
   */
  public NiftiSFormCoordinateMap getSFormCoordinateMap() {
    if(n1h.sform_code > 0)
      return new NiftiSFormCoordinateMap(n1h.srow_x, n1h.srow_y, n1h.srow_z,
					 new XYZDouble(n1h.pixdim[1],
						       n1h.pixdim[2],
						       n1h.pixdim[3]),
					 get_space_to_meters_factor(),
					 niftiXFormCodeToString(n1h.sform_code));
    else return null;
  }
  /**
   * Gets the quantification factor for this nifti header.
   * True voxel value = getFactor() * voxel_value + getTranslation();
   *
   * @return	quantification factor
   */
  public double getFactor() {
    synchronized (localLock) {
      return (n1h.scl_slope != 0) ? n1h.scl_slope : 1.0;
    }
  }
  /**
   * Gets the quantification translation for this nifti header.
   * True voxel value = getFactor() * voxel_value + getTranslation();
   * This is ignored for now.
   *
   * @return	quantification translation value
   */
  public double getTranslation() {
    synchronized (localLock) {
      return (n1h.scl_slope != 0) ? n1h.scl_inter : 0.0;
    }
  }
  /**
   * Converts the header information into a readable string form.
   *
   * @return	header information as a string
   */
  public String toString() {
    return("** NIFTI Header =\n" + n1h);
  }
  /*    brief nifti1 datatype codes
   */
  final static int DT_UNKNOWN =		0;
  final static int DT_BINARY =		1;
  final static int DT_UNSIGNED_CHAR =	2;
  final static int DT_SIGNED_SHORT =	4;
  final static int DT_SIGNED_INT = 	8;
  final static int DT_FLOAT = 		16;
  final static int DT_COMPLEX =		32;
  final static int DT_DOUBLE = 		64;
  final static int DT_RGB =		128;
  final static int DT_ALL =             255;
  /*----- another set of names for the same ---*/
  final static int DT_UINT8 =           2;
  final static int DT_INT16 =           4;
  final static int DT_INT32 =           8;
  final static int DT_FLOAT32 =         16;
  final static int DT_COMPLEX64 =       32;
  final static int DT_FLOAT64 =         64;
  final static int DT_RGB24 =           128;
  /*------------------- new codes for NIFTI ---*/
  final static int DT_INT8 =            256;     /* signed char (8 bits)         */
  final static int DT_UINT16 =          512;     /* unsigned short (16 bits)     */
  final static int DT_UINT32 =          768;     /* unsigned int (32 bits)       */
  final static int DT_INT64 =           1024;    /* long long (64 bits)          */
  final static int DT_UINT64 =          1280;    /* unsigned long long (64 bits) */
  final static int DT_FLOAT128 =        1536;    /* long double (128 bits)       */
  final static int DT_COMPLEX128 =      1792;    /* double pair (128 bits)       */
  final static int DT_COMPLEX256 =      2048;    /* long double pair (256 bits)  */
  final static int DT_RGBA32 =          2304;    /* 4 byte RGBA (32 bits/voxel)  */
  //brief aliases for the nifti1 datatype codes                                       
  final static int NIFTI_TYPE_UINT8 =           2; /*! unsigned char. */
  final static int NIFTI_TYPE_INT16 =           4; /*! signed short. */
  final static int NIFTI_TYPE_INT32 =           8;  /*! signed int. */
  final static int NIFTI_TYPE_FLOAT32 =        16;  /*! 32 bit float. */
  final static int NIFTI_TYPE_COMPLEX64 =      32;  /*! 64 bit complex = 2 32 bit floats. */
  final static int NIFTI_TYPE_FLOAT64 =        64;  /*! 64 bit float = double. */
  final static int NIFTI_TYPE_RGB24 =         128;  /*! 3 8 bit bytes. */
  final static int NIFTI_TYPE_INT8 =          256;  /*! signed char. */
  final static int NIFTI_TYPE_UINT16 =        512;  /*! unsigned short. */
  final static int NIFTI_TYPE_UINT32 =        768;  /*! unsigned int. */
  final static int NIFTI_TYPE_INT64 =        1024;  /*! signed long long. */
  final static int NIFTI_TYPE_UINT64 =       1280;  /*! unsigned long long. */
  final static int NIFTI_TYPE_FLOAT128 =     1536;  /*! 128 bit float = long double. */
  final static int NIFTI_TYPE_COMPLEX128 =   1792;  /*! 128 bit complex = 2 64 bit floats. */
  final static int NIFTI_TYPE_COMPLEX256 =   2084;  /*! 256 bit complex = 2 128 bit floats */
  final static int NIFTI_TYPE_RGBA32 =       2304;  /*! 4 8 bit bytes. */

  /**
   * Converts a NIFTI header type to a String representation.
   *
   * @param niftiType NIFTI data type
   * @return data type name
   */
  public final static String niftiTypeToString(int niftiType) {
    switch (niftiType) {
    default:
      return "NIFTI_TYPE_UNKNOWN" + niftiType;
    case DT_UNKNOWN:
      return "DT_UNKNOWN";
    case DT_BINARY:
      return "DT_BINARY";
    case DT_ALL:
      return "DT_ALL";
    case NIFTI_TYPE_UINT8:
      return "NIFTI_TYPE_UINT8";
    case NIFTI_TYPE_INT16:
      return "NIFTI_TYPE_INT16";
    case NIFTI_TYPE_INT32:
      return "NIFTI_TYPE_INT32";
    case NIFTI_TYPE_FLOAT32:
      return "NIFTI_TYPE_FLOAT32";
    case NIFTI_TYPE_COMPLEX64:
      return "NIFTI_TYPE_COMPLEX64";
    case NIFTI_TYPE_FLOAT64:
      return "NIFTI_TYPE_FLOAT64";
    case NIFTI_TYPE_RGB24:
      return "NIFTI_TYPE_RGB24";
    case NIFTI_TYPE_INT8:
      return "NIFTI_TYPE_INT8";
    case NIFTI_TYPE_UINT16:
      return "NIFTI_TYPE_UINT16";
    case NIFTI_TYPE_UINT32:
      return "NIFTI_TYPE_UINT32";
    case NIFTI_TYPE_INT64:
      return "NIFTI_TYPE_INT64";
    case NIFTI_TYPE_UINT64:
      return "NIFTI_TYPE_UINT64";
    case NIFTI_TYPE_FLOAT128:
      return "NIFTI_TYPE_FLOAT128";
    case NIFTI_TYPE_COMPLEX128:
      return "NIFTI_TYPE_COMPLEX128";
    case NIFTI_TYPE_COMPLEX256:
      return "NIFTI_TYPE_COMPLEX256";
    case NIFTI_TYPE_RGBA32:      
      return "NIFTI_TYPE_RGBA32";
    }
  }
  /**
   * Converts a NIFTI header type into CNU type.
   *
   * @param niftiType NIFTI data type
   * @return	CNU data type
   */
  public final static int niftiToCNUType(int niftiType) {
    switch (niftiType) {
    case NIFTI_TYPE_INT8:
      return CNUTypes.BYTE;
    case NIFTI_TYPE_UINT8: // case DT_UNSIGNED_CHAR: 
      return CNUTypes.UNSIGNED_BYTE;
    case NIFTI_TYPE_INT16: // case DT_SIGNED_SHORT:
      return CNUTypes.SHORT;
    case NIFTI_TYPE_UINT16:
      return CNUTypes.UNSIGNED_SHORT;
    case NIFTI_TYPE_INT32: //    case DT_SIGNED_INT:
      return CNUTypes.INTEGER;
    case NIFTI_TYPE_UINT32:
      return CNUTypes.UNSIGNED_INTEGER;
    case NIFTI_TYPE_INT64:
      return CNUTypes.LONG;
    case NIFTI_TYPE_FLOAT32: // case DT_FLOAT:
      return CNUTypes.FLOAT;
    case NIFTI_TYPE_FLOAT64: // case DT_DOUBLE:
      return CNUTypes.DOUBLE;
    case NIFTI_TYPE_RGB24: // case DT_RGB:
      return CNUTypes.INTEGER;
    case NIFTI_TYPE_RGBA32:      
      return CNUTypes.INTEGER;
    case DT_UNKNOWN:
    case DT_BINARY:
    case NIFTI_TYPE_UINT64:
    case NIFTI_TYPE_COMPLEX64: // case DT_COMPLEX:
    case NIFTI_TYPE_FLOAT128:
    case NIFTI_TYPE_COMPLEX128:
    case NIFTI_TYPE_COMPLEX256:
    case DT_ALL:
    default:
      return CNUTypes.UNKNOWN;
    }
  }

  final static int NIFTI_INTENT_NONE = 0;
  /*-------- These codes are for probability distributions ---------------*/
  /* Most distributions have a number of parameters,
     below denoted by p1, p2, and p3, and stored in
     - intent_p1, intent_p2, intent_p3 if dataset doesn't have 5th dimension
     - image data array                if dataset does have 5th dimension
     
     Functions to compute with many of the distributions below can be found
     in the CDF library from U Texas.

     Formulas for and discussions of these distributions can be found in the
     following books:

     [U] Univariate Discrete Distributions,
     NL Johnson, S Kotz, AW Kemp.

     [C1] Continuous Univariate Distributions, vol. 1,
     NL Johnson, S Kotz, N Balakrishnan.
       
     [C2] Continuous Univariate Distributions, vol. 2,
     NL Johnson, S Kotz, N Balakrishnan.                            */
  /*----------------------------------------------------------------------*/
  /*! [C2, chap 32] Correlation coefficient R (1 param):
    p1 = degrees of freedom
    R/sqrt(1-R*R) is t-distributed with p1 DOF. */
  /*! \defgroup NIFTI1_INTENT_CODES
    \brief nifti1 intent codes, to describe intended meaning of dataset contents
    @{
  */
  final static int NIFTI_INTENT_CORREL =      2;
  /*! [C2, chap 28] Student t statistic (1 param): p1 = DOF. */
  final static int NIFTI_INTENT_TTEST =       3;
  /*! [C2, chap 27] Fisher F statistic (2 params):
    p1 = numerator DOF, p2 = denominator DOF. */
  final static int NIFTI_INTENT_FTEST =       4;
  /*! [C1, chap 13] Standard normal (0 params): Density = N(0,1). */
  final static int NIFTI_INTENT_ZSCORE =      5;
  /*! [C1, chap 18] Chi-squared (1 param): p1 = DOF.
    Density(x) proportional to exp(-x/2) * x^(p1/2-1). */
  final static int NIFTI_INTENT_CHISQ  =      6;
  /*! [C2, chap 25] Beta distribution (2 params): p1=a, p2=b.
      Density(x) proportional to x^(a-1) * (1-x)^(b-1). */
  final static int NIFTI_INTENT_BETA  =       7;
  /*! [U, chap 3] Binomial distribution (2 params):
       p1 = number of trials, p2 = probability per trial.
      Prob(x) = (p1 choose x) * p2^x * (1-p2)^(p1-x), for x=0,1,...,p1. */
  final static int NIFTI_INTENT_BINOM =       8;
  /*! [C1, chap 17] Gamma distribution (2 params):
       p1 = shape, p2 = scale.
      Density(x) proportional to x^(p1-1) * exp(-p2*x). */
  final static int NIFTI_INTENT_GAMMA =       9;
  /*! [U, chap 4] Poisson distribution (1 param): p1 = mean.
      Prob(x) = exp(-p1) * p1^x / x! , for x=0,1,2,.... */
  final static int NIFTI_INTENT_POISSON =    10;
  /*! [C1, chap 13] Normal distribution (2 params):
       p1 = mean, p2 = standard deviation. */
  final static int NIFTI_INTENT_NORMAL =     11;
  /*! [C2, chap 30] Noncentral F statistic (3 params):
       p1 = numerator DOF, p2 = denominator DOF,
       p3 = numerator noncentrality parameter.  */
  final static int NIFTI_INTENT_FTEST_NONC = 12;
  /*! [C2, chap 29] Noncentral chi-squared statistic (2 params):
       p1 = DOF, p2 = noncentrality parameter.     */
  final static int NIFTI_INTENT_CHISQ_NONC = 13;
  /*! [C2, chap 23] Logistic distribution (2 params):
       p1 = location, p2 = scale.
      Density(x) proportional to sech^2((x-p1)/(2*p2)). */
  final static int NIFTI_INTENT_LOGISTIC =   14;
  /*! [C2, chap 24] Laplace distribution (2 params):
       p1 = location, p2 = scale.
      Density(x) proportional to exp(-abs(x-p1)/p2). */
  final static int NIFTI_INTENT_LAPLACE =    15;
  /*! [C2, chap 26] Uniform distribution: p1 = lower end, p2 = upper end. */
  final static int NIFTI_INTENT_UNIFORM =    16;
  /*! [C2, chap 31] Noncentral t statistic (2 params):
       p1 = DOF, p2 = noncentrality parameter. */
  final static int NIFTI_INTENT_TTEST_NON = 17;
  /*! [C1, chap 21] Weibull distribution (3 params):
       p1 = location, p2 = scale, p3 = power.
      Density(x) proportional to
       ((x-p1)/p2)^(p3-1) * exp(-((x-p1)/p2)^p3) for x > p1. */
  final static int NIFTI_INTENT_WEIBULL =    18;
  /*! [C1, chap 18] Chi distribution (1 param): p1 = DOF.
      Density(x) proportional to x^(p1-1) * exp(-x^2/2) for x > 0.
       p1 = 1 = 'half normal' distribution
       p1 = 2 = Rayleigh distribution
       p1 = 3 = Maxwell-Boltzmann distribution.                  */
  final static int NIFTI_INTENT_CHI  =       19;
  /*! [C1, chap 15] Inverse Gaussian (2 params):
       p1 = mu, p2 = lambda
      Density(x) proportional to
       exp(-p2*(x-p1)^2/(2*p1^2*x)) / x^3  for x > 0. */
  final static int NIFTI_INTENT_INVGAUSS =   20;
  /*! [C2, chap 22] Extreme value type I (2 params):
       p1 = location, p2 = scale
      cdf(x) = exp(-exp(-(x-p1)/p2)). */
  final static int NIFTI_INTENT_EXTVAL =     21;
  /*! Data is a 'p-value' (no params). */
  final static int NIFTI_INTENT_PVAL =       22;
  /*! Data is ln(p-value) (no params).
      To be safe, a program should compute p = exp(-abs(this_value)).
      The nifti_stats.c library returns this_value
      as positive, so that this_value = -log(p). */
  final static int NIFTI_INTENT_LOGPVAL =    23;
  /*! Data is log10(p-value) (no params).
      To be safe, a program should compute p = pow(10.,-abs(this_value)).
      The nifti_stats.c library returns this_value
      as positive, so that this_value = -log10(p). */
  final static int NIFTI_INTENT_LOG10PVAL =  24;
  /*! Smallest intent_code that indicates a statistic. */
  final static int NIFTI_FIRST_STATCODE =     2;
  /*! Largest intent_code that indicates a statistic. */
  final static int NIFTI_LAST_STATCODE =     24;
    /*---------- these values for intent_code aren't for statistics ----------*/

    /*! To signify that the value at each voxel is an estimate
      of some parameter, set intent_code = NIFTI_INTENT_ESTIMATE.
      The name of the parameter may be stored in intent_name.     */
  final static int NIFTI_INTENT_ESTIMATE =  1001;
    /*! To signify that the value at each voxel is an index into
      some set of labels, set intent_code = NIFTI_INTENT_LABEL.
      The filename with the labels may stored in aux_file.        */
    final static int NIFTI_INTENT_LABEL =     1002;
  /*! To signify that the value at each voxel is an index into the
    NeuroNames labels set, set intent_code = NIFTI_INTENT_NEURONAME. */
  final static int NIFTI_INTENT_NEURONAME = 1003;
  /*! To store an M x N matrix at each voxel:
    - dataset must have a 5th dimension (dim[0]=5 and dim[5]>1)
    - intent_code must be NIFTI_INTENT_GENMATRIX
    - dim[5] must be M*N
    - intent_p1 must be M (in float format)
    - intent_p2 must be N (ditto)
    - the matrix values A[i][[j] are stored in row-order:
    - A[0][0] A[0][1] ... A[0][N-1]
    - A[1][0] A[1][1] ... A[1][N-1]
    - etc., until
    - A[M-1][0] A[M-1][1] ... A[M-1][N-1]        */
  final static int NIFTI_INTENT_GENMATRIX = 1004;
  
  /*! To store an NxN symmetric matrix at each voxel:
    - dataset must have a 5th dimension
    - intent_code must be NIFTI_INTENT_SYMMATRIX
    - dim[5] must be N*(N+1)/2
    - intent_p1 must be N (in float format)
    - the matrix values A[i][[j] are stored in row-order:
    - A[0][0]
    - A[1][0] A[1][1]
    - A[2][0] A[2][1] A[2][2]
    - etc.: row-by-row                           */
  final static int NIFTI_INTENT_SYMMATRIX = 1005;
  /*! To signify that the vector value at each voxel is to be taken
    as a displacement field or vector:
    - dataset must have a 5th dimension
    - intent_code must be NIFTI_INTENT_DISPVECT
    - dim[5] must be the dimensionality of the displacment
    vector (e.g., 3 for spatial displacement, 2 for in-plane) */
  final static int NIFTI_INTENT_DISPVECT  = 1006;   /* specifically for displacements */
  final static int NIFTI_INTENT_VECTOR =    1007;   /* for any other type of vector */
  /*! To signify that the vector value at each voxel is really a
    spatial coordinate (e.g., the vertices or nodes of a surface mesh):
    - dataset must have a 5th dimension
    - intent_code must be NIFTI_INTENT_POINTSET
    - dim[0] = 5
    - dim[1] = number of points
    - dim[2] = dim[3] = dim[4] = 1
    - dim[5] must be the dimensionality of space (e.g., 3 => 3D space).
    - intent_name may describe the object these points come from
    (e.g., "pial", "gray/white" , "EEG", "MEG").                   */
  final static int NIFTI_INTENT_POINTSET =  1008;
  /*! To signify that the vector value at each voxel is really a triple
    of indexes (e.g., forming a triangle) from a pointset dataset:
    - dataset must have a 5th dimension
    - intent_code must be NIFTI_INTENT_TRIANGLE
    - dim[0] = 5
    - dim[1] = number of triangles
    - dim[2] = dim[3] = dim[4] = 1
    - dim[5] = 3
    - datatype should be an integer type (preferably DT_INT32)
    - the data values are indexes (0,1,...) into a pointset dataset. */
  final static int NIFTI_INTENT_TRIANGLE =  1009;
  /*! To signify that the vector value at each voxel is a quaternion:
    - dataset must have a 5th dimension
    - intent_code must be NIFTI_INTENT_QUATERNION
    - dim[0] = 5
    - dim[5] = 4
    - datatype should be a floating point type     */
  final static int NIFTI_INTENT_QUATERNION = 1010;
  /*! Dimensionless value - no params - although, as in _ESTIMATE 
    the name of the parameter may be stored in intent_name.     */
  final static int NIFTI_INTENT_DIMLESS =    1011;
  /*---------- these values apply to GIFTI datasets ----------*/
  /*! To signify that the value at each location is from a time series. */
  final static int NIFTI_INTENT_TIME_SERIES =  2001;
  /*! To signify that the value at each location is a node index, from
    a complete surface dataset.                                       */
  final static int NIFTI_INTENT_NODE_INDEX =   2002;
  /*! To signify that the vector value at each location is an RGB triplet,
    of whatever type.
    - dataset must have a 5th dimension
    - dim[0] = 5
    - dim[1] = number of nodes
    - dim[2] = dim[3] = dim[4] = 1
    - dim[5] = 3
  */
  final static int NIFTI_INTENT_RGB_VECTOR =   2003;
  /*! To signify that the vector value at each location is a 4 valued RGBA
    vector, of whatever type.
    - dataset must have a 5th dimension
    - dim[0] = 5
    - dim[1] = number of nodes
    - dim[2] = dim[3] = dim[4] = 1
    - dim[5] = 4
  */
  final static int NIFTI_INTENT_RGBA_VECTOR =  2004;
  /*! To signify that the value at each location is a shape value, such
    as the curvature.  */
  final static int NIFTI_INTENT_SHAPE  =       2005;

  /**
   * Converts a NIFTI intent to a String representation.
   *
   * @param niftiIntent NIFTI intent code
   * @return intent code name
   */
  public final static String niftiIntentToString(int niftiIntent) {
    switch(niftiIntent) {
    default:
      return "NIFTI_INTENT_UNKNOWN" + niftiIntent;
    case NIFTI_INTENT_NONE:
      return "NIFTI_INTENT_NONE";
    case NIFTI_INTENT_CORREL:
      return "NIFTI_INTENT_CORREL";
    case NIFTI_INTENT_TTEST:
      return "NIFTI_INTENT_TTEST";
    case NIFTI_INTENT_FTEST:
      return "NIFTI_INTENT_FTEST";
    case NIFTI_INTENT_ZSCORE:
      return "NIFTI_INTENT_ZSCORE";
    case NIFTI_INTENT_CHISQ:
      return "NIFTI_INTENT_CHISQ";
    case NIFTI_INTENT_BETA:
      return "NIFTI_INTENT_BETA";
    case NIFTI_INTENT_BINOM:
      return "NIFTI_INTENT_BINOM";
    case NIFTI_INTENT_GAMMA:
      return "NIFTI_INTENT_GAMMA";
    case NIFTI_INTENT_POISSON:
      return "NIFTI_INTENT_POISSON";
    case NIFTI_INTENT_NORMAL:
      return "NIFTI_INTENT_NORMAL";
    case NIFTI_INTENT_FTEST_NONC:
      return "NIFTI_INTENT_FTEST_NONC";
    case NIFTI_INTENT_CHISQ_NONC:
      return "NIFTI_INTENT_CHISQ_NONC";
    case NIFTI_INTENT_LOGISTIC:
      return "NIFTI_INTENT_LOGISTIC";
    case NIFTI_INTENT_LAPLACE:
      return "NIFTI_INTENT_LAPLACE";
    case NIFTI_INTENT_UNIFORM:
      return "NIFTI_INTENT_UNIFORM";
    case NIFTI_INTENT_TTEST_NON:
      return "NIFTI_INTENT_TTEST_NON";
    case NIFTI_INTENT_WEIBULL:
      return "NIFTI_INTENT_WEIBULL";
    case NIFTI_INTENT_CHI:
      return "NIFTI_INTENT_CHI";
    case NIFTI_INTENT_INVGAUSS:
      return "NIFTI_INTENT_INVGAUSS";
    case NIFTI_INTENT_EXTVAL:
      return "NIFTI_INTENT_EXTVAL";
    case NIFTI_INTENT_PVAL:
      return "NIFTI_INTENT_PVAL";
    case NIFTI_INTENT_LOGPVAL:
      return "NIFTI_INTENT_LOGPVAL";
    case NIFTI_INTENT_LOG10PVAL:
      return "NIFTI_INTENT_LOG10PVAL";
    case NIFTI_INTENT_ESTIMATE:
      return "NIFTI_INTENT_ESTIMATE";
    case NIFTI_INTENT_LABEL:
      return "NIFTI_INTENT_LABEL";
    case NIFTI_INTENT_NEURONAME:
      return "NIFTI_INTENT_NEURONAME";
    case NIFTI_INTENT_GENMATRIX:
      return "NIFTI_INTENT_GENMATRIX";
    case NIFTI_INTENT_SYMMATRIX:
      return "NIFTI_INTENT_SYMMATRIX";
    case NIFTI_INTENT_DISPVECT:
      return "NIFTI_INTENT_DISPVECT";
    case NIFTI_INTENT_VECTOR:
      return "NIFTI_INTENT_VECTOR";
    case NIFTI_INTENT_POINTSET:
      return "NIFTI_INTENT_POINTSET";
    case NIFTI_INTENT_TRIANGLE:
      return "NIFTI_INTENT_TRIANGLE";
    case NIFTI_INTENT_QUATERNION:
      return "NIFTI_INTENT_QUATERNION";
    case NIFTI_INTENT_DIMLESS:
      return "NIFTI_INTENT_DIMLESS";
    case NIFTI_INTENT_TIME_SERIES:
      return "NIFTI_INTENT_TIME_SERIES";
    case NIFTI_INTENT_NODE_INDEX:
      return "NIFTI_INTENT_NODE_INDEX";
    case NIFTI_INTENT_RGB_VECTOR:
      return "NIFTI_INTENT_RGB_VECTOR";
    case NIFTI_INTENT_RGBA_VECTOR:
      return "NIFTI_INTENT_RGBA_VECTOR";
    case NIFTI_INTENT_SHAPE:
      return "NIFTI_INTENT_SHAPE";
    }
  }

  /* [qs]form_code value:  */      /* x,y,z coordinate system refers to:    */
  /*-----------------------*/      /*---------------------------------------*/

  /*! \defgroup NIFTI1_XFORM_CODES
    \brief nifti1 xform codes to describe the "standard" coordinate system
    @{
  */
  /*! Arbitrary coordinates (Method 1). */
  final static int NIFTI_XFORM_UNKNOWN = 0;
  /*! Scanner-based anatomical coordinates */
  final static int NIFTI_XFORM_SCANNER_ANAT = 1;
  /*! Coordinates aligned to another file's,
    or to anatomical "truth".            */
  final static int NIFTI_XFORM_ALIGNED_ANAT = 2;
  /*! Coordinates aligned to Talairach-
    Tournoux Atlas; (0,0,0)=AC, etc. */
  final static int NIFTI_XFORM_TALAIRACH = 3;
  /*! MNI 152 normalized coordinates. */
  final static int NIFTI_XFORM_MNI_152 = 4;
  /* @} */
  /**
   * Converts a NIFTI xform_code to a String representation.
   *
   * @param niftiXFormCode NIFTI xform code
   * @return xform code name
   */
  public final static String niftiXFormCodeToString(int niftiXFormCode) {
    switch(niftiXFormCode) {
    case NIFTI_XFORM_UNKNOWN:
      return "NIFTI_XFORM_UNKNOWN";
    case NIFTI_XFORM_SCANNER_ANAT:
      return "NIFTI_XFORM_SCANNER_ANAT";
    case NIFTI_XFORM_ALIGNED_ANAT:
      return "NIFTI_XFORM_ALIGNED_ANAT";
    case NIFTI_XFORM_TALAIRACH:
      return "NIFTI_XFORM_TALAIRACH";
    case NIFTI_XFORM_MNI_152:
      return "NIFTI_XFORM_MNI_152";
    default:
      return "NIFT_XFORM_UNKNOWN" + niftiXFormCode;
    }
  }

  /*! \defgroup NIFTI1_UNITS
    \brief nifti1 units codes to describe the unit of measurement for
    each dimension of the dataset
    @{
  */
  /*! NIFTI code for unspecified units. */
  final static int NIFTI_UNITS_UNKNOWN = 0;
  /** Space codes are multiples of 1. **/
  /*! NIFTI code for meters. */
  final static int NIFTI_UNITS_METER = 1;
  /*! NIFTI code for millimeters. */
  final static int NIFTI_UNITS_MM = 2;
  /*! NIFTI code for micrometers. */
  final static int NIFTI_UNITS_MICRON = 3;
  /** Time codes are multiples of 8. **/
  /*! NIFTI code for seconds. */
  final static int NIFTI_UNITS_SEC = 8;
  /*! NIFTI code for milliseconds. */
  final static int NIFTI_UNITS_MSEC = 16;
  /*! NIFTI code for microseconds. */
  final static int NIFTI_UNITS_USEC = 24;
  /*** These units are for spectral data: ***/
  /*! NIFTI code for Hertz. */
  final static int NIFTI_UNITS_HZ = 32;
  /*! NIFTI code for ppm. */
  final static int NIFTI_UNITS_PPM = 40;
  /*! NIFTI code for radians per second. */
  final static int NIFTI_UNITS_RADS = 48;
  /* @} */
  /**
   * Converts a NIFTI units code to a String representation.
   *
   * @param niftiUnitsCode units code
   * @return units code name
   */
  public final static String niftiUnitsCodeToString(int niftiUnitsCode) {
    switch(niftiUnitsCode) {
    case NIFTI_UNITS_UNKNOWN:
      return "NIFTI_UNITS_UNKNOWN";
    case NIFTI_UNITS_METER:
      return "NIFTI_UNITS_METER";
    case NIFTI_UNITS_MM:
      return "NIFTI_UNITS_MM";
    case NIFTI_UNITS_MICRON:
      return "NIFTI_UNITS_MICRON";
    case NIFTI_UNITS_SEC:
      return "NIFTI_UNITS_SEC";
    case NIFTI_UNITS_MSEC:
      return "NIFTI_UNITS_MSEC";
    case NIFTI_UNITS_USEC:
      return "NIFTI_UNITS_USEC";
    case NIFTI_UNITS_HZ:
      return "NIFTI_UNITS_HZ";
    case NIFTI_UNITS_PPM:
      return "NIFTI_UNITS_PPM";
    case  NIFTI_UNITS_RADS:
      return " NIFTI_UNITS_RADS";
    default:
      return "NIFTI_UNITS_UNKNOWN" + niftiUnitsCode;
    }
  }

  /*! \defgroup NIFTI1_SLICE_ORDER
    \brief nifti1 slice order codes, describing the acquisition order
    of the slices
    @{
  */
  final static int NIFTI_SLICE_UNKNOWN =   0;
  final static int NIFTI_SLICE_SEQ_INC =   1;
  final static int NIFTI_SLICE_SEQ_DEC =   2;
  final static int NIFTI_SLICE_ALT_INC =   3;
  final static int NIFTI_SLICE_ALT_DEC =   4;
  final static int NIFTI_SLICE_ALT_INC2 =  5;  /* 05 May 2005: RWCox */
  final static int NIFTI_SLICE_ALT_DEC2  = 6;  /* 05 May 2005: RWCox */
  /* @} */
  /**
   * Converts a NIFTI slice order code to a String representation.
   *
   * @param niftiSliceCode slice code
   * @return String slice code name
   */
  public final static String niftiSliceCodeToString(int niftiSliceCode) {
    switch(niftiSliceCode) {
    case NIFTI_SLICE_UNKNOWN:
      return "NIFTI_SLICE_UNKNOWN";
    case NIFTI_SLICE_SEQ_INC:
      return "NIFTI_SLICE_SEQ_INC";
    case NIFTI_SLICE_SEQ_DEC:
      return "NIFTI_SLICE_SEQ_DEC";
    case NIFTI_SLICE_ALT_INC:
      return "NIFTI_SLICE_ALT_INC";
    case NIFTI_SLICE_ALT_DEC:
      return "NIFTI_SLICE_ALT_DEC";
    case NIFTI_SLICE_ALT_INC2:
      return "NIFTI_SLICE_ALT_INC2";
    case NIFTI_SLICE_ALT_DEC2:
      return "NIFTI_SLICE_ALT_DEC2";
    default:
      return "NIFTI_SLICE_UNKNOWN" + niftiSliceCode;
    }
  }
  /**
   * Gets the offset to the first data voxel in the image file.
   *
   * @return voxel offset
   */
  public long getVoxelOffset() { return (long) n1h.vox_offset; }
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
   * Reads and stores the header key portion of an NIFTI header from
   * a DataInputStream.
   *
   * @author	Joel T. Lee
   * @version %I%, %G%
   * @see		NiftiHeader
   * @see		java.io.DataInputStream
   * @since	iiV1.0
   */
  class nifti_1_header {
    /* off + size*/
    int sizeof_hdr;			/* 0 + 4     */ /*!< MUST be 348           */
    char[] data_type=new char[10];	/* 4 + 10    */ /* nifti unused */
    char[] db_name=new char[18];	/* 14 + 18   */ /* nifti unused */
    int extents;			/* 32 + 4    */ /* nifti unused */
    short session_error;		/* 36 + 2    */ /* nifti unused */
    char regular;			/* 38 + 1    */ /* nifti unused */
    //char hkey_un0;			/* 39 + 1    */
    char dim_info;			/* 39 + 1    */ /* MRI slice ordering */

    short[] dim=new short[8];	        /* 40 + 16    */
    //char[] vox_units=new char[4];	/* 40 + 4    */
    float intent_p1;                    /* 56 + 4    */ /* 1st intent parameter */
    //char[] cal_units=new char[8];	/* 60 + 4    */
    float intent_p2;                    /* 60 + 4    */ /* 2nd intent parameter */
    float intent_p3;                    /* 64 + 4    */ /* 3rd intent parameter */
    //short unused1;			/* 68 + 2    */
    short intent_code;			/* 68 + 2    */ /* NIFTI INTENT code. */
    short datatype;			/* 70 + 2    */
    short bitpix;			/* 72 + 2    */
    //short dim_un0;			/* 74 + 2    */
    short slice_start;			/* 74 + 2    */
    float[] pixdim=new float[8];	/* 76 + 32   */
    /* 
       pixdim[] specifies the voxel dimensions:
       pixdim[0] - quantification - my own invent jtl
       pixdim[1] - voxel width
       pixdim[2] - voxel height
       pixdim[3] - interslice distance
       ..etc
    */
    float vox_offset;		        /* 108 + 4    */
    float scl_slope;			/* 112 + 4    */ /* Data scaling: slope. */
    //float funused1;			/* 112 + 4    */
    float scl_inter;			/* 116 + 4    */ /* Data scaling: offset. */
    //float funused2;			/* 116 + 4    */
    short slice_end;			/* 120 + 2    */ /* Last slice index. */
    char slice_code;			/* 122 + 1    */ /* Slice timing order. */
    char xyzt_units;			/* 123 + 1    */ /* Units of pixdim[1..4]. */
    //float funused3;			/* 120 + 4    */
    float cal_max; 			/* 124 + 4    */ /* Max display intensity */
    float cal_min;			/* 128 + 4    */ /* Min display intensity */
    //int compressed;			/* 132 + 4    */
    float slice_duration;		/* 132 + 4    */ /* Time for 1 slice. */
    float toffset;		        /* 136 + 4    */ /* Time axis shirt. */
    //int verified;			/* 136 + 4    */
    int glmax, glmin;			/* 140 + 8    */ /* unused */

    char[] descrip=new char[80];	/* 148 + 80   */ /* any text you like. */
    char[] aux_file=new char[24];	/* 228 + 24   */ /* auxiliary filename */
    short qform_code;                   /* 252 + 2    */ /*!< NIFTIXFORM code. */ /*-- all ANALYZE 7.5 ---*/
    short sform_code;                   /* 254 + 2    */ /*!< NIFTIXFORM code. */ /* fields below here */
                                                                                  /* are replaced */
    float quatern_b;			/* 256 + 4    */ /*!< Quaternion b param. */
    float quatern_c;		        /* 260 + 4    */ /*!< Quaternion c param. */
    float quatern_d; 			/* 264 + 4    */ /*!< Quaternion d param. */
    float qoffset_x;			/* 268 + 4    */ /*!< Quaternion x shift. */
    float qoffset_y;			/* 272 + 4    */ /*!< Quaternion y shift. */
    float qoffset_z;			/* 276 + 4    */ /*!< Quaternion z shift. */

    float[] srow_x=new float[4];	/* 280 + 16    */ /*!< 1st row affine transform. */
    float[] srow_y=new float[4]; 	/* 296 + 16    */ /*!< 2nd row affine transform. */
    float[] srow_z=new float[4]; 	/* 312 + 16    */ /*!< 3rd row affine transform. */

    char[] intent_name=new char[16];	/* 328 + 16    */ /*!< name or meaning of data. */

    char[] magic=new char[4];		/* 344 + 4    */ /*!< MUST be "ni1\0" or "n+1\0". */ 
    byte[] extender=new byte[4];
    int esize = 0;
    int ecode;
    byte[] edata = null;


    public int get_space_units_code() { return xyzt_units & 0x07; }
    public int get_time_units_code() { return xyzt_units & 0x38; }

    public int combine_time_and_space_units_code(int xyz_units, int t_units) {return (xyz_units & 0x07) | (t_units & 0x38);}
    public int get_freq_dim() { return dim_info & 0x03; }
    public int get_phase_dim() { return (dim_info >> 2) & 0x03; }
    public int get_slice_dim() { return (dim_info >> 4) & 0x03; }
    public double get_qfac() { return (pixdim[0] >= -0.5) ? 1.0d : -1.0d; }

    //byte orient;			/* 104 + 1   */
    //char[] originator=new char[10];	/* 105 + 10  */
    //char[] generated=new char[10];	/* 115 + 10  */
    //char[] scannum=new char[10];	/* 125 + 10  */
    //char[] patient_id=new char[10];	/* 135 + 10  */
    //char[] exp_date=new char[10];	/* 145 + 10  */
    //char[] exp_time=new char[10];	/* 155 + 10  */
    //char[] hist_un0=new char[3];	/* 165 + 3   */
    //int views; 			/* 168 + 4   */
    //int vols_added;			/* 172 + 4   */
    //int start_field;			/* 176 + 4   */
    //int field_skip;			/* 180 + 4   */
    //int omax,omin;			/* 184 + 8   */
    //int smax,smin;			/* 192 + 8   */

    /**
     * Constructs a new instance of nifti_1_header.
     *
     * @param inData	DataInputStream to read from
     * @exception	IOException	thrown if error reading from inData
     */
    public nifti_1_header(ConvertDataInputStream inData) throws IOException {
      byte[] inBytes = new byte[4];
      if(inData.read(inBytes, 0, 4) != 4)
	throw new IOException("error reading nifti header_key");
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
      for(int i=0; i<10; i++) data_type[i] = (char) inData.readByte();
      for(int i=0; i<18; i++) db_name[i] = (char) inData.readByte();
      extents = inData.readInt();
      session_error = inData.readShort();
      regular = (char) inData.readByte();
      //      if(regular != 'r')
      //	throw new IOException(
      //	    "invalid header_key value for regular should be 'r'");
      dim_info = (char) inData.readByte();

      for(int i=0; i<8; i++) dim[i] = inData.readShort();
      //      for(int i=0; i<4; i++)
      //	vox_units[i] = CNUTypes.byteToChar(inData.readByte());
      intent_p1 = inData.readFloat();
      intent_p2 = inData.readFloat();
      intent_p3 = inData.readFloat();
      //      for(int i=0; i<8; i++)
      //	cal_units[i] = CNUTypes.byteToChar(inData.readByte());
      intent_code = inData.readShort();
      datatype = inData.readShort();
      bitpix =  inData.readShort();
      slice_start =  inData.readShort();
      for(int i=0; i<8; i++) pixdim[i] = inData.readFloat();
      vox_offset = inData.readFloat();
      scl_slope = inData.readFloat();
      scl_inter = inData.readFloat();
      slice_end = inData.readShort();
      slice_code = (char) inData.readByte();
      xyzt_units = (char) inData.readByte();
      cal_max = inData.readFloat();
      cal_min = inData.readFloat();
      slice_duration = inData.readFloat();
      toffset = inData.readFloat();
      glmax = inData.readInt();
      glmin = inData.readInt();


      for(int i=0; i<80; i++) descrip[i] = (char) inData.readByte();
      for(int i=0; i<24; i++) aux_file[i] = (char) inData.readByte();
      qform_code = inData.readShort();
      sform_code = inData.readShort();
      quatern_b = inData.readFloat();
      quatern_c = inData.readFloat();
      quatern_d = inData.readFloat();
      qoffset_x = inData.readFloat();
      qoffset_y = inData.readFloat();
      qoffset_z = inData.readFloat();
      for(int i=0; i<srow_x.length; i++) srow_x[i] = inData.readFloat();
      for(int i=0; i<srow_y.length; i++) srow_y[i] = inData.readFloat();
      for(int i=0; i<srow_z.length; i++) srow_z[i] = inData.readFloat();
      for(int i=0; i<intent_name.length; i++) intent_name[i] = (char) inData.readByte();
      for(int i=0; i<magic.length; i++) magic[i] = (char) inData.readByte();
      if(magic[0] != 'n' || !(magic[1] == '+' || magic[1] == 'i') || magic[2] != '1') {
	throw new IOException(
			      "invalid nifti magic=\"" +
			      magic[0] + magic[1] + magic[2] +
			      "\" should be \"n+1\" or \"ni1\"");
      }
      try {
	for(int i=0; i<extender.length; i++) extender[i] = inData.readByte();
      } catch (EOFException eofe) {} // extensions not required so ignore eof
      if(extender[0] != 0) {
	esize = inData.readInt();
	ecode = inData.readInt();
	// edata = inData.readByte();
	edata = null;
      }
    }
    /**
     * Converts the header key information into a readable string form.
     *
     * @return	String form of header_key information
     */
    public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("sizeof_hdr=").append(sizeof_hdr).append("\n");
      sb.append("data_type=\"").append(CharArrayToString(data_type)).append("\"\n");
      sb.append("db_name=\"").append(CharArrayToString(db_name)).append("\"\n");
      sb.append("extents=").append(extents).append("\n");
      sb.append("session_error=").append(session_error).append("\n");
      sb.append("regular=\'").append(regular).append("\'(0x").append(Integer.toHexString(regular)).append(")\n");
      sb.append("dim_info=\'").append(dim_info).append("\'(0x").append(Integer.toHexString(dim_info)).append(")\n");
      sb.append("  freq_dim=").append(get_freq_dim()).append("\n");
      sb.append("  phase_dim=").append(get_phase_dim()).append("\n");
      sb.append("  slice_dim=").append(get_slice_dim()).append("\n");
      sb.append("dim=").append(ShortArrayToString(dim)).append("\n");
      sb.append("intent_p1=").append(intent_p1).append("\n");
      sb.append("intent_p2=").append(intent_p2).append("\n");
      sb.append("intent_p3=").append(intent_p3).append("\n");
      sb.append("intent_code=").append(intent_code).append("(").append(niftiIntentToString(intent_code)).append(")\n");
      sb.append("datatype=").append(datatype).append("(").append(niftiTypeToString(datatype)).append(")\n");

      sb.append("bitpix=").append(bitpix).append("\n");
      sb.append("slice_start=").append(slice_start).append("\n");
      sb.append("pixdim=").append(FloatArrayToString(pixdim)).append("\n");
      sb.append("vox_offset=").append(vox_offset).append("\n");
      sb.append("scl_slope=").append(scl_slope).append("\n");
      sb.append("scl_inter=").append(scl_inter).append("\n");
      sb.append("slice_end=").append(slice_end).append("\n");
      sb.append("slice_code=\'").append(slice_code).append("\'(").append(niftiSliceCodeToString(slice_code)).append(")\n");
      sb.append("xyzt_units=\'").append(xyzt_units).append("\'(0x").append(Integer.toHexString(xyzt_units)).append(")\n");
      int xyz_units = get_space_units_code();
      sb.append("  xyz_units=\'").append(xyz_units).append("\'(").append(niftiUnitsCodeToString(xyz_units)).append(")\n");
      int t_units = get_time_units_code();
      sb.append("  t_units=\'").append(t_units).append("\'(").append(niftiUnitsCodeToString(t_units)).append(")\n");

      sb.append("cal_max=").append(cal_max).append("\n");
      sb.append("cal_min=").append(cal_min).append("\n");
      sb.append("slice_duration=").append(slice_duration).append("\n");
      sb.append("toffset=").append(toffset).append("\n");
      sb.append("glmax=").append(glmax).append("\n");
      sb.append("glmin=").append(glmin).append("\n");
      sb.append("descrip=\"").append(CharArrayToString(descrip)).append("\"\n");
      sb.append("aux_file=\"").append(CharArrayToString(aux_file)).append("\"\n");
      sb.append("qform_code=").append(qform_code).append("(").append(niftiXFormCodeToString(qform_code)).append(")\n");
      sb.append("sform_code=").append(sform_code).append("(").append(niftiXFormCodeToString(sform_code)).append(")\n");
      sb.append("quatern_b=").append(quatern_b).append("\n");
      sb.append("quatern_c=").append(quatern_c).append("\n");
      sb.append("quatern_d=").append(quatern_d).append("\n");
      sb.append("qoffset_x=").append(qoffset_x).append("\n");
      sb.append("qoffset_y=").append(qoffset_y).append("\n");
      sb.append("qoffset_z=").append(qoffset_z).append("\n");
      sb.append("srow_x=").append(FloatArrayToString(srow_x)).append("\n");
      sb.append("srow_y=").append(FloatArrayToString(srow_y)).append("\n");
      sb.append("srow_z=").append(FloatArrayToString(srow_z)).append("\n");
      sb.append("intent_name=\"").append(CharArrayToString(intent_name)).append("\"\n");
      sb.append("magic=\"").append(CharArrayToString(magic)).append("\"(").append(CharArrayToHex(magic)).append(")\n");
      sb.append("extender=").append(ByteArrayToString(extender)).append("\n");
      if(extender[0] != 0) {
	sb.append("esize=").append(esize).append("\n");
	sb.append("ecode=").append(ecode).append("\n");
      }
      return sb.toString();
    }
  }
  
  /**
   * Converts an char array into a readable string.
   *
   * @param array	the array to be converted
   * @return	String form of short array information
   */
  public static String CharArrayToString(char[] array) {
    String S="";
    for(int i = 0; i < array.length; i++)
      S += array[i];
    return(S);
  }
  /**
   * Converts an char array into a readable hex string.
   *
   * @param array	the array to be converted
   * @return	String form of short array information
   */
  public static String CharArrayToHex(char[] array) {
    String S;
    if(array == null) S="null";
    else {
      S="{";
      S += "0x" + Integer.toHexString(array[0]);
      for(int i = 1; i < array.length; i++)
	S += ",0x" + Integer.toHexString(array[i]);
      S += "}";
    }
    return(S);
  }
  /**
   * Converts an short array into a readable string.
   *
   * @param array	the array to be converted
   * @return	String form of short array information
   */
  public static String ShortArrayToString(short[] array) {
    String S;
    if(array == null) S="null";
    else {
      S="{";
      if(array.length > 0) {
	S += array[0];
	for(int i = 1; i < array.length; i++)
	  S += "," + array[i];
      }
      S+="}";
    }
    return(S);
  }
  /**
   * Converts an float array into a readable string.
   *
   * @param array	the array to be converted
   * @return	String form of float array information
   */
  public static String FloatArrayToString(float[] array) {
    String S;
    if(array == null) S="null";
    else {
      S="{";
      if(array.length > 0) {
	S += array[0];
	for(int i = 1; i < array.length; i++)
	  S += "," + array[i];
      }
      S+="}";
    }
    return(S);
  }
  /**
   * Converts an char array into a readable string.
   *
   * @param array	the array to be converted
   * @return	String form of short array information
   */
  public static String ByteArrayToString(byte[] array) {
    String S;
    if(array == null) S="null";
    else {
      S="{";
      if(array.length > 0) {
	S += array[0];
	for(int i = 1; i < array.length; i++)
	  S += "," + array[i];
      }
      S+="}";
    }
    return(S);
  }
  
}
