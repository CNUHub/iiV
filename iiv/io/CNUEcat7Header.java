package iiv.io;
import iiv.data.*;
import java.io.*;
/**
 * CNUEcat7Header is a class to handle storage and access of ECAT 7 headers.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUEcatImgFile
 */
public class CNUEcat7Header extends CNUEcatHeader {
  private CNUDimensions dims = null;
  private int dataOffset = 1536;
  private double quantificationFactor = 1.0;

  /** ECAT 7 file types */
  final static int UNKNOWN_FILE_TYPE = 0;
  final static int ECAT7_SINOGRAM = 1;
  final static int ECAT7_IMAGE_16 = 2;
  final static int ECAT7_ATTENUATION_CORRECTION = 3;
  final static int ECAT7_NORMALIZATION = 4;
  final static int ECAT7_POLAR_MAP = 5;
  final static int ECAT7_VOLUME_8 = 6;
  final static int ECAT7_VOLUME_16 = 7;
  final static int ECAT7_PROJECTION_8 = 8;
  final static int ECAT7_PROJECTION_16 = 9;
  final static int ECAT7_IMAGE_8 = 10;
  final static int ECAT7_3D_SINOGRAM_16 = 11;
  final static int ECAT7_3D_SINOGRAM_8 = 12;
  final static int ECAT7_3D_SINOGRAM_NORMALIZATION = 13;
  final static int ECAT7_3D_SINOGRAM_FIT = 14;
  private static String[] ECAT7FILETYPES = {
    "UNKNOWN_ECAT7_FILE_TYPE", "ECAT7_SINOGRAM", "ECAT7_IMAGE_16",
    "ECAT7_ATTENUATION_CORRECTION", "ECAT7_NORMALIZATION",
    "ECAT7_POLAR_MAP", "ECAT7_VOLUME_8", "ECAT7_VOLUME_16",
    "ECAT7_PROJECTION_8", "ECAT7_PROJECTION_16", "ECAT7_IMAGE_8",
    "ECAT7_3D_SINOGRAM_16", "ECAT7_3D_SINOGRAM_8",
    "ECAT7_3D_SINOGRAM_NORMALIZATION", "ECAT7_3D_SINOGRAM_FIT"
  };
  /**
   * Converts a file type to the string name.
   *
   * @param fileType integer file type
   * @return string name for file type
   */
  public static String ecat7FileTypeToString(int fileType) {
    if((fileType < 0) || (fileType > ECAT7FILETYPES.length))
      return "invalid ECAT 7 file type=" + fileType;
    else return ECAT7FILETYPES[fileType];
  }
  /**
   * Read header information from a file.
   *
   * @return header information
   * @exception  IOException thrown on error reading file or wrong file type
   */
  public static CNUEcat7Header read(String filename) throws IOException {
    CNUEcat7Header ecatHeader = null;
    InputStream inS = null;
    try {
      CNUFile cnufile = new CNUFile(filename);
      inS = cnufile.getInputStream();
      ecatHeader = new CNUEcat7Header();
      // read 1024 instead of 512 because subheader starts at 1024?
      ecatHeader.setMainHdr(CNUEcatImgFile.readBlock(inS, null, 0, 1024));
      ecatHeader.setSubHdr(CNUEcatImgFile.readBlock(inS, null, 0, 512));
    } finally {
      if(inS != null) inS.close();
    }
    return ecatHeader;
  }
  /**
   * Converts this file type to the string name.
   *
   * @return string name for file type
   */
  public String fileTypeToString() {
    return ecat7FileTypeToString(getIntValue("file_type"));
  }
  /**
   *  Sets the main header data block and file_type and
   *  subhdr info based data.
   *
   * @param main_hdr main header data
   * @exception  IOException thrown if invalid header data
   */
  public synchronized void setMainHdr(byte[] main_hdr) throws IOException {
    main_hdr_info = ecat7_main_header;
    this.main_hdr = main_hdr;
    String magic_number =
      getStringValue("magic_number", main_hdr_info, main_hdr);
    if(! magic_number.startsWith("MATRIX"))
      throw new IOException("invalid ecat 7 magic number");
    file_type = getIntValue("file_type");
    switch (file_type) {
    case ECAT7_IMAGE_16:
    case ECAT7_IMAGE_8:
    case ECAT7_ATTENUATION_CORRECTION:
    case ECAT7_NORMALIZATION:
    case ECAT7_POLAR_MAP:
    case ECAT7_PROJECTION_8:
    case ECAT7_PROJECTION_16:
    case ECAT7_3D_SINOGRAM_16:
    case ECAT7_3D_SINOGRAM_8:
    case ECAT7_3D_SINOGRAM_NORMALIZATION:
    case ECAT7_3D_SINOGRAM_FIT:
    case ECAT7_VOLUME_8:
    case ECAT7_VOLUME_16:
    case ECAT7_SINOGRAM:
      break;
    case UNKNOWN_FILE_TYPE:
      throw new IOException("header block contains unknown ecat 7 file type");
    default:
      throw new IOException("header block contains invalid ecat 7 file type="+file_type);
    }
  }
/*
ecat7_imported_6_5_scan_subheader
ecat7_imported_6_5_norm_subheader
*/
  /**
   * Sets the subheader data.
   *
   * @param sub_hdr	subheader data
   */
  public synchronized void setSubHdr(byte[] sub_hdr) {
    this.sub_hdr = sub_hdr;
    int xdim = 0;
    int ydim = 0;
    int zdim = getIntValue("num_planes", main_hdr_info, main_hdr);
    int idim = getIntValue("num_frames", main_hdr_info, main_hdr);
    int cnuDataType = 0;

    double xres = 1.0;
    double yres = 1.0;
    double zres = getFloatValue("plane_separation", main_hdr_info, main_hdr) * 1e-2;
    double ires = 1.0;

    file_type = getIntValue("file_type");
    switch (file_type) {
    default:
    case UNKNOWN_FILE_TYPE:
      System.out.println("header block contains invalid ecat 7 file type");
    case ECAT7_VOLUME_8:
    case ECAT7_VOLUME_16:
    case ECAT7_IMAGE_16:
    case ECAT7_IMAGE_8:
    case ECAT7_PROJECTION_8:
    case ECAT7_PROJECTION_16:
      sub_hdr_info = ecat7_image_subheader;
      xdim = getIntValue("x_dimension", sub_hdr_info, sub_hdr);
      ydim = getIntValue("y_dimension", sub_hdr_info, sub_hdr);
      zdim = Math.max(zdim, getIntValue("z_dimension", sub_hdr_info, sub_hdr));
      xres = getFloatValue("x_pixel_size", sub_hdr_info, sub_hdr) * 1e-2;
      yres = getFloatValue("y_pixel_size", sub_hdr_info, sub_hdr) * 1e-2;
      quantificationFactor = getFloatValue("scale_factor", sub_hdr_info, sub_hdr);
      break;
    case ECAT7_ATTENUATION_CORRECTION:
      sub_hdr_info = ecat7_attenuation_subheader;
      xdim = getIntValue("num_r_elements", sub_hdr_info, sub_hdr);
      ydim = getIntValue("num_angles", sub_hdr_info, sub_hdr);
      zdim = Math.max(zdim, getIntValue("num_z_elements", sub_hdr_info, sub_hdr));
      xres = getFloatValue("x_resolution", sub_hdr_info, sub_hdr) * 1e-2;
      yres = getFloatValue("y_resolution", sub_hdr_info, sub_hdr) * 1e-2;
      quantificationFactor = getFloatValue("scale_factor", sub_hdr_info, sub_hdr);
      break;
    case ECAT7_NORMALIZATION:
    case ECAT7_3D_SINOGRAM_NORMALIZATION:
      sub_hdr_info = ecat7_3d_norm_subheader;
      ydim = getIntValue("num_r_elements", sub_hdr_info, sub_hdr);
      xdim = getIntValue("num_crystal_rings", sub_hdr_info, sub_hdr);
      xdim *= getIntValue("crystals_per_ring", sub_hdr_info, sub_hdr);
      break;
    case ECAT7_POLAR_MAP:
      sub_hdr_info = ecat7_polar_map_subheader;
      // i don't know sizes
      xdim = getIntValue("num_rings", sub_hdr_info, sub_hdr);
//      xdim *= getIntValue("sectors_per_ring", sub_hdr_info, sub_hdr);
      ydim = 1;
      xres = getFloatValue("pixel_size", sub_hdr_info, sub_hdr) * 1e-2;
      yres = xres;
      quantificationFactor = getFloatValue("scale_factor", sub_hdr_info, sub_hdr);
      break;
    case ECAT7_SINOGRAM:
    case ECAT7_3D_SINOGRAM_16:
    case ECAT7_3D_SINOGRAM_8:
    case ECAT7_3D_SINOGRAM_FIT:
      sub_hdr_info = ecat7_3d_scan_subheader;
      xdim = getIntValue("num_r_elements", sub_hdr_info, sub_hdr);
      ydim = getIntValue("num_angles", sub_hdr_info, sub_hdr);
      zdim = Math.max(zdim, getIntValue("first_num_z_elements", sub_hdr_info, sub_hdr));
      xres = getFloatValue("x_resolution", sub_hdr_info, sub_hdr) * 1e-2;
      yres = getFloatValue("v_resolution", sub_hdr_info, sub_hdr);
      quantificationFactor = getFloatValue("scale_factor", sub_hdr_info, sub_hdr);
      int storage_order = getIntValue("storage_order", sub_hdr_info, sub_hdr);
      if(storage_order != 0) {
	// swap z and angle?
      }
      break;
    }
    cnuDataType =
       ecatToCNUType(getIntValue("data_type", sub_hdr_info, sub_hdr));
    dims = new CNUDimensions();
    dims.set4DValues(xdim, ydim, zdim, idim, cnuDataType, 0);
    dims.set4DSpatialResolutions(xres, yres, zres, ires);
    dims.setOrientationOrder(CNUDimensions.LEFT_POSITIVE |
			     CNUDimensions.POSTERIOR_POSITIVE |
			     CNUDimensions.INFERIOR_POSITIVE);
  }
  /**
   * Gets the conversion needed to transform the file type into
   * internal data..
   *
   * @return data conversion
   */
  public synchronized CNUDataConversions getDataConversions() {
    if(main_hdr == null) return null;
    int data_type = getIntValue("data_type", sub_hdr_info, sub_hdr);
    int internal_type = ecatToCNUType(data_type);
    int convertType = CNUConversionTypes.NO_CONVERSION;
    switch(data_type) {
    case VAX_I2:
    case VAX_I4:
    case VAX_R4:
      convertType=CNUConversionTypes.VAX_TO_SUN;
      break;
    default:
      break;
    }
    CNUDataConversions dataConversions = new CNUDataConversions();
    dataConversions.setConvert(data_type, convertType);
    return dataConversions;
  }
  /**
   * Gets the Ecat dimensions as CNUDimensions.
   *
   * @return dimensions
   */
  public synchronized CNUDimensions getDimensions() {
    if(dims == null) return null;
    else return (CNUDimensions) dims.clone();
  }
  /**
   * Gets the data offset.
   *
   * @return offset
   */
  public int getDataOffset() { return dataOffset; }
  /**
   * Gets the quantifcation factor.
   *
   * @return quantificationFactor
   */
  public double getQuantificationFactor() { return quantificationFactor; }

  final static headerInfo[] ecat7_main_header = {
    new headerInfo(0,BYTE_TYPE,"magic_number",14),
    new headerInfo(14,BYTE_TYPE,"original_file_name",32),
    new headerInfo(46,SUN_I2,"sw_version",1),
    new headerInfo(48,SUN_I2,"system_type",1),
    new headerInfo(50,SUN_I2,"file_type",1),
    new headerInfo(52,BYTE_TYPE,"serial_number",10),
    new headerInfo(62,SUN_I4,"SCAN_START_TIME",1),
    new headerInfo(66,BYTE_TYPE,"isotype_name",8),
    new headerInfo(74,SUN_R4,"isotope_halflife",1),
    new headerInfo(78,BYTE_TYPE,"radiopharmeceutical",32),
    new headerInfo(110,SUN_R4,"gantry_tilt",1),
    new headerInfo(114,SUN_R4,"gantry_rotation",1),
    new headerInfo(118,SUN_R4,"bed_elevation",1),
    new headerInfo(122,SUN_R4,"intrinsic_tilt",1),
    new headerInfo(126,SUN_I2,"wobble_speed",1),
    new headerInfo(128,SUN_I2,"transm_source_type",1),
    new headerInfo(130,SUN_R4,"distance_scanned",1),
    new headerInfo(134,SUN_R4,"transaxial_fov",1),
    new headerInfo(138,SUN_I2,"angular_compression",1),
    new headerInfo(140,SUN_I2,"coin_samp_mode",1),
    new headerInfo(142,SUN_I2,"axial_samp_mode",1),
    new headerInfo(144,SUN_R4,"ecat_calibration_factor",1),
    new headerInfo(148,SUN_I2,"calibration_units",1),
    new headerInfo(150,SUN_I2,"calibration_units_label",1),
    new headerInfo(152,SUN_I2,"compression_code",1),
    new headerInfo(154,BYTE_TYPE,"study_type",12),
    new headerInfo(166,BYTE_TYPE,"patient_id",16),
    new headerInfo(182,BYTE_TYPE,"patient_name",32),
    new headerInfo(214,BYTE_TYPE,"patient_sex",1),
    new headerInfo(215,BYTE_TYPE,"patient_dexterity",1),
    new headerInfo(216,SUN_R4,"patient_age",1),
    new headerInfo(220,SUN_R4,"patient_height",1),
    new headerInfo(224,SUN_R4,"patient_weight",1),
    new headerInfo(228,SUN_I4,"patient_birth_date",1),
    new headerInfo(232,BYTE_TYPE,"physician_name",32),
    new headerInfo(264,BYTE_TYPE,"operator_name",32),
    new headerInfo(296,BYTE_TYPE,"study_description",32),
    new headerInfo(328,SUN_I2,"acquisition_type",1),
    new headerInfo(330,SUN_I2,"patient_orientation",1),
    new headerInfo(332,BYTE_TYPE,"facility_name",20),
    new headerInfo(352,SUN_I2,"num_planes",1),
    new headerInfo(354,SUN_I2,"num_frames",1),
    new headerInfo(356,SUN_I2,"num_gates",1),
    new headerInfo(358,SUN_I2,"num_bed_pos",1),
    new headerInfo(360,SUN_R4,"init_bed_position",1),
    new headerInfo(364,SUN_R4,"bed_position",15),
    new headerInfo(424,SUN_R4,"plane_separation",1),
    new headerInfo(428,SUN_I2,"lwr_sctr_thres",1),
    new headerInfo(430,SUN_I2,"lwr_true_thres",1),
    new headerInfo(432,SUN_I2,"upr_true_thres",1),
    new headerInfo(434,BYTE_TYPE,"user_process_code",10),
    new headerInfo(444,SUN_I2,"acquisition_mode",1),
    new headerInfo(446,SUN_R4,"bin_size",1),
    new headerInfo(450,SUN_R4,"branching_fraction",1),
    new headerInfo(454,SUN_I4,"dose_start_time",1),
    new headerInfo(458,SUN_R4,"dosage",1),
    new headerInfo(462,SUN_R4,"well_counter_corr_factor",1),
    new headerInfo(466,BYTE_TYPE,"date_units",32),
    new headerInfo(498,SUN_I2,"septa_state",1),
    new headerInfo(500,SUN_I2,"fill",6),
    new headerInfo(-1,BYTE_TYPE,"",0)
  };
  final static headerInfo[] ecat7_attenuation_subheader = {
    new headerInfo(0,SUN_I2,"data_type",1),
    new headerInfo(2,SUN_I2,"num_dimensions",1),
    new headerInfo(4,SUN_I2,"attenuation_type",1),
    new headerInfo(6,SUN_I2,"num_r_elements",1),
    new headerInfo(8,SUN_I2,"num_angles",1),
    new headerInfo(10,SUN_I2,"num_z_elements",1),
    new headerInfo(12,SUN_I2,"ring_difference",1),
    new headerInfo(14,SUN_R4,"x_resolution",1),
    new headerInfo(18,SUN_R4,"y_resolution",1),
    new headerInfo(22,SUN_R4,"z_resolution",1),
    new headerInfo(26,SUN_R4,"w_resolution",1),
    new headerInfo(30,SUN_R4,"scale_factor",1),
    new headerInfo(34,SUN_R4,"x_offset",1),
    new headerInfo(38,SUN_R4,"y_offset",1),
    new headerInfo(42,SUN_R4,"x-radius",1),
    new headerInfo(46,SUN_R4,"y_radius",1),
    new headerInfo(50,SUN_R4,"tilt_angle",1),
    new headerInfo(54,SUN_R4,"attenuation_coeff",1),
    new headerInfo(58,SUN_R4,"attenuation_min",1),
    new headerInfo(62,SUN_R4,"attenuation_max",1),
    new headerInfo(66,SUN_R4,"skull_thickness",1),
    new headerInfo(70,SUN_I2,"num_additional_atten_coeff",1),
    new headerInfo(72,SUN_R4,"additional_atten_coeff",8),
    new headerInfo(104,SUN_R4,"edge_finding_threshold",1),
    new headerInfo(108,SUN_I2,"storage_order",1),
    new headerInfo(110,SUN_I2,"span",1),
    new headerInfo(112,SUN_I2,"z_elements",64),
    new headerInfo(240,SUN_I2,"fill1",86),
    new headerInfo(412,SUN_I2,"fill2",50),
    new headerInfo(-1,BYTE_TYPE,"",0)
  };
  final static headerInfo[] ecat7_image_subheader = {
    new headerInfo(0,SUN_I2,"data_type",1),
    new headerInfo(2,SUN_I2,"num_dimensions",1),
    new headerInfo(4,SUN_I2,"x_dimension",1),
    new headerInfo(6,SUN_I2,"y_dimension",1),
    new headerInfo(8,SUN_I2,"z_dimension",1),
    new headerInfo(10,SUN_R4,"x_offset",1),
    new headerInfo(14,SUN_R4,"y_offset",1),
    new headerInfo(18,SUN_R4,"z_offset",1),
    new headerInfo(22,SUN_R4,"recon_zoom",1),
    new headerInfo(26,SUN_R4,"scale_factor",1),
    new headerInfo(30,SUN_I2,"image_min",1),
    new headerInfo(32,SUN_I2,"image_max",1),
    new headerInfo(34,SUN_R4,"x_pixel_size",1),
    new headerInfo(38,SUN_R4,"y_pixel_size",1),
    new headerInfo(42,SUN_R4,"z_pixel_size",1),
    new headerInfo(46,SUN_I4,"frame_duration",1),
    new headerInfo(50,SUN_I4,"frame_start_time",1),
    new headerInfo(54,SUN_I2,"filter_code",1),
    new headerInfo(56,SUN_R4,"x_resolution",1),
    new headerInfo(60,SUN_R4,"y_resolution",1),
    new headerInfo(64,SUN_R4,"z_resolution",1),
    new headerInfo(68,SUN_R4,"num_r_elements",1),
    new headerInfo(72,SUN_R4,"num_angles",1),
    new headerInfo(76,SUN_R4,"z_rotation_angle",1),
    new headerInfo(80,SUN_R4,"decay_corr_fctr",1),
    new headerInfo(84,SUN_I4,"processing_code",1),
    new headerInfo(88,SUN_I4,"gate_duration",1),
    new headerInfo(92,SUN_I4,"r_wave_offset",1),
    new headerInfo(96,SUN_I4,"num_accepted_beats",1),
    new headerInfo(100,SUN_R4,"filter_cutoff_freouency",1),
    new headerInfo(104,SUN_R4,"filter_resolution",1),
    new headerInfo(108,SUN_R4,"filter_ramp_slope",1),
    new headerInfo(112,SUN_I2,"filter_order",1),
    new headerInfo(114,SUN_R4,"filter_scatter_fraction",1),
    new headerInfo(118,SUN_R4,"filter_scatter_slope",1),
    new headerInfo(122,BYTE_TYPE,"annotation",40),
    new headerInfo(162,SUN_R4,"mt_1_1",1),
    new headerInfo(166,SUN_R4,"mt_1_2",1),
    new headerInfo(170,SUN_R4,"mt_1_3",1),
    new headerInfo(174,SUN_R4,"mt_2_1",1),
    new headerInfo(178,SUN_R4,"mt_2_2",1),
    new headerInfo(182,SUN_R4,"mt_2_3",1),
    new headerInfo(186,SUN_R4,"mt_3_1",1),
    new headerInfo(190,SUN_R4,"mt_3_2",1),
    new headerInfo(194,SUN_R4,"mt_3_3",1),
    new headerInfo(198,SUN_R4,"rfilter_cutoff",1),
    new headerInfo(202,SUN_R4,"rfilter_resolution",1),
    new headerInfo(206,SUN_I2,"rfilter_code",1),
    new headerInfo(208,SUN_I2,"rfilter_order",1),
    new headerInfo(210,SUN_R4,"zfilter_cutoff",1),
    new headerInfo(214,SUN_R4,"zfilter_resolution",1),
    new headerInfo(218,SUN_I2,"zfilter_code",1),
    new headerInfo(220,SUN_I2,"zfilter_order",1),
    new headerInfo(222,SUN_R4,"mt_1_4",1),
    new headerInfo(226,SUN_R4,"mt_2_4",1),
    new headerInfo(230,SUN_R4,"mt_3_4",1),
    new headerInfo(234,SUN_I2,"scatter_type",1),
    new headerInfo(236,SUN_I2,"recon_type",1),
    new headerInfo(238,SUN_I2,"recon_views",1),
    new headerInfo(240,SUN_I2,"fill1",87),
    new headerInfo(414,SUN_I2,"fill2",49),
    new headerInfo(-1,BYTE_TYPE,"",0)
  };
  final static headerInfo[] ecat7_polar_map_subheader = {
    new headerInfo(0,SUN_I2,"data_type",1),
    new headerInfo(2,SUN_I2,"polar_map_type",1),
    new headerInfo(4,SUN_I2,"num_rings",1),
    new headerInfo(6,SUN_I2,"sectors_per_ring",32),
    new headerInfo(70,SUN_R4,"ring_position",32),
    new headerInfo(198,SUN_I2,"ring_angle",32),
    new headerInfo(262,SUN_I2,"start_angle",1),
    new headerInfo(264,SUN_I2,"long_axis_left",3),
    new headerInfo(270,SUN_I2,"long_axis_right",3),
    new headerInfo(276,SUN_I2,"position_data",1),
    new headerInfo(278,SUN_I2,"image_min",1),
    new headerInfo(280,SUN_I2,"image_max",1),
    new headerInfo(282,SUN_R4,"scale_factor",1),
    new headerInfo(286,SUN_R4,"pixel_size",1),
    new headerInfo(290,SUN_I4,"frame_duration",1),
    new headerInfo(294,SUN_I4,"frame_start_time",1),
    new headerInfo(298,SUN_I2,"processing_code",1),
    new headerInfo(300,SUN_I2,"quant_units",1),
    new headerInfo(302,BYTE_TYPE,"annotation",40),
    new headerInfo(342,SUN_I4,"gate_duration",1),
    new headerInfo(346,SUN_I4,"r_wave_offset",1),
    new headerInfo(350,SUN_I4,"num_accepted_beats",1),
    new headerInfo(354,BYTE_TYPE,"polar_map_protocol",20),
    new headerInfo(374,BYTE_TYPE,"database_name",30),
    new headerInfo(404,SUN_I2,"fill1",27),
    new headerInfo(458,SUN_I2,"fill2",27),
    new headerInfo(-1,BYTE_TYPE,"",0)
  };
  final static headerInfo[] ecat7_3d_scan_subheader = {
    new headerInfo(0,SUN_I2,"data_type",1),
    new headerInfo(2,SUN_I2,"num_dimensions",1),
    new headerInfo(4,SUN_I2,"num_r_elements",1),
    new headerInfo(6,SUN_I2,"num_angles",1),
    new headerInfo(8,SUN_I2,"corrections_applied",1),
    new headerInfo(10,SUN_I2,"first_num_z_elements",1),
    new headerInfo(10,SUN_I2,"num_z_elements",64),
    new headerInfo(138,SUN_I2,"ring_difference",1),
    new headerInfo(140,SUN_I2,"storage_order",1),
    new headerInfo(142,SUN_I2,"axial_compression",1),
    new headerInfo(144,SUN_R4,"x_resolution",1),
    new headerInfo(148,SUN_R4,"v_resolution",1),
    new headerInfo(152,SUN_R4,"z_resolution",1),
    new headerInfo(156,SUN_R4,"w_resolution",1),
    new headerInfo(160,SUN_I2,"fill1",6),
    new headerInfo(172,SUN_I4,"gate_duration",1),
    new headerInfo(176,SUN_I4,"r_wave_offset",1),
    new headerInfo(180,SUN_I4,"num_accepted_beats",1),
    new headerInfo(184,SUN_R4,"scale_factor",1),
    new headerInfo(188,SUN_I2,"scan_min",1),
    new headerInfo(190,SUN_I2,"scan_max",1),
    new headerInfo(192,SUN_I4,"prompts",1),
    new headerInfo(196,SUN_I4,"delayed",1),
    new headerInfo(200,SUN_I4,"multiples",1),
    new headerInfo(204,SUN_I4,"net_trues",1),
    new headerInfo(208,SUN_R4,"tot_avg_cor",1),
    new headerInfo(212,SUN_R4,"tot_avg_uncor",1),
    new headerInfo(216,SUN_I4,"total_coin_rate",1),
    new headerInfo(220,SUN_I4,"frame_start_time",1),
    new headerInfo(224,SUN_I4,"frame_duration",1),
    new headerInfo(228,SUN_R4,"deadtime_correction_factor",1),
    new headerInfo(232,SUN_I2,"fill2",90),
    new headerInfo(412,SUN_I2,"fill3",50),
// this next line must be wrong because it would be greater then 512
//    new headerInfo(512,SUN_R4,"uncor_singles",128),
    new headerInfo(-1,BYTE_TYPE,"",0)
  };
  final static headerInfo[] ecat7_3d_norm_subheader = {
    new headerInfo(0,SUN_I2,"data_type",1),
    new headerInfo(2,SUN_I2,"num_r_elements",1),
    new headerInfo(4,SUN_I2,"num_transaxial_crystals",1),
    new headerInfo(6,SUN_I2,"num_crystal_rings",1),
    new headerInfo(8,SUN_I2,"crystals_per_ring",1),
    new headerInfo(10,SUN_I2,"num_geo_corr_planes",1),
    new headerInfo(12,SUN_I2,"uld",1),
    new headerInfo(14,SUN_I2,"lld",1),
    new headerInfo(16,SUN_I2,"scatter_energy",1),
    new headerInfo(18,SUN_R4,"norm_quality_factor",1),
    new headerInfo(22,SUN_I2,"norm_quality_factor_code",1),
    new headerInfo(24,SUN_R4,"ring_dtcor1",32),
    new headerInfo(152,SUN_R4,"ring_dtcor2",32),
    new headerInfo(280,SUN_R4,"crystal_dtcor",8),
    new headerInfo(312,SUN_I2,"span",1),
    new headerInfo(314,SUN_I2,"max_ring_diff",1),
    new headerInfo(316,SUN_I2,"fill1",48),
    new headerInfo(412,SUN_I2,"fill2",50),
    new headerInfo(-1,BYTE_TYPE,"",0)
  };
  final static headerInfo[] ecat7_imported_6_5_scan_subheader = {
    new headerInfo(0,SUN_I2,"data_type",1),
    new headerInfo(2,SUN_I2,"num_dimensions",1),
    new headerInfo(4,SUN_I2,"num_r_elements",1),
    new headerInfo(6,SUN_I2,"num_angles",1),
    new headerInfo(8,SUN_I2,"corrections_applied",1),
    new headerInfo(10,SUN_I2,"num_z_elements",1),
    new headerInfo(12,SUN_I2,"ring_difference",1),
    new headerInfo(14,SUN_R4,"x_resolution",1),
    new headerInfo(18,SUN_R4,"y_resolution",1),
    new headerInfo(22,SUN_R4,"z_resolution",1),
    new headerInfo(26,SUN_R4,"w_resolution",1),
    new headerInfo(30,SUN_I2,"fill1",6),
    new headerInfo(42,SUN_I4,"gate_duration",1),
    new headerInfo(46,SUN_I4,"r_wave_offset",1),
    new headerInfo(50,SUN_I4,"num_accepted_beats",1),
    new headerInfo(54,SUN_R4,"scale_factor",1),
    new headerInfo(58,SUN_I2,"scan_min",1),
    new headerInfo(60,SUN_I2,"scan_max",1),
    new headerInfo(62,SUN_I4,"prompts",1),
    new headerInfo(66,SUN_I4,"delayed",1),
    new headerInfo(70,SUN_I4,"multiples",1),
    new headerInfo(74,SUN_I4,"net_trues",1),
    new headerInfo(78,SUN_R4,"cor_singles",16),
    new headerInfo(142,SUN_R4,"uncor_singles",16),
    new headerInfo(206,SUN_R4,"tot_avg_cor",1),
    new headerInfo(210,SUN_R4,"tot_avg_uncor",1),
    new headerInfo(214,SUN_I4,"total_coin_rate",1),
    new headerInfo(218,SUN_I4,"frame_start_time",1),
    new headerInfo(222,SUN_I4,"frame_duration",1),
    new headerInfo(226,SUN_R4,"deadtime_correction_factor",1),
    new headerInfo(230,SUN_I2,"physical_planes",8),
    new headerInfo(246,SUN_I2,"fill2",83),
    new headerInfo(412,SUN_I2,"fill3",50),
    new headerInfo(-1,BYTE_TYPE,"",0)
  };
  final static headerInfo[] ecat7_imported_6_5_norm_subheader = {
    new headerInfo(0,SUN_I2,"data_type",1),
    new headerInfo(2,SUN_I2,"num_dimensions",1),
    new headerInfo(4,SUN_I2,"num_r_elements",1),
    new headerInfo(6,SUN_I2,"num_angles",1),
    new headerInfo(8,SUN_I2,"num_z_elements",1),
    new headerInfo(10,SUN_I2,"ring_difference",1),
    new headerInfo(12,SUN_R4,"scale_factor",1),
    new headerInfo(16,SUN_R4,"norm_min",1),
    new headerInfo(20,SUN_R4,"norm_max",1),
    new headerInfo(24,SUN_R4,"fov_source_width",1),
    new headerInfo(28,SUN_R4,"norm_quality_factor",1),
    new headerInfo(32,SUN_I2,"norm_quality_factor_code",1),
    new headerInfo(34,SUN_I2,"storage_order",1),
    new headerInfo(36,SUN_I2,"span",1),
    new headerInfo(38,SUN_I2,"z_elements",64),
    new headerInfo(166,SUN_I2,"fill1",123),
    new headerInfo(412,SUN_I2,"fill2",50),
    new headerInfo(-1,BYTE_TYPE,"",0)
  };
}
