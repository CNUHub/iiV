package iiv.io;
import iiv.data.*;
import java.io.*;

/**
 * Extends DataInputStream providing methods for reading foreign data types.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		AnalyzeHeader
 * @since	iiV1.0
 */
public class ConvertDataInputStream extends FilterInputStream
  implements CNUConversionTypes, DataInput {
  // this stream is not multi-thread save
  private CNUDataConversions cnuDataConv = new CNUDataConversions();
  private byte b[] = new byte[8];
  /**
   * Constructs a new instance.
   *
   * @param inputStream	InputStream
   */
  public ConvertDataInputStream( InputStream inputStream) {
    super((inputStream instanceof DataInputStream) ?
	  inputStream : new DataInputStream(inputStream) );
  }
  /**
   * Constructs a new instance with the given conversions.
   *
   * @param inputStream	InputStream
   * @param cnudc	data conversions
   */
  public ConvertDataInputStream( InputStream inputStream, 
				 CNUDataConversions cnudc) {
    super((inputStream instanceof DataInputStream) ?
	  inputStream : new DataInputStream(inputStream) );
    setCNUDataConversions(cnudc);
  }
  public void setCNUDataConversions(CNUDataConversions cnudc) {
    cnuDataConv = new CNUDataConversions(cnudc);
  }
  public CNUDataConversions getCNUDataConversions() {
    return new CNUDataConversions(cnuDataConv);
  }
  public void readFully(byte b[]) throws IOException {
    ( (DataInputStream) in).readFully(b);
  }
  public void readFully(byte b[], int off, int len) throws IOException {
    ( (DataInputStream) in).readFully(b, off, len);
  }
  public int skipBytes(int n) throws IOException {
    return ( (DataInputStream) in).skipBytes(n);
  }
  public boolean readBoolean() throws IOException {
    return ( (DataInputStream) in).readBoolean();
  }
  public byte readByte() throws IOException {
    return ( (DataInputStream) in).readByte();
  }
  public int readUnsignedByte() throws IOException {
    return ( (DataInputStream) in).readUnsignedByte();
  }
  public short readShort() throws IOException {
    ( (DataInputStream) in).readFully(b, 0, 2);
    return cnuDataConv.bytesToShort(b, 0);
  }
  public int readUnsignedShort() throws IOException {
    ( (DataInputStream) in).readFully(b, 0, 2);
    return cnuDataConv.bytesToUnsignedShort(b, 0);
  }
  public char readChar() throws IOException {
    return ( (DataInputStream) in).readChar();
  }
  public int readInt() throws IOException {
    ( (DataInputStream) in).readFully(b, 0, cnuDataConv.bytesPerInt());
    return cnuDataConv.bytesToInt(b, 0);
  }
  public long readLong() throws IOException {
    ( (DataInputStream) in).readFully(b, 0, 8);
    return cnuDataConv.bytesToLong(b, 0);
  }
  public float readFloat() throws IOException {
    ( (DataInputStream) in).readFully(b, 0, 4);
    return cnuDataConv.bytesToFloat(b, 0);
  }
  public double readDouble() throws IOException {
    ( (DataInputStream) in).readFully(b, 0, 8);
    return cnuDataConv.bytesToDouble(b, 0);
  }
  public String readLine() throws IOException {
    throw new IOException("readLine not implemented because deprecated");
  }
  public String readUTF() throws IOException {
    return ( (DataInputStream) in).readUTF();
  }
}
