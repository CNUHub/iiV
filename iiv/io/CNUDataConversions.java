package iiv.io;
import iiv.data.*;
import iiv.script.*;
/**
 * Manages conversion parameters.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		AnalyzeHeader
 * @since	iiV1.0
 */
public class CNUDataConversions implements CNUConversionTypes,
					   CNUTypesConstants, iiVScriptable {
  private int shortConvert = NO_CONVERSION;
  private int intConvert = NO_CONVERSION;
  private int floatConvert = NO_CONVERSION;
  private int longConvert = NO_CONVERSION;
  private int doubleConvert = NO_CONVERSION;
  private final static Object staticLock = new Object();
  private static CNUDataConversions standardNoConversions = null;
  /**
   * Gets a CNUDataConversions for converting VAX formatted data to SUN.
   *
   * @return CNUDataConversions for converting VAX formatted data to SUN.
   */
  public final static CNUDataConversions getStandardNoConversions() {
    if(standardNoConversions == null) synchronized(staticLock) {
      if(standardNoConversions == null) {
	CNUDataConversions cdc = new CNUDataConversions() {
	    private final void throwError() {
	      throw new IllegalArgumentException("Can't change values of default StandardVaxToSunConversions");
	    }
	    public final void setShortConvert() { throwError(); }
	    public final void setIntConvert() { throwError(); }
	    public final void setFloatConvert() { throwError(); }
	    public final void setLongConvert() { throwError(); }
	    public final void setDoubleConvert() { throwError(); }
	  };
	standardNoConversions = cdc;
      }
    }
    return standardNoConversions;
  }
  private static CNUDataConversions standardVaxToSunConversions = null;
  /**
   * Gets a CNUDataConversions for converting VAX formatted data to SUN.
   *
   * @return CNUDataConversions for converting VAX formatted data to SUN.
   */
  public final static CNUDataConversions getStandardVaxToSunConversions() {
    if(standardVaxToSunConversions == null) synchronized(staticLock) {
      if(standardVaxToSunConversions == null) {
	CNUDataConversions cdc = new CNUDataConversions() {
	    {
	      super.setShortConvert(VAX_TO_SUN);
	      super.setIntConvert(VAX_TO_SUN);
	      super.setFloatConvert(VAX_TO_SUN);
	      super.setLongConvert(VAX_TO_SUN);
	      super.setDoubleConvert(VAX_TO_SUN);
	    }
	    private final void throwError() {
	      throw new IllegalArgumentException("Can't change values of default StandardVaxToSunConversions");
	    }
	    public final void setShortConvert() { throwError(); }
	    public final void setIntConvert() { throwError(); }
	    public final void setFloatConvert() { throwError(); }
	    public final void setLongConvert() { throwError(); }
	    public final void setDoubleConvert() { throwError(); }
	  };
	standardVaxToSunConversions = cdc;
      }
    }
    return standardVaxToSunConversions;
  }
  private static CNUDataConversions standardReverseBytesConversions = null;
  /**
   * Gets a CNUDataConversions for converting VAX formatted data to SUN.
   *
   * @return CNUDataConversions for converting VAX formatted data to SUN.
   */
  public final static CNUDataConversions getStandardReverseBytesConversions() {
    if(standardReverseBytesConversions == null) synchronized(staticLock) {
      if(standardReverseBytesConversions == null) {
	CNUDataConversions cdc = new CNUDataConversions() {
	    {
	      super.setShortConvert(REVERSE_BYTES);
	      super.setIntConvert(REVERSE_BYTES);
	      super.setFloatConvert(REVERSE_BYTES);
	      super.setLongConvert(REVERSE_BYTES);
	      super.setDoubleConvert(REVERSE_BYTES);
	    }
	    private final void throwError() {
	      throw new IllegalArgumentException("Can't change values of default StandardVaxToSunConversions");
	    }
	    public final void setShortConvert() { throwError(); }
	    public final void setIntConvert() { throwError(); }
	    public final void setFloatConvert() { throwError(); }
	    public final void setLongConvert() { throwError(); }
	    public final void setDoubleConvert() { throwError(); }
	  };
	standardReverseBytesConversions = cdc;
      }
    }
    return standardReverseBytesConversions;
  }
  /**
   * Constructs a new instance of CNUDataConversions.
   *
   */
   public CNUDataConversions() {}
  /**
   * Constructs a new instance of CNUDataConversions duplicating the values
   * in the given CNUDataConversions.
   *
   * @param cnudc instance containing values to duplicate
   */
   public CNUDataConversions(CNUDataConversions cnudc) {
     if(cnudc != null) {
       shortConvert = cnudc.shortConvert;
       intConvert = cnudc.intConvert;
       floatConvert = cnudc.floatConvert;
       longConvert = cnudc.longConvert;
       doubleConvert = cnudc.doubleConvert;
     }
   }
   /**
   * Sets the conversion type for the given data type.
   *
   * @param dataType	type of data to set conversion for.
   *			If <code>UNKNOWN</code> sets
   *			all types.
   * @param convertType	conversion type to set
   */
   public void setConvert(int dataType, int convertType) {
     switch(dataType) {
     case BYTE:
     case UNSIGNED_BYTE:
       // no conversions for byte
       break;
     case SHORT:
     case UNSIGNED_SHORT:
       setShortConvert(convertType);
       break;
     case INTEGER:
       setIntConvert(convertType);
       break;
     case LONG:
       setLongConvert(convertType);
       break;
     case FLOAT:
       setFloatConvert(convertType);
       break;
     case DOUBLE:
       setDoubleConvert(convertType);
       break;
     case UNKNOWN:
       setShortConvert(convertType);
       setIntConvert(convertType);
       setLongConvert(convertType);
       setFloatConvert(convertType);
       setDoubleConvert(convertType);
       break;
     default:
       break;
     }
   }
   /**
   * Gets the conversion type for the given data type.
   *
   * @param dataType	type of data to get conversion for.
   * @return	conversion type for given data type
   */
   public int getConvert(int dataType) {
     switch(dataType) {
     default:
     case BYTE:
     case UNSIGNED_BYTE:
       return NO_CONVERSION;
     case SHORT:
     case UNSIGNED_SHORT:
       return shortConvert;
     case INTEGER:
       return intConvert;
     case LONG:
       return longConvert;
     case FLOAT:
       return floatConvert;
     case DOUBLE:
       return doubleConvert;
     }
   }
  /**
   * Sets the conversion type for input words of type short.
   *
   * @param convertType	conversion type
   */
  public void setShortConvert(int convertType) { shortConvert = convertType; }
  /**
   * Sets the conversion type for input words of type int.
   *
   * @param convertType	conversion type
   */
  public void setIntConvert(int convertType) { intConvert = convertType; }
  /**
   * Sets the conversion type for input words of type float.
   *
   * @param convertType	conversion type
   */
  public void setFloatConvert(int convertType) { floatConvert = convertType; }
  /**
   * Sets the conversion type for input words of type long.
   *
   * @param convertType	conversion type
   */
  public void setLongConvert(int convertType) { longConvert = convertType; }
  /**
   * Sets the conversion type for input words of type double.
   *
   * @param convertType	conversion type
   */
  public void setDoubleConvert(int convertType) {
    doubleConvert = convertType;
  }
  /**
   * Checks whether any conversions are needed.
   *
   * @return <code>true</code> if conversions are specified,
   * <code>false</code> otherwise.
   */
   public boolean getConversionsNeeded() {
     return
	(shortConvert != NO_CONVERSION) |
	(intConvert != NO_CONVERSION) |
	(floatConvert != NO_CONVERSION) |
	(longConvert != NO_CONVERSION) |
	(doubleConvert != NO_CONVERSION);
   }
  /**
   * Creates a script that may be used to recreate this display component.
   *
   * @param scriptedObjects scripted objects list to add this object to.
   * @return  the script
   */
  public String toScript(CNUScriptObjects scriptedObjects) {
    StringBuffer sb = new StringBuffer();
    String classname = getClass().getName();
    sb.append("// -- start ").append(classname).append(" script\n");

    if(scriptedObjects == null) scriptedObjects = new CNUScriptObjects();
    String variableName = scriptedObjects.get(this);
    if(variableName == null) {
	variableName = scriptedObjects.addObject(this, "cnudconv");

	sb.append(variableName).append(" = new ").append(classname).append("();\n");

	if(shortConvert != NO_CONVERSION) {
	    sb.append(variableName);
	    sb.append(".setShortConvert(");
	    sb.append(classname).append(".");
	    sb.append(convertTypeToString(shortConvert));
	    sb.append(");\n");
	}
	if(intConvert != NO_CONVERSION) {
	    sb.append(variableName);
	    sb.append(".setIntConvert(");
	    sb.append(classname).append(".");
	    sb.append(convertTypeToString(intConvert));
	    sb.append(");\n");
	}
	if(floatConvert != NO_CONVERSION) {
	    sb.append(variableName);
	    sb.append(".setFloatConvert(");
	    sb.append(classname).append(".");
	    sb.append(convertTypeToString(floatConvert));
	    sb.append(");\n");
	}
	if(longConvert != NO_CONVERSION) {
	    sb.append(variableName);
	    sb.append(".setLongConvert(");
	    sb.append(classname).append(".");
	    sb.append(convertTypeToString(longConvert));
	    sb.append(");\n");
	}
	if(doubleConvert != NO_CONVERSION) {
	    sb.append(variableName);
	    sb.append(".setDoubleConvert(");
	    sb.append(classname).append(".");
	    sb.append(convertTypeToString(doubleConvert));
	    sb.append(");\n");
	}
    }
    sb.append("script_rtn=").append(variableName).append(";\n");
    sb.append("// -- end ").append(classname).append(" script\n");
    return sb.toString();
  }
  /**
   * Use the short conversion type to convert a set of bytes
   * to short.
   *
   * @param b0 first byte
   * @param b1 first byte
   * @return short number created from the bytes
   * @exception  IllegalArgumentException thrown on invalid conversion type
   */
  public short bytesToShort(byte b0, byte b1) {
    switch (shortConvert) {
    case NO_CONVERSION:
      return (short) ( ((((int) b0) & 0xff) << 8) |
		       (((int) b1) & 0xff) );
    case SWAP_BYTES:
    case REVERSE_BYTES:
    case VAX_TO_SUN:
      return (short) ( ((((int) b1) & 0xff) << 8) |
		       (((int) b0) & 0xff) );
    default:
      throw new IllegalArgumentException("Conversion type=" +
					 convertTypeToString(shortConvert) +
					 " not implemented for short data");
    }
  }
  /**
   * Use the short conversion type to convert a set of bytes
   * to short.
   *
   * @param b array of bytes
   * @param offset offset to first byte of short
   * @return short number created from the bytes
   * @exception  IllegalArgumentException thrown on invalid conversion type
   */
  public short bytesToShort(byte[] b, int offset) {
    switch (shortConvert) {
    case NO_CONVERSION:
      return (short) ( ((((int) b[offset]) & 0xff) << 8) |
		       (((int) b[offset+1]) & 0xff) );
    case SWAP_BYTES:
    case REVERSE_BYTES:
    case VAX_TO_SUN:
      return (short) ( ((((int) b[offset+1]) & 0xff) << 8) |
		       (((int) b[offset]) & 0xff) );
    default:
      throw new IllegalArgumentException("Conversion type=" +
					 convertTypeToString(shortConvert) +
					 " not implemented for short data");
    }
  } 
  /**
   * Use the short conversion type to convert a set of bytes
   * to short.
   *
   * @param b0 first byte
   * @param b1 first byte
   * @return unsigned short number created from b0 and b1 stored as an int
   * @exception  IllegalArgumentException thrown on invalid conversion type
   */
  public int bytesToUnsignedShort(byte b0, byte b1) {
    return ((int) bytesToShort(b0, b1)) & 0xFFFF;
  }
  /**
   * Use the short conversion type to convert a set of bytes
   * to short.
   *
   * @param b array of bytes
   * @param offset offset to first byte of short
   * @return unsigned short number created from b0 and b1 stored as an int
   * @exception  IllegalArgumentException thrown on invalid conversion type
   */
  public int bytesToUnsignedShort(byte[] b, int offset) {
    return ((int) bytesToShort(b, offset)) & 0xFFFF;
  }
  /**
   * Use the integer conversion type to convert a set of bytes
   * to integer.
   *
   * @return number of bytes needed for convertion to int
   */
  public int bytesPerInt() {
    switch(intConvert) {
    case NO_CONVERSION:
    case SWAP_BYTES:
    case SWAP_SHORTS:
    case VAX_TO_SUN:
    case REVERSE_BYTES:
      return 4;
    case RGB24_TO_INT:
    case BGR24_TO_INT:
      return 3;
    default:
      throw new IllegalArgumentException("Conversion type=" +
					 convertTypeToString(intConvert) +
					 " not implemented for int data");
    }
  }
  /**
   * Use the integer conversion type to convert a set of bytes
   * to integer.
   *
   * @param b array of bytes
   * @param offset offset to first byte of short
   * @return integer number created from the bytes
   * @exception  IllegalArgumentException thrown on invalid conversion type
   */
  public int bytesToInt(byte[] b, int offset) {
    switch(intConvert) {
    case NO_CONVERSION:
      return (((((int) b[offset + 0]) & 0xff) << 24) |
	      ((((int) b[offset + 1]) & 0xff) << 16) |
	      ((((int) b[offset + 2]) & 0xff) << 8) |
	      (((int) b[offset + 3]) & 0xff));
    case SWAP_BYTES:
      // 0,1,2,3 -> 1,0,3,2
      return (((((int) b[offset + 1]) & 0xff) << 24) |
	      ((((int) b[offset + 0]) & 0xff) << 16) |
	      ((((int) b[offset + 3]) & 0xff) << 8) |
	      (((int) b[offset + 2]) & 0xff));
    case SWAP_SHORTS:
      // 0,1,2,3 -> 2,3,0,1
      return (((((int) b[offset + 2]) & 0xff) << 24) |
	      ((((int) b[offset + 3]) & 0xff) << 16) |
	      ((((int) b[offset + 0]) & 0xff) << 8) |
	      (((int) b[offset + 1]) & 0xff));
    case VAX_TO_SUN:
    case REVERSE_BYTES:
      // 0,1,2,3 -> 3,2,1,0
      return (((((int) b[offset + 3]) & 0xff) << 24) |
	      ((((int) b[offset + 2]) & 0xff) << 16) |
	      ((((int) b[offset + 1]) & 0xff) << 8) |
	      (((int) b[offset + 0]) & 0xff));
    case RGB24_TO_INT:
      // 0,1,2 -> ALPHA_255,1,2,3
      return (((255 & 0xff) << 24) |
	      ((((int) b[offset + 0]) & 0xff) << 16) |
	      ((((int) b[offset + 1]) & 0xff) << 8) |
	      (((int) b[offset + 2]) & 0xff));
    case BGR24_TO_INT:
      // 0,1,2 -> ALPHA_255,2,1,0
      return (((255 & 0xff) << 24) |
	      ((((int) b[offset + 2]) & 0xff) << 16) |
	      ((((int) b[offset + 1]) & 0xff) << 8) |
	      (((int) b[offset + 0]) & 0xff));
    default:
      throw new IllegalArgumentException("Conversion type=" +
					 convertTypeToString(intConvert) +
					 " not implemented for int data");
    }
  }
  /**
   * Use the long conversion type to convert a set of bytes
   * to long.
   *
   * @param b array of bytes
   * @param offset offset to first byte of short
   * @return long number created from the bytes
   * @exception  IllegalArgumentException thrown on invalid conversion type
   */
  public long bytesToLong(byte[] b, int offset) {
    switch (longConvert) {
    case NO_CONVERSION:
      return ((((long) b[offset + 0]) & 0xff) << 56) |
	((((long) b[offset + 1]) & 0xff) << 48) |
	((((long) b[offset + 2]) & 0xff) << 40) |
	((((long) b[offset + 3]) & 0xff) << 32) |
	((((long) b[offset + 4]) & 0xff) << 24) |
	((((long) b[offset + 5]) & 0xff) << 16) |
	((((long) b[offset + 6]) & 0xff) << 8) |
	(((long) b[offset + 7]) & 0xff );
    case SWAP_BYTES:
      // 0,1,2,3,4,5,6,7 -> 1,0,3,2,5,4,7,6
      return ((((long) b[offset + 1]) & 0xff) << 56) |
	((((long) b[offset + 0]) & 0xff) << 48) |
	((((long) b[offset + 3]) & 0xff) << 40) |
	((((long) b[offset + 2]) & 0xff) << 32) |
	((((long) b[offset + 5]) & 0xff) << 24) |
	((((long) b[offset + 4]) & 0xff) << 16) |
	((((long) b[offset + 7]) & 0xff) << 8) |
	(((long) b[offset + 6]) & 0xff );
    case SWAP_SHORTS:
      // 0,1,2,3,4,5,6,7 -> 2,3,0,1,6,7,4,5
      return ((((long) b[offset + 2]) & 0xff) << 56) |
	((((long) b[offset + 3]) & 0xff) << 48) |
	((((long) b[offset + 0]) & 0xff) << 40) |
	((((long) b[offset + 1]) & 0xff) << 32) |
	((((long) b[offset + 6]) & 0xff) << 24) |
	((((long) b[offset + 7]) & 0xff) << 16) |
	((((long) b[offset + 4]) & 0xff) << 8) |
	(((long) b[offset + 5]) & 0xff );
    case REVERSE_BYTES:
      // 0,1,2,3,4,5,6,7 -> 7,6,5,4,3,2,1,0
      return ((((long) b[offset + 7]) & 0xff) << 56) |
	((((long) b[offset + 6]) & 0xff) << 48) |
	((((long) b[offset + 5]) & 0xff) << 40) |
	((((long) b[offset + 4]) & 0xff) << 32) |
	((((long) b[offset + 3]) & 0xff) << 24) |
	((((long) b[offset + 2]) & 0xff) << 16) |
	((((long) b[offset + 1]) & 0xff) << 8) |
	(((long) b[offset + 0]) & 0xff );
    default:
      throw new IllegalArgumentException("Conversion type=" +
					 convertTypeToString(longConvert) +
					 " not implemented for long data");
    }
  }
  /**
   * Use the float conversion type to convert a set of bytes
   * to float.
   *
   * @param b array of bytes
   * @param offset offset to first byte of short
   * @return float number created from the bytes
   * @exception  IllegalArgumentException thrown on invalid conversion type
   */
  public float bytesToFloat(byte[] b, int offset) {
    switch (floatConvert) {
    case NO_CONVERSION:
      return Float.intBitsToFloat(((((int) b[offset + 0]) & 0xff) << 24) |
				  ((((int) b[offset + 1]) & 0xff) << 16) |
				  ((((int) b[offset + 2]) & 0xff) << 8) |
				  (((int) b[offset + 3]) & 0xff) );
    case SWAP_BYTES:
      // 0,1,2,3 -> 1,0,3,2
      return Float.intBitsToFloat(((((int) b[offset + 1]) & 0xff) << 24) |
				  ((((int) b[offset + 0]) & 0xff) << 16) |
				  ((((int) b[offset + 3]) & 0xff) << 8) |
				  (((int) b[offset + 2]) & 0xff) );
    case SWAP_SHORTS:
      // 0,1,2,3 -> 2,3,0,1
      return Float.intBitsToFloat(((((int) b[offset + 2]) & 0xff) << 24) |
				  ((((int) b[offset + 3]) & 0xff) << 16) |
				  ((((int) b[offset + 0]) & 0xff) << 8) |
				  (((int) b[offset + 1]) & 0xff) );
    case REVERSE_BYTES:
      // 0,1,2,3 -> 3,2,1,0
      return Float.intBitsToFloat(((((int) b[offset + 3]) & 0xff) << 24) |
				  ((((int) b[offset + 2]) & 0xff) << 16) |
				  ((((int) b[offset + 1]) & 0xff) << 8) |
				  ((int) b[offset + 0] & 0xff) );
    case VAX_TO_SUN:
      // swap  0,1,2,3 -> 1,0,3,2
      return vaxExpFixIntToFloat( ((((int) b[offset + 1]) & 0xff) << 24) |
				  ((((int) b[offset + 0]) & 0xff) << 16) |
				  ((((int) b[offset + 3]) & 0xff) << 8) |
				  (((int) b[offset + 2]) & 0xff) );
    default:
      throw new IllegalArgumentException("Conversion type=" +
					 convertTypeToString(floatConvert) +
					 " not implemented for float data");
    }
  }
  /**
   * Use the double conversion type to convert a set of bytes
   * to double.
   *
   * @param b array of bytes
   * @param offset offset to first byte of short
   * @return double number created from the bytes
   * @exception  IllegalArgumentException thrown on invalid conversion type
   */
  public double bytesToDouble(byte[] b, int offset) {
    switch (doubleConvert) {
    case NO_CONVERSION:
      return Double.longBitsToDouble( ((((long) b[offset + 0]) & 0xff) << 56) |
				      ((((long) b[offset + 1]) & 0xff) << 48) |
				      ((((long) b[offset + 2]) & 0xff) << 40) |
				      ((((long) b[offset + 3]) & 0xff) << 32) |
				      ((((long) b[offset + 4]) & 0xff) << 24) |
				      ((((long) b[offset + 5]) & 0xff) << 16) |
				      ((((long) b[offset + 6]) & 0xff) << 8) |
				      ((((long) b[offset + 7]) & 0xff )) );
    case SWAP_BYTES:
      // 0,1,2,3,4,5,6,7 -> 1,0,3,2,5,4,7,6
      return Double.longBitsToDouble( ((((long) b[offset + 1]) & 0xff) << 56) |
				      ((((long) b[offset + 0]) & 0xff) << 48) |
				      ((((long) b[offset + 3]) & 0xff) << 40) |
				      ((((long) b[offset + 2]) & 0xff) << 32) |
				      ((((long) b[offset + 5]) & 0xff) << 24) |
				      ((((long) b[offset + 4]) & 0xff) << 16) |
				      ((((long) b[offset + 7]) & 0xff) << 8) |
				      ((((long) b[offset + 6]) & 0xff)) );
    case SWAP_SHORTS:
      // 0,1,2,3,4,5,6,7 -> 2,3,0,1,6,7,4,5
      return Double.longBitsToDouble( ((((long) b[offset + 2]) & 0xff) << 56) |
				      ((((long) b[offset + 3]) & 0xff) << 48) |
				      ((((long) b[offset + 0]) & 0xff) << 40) |
				      ((((long) b[offset + 1]) & 0xff) << 32) |
				      ((((long) b[offset + 6]) & 0xff) << 24) |
				      ((((long) b[offset + 7]) & 0xff) << 16) |
				      ((((long) b[offset + 4]) & 0xff) << 8) |
				      ((((long) b[offset + 5]) & 0xff)) );
    case REVERSE_BYTES:
      // 0,1,2,3,4,5,6,7 -> 7,6,5,4,3,2,1,0
      return Double.longBitsToDouble( ((((long) b[offset + 7]) & 0xff) << 56) |
				      ((((long) b[offset + 6]) & 0xff) << 48) |
				      ((((long) b[offset + 5]) & 0xff) << 40) |
				      ((((long) b[offset + 4]) & 0xff) << 32) |
				      ((((long) b[offset + 3]) & 0xff) << 24) |
				      ((((long) b[offset + 2]) & 0xff) << 16) |
				      ((((long) b[offset + 1]) & 0xff) << 8) |
				      ((((long) b[offset + 0]) & 0xff)) );
    case VAX_TO_SUN:
    default:
      throw new IllegalArgumentException("Conversion type=" +
					 convertTypeToString(floatConvert) +
					 " not implemented for double data");
    }
  }
  /**
   * Convert vax floating point formatted bits to our floating point format.
   *
   * @param vaxBits properly ordered vax formatted bits
   * @return floating point number
   */
  public final static float vaxExpFixIntToFloat(int vaxBits) {
    if(vaxBits != 0) {    // don't mess up zero data
      // bit format seee eeee  emmm mmmm  mmmm mmmm  mmmm mmmm
      // s = sign, e = exponent, m = mantissa
      int exp = (vaxBits & 0x7f800000) >> 23;
      // decrease exponent by 2 -- I don't know why
      // kirt refers to something about VMS is excess 128 sun 127
      exp -= 2;
      // reapply exponent
      vaxBits = (vaxBits & 0x807fffff) | ((exp & 0x000000ff) << 23);
    }
    return Float.intBitsToFloat(vaxBits);
  }
  /**
   * Returns a String representing the conversion type.
   *
   * @param convertType conversion type
   * @return	 <code>true</code> if valid conversion type,
   *		 <code>false</code> otherwise
   */
  public final static boolean isValidConvertType(int convertType) {
    if((convertType < 0) || (convertType > CONVERSION_NAMES.length))
      return false;
    else return true;
  }
  /**
   * Returns a String representing the conversion type.
   *
   * @param convertType conversion type
   * @return string representation of conversion type
   */
  public final static String convertTypeToString(int convertType) {
    if(isValidConvertType(convertType)) return CONVERSION_NAMES[convertType];
    return "invalid";
  }
  /**
   * Returns a String representing this object.
   *
   * @return string representation of this object
   */
  public String toString() {
    return
	"shortConvert=" + convertTypeToString(shortConvert) +
	"\nintConvert=" + convertTypeToString(intConvert) +
	"\nfloatConvert=" + convertTypeToString(floatConvert) +
	"\nlongConvert=" + convertTypeToString(longConvert) +
	"\ndoubleConvert=" + convertTypeToString(doubleConvert);
  }
}
