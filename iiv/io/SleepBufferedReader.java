package iiv.io;
import java.io.*;
/**
 * Class extends BufferedReader avoiding blocking by sleeping until ready.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.CNUViewer
 * @since	iiV1.1
 */
public class SleepBufferedReader extends BufferedReader
  implements SleepReader {
  /**
   * Constructs a new instance of SleepBufferedReader.
   *
   * @param in	input reader
   * @param sz	size of buffer
   */
  public SleepBufferedReader( Reader in, int sz ) {
    super(in,  sz);
  }
  /**
   * Constructs a new instance of SleepBufferedReader.
   *
   * @param in	input reader
   */
  public SleepBufferedReader( Reader in ) {
    super(in);
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
  /**
   * Reads a line of characters.
   *
   * @return	read line
   * @exception IOException thrown on error reading
   */
  public String readLine() throws IOException {
    try {
      while( ! ready() ) Thread.sleep(100);
    } catch (InterruptedException ie) {
    }
    return super.readLine();
  }
}
