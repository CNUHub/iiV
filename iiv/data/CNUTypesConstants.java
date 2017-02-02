package iiv.data;
/**
 * A utility interface for defining data type constants.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 */
public interface CNUTypesConstants {
  public final static int UNKNOWN = 0;
  public final static int BYTE = 1;
  public final static int UNSIGNED_BYTE = -1;
  public final static int SHORT = 2;
  public final static int UNSIGNED_SHORT = -2;
  public final static int INTEGER = 4;
  public final static int UNSIGNED_INTEGER = -4;
  public final static int LONG = 8;
  //	public final static int UNSIGNED_LONG = -8;
  public final static int FLOAT = 5;
  public final static int DOUBLE = 9;
  public final static int STRING = 11;

  // Special types that need to stay consistant with CNUTypes
  public static final int DICOM_DATA_ELEMENT = 101;

  public final static int[] DATA_TYPES = { BYTE, UNSIGNED_BYTE,
    SHORT, UNSIGNED_SHORT, INTEGER, LONG, FLOAT, DOUBLE };

  public final static int BYTE_MAX_VALUE = 0x7f;  // 127
  public final static int BYTE_MIN_VALUE = 0xffffff80;  // -128
  public final static int UNSIGNED_BYTE_MAX_VALUE = 0xff; // 255
  public final static int UNSIGNED_BYTE_MIN_VALUE = 0x0; // 0
  public final static int SHORT_MAX_VALUE = 0x7fff; // 32767
  public final static int SHORT_MIN_VALUE = 0xffff8000; // -32768
  public final static int UNSIGNED_SHORT_MAX_VALUE = 0xffff; // 65535
  public final static int UNSIGNED_SHORT_MIN_VALUE = 0x0;  // 0
  public final static long UNSIGNED_INTEGER_MAX_VALUE = 0xffffffff; 
							// 4294967295
  public final static long UNSIGNED_INTEGER_MIN_VALUE = 0x0; // 0
}
