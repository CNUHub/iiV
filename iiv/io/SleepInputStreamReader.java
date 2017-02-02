package iiv.io;
import java.io.*;
/**
 * Class extends InputStreamReader avoiding blocking by sleeping
 * until ready.
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.CNUViewer
 * @since	iiV1.1
 */
public class SleepInputStreamReader extends InputStreamReader
  implements SleepReader {
  /**
   * Constructs a new instance of SleepInputStreamReader.
   *
   * @param in	input stream to work around
   */
  public SleepInputStreamReader( InputStream in ) {
    super(in);
  }
  /**
   * Constructs a new instance of SleepInputStreamReader.
   *
   * @param in		input stream to work around
   * @param enc		encoding
   * @exception UnsupportedEncodingException thrown on unsupported encoding
   */
  public SleepInputStreamReader( InputStream in, String enc )
    throws UnsupportedEncodingException {
    super(in,  enc);
  }
  /**
   * Reads a single character.
   *
   * @return	byte read
   * @exception IOException thrown on error reading
   */
  public int read() throws IOException {
    try {
      while( ! ready() ) Thread.sleep(100);
    } catch (InterruptedException ie) {
    }
    return super.read();
  }
  /**
   * Reads characters into a portion of an array.
   *
   * @param cbuf	buffer to read into
   * @param off		offset into buffer to start reading into
   * @param len		number of characters to read
   * @return	number of bytes read
   * @exception IOException thrown on error reading
   */
  public int read(char cbuf[], int off, int len) throws IOException {
    try {
      while( ! ready() ) Thread.sleep(100);
    } catch (InterruptedException ie) {
    }
    return super.read(cbuf, off, len);
  }
}
