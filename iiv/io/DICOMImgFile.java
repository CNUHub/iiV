package iiv.io;
import iiv.data.*;
import iiv.display.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
/**
 * DICOMImgFile is a class to handle storage and access of DICOM headers.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		SingleImg
 */
public class DICOMImgFile extends CNUImgFile implements CNUTypesConstants {
  private static final int PIXEL_REPRESENTATION = 0x00280103;
  private static final int SAMPLES_PER_PIXEL = 0x00280002;
  private static final int PHOTOMETRIC_INTERPRETATION = 0x00280004;
  private static final int PLANER_CONFIGURATION = 0x00280006;
  private static final int TRANSFER_SYNTAX_UID = 0x00020010;
  private static final int SLICE_SPACING = 0x00180088;
  private static final int NUMBER_OF_FRAMES = 0x00280008;
  private static final int ROWS = 0x00280010;
  private static final int COLUMNS = 0x00280011;
  private static final int PIXEL_SPACING = 0x00280030;
  private static final int BITS_ALLOCATED = 0x00280100;
  private static final int BITS_STORED = 0x00280101;
  private static final int HIGH_BIT = 0x00280102;
  private static final int SMALLEST_PIXEL_VALUE = 0x00280106;
  private static final int LARGEST_PIXEL_VALUE = 0x00280107;
  private static final int RED_PALETTE_DESCRIPTOR = 0x00281101;
  private static final int GREEN_PALETTE_DESCRIPTOR = 0x00281102;
  private static final int BLUE_PALETTE_DESCRIPTOR = 0x00281103;
  private static final int RED_PALETTE = 0x00281201;
  private static final int GREEN_PALETTE = 0x00281202;
  private static final int BLUE_PALETTE = 0x00281203;
  private static final int PIXEL_DATA = 0x7FE00010;

  private static final int DIRECTORY_RECORD_SEQUENCE = 0x00041220;
  private static final int ITEM = 0xFFFEE000;
  private static final int ITEM_DELIMITATION_ITEM = 0xFFFEE00D;
  private static final int FILE_SET_ID = 0x00041130;
  private static final int DIRECTORY_RECORD_TYPE = 0x00041430;
  private static final int PATIENT_NAME = 0x00100010;
  private static final int PATIENT_ID = 0x00100020;
  private static final int SERIES_NUMBER = 0x00200011;
  private static final int SERIES_DESCRIPTION = 0x0008103e;
  private static final int IMAGE_NUMBER = 0x00200013;
  private static final int IMAGE_POSITION = 0x00200032;
  private static final int REFERENCED_FILE_ID = 0x00041500;
  private static final int REFERENCED_IMAGE_SEQUENCE = 0x00081140;
  private static final int ICON_IMAGE_SEQUENCE = 0x880200;
  private static final int IMAGE_TYPE = 0x80008;

  private static final int keyOffset = 128;  //location of "DICM"
  private static final String DICOM_Key = "DICM";

  private boolean dataRead = false;
  private Object dataReadLock = new Object();
  private Vector<DICOM_DataElement> dataElements = new Vector<DICOM_DataElement>();
  private Vector<String> ignoredFormatErrors = new Vector<String>();
  private ColorModel colorModel = null;
  private CNUScale cnuScale = null;

  /**
   * Constructs an instance of DICOMImgFile with a given file name.
   *
   * @param filename	file to read data from
   */
  public DICOMImgFile(String filename) {
    setFileName(filename);
    setCNUDataConversions(CNUDataConversions.getStandardReverseBytesConversions());
    //    setCNUDataConversions(CNUDataConversions.getStandardNoConversions());
  }
  /*
   * Constructs an instance of DICOMImgFile with a given file name.
   *
   * @param filename	file to read data from
   * @param ignored	added for compatibility
   * @exception	IOException	thrown if error reading from file
   */
  public DICOMImgFile(String filename, Object ignored)
       throws IOException {
    this(filename);
  }
 /**
   * Overrides getDimensions to read data if not read.
   *
   * @return	the dimensions of the image data
   */
  public synchronized CNUDimensions getDimensions(){
    // dimensions not set until data read
    CNUDimensions dims = super.getDimensions();
    if(dims != null) return dims;
    try {
      readData();
      return(super.getDimensions());
    } catch (IOException ioe) {
      System.out.println(ioe);
    }
    return null;
  }

  /**
   * Reads data from the file.
   *
   * @exception IOException thrown on errors reading from the file.
   */
  public void readData() throws IOException {
    if(dataRead) return;
    synchronized (dataReadLock) {
      if(dataRead) return;
      DataInput dataInput = null;
      long currentOffset = 0;
      try {
	dataInput = getDataInput();
	CNUDataConversions cdc = getCNUDataConversions();
	DICOM_readDataInfo rdi = new DICOM_readDataInfo();

	// check for header
	int skipCount = keyOffset;
        while(skipCount > 0) skipCount -= dataInput.skipBytes(skipCount);
        currentOffset += keyOffset;
        byte keyBytes[] = new byte[4];
        dataInput.readFully(keyBytes);
        currentOffset += keyBytes.length;
        String readKey = new String(keyBytes);
        boolean DICMFound = true;
        if( ! DICOM_Key.equals(readKey)) {
	  // some DICOM files don't have key so still try reading
	  // readData will throw IOException if unknown data tag found with large data size
	  DICMFound = false;
          ignoredFormatErrors.addElement("key (" + DICOM_Key + ") not found at offset (" + keyOffset +
                                         ") - resetting to read data element tags starting at byte 0");
          if(dataInput instanceof InputStream) {
	    InputStream inputStream = (InputStream) dataInput;
            dataInput = null;
            inputStream.close();
          }
          dataInput = getDataInput();
          currentOffset = 0;
        }

	readData(dataInput, cdc, currentOffset, -1,
		 dataElements, ignoredFormatErrors,
		 DICMFound, rdi);


	if(rdi.dataArray != null) {
	  setDataArray(rdi.dims, rdi.dataArray);
	  setColorModel(rdi.cm);
	  setScale(rdi.sc);
	}
	else throw new IOException("read data failure -- rdi.dataArray still null");

	dataRead = true;
      } finally {
	if(dataInput instanceof InputStream) ((InputStream) dataInput).close();
      }
    }
  }

  /**
   * Sets the color model.
   *
   * @param cm	a new color model to use with this data
   *            or <code>null</code> to use standard default
   */
  public void setColorModel(ColorModel cm) { colorModel = cm; }
  /**
   * Gets the color model that should be used to display this data.
   *
   * @return	the color model or <null> if default should be used.
   */
  public ColorModel getColorModel() { return colorModel; }
  /**
   * Sets the scale object for converting voxel values to lookup table indices.
   *
   * @param sc	the scaling object
   */
  public void setScale(CNUScale sc) { cnuScale = sc; }
  /**
   * Gets the scale object that is used for converting voxel values to
   * lookup table indices.
   *
   * @return	the scaling object
   */
  public CNUScale getScale() { return cnuScale; }
  /**
   * Creates a string representation of the object
   *
   * @return the string representation
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(super.toString()).append("\n");
    Enumeration e = dataElements.elements();
    while(e.hasMoreElements()) {
      sb.append(e.nextElement().toString());
      sb.append('\n');
    }
    sb.append("dataRead=").append(dataRead).append("\n");
    sb.append("colorModel=");
    sb.append((colorModel == null) ? "null" : colorModel.toString());
    sb.append("\n");
    if(ignoredFormatErrors.size() > 0) {
      sb.append("The following DICOM format errors were ignored:\n");
      e = ignoredFormatErrors.elements();
      while(e.hasMoreElements()) {
	sb.append(e.nextElement().toString());
	sb.append('\n');
      }
    }
    return sb.toString();
  }

  /**
   * DICOM_readDataInfo stores the primary stuff utilized from DICOM files.
   */
  public static class DICOM_readDataInfo {
      public CNUScale sc = null;
      public ColorModel cm = null;
      public CNUDimensions dims = null;
      public Object dataArray = null;

      public int xdim = 1, ydim = 1, zdim = 1;
      public double xres = 1.0, yres=1.0, zres = 1.0;
      public int samplesPerPixel = -1;
      public int bitsAllocated = -1;
      public int bitsStored = -1;
      public int highBit = -1;
      public int pixelRepresentation = -1;
      public int planerConfiguration = -1;
      public byte[] reds = null, greens = null, blues = null;
      public String photometric = null;
      public double smallestPixelValue = Double.MIN_VALUE;
      public double largestPixelValue = Double.MAX_VALUE;
      // palette descriptor values
      public int redPaletteEntries = -1;
      public int redPalettePixelValueMapped = 0;
      public int redPaletteTableDataBits = -1;
      public int greenPaletteEntries = -1;
      public int greenPalettePixelValueMapped = 0;
      public int greenPaletteTableDataBits = -1;
      public int bluePaletteEntries = -1;
      public int bluePalettePixelValueMapped = 0;
      public int bluePaletteTableDataBits = -1;

      public int series_number = -1;
      public String referenced_file_id = null;
  }

  /**
   * Reads data from the file.
   *
   * @param dataInput input to read from
   * @param cdc data conversions
   * @param currentOffset offset to input mainly for debugging
   * @param dataElements Vector for storing DICOM_DataElement as we read them
   * @param ignoredFormatErrors for storing ignored errors may be <code>null</code>
   * @param DICMFound <code>true</code> if DICM value was found in header
   * @param rdi storage for info needed when building an image
   * @return currentOffset
   * @exception IOException thrown on errors reading from the file.
   */
  public static long readData(DataInput dataInput, CNUDataConversions cdc,
			      long currentOffset, long endOffset,
			      Vector<DICOM_DataElement> dataElements, Vector<String> ignoredFormatErrors,
			      boolean DICMFound, DICOM_readDataInfo rdi)
      throws IOException {
    int vrMode = DICOM_DataElement.UNKNOWN_VR_MODE;
    int lastVrMode = DICOM_DataElement.UNKNOWN_VR_MODE;
    int storageType = UNSIGNED_SHORT;
    int unsignedType = SHORT;

    if(ignoredFormatErrors == null) ignoredFormatErrors = new Vector<String>();
    if(rdi == null) rdi = new DICOM_readDataInfo();
    long ddeOffset = 0;
    Thread currentThread = Thread.currentThread();

    try {
      if(endOffset < 0) endOffset = Long.MAX_VALUE; // stop loop on EOF or other error
      while(currentOffset < endOffset && ! currentThread.isInterrupted()) {
	ddeOffset = currentOffset;
	// read the next data elements header
	DICOM_DataElement dde =
	    DICOM_DataElement.getDICOM_DataElement(dataInput, currentOffset, vrMode, cdc,
						   ignoredFormatErrors);
	currentOffset += dde.getHeaderLength();
	dataElements.addElement(dde);
	// note mode errors
	if(lastVrMode == DICOM_DataElement.UNKNOWN_VR_MODE) {
	  lastVrMode = dde.getVRMode();
	  if(lastVrMode != DICOM_DataElement.EXPLICIT_VR)
	    ignoredFormatErrors.addElement(ddeOffset +
	      " data elements implicit - files should only be explicit");
	}
	else if(lastVrMode != dde.getVRMode()) {
	  lastVrMode = dde.getVRMode();
	  ignoredFormatErrors.addElement(ddeOffset + " VR mode changed to " +
					   ((lastVrMode == DICOM_DataElement.EXPLICIT_VR)?
					    "explicit" : "implicit"));
	}
	
	// handling of actual data depends on tag
	switch(dde.getTag().getCombinedGroupAndElement()) {
	default:
	  //  handle unused tags but don't store large data arrays
	  if(dde.getDataLength() > 1024) {
	    if((! dde.getTag().getKnownTag()) && (! DICMFound))
	      throw new IOException(ddeOffset +
				    " large unknown tag found - probably not a DICOM file");
	    else { 
	      ignoredFormatErrors.addElement(ddeOffset +
	       " skipping reading value of unused large element (> 1024): element="
						 + dde.toString());
	      dde.skipValue(dataInput);
	      currentOffset += dde.getDataLength();
	    }
	  }
	  else {
	    dde.readValue(dataInput, null, 0, ignoredFormatErrors);
	    //	    dde.readValue(dataInput);
	    currentOffset += dde.getDataLength();
	  }
	  break;
	case DIRECTORY_RECORD_SEQUENCE:
	case ITEM:
	case REFERENCED_IMAGE_SEQUENCE:
	    //	case ICON_IMAGE_SEQUENCE:
	  dde.readValue(dataInput, null, 0, ignoredFormatErrors);
	  currentOffset += dde.getDataLength();
	  break;
	case TRANSFER_SYNTAX_UID:
	  dde.readValue(dataInput);
	  currentOffset += dde.getDataLength();
	  /*
	    1.2.840.10008.1.2 "Implicit VR Little Endian"
	    1.2.840.10008.1.2.1 "Explicit VR Little Endian"
	    1.2.840.10008.1.2.1.99 "Deflated Explicit VR Little Endian"
	    1.2.840.10008.1.2.2 "Explicit VR Big Endian"
	  */
	  break;
	case NUMBER_OF_FRAMES:
	  dde.readValue(dataInput);
	  currentOffset += dde.getDataLength();
	  rdi.zdim = (int) dde.getLongValue();
	  break;
	case ROWS:
	  dde.readValue(dataInput);
	  currentOffset += dde.getDataLength();
	  rdi.ydim = (int) dde.getLongValue();
	  break;
	case COLUMNS:
	  dde.readValue(dataInput);
	  currentOffset += dde.getDataLength();
	  rdi.xdim = (int) dde.getLongValue();
	  break;
	case SLICE_SPACING:
	  dde.readValue(dataInput);
	  currentOffset += dde.getDataLength();
	  rdi.zres = dde.getDoubleValue();
	  rdi.zres *= 1e-3; // convert from mm to meters
	  break;
	case PIXEL_SPACING:
	  dde.readValue(dataInput);
	  currentOffset += dde.getDataLength();
	  String pixelSpacing = (String) dde.getValue();
	  int index = pixelSpacing.indexOf('\\'); // offset to second double string
	  if(index < 0) rdi.xres = new Double(pixelSpacing).doubleValue();
	  else rdi.xres =  new Double(pixelSpacing.substring(0,index)).doubleValue();
	  rdi.xres *= 1e-3; // convert from mm to meters
	  index++;
	  if((index < 2) || (index >= pixelSpacing.length()) ) rdi.yres = rdi.xres;
	  else rdi.yres = new Double(pixelSpacing.substring(index)).doubleValue();
	  rdi.yres *= 1e-3; // convert from mm to meters
	  break;
	case BITS_ALLOCATED:
	  dde.readValue(dataInput);
	  currentOffset += dde.getDataLength();
	  // number of bits allocated per pixel >= bits stored
	  rdi.bitsAllocated = (int) dde.getLongValue();
	  break;
	case BITS_STORED:
	  dde.readValue(dataInput);
	  // number of bits allocated per pixel <= bits_allocated
	  rdi.bitsStored = (int) dde.getLongValue();
	  break;
	case HIGH_BIT:
	  dde.readValue(dataInput);
	  currentOffset += dde.getDataLength();
	  // high bit of bits stored (< bits allocated) (> bits stored -1)
	  rdi.highBit = (int) dde.getLongValue();
	  break;
	case PIXEL_REPRESENTATION:
	  dde.readValue(dataInput);
	  currentOffset += dde.getDataLength();
	  // 0x0000 = unsigned integer, 0x0001 = 2's complement 
	  rdi.pixelRepresentation = (int) dde.getLongValue();
	  break;
	case PHOTOMETRIC_INTERPRETATION:
	  dde.readValue(dataInput);
	  currentOffset += dde.getDataLength();
	  rdi.photometric = (String) dde.getValue();
	  // "MONOCHROME1" samples per pixel == 1, min value = white
	  // "MONOCHROME2" samples per pixel == 1, min value = black
	  // "PALETTE COLOR" samples per pixel == 1, red, green, blue palletes needed
	  // "RGB" samples per pixel == 3
	  // "ARGB" samples per pixel == 4
	  // "YBR_FULL" samples per pixel == 3
	  // "YBR_FULL_422" samples per pixel == 3
	  // "YBR_PARTIAL_422" samples per pixel == 3
	  break;
	case PLANER_CONFIGURATION:
	  dde.readValue(dataInput);
	  currentOffset += dde.getDataLength();
	  rdi.planerConfiguration = (int) dde.getLongValue();
	  // 000 = RGB byte ordering is R1,G1,B1,R2,G2,B2, ...
	  // 001 = RGB byte ordering is R1,R2,R3, ... G1,G2,G2, ... B1,B2,B3, ...
	  break;
	case RED_PALETTE_DESCRIPTOR:
	  dde.readValue(dataInput);
	  currentOffset += dde.getDataLength();
	  storageType = dde.getStorageType();
	  unsignedType = (storageType == BYTE) ? UNSIGNED_BYTE :
	      ((storageType == SHORT) ? UNSIGNED_SHORT : storageType);
	  rdi.redPaletteEntries = CNUTypes.getArrayValueAsInt(dde.getValue(), 0,
							      unsignedType);
	  if(rdi.redPaletteEntries == 0) rdi.redPaletteEntries = 2^16;
	  rdi.redPalettePixelValueMapped = CNUTypes.getArrayValueAsInt(dde.getValue(), 1,
								       storageType);
	  // bits should be 8 or 16
	  rdi.redPaletteTableDataBits = CNUTypes.getArrayValueAsInt(dde.getValue(), 2,
								    unsignedType);
	  break;
	case GREEN_PALETTE_DESCRIPTOR:
	  dde.readValue(dataInput);
	  currentOffset += dde.getDataLength();
	  storageType = dde.getStorageType();
	  unsignedType = (storageType == BYTE) ? UNSIGNED_BYTE :
	      ((storageType == SHORT) ? UNSIGNED_SHORT : storageType);
	  rdi.greenPaletteEntries = CNUTypes.getArrayValueAsInt(dde.getValue(), 0,
								unsignedType);
	  if(rdi.greenPaletteEntries == 0) rdi.greenPaletteEntries = 2^16;
	  rdi.greenPalettePixelValueMapped = CNUTypes.getArrayValueAsInt(dde.getValue(), 1,
									 storageType);
	  // bits should be 8 or 16
	  rdi.greenPaletteTableDataBits = CNUTypes.getArrayValueAsInt(dde.getValue(), 2,
								      unsignedType);
	  break;
	case BLUE_PALETTE_DESCRIPTOR:
	  dde.readValue(dataInput);
	  currentOffset += dde.getDataLength();
	  storageType = dde.getStorageType();
	  unsignedType = (storageType == BYTE) ? UNSIGNED_BYTE :
	      ((storageType == SHORT) ? UNSIGNED_SHORT : storageType);
	  rdi.bluePaletteEntries = CNUTypes.getArrayValueAsInt(dde.getValue(), 0,
							       unsignedType);
	  if(rdi.bluePaletteEntries == 0) rdi.bluePaletteEntries = 2^16;
	  rdi.bluePalettePixelValueMapped = CNUTypes.getArrayValueAsInt(dde.getValue(), 1,
									storageType);
	  // bits should be 8 or 16
	  rdi.bluePaletteTableDataBits = CNUTypes.getArrayValueAsInt(dde.getValue(), 2,
								     unsignedType);
	  break;
	case RED_PALETTE:
	  dde.readValue(dataInput);
	  currentOffset += dde.getDataLength();
	  try {
	    rdi.reds = colorValuesToBytes(dde.getValue());
	  } catch (IllegalArgumentException iae) {
	    ignoredFormatErrors.addElement(ddeOffset +
					     " invalid reds color palette " +
					     iae.toString());
	  }
	  break;
	case GREEN_PALETTE:
	  dde.readValue(dataInput);
	  currentOffset += dde.getDataLength();
	  try {
	    rdi.greens = colorValuesToBytes(dde.getValue());
	  } catch (IllegalArgumentException iae) {
	    ignoredFormatErrors.addElement(ddeOffset +
					     " invalid greens color palette " +
					     iae.toString());
	  }
	  break;
	case BLUE_PALETTE:
	  dde.readValue(dataInput);
	  currentOffset += dde.getDataLength();
	  try {
	    rdi.blues = colorValuesToBytes(dde.getValue());
	  } catch (IllegalArgumentException iae) {
	    ignoredFormatErrors.addElement(ddeOffset +
					     " invalid blues color palette " +
					     iae.toString());
	  }
	  break;
	case SAMPLES_PER_PIXEL:
	  dde.readValue(dataInput);
	  currentOffset += dde.getDataLength();
	  // number of sample planes = 1 (ie. monochrome or pallete), 3 (ie. RGB) or 4 (ie. ARGB)
	  rdi.samplesPerPixel = (int) dde.getLongValue();
	  break;
	case SMALLEST_PIXEL_VALUE:
	  dde.readValue(dataInput);
	  currentOffset += dde.getDataLength();
	  rdi.smallestPixelValue = dde.getDoubleValue();
	  break;
	case LARGEST_PIXEL_VALUE:
	  dde.readValue(dataInput);
	  currentOffset += dde.getDataLength();
	  rdi.largestPixelValue = dde.getDoubleValue();
	  break;
	case PIXEL_DATA:
	  // may be OW or OB - how to know?
	  // base OW or OB on bitsAllocated or dimensions
	  if(rdi.bitsAllocated < 1) {
	      if( (rdi.xdim*rdi.ydim*rdi.zdim) >= dde.getDataLength())
		dde.setStorageType(BYTE);
	      else dde.setStorageType(SHORT);
	  }
	  else if(rdi.bitsAllocated <= 8) dde.setStorageType(BYTE);
	  else if(rdi.bitsAllocated <= 16) dde.setStorageType(SHORT);
	  else ignoredFormatErrors.addElement(ddeOffset +
					      "bitsAllocated="+ rdi.bitsAllocated + 
					      "does not fit into OW(short) or OB(byte)");
	  dde.readValue(dataInput);
	  currentOffset += dde.getDataLength();

	  rdi.dataArray = dde.getValue();
	  storageType = dde.getStorageType();
	  int dataLengthInWords = dde.getDataLengthInWords();

	  rdi.dims = new CNUDimensions();
	  boolean signed = false;
	  // assume if pixelRepresentation set it overrides signed/unsigned
	  // value representations
	  if(rdi.pixelRepresentation == 0) {
	    signed = false;
	    switch (storageType) {
	    case BYTE:
	      ignoredFormatErrors.addElement(ddeOffset +
    " signed byte pixel data storage didn't match pixel representation - assuming unsigned");
	      storageType = UNSIGNED_BYTE;
	      break;
	    case SHORT:
	      ignoredFormatErrors.addElement(ddeOffset +
    " signed short pixel data storage didn't match pixel representation - assuming unsigned");
	      storageType = UNSIGNED_SHORT;
	      break;
	    case INTEGER:
	      ignoredFormatErrors.addElement(ddeOffset +
     " signed int pixel data storage didn't match pixel representation - assuming unsigned");
	      storageType = UNSIGNED_INTEGER;
	      break;
	    default:
	      break;
	    }
	  } 
	  else if(rdi.pixelRepresentation == 1) {
	    signed = true;
	    switch (storageType) {
	    case UNSIGNED_BYTE:
	      ignoredFormatErrors.addElement(ddeOffset +
  " unsigned byte pixel data storage didn't match pixel representation - assuming signed");
	      storageType = BYTE;
	      break;
	    case UNSIGNED_SHORT:
	      ignoredFormatErrors.addElement(ddeOffset +
   " unsigned short pixel data storage didn't match pixel representation - assuming signed");
	      storageType = SHORT;
	      break;
	    case UNSIGNED_INTEGER:
	      ignoredFormatErrors.addElement(ddeOffset +
    " unsigned int pixel data storage didn't match pixel representation - assuming signed");
	      storageType = INTEGER;
	      break;
	    default:
	      break;
	    }
	  }
	  else switch (storageType) {
	  case UNSIGNED_BYTE:
	  case UNSIGNED_SHORT:
	  case UNSIGNED_INTEGER:
	    ignoredFormatErrors.addElement(ddeOffset +
       " pixel representation not set - assuming unsigned due to storagetype");
	    rdi.pixelRepresentation = 0; // unsigned
	    signed = false;
	    break;
	  default:
	    ignoredFormatErrors.addElement(ddeOffset +
     " pixel representation not set - assuming signed due to storagetype");
	    rdi.pixelRepresentation = 1; // twos complement
	    signed = true;
	    break;
	  }

	  // if not set initialize bitsAllocated, bitsStored and
	  // highBit based on storageType
	  int bytesPerWord = CNUTypes.bytesPerWord(storageType);
	  int bitsPerWord = bytesPerWord * 8;
	  if(rdi.bitsAllocated < 1) {
	    ignoredFormatErrors.addElement(ddeOffset +
			  " bits allocated not set assuming same as bitsPerWord");
	    rdi.bitsAllocated = bitsPerWord;
	  }
	  else if(rdi.bitsAllocated != bitsPerWord) {
	    ignoredFormatErrors.addElement(ddeOffset +
    " Can this work? - bits allocated=" + rdi.bitsAllocated + " bitsPerWord=" + bitsPerWord);
	  }

	  if(rdi.highBit > (bitsPerWord - 1)) {
	    ignoredFormatErrors.addElement(ddeOffset +
	         " highBit > bitsPerWord resetting to bitsPerWord - 1");
	    rdi.highBit = bitsPerWord - 1;
	  }
	  if(rdi.bitsStored > bitsPerWord) {
	    ignoredFormatErrors.addElement(ddeOffset +
		 " bitsStored > bitsPerWord resetting to bitsPerWord");
	    rdi.bitsStored = bitsPerWord;
	  }
	  else if(rdi.bitsStored < 1) {
	    if(rdi.highBit < 0) {
	      ignoredFormatErrors.addElement(ddeOffset +
					     " bitsStored and high bit not set - setting based on bitsPerWord");
	      rdi.bitsStored = bitsPerWord; rdi.highBit = rdi.bitsStored - 1;
	    }
	    else {
	      ignoredFormatErrors.addElement(ddeOffset +
					     " bitsStored not set - setting based on highBit");
	      rdi.bitsStored = rdi.highBit + 1;
	    }
	  }
	  if(rdi.highBit < 0) {
	    ignoredFormatErrors.addElement(ddeOffset +
					   " highBit not set - setting based on bitsStored");
	    rdi.highBit = rdi.bitsStored - 1;
	  }

	  // deal with data size corrections
	  if(rdi.bitsStored != bitsPerWord) {
	    // at a min we need to template out unused bits
	    // we may also have to shift right
	    // and we may have to convert to a new storage size
	    boolean shiftNeeded = (rdi.highBit > (rdi.bitsStored - 1));
	    int bytesNeeded = bytesPerWord;
	    if(rdi.bitsStored < bitsPerWord) {
	      bytesNeeded = rdi.bitsStored/8;
	      if((bytesNeeded * 8) < rdi.bitsStored) bytesNeeded++;
	      if(bytesNeeded == 3) bytesNeeded = 4;
	    }

	    int newStorageType = storageType;
	    if(rdi.bitsStored <= 8) {
	      newStorageType = signed ? BYTE : UNSIGNED_BYTE;
	    }
	    else if(rdi.bitsStored <= 16) {
	      newStorageType = signed ? SHORT : UNSIGNED_SHORT;
	    }
	    else if(rdi.bitsStored <= 32) {
	      newStorageType = signed ?  INTEGER : UNSIGNED_INTEGER;
	    }
	      
	    int shiftLeft = 31 - rdi.highBit;
	    int shiftRight = 32 - rdi.bitsStored;
	    int word=0;
	    if(newStorageType != storageType) {
	      // requires new data array
	      Object newDataArray = CNUTypes.arrayOf(newStorageType, dataLengthInWords);
	      for(int i = 0; i < dataLengthInWords; i++) {
		switch (storageType) {
		case SHORT:
		case UNSIGNED_SHORT:
		  word = (int) ((short[]) rdi.dataArray)[i];
		  break;
		case INTEGER:
		case UNSIGNED_INTEGER:
		  word = ((int[]) rdi.dataArray)[i];
		  break;
		default:
		  throw new IOException("Shifting data of type=" +
					CNUTypes.typeToString(storageType) +
					" not implemented");
		}
		word = word<<shiftLeft;
		// >> fills with sign bit where >>> fills with zeros
		word = signed ? (word >> shiftRight) : (word >>> shiftRight);
		switch(newStorageType) {
		case BYTE:
		case UNSIGNED_BYTE:
		  ((byte[]) newDataArray)[i] = (byte) word;
		  break;
		case SHORT:
		case UNSIGNED_SHORT:
		  ((short[]) newDataArray)[i] = (short) word;
		  break;
		case INTEGER:
		case UNSIGNED_INTEGER:
		  ((int[]) newDataArray)[i] = word;
		  break;
		default:
		  // should not get here
		  break;
		}
	      }
	      rdi.dataArray = newDataArray;
	      storageType = newStorageType;
	    }
	    else {
	      // done in place in current data array
	      for(int i = 0; i < dataLengthInWords; i++) {
		switch (storageType) {
		case BYTE:
		case UNSIGNED_BYTE:
		  word = (int)((byte[]) rdi.dataArray)[i];
		  word = word<<shiftLeft;
		  word = signed ? (word >> shiftRight) : (word >>> shiftRight);
		  ((byte[]) rdi.dataArray)[i] = (byte) word;
		  break;
		case SHORT:
		case UNSIGNED_SHORT:
		  word = (int) ((short[]) rdi.dataArray)[i];
		  word = word<<shiftLeft;
		  word = signed ? (word >> shiftRight) : (word >>> shiftRight);
		  ((short[]) rdi.dataArray)[i] = (short) word;
		  break;
		case INTEGER:
		case UNSIGNED_INTEGER:
		  word = ((int[]) rdi.dataArray)[i];
		  word = word<<shiftLeft;
		  word = signed ? (word >> shiftRight) : (word >>> shiftRight);
		  ((int[]) rdi.dataArray)[i] = word;
		  break;
		default:
		  throw new IOException("Shifting data of type=" +
					CNUTypes.typeToString(storageType) +
					" not implemented");
		}
		word = word<<shiftLeft;
		// >> fills with sign bit where >>> fills with zeros
		word = signed ? (word >> shiftRight) : (word >>> shiftRight);
	      } // end for(int i = 0; i < dataLengthInWords; i++)
	    }
	    if(signed) {
	      rdi.largestPixelValue = Math.min(rdi.largestPixelValue,
					       Math.pow(2, rdi.bitsStored - 1) - 1);
	      rdi.smallestPixelValue = Math.max(rdi.smallestPixelValue,
						- Math.pow(2, rdi.bitsStored - 1));
	    }
	    else {
	      rdi.largestPixelValue = Math.min(rdi.largestPixelValue,
					       Math.pow(2, rdi.bitsStored) - 1);
	      rdi.smallestPixelValue = Math.max(rdi.smallestPixelValue, 0);
	    }
	  } // end if(bitsStored != bitsPerWord)
	  else {
	    rdi.largestPixelValue = Math.min(rdi.largestPixelValue, CNUTypes.maxValue(storageType));
	    rdi.smallestPixelValue = Math.max(rdi.smallestPixelValue, CNUTypes.minValue(storageType));
	  }
	  // done dealing with data size corrections
	  
	  // now deal with color mode
	  if(rdi.photometric != null) rdi.photometric = rdi.photometric.trim();
	  if(rdi.photometric == null) ; // no color mode given
	  else if("MONOCHROME1".equals(rdi.photometric)) {
	    // "MONOCHROME1" samples per pixel == 1, min value = white
	    rdi.sc = new CNUScale(1.0);
	    rdi.sc.setToFitDataInRange(rdi.smallestPixelValue, rdi.largestPixelValue,
				       255, 0, false);
	    rdi.cm = CNUColorModel.getGreyColorModel();
	  }
	  else if("MONOCHROME2".equals(rdi.photometric)) {
	    // "MONOCHROME2" samples per pixel == 1, min value = black
	    rdi.sc = new CNUScale(1.0);
	    rdi.sc.setToFitDataInRange(rdi.smallestPixelValue, rdi.largestPixelValue,
				       0, 255, false);
	    rdi.cm = CNUColorModel.getGreyColorModel();
	  }
	  else if("PALETTE COLOR".equals(rdi.photometric)) {
	    // "PALETTE COLOR" samples per pixel == 1
	    if(rdi.samplesPerPixel != 1)
	      ignoredFormatErrors.addElement(ddeOffset +
					     " bad palette color samples per pixel=" +
					     rdi.samplesPerPixel +
					     " assuming correct value of 1");
	    if((rdi.reds == null) || (rdi.greens == null) || (rdi.blues == null)) {
	      ignoredFormatErrors.addElement(ddeOffset + " specified Palette Color but missing reds, greens or blues data - no data specific color table built");
	    } else {
	      if(rdi.redPaletteEntries < 0) {
		ignoredFormatErrors.addElement(ddeOffset +
					       " redPaletteEntries not set reverting to reds size");
		rdi.redPaletteEntries = rdi.reds.length;
	      }
	      else if(rdi.redPaletteEntries != rdi.reds.length) {
		ignoredFormatErrors.addElement(ddeOffset +
					       " redPaletteEntries not equal to redsLengthInWords setting to lesser");
		rdi.redPaletteEntries = Math.min(rdi.redPaletteEntries, rdi.reds.length);
	      }
	      if(rdi.greenPaletteEntries < 0) {
		ignoredFormatErrors.addElement(ddeOffset +
						 " greenPaletteEntries not set reverting to greens size");
		rdi.greenPaletteEntries = rdi.greens.length;
	      }
	      else if(rdi.greenPaletteEntries != rdi.greens.length) {
		ignoredFormatErrors.addElement(ddeOffset +
					       " greenPaletteEntries not equal to greensLengthInWords setting to lesser");
		rdi.greenPaletteEntries = Math.min(rdi.greenPaletteEntries, rdi.greens.length);
	      }
	      if(rdi.bluePaletteEntries < 0) {
		ignoredFormatErrors.addElement(ddeOffset +
					       " bluePaletteEntries not set reverting to blues size");
		rdi.bluePaletteEntries = rdi.blues.length;
	      }
	      else if(rdi.bluePaletteEntries != rdi.blues.length) {
		ignoredFormatErrors.addElement(ddeOffset +
					       " bluePaletteEntries not equal to bluesLengthInWords setting to lesser");
		rdi.bluePaletteEntries = Math.min(rdi.bluePaletteEntries, rdi.blues.length);
	      }
	      if((rdi.redPaletteEntries != rdi.bluePaletteEntries) ||
		 (rdi.redPaletteEntries != rdi.greenPaletteEntries)) {
		ignoredFormatErrors.addElement(ddeOffset +
					       " can't handle unequal red, green and blue palette entries using smallest");
		rdi.redPaletteEntries = Math.min(rdi.redPaletteEntries, rdi.greenPaletteEntries);
		rdi.redPaletteEntries = Math.min(rdi.redPaletteEntries, rdi.bluePaletteEntries);
		rdi.greenPaletteEntries = rdi.bluePaletteEntries = rdi.redPaletteEntries;
	      }
	      if((rdi.redPalettePixelValueMapped != rdi.bluePalettePixelValueMapped) ||
		 (rdi.redPalettePixelValueMapped != rdi.greenPalettePixelValueMapped)) {
		ignoredFormatErrors.addElement(ddeOffset +
					       " can't handle unequal red, green and blue palette values mapped using smallest");
		rdi.redPalettePixelValueMapped = Math.min(rdi.redPalettePixelValueMapped,
							  rdi.greenPalettePixelValueMapped);
		rdi.redPalettePixelValueMapped = Math.min(rdi.redPalettePixelValueMapped,
							  rdi.bluePalettePixelValueMapped);
		rdi.greenPalettePixelValueMapped = rdi.bluePalettePixelValueMapped =
		    rdi.redPalettePixelValueMapped;
	      }
	      rdi.cm = new IndexColorModel(8, rdi.redPaletteEntries,
					   rdi.reds, rdi.greens, rdi.blues);
	      rdi.sc = new CNUScale(1);
	      rdi.sc.setToFitDataInRange(rdi.bluePalettePixelValueMapped,
					 rdi.bluePalettePixelValueMapped + rdi.redPaletteEntries - 1,
					 0, rdi.redPaletteEntries - 1, true);
	    }
	  }
	  else { // remaining photometrics rely on planerConfiguration
	    switch (rdi.planerConfiguration) {
	    case 000: // RGB byte ordering is R1,G1,B1,R2,G2,B2, ...
	    case 001: // RGB byte ordering is R1,R2,R3, ... G1,G2,G2, ... B1,B2,B3, ...
	      break;
	    case -1: // unset
	      ignoredFormatErrors.addElement(ddeOffset +
					     " unset planerConfiguration=" +
					     rdi.planerConfiguration);
	      ignoredFormatErrors.addElement(ddeOffset +
					     " setting planerConfiguration to RGB cycling");
	      rdi.planerConfiguration = 0;
	      break;
	    default:
	      ignoredFormatErrors.addElement(ddeOffset +
					     " unknown planerConfiguration=" +
					     rdi.planerConfiguration);
	      ignoredFormatErrors.addElement(ddeOffset +
					     " setting planerConfiguration to RGB cycling");
	      rdi.planerConfiguration = 0;
	      break;
	    }
	    if("RGB".equals(rdi.photometric)) {
	      // "RGB" samples per pixel == 3
	      if(rdi.samplesPerPixel != 3)
		ignoredFormatErrors.addElement(ddeOffset +
					       " bad RGB samples per pixel=" +
					       rdi.samplesPerPixel +
					       " assuming correct value of 3");
	      // convert to int RGB storage
	      int newStorageType = UNSIGNED_INTEGER;
	      int newDataLengthInWords = dataLengthInWords/3;
	      int[] newDataArray = new int[newDataLengthInWords];
	      int inStep = 3, greenOffset = 1, blueOffset = 2;
	      if(rdi.planerConfiguration == 1) {
		inStep = 1;
		greenOffset = newDataLengthInWords;
		blueOffset = 2 * newDataLengthInWords;
	      }
	      for(int outIndex = 0, inIndex = 0; outIndex < newDataLengthInWords;
		  outIndex++, inIndex += inStep) {
		int red = CNUTypes.getArrayValueAsInt(rdi.dataArray, inIndex, storageType);
		int green = CNUTypes.getArrayValueAsInt(rdi.dataArray, inIndex + greenOffset,
							storageType);
		int blue = CNUTypes.getArrayValueAsInt(rdi.dataArray, inIndex + blueOffset,
						       storageType);
		newDataArray[outIndex] = (0xFF << 24) | ((red & 0xFF) << 16) |
		    ((green & 0xFF) << 8) | (blue & 0xFF);
	      }
	      rdi.dataArray = newDataArray;
	      dataLengthInWords = newDataLengthInWords;
	      storageType = UNSIGNED_INTEGER;
	      rdi.sc = new CNUScale(1.0);
	      rdi.cm = ColorModel.getRGBdefault();
	    }
	    else if("ARGB".equals(rdi.photometric)) {
	      if(rdi.samplesPerPixel != 4)
		ignoredFormatErrors.addElement(ddeOffset +
					       " bad ARGB samples per pixel=" +
					       rdi.samplesPerPixel +
					       " assuming correct value of 3");
	    }
	    else if("HSV".equals(rdi.photometric)) {
	      if(rdi.samplesPerPixel != 3)
		ignoredFormatErrors.addElement(ddeOffset +
					       " bad HSV samples per pixel=" +
					       rdi.samplesPerPixel + 
					       " assuming correct value of 3");
	      ignoredFormatErrors.addElement(ddeOffset +
					     " HSV photometric not implemented");
	    }
	    else if("CMYK".equals(rdi.photometric)) {
	      if(rdi.samplesPerPixel != 4)
		ignoredFormatErrors.addElement(ddeOffset +
					       " bad CMYK samples per pixel=" +
					       rdi.samplesPerPixel +
					       " assuming correct value of 3");
	      ignoredFormatErrors.addElement(ddeOffset +
					     " CMYK photometric not implemented");
	    }
	    else if("YBR_FULL".equals(rdi.photometric)) {
	      if(rdi.samplesPerPixel != 3)
		ignoredFormatErrors.addElement(ddeOffset +
					       " bad YBR_FULL samples per pixel=" +
					       rdi.samplesPerPixel + 
					       " assuming correct value of 3");
	      ignoredFormatErrors.addElement(ddeOffset +
					     " YBR_FULL photometric not implemented");
	    }
	    else if("YBR_FULL_422".equals(rdi.photometric)) {
	      if(rdi.samplesPerPixel != 3)
		ignoredFormatErrors.addElement(ddeOffset +
					       " bad YBR_FULL_422 samples per pixel=" +
					       rdi.samplesPerPixel +
					       " assuming correct value of 3");
	      ignoredFormatErrors.addElement(ddeOffset +
					     " YBR_FULL_422 photometric not implemented");
	    }
	    else if("YBR_PARTIAL_422".equals(rdi.photometric)) {
	      if(rdi.samplesPerPixel != 3)
		ignoredFormatErrors.addElement(ddeOffset +
					       " bad YBR_PARTIAL_422 samples per pixel=" +
					       rdi.samplesPerPixel +
					       " assuming correct value of 3");
	      ignoredFormatErrors.addElement(ddeOffset +
					     " YBR_PARTIAL_422 photometric not implemented");
	    }
	    else ignoredFormatErrors.addElement(ddeOffset +
						" Unknown photometric=" + rdi.photometric);
	  }
	  rdi.dims.set3DValues(rdi.xdim, rdi.ydim, rdi.zdim, storageType, 0);
	  rdi.dims.set3DSpatialResolutions(rdi.xres, rdi.yres, rdi.zres);
	  rdi.dims.setOrientation(CNUDimensions.TRANSVERSE);
	  rdi.dims.setOrientationOrder(CNUDimensions.LEFT_POSITIVE | CNUDimensions.ANTERIOR_POSITIVE |
				       CNUDimensions.SUPERIOR_POSITIVE);
	  break;
	  
	} // end switch(dde.getTag().getCombinedGroupAndElement())

      } // end while(currentOffset < endOffset)
    } catch (EOFException eofe) { // normal exit
    } catch (IllegalArgumentException iae) {  // superclass of NumberFormatException
      throw new IOException("DICOMImgFile invalid - caught IllegalArgumentException=" +
			    iae.toString() + " at currentOffset=" + currentOffset);
    }
    if(currentThread.isInterrupted()) throw new IOException("Interrupted");
    return currentOffset;
  }



  /**
   * Makes sure an array of shorts or bytes is an array of bytes
   * by converting a short array to byte using only the most
   * significant bytes.
   *
   * @param array an array of bytes or shorts
   * @return an array of bytes
   * @exception IllegalArgumentException if the array is not byte[] or short[]
   */
  public static byte[] colorValuesToBytes(Object array) throws IllegalArgumentException {
    if(array instanceof byte[]) return (byte[]) array;
    else if(array instanceof short[]) {
      short s[] = (short[]) array;
      byte b[] = new byte[s.length];
      for(int i=0; i<s.length; i++) b[i] = (byte) (s[i] >> 8);
      return b;
    }
    else throw new IllegalArgumentException("color values not 8 bit or 16 bit");
  }
  /**
   * Runs DICOMImgFile as a standalone program to print out header
   * information.
   *
   * @param args	array of arguments from the command line
   * @exception IOException thrown some how
   */
  static public void main(String[] args) throws IOException {
    String filename = null;
    boolean lineMode = false;
    String usage = "usage:  java DicomImgFile filename [-help] [-lineMode]";
    for(int i=0; i<args.length; i++) {
      if("-help".equals(args[i])) { System.out.println(usage); System.exit(0); }
      else if("-linemode".equalsIgnoreCase(args[i])) lineMode = true;
      else if(filename == null) filename = args[i];
      else {
	System.out.println("invalid parameter=" + args[i]); System.exit(1);
      }
    }
    if(filename == null) { System.out.println(usage); System.exit(0); }
    DICOMImgFile dicomImgFile = new DICOMImgFile(filename);
    if(lineMode) {
      DataInput dataInput = dicomImgFile.getDataInput();
      int currentOffset = 0;
      // step through tags as directed by user
      StreamTokenizer tokens = new StreamTokenizer(new InputStreamReader(System.in));
      tokens.eolIsSignificant(false);
      tokens.wordChars('?', '?');
      int c = StreamTokenizer.TT_EOL;
      String options = "help, ?, q, reverse, noreverse, tag, vr, goto N, skip N, string N, short N, int N, float N, double N\n{byte128=DICM:tag:[vr:length]:value:tag:[vr:length]:...}";
      String response = null;
      while((! "q".equalsIgnoreCase(response)) && (c != StreamTokenizer.TT_EOF) ) {
	System.out.print("input or ?:  ");
	c = tokens.nextToken();
	if(c == StreamTokenizer.TT_EOF) continue;
	if(c == StreamTokenizer.TT_WORD) response = tokens.sval;
	else {
	  System.out.println("invalid token=" + tokens.toString());
	  continue;
	}
	if("help".equals(response)) System.out.println(options);
	else if("?".equals(response)) {
	  System.out.println("file=" + filename);
	  System.out.println("current location(bytes)=" + currentOffset);
	  System.out.print("conversions=");
	  if(CNUDataConversions.getStandardNoConversions().equals(dicomImgFile.getCNUDataConversions()))
	    System.out.println("no conversions");
	  else if(CNUDataConversions.getStandardReverseBytesConversions().equals(dicomImgFile.getCNUDataConversions()))
	    System.out.println("reverse bytes");
	  else System.out.println(dicomImgFile.getCNUDataConversions().toString());
	}
	else if("q".equalsIgnoreCase(response)) System.exit(0);
	else if("reverse".equalsIgnoreCase(response)) {
	  dicomImgFile.setCNUDataConversions(CNUDataConversions.getStandardReverseBytesConversions());
	  System.out.println("words now treated as litte endian(reversed)");
	}
	else if("noreverse".equalsIgnoreCase(response)) {
	  dicomImgFile.setCNUDataConversions(CNUDataConversions.getStandardNoConversions());
	  System.out.println("words now treated as big endian(native)");
	}
	else if("tag".equalsIgnoreCase(response)) {
	  System.out.println("tag at " + currentOffset + "=" +
			     DICOM_Tag.getDICOM_Tag(dataInput).toString());
	  currentOffset += 4;
	}
	else if("vr".equalsIgnoreCase(response)) {
	  try {
	    System.out.println("vr at " + currentOffset + "=" +
			       DICOM_VR.getDICOM_VR(dataInput).toString());
	  } catch (IllegalArgumentException iae) {
	    System.out.println(iae.toString());
	  }
	  currentOffset += 2;
	}
	else { // parameters requiring numbers
	  c = tokens.nextToken();
	  double nval = 0;
	  if(c == StreamTokenizer.TT_NUMBER) nval = tokens.nval;
	  else {
	    System.out.println("did not find number for command=" + response);
	    continue;
	  }
	  if("goto".equalsIgnoreCase(response) || "skip".equalsIgnoreCase(response)) {
	    int newOffset = (int) nval;
	    if("skip".equalsIgnoreCase(response)) newOffset += currentOffset;
	    if(newOffset < 0) {
	      System.out.println("can not go to negative location=" + newOffset);
	      continue;
	    }
	    else if(newOffset < currentOffset) {
	      System.out.println("re-openning file");
	      dataInput = dicomImgFile.getDataInput();
	      currentOffset = 0;
	    }
	    System.out.println("skipping to " + newOffset);
	    while(currentOffset < newOffset)
	      currentOffset += dataInput.skipBytes(newOffset - currentOffset);
	    System.out.println("offset =" + currentOffset);
	  }
	  else if("string".equalsIgnoreCase(response)) {
	    byte[] b = new byte[(int) nval];
	    dataInput.readFully(b);
	    System.out.println("string at " + currentOffset + "=\"" + new String(b) + '\"');
	    currentOffset += b.length;
	  }
	  else if("short".equalsIgnoreCase(response)) {
	    int length = (int) nval;
	    System.out.print("short values at " + currentOffset + "={");
	    System.out.print(dataInput.readUnsignedShort());
	    currentOffset += 2;
	    for(int i=1; i<length; i++) {
	      System.out.print("," + dataInput.readUnsignedShort());
	      currentOffset += 2;
	    }
	    System.out.println("}");
	  }
	  else if("int".equalsIgnoreCase(response)) {
	    int length = (int) nval;
	    System.out.print("int values at " + currentOffset + "={");
	    System.out.print(dataInput.readInt());
	    currentOffset += 4;
	    for(int i=1; i<length; i++) {
	      System.out.print("," + dataInput.readInt());
	      currentOffset += 4;
	    }
	    System.out.println("}");
	  }
	  else if("float".equalsIgnoreCase(response)) {
	    int length = (int) nval;
	    System.out.print("float values at " + currentOffset + "={");
	    System.out.print(dataInput.readFloat());
	    currentOffset += 4;
	    for(int i=1; i<length; i++) {
	      System.out.print("," + dataInput.readFloat());
	      currentOffset += 4;
	    }
	    System.out.println("}");
	  }
	  else if("double".equalsIgnoreCase(response)) {
	    int length = (int) nval;
	    System.out.print("double values at " + currentOffset + "={");
	    System.out.print(dataInput.readDouble());
	    currentOffset += 8;
	    for(int i=1; i<length; i++) {
	      System.out.print("," + dataInput.readDouble());
	      currentOffset += 8;
	    }
	    System.out.println("}");
	  }
	  else {
	    System.out.println("invalid input=" + response);
	    System.out.println("valid input=" + options);
	  }
	}
      } // end while
    }
    else {
      // read full file and print out the header
      dicomImgFile.readData();
      System.out.println(dicomImgFile.toString());
    }
  }
}
