package iiv.data;
/**
 * A utility class for working with different data types.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 */
public class CNUTypes implements CNUTypesConstants {
  /**
   * Checks whether a int is a valid data type.
   *
   * @param type value to check validity of
   * @return <code>true</code> if valid data type, <code>false</code> otherwise
   */
  public final static boolean valid(int type) {
    switch (type) {
    case BYTE:
    case UNSIGNED_BYTE:
    case SHORT:
    case UNSIGNED_SHORT:
    case INTEGER:
    case UNSIGNED_INTEGER:
    case LONG:
    case FLOAT:
    case DOUBLE:
      return true;
    default:
      return false;
    }
  }
  /**
   * Returns the maximum value a word of the given data type can hold.
   *
   * @param type	CNU data type
   * @return the	maximum value the data type can hold
   */
  public final static double maxValue(int type) {
    switch (type) {
    case BYTE:
      return BYTE_MAX_VALUE;
    case UNSIGNED_BYTE:
      return UNSIGNED_BYTE_MAX_VALUE;
    case SHORT:
      return SHORT_MAX_VALUE;
    case UNSIGNED_SHORT:
      return UNSIGNED_SHORT_MAX_VALUE;
    case INTEGER:
      return Integer.MAX_VALUE;
    case UNSIGNED_INTEGER:
      return UNSIGNED_INTEGER_MAX_VALUE;
    case LONG:
      return Long.MAX_VALUE;
    case FLOAT:
      return Float.MAX_VALUE;
    case DOUBLE:
      return Double.MAX_VALUE;
    default:
      return Double.NaN;
    }
  }
  /**
   *  Returns the minimum value a word of given data type can hold.
   *
   * @param type	CNU data type
   * @return the	minimum value the data type can hold
   */
  public final static double minValue(int type) {
    switch (type) {
    case BYTE:
      return BYTE_MIN_VALUE;
    case UNSIGNED_BYTE:
      return UNSIGNED_BYTE_MIN_VALUE;
    case SHORT:
      return SHORT_MIN_VALUE;
    case UNSIGNED_SHORT:
      return UNSIGNED_SHORT_MIN_VALUE;
    case INTEGER:
      return Integer.MIN_VALUE;
    case UNSIGNED_INTEGER:
      return UNSIGNED_INTEGER_MIN_VALUE;
    case LONG:
      return Long.MIN_VALUE;
    case FLOAT:
      return -Float.MAX_VALUE;
    case DOUBLE:
      return -Double.MAX_VALUE;
    default:
      return Double.NaN;
    }
  }
  /**
   * Returns number of bytes in one word of the given data type.
   *
   * @param type	CNU data type
   * @return the	number of bytes per word
   */
  public final static int bytesPerWord(int type) {
    switch (type) {
    case BYTE:
    case UNSIGNED_BYTE:
      return(1);
    case SHORT:
    case UNSIGNED_SHORT:
      return(2);
    case INTEGER:
    case UNSIGNED_INTEGER:
    case FLOAT:
      return(4);
    case LONG:
    case DOUBLE:
      return(8);
    default:
      return(-1);
    }
  }
  /**
   * Returns an array to hold data of the given data type.
   * Written to deal consistently with unsigned byte and unsigned short
   * values.
   *
   * @param type	CNU data type
   * @param size	size of the desired array
   * @return		a data array
   */
  public final static Object arrayOf(int type, int size) {
    switch (type) {
    case BYTE:
    case UNSIGNED_BYTE:
      return (Object) new byte[size];
    case SHORT:
    case UNSIGNED_SHORT:
      return (Object) new short[size];
    case INTEGER:
    case UNSIGNED_INTEGER:
      return (Object) new int[size];
    case FLOAT:
      return (Object) new float[size];
    case LONG:
      return (Object) new long[size];
    case DOUBLE:
      return (Object) new double[size];
    default:
      return null;
    }
  }
  /**
   * Gets an integer value from an array of the given data type.
   * Written to deal consistently with unsigned byte and unsigned short
   * values.
   *
   * @param Array	array containing data of the given type
   * @param index	index to the requested word
   * @param type	CNU data type 
   * @return		value from array
   */
  public final static int getArrayValueAsInt(Object Array, int index,
					     int type) {
    switch (type) {
    case BYTE:
      return (int)(((byte [])Array)[index]);
    case UNSIGNED_BYTE:
      return UnsignedByteToInt(((byte [])Array)[index]);
    case SHORT:
      return (int)(((short [])Array)[index]);
    case UNSIGNED_SHORT:
      return UnsignedShortToInt(((short [])Array)[index]);
    case INTEGER:
    case UNSIGNED_INTEGER:
      return ((int [])Array)[index];
    case FLOAT:
      return Math.round(((float [])Array)[index]);
    case LONG:
      return (int)(((long [])Array)[index]);
    case DOUBLE:
      return (int)Math.round(((double [])Array)[index]);
    default:
      return Integer.MIN_VALUE;
    }
  }
  /**
   * Gets an long value from an array of the given data type.
   * Written to deal consistently with unsigned byte and unsigned short
   * values.
   *
   * @param Array	array containing data of the given type
   * @param index	index to the requested word
   * @param type	CNU data type 
   * @return		value from array
   */
  public final static long getArrayValueAsLong(Object Array, int index,
					       int type) {
    switch (type) {
    case BYTE:
      return (long)(((byte [])Array)[index]);
    case UNSIGNED_BYTE:
      return (long)UnsignedByteToInt(((byte [])Array)[index]);
    case SHORT:
      return (long)(((short [])Array)[index]);
    case UNSIGNED_SHORT:
      return (long)UnsignedShortToInt(((short [])Array)[index]);
    case INTEGER:
      return (long)((int [])Array)[index];
    case UNSIGNED_INTEGER:
      return ((long)((int [])Array)[index]) & 0xFFFFFFFF;
    case FLOAT:
      return (long)Math.round(((float [])Array)[index]);
    case LONG:
      return (((long [])Array)[index]);
    case DOUBLE:
      return Math.round(((double [])Array)[index]);
    default:
      return Long.MIN_VALUE;
    }
  }
  /**
   * Sets the value in an array of given data type from a integer value.
   *
   * @param value	value to set array location to
   * @param Array	array containing data of the given type
   * @param index	location in array to set
   * @param type	CNU data type 
   */
  public final static void setArrayValue( int value, Object Array,
					  int index, int type) {
    switch (type) {
    case BYTE:
      // version that truncates
      ((byte [])Array)[index] = IntToByte(value);
      return;
    case UNSIGNED_BYTE:
      // note - this truncates producing weird values for things out of range
      //      ((byte [])Array)[index] = (byte) value;
      // replace with version that clips
      ((byte [])Array)[index] = IntToUnsignedByte(value);
      return;
    case SHORT:
    case UNSIGNED_SHORT:
      // note - this truncates producing weird values for things out of range
      ((short []) Array)[index] = (short) value;
      return;
    case INTEGER:
    case UNSIGNED_INTEGER:
      ((int []) Array)[index] = value;
      return;
    case FLOAT:
      ((float []) Array)[index] = (float) value;
      return;
    case LONG:
      ((long []) Array)[index] = (long) value;
      return;
    case DOUBLE:
      ((double []) Array)[index] = (double) value;
      return;
    default:
      return;
    }
  }
  /**
   * Retrieves a double value from an array of the given type.
   *
   * @param Array	array containing data of the given type
   * @param index	index to the requested word
   * @param type	CNU data type 
   * @return		value from array
   */
  public final static double getArrayValueAsDouble(Object Array,
						   int index, int type) {
    switch (type) {
    case BYTE:
      return (double)(((byte [])Array)[index]);
    case UNSIGNED_BYTE:
      return (double)
	UnsignedByteToInt(((byte [])Array)[index]);
    case SHORT:
      return (double)(((short [])Array)[index]);
    case UNSIGNED_SHORT:
      return (double)
	UnsignedShortToInt(((short [])Array)[index]);
    case INTEGER:
      return (double)((int [])Array)[index];
    case UNSIGNED_INTEGER:
      return (double)( ((int [])Array)[index] & 0xFFFFFFFF);
    case FLOAT:
      return (double)(((float [])Array)[index]);
    case LONG:
      return (double)(((long [])Array)[index]);
    case DOUBLE:
      return ((double [])Array)[index];
    default:
      return Double.NaN;
    }
  }
  /**
   * Sets the value in an array of type from a double value.
   *
   * @param value	value to set array location to
   * @param Array	array containing data of the given type
   * @param index	location in array to set
   * @param type	CNU data type 
   */
  public final static void setArrayValue(double value,
					 Object Array, int index, int type) {
    switch (type) {
    case BYTE:
      ((byte [])Array)[index] = IntToByte( (int) Math.round(value) );
      return;
    case UNSIGNED_BYTE:
      ((byte [])Array)[index] = IntToUnsignedByte( (int) Math.round(value) );
      return;
    case SHORT:
    case UNSIGNED_SHORT:
      // note - this truncates producing weird values for things out of range
      ((short []) Array)[index] = (short) Math.round(value);
      return;
    case INTEGER:
    case UNSIGNED_INTEGER:
      // note - this truncates producing weird values for things out of range
      ((int []) Array)[index] = (int) Math.round(value);
      return;
    case FLOAT:
      ((float []) Array)[index] = (float) value;
      return;
    case LONG:
      ((long []) Array)[index] = Math.round(value);
      return;
    case DOUBLE:
      ((double []) Array)[index] = value;
      return;
    default:
      return;
    }
  }
  /**
   * Generates a string representation for the given data type.
   *
   * @param type	CNU data type
   * @return		String representing the CNU data type
   */
  public final static String typeToString( int type ) {
    switch (type) {
    case BYTE:
      return "byte";
    case UNSIGNED_BYTE:
      return "unsigned_byte";
    case SHORT:
      return "short";
    case UNSIGNED_SHORT:
      return "unsigned_short";
    case INTEGER:
      return "integer";
    case UNSIGNED_INTEGER:
      return "unsigned_integer";
    case FLOAT:
      return "float";
    case LONG:
      return "long";
    case DOUBLE:
      return "double";
    case STRING:
      return "string";
    case DICOM_DATA_ELEMENT:
      return "dicom_data_element";
    default:
      return "unknown";
    }
  }
  /**
   * Gets the CNU data type from a string representation.
   *
   * @param typeString	string representation of a CNU data type
   * @return		CNU type or <code>UNKNOWN</code> if invalid string
   */
  public static final int typeValueOf( String typeString ) {
    typeString = typeString.trim();
    if("byte".equalsIgnoreCase(typeString)) return BYTE;
    else if("unsigned_byte".equalsIgnoreCase(typeString)) return UNSIGNED_BYTE;
    else if("short".equalsIgnoreCase(typeString)) return SHORT;
    else if("unsigned_short".equalsIgnoreCase(typeString)) return UNSIGNED_SHORT;
    else if("integer".equalsIgnoreCase(typeString)) return INTEGER;
    else if("unsigned_integer".equalsIgnoreCase(typeString)) return UNSIGNED_INTEGER;
    else if("int".equalsIgnoreCase(typeString)) return INTEGER;
    else if("float".equalsIgnoreCase(typeString)) return FLOAT;
    else if("long".equalsIgnoreCase(typeString)) return LONG;
    else if("double".equalsIgnoreCase(typeString)) return DOUBLE;
    else if("dble".equalsIgnoreCase(typeString)) return DOUBLE;
    else if("string".equalsIgnoreCase(typeString)) return STRING;
    else if("dicom_data_element".equalsIgnoreCase(typeString)) return DICOM_DATA_ELEMENT;
    else return UNKNOWN;
  }
  /**
   * Creates a character from a byte correcting for null characters.
   *
   * @return	non-null character
   */
  public final static char byteToChar( byte b ) {
    if( b == 0 ) return ' ';
    else return (char) b;
  }
  /**
   * Converts an integer to byte with clamping instead of truncation.
   *
   * @param value	integer to convert
   * @return		clamped byte value
   */
  public final static byte IntToByte(int value) {
    // largest value of byte is 127
    if(value > 127)return (byte) 127;
    // smallest value is -128
    else if(value < -128)return (byte) -128;
    return (byte) value;
  }
  /**
   * Converts an integer to unsigned byte with clamping instead
   * of truncation.
   *
   * @param value	integer to convert
   * @return		clamped unsigned byte value
   */
  public final static byte IntToUnsignedByte(int value) {
    // note - java only supports signed int but casting would still work for
    // integers from 0 to 255
    // largest value of unsigned is 255 stored as -1 in signed byte
    if(value >= 255)return (byte) 255; // = -1 signed byte
    // smallest value is 0
    else if(value <= 0)return (byte) 0;
    // a straight cast works for in range values from 0 to 255
    return (byte) value;
  }
  /**
   * Converts an unsigned byte value to an integer.
   * Bote - java only supports signed byte so casting produces the
   * wrong results for values greater then 127.
   * Unsigned values from 128 to 255 are seen as
   * -128 to -1 when stored as signed byte.
   *
   * @param b	unsigned byte to convert
   * @return	integer value
   */
  public final static int UnsignedByteToInt(byte b) {
    return 0xff & (int)b; // remove 2's compliment sign bits
  }
  /**
   * Converts an unsigned short value to integer.
   * Note - java only supports signed short so casting would produce the
   * wrong results for values greater then 32767.
   * Unsigned values from 32768 to 65535 are seen as
   * -32768 to -1 when stored as signed short.
   */
  public final static int UnsignedShortToInt(short b) {
    return 0xffff & (int)b; // remove 2's compliment sign bits
  }
  /**
   * Copies an array of one data type into an array of possibly another
   * input type.  Data from the input array can be undersampled or
   * reverse sampled by specifying an in increment other then one.
   *
   * @param inarray	input data array
   * @param inoffset	location of input array to begin copying from
   * @param intype	CNU data type of input array
   * @param inInc	amount to increment to get to the next input word
   *			after copying each word
   * @param outarray	output data array
   * @param outoffset	location of output array to begin copying to
   * @param outtype	CNU data type of output array
   * @param cnt		number of words to copy from input to output
   * @param sc		CNUScale class describing linear translation and
   *			scaling to apply to words copied.
   */
  public final static void
  copyArray( Object inarray, int inoffset,
	     int intype, int inInc,
	     Object outarray, int outoffset, int outtype,
	     int cnt, CNUScale sc) {
    int outindex = outoffset;
    int endoutindex = outindex + cnt;
    int inindex = inoffset;
    boolean noscale = (sc == null)? true : sc.identity();
    if((outtype == intype) && noscale && (inInc == 1)) {
      System.arraycopy(inarray, inoffset, outarray,
		       outoffset, cnt);
      return;
    }
    switch (outtype) {
    case BYTE:
      if(noscale)
	for(; outindex < endoutindex; outindex++,
	      inindex += inInc) {
	  ((byte [])outarray)[outindex] =
	     IntToByte(getArrayValueAsInt(inarray, inindex, intype));
	}
      else
	for(; outindex < endoutindex; outindex++,
	      inindex += inInc) {
	  ((byte [])outarray)[outindex] = 
	    IntToByte(
	   sc.convert_to_int(getArrayValueAsDouble(inarray, inindex, intype)));
	}
      return;
    case UNSIGNED_BYTE:
      if(noscale)
	for(; outindex < endoutindex; outindex++,
	      inindex += inInc) {
	  ((byte [])outarray)[outindex] =
	     IntToUnsignedByte(getArrayValueAsInt(inarray, inindex, intype));
	}
      else
	for(; outindex < endoutindex; outindex++,
	      inindex += inInc) {
	  ((byte [])outarray)[outindex] = 
	    IntToUnsignedByte(
		sc.convert_to_int(getArrayValueAsDouble(inarray, inindex, intype)));
	}
      return;
    case SHORT:
    case UNSIGNED_SHORT:
      if(noscale)
	for(; outindex < endoutindex; outindex++,
	      inindex += inInc) {
	  // note - this truncates producing weird values
	  // for things out of range
	  ((short []) outarray)[outindex] = (short)
	    getArrayValueAsInt(inarray, inindex, intype);
	}
      else
	for(; outindex < endoutindex; outindex++,
	      inindex += inInc) {
	  // note - this truncates producing weird values for
	  // things out of range
	  ((short []) outarray)[outindex] = (short)
	    sc.convert_to_int(getArrayValueAsDouble(inarray, inindex,
						    intype));
	}
      return;
    case INTEGER:
      if(noscale)
	for(; outindex < endoutindex; outindex++,
	      inindex += inInc) {
	  ((int []) outarray)[outindex] =
	    getArrayValueAsInt(inarray, inindex, intype);
	}
      else
	for(; outindex < endoutindex; outindex++,
	      inindex += inInc) {
	  ((int []) outarray)[outindex] =
	    sc.convert_to_int(getArrayValueAsDouble(inarray, inindex, intype));
	}
      return;
    case UNSIGNED_INTEGER:
      if(noscale)
	for(; outindex < endoutindex; outindex++,
	      inindex += inInc) {
	  ((int []) outarray)[outindex] = (int)
	    getArrayValueAsLong(inarray, inindex, intype);
	}
      else
	for(; outindex < endoutindex; outindex++,
	      inindex += inInc) {
	  ((int []) outarray)[outindex] =
	    sc.convert_to_int(getArrayValueAsDouble(inarray, inindex, intype));
	}
      return;
    case LONG:
      if(noscale)
	for(; outindex < endoutindex; outindex++,
	    inindex += inInc) {
	  ((long []) outarray)[outindex] =
	    getArrayValueAsLong(inarray, inindex, intype);
	}
      else
	for(; outindex < endoutindex; outindex++,
	  inindex += inInc) {
	  ((long []) outarray)[outindex] = (long)
	    sc.convert_to_int(getArrayValueAsDouble(inarray,
			      inindex, intype));
        }
    return;
    case FLOAT:
      if(noscale)
	for(; outindex < endoutindex; outindex++,
	      inindex += inInc) {
	  ((float []) outarray)[outindex] = (float)
	    getArrayValueAsDouble(inarray, inindex, intype);
	}
      else
	for(; outindex < endoutindex; outindex++,
	      inindex += inInc) {
	  ((float []) outarray)[outindex] = (float)
	    sc.convert(getArrayValueAsDouble(inarray, inindex, intype));
	}
      return;
    case DOUBLE:
      if(noscale)
	for(; outindex < endoutindex; outindex++,
	      inindex += inInc) {
	  ((double []) outarray)[outindex] =
	    getArrayValueAsDouble(inarray, inindex,
				  intype);
	}
      else
	for(; outindex < endoutindex; outindex++,
	      inindex += inInc) {
	  ((double []) outarray)[outindex] =
	    sc.convert(getArrayValueAsDouble(inarray,
					     inindex, intype));
	}
      return;
    default:
      return;
    }
  }
  /**
   * Copies a multi-dimensional square region from one data array to
   * completely fill another.  The amount of data copied is governed
   * by the array of output dimensions.  The output array is filled
   * from the output offset with no word skipping.
   * Uses a recursive routine - calling this routine to copy bytes
   * for lower dimensions until lowest dimension (currentDim = 0)
   * is reached and then calling copyArray.
   *
   * @param inArray	input data array
   * @param inOffset	location of input array to begin copying from
   * @param inType	CNU data type of input array
   * @param inInc	array containing one increment for each dimension.
   *			The increments specify the number of words to the
   *			next input word for the same dimension.
   * @param outArray	output data array
   * @param outOffset	location of output array to begin copying to
   * @param outType	CNU data type of output array
   * @param outDims	array of output dimensions which governs the amount
   *			of data copied for each dimension
   * @param currentDim	index of dimension recursive routine is currently
   *			working with
   * @param sc		CNUScale class describing linear translation and
   *			scaling to apply to words copied.
   * @return		the offset to the next location in the output array.
   */ 
  public final static int copyRegion (Object inArray, int inOffset,
				      int inType, int[] inInc,
				      Object outArray, int outOffset,
				      int outType, int[] outDims,
				      int currentDim, CNUScale sc) {
    if(currentDim == 0) {
      copyArray(inArray, inOffset, inType, inInc[currentDim],
		outArray, outOffset, outType,
		outDims[currentDim], sc);
      return outDims[currentDim];
    } else {
      int wordsCopied = 0;
      for( int n = 0; n < outDims[currentDim];
	   n++, inOffset += inInc[currentDim]) {
	wordsCopied +=
	  copyRegion(inArray, inOffset, inType, inInc,
		     outArray, outOffset + wordsCopied, outType,
		     outDims, currentDim - 1, sc);
      }
      return wordsCopied;
    }
  }
  /**
   * Converts an array of primitive type to a bsh script to recreate the array.
   *
   * @param object array of primitive type
   * @return string representing the array
   */
  public final static String arrayToScript(Object obj) {
    String type = null;
    if(obj == null) return "null";
    else if(obj instanceof boolean[]) type="boolean[]";
    else if(obj instanceof char[]) type="char[]";
    else if(obj instanceof byte[]) type="byte[]";
    else if(obj instanceof short[]) type="short[]";
    else if(obj instanceof int[]) type="int[]";
    else if(obj instanceof long[]) type="long[]";
    else if(obj instanceof float[]) type="float[]";
    else if(obj instanceof double[]) type="double[]";
    else return "iiv.data.CNUTypes.arrayToScript--invalid-array=" + obj.toString();
    return "new " + type + arrayToString(obj);
  }
  /**
   * Converts an array of primitive type to a bracked delimited, comma seperated string.
   *
   * @param object array of primitive type
   * @return string representing the array
   */
  public final static String arrayToString(Object obj) {
    return arrayToString(obj, 0, -1);
  }
  /**
   * Converts an array of primitive type to a bracked delimited, comma seperated string.
   *
   * @param object array of primitive type
   * @param begin start location for printing values from the array
   * @param end end location for printing values from the array.  If less then 0 prints to end of array.
   * @return string representing the array
   */
  public final static String arrayToString(Object obj, int begin, int end) {
    if(obj == null) return ("null");
    StringBuffer sb = new StringBuffer();
    sb.append("{");
    if(obj instanceof boolean[]) {
      boolean[] array = (boolean[]) obj;
      if(end < 0) end = array.length - 1;
      for(int i=begin; i<=end; i++) {
	if(i != 0) sb.append(",");
	sb.append(array[i]);
      }
    }
    else if(obj instanceof char[]) {
      char[] array = (char[]) obj;
      if(end < 0) end = array.length - 1;
      for(int i=begin; i<=end; i++) {
	if(i != 0) sb.append(",");
	sb.append(array[i]);
      }
    }
    else if(obj instanceof byte[]) {
      byte[] array = (byte[]) obj;
      if(end < 0) end = array.length - 1;
      for(int i=begin; i<=end; i++) {
	if(i != 0) sb.append(",");
	sb.append(array[i]);
      }
    }
    else if(obj instanceof short[])  {
      short[] array = (short[]) obj;
      if(end < 0) end = array.length - 1;
      for(int i=begin; i<=end; i++) {
	if(i != 0) sb.append(",");
	sb.append(array[i]);
      }
    }
    else if(obj instanceof int[])  {
      int[] array = (int[]) obj;
      if(end < 0) end = array.length - 1;
      for(int i=begin; i<=end; i++) {
	if(i != 0) sb.append(",");
	sb.append(array[i]);
      }
    }
    else if(obj instanceof long[])  {
      long[] array = (long[]) obj;
      if(end < 0) end = array.length - 1;
      for(int i=begin; i<=end; i++) {
	if(i != 0) sb.append(",");
	sb.append(array[i]);
      }
    }
    else if(obj instanceof float[])  {
      float[] array = (float[]) obj;
      if(end < 0) end = array.length - 1;
      for(int i=begin; i<=end; i++) {
	if(i != 0) sb.append(",");
	sb.append(array[i]);
      }
    }
    else if(obj instanceof double[])  {
      double[] array = (double[]) obj;
      if(end < 0) end = array.length - 1;
      for(int i=begin; i<=end; i++) {
	if(i != 0) sb.append(",");
	sb.append(array[i]);
      }
    }
    else sb.append("iiv.data.CNUTypes.arrayToScript--invalid-array=").append(obj);
    sb.append("}");
    return sb.toString();
  }
}
