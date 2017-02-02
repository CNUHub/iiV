package iiv.io;
import iiv.data.*;
import java.lang.*;
import java.io.*;
import java.util.*;
/**
 * DICOM_DataElement is a class to handle an element of DICOM data.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		DICOMImgFile
 */
public class DICOM_DataElement implements CNUTypesConstants {
  public static final int UNKNOWN_VR_MODE = 0;
  public static final int IMPLICIT_VR = 1;
  public static final int EXPLICIT_VR = 2;
  private DICOM_Tag tag;
  private DICOM_VR vr;
  private int vrMode = UNKNOWN_VR_MODE;
  private int storageType = UNKNOWN;  // may not be determined by vr
  private CNUDataConversions cdc = null;
  private long offset = 0;
  private int dataLength;
  private Object value = null;
  private int valueOffset = 0;
  private boolean valueRead = false;
  private int headerLength = 0;
  /**
   * Gets a new instance of DICOM_DataElement with values read from dataInput.
   *
   * @param dataInput input to initialize values from
   * @return new DICOM_DataElement
   * @exception  IOException thrown on error reading from dataInput
   */
  public static DICOM_DataElement getDICOM_DataElement(DataInput dataInput,
 						       long currentOffset,
						       int vrMode,
						       CNUDataConversions cdc,
						       Vector<String> ignoredFormatErrors)
    throws IOException {
    DICOM_DataElement dataElement = new DICOM_DataElement();
    dataElement.cdc = cdc;
    dataElement.tag = DICOM_Tag.getDICOM_Tag(dataInput);
    dataElement.headerLength += 4;
    dataElement.vr = dataElement.tag.getImplicitVR(); // vr implied by tag
    // the next 4 bytes may contain the length or the value representation
    byte[] b = new byte[4];
    dataInput.readFully(b);
    dataElement.headerLength += b.length;
    int dataLength = cdc.bytesToInt(b, 0);
    if(vrMode != IMPLICIT_VR) try {
      int explicitVr = ((b[0] << 8) & 0xFF00) | (b[1] & 0xFF);
      // try to interpret the explicit vr
      // - throws an IllegalArgumentException if VR value unknown
      dataElement.vr = DICOM_VR.getDICOM_VR(explicitVr);
      vrMode = EXPLICIT_VR;
      if((dataElement.vr == DICOM_VR.OB) || (dataElement.vr == DICOM_VR.OW) ||
	  (dataElement.vr == DICOM_VR.SQ) || (dataElement.vr == DICOM_VR.UN) ||
	 (dataElement.vr == DICOM_VR.UT)) {
	if((b[2] != 0) || (b[3] != 0))
	  throw new IllegalArgumentException("VR mode not followed by valid 0 bytes");
	dataLength = dataInput.readInt();
	dataElement.headerLength += 4;
	if(dataLength < 0)
	  throw new IOException("invalid (negative) explicit long data length");
      }
      else { dataLength = cdc.bytesToUnsignedShort(b, 2); }
    } catch (IllegalArgumentException iae) {
      if(vrMode == EXPLICIT_VR) 
	throw new IOException("invalid explicit vr - IllegalArgumentException=" +
			      iae.toString());;
      vrMode = IMPLICIT_VR;
    }
    dataElement.vrMode = vrMode;
    if(dataLength < 0) throw new IOException("invalid (negative) data length");
    // fix invalid data length problem
    if((dataLength & 1) != 0) {
      if(dataLength == 13) {
	// some known files have this invalid length and it should be 10
	dataLength= 10;
	if(ignoredFormatErrors != null)
	    ignoredFormatErrors.addElement(currentOffset +
					   " Invalid odd data length of 13 set to 10");
      }
      else if(ignoredFormatErrors != null)
	  ignoredFormatErrors.addElement(currentOffset + " Invalid odd data length ="
					 + dataLength);
    }
    dataElement.dataLength = dataLength;
    dataElement.offset = currentOffset;
    return dataElement;
  }
  /**
   * Get the number of bytes that were in this elements header.
   *
   * @return bytes read
   */
  public int getHeaderLength() { return headerLength; }
  /**
   * Sets the type used for internal storage of the data.
   *
   * @param type storage type
   */
  public void setStorageType(int type) { this.storageType = type; }
  /**
   * Gets the type used for internal storage of the data.
   *
   * @return type
   */
  public int getStorageType() {
    if(storageType != UNKNOWN) return storageType;
    if(vr != null) return vr.getStorageType();
    return UNKNOWN;
  };
  /**
   * Get the offset of this data element in the original file
   *
   * @return offset
   */
  public long getOffset() { return offset; }
  /**
   * Get the length in bytes of the value.
   *
   * @return length
   */
  public int getDataLength() { return dataLength; }
  /**
   * Set the length in bytes of the value.
   *
   * @param length length in bytes of the value
   */
    //  public void setDataLength(int length) { this.dataLength = length; }
  /**
   * Get the length in words of the value.
   *
   * @return length
   */
  public int getDataLengthInWords() {
    int words = dataLength / CNUTypes.bytesPerWord(getStorageType());
    return (words < 0) ? dataLength : words;
  }
  /**
   * Get the tag for this data element.
   *
   * @return tag
   */
  public DICOM_Tag getTag() { return tag; }
  /**
   * Get the value representation for this data element.
   *
   * @return tag
   */
  public DICOM_VR getVR() { return vr; }
  /**
   * Get the vr mode for this data element.
   *
   * @return vr mode
   */
  public int getVRMode() { return vrMode; }
  /**
   * Get the vr mode for this data element.
   *
   * @return vr mode
   */
  public static String vrModeToString(int vrMode) {
    switch(vrMode) {
    default:
    case UNKNOWN_VR_MODE:
      return "unknown_vr_mode";
    case IMPLICIT_VR:
      return "implicit_vr_mode";
    case EXPLICIT_VR:
      return "explicit_vr_mode";
    }
  }
  /**
   * Reads the value for this element.
   *
   * @param dataInput source to read value from
   * @return value read
   * @exception  IOException thrown on error reading from dataInput
   */
  public Object readValue(DataInput dataInput) throws IOException {
    return readValue(dataInput, null, 0, null);
  }
  /**
   * Reads the value for this element into an existing array - probably
   * to keep some related elements in the same array.
   *
   * @param dataInput source to read value from
   * @param array - existing array to put data into.
   * @return value read
   * @exception  IOException thrown on error reading from dataInput
   */
  public Object readValue(DataInput dataInput,
			  Object array, int arrayOffset,
			  Vector<String> ignoredFormatErrors) throws IOException {
    if(array == null) arrayOffset=0;
    value = readWords(dataInput, getStorageType(), getDataLengthInWords(),
		      getOffset(), getDataLength(), cdc, array, arrayOffset,
		      ignoredFormatErrors);
    if(value == array) valueOffset = arrayOffset;
    else valueOffset = 0;
    valueRead = (value != null);
    return value;
  }
  /**
   * Reads words from from a DataInput
   * This is DICOM specific because strings are stored as bytes
   * not 16 bit unicode characters and padded with nulls to even
   * byte lengths.
   *
   * @param dataInput input to read words from
   * @param storageType the type of data to read
   * @param totalBytes total number of bytes to read
   *                   (should be even bytes and multiple of word size)
   * @param outArray array to store read words in.  If <code>null</code> creates new array
   * @param outOffset offset location into outArray to start storing read words at
   * @return object an array (or string) of the storage type filled with read words
   * @exception IOException thrown on failure reading from dataInput
   */
  public static Object readWords(DataInput dataInput, int storageType,
				 int totalWords, long offset, int totalBytes,
				 CNUDataConversions cdc,
				 Object outArray, int outOffset,
				 Vector<String> ignoredFormatErrors)
    throws IOException {
    if(totalWords == 0) return null;
    if(outArray == null && outOffset != 0)
      throw new IOException("DICOM_DataElement::readWords - outArray null but outOffset not zero");

    int endIndex = totalWords + outOffset;

    switch(storageType) {
    case DICOM_DATA_ELEMENT:
      Vector<DICOM_DataElement> subElements = new Vector<DICOM_DataElement>();
      DICOMImgFile.readData(dataInput, cdc, offset, offset+totalBytes,
			    subElements, ignoredFormatErrors, true, null);
      return subElements;
    default:
    case UNKNOWN:
    case BYTE:
    case UNSIGNED_BYTE:
    case STRING:
      byte bytes[];
      if(outArray == null) bytes = new byte[totalWords];
      else if(outArray instanceof byte[]) bytes = (byte[])outArray;
      else throw new IOException("DICOM_DataElement::readWords - outArray not a byte array");

      dataInput.readFully(bytes, outOffset, totalWords);
      if(storageType == STRING) {
	int cnt = bytes.length;
	if(bytes[cnt + outOffset - 1] == 0) cnt--; // don't include null padding to even length
	return new String(bytes, outOffset, cnt);
      }
      else return bytes;
    case SHORT:
    case UNSIGNED_SHORT:
      short shorts[];
      if(outArray == null) shorts = new short[totalWords];
      else if(outArray instanceof short[]) shorts = (short[])outArray;
      else throw new IOException("DICOM_DataElement::readWords - outArray not a short array");

      for(int i=outOffset; i<endIndex; i++) shorts[i] = dataInput.readShort();
      return shorts;
    case INTEGER:
    case UNSIGNED_INTEGER:
      int integers[];
      if(outArray == null) integers = new int[totalWords];
      else if(outArray instanceof int[]) integers = (int[])outArray;
      else throw new IOException("DICOM_DataElement::readWords - outArray not an integer array");

      for(int i=outOffset; i<endIndex; i++) integers[i] = dataInput.readInt();
      return integers;
    case FLOAT:
      float floats[];
      if(outArray == null) floats = new float[totalWords];
      else if(outArray instanceof float[]) floats = (float[])outArray;
      else throw new IOException("DICOM_DataElement::readWords - outArray not an integer array");

      for(int i=outOffset; i<endIndex; i++) floats[i] = dataInput.readFloat();
      return floats;
    case DOUBLE:
      double doubles[];
      if(outArray == null) doubles = new double[totalWords];
      else if(outArray instanceof double[]) doubles = (double[])outArray;
      else throw new IOException("DICOM_DataElement::readWords - outArray not a double array");

      for(int i=outOffset; i<endIndex; i++) doubles[i] = dataInput.readDouble();
      return doubles;
    }
  }
  /**
   * Get the value for this data element which
   * will be null if not read.
   *
   * @return value read or <code>null</code> if not read.
   */
  public Object getValue() { return value; }
  /**
   * Get the offset to value in array if value is an array
   *
   * @return offset to value array
   */
  public int getValueOffset() { return valueOffset; }
  /**
   * Get the value for this data element as a long.
   *
   * @return value as long
   * @exception  NumberFormatException thrown if value not convertable to long
   */
  public long getLongValue() {
    if(value == null) throw new NumberFormatException("null data element value");
    int type = getStorageType();
    switch(type) {
    case STRING:
      // try parsing string as long
      return Long.parseLong(((String) value).trim());
    case DICOM_DATA_ELEMENT:
//      throw new NumberFormatException("Can not convert a data element to long");
    case UNKNOWN:
      return CNUTypes.getArrayValueAsLong(value, valueOffset, UNSIGNED_BYTE);
    default:
      return CNUTypes.getArrayValueAsLong(value, valueOffset, type);
    }
  }
  /**
   * Get the value for this data element as a double.
   *
   * @return value as double
   * @exception  NumberFormatException thrown if value not convertable to double
   */
  public double getDoubleValue() {
    if(value == null) throw new NumberFormatException("null data element value");
    int type = getStorageType();
    switch(type) {
    case STRING:
      return new Double(((String) value).trim()).doubleValue(); // try parsing string as double
    case DICOM_DATA_ELEMENT:
	//      throw new NumberFormatException("Can not convert a data element to double");
    case UNKNOWN:
      return CNUTypes.getArrayValueAsDouble(value, valueOffset, UNSIGNED_BYTE);
    default:
      return CNUTypes.getArrayValueAsDouble(value, valueOffset, type);
    }
  }
  /**
   * Skips the value for this element.
   *
   * @param dataInput source to read value from
   * @exception  IOException thrown on error reading from dataInput
   */
  public void skipValue(DataInput dataInput) throws IOException {
    dataInput.skipBytes(dataLength);
  }
  /**
   * Constructs a new instance of DICOM_DataElement.
   */
  private DICOM_DataElement() {}
  /**
   * Creates a string representation of this DICOM data element.
   *
   * @return string representation
   */
  public String toString() {
    return toString("");
  }
  /**
   * Creates a string representation of this DICOM data element.
   *
   * @return string representation
   */
  public String toString(String indent) {
    StringBuffer sb = new StringBuffer();
    if(indent == null) indent="";
    sb.append(indent);
    sb.append(offset);
    sb.append(" tag=").append(tag).append(" vr=").append(vr);
    sb.append(" vrMode=").append(vrModeToString(getVRMode()));
    sb.append(" storageType=" + CNUTypes.typeToString(getStorageType()));
    sb.append(" dataLength=").append(dataLength);
    if(valueRead) {
      sb.append(" value=");
      if(value == null) sb.append("shouldn't be null");
      else if(dataLength > 2048) sb.append("too long to show");
      else {
	int endIndex = valueOffset + getDataLengthInWords();
	sb.append('{');
	switch(getStorageType()) {
	default:
	case STRING:
	  sb.append(value);
	  break;
	case BYTE:
	  byte b[] = (byte[]) value;
	  for(int i = valueOffset; i < endIndex; i++) { if(i > valueOffset) sb.append(','); sb.append(b[i]); }
	  break;
	case DICOM_DATA_ELEMENT:
	  String subIndent = indent + "===>";
	  if(value instanceof DICOM_DataElement[]) {
	    DICOM_DataElement dde[] = (DICOM_DataElement[]) value;
	    for(int i = valueOffset; i < endIndex; i++) {
	      if(i > valueOffset) sb.append(',');
	      sb.append("\n"); // seperate sub data elements from previous lines and indent
	      sb.append(dde[i].toString(subIndent));
	    }
	    sb.append("\n"); // seperate data elements from ending bracket
	  }
	  else if(value instanceof Vector) {
	    boolean first=true;
	    Enumeration e = ((Vector) value).elements();
	    while(e.hasMoreElements()) {
	      if(! first) sb.append(',');
	      else first = false;
	      sb.append("\n"); //seperate sub data elements from previous lines and indent
	      sb.append(((DICOM_DataElement)e.nextElement()).toString(subIndent));
	    }
	    sb.append("\n"); // seperate data elements from ending bracket
	  } else if(value instanceof DICOM_DataElement) {
	    sb.append(((DICOM_DataElement) value).toString());
	  } else {
	      sb.append("**** DICOM_DataElement:toString() - unknown value for storage type DICOM_DATA_ELEMENT = " + value.toString());
	  }
	  break;
	case UNKNOWN:
	case UNSIGNED_BYTE:
	  byte ub[] = (byte[]) value;
	  for(int i = valueOffset; i < endIndex; i++) { if(i > valueOffset) sb.append(','); sb.append(((int) ub[i]) & 0xFF); }
	  break;
	case SHORT:
	  short s[] = (short[]) value;
	  for(int i = valueOffset; i < endIndex; i++) { if(i > valueOffset) sb.append(','); sb.append(s[i]); }
	  break;
	case UNSIGNED_SHORT:
	  short us[] = (short[]) value;
	  for(int i = valueOffset; i < endIndex; i++) { if(i > valueOffset) sb.append(','); sb.append(((int) us[i]) & 0xFFFF); }
	  break;
	case INTEGER:
	  int I[] = (int[]) value;
	  for(int i = valueOffset; i < endIndex; i++) { if(i > valueOffset) sb.append(','); sb.append(I[i]); }
	  break;
	case UNSIGNED_INTEGER:
	  int UI[] = (int[]) value;
	  for(int i = valueOffset; i < endIndex; i++) { if(i > valueOffset) sb.append(','); sb.append(((long) UI[i]) & 0x00FFFFFFFF); }
	  break;
	case FLOAT:
	  float f[] = (float[]) value;
	  for(int i = valueOffset; i < endIndex; i++) { if(i > valueOffset) sb.append(','); sb.append(f[i]); }
	  break;
	case DOUBLE:
	  double d[] = (double[]) value;
	  for(int i = valueOffset; i < endIndex; i++) { if(i > valueOffset) sb.append(','); sb.append(d[i]); }
	  break;
	}
	if(getVR() == DICOM_VR.UI) {
	  String uid[] = getUIDDescription((String) value);
	  sb.append(":").append(uid[1]).append(',').append(uid[2]);
	}
	sb.append('}');
      }
    }
    return sb.toString();
  }
  /**
   * Gets the description of a UID.
   *
   * @param UID Unique ID to get description of.
   * @return String array with first element containing UID, second the UID Name and
   *         third element containing UID Type
   */
  public static String[] getUIDDescription(String UID) {
    for(int i=0; i < UIDs.length; i++) if(UIDs[i].equals(UID)) return UIDs[i];
    return unknownUID;
  }
  public static final String unknownUID[] = { "uknown UID", "unknown UID name", "unknown UID type" };
  /** Registry of DICOM unique identifiers (UID) from DICOM 1.6 Document */
  private static final String UIDs[][] = {
    { "1.2.840.10008.1.1", "Verification SOP Class", "SOP Class" },
    { "1.2.840.10008.1.2", "Implicit VR Little Endian: Default Transfer Syntax for DICOM", "Transfer Syntax" },
    { "1.2.840.10008.1.2.1", "Explicit VR Little Endian", "Transfer Syntax" },
    { "1.2.840.10008.1.2.1.99", "Deflated Explicit VR Little Endian", "Transfer Syntax" },
    { "1.2.840.10008.1.2.2", "Explicit VR Big Endian", "Transfer Syntax" },
    { "1.2.840.10008.1.2.4.50", "JPEG Baseline (Process 1):  Default Transfer Syntax for Lossy JPEG 8 Bit Image Compression", "Transfer Syntax" },
    { "1.2.840.10008.1.2.4.51", "JPEG Extended (Process 2 & 4):  Default Transfer Syntax for Lossy JPEG 12 Bit Image Compression (Process 4 only)", "Transfer Syntax" },
    { "1.2.840.10008.1.2.4.52", "JPEG Extended (Process 3 & 5)", "Transfer Syntax" },
    { "1.2.840.10008.1.2.4.53", "JPEG Spectral Selection, Non-Hierarchical (Process 6 & 8)", "Transfer Syntax" },
    { "1.2.840.10008.1.2.4.54 JPEG Spectral Selection, Non-Hierarchical (Process 7 & 9)", "Transfer Syntax" },
    { "1.2.840.10008.1.2.4.55 JPEG Full Progression, Non-Hierarchical (Process 10 & 12)", "Transfer Syntax" },
    { "1.2.840.10008.1.2.4.56 JPEG Full Progression, Non-Hierarchical (Process 11 & 13)", "Transfer Syntax" },
    { "1.2.840.10008.1.2.4.57 JPEG Lossless, Non-Hierarchical (Process 14)", "Transfer Syntax" },
    { "1.2.840.10008.1.2.4.58 JPEG Lossless, Non-Hierarchical (Process 15)", "Transfer Syntax" },
    { "1.2.840.10008.1.2.4.59 JPEG Extended, Hierarchical (Process 16 & 18)", "Transfer Syntax" },
    { "1.2.840.10008.1.2.4.60 JPEG Extended, Hierarchical (Process 17 & 19)", "Transfer Syntax" },
    { "1.2.840.10008.1.2.4.61 JPEG Spectral Selection, Hierarchical (Process 20 & 22)", "Transfer Syntax" },
    { "1.2.840.10008.1.2.4.62 JPEG Spectral Selection, Hierarchical (Process 21 & 23)", "Transfer Syntax" },
    { "1.2.840.10008.1.2.4.63 JPEG Full Progression, Hierarchical (Process 24 & 26)", "Transfer Syntax" },
    { "1.2.840.10008.1.2.4.64 JPEG Full Progression, Hierarchical (Process 25 & 27)", "Transfer Syntax" },
    { "1.2.840.10008.1.2.4.65 JPEG Lossless, Hierarchical (Process 28)", "Transfer Syntax" },
    { "1.2.840.10008.1.2.4.66 JPEG Lossless, Hierarchical (Process 29)", "Transfer Syntax" },
    { "1.2.840.10008.1.2.4.70 JPEG Lossless, Non-Hierarchical, First-Order Prediction (Process 14 [Selection Value 1]): Default Transfer Syntax for Lossless JPEG Image Compression", "Transfer Syntax" },
    { "1.2.840.10008.1.2.4.80 JPEG-LS Lossless Image Compression", "Transfer Syntax" },
    { "1.2.840.10008.1.2.4.81 JPEG-LS Lossy (Near-Lossless) Image Compression", "Transfer Syntax" },
    { "1.2.840.10008.1.2.5 RLE Lossless Transfer Syntax" },
    { "1.2.840.10008.1.3.10 Media Storage Directory Storage SOP Class" },
    { "1.2.840.10008.1.9 Basic Study Content Notification SOP Class", "SOP Class" },
    { "1.2.840.10008.1.20.1 Storage Commitment Push Model SOP Class", "SOP Class" },
    { "1.2.840.10008.1.20.1.1 Storage Commitment Push Model SOP Instance", "Well-known SOP Instance" },
    { "1.2.840.10008.1.20.2 Storage Commitment Pull Model SOP Class", "SOP Class" },
    { "1.2.840.10008.1.20.2.1 Storage Commitment Pull Model SOP Instance", "Well-known SOP Instance" },
    { "1.2.840.10008.3.1.1.1 DICOM Application Context Name", "Application Context Name" },
    { "1.2.840.10008.3.1.2.1.1 Detached Patient Management SOP Class", "SOP Class" },
    { "1.2.840.10008.3.1.2.1.4 Detached Patient Management Meta SOP Class", "Meta SOP Class" },
    { "1.2.840.10008.3.1.2.2.1 Detached Visit Management SOP Class", "SOP Class" },
    { "1.2.840.10008.3.1.2.3.1 Detached Study Management SOP Class", "SOP Class" },
    { "1.2.840.10008.3.1.2.3.2 Study Component Management SOP Class", "SOP Class" },
    { "1.2.840.10008.3.1.2.3.3 Modality Performed Procedure Step SOP Class", "SOP Class" },
    { "1.2.840.10008.3.1.2.3.4 Modality Performed Procedure Step Retrieve SOP Class", "SOP Class" },
    { "1.2.840.10008.3.1.2.3.5 Modality Performed Procedure Step Notification SOP Class", "SOP Class" },
    { "1.2.840.10008.3.1.2.5.1 Detached Results Management SOP Class", "SOP Class" },
    { "1.2.840.10008.3.1.2.5.4 Detached Results Management Meta SOP Class", "Meta SOP Class" },
    { "1.2.840.10008.3.1.2.5.5 Detached Study Management Meta SOP Class", "Meta SOP Class" },
    { "1.2.840.10008.3.1.2.6.1 Detached Interpretation Management SOP Class", "SOP Class" },
    { "1.2.840.10008.5.1.1.1 Basic Film Session SOP Class", "SOP Class" },
    { "1.2.840.10008.5.1.1.2 Basic Film Box SOP Class", "SOP Class" },
    { "1.2.840.10008.5.1.1.4 Basic Grayscale Image Box SOP Class", "SOP Class" },
    { "1.2.840.10008.5.1.1.4.1 Basic Color Image Box SOP Class", "SOP Class" },
    { "1.2.840.10008.5.1.1.4.2 Referenced Image Box SOP Class (Retired)", "SOP Class" },
    { "1.2.840.10008.5.1.1.9 Basic Grayscale Print Management Meta SOP Class", "Meta SOP Class" },
    { "1.2.840.10008.5.1.1.9.1 Referenced Grayscale Print Management Meta SOP Class (Retired)", "Meta SOP Class" },
    { "1.2.840.10008.5.1.1.14 Print Job SOP Class SOP Class`" },
    { "1.2.840.10008.5.1.1.15 Basic Annotation Box SOP Class", "SOP Class" },
    { "1.2.840.10008.5.1.1.16 Printer SOP Class", "SOP Class" },
    { "1.2.840.10008.5.1.1.16.376 Printer Configuration Retrieval SOP Class", "SOP Class" },
    { "1.2.840.10008.5.1.1.17 Printer SOP Instance", "Well-known Printer SOP Instance" },
    { "1.2.840.10008.5.1.1.17.376 Printer Configuration Retrieval SOP Instance", "Well-known Printer SOP Instance" },
    { "1.2.840.10008.5.1.1.18 Basic Color Print Management Meta SOP Class", "Meta SOP Class" },
    { "1.2.840.10008.5.1.1.18.1 Referenced Color Print Management Meta SOP Class (Retired)", "Meta SOP Class" },
    { "1.2.840.10008.5.1.1.22 VOI LUT Box SOP Class", "SOP Class" },
    { "1.2.840.10008.5.1.1.23 Presentation LUT SOP Class", "SOP Class" },
    { "1.2.840.10008.5.1.1.24 Image Overlay Box SOP Class (Retired)", "SOP Class" },
    { "1.2.840.10008.5.1.1.24.1 Basic Print Image Overlay Box SOP Class", "SOP Class" },
    { "1.2.840.10008.5.1.1.25 Print Queue SOP Instance", "Well-known Print Queue SOP Instance" },
    { "1.2.840.10008.5.1.1.26 Print Queue Management SOP", "Class SOP Class" },
    { "1.2.840.10008.5.1.1.27 Stored Print Storage SOP Class", "SOP Class" },
    { "1.2.840.10008.5.1.1.29 Hardcopy Grayscale Image Storage SOP Class", "SOP Class" },
    { "1.2.840.10008.5.1.1.30 Hardcopy Color Image Storage SOP Class", "SOP Class" },
    { "1.2.840.10008.5.1.1.31 Pull Print Request SOP Class", "SOP Class" },
    { "1.2.840.10008.5.1.1.32 Pull Stored Print Management Meta SOP Class", "Meta SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.1 Computed Radiography Image Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.1.1 Digital X-Ray Image Storage - For Presentation", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.1.1.1 Digital X-Ray Image Storage - For Processing", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.1.2 Digital Mammography X-Ray Image Storage - For Presentation", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.1.2.1 Digital Mammography X-Ray Image Storage - For Processing", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.1.3 Digital Intra-oral X-Ray Image Storage - For Presentation", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.1.3.1 Digital Intra-oral X-Ray Image Storage - For Processing", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.2 CT Image Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.3 Ultrasound Multi-frame Image Storage (Retired)", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.3.1 Ultrasound Multi-frame Image Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.4 MR Image Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.5 Nuclear Medicine Image Storage (Retired)", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.6 Ultrasound Image Storage (Retired)", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.6.1 Ultrasound Image Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.7 Secondary Capture Image Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.7.1 Multi-frame Single Bit Secondary Capture Image Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.7.2 Multi-frame Grayscale Byte Secondary Capture Image Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.7.3 Multi-frame Grayscale Word Secondary Capture Image Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.7.4 Multi-frame True Color Secondary Capture Image Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.8 Standalone Overlay Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.9 Standalone Curve Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.9.1.1 12-lead ECG Waveform Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.9.1.2 General ECG Waveform Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.9.1.3 Ambulatory ECG Waveform Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.9.2.1 Hemodynamic Waveform Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.9.3.1 Cardiac Electrophysiology Waveform Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.9.4.1 Basic Voice Audio Waveform Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.10 Standalone Modality LUT Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.11 Standalone VOI LUT Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.11.1 Grayscale Softcopy Presentation State Storage SOP Class", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.12.1 X-Ray Angiographic Image Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.12.2 X-Ray Radiofluoroscopic Image Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.12.3 X-Ray Angiographic Bi-Plane Image Storage (Retired)", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.20 Nuclear Medicine Image Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.77.1 VL Image Storage (Retired)", "", },
    { "1.2.840.10008.5.1.4.1.1.77.2 VL Multi-frame Image Storage (Retired)", "",},
    { "1.2.840.10008.5.1.4.1.1.77.1.1 VL Endoscopic Image Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.77.1.2 VL Microscopic Image Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.77.1.3 VL Slide-Coordinates Microscopic Image Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.77.1.4 VL Photographic Image Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.88.11 Basic Text SR", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.88.22 Enhanced SR", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.88.33 Comprehensive SR", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.88.50 Mammography CAD SR", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.88.59 Key Object Selection Document", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.128 Positron Emission Tomography Image Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.129 Standalone PET Curve Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.481.1 RT Image Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.481.2 RT Dose Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.481.3 RT Structure Set Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.481.4 RT Beams Treatment Record Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.481.5 RT Plan Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.481.6 RT Brachy Treatment Record Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.1.481.7 RT Treatment Summary Record Storage", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.2.1.1 Patient Root Query/Retrieve Information Model - FIND", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.2.1.2 Patient Root Query/Retrieve Information Model - MOVE", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.2.1.3 Patient Root Query/Retrieve Information Model - GET", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.2.2.1 Study Root Query/Retrieve Information Model - FIND", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.2.2.2 Study Root Query/Retrieve Information Model - MOVE", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.2.2.3 Study Root Query/Retrieve Information Model - GET", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.2.3.1 Patient/Study Only Query/Retrieve Information Model - FIND", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.2.3.2 Patient/Study Only Query/Retrieve Information Model - MOVE", "SOP Class" },
    { "1.2.840.10008.5.1.4.1.2.3.3 Patient/Study Only Query/Retrieve Information Model - GET", "SOP Class" },
    { "1.2.840.10008.5.1.4.31 Modality Worklist Information Model - FIND", "SOP Class" },
    { "1.2.840.10008.5.1.4.32.1 General Purpose Worklist Information Model - FIND", "SOP Class" },
    { "1.2.840.10008.5.1.4.32.2 General Purpose Scheduled Procedure Step SOP Class", "SOP Class" },
    { "1.2.840.10008.5.1.4.32.3 General Purpose Performed Procedure Step SOP Class", "SOP Class" },
    { "1.2.840.10008.5.1.4.32 General Purpose Worklist", "Meta", "SOP Class" }
  };
}
