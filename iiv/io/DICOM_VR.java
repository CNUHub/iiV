package iiv.io;
import iiv.data.*;
import java.lang.*;
import java.io.*;
/**
 * DICOM_VR is a class to handle known DICOM Value Representations.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		DICOMImgFile
 */
public class DICOM_VR implements CNUTypesConstants {
  public static final DICOM_VR AE=new DICOM_VR(0x4145, "Application Entity", STRING);//16 bytes max
  public static final DICOM_VR AS=new DICOM_VR(0x4153, "Age String", STRING);//4 bytes fixed
  public static final DICOM_VR AT=new DICOM_VR(0x4154, "Attribute Tag", UNSIGNED_SHORT);
  public static final DICOM_VR CS=new DICOM_VR(0x4353, "Code String", STRING);
  public static final DICOM_VR DA=new DICOM_VR(0x4441, "Date", STRING);
  public static final DICOM_VR DS=new DICOM_VR(0x4453, "Decimal String", STRING);
  public static final DICOM_VR DT=new DICOM_VR(0x4454, "Date Time", STRING);
  public static final DICOM_VR FL=new DICOM_VR(0x464C, "Floating Point Single", FLOAT);
  public static final DICOM_VR FD=new DICOM_VR(0x4644, "Floating Point Double", DOUBLE);
  public static final DICOM_VR IS=new DICOM_VR(0x4953, "Integer String", STRING); //12 bytes max
  public static final DICOM_VR LO=new DICOM_VR(0x4C4F, "Long String", STRING); //64 chars max
  public static final DICOM_VR LT=new DICOM_VR(0x4C54, "Long Text", STRING);//10240 chars max
  public static final DICOM_VR OB=new DICOM_VR(0x4F42, "Other Byte String", BYTE);
  public static final DICOM_VR OW=new DICOM_VR(0x4F57, "Other Word String", SHORT);
  public static final DICOM_VR PN=new DICOM_VR(0x504E, "Person Name", STRING);//5 groups X 64max
  public static final DICOM_VR SH=new DICOM_VR(0x5348, "Short String", STRING);//16 chars max
  public static final DICOM_VR SL=new DICOM_VR(0x534C, "Signed Long", INTEGER);
  public static final DICOM_VR SQ=new DICOM_VR(0x5351, "Value Sequence", DICOM_DATA_ELEMENT);
  public static final DICOM_VR SS=new DICOM_VR(0x5353, "Signed Short", SHORT);
  public static final DICOM_VR ST=new DICOM_VR(0x5354, "Short Text", STRING);//1024 chars max
  public static final DICOM_VR TM=new DICOM_VR(0x544D, "Time", STRING);//16 bytes max
  public static final DICOM_VR UI=new DICOM_VR(0x5549, "Unique Identifier", STRING);//64 bytes max
  public static final DICOM_VR UL=new DICOM_VR(0x554C, "Unsigned Long", UNSIGNED_INTEGER);
  public static final DICOM_VR UN=new DICOM_VR(0x554E, "Unknown", BYTE);
  public static final DICOM_VR US=new DICOM_VR(0x5553, "Unsigned Short", UNSIGNED_SHORT);
  public static final DICOM_VR UT=new DICOM_VR(0x5554, "Unlimited Text", STRING);
  public static final DICOM_VR QQ=new DICOM_VR(0x3F3F, "", UNKNOWN);
  /** List of none value representations */
  private static final DICOM_VR vrList[]= {
    AE, AS, AT, CS, DA, DS, DT, FD, FL, IS, LO, LT, PN, SH, SL, SS, ST,
    TM, UI, UL, US, UT, OB, OW, SQ, UN, QQ
  };
  /**
   * Gets the static value representation from an input stream.
   *
   * @param dataInput input stream to get value representation from.
   * @exception throws an IllegalArgumentException if the value
   *                   is not a known vr value.
   */
  public static DICOM_VR getDICOM_VR(DataInput dataInput) throws IOException {
    //    return getDICOM_VR(dataInput.readUnsignedShort());
    return getDICOM_VR((dataInput.readUnsignedByte()<<8) | dataInput.readUnsignedByte());
  }
  /**
   * Gets the static value representation for a given value.
   *
   * @param vr value to get value representation for.
   * @exception throws an IllegalArgumentException if the value
   *                   is not a known vr value.
   */
  public static DICOM_VR getDICOM_VR(int vr) {
    for(int i=0; i < vrList.length; i++) if(vrList[i].vr == vr) return vrList[i];
    // return new DICOM_VR(vr, "unknown to all", BYTE);
    throw new IllegalArgumentException("invalid DICOM_VR=0x" + Integer.toHexString(vr));
  };

  private int vr;
  private String name;
  private int type = UNKNOWN;
  /**
   * Constructs a new instance of DICOM_VR.
   *
   * @param vr value
   */
  private DICOM_VR(int vr, String name, int type) {
    this.vr = vr & 0xFFFF; this.name = name; this.type = type;
  }
  /**
   * Gets the value representation name.
   *
   * @return name
   */
  public String getName() { return name; };
  /**
   * Gets the type used for internal storage of the value representation.
   *
   * @return type
   */
  public int getStorageType() { return type; };
  /**
   * Creates a string representation for this object.
   *
   * @return string respresentation
   */
  public String toString() {
    byte b[] = { (byte) (vr >>> 8), (byte) (vr & 0xFF) };
    return new String(b) + ":0x" + Integer.toHexString(vr) + '"' + name + "\":" + CNUTypes.typeToString(type);
  }
}
