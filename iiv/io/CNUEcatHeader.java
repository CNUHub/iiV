package iiv.io;
import iiv.data.*;
import java.io.*;
/**
 * CNUEcatHeader is a class to handle storage and access of ECAT headers.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUEcatImgFile
 */
public class CNUEcatHeader {
  protected byte[] main_hdr = null;
  protected byte[] sub_hdr = null;
  protected headerInfo[] main_hdr_info = ecat_main_header;
  protected headerInfo[] sub_hdr_info = ecat_image_subhdr;
  protected int file_type = 1;
  /**
   *  Sets the main header data block and file_type and
   *  subhdr info based data.
   *
   * @param main_hdr main header data
   */
  public synchronized void setMainHdr(byte[] main_hdr) throws IOException {
    main_hdr_info = ecat_main_header;
    this.main_hdr = main_hdr;
    file_type = getIntValue("file_type");
    switch (file_type) {
    case IMAGE_FILE:
      sub_hdr_info = ecat_image_subhdr;
      break;
    case SCAN_FILE:
      sub_hdr_info = ecat_scan_subhdr;
      break;
    case ATTN_FILE:
      sub_hdr_info = ecat_attn_subhdr;
      break;
    case NORM_FILE:
      sub_hdr_info = ecat_norm_subhdr;
      break;
    default:
      throw new IOException("header block contains invalid ecat file type");
    }
  }
  /**
   * Sets the subheader data.
   *
   * @param sub_hdr	subheader data
   */
  public synchronized void setSubHdr(byte[] sub_hdr) {
    this.sub_hdr = sub_hdr;
  }
  /**
   * Returns the subheader data.
   *
   * @return	subheader data
   */
  public synchronized byte[] getSubHdr() {
    return sub_hdr;
  }
  /**
   * Returns the file type.
   *
   * @return ECAT file type
   */
  public synchronized int getFileType() {
    return file_type;
  }
  /**
   * Returns the subheader info.
   *
   * @return subheader info
   */
  public synchronized headerInfo[] getSubHdrInfo() {
    return sub_hdr_info;
  }
  /**
   * Gets the Ecat dimensions as CNUDimensions.
   *
   * @return dimensions
   */
  public synchronized CNUDimensions getDimensions() {
    if(main_hdr == null || sub_hdr == null) return null;
    int file_type = getIntValue("file_type", main_hdr_info, main_hdr);
    CNUDimensions lsize = new CNUDimensions();
    lsize.set4DValues(
	getIntValue("dimension_1", sub_hdr_info, sub_hdr),
	getIntValue("dimension_2", sub_hdr_info, sub_hdr),
	getIntValue("num_planes", main_hdr_info, main_hdr),
	getIntValue("num_frames", main_hdr_info, main_hdr),
        ecatToCNUType(getIntValue("data_type", main_hdr_info, main_hdr)),
	0);
    double res;
    switch (file_type) {
    default:
    case IMAGE_FILE:
      res = getFloatValue("pixel_size", sub_hdr_info, sub_hdr) * 1e-2;
      break;
    case SCAN_FILE:
    case ATTN_FILE:
      res = getFloatValue("sample_distance", sub_hdr_info, sub_hdr) * 1e-2;
      break;
    case NORM_FILE:
      res = 1.0;
      break;
    }
    lsize.set4DSpatialResolutions(res, res,
	getFloatValue("plane_separation", main_hdr_info, main_hdr) * 1e-2,
	1.0);
    lsize.setOrientationOrder(CNUDimensions.LEFT_POSITIVE |
			      CNUDimensions.POSTERIOR_POSITIVE |
			      CNUDimensions.INFERIOR_POSITIVE);
    return(lsize);
  }
  /**
   * Converts this file type to the string name.
   *
   * @return string name for file type
   */
  public String fileTypeToString() {
    return ecatFileTypeToString(getIntValue("file_type"));
  }
  /**
   * Creates a string representing the ecat header info.
   *
   * @return 	header info as string
   */
  public synchronized String toString() {
    String s = "Ecat file type: " + fileTypeToString() + "\n";
    if(main_hdr != null) {
      s += "main header\n";
      s += headerToString(main_hdr, main_hdr_info);
    }
    if(sub_hdr != null) {
      s += "frame header\n";
      s += headerToString(sub_hdr, sub_hdr_info);
    }
    return s;
  }
  /*
   * Converts a single header byte array into a string representation.
   *
   * @param hdr		header data
   * @param hi		info on how to interpret header data
   * @return		header info as string
   */
  public static String headerToString(byte[] hdr, headerInfo[] hi) {
    String s = "";
    for( int i = 0; i < hi.length; i++ ) {
      s += hi[i].name + " = ";
      int offset = hi[i].byteIndex;
      if(offset < 0) s += "end header flag";
      else switch(hi[i].type) {
      case BYTE_TYPE:
	int asciibytes = 0;
	for(; asciibytes < hi[i].words; asciibytes++) {
	  if(hdr[offset + asciibytes] == 0) break;
	}
	s += new String(hdr, offset, asciibytes);
	break;
      case VAX_I2:
      case SUN_I2:
	for( int j=0; j < hi[i].words; j++, offset += 2) {
	  s += getIntValue(hdr, offset, hi[i].type) + " ";
	}
	break;
      case VAX_I4:
      case SUN_I4:
	for( int j=0; j < hi[i].words; j++, offset += 4) {
	  s += getIntValue(hdr, offset, hi[i].type) + " ";
        }
	break;
      case VAX_R4:
      case SUN_R4:
	for( int j=0; j < hi[i].words; j++, offset += 4) {
	  s += getFloatValue(hdr, offset, hi[i].type) + " ";
        }
	break;
      }
      s += "\n";
    }
    return s;
  }
  /**
   * Gets the string value of a header byte array.
   *
   * @param valueName	name of header value to retrieve
   * @param hi		info on how to interpret header data
   * @param hdr		header data
   * @return 		string value from header
   */
  public static String getStringValue(String valueName,
				      headerInfo[] hi, byte[] hdr)
  {
    for( int i = 0; i < hi.length; i++) {
      if(hi[i].name.equals(valueName)) {
	if(hi[i].type != CNUTypes.BYTE) break;
	return new String(hdr, hi[i].byteIndex, hi[i].words);
      }
    }
    System.err.println("CNUViewer warning coding error - ecat header value not found for " +
      valueName);
    return null;
  }
  /**
   * Gets an int value from a byte array based on main or subhdr.
   *
   * @param valueName	header name of value to get
   * @return		if name is found the integer value for name,
   *			otherwise <code>FALLOUT_INT_VALUE</code>
   */
  public synchronized int getIntValue(String valueName) {
    int value = FALLOUT_INT_VALUE;
    if(main_hdr != null)
	value = getIntValue(valueName, main_hdr_info, main_hdr);
    if((value == FALLOUT_INT_VALUE) && (sub_hdr != null))
	value = getIntValue(valueName, sub_hdr_info, sub_hdr);
    return value;
  }
  /**
   * Gets an int value from a byte array based on headerInfo.
   *
   * @param valueName	header name of value to get
   * @return		if name is found the integer value for name,
   *			otherwise <code>FALLOUT_INT_VALUE</code>
   */
  public static int getIntValue(String valueName,
			        headerInfo[] hi, byte[] hdr) {
    for( int i = 0; i < hi.length; i++) {
      if(hi[i].name.equals(valueName)) {
	return getIntValue(hdr, hi[i].byteIndex, hi[i].type);
      }
    }
    System.err.println("CNUViewer warning coding error - ecat header value not found for " +
      valueName);
    return FALLOUT_INT_VALUE;
  }
  /**
   * Gets an int value from a byte array converting from foreign
   * type if needed.
   *
   * @param hdr		data array containing value
   * @param offset	offset to first byte of value in data array
   * @param type	data type for value in data array
   * @return		value as integer
   */
  public static int getIntValue(byte[] hdr, int offset, int type) {
    int value = 0;
    int[] iByte = new int[4];
    switch(type) {
    case BYTE_TYPE:
      return (int) hdr[offset];
    case VAX_I2:
      // swap order 0,1 -> 1,0
      return (int) CNUDataConversions.getStandardVaxToSunConversions().bytesToShort(hdr, offset);
    case SUN_I2:
      return (int) CNUDataConversions.getStandardNoConversions().bytesToShort(hdr, offset);
    case VAX_I4:
      // swap order 0,1,2,3 -> 3,2,1,0
      return CNUDataConversions.getStandardVaxToSunConversions().bytesToInt(hdr, offset);
    case SUN_I4:
      return CNUDataConversions.getStandardNoConversions().bytesToInt(hdr, offset);
    default:
      return FALLOUT_INT_VALUE;
    }
  }
  /**
   * Gets a float value from a byte array based on headerInfo.
   *
   * @param valueName	name of value to get
   * @param hi		info describing data array
   * @param hdr		data array containing value
   * @return		named value as float
   */
  public static float getFloatValue(String valueName,
	headerInfo[] hi, byte[] hdr) {
    for( int i = 0; i < hi.length; i++) {
      if(hi[i].name.equals(valueName)) {
	return getFloatValue(hdr, hi[i].byteIndex, hi[i].type);
      }
    }
    System.err.println("CNUViewer warning coding error - ecat header value not found for " +
      valueName);
    return FALLOUT_FLOAT_VALUE;
  }
  /**
   * Gets a float value from a byte array converting from foreign type
   * if needed.
   *
   * @param hdr		data array containing value
   * @param offset	offset to first byte of value in data array
   * @param type	data type for value in data array
   * @return		value as float
   */
  public static float getFloatValue(byte[] hdr, int offset, int type) {
    int[] iByte = new int[4];
    switch(type) {
    case VAX_R4:
      // swap  0,1,2,3 -> 1,0,3,2
      return CNUDataConversions.getStandardVaxToSunConversions().bytesToFloat(hdr, offset);
    case SUN_R4:
      return CNUDataConversions.getStandardNoConversions().bytesToFloat(hdr, offset);
    default:
      return FALLOUT_FLOAT_VALUE;
    }
  }
  /**
   * Copy a block of data while converting it from external
   * ecat formats to internal formats.
   *
   * @param inData	data to convert
   * @param inOffset	offset to first byte of data to convert
   * @param inEcatType  type of input data
   * @param outArray	array to contain data of output type
   * @param outOffset	offset to outArray for first word of converted data
   * @param outCNUType	type of data to convert to and store as words
   *			in outArray
   * @param count	number of words to convert
   * @return		offset to next available location in outArray
   */
  public static int copyConvertBlock( byte[] inData, int inOffset,
	int inEcatType, Object outArray, int outOffset, int outCNUType,
	int count) {
    int wordBytes = bytesPerWord(inEcatType);
    int i;
    switch (inEcatType) {
    case SUN_R4:
    case VAX_R4:
      // float types
      for( i = 0; (i < count) && (inOffset < inData.length); i++) {
        double value = getFloatValue(inData, inOffset, inEcatType);
        CNUTypes.setArrayValue(value, outArray, outOffset, outCNUType);
        inOffset += wordBytes;
        outOffset++;
      }
      break;
    default:
      // integer types
      for( i = 0; (i < count) && (inOffset < inData.length); i++) {
        int value = getIntValue(inData, inOffset, inEcatType);
        CNUTypes.setArrayValue(value, outArray, outOffset, outCNUType);
        inOffset += wordBytes;
        outOffset++;
      }
      break;
    }
    return outOffset;
  }
  static int FALLOUT_INT_VALUE = Integer.MIN_VALUE;
  static float FALLOUT_FLOAT_VALUE = Float.NaN;
/* size constants */
  final static int MatBLKSIZE = 512;
  final static int PlanesPerMatBlk = 31;
  final static int MatFirstDirBlk = 2;
  final static int MAXMAT = 8192;
/* ecat file types */
  final static int SCAN_FILE = 1;
  final static int IMAGE_FILE = 2;
  final static int ATTN_FILE = 3;
  final static int NORM_FILE = 4;
  final static String[] ECATFILETYPES = {
   "UNKNOWN_ECAT_FILE_TYPE",
   "SCAN_FILE", "IMAGE_FILE", "ATTN_FILE", "NORM_FILE"
  };
  /**
   * Converts a file type to the string name.
   *
   * @param fileType integer file type
   * @return string name for file type
   */
  public static String ecatFileTypeToString(int fileType) {
    if((fileType < 0) || (fileType > ECATFILETYPES.length))
      return "invalid ECAT file type=" + fileType;
    else return ECATFILETYPES[fileType];
  }
/* ecat data types */
  final static int GENERIC = 0;
  final static int BYTE_TYPE = 1;
  final static int VAX_I2 = 2;
  final static int VAX_I4 = 3;
  final static int VAX_R4 = 4;
  final static int SUN_R4 = 5;
  final static int SUN_I2 = 6;
  final static int SUN_I4 = 7;
  /**
   * Gets the corresponding cnu data type from the ecat data type.
   *
   * @param ecatType	ecat data type
   * @return		cnu data type
   */
  final static public int ecatToCNUType( int ecatType ) {
    switch( ecatType ) {
    case BYTE_TYPE:
      return CNUTypes.BYTE;
    case VAX_I2:
    case SUN_I2:
      return CNUTypes.SHORT;
    case VAX_I4:
    case SUN_I4:
      return CNUTypes.INTEGER;
    case VAX_R4:
    case SUN_R4:
      return CNUTypes.FLOAT;
    default:
      return CNUTypes.UNKNOWN;
    }
  }
  /**
   * Returns number of bytes in one word of a given ecat data type.
   *
   * @param ecatType	ecat data type
   * @return		byte per word
   */
  public final static int bytesPerWord(int ecatType) {
    switch (ecatType) {
    case BYTE_TYPE:
      return(1);
    case VAX_I2:
    case SUN_I2:
      return(2);
    case VAX_I4:
    case SUN_I4:
    case VAX_R4:
    case SUN_R4:
      return(4);
    default:
      return(-1);
    }
  }
  /*
   * ECAT header structures - each should be one block(512 bytes) long.
   */
  final static class headerInfo {
    int byteIndex;
    int type;
    String name;
    int words;
    headerInfo( int byteIndex, int type, String name, int words) {
      this.byteIndex = byteIndex;
      this.type = type;
      this.name = name;
      this.words = words;
    }
  }
  final static headerInfo[] ecat_main_header = {
    new headerInfo(0,VAX_I2,"fill0",14),
    new headerInfo(28,BYTE_TYPE,"original_file_name",20),
    new headerInfo(48,VAX_I2,"sw_version",1),
    new headerInfo(50,VAX_I2,"data_type",1),
    new headerInfo(52,VAX_I2,"system_type",1),
    new headerInfo(54,VAX_I2,"file_type",1),
    new headerInfo(56,BYTE_TYPE,"node_id",10),
    new headerInfo(66,VAX_I2,"scan_start_day",1),
    new headerInfo(68,VAX_I2,"scan_start_month",1),
    new headerInfo(70,VAX_I2,"scan_start_year",1),
    new headerInfo(72,VAX_I2,"scan_start_hour",1),
    new headerInfo(74,VAX_I2,"scan_start_minute",1),
    new headerInfo(76,VAX_I2,"scan_start_second",1),
    new headerInfo(78,BYTE_TYPE,"isotope_code",8),
    new headerInfo(86,VAX_R4,"isotope_halflife",1),
    new headerInfo(90,BYTE_TYPE,"radiopharmaceutical",32),
    new headerInfo(122,VAX_R4,"gantry_bed_stuff",3),
    new headerInfo(134,VAX_I2,"mcs_stuff",3),
    new headerInfo(140,VAX_R4,"axial_fov",1),
    new headerInfo(144,VAX_R4,"transaxial_fov",1),
    new headerInfo(148,VAX_I2,"sampling_stuff",3),
    new headerInfo(154,VAX_R4,"calibration_factor",1),
    new headerInfo(158,VAX_I2,"calibration_units",1),
    new headerInfo(160,VAX_I2,"compression_code",1),
    new headerInfo(162,BYTE_TYPE,"study_name",12),
    new headerInfo(174,BYTE_TYPE,"patient_id",16),
    new headerInfo(190,BYTE_TYPE,"patient_name",32),
    new headerInfo(222,BYTE_TYPE,"patient_sex",1),
    new headerInfo(223,BYTE_TYPE,"patient_age",10),
    new headerInfo(233,BYTE_TYPE,"patient_height",10),
    new headerInfo(243,BYTE_TYPE,"patient_weight",10),
    new headerInfo(253,BYTE_TYPE,"patient_dexterity",1),
    new headerInfo(254,BYTE_TYPE,"physician_name",32),
    new headerInfo(286,BYTE_TYPE,"operator_name",32),
    new headerInfo(318,BYTE_TYPE,"study_description",32),
    new headerInfo(350,VAX_I2,"acquisition_type",1),
    new headerInfo(352,VAX_I2,"bed_type",1),
    new headerInfo(354,VAX_I2,"septa_type",1),
    new headerInfo(356,BYTE_TYPE,"facility_name",20),
    new headerInfo(376,VAX_I2,"num_planes",1),
    new headerInfo(378,VAX_I2,"num_frames",1),
    new headerInfo(380,VAX_I2,"num_gates",1),
    new headerInfo(382,VAX_I2,"num_bed_pos",1),
    new headerInfo(384,VAX_R4,"init_bed_position",1),
    new headerInfo(388,VAX_R4,"bed_offset",15),
    new headerInfo(448,VAX_R4,"plane_separation",1),
    new headerInfo(452,VAX_I2,"lwr_sctr_thres",1),
    new headerInfo(454,VAX_I2,"lwr_true_thres",1),
    new headerInfo(456,VAX_I2,"upr_true_thres",1),
    new headerInfo(458,VAX_R4,"collimator",1),
    new headerInfo(462,BYTE_TYPE,"user_process_code",10),
    new headerInfo(472,VAX_I2,"acquisition_mode",1),
    new headerInfo(474,VAX_I2,"fill1",19),
    new headerInfo(-1,BYTE_TYPE,"",0)
  };
  /*
    ; gantry_bed_stuff consists of:
    ;	gantry_tilt, gantry_rotation, bed_elevation
    ; mcs_stuff consists of:
    ;	rot_source_speed, wobble_speed, transm_source_type
    ; sampling_stuff consists of:
    ;	transaxial_samp_mode, coin_samp_mode, axial_samp_mode
    */
  final static headerInfo[] ecat_image_subhdr = {
    new headerInfo(0,VAX_I2,"fill0",63),
    new headerInfo(126,VAX_I2,"data_type",1),
    new headerInfo(128,VAX_I2,"num_dimensions",1),
    new headerInfo(130,VAX_I2,"fill1",1),
    new headerInfo(132,VAX_I2,"dimension_1",1),
    new headerInfo(134,VAX_I2,"dimension_2",1),
    new headerInfo(136,VAX_I2,"fill2",12),
    new headerInfo(160,VAX_R4,"x_origin",1),
    new headerInfo(164,VAX_R4,"y_origin",1),
    new headerInfo(168,VAX_R4,"recon_scale",1),
    new headerInfo(172,VAX_R4,"quant_scale",1),
    new headerInfo(176,VAX_I2,"image_min",1),
    new headerInfo(178,VAX_I2,"image_max",1),
    new headerInfo(180,VAX_I2,"fill3",2),
    new headerInfo(184,VAX_R4,"pixel_size",1),
    new headerInfo(188,VAX_R4,"slice_width",1),
    new headerInfo(192,VAX_I4,"frame_duration",1),
    new headerInfo(196,VAX_I4,"frame_start_time",1),
    new headerInfo(200,VAX_I2,"slice_location",1),
    new headerInfo(202,VAX_I2,"recon_start_hour",1),
    new headerInfo(204,VAX_I2,"recon_start_minute",1),
    new headerInfo(206,VAX_I2,"recon_start_sec",1),
    new headerInfo(208,VAX_I4,"recon_duration",1),
    new headerInfo(212,VAX_I2,"fill4",12),
    new headerInfo(236,VAX_I2,"filter_code",1),
    new headerInfo(238,VAX_I4,"scan_matrix_num",1),
    new headerInfo(242,VAX_I4,"norm_matrix_num",1),
    new headerInfo(246,VAX_I4,"atten_cor_matrix_num",1),
    new headerInfo(250,VAX_I2,"fill5",23),
    new headerInfo(296,VAX_R4,"image_rotation",1),
    new headerInfo(300,VAX_R4,"plane_eff_corr_fctr",1),
    new headerInfo(304,VAX_R4,"decay_corr_fctr",1),
    new headerInfo(308,VAX_R4,"loss_corr_fctr",1),
    new headerInfo(312,VAX_R4,"intrinsic_tilt",1),
    new headerInfo(316,VAX_I2,"fill6",30),
    new headerInfo(376,VAX_I2,"processing_code",1),
    new headerInfo(378,VAX_I2,"fill7",1),
    new headerInfo(380,VAX_I2,"quant_units",1),
    new headerInfo(382,VAX_I2,"recon_start_day",1),
    new headerInfo(384,VAX_I2,"recon_start_month",1),
    new headerInfo(386,VAX_I2,"recon_start_year",1),
    new headerInfo(388,VAX_R4,"ecat_calibration_fctr",1),
    new headerInfo(392,VAX_R4,"well_counter_cal_fctr",1),
    new headerInfo(396,VAX_R4,"filter_params",6),
    new headerInfo(420,BYTE_TYPE,"annotation",40),
    new headerInfo(460,BYTE_TYPE,"fill8",26),
    new headerInfo(-1,BYTE_TYPE,"",0)
  };
  final static headerInfo[] ecat_scan_subhdr = {
    new headerInfo(0,VAX_I2,"fill0",63),
    new headerInfo(126,VAX_I2,"data_type",1),
    new headerInfo(128,VAX_I2,"fill1",2),
    new headerInfo(132,VAX_I2,"dimension_1",1),
    new headerInfo(134,VAX_I2,"dimension_2",1),
    new headerInfo(136,VAX_I2,"smoothing",1),
    new headerInfo(138,VAX_I2,"processing_code",1),
    new headerInfo(140,VAX_I2,"fill2",3),
    new headerInfo(146,VAX_R4,"sample_distance",1),
    new headerInfo(150,VAX_I2,"fill3",8),
    new headerInfo(166,VAX_R4,"isotope_halflife",1),
    new headerInfo(170,VAX_I2,"frame_duration_sec",1),
    new headerInfo(172,VAX_I4,"gate_duration",1),
    new headerInfo(176,VAX_I4,"r_wave_offset",1),
    new headerInfo(180,VAX_I2,"fill4",1),
    new headerInfo(182,VAX_R4,"scale_factor",1),
    new headerInfo(186,VAX_I2,"fill5",3),
    new headerInfo(192,VAX_I2,"scan_min",1),
    new headerInfo(194,VAX_I2,"scan_max",1),
    new headerInfo(196,VAX_I4,"prompts",1),
    new headerInfo(200,VAX_I4,"delayed",1),
    new headerInfo(204,VAX_I4,"multiples",1),
    new headerInfo(208,VAX_I4,"net_trues",1),
    new headerInfo(212,VAX_I2,"fill6",52),
    new headerInfo(316,VAX_R4,"cor_singles",16),
    new headerInfo(380,VAX_R4,"uncor_singles",16),
    new headerInfo(444,VAX_R4,"tot_avg_cor",1),
    new headerInfo(448,VAX_R4,"tot_avg_uncor",1),
    new headerInfo(452,VAX_I4,"total_coin_rate",1),
    new headerInfo(456,VAX_I4,"frame_start_time",1),
    new headerInfo(460,VAX_I4,"frame_duration",1),
    new headerInfo(464,VAX_R4,"loss_correction_fctr",1),
    new headerInfo(468,VAX_I4,"phy_planes",8),
    new headerInfo(500,VAX_I2,"fill7",6),
    new headerInfo(-1,BYTE_TYPE,"",0)
  };
  final static headerInfo[] ecat_norm_subhdr = {
    new headerInfo(0,VAX_I2,"fill0",63),
    new headerInfo(126,VAX_I2,"data_type",1),
    new headerInfo(128,VAX_I2,"fill1",2),
    new headerInfo(132,VAX_I2,"dimension_1",1),
    new headerInfo(134,VAX_I2,"dimension_2",1),
    new headerInfo(136,VAX_I2,"fill2",23),
    new headerInfo(182,VAX_R4,"scale_factor",1),
    new headerInfo(186,VAX_I2,"norm_hour",1),
    new headerInfo(188,VAX_I2,"norm_minute",1),
    new headerInfo(190,VAX_I2,"norm_second",1),
    new headerInfo(192,VAX_I2,"norm_day",1),
    new headerInfo(194,VAX_I2,"norm_month",1),
    new headerInfo(196,VAX_I2,"norm_year",1),
    new headerInfo(198,VAX_R4,"fov_source_width",1),
    new headerInfo(202,VAX_R4,"ecat_calib_factor",1),
    new headerInfo(206,VAX_I2,"fill3",153),
    new headerInfo(-1,BYTE_TYPE,"",0)
  };
  final static headerInfo[] ecat_attn_subhdr = {
    new headerInfo(0,VAX_I2,"fill0",63),
    new headerInfo(126,VAX_I2,"data_type",1),
    new headerInfo(128,VAX_I2,"attenuation_type",1),
    new headerInfo(130,VAX_I2,"fill1",1),
    new headerInfo(132,VAX_I2,"dimension_1",1),
    new headerInfo(134,VAX_I2,"dimension_2",1),
    new headerInfo(136,VAX_I2,"fill2",23),
    new headerInfo(182,VAX_R4,"scale_factor",1),
    new headerInfo(186,VAX_R4,"x_origin",1),
    new headerInfo(190,VAX_R4,"y_origin",1),
    new headerInfo(194,VAX_R4,"x_radius",1),
    new headerInfo(198,VAX_R4,"y_radius",1),
    new headerInfo(202,VAX_R4,"tilt_angle",1),
    new headerInfo(206,VAX_R4,"attenuation_coeff",1),
    new headerInfo(210,VAX_R4,"sample_distance",1),
    new headerInfo(214,VAX_I2,"fill3",149),
    new headerInfo(-1,BYTE_TYPE,"",0)
  };
}
